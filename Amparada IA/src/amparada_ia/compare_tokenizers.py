"""Treina dois modelos e compara BPE vs caractere no mesmo corpus de validação."""
from __future__ import annotations
import argparse, subprocess, sys, math
from pathlib import Path
import torch
from .model import AmparadaLanguageModel
from .tokenizer import CharTokenizer
from .bpe_tokenizer import BPETokenizer
ROOT=Path(__file__).resolve().parents[2]

def measure(checkpoint,data_path,batches,device):
    ck=torch.load(checkpoint,map_location=device,weights_only=False)
    tok=BPETokenizer.from_dict(ck["tokenizer"]) if ck.get("tokenizer_type")=="bpe" else CharTokenizer.from_dict(ck["tokenizer"])
    raw=data_path.read_text(encoding="utf-8"); ids=tok.encode(raw)
    if 0 in ids: raise ValueError("A validação contém caracteres fora do alfabeto do tokenizador.")
    model=AmparadaLanguageModel(**ck["model_config"]).to(device);model.load_state_dict(ck["model_state"]);model.eval()
    block=model.config["block_size"]
    if len(ids)<=block+1: raise ValueError("Corpus de validação curto demais.")
    values=[]
    with torch.no_grad():
        for k in range(batches):
            start=(k*block)%(len(ids)-block-1)
            x=torch.tensor([ids[start:start+block]],dtype=torch.long,device=device)
            y=torch.tensor([ids[start+1:start+block+1]],dtype=torch.long,device=device)
            _,loss=model(x,y);values.append(float(loss))
    loss=sum(values)/len(values)
    return {"kind":ck.get("tokenizer_type","char"),"vocab":tok.vocab_size,"chars":len(raw),
            "tokens":len(ids),"chars_per_token":len(raw)/max(1,len(ids)),
            "context_chars":block*len(raw)/max(1,len(ids)),"loss":loss,
            "ppl":math.exp(loss) if loss<80 else float("inf"),
            "bits_char":loss*len(ids)/max(1,len(raw))/math.log(2)}

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("--steps",type=int,default=500);p.add_argument("--block-size",type=int,default=64)
    p.add_argument("--vocab-size",type=int,default=256);p.add_argument("--batches",type=int,default=20)
    p.add_argument("--device",default="cpu",choices=["cpu","cuda"]);a=p.parse_args()
    if a.device=="cuda" and not torch.cuda.is_available():p.error("CUDA indisponível.")
    runs=ROOT/"runs";runs.mkdir(exist_ok=True);device=torch.device(a.device)
    for kind in ("char","bpe"):
        ckpt=runs/f"amparada_{kind}.pt"
        subprocess.run([sys.executable,"-m","src.amparada_ia.train","--tokenizer",kind,
                        "--output",str(ckpt),"--steps",str(a.steps),"--block-size",str(a.block_size),
                        "--vocab-size",str(a.vocab_size),"--device",a.device],cwd=ROOT,check=True)
    validation=ROOT/"data"/"validacao.txt"
    results=[measure(runs/f"amparada_{kind}.pt",validation,a.batches,device) for kind in ("char","bpe")]
    print("\n| Tokenizador | Vocabulário | Tokens val. | caracteres/token | contexto aprox. (caracteres) | PPL/token | bits/caractere |")
    print("|---|---:|---:|---:|---:|---:|---:|")
    for x in results:
        print(f"| {x['kind']} | {x['vocab']} | {x['tokens']} | {x['chars_per_token']:.3f} | {x['context_chars']:.1f} | {x['ppl']:.3f} | {x['bits_char']:.4f} |")
    print("\nA perplexidade/token não é comparável isoladamente entre vocabulários. Use bits/caractere como referência mais justa; contexto em caracteres é estimado pela média global.")
if __name__=="__main__":main()
