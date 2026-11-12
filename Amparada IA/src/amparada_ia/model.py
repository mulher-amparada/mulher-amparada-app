"""Pequeno Transformer Decoder-only treinado para prever o próximo token BPE."""

from __future__ import annotations

import torch
from torch import nn
from torch.nn import functional as F


class CausalBlock(nn.Module):
    def __init__(self, n_embd: int, n_head: int, dropout: float) -> None:
        super().__init__()
        self.norm1 = nn.LayerNorm(n_embd)
        self.attention = nn.MultiheadAttention(
            embed_dim=n_embd,
            num_heads=n_head,
            dropout=dropout,
            batch_first=True,
        )
        self.norm2 = nn.LayerNorm(n_embd)
        self.feed_forward = nn.Sequential(
            nn.Linear(n_embd, 4 * n_embd),
            nn.GELU(),
            nn.Linear(4 * n_embd, n_embd),
            nn.Dropout(dropout),
        )
        self.dropout = nn.Dropout(dropout)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        sequence_length = x.size(1)
        # True na máscara significa que a posição não pode ser observada.
        causal_mask = torch.triu(
            torch.ones(
                sequence_length,
                sequence_length,
                device=x.device,
                dtype=torch.bool,
            ),
            diagonal=1,
        )
        normalized = self.norm1(x)
        attended, _ = self.attention(
            normalized,
            normalized,
            normalized,
            attn_mask=causal_mask,
            need_weights=False,
        )
        x = x + self.dropout(attended)
        x = x + self.feed_forward(self.norm2(x))
        return x


class AmparadaLanguageModel(nn.Module):
    """Modelo de linguagem pequeno, treinado para prever o próximo token."""

    def __init__(
        self,
        vocab_size: int,
        block_size: int = 64,
        n_embd: int = 128,
        n_head: int = 4,
        n_layer: int = 2,
        dropout: float = 0.1,
    ) -> None:
        super().__init__()
        if n_embd % n_head != 0:
            raise ValueError("n_embd precisa ser divisível por n_head")

        self.config = {
            "vocab_size": vocab_size,
            "block_size": block_size,
            "n_embd": n_embd,
            "n_head": n_head,
            "n_layer": n_layer,
            "dropout": dropout,
        }
        self.token_embedding = nn.Embedding(vocab_size, n_embd)
        self.position_embedding = nn.Embedding(block_size, n_embd)
        self.blocks = nn.Sequential(
            *[CausalBlock(n_embd, n_head, dropout) for _ in range(n_layer)]
        )
        self.final_norm = nn.LayerNorm(n_embd)
        self.language_head = nn.Linear(n_embd, vocab_size, bias=False)

    def forward(
        self,
        token_ids: torch.Tensor,
        targets: torch.Tensor | None = None,
    ) -> tuple[torch.Tensor, torch.Tensor | None]:
        batch_size, sequence_length = token_ids.shape
        if sequence_length > self.config["block_size"]:
            raise ValueError("Sequência maior que block_size")

        positions = torch.arange(sequence_length, device=token_ids.device)
        x = self.token_embedding(token_ids) + self.position_embedding(positions)
        x = self.blocks(x)
        logits = self.language_head(self.final_norm(x))

        loss = None
        if targets is not None:
            loss = F.cross_entropy(
                logits.reshape(-1, logits.size(-1)),
                targets.reshape(-1),
            )
        return logits, loss

    @torch.no_grad()
    def generate(
        self,
        token_ids: torch.Tensor,
        max_new_tokens: int = 200,
        temperature: float = 1.0,
    ) -> torch.Tensor:
        if temperature <= 0:
            raise ValueError("temperature precisa ser maior que zero")

        self.eval()
        for _ in range(max_new_tokens):
            context = token_ids[:, -self.config["block_size"] :]
            logits, _ = self(context)
            next_token_logits = logits[:, -1, :] / temperature
            probabilities = F.softmax(next_token_logits, dim=-1)
            next_token = torch.multinomial(probabilities, num_samples=1)
            token_ids = torch.cat((token_ids, next_token), dim=1)
        return token_ids
