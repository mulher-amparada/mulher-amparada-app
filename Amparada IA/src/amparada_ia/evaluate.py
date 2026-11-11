"""Avalia perda, perplexidade e cobertura do tokenizador."""
from __future__ import annotations
import argparse, math
from pathlib import Path
import torch
from .model import AmparadaLanguageModel
from .tokenizer import CharTokenizer
from .bpe_tokenizer import BPETokenizer
ROOT = Path(__file__).resolve().parents[2]

def main():
    p = argparse.ArgumentParser(description="Avalia um checkpoint da Amparada IA.")
    p.add_argument("--checkpoint", type=Path, default=ROOT / "runs" / "amparada_ia.pt")
    p.add_argument("--data", type=Path, default=ROOT / "data" / "validacao.txt")
    p.add_argument("--batch-size", type=int, default=16); p.add_argument("--batches", type=int, default=20)
    p.add_argument("--device", default="auto", choices=["auto", "cpu", "cuda"])
    a = p.parse_args()
    if not a.checkpoint.is_file() or not a.data.is_file(): p.error("Checkpoint ou corpus de validação ausente.")
    if a.batch_size < 1 or a.batches < 1: p.error("batch-size e batches precisam ser positivos.")
    if a.device == "cuda" and not torch.cuda.is_available(): p.error("CUDA indisponível.")
    device = torch.device("cuda" if a.device == "cuda" or (a.device == "auto" and torch.cuda.is_available()) else "cpu")
    ckpt = torch.load(a.checkpoint, map_location=device, weights_only=False)
    kind = ckpt.get("tokenizer_type", "char")
    tok = BPETokenizer.from_dict(ckpt["tokenizer"]) if kind == "bpe" else CharTokenizer.from_dict(ckpt["tokenizer"])
    model = AmparadaLanguageModel(**ckpt["model_config"]).to(device)
    model.load_state_dict(ckpt["model_state"]); model.eval()
    raw = a.data.read_text(encoding="utf-8"); ids = tok.encode(raw)
    if 0 in ids: p.error("Validação contém símbolos desconhecidos para o vocabulário de treino.")
    data = torch.tensor(ids, dtype=torch.long); block = model.config["block_size"]
    if data.numel() <= block + 1: p.error("Validação curta demais para o block-size.")
    losses = []
    with torch.no_grad():
        for _ in range(a.batches):
            starts = torch.randint(0, data.numel() - block - 1, (a.batch_size,))
            x = torch.stack([data[i:i+block] for i in starts]).to(device)
            y = torch.stack([data[i+1:i+block+1] for i in starts]).to(device)
            _, loss = model(x, y); losses.append(float(loss.item()))
    mean = sum(losses) / len(losses)
    try: ppl = math.exp(mean)
    except OverflowError: ppl = float("inf")
    print(f"Tokenizador: {kind} | vocabulário: {tok.vocab_size}")
    print(f"Caracteres no corpus: {len(raw)} | tokens após codificação: {len(ids)}")
    print(f"Perda média/token: {mean:.4f} | perplexidade/token: {ppl:.4f}")
    print(f"Bits por caractere (estimativa): {mean * len(ids) / max(1, len(raw)) / math.log(2):.4f}")
    print("Compare bits/caractere e taxa tokens/caractere; perplexidade/token isolada não é comparável entre tokenizadores.")

if __name__ == "__main__": main()
