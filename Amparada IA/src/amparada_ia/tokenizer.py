"""Tokenizador mínimo por caractere para a prova de conceito."""

from __future__ import annotations

from dataclasses import dataclass


@dataclass
class CharTokenizer:
    """Mapeia cada caractere para um ID; 0 é reservado para desconhecidos."""

    stoi: dict[str, int]
    itos: list[str]

    @classmethod
    def from_text(cls, text: str) -> "CharTokenizer":
        characters = sorted(set(text))
        itos = ["<unk>", *characters]
        stoi = {character: index for index, character in enumerate(itos) if index > 0}
        return cls(stoi=stoi, itos=itos)

    @property
    def vocab_size(self) -> int:
        return len(self.itos)

    def encode(self, text: str) -> list[int]:
        return [self.stoi.get(character, 0) for character in text]

    def decode(self, token_ids: list[int]) -> str:
        output: list[str] = []
        for token_id in token_ids:
            if 0 <= token_id < len(self.itos) and token_id != 0:
                output.append(self.itos[token_id])
            else:
                output.append("�")
        return "".join(output)

    def to_dict(self) -> dict:
        return {"stoi": self.stoi, "itos": self.itos}

    @classmethod
    def from_dict(cls, data: dict) -> "CharTokenizer":
        return cls(stoi=data["stoi"], itos=data["itos"])
