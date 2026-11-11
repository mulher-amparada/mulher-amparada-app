import unittest
from src.amparada_ia.bpe_tokenizer import BPETokenizer

class BPETokenizerTests(unittest.TestCase):
    def test_round_trip(self):
        text="respeito respeito autonomia"
        tok=BPETokenizer.train(text,vocab_size=32)
        self.assertEqual(tok.decode(tok.encode(text)),text)
    def test_merges_reduce_or_preserve_token_count(self):
        text="amparada amparada amparada"
        tok=BPETokenizer.train(text,vocab_size=64)
        self.assertLessEqual(len(tok.encode(text)),len(text))
    def test_serialization(self):
        tok=BPETokenizer.train("mulher amparada",vocab_size=24)
        restored=BPETokenizer.from_dict(tok.to_dict())
        self.assertEqual(restored.encode("mulher amparada"),tok.encode("mulher amparada"))

if __name__=="__main__": unittest.main()
