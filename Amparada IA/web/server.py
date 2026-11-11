"""Servidor local da demonstração web da Amparada IA com tokenizador BPE."""
from __future__ import annotations

import json
import re
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
if not FONT_FILE.is_file():
    FONT_FILE = WEB_DIR / "font.ttf"

sys.path.insert(0, str(ROOT / "src"))
MODEL = TOKENIZER = MODEL_ERROR = None


def normalize_message(message: str) -> str:
    normalized = message.casefold().replace("você", "voce").replace("cê", "ce")
    normalized = re.sub(r"[^a-záàâãéêíóôõúç ]", "", normalized)
    return re.sub(r"\s+", " ", normalized).strip()


# 20 formas-base combinadas com 50 complementos naturais = 1.000 entradas.
HOW_ARE_YOU_BASES = (
    "como voce esta", "como vc esta", "como ce ta", "como tu ta",
    "como tu estas", "como vai voce", "como vai vc", "tudo bem com voce",
    "tudo bem contigo", "ta tudo bem com voce", "esta tudo bem contigo",
    "como voce se sente", "como voce anda", "como anda voce",
    "como estao as coisas com voce", "como vao as coisas por ai",
    "tudo tranquilo por ai", "tudo certo por ai", "como esta sendo seu dia",
    "como foi seu dia",
)
HOW_ARE_YOU_ENDINGS = (
    "", " hoje", " agora", " por ai", " neste momento", " ultimamente",
    " nesses dias", " nesses ultimos dias", " nesta manha", " nesta tarde",
    " nesta noite", " hoje de manha", " hoje a tarde", " hoje a noite",
    " nesse momento", " nesse instante", " atualmente", " esses dias",
    " de uns tempos pra ca", " por esses dias", " nessa semana",
    " nesta semana", " neste dia", " por enquanto", " tudo bem",
    " tudo tranquilo", " tudo certo", " como voce esta", " como voce vai",
    " como anda a vida", " como vao as coisas", " como estao as coisas",
    " e por ai", " e com voce", " e contigo", " do seu lado",
    " ai onde voce esta", " ai", " nessa hora", " neste horario",
    " depois de hoje", " depois desses dias", " ultimamente por ai",
    " nesses momentos", " nessa fase", " nessa epoca", " neste periodo",
    " por esses lados", " como tem passado", " como voce tem estado",
)
HOW_ARE_YOU_ENTRIES = {
    base + ending
    for base in HOW_ARE_YOU_BASES
    for ending in HOW_ARE_YOU_ENDINGS
}


def get_greeting_response(message: str) -> str | None:
    """Reconhece cumprimentos e 1.000 formas de perguntar como alguém está."""
    normalized = normalize_message(message)

    greetings = {
        "oi": "Olá! Tudo bem?",
        "oie": "Olá! Tudo bem?",
        "oii": "Olá! Tudo bem?",
        "ola": "Olá! Tudo bem?",
        "bom dia": "Bom dia! Tudo bem?",
        "boa tarde": "Boa tarde! Tudo bem?",
        "boa noite": "Boa noite! Tudo bem?",
    }
    if normalized in greetings:
        return greetings[normalized]

    if normalized in HOW_ARE_YOU_ENTRIES:
        return "Estou bem, obrigada por perguntar! E você, como está?"

    cleaned = re.sub(r"^(e |me diz,? |me conta,? )", "", normalized)
    cleaned = re.sub(r"^(por favor |meu bem |amiga,? )", "", cleaned)
    if cleaned in HOW_ARE_YOU_ENTRIES:
        return "Estou bem, obrigada por perguntar! E você, como está?"

    return None

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

            greeting = get_greeting_response(message)
            if greeting is not None:
                self.send_json(
                    200,
                    {"answer": greeting, "experimental": False},
                )
                return

            ids = TOKENIZER.encode(message.strip())
            if not ids:
                self.send_json(400, {"error": "Mensagem sem tokens reconhecíveis."})
                return

            context = torch.tensor(
                [ids[-MODEL.config["block_size"]:]],
                dtype=torch.long,
            )

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
                    "answer": answer or "O modelo não gerou continuação.",
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
