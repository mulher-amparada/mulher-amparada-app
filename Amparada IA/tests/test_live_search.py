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
    def test_requires_api_key(self):
        with patch.dict("os.environ", {}, clear=True):
            with self.assertRaises(WebSearchError):
                search_web("violência psicológica")

    @patch("src.amparada_ia.live_search.urlopen")
    def test_search_returns_safe_web_sources(self, mocked_urlopen):
        mocked_urlopen.return_value = FakeResponse({
            "web": {"results": [
                {"title": "Informação oficial", "url": "https://www.gov.br/exemplo",
                 "description": "Resumo do documento oficial."},
                {"title": "URL inválida", "url": "javascript:alert(1)",
                 "description": "Não deve ser incluída."},
            ]}
        })
        results = search_web("direitos das mulheres", api_key="test-key")
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0]["title"], "Informação oficial")
        request = mocked_urlopen.call_args.args[0]
        self.assertEqual(request.get_header("X-subscription-token"), "test-key")
        self.assertIn("country=BR", request.full_url)

    def test_answer_includes_source_links(self):
        answer = build_web_answer("direitos", [{
            "title": "Fonte oficial", "url": "https://www.gov.br/",
            "description": "Resumo consultado.", "published": "",
        }])
        self.assertEqual(answer["mode"], "live_web_search")
        self.assertIn("Resumo consultado.", answer["answer"])
        self.assertEqual(answer["sources"][0]["url"], "https://www.gov.br/")

    def test_empty_results_are_reported_honestly(self):
        answer = build_web_answer("consulta", [])
        self.assertEqual(answer["sources"], [])
        self.assertIn("não encontrei", answer["answer"])


if __name__ == "__main__":
    unittest.main()
