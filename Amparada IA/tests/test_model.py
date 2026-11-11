import unittest

import torch

from src.amparada_ia.model import AmparadaLanguageModel


class AmparadaLanguageModelTests(unittest.TestCase):
    def setUp(self):
        torch.manual_seed(7)
        self.model = AmparadaLanguageModel(
            vocab_size=20,
            block_size=12,
            n_embd=16,
            n_head=4,
            n_layer=2,
            dropout=0.0,
        )
        self.model.eval()

    def test_forward_returns_expected_shape_and_loss(self):
        tokens = torch.randint(0, 20, (2, 8))
        logits, loss = self.model(tokens, tokens)
        self.assertEqual(tuple(logits.shape), (2, 8, 20))
        self.assertIsNotNone(loss)
        self.assertTrue(torch.isfinite(loss).item())

    def test_future_tokens_do_not_change_previous_logits(self):
        first = torch.tensor([[1, 2, 3, 4, 5, 6]])
        second = torch.tensor([[1, 2, 3, 9, 8, 7]])
        with torch.no_grad():
            first_logits, _ = self.model(first)
            second_logits, _ = self.model(second)
        self.assertTrue(torch.allclose(first_logits[:, :3], second_logits[:, :3], atol=1e-6))

    def test_generation_adds_requested_number_of_tokens(self):
        prompt = torch.tensor([[1, 2, 3]])
        generated = self.model.generate(prompt, max_new_tokens=4, temperature=1.0)
        self.assertEqual(tuple(generated.shape), (1, 7))

    def test_rejects_sequence_longer_than_context(self):
        tokens = torch.randint(0, 20, (1, 13))
        with self.assertRaises(ValueError):
            self.model(tokens)


if __name__ == "__main__":
    unittest.main()
