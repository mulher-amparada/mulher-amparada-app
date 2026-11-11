"""Avalia a perda média em um corpus de validação separado."""

from __future__ import annotations

import argparse
import math
from pathlib import Path

import torch

from .model import AmparadaLanguageModel
from .tokenizer import CharTokenizer


ROOT = Path(__file__).resolve().parents[2]


def main() -> None:
    parser = argparse.ArgumentParser(description="Avalia um checkpoint da Amparada IA.")
    parser.add_argument("--checkpoint", type=Path, default=ROOT / "runs" / "amparada_ia.pt")
    parser.add_argument("--data", type=Path, default=ROOT / "data" / "validacao.txt")
    parser.add_argument("--batch-size", type=int, default=16)
    parser.add_argument("--batches", type=int, default=20)
    parser.add_argument("--device", default="auto", choices=["auto", "cpu", "cuda"])
    args = parser.parse_args()

    if not args.checkpoint.is_file():
        parser.error("Checkpoint não encontrado; treine o modelo primeiro.")
    if not args.data.is_file():
        parser.error("Arquivo de validação não encontrado.")
    if args.batch_size < 1 or args.batches < 1:
        parser.error("--batch-size e --batches precisam ser positivos.")
    if args.device == "cuda" and not torch.cuda.is_available():
        parser.error("CUDA foi solicitada, mas não está disponível.")

    device = torch.device(
        "cuda" if args.device == "cuda" or (args.device == "auto" and torch.cuda.is_available()) else "cpu"
    )
    checkpoint = torch.load(args.checkpoint, map_location=device, weights_only=False)
    tokenizer = CharTokenizer.from_dict(checkpoint["tokenizer"])
    model = AmparadaLanguageModel(**checkpoint["model_config"]).to(device)
    model.load_state_dict(checkpoint["model_state"])
    model.eval()

    text = args.data.read_text(encoding="utf-8")
    encoded = torch.tensor(tokenizer.encode(text), dtype=torch.long)
    block_size = model.config["block_size"]
    if encoded.numel() <= block_size + 1:
        parser.error("O corpus de validação precisa ter mais caracteres que block_size + 1.")
    if (encoded == 0).any():
        parser.error("O corpus contém caracteres ausentes do vocabulário usado no treinamento.")

    losses: list[float] = []
    with torch.no_grad():
        for _ in range(args.batches):
            starts = torch.randint(0, encoded.numel() - block_size - 1, (args.batch_size,))
            x = torch.stack([encoded[i : i + block_size] for i in starts]).to(device)
            y = torch.stack([encoded[i + 1 : i + block_size + 1] for i in starts]).to(device)
            _, loss = model(x, y)
            losses.append(float(loss.item()))

    mean_loss = sum(losses) / len(losses)
    try:
        perplexity = math.exp(mean_loss)
    except OverflowError:
        perplexity = float("inf")
    print(f"Perda média: {mean_loss:.4f}")
    print(f"Perplexidade aproximada: {perplexity:.4f}")
    print("Interprete essas métricas junto com avaliação qualitativa; elas não medem segurança nem utilidade.")


if __name__ == "__main__":
    main()
