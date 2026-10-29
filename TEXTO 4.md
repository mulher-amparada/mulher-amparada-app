# ⚒️Todas as funções do aplicativo!:

### Ícone Monocromático e Integração com Material You:

O aplicativo possui suporte aos ícones temáticos do Android (Themed Icons), permitindo que seu ícone se adapte visualmente à paleta de cores dinâmica definida pelo sistema.

* Máscara Monocromática: O aplicativo fornece uma versão monocromática específica do ícone para que o Android possa utilizá-la quando os ícones temáticos estiverem disponíveis e ativados no dispositivo.

* Adaptação à Paleta do Sistema: Em vez de utilizar uma cor fixa definida pelo aplicativo, o Android pode aplicar a paleta dinâmica escolhida para o dispositivo ao ícone temático. Dessa forma, o ícone acompanha visualmente as cores utilizadas pelo restante da interface.

* Integração Visual: A utilização do sistema de ícones temáticos permite que o aplicativo mantenha uma aparência mais integrada à tela inicial, acompanhando o padrão visual adotado pelo próprio Android.

O Impacto Visual: O ícone deixa de depender exclusivamente de suas cores originais e passa a responder à personalização visual do sistema. Isso proporciona uma apresentação mais discreta e consistente com a interface do dispositivo, sem que o aplicativo precise criar manualmente uma versão diferente para cada paleta de cores.

### Disfarce do app (assistente de saúde falso!):

ao entrar pela primeiríssima vez no app, aparece a WelcomeActivity, que é uma tela de boas-vindas para o "Assistente UI 2", ajudando a disfarçar  ainda mais!

tutorial: ao entrar no app, clique no canto superior direito com o icone de calculadora, isso é o disfarce do assistente de saude,  ao arrastar para o lado na `AssistenteActivity`, o app solicita o `BiometricPrompt`; quando validado, leva para a `HubActivity`.

e ai quando ele for iniciado, voce precisará tocar no visor 5 vezes para cadastrar a senha e a pergunta de recuperação (e ele salva em uma classe kt de criptografia), assim so acessa com a senha informada, para resetar essa senha (dê 5 toques em menos de 2 segundos, e digite como você gosta de ser chamada, e digite sua nova senha!), mas antes dessa tela, tem outra tipo uma gaveta de apps..., porém, agora no mulher amparada, ele já vem com o icone de calculadora e o nome calculadora, só dá para mudar o icone, ou seja, o app ja vem com icone de (Assistente de saúde), uma tela genérica de elementos de medição de saúde (bpm e etc), e vale lembrar que:

Os dados da primeira página do app (disfarce do assistente de saúde) são meramente fictícios e não representam informações reais!

e tambem, reforcando que no canto superior direito tem um icone de calculadora que quando clica vai pra uma calculadora e aparece o disfarce de calculadora 

> no disfarce de calculadora, caso a usuaria esqueça tudo, existe um popup que usa o BiometricPrompt que poderá resetar tanto a senha tanto a pergunta de recuperação (e ele usa APENAS o device credencial!), porém, quando utiliza o BiometricPrompt e é validado, ele pergunta a pergunta de recuperação e a senha!

> Agora, saiba que tem 3 tipos de autenticação nesse app (o esqueci tudo, o toque no visor e a senha no teclado), quando voce digita a senha no teclado, toda a tela fica preta ou branca, dependendo do modo do visor do celular, e ao dar 5 toques na tela ela volta ao normal e vai pra HubActivity, no esqueci tudo, voce valida e confirma no popup, ai vem a tela preta e voce da 5 toques, e digita a nova senha e a nova pergunta de recuperação, agora nos 5 toques no visor, voce coloca sua pergunta de recuperação, e so depois dela vem a tela preta em que você dá 5 toques, depois dela você coloca sua nova senha!

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

### Emergência:
Saindo dessa área, existem botões que abrem o aplicativo nativo do telefone nos números 190, 192 e 180.

### Compartilhamento Rápido de Localização:
Além disso, existe um botão dentro do aplicativo que obtém a localização atual, monta um link do Google Maps com as coordenadas e abre uma conversa no WhatsApp do 180 com a mensagem preparada. O envio não é automático: é necessário apenas conferir a mensagem e tocar no botão de enviar.

### Contatos de Confiança:
Além dos contatos de confiança, clicando no primeiro botão você seleciona e salva o contato. O botão abaixo envia um pedido de ajuda para ele.

> e os contatos de confiança, quando são cadrastados eles também são criptografados!

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

### Calendário Menstrual:
Registre como dói cada dia e, com isso, o aplicativo monta um calendário.

### Mapa:
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



### Tarefas:
O sistema permite categorizar tarefas em áreas como estudos, trabalho, pessoal e saúde.

As tarefas podem ser marcadas como concluídas para acompanhamento do progresso.

Todas as tarefas são salvas diretamente na classe cripto da usuária.

Os dados ficam armazenados localmente no dispositivo do usuário.

### Gravador de voz:
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

# 🏅Área de distintivos:

ele é mais uma área protegida independente, quando clica no botão da HubActivity correspondente a área, ele abre a PinpadActivity, ele tem um texto em que quando toca 5 vezes chama o BiometricPrompt com device credential e biometric weak, e você pode resetar a senha, no início você cria uma senha..., ao digitar a senha correta, as bolinhas ficam verdes e aparece = "pin correto", ao digitar a senha errada, as bolinhas ficam vermelhas e aparece = "pin incorreto", ao desbloquear, aparece a RankingActivity, em que tem o ranking1 png, ate o ranking 6 png, e aparece essa mensagem abaixo:

```
Você é bem-vinda!

O Mulher Amparada foi criado pensando em você.

Agradecemos por confiar em nosso aplicativo e permitir que ele faça parte da sua jornada.

Você merece acolhimento, respeito e segurança.

Estamos aqui para ajudar.
```

### Especificações da RankingActivity:

e a imagem troca a cada dia depois das meia-noite, até chegar na ranking6 png, e o estado de qual imagem está é salvo na classe cripto!

e a usuária tem que entrar nessa tela todos os dias pra ter garantia de ganhar todos os dinstintivos!

e ao clicar no botão voltar nessa tela, ela volta direto pra HubActivity!