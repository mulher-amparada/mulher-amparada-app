"""Pesquisa web em tempo real por uma instância SearXNG.

A instância é configurada por SEARXNG_URL. Por padrão, usa uma instância
local em http://127.0.0.1:8080; não é necessária chave de API.
"""
from __future__ import annotations

import json
import os
import re
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urlparse
from urllib.request import Request, urlopen

DEFAULT_SEARXNG_URL = "http://127.0.0.1:8080"
TIMEOUT_SECONDS = 15
MAX_QUERY_LENGTH = 500


class WebSearchError(RuntimeError):
    """Erro de configuração ou comunicação com o serviço de pesquisa."""


def _base_url() -> str:
    value = os.environ.get("SEARXNG_URL", DEFAULT_SEARXNG_URL).strip().rstrip("/")
    parsed = urlparse(value)
    if parsed.scheme not in {"http", "https"} or not parsed.netloc:
        raise WebSearchError("SEARXNG_URL precisa ser um endereço HTTP ou HTTPS válido.")
    if parsed.username or parsed.password or parsed.query or parsed.fragment:
        raise WebSearchError("SEARXNG_URL não deve conter credenciais, consulta ou fragmento.")
    return value


def _clean_result(item: dict) -> dict | None:
    title = str(item.get("title") or "").strip()
    url = str(item.get("url") or "").strip()
    description = str(item.get("content") or item.get("description") or "").strip()
    parsed = urlparse(url)
    if not title or parsed.scheme not in {"http", "https"} or not parsed.netloc:
        return None
    return {
        "title": title[:240],
        "url": url[:2000],
        "description": description[:1600],
        "published": str(item.get("publishedDate") or item.get("published") or "")[:80],
    }


def search_web(query: str, *, base_url: str | None = None, count: int = 5) -> list[dict]:
    """Pesquisa páginas atuais pela API JSON do SearXNG."""
    query = re.sub(r"\s+", " ", str(query)).strip()[:MAX_QUERY_LENGTH]
    if not query:
        raise ValueError("A consulta de pesquisa está vazia.")

    base = base_url.strip().rstrip("/") if base_url else _base_url()
    parsed_base = urlparse(base)
    if (parsed_base.scheme not in {"http", "https"} or not parsed_base.netloc
            or parsed_base.username or parsed_base.password
            or parsed_base.query or parsed_base.fragment):
        raise WebSearchError("O endereço do SearXNG precisa ser HTTP ou HTTPS e não pode conter credenciais.")
    params = urlencode({
        "q": query,
        "format": "json",
        "language": "pt-BR",
        "categories": "general",
        "safesearch": 2,
    })
    request = Request(
        f"{base}/search?{params}",
        headers={
            "Accept": "application/json",
            "User-Agent": "AmparadaIA/1.0 (SearXNG search)",
        },
        method="GET",
    )
    try:
        with urlopen(request, timeout=TIMEOUT_SECONDS) as response:
            payload = json.loads(response.read().decode("utf-8"))
    except HTTPError as exc:
        if exc.code == 429:
            raise WebSearchError("A instância SearXNG limitou temporariamente as consultas. Tente novamente mais tarde.") from exc
        if exc.code in {401, 403}:
            raise WebSearchError("A instância SearXNG recusou a consulta. Verifique sua configuração.") from exc
        raise WebSearchError(f"A instância SearXNG retornou HTTP {exc.code}.") from exc
    except (URLError, TimeoutError, OSError) as exc:
        raise WebSearchError("Não foi possível conectar à instância SearXNG. Verifique se ela está em execução.") from exc
    except (UnicodeDecodeError, json.JSONDecodeError) as exc:
        raise WebSearchError("A instância SearXNG retornou uma resposta inválida. Confirme se o formato JSON está habilitado.") from exc

    raw_results = payload.get("results", [])
    results = []
    for item in raw_results:
        if isinstance(item, dict):
            cleaned = _clean_result(item)
            if cleaned:
                results.append(cleaned)
        if len(results) >= max(1, min(int(count), 10)):
            break
    return results


def build_web_answer(query: str, results: list[dict]) -> dict:
    """Cria resposta baseada somente nos resumos devolvidos pela busca."""
    if not results:
        return {
            "answer": "Pesquisei na internet, mas não encontrei resultados suficientemente relevantes. Tente outros termos.",
            "sources": [],
            "mode": "live_web_search",
            "provider": "SearXNG",
        }
    sections = ["Pesquisei na internet agora. Estes foram os resultados mais relevantes:"]
    sources = []
    for index, result in enumerate(results, start=1):
        summary = result.get("description") or "O resultado não trouxe um resumo."
        sections.append(f"\n{index}. {result['title']}\n{summary}")
        sources.append({
            "title": result["title"],
            "url": result["url"],
            "updated_at": result.get("published", ""),
        })
    sections.append(
        "\nResumo baseado nos trechos apresentados pela instância de busca. "
        "Abra as fontes para conferir o contexto completo; os resultados podem "
        "estar incompletos ou desatualizados."
    )
    return {
        "answer": "\n".join(sections),
        "sources": sources,
        "mode": "live_web_search",
        "provider": "SearXNG",
        "query": query,
    }
