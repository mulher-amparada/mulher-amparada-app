# Runtime C experimental

Este diretório contém o leitor de pesos AMPAW01 e a base do runtime de inferência em C puro.

## Estado e compatibilidade

- O exportador escreve tensores float32 little-endian e dimensões explícitas.
- Esta primeira versão é uma referência didática, não otimizada.
- O formato é versionado por magic bytes; não carregue arquivos de origem desconhecida.
- O runtime de referência aceita checkpoints treinados com tokenizador por caractere. BPE usa vocabulário variável e será exportado em etapa separada. O script `python -m src.amparada_ia.verify_c` compara os logits C com PyTorch e falha se a diferença exceder a tolerância.
- O runtime não implementa treinamento, backpropagation, GPU, batching ou amostragem avançada.

## Compilar

A partir de `Amparada IA/`:

```sh
python -m src.amparada_ia.train --steps 500
python -m src.amparada_ia.export_c
cc -O2 -std=c11 c_runtime/amparada.c -lm -o runs/amparada-c
```

O binário C recebe o caminho do arquivo e uma sequência de IDs de tokens; retorna logits para a última posição e o ID de maior pontuação. O vocabulário pode ser consultado no checkpoint/tokenizador Python.

A implementação é uma referência de interoperabilidade. Antes de uso, compare logits contra PyTorch em entradas fixas, incluindo tolerância numérica, e adicione testes automatizados de paridade.
