# Mulher Amparada

Um projeto totalmente gratuito e livre de anúncios, projetado por um menino autista nível 1 de 15 anos!, usando o apoio do chatgpt, sem curso formal!

e eu programei todo esse projeto no A16 5g da samsung, e nas primeiras versoes, onde nem tinha os recursos, ja programei ele num app de A-IDE, num A05, e eu ja perdi vários projetos porque o celular nao aguentava, matava o projeto porque matou o processo de compilação!, e uma vez eu fiz o projeto do mulher amparada e eu mesmo fiz o app do mulher amparada (primeiro eu refiz porque o family link apagou a pasta segura samsung, depois na 2 vez que perdi portei tudo do apk compilado para descompilado, e depois perdi denovo mas ai eu ja tinha o código-fonte!)

E vale lembrar que antes todas as páginas eram html com webview, agora não são mais, só a função de navegador usa webview sem html, tudo é compose (no primeiro dia foram 10h de trabalho no segundo foram 7h se trabalho!)

**Commits totais de toda a história do projeto, (feitos por mim e pelos workflows do github actions!) = 4857**

E o projeto é = Open-source! (aderido no dia 02/10/2026)

# 👾Nossas contribuições!:

*Gerado pelo workflow do projeto junto com uma action do marketplace do github!*

![Snake das contribuições](Código-fonte%20do%20app%20%3D%20Mulher%20Amparada/snake.gif)

![Snake das contribuições](Código-fonte%20do%20app%20%3D%20Mulher%20Amparada/snake.svg)

# ⚠️MURAL DE AVISOS:

### Sobre como o projeto foi estruturado:

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

```
⚙️ Automações do Mulher Amparada

O projeto Mulher Amparada Pela Liberdade Feminina utiliza um workflow automatizado do GitHub Actions para compilar o aplicativo Android, assinar o APK, publicar atualizações, construir e implantar o site, gerar arquivos auxiliares, documentar o próprio workflow, sincronizar publicações externas e produzir relatórios.

O workflow está definido em ".github/workflows/automations.yml" e utiliza jobs independentes, dependências entre etapas, artifacts, GitHub Releases, GitHub Pages e scripts em diferentes linguagens de programação.

🚀 Como o workflow é iniciado

O workflow é executado automaticamente quando ocorre um "push" em qualquer branch do repositório. Também pode ser iniciado manualmente pela opção "workflow_dispatch" na interface do GitHub Actions.

A configuração de concorrência utiliza o nome do workflow e a referência da execução para separar os grupos. Execuções automáticas anteriores do mesmo grupo podem ser canceladas quando uma nova execução é iniciada, enquanto execuções manuais não ativam esse cancelamento pela configuração apresentada.

As permissões globais concedidas ao workflow são:

- "contents: write" — permite operações de escrita no conteúdo do repositório.
- "pages: write" — permite operações necessárias à publicação no GitHub Pages.
- "id-token: write" — permite solicitar tokens de identidade OIDC.
- "actions: read" — permite leitura de informações de execuções do GitHub Actions.

Jobs que declaram permissões próprias podem restringir as permissões disponíveis para suas operações.

🏗️ 1. Compilar APK Kotlin

O job "build", denominado Compilar APK Kotlin, é executado em "ubuntu-24.04". Ele prepara o ambiente de compilação, verifica a estrutura do projeto, compila o aplicativo, assina o APK e atualiza os arquivos de distribuição.

Preparação do ambiente

O job define as seguintes variáveis:

- "App1_DIR": diretório "Código-fonte do app = Mulher Amparada".
- "RELEASE_TAG": identificador "app" da GitHub Release.
- "RELEASE_APK_NAME": nome "app-debug-assinado.apk" utilizado para distribuir o APK assinado.

As etapas iniciais:

1. Baixam o repositório com "actions/checkout@v6", utilizando histórico superficial.
2. Verificam se o diretório do aplicativo existe.
3. Verificam se o arquivo "gradlew" está presente.
4. Configuram o Java 17 por meio de "actions/setup-java@v6", utilizando a distribuição Temurin e o cache do Gradle.
5. Verificam a presença de uma plataforma Android SDK 37 em "ANDROID_HOME/platforms".
6. Concedem permissão de execução ao Gradle Wrapper.

Se o diretório, o Gradle Wrapper ou a plataforma Android SDK 37 esperada não forem encontrados, a execução é interrompida com uma mensagem de erro.

Compilação com Gradle

A compilação utiliza:

"./gradlew assembleDebug --stacktrace"

A saída do Gradle é exibida no log e gravada em "gradle-build.log". O script também verifica o código de saída do processo.

Se a compilação falhar, o resumo da execução recebe uma seção em Markdown contendo:

- Identificação da etapa com falha.
- Descrição do problema.
- As últimas 100 linhas do log do Gradle.
- O código de saída retornado pelo processo.

A falha é propagada para que o job seja marcado como malsucedido.

Localização do APK

Após a compilação, o workflow procura arquivos ".apk" em "app/build/outputs/apk/debug". Se nenhum APK for encontrado, a etapa falha. Caso contrário, o caminho encontrado é armazenado em uma saída para utilização nas etapas seguintes.

Assinatura e verificação do APK

A assinatura utiliza o utilitário "apksigner", encontrado no Android SDK Build Tools, e um keystore fornecido por meio dos seguintes GitHub Secrets:

- "KEYSTORE_BASE64"
- "KEYSTORE_PASSWORD"
- "KEY_ALIAS"
- "KEY_PASSWORD"

O processo:

1. Verifica se todos os secrets necessários estão configurados.
2. Decodifica o keystore Base64 para o arquivo temporário "mulher-amparada.jks".
3. Confere se o APK de entrada existe.
4. Localiza o executável "apksigner".
5. Assina o APK utilizando o keystore e as credenciais configuradas.
6. Substitui o arquivo de entrada pela versão assinada.
7. Executa "apksigner verify --verbose" para verificar a assinatura.
8. Remove o arquivo temporário do keystore após a execução bem-sucedida das operações previstas.

As credenciais não são escritas diretamente no código-fonte: são fornecidas por meio dos secrets do repositório.

Artifact do APK

O arquivo assinado é copiado para a raiz do workspace com o nome "app-debug-assinado.apk".

Em seguida, "actions/upload-artifact@v6" publica o arquivo como artifact da execução, com o nome:

"APK-Kotlin-Assinado"

A opção "if-no-files-found: error" faz a etapa falhar se o arquivo esperado não existir.

Esse artifact é uma cópia associada à execução do workflow, separada do asset publicado na GitHub Release.

Cálculo do tamanho real

O tamanho do APK é calculado a partir do arquivo assinado, usando "stat -c%s" para obter o tamanho em bytes.

O valor em megabytes é calculado dividindo os bytes por (1024^2), com duas casas decimais. O resultado é disponibilizado como saída para outras etapas.

São registrados:

- Caminho do arquivo.
- Tamanho em bytes.
- Tamanho formatado em MB.

Cálculo do SHA-256

O hash é calculado com "sha256sum" sobre o APK assinado.

O SHA-256 é armazenado como saída do job e posteriormente utilizado nas notas da Release, na página de download e no resumo da compilação. Isso permite comparar o hash publicado com o hash calculado sobre um arquivo obtido pelo usuário.

Atualização do APK na branch "main"

O workflow configura a identidade de commit do "github-actions[bot]", copia o APK para:

"Código-fonte do app = Mulher Amparada/app-release.apk"

Depois, busca a referência remota "main", sincroniza o checkout com "origin/main", copia novamente o APK gerado, adiciona o arquivo ao Git e verifica se existem alterações.

Se o conteúdo for diferente, cria um commit com a mensagem:

"Atualizar APK assinado [skip ci]"

e tenta enviá-lo para "main".

Se o arquivo já estiver atualizado, nenhum commit adicional é criado por essa etapa.

Atualização da GitHub Release

A Release identificada pela tag "app" precisa existir previamente. O workflow verifica sua existência usando a GitHub CLI.

Depois:

1. Consulta os assets associados à Release.
2. Procura um asset com o nome "app-debug-assinado.apk".
3. Exclui o asset antigo, se encontrado.
4. Envia o APK recém-assinado para a Release.
5. Recupera as notas atuais da Release.
6. Atualiza a linha "SHA-256 gerado nesta compilação:" ou acrescenta essa informação caso ela ainda não exista.
7. Salva as notas atualizadas na Release.

A substituição do asset é realizada por exclusão seguida de envio do novo arquivo. Portanto, não constitui uma troca atômica.

Atualização de "download.html"

O script Python integrado ao workflow atualiza automaticamente informações da página "download.html".

O endereço de download é montado a partir do repositório, da tag "app" e do nome do asset.

O script procura e substitui, por meio de expressões regulares:

- O atributo "href" do primeiro link com a classe "download".
- O tamanho do APK no elemento com a classe "summary-value".
- O tamanho aproximado no elemento com a classe "detail-value".
- O valor exibido no campo identificado como "Código SHA-256".

Se algum dos campos esperados não for encontrado, o script encerra com uma mensagem de erro, evitando declarar a atualização como concluída.

O tamanho e o hash exibidos correspondem aos valores calculados sobre o APK assinado naquela execução.

Salvamento de "download.html"

Após a atualização da página, o workflow configura a identidade do bot, adiciona "download.html" ao Git e verifica se existem alterações.

Se houver mudanças, cria um commit com a mensagem:

"Atualizar link, tamanho e SHA-256 do APK [skip ci]"

e tenta enviá-lo para "main".

Resumo da compilação

A última etapa do job "build" adiciona informações ao resumo da execução do GitHub Actions, incluindo:

- Identificação da Release.
- Branch de referência.
- Nome do APK enviado.
- Tamanho real em MB.
- Tamanho em bytes.
- SHA-256 completo do arquivo.

Esse resumo facilita a consulta dos dados da compilação sem precisar localizar cada valor nos logs individuais.

🌐 2. Construir o site com Jekyll

O job "build-site" depende do sucesso de "build".

Ele utiliza o ambiente "ubuntu-24.04" e executa as seguintes operações:

1. Baixa o repositório.
2. Configura o GitHub Pages por meio de "actions/configure-pages@v6".
3. Constrói o site com "actions/jekyll-build-pages@v1".
4. Define "./" como diretório de origem e "./_site" como destino da construção.
5. Publica o diretório gerado como artifact do GitHub Pages por meio de "actions/upload-pages-artifact@v5".

O resultado é um pacote estático preparado para implantação.

🚀 3. Implantar o site no GitHub Pages

O job "deploy" depende de "build-site".

Ele utiliza "actions/deploy-pages@v5" para implantar o artifact do site no ambiente "github-pages".

A etapa de implantação recebe o identificador "deployment", e o endereço retornado é utilizado como URL do ambiente do job.

A implantação disponibiliza a versão construída do site no GitHub Pages.

🔢 4. Atualizar o total histórico de commits

O job "atualizar-commits" depende da conclusão de "deploy".

Ele baixa o repositório com "fetch-depth: 0", permitindo consultar o histórico Git disponível em todas as referências buscadas.

O comando "git rev-list --all --count" calcula a quantidade de commits alcançáveis pelas referências locais existentes naquele checkout.

Um script Python recebe esse total e utiliza uma expressão regular para localizar no "README.md" a linha que começa com:

"Commits totais de toda a história do projeto"

A linha é atualizada com o valor calculado, mantendo o formato textual esperado pelo README.

O arquivo atualizado é enviado como artifact chamado "arquivo-readme". Ele será utilizado posteriormente pelo job de centralização dos arquivos.

O valor calculado depende do histórico e das referências disponíveis no checkout; não deve ser interpretado como uma contagem universal de todos os commits que possam existir em referências remotas não buscadas.

🐍 5. Sincronizar o README com o DEV.to

O job "sincronizar-devto" depende de "salvar-na-main".

Ele baixa a versão do repositório correspondente à branch "main" e lê o conteúdo completo do "README.md".

Um script Python utiliza a biblioteca padrão para:

1. Ler o README em UTF-8.
2. Criar um payload JSON com o conteúdo em "article.body_markdown".
3. Enviar uma requisição HTTP "PUT" para a API do artigo "4771914" do DEV.to.
4. Autenticar utilizando o secret "DEVTO_API_KEY".
5. Definir o cabeçalho "User-Agent" como "Mulher-Amparada-GitHub-Action".
6. Exibir a resposta da API quando a requisição for aceita.
7. Exibir o código HTTP e a resposta retornada quando ocorrer um erro HTTP.

O objetivo é manter o corpo do artigo do DEV.to sincronizado com o README do repositório.

O job não cria um artigo novo: a requisição está direcionada ao identificador de artigo configurado no script.

📝 6. Sincronizar o README com o Paper.wf

O job "sincronizar-paperwf" também depende de "salvar-na-main".

Ele baixa o README atualizado e utiliza um script Python para autenticar na API do Paper.wf e atualizar uma publicação existente.

A configuração utiliza:

- Base da API: "https://paper.wf"
- Coleção: "mulheramparada"
- Slug: "projeto-mulher-amparada"
- Título enviado: "Projeto Mulher Amparada"
- Secrets: "PAPERWF_USERNAME" e "PAPERWF_PASSWORD"

O processo:

1. Lê o README em UTF-8.
2. Envia as credenciais para a rota de autenticação.
3. Procura um token de acesso em diferentes formatos possíveis da resposta.
4. Consulta a publicação configurada na coleção.
5. Identifica o ID da publicação, tratando respostas que contenham objetos ou listas.
6. Monta o payload com o título e o conteúdo integral do README.
7. Envia uma requisição "POST" para atualizar a publicação identificada.
8. Exibe uma mensagem de sucesso se as operações terminarem sem exceção.

O script também apresenta respostas HTTP de erro e interrompe a execução quando não consegue autenticar, identificar o token ou localizar o ID da publicação.

🗺️ 7. Gerar o sitemap

O job "sitemap" depende de "snake".

Ele baixa o repositório na branch "main" e utiliza a action "cicirello/generate-sitemap@v1" para gerar um sitemap XML.

A URL-base configurada é:

"https://mulher-amparada.github.io/mulher-amparada-app/"

O arquivo gerado, "sitemap.xml", é publicado como artifact com o nome "arquivo-sitemap".

Posteriormente, o job "salvar-na-main" baixa esse artifact e copia o arquivo para a raiz do repositório.

O sitemap serve como índice de URLs para mecanismos de busca, mas sua geração e publicação não garantem que as páginas serão indexadas.

🔎 8. IndexNow

O job "indexnow" depende de "deploy".

Ele utiliza "bojieyang/indexnow-action@v3" para enviar o endereço do sitemap configurado:

"https://mulher-amparada.github.io/mulher-amparada-app/sitemap.xml"

A chave é fornecida pelo secret "INDEXNOW_KEY".

A finalidade é comunicar URLs aos mecanismos de busca que participam do protocolo IndexNow. O envio não garante rastreamento, indexação ou posicionamento nos resultados de pesquisa.

🔗 9. Verificar links quebrados

O job "broken-links" depende de "deploy".

Ele utiliza "ScholliYT/Broken-Links-Crawler-Action@v3" para rastrear links do site publicado.

A configuração inclui:

- URL inicial: "https://mulher-amparada.github.io/mulher-amparada-app/"
- Prefixo de URLs a incluir: o mesmo endereço-base.
- "resolve_before_filtering: "true"" para resolver URLs antes da filtragem.
- "verbose: "true"" para ampliar a saída de diagnóstico.
- Tempo máximo de tentativa: 30 segundos.
- Até cinco tentativas.
- Profundidade máxima configurada em "-1".

O objetivo é encontrar links que não possam ser acessados corretamente durante o rastreamento. A abrangência real depende do comportamento da action, dos links descobertos e das respostas dos servidores.

💡 10. Auditar o site com Lighthouse

O job "lighthouse" depende de "deploy".

Ele utiliza "treosh/lighthouse-ci-action@v12" para executar uma auditoria na página publicada.

A URL configurada é:

"https://mulher-amparada.github.io/mulher-amparada-app/"

A opção "uploadArtifacts: true" solicita o envio dos artifacts produzidos pela action.

O Lighthouse pode avaliar aspectos como desempenho, acessibilidade, boas práticas e SEO, conforme as categorias executadas pela configuração utilizada.

A auditoria fornece informações para identificar oportunidades de melhoria, mas não significa que o site recebeu automaticamente uma pontuação específica ou que todos os problemas foram corrigidos.

🐍 11. Gerar a Snake de contribuições

O job "snake" depende de "atualizar-commits".

Ele baixa o repositório com o histórico completo disponível e utiliza "Platane/snk@v3" para gerar representações animadas da grade de contribuições do GitHub.

O nome do usuário é obtido de "github.repository_owner".

Os arquivos gerados são:

- "dist/github-contribution-grid-snake.svg"
- "dist/github-contribution-grid-snake.gif"

Uma etapa posterior copia os arquivos para "arquivos-gerados/snake/", renomeando-os para:

- "snake.svg"
- "snake.gif"

Esses arquivos são publicados como artifact com o nome "arquivos-snake".

O job "salvar-na-main" baixa os arquivos e os copia para o diretório "Código-fonte do app = Mulher Amparada", onde são adicionados ao commit de centralização.

📦 12. Publicar o kit de divulgação

O job "publicar-divulgacao" depende de "lighthouse".

Ele baixa o repositório na branch "main", configura o Node.js 24 por meio de "actions/setup-node@v6" e executa "npm pack" dentro do diretório "divulgacao".

O comando gera um pacote compactado no formato ".tgz", conforme a configuração do pacote npm.

Em seguida, "softprops/action-gh-release@v3" publica o pacote na GitHub Release configurada com:

- Tag: "divulgacao"
- Nome: "Kit de Divulgação"
- Arquivos: "divulgacao/*.tgz"

A opção "fail_on_unmatched_files: true" faz a publicação falhar se nenhum arquivo corresponder ao padrão esperado.

O token "GITHUB_TOKEN" é disponibilizado para a etapa de publicação. A existência de uma Release com a tag correspondente e as permissões necessárias deve ser compatível com a configuração do repositório e da action.

🦶 13. Atualizar o footer com o README

O job "atualizar-footer" depende de "atualizar-commits".

Ele baixa o repositório na branch "main", instala o Python 3.x por meio de "actions/setup-python@v6" e instala a dependência Python "Markdown".

Depois, executa:

"python3 scripts/atualizar_footer.py"

O script é responsável por gerar ou atualizar o conteúdo do "index.html" a partir do README, conforme a lógica implementada em "scripts/atualizar_footer.py".

O arquivo "index.html" resultante é enviado como artifact com o nome "arquivo-footer".

Posteriormente, o job "salvar-na-main" baixa esse artifact e copia o "index.html" gerado para a raiz do repositório.

📚 14. Gerar o relatório histórico de commits

O job "atualizar-relatorio-commits" depende de "atualizar-commits".

Ele baixa o repositório com o histórico Git completo disponível, configura o Ruby 3.4 utilizando "ruby/setup-ruby@v1" e executa:

"ruby scripts/commits.rb"

O script é responsável por processar o histórico de commits e gerar os arquivos de relatório dentro do diretório "commits/", de acordo com sua implementação.

Os arquivos produzidos são enviados como artifact chamado "arquivos-relatorio-commits".

A configuração exige que o diretório contenha arquivos correspondentes ao caminho informado; caso contrário, o upload falha.

O job "salvar-na-main" baixa o artifact e incorpora os arquivos gerados ao diretório "commits/" do repositório.

📖 15. Documentar o workflow YAML

O job "pesquisar-projeto", identificado como Documentar workflow YAML, gera documentação técnica a partir do script PHP "scripts/pesquisar_projeto.php".

Ele baixa o repositório na branch "main", instala o PHP CLI e executa as seguintes verificações e operações:

1. "php -l scripts/pesquisar_projeto.php" verifica a sintaxe do script PHP.
2. "php scripts/pesquisar_projeto.php" executa o gerador de documentação.
3. "test -s .github/workflows/workflow.md" verifica se o arquivo de saída existe e não está vazio.
4. "ls -lh .github/workflows/workflow.md" exibe informações do arquivo gerado.

O arquivo resultante é enviado como artifact com o nome "arquivos-pesquisas", com retenção configurada para 30 dias.

Observação: o destino verificado pelo job é ".github/workflows/workflow.md", enquanto o job de centralização copia os arquivos baixados para o diretório "pesquisas/". Portanto, o arquivo será armazenado no destino pretendido somente se o conteúdo do artifact e a lógica de cópia forem compatíveis com essa estrutura.

🗃️ 16. Centralizar e salvar os arquivos na branch "main"

O job "salvar-na-main" reúne os resultados de vários jobs anteriores e tenta registrá-los no repositório.

Ele depende de:

- "atualizar-commits"
- "snake"
- "sitemap"
- "atualizar-footer"
- "atualizar-relatorio-commits"
- "pesquisar-projeto"

Download dos artifacts

O job baixa a branch "main" com histórico completo e recupera os artifacts:

- "arquivo-readme"
- "arquivos-snake"
- "arquivo-sitemap"
- "arquivo-footer"
- "arquivos-relatorio-commits"
- "arquivos-pesquisas"

Os arquivos são armazenados inicialmente em subdiretórios de "artefatos/".

Organização dos arquivos

Uma etapa de cópia reúne os resultados nos destinos do repositório:

Artifact| Destino
"arquivo-readme"| "README.md"
"arquivos-snake"| "Código-fonte do app = Mulher Amparada/snake.svg" e "snake.gif"
"arquivo-sitemap"| "sitemap.xml"
"arquivo-footer"| "index.html"
"arquivos-relatorio-commits"| Diretório "commits/"
"arquivos-pesquisas"| Diretório "pesquisas/"

Os diretórios "commits/" e "pesquisas/" são criados quando necessário, e os arquivos dos artifacts são copiados preservando sua estrutura interna.

Commit e envio

O job configura a identidade de commit do bot, adiciona os arquivos gerados ao Git e verifica se existem alterações.

Se não houver diferenças preparadas para commit, a etapa encerra sem criar um commit.

Se houver mudanças, cria um commit com a mensagem:

"Atualizar arquivos gerados [skip ci]"

Depois, busca a versão remota de "main", tenta reaplicar o commit sobre a versão atualizada por meio de "git rebase origin/main" e envia o resultado para "main".

A intenção é centralizar os resultados de geração em um commit, reduzindo a necessidade de vários commits separados para cada artifact.

O sucesso depende de todos os artifacts exigidos estarem disponíveis, de os caminhos coincidirem com a estrutura esperada e de o rebase e o push serem concluídos sem conflitos.

🛠️ 17. Relatório de erros em português

O job "relatorio-erros" utiliza "ubuntu-24.04" e declara:

"if: ${{ always() }}"

Essa condição solicita que o job seja avaliado mesmo quando as dependências anteriores falharem ou forem ignoradas, respeitando as demais condições de execução do GitHub Actions.

O job declara dependências em "sincronizar-paperwf" e "sincronizar-devto", para acompanhar o resultado das duas sincronizações externas.

Preparação do relatório

O job baixa o código e cria inicialmente "relatorio-erros.md", contendo um título e uma mensagem informando que a coleta ainda não foi concluída.

Em seguida, instala a toolchain estável do Rust por meio de "dtolnay/rust-toolchain@stable".

Execução do coletor Rust

O script é executado com:

"cargo run --release --manifest-path "$GITHUB_WORKSPACE/tools/relatorio-erros/Cargo.toml""

A execução utiliza as variáveis:

- "GITHUB_TOKEN", fornecida pelo token da execução.
- "GITHUB_REPOSITORY", com o identificador do repositório.
- "GITHUB_RUN_ID", com o identificador da execução atual.

A saída é encaminhada ao terminal e gravada em "erro-rust.log".

O shell captura o código de saída do processo Cargo por meio de "PIPESTATUS[0]".

Se "relatorio-erros.md" estiver ausente ou vazio depois da execução, o job gera um relatório alternativo contendo o código de saída e as últimas 100 linhas do log técnico.

A etapa está marcada com "continue-on-error: true", permitindo que o job prossiga para a publicação do artifact mesmo quando a coleta falhar. O código de saída é preservado na etapa, e o resultado final deve ser interpretado considerando o tratamento de falhas da etapa e o comportamento do job.

Publicação do relatório

A etapa de upload utiliza "if: ${{ always() }}" e publica:

- "relatorio-erros.md"
- "erro-rust.log"

O artifact recebe o nome "relatorio-erros-portugues" e tem retenção configurada para 30 dias.

Limite da implementação apresentada: o job executa o programa Rust e fornece um token de acesso, mas a coleta efetiva dos logs de outros jobs depende do código implementado em "tools/relatorio-erros/". O YAML, isoladamente, não comprova que todos os logs do workflow são baixados ou analisados.

🔐 18. Secrets utilizados

O workflow utiliza credenciais armazenadas no sistema de secrets do GitHub Actions.

Secret| Finalidade
"KEYSTORE_BASE64"| Keystore codificado em Base64 para assinar o APK
"KEYSTORE_PASSWORD"| Senha do keystore
"KEY_ALIAS"| Alias da chave de assinatura
"KEY_PASSWORD"| Senha da chave
"DEVTO_API_KEY"| Autenticação na API do DEV.to
"PAPERWF_USERNAME"| Identificação da conta no Paper.wf
"PAPERWF_PASSWORD"| Senha da conta no Paper.wf
"INDEXNOW_KEY"| Chave utilizada na integração com IndexNow

O workflow também utiliza o "GITHUB_TOKEN" ou "${{ github.token }}" para operações autorizadas no GitHub.

Os secrets devem ser configurados nas definições do repositório antes da execução dos jobs que dependem deles. O acesso efetivo depende das permissões concedidas ao token e às actions utilizadas.

🧰 19. Tecnologias e ferramentas utilizadas

O workflow combina diferentes tecnologias para automatizar tarefas específicas:

Tecnologia ou ferramenta| Utilização
GitHub Actions| Orquestração dos jobs e das etapas
Bash| Verificações, cópias, operações Git e comandos do sistema
Java 17 / Temurin| Ambiente de execução do Gradle
Kotlin e Gradle| Compilação do aplicativo Android
Android SDK 37| Verificação da plataforma Android exigida
"apksigner"| Assinatura e verificação do APK
Git e GitHub CLI| Versionamento, Releases e operações no repositório
Python 3| Atualização do README, da página de download e do footer
Biblioteca Python "Markdown"| Dependência utilizada na geração do footer
Ruby 3.4| Geração do relatório de commits
PHP CLI| Geração da documentação do workflow
Rust| Execução do coletor do relatório de erros
Node.js 24 / npm| Empacotamento do kit de divulgação
Jekyll| Construção do site estático
GitHub Pages| Publicação do site
DEV.to API| Sincronização do artigo
Paper.wf API| Sincronização da publicação
IndexNow| Notificação do sitemap
Broken Links Crawler| Rastreamento de links do site
Lighthouse CI| Auditoria automatizada do site
Platane/snk| Geração da Snake de contribuições
GitHub Artifacts| Transporte dos arquivos gerados entre jobs

🔄 20. Fluxo geral de dependências

O workflow é dividido em etapas encadeadas. O fluxo principal do aplicativo e do site é:

1. "build" compila e assina o APK.
2. "build-site" constrói o site após a conclusão de "build".
3. "deploy" publica o site.
4. "atualizar-commits" atualiza o total de commits após a implantação.
5. "snake", "atualizar-footer" e "atualizar-relatorio-commits" geram arquivos auxiliares a partir de "atualizar-commits".
6. "sitemap" depende de "snake".
7. "pesquisar-projeto" gera a documentação do workflow.
8. "salvar-na-main" reúne os artifacts exigidos e tenta salvá-los na branch "main".
9. "sincronizar-devto" e "sincronizar-paperwf" dependem de "salvar-na-main".
10. "relatorio-erros" acompanha os resultados das duas sincronizações externas, com a condição "always()".

Em paralelo à cadeia de geração e sincronização, "indexnow", "broken-links" e "lighthouse" dependem de "deploy". O job "publicar-divulgacao" depende de "lighthouse".

Essas relações representam as dependências declaradas em "needs". Elas não significam que todos os jobs sejam executados em uma única sequência linear, pois jobs sem dependências diretas entre si podem ser executados em paralelo.

📦 21. Artifacts produzidos

O workflow utiliza artifacts para transportar arquivos entre jobs sem precisar gerar todos os resultados novamente em cada etapa.

Nome do artifact| Conteúdo
"APK-Kotlin-Assinado"| APK assinado para download na execução
"arquivo-readme"| README atualizado com o total de commits
"arquivos-snake"| Snake em SVG e GIF
"arquivo-sitemap"| Sitemap XML
"arquivo-footer"| "index.html" gerado
"arquivos-relatorio-commits"| Arquivos do relatório histórico de commits
"arquivos-pesquisas"| Documentação do workflow
"relatorio-erros-portugues"| Relatório em Markdown e log do coletor Rust

O artifact "APK-Kotlin-Assinado" é independente do asset da GitHub Release. Da mesma forma, os artifacts intermediários de geração não são publicados automaticamente no site: sua persistência no repositório depende das etapas que os baixam e copiam.

⚠️ 22. Observações operacionais

- A Release com tag "app" precisa existir antes da execução do job de compilação.
- As credenciais de assinatura precisam corresponder ao keystore esperado.
- A assinatura só é considerada concluída após a execução da verificação do "apksigner".
- Os scripts de atualização dependem dos elementos e caminhos esperados nos arquivos do projeto.
- A sincronização com serviços externos depende das APIs, das credenciais e das respostas recebidas.
- A geração do sitemap não garante indexação nos mecanismos de busca.
- As auditorias de links e Lighthouse dependem da disponibilidade do site implantado.
- A centralização dos arquivos depende dos artifacts exigidos e de seus caminhos internos.
- Os commits feitos pelos workflows podem atualizar o histórico utilizado pelo contador de commits.
- O uso de "[skip ci]" nas mensagens de commit solicita que os mecanismos compatíveis ignorem a execução automática associada àquele commit; isso não substitui as regras de proteção do repositório.
- O job de relatório de erros precisa ter sua implementação Rust configurada para consultar os logs que pretende analisar.

🎯 Objetivo geral

O workflow Automações do Mulher Amparada concentra em uma única configuração a compilação e assinatura do aplicativo Android, a atualização dos arquivos de distribuição, a publicação do site, a manutenção de informações do README, a geração de documentação e relatórios, a criação de recursos visuais de contribuições e a sincronização de conteúdo com plataformas externas.

A automação reduz tarefas manuais repetitivas e organiza os resultados por meio de dependências entre jobs, scripts especializados, artifacts e commits automatizados. Cada etapa mantém uma responsabilidade específica dentro do processo de manutenção e distribuição do projeto.
```

E temos esse dependabot (.github/dependabot yml):
```
version: 2

updates:
  - package-ecosystem: "gradle"
    directory: "/Código-fonte do app = Mulher Amparada"
    schedule:
      interval: "weekly"
      
      
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

> no disfarce de calculadora, caso a usuaria esqueça tudo, existe um popup que usa o BiometricPrompt que poderá resetar tanto a senha tanto a pergunta de recuperação (e ele usa APENAS o device credencial!), porém, quando utiliza o BiometricPrompt e é validado, ele pergunta repetidamente a pergunta de recuperação e a senha, eu preferi deixar assim porque ai a usuaria pode errar quantas vezes ela quiser!, e eu preferi que o BiometricPrompt use só apenas o device credential, porque senão o agressor que tiver com acesso físico ao aparelho pode ou se cadastrar ou coagir a usuária a desbloquear!

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
Usando criptografia, a usuária poderá anotar o que quiser. Com a senha, ficará seguro e também não some, pois estará guardado, e é possivel baixar as páginas desse diário!



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
