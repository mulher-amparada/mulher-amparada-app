import json
import unittest
from unittest.mock import patch

from src.amparada_ia.live_search import WebSearchError, build_web_answer, search_web


class FakeResponse:
    def __init__(self, payload):
        self.payload = json.dumps(payload).encode("utf-8")

    def __enter__(self):
        return self

    def __exit__(self, *args):
        return False

    def read(self):
        return self.payload


class LiveSearchTests(unittest.TestCase):
    @patch("src.amparada_ia.live_search.urlopen")
    def test_search_uses_searxng_json_endpoint(self, mocked_urlopen):
        mocked_urlopen.return_value = FakeResponse({
            "results": [
                {"title": "Informação oficial", "url": "https://www.gov.br/exemplo",
                 "content": "Resumo do documento oficial.", "publishedDate": "2026-11-01"},
                {"title": "URL inválida", "url": "javascript:alert(1)",
                 "content": "Não deve ser incluída."},
            ]
        })
        results = search_web("direitos das mulheres", base_url="http://localhost:8080")
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0]["title"], "Informação oficial")
        request = mocked_urlopen.call_args.args[0]
        self.assertIn("/search?", request.full_url)
        self.assertIn("format=json", request.full_url)
        self.assertIn("language=pt-BR", request.full_url)

    def test_rejects_invalid_instance_url(self):
        with self.assertRaises(WebSearchError):
            search_web("consulta", base_url="file:///etc/passwd")

    def test_answer_includes_source_links(self):
        answer = build_web_answer("direitos", [{
            "title": "Fonte oficial", "url": "https://www.gov.br/",
            "description": "Resumo consultado.", "published": "",
        }])
        self.assertEqual(answer["mode"], "live_web_search")
        self.assertEqual(answer["provider"], "SearXNG")
        self.assertIn("Resumo consultado.", answer["answer"])
        self.assertEqual(answer["sources"][0]["url"], "https://www.gov.br/")

    def test_empty_results_are_reported_honestly(self):
        answer = build_web_answer("consulta", [])
        self.assertEqual(answer["sources"], [])
        self.assertIn("não encontrei", answer["answer"])


if __name__ == "__main__":
    unittest.main()
