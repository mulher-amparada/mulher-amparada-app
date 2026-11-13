"""Servidor local da demonstração web da Amparada IA com tokenizador BPE."""
from __future__ import annotations

import json
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import sys

import torch

ROOT = Path(__file__).resolve().parents[1]
WEB_DIR = Path(__file__).resolve().parent
CHECKPOINT = ROOT / "runs" / "amparada_ia_bpe.pt"
FONT_FILE = ROOT.parent / "font.ttf"
if not FONT_FILE.is_file():
    FONT_FILE = ROOT / "font.ttf"

sys.path.insert(0, str(ROOT / "src"))
from amparada_ia.rag import LocalRAG

RAG = LocalRAG(ROOT / "data")
MODEL = TOKENIZER = MODEL_ERROR = None


def load_model():
    global MODEL, TOKENIZER, MODEL_ERROR

    if not CHECKPOINT.is_file():
        MODEL_ERROR = (
            "Checkpoint BPE não encontrado. Treine com "
            "python -m src.amparada_ia.train antes de iniciar o servidor."
        )
        return

    try:
        checkpoint = torch.load(
            CHECKPOINT,
            map_location="cpu",
            weights_only=True,
        )

        from amparada_ia.model import AmparadaLanguageModel
        from amparada_ia.tokenizer import BPETokenizer

        if checkpoint.get("tokenizer", {}).get("type") != "bpe":
            raise ValueError("O checkpoint não usa o tokenizador BPE.")

        TOKENIZER = BPETokenizer.from_dict(checkpoint["tokenizer"])
        MODEL = AmparadaLanguageModel(**checkpoint["model_config"])
        MODEL.load_state_dict(checkpoint["model_state"])
        MODEL.eval()
        MODEL_ERROR = None
    except Exception as exc:
        MODEL = TOKENIZER = None
        MODEL_ERROR = f"Falha ao carregar checkpoint BPE: {type(exc).__name__}: {exc}"


class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(WEB_DIR), **kwargs)

    def send_json(self, status, payload):
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path == "/api/status":
            self.send_json(
                200,
                {
                    "ready": MODEL is not None,
                    "message": "Modelo BPE carregado." if MODEL is not None else MODEL_ERROR,
                    "rag_ready": RAG.stats["chunks"] > 0,
                    "rag_files": RAG.stats["files"],
                    "rag_chunks": RAG.stats["chunks"],
                },
            )
            return

        if self.path == "/font.ttf":
            if not FONT_FILE.is_file():
                self.send_error(404, "Fonte não encontrada.")
                return

            font_data = FONT_FILE.read_bytes()
            self.send_response(200)
            self.send_header("Content-Type", "font/ttf")
            self.send_header("Content-Length", str(len(font_data)))
            self.send_header("Cache-Control", "public, max-age=86400")
            self.end_headers()
            self.wfile.write(font_data)
            return

        if self.path == "/":
            self.path = "/index.html"

        super().do_GET()

    def do_POST(self):
        if self.path != "/api/chat":
            self.send_json(404, {"error": "Rota não encontrada."})
            return

        if MODEL is None or TOKENIZER is None:
            self.send_json(503, {"error": MODEL_ERROR or "Modelo indisponível."})
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))
            if length < 1 or length > 8192:
                self.send_json(413, {"error": "Mensagem muito grande."})
                return

            payload = json.loads(self.rfile.read(length))
            message = payload.get("message", "")

            if not isinstance(message, str) or not message.strip():
                self.send_json(400, {"error": "Escreva uma mensagem."})
                return

            if len(message) > 500:
                self.send_json(400, {"error": "Use até 500 caracteres."})
                return

            question_ids = TOKENIZER.encode(message.strip())
            if not question_ids:
                self.send_json(400, {"error": "Mensagem sem tokens reconhecíveis."})
                return

            retrieved = RAG.search(message.strip(), top_k=3)
            context_text = "\n".join(
                f"[{item['source']}] {item['text']}" for item in retrieved
            )
            context_ids = TOKENIZER.encode(context_text) if context_text else []
            block_size = MODEL.config["block_size"]
            question_budget = min(len(question_ids), max(1, block_size // 2))
            context_budget = max(0, block_size - question_budget)
            prompt_ids = context_ids[-context_budget:] if context_budget else []
            prompt_ids += question_ids[-question_budget:]
            if not prompt_ids:
                prompt_ids = question_ids[-block_size:]

            context = torch.tensor([prompt_ids], dtype=torch.long)

            with torch.inference_mode():
                output = MODEL.generate(
                    context,
                    max_new_tokens=80,
                    temperature=0.8,
                )

            answer = TOKENIZER.decode(output[0, len(context[0]):].tolist()).strip()
            self.send_json(
                200,
                {
                    "answer": answer or "O modelo não gerou uma continuação.",
                    "sources": [
                        {"file": item["source"], "score": item["score"]}
                        for item in retrieved
                    ],
                    "retrieved_chunks": len(retrieved),
                    "experimental": True,
                },
            )

        except (ValueError, json.JSONDecodeError):
            self.send_json(400, {"error": "Requisição inválida."})
        except Exception as exc:
            self.send_json(500, {"error": f"Falha na inferência: {type(exc).__name__}"})

    def log_message(self, format, *args):
        super().log_message(format, *args)


if __name__ == "__main__":
    load_model()

    if MODEL is None:
        print("Aviso:", MODEL_ERROR)

    print("Abra http://127.0.0.1:8000 — acesso somente local.")
    ThreadingHTTPServer(("127.0.0.1", 8000), Handler).serve_forever()
