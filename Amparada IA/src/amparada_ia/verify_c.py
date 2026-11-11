"""Compara os logits do runtime C com os logits PyTorch para um prompt."""
from __future__ import annotations
import argparse, subprocess, tempfile
from pathlib import Path
import torch
from .model import AmparadaLanguageModel
from .tokenizer import CharTokenizer
ROOT=Path(__file__).resolve().parents[2]

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("--checkpoint",type=Path,default=ROOT/"runs"/"amparada_ia.pt")
    p.add_argument("--prompt",default="Respeito")
    p.add_argument("--tolerance",type=float,default=2e-4)
    a=p.parse_args()
    if not a.checkpoint.is_file():p.error("Checkpoint não encontrado.")
    ck=torch.load(a.checkpoint,map_location="cpu",weights_only=False)
    if ck.get("tokenizer_type","char")!="char":p.error("Runtime C atual aceita apenas tokenizador por caractere.")
    tok=CharTokenizer.from_dict(ck["tokenizer"]);ids=tok.encode(a.prompt)
    if not ids or 0 in ids:p.error("Prompt vazio ou com caractere desconhecido.")
    model=AmparadaLanguageModel(**ck["model_config"]);model.load_state_dict(ck["model_state"]);model.eval()
    with torch.no_grad(): expected=model(torch.tensor([ids],dtype=torch.long))[0][0,-1].tolist()
    weights=ROOT/"runs"/"amparada_ia.ampaw"
    subprocess.run(["python","-m","src.amparada_ia.export_c","--checkpoint",str(a.checkpoint),"--output",str(weights)],cwd=ROOT,check=True)
    with tempfile.TemporaryDirectory() as tmp:
        binary=Path(tmp)/"amparada-c"
        subprocess.run(["cc","-O2","-std=c11",str(ROOT/"c_runtime"/"amparada.c"),"-lm","-o",str(binary)],check=True)
        result=subprocess.run([str(binary),str(weights),*[str(i) for i in ids]],capture_output=True,text=True,check=True)
    line=next(line for line in result.stdout.splitlines() if line.startswith("logits="))
    actual=[float(v) for v in line[len("logits="):].split(",")]
    errors=[abs(x-y) for x,y in zip(expected,actual)]
    maximum=max(errors,default=0.0)
    print(f"Maior diferença absoluta: {maximum:.8g} (tolerância: {a.tolerance})")
    if len(actual)!=len(expected) or maximum>a.tolerance:raise SystemExit("FALHA: logits C divergem do PyTorch.")
    print("OK: logits C e PyTorch dentro da tolerância.")

if __name__=="__main__":main()
