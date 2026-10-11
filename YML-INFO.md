# ⚙️ Automações do Mulher Amparada

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