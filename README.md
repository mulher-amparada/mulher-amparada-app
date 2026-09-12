# Mulher Amparada

## um projeto totalmente gratuito e livre de anúncios, projetado por um menino autista nível 1 de 15 anos!, usando o apoio do chatgpt, sem curso formal!

## E saibam que o projeto é: Source-Available

O **Mulher Amparada** é um aplicativo de segurança e proteção desenvolvido especialmente para mulheres que desejam se sentir mais seguras em situações do dia a dia. O objetivo principal do aplicativo é oferecer suporte rápido, eficiente e acessível em momentos de risco, permitindo que a usuária consiga pedir ajuda de forma simples e imediata. Em um cenário onde muitas mulheres enfrentam situações de vulnerabilidade, ter uma ferramenta confiável pode fazer toda a diferença, e é exatamente isso que o Mulher Amparada busca proporcionar.

O aplicativo foi pensado para funcionar como um apoio digital em momentos críticos, permitindo acesso rápido a serviços essenciais como**Polícia (190)**,**SAMU (192)**e**Central de Atendimento à Mulher (180)**. Com apenas alguns toques, a usuária pode realizar ligações de emergência, reduzindo o tempo de reação em situações onde cada segundo é importante. Essa agilidade pode ser decisiva para evitar agravamentos e garantir assistência o mais rápido possível.

Para desinstalar o aplicativo, primeiro será necessário desativar a permissão de Administrador do dispositivo. Em alguns aparelhos, também poderá ser necessário permitir Configurações restritas. Se essa opção estiver disponível, pressione e segure o ícone do aplicativo, toque em Informações do aplicativo, abra o menu de três pontos e ative Permitir configurações restritas.

Antes de conceder essa permissão, leia atentamente as informações exibidas pelo Android e só prossiga se compreender as funcionalidades e os efeitos dessa autorização.

(como o código esta dentro do repositório , nao precisarei explicar!)

## Avisos:

> ⚠️Vale lembrar que o projeto não substitui serviços oficiais do governo e também não garante segurança imediata, bem como as funções dependem do estado e hardware de cada aparelho!

> ⚠️ Vale lembrar: o Gerenciador de Arquivos do Mulher Amparada funciona principalmente como um visualizador de arquivos. O nome “Gerenciador de Arquivos” também faz parte do disfarce do aplicativo. Ele foi projetado dessa forma por uma questão de segurança: o aplicativo não oferece funções próprias para excluir, mover, copiar ou renomear arquivos, reduzindo o risco de apagar ou alterar acidentalmente algum arquivo importante — inclusive possíveis registros que a usuária queira preservar.

> ⚠️E também quando for clicar em qualquer botão de apagar no app, ele pode apagar TODOS OS CONTEÚDOS criptografados de TODO O APP, então CUIDADO!

> ⚠️Ao utilizar a função de baixar o histórico de ajuda, o aplicativo gera um único arquivo no formato JSON. Os dados utilizados para gerar esse histórico são obtidos por meio das APIs protegidas do Android, que controlam o acesso ao histórico de chamadas por meio das permissões do sistema. Por isso, o aplicativo não aplica a classe "Cripto" novamente durante a exportação. O arquivo JSON exportado, entretanto, não é um arquivo criptografado. Ele é salvo como um arquivo comum na área de downloads do dispositivo. A proteção de acesso fornecida pelo Android ao histórico de chamadas não significa que o arquivo exportado permaneça criptografado. O arquivo também contém hashes SHA-256, utilizados para verificação de integridade dos dados. SHA-256 não é um mecanismo de criptografia e não substitui a criptografia para proteção do conteúdo.

> ⚠️APENAS nas proteções por sensores — Escurecimento por inclinação e Balançar o celular para pedir ajuda — o app verifica se o aparelho possui os sensores adequados. Caso não possua, a função não é ativada e um aviso é exibido ao usuário.

> ⚠️Botão de pânico: caso o aplicativo não possua a permissão necessária para realizar chamadas diretamente, ele utiliza o "ACTION_DIAL" como alternativa, abrindo o discador com o número de emergência. Dessa forma, o recurso continua disponível mesmo sem a permissão de chamada.

## Sobre o site que está hospedado pelo github pages:

Na primeira página do site, tem 3 botoes que ligam para (180, 190 e 192), e usam o tel: do navegador para abrir o telefone nativo do celular com esses números já discados de acordo com o que você escolheu!

## Sobre como o aplicativo é compilado:

e os apps são compilados com o workflow, gera o código sha-256 e o zip com o app, envia para a release pela tag correspondente, e atualiza o link das páginas de download, e o tamanho do apk dessas páginas!

no repositório tem um yml que ativa todos os dias, procura no codigo fonte dos 3 apps o arquivo build do módulo do app, e se ele ver que saiu uma versão android nova, ele atualiza, totalmente sozinho!, mantendo os APKs sincronizados com as versões do código-fonte detectadas pelo workflow.

e todas as activitys tem o android exported="false", menos a MainActivity porque a partir das versões mais recentes do android, a tela inicial do app, se ela tiver um intent filter, ela tem que estar obrigatoriamente com android exported="true"

E o workflow utiliza um keystore de assinatura armazenado de forma protegida nos GitHub Actions Secrets. As informações necessárias para acessar o keystore e selecionar a chave são fornecidas pelas variáveis KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS e KEY_PASSWORD.

Em:

/settings/secrets and variables/actions/repository secrets/

## Sobre como eu automatizo o projeto:

E eu também já consegui configurar um ssh na conta, e 2fa nela tambem, e com o ssh, eu consegui mover pastas inteiras para o repositório, e transformei 4 em 1, e mais de 100 commits em 1,

e agora a pagina la no GitHub pages faz um deploy a cada 1 hora

e no site, ele conta com 3 botoes que ligam pro 192, 190 e 180 (ai voce escolhe), e eles usam o "tel:" do navegador

e os contatos de confiança, quando são cadrastados eles também são criptografados!

e o bloqueio do celular por barulho é assim: bloqueia, voce desbloqueia e pra ter denovo tem que desativar e ativar denovo!

## Sobre como foi escrito o texto do biometricPrompt do app:

- MainActivity (tela que contém o webview):

**Título:**
> Desbloquear a área protegida

**Descrição:**
> 🌸 Apenas a usuária cadastrada pode acessar este local

- OrdemActivity (tela que contém o assistente de voz):
 
**Título:**
> Desbloquear o acesso ao assistente de voz

**Descrição:**
> 🌸 Apenas a usuária cadastrada pode acessar este local

O método de autenticação é definido pelo próprio Android de acordo com os autenticadores disponíveis no dispositivo, utilizando "BIOMETRIC_WEAK" e "DEVICE_CREDENTIAL".

# Direitos que toda mulher tem!

Conheça 100 direitos e garantias assegurados às mulheres pela legislação brasileira.

Para consultar a legislação completa e as referências utilizadas nesta seção, acesse**[LEIS.md](LEIS.md)**.

Conhecer seus direitos é importante para reconhecer situações de proteção, buscar ajuda quando necessário e entender as garantias previstas em lei.

----

# Conhecimentos para recuperar sua autonomia

Conhecimentos e informações para ajudar você a compreender melhor sua vida financeira, organizar seu dinheiro e fortalecer sua autonomia.

Para acessar o conteúdo completo sobre finanças, consulte**[Finanças](FINANÇAS.md)**.

----

# Mensagem de apoio e acolhimento para as usuárias

Uma carta para você

Esta carta foi feita para apoiar você em sua caminhada, trazendo conhecimentos e informações que podem ajudar a compreender melhor sua vida financeira, organizar seu dinheiro e fortalecer, cada vez mais, sua autonomia.

Você não precisa saber tudo de uma vez. Conhecimento também é uma forma de proteção, e entender suas próprias finanças pode ajudar você a tomar decisões com mais segurança e independência.

Para acessar o conteúdo completo sobre finanças, consulte a [Carta do desenvolvedor](ABOUT.md).

----

## Todas as funções do aplicativo!:

**Disfarce do app (assistente de saúde falso!):**
tutorial: ao entrar no app, clique no canto superior direito com o icone de calculadora. e ai quando ele for iniciado, ele pedirá para criar uma senha (e salva em uma classe kt de criptografia), assim so acessa com a senha informada, para resetar essa senha (dê 5 toques em menos de 2 segundos, e digite como você gosta de ser chamada, e digite sua nova senha!), mas antes dessa tela, tem outra tipo uma gaveta de apps..., porém, agora no mulher amparada, ele já vem com o icone de calculadora e o nome calculadora, só dá para mudar o icone, ou seja, o app ja vem com icone de (Assistente de saúde), uma tela genérica de elementos de medição de saúde (bpm e etc), e vale lembrar que:

Os dados da primeira página do app são meramente fictícios e não representam informações reais!

e tambem, reforcando que no canto superior direito tem um icone de calculadora que quando clica vai pra uma calculadora e aparece o disfarce de calculadora 

----

**Botão de Pânico:**
Botão de Pânico, com ligação ao 180 de forma direta no primeiro clique.

----

**Proteção por Barulho:**
Ative a proteção, faça barulho alto e ele liga para o 180.

----

**Balançar o Celular para Pedir Ajuda:**
Ative e, ao chacoalhar o celular, ele liga para o 180.

**Escurecimento por inclinação:**
com isso, voce pode controlar o brilho da tela clicando em um botão..., porém, e tipo como se fosse o menor brilho do celular, e ai depois ele deixa a tela preta (nao com brilho e sim colocando a cor), (honestamente, antes aparecia as duas barras, agora elas se escondem!), e o efeito e vitalicio ate fechar e abrir o app!

----

**Bloquear a tela do celular pelo barulho:**
ao ativado, ao fazer barulho alto, ele usa o administrador do dispositivo e bloqueia a tela do celular!, e o melhor e que da para ativar essa e a proteção por barulho ao mesmo tempo!, (sim, eu testei isso no dia 06/09/2026, e não só isso, se o agressor jogar o celular e ele não quebrar, a proteção por barulho se tiver ativa em teoria pode iniciar a ligação pro 180!)

----

**Emergência:**
Saindo dessa área, existem botões que abrem o aplicativo nativo do telefone nos números 190, 192 e 180.

----

**Compartilhamento Rápido de Localização:**
Além disso, existe um botão dentro do aplicativo que obtém a localização atual, monta um link do Google Maps com as coordenadas e abre uma conversa no WhatsApp do 180 com a mensagem preparada. O envio não é automático: é necessário apenas conferir a mensagem e tocar no botão de enviar.

----

Contatos de Confiança:
Além dos contatos de confiança, clicando no primeiro botão você seleciona e salva o contato. O botão abaixo envia um pedido de ajuda para ele.

----

## Área Protegida:
Se estiver cadastrado no celular, com Biometric Prompt junto com Device Credential e autenticação weak, pode desbloquear essa área com impressão digital, rosto, PIN, padrão, senha e outros métodos.


🔐 Sistema Cripto (Segurança do App) = Antigo LocalStorage!:


Este sistema salva dados de forma segura usando criptografia nativa do Android.


🧠 Como funciona:


Quando você salva um dado no app, ele não fica em texto normal no celular. Ele é automaticamente criptografado antes de ser armazenado.


Isso significa que mesmo acessando os arquivos do dispositivo, os dados aparecem como códigos ilegíveis.


🔑 Tecnologia usada:



AES-256 (criptografia forte)

Android Keystore (chave protegida pelo sistema)

EncryptedSharedPreferences


⚙️ O que cada função faz:


salvar(chave, valor) → guarda o dado de forma criptografada

carregar(chave) → recupera o dado original

remover(chave) → apaga um dado específico

limparTudo() → remove todos os dados salvos


🔒 Segurança:


Os dados são protegidos por uma chave segura do próprio Android e não ficam visíveis diretamente no armazenamento do aparelho.

----

**Calendário Menstrual:**
Registre como dói cada dia e, com isso, o aplicativo monta um calendário.

----

**Calendário de eventos:**
Registra eventos da usuária quando ela precisar

----

**Rotina:**
Sistema de pontos, com registro de comidas e bebidas boas e ruins, bem como a adição de registro de exercícios físicos fáceis, médios ou difíceis e contagem de tempo de cada um deles, além de sistema de nível e conquistas.

----

**Mapa:**
Mostra um mapa da região e, quando a localização estiver disponível e autorizada, permite visualizar a posição atual.

----

**Diário:**
Usando criptografia, a usuária poderá anotar o que quiser. Com a senha, ficará seguro e também não some, pois estará guardado.

----

**Relógio:**
Mostra o mapa do local atual, o país e outros dados, bem como o ano, semestre, bimestre, mês, quinzena, semana, dia, hora, minuto e segundo.

----

**Calculadora:**
A calculadora pode ser usada para cálculos rápidos do dia a dia.

----

**Tarefas:**
O sistema permite categorizar tarefas em áreas como estudos, trabalho, pessoal e saúde.

As tarefas podem ser marcadas como concluídas para acompanhamento do progresso.

Todas as tarefas são salvas diretamente no webview da usuária.

Os dados ficam armazenados localmente no dispositivo do usuário.

----

**Gravador de voz:**
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

----

Meus arquivos

Dentro do aplicativo, o Gerenciador de Arquivos do Mulher Amparada funciona como um visualizador. Ele permite navegar pelas pastas e, ao selecionar um arquivo, utiliza o mecanismo do Android para abrir um seletor de aplicativos compatíveis com aquele tipo de arquivo, permitindo que a usuária escolha um aplicativo para visualizá-lo ou executá-lo.

O aplicativo possui um único botão para alternar entre os diferentes tipos de armazenamento disponíveis, seguindo uma sequência entre:

Armazenamento interno → cartão SD → dispositivo externo (como pendrive via USB OTG) → armazenamento interno.

O acesso varia de acordo com o tipo de armazenamento:

- Armazenamento interno e cartão SD: utilizam a permissão de Acesso a todos os arquivos, quando concedida pelo Android.
- Dispositivos externos, como pendrives conectados por USB OTG: utilizam o SAF (Storage Access Framework), mecanismo oficial do Android para acesso a documentos e dispositivos de armazenamento externos autorizados pela usuária.

⚠️ Reforçando: o Gerenciador de Arquivos não acessa os arquivos internos do Gravador de Voz do Mulher Amparada nem os dados ou recursos internos de outras funcionalidades do aplicativo. Ele trabalha com arquivos que já estão disponíveis nos armazenamentos do dispositivo e que podem ser acessados pelos mecanismos de armazenamento autorizados pelo Android.

----

**Bloquear a tela do celular:**
Ao tocar neste botão, o aplicativo solicitará a permissão de Administrador do dispositivo, caso ela ainda não tenha sido concedida. Quando essa permissão estiver ativa, o aplicativo poderá bloquear imediatamente a tela do dispositivo, (aviso: depois que a tela é bloqueada pelo sistema, o Android pode exigir novamente o método de credencial do dispositivo antes de permitir determinadas formas de autenticação biométrica.)

----

**Tela de aplicativos:**
Ao tocar neste botão, o app mostrará um site dentro do app que lista todos os outros apps com a permissão query all packpages...

----

## FUNÇÕES AVANÇADAS DA ÁREA PROTEGIDA!:

**lembrando que, sobre esses apps, ja tem o código fonte dele dentro do repositório no site!:**

e o de comando de voz, também precisa desbloquear com biometria 

----

**Assistente Inteligente do Mulher Amparada 🤖💗**

O Assistente Inteligente do Mulher Amparada permite controlar diversas funções do aplicativo utilizando comandos em linguagem natural, tornando o uso mais rápido e acessível.

Com apenas um comando, é possível abrir aplicativos instalados no dispositivo sem precisar procurá-los manualmente.

O assistente também pode ligar diretamente para a Central de Atendimento à Mulher (180), agilizando o acesso ao serviço em situações de necessidade.

Caso autorizado, é possível enviar sua localização pelo WhatsApp para o número oficial do Ligue 180, facilitando o compartilhamento da sua posição.

Você pode solicitar o envio de notificações personalizadas para lembrar compromissos, informações importantes ou qualquer mensagem desejada.

Também existe a opção de simular uma notificação de download com barra de progresso, exibindo o andamento até a conclusão.

Crie eventos no calendário do dispositivo utilizando comandos simples. O aplicativo abre a tela de criação do evento já preenchida para confirmação.

Inicie timers por tempo determinado e receba uma notificação quando o tempo terminar. O timer também pode ser cancelado por comando.

Consulte rapidamente os principais telefones de apoio e emergência disponíveis no Brasil diretamente pelo assistente.

Visualize quantas vezes números de emergência, como 180, 181, 188, 190, 191, 192, 193 e 156 foram acionados, utilizando o histórico de chamadas do dispositivo quando autorizado.

Abra fotos armazenadas no aparelho informando apenas o nome do arquivo.

Também é possível abrir músicas e vídeos diretamente pelo nome do arquivo, utilizando os aplicativos compatíveis instalados no dispositivo.

Verifique se o Bluetooth está ligado ou desligado por comando de voz ou texto.

Abra rapidamente as configurações do Bluetooth quando desejar conectar ou gerenciar dispositivos.

Consulte a lista de dispositivos Bluetooth pareados no aparelho de forma prática.

Quando configurado como Administrador do Dispositivo, o assistente também pode bloquear imediatamente a tela do celular.

Todas essas funções foram desenvolvidas para oferecer praticidade, rapidez e facilidade no dia a dia, mantendo uma experiência simples, intuitiva e totalmente gratuita, sem anúncios.

----
    
**Comandos do Assistente Inteligente 🤖💗**

O Assistente Inteligente reconhece comandos em linguagem natural. Veja alguns exemplos:

📱 Aplicativos
• Abrir WhatsApp
• Abrir Instagram
• Abrir Configurações

📞 Emergência
• Alô e da companhia de energia
• Alô é da energia
• Alô e da energia
• é da energia
• e da companhia de energia
• É da companhia de energia


📍 Localização
• Enviar minha localização para o 180
• Mandar minha localização para o 180

🔔 Notificações
• Me mande uma notificação Estou chegando.
• Me mande uma notificação Lembrete importante.

⬇️ Download fictício

• me envie uma notificação de download ficticio
• Me envie uma notificação de download ficticio
• me envie uma notificação de download fictício
• Me envie uma notificação de download fictício
• me mande uma notificação de download fictício
• Me mande uma notificação de download fictício
• me mande uma notificação de download ficticio
• Me mande uma notificação de download ficticio

📅 Calendário
• Criar evento Reunião
• Crie um evento Consulta médica
• Adicionar evento Aniversário
• Marcar compromisso Academia

⏲️ Timer
• Inicie um timer de 5 minutos
• Inicie um timer de 30 minutos
• Cancelar timer

☎️ Telefones de apoio
• Quais são os meus apoios
• Quais sao os meus apoios
• Meus apoios

📋 Histórico de ajuda
• Quantas vezes liguei para pedir ajuda
• Ligações de ajuda
• Histórico de ajuda

🖼️ Fotos
• Abrir foto viagem.jpg
• Abrir foto imagem.png

🎵 Músicas
• Abrir música musica.mp3

🎬 Vídeos
• Abrir vídeo video.mp4

📶 Bluetooth
• Bluetooth está ligado
• Bluetooth esta ligado
• Abrir Bluetooth
• Configurações do Bluetooth
• Listar dispositivos Bluetooth
• Quais dispositivos Bluetooth
• Dispositivos pareados

🔒 Bloqueio do aparelho
• Bloquear celular
• Bloquear aparelho

🔍 Pesquisa
• Pesquisar + termo a ser pesquisado (e ai ele abre o navegador com a pesquisa feita no google)


```md

----

## Sobre o comando de baixar histórico de chamadas pelo assistente de voz:

**Palavras para acionar o comando:**

baixar registro de chamada
baixe o registro de chamada
baixar registro das chamadas
baixar histórico de chamadas
baixe o histórico de chamadas
baixar histórico das chamadas
salvar registro de chamada
salvar histórico de chamadas
exportar registro de chamada
exportar histórico de chamadas
exporte o registro de chamada
exporte o histórico de chamadas
gerar registro de chamada
gerar histórico de chamadas
gerar arquivo das chamadas
criar registro de chamadas
criar arquivo de chamadas
criar relatório de chamadas

**Observações sobre essa função:**

# Registro de Chamadas — Exportação e Integridade

## 1. Objetivo

A função de exportação de registro de chamadas do aplicativo **Mulher Amparada** permite que a usuária, mediante as permissões necessárias do sistema Android, gere uma cópia do histórico de chamadas disponível no aparelho.

O arquivo é exportado em formato **JSON** e salvo na pasta pública de **Downloads** do dispositivo.

O recurso tem finalidade de organização, consulta e preservação de uma cópia dos dados disponíveis no aparelho.

---

## 2. Funcionamento

Quando a usuária utiliza o comando:

> baixar registro de chamada

o aplicativo executa as seguintes etapas:

1. Verifica se a permissão de leitura do histórico de chamadas foi concedida.
2. Consulta o registro de chamadas disponibilizado pelo sistema Android.
3. Obtém, quando disponíveis:
   - número da chamada;
   - data e horário;
   - tipo da chamada;
   - duração da chamada.
4. Organiza cada chamada como um objeto JSON.
5. Calcula um hash SHA-256 dos dados do registro.
6. Calcula novamente o SHA-256 dos mesmos dados para conferência.
7. Cria o arquivo JSON.
8. Salva o arquivo na pasta `Downloads`.
9. Informa à usuária que o arquivo foi criado.

---

## 3. Dados coletados pela função

A função utiliza os dados disponibilizados pelo `CallLog` do Android.

Atualmente são utilizados:

| Campo | Descrição |
|---|---|
| `numero` | Número associado à chamada |
| `data` | Data e horário registrados pelo sistema |
| `tipo` | Tipo da chamada |
| `duracao_segundos` | Duração registrada em segundos |

Os tipos podem ser:

- `Recebida`
- `Realizada`
- `Perdida`
- `Rejeitada`
- `Bloqueada`
- `Outro`

O aplicativo não deve afirmar que esses dados representam necessariamente toda a atividade telefônica do aparelho.

O resultado depende das informações que o próprio sistema Android disponibiliza para o aplicativo e das permissões concedidas pela usuária.

---

## 4. Arquivo exportado

O arquivo atualmente gerado possui o nome:

`registro_de_chamada.json`

O tipo MIME utilizado é:

`application/json`

O arquivo é salvo na pasta:

`Downloads`

---

## 5. Estrutura do JSON

A estrutura possui informações sobre o formato, o algoritmo utilizado e o registro das chamadas.

Exemplo:


{
  "tipo": "registro_de_chamada",
  "formato": "JSON",
  "algoritmo_hash": "SHA-256",
  "sha256_antes": "HASH_AQUI",
  "registro_chamadas": [
    {
      "numero": "190",
      "data": "12/09/2026 16:30:00",
      "tipo": "Realizada",
      "duracao_segundos": 35
    }
  ],
  "sha256_depois": "HASH_AQUI"
}

Os valores apresentados acima são apenas ilustrativos.

---

## 6. SHA-256 antes

Antes da criação do JSON final, o aplicativo transforma a representação dos registros de chamadas em bytes utilizando UTF-8.

Esses dados são processados pelo algoritmo:

`SHA-256`

O resultado é armazenado no campo:

`sha256_antes`

O SHA-256 produz uma representação hexadecimal do conteúdo utilizado no cálculo do hash.

---

## 7. SHA-256 depois

O aplicativo calcula novamente o SHA-256 sobre a mesma representação dos dados do registro.

O resultado é armazenado no campo:

`sha256_depois`

Em condições normais, os dois valores devem ser iguais:

```text
sha256_antes == sha256_depois


Essa comparação funciona como uma verificação de consistência do conteúdo que foi utilizado para gerar o arquivo.

---

## 8. O que o SHA-256 significa

O SHA-256 é uma função de hash criptográfico.

Uma pequena alteração nos dados utilizados no cálculo normalmente produz um resultado completamente diferente.

Exemplo:

```text
Dados originais
       ↓
    SHA-256
       ↓
    Hash A

Se os dados forem alterados:

Dados alterados
       ↓
    SHA-256
       ↓
    Hash B

Os valores `Hash A` e `Hash B` deverão ser diferentes quando o conteúdo realmente tiver sido alterado.

---

## 9. Limitação importante do SHA-256

O SHA-256 **não comprova, sozinho, que uma informação é verdadeira**.

Ele pode ajudar a verificar se determinado conteúdo permaneceu igual ao conteúdo utilizado no cálculo do hash.

Portanto:

> O SHA-256 é utilizado como mecanismo de verificação de integridade, e não como prova isolada da autenticidade, origem ou veracidade dos registros.

A existência de um hash não significa, por si só, que o conteúdo foi produzido por uma autoridade pública, operadora telefônica, autoridade policial ou judicial.

---

## 10. Origem dos dados

Os dados utilizados pela função são obtidos através da API de registro de chamadas disponibilizada pelo sistema Android.

Consequentemente, o aplicativo depende:

- da versão do Android;
- das permissões concedidas;
- das informações existentes no dispositivo;
- das limitações impostas pelo sistema;
- do comportamento do aplicativo de telefone e do fabricante do aparelho.

O aplicativo não cria artificialmente as chamadas apresentadas no registro.

---

## 11. Permissão

Para realizar a leitura do histórico de chamadas, o aplicativo necessita da permissão correspondente do Android:

`android.permission.READ_CALL_LOG`

A função verifica essa permissão antes de acessar os dados.

Caso a permissão não esteja disponível, o aplicativo não realiza a leitura e informa a situação à usuária.

---

## 12. Controle da usuária

A exportação é realizada a partir de uma ação solicitada pela usuária.

O aplicativo não deve apresentar o arquivo como um documento oficial emitido por terceiros.

O arquivo representa uma exportação dos dados de chamadas disponíveis para o aplicativo naquele momento.

---

## 13. Privacidade

O registro de chamadas pode conter informações pessoais, incluindo números de telefone, datas, horários e informações relacionadas às comunicações.

Por isso, o arquivo deve ser tratado como um documento potencialmente privado.

A usuária deve evitar compartilhar o arquivo publicamente ou com pessoas que não tenham necessidade de acesso aos dados.

O armazenamento e o compartilhamento do arquivo são de responsabilidade da pessoa que possuir acesso ao dispositivo e ao arquivo exportado, observadas as regras legais aplicáveis.

---

## 14. Sobre a LGPD

O tratamento de dados pessoais deve observar a legislação aplicável, incluindo, quando aplicável, a **Lei Geral de Proteção de Dados Pessoais — Lei nº 13.709/2018 (LGPD)**.

A utilização deste recurso não deve ser interpretada como uma autorização geral para coletar, compartilhar ou divulgar dados pessoais de terceiros.

A finalidade, a necessidade, a segurança, o acesso e eventual compartilhamento dos dados devem ser avaliados de acordo com o contexto concreto e com a legislação aplicável.

**Esta documentação não constitui parecer ou aconselhamento jurídico.**

---

## 15. Sobre utilização como documentação

O arquivo pode ser utilizado pela usuária como uma cópia organizada dos dados disponíveis no dispositivo.

Entretanto, sua aceitação como documento ou elemento probatório dependerá do contexto e da autoridade, instituição ou procedimento em questão.

A simples geração do arquivo pelo aplicativo não garante sua aceitação como prova.

Quando houver necessidade de utilização em procedimento administrativo, policial ou judicial, recomenda-se preservar o arquivo original e buscar orientação jurídica adequada sobre a forma correta de apresentação e preservação dos dados.

---

## 16. Não alteração do histórico original

A função de exportação possui finalidade de leitura e geração de uma cópia.

A criação do arquivo não deve ser interpretada como alteração, exclusão ou correção do histórico original existente no aplicativo de telefone do dispositivo.

O arquivo exportado é uma representação dos dados obtidos no momento da exportação.

---

## 17. Integridade do arquivo

Os hashes armazenados no JSON servem para documentar o valor calculado sobre os dados utilizados durante a geração do registro.

É importante diferenciar:

- integridade dos dados utilizados no cálculo;
- integridade do arquivo JSON;
- autenticidade da origem dos dados;
- veracidade das informações;
- validade jurídica do documento.

Esses conceitos não são equivalentes.

O SHA-256 utilizado pelo aplicativo não substitui mecanismos de assinatura digital ou outros mecanismos de autenticação quando estes forem necessários.

---

## 18. Preservação do arquivo

Se o arquivo for importante para documentação, recomenda-se:

1. não editar o conteúdo original;
2. manter uma cópia de segurança;
3. evitar abrir e salvar o arquivo novamente em editores que possam modificar seu conteúdo;
4. registrar a data em que o arquivo foi exportado;
5. preservar o arquivo original separadamente de cópias modificadas;
6. não substituir o arquivo original por uma versão editada;
7. quando necessário, obter orientação profissional sobre preservação e apresentação do documento.

---

## 19. Alterações posteriores

Se o arquivo JSON for alterado manualmente, seu conteúdo poderá deixar de corresponder aos dados utilizados originalmente no cálculo do hash.

Por isso, uma versão modificada deve ser tratada como uma **cópia modificada**, e não como o arquivo original.

Não se deve substituir o arquivo original por uma versão editada quando houver necessidade de preservar sua integridade.

---

## 20. Arquivo original e cópias

Para fins de organização, recomenda-se diferenciar:

```text
registro_de_chamada.json


como arquivo original exportado pelo aplicativo, de eventuais cópias posteriormente modificadas.

Exemplo:

```text
registro_de_chamada.json
registro_de_chamada_copia.json
registro_de_chamada_anotado.json

Uma cópia modificada não deve ser apresentada como se fosse o arquivo original.

---

## 21. Limitações técnicas

O recurso está sujeito às limitações técnicas do Android.

Entre elas podem estar:

- permissões não concedidas;
- registros indisponíveis;
- limitações do fabricante;
- diferenças entre versões do Android;
- alterações na API do sistema;
- chamadas que não estejam presentes no histórico;
- informações incompletas fornecidas pelo sistema.

Portanto, o aplicativo não garante que todos os registros telefônicos existentes no dispositivo estarão necessariamente disponíveis para exportação.

---

## 22. Responsabilidade sobre os dados

O aplicativo fornece uma ferramenta para exportação dos dados disponíveis ao aplicativo.

A responsabilidade pela utilização posterior do arquivo depende da finalidade e do contexto em que os dados forem utilizados.

O desenvolvedor não deve representar o arquivo como documento oficial de uma operadora telefônica, órgão público, autoridade policial, autoridade judicial ou qualquer outra instituição.

---

## 23. Finalidade do recurso

A funcionalidade foi desenvolvida para auxiliar na:

- organização de registros;
- consulta;
- preservação de uma cópia;
- documentação pessoal;
- verificação de integridade dos dados exportados.

Ela não substitui sistemas oficiais de registro ou consulta de chamadas.

---

## 24. Aviso jurídico

> **AVISO:** Esta documentação possui finalidade informativa e técnica. Ela não constitui aconselhamento jurídico, parecer jurídico ou garantia de validade probatória.

A validade, admissibilidade ou força probatória de um arquivo dependerá das circunstâncias concretas, da legislação aplicável e da análise da autoridade ou profissional competente.

Quando houver uma situação jurídica relevante, recomenda-se procurar orientação de um profissional habilitado.

---

## 25. Resumo técnico

Fluxo simplificado:

```text
Histórico de chamadas do Android
              ↓
       Verificação de permissão
              ↓
          Leitura do CallLog
              ↓
       Organização dos dados
              ↓
          SHA-256 antes
              ↓
        Geração do JSON
              ↓
          SHA-256 depois
              ↓
       Arquivo JSON final
              ↓
        Pasta Downloads


O recurso foi projetado para fornecer uma cópia estruturada dos dados de chamadas disponíveis no dispositivo, acompanhada de informações de integridade criptográfica.

---

## 26. Observação final

O arquivo exportado deve ser considerado uma **cópia dos dados disponibilizados pelo sistema Android no momento da exportação**.

O SHA-256 acrescenta uma camada de verificação de integridade, mas não transforma automaticamente o arquivo em documento oficial ou prova jurídica.

A utilização responsável do recurso envolve preservar o arquivo original, proteger os dados pessoais presentes nele e observar a legislação aplicável.

----

```



## Considerações finais:

e os audios do gravador de voz também são criptografados com a classe Cripto

e o recurso de proteção ppr barulho, chacalhoar o celular e escurecer a tela, tem como ativar e desativar!

todas as activitys tem a flag secure!

e no application do AndroidManifest do app tem allowBackup="false" 

O aplicativo possui um navegador interno. A navegação para o Google é feita diretamente pelo código usando window location replace(), sem disponibilizar o endereço como um link na interface. O aplicativo também não implementa um sistema próprio de registro de histórico de navegação, (ou pelo ou menos eu não coloquei na página)

E temos um recurso na área protegida que é:

História das Mulheres

A página “História das Mulheres” é uma experiência interativa que combina conteúdo histórico com um sistema de progressão. O usuário acumula Pontos de História, desbloqueia cinco períodos históricos, compra melhorias para aumentar sua produção e pode realizar prestígios para obter Legado Permanente.

Pontos de História

Os Pontos de História são o recurso principal da página. O usuário pode obtê-los clicando no botão “+ HISTÓRIA” ou por meio da produção automática.

Cada clique começa concedendo 1 ponto, mas esse valor pode aumentar com as melhorias. O sistema também possui um combo: quando vários cliques são realizados em sequência, o ganho recebe um bônus adicional, limitado a 2x.

Melhorias

A página possui seis melhorias, cada uma com uma função diferente:

✊ Voz coletiva
Aumenta diretamente a quantidade de pontos recebidos por clique. Cada nível acrescenta +1 ao valor base de cada clique. Quanto maior o nível, mais pontos o usuário recebe manualmente.

⚡ Movimento
Cria produção automática de Pontos de História. Cada nível aumenta a quantidade de pontos produzidos por segundo. Assim, o usuário não precisa clicar continuamente para continuar acumulando pontos.

📚 Educação
Aumenta todos os ganhos em 10% por nível. Esse bônus funciona como um multiplicador geral, afetando tanto os pontos obtidos por clique quanto a produção automática.

💼 Organização
Aumenta especificamente a produção automática. Cada nível acrescenta 15% à quantidade de pontos produzidos por segundo pelo “Movimento”.

📰 Imprensa
Aumenta o valor base dos cliques. Cada nível acrescenta +5 pontos ao valor de cada clique antes da aplicação dos multiplicadores.

💎 Memória histórica
Aumenta todos os ganhos em 25% por nível. Assim como “Educação”, funciona como um multiplicador geral e fortalece tanto os cliques quanto a produção automática.

As melhorias possuem preços que aumentam a cada compra. Isso faz com que os primeiros níveis sejam mais acessíveis e os níveis seguintes exijam cada vez mais Pontos de História.

As cinco fases

A página apresenta cinco grandes períodos históricos:

Fase 1 — Antiguidade
Apresenta a participação das mulheres nas sociedades antigas, incluindo suas funções econômicas, familiares, religiosas e culturais.

Fase 2 — Idade Média
Aborda a participação feminina na agricultura, comércio, artesanato, religião, administração de propriedades e outras atividades.

Fase 3 — Idade Moderna
Mostra transformações entre os séculos XV e XVIII, incluindo a expansão da imprensa, da educação e das discussões sobre a capacidade e os direitos das mulheres.

Fase 4 — Revolução Industrial
Apresenta as mudanças provocadas pela industrialização, incluindo o crescimento do trabalho feminino assalariado e dos movimentos por direitos trabalhistas, educação e participação política.

Fase 5 — Século XX e XXI
Mostra transformações relacionadas ao direito ao voto, educação, profissões, ciência, política, movimentos sociais e debates contemporâneos sobre igualdade e direitos.

Cada fase possui uma história própria, um destaque e uma linha do tempo com informações relacionadas ao período.

Legado Permanente

O Legado Permanente é o sistema de progressão que permanece depois de um prestígio.

Para realizar um prestígio, é necessário atingir uma quantidade específica de Pontos de História. O primeiro requisito é de 100 mil pontos. Depois de cada prestígio, o requisito para o próximo aumenta.

Ao realizar o prestígio, os pontos e as melhorias normais são reiniciados, mas o usuário recebe Legado.

Cada ponto de Legado acrescenta 10% ao multiplicador geral dos ganhos. Portanto, o Legado funciona como um bônus permanente que torna as próximas progressões mais eficientes.

Prestígio

O prestígio permite trocar parte do progresso atual por uma vantagem permanente.

Quando o jogador alcança o requisito, a página informa quantos pontos de Legado serão recebidos. Depois da confirmação, os níveis das melhorias normais são zerados e os Pontos de História voltam para zero.

O Legado, entretanto, deve permanecer caso a intenção seja que ele seja realmente permanente.

Salvamento

O progresso é armazenado pelo navegador usando "localStorage". A classe "cripto" é responsável por salvar, carregar e apagar os dados da página.

Dessa maneira, os pontos, melhorias, Legado e outras informações do jogo podem permanecer salvos quando a página é fechada e aberta novamente.

Interface e efeitos

A página possui animações para os cliques, números de pontos que aparecem na tela, combos, desbloqueios, melhorias e prestígio. O prestígio possui ainda um efeito especial com brilho, texto e partículas.

O sistema também utiliza mecanismos para evitar atualizações desnecessárias da interface e limitar a quantidade de elementos visuais criados simultaneamente, ajudando a manter a página mais fluida.

Em conjunto, a página transforma o aprendizado sobre a história das mulheres em uma experiência de progressão: o usuário acumula Pontos de História, melhora sua produção, desbloqueia novos períodos históricos e utiliza o sistema de prestígio para construir um Legado Permanente.

e eu programei todo esse projeto no A16 5g da samsung, e nas primeiras versoes, onde nem tinha os recursos, ja programei ele num app de A-IDE, num A05, e eu ja perdi vários projetos porque o celular nao aguentava, matava o projeto porque matou o processo de compilação!, e uma vez eu fiz o projeto do mulher amparada e eu mesmo fiz o app do mulher amparada (primeiro eu refiz, depois na 2 vez que perdi portei tudo do apk compilado para descompilado, e depois perdi denovo mas ai eu ja tinha o código-fonte!)

e a calculadora de disfarce realmente faz contas, se digitar:

2 + 2, aparece 4!

e se digitar a senha ele desbloqueia...

----

# Decisões técnicas:

lembre-se que hoje em dia uso github para compilar os apps e o a16 5g da samsung, então ele nao mata o processo de compilação mais!

Porque usei webview e html?

porque ele e mais fluido, e também deixa o aplicativo mais leve em tamanho, um exemplo disso e o instagram lite, e porque nao precisa gerar muitos arquivos XMLs ou muito texto em kotlin para fazer todas as telas e além disso html com WebView é mais difícil de manter, mas eu tenho sim activitys em kotlin em xml, mas eu também tenho páginas em html, (mas não necessariamente o webview é mais leve universalmente!)

ah, mas o webview carrega uma versão cromium inteira...

e as activitys em kotlin e jetpack compose ou xml carrega imports do build, muitos arquivos e muitos textos para algo que dá para ser feito facilmente em html, e para determinadas telas, HTML/CSS/JavaScript permite implementar a interface com menos código específico de Android..., 

Mas a proposta e ele ser otimizado para ser mais rápido!

E também o webview chama metodos expostos via js que chama o android

, e sobre injeção de código, o app guarda os dados usando criptografia..., mas isso não garante que xss aconteça, mas eu só estou dizendo que ele guarda texto do diário por exemplo em criptografia, mas isso poderá acontecer como em qualquer outro app em certas condições...

e TODAS AS PÁGINAS EM HTML, estão dentro da pasta assets (menos o navegador que usa o Google, mas aí eu não controlo e é com eles lá!)



E o webview do app está assim!:

```kt

val settings = webView.settings  

webView.overScrollMode = View.OVER_SCROLL_NEVER  

  webView.isVerticalScrollBarEnabled = false

webView.isFocusable = true
webView.isFocusableInTouchMode = true
webView.setOnFocusChangeListener { _, _ -> }

webView.isHorizontalScrollBarEnabled = false

webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

settings.javaScriptEnabled = true

settings.mediaPlaybackRequiresUserGesture = false

settings.domStorageEnabled = true

settings.setGeolocationEnabled( true )

settings.allowFileAccess = true

settings.allowContentAccess = false

settings.allowFileAccessFromFileURLs = false

settings.allowUniversalAccessFromFileURLs = false

settings.javaScriptCanOpenWindowsAutomatically = false

settings.setSupportMultipleWindows( false )

```

Por que, na FileActivity, o botão para trocar o tipo de armazenamento é unificado em um só?

O botão foi unificado para tornar a navegação entre os diferentes tipos de armazenamento mais simples e organizada, especialmente em situações de pânico ou urgência.

Em vez de apresentar vários botões separados, o mesmo botão alterna sequencialmente entre os armazenamentos disponíveis, seguindo o ciclo 1 → 2 → 3 → 1.

Essa escolha também foi pensada como uma forma de incentivar uma vistoria sequencial. Ao passar por cada armazenamento antes de retornar ao primeiro, a usuária é estimulada a verificar diferentes locais onde arquivos importantes podem estar, reduzindo a possibilidade de deixar algum armazenamento sem ser conferido por distração ou pressa.

é tipo assim:

armazenamento interno, para trocar para cartao sd, clica no botão e troca o icone, mas aí para voltar atrás, tem passar pelo cartao sd e pelo pendrive para só então voltar para o armazenamento interno

Por que usei a permissão de Administrador do dispositivo e não um Serviço de acessibilidade?

Optei pelo Administrador do dispositivo em vez de um Serviço de acessibilidade porque, para a função específica que o aplicativo precisa realizar — bloquear imediatamente a tela do celular — o Administrador do dispositivo oferece um mecanismo próprio do Android para essa finalidade, por meio do "DevicePolicyManager.lockNow()".

No caso de um Serviço de acessibilidade, é necessário habilitar o serviço nas configurações de Acessibilidade do Android, seguindo o fluxo de permissões e confirmações exigido pelo sistema. Além disso, o uso de Acessibilidade envolve requisitos e políticas próprios dessa plataforma.

Já o Administrador do dispositivo possui um fluxo específico para sua ativação: o usuário concede a função de administrador ao aplicativo e, depois disso, o aplicativo pode utilizar as capacidades de administração que foram concedidas.

É importante esclarecer uma diferença: as chamadas “permissões restritas” do Android não são simplesmente uma consequência de usar Administrador do dispositivo. Elas são um mecanismo separado do sistema. Dependendo da versão do Android e da forma como o aplicativo foi instalado, determinadas configurações podem aparecer durante o gerenciamento ou a desinstalação do aplicativo.

E o fato de o Administrador do dispositivo estar sendo descontinuado?

É verdade que várias funcionalidades tradicionais do Device Administrator foram descontinuadas ou deixaram de ser recomendadas pelo Android. Porém, isso não significa que toda a API tenha sido removida.

A função utilizada pelo aplicativo para essa finalidade é:

"DevicePolicyManager.lockNow()"

Ela é destinada a bloquear imediatamente o dispositivo, e não a desligá-lo.

Portanto, a documentação do projeto não deve dizer que “o Administrador do dispositivo está totalmente deprecated”. O correto é explicar que algumas capacidades tradicionais do Device Administrator foram descontinuadas/restritas, enquanto o bloqueio imediato da tela utilizado pelo aplicativo continua sendo uma capacidade disponível do Android.

O aplicativo utiliza somente a capacidade necessária para o seu mecanismo de proteção, sem depender das funções administrativas antigas, como alteração de senha ou outras políticas que foram descontinuadas.

----

## Bibliografia

- https://www.metmuseum.org/-/media/files/learn/for-educators/publications-for-educators/the-art-of-ancient-egypt.pdf

- https://www.britishmuseum.org/blog/mary-beards-top-five-powerful-women-ancient-greece-and-rome

- https://www.worldhistory.org/trans/pt/2-2081/mulheres-na-antiga-mesopotamia/

- https://www.worldhistory.org/trans/pt/2-927/as-mulheres-na-grecia-antiga/

----