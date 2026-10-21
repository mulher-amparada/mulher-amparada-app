# ⚠️MURAL DE AVISOS:

### Sobre como o projeto foi estruturado:

> Quando a usuária arrasta pro lado no AssistenteActivity, valida o BiometricPrompt e acessa a HubActivity, ao apertar voltar na barra de navegação do sistema dentro da HubActivity, o app vai para o background em vez de retornar à AssistenteActivity; ao reabrir pelo ícone, volta direto na HubActivity com sessão viva, e para exigir biometria é preciso remover dos recentes — decisão que prioriza a saída rápida para pedir ajuda em situação de risco, já que voltar à biometria criaria um loop que impediria a usuária de sair, aceitando o trade-off de não reautenticar a cada retorno (mitigável com timeout de sessão de 1–2 min).

> o EmergencyOverlay só funciona dentro da HubActivity!

>Sobre as permissões: infelizmente, foi necessário configurar a HubActivity para não solicitar permissões automaticamente. Por isso, as permissões necessárias deverão ser concedidas manualmente pela usuária nas configurações do dispositivo. As permissões utilizadas por outras Activitys continuam sendo solicitadas normalmente pelo aplicativo.
>
> Ao entrar no aplicativo após sair dos disfarces, um aviso é exibido, bloqueando o acesso até que todas as permissões necessárias estejam concedidas. O aviso oferece à usuária a opção de acessar diretamente o popup de permissões do sistema. E se ainda houver alguma permissão necessária que não tenha sido concedida, o aviso continuará sendo exibido e o acesso permanecerá bloqueado. O aviso só desaparecerá quando todas as permissões necessárias estiverem concedidas. (caso o sistema nao consiga mostrar o popup de permissão novamente, ele abre a tela de configurações do app!)

>Vale lembrar que o projeto não substitui serviços oficiais do governo e também não garante segurança imediata, bem como as funções dependem do estado e hardware de cada aparelho!

> E o webview do aplicativo, quando disponivel na activity, tem suporte para seletor de arquivos se precisar!

> Vale lembrar: o Gerenciador de Arquivos do Mulher Amparada funciona principalmente como um visualizador de arquivos. O nome “Gerenciador de Arquivos” também faz parte do disfarce do aplicativo. Ele foi projetado dessa forma por uma questão de segurança: o aplicativo não oferece funções próprias para excluir, mover, copiar ou renomear arquivos, reduzindo o risco de apagar ou alterar acidentalmente algum arquivo importante — inclusive possíveis registros que a usuária queira preservar.

### Sobre as funções do projeto:

> Para as funções de balançar o celular para pedir ajuda e escurecimento por inclinação funcionarem, é preciso estar dentro do app, e repito que o escurecimento por inclinação só funciona dentro da HubActivity!

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

