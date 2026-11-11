"""Tokenizador BPE simples, sem dependências externas."""

from __future__ import annotations

import re
from collections import Counter
from dataclasses import dataclass

# Mantém palavras, espaços e pontuação como unidades iniciais separadas.
_PATTERN = re.compile(r"\w+|[^\w\s]+|\s+", re.UNICODE)


@dataclass
class BPETokenizer:
    """BPE treinado no próprio corpus; IDs são estáveis dentro do checkpoint."""

    itos: list[str]
    merges: list[tuple[str, str]]

    @classmethod
    def from_text(cls, text: str, vocab_size: int = 512) -> "BPETokenizer":
        if vocab_size < 16:
            raise ValueError("vocab_size precisa ser pelo menos 16.")

        words = Counter(tuple(piece) for piece in _PATTERN.findall(text) if piece)
        symbols = {"<unk>"}
        for word in words:
            symbols.update(word)

        # BPE aprende pares frequentes ponderados pela frequência de cada palavra.
        merges: list[tuple[str, str]] = []
        while len(symbols) < vocab_size:
            pairs: Counter[tuple[str, str]] = Counter()
            for word, frequency in words.items():
                for pair in zip(word, word[1:]):
                    pairs[pair] += frequency

            if not pairs:
                break

            pair, frequency = pairs.most_common(1)[0]
            if frequency < 2:
                break

            left, right = pair
            merged = left + right
            updated = Counter()
            for word, count in words.items():
                result = []
                i = 0
                while i < len(word):
                    if i + 1 < len(word) and word[i] == left and word[i + 1] == right:
                        result.append(merged)
                        i += 2
                    else:
                        result.append(word[i])
                        i += 1
                updated[tuple(result)] += count

            words = updated
            merges.append(pair)
            symbols.add(merged)

        # IDs determinísticos: <unk>, caracteres e depois as fusões aprendidas.
        base = sorted({ch for piece in _PATTERN.findall(text) for ch in piece})
        itos = ["<unk>"] + base
        for left, right in merges:
            merged = left + right
            if merged not in itos:
                itos.append(merged)
        return cls(itos=itos, merges=merges)

    @property
    def vocab_size(self) -> int:
        return len(self.itos)

    def encode(self, text: str) -> list[int]:
        stoi = {token: i for i, token in enumerate(self.itos)}
        merge_rank = {pair: rank for rank, pair in enumerate(self.merges)}
        output: list[int] = []

        for piece in _PATTERN.findall(text):
            tokens = list(piece)
            while len(tokens) > 1:
                candidates = [
                    (merge_rank[(tokens[i], tokens[i + 1])], i)
                    for i in range(len(tokens) - 1)
                    if (tokens[i], tokens[i + 1]) in merge_rank
                ]
                if not candidates:
                    break
                _, index = min(candidates)
                tokens[index:index + 2] = [tokens[index] + tokens[index + 1]]

            output.extend(stoi.get(token, 0) for token in tokens)
        return output

    def decode(self, token_ids: list[int]) -> str:
        return "".join(
            self.itos[token_id]
            if 0 <= token_id < len(self.itos) and token_id != 0
            else "�"
            for token_id in token_ids
        )

    def to_dict(self) -> dict:
        return {"type": "bpe", "itos": self.itos, "merges": [list(pair) for pair in self.merges]}

    @classmethod
    def from_dict(cls, data: dict) -> "BPETokenizer":
        return cls(
            itos=data["itos"],
            merges=[tuple(pair) for pair in data["merges"]],
        )
