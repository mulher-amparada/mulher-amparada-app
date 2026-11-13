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

## RAG local (Retrieval-Augmented Generation):

A interface web integra um mecanismo de recuperação local em `src/amparada_ia/rag.py`. Ele pesquisa os dados que já existem em `data/` e acrescenta trechos relevantes ao contexto enviado ao Transformer. Não é necessário treinar novamente o modelo para indexar esses arquivos.

### Como funciona:

1. **Leitura:** percorre recursivamente os arquivos suportados dentro de `data/`.
2. **Fragmentação:** divide o texto em trechos de até 900 caracteres, com sobreposição de 140 caracteres para preservar parte do contexto entre trechos.
3. **Indexação:** normaliza os termos (incluindo remoção de acentos) e calcula frequências para a busca.
4. **Recuperação:** usa BM25 para classificar os trechos em relação à pergunta e seleciona até três resultados.
5. **Geração:** o servidor codifica os trechos recuperados com o mesmo tokenizador BPE do checkpoint e os coloca antes da pergunta no contexto do modelo.
6. **Fontes:** a API retorna os caminhos relativos dos arquivos recuperados para a interface mostrar de onde veio o contexto.

### Dados e privacidade:

- Diretório de origem: `data/`, incluindo subpastas.
- Formatos lidos: `.txt`, `.md`, `.json` e `.csv`, em UTF-8.
- Busca: BM25 lexical; não utiliza embeddings nem busca semântica.
- Execução: local, sem APIs, serviços externos, banco vetorial ou dependências adicionais.
- O índice fica em memória e é reconstruído quando a assinatura dos arquivos (caminho, data de modificação e tamanho) muda.
- Os arquivos originais não são modificados. Arquivos ocultos e links simbólicos não são indexados.

### Executar a interface web:

Na pasta `Amparada IA`, com os requisitos instalados:

```bash
python web/server.py
```

O servidor local disponibiliza:

- `GET /api/status`: estado do modelo e estatísticas do índice RAG (`rag_ready`, `rag_files` e `rag_chunks`).
- `POST /api/chat`: recebe uma mensagem, recupera até três trechos e retorna a resposta experimental, os arquivos consultados e a quantidade de trechos recuperados.

O servidor foi projetado para escutar em `127.0.0.1`; não o exponha diretamente à internet.

### Testes automatizados:

Os testes do mecanismo RAG podem ser executados na pasta `Amparada IA`:

```bash
python -m unittest discover -s tests -p "test_rag.py" -v
```

O workflow `.github/workflows/teste-rag.yml` executa esses testes no GitHub Actions quando os arquivos relevantes mudam ou quando o workflow é iniciado manualmente. Na execução registrada durante a implementação, os três testes passaram: recuperação de conteúdo relevante, atualização do índice após alteração de arquivo e exclusão de formatos não suportados.

### Limitações importantes:

BM25 encontra correspondências lexicais; não entende sinônimos ou significado como um sistema de embeddings poderia. A recuperação de trechos não garante que o Transformer os compreenda, cite corretamente ou produza uma resposta factual. O modelo continua sendo experimental, treinado para prever o próximo token, e pode ignorar ou distorcer o contexto recuperado. Os testes automatizados verificam o mecanismo de recuperação, não a qualidade ou segurança das respostas geradas.

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