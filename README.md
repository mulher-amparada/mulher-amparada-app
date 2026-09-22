<div align="center">
  <img src="user1.png" alt="Logo Mulher Amparada" width="200" height="200">
</div>

<div align="center">
  <h1>Mulher Amparada</h1>
  </div>

Um projeto totalmente gratuito e livre de anúncios, projetado por um menino autista nível 1 de 15 anos!, usando o apoio do chatgpt, sem curso formal!

E saibam que o projeto é: Source-Available

**Commits totais de toda a história do projeto, (feitos por mim e pelos workflows do github actions!) = 2929**

# ⚠️MURAL DE AVISOS:

### Sobre como o projeto foi estruturado:

>Sobre as permissões: infelizmente, foi necessário configurar a MainActivity para não solicitar permissões automaticamente. Por isso, as permissões necessárias deverão ser concedidas manualmente pela usuária nas configurações do dispositivo. As permissões utilizadas por outras Activitys continuam sendo solicitadas normalmente pelo aplicativo.
>
> Ao entrar no aplicativo após sair dos disfarces, um aviso é exibido, bloqueando o acesso até que todas as permissões necessárias estejam concedidas. O aviso oferece à usuária a opção de acessar diretamente a tela de permissões do aplicativo nas configurações do dispositivo. Ao retornar ao aplicativo, as permissões são verificadas novamente. Se ainda houver alguma permissão necessária que não tenha sido concedida, o aviso continuará sendo exibido e o acesso permanecerá bloqueado. O aviso só desaparecerá quando todas as permissões necessárias estiverem concedidas.
>
> (mas a permissão de Administrador do dispositivo a acitvity pede normalmente!).
 
>Vale lembrar que o projeto não substitui serviços oficiais do governo e também não garante segurança imediata, bem como as funções dependem do estado e hardware de cada aparelho!

> Vale lembrar: o Gerenciador de Arquivos do Mulher Amparada funciona principalmente como um visualizador de arquivos. O nome “Gerenciador de Arquivos” também faz parte do disfarce do aplicativo. Ele foi projetado dessa forma por uma questão de segurança: o aplicativo não oferece funções próprias para excluir, mover, copiar ou renomear arquivos, reduzindo o risco de apagar ou alterar acidentalmente algum arquivo importante — inclusive possíveis registros que a usuária queira preservar.

### Sobre as funções do projeto:

>Ao utilizar a função de baixar o histórico de ajuda, o aplicativo gera um único arquivo no formato JSON. Os dados utilizados para gerar esse histórico são obtidos por meio das APIs protegidas do Android, que controlam o acesso ao histórico de chamadas por meio das permissões do sistema. Por isso, o aplicativo não aplica a classe "Cripto" novamente durante a exportação. O arquivo JSON exportado, entretanto, não é um arquivo criptografado. Ele é salvo como um arquivo comum na área de downloads do dispositivo. A proteção de acesso fornecida pelo Android ao histórico de chamadas não significa que o arquivo exportado permaneça criptografado. O arquivo também contém hashes SHA-256, utilizados para verificação de integridade dos dados. SHA-256 não é um mecanismo de criptografia e não substitui a criptografia para proteção do conteúdo.

>ATENÇÃO: Reforço que as proteções que utilizam sensores podem não funcionar corretamente em alguns aparelhos, dependendo das limitações ou características do hardware da usuária.

>Botão de pânico: caso o aplicativo não possua a permissão necessária para realizar chamadas diretamente, ele utiliza o "ACTION_DIAL" como alternativa, abrindo o discador com o número de emergência. Dessa forma, o recurso continua disponível mesmo sem a permissão de chamada.

> Sobre as proteções por movimento e escurecimento: caso ocorra alguma falha ou o aparelho da usuária não possua o sensor necessário, o aplicativo utiliza o microfone como alternativa. Ao detectar um barulho alto, a proteção é acionada.

> **Recurso de Privacidade: Escurecimento por Inclinação (Disfarce Rápido)**
>
>O aplicativo conta com uma funcionalidade exclusiva de privacidade, projetada para proteger as informações da usuária contra olhares curiosos. Ao inclinar o dispositivo, o aplicativo ativa instantaneamente um modo de disfarce visual, escurecendo a interface para simular que a tela está desligada ou que o celular está bloqueado.
>
>#### Funcionamento Exclusivo na Tela Principal (MainActivity)
>
>Por decisões estratégicas de segurança, desempenho e utilidade prática, este recurso opera **estritamente dentro da tela principal do aplicativo**, não sendo ativado nas demais telas ou no momento em que o app está abrindo. As razões para essa escolha incluem:
>
>* **Velocidade Máxima no Socorro:** A tela de entrada do aplicativo precisa abrir o app o mais rápido possível. Ligar os sensores de movimento do celular logo na inicialização causaria um atraso na abertura, o que comprometeria o pedido de ajuda em situações de urgência extrema.
>* **Economia Extrema de Bateria:** Os sensores de inclinação consomem energia do dispositivo. Ao restringir o uso do sensor apenas para a tela principal (onde a usuária passa a maior parte do tempo), o aplicativo evita o desgaste desnecessário da bateria e impede que o sensor continue rodando quando o aplicativo for fechado ou minimizado.
>* **Proteção Focada onde Importa:** A tela principal é o local que concentra as informações realmente sensíveis do projeto (como finanças e anotações). Telas de transição ou de carregamento não exibem dados confidenciais, eliminando a necessidade de um disfarce visual nessas etapas.

>Aviso — O recurso de “Desembarque seguro” do Mulher Amparada é informativo e atualmente apresenta a legislação aplicável à Cidade de São Paulo, especialmente a Lei Municipal nº 16.490/2016 e sua regulamentação. Essa legislação não deve ser interpretada como uma regra válida em todo o Brasil. As regras sobre desembarque fora dos pontos podem variar conforme o município, o estado e o tipo de transporte. A carteirinha apresentada pelo aplicativo não é um documento oficial e não substitui a legislação vigente, regulamentações, orientações das empresas de transporte ou autoridades competentes. E antes de utilizar esse recurso em outra localidade, verifique a legislação específica aplicável ao local.

>Sobre a proteção por palmas/barulho: Nota de Segurança: Uma vez ativada, a proteção permanecerá vigilante e reativará o microfone automaticamente após cada detecção e ligação pro 180. Isso garante que o aplicativo continue te protegendo caso a situação de risco persista. Para desligá-la por completo, você deve fazer isso manualmente no aplicativo após o término da situação de risco.

> Sobre as funções por sensores (bloquear a tela por barulho, proteção por barulho que liga pro 180, balançar o celular para pedir ajuda e escurecimento por inclinação): A função Bloquear a tela por Barulho foi projetada com uma medida adicional de segurança: depois de ser ativada e funcionar de fato, ela permanece ativa até que o usuário decida desativá-la pelo próprio aplicativo. Para evitar que o estado da função seja perdido ao sair e retornar à página, o aplicativo salva seu estado de ativação utilizando a classe Cripto. Dessa forma, ao retornar à página, o aplicativo recupera o estado salvo e exibe corretamente se a função está ATIVADA ou DESATIVADA. Essa persistência permite que a interface continue refletindo o estado real configurado pelo usuário, mesmo após a navegação entre as páginas do aplicativo. Mas quando a usuária desativar ou a função ser concluída, na página ele mostra como desativado!

>O Gerenciador de Arquivos (que apenas visualiza) não acessa os arquivos internos do Gravador de Voz do Mulher Amparada nem os dados ou recursos internos de outras funcionalidades do aplicativo. Ele trabalha com arquivos que já estão disponíveis nos armazenamentos do dispositivo e que podem ser acessados pelos mecanismos de armazenamento autorizados pelo Android.

>O aplicativo possui um navegador interno. A navegação para o Google é feita diretamente pelo código usando window location replace(), sem disponibilizar o endereço como um link na interface. O aplicativo também não implementa um sistema próprio de registro de histórico de navegação, (ou pelo ou menos eu não coloquei na página)

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

### Documentação e conteúdo:

>O conteúdo dos arquivos `about.md`, `LEIS.md` e `FINANÇAS.md` está disponível tanto no site publicado pelo GitHub Pages quanto dentro da área protegida do aplicativo.
>
>Dessa forma, essas informações podem ser consultadas diretamente pelo site ou pelo próprio aplicativo, mantendo o conteúdo acessível nos dois ambientes.

# 🏗Estrutura do projeto:

### Sobre como eu automatizo o projeto:

e os apps são compilados com o workflow, gera o código sha-256 e o zip com o app, envia para a release pela tag correspondente, e atualiza o link da página de download, o código SHA-256, e o tamanho do apk dessas páginas!, e depois ele publica as alterações no site, e depois altera o valor donúmero de commits que é informado no readme, (tudo isso em um único workflow!)

E eu também já consegui configurar um ssh na conta, e 2fa nela tambem, e com o ssh, eu consegui mover pastas inteiras para o repositório, e transformei 4 em 1, e mais de 100 commits em 1,

e os contatos de confiança, quando são cadrastados eles também são criptografados!

### Sobre as atualizações do app:

O app já está na versão:

`🌸33 (versão final)`

ele tem o TargetSdkVersion 37

ele tem o CompileSdkVersion 37

Versão agp no toml de 9.4.0 e versão kotlin é a 2.4.20

e ele tem o jetpack compose (embora no app só usei xml até agora, mas ele está ativado!) e estilo via xml ativados!


## Sobre como o aplicativo é compilado:

Proteção da Activity de Entrada:

O aplicativo está configurado para iniciar pela EntradaActivity, que é a única Activity com android:exported="true" por possuir o intent-filter de inicialização (MAIN e LAUNCHER).

A MainActivity permanece com android:exported="false", impedindo que outros aplicativos iniciem essa Activity diretamente por meio de uma Intent externa. Todas as demais Activities do aplicativo também estão configuradas com android:exported="false".

Dessa forma, a EntradaActivity funciona como uma camada de entrada: ela gerencia a inicialização visual do aplicativo e encaminha o usuário de forma segura para a MainActivity internamente. 

E a EntradaActivity tem o fundo preto!

Essa configuração reduz a exposição direta das Activities internas a inicializações externas. Ela não impede a análise do APK ou de seus arquivos por ferramentas de engenharia reversa.

Proteção do Código

O aplicativo utiliza o **R8**, o sistema oficial de otimização, redução e ofuscação integrado ao *Android Gradle Plugin*, para aplicar proteção ao código-fonte na compilação da versão de lançamento (*release build*).

O R8 reduz o tamanho do aplicativo e aplica a ofuscação no código compilado, substituindo nomes de classes, métodos e variáveis por caracteres genéricos. Isso dificulta significativamente a leitura e a engenharia reversa do código por meio de ferramentas de descompilação.

*Nota: Esta proteção aumenta a barreira contra análise estática, mas não torna o APK completamente imune à engenharia reversa.*

E o fundo do icone do app é um adaptativo, em que o fundo e preto, e tem um bonequinho em cores azuis correndo, e o ic_launcher_foreground na pasta res/drawable, e os ic_launcher em cada mipmap tem o fundo transparente, eo ic_launcher_background também na pasta res/drawable é um quadrado preto

e quando você clica no botão voltar na MainActivity, ele volta a página!

e o fundo de todas as telas são nativamente pretas!

E o workflow utiliza um keystore de assinatura armazenado de forma protegida nos GitHub Actions Secrets. As informações necessárias para acessar o keystore e selecionar a chave são fornecidas pelas variáveis KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS e KEY_PASSWORD.

Em:

/settings/secrets and variables/actions/repository secrets/

E também as barras tanto de status tanto de navegação são transparentes, porém o fundo atrás do WebView e preto, espaçado dos lados e de cima e com um raio de borda!, e o webview não fica mais embaixo das duas barras, ele respeita elas!

### Sobre como foi escrito o texto do biometricPrompt do app:

- MainActivity (tela que contém o webview):

**Título:**
>Desbloquear a área protegida

**Descrição:**
>🌸 Apenas a usuária cadastrada pode acessar este local

O método de autenticação é definido pelo próprio Android de acordo com os autenticadores disponíveis no dispositivo, utilizando "BIOMETRIC_WEAK" e "DEVICE_CREDENTIAL".

# ⚒️Todas as funções do aplicativo!:

### Ícone Monocromático e Integração com Material You:

O aplicativo possui suporte aos ícones temáticos do Android (Themed Icons), permitindo que seu ícone se adapte visualmente à paleta de cores dinâmica definida pelo sistema.

* Máscara Monocromática: O aplicativo fornece uma versão monocromática específica do ícone para que o Android possa utilizá-la quando os ícones temáticos estiverem disponíveis e ativados no dispositivo.

* Adaptação à Paleta do Sistema: Em vez de utilizar uma cor fixa definida pelo aplicativo, o Android pode aplicar a paleta dinâmica escolhida para o dispositivo ao ícone temático. Dessa forma, o ícone acompanha visualmente as cores utilizadas pelo restante da interface.

* Integração Visual: A utilização do sistema de ícones temáticos permite que o aplicativo mantenha uma aparência mais integrada à tela inicial, acompanhando o padrão visual adotado pelo próprio Android.

O Impacto Visual: O ícone deixa de depender exclusivamente de suas cores originais e passa a responder à personalização visual do sistema. Isso proporciona uma apresentação mais discreta e consistente com a interface do dispositivo, sem que o aplicativo precise criar manualmente uma versão diferente para cada paleta de cores.

### Disfarce do app (assistente de saúde falso!):
tutorial: ao entrar no app, clique no canto superior direito com o icone de calculadora. e ai quando ele for iniciado, ele pedirá para criar uma senha (e salva em uma classe kt de criptografia), assim so acessa com a senha informada, para resetar essa senha (dê 5 toques em menos de 2 segundos, e digite como você gosta de ser chamada, e digite sua nova senha!), mas antes dessa tela, tem outra tipo uma gaveta de apps..., porém, agora no mulher amparada, ele já vem com o icone de calculadora e o nome calculadora, só dá para mudar o icone, ou seja, o app ja vem com icone de (Assistente de saúde), uma tela genérica de elementos de medição de saúde (bpm e etc), e vale lembrar que:

Os dados da primeira página do app são meramente fictícios e não representam informações reais!

e tambem, reforcando que no canto superior direito tem um icone de calculadora que quando clica vai pra uma calculadora e aparece o disfarce de calculadora 

### Botão de Pânico:
Botão de Pânico, com ligação ao 180 de forma direta no primeiro clique.

### Proteção por Barulho:
Ative a proteção, faça barulho alto e ele liga para o 180.

caso ocorra alguma falha ou o aparelho da usuária não possua o sensor necessário, o aplicativo utiliza o microfone como alternativa. Ao detectar um barulho alto, a proteção é acionada.

### Balançar o Celular para Pedir Ajuda:
Ative e, ao chacoalhar o celular, ele liga para o 180.

### Escurecimento por inclinação:
com isso, voce pode controlar o brilho da tela clicando em um botão..., porém, e tipo como se fosse o menor brilho do celular, e ai depois ele deixa a tela preta (nao com brilho e sim colocando a cor), (honestamente, antes aparecia as duas barras, agora elas se escondem!), e o efeito e vitalicio ate fechar e abrir o app!

###Bloquear a tela do celular pelo barulho:
ao ativado, ao fazer barulho alto, ele usa o administrador do dispositivo e bloqueia a tela do celular!

###Emergência:
Saindo dessa área, existem botões que abrem o aplicativo nativo do telefone nos números 190, 192 e 180.

###Compartilhamento Rápido de Localização:
Além disso, existe um botão dentro do aplicativo que obtém a localização atual, monta um link do Google Maps com as coordenadas e abre uma conversa no WhatsApp do 180 com a mensagem preparada. O envio não é automático: é necessário apenas conferir a mensagem e tocar no botão de enviar.

### Contatos de Confiança:
Além dos contatos de confiança, clicando no primeiro botão você seleciona e salva o contato. O botão abaixo envia um pedido de ajuda para ele.

# 🔐Área Protegida:
Se estiver cadastrado no celular, com Biometric Prompt junto com Device Credential e autenticação weak, pode desbloquear essa área com impressão digital, rosto, PIN, padrão, senha e outros métodos.


### Sistema Cripto (Segurança do App) = Antigo LocalStorage!:
Este sistema salva dados de forma segura usando criptografia nativa do Android.

```
Como funciona:
Quando você salva um dado no app, ele não fica em texto normal no celular. Ele é automaticamente criptografado antes de ser armazenado.

Isso significa que mesmo acessando os arquivos do dispositivo, os dados aparecem como códigos ilegíveis.
```

```
Tecnologia usada:

AES-256 (criptografia forte)

Android Keystore (chave protegida pelo sistema)

EncryptedSharedPreferences
```

```
O que cada função faz:

salvar(chave, valor) → guarda o dado de forma criptografada

carregar(chave) → recupera o dado original

remover(chave) → apaga um dado específico

limparTudo() → remove todos os dados salvos
```
```
Segurança:
Os dados são protegidos por uma chave segura do próprio Android e não ficam visíveis diretamente no armazenamento do aparelho.
```

###Calendário Menstrual:
Registre como dói cada dia e, com isso, o aplicativo monta um calendário.

###Calendário de eventos:
Registra eventos da usuária quando ela precisar

###Rotina:
Sistema de pontos, com registro de comidas e bebidas boas e ruins, bem como a adição de registro de exercícios físicos fáceis, médios ou difíceis e contagem de tempo de cada um deles, além de sistema de nível e conquistas.

###Mapa:
Mostra um mapa da região e, quando a localização estiver disponível e autorizada, permite visualizar a posição atual.

###Diário:
Usando criptografia, a usuária poderá anotar o que quiser. Com a senha, ficará seguro e também não some, pois estará guardado.

###Relógio:
Mostra o mapa do local atual, o país e outros dados, bem como o ano, semestre, bimestre, mês, quinzena, semana, dia, hora, minuto e segundo.

###Calculadora:
A calculadora pode ser usada para cálculos rápidos do dia a dia.

###Tarefas:
O sistema permite categorizar tarefas em áreas como estudos, trabalho, pessoal e saúde.

As tarefas podem ser marcadas como concluídas para acompanhamento do progresso.

Todas as tarefas são salvas diretamente no webview da usuária.

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

###Bloquear a tela do celular:
Ao tocar neste botão, o aplicativo solicitará a permissão de Administrador do dispositivo, caso ela ainda não tenha sido concedida. Quando essa permissão estiver ativa, o aplicativo poderá bloquear imediatamente a tela do dispositivo, (aviso: depois que a tela é bloqueada pelo sistema, o Android pode exigir novamente o método de credencial do dispositivo antes de permitir determinadas formas de autenticação biométrica.)

###Tela de aplicativos:
Ao tocar neste botão, o app mostrará um site dentro do app que lista todos os outros apps com a permissão query all packpages...

### História das Mulheres:

```
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
```

### Navegador: 

A navegação para o Google é feita diretamente pelo código usando window location replace(), sem disponibilizar o endereço como um link na interface. O aplicativo também não implementa um sistema próprio de registro de histórico de navegação, (ou pelo ou menos eu não coloquei na página)

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

# Decisões técnicas:

lembre-se que hoje em dia uso github para compilar os apps e o a16 5g da samsung, então ele nao mata o processo de compilação mais!

### Porque usei webview e html?

porque ele e mais fluido, e também deixa o aplicativo mais leve em tamanho, um exemplo disso e o instagram lite, e porque nao precisa gerar muitos arquivos XMLs ou muito texto em kotlin para fazer todas as telas e além disso html com WebView é mais difícil de manter, mas eu tenho sim activitys em kotlin em xml, mas eu também tenho páginas em html, (mas não necessariamente o webview é mais leve universalmente!)

ah, mas o webview carrega uma versão cromium inteira...

e as activitys em kotlin e jetpack compose ou xml carrega imports do build, muitos arquivos e muitos textos para algo que dá para ser feito facilmente em html, e para determinadas telas, HTML/CSS/JavaScript permite implementar a interface com menos código específico de Android..., 

Mas a proposta e ele ser otimizado para ser mais rápido!

E também o webview chama metodos expostos via js que chama o android

### Sobre injeção de código:

o app guarda os dados usando criptografia..., mas isso não garante que xss aconteça, mas eu só estou dizendo que ele guarda texto do diário por exemplo em criptografia, mas isso poderá acontecer como em qualquer outro app em certas condições..., e também eu uso o proguard8

### Sobre o WebView do app:

e TODAS AS PÁGINAS EM HTML, estão dentro da pasta assets (menos o navegador que usa o Google, mas aí eu não controlo e é com eles lá!)

E o webview do app está assim!:

```kt

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

```kt

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

### Por que usei a permissão de Administrador do dispositivo e não um Serviço de acessibilidade?

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

### Por que o WebView do app não trata "intent" para abrir outros aplicativos?

Por questões de segurança e privacidade.

Permitir que páginas dentro do WebView utilizem "intent" livremente para abrir outros aplicativos ou executar ações externas poderia aumentar os riscos de comportamentos inesperados, abuso de links e rastreamento de usuários.

Links também podem conter parâmetros de rastreamento, redirecionamentos e outros mecanismos capazes de identificar ou acompanhar a navegação.

Por isso, o WebView mantém esse comportamento limitado. A exceção é a tela de aplicativos, que possui uma função específica e um fluxo controlado pelo próprio app.

### Sobre o tema escuro, o conforto visual e a discrição:

O projeto utiliza fundos escuros em todas as páginas da interface, independentemente do tipo de tela utilizado pelo dispositivo. Essa escolha busca manter uma experiência visual consistente, confortável e com menor brilho geral, evitando o uso desnecessário de grandes áreas brancas.

A interface também utiliza uma variedade de cores nos elementos, em vez de depender exclusivamente do contraste entre preto e branco. Isso não significa que textos brancos sejam evitados: eles continuam presentes quando são apropriados e necessários para garantir boa legibilidade. As cores são utilizadas para criar hierarquia visual, destacar informações e tornar a interface mais equilibrada e agradável.

O tema escuro também considera o contexto de uso do aplicativo. Em situações nas quais a usuária precisa pedir ajuda ou utilizar o aplicativo de forma discreta, telas muito claras e brilhantes podem chamar atenção desnecessariamente. Por isso, a interface mantém uma aparência escura, discreta e confortável, sem comprometer a legibilidade.

## Sobre as animações e o desempenho:

Todas as páginas sob controle do projeto implementam suporte a `prefers-reduced-motion`, permitindo que as animações sejam desativadas automaticamente quando essa preferência estiver habilitada no dispositivo. A página `navegador.html` é uma exceção, pois utiliza `window.replace` e pode carregar páginas externas que não são controladas pelo projeto.

Além disso, as animações foram amplamente reduzidas, principalmente as animações de entrada. O projeto prioriza transições rápidas e discretas, mantendo apenas algumas animações pontuais quando elas contribuem para a experiência de uso. Dessa forma, a interface permanece visualmente agradável sem comprometer a agilidade e a responsividade do aplicativo.

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

### Bibliografia

- https://www.metmuseum.org/-/media/files/learn/for-educators/publications-for-educators/the-art-of-ancient-egypt.pdf

- https://www.britishmuseum.org/blog/mary-beards-top-five-powerful-women-ancient-greece-and-rome

- https://www.worldhistory.org/trans/pt/2-2081/mulheres-na-antiga-mesopotamia/

- https://www.worldhistory.org/trans/pt/2-927/as-mulheres-na-grecia-antiga/
