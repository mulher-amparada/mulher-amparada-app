"""Treinamento do Transformer com tokenizador BPE e validação separada."""

from __future__ import annotations

import argparse
import random
from pathlib import Path

import torch

from .model import AmparadaLanguageModel
from .tokenizer import BPETokenizer

ROOT = Path(__file__).resolve().parents[2]


def encode_file(path: Path, tokenizer: BPETokenizer) -> torch.Tensor:
    if not path.is_file():
        raise FileNotFoundError(f"Arquivo não encontrado: {path}")
    text = path.read_text(encoding="utf-8")
    encoded = torch.tensor(tokenizer.encode(text), dtype=torch.long)
    unknown = int((encoded == 0).sum())
    if unknown:
        print(f"Aviso: {path} contém {unknown} tokens desconhecidos.")
    return encoded


def get_batch(data: torch.Tensor, batch_size: int, block_size: int, device: torch.device):
    if data.numel() <= block_size + 1:
        raise ValueError(
            f"O corpus precisa ter mais de {block_size + 1} tokens; recebidos: {data.numel()}."
        )
    starts = torch.randint(0, data.numel() - block_size - 1, (batch_size,))
    x = torch.stack([data[start : start + block_size] for start in starts])
    y = torch.stack([data[start + 1 : start + block_size + 1] for start in starts])
    return x.to(device), y.to(device)


@torch.no_grad()
def estimate_loss(model, train_data, validation_data, batch_size, block_size, device, batches):
    model.eval()
    result = {}
    for name, data in (("treino", train_data), ("validação", validation_data)):
        losses = []
        for _ in range(batches):
            x, y = get_batch(data, batch_size, block_size, device)
            _, loss = model(x, y)
            losses.append(float(loss.item()))
        result[name] = sum(losses) / len(losses)
    model.train()
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description="Treina a Amparada IA com BPE.")
    parser.add_argument("--data", type=Path, default=ROOT / "data" / "treino.txt")
    parser.add_argument("--validation-data", type=Path, default=ROOT / "data" / "validacao.txt")
    parser.add_argument("--output", type=Path, default=ROOT / "runs" / "amparada_ia_bpe.pt")
    parser.add_argument("--steps", type=int, default=2000)
    parser.add_argument("--batch-size", type=int, default=4)
    parser.add_argument("--block-size", type=int, default=96)
    parser.add_argument("--n-embd", type=int, default=64)
    parser.add_argument("--n-head", type=int, default=4)
    parser.add_argument("--n-layer", type=int, default=2)
    parser.add_argument("--vocab-size", type=int, default=512)
    parser.add_argument("--learning-rate", type=float, default=3e-4)
    parser.add_argument("--eval-interval", type=int, default=100)
    parser.add_argument("--eval-batches", type=int, default=5)
    parser.add_argument("--device", default="auto", choices=["auto", "cpu", "cuda"])
    args = parser.parse_args()

    for name in ("steps", "batch_size", "block_size", "n_embd", "n_head", "n_layer", "vocab_size", "eval_interval", "eval_batches"):
        if getattr(args, name) < 1:
            parser.error(f"--{name.replace('_', '-')} precisa ser positivo.")
    if args.n_embd % args.n_head:
        parser.error("--n-embd precisa ser divisível por --n-head.")
    if args.device == "cuda" and not torch.cuda.is_available():
        parser.error("CUDA foi solicitada, mas não está disponível nesta instalação do PyTorch.")

    device = torch.device(
        "cuda" if args.device == "cuda" or (args.device == "auto" and torch.cuda.is_available()) else "cpu"
    )
    random.seed(42)
    torch.manual_seed(42)

    train_text = args.data.read_text(encoding="utf-8")
    tokenizer = BPETokenizer.from_text(train_text, vocab_size=args.vocab_size)
    train_data = torch.tensor(tokenizer.encode(train_text), dtype=torch.long)
    validation_data = encode_file(args.validation_data, tokenizer)

    if train_data.numel() <= args.block_size + 1:
        parser.error("Corpus de treino pequeno demais para o block-size escolhido.")
    if validation_data.numel() <= args.block_size + 1:
        parser.error("Corpus de validação pequeno demais para o block-size escolhido.")

    model = AmparadaLanguageModel(
        vocab_size=tokenizer.vocab_size,
        block_size=args.block_size,
        n_embd=args.n_embd,
        n_head=args.n_head,
        n_layer=args.n_layer,
    ).to(device)
    optimizer = torch.optim.AdamW(model.parameters(), lr=args.learning_rate)

    print(f"Dispositivo: {device}")
    print(f"Tokens de treino: {train_data.numel()} | validação: {validation_data.numel()}")
    print(f"Vocabulário BPE: {tokenizer.vocab_size} | parâmetros: {sum(p.numel() for p in model.parameters()):,}")

    for step in range(args.steps):
        model.train()
        x, y = get_batch(train_data, args.batch_size, args.block_size, device)
        _, loss = model(x, y)
        optimizer.zero_grad(set_to_none=True)
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
        optimizer.step()

        if (step + 1) % args.eval_interval == 0 or step == 0 or step + 1 == args.steps:
            metrics = estimate_loss(
                model, train_data, validation_data, args.batch_size,
                args.block_size, device, args.eval_batches,
            )
            print(
                f"etapa {step + 1}/{args.steps} | "
                f"perda treino: {metrics['treino']:.4f} | "
                f"perda validação: {metrics['validação']:.4f}"
            )

    args.output.parent.mkdir(parents=True, exist_ok=True)
    torch.save(
        {
            "model_state": model.state_dict(),
            "model_config": model.config,
            "tokenizer": tokenizer.to_dict(),
            "training_info": {
                "steps": args.steps,
                "train_tokens": int(train_data.numel()),
                "validation_tokens": int(validation_data.numel()),
                "seed": 42,
            },
        },
        args.output,
    )
    print(f"Checkpoint BPE salvo em: {args.output}")


if __name__ == "__main__":
    main()
