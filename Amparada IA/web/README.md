# 🌐Interface web local da Amparada IA:

Esta interface conversa com o checkpoint experimental criado pelo projeto e usa RAG lexical local (BM25) para recuperar trechos dos arquivos existentes em `data/`. O modelo continua pequeno e pode produzir continuações incoerentes.

## 1. Treinar o modelo:

A partir da pasta Amparada IA:

    python -m pip install -r requirements.txt
    python -m src.amparada_ia.train --steps 500

O treinamento cria runs/amparada_ia.pt.

## 2. Iniciar o site e a API local:

Ainda na pasta Amparada IA:

    python web/server.py

Abra http://127.0.0.1:8000 no navegador.

O servidor escuta somente em 127.0.0.1 e não deve ser publicado diretamente na internet. O índice é criado localmente a partir de `data/` e atualizado quando arquivos compatíveis são modificados.

## Limites e privacidade:

- Demonstração técnica, não canal de acolhimento ou atendimento.
- Não usar para emergência, triagem de risco ou aconselhamento profissional.
- Não enviar dados pessoais, relatos reais, endereços ou informações sensíveis.
- Não possui autenticação, moderação robusta, proteção contra abuso ou infraestrutura de produção.
- O histórico existe apenas na tela e não é salvo pelo site.
- GitHub Pages hospeda arquivos estáticos e não executa esta API Python. Acesso público exige backend hospedado e avaliação de segurança, privacidade, disponibilidade e custos.

## Endpoints:

- GET /api/status: informa o estado do checkpoint e do índice RAG local.
- POST /api/chat: recebe {"message":"..."}, recupera até três trechos locais, inclui esses trechos no contexto do modelo e retorna a continuação experimental e os arquivos consultados.

## RAG local:

- Fontes: arquivos `.txt`, `.md`, `.json` e `.csv` dentro de `data/` (incluindo subpastas).
- Recuperação: BM25, sem serviços externos, banco vetorial ou dependências adicionais.
- Os arquivos originais não são alterados. O índice fica apenas em memória e é reconstruído quando detecta alterações nos arquivos.
- O RAG recupera contexto; ele não transforma o pequeno modelo experimental em um assistente confiável nem garante que a continuação use corretamente as fontes.
