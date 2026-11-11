# 🫆 Amparada IA — modelo experimental

Este diretório contém uma prova de conceito própria de modelo de linguagem. A arquitetura é inicializada com pesos aleatórios; PyTorch fornece operações tensoriais, diferenciação automática e treinamento.

## Evolução do treinamento

O treinamento agora:
- usa `data/treino.txt` e `data/validacao.txt` como arquivos separados;
- verifica se a validação contém caracteres presentes no vocabulário de treino;
- calcula perdas médias de treino e validação em intervalos;
- permite ajustar tamanho do lote, contexto e dimensões do modelo;
- salva configuração, tokenizador e metadados no checkpoint.

O corpus atual é **demonstrativo e pequeno**. Ele serve para testar o pipeline, não para produzir um assistente fluente. Aumentar etapas não substitui um corpus amplo, variado, revisado e com licença adequada.

## Requisitos

- Python 3.10 ou superior
- PyTorch 2.2 ou superior
- NumPy conforme `requirements.txt`

## Treinar no computador ou ambiente Linux

Execute na pasta `Amparada IA`, com o ambiente virtual ativado:

```bash
python -m src.amparada_ia.train --steps 2000
```

Os padrões foram reduzidos para um experimento mais leve em CPU: lote 4, contexto 96, embedding 64 e duas camadas. O tempo depende do dispositivo. Em celulares com pouca memória, comece com:

```bash
python -m src.amparada_ia.train --steps 300 --batch-size 2 --block-size 64 --n-embd 32
```

O checkpoint será salvo em `runs/amparada_ia.pt`.

Parâmetros disponíveis incluem `--validation-data`, `--eval-interval`, `--eval-batches`, `--batch-size`, `--block-size`, `--n-embd`, `--n-head` e `--n-layer`.

## Gerar texto

```bash
python -m src.amparada_ia.generate --prompt "Tecnologia responsável" --tokens 120
```

A geração continua experimental. O modelo prevê o próximo caractere; não possui compreensão garantida. Frases coerentes exigem corpus muito maior e mais diverso, experimentos de tokenização e avaliação qualitativa.

## Avaliar

```bash
python -m src.amparada_ia.evaluate --batches 20
```

Perda e perplexidade medem aspectos da previsão de tokens. Não medem compreensão, factualidade, segurança ou qualidade de acolhimento.

## Dados e segurança

Use somente textos próprios, sintéticos ou de origem e licença verificadas. Não adicione relatos privados, nomes, endereços, conversas ou informações sobre situações reais de violência.

Este protótipo não deve ser apresentado como serviço de acolhimento, aconselhamento jurídico, médico ou de emergência. Não conecte esta versão experimental a usuárias nem a fluxos reais de atendimento. Antes de qualquer implantação, são necessárias avaliação humana, revisão de segurança, privacidade, acessibilidade e proteção de dados.
