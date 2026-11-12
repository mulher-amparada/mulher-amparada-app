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

Use somente textos próprios, sintéticos ou de origem e licença verificadas. Preserve atribuições e condições de licença. Não adicione relatos privados, nomes, endereços, conversas ou informações identificáveis sobre situações reais.

## Base local de conhecimento (RAG)

O módulo `src/amparada_ia/retrieval.py` e os documentos em `knowledge/base.json` continuam disponíveis para testes e desenvolvimento de recuperação local. A rota web `/api/chat`, nesta versão, faz pesquisa na internet em tempo real via SearXNG.

## Pesquisa na internet em tempo real com SearXNG

A rota `/api/chat` consulta o endpoint JSON de uma instância SearXNG e apresenta títulos, resumos e links das fontes. A configuração padrão aponta para uma instância local em `http://127.0.0.1:8080`. Não é necessária chave de API.

### Iniciar uma instância local

Com Docker instalado, execute:

```bash
docker run --rm -d --name amparada-searxng -p 8080:8080 searxng/searxng
```

Depois, em outro terminal, dentro da pasta `Amparada IA`, inicie o servidor:

```bash
python -m web.server
```

Abra `http://127.0.0.1:8000`. Para apontar para outra instância SearXNG, configure o endereço-base no ambiente do servidor:

```bash
export SEARXNG_URL="https://seu-servidor-searxng.example"
python -m web.server
```

A instância precisa permitir o formato de resposta JSON. A disponibilidade e a política de uso dependem da instância e dos mecanismos de busca que ela consulta. Hospedar sua própria instância evita uma cota de API de um fornecedor, mas não significa consultas infinitas garantidas: mecanismos externos podem aplicar limites, bloqueios ou indisponibilidade. Instâncias públicas também podem registrar consultas; não envie nomes, telefones, endereços, relatos privados ou dados identificáveis de usuárias.

**Privacidade e segurança:** as perguntas são transmitidas à instância SearXNG e, conforme sua configuração, aos mecanismos externos consultados. Antes de disponibilizar o serviço a outras pessoas, implemente minimização de dados, política de privacidade, consentimento apropriado, limitação de requisições e revisão de segurança.

**Limitação da versão:** esta integração apresenta resumos devolvidos pelo mecanismo de busca. Ela não equivale a uma síntese feita por um modelo de linguagem avançado nem garante que cada página foi lida integralmente. Confira as fontes antes de tomar decisões importantes.
