# Documentação do workflow — Mulher Amparada

- **Arquivo de origem:** `.github/workflows/automations.yml`
- **Arquivo gerado:** `.github/workflows/workflow.md`
- **Jobs identificados:** 16
- **Etapas identificadas:** 76

## Tabela completa de jobs e etapas

| ID do job | Nome | Runner | Dependências | Etapas |
|---|---|---|---|---|
| `build` | Compilar APK Kotlin | ubuntu-24.04 | Nenhuma | 1. Baixar código (`actions/checkout@v6`); 2. Verificar projeto — executa comandos; 3. Configurar Java 17 (`actions/setup-java@v6`); 4. Verificar Android SDK 37 — executa comandos; 5. Dar permissão ao Gradle — executa comandos; 6. Compilar APK — executa comandos; 7. Localizar APK — executa comandos; 8. Assinar APK — executa comandos; 9. Preparar APK da Release — executa comandos; 10. APK-Kotlin-Assinado (`actions/upload-artifact@v6`); 11. Calcular tamanho real do APK — executa comandos; 12. Calcular SHA-256 — executa comandos; 13. Atualizar APK no main — executa comandos; 14. Verificar Release — executa comandos; 15. Excluir APK antigo da Release — executa comandos; 16. Enviar APK para a Release — executa comandos; 17. Atualizar SHA-256 da Release — executa comandos; 18. Atualizar link, tamanho e SHA-256 no download.html — executa comandos; 19. Enviar download.html atualizado para o main — executa comandos; 20. Mostrar resumo — executa comandos |
| `linguagens` | Analisar linguagens e atualizar estilos | ubuntu-24.04 | `build`, `publicar-divulgacao` | 1. Baixar repositório completo (`actions/checkout@v6`); 2. Instalar cloc — executa comandos; 3. Analisar arquivos e linguagens — executa comandos; 4. Gerar cores e atualizar os dois temas — executa comandos; 5. Validar relatórios, CSS e pacote — executa comandos; 6. relatorio-linguagens (`actions/upload-artifact@v6`) |
| `bot` | Análise de dados em Julia | ubuntu-24.04 | Nenhuma | 1. Baixar repositório (`actions/checkout@v6`); 2. Instalar Julia (`julia-actions/setup-julia@v3`); 3. Instalar dependências Julia — executa comandos; 4. Executar análise de dados em Julia — executa comandos; 5. Validar e preparar resultados — executa comandos; 6. resultados-ligue180 (`actions/upload-artifact@v6`) |
| `build-site` | build-site | ubuntu-24.04 | `build` | 1. Checkout (`actions/checkout@v6`); 2. Setup Pages (`actions/configure-pages@v6`); 3. Build with Jekyll (`actions/jekyll-build-pages@v1`); 4. Upload Pages artifact (`actions/upload-pages-artifact@v5`) |
| `deploy` | deploy | ubuntu-24.04 | `build-site` | 1. Deploy to GitHub Pages (`actions/deploy-pages@v5`) |
| `atualizar-commits` | atualizar-commits | ubuntu-24.04 | `deploy` | 1. Baixar todo o histórico (`actions/checkout@v6`); 2. Atualizar total de commits — executa comandos; 3. arquivo-readme (`actions/upload-artifact@v6`); 4. Publicar README na main — executa comandos |
| `sincronizar-devto` | Sincronizar README com DEV.to | ubuntu-24.04 | `atualizar-commits` | 1. Baixar código atualizado (`actions/checkout@v6`); 2. Atualizar artigo no DEV.to — executa comandos |
| `sitemap` | Gerar Sitemap | ubuntu-24.04 | `deploy` | 1. Checkout (`actions/checkout@v6`); 2. Gerar Sitemap (`cicirello/generate-sitemap@v1`); 3. arquivo-sitemap (`actions/upload-artifact@v6`) |
| `indexnow` | IndexNow | ubuntu-24.04 | `deploy`, `bot`, `linguagens`, `publicar-divulgacao`, `atualizar-commits` | 1. Enviar Sitemap ao IndexNow (`bojieyang/indexnow-action@v3`) |
| `lighthouse` | Lighthouse | ubuntu-24.04 | `deploy` | 1. Lighthouse (`treosh/lighthouse-ci-action@v12`) |
| `publicar-divulgacao` | Publicar Kit de Divulgação | ubuntu-24.04 | Nenhuma | 1. Baixar repositório (`actions/checkout@v6`); 2. Configurar Node.js (`actions/setup-node@v6`); 3. Criar pacote de divulgação — executa comandos; 4. Kit de Divulgação (`softprops/action-gh-release@v3`) |
| `observatorio` | Observatório | ubuntu-24.04 | `atualizar-commits` | 1. Baixar repositório completo (`actions/checkout@v6`); 2. Instalar dependências do Observatório — executa comandos; 3. Compilar observatório — executa comandos; 4. Executar observatório — executa comandos; 5. Verificar todos os mapas gerados — executa comandos; 6. observatorio (`actions/upload-artifact@v6`) |
| `atualizar-footer` | Atualizar Footer com README | ubuntu-24.04 | `atualizar-commits` | 1. Baixar repositório (`actions/checkout@v6`); 2. Instalar Python (`actions/setup-python@v6`); 3. Instalar dependência Python — executa comandos; 4. Atualizar footer com README — executa comandos; 5. arquivo-footer (`actions/upload-artifact@v6`) |
| `atualizar-relatorio-commits` | Atualizar relatório de commits | ubuntu-24.04 | `atualizar-commits` | 1. Baixar repositório (`actions/checkout@v6`); 2. Instalar Ruby (`ruby/setup-ruby@v1`); 3. Gerar relatório de commits — executa comandos; 4. arquivos-relatorio-commits (`actions/upload-artifact@v6`) |
| `pesquisar-projeto` | Documentar workflow YAML | ubuntu-24.04 | Nenhuma | 1. Baixar repositório (`actions/checkout@v6`); 2. Instalar PHP — executa comandos; 3. Gerar documentação do workflow — executa comandos; 4. arquivos-pesquisas (`actions/upload-artifact@v6`) |
| `salvar-na-main` | Salvar artifacts na main pelo Mulher Amparada Bot | ubuntu-24.04 | `build`, `linguagens`, `bot`, `atualizar-commits`, `sitemap`, `atualizar-footer`, `atualizar-relatorio-commits`, `pesquisar-projeto`, `observatorio` | 1. Baixar código-fonte da main (`actions/checkout@v6`); 2. Baixar todos os artifacts desta execução (`actions/download-artifact@v8`); 3. Consultar workflow atual — executa comandos; 4. Compilar o publicador C++ — executa comandos; 5. Publicar na main pelo GitHub App — executa comandos |

## Grafo completo de dependências

As setas representam as dependências declaradas em `needs` no YAML original.

```mermaid
flowchart TD
    job_build["Compilar APK Kotlin"]
    job_linguagens["Analisar linguagens e atualizar estilos"]
    job_bot["Análise de dados em Julia"]
    job_build_site["build-site"]
    job_deploy["deploy"]
    job_atualizar_commits["atualizar-commits"]
    job_sincronizar_devto["Sincronizar README com DEV.to"]
    job_sitemap["Gerar Sitemap"]
    job_indexnow["IndexNow"]
    job_lighthouse["Lighthouse"]
    job_publicar_divulgacao["Publicar Kit de Divulgação"]
    job_observatorio["Observatório"]
    job_atualizar_footer["Atualizar Footer com README"]
    job_atualizar_relatorio_commits["Atualizar relatório de commits"]
    job_pesquisar_projeto["Documentar workflow YAML"]
    job_salvar_na_main["Salvar artifacts na main pelo Mulher Amparada Bot"]
    job_build --> job_linguagens
    job_publicar_divulgacao --> job_linguagens
    job_build --> job_build_site
    job_build_site --> job_deploy
    job_deploy --> job_atualizar_commits
    job_atualizar_commits --> job_sincronizar_devto
    job_deploy --> job_sitemap
    job_deploy --> job_indexnow
    job_bot --> job_indexnow
    job_linguagens --> job_indexnow
    job_publicar_divulgacao --> job_indexnow
    job_atualizar_commits --> job_indexnow
    job_deploy --> job_lighthouse
    job_atualizar_commits --> job_observatorio
    job_atualizar_commits --> job_atualizar_footer
    job_atualizar_commits --> job_atualizar_relatorio_commits
    job_build --> job_salvar_na_main
    job_linguagens --> job_salvar_na_main
    job_bot --> job_salvar_na_main
    job_atualizar_commits --> job_salvar_na_main
    job_sitemap --> job_salvar_na_main
    job_atualizar_footer --> job_salvar_na_main
    job_atualizar_relatorio_commits --> job_salvar_na_main
    job_pesquisar_projeto --> job_salvar_na_main
    job_observatorio --> job_salvar_na_main
```

## Detalhamento dos jobs

### Compilar APK Kotlin

- **ID:** `build`
- **Runner:** `ubuntu-24.04`
- **Dependências:** Nenhuma

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar código | `actions/checkout@v6` | — |
| 2 | Verificar projeto | — | \| |
| 3 | Configurar Java 17 | `actions/setup-java@v6` | — |
| 4 | Verificar Android SDK 37 | — | \| |
| 5 | Dar permissão ao Gradle | — | chmod +x gradlew |
| 6 | Compilar APK | — | \| |
| 7 | Localizar APK | — | \| |
| 8 | Assinar APK | — | \| |
| 9 | Preparar APK da Release | — | \| |
| 10 | APK-Kotlin-Assinado | `actions/upload-artifact@v6` | — |
| 11 | Calcular tamanho real do APK | — | \| |
| 12 | Calcular SHA-256 | — | \| |
| 13 | Atualizar APK no main | — | \| |
| 14 | Verificar Release | — | \| |
| 15 | Excluir APK antigo da Release | — | \| |
| 16 | Enviar APK para a Release | — | \| |
| 17 | Atualizar SHA-256 da Release | — | \| |
| 18 | Atualizar link, tamanho e SHA-256 no download.html | — | \| |
| 19 | Enviar download.html atualizado para o main | — | \| |
| 20 | Mostrar resumo | — | \| |

### Analisar linguagens e atualizar estilos

- **ID:** `linguagens`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `build`, `publicar-divulgacao`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar repositório completo | `actions/checkout@v6` | — |
| 2 | Instalar cloc | — | \| |
| 3 | Analisar arquivos e linguagens | — | \| |
| 4 | Gerar cores e atualizar os dois temas | — | \| |
| 5 | Validar relatórios, CSS e pacote | — | \| |
| 6 | relatorio-linguagens | `actions/upload-artifact@v6` | — |

### Análise de dados em Julia

- **ID:** `bot`
- **Runner:** `ubuntu-24.04`
- **Dependências:** Nenhuma

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar repositório | `actions/checkout@v6` | — |
| 2 | Instalar Julia | `julia-actions/setup-julia@v3` | — |
| 3 | Instalar dependências Julia | — | \| |
| 4 | Executar análise de dados em Julia | — | \| |
| 5 | Validar e preparar resultados | — | \| |
| 6 | resultados-ligue180 | `actions/upload-artifact@v6` | — |

### build-site

- **ID:** `build-site`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `build`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Checkout | `actions/checkout@v6` | — |
| 2 | Setup Pages | `actions/configure-pages@v6` | — |
| 3 | Build with Jekyll | `actions/jekyll-build-pages@v1` | — |
| 4 | Upload Pages artifact | `actions/upload-pages-artifact@v5` | — |

### deploy

- **ID:** `deploy`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `build-site`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Deploy to GitHub Pages | `actions/deploy-pages@v5` | — |

### atualizar-commits

- **ID:** `atualizar-commits`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `deploy`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar todo o histórico | `actions/checkout@v6` | — |
| 2 | Atualizar total de commits | — | \| |
| 3 | arquivo-readme | `actions/upload-artifact@v6` | — |
| 4 | Publicar README na main | — | \| |

### Sincronizar README com DEV.to

- **ID:** `sincronizar-devto`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `atualizar-commits`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar código atualizado | `actions/checkout@v6` | — |
| 2 | Atualizar artigo no DEV.to | — | \| |

### Gerar Sitemap

- **ID:** `sitemap`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `deploy`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Checkout | `actions/checkout@v6` | — |
| 2 | Gerar Sitemap | `cicirello/generate-sitemap@v1` | — |
| 3 | arquivo-sitemap | `actions/upload-artifact@v6` | — |

### IndexNow

- **ID:** `indexnow`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `deploy`, `bot`, `linguagens`, `publicar-divulgacao`, `atualizar-commits`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Enviar Sitemap ao IndexNow | `bojieyang/indexnow-action@v3` | — |

### Lighthouse

- **ID:** `lighthouse`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `deploy`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Lighthouse | `treosh/lighthouse-ci-action@v12` | — |

### Publicar Kit de Divulgação

- **ID:** `publicar-divulgacao`
- **Runner:** `ubuntu-24.04`
- **Dependências:** Nenhuma

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar repositório | `actions/checkout@v6` | — |
| 2 | Configurar Node.js | `actions/setup-node@v6` | — |
| 3 | Criar pacote de divulgação | — | npm pack |
| 4 | Kit de Divulgação | `softprops/action-gh-release@v3` | — |

### Observatório

- **ID:** `observatorio`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `atualizar-commits`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar repositório completo | `actions/checkout@v6` | — |
| 2 | Instalar dependências do Observatório | — | \| |
| 3 | Compilar observatório | — | \| |
| 4 | Executar observatório | — | \| |
| 5 | Verificar todos os mapas gerados | — | \| |
| 6 | observatorio | `actions/upload-artifact@v6` | — |

### Atualizar Footer com README

- **ID:** `atualizar-footer`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `atualizar-commits`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar repositório | `actions/checkout@v6` | — |
| 2 | Instalar Python | `actions/setup-python@v6` | — |
| 3 | Instalar dependência Python | — | pip install Markdown |
| 4 | Atualizar footer com README | — | python3 scripts/atualizar_footer.py |
| 5 | arquivo-footer | `actions/upload-artifact@v6` | — |

### Atualizar relatório de commits

- **ID:** `atualizar-relatorio-commits`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `atualizar-commits`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar repositório | `actions/checkout@v6` | — |
| 2 | Instalar Ruby | `ruby/setup-ruby@v1` | — |
| 3 | Gerar relatório de commits | — | ruby scripts/commits.rb |
| 4 | arquivos-relatorio-commits | `actions/upload-artifact@v6` | — |

### Documentar workflow YAML

- **ID:** `pesquisar-projeto`
- **Runner:** `ubuntu-24.04`
- **Dependências:** Nenhuma

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar repositório | `actions/checkout@v6` | — |
| 2 | Instalar PHP | — | \| |
| 3 | Gerar documentação do workflow | — | \| |
| 4 | arquivos-pesquisas | `actions/upload-artifact@v6` | — |

### Salvar artifacts na main pelo Mulher Amparada Bot

- **ID:** `salvar-na-main`
- **Runner:** `ubuntu-24.04`
- **Dependências:** `build`, `linguagens`, `bot`, `atualizar-commits`, `sitemap`, `atualizar-footer`, `atualizar-relatorio-commits`, `pesquisar-projeto`, `observatorio`

| # | Etapa | Ação | Execução |
|---:|---|---|---|
| 1 | Baixar código-fonte da main | `actions/checkout@v6` | — |
| 2 | Baixar todos os artifacts desta execução | `actions/download-artifact@v8` | — |
| 3 | Consultar workflow atual | — | \| |
| 4 | Compilar o publicador C++ | — | \| |
| 5 | Publicar na main pelo GitHub App | — | \| |

## YAML original completo

O bloco abaixo reproduz o conteúdo original do arquivo `automations.yml`, preservando comandos, condições, variáveis, comentários e configurações.

````yaml
name: Automações do Mulher Amparada

on:
  push:
    branches:
      - '**'

  workflow_dispatch:


permissions:
  contents: write
  pages: write
  id-token: write
  actions: read

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: ${{ github.event_name != 'workflow_dispatch' }}


jobs:

  # =========================================================
  # COMPILAR APK
  # =========================================================

  build:

    name: Compilar APK Kotlin

    runs-on: ubuntu-24.04

    env:
      App1_DIR: "Código-fonte do app = Mulher Amparada"
      RELEASE_TAG: "app"
      RELEASE_APK_NAME: "app-debug-assinado.apk"

    steps:

      # =========================================================
      # BAIXAR CÓDIGO
      # =========================================================

      - name: Baixar código
        uses: actions/checkout@v6
        with:
          fetch-depth: 1


      # =========================================================
      # VERIFICAR PROJETO
      # =========================================================

      - name: Verificar projeto
        run: |

          if [ ! -d "$App1_DIR" ]; then
            echo "ERRO: pasta não encontrada:"
            echo "$App1_DIR"
            exit 1
          fi

          if [ ! -f "$App1_DIR/gradlew" ]; then
            echo "ERRO: gradlew não encontrado."
            exit 1
          fi


      # =========================================================
      # JAVA
      # =========================================================

      - name: Configurar Java 17
        uses: actions/setup-java@v6
        with:
          distribution: temurin
          java-version: "17"
          cache: gradle


      # =========================================================
      # VERIFICAR ANDROID SDK 37
      # =========================================================

      - name: Verificar Android SDK 37
        run: |

          echo "ANDROID_HOME:"
          echo "$ANDROID_HOME"

          echo ""
          echo "Plataformas Android instaladas:"
          ls -la "$ANDROID_HOME/platforms"

          echo ""
          echo "Plataformas 37 disponíveis:"
          find "$ANDROID_HOME/platforms" \
            -maxdepth 1 \
            -type d \
            -name "android-37*"

          if ! find "$ANDROID_HOME/platforms" \
            -maxdepth 1 \
            -type d \
            -name "android-37*" \
            | grep -q .; then

            echo "ERRO: nenhuma plataforma Android 37 encontrada."
            exit 1
          fi


      # =========================================================
      # PERMISSÃO DO GRADLE
      # =========================================================

      - name: Dar permissão ao Gradle
        working-directory: ${{ env.App1_DIR }}
        run: chmod +x gradlew


      # =========================================================
      # COMPILAR APK
      # =========================================================

      - name: Compilar APK
        id: compilar_apk
        working-directory: ${{ env.App1_DIR }}
        run: |

          set -o pipefail

          ./gradlew assembleDebug --stacktrace 2>&1 \
            | tee "$GITHUB_WORKSPACE/gradle-build.log"

          STATUS=${PIPESTATUS[0]}

          if [ "$STATUS" -ne 0 ]; then

            {
              echo ""
              echo "# ❌ ERRO NA COMPILAÇÃO"
              echo ""
              echo "## 🔴 Etapa"
              echo ""
              echo "**Compilar APK**"
              echo ""
              echo "## 💥 O que aconteceu"
              echo ""
              echo "O Gradle não conseguiu compilar o APK."
              echo ""
              echo "## 📋 Erro do Gradle"
              echo ""
              echo '```text'
              tail -n 100 "$GITHUB_WORKSPACE/gradle-build.log"
              echo '```'
              echo ""
              echo "**Código de saída:** \`$STATUS\`"
            } >> "$GITHUB_STEP_SUMMARY"

            exit "$STATUS"
          fi


      # =========================================================
      # LOCALIZAR APK
      # =========================================================

      - name: Localizar APK
        id: apk
        working-directory: ${{ env.App1_DIR }}
        run: |

          APK=$(find app/build/outputs/apk/debug \
            -type f \
            -name "*.apk" \
            | head -n 1)

          if [ -z "$APK" ]; then
            echo "ERRO: APK não encontrado."
            exit 1
          fi

          echo "APK encontrado:"
          echo "$APK"

          echo "apk=$APK" >> "$GITHUB_OUTPUT"


      # =========================================================
      # ASSINAR APK
      # =========================================================

      - name: Assinar APK
        working-directory: ${{ env.App1_DIR }}
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |

          if [ -z "$KEYSTORE_BASE64" ]; then
            echo "ERRO: KEYSTORE_BASE64 não configurado."
            exit 1
          fi

          if [ -z "$KEYSTORE_PASSWORD" ]; then
            echo "ERRO: KEYSTORE_PASSWORD não configurado."
            exit 1
          fi

          if [ -z "$KEY_ALIAS" ]; then
            echo "ERRO: KEY_ALIAS não configurado."
            exit 1
          fi

          if [ -z "$KEY_PASSWORD" ]; then
            echo "ERRO: KEY_PASSWORD não configurado."
            exit 1
          fi

          echo "$KEYSTORE_BASE64" | base64 --decode > mulher-amparada.jks

          APK="${{ steps.apk.outputs.apk }}"

          if [ ! -f "$APK" ]; then
            echo "ERRO: APK não encontrado:"
            echo "$APK"
            rm -f mulher-amparada.jks
            exit 1
          fi

          APKSIGNER=$(find "$ANDROID_HOME/build-tools" \
            -type f \
            -name "apksigner" \
            | sort -V \
            | tail -n 1)

          if [ -z "$APKSIGNER" ]; then
            echo "ERRO: apksigner não encontrado."
            rm -f mulher-amparada.jks
            exit 1
          fi

          echo "Assinando APK..."

          "$APKSIGNER" sign \
            --ks mulher-amparada.jks \
            --ks-key-alias "$KEY_ALIAS" \
            --ks-pass "pass:$KEYSTORE_PASSWORD" \
            --key-pass "pass:$KEY_PASSWORD" \
            --out app-assinado.apk \
            "$APK"

          mv app-assinado.apk "$APK"

          echo "Verificando assinatura..."

          "$APKSIGNER" verify \
            --verbose \
            "$APK"

          rm -f mulher-amparada.jks

          echo "APK assinado e verificado com sucesso."


      # =========================================================
      # PREPARAR APK DA RELEASE
      # =========================================================

      - name: Preparar APK da Release
        working-directory: ${{ env.App1_DIR }}
        run: |

          APK="${{ steps.apk.outputs.apk }}"

          cp "$APK" "$GITHUB_WORKSPACE/$RELEASE_APK_NAME"

          echo "APK preparado:"
          echo "$GITHUB_WORKSPACE/$RELEASE_APK_NAME"


      # =========================================================
      # ARTIFACT
      # =========================================================

      - name: Enviar APK como artifact
        uses: actions/upload-artifact@v6
        with:
          name: APK-Kotlin-Assinado
          path: "${{ github.workspace }}/${{ env.RELEASE_APK_NAME }}"
          if-no-files-found: error


      # =========================================================
      # TAMANHO REAL
      # =========================================================

      - name: Calcular tamanho real do APK
        id: apk_size
        run: |

          APK="$GITHUB_WORKSPACE/$RELEASE_APK_NAME"

          if [ ! -f "$APK" ]; then
            echo "ERRO: APK não encontrado."
            exit 1
          fi

          BYTES=$(stat -c%s "$APK")

          SIZE_MB=$(awk "BEGIN {
            printf \"%.2f\", $BYTES / 1024 / 1024
          }")

          SIZE_FORMATADO="${SIZE_MB} MB"

          echo "========================================"
          echo "TAMANHO REAL DO APK ASSINADO"
          echo "========================================"
          echo "Arquivo: $APK"
          echo "Bytes: $BYTES"
          echo "MB: $SIZE_FORMATADO"
          echo "========================================"

          echo "bytes=$BYTES" >> "$GITHUB_OUTPUT"
          echo "size_mb=$SIZE_MB" >> "$GITHUB_OUTPUT"
          echo "size_formatado=$SIZE_FORMATADO" >> "$GITHUB_OUTPUT"


      # =========================================================
      # SHA-256
      # =========================================================

      - name: Calcular SHA-256
        id: sha256
        run: |

          APK="$GITHUB_WORKSPACE/$RELEASE_APK_NAME"

          if [ ! -f "$APK" ]; then
            echo "ERRO: APK não encontrado."
            exit 1
          fi

          SHA256=$(sha256sum "$APK" | awk '{print $1}')

          echo "SHA-256:"
          echo "$SHA256"

          echo "sha256=$SHA256" >> "$GITHUB_OUTPUT"


      # =========================================================
      # ATUALIZAR APK NO MAIN
      # =========================================================

      - name: Atualizar APK no main
        run: |

          git config user.name "github-actions[bot]"
          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"

          TARGET="${App1_DIR}/app-release.apk"

          cp "$GITHUB_WORKSPACE/$RELEASE_APK_NAME" "$TARGET"

          git fetch origin main

          git reset --hard origin/main

          cp "$GITHUB_WORKSPACE/$RELEASE_APK_NAME" "$TARGET"

          git add "$TARGET"

          if git diff --cached --quiet; then
            echo "APK já está atualizado."
          else
            git commit -m "Atualizar APK assinado [skip ci]"
            git push origin main
          fi


      # =========================================================
      # VERIFICAR RELEASE
      # =========================================================

      - name: Verificar Release
        env:
          GH_TOKEN: ${{ github.token }}
        run: |

          if ! gh release view "$RELEASE_TAG" \
            --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1; then

            echo "ERRO: A Release '$RELEASE_TAG' não existe."
            exit 1
          fi

          echo "Release encontrada."


      # =========================================================
      # EXCLUIR APK ANTIGO
      # =========================================================

      - name: Excluir APK antigo da Release
        env:
          GH_TOKEN: ${{ github.token }}
        run: |

          ASSET_ID=$(gh api \
            "repos/$GITHUB_REPOSITORY/releases/tags/$RELEASE_TAG" \
            --jq ".assets[] | select(.name == \"$RELEASE_APK_NAME\") | .id" \
            | head -n 1)

          if [ -n "$ASSET_ID" ]; then

            echo "Excluindo APK antigo..."

            gh api \
              --method DELETE \
              "repos/$GITHUB_REPOSITORY/releases/assets/$ASSET_ID"

            echo "APK antigo excluído."

          else

            echo "Nenhum APK antigo encontrado."

          fi


      # =========================================================
      # ENVIAR APK PARA RELEASE
      # =========================================================

      - name: Enviar APK para a Release
        env:
          GH_TOKEN: ${{ github.token }}
        run: |

          gh release upload \
            "$RELEASE_TAG" \
            "$GITHUB_WORKSPACE/$RELEASE_APK_NAME" \
            --repo "$GITHUB_REPOSITORY"

          echo "Novo APK enviado para a Release."


      # =========================================================
      # ATUALIZAR SHA-256 DA RELEASE
      # =========================================================

      - name: Atualizar SHA-256 da Release
        env:
          GH_TOKEN: ${{ github.token }}
        run: |

          SHA256="${{ steps.sha256.outputs.sha256 }}"

          gh release view "$RELEASE_TAG" \
            --repo "$GITHUB_REPOSITORY" \
            --json body \
            --jq '.body' > release-notes.txt

          if grep -q "SHA-256 gerado nesta compilação:" release-notes.txt; then

            sed -i \
              "s/^SHA-256 gerado nesta compilação:.*$/SHA-256 gerado nesta compilação: $SHA256/" \
              release-notes.txt

          else

            printf '\n\nSHA-256 gerado nesta compilação: %s\n' "$SHA256" \
              >> release-notes.txt

          fi

          gh release edit "$RELEASE_TAG" \
            --repo "$GITHUB_REPOSITORY" \
            --notes-file release-notes.txt

          echo "SHA-256 atualizado:"
          echo "$SHA256"


      # =========================================================
      # ATUALIZAR download.html
      # =========================================================

      - name: Atualizar link, tamanho e SHA-256 no download.html
        working-directory: ${{ github.workspace }}
        run: |

          ASSET_URL="https://github.com/$GITHUB_REPOSITORY/releases/download/$RELEASE_TAG/$RELEASE_APK_NAME"

          APK_SIZE="${{ steps.apk_size.outputs.size_formatado }}"

          SHA256="${{ steps.sha256.outputs.sha256 }}"

          echo "Novo link:"
          echo "$ASSET_URL"

          echo "Tamanho REAL do APK:"
          echo "$APK_SIZE"

          echo "SHA-256:"
          echo "$SHA256"

          python3 - <<PY

          from pathlib import Path
          import re

          arquivo = Path("download.html")

          if not arquivo.exists():
              raise SystemExit(
                  "ERRO: download.html não encontrado."
              )

          texto = arquivo.read_text(
              encoding="utf-8"
          )

          novo_url = """$ASSET_URL"""
          novo_tamanho = """$APK_SIZE"""
          novo_sha256 = """$SHA256"""

          padrao_url = (
              r'(<a\s+class="download"\s+href=")'
              r'[^"]*'
              r'(")'
          )

          texto, quantidade_url = re.subn(
              padrao_url,
              lambda m:
                  m.group(1)
                  + novo_url
                  + m.group(2),
              texto,
              count=1
          )

          if quantidade_url == 0:
              raise SystemExit(
                  'ERRO: <a class="download" ...> não encontrado.'
              )

          padrao_resumo = (
              r'(<strong\s+class="summary-value">\s*)'
              r'(?:[0-9]+(?:[.,][0-9]+)?\s*MB)'
              r'(\s*</strong>)'
          )

          texto, quantidade_resumo = re.subn(
              padrao_resumo,
              lambda m:
                  m.group(1)
                  + novo_tamanho
                  + m.group(2),
              texto,
              count=1
          )

          if quantidade_resumo == 0:
              raise SystemExit(
                  "ERRO: tamanho no resumo não encontrado."
              )

          padrao_detalhe = (
              r'(<strong\s+class="detail-value">\s*)'
              r'≈?\s*[0-9]+(?:[.,][0-9]+)?\s*MB'
              r'(\s*</strong>)'
          )

          texto, quantidade_detalhe = re.subn(
              padrao_detalhe,
              lambda m:
                  m.group(1)
                  + "≈ "
                  + novo_tamanho
                  + m.group(2),
              texto,
              count=1
          )

          if quantidade_detalhe == 0:
              raise SystemExit(
                  "ERRO: tamanho nas informações do app não encontrado."
              )

          padrao_sha256 = (
              r'(<span\s+class="detail-title">\s*'
              r'Código SHA-256\s*'
              r'</span>\s*'
              r'<strong\s+class="detail-value">\s*)'
              r'[^<]*'
              r'(\s*</strong>)'
          )

          texto, quantidade_sha256 = re.subn(
              padrao_sha256,
              lambda m:
                  m.group(1)
                  + novo_sha256
                  + m.group(2),
              texto,
              count=1
          )

          if quantidade_sha256 == 0:
              raise SystemExit(
                  "ERRO: campo Código SHA-256 não encontrado."
              )

          arquivo.write_text(
              texto,
              encoding="utf-8"
          )

          print("download.html atualizado.")
          print("Novo link:", novo_url)
          print("Novo tamanho REAL do APK:", novo_tamanho)
          print("Novo SHA-256:", novo_sha256)

          PY


      # =========================================================
      # ENVIAR download.html PARA MAIN
      # =========================================================

      - name: Enviar download.html atualizado para o main
        run: |

          git config user.name "github-actions[bot]"
          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"

          git add download.html

          if git diff --cached --quiet; then

            echo "download.html já está atualizado."

          else

            git commit -m "Atualizar link, tamanho e SHA-256 do APK [skip ci]"

            git push origin main

          fi


      # =========================================================
      # RESUMO
      # =========================================================

      - name: Mostrar resumo
        run: |

          echo "## Compilação concluída" >> "$GITHUB_STEP_SUMMARY"

          echo "" >> "$GITHUB_STEP_SUMMARY"

          echo "**Release:** $RELEASE_TAG" \
            >> "$GITHUB_STEP_SUMMARY"

          echo "**Branch:** main" \
            >> "$GITHUB_STEP_SUMMARY"

          echo "**Release recebe:** $RELEASE_APK_NAME" \
            >> "$GITHUB_STEP_SUMMARY"

          echo "" >> "$GITHUB_STEP_SUMMARY"

          echo "**Tamanho REAL do APK assinado:** ${{ steps.apk_size.outputs.size_formatado }}" \
            >> "$GITHUB_STEP_SUMMARY"

          echo "**Tamanho em bytes:** ${{ steps.apk_size.outputs.bytes }}" \
            >> "$GITHUB_STEP_SUMMARY"

          echo "" >> "$GITHUB_STEP_SUMMARY"

          echo "**SHA-256 do APK:**" \
            >> "$GITHUB_STEP_SUMMARY"

          echo '```' \
            >> "$GITHUB_STEP_SUMMARY"

          echo "${{ steps.sha256.outputs.sha256 }}" \
            >> "$GITHUB_STEP_SUMMARY"

          echo '```' \
            >> "$GITHUB_STEP_SUMMARY"


  # =========================================================
  # ANALISAR LINGUAGENS E ATUALIZAR CORES AUTOMATICAMENTE
  # =========================================================

  linguagens:
    name: Analisar linguagens e atualizar estilos
    runs-on: ubuntu-24.04
    timeout-minutes: 15
    needs:
      - build
      - publicar-divulgacao

    steps:
      - name: Baixar repositório completo
        uses: actions/checkout@v6
        with:
          fetch-depth: 0

      - name: Instalar cloc
        run: |
          set -euo pipefail
          sudo apt-get update
          sudo apt-get install -y cloc

      # =======================================================
      # ANALISAR TODAS AS LINGUAGENS
      # =======================================================

      - name: Analisar arquivos e linguagens
        shell: bash
        run: |
          set -euo pipefail

          mkdir -p relatorios

          git ls-files > "$RUNNER_TEMP/arquivos-repositorio.txt"

          test -s "$RUNNER_TEMP/arquivos-repositorio.txt"

          cloc \
            --list-file="$RUNNER_TEMP/arquivos-repositorio.txt" \
            --json \
            --out=relatorios/linguagens.json

          cloc \
            --list-file="$RUNNER_TEMP/arquivos-repositorio.txt" \
            --md \
            --out=relatorios/linguagens.md

          python3 -m json.tool relatorios/linguagens.json >/dev/null

      # =======================================================
      # ATUALIZAR CSS E CLASSES DO SITE
      # =======================================================

      - name: Gerar cores e atualizar os dois temas
        shell: bash
        run: |
          set -euo pipefail

          python3 - <<'PY'
          import colorsys
          import hashlib
          import json
          import re
          import shutil
          import unicodedata
          from pathlib import Path

          ROOT = Path.cwd()
          PACOTE = ROOT / "pacote-linguagens"

          DAY = ROOT / "linguagens-day.css"
          NIGHT = ROOT / "linguagens-night.css"
          RELATORIO = ROOT / "relatorios/linguagens.json"

          for arquivo in (DAY, NIGHT, RELATORIO):
              if not arquivo.is_file():
                  raise SystemExit(f"Arquivo obrigatório ausente: {arquivo}")

          dados = json.loads(RELATORIO.read_text(encoding="utf-8"))

          linguagens = sorted(
              nome for nome, valores in dados.items()
              if nome not in ("header", "SUM")
              and isinstance(valores, dict)
              and "code" in valores
          )

          if not linguagens:
              raise SystemExit("Nenhuma linguagem encontrada no relatório.")

          def slug(nome):
              nome = unicodedata.normalize("NFKD", nome)
              nome = "".join(
                  c for c in nome if not unicodedata.combining(c)
              )
              nome = re.sub(r"[^a-zA-Z0-9]+", "-", nome)
              return nome.strip("-").lower() or "linguagem"

          def cor_deterministica(nome):
              digest = hashlib.sha256(nome.encode("utf-8")).digest()
              hue = int.from_bytes(digest[:4], "big") / 0xffffffff
              rgb = colorsys.hls_to_rgb(hue, 0.48, 0.68)
              return "#{:02x}{:02x}{:02x}".format(
                  *(round(v * 255) for v in rgb)
              )

          # Preserva as regras existentes e substitui somente
          # o bloco gerenciado por esta automação.
          inicio = "/* AUTO-LINGUAGENS: INICIO */"
          fim = "/* AUTO-LINGUAGENS: FIM */"

          regras = [inicio]

          for nome in linguagens:
              classe = "language-" + slug(nome)
              cor = cor_deterministica(nome)
              nome_css = nome.replace("\\", "\\\\").replace('"', '\\"')

              regras.extend([
                  f'/* {nome} */',
                  f'.{classe} .dot,',
                  f'.dot.{classe},',
                  f'.{classe}.bar-segment,',
                  f'.bar-segment.{classe},',
                  f'[data-language="{nome_css}"] .dot,',
                  f'.dot[data-language="{nome_css}"],',
                  f'[data-language="{nome_css}"] .bar-segment,',
                  f'.bar-segment[data-language="{nome_css}"]' + " {",
                  f"    background-color: {cor};",
                  "}",
                  ""
              ])

          regras.append(fim)
          bloco = "\n".join(regras)

          for arquivo in (DAY, NIGHT):
              texto = arquivo.read_text(encoding="utf-8")

              padrao = re.compile(
                  re.escape(inicio) + r".*?" + re.escape(fim),
                  re.DOTALL
              )

              if padrao.search(texto):
                  texto = padrao.sub(
                      lambda _: bloco,
                      texto
                  )
              else:
                  texto = texto.rstrip() + "\n\n" + bloco + "\n"

              arquivo.write_text(texto, encoding="utf-8")

          # Atualiza as páginas HTML que realmente carregam
          # os CSS de linguagens, sem tocar nas outras páginas.
          marcador = "AUTO-LINGUAGENS-CORES-V1"

          script = r"""
          <script id="AUTO-LINGUAGENS-CORES-V1">
          (() => {
            const slug = name => name.normalize("NFKD")
              .replace(/[\u0300-\u036f]/g, "")
              .replace(/[^a-zA-Z0-9]+/g, "-")
              .replace(/^-+|-+$/g, "")
              .toLowerCase() || "linguagem";

            const aplicar = (elemento, nome) => {
              if (!elemento || !nome) return;
              elemento.classList.add("language-" + slug(nome));
              elemento.dataset.language = nome;
            };

            const legenda = [...document.querySelectorAll(".legend-item")];
            const nomes = [];

            legenda.forEach(item => {
              const strong = item.querySelector("strong");
              if (!strong) return;

              const nome = strong.textContent.trim();
              if (!nome) return;

              nomes.push(nome);
              aplicar(item, nome);
              aplicar(item.querySelector(".dot"), nome);
            });

            document.querySelectorAll("tbody tr").forEach(row => {
              const elemento = row.querySelector(".language");
              if (!elemento) return;

              const nome = elemento.textContent.trim();
              if (!nome) return;

              aplicar(row, nome);
              aplicar(elemento, nome);
              aplicar(elemento.querySelector(".dot"), nome);
            });

            const segmentos = [
              ...document.querySelectorAll(".bar-segment")
            ];

            segmentos.forEach((segmento, indice) => {
              const nome = segmento.dataset.language ||
                segmento.getAttribute("aria-label") ||
                segmento.getAttribute("title");

              if (nome) {
                aplicar(segmento, nome.trim());
              } else if (nomes[indice]) {
                aplicar(segmento, nomes[indice]);
              }
            });
          })();
          </script>
          """

          paginas = []

          for arquivo in ROOT.rglob("*.html"):
              relativo = arquivo.relative_to(ROOT)

              if any(
                  parte in {
                      ".git", "node_modules", "_site",
                      "pacote-linguagens"
                  }
                  for parte in relativo.parts
              ):
                  continue

              texto = arquivo.read_text(encoding="utf-8")

              if not re.search(
                  r'linguagens-(?:day|night)\.css',
                  texto,
                  re.IGNORECASE
              ):
                  continue

              if marcador not in texto:
                  if re.search(r"</body\s*>", texto, re.IGNORECASE):
                      texto = re.sub(
                          r"</body\s*>",
                          lambda _: script + "\n</body>",
                          texto,
                          count=1,
                          flags=re.IGNORECASE
                      )
                  else:
                      texto += "\n" + script + "\n"

                  arquivo.write_text(texto, encoding="utf-8")

              paginas.append(arquivo)

          if not paginas:
              raise SystemExit(
                  "Nenhuma página HTML que carregue "
                  "linguagens-day.css ou linguagens-night.css foi encontrada."
              )

          # Prepara um pacote com os caminhos originais preservados.
          (PACOTE / "relatorios").mkdir(parents=True, exist_ok=True)

          shutil.copy2(
              ROOT / "relatorios/linguagens.json",
              PACOTE / "relatorios/linguagens.json"
          )

          shutil.copy2(
              ROOT / "relatorios/linguagens.md",
              PACOTE / "relatorios/linguagens.md"
          )

          shutil.copy2(DAY, PACOTE / DAY.name)
          shutil.copy2(NIGHT, PACOTE / NIGHT.name)

          for pagina in paginas:
              destino = PACOTE / pagina.relative_to(ROOT)
              destino.parent.mkdir(parents=True, exist_ok=True)
              shutil.copy2(pagina, destino)

          print("Linguagens detectadas:", len(linguagens))
          print("Páginas atualizadas:", len(paginas))
          print("CSS claro e escuro atualizados.")

          for nome in linguagens:
              print(f"  {nome}: #{cor_deterministica(nome)[1:]}")

          PY

      # =======================================================
      # VALIDAR ARQUIVOS
      # =======================================================

      - name: Validar relatórios, CSS e pacote
        shell: bash
        run: |
          set -euo pipefail

          test -s relatorios/linguagens.json
          test -s relatorios/linguagens.md
          test -s linguagens-day.css
          test -s linguagens-night.css

          grep -q 'AUTO-LINGUAGENS: INICIO' linguagens-day.css
          grep -q 'AUTO-LINGUAGENS: FIM' linguagens-day.css
          grep -q 'AUTO-LINGUAGENS: INICIO' linguagens-night.css
          grep -q 'AUTO-LINGUAGENS: FIM' linguagens-night.css

          test -s pacote-linguagens/linguagens-day.css
          test -s pacote-linguagens/linguagens-night.css

          echo "Todos os arquivos foram validados."

      # =======================================================
      # ENVIAR ARTIFACT PARA SALVAR-NA-MAIN
      # =======================================================

      - name: Enviar relatórios, CSS e HTML como artifact
        uses: actions/upload-artifact@v6
        with:
          name: relatorio-linguagens
          path: pacote-linguagens/
          if-no-files-found: error
          retention-days: 7

         

  
  # =========================================================
  # ANÁLISE DE DADOS EM JULIA
  # =========================================================

  bot:
    name: Análise de dados em Julia
    runs-on: ubuntu-24.04
    timeout-minutes: 45

    steps:
      - name: Baixar repositório
        uses: actions/checkout@v6
        with:
          fetch-depth: 1

      - name: Instalar Julia
        uses: julia-actions/setup-julia@v3
        with:
          version: "1"

      - name: Instalar dependências Julia
        run: |
          set -euo pipefail
          julia --project=. -e '
            using Pkg
            Pkg.instantiate()
          '

      - name: Executar análise de dados em Julia
        run: |
          set -euo pipefail
          julia --project=. src/main.jl

      - name: Validar e preparar resultados
        shell: bash
        run: |
          set -euo pipefail

          CSV_ORIGINAL="dados/processados/ligue180-2019-processado.csv"
          PNG_ORIGINAL="relatorios/graficos/ligue180-2019.png"

          test -s "$CSV_ORIGINAL" || {
            echo "::error::CSV processado não foi gerado."
            exit 1
          }

          test -s "$PNG_ORIGINAL" || {
            echo "::error::Gráfico não foi gerado."
            exit 1
          }

          mkdir -p \
            pacote-resultados/dados/processados \
            pacote-resultados/relatorios/graficos

          cp "$CSV_ORIGINAL" \
            pacote-resultados/dados/processados/

          cp "$PNG_ORIGINAL" \
            pacote-resultados/relatorios/graficos/

      - name: Enviar resultados como artifact
        uses: actions/upload-artifact@v6
        with:
          name: resultados-ligue180
          path: pacote-resultados/
          if-no-files-found: error
          retention-days: 1


  # =========================================================
  # BUILD SITE
  # =========================================================

          
  build-site:

    runs-on: ubuntu-24.04

    needs: build

    steps:

      - name: Checkout
        uses: actions/checkout@v6

      - name: Setup Pages
        uses: actions/configure-pages@v6

      - name: Build with Jekyll
        uses: actions/jekyll-build-pages@v1
        with:
          source: ./
          destination: ./_site

      - name: Upload Pages artifact
        uses: actions/upload-pages-artifact@v5
        with:
          path: ./_site

  # =========================================================
  # DEPLOY GITHUB PAGES
  # =========================================================

  deploy:

    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}

    runs-on: ubuntu-24.04

    needs: build-site

    steps:

      - name: Deploy to GitHub Pages
        id: deployment
        uses: actions/deploy-pages@v5
        


  # =========================================================
  # ATUALIZAR TOTAL DE COMMITS
  # =========================================================
  


  atualizar-commits:
    runs-on: ubuntu-24.04
    needs: deploy
    permissions:
      contents: write
      
    steps:
      - name: Baixar todo o histórico
        uses: actions/checkout@v6
        with:
          fetch-depth: 0

      - name: Atualizar total de commits
        run: |
          TOTAL=$(git rev-list --all --count)

          python3 - "$TOTAL" <<'PY'
          from pathlib import Path
          import re
          import sys

          total = sys.argv[1]
          arquivo = Path("README.md")
          texto = arquivo.read_text(encoding="utf-8")

          texto = re.sub(
              r"\*\*Commits totais de toda a história do projeto, \(feitos por mim e pelos workflows do github actions!\) =.*\*\*",
              f"**Commits totais de toda a história do projeto, (feitos por mim e pelos workflows do github actions!) = {total}**",
              texto,
              count=1
          )

          arquivo.write_text(texto, encoding="utf-8")
          PY

      - name: Enviar README atualizado
        uses: actions/upload-artifact@v6
        with:
          name: arquivo-readme
          path: README.md
          if-no-files-found: error

      - name: Publicar README na main
        shell: bash
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          set -euo pipefail

          git config user.name "github-actions[bot]"
          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"

          git add README.md

          if git diff --cached --quiet; then
            echo "README já está atualizado."
            exit 0
          fi

          git commit -m "Atualizar total de commits [skip ci]"

          git fetch origin main

          git rebase origin/main

          git push origin HEAD:main

  # =========================================================
  # SINCRONIZAR README COM DEV.TO
  # =========================================================

  sincronizar-devto:
    name: Sincronizar README com DEV.to
    runs-on: ubuntu-24.04
    needs: atualizar-commits

    steps:
      - name: Baixar código atualizado
        uses: actions/checkout@v6
        with:
          fetch-depth: 1
          ref: main

      - name: Atualizar artigo no DEV.to
        env:
          DEVTO_API_KEY: ${{ secrets.DEVTO_API_KEY }}
        run: |
          python3 - <<'PY'
          import json
          import os
          import urllib.request
          import urllib.error

          with open("README.md", "r", encoding="utf-8") as arquivo:
              markdown = arquivo.read()

          dados = json.dumps({
              "article": {
                  "body_markdown": markdown
              }
          }).encode("utf-8")

          requisicao = urllib.request.Request(
              "https://dev.to/api/articles/4771914",
              data=dados,
              method="PUT",
              headers={
                  "Content-Type": "application/json",
                  "api-key": os.environ["DEVTO_API_KEY"],
                  "User-Agent": "Mulher-Amparada-GitHub-Action"
              }
          )

          try:
              with urllib.request.urlopen(requisicao) as resposta:
                  print(resposta.read().decode("utf-8"))
                  print("DEV.to atualizado com sucesso.")
          except urllib.error.HTTPError as erro:
              print(f"HTTP {erro.code}")
              print(erro.read().decode("utf-8", errors="replace"))
              raise
          PY


  
  # =========================================================
  # GERAR SITEMAP
  # =========================================================

  sitemap:
    name: Gerar Sitemap
    runs-on: ubuntu-24.04
    needs: deploy

    steps:
      - name: Checkout
        uses: actions/checkout@v6
        with:
          fetch-depth: 1
          ref: main

      - name: Gerar Sitemap
        uses: cicirello/generate-sitemap@v1
        with:
          base-url-path: https://mulher-amparada.github.io/mulher-amparada-app/

      - name: Enviar Sitemap gerado
        uses: actions/upload-artifact@v6
        with:
          name: arquivo-sitemap
          path: sitemap.xml
          if-no-files-found: error


  # =========================================================
  # INDEXNOW
  # =========================================================

  indexnow:
    name: IndexNow
    runs-on: ubuntu-24.04
    needs:
      - deploy
      - bot
      - linguagens
      - publicar-divulgacao
      - atualizar-commits

    steps:
      - name: Enviar Sitemap ao IndexNow
        uses: bojieyang/indexnow-action@v3
        with:
          sitemap-location: https://mulher-amparada.github.io/mulher-amparada-app/sitemap.xml
          key: ${{ secrets.INDEXNOW_KEY }}



  # =========================================================
  # LIGHTHOUSE
  # =========================================================

  lighthouse:
    name: Lighthouse
    runs-on: ubuntu-24.04
    needs: deploy

    steps:
      - name: Lighthouse
        uses: treosh/lighthouse-ci-action@v12
        with:
          urls: |
            https://mulher-amparada.github.io/mulher-amparada-app/
          uploadArtifacts: true




  # =========================================================
  # PUBLICAR KIT DE DIVULGAÇÃO
  # =========================================================

  publicar-divulgacao:
    name: Publicar Kit de Divulgação
    runs-on: ubuntu-24.04
    

    steps:
      - name: Baixar repositório
        uses: actions/checkout@v6
        with:
          fetch-depth: 1
          ref: main

      - name: Configurar Node.js
        uses: actions/setup-node@v6
        with:
          node-version: 24

      - name: Criar pacote de divulgação
        working-directory: divulgacao
        run: npm pack

      - name: Publicar pacote no GitHub Release
        uses: softprops/action-gh-release@v3
        with:
          tag_name: divulgacao
          name: Kit de Divulgação
          files: divulgacao/*.tgz
          fail_on_unmatched_files: true
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          
# =========================================================
  # OBSERVATÓRIO — PNGs SEQUENCIAIS
  # =========================================================

  observatorio:
    name: Observatório
    runs-on: ubuntu-24.04
    needs: atualizar-commits

    steps:
      - name: Baixar repositório completo
        uses: actions/checkout@v6
        with:
          fetch-depth: 0

      - name: Instalar dependências do Observatório
        run: |
          set -e
          sudo apt-get update
          sudo apt-get install -y gcc libcairo2-dev

      - name: Compilar observatório
        run: |
          set -e

          gcc -O2 -Wall -Wextra \
            tools/observatorio.c \
            -o observatorio \
            $(pkg-config --cflags --libs cairo) \
            -lm

      - name: Executar observatório
        run: |
          set -e

          chmod +x observatorio

          # Remover somente os arquivos antigos do padrão
          # sequencial e o PNG legado, evitando artefatos obsoletos.
          mkdir -p docs/observatorio
          rm -f docs/observatorio/observatorio.png
          rm -f docs/observatorio/observatorio-[0-9]*.png

          ./observatorio

      - name: Verificar todos os mapas gerados
        run: |
          set -e

          DIR="docs/observatorio"

          COUNT=$(find "$DIR" -maxdepth 1 -type f \
            -regextype posix-extended \
            -regex '.*/observatorio-[0-9]+\.png' \
            | wc -l)

          if [ "$COUNT" -eq 0 ]; then
            echo "ERRO: nenhum PNG sequencial foi gerado."
            exit 1
          fi

          echo "Imagens encontradas: $COUNT"

          find "$DIR" -maxdepth 1 -type f \
            -regextype posix-extended \
            -regex '.*/observatorio-[0-9]+\.png' \
            -print | sort -V

          while IFS= read -r -d '' FILE; do
            if [ ! -s "$FILE" ]; then
              echo "ERRO: arquivo vazio: $FILE"
              exit 1
            fi

            file "$FILE"
          done < <(
            find "$DIR" -maxdepth 1 -type f \
              -regextype posix-extended \
              -regex '.*/observatorio-[0-9]+\.png' \
              -print0
          )

          echo "Todos os PNGs sequenciais foram verificados."

      - name: Disponibilizar todos os mapas
        uses: actions/upload-artifact@v6
        with:
          name: observatorio
          path: docs/observatorio/observatorio-*.png
          if-no-files-found: error
          retention-days: 30

  # =========================================================
  # ATUALIZAR FOOTER COM README
  # =========================================================

  atualizar-footer:
    name: Atualizar Footer com README
    runs-on: ubuntu-24.04
    needs: atualizar-commits

    steps:
      - name: Baixar repositório
        uses: actions/checkout@v6
        with:
          fetch-depth: 1
          ref: main

      - name: Instalar Python
        uses: actions/setup-python@v6
        with:
          python-version: "3.x"

      - name: Instalar dependência Python
        run: pip install Markdown

      - name: Atualizar footer com README
        run: python3 scripts/atualizar_footer.py

      - name: Enviar footer gerado
        uses: actions/upload-artifact@v6
        with:
          name: arquivo-footer
          path: index.html
          if-no-files-found: error


  # =========================================================
  # ATUALIZAR RELATÓRIO DE COMMITS
  # =========================================================

  atualizar-relatorio-commits:
    name: Atualizar relatório de commits
    runs-on: ubuntu-24.04
    needs: atualizar-commits

    steps:
      - name: Baixar repositório
        uses: actions/checkout@v6
        with:
          fetch-depth: 0
          ref: main

      - name: Instalar Ruby
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: "3.4"

      - name: Gerar relatório de commits
        run: ruby scripts/commits.rb

      - name: Enviar relatório gerado
        uses: actions/upload-artifact@v6
        with:
          name: arquivos-relatorio-commits
          path: commits/
          if-no-files-found: error


  

# =========================================================
  # DOCUMENTAR WORKFLOW YAML
  # =========================================================

  pesquisar-projeto:
    name: Documentar workflow YAML
    runs-on: ubuntu-24.04

    steps:
      - name: Baixar repositório
        uses: actions/checkout@v6
        with:
          fetch-depth: 1
          ref: main

      - name: Instalar PHP
        run: |
          sudo apt-get update
          sudo apt-get install -y php-cli

      - name: Gerar documentação do workflow
        run: |
          set -e

          mkdir -p pesquisas

          php -l scripts/pesquisar_projeto.php
          php scripts/pesquisar_projeto.php

          if [ -f ".github/workflows/workflow.md" ]; then
            mv .github/workflows/workflow.md pesquisas/workflow.md
          fi

          test -s pesquisas/workflow.md

          echo "Documentação gerada:"
          ls -lh pesquisas/workflow.md

      - name: Enviar documentação do workflow como artifact
        uses: actions/upload-artifact@v6
        with:
          name: arquivos-pesquisas
          path: pesquisas/workflow.md
          if-no-files-found: error
          retention-days: 30
          

          

  
# =========================================================
  # SALVAR NA BRANCH MAIN
  # =========================================================

  salvar-na-main:
    name: Salvar artifacts na main pelo Mulher Amparada Bot
    runs-on: ubuntu-24.04
    needs:
      - build
      - linguagens
      - bot
      - atualizar-commits
      - sitemap
      - atualizar-footer
      - atualizar-relatorio-commits
      - pesquisar-projeto
      - observatorio
    if: >-
      ${{
        !cancelled() &&
        needs.build.result == 'success' &&
        needs.linguagens.result == 'success' &&
        needs.bot.result == 'success' &&
        
        needs.atualizar-commits.result == 'success' &&
        needs.sitemap.result == 'success' &&
        needs.atualizar-footer.result == 'success' &&
        needs.atualizar-relatorio-commits.result == 'success' &&
        needs.pesquisar-projeto.result == 'success' &&
        needs.observatorio.result == 'success'
      }}

    permissions:
      contents: write
      actions: read

    steps:
      - name: Baixar código-fonte da main
        uses: actions/checkout@v6
        with:
          ref: main
          fetch-depth: 0

      - name: Baixar todos os artifacts desta execução
        uses: actions/download-artifact@v8
        with:
          path: ${{ runner.temp }}/mulher-amparada-artifacts
          merge-multiple: false

      - name: Consultar workflow atual
        shell: bash
        run: |
          set -euo pipefail

          WORKFLOW=".github/workflows/automations.yml"

          if [[ ! -f "$WORKFLOW" ]]; then
            echo "::error::Workflow não encontrado: $WORKFLOW"
            exit 1
          fi

          echo "Workflow consultado:"
          wc -l "$WORKFLOW"

      - name: Compilar o publicador C++
        shell: bash
        run: |
          set -euo pipefail

          sudo apt-get update
          sudo apt-get install -y \
            g++ \
            libcurl4-openssl-dev \
            libssl-dev \
            pkg-config \
            unzip

          g++ -std=c++17 -g -O1 \
            -fsanitize=address,undefined \
            -fno-omit-frame-pointer \
            tools/automation_bot.cpp \
            -o "$RUNNER_TEMP/automation_bot" \
            $(pkg-config --cflags --libs libcurl openssl)

      - name: Publicar na main pelo GitHub App
        shell: bash
        
        env:
          GH_TOKEN: ${{ github.token }}
          MULHER_AMPARADA_APP_ID: ${{ secrets.MULHER_AMPARADA_APP_ID }}
          MULHER_AMPARADA_INSTALLATION_ID: ${{ secrets.MULHER_AMPARADA_INSTALLATION_ID }}
          MULHER_AMPARADA_PRIVATE_KEY: ${{ secrets.MULHER_AMPARADA_PRIVATE_KEY }}
          GITHUB_REPOSITORY: ${{ github.repository }}
          GITHUB_RUN_ID: ${{ github.run_id }}
          GITHUB_WORKSPACE: ${{ github.workspace }}
          MULHER_AMPARADA_ARTIFACT_MAP: "arquivo-readme:.;arquivo-sitemap:.;arquivo-footer:.;arquivos-relatorio-commits:commits;arquivos-pesquisas:pesquisas;observatorio:docs/observatorio;resultados-ligue180:.;relatorio-linguagens:."
        run: |
          set -euo pipefail
          "$RUNNER_TEMP/automation_bot"
````
