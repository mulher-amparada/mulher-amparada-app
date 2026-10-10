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

# 🎨 `gerar-linguagens`

## Imagem das linguagens de programação

O job `gerar-linguagens` substitui a antiga etapa de documentação direta do workflow no README.

Ele é responsável por executar:

`tools/languages.go`

O job utiliza Go 1.25 através de:

`actions/setup-go@v6`

Antes da execução, o README atualizado é baixado através do artifact:

`arquivo-readme`

Depois o programa:

`tools/languages.go`

é executado.

O programa gera:

`github-languages.png`

e atualiza o:

`README.md`

A imagem representa as linguagens de programação detectadas pelo projeto.

O README atualizado é enviado como:

`arquivo-readme-linguagens`

A imagem é enviada como:

`github-languages`

---

# 🖼️ `github-languages.png`

A imagem:

`github-languages.png`

é gerada automaticamente pelo programa:

`tools/languages.go`

Ela é posteriormente copiada para a raiz do repositório.

Dessa maneira, o README pode utilizar a imagem diretamente a partir do próprio repositório.

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