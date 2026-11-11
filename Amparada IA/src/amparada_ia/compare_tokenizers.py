"""Treina/evalua checkpoints char e BPE e compara custo em tokens e bits/caractere."""
from __future__ import annotations
import argparse, subprocess, sys
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]

def run(command):
    subprocess.run(command, cwd=ROOT, check=True)

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("--steps",type=int,default=500); p.add_argument("--block-size",type=int,default=64)
    p.add_argument("--vocab-size",type=int,default=256); p.add_argument("--batches",type=int,default=20)
    a=p.parse_args(); runs=ROOT/"runs"; runs.mkdir(exist_ok=True)
    for kind in ("char","bpe"):
        ckpt=runs/f"amparada_{kind}.pt"
        run([sys.executable,"-m","src.amparada_ia.train","--tokenizer",kind,"--output",str(ckpt),
             "--steps",str(a.steps),"--block-size",str(a.block_size),"--vocab-size",str(a.vocab_size)])
        run([sys.executable,"-m","src.amparada_ia.evaluate","--checkpoint",str(ckpt),"--batches",str(a.batches)])
    print("\nOs dois modelos usam a mesma arquitetura e orçamento de passos, mas vocabulários e número de tokens diferentes.")
    print("Use bits/caractere e tokens/caractere como comparações auxiliares; a perplexidade por token não deve ser comparada isoladamente.")

if __name__=="__main__": main()
