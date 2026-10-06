# Mulher Amparada

Um projeto totalmente gratuito e livre de anúncios, projetado por um menino autista nível 1 de 15 anos!, usando o apoio do chatgpt, sem curso formal!

e eu programei todo esse projeto no A16 5g da samsung, e nas primeiras versoes, onde nem tinha os recursos, ja programei ele num app de A-IDE, num A05, e eu ja perdi vários projetos porque o celular nao aguentava, matava o projeto porque matou o processo de compilação!, e uma vez eu fiz o projeto do mulher amparada e eu mesmo fiz o app do mulher amparada (primeiro eu refiz porque o family link apagou a pasta segura samsung, depois na 2 vez que perdi portei tudo do apk compilado para descompilado, e depois perdi denovo mas ai eu ja tinha o código-fonte!)

E vale lembrar que antes todas as páginas eram html com webview, agora não são mais, só a função de navegador usa webview sem html, tudo é compose (no primeiro dia foram 10h de trabalho no segundo foram 7h se trabalho!)

**Commits totais de toda a história do projeto, (feitos por mim e pelos workflows do github actions!) = 4478**

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
Automações do Mulher Amparada

O workflow Automações do Mulher Amparada automatiza a compilação, assinatura, publicação, atualização e verificação do projeto.

Ele é executado automaticamente quando há um "push" na branch "main" e também pode ser executado manualmente através do "workflow_dispatch".

⚙️ O que ele faz

📱 Compilação do APK

O workflow:

- Baixa o código do repositório.
- Verifica se o projeto Android está presente.
- Configura Java 17 Temurin.
- Verifica a disponibilidade do Android SDK 37.
- Dá permissão de execução ao Gradle.
- Compila o APK usando Gradle.
- Localiza o APK gerado.
- Assina o APK usando "apksigner".
- Verifica a assinatura do APK.
- Remove o keystore temporário utilizado durante a assinatura.

🔐 Assinatura

A assinatura utiliza os seguintes Secrets do GitHub:

- "KEYSTORE_BASE64"
- "KEYSTORE_PASSWORD"
- "KEY_ALIAS"
- "KEY_PASSWORD"

O keystore é reconstruído temporariamente durante a execução e removido depois da assinatura.

📦 Artifact

O APK assinado é enviado como um GitHub Actions Artifact utilizando:

"actions/upload-artifact@v6"

📏 Tamanho e SHA-256

O workflow calcula automaticamente:

- tamanho real do APK em bytes;
- tamanho em MB;
- hash SHA-256.

Essas informações são utilizadas posteriormente na Release e no "download.html".

📂 Atualização do APK no repositório

O APK assinado é copiado automaticamente para:

"Código-fonte do app = Mulher Amparada/app-release.apk"

Se houver alteração, o workflow cria um commit e envia o arquivo para a branch "main".

🚀 GitHub Release

O workflow utiliza a Release:

"app"

Ele:

1. Verifica se a Release existe.
2. Procura o APK antigo.
3. Remove o APK antigo.
4. Envia o novo APK.
5. Atualiza as notas da Release com o SHA-256 da compilação.

🌐 Atualização do "download.html"

O workflow atualiza automaticamente no "download.html":

- link de download do APK;
- tamanho real do APK;
- SHA-256.

Depois, se houver alteração, o arquivo é enviado para a branch "main".

---

🌐 GitHub Pages

O site é construído e publicado automaticamente.

Build do site

Utiliza:

- "actions/checkout@v6"
- "actions/configure-pages@v6"
- "actions/jekyll-build-pages@v1"
- "actions/upload-pages-artifact@v5"

O conteúdo do repositório é transformado em um artifact do GitHub Pages.

Deploy

A publicação é feita através de:

"actions/deploy-pages@v5"

Assim, o site é atualizado automaticamente após uma nova compilação.

---

🔢 Atualização do contador de commits

O workflow calcula a quantidade total de commits existentes no histórico do projeto.

Ele:

1. Baixa todo o histórico Git.
2. Conta os commits.
3. Localiza o número no "README.md".
4. Atualiza o valor.
5. Cria um commit somente quando existe alteração.

---

📝 Sincronização com DEV.to

O README é utilizado como fonte de conteúdo para o artigo do projeto no DEV.to.

O workflow:

- baixa o README atualizado;
- autentica utilizando "DEVTO_API_KEY";
- envia o conteúdo através da API do DEV.to;
- atualiza automaticamente o artigo.

---

📰 Sincronização com Paper.wf

O README também é sincronizado com a publicação do projeto no Paper.wf.

O workflow:

1. Autentica na API do Paper.wf.
2. Localiza a publicação existente.
3. Obtém o ID da publicação.
4. Atualiza o título e o conteúdo.
5. Utiliza o README como conteúdo principal.

São utilizados os Secrets:

- "PAPERWF_USERNAME"
- "PAPERWF_PASSWORD"

---

🗺️ Sitemap

O workflow gera automaticamente o "sitemap.xml" utilizando:

"cicirello/generate-sitemap@v1"

O sitemap utiliza como endereço-base:

"https://mulher-amparada.github.io/mulher-amparada-app/"

Depois de gerado, ele é salvo no próprio repositório.

---

🔎 IndexNow

O sitemap é enviado ao IndexNow utilizando:

"bojieyang/indexnow-action@v3"

A autenticação utiliza o Secret:

"INDEXNOW_KEY"

O objetivo é comunicar aos mecanismos de busca compatíveis que existem URLs atualizadas para serem rastreadas.

---

🔍 Auditoria SEO

O workflow executa uma auditoria SEO completa utilizando:

"@nurkamol/seo-audit"

A ferramenta analisa o site e gera um relatório:

"seo-audit.md"

O relatório é armazenado como artifact do GitHub Actions.

A auditoria permite verificar problemas relacionados a aspectos como:

- SEO técnico;
- metadados;
- indexabilidade;
- links;
- imagens;
- canonical;
- Open Graph;
- dados estruturados;
- sitemap.

---

🔗 Verificação de links quebrados

O workflow utiliza:

"ScholliYT/Broken-Links-Crawler-Action@v3"

para rastrear o site e verificar se existem links que não funcionam.

Ele analisa o endereço:

"https://mulher-amparada.github.io/mulher-amparada-app/"

e seus links internos.

---

🚦 Lighthouse

O workflow utiliza:

"treosh/lighthouse-ci-action@v12"

para executar uma auditoria Lighthouse na página inicial:

"https://mulher-amparada.github.io/mulher-amparada-app/"

Os resultados são enviados como artifacts através de:

"uploadArtifacts: true"

O Lighthouse permite analisar aspectos como:

- desempenho;
- acessibilidade;
- boas práticas;
- SEO;
- experiência geral da página.

---

🔐 Principais Secrets utilizados

Secret| Finalidade
"KEYSTORE_BASE64"| Keystore Android codificado em Base64
"KEYSTORE_PASSWORD"| Senha do keystore
"KEY_ALIAS"| Alias da chave de assinatura
"KEY_PASSWORD"| Senha da chave
"DEVTO_API_KEY"| Autenticação da API do DEV.to
"PAPERWF_USERNAME"| Usuário do Paper.wf
"PAPERWF_PASSWORD"| Senha do Paper.wf
"INDEXNOW_KEY"| Chave utilizada pelo IndexNow

---

🧩 Tecnologias e serviços utilizados

O workflow integra:

- GitHub Actions
- GitHub Pages
- GitHub Releases
- Gradle
- Android SDK 37
- Java 17 Temurin
- apksigner
- Jekyll
- DEV.to API
- Paper.wf API
- IndexNow
- Lighthouse
- SEO Audit
- Broken Links Crawler
- Git

🔄 Fluxo geral

Push na main
      │
      ▼
Compilar APK
      │
      ├──► Assinar APK
      │
      ├──► Calcular SHA-256
      │
      ├──► Publicar Release
      │
      └──► Atualizar download.html
      │
      ▼
Construir GitHub Pages
      │
      ▼
Publicar GitHub Pages
      │
      ├──► IndexNow
      ├──► Auditoria SEO
      ├──► Links quebrados
      └──► Lighthouse

Dessa forma, uma alteração enviada para a "main" pode disparar automaticamente praticamente todo o ciclo de atualização do Mulher Amparada, desde a compilação do aplicativo até a publicação do site e verificações de SEO.
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

####Área protegida:

**Título:**
>Desbloquear a área protegida

**Descrição:**
>🌸 Apenas a usuária cadastrada pode acessar este local

####Área do amparo:

**Título:**

>Desbloquear o espaço do amparo

**Descrição:**

>🌸 Acesso protegido por biometria

####E nos dois tem embaixo (Use sua impressão digital., Usar o reconhecimento facial.)

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

> no disfarce de calculadora, caso a usuaria esqueça tudo, existe um popup que usa o BiometricPrompt que poderá resetar tanto a senha tanto a pergunta de recuperação (e ele usa APENAS o device credencial!)

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

# 📢 Divulgação:

Acompanhe as referências e publicações relacionadas ao projeto **Mulher Amparada** na página oficial de divulgação da Wiki.

🔗 **[Acessar a página de divulgação](https://github.com/mulher-amparada/mulher-amparada-app/wiki/Divulga%C3%A7%C3%A3o-do-projeto-=-Mulher-Amparada)**

# 📦Packpage do repositório no github:

ele é um packpage focado para quem quiser divulgar o projeto!

### ele contém:

pngs de divulgação com link, texto e qr do site do giithub pages;

o proprio qr do site do giithub pages;

banners do projeto, tanto png, tanto um código para html;

icones para sites e para usos no geral, tanto o do mulher amparada tanto o ic_launcher do app, versão normal e monocromático;

>Ao indicar o projeto para uma mulher em situação de risco, **priorize o compartilhamento do QR Code impresso ou na tela**, em vez de enviar links de texto por mensagens (como WhatsApp ou SMS). 
>
**Por que o QR Code?** Links de texto deixam rastros fáceis de serem interceptados por agressores que monitoram o celular da vítima. 
>
>**Como agir:** Se for imprimir cartazes ou banners, certifique-se de que o QR Code está visível e em alta resolução. Isso permite que a usuária aponte a câmera e acesse o projeto diretamente, minimizando o histórico de digitação e mensagens trocadas.

### Instalação

```bash
npm install @mulher-amparada/divulgacao