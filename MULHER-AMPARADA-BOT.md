# 🤖 Mulher Amparada Bot

O **Mulher Amparada Bot** é uma GitHub App criada especificamente para executar e controlar as automações do projeto **Mulher Amparada**, utilizando uma identidade própria no GitHub em vez de depender diretamente da identidade padrão `github-actions[bot]`.

e ele executa todos os dias as 15:00h!

A autenticação é realizada por meio de uma **GitHub App**, utilizando um **JWT assinado com a Private Key da aplicação**. Depois disso, o bot obtém um **Installation Access Token**, utilizado para acessar a API do GitHub de acordo com as permissões concedidas à instalação.

## 🎯 Objetivo

O bot foi criado para:

- 🤖 possuir uma identidade própria no GitHub;
- 🔐 autenticar-se por meio de uma GitHub App;
- ⚙️ disparar workflows do projeto;
- 🔑 manter as credenciais protegidas em GitHub Secrets;
- 📦 atuar somente no repositório autorizado;
- 🚫 não depender diretamente da identidade `github-actions[bot]`;
- 🔒 evitar armazenar chaves privadas dentro do código-fonte.

## 🏗️ Estrutura

    Mulher Amparada Bot
    │
    ├── GitHub App
    │   ├── App ID
    │   ├── Client ID
    │   ├── Private Key
    │   └── Installation
    │
    ├── GitHub Actions
    │   └── .github/workflows/automation-bot.yml
    │
    └── C++
        └── tools/automation_bot.cpp

## 🔄 Fluxo de funcionamento

    GitHub Actions
           │
           ▼
    automation-bot.yml
           │
           ▼
    automation_bot.cpp
           │
           ├── App ID
           ├── Installation ID
           └── Private Key
                  │
                  ▼
            JWT assinado
                  │
                  ▼
       GitHub Installation API
                  │
                  ▼
       Installation Access Token
                  │
                  ▼
         GitHub Actions API
                  │
                  ▼
           automations.yml

## 🏷️ Identidade

A identidade utilizada pelo sistema é:

**Mulher Amparada Bot**

Ela é uma **GitHub App**, e não apenas um nome configurado no Git.

Alterar `git config user.name` não cria uma nova identidade de autenticação no GitHub.

A GitHub App possui sua própria identidade, permissões e credenciais.

## 🔑 Credenciais

O bot utiliza três informações armazenadas como **Repository Secrets**:

    MULHER_AMPARADA_APP_ID
    MULHER_AMPARADA_INSTALLATION_ID
    MULHER_AMPARADA_PRIVATE_KEY

### `MULHER_AMPARADA_APP_ID`

É o identificador numérico da GitHub App.

Ele identifica a aplicação:

    Mulher Amparada Bot

Não deve ser confundido com o **Client ID**.

### `MULHER_AMPARADA_INSTALLATION_ID`

Identifica a instalação específica da GitHub App.

A instalação utilizada pelo projeto possui o ID:

    169973406

A instalação está associada ao repositório:

    mulher-amparada/mulher-amparada-app

### `MULHER_AMPARADA_PRIVATE_KEY`

É a chave privada gerada pela GitHub App.

Ela é utilizada para assinar o JWT.

Por segurança, a chave:

- não fica no repositório;
- não fica no código C++;
- não fica diretamente no workflow;
- não deve ser publicada;
- não deve ser compartilhada;
- fica armazenada como GitHub Secret.

Neste projeto, a chave foi convertida para **Base64** antes de ser armazenada no Secret.

O programa C++ realiza a decodificação antes de utilizá-la.

## 🔐 Base64

A chave PEM normalmente possui várias linhas:

    -----BEGIN PRIVATE KEY-----
    ...
    -----END PRIVATE KEY-----

Para evitar problemas durante o armazenamento, ela é convertida para Base64.

O fluxo é:

    Private Key PEM
          │
          ▼
        Base64
          │
          ▼
    GitHub Secret
          │
          ▼
    automation_bot.cpp
          │
          ▼
      Decodificação
          │
          ▼
    Private Key PEM

Base64 **não é criptografia**. É apenas uma representação textual dos dados.

A proteção da chave depende do armazenamento seguro no GitHub Secret.

## ⚙️ Workflow

O arquivo responsável por executar o bot é:

    .github/workflows/automation-bot.yml

Ele pode ser executado automaticamente ou manualmente.

    on:
      schedule:
        - cron: "0 15 * * *"
      workflow_dispatch:

O `schedule` utiliza o horário UTC do GitHub.

O `workflow_dispatch` permite executar o bot manualmente pela interface do GitHub Actions.

## 📦 Dependências

O workflow instala:

    g++
    libcurl4-openssl-dev
    libssl-dev

### G++

Utilizado para compilar:

    tools/automation_bot.cpp

### libcurl

Utilizado para realizar requisições HTTP à API do GitHub.

### OpenSSL

Utilizado para:

- ler a Private Key;
- realizar operações RSA;
- assinar o JWT;
- utilizar RS256;
- trabalhar com Base64.

## 🧩 Compilação

O código é compilado com:

    g++ \
      tools/automation_bot.cpp \
      -o automation-bot \
      -std=c++17 \
      -lcurl \
      -lssl \
      -lcrypto

O resultado é o executável:

    automation-bot

## 🔐 Variáveis de ambiente

O workflow disponibiliza os Secrets ao programa:

    env:
      MULHER_AMPARADA_APP_ID: ${{ secrets.MULHER_AMPARADA_APP_ID }}
      MULHER_AMPARADA_INSTALLATION_ID: ${{ secrets.MULHER_AMPARADA_INSTALLATION_ID }}
      MULHER_AMPARADA_PRIVATE_KEY: ${{ secrets.MULHER_AMPARADA_PRIVATE_KEY }}
      GITHUB_REPOSITORY: ${{ github.repository }}

O programa C++ lê essas informações por meio de `std::getenv()`.

Dessa forma, nenhuma credencial precisa ser escrita diretamente no código.

## 🪪 Autenticação

A autenticação acontece em duas etapas principais.

### 1. Geração do JWT

O bot cria um JWT contendo:

    iat
    exp
    iss

Onde:

- `iat` representa o momento em que o token foi emitido;
- `exp` representa o momento de expiração;
- `iss` representa o App ID.

O JWT é assinado utilizando:

    RS256

e a Private Key da GitHub App.

### 2. Installation Access Token

Depois de gerar o JWT, o bot solicita um token específico para a instalação da App.

A solicitação é realizada para:

    POST /app/installations/{installation_id}/access_tokens

O GitHub retorna um:

    Installation Access Token

Esse token representa a instalação da GitHub App e possui as permissões concedidas a ela.

## 🚀 Disparo do workflow

Depois de obter o Installation Access Token, o bot utiliza a API do GitHub para disparar:

    .github/workflows/automations.yml

No repositório:

    mulher-amparada/mulher-amparada-app

O workflow é solicitado para a branch:

    main

com o conteúdo:

    {
      "ref": "main"
    }

## 🛡️ User-Agent

As requisições HTTP utilizam:

    User-Agent: mulher-amparada-bot

Isso identifica o cliente que está realizando as requisições.

O `libcurl` também recebe esse User-Agent.

## 📡 Headers da API

As requisições utilizam:

    Accept: application/vnd.github+json
    User-Agent: mulher-amparada-bot
    X-GitHub-Api-Version: 2022-11-28

Para requisições POST com JSON:

    Content-Type: application/json

## 🔒 Permissões

A GitHub App deve receber somente as permissões necessárias.

Configuração utilizada:

    Actions
    └── Read and write

    Contents
    └── Read-only

A instalação deve ser limitada ao repositório:

    mulher-amparada-app

## 📁 Arquivos

Os principais arquivos do sistema são:

    .github/
    └── workflows/
        └── automation-bot.yml

    tools/
    └── automation_bot.cpp

### `.github/workflows/automation-bot.yml`

Responsável por:

- iniciar o bot;
- instalar dependências;
- compilar o código C++;
- fornecer os Secrets;
- executar o programa.

### `tools/automation_bot.cpp`

Responsável por:

- ler as credenciais;
- decodificar a Private Key;
- gerar o JWT;
- autenticar a GitHub App;
- solicitar o Installation Access Token;
- chamar a API do GitHub;
- disparar o workflow de automações.

## 🔒 Segurança

As credenciais do bot nunca devem ser colocadas diretamente no código.

Não devem ser adicionados ao Git:

    *.pem

nem arquivos contendo:

    MULHER_AMPARADA_PRIVATE_KEY

A Private Key deve permanecer exclusivamente no GitHub Secrets.

Também não deve ser compartilhada em:

- commits;
- Issues;
- Pull Requests;
- README público;
- documentação pública;
- mensagens;
- logs.

## 🧪 Execução manual

Para testar o bot:

    GitHub
      ↓
    Actions
      ↓
    Mulher Amparada Bot
      ↓
    Run workflow

O workflow então:

    compila
       ↓
    executa
       ↓
    gera JWT
       ↓
    obtém Installation Token
       ↓
    chama GitHub API
       ↓
    dispara automations.yml

## ✅ Resultado esperado

Quando a autenticação funcionar, o log deverá indicar etapas semelhantes a:

    🤖 Mulher Amparada Bot
    App ID: ********
    Installation ID: 169973406
    ✅ JWT gerado.
    HTTP 201
    ✅ Installation Access Token obtido.
    HTTP 204
    🚀 Workflow automations.yml acionado pelo Mulher Amparada Bot.

Os valores sensíveis não devem ser exibidos nos logs.

## 🧠 Resumo

O **Mulher Amparada Bot** funciona como uma identidade automatizada independente do `github-actions[bot]`.

Seu fluxo é:

    GitHub App
        │
        ├── App ID
        ├── Installation ID
        └── Private Key
                 │
                 ▼
          automation_bot.cpp
                 │
                 ▼
              JWT RS256
                 │
                 ▼
      Installation Access Token
                 │
                 ▼
         GitHub Actions API
                 │
                 ▼
          automations.yml

Dessa maneira, as automações do projeto podem ser iniciadas por uma identidade própria denominada **Mulher Amparada Bot**, mantendo as credenciais protegidas e separadas da identidade padrão do GitHub Actions.