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
- [ ] Avaliação com conjunto de validação separado e métricas
- [ ] Testes automatizados e reprodutibilidade ampliada
- [ ] Experimentos com tokenização por subpalavras
- [ ] Otimização de inferência em C (etapa futura)
- [ ] Avaliação de segurança, privacidade e acessibilidade antes de qualquer uso real

## Estrutura

```text
Amparada IA/
├── data/
│   └── treino.txt
├── src/
│   └── amparada_ia/
│       ├── __init__.py
│       ├── tokenizer.py
│       ├── model.py
│       ├── train.py
│       └── generate.py
├── .gitignore
└── requirements.txt
```

## Requisitos

- Python 3.10 ou superior
- PyTorch 2.2 ou superior
- CPU funciona para o experimento pequeno; GPU compatível acelera o treinamento

Instale as dependências:

```bash
python -m venv .venv
# Linux/macOS:
source .venv/bin/activate
# Windows:
# .venv\Scripts\activate

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

## Dados e privacidade

O arquivo `data/treino.txt` contém somente frases demonstrativas e sintéticas. Não adicione relatos privados, nomes, endereços, conversas de usuárias ou informações sobre situações reais de violência. Dados reais exigiriam base legal, consentimento quando aplicável, minimização, proteção e revisão especializada.

## Segurança e limites

- Este protótipo **não deve ser apresentado como serviço de acolhimento, aconselhamento jurídico, médico ou de emergência**.
- Não conecte esta versão experimental a usuárias nem a fluxos de atendimento real.
- Não confie em texto gerado como fonte factual. Modelos podem inventar informações.
- Antes de qualquer implantação, serão necessários testes, avaliação humana, proteção de dados, mecanismos de denúncia e encaminhamento para serviços humanos competentes.
- A finalidade social do projeto não substitui validação técnica nem avaliação de riscos.

## Próximas etapas de pesquisa

1. Aumentar e documentar um corpus de treinamento lícito, diverso e revisado.
2. Criar testes quantitativos e qualitativos de qualidade.
3. Medir uso de memória, tempo de treinamento e inferência.
4. Avaliar vieses, respostas inseguras e alucinações.
5. Só então estudar uma interface e integração com o aplicativo.

**Amparada IA é um experimento de pesquisa e aprendizado. Não é uma versão pronta para uso público.**
