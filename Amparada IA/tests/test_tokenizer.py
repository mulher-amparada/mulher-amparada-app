import unittest

from src.amparada_ia.tokenizer import CharTokenizer


class CharTokenizerTests(unittest.TestCase):
    def test_round_trip(self):
        tokenizer = CharTokenizer.from_text("mulher amparada")
        encoded = tokenizer.encode("mulher")
        self.assertEqual(tokenizer.decode(encoded), "mulher")

    def test_unknown_character_uses_reserved_id(self):
        tokenizer = CharTokenizer.from_text("abc")
        self.assertEqual(tokenizer.encode("z"), [0])
        self.assertEqual(tokenizer.decode([0]), "�")

    def test_serialization_preserves_vocabulary(self):
        tokenizer = CharTokenizer.from_text("ação")
        restored = CharTokenizer.from_dict(tokenizer.to_dict())
        self.assertEqual(restored.encode("ação"), tokenizer.encode("ação"))
        self.assertEqual(restored.vocab_size, tokenizer.vocab_size)


if __name__ == "__main__":
    unittest.main()
