# Interface web local da Amparada IA

Esta interface conversa com o checkpoint experimental criado pelo projeto. O modelo atual é pequeno, treinado por caracteres e pode produzir continuações incoerentes.

## 1. Treinar o modelo

A partir da pasta Amparada IA:

    python -m pip install -r requirements.txt
    python -m src.amparada_ia.train --steps 500

O treinamento cria runs/amparada_ia.pt.

## 2. Iniciar o site e a API local

Ainda na pasta Amparada IA:

    python web/server.py

Abra http://127.0.0.1:8000 no navegador.

O servidor escuta somente em 127.0.0.1 e não deve ser publicado diretamente na internet.

## Limites e privacidade

- Demonstração técnica, não canal de acolhimento ou atendimento.
- Não usar para emergência, triagem de risco ou aconselhamento profissional.
- Não enviar dados pessoais, relatos reais, endereços ou informações sensíveis.
- Não possui autenticação, moderação robusta, proteção contra abuso ou infraestrutura de produção.
- O histórico existe apenas na tela e não é salvo pelo site.
- GitHub Pages hospeda arquivos estáticos e não executa esta API Python. Acesso público exige backend hospedado e avaliação de segurança, privacidade, disponibilidade e custos.

## Endpoints

- GET /api/status: informa se o checkpoint foi carregado.
- POST /api/chat: recebe {"message":"..."} e retorna uma continuação experimental.
