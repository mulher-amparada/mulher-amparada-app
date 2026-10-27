
# 🏗Estrutura do projeto:

### Sobre como eu automatizo o projeto:

E eu também já consegui configurar um ssh na conta, e 2fa nela tambem, e com o ssh, eu consegui mover pastas inteiras para o repositório, e transformei 4 em 1, e mais de 100 commits em 1,

```
Automações do Mulher Amparada:

O projeto Mulher Amparada utiliza GitHub Actions para automatizar a compilação do aplicativo, a análise de dados, a geração de relatórios, a atualização do site e a publicação de arquivos.

Compilação do aplicativo:

Compila o aplicativo Android em Kotlin utilizando Java 17 e Gradle, verifica a disponibilidade do Android SDK 37 e assina o APK com as credenciais configuradas nos Secrets do GitHub.

Assinatura e integridade do APK:

Assina o APK, verifica sua assinatura, calcula o tamanho real do arquivo e gera seu hash SHA-256 para conferência de integridade.

Artifacts:

Disponibiliza os arquivos produzidos pelas automações como artifacts do GitHub Actions, incluindo o APK assinado, relatórios, gráficos, documentação, sitemap e arquivos do site.

Página de download:

Atualiza automaticamente o "download.html" com o tamanho real do arquivo e seu SHA-256.

Análise de linguagens:

Utiliza o "cloc" para identificar as linguagens presentes no repositório e gerar relatórios em JSON e Markdown. atribuindo cores determinísticas às linguagens e atualizando as páginas HTML compatíveis.

Análise de dados em Julia:

Instala as dependências do projeto Julia, executa "src/main.jl" e valida os resultados produzidos, incluindo o CSV processado e o gráfico do Ligue 180 de 2019.

GitHub Pages:

Gera o site com Jekyll, prepara os arquivos para publicação e realiza o deploy no GitHub Pages.

Observatório:

Compila o programa "tools/observatorio.c", gera mapas em PNG sequenciais, verifica os arquivos produzidos e disponibiliza as imagens como artifacts.

Atualização do rodapé:

Executa o script "scripts/atualizar_footer.py" para atualizar o "index.html" com conteúdo convertido do "README.md" para HTML.

Relatório de commits:

Executa o script "scripts/commits.rb" para gerar relatórios sobre o histórico de commits e disponibilizá-los como artifacts.

Documentação automática do workflow:

Executa o script PHP "scripts/pesquisar_projeto.php" para gerar a documentação do workflow em Markdown e disponibilizá-la como artifact.

Auto-merge das pull requests do dependabot:

Basicamente é um job que faz auto-merge de todas as pull request do dependabot, deixando o projeto atualizado constantemente

Publicação pelo Mulher Amparada Bot:

O job "salvar-na-main" baixa os artifacts gerados pelos outros jobs, compila o publicador em C++ e utiliza as credenciais de um GitHub App para publicar os arquivos na branch "main".

sincronizar-app:
    
ele aoenas chama outro workflow que pega todo o codigo-fonte do app do mulher amparada e copia e mescla para o nosso sistema operacional (que contém só uma pequena parte de todo o AOSP!)

O publicador utiliza os Secrets "MULHER_AMPARADA_APP_ID", "MULHER_AMPARADA_INSTALLATION_ID" e "MULHER_AMPARADA_PRIVATE_KEY" para autenticação.

executar-dependabot-maintenance:

ele executa o workflow que faz uma solicitação para disparar o dependabot!

Dependabot:

O projeto também conta com o Dependabot, ferramenta do GitHub que verifica dependências desatualizadas e vulnerabilidades compatíveis com suas configurações, podendo abrir pull requests para atualizar dependências.

Resultado final:

O workflow automatiza tarefas de compilação, análise, documentação, publicação e manutenção do projeto. Ao final, o Mulher-Amparada-Bot grava os artifacts na branch "main", conforme as dependências e condições definidas no workflow.

e o yml também usa o typescript e bash para validar o qr code de todas as imagens!

e no final do automations yml, ele atualiza o total de commits la no readme (existem tanto em outro repo antigo do projeto + a versão nova!), e também publica nosso packpage!
```


### Sobre o site que está hospedado pelo github pages:

Na primeira página do site, tem um botão de pânico que liga para o 180 e usa o tel: do navegador para abrir o telefone nativo do celular com esse número já discado!

embaixo, tem um botão que leva pro financas md, depois pro leis md, depois pro readme md, e depois pro suport md

depois um gerador de qr code

e uma página mostrando todas as linguagens de programação utilizadas

depois uma página com imagens dos apks de todas as versões do app (só as imagens)

o nome verdadeiro do app = Mulher Amparada Pela Liberdade Feminina 

E uma página mostrando o trabalho de uma mulher empoderada (e eu estava mostrando sobre as vendas dela, para dizer que todas as mulheres podem crescer!, e ela não tem relação com a questão do projeto ser gratuito, essa página é algo separado, o app ainda é totalmente grátis!)

e depois o conteúdo do readme em convertido para html!

e as páginas desse site estão indexadas no google search console também!

### Sobre as atualizações do app:

O app já está na versão:

`Versão 33 (versão final)`

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

Proteção do Código:

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