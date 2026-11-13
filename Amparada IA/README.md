# 🫆Amparada IA — modelo experimental:

Esta pasta contém uma prova de conceito de modelo de linguagem implementada com PyTorch e treinada do zero. É um projeto experimental para estudar tokenização, treinamento e geração de texto; não é um assistente pronto para atendimento.

## Estado atual:

- Arquitetura: Transformer Decoder-only.
- Tokenização: BPE (Byte Pair Encoding) simples, implementado no projeto.
- Objetivo: prever o próximo token de uma sequência.
- Dados: arquivos UTF-8 separados para treino e validação.
- Checkpoint padrão: `runs/amparada_ia_bpe.pt`.

O checkpoint armazena os pesos, a configuração e o vocabulário/fusões do tokenizador. Checkpoints antigos baseados em caracteres não são compatíveis com o formato BPE atual.

## Tokenização BPE:

O tokenizador separa inicialmente palavras, sequências de espaços e grupos de pontuação. Depois aprende fusões de pares de símbolos frequentes no corpus de treino e reaplica essas fusões na ordem aprendida.

`--vocab-size` é uma meta, não um limite rígido: se o conjunto inicial de caracteres já for maior que o valor informado, o vocabulário final poderá ultrapassá-lo. Caracteres ausentes no vocabulário de treino são convertidos para `<unk>`.

## Requisitos:

- Python 3.10 ou superior
- PyTorch 2.2 ou superior
- NumPy conforme `requirements.txt`

## Treinar:

Execute dentro desta pasta (`Amparada IA`), com o ambiente virtual ativado:

```bash
python -m src.amparada_ia.train --steps 2000
```

Para começar em um celular com memória limitada:

```bash
python -m src.amparada_ia.train --steps 350 --batch-size 1 --block-size 64 --n-embd 32 --n-head 4 --vocab-size 256
```

O corpus padrão é `data/treino.txt`; a validação usa `data/validacao.txt`. O tokenizador é aprendido somente com o corpus de treino. A validação não atualiza os pesos. O arquivo original de treino não é apagado.

O consumo de memória depende principalmente de `--batch-size`, `--block-size`, `--n-embd`, `--n-head` e `--n-layer`. Aumentar `--steps` aumenta o número de atualizações, mas não garante respostas melhores.

## RAG local:

A interface web usa `src/amparada_ia/rag.py` para pesquisar os arquivos de texto já existentes em `data/`. A busca BM25 é executada localmente, sem APIs, serviços externos ou dependências adicionais. São aceitos arquivos `.txt`, `.md`, `.json` e `.csv`; o índice é mantido em memória e atualizado quando os arquivos mudam. Os arquivos originais permanecem intactos. O servidor inclui até três trechos recuperados no contexto enviado ao modelo e informa os arquivos consultados.

O RAG recupera passagens relevantes, mas não garante que o modelo experimental interprete ou utilize essas passagens corretamente. A geração continua sujeita às limitações descritas acima.

## Gerar texto:

```bash
python -m src.amparada_ia.generate --prompt "Tecnologia responsável" --tokens 120
```

A geração usa o tokenizador armazenado no checkpoint para manter os mesmos IDs do treinamento.

## Avaliar:

```bash
python -m src.amparada_ia.evaluate --batches 20 --batch-size 2
```

A avaliação calcula a perda em lotes do conjunto de validação. Perda menor pode indicar melhor previsão nesse conjunto, mas não comprova compreensão, factualidade ou segurança.

## Limitações:

BPE pode representar partes frequentes de palavras com menos tokens do que a tokenização por caracteres, mas não cria conhecimento por si só. Fluência depende também da quantidade e qualidade do corpus, da capacidade do modelo e do treinamento. Um corpus e um modelo pequenos normalmente produzem texto pouco coerente.

Este protótipo pode repetir padrões, errar fatos ou gerar conteúdo inadequado. Não deve ser apresentado como serviço de acolhimento, aconselhamento jurídico, médico ou de emergência, nem conectado a fluxos reais de atendimento sem avaliação humana e revisão de segurança, privacidade, acessibilidade e proteção de dados.

## Dados e segurança:

Use somente textos próprios, sintéticos ou de origem e licença verificadas. Preserve atribuições e condições de licença. Não adicione relatos privados, nomes, endereços, conversas ou informações identificáveis sobre situações reais de violência.