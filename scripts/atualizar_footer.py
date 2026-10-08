from pathlib import Path
import markdown

raiz = Path(__file__).resolve().parent.parent

readme = raiz / "README.md"
index = raiz / "index.html"

html = markdown.markdown(
    readme.read_text(encoding="utf-8"),
    extensions=["tables", "fenced_code"]
)

conteudo = index.read_text(encoding="utf-8")

inicio = "<!-- README-FOOTER-START -->"
fim = "<!-- README-FOOTER-END -->"

if inicio not in conteudo or fim not in conteudo:
    raise SystemExit("Marcadores do footer não encontrados no index.html")

novo_footer = f"{inicio}\n{html}\n{fim}"

antes, restante = conteudo.split(inicio, 1)
_, depois = restante.split(fim, 1)

index.write_text(
    antes + novo_footer + depois,
    encoding="utf-8"
)

print("Footer atualizado com o README!")