"""Servidor local da demonstração web da Amparada IA com pesquisa web em tempo real."""
from __future__ import annotations

import json
import os
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
WEB_DIR = Path(__file__).resolve().parent
KNOWLEDGE_FILE = ROOT / "knowledge" / "base.json"
FONT_FILE = ROOT.parent / "font.ttf"
if not FONT_FILE.is_file():
    FONT_FILE = ROOT / "font.ttf"

sys.path.insert(0, str(ROOT / "src"))
from amparada_ia.live_search import WebSearchError, build_web_answer, search_web
from amparada_ia.retrieval import load_knowledge

KNOWLEDGE = []
KNOWLEDGE_ERROR = None


def load_knowledge_base():
    global KNOWLEDGE, KNOWLEDGE_ERROR
    try:
        KNOWLEDGE = load_knowledge(KNOWLEDGE_FILE)
        KNOWLEDGE_ERROR = None
    except Exception as exc:
        KNOWLEDGE = []
        KNOWLEDGE_ERROR = f"Falha ao carregar base de conhecimento: {type(exc).__name__}"


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
                    "ready": bool(os.environ.get("BRAVE_SEARCH_API_KEY")),
                    "web_search_ready": bool(os.environ.get("BRAVE_SEARCH_API_KEY")),
                    "search_provider": "Brave Search",
                    "knowledge_ready": bool(KNOWLEDGE),
                    "knowledge_documents": len(KNOWLEDGE),
                    "message": (
                        "Pesquisa na internet em tempo real pronta."
                        if os.environ.get("BRAVE_SEARCH_API_KEY")
                        else "Configure BRAVE_SEARCH_API_KEY no ambiente do servidor."
                    ),
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

            # Pesquisa obrigatoriamente na internet em tempo real.
            # Sem resultados ou sem chave, não finge ter consultado a web.
            results = search_web(message.strip(), count=5)
            self.send_json(200, build_web_answer(message.strip(), results))

        except WebSearchError as exc:
            self.send_json(503, {"error": str(exc), "mode": "live_web_search_unavailable"})
        except (ValueError, json.JSONDecodeError):
            self.send_json(400, {"error": "Requisição inválida."})
        except Exception as exc:
            self.send_json(500, {"error": f"Falha na consulta: {type(exc).__name__}"})

    def log_message(self, format, *args):
        super().log_message(format, *args)


if __name__ == "__main__":
    load_knowledge_base()
    if KNOWLEDGE_ERROR:
        print("Aviso:", KNOWLEDGE_ERROR)
    print("Abra http://127.0.0.1:8000 — acesso somente local.")
    ThreadingHTTPServer(("127.0.0.1", 8000), Handler).serve_forever()
