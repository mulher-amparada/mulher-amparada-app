import unittest

from src.amparada_ia.tokenizer import BPETokenizer


class BPETokenizerTests(unittest.TestCase):
    def test_round_trip(self):
        tokenizer = BPETokenizer.from_text("mulher amparada")
        encoded = tokenizer.encode("mulher amparada")
        self.assertEqual(tokenizer.decode(encoded), "mulher amparada")

    def test_unknown_character_uses_reserved_id(self):
        tokenizer = BPETokenizer.from_text("abc")
        self.assertEqual(tokenizer.encode("🦋"), [0])
        self.assertEqual(tokenizer.decode([0]), "�")

    def test_serialization_preserves_vocabulary_and_merges(self):
        tokenizer = BPETokenizer.from_text("ação ação mulher mulher", vocab_size=64)
        restored = BPETokenizer.from_dict(tokenizer.to_dict())
        sample = "ação mulher!"
        self.assertEqual(restored.encode(sample), tokenizer.encode(sample))
        self.assertEqual(restored.decode(restored.encode(sample)), sample)
        self.assertEqual(restored.vocab_size, tokenizer.vocab_size)
        self.assertEqual(restored.merges, tokenizer.merges)

    def test_vocab_size_is_at_least_reserved_and_base_symbols(self):
        tokenizer = BPETokenizer.from_text("abc", vocab_size=16)
        self.assertGreaterEqual(tokenizer.vocab_size, 4)


if __name__ == "__main__":
    unittest.main()
