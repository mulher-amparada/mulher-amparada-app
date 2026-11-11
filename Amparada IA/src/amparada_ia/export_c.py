"""Exporta tensores de um checkpoint confiável para o formato binário AMPAW01."""
from __future__ import annotations
import argparse, struct
from pathlib import Path
import torch
ROOT=Path(__file__).resolve().parents[2]
MAGIC=b"AMPAW01\0"

def main():
    p=argparse.ArgumentParser(description="Exporta pesos PyTorch para o runtime C experimental.")
    p.add_argument("--checkpoint",type=Path,default=ROOT/"runs"/"amparada_ia.pt")
    p.add_argument("--output",type=Path,default=ROOT/"runs"/"amparada_ia.ampaw")
    a=p.parse_args()
    if not a.checkpoint.is_file(): p.error("Checkpoint não encontrado; treine primeiro.")
    # Nunca use checkpoints baixados de fontes desconhecidas.
    ckpt=torch.load(a.checkpoint,map_location="cpu",weights_only=False)
    cfg=ckpt["model_config"]; state=ckpt["model_state"]
    if ckpt.get("tokenizer_type","char")!="char":
        p.error("O runtime C inicial aceita somente o tokenizador por caractere.")
    with a.output.open("wb") as f:
        f.write(MAGIC); f.write(struct.pack("<6I",cfg["vocab_size"],cfg["block_size"],cfg["n_embd"],
                                           cfg["n_head"],cfg["n_layer"],len(state)))
        for name,tensor in state.items():
            raw=name.encode("utf-8"); t=tensor.detach().cpu().contiguous().float()
            f.write(struct.pack("<H",len(raw))); f.write(raw)
            f.write(struct.pack("<B",t.ndim))
            for dim in t.shape: f.write(struct.pack("<I",dim))
            f.write(t.numpy().astype("<f4",copy=False).tobytes())
    print(f"Exportado: {a.output} | tensores={len(state)} | dimensões={cfg}")

if __name__=="__main__": main()
