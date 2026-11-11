# Amparada IA — modelo experimental do zero

Este diretório inicia a pesquisa de uma IA própria para o ecossistema Mulher Amparada.

## O que significa “do zero” aqui?

O protótipo implementa sua própria arquitetura de linguagem e inicializa todos os pesos aleatoriamente. Ele **não carrega pesos de outro modelo** nem utiliza um chatbot pronto. PyTorch é usado somente como biblioteca de operações tensoriais, diferenciação automática e treinamento.

A primeira versão é um modelo de linguagem pequeno, baseado em Transformer Decoder-only, treinado para prever o próximo caractere. É uma experiência educacional, não um assistente inteligente pronto.

## Estado atual

- [x] Tokenizador simples por caractere
- [x] Blocos Transformer causais
- [x] Treinamento por previsão do próximo token
- [x] Geração autoregressiva
- [x] Corpus sintético separado para validação
- [x] Script de avaliação com perda e perplexidade
- [x] Testes automatizados básicos
- [x] Documentação da arquitetura
- [ ] Experimentos com tokenização por subpalavras
- [ ] Otimização de inferência em C (etapa futura)
- [ ] Avaliação de segurança, privacidade e acessibilidade antes de qualquer uso real

## Estrutura

```text
Amparada IA/
├── data/
│   ├── treino.txt
│   └── validacao.txt
├── src/
│   └── amparada_ia/
│       ├── __init__.py
│       ├── tokenizer.py
│       ├── model.py
│       ├── train.py
│       ├── generate.py
│       └── evaluate.py
├── tests/
│   ├── test_tokenizer.py
│   └── test_model.py
├── ARCHITECTURE.md
├── .gitignore
└── requirements.txt
```

## Requisitos

- Python 3.10 ou superior
- PyTorch 2.2 ou superior
- CPU funciona para os experimentos pequenos; GPU compatível acelera o treinamento

Instale as dependências:

```bash
python -m venv .venv
# Linux/macOS:
source .venv/bin/activate
# Windows:
# .venv\\Scripts\\activate

pip install -r requirements.txt
```

## Treinar

Execute a partir deste diretório:

```bash
python -m src.amparada_ia.train --steps 500
```

O checkpoint será salvo em `runs/amparada_ia.pt`.

Para usar uma GPU NVIDIA compatível com sua instalação do PyTorch:

```bash
python -m src.amparada_ia.train --device cuda --steps 500
```

## Gerar texto

Depois do treinamento:

```bash
python -m src.amparada_ia.generate --prompt "O respeito"
```

A geração é experimental. Como o corpus inicial é minúsculo, a saída provavelmente será repetitiva, incoerente ou conterá erros. Isso é esperado nesta primeira prova de conceito.

## Testes automatizados

A partir desta pasta:

```bash
python -m unittest discover -s tests -v
```

Os testes verificam o tokenizador, dimensões de saída, cálculo da perda, máscara causal e geração. Eles não comprovam qualidade linguística nem segurança do modelo.

## Avaliar

Depois do treinamento, execute:

```bash
python -m src.amparada_ia.evaluate --batches 20
```

A avaliação usa `data/validacao.txt`, separado do corpus de treinamento. O vocabulário é construído no treinamento; o texto de validação deve usar caracteres presentes nesse vocabulário.

A perda mede erro de previsão. A perplexidade é uma transformação dessa perda. Nenhuma das duas métricas mede compreensão, factualidade, segurança ou qualidade de acolhimento.

## Dados e privacidade

Os arquivos de treinamento e validação contêm somente frases demonstrativas e sintéticas. Não adicione relatos privados, nomes, endereços, conversas de usuárias ou informações sobre situações reais de violência. Dados reais exigiriam avaliação jurídica, ética, de privacidade e de segurança, além de base legal e proteção apropriadas.

## Segurança e limites

- Este protótipo **não deve ser apresentado como serviço de acolhimento, aconselhamento jurídico, médico ou de emergência**.
- Não conecte esta versão experimental a usuárias nem a fluxos de atendimento real.
- Não confie em texto gerado como fonte factual. Modelos podem inventar informações.
- Antes de qualquer implantação, serão necessários testes, avaliação humana, proteção de dados, mecanismos de denúncia e encaminhamento para serviços humanos competentes.
- A finalidade social do projeto não substitui validação técnica nem avaliação de riscos.

**Amparada IA é um experimento de pesquisa e aprendizado. Não é uma versão pronta para uso público.**
