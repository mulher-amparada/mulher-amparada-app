"""Testes da recuperação lexical local."""

import tempfile
import unittest
from pathlib import Path

from src.amparada_ia.rag import LocalRAG


class LocalRAGTests(unittest.TestCase):
    def test_recovers_relevant_existing_text(self):
        with tempfile.TemporaryDirectory() as directory:
            data_dir = Path(directory)
            (data_dir / "direitos.txt").write_text(
                "Autonomia significa participar das decisões sobre a própria vida.",
                encoding="utf-8",
            )
            (data_dir / "programacao.txt").write_text(
                "Python permite automatizar tarefas e organizar arquivos.",
                encoding="utf-8",
            )

            results = LocalRAG(data_dir).search("autonomia e decisões")
            self.assertTrue(results)
            self.assertEqual(results[0]["source"], "direitos.txt")

    def test_refreshes_index_after_file_changes(self):
        with tempfile.TemporaryDirectory() as directory:
            data_dir = Path(directory)
            source = data_dir / "base.txt"
            source.write_text("Privacidade protege informações pessoais.", encoding="utf-8")
            rag = LocalRAG(data_dir)

            self.assertTrue(rag.search("privacidade"))
            source.write_text("Acessibilidade melhora o uso de interfaces.", encoding="utf-8")

            self.assertFalse(rag.search("privacidade"))
            self.assertTrue(rag.search("acessibilidade"))

    def test_ignores_unsupported_files(self):
        with tempfile.TemporaryDirectory() as directory:
            data_dir = Path(directory)
            (data_dir / "binary.bin").write_bytes(b"not indexed")
            self.assertEqual(LocalRAG(data_dir).stats["chunks"], 0)


if __name__ == "__main__":
    unittest.main()
