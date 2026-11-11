"""Treinamento do Transformer com tokenização por caractere ou BPE."""
from __future__ import annotations
import argparse, random
from pathlib import Path
import torch
from .model import AmparadaLanguageModel
from .tokenizer import CharTokenizer
from .bpe_tokenizer import BPETokenizer
ROOT=Path(__file__).resolve().parents[2]

def get_batch(data,batch_size,block_size,device):
    if data.numel()<=block_size+1: raise ValueError("Corpus precisa ter mais tokens que block_size + 1")
    starts=torch.randint(0,data.numel()-block_size-1,(batch_size,))
    x=torch.stack([data[i:i+block_size] for i in starts]);y=torch.stack([data[i+1:i+block_size+1] for i in starts])
    return x.to(device),y.to(device)

def main():
    p=argparse.ArgumentParser(description="Treina a Amparada IA experimental.")
    p.add_argument("--data",type=Path,default=ROOT/"data"/"treino.txt")
    p.add_argument("--output",type=Path,default=ROOT/"runs"/"amparada_ia.pt")
    p.add_argument("--steps",type=int,default=500);p.add_argument("--batch-size",type=int,default=16)
    p.add_argument("--block-size",type=int,default=64);p.add_argument("--learning-rate",type=float,default=3e-4)
    p.add_argument("--device",default="auto",choices=["auto","cpu","cuda"])
    p.add_argument("--tokenizer",choices=["char","bpe"],default="char")
    p.add_argument("--vocab-size",type=int,default=256)
    a=p.parse_args()
    if a.steps<1 or a.batch_size<1 or a.block_size<2:p.error("steps/batch-size positivos e block-size >= 2")
    if a.device=="cuda" and not torch.cuda.is_available():p.error("CUDA solicitada, mas indisponível")
    device=torch.device("cuda" if a.device=="cuda" or (a.device=="auto" and torch.cuda.is_available()) else "cpu")
    random.seed(42);torch.manual_seed(42)
    if torch.cuda.is_available():torch.cuda.manual_seed_all(42)
    raw=a.data.read_text(encoding="utf-8")
    cut=max(1,int(len(raw)*0.9));train_text,internal_val_text=raw[:cut],raw[cut:]
    if a.tokenizer=="bpe":tok=BPETokenizer.train(train_text,a.vocab_size,alphabet_text=raw)
    else:tok=CharTokenizer.from_text(train_text)
    train_ids=torch.tensor(tok.encode(train_text),dtype=torch.long)
    val_ids=torch.tensor(tok.encode(internal_val_text),dtype=torch.long)
    if 0 in train_ids.tolist() or 0 in val_ids.tolist():p.error("Token desconhecido no corpus; revise alfabeto do tokenizador.")
    if train_ids.numel()<=a.block_size+1:p.error("Corpus de treino muito pequeno para block-size.")
    model=AmparadaLanguageModel(vocab_size=tok.vocab_size,block_size=a.block_size).to(device)
    opt=torch.optim.AdamW(model.parameters(),lr=a.learning_rate)
    for step in range(a.steps):
        model.train();x,y=get_batch(train_ids,a.batch_size,a.block_size,device);_,loss=model(x,y)
        opt.zero_grad(set_to_none=True);loss.backward();torch.nn.utils.clip_grad_norm_(model.parameters(),1.0);opt.step()
        if (step+1)%50==0 or step==0:
            msg=f"etapa {step+1}/{a.steps} | perda treino: {loss.item():.4f}"
            if val_ids.numel()>a.block_size+1:
                model.eval()
                with torch.no_grad():vx,vy=get_batch(val_ids,a.batch_size,a.block_size,device);_,vl=model(vx,vy)
                msg+=f" | perda validação: {vl.item():.4f}"
            print(msg)
    a.output.parent.mkdir(parents=True,exist_ok=True)
    torch.save({"model_state":model.state_dict(),"model_config":model.config,
                "tokenizer":tok.to_dict(),"tokenizer_type":a.tokenizer},a.output)
    print(f"Checkpoint salvo: {a.output} | tokenizer={a.tokenizer} | vocab={tok.vocab_size}")
if __name__=="__main__":main()
