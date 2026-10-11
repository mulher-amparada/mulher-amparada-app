<div align="center">
  <img src="https://mulher-amparada.github.io/mulher-amparada-app/user1.png" width="250">
</div>

<div align="center">
  <img src="https://mulher-amparada.github.io/mulher-amparada-app/title.png">
</div>


Um projeto totalmente gratuito e livre de anúncios, projetado por um menino autista laudado nível 1 de 15 anos!, usando o apoio do chatgpt, sem curso formal!

e eu programei todo esse projeto no A16 5g da samsung, e nas primeiras versoes, onde nem tinha os recursos, ja programei ele num app de A-IDE, num A05, e eu ja perdi vários projetos porque o celular nao aguentava, matava o projeto porque matou o processo de compilação!, e uma vez eu fiz o projeto do mulher amparada e eu mesmo fiz o app do mulher amparada (primeiro eu refiz porque o family link apagou a pasta segura samsung, depois na 2 vez que perdi portei tudo do apk compilado para descompilado, e depois perdi denovo mas ai eu ja tinha o código-fonte!)

E vale lembrar que antes todas as páginas eram html com webview, agora não são mais, só a função de navegador usa webview sem html, tudo é compose (no primeiro dia foram 10h de trabalho no segundo foram 7h se trabalho!)

e pra ser sincero, comecei esse projeto no dia 13/05/2026 e terminei no dia 10/10/2026!

**Commits totais de toda a história do projeto, (feitos por mim e pelos workflows do github actions!) = 5151**

E o projeto é = Open-source! (aderido no dia 02/10/2026)

# 🎉Projeto Finalizado:

https://github.com/mulher-amparada/mulher-amparada-app/wiki/%23-%F0%9F%8E%89-Projeto-Finalizado

# 👾Nossas contribuições!:

<img
  src="https://github.com/mulher-amparada/mulher-amparada-app/blob/main/docs%2Fobservatorio%2Fobservatorio-001.png"
  alt="Observatório do Mulher Amparada"
  width="100%"
/>

# 🩸 Sofrimento das mulheres"

<img
  src="https://github.com/mulher-amparada/mulher-amparada-app/blob/main/relatorios%2Fgraficos%2Fligue180-2019.png"
  alt="Observatório do Mulher Amparada"
  width="100%"
/>

# ⚠️MURAL DE AVISOS:

### Sobre como o projeto foi estruturado:

>Nossos termos e políticas: https://github.com/mulher-amparada/mulher-amparada-app/blob/main/TERMS%20AND%20POLICY.md

>Sobre as permissões: infelizmente, foi necessário configurar a HubActivity para não solicitar permissões automaticamente. Por isso, as permissões necessárias deverão ser concedidas manualmente pela usuária nas configurações do dispositivo. As permissões utilizadas por outras Activitys continuam sendo solicitadas normalmente pelo aplicativo.
>
> Ao entrar no aplicativo após sair dos disfarces, um aviso é exibido, bloqueando o acesso até que todas as permissões necessárias estejam concedidas. O aviso oferece à usuária a opção de acessar diretamente o popup de permissões do sistema. E se ainda houver alguma permissão necessária que não tenha sido concedida, o aviso continuará sendo exibido e o acesso permanecerá bloqueado. O aviso só desaparecerá quando todas as permissões necessárias estiverem concedidas. (caso o sistema nao consiga mostrar o popup de permissão novamente, ele abre a tela de configurações do app!)

>Vale lembrar que o projeto não substitui serviços oficiais do governo e também não garante segurança imediata, bem como as funções dependem do estado e hardware de cada aparelho!

> Vale lembrar: o Gerenciador de Arquivos do Mulher Amparada funciona principalmente como um visualizador de arquivos. O nome “Gerenciador de Arquivos” também faz parte do disfarce do aplicativo. Ele foi projetado dessa forma por uma questão de segurança: o aplicativo não oferece funções próprias para excluir, mover, copiar ou renomear arquivos, reduzindo o risco de apagar ou alterar acidentalmente algum arquivo importante — inclusive possíveis registros que a usuária queira preservar.

### Sobre as funções do projeto:

>ATENÇÃO: Reforço que as proteções que utilizam sensores podem não funcionar corretamente em alguns aparelhos, dependendo das limitações ou características do hardware da usuária.


> Sobre as proteções por movimento e escurecimento: caso ocorra alguma falha ou o aparelho da usuária não possua o sensor necessário, o aplicativo utiliza o microfone como alternativa. Ao detectar um barulho alto, a proteção é acionada.

> **Recurso de Privacidade: Escurecimento por Inclinação (Disfarce Rápido)**
>
>O aplicativo conta com uma funcionalidade exclusiva de privacidade, projetada para proteger as informações da usuária contra olhares curiosos. Ao inclinar o dispositivo, o aplicativo ativa instantaneamente um modo de disfarce visual, escurecendo a interface para simular que a tela está desligada ou que o celular está bloqueado.

>Aviso — O recurso de “Desembarque seguro” do Mulher Amparada é informativo e atualmente apresenta a legislação aplicável à Cidade de São Paulo, especialmente a Lei Municipal nº 16.490/2016 e sua regulamentação. Essa legislação não deve ser interpretada como uma regra válida em todo o Brasil. As regras sobre desembarque fora dos pontos podem variar conforme o município, o estado e o tipo de transporte. A carteirinha apresentada pelo aplicativo não é um documento oficial e não substitui a legislação vigente, regulamentações, orientações das empresas de transporte ou autoridades competentes. E antes de utilizar esse recurso em outra localidade, verifique a legislação específica aplicável ao local.

> Sobre as funções por sensores (proteção por barulho que liga para o 180, balançar o celular para pedir ajuda e escurecimento por inclinação): essas proteções podem ser ativadas e desativadas pela usuária diretamente no aplicativo. O funcionamento pode variar conforme os sensores e as características de hardware do dispositivo. Em caso de falha ou ausência do sensor necessário, algumas proteções podem utilizar o microfone como alternativa, quando aplicável.

### Estrutura de Telas Secretas (Acesso Biométrico):

>O aplicativo divide suas funcionalidades confidenciais em DUAS ÁREAS COMPLETAMENTE SEPARADAS no menu principal. Cada área possui sua própria proteção e requer autenticação independente por meio do sistema BiometricPrompt  utilizando BIOMETRIC_WEAK e DEVICE_CREDENTIAL:
>
>Área Protegida.
>
>e
>
>Área do amparo.

>Sobre a autenticação quando não há biometria ou credencial cadastrada:
>
>As áreas protegidas do aplicativo utilizam o BiometricPrompt com os autenticadores BIOMETRIC_WEAK e DEVICE_CREDENTIAL.
>
>Quando existe uma biometria cadastrada, o Android pode apresentar a autenticação biométrica, como impressão digital ou reconhecimento facial compatível.
>
>Quando não existe biometria cadastrada, mas o dispositivo possui uma credencial de segurança configurada, como PIN, padrão ou senha, o Android pode utilizar essa credencial como alternativa.
>
>Caso o dispositivo não possua nenhum dos métodos de autenticação aceitos configurado, não existe um método válido para desbloquear a área protegida. Nesse cenário, o aplicativo não deve considerar a autenticação como concluída nem liberar a área protegida simplesmente porque a biometria não está disponível.

# 🔗Seção de links e paginas:

### Direitos que toda mulher tem!

Conheça 100 direitos e garantias assegurados às mulheres pela legislação brasileira.

Para consultar a legislação completa e as referências utilizadas nesta seção, acesse**[LEIS.md](LEIS.md)**.

Conhecer seus direitos é importante para reconhecer situações de proteção, buscar ajuda quando necessário e entender as garantias previstas em lei.

### Conhecimentos para recuperar sua autonomia

Conhecimentos e informações para ajudar você a compreender melhor sua vida financeira, organizar seu dinheiro e fortalecer sua autonomia.

Para acessar o conteúdo completo sobre finanças, consulte**[Finanças](FINANÇAS.md)**.

### Mensagem de apoio e acolhimento para as usuárias

Uma carta para você

Esta carta foi feita para apoiar você em sua caminhada, trazendo conhecimentos e informações que podem ajudar a compreender melhor sua vida financeira, organizar seu dinheiro e fortalecer, cada vez mais, sua autonomia.

Você não precisa saber tudo de uma vez. Conhecimento também é uma forma de proteção, e entender suas próprias finanças pode ajudar você a tomar decisões com mais segurança e independência.

Para acessar o conteúdo completo sobre finanças, consulte a [Carta do desenvolvedor](ABOUT.md).

# 🏗Estrutura do projeto:

### Sobre como eu automatizo o projeto:

A documentação completa do arquivo `.github/workflows/automations.yml` está disponível em:

<a href="https://github.com/mulher-amparada/mulher-amparada-app/blob/main/pesquisas/workflow.md">
  📖 Pesquisas — Documentação do Workflow
</a>

E temos esse dependabot (.github/dependabot.yml):
```
version: 2

updates:
  - package-ecosystem: "gradle"
    directory: "/Código-fonte do app = Mulher Amparada"
    schedule:
      interval: "weekly"
      
      
```
      
### 🤖 Mulher Amparada Bot:

```
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
```

### ⚙️ Automações do Mulher Amparada:

```
O projeto **Mulher Amparada Pela Liberdade Feminina** utiliza um workflow automatizado do GitHub Actions para compilar o aplicativo Android, assinar e publicar o APK, construir e publicar o site, atualizar arquivos do projeto, gerar documentação, relatórios, imagens, sitemap e outros arquivos auxiliares.

O workflow principal está localizado em:

`.github/workflows/automations.yml`

---

# 🚀 Funcionamento geral

O workflow é executado automaticamente quando ocorre um `push` em qualquer branch:

    on:
      push:
        branches:
          - '**'

      workflow_dispatch:

Também pode ser executado manualmente através do GitHub Actions.

O workflow possui controle de concorrência para evitar execuções automáticas simultâneas da mesma referência:

    concurrency:
      group: ${{ github.workflow }}-${{ github.ref }}
      cancel-in-progress: ${{ github.event_name != 'workflow_dispatch' }}

Execuções automáticas anteriores podem ser canceladas quando uma nova execução automática é iniciada.

Execuções iniciadas manualmente através de `workflow_dispatch` não são canceladas por essa regra.

---

# 🔐 Permissões

O workflow utiliza:

    permissions:
      contents: write
      pages: write
      id-token: write
      actions: read

## `contents: write`

Permite:

- ler o repositório;
- modificar arquivos;
- criar commits;
- enviar alterações para a `main`;
- atualizar arquivos gerados automaticamente.

## `pages: write`

Permite publicar o site através do GitHub Pages.

## `id-token: write`

Permite utilizar os recursos de identidade necessários para a implantação do GitHub Pages.

## `actions: read`

Permite acessar informações e artifacts relacionados ao GitHub Actions.

---

# 🧩 Estrutura do workflow

O workflow é dividido em vários jobs:

- `build`
- `build-site`
- `deploy`
- `atualizar-commits`
- `sincronizar-devto`
- `sitemap`
- `indexnow`
- `lighthouse`
- `publicar-divulgacao`
- `observatorio`
- `atualizar-footer`
- `atualizar-relatorio-commits`
- `pesquisar-projeto`
- `gerar-linguagens`
- `salvar-na-main`

Esses jobs trabalham em conjunto para automatizar o ciclo de atualização do aplicativo, site e documentação.

---

# 📱 `build`

## Compilar APK Kotlin

O job `build` é responsável pela compilação do aplicativo Android.

Ele utiliza:

    runs-on: ubuntu-24.04

As principais variáveis utilizadas são:

    env:
      App1_DIR: "Código-fonte do app = Mulher Amparada"
      RELEASE_TAG: "app"
      RELEASE_APK_NAME: "app-debug-assinado.apk"

O diretório:

`Código-fonte do app = Mulher Amparada`

contém o projeto Android.

A Release utilizada pelo workflow possui a tag:

`app`

O APK final utilizado pela Release recebe o nome:

`app-debug-assinado.apk`

---

# 📥 Download do código

O workflow utiliza:

    actions/checkout@v6

para baixar o código do repositório.

A compilação utiliza inicialmente:

    fetch-depth: 1

porque não é necessário baixar todo o histórico para compilar o aplicativo.

---

# 🔎 Verificação do projeto Android

Antes de iniciar a compilação, o workflow verifica se o diretório do aplicativo existe.

Também verifica se o arquivo:

`gradlew`

está presente.

Caso a pasta ou o Gradle Wrapper não existam, o job é interrompido.

---

# ☕ Java 17

O ambiente Java é configurado através de:

    actions/setup-java@v6

com:

    distribution: temurin
    java-version: "17"
    cache: gradle

O cache do Gradle reduz downloads desnecessários durante compilações posteriores.

---

# 🤖 Android SDK 37

O workflow verifica se o Android SDK 37 está disponível no runner.

São exibidos:

- `ANDROID_HOME`;
- plataformas Android instaladas;
- plataformas Android 37 disponíveis.

Caso nenhuma plataforma Android 37 seja encontrada, a compilação é interrompida.

---

# 🔑 Permissão do Gradle

O workflow concede permissão de execução ao Gradle Wrapper:

    chmod +x gradlew

Isso garante que:

`./gradlew`

possa ser executado no ambiente Linux do GitHub Actions.

---

# 🏗️ Compilação do APK

A compilação é realizada com:

    ./gradlew assembleDebug --stacktrace

O resultado do Gradle também é salvo em:

`gradle-build.log`

O log é utilizado caso a compilação falhe.

Quando ocorre uma falha, o workflow adiciona ao resumo da execução:

- identificação da etapa;
- descrição do erro;
- últimas linhas do log do Gradle;
- código de saída.

---

# 📦 Localização do APK

Depois da compilação, o workflow procura um arquivo `.apk` dentro de:

`app/build/outputs/apk/debug`

O caminho encontrado é armazenado como output da etapa para ser utilizado posteriormente.

---

# ✍️ Assinatura do APK

O APK é assinado utilizando um keystore fornecido através dos seguintes secrets:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

O keystore é reconstruído temporariamente através de Base64.

Depois é utilizado o:

`apksigner`

fornecido pelo Android SDK.

O APK assinado recebe o nome:

`app-assinado.apk`

Após a assinatura, o workflow executa uma verificação utilizando:

    apksigner verify --verbose

O keystore temporário é removido após a utilização.

---

# 📤 Artifact do APK

O APK assinado é disponibilizado como artifact:

`APK-Kotlin-Assinado`

O arquivo enviado é:

`app-debug-assinado.apk`

Isso permite acessar o APK diretamente a partir da execução do GitHub Actions.

---

# 📏 Tamanho do APK

O workflow calcula o tamanho real do APK utilizando:

`stat`

O tamanho é calculado em bytes e convertido para MB.

Essas informações ficam disponíveis como outputs:

- `bytes`;
- `size_mb`;
- `size_formatado`.

---

# 🔐 SHA-256

O workflow calcula o hash SHA-256 do APK através de:

    sha256sum

O resultado é armazenado como output:

`sha256`

Esse valor é utilizado posteriormente na Release e no `download.html`.

---

# 📲 Atualização do APK na `main`

O APK assinado também é copiado para:

`Código-fonte do app = Mulher Amparada/app-release.apk`

O workflow atualiza a referência da `main` antes de realizar a cópia.

Caso exista uma alteração, é criado um commit:

`Atualizar APK assinado [skip ci]`

O commit é enviado para:

`main`

O `[skip ci]` evita iniciar novamente determinadas automações a partir desse commit.

---

# 🚀 Release

O workflow verifica se a Release:

`app`

existe.

Caso ela não exista, o job é interrompido.

---

# 🗑️ Remoção do APK anterior

Antes de publicar o novo APK, o workflow procura um asset com o nome:

`app-debug-assinado.apk`

Se existir, o asset antigo é excluído.

Isso evita manter várias versões do mesmo APK na Release.

---

# 📤 Publicação do APK

O novo APK é enviado para a Release:

`app`

O arquivo publicado é:

`app-debug-assinado.apk`

---

# 🔐 SHA-256 da Release

As notas da Release são lidas.

O workflow procura a informação:

`SHA-256 gerado nesta compilação:`

Se ela já existir, seu valor é atualizado.

Caso ainda não exista, a informação é adicionada.

Dessa forma, a Release mantém o SHA-256 correspondente ao APK publicado.

---

# 🌐 Atualização do `download.html`

O arquivo:

`download.html`

é atualizado automaticamente.

O workflow atualiza:

- URL do APK;
- tamanho real do APK;
- SHA-256.

A URL utilizada segue o padrão da Release:

`https://github.com/$GITHUB_REPOSITORY/releases/download/$RELEASE_TAG/$RELEASE_APK_NAME`

O script Python utiliza expressões regulares para localizar os elementos correspondentes dentro do HTML.

Caso algum elemento esperado não seja encontrado, a atualização falha.

Depois da alteração, o `download.html` é enviado para a `main`.

---

# 📊 Resumo da compilação

Ao final da compilação, o GitHub Actions recebe um resumo contendo:

- Release utilizada;
- branch;
- nome do APK;
- tamanho real;
- tamanho em bytes;
- SHA-256.

---

📊 "bot" — Bot e análise de dados em Julia

Objetivo

O job "bot" executa o Mulher Amparada Bot, compila seu código C++ e realiza uma análise de dados utilizando a linguagem Julia.

A análise é executada no ambiente do GitHub Actions e produz arquivos que são encaminhados ao job "salvar-na-main", responsável por centralizar e publicar os resultados na branch "main".

O job está definido no workflow:

".github/workflows/automations.yml"

Seu nome no GitHub Actions é:

"Bot e análise de dados em Julia"

⚙️ Ambiente de execução

O job utiliza o runner:

"ubuntu-24.04"

O tempo máximo de execução é de 45 minutos:

timeout-minutes: 45

O código do repositório é obtido por meio de:

- name: Baixar repositório
  uses: actions/checkout@v6
  with:
    fetch-depth: 1

A opção "fetch-depth: 1" utiliza um checkout superficial, suficiente quando as etapas não precisam consultar o histórico completo de commits.

🤖 Compilação e execução do bot C++

Antes da análise em Julia, o workflow instala as dependências necessárias à compilação do bot:

- "g++";
- "libcurl4-openssl-dev";
- "libssl-dev".

O arquivo compilado é:

"tools/automation_bot.cpp"

A compilação utiliza C++17, libcurl e as bibliotecas OpenSSL:

g++ \
  tools/automation_bot.cpp \
  -o automation-bot \
  -std=c++17 \
  -lcurl \
  -lssl \
  -lcrypto

O executável resultante é:

"automation-bot"

O bot recebe as credenciais necessárias por meio dos GitHub Actions Secrets:

- "MULHER_AMPARADA_APP_ID";
- "MULHER_AMPARADA_INSTALLATION_ID";
- "MULHER_AMPARADA_PRIVATE_KEY".

O nome do repositório é fornecido por meio da variável "GITHUB_REPOSITORY".

As credenciais são transmitidas por variáveis de ambiente, evitando a necessidade de escrevê-las diretamente no código do workflow.

🟠 Instalação do Julia

Após a execução do bot C++, o workflow configura o ambiente Julia através da action:

"julia-actions/setup-julia@v3"

A configuração utilizada é:

- name: Instalar Julia
  uses: julia-actions/setup-julia@v3
  with:
    version: "1"

A versão ""1"" solicita uma versão da série estável Julia 1.x disponibilizada pela action.

O Julia é utilizado para executar o código de análise de dados do projeto.

📦 Instalação das dependências Julia

As dependências do ambiente são inicializadas com:

- name: Instalar dependências Julia
  run: |
    julia --project=. -e '
      using Pkg
      Pkg.instantiate()
    '

O comando "julia --project=." inicia o Julia utilizando o ambiente de projeto localizado na raiz do repositório.

A instrução:

using Pkg

carrega o gerenciador de pacotes do Julia.

Em seguida:

Pkg.instantiate()

instala e prepara as dependências declaradas pelo projeto, utilizando os arquivos de ambiente disponíveis no repositório, como "Project.toml" e, quando presente, "Manifest.toml".

A execução depende de esses arquivos declararem corretamente as dependências exigidas pelo código.

📈 Execução da análise de dados

A análise é iniciada pela seguinte etapa:

- name: Executar análise de dados em Julia
  run: |
    julia --project=. src/main.jl

O arquivo de entrada é:

"src/main.jl"

O script é executado no ambiente Julia configurado anteriormente.

O código desse arquivo é responsável pela lógica efetiva da análise de dados. Portanto, os cálculos, as transformações, a leitura dos dados de origem e a geração dos resultados dependem da implementação presente em "src/main.jl" e dos módulos utilizados por ele.

📄 Resultados esperados

A integração com o job "salvar-na-main" utiliza dois arquivos de saída:

Arquivo| Finalidade
"dados/processados/ligue180-2019-processado.csv"| Disponibilizar os dados processados da análise do Ligue 180 de 2019 em formato CSV.
"relatorios/graficos/ligue180-2019.png"| Disponibilizar o gráfico gerado para a análise do Ligue 180 de 2019.

Esses caminhos são os resultados esperados pelo workflow. Para que o processo funcione, a implementação Julia precisa gerar os arquivos nesses locais.

A existência desses arquivos, isoladamente, não comprova a exatidão estatística dos dados nem a correção científica do gráfico.

✅ Validação dos resultados

Depois de executar "src/main.jl", o workflow verifica se os dois arquivos esperados existem e possuem conteúdo.

As verificações são realizadas com:

test -s dados/processados/ligue180-2019-processado.csv
test -s relatorios/graficos/ligue180-2019.png

A opção "-s" verifica se o arquivo existe e tem tamanho maior que zero.

Se um dos arquivos estiver ausente ou vazio, a etapa falha e interrompe o fluxo. Isso impede que resultados incompletos sejam encaminhados silenciosamente ao processo de centralização.

Essa validação verifica a presença e o tamanho dos arquivos; não substitui a validação do conteúdo do CSV, dos dados estatísticos ou da integridade da imagem.

📤 Preparação dos resultados

Depois da validação, o workflow cria um diretório temporário para organizar os arquivos:

"pacote-resultados/"

A estrutura preparada é:

pacote-resultados/
├── dados/
│   └── processados/
│       └── ligue180-2019-processado.csv
└── relatorios/
    └── graficos/
        └── ligue180-2019.png

Os arquivos originais são copiados para essa estrutura, preservando os caminhos relativos necessários para a etapa de download posterior.

O diretório funciona como uma área de preparação dos resultados, sem exigir que o job "bot" crie um commit próprio.

📦 Publicação do artifact

Os resultados preparados são enviados ao GitHub Actions como um artifact chamado:

"resultados-ligue180"

A publicação utiliza:

- name: Enviar resultados como artifact
  uses: actions/upload-artifact@v6
  with:
    name: resultados-ligue180
    path: pacote-resultados/
    if-no-files-found: error
    retention-days: 1

O artifact contém o CSV processado e o gráfico da análise.

A opção "if-no-files-found: error" determina que a etapa falhe caso nenhum arquivo correspondente seja encontrado.

A retenção de um dia mantém o artifact disponível temporariamente para o job consumidor, sem tratá-lo como armazenamento permanente.

O artifact não é, por si só, um commit no Git. Ele funciona como um meio de transferência de arquivos entre jobs da mesma execução do workflow.

💾 Integração com "salvar-na-main"

O job "salvar-na-main" deve declarar "bot" em sua lista "needs":

needs:
  - atualizar-commits
  - sitemap
  - atualizar-footer
  - atualizar-relatorio-commits
  - pesquisar-projeto
  - observatorio
  - bot

Essa dependência faz com que o job de centralização aguarde a conclusão bem-sucedida do job "bot", respeitando também as demais dependências declaradas.

Antes da etapa "Reunir arquivos gerados", o artifact é baixado:

- name: Baixar resultados do bot
  uses: actions/download-artifact@v7
  with:
    name: resultados-ligue180
    path: artefatos/resultados

Após o download, os arquivos ficam disponíveis nos caminhos:

- "artefatos/resultados/dados/processados/ligue180-2019-processado.csv";
- "artefatos/resultados/relatorios/graficos/ligue180-2019.png".

A etapa "Reunir arquivos gerados" copia esses arquivos para seus destinos definitivos no repositório.

📝 Commit final

O job "bot" não precisa executar "git commit" nem "git push" para publicar os dois resultados.

A etapa "Salvar todas as alterações em um commit", dentro de "salvar-na-main", adiciona os caminhos dos arquivos ao conjunto de alterações:

git add \
  dados/processados/ligue180-2019-processado.csv \
  relatorios/graficos/ligue180-2019.png

Esses arquivos são incluídos no commit final junto aos demais arquivos gerados pelos jobs que participam da centralização.

Quando existem alterações preparadas, o processo cria o commit e envia as alterações para a branch "main", conforme a lógica de sincronização e publicação já definida no workflow.

Isso centraliza a publicação dos resultados da análise em um único job, em vez de permitir que o job "bot" envie os arquivos separadamente.

🔄 Fluxo da análise Julia

Job bot
   │
   ├── Baixar repositório
   │
   ├── Instalar dependências C++
   │
   ├── Compilar automation_bot.cpp
   │
   ├── Executar Mulher Amparada Bot
   │
   ├── Instalar Julia
   │
   ├── Inicializar dependências
   │
   ├── Executar src/main.jl
   │
   ├── Validar CSV e gráfico
   │
   ├── Preparar pacote-resultados/
   │
   └── Publicar artifact
         │
         ▼
   resultados-ligue180
         │
         ▼
   Job salvar-na-main
         │
         ├── Baixar artifact
         │
         ├── Reunir arquivos gerados
         │
         ├── Adicionar arquivos ao Git
         │
         ├── Criar commit
         │
         └── Enviar alterações para main

🔐 Segurança e confiabilidade

O processo utiliza algumas medidas para reduzir erros operacionais:

- Credenciais do bot fornecidas por GitHub Actions Secrets.
- Execução das etapas com falha interrompendo o processo quando um comando retorna erro.
- Verificação dos arquivos esperados antes de publicar o artifact.
- Transferência dos resultados entre jobs por meio de artifacts.
- Centralização do commit final no job "salvar-na-main".

A análise também depende da confiabilidade do código Julia, das fontes de dados e das bibliotecas utilizadas. A execução automatizada não substitui auditoria, revisão metodológica ou validação independente dos resultados.

📁 Arquivos relacionados

Caminho| Função
"tools/automation_bot.cpp"| Código-fonte do bot C++.
"src/main.jl"| Ponto de entrada da análise Julia.
"Project.toml"| Declaração do ambiente e das dependências Julia, quando presente.
"Manifest.toml"| Registro das versões resolvidas das dependências, quando presente.
"dados/processados/ligue180-2019-processado.csv"| CSV processado esperado.
"relatorios/graficos/ligue180-2019.png"| Gráfico esperado.
".github/workflows/automations.yml"| Definição do workflow automatizado.

🎯 Resultado final

O job "bot" combina a execução do bot C++ com a análise de dados em Julia. Depois de validar e empacotar os resultados, disponibiliza-os para o job "salvar-na-main".

A publicação dos resultados ocorre junto dos demais arquivos gerados pelo workflow, mantendo a centralização das alterações na branch "main".

Esse mecanismo separa a geração dos dados da publicação no Git, facilita a transferência entre jobs e permite que a etapa central de commit seja responsável pela consolidação dos arquivos do projeto.

---
# 🌐 `build-site`

## Construção do site

O job `build-site` depende do job:

`build`

Depois da compilação do aplicativo, o site é construído utilizando GitHub Pages e Jekyll.

O job utiliza:

`actions/configure-pages@v6`

para configurar o GitHub Pages.

Depois utiliza:

`actions/jekyll-build-pages@v1`

para construir o site.

O resultado é armazenado em:

`_site`

Esse diretório é enviado como artifact do GitHub Pages utilizando:

`actions/upload-pages-artifact@v5`

---

# 🚀 `deploy`

## Publicação do GitHub Pages

O job `deploy` depende de:

`build-site`

Ele utiliza:

`actions/deploy-pages@v5`

para publicar o site no GitHub Pages.

A URL gerada pela implantação é disponibilizada no ambiente:

`github-pages`

---

# 🔢 `atualizar-commits`

## Atualização do total de commits

Esse job depende de:

`deploy`

Ele baixa todo o histórico do Git:

    fetch-depth: 0

Depois executa:

    git rev-list --all --count

para descobrir a quantidade total de commits existentes no histórico.

O valor encontrado é inserido automaticamente no `README.md`.

O texto atualizado segue o padrão:

`**Commits totais de toda a história do projeto, (feitos por mim e pelos workflows do github actions!) = TOTAL**`

Depois o README é enviado como artifact:

`arquivo-readme`

Esse artifact é utilizado por outros jobs que também modificam o README.

---

# 📝 `sincronizar-devto`

## Sincronização com DEV.to

O job `sincronizar-devto` depende de:

`salvar-na-main`

Ele baixa o `README.md` atualizado da `main`.

Depois utiliza a API do DEV.to para atualizar o conteúdo do artigo:

`4771914`

O conteúdo enviado corresponde ao conteúdo atual do:

`README.md`

A autenticação utiliza o secret:

`DEVTO_API_KEY`

A atualização é realizada através de uma requisição HTTP `PUT`.

---

# 🗺️ `sitemap`

## Geração do Sitemap

O job `sitemap` depende de:

`atualizar-commits`

Ele utiliza:

`cicirello/generate-sitemap@v1`

para gerar:

`sitemap.xml`

O endereço base utilizado é:

`https://mulher-amparada.github.io/mulher-amparada-app/`

O arquivo gerado é disponibilizado como artifact:

`arquivo-sitemap`

---

# 🔎 `indexnow`

## IndexNow

O job `indexnow` depende de:

`deploy`

Ele envia o sitemap para o IndexNow.

O sitemap utilizado é:

`https://mulher-amparada.github.io/mulher-amparada-app/sitemap.xml`

A autenticação utiliza:

`INDEXNOW_KEY`

O objetivo é informar mecanismos de busca compatíveis sobre alterações no site.

---

# 💡 `lighthouse`

## Análise de desempenho

O job `lighthouse` depende de:

`deploy`

Ele utiliza:

`treosh/lighthouse-ci-action@v12`

para analisar o site publicado.

A URL analisada é:

`https://mulher-amparada.github.io/mulher-amparada-app/`

O Lighthouse verifica aspectos relacionados à qualidade e desempenho da página.

Os artifacts da análise são disponibilizados pela própria Action.

---

# 📢 `publicar-divulgacao`

## Publicação do Kit de Divulgação

Esse job depende de:

`lighthouse`

Ele acessa o diretório:

`divulgacao`

e configura Node.js 24.

Depois executa:

`npm pack`

para gerar o pacote de divulgação.

O pacote resultante é publicado em uma Release específica:

`divulgacao`

com o nome:

`Kit de Divulgação`

O workflow utiliza:

`softprops/action-gh-release@v3`

para publicar o pacote.

---

# 🔭 `observatorio`

## Geração do Observatório

O job `observatorio` depende de:

`atualizar-commits`

Ele utiliza o arquivo:

`tools/observatorio.c`

O código C é compilado utilizando GCC e Cairo.

As dependências instaladas incluem:

- `gcc`;
- `libcairo2-dev`.

A compilação gera o executável:

`observatorio`

Depois o executável é executado.

O resultado esperado é:

`docs/observatorio/observatorio.png`

O workflow verifica se a imagem existe e possui conteúdo.

A imagem é então enviada como artifact:

`observatorio`

com retenção de 30 dias.

e ele pode cortar em outras imagens se não couber!
---

# 🦶 `atualizar-footer`

## Atualização do Footer

O job `atualizar-footer` depende de:

`atualizar-commits`

Ele utiliza Python.

O Python é configurado através de:

`actions/setup-python@v6`

Depois instala:

`Markdown`

através do `pip`.

O script:

`scripts/atualizar_footer.py`

é executado para gerar ou atualizar:

`index.html`

O arquivo resultante é disponibilizado como artifact:

`arquivo-footer`

---

# 📈 `atualizar-relatorio-commits`

## Relatório de commits

Esse job depende de:

`atualizar-commits`

Ele utiliza Ruby 3.4.

A configuração é feita através de:

`ruby/setup-ruby@v1`

Depois executa:

`scripts/commits.rb`

O script gera os relatórios no diretório:

`commits/`

Os arquivos são enviados como artifact:

`arquivos-relatorio-commits`

---

# 🔎 `pesquisar-projeto`

## Documentação do workflow YAML

O job `pesquisar-projeto` é responsável por gerar uma documentação específica do workflow.

Ele instala PHP através do sistema operacional.

Depois valida a sintaxe do script:

`scripts/pesquisar_projeto.php`

utilizando:

`php -l`

Em seguida executa:

`scripts/pesquisar_projeto.php`

O script gera:

`.github/workflows/workflow.md`

Esse arquivo é movido para:

`pesquisas/workflow.md`

O resultado é enviado como artifact:

`arquivos-pesquisas`

Esse arquivo posteriormente é centralizado na `main`.

---

# 💾 `salvar-na-main`

## Centralização dos arquivos

O job `salvar-na-main` funciona como o ponto central de consolidação dos arquivos gerados pelos outros jobs.

Ele depende de:

- `atualizar-commits`;
- `sitemap`;
- `atualizar-footer`;
- `atualizar-relatorio-commits`;
- `pesquisar-projeto`;
- `observatorio`;
- `gerar-linguagens`.

Primeiro ele baixa o código atual da:

`main`

Depois baixa todos os artifacts necessários.

---

# 📥 Artifacts utilizados

O job baixa:

### README

`arquivo-readme`

### README atualizado pelas linguagens

`arquivo-readme-linguagens`

### Imagem das linguagens

`github-languages`

### Sitemap

`arquivo-sitemap`

### Footer

`arquivo-footer`

### Relatórios

`arquivos-relatorio-commits`

### Pesquisas

`arquivos-pesquisas`

### Observatório

`observatorio`

---

# 📂 Arquivos centralizados

Depois de baixar os artifacts, o workflow reúne os arquivos no repositório.

O README final é:

`README.md`

A imagem das linguagens é:

`github-languages.png`

O site é:

`index.html`

O sitemap é:

`sitemap.xml`

Os relatórios são colocados em:

`commits/`

As pesquisas são colocadas em:

`pesquisas/`

O Observatório é colocado em:

`docs/observatorio/observatorio.png`

---

# 🔧 Correção da documentação

Caso a documentação seja encontrada em:

`pesquisas/.github/workflows/workflow.md`

ela é movida para:

`pesquisas/workflow.md`

Isso impede que a estrutura interna `.github/workflows` seja mantida dentro do diretório de pesquisas.

---

# ✅ Validação dos arquivos

Antes de criar o commit, o workflow verifica se os principais arquivos existem e possuem conteúdo.

São verificados:

- `README.md`;
- `github-languages.png`;
- `sitemap.xml`;
- `index.html`;
- `pesquisas/workflow.md`;
- `docs/observatorio/observatorio.png`.

Caso algum deles não exista ou esteja vazio, o job falha.

---

# 📝 Commit final

Depois que todos os arquivos são reunidos, o workflow configura:

`github-actions[bot]`

como autor do commit.

Os arquivos são adicionados através de:

    git add

Incluindo:

- `README.md`;
- `github-languages.png`;
- `index.html`;
- `sitemap.xml`;
- `commits/`;
- `pesquisas/`;
- `docs/observatorio/`.

---

# 📦 Commit automático

Se existirem alterações, é criado:

`Atualizar arquivos gerados [skip ci]`

Depois o workflow atualiza a referência da `main`:

    git fetch origin main

    git rebase origin/main

Finalmente envia as alterações:

    git push origin HEAD:main

Assim, os arquivos produzidos pelos jobs são centralizados automaticamente na branch principal.

---

# 🔄 Fluxo completo

O processo geral pode ser resumido da seguinte forma:

    Código
      │
      ├── Compilar APK
      │     ├── Java 17
      │     ├── Android SDK 37
      │     ├── Gradle
      │     ├── Assinatura
      │     ├── SHA-256
      │     └── Release
      │
      ├── Construir site
      │     └── Jekyll
      │
      ├── GitHub Pages
      │
      ├── Atualizar commits
      │
      ├── Gerar Sitemap
      │
      ├── IndexNow
      │
      ├── Lighthouse
      │
      ├── Kit de Divulgação
      │
      ├── Observatório
      │     └── observatorio.png
      │
      ├── Footer
      │     └── index.html
      │
      ├── Relatórios
      │     └── commits/
      │
      ├── Documentação
      │     └── pesquisas/workflow.md
      │
      ├── Linguagens
      │     ├── tools/languages.go
      │     └── github-languages.png
      │
      └── Centralização
            ├── README.md
            ├── github-languages.png
            ├── index.html
            ├── sitemap.xml
            ├── commits/
            ├── pesquisas/
            └── docs/observatorio/

                    ↓

                  main

                    ↓

              DEV.to atualizado

---

# 📁 Principais arquivos utilizados

| Arquivo | Função |
|---|---|
| `.github/workflows/automations.yml` | Workflow principal |
| `tools/languages.go` | Geração da imagem das linguagens |
| `tools/observatorio.c` | Geração do Observatório |
| `scripts/pesquisar_projeto.php` | Geração da documentação do workflow |
| `scripts/atualizar_footer.py` | Atualização do footer |
| `scripts/commits.rb` | Geração dos relatórios de commits |
| `README.md` | Documentação principal |
| `github-languages.png` | Imagem das linguagens do projeto |
| `docs/observatorio/observatorio.png` | Imagem do Observatório |
| `pesquisas/workflow.md` | Documentação gerada do workflow |
| `commits/` | Relatórios de commits |
| `sitemap.xml` | Sitemap do site |
| `index.html` | Página principal/site |

---

# 📦 Principais artifacts

| Artifact | Conteúdo |
|---|---|
| `APK-Kotlin-Assinado` | APK assinado |
| `arquivo-readme` | README atualizado |
| `arquivo-readme-linguagens` | README atualizado pelo `languages.go` |
| `github-languages` | Imagem das linguagens |
| `arquivo-sitemap` | Sitemap |
| `arquivo-footer` | Footer/site |
| `arquivos-relatorio-commits` | Relatórios de commits |
| `arquivos-pesquisas` | Documentação do workflow |
| `observatorio` | Imagem do Observatório |

---

# 🔗 Integrações externas

O workflow integra o projeto com:

- GitHub Actions;
- GitHub Releases;
- GitHub Pages;
- DEV.to;
- IndexNow;
- Lighthouse;
- Jekyll;
- Android SDK;
- Gradle;
- Java;
- Go;
- PHP;
- Ruby;
- Python;
- GCC;
- Cairo;
- Node.js.

---

# 🔐 Secrets utilizados

O workflow utiliza secrets para operações que exigem autenticação ou dados privados.

Entre eles estão:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `DEVTO_API_KEY`
- `INDEXNOW_KEY`

Esses valores não ficam armazenados diretamente no código do workflow.

---

# 🛡️ Segurança

As informações sensíveis utilizadas durante a compilação e publicação são fornecidas através dos Secrets do GitHub.

O keystore utilizado para assinar o APK é reconstruído temporariamente durante a execução e removido depois da assinatura.

As credenciais não devem ser colocadas diretamente no código-fonte.

---

# ♻️ Automação contínua

O objetivo desse workflow é reduzir tarefas manuais no projeto.

Depois de uma alteração no repositório, as automações podem:

1. compilar o aplicativo;
2. assinar o APK;
3. verificar o APK;
4. calcular seu tamanho;
5. calcular o SHA-256;
6. atualizar a Release;
7. atualizar o `download.html`;
8. construir o site;
9. publicar o GitHub Pages;
10. atualizar o total de commits;
11. gerar o sitemap;
12. enviar o sitemap ao IndexNow;
13. executar o Lighthouse;
14. publicar o kit de divulgação;
15. gerar o Observatório;
16. atualizar o footer;
17. gerar relatórios de commits;
18. gerar documentação do workflow;
19. identificar as linguagens utilizadas;
20. gerar `github-languages.png`;
21. atualizar o README;
22. reunir todos os arquivos;
23. criar um commit;
24. enviar as alterações para a `main`;
25. sincronizar o README com o DEV.to.

---

# 🎯 Resultado final

Ao final do processo, a branch `main` recebe os arquivos gerados e atualizados pelas automações.

O repositório mantém automaticamente sincronizados:

- aplicativo Android;
- APK de Release;
- informações da Release;
- SHA-256;
- página de download;
- site;
- GitHub Pages;
- README;
- imagem das linguagens;
- sitemap;
- relatórios;
- documentação;
- Observatório;
- footer;
- pesquisas;
- kit de divulgação.

Dessa forma, o GitHub Actions funciona como o sistema central de automação do projeto **Mulher Amparada Pela Liberdade Feminina**.
```

E eu também já consegui configurar um ssh na conta, e 2fa nela tambem, e com o ssh, eu consegui mover pastas inteiras para o repositório, e transformei 4 em 1, e mais de 100 commits em 1,

e os contatos de confiança, quando são cadrastados eles também são criptografados!

### Sobre o site que está hospedado pelo github pages:

Na primeira página do site, tem 3 botoes que ligam para (180, 190 e 192), e usam o tel: do navegador para abrir o telefone nativo do celular com esses números já discados de acordo com o que você escolheu!

embaixo, tem um botão que leva pro repositório 

depois um gerador de qr code

depois uma página com imagens dos apks de todas as versões do app (só as imagens)

o nome verdadeiro do app = Mulher Amparada Pela Liberdade Feminina 

E uma página mostrando o trabalho de uma mulher empoderada (e eu estava mostrando sobre as vendas dela, para dizer que todas as mulheres podem crescer!, e ela não tem relação com a questão do projeto ser gratuito, essa página é algo separado, o app ainda é totalmente grátis!)

e as páginas desse site estão indexadas no google search console também!

### Sobre as atualizações do app:

O app já está na versão:

`33 (versão final)`

ele tem o TargetSdkVersion 37

ele tem o CompileSdkVersion 37

Versão agp no toml de 9.4.0 e versão kotlin é a 2.4.20

e ele tem o jetpack compose e estilo via xml ativados!

e a versão do gradle é 9.6!

## Sobre as telas do aplicativo;

Temos elas (está organizado conforme foi adicionado, de forma cronológica):

* EntradaActivity (compose) = Tela que contém a splashscreen 

AssistenteActivity (compose) = Tela que contém o disfarce de saúde do app 

NeoCalcActivity (compose) = Tela que contém o disfarce de calculadora

 HubActivity (compose) = Tela que contém o botão de pânico, proteção por palmas, balançar o celular pra pedir ajuda, escurecimento por inclinação, bloqueio por barulho, usando o action dial, ele chama a policia, samu e o 180, enviar localização para o 180, contatos de confiança, o botão feito com compose, (aquele do emergencyoverlay), 
 
 HomeActivity (compose) = Tela da área do amparo onde mostra as 2 carteirinhas
 
 ExigirActivity (compose) = Conteúdo da primeira carteirinha
 
 GestoActivity (compose) = Conteúdo da segunda carteirinha
 
 MapaActivity (compose) = Recurso do mapa da área protegida 
 
 AppsActivity (compose) = Recurso da tela de aplicativos da área protegida 
 
 DiarioActivity (compose) = Recurso do diário criptografado da área protegida 
 
 CicloActivity (compose) = Recurso do calendário menstrual da área protegida 
 
 TarefaActivity (compose) = Recurso das tarefas da área protegida 
 
 A MainActivity é a única que é feita com webview!, e representa o recurso de navegador da área protegida
 
 GravarActivity (xml) = Recurso do gravador de voz da área protegida
 
 FileActivity (xml) = Recurso de Gerenciador de arquivos da área protegida (mas ele apenas visualiza!)
 
 ArquivoActivity = (compose) Tela da funcao de arquivo seguro da área protegida!
 
 UpdateActivity = (compose) Tela da funcao que mostra o ícone do app disfarçado nome do aplicativo, versão instalada (versionName), número da build (versionCode) e um indicador visual de disponibilidade do sistema
 
 GeoActivity = (compose) Tela da funcao de raio seguro com georreferenciamento seguro da área protegida!
 
 # Sobre como o aplicativo é compilado:

Proteção da Activity de Entrada:

O aplicativo está configurado para iniciar pela EntradaActivity, que é a única Activity com android:exported="true" por possuir o intent-filter de inicialização (MAIN e LAUNCHER).

A MainActivity permanece com android:exported="false", impedindo que outros aplicativos iniciem essa Activity diretamente por meio de uma Intent externa. Todas as demais Activities do aplicativo também estão configuradas com android:exported="false".

Dessa forma, a EntradaActivity funciona como uma camada de entrada: ela gerencia a inicialização visual do aplicativo e encaminha o usuário de forma segura para a AssistenteActivity internamente. 

E a EntradaActivity tem o fundo preto!

Essa configuração reduz a exposição direta das Activities internas a inicializações externas. Ela não impede a análise do APK ou de seus arquivos por ferramentas de engenharia reversa.

Proteção do Código

O aplicativo utiliza o **R8**, o sistema oficial de otimização, redução e ofuscação integrado ao *Android Gradle Plugin*, para aplicar proteção ao código-fonte na compilação da versão de lançamento (*release build*).

O R8 reduz o tamanho do aplicativo e aplica a ofuscação no código compilado, substituindo nomes de classes, métodos e variáveis por caracteres genéricos. Isso dificulta significativamente a leitura e a engenharia reversa do código por meio de ferramentas de descompilação.

*Nota: Esta proteção aumenta a barreira contra análise estática, mas não torna o APK completamente imune à engenharia reversa.*

E o fundo do icone do app é um adaptativo, em que o fundo e preto, e tem um bonequinho em cores azuis correndo, e o ic_launcher_foreground na pasta res/drawable, e os ic_launcher em cada mipmap tem o fundo transparente, eo ic_launcher_background também na pasta res/drawable é um quadrado preto

e quando você clica no botão voltar na MainActivity, ele volta a página!

E o workflow utiliza um keystore de assinatura armazenado de forma protegida nos GitHub Actions Secrets. As informações necessárias para acessar o keystore e selecionar a chave são fornecidas pelas variáveis KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS e KEY_PASSWORD.

Em:

/settings/secrets and variables/actions/repository secrets/

E também as barras tanto de status tanto de navegação são transparentes, porém o fundo atrás do WebView e preto, espaçado dos lados e de cima e com um raio de borda!, e o webview não fica mais embaixo das duas barras, ele respeita elas!

----

### Sobre como foi escrito o texto do biometricPrompt do app:

- HubActivity:

#### Disfarce da calculadora:

**Título:**
> Confirmar identidade

**Subtítulo:**
> Use o bloqueio de tela do dispositivo

**Descrição:**
> Confirme sua identidade para redefinir a senha.

**Método:** somente `DEVICE_CREDENTIAL`.

#### Área protegida:

**Título:**
>Desbloquear a área protegida

**Descrição:**
>🌸 Apenas a usuária cadastrada pode acessar este local

#### Área do amparo:

**Título:**

>Desbloquear o espaço do amparo

**Descrição:**

>🌸 Acesso protegido por biometria

#### E nos dois tem embaixo (Use sua impressão digital., Usar o reconhecimento facial.)

O método de autenticação é definido pelo próprio Android de acordo com os autenticadores disponíveis no dispositivo, utilizando "BIOMETRIC_WEAK" e "DEVICE_CREDENTIAL".


# ⚒️Todas as funções do aplicativo!:

### Ícone Monocromático e Integração com Material You:

O aplicativo possui suporte aos ícones temáticos do Android (Themed Icons), permitindo que seu ícone se adapte visualmente à paleta de cores dinâmica definida pelo sistema.

* Máscara Monocromática: O aplicativo fornece uma versão monocromática específica do ícone para que o Android possa utilizá-la quando os ícones temáticos estiverem disponíveis e ativados no dispositivo.

* Adaptação à Paleta do Sistema: Em vez de utilizar uma cor fixa definida pelo aplicativo, o Android pode aplicar a paleta dinâmica escolhida para o dispositivo ao ícone temático. Dessa forma, o ícone acompanha visualmente as cores utilizadas pelo restante da interface.

* Integração Visual: A utilização do sistema de ícones temáticos permite que o aplicativo mantenha uma aparência mais integrada à tela inicial, acompanhando o padrão visual adotado pelo próprio Android.

O Impacto Visual: O ícone deixa de depender exclusivamente de suas cores originais e passa a responder à personalização visual do sistema. Isso proporciona uma apresentação mais discreta e consistente com a interface do dispositivo, sem que o aplicativo precise criar manualmente uma versão diferente para cada paleta de cores.

### Disfarce do app (assistente de saúde falso!):
tutorial: ao entrar no app, clique no canto superior direito com o icone de calculadora. e ai quando ele for iniciado, voce precisará tocar no visor 5 vezes para cadastrar a senha e a pergunta de recuperação (e ele salva em uma classe kt de criptografia), assim so acessa com a senha informada, para resetar essa senha (dê 5 toques em menos de 2 segundos, e digite como você gosta de ser chamada, e digite sua nova senha!), mas antes dessa tela, tem outra tipo uma gaveta de apps..., porém, agora no mulher amparada, ele já vem com o icone de calculadora e o nome calculadora, só dá para mudar o icone, ou seja, o app ja vem com icone de (Assistente de saúde), uma tela genérica de elementos de medição de saúde (bpm e etc), e vale lembrar que:

Os dados da primeira página do app (disfarce do assistente de saúde) são meramente fictícios e não representam informações reais!

e tambem, reforcando que no canto superior direito tem um icone de calculadora que quando clica vai pra uma calculadora e aparece o disfarce de calculadora 

> no disfarce de calculadora, caso a usuaria esqueça tudo, existe um popup que usa o BiometricPrompt que poderá resetar tanto a senha tanto a pergunta de recuperação (e ele usa APENAS o device credencial!), porém, quando utiliza o BiometricPrompt e é validado, ele pergunta a pergunta de recuperação e a senha!

### Versão do app:

A "UpdateActivity" apresenta as informações da versão instalada do Mulher Amparada, utilizando os dados diretamente do aplicativo instalado.

Ela exibe:

- Ícone do app disfarçado
- Nome do aplicativo
- Versão instalada ("versionName")
- Número da build ("versionCode")
- Indicador visual de disponibilidade do sistema

A versão e a build são obtidas diretamente dos metadados do APK, garantindo que as informações exibidas correspondam à versão realmente instalada no dispositivo.

### Botão de Pânico:
Botão de Pânico, com ligação ao 180 de forma direta no primeiro clique.

### Proteção por Barulho:
Ative a proteção, faça barulho alto e ele liga para o 180.

caso ocorra alguma falha ou o aparelho da usuária não possua o sensor necessário, o aplicativo utiliza o microfone como alternativa. Ao detectar um barulho alto, a proteção é acionada.

>Sobre a proteção por palmas/barulho: Nota de Segurança: Uma vez ativada, a proteção permanecerá vigilante e reativará o microfone automaticamente após cada detecção e ligação pro 180. Isso garante que o aplicativo continue te protegendo caso a situação de risco persista. Para desligá-la por completo, você deve fazer isso manualmente no aplicativo após o término da situação de risco.

### Balançar o Celular para Pedir Ajuda:
Ative e, ao chacoalhar o celular, ele liga para o 180.

### Escurecimento por inclinação:
com isso, voce pode controlar o brilho da tela clicando em um botão..., porém, e tipo como se fosse o menor brilho do celular, e ai depois ele deixa a tela preta (nao com brilho e sim colocando a cor), (honestamente, antes aparecia as duas barras, agora elas se escondem!), e o efeito e vitalicio ate fechar e abrir o app!

>Vale lembrar que ele só funciona dentro da HubActivity!

###Emergência:
Saindo dessa área, existem botões que abrem o aplicativo nativo do telefone nos números 190, 192 e 180.

###Compartilhamento Rápido de Localização:
Além disso, existe um botão dentro do aplicativo que obtém a localização atual, monta um link do Google Maps com as coordenadas e abre uma conversa no WhatsApp do 180 com a mensagem preparada. O envio não é automático: é necessário apenas conferir a mensagem e tocar no botão de enviar.

### Contatos de Confiança:
Além dos contatos de confiança, clicando no primeiro botão você seleciona e salva o contato. O botão abaixo envia um pedido de ajuda para ele.

### Compartilhamento de metadados;

Ao pressionar o botão de emergência, um botao circular feito com compose aparece no canto inferior direito da tela. O aplicativo obtém a localização atual do dispositivo e abre o menu de compartilhamento do Android.

A mensagem contém:

- Latitude e longitude;
- Link para visualizar a localização no Google Maps;
- Precisão da localização em metros;
- Data e hora;
- Fabricante e modelo do dispositivo;
- Versão do Android;
- Porcentagem da bateria.

A usuária pode escolher por qual aplicativo deseja compartilhar essas informações, como WhatsApp, SMS ou e-mail.

 O compartilhamento não é enviado automaticamente para um contato específico. O usuário precisa escolher o aplicativo e confirmar o envio.

Vale lembrar que o compose é um overlay usandi a classe `EmergencyOverlay`, e a HubActivity chama essa classe!

# 🔐Área Protegida:
Se estiver cadastrado no celular, com Biometric Prompt junto com Device Credential e autenticação weak, pode desbloquear essa área com impressão digital, rosto, PIN, padrão, senha e outros métodos.

Sistema Cripto (Segurança do App)

O aplicativo possui um sistema próprio de armazenamento seguro que utiliza criptografia nativa do Android para proteger os dados armazenados.

Como funciona:

Quando um dado é salvo no app, ele é criptografado antes de ser armazenado.

O conteúdo armazenado não permanece em texto simples. Para recuperar o dado, o aplicativo utiliza a chave de criptografia protegida pelo Android e descriptografa o conteúdo somente quando necessário.

Tecnologias usadas:

AES-256
Criptografia simétrica utilizada para proteger os dados.

Android Keystore
Sistema do Android utilizado para proteger as chaves criptográficas.

Jetpack DataStore Preferences
Sistema utilizado para armazenar as informações persistentes do aplicativo.

O que cada função faz:

salvar(chave, valor) → criptografa e armazena o dado

carregar(chave) → recupera e descriptografa o dado

remover(chave) → remove um dado específico

limparTudo() → remove todos os dados armazenados

Segurança:

Os dados não são armazenados diretamente em texto simples.

A criptografia utiliza uma chave protegida pelo Android Keystore, enquanto o armazenamento persistente é realizado pelo Jetpack DataStore.

Dessa forma, o conteúdo protegido não fica diretamente legível no armazenamento do aparelho.

###Calendário Menstrual:
Registre como dói cada dia e, com isso, o aplicativo monta um calendário.

###Mapa:
Mostra um mapa da região e, quando a localização estiver disponível e autorizada, permite visualizar a posição atual.

### Raio Seguro

Recurso de georreferenciamento que permite definir uma área segura e verificar se a usuária permanece dentro do raio estabelecido.

- 📌 Define a sua localização como centro da área segura e isso é fixo.
- 📏 Permite configurar o raio em metros.
- 🗺️ Utiliza OpenStreetMap para visualização do mapa.
- 📍 Compara a localização atual com a área definida.
- ⚠️ Em caso de saída do raio, exibe um alerta.
- 🆘 Disponibiliza a opção Pedir ajuda.
- 🔐 Os dados da área segura são armazenados de forma criptografada.
- 🚫 Não cria histórico de trajetos no aplicativo.
- 🌐 O carregamento dos mapas depende de conexão com a internet.

O recurso foi desenvolvido como uma ferramenta de apoio à segurança, permitindo que a própria usuária estabeleça os limites da área que considera segura.

> Só funciona dentro da GeoActivity!

> e quando ele detecta que a usuária saiu da área segura, ele mostra um popup com um botão vermelho, e ao clicar ele leva pro telefone nativo do sistema com o 190 já discado!

###Diário criptografado:
Usando criptografia, a usuária poderá anotar o que quiser. Com a senha, ficará seguro e também não some, pois estará guardado, e é possivel baixar as páginas desse diário!, e ele tem suporte para markdown...



###Tarefas:
O sistema permite categorizar tarefas em áreas como estudos, trabalho, pessoal e saúde.

As tarefas podem ser marcadas como concluídas para acompanhamento do progresso.

Todas as tarefas são salvas diretamente na classe cripto da usuária.

Os dados ficam armazenados localmente no dispositivo do usuário.

###Gravador de voz:
```
Usando uma activity (uma tela) em kotlin, é possivel ter um gravador de voz no app, sendo possível registrar gravações que podem ser utilizadas pela própria usuária para documentação, além do que a usuária quiser, sempre usando permissoes android e com o consentimento da usuária!

GRAVAÇÕES — COMPORTAMENTO E METADADOS

1. DURANTE A GRAVAÇÃO

Ao tocar no botão de gravação:

• O aplicativo inicia a gravação de áudio pelo microfone.
• É criado temporariamente um arquivo .3gp, (ou um arquivo 3ga, ou outro arquivo, isso varia conforme o aparelho).
• É registrado o horário exato de início da gravação.
• O nível da bateria é registrado.
• O acelerômetro começa a acompanhar os movimentos do aparelho e registra o maior valor de força G observado durante a gravação.
• Se as permissões de localização estiverem disponíveis, o aplicativo acompanha a localização e registra latitude, longitude, precisão e provedor.
• O arquivo original permanece temporário durante o processo.

2. AO ENCERRAR A GRAVAÇÃO

Quando a gravação é encerrada:

• É registrado o horário exato de encerramento.
• É calculado o SHA-256 do áudio original.
• O áudio é criptografado usando AES/GCM.
• O arquivo protegido recebe a extensão .enc.
• É calculado o SHA-256 do arquivo criptografado.
• É criado um arquivo separado .metadata.json contendo os metadados.
• Depois que a criptografia é concluída, o .3gp, (ou um arquivo 3ga, ou outro arquivo, isso varia conforme o aparelho) original temporário é apagado do armazenamento privado do aplicativo.
• A gravação protegida e seus metadados aparecem na lista do aplicativo.

3. METADADOS GERADOS

TEMPO:

• created_at — timestamp UNIX do início.
• created_at_utc — início em UTC.
• closed_at — timestamp UNIX do encerramento.
• closed_at_utc — encerramento em UTC.
• last_modified — última modificação registrada pelo sistema.
• last_modified_utc — última modificação em UTC.
• ntp_synced — indicação relacionada à configuração de hora automática do Android.

INTEGRIDADE E CRIPTOGRAFIA:

• sha256_raw — SHA-256 do áudio original antes da criptografia.
• sha256_encrypted — SHA-256 do arquivo .enc.
• crypto_algorithm — AES/GCM/NoPadding, 256-bit.
• key_provider — AndroidKeyStore.

LOCALIZAÇÃO:

• gps_latitude — latitude registrada.
• gps_longitude — longitude registrada.
• gps_accuracy — precisão estimada em metros.
• location_provider — provedor utilizado, como GPS ou rede.

DISPOSITIVO:

• device_model — modelo do aparelho.
• device_brand — fabricante/marca.
• android_version — versão do Android.
• api_level — nível da API.
• device_hash — identificador derivado do Android ID e protegido por SHA-256.

SENSORES E AMBIENTE:

• max_g_force — maior aceleração registrada pelo acelerômetro durante a gravação.
• battery_level — nível da bateria no início da gravação.

ARQUIVO:

• file_name — nome do arquivo criptografado.
• file_size_bytes — tamanho do arquivo .enc.
• file_format — formato original do áudio, 3gp, (ou um arquivo 3ga, ou outro arquivo, isso varia conforme o aparelho)/AMR-NB.
• metadata_version — versão do formato dos metadados.

4. O QUE ACONTECE AO TOCAR EM "DOWNLOAD"

O botão de download NÃO simplesmente copia o .enc.

O aplicativo:

1. Localiza o arquivo .enc protegido.
2. Descriptografa temporariamente o conteúdo.
3. Cria um arquivo .3gp, (ou um arquivo 3ga, ou outro arquivo, isso varia conforme o aparelho) temporário no cache do aplicativo.
4. Copia esse áudio para a pasta Downloads do Android.
5. No Android 10 ou superior, utiliza o MediaStore.
6. Em versões antigas, utiliza a pasta pública Downloads.
7. Depois da cópia, o arquivo .3gp, (ou um arquivo 3ga, ou outro arquivo, isso varia conforme o aparelho) temporário utilizado durante o processo é apagado.
8. O arquivo .enc original continua protegido dentro do aplicativo.

5. IMPORTANTE SOBRE O JSON

O .metadata.json é um arquivo separado do áudio.

Por exemplo:

Downloads:

rec_1787821440.3gp, (ou um arquivo 3ga, ou outro arquivo, isso varia conforme o aparelho)

rec_1787821440.metadata.json

O .3gp, (ou um arquivo 3ga, ou outro arquivo, isso varia conforme o aparelho) é o áudio reproduzível.

O .metadata.json contém as informações técnicas associadas àquela gravação.

6. IMPORTANTE SOBRE A LOCALIZAÇÃO

A localização não é garantida em todas as gravações.

Se a permissão de localização estiver concedida e o Android fornecer uma localização válida, os campos de localização são preenchidos.

Caso contrário, eles ficam como null.

7. IMPORTANTE SOBRE OS METADADOS

Esses metadados são registros técnicos produzidos pelo aplicativo. Eles podem ajudar a documentar como e quando uma gravação foi criada, mas a existência de hashes, localização ou timestamps, por si só, NÃO garante validade jurídica ou prova que um fato ocorreu.

O SHA-256 permite verificar se os bytes de um arquivo correspondem ao conteúdo anteriormente registrado, enquanto os dados de localização, sensores e horário dependem dos recursos e configurações do próprio aparelho.
```

### Meus arquivos:

Dentro do aplicativo, o Gerenciador de Arquivos do Mulher Amparada funciona como um visualizador. Ele permite navegar pelas pastas e, ao selecionar um arquivo, utiliza o mecanismo do Android para abrir um seletor de aplicativos compatíveis com aquele tipo de arquivo, permitindo que a usuária escolha um aplicativo para visualizá-lo ou executá-lo.

O aplicativo possui um único botão para alternar entre os diferentes tipos de armazenamento disponíveis, seguindo uma sequência entre:

Armazenamento interno → cartão SD → dispositivo externo (como pendrive via USB OTG) → armazenamento interno.

O acesso varia de acordo com o tipo de armazenamento:

- Armazenamento interno e cartão SD: utilizam a permissão de Acesso a todos os arquivos, quando concedida pelo Android.
- Dispositivos externos, como pendrives conectados por USB OTG: utilizam o SAF (Storage Access Framework), mecanismo oficial do Android para acesso a documentos e dispositivos de armazenamento externos autorizados pela usuária.


>O Gerenciador de Arquivos (que apenas visualiza) não acessa os arquivos internos do Gravador de Voz do Mulher Amparada nem os dados ou recursos internos de outras funcionalidades do aplicativo. Ele trabalha com arquivos que já estão disponíveis nos armazenamentos do dispositivo e que podem ser acessados pelos mecanismos de armazenamento autorizados pelo Android.

>Porém, quando eu digo que o Gerenciador de arquivos apenas visualiza, eu quero dizer que ele nao faz operações de arquivos, mas quando clica em um arquivo, ele abre um seletor de apps, ajudando muito para a coleta de evidências!, porém, se a usuária selecionar um app por esse seletor e perder os dados, o desenvolvedor não se responsabiliza!

### Arquivo seguro:

Ao tocar neste botão, o app te leva para uma activity que você pode adicionar arquivos usando o storage acess framework (S.A.F) e ele criptografa com a classe cripto, e você pode descriptografar ou excluir!

### Navegador: 

A navegação para o Google é feita diretamente pelo código usando window location replace(), sem disponibilizar o endereço como um link na interface. O aplicativo também não implementa um sistema próprio de registro de histórico de navegação, (ou pelo ou menos eu não coloquei na página)


>O aplicativo possui um navegador interno. A navegação para o Google é feita diretamente pelo código usando link na MainActivity sem disponibilizar o endereço como um link na interface. O aplicativo também não implementa um sistema próprio de registro de histórico de navegação, (ou pelo ou menos eu não coloquei na página)
>
>MainActivity > Google > pesquisa/link do Google
>
>Voltar > fecha a MainActivity


## Considerações finais:

e os audios do gravador de voz também são criptografados com a classe Cripto

e o recurso de proteção ppr barulho, chacalhoar o celular e escurecer a tela, tem como ativar e desativar!

todas as activitys tem a flag secure!

e no application do AndroidManifest do app tem allowBackup="false" 

# ❤️‍🩹Área do amparo:

A Área do amparo também utiliza o sistema "BiometricPrompt", com autenticação por "BIOMETRIC_WEAK" e "DEVICE_CREDENTIAL".

### Carteirinha de reivindicação dos direitos de transporte das mulheres:

Após o desbloqueio, é exibida uma carteirinha informativa que não constitui um documento oficial do governo. Ao tocá-la, a usuária é direcionada para uma página destinada à apresentação à equipe de motoristas do transporte, para solicitar o desembarque em um ponto mais seguro durante o período noturno, quando a legislação aplicável permitir.

### Carteirinha do gesto de ajuda:

Também há uma segunda carteirinha que, ao ser selecionada, direciona para um tutorial sobre como realizar o gesto internacional de combate à violência.

O gesto é um sinal silencioso de pedido de ajuda e pode ser utilizado em diferentes situações de violência. Ele não é exclusivo de mulheres: qualquer pessoa, independentemente de ser homem ou mulher, pode realizá-lo quando precisar sinalizar que necessita de ajuda.

# ⚙️Decisões técnicas:

lembre-se que hoje em dia uso github para compilar os apps e o a16 5g da samsung, então ele nao mata o processo de compilação mais!

### Porque usei webview e html?

porque ele e mais fluido, e também deixa o aplicativo mais leve em tamanho, um exemplo disso e o instagram lite, e porque nao precisa gerar muitos arquivos XMLs ou muito texto em kotlin para fazer todas as telas e além disso html com WebView é mais difícil de manter, mas eu tenho sim activitys em kotlin em xml e compose (mas não necessariamente o webview é mais leve universalmente!)

ah, mas o webview carrega uma versão cromium inteira...

e as activitys em kotlin e jetpack compose ou xml carrega imports do build, muitos arquivos e muitos textos para algo que dá para ser feito facilmente em html, e para determinadas telas, HTML/CSS/JavaScript permite implementar a interface com menos código específico de Android..., 

Mas a proposta e ele ser otimizado para ser mais rápido!

### Sobre injeção de código:

o app guarda os dados usando criptografia..., mas isso não garante que xss aconteça, mas eu só estou dizendo que ele guarda texto do diário por exemplo em criptografia, mas isso poderá acontecer como em qualquer outro app em certas condições..., e também eu uso o proguard8

### Sobre o WebView do app:

e APENAS A FUNÇÃO DE NAVEGADOR, AS OUTRAS SÃO OU FEITAS COM COMPOSE OU FEITAS COM XML, usa este webview:

```kotlin

val settings =  
        webView.settings  

settings.cacheMode =
    android.webkit.WebSettings.LOAD_DEFAULT

settings.loadsImagesAutomatically =
    true

settings.blockNetworkImage =
    false

settings.databaseEnabled =
    true

settings.displayZoomControls =
    false

settings.builtInZoomControls =
    false

settings.setSupportZoom(
    false
)

settings.textZoom =
    100

settings.defaultTextEncodingName =
    "UTF-8"

settings.mixedContentMode =
    android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW

    webView.overScrollMode =  
        View.OVER_SCROLL_NEVER  


    webView.isVerticalScrollBarEnabled =  
        false  


    webView.isFocusable =  
        true  


    webView.isFocusableInTouchMode =  
        true  


    webView.setOnFocusChangeListener {  
            _,  
            _ -> 
    }  


    webView.isHorizontalScrollBarEnabled =  
        false  


    webView.scrollBarStyle =  
        View.SCROLLBARS_INSIDE_OVERLAY  


    settings.javaScriptEnabled =  
        true  


    settings.mediaPlaybackRequiresUserGesture =  
        false  


    settings.domStorageEnabled =  
        true  


    settings.setGeolocationEnabled(  
        true  
    )  


    settings.allowFileAccess =  
        true  


    settings.allowContentAccess =  
        false  


    settings.allowFileAccessFromFileURLs =  
        false  


    settings.allowUniversalAccessFromFileURLs =  
        false  


    settings.javaScriptCanOpenWindowsAutomatically =  
        false  


    settings.setSupportMultipleWindows(  
        false  
    )  

```

```kotlin

onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        val urlAtual = webView.url

        if (urlAtual != null && urlAtual.contains("google.com")) {
            webView.clearHistory()
            finish()
        } else {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
})
```

### Por que, na FileActivity, o botão para trocar o tipo de armazenamento é unificado em um só?

O botão foi unificado para tornar a navegação entre os diferentes tipos de armazenamento mais simples e organizada, especialmente em situações de pânico ou urgência.

Em vez de apresentar vários botões separados, o mesmo botão alterna sequencialmente entre os armazenamentos disponíveis, seguindo o ciclo 1 → 2 → 3 → 1.

Essa escolha também foi pensada como uma forma de incentivar uma vistoria sequencial. Ao passar por cada armazenamento antes de retornar ao primeiro, a usuária é estimulada a verificar diferentes locais onde arquivos importantes podem estar, reduzindo a possibilidade de deixar algum armazenamento sem ser conferido por distração ou pressa.

é tipo assim:

armazenamento interno, para trocar para cartao sd, clica no botão e troca o icone, mas aí para voltar atrás, tem passar pelo cartao sd e pelo pendrive para só então voltar para o armazenamento interno

### Por que o WebView do app não trata "intent" para abrir outros aplicativos?

Por questões de segurança e privacidade.

Permitir que páginas dentro do WebView utilizem "intent" livremente para abrir outros aplicativos ou executar ações externas poderia aumentar os riscos de comportamentos inesperados, abuso de links e rastreamento de usuários.

Links também podem conter parâmetros de rastreamento, redirecionamentos e outros mecanismos capazes de identificar ou acompanhar a navegação.

Por isso, o WebView mantém esse comportamento limitado. A exceção é a tela de aplicativos, que possui uma função específica e um fluxo controlado pelo próprio app.

### Sobre o tema escuro, o conforto visual e a discrição:

O aplicativo oferece suporte a ícones monocromáticos, modo claro e modo escuro. O ícone do aplicativo também se adapta automaticamente ao tema selecionado, enquanto os sites acompanham a configuração, alterando seus fundos e elementos visuais conforme o modo ativo.

E o site do github pages usa essa estrutura (os nomes dos arquivos podem varias):

```
<link rel="stylesheet" href="claro.css" media="(prefers-color-scheme: light)">
<link rel="stylesheet" href="escuro.css" media="(prefers-color-scheme: dark)">
```

## Sobre as animações e o desempenho:

Além disso, as animações foram amplamente reduzidas, principalmente as animações de entrada. O projeto prioriza transições rápidas e discretas, mantendo apenas algumas animações pontuais quando elas contribuem para a experiência de uso. Dessa forma, a interface permanece visualmente agradável sem comprometer a agilidade e a responsividade do aplicativo.

# 🔗 Deep Link do aplicativo:

O aplicativo utiliza um Deep Link personalizado para permitir que o Android abra diretamente o Mulher Amparada:

com.mulheres://abrir

Esse endereço utiliza o identificador do aplicativo ("com.mulheres") como esquema de URI. Ao acessar o link em um dispositivo Android que possui o aplicativo instalado, o sistema pode encaminhar a abertura diretamente para o aplicativo, sem a necessidade de um site ou domínio externo.

O esquema é registrado no "AndroidManifest.xml" por meio de um "intent-filter":

<data android:scheme="com.mulheres" />

Assim, o endereço:

com.mulheres://abrir

funciona como uma forma direta de solicitar a abertura do aplicativo Mulher Amparada.

e temos um botão no index.html no site do github pages que leva pro app usando esse link também!

# 🔗Central de links e referências do projeto **Mulher Amparada**.

```

---

## 📱 Redes sociais:

- 🐦 [X — @mulheramparada](https://x.com/mulheramparada/status/2105045353615761521?s=20)
- 🌷 [Tumblr — Mulher Amparada: um projeto gratuito e livre](https://www.tumblr.com/mulheramparada/829407067773255680/mulher-amparada-um-projeto-gratuito-e-livre)
- 📰 [Blogger — Links do Projeto Mulher Amparada](https://mulheramparada.blogspot.com/2026/10/projeto-mulher-amparada-links.html?m=1)

---

## 📰 Imprensa e divulgação:

- 📢 [SubmitPR — Mulher Amparada](https://submitpr.org/press-release/mulheramparada)

---

## 🚀 Product Hunt:

- 🟣 [Product Hunt — Mulher Amparada](https://www.producthunt.com/products/mulher-amparada?launch=mulher-amparada)

- 📃 [Paper WF — Projeto Mulher Amparada](https://paper.wf/mulheramparada/projeto-mulher-amparada)

---
## 👨🏾‍💻 DEV Community:

- 📝 [DEV Community — Projeto Mulher Amparada](https://dev.to/mulher_amparada/projeto-mulher-amparada-1c49)

---
## 💻 CoderLegion:

- 🔗 [Links do Projeto Mulher Amparada](https://coderlegion.com/29843/links-do-projeto-mulher-amparada)

---
## 📰 Tabnews:

- 🔗 [Mulher Amparada — Desenvolvimento de um aplicativo Android independente](https://www.tabnews.com.br/projetomulheramparadaapp/mulher-amparada-desenvolvimento-de-um-aplicativo-android-independente)

---

## 🌱 Agregadores:

- 🚀 [NicheLoom — Mulher Amparada](https://www.nicheloom.com/launches/40835/)

- ⚠️ [Observatório Blockchain — link atualmente retorna 404](https://observatorioblockchain.com/newsfeed/item/23123e760a8918f1/)

---

```

# 📦Packpage do repositório no github:

ele é um packpage focado para quem quiser divulgar o projeto!

### ele contém:

pngs de divulgação com link, texto e qr do site do giithub pages;

o proprio qr do site do giithub pages;

banners do projeto, tanto png, tanto um código para html;

icones para sites e para usos no geral, tanto o do mulher amparada tanto o ic_launcher do app, versão normal e monocromático;

>APENAS PARA AS USUÁRIAS!: Ao indicar o projeto para uma mulher em situação de risco, **priorize o compartilhamento do QR Code impresso ou na tela**, em vez de enviar links de texto por mensagens (como WhatsApp ou SMS). 
>
**Por que o QR Code?** Links de texto deixam rastros fáceis de serem interceptados por agressores que monitoram o celular da vítima. 
>
>**Como agir:** Se for imprimir cartazes ou banners, certifique-se de que o QR Code está visível e em alta resolução. Isso permite que a usuária aponte a câmera e acesse o projeto diretamente, minimizando o histórico de digitação e mensagens trocadas.

a pessoa instala pelo github releases!

e quem publica e atualiza essa release é o proprio workflow!
