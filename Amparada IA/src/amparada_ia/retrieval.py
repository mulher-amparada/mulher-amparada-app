"""Busca local de trechos confiáveis para a Amparada IA (RAG simples)."""

from __future__ import annotations

import json
import re
import unicodedata
from pathlib import Path
from typing import Any

DEFAULT_KNOWLEDGE_PATH = Path(__file__).resolve().parents[2] / "knowledge" / "base.json"
STOP_WORDS = {
    "a", "ao", "aos", "as", "com", "como", "da", "das", "de", "do", "dos",
    "e", "ela", "ele", "em", "essa", "esse", "esta", "este", "eu", "isso",
    "na", "nas", "no", "nos", "o", "os", "ou", "para", "pela", "pelas",
    "pelo", "pelos", "por", "que", "se", "sem", "sobre", "sua", "suas",
    "seu", "seus", "um", "uma", "umas", "uns", "é", "ser", "ter", "me",
    "minha", "meu", "minhas", "meus", "quero", "preciso", "pode", "podem",
}


def normalize(text: str) -> str:
    """Remove acentos e normaliza o texto para comparação lexical."""
    normalized = unicodedata.normalize("NFKD", text.casefold())
    without_marks = "".join(char for char in normalized if not unicodedata.combining(char))
    return re.sub(r"[^a-z0-9]+", " ", without_marks).strip()


def terms(text: str) -> set[str]:
    return {
        token
        for token in normalize(text).split()
        if len(token) > 1 and token not in STOP_WORDS
    }


def load_knowledge(path: Path | str = DEFAULT_KNOWLEDGE_PATH) -> list[dict[str, Any]]:
    """Carrega documentos locais; falhas de arquivo ou JSON são explícitas."""
    knowledge_path = Path(path)
    with knowledge_path.open("r", encoding="utf-8") as file:
        documents = json.load(file)

    if not isinstance(documents, list):
        raise ValueError("A base de conhecimento precisa ser uma lista JSON.")

    required = {"id", "title", "content", "source_name", "source_url"}
    for index, document in enumerate(documents):
        if not isinstance(document, dict) or not required.issubset(document):
            raise ValueError(f"Documento inválido na posição {index}.")
        if not all(isinstance(document[key], str) and document[key].strip() for key in required):
            raise ValueError(f"Campos obrigatórios vazios no documento {index}.")
    return documents


def search_knowledge(
    question: str,
    documents: list[dict[str, Any]],
    limit: int = 4,
) -> list[dict[str, Any]]:
    """Retorna os documentos com maior correspondência lexical com a pergunta."""
    query_terms = terms(question)
    if not query_terms:
        return []

    ranked: list[tuple[float, dict[str, Any]]] = []
    for document in documents:
        searchable = " ".join(
            [
                document.get("title", ""),
                document.get("content", ""),
                " ".join(document.get("tags", [])),
            ]
        )
        document_terms = terms(searchable)
        matched = query_terms & document_terms
        if not matched:
            continue

        # Relevância simples e explicável: cobertura da pergunta, com leve
        # preferência por termos que aparecem no título ou nas etiquetas.
        title_terms = terms(document.get("title", ""))
        tag_terms = terms(" ".join(document.get("tags", [])))
        score = len(matched) / len(query_terms)
        score += 0.15 * len(matched & title_terms)
        score += 0.10 * len(matched & tag_terms)
        ranked.append((score, document))

    ranked.sort(key=lambda item: item[0], reverse=True)
    return [document for score, document in ranked[: max(1, limit)] if score >= 0.18]


def build_grounded_answer(
    question: str,
    documents: list[dict[str, Any]],
    limit: int = 3,
) -> dict[str, Any] | None:
    """Monta uma resposta extrativa com fontes; não inventa uma síntese do modelo."""
    matches = search_knowledge(question, documents, limit=limit)
    if not matches:
        return None

    sections = [
        "Encontrei estas informações na base de conhecimento da Amparada IA:"
    ]
    for document in matches:
        sections.append(f"\n{document['title']}\n{document['content']}")

    sections.append(
        "\nEstas informações são educativas e gerais, não uma avaliação individual. "
        "Se a situação envolver risco imediato, procure os serviços de emergência."
    )
    sources = [
        {
            "title": document["source_name"],
            "url": document["source_url"],
            "updated_at": document.get("updated_at", ""),
        }
        for document in matches
    ]
    return {
        "answer": "\n".join(sections),
        "sources": sources,
        "mode": "rag",
        "documents_used": [document["id"] for document in matches],
    }
