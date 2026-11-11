"""Gera texto com checkpoint treinado localmente."""
from __future__ import annotations
import argparse
from pathlib import Path
import torch
from .model import AmparadaLanguageModel
from .tokenizer import CharTokenizer
from .bpe_tokenizer import BPETokenizer
ROOT = Path(__file__).resolve().parents[2]

def main():
    parser = argparse.ArgumentParser(description="Gera texto com a Amparada IA.")
    parser.add_argument("--checkpoint", type=Path, default=ROOT / "runs" / "amparada_ia.pt")
    parser.add_argument("--prompt", default="Tecnologia responsável")
    parser.add_argument("--tokens", type=int, default=100)
    parser.add_argument("--temperature", type=float, default=0.8)
    parser.add_argument("--device", default="auto", choices=["auto", "cpu", "cuda"])
    args = parser.parse_args()
    if not args.checkpoint.is_file(): parser.error("Checkpoint não encontrado; treine primeiro.")
    if args.tokens < 1 or args.temperature <= 0: parser.error("tokens e temperature precisam ser positivos.")
    if args.device == "cuda" and not torch.cuda.is_available(): parser.error("CUDA indisponível.")
    device = torch.device("cuda" if args.device == "cuda" or (args.device == "auto" and torch.cuda.is_available()) else "cpu")
    # Carregue apenas checkpoints locais produzidos pelo treinamento deste projeto.
    ckpt = torch.load(args.checkpoint, map_location=device, weights_only=False)
    kind = ckpt.get("tokenizer_type", "char")
    tokenizer = BPETokenizer.from_dict(ckpt["tokenizer"]) if kind == "bpe" else CharTokenizer.from_dict(ckpt["tokenizer"])
    model = AmparadaLanguageModel(**ckpt["model_config"]).to(device)
    model.load_state_dict(ckpt["model_state"]); model.eval()
    ids = tokenizer.encode(args.prompt) or [1]
    output = model.generate(torch.tensor([ids], dtype=torch.long, device=device),
                            max_new_tokens=args.tokens, temperature=args.temperature)
    print(tokenizer.decode(output[0].tolist()))

if __name__ == "__main__": main()
