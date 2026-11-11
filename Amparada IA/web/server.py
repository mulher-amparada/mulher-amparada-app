"""Servidor local da demonstração web da Amparada IA."""
from __future__ import annotations
import json
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import sys
import torch

ROOT = Path(__file__).resolve().parents[1]
WEB_DIR = Path(__file__).resolve().parent
CHECKPOINT = ROOT / "runs" / "amparada_ia.pt"
sys.path.insert(0, str(ROOT / "src"))
MODEL = TOKENIZER = MODEL_ERROR = None

def load_model():
    global MODEL, TOKENIZER, MODEL_ERROR
    if not CHECKPOINT.is_file():
        MODEL_ERROR = "Modelo ainda não treinado. Siga o README da Amparada IA."
        return
    try:
        # Carrega somente checkpoint local criado pelo treinamento deste projeto.
        checkpoint = torch.load(CHECKPOINT, map_location="cpu", weights_only=True)
        from amparada_ia.model import AmparadaLanguageModel
        from amparada_ia.tokenizer import CharTokenizer
        TOKENIZER = CharTokenizer.from_dict(checkpoint["tokenizer"])
        MODEL = AmparadaLanguageModel(**checkpoint["model_config"])
        MODEL.load_state_dict(checkpoint["model_state"])
        MODEL.eval()
        MODEL_ERROR = None
    except Exception as exc:
        MODEL_ERROR = f"Falha ao carregar checkpoint: {type(exc).__name__}"

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
            self.send_json(200, {"ready": MODEL is not None,
                "message": "Modelo carregado." if MODEL is not None else MODEL_ERROR})
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
            ids = TOKENIZER.encode(message.strip())
            if not ids:
                self.send_json(400, {"error": "Mensagem sem caracteres reconhecíveis."})
                return
            context = torch.tensor([ids[-MODEL.config["block_size"]:]], dtype=torch.long)
            with torch.inference_mode():
                output = MODEL.generate(context, max_new_tokens=80, temperature=0.8)
            answer = TOKENIZER.decode(output[0, len(context[0]):].tolist()).strip()
            self.send_json(200, {"answer": answer or "O modelo não gerou continuação.",
                                 "experimental": True})
        except (ValueError, json.JSONDecodeError):
            self.send_json(400, {"error": "Requisição inválida."})
        except Exception as exc:
            self.send_json(500, {"error": f"Falha na inferência: {type(exc).__name__}"})

    def log_message(self, format, *args):
        # Não registra o conteúdo das mensagens.
        super().log_message(format, *args)

if __name__ == "__main__":
    load_model()
    if MODEL is None:
        print("Aviso:", MODEL_ERROR)
    print("Abra http://127.0.0.1:8000 — acesso somente local.")
    ThreadingHTTPServer(("127.0.0.1", 8000), Handler).serve_forever()
