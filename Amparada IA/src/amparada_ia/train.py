"""Treinamento didático do Transformer com previsão do próximo caractere."""

from __future__ import annotations

import argparse
import random
from pathlib import Path

import torch

from .model import AmparadaLanguageModel
from .tokenizer import CharTokenizer


ROOT = Path(__file__).resolve().parents[2]


def get_batch(
    data: torch.Tensor,
    batch_size: int,
    block_size: int,
    device: torch.device,
) -> tuple[torch.Tensor, torch.Tensor]:
    if data.numel() <= block_size + 1:
        raise ValueError("O corpus precisa ter mais caracteres que block_size + 1")
    starts = torch.randint(0, data.numel() - block_size - 1, (batch_size,))
    x = torch.stack([data[start : start + block_size] for start in starts])
    y = torch.stack([data[start + 1 : start + block_size + 1] for start in starts])
    return x.to(device), y.to(device)


def main() -> None:
    parser = argparse.ArgumentParser(description="Treina a Amparada IA experimental.")
    parser.add_argument("--data", type=Path, default=ROOT / "data" / "treino.txt")
    parser.add_argument("--output", type=Path, default=ROOT / "runs" / "amparada_ia.pt")
    parser.add_argument("--steps", type=int, default=500)
    parser.add_argument("--batch-size", type=int, default=16)
    parser.add_argument("--block-size", type=int, default=64)
    parser.add_argument("--learning-rate", type=float, default=3e-4)
    parser.add_argument("--device", default="auto", choices=["auto", "cpu", "cuda"])
    args = parser.parse_args()

    if args.steps < 1:
        parser.error("--steps precisa ser pelo menos 1")
    if args.batch_size < 1:
        parser.error("--batch-size precisa ser pelo menos 1")

    if args.device == "cuda" and not torch.cuda.is_available():
        parser.error("CUDA foi solicitada, mas não está disponível nesta instalação do PyTorch")
    device = torch.device(
        "cuda" if args.device == "cuda" or (args.device == "auto" and torch.cuda.is_available()) else "cpu"
    )

    random.seed(42)
    torch.manual_seed(42)
    if torch.cuda.is_available():
        torch.cuda.manual_seed_all(42)

    text = args.data.read_text(encoding="utf-8")
    tokenizer = CharTokenizer.from_text(text)
    encoded = torch.tensor(tokenizer.encode(text), dtype=torch.long)
    split = max(args.block_size + 2, int(len(encoded) * 0.9))
    if split >= len(encoded):
        parser.error("Corpus muito pequeno para block_size escolhido; adicione texto de treinamento.")
    train_data = encoded[:split]
    validation_data = encoded[split:]

    model = AmparadaLanguageModel(
        vocab_size=tokenizer.vocab_size,
        block_size=args.block_size,
    ).to(device)
    optimizer = torch.optim.AdamW(model.parameters(), lr=args.learning_rate)

    for step in range(args.steps):
        model.train()
        x, y = get_batch(train_data, args.batch_size, args.block_size, device)
        _, loss = model(x, y)
        optimizer.zero_grad(set_to_none=True)
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
        optimizer.step()

        if (step + 1) % 50 == 0 or step == 0:
            message = f"etapa {step + 1}/{args.steps} | perda treino: {loss.item():.4f}"
            if validation_data.numel() > args.block_size + 1:
                model.eval()
                with torch.no_grad():
                    vx, vy = get_batch(
                        validation_data, args.batch_size, args.block_size, device
                    )
                    _, validation_loss = model(vx, vy)
                message += f" | perda validação: {validation_loss.item():.4f}"
            print(message)

    args.output.parent.mkdir(parents=True, exist_ok=True)
    torch.save(
        {
            "model_state": model.state_dict(),
            "model_config": model.config,
            "tokenizer": tokenizer.to_dict(),
        },
        args.output,
    )
    print(f"Checkpoint salvo em: {args.output}")


if __name__ == "__main__":
    main()
