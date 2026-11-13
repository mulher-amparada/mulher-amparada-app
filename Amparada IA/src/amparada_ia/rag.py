"""Busca lexical local (BM25) para recuperar trechos dos dados existentes."""

from __future__ import annotations

import math
import re
import unicodedata
from dataclasses import dataclass
from pathlib import Path

SUPPORTED_EXTENSIONS = {".txt", ".md", ".json", ".csv"}
TOKEN_PATTERN = re.compile(r"[^\W_]+", re.UNICODE)
CHUNK_SIZE = 900
CHUNK_OVERLAP = 140


def _terms(text: str) -> list[str]:
    normalized = unicodedata.normalize("NFKD", text.casefold())
    normalized = "".join(char for char in normalized if not unicodedata.combining(char))
    return TOKEN_PATTERN.findall(normalized)


@dataclass(frozen=True)
class DocumentChunk:
    source: str
    text: str
    terms: tuple[str, ...]


class LocalRAG:
    """Indexa somente arquivos de texto de uma pasta local e atualiza ao detectar mudanças."""

    def __init__(self, data_dir: Path) -> None:
        self.data_dir = data_dir.resolve()
        self._signature: tuple[tuple[str, int, int], ...] | None = None
        self._chunks: list[DocumentChunk] = []
        self._document_frequency: dict[str, int] = {}

    def _files(self) -> list[Path]:
        if not self.data_dir.is_dir():
            return []
        return sorted(
            path for path in self.data_dir.rglob("*")
            if path.is_file()
            and not path.is_symlink()
            and path.suffix.lower() in SUPPORTED_EXTENSIONS
            and not any(part.startswith(".") for part in path.relative_to(self.data_dir).parts)
        )

    def _current_signature(self, files: list[Path]) -> tuple[tuple[str, int, int], ...]:
        signature = []
        for path in files:
            try:
                stat = path.stat()
            except OSError:
                continue
            signature.append((str(path.relative_to(self.data_dir)), stat.st_mtime_ns, stat.st_size))
        return tuple(signature)

    @staticmethod
    def _split(text: str) -> list[str]:
        text = text.strip()
        if not text:
            return []
        chunks = []
        start = 0
        while start < len(text):
            end = min(start + CHUNK_SIZE, len(text))
            if end < len(text):
                boundary = text.rfind("\n", start + CHUNK_SIZE // 2, end)
                if boundary > start:
                    end = boundary
            piece = text[start:end].strip()
            if piece:
                chunks.append(piece)
            if end >= len(text):
                break
            start = max(end - CHUNK_OVERLAP, start + 1)
        return chunks

    def _refresh(self) -> None:
        files = self._files()
        signature = self._current_signature(files)
        if signature == self._signature:
            return

        chunks: list[DocumentChunk] = []
        for path in files:
            try:
                text = path.read_text(encoding="utf-8")
            except (OSError, UnicodeError):
                continue
            source = str(path.relative_to(self.data_dir))
            for piece in self._split(text):
                terms = tuple(_terms(piece))
                if terms:
                    chunks.append(DocumentChunk(source, piece, terms))

        document_frequency: dict[str, int] = {}
        for chunk in chunks:
            for term in set(chunk.terms):
                document_frequency[term] = document_frequency.get(term, 0) + 1

        self._chunks = chunks
        self._document_frequency = document_frequency
        self._signature = signature

    @property
    def stats(self) -> dict[str, int]:
        self._refresh()
        return {
            "files": len({chunk.source for chunk in self._chunks}),
            "chunks": len(self._chunks),
        }

    def search(self, query: str, top_k: int = 3) -> list[dict[str, str | float]]:
        self._refresh()
        query_terms = _terms(query)
        if not query_terms or not self._chunks:
            return []

        total = len(self._chunks)
        average_length = sum(len(chunk.terms) for chunk in self._chunks) / total
        scores = []
        k1, b = 1.5, 0.75

        for chunk in self._chunks:
            frequencies: dict[str, int] = {}
            for term in chunk.terms:
                frequencies[term] = frequencies.get(term, 0) + 1

            score = 0.0
            for term in set(query_terms):
                frequency = frequencies.get(term, 0)
                if not frequency:
                    continue
                doc_frequency = self._document_frequency.get(term, 0)
                inverse_frequency = math.log(
                    1 + (total - doc_frequency + 0.5) / (doc_frequency + 0.5)
                )
                denominator = frequency + k1 * (
                    1 - b + b * len(chunk.terms) / max(average_length, 1)
                )
                score += inverse_frequency * (frequency * (k1 + 1)) / denominator

            if score > 0:
                scores.append((score, chunk))

        scores.sort(key=lambda item: item[0], reverse=True)
        return [
            {"source": chunk.source, "text": chunk.text, "score": round(score, 4)}
            for score, chunk in scores[:max(1, top_k)]
        ]
