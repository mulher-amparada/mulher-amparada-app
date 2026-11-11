# 🫆 Amparada IA — modelo experimental

Este diretório contém uma prova de conceito própria de modelo de linguagem, treinada do zero com PyTorch.

## Tokenização BPE

O treinamento usa agora um tokenizador BPE (Byte Pair Encoding) simples, aprendido a partir de `data/treino.txt`. Ele combina caracteres frequentes em unidades maiores, como pedaços de palavras. Não exige bibliotecas adicionais.

O vocabulário é salvo dentro do checkpoint para que geração e avaliação usem exatamente os mesmos IDs. O checkpoint antigo baseado em caracteres não é compatível com o novo modelo BPE.

## Requisitos

- Python 3.10 ou superior
- PyTorch 2.2 ou superior
- NumPy conforme `requirements.txt`

## Treinar

Execute na pasta `Amparada IA`, com o ambiente virtual ativado:

```bash
python -m src.amparada_ia.train --steps 2000
```

Em celulares com pouca memória, comece com:

```bash
python -m src.amparada_ia.train --steps 300 --batch-size 2 --block-size 64 --n-embd 32 --n-head 4 --vocab-size 256
```

O checkpoint novo será salvo em `runs/amparada_ia_bpe.pt`. O arquivo de treino original não é apagado. O vocabulário BPE é aprendido a partir do corpus de treino, não da validação.

## Gerar texto

```bash
python -m src.amparada_ia.generate --prompt "Tecnologia responsável" --tokens 120
```

## Avaliar

```bash
python -m src.amparada_ia.evaluate --batches 20 --batch-size 2
```

BPE pode ajudar a representar palavras e subpalavras com menos posições do que a tokenização por caractere, mas não garante frases coerentes. Fluência depende também de corpus amplo e de qualidade, capacidade do modelo, treinamento e avaliação.

## Dados e segurança

Use somente textos próprios, sintéticos ou de origem e licença verificadas. Preserve atribuições e condições de licença. Não adicione relatos privados, nomes, endereços, conversas ou informações sobre situações reais de violência.

Este protótipo não deve ser apresentado como serviço de acolhimento, aconselhamento jurídico, médico ou de emergência. Não conecte esta versão experimental a usuárias nem a fluxos reais de atendimento sem avaliação humana, revisão de segurança, privacidade, acessibilidade e proteção de dados.
