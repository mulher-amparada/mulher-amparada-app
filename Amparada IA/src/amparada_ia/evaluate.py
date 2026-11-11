"""Avalia a perda média de um checkpoint BPE em validação."""

from __future__ import annotations

import argparse
import math
from pathlib import Path

import torch

from .model import AmparadaLanguageModel
from .tokenizer import BPETokenizer

ROOT = Path(__file__).resolve().parents[2]


def main() -> None:
    parser = argparse.ArgumentParser(description="Avalia um checkpoint BPE.")
    parser.add_argument("--checkpoint", type=Path, default=ROOT / "runs" / "amparada_ia_bpe.pt")
    parser.add_argument("--data", type=Path, default=ROOT / "data" / "validacao.txt")
    parser.add_argument("--batch-size", type=int, default=4)
    parser.add_argument("--batches", type=int, default=20)
    parser.add_argument("--device", default="auto", choices=["auto", "cpu", "cuda"])
    args = parser.parse_args()

    if not args.checkpoint.is_file():
        parser.error("Checkpoint BPE não encontrado; treine o modelo primeiro.")
    if not args.data.is_file():
        parser.error("Arquivo de validação não encontrado.")
    if args.batch_size < 1 or args.batches < 1:
        parser.error("--batch-size e --batches precisam ser positivos.")

    device = torch.device(
        "cuda" if args.device == "cuda" or (args.device == "auto" and torch.cuda.is_available()) else "cpu"
    )
    checkpoint = torch.load(args.checkpoint, map_location=device, weights_only=False)
    if checkpoint.get("tokenizer", {}).get("type") != "bpe":
        parser.error("O checkpoint não usa BPE.")
    tokenizer = BPETokenizer.from_dict(checkpoint["tokenizer"])
    model = AmparadaLanguageModel(**checkpoint["model_config"]).to(device)
    model.load_state_dict(checkpoint["model_state"])
    model.eval()

    text = args.data.read_text(encoding="utf-8")
    encoded = torch.tensor(tokenizer.encode(text), dtype=torch.long)
    block_size = model.config["block_size"]
    if encoded.numel() <= block_size + 1:
        parser.error("O corpus de validação é pequeno demais para o block-size.")
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
    print("Essas métricas não medem compreensão, factualidade ou segurança.")


if __name__ == "__main__":
    main()
