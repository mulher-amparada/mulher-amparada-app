import json
import tempfile
import unittest
from pathlib import Path

from src.amparada_ia.retrieval import (
    build_grounded_answer,
    load_knowledge,
    search_knowledge,
)


class RetrievalTests(unittest.TestCase):
    def setUp(self):
        self.documents = [
            {
                "id": "gaslighting",
                "title": "Sinais de manipulação e violência psicológica",
                "content": "Manipulação, humilhação, isolamento e controle podem ser sinais de violência psicológica.",
                "tags": ["gaslighting", "controle"],
                "source_name": "Fonte institucional",
                "source_url": "https://example.org/fonte",
                "updated_at": "2026-01-01",
            },
            {
                "id": "apoio",
                "title": "Como oferecer apoio",
                "content": "Escute sem julgar e respeite a autonomia da pessoa.",
                "tags": ["rede de apoio", "acolhimento"],
                "source_name": "Outra fonte",
                "source_url": "https://example.org/apoio",
            },
        ]

    def test_search_is_accent_insensitive(self):
        results = search_knowledge("violência psicologica e manipulacao", self.documents)
        self.assertEqual(results[0]["id"], "gaslighting")

    def test_matches_common_portuguese_inflections(self):
        results = search_knowledge("ele controla meu celular", self.documents)
        self.assertEqual(results[0]["id"], "gaslighting")

    def test_returns_no_results_for_unrelated_question(self):
        self.assertEqual(search_knowledge("receita de bolo", self.documents), [])

    def test_grounded_answer_includes_sources(self):
        result = build_grounded_answer("como reconhecer gaslighting?", self.documents)
        self.assertIsNotNone(result)
        self.assertEqual(result["mode"], "rag")
        self.assertTrue(result["sources"])
        self.assertIn("não uma avaliação individual", result["answer"])

    def test_load_knowledge_validates_json(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            path = Path(temp_dir) / "base.json"
            path.write_text(json.dumps(self.documents, ensure_ascii=False), encoding="utf-8")
            loaded = load_knowledge(path)
            self.assertEqual(len(loaded), 2)


if __name__ == "__main__":
    unittest.main()
