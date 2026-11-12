"""Pesquisa web em tempo real usando a API oficial do Brave Search.

A chave é lida exclusivamente do ambiente do servidor. Nunca a exponha no
JavaScript, HTML, logs ou no repositório.
"""
from __future__ import annotations

import json
import os
import re
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urlparse
from urllib.request import Request, urlopen

BRAVE_ENDPOINT = "https://api.search.brave.com/res/v1/web/search"
TIMEOUT_SECONDS = 12
MAX_QUERY_LENGTH = 500


class WebSearchError(RuntimeError):
    """Erro de configuração ou comunicação com o provedor de busca."""


def _clean_result(item: dict) -> dict | None:
    title = str(item.get("title") or "").strip()
    url = str(item.get("url") or "").strip()
    description = str(item.get("description") or "").strip()
    parsed = urlparse(url)
    if not title or parsed.scheme not in {"http", "https"} or not parsed.netloc:
        return None
    extras = item.get("extra_snippets") or []
    if isinstance(extras, list):
        description = " ".join(
            [description] + [str(value).strip() for value in extras[:2] if value]
        ).strip()
    return {
        "title": title[:240],
        "url": url[:2000],
        "description": description[:1600],
        "published": str(item.get("page_age") or item.get("published") or "")[:80],
    }


def search_web(query: str, *, api_key: str | None = None, count: int = 5) -> list[dict]:
    """Pesquisa páginas atuais na web e devolve resultados com URL e resumo."""
    key = api_key or os.environ.get("BRAVE_SEARCH_API_KEY")
    if not key:
        raise WebSearchError(
            "Pesquisa na internet não configurada. Defina BRAVE_SEARCH_API_KEY "
            "no ambiente do servidor."
        )
    query = re.sub(r"\s+", " ", str(query)).strip()[:MAX_QUERY_LENGTH]
    if not query:
        raise ValueError("A consulta de pesquisa está vazia.")
    params = urlencode({
        "q": query,
        "country": "BR",
        "search_lang": "pt",
        "ui_lang": "pt-BR",
        "count": max(1, min(int(count), 10)),
        "safesearch": "strict",
    })
    request = Request(
        f"{BRAVE_ENDPOINT}?{params}",
        headers={
            "Accept": "application/json",
            "X-Subscription-Token": key,
            "User-Agent": "AmparadaIA/1.0 (web search)",
        },
        method="GET",
    )
    try:
        with urlopen(request, timeout=TIMEOUT_SECONDS) as response:
            payload = json.loads(response.read().decode("utf-8"))
    except HTTPError as exc:
        if exc.code in {401, 403}:
            raise WebSearchError("A chave da pesquisa web é inválida ou não tem acesso à API.") from exc
        if exc.code == 429:
            raise WebSearchError("O limite de pesquisas foi atingido. Tente novamente mais tarde.") from exc
        raise WebSearchError(f"O provedor de pesquisa retornou HTTP {exc.code}.") from exc
    except (URLError, TimeoutError, OSError) as exc:
        raise WebSearchError("Não foi possível conectar ao serviço de pesquisa na internet.") from exc
    except (UnicodeDecodeError, json.JSONDecodeError) as exc:
        raise WebSearchError("O serviço de pesquisa retornou uma resposta inválida.") from exc

    raw_results = payload.get("web", {}).get("results", [])
    results = []
    for item in raw_results:
        if isinstance(item, dict):
            cleaned = _clean_result(item)
            if cleaned:
                results.append(cleaned)
        if len(results) >= count:
            break
    return results


def build_web_answer(query: str, results: list[dict]) -> dict:
    """Cria resposta baseada somente nos resumos devolvidos pela busca."""
    if not results:
        return {
            "answer": "Pesquisei na internet, mas não encontrei resultados suficientemente relevantes. Tente outros termos.",
            "sources": [],
            "mode": "live_web_search",
            "provider": "Brave Search",
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
        "\nResumo baseado nos trechos apresentados pelo mecanismo de busca. "
        "Abra as fontes para conferir o contexto completo; os resultados podem "
        "estar incompletos ou desatualizados."
    )
    return {
        "answer": "\n".join(sections),
        "sources": sources,
        "mode": "live_web_search",
        "provider": "Brave Search",
        "query": query,
    }
