"""Gera texto usando um checkpoint BPE treinado."""

from __future__ import annotations

import argparse
from pathlib import Path

import torch

from .model import AmparadaLanguageModel
from .tokenizer import BPETokenizer

ROOT = Path(__file__).resolve().parents[2]


def main() -> None:
    parser = argparse.ArgumentParser(description="Gera texto com a Amparada IA BPE.")
    parser.add_argument("--checkpoint", type=Path, default=ROOT / "runs" / "amparada_ia_bpe.pt")
    parser.add_argument("--prompt", default="Tecnologia responsável")
    parser.add_argument("--tokens", type=int, default=200)
    parser.add_argument("--temperature", type=float, default=0.8)
    parser.add_argument("--device", default="auto", choices=["auto", "cpu", "cuda"])
    args = parser.parse_args()

    if not args.checkpoint.is_file():
        parser.error("Checkpoint BPE não encontrado. Execute primeiro o módulo train.")
    if args.tokens < 1:
        parser.error("--tokens precisa ser pelo menos 1")
    if args.device == "cuda" and not torch.cuda.is_available():
        parser.error("CUDA foi solicitada, mas não está disponível nesta instalação do PyTorch")
    device = torch.device(
        "cuda" if args.device == "cuda" or (args.device == "auto" and torch.cuda.is_available()) else "cpu"
    )

    checkpoint = torch.load(args.checkpoint, map_location=device, weights_only=False)
    if checkpoint.get("tokenizer", {}).get("type") != "bpe":
        parser.error("Este checkpoint não usa BPE. Treine novamente com a versão atual.")
    tokenizer = BPETokenizer.from_dict(checkpoint["tokenizer"])
    model = AmparadaLanguageModel(**checkpoint["model_config"]).to(device)
    model.load_state_dict(checkpoint["model_state"])
    model.eval()

    prompt_ids = tokenizer.encode(args.prompt) or [0]
    context = torch.tensor([prompt_ids], dtype=torch.long, device=device)
    output = model.generate(
        context, max_new_tokens=args.tokens, temperature=args.temperature
    )
    print(tokenizer.decode(output[0].tolist()))


if __name__ == "__main__":
    main()
