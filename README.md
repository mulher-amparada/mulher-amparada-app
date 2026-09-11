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

> ⚠️Vale lembrar que o Gerenciador De Arquivos Do Mulher Amparada na verdade ele so visualiza, e ele tem esse nome porque isso faz parte do disfarce do app, e ele so visualiza para a seguranca e nao apagar provas, e para não apagar algo importante acidentalmente!

> ⚠️E também quando for clicar em qualquer botão de apagar no app, ele pode apagar TODOS OS CONTEÚDOS criptografados de TODO O APP, então CUIDADO!

## Sobre como o aplicativo é compilado:

e os apps são compilados com o workflow, gera o código sha-256 e o zip com o app, envia para a release pela tag correspondente, e atualiza o link das páginas de download, e o tamanho do apk dessas páginas!

no repositório tem um yml que ativa todos os dias, procura no codigo fonte dos 3 apps o arquivo build do módulo do app, e se ele ver que saiu uma versão android nova, ele atualiza, totalmente sozinho!, garantindo que elas tenham os apps mais recentes possíveis!

e todas as activitys tem o android exported="false", menos a MainActivity porque a partir das versões mais recentes do android, a tela inicial do app tem que estar obrigatoriamente com android exported="true"

E nao faz sentido usar proguard8 
pois o app tem o código-fonte publico

E ele tem uma chave que assina os apps, usando essas 4 informações:

KEYSTORE_BASE64
KEYSTORE_PASSWORD
KEY_ALIAS
KEY_PASSWORD

Em:

/settings/secrets and variables/actions/repository secrets/

## Sobre como eu automatizo o projeto:

E eu também já consegui configurar um ssh na conta, e 2fa nela tambem, e com o ssh, eu consegui mover pastas inteiras para o repositório, e transformei 4 em 1, e mais de 100 commits em 1,

e agora a pagina la no GitHub pages atualiza a cada 1 hora

e no site, ele conta com 3 botoes que ligam pro 192, 190 e 180 (ai voce escolhe), e eles usam o "tel:" do navegador

e os contatos de confiança, quando são cadrastados eles também são criptografados!

e o bloqueio do celular por barulho é assim: bloqueia, voce desbloqueia e pra ter denovo tem que desativar e ativar denovo!

---- 

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

**Desligar o celular pelo barulho:**
ao ativado, ao fazer barulho alto, ele usa o administrador do dispositivo e desliga o celular!, e o melhor e que da para ativar essa e a proteção por barulho ao mesmo tempo!, (sim, eu testei isso no dia 06/09/2026, e não só isso, se o agressor jogar o celular e ele não quebrar, a proteção por barulho se tiver ativa em teoria pode iniciar a ligação pro 180!)

----

**Emergência:**
Saindo dessa área, existem botões que abrem o aplicativo nativo do telefone nos números 190, 192 e 180.

----

**Compartilhamento Rápido de Localização:**
Além disso, existe um botão dentro do app que pega a localização atual, monta um link do Google Maps e já manda para o WhatsApp do 180, precisando apenas clicar no botão de enviar.

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
Mostra um mapa da região da usuária, com funções que dá para ver onde ela está

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

Todas as tarefas são salvas diretamente no navegador do usuário.

Os dados ficam armazenados localmente no dispositivo do usuário.

----

**Gravador de voz:**
Usando uma activity (uma tela) em kotlin, é possivel ter um gravador de voz no app, sendo possível registrar evidências e provas, além do que a usuária quiser, sempre usando permissoes android e com o consentimento da usuária!

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

**Meus arquivos:**
Dentro do app, ele só visualiza as pastas e ao clicar em um arquivo, abre um seletor de apps para executar/visualizar ele

e ele tem um botão que troca entre armazenamento interno e cartão sd e pendrive

----

**Desligar o celular:**
Ao tocar neste botão, o aplicativo solicitará a permissão de Administrador do dispositivo, caso ela ainda não tenha sido concedida. Quando essa permissão estiver ativa, o aplicativo poderá bloquear imediatamente a tela do dispositivo, (aviso: possivelmente você não poderá usar a sua biometria!)

----

**Tela de aplicativos:**
Ao tocar neste botão, o app mostrará um site dentro do app que lista todos os outros apps com a permissão query all packpages...

----

## FUNÇÕES AVANÇADAS DA ÁREA PROTEGIDA!:

**lembrando que, sobre esses apps, ja tem o código fonte dele dentro do repositório no site!:**

**Lembrando que o app de gerenciador de arquivos so visualiza arquivos  nao copia, nao exclui, nao move e nao renomeia, e ele também precisa desbloquear com biometria**

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

----

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

e a calculadora falsa realmente faz contas, se digitar:

2 + 2, aparece 4!

e se digitar a senha ele desbloqueia...

----

# Decisões técnicas:

lembre-se que hoje em dia uso github para compilar os apps e o a16 5g da samsung, então ele nao mata o processo de compilação mais!

Porque usei webview e html?

porque ele e mais leve, sim ele e mais leve sim, um exemplo e o instagram lite, e porque nao precisa gerar muitos xml ou muito texto em kotlin para fazer todas as telas e além disso html com WebView é mais difícil de manter, mas eu tenho sim activitys em kotlin em xml, mas eu também tenho páginas em html

ah, mas o webview carrega uma versão cromium inteira...

e as activitys em kotlin e jetpack compose ou xml carrega imports do build, muitos arquivos e muitos textos para algo que dá para ser feito facilmente em html, e o html acaba sendo mais leve e por isso mais otimizado..., a proposta e ele ser otimizado para ser mais rápido, e o webview chama metodos expostos via js que chama o android, e sobre injeção de código, o app guarda os dados usando criptografia..., e TODAS AS PÁGINAS EM HTML, estão dentro da pasta assets (menos o navegador que usa o Google, mas aí eu não controlo e é com eles lá!)

Porque usei a permissão de Administrador do dispositivo e não serviço de acessibilidade?

porque os serviços de acessibilidade para os ativar precisa ir para a tela de acessibilidade e conceder as permissões restritas, enquanto o administrador do dispositivo ele só precisa ativar e as permissões restritas só acontece quando o desativa!, exceto quando desinstala o app quando clica em informações do app!

e bem verdade que ele está ficando deprecated, mas também é verdade que a parte de bloquear a tela não esta deprecated, o resto sim (alterar a senha e etc)

E o webview do app está assim!:

        val settings =  
            webView.settings  

            webView.overScrollMode = View.OVER_SCROLL_NEVER  

  webView.isVerticalScrollBarEnabled =
    false

webView.isFocusable = true
webView.isFocusableInTouchMode = true
webView.setOnFocusChangeListener { _, _ -> }

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

// Necessário para o seu uso com android_asset
settings.allowFileAccess =
    true

// Não é necessário para as páginas locais
settings.allowContentAccess =
    false

// Evita que páginas file:// acessem outros file://
settings.allowFileAccessFromFileURLs =
    false

// Evita que páginas file:// acessem outras origens
settings.allowUniversalAccessFromFileURLs =
    false

// Reduz abertura automática de novas janelas
settings.javaScriptCanOpenWindowsAutomatically =
    false

settings.setSupportMultipleWindows(
    false
)

----