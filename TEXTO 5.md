
# ⚙️Decisões técnicas:

lembre-se que hoje em dia uso github para compilar os apps e o a16 5g da samsung, então ele nao mata o processo de compilação mais!

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
        true  


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

# 📦Packpage do repositório no github:

ele é um packpage focado para quem quiser divulgar o projeto!

### ele contém:

pngs de divulgação com link, texto e qr do site do giithub pages;

o proprio qr do site do giithub pages;

banners do projeto, tanto png, tanto um código para html;

posters de divulgação para o projeto;

icones para sites e para usos no geral, tanto o do mulher amparada tanto o ic_launcher do app, versão normal e monocromático;

o ícone do mulher-amparada-bot

>APENAS PARA AS USUÁRIAS!: Ao indicar o projeto para uma mulher em situação de risco, **priorize o compartilhamento do QR Code impresso ou na tela**, em vez de enviar links de texto por mensagens (como WhatsApp ou SMS). 
>
**Por que o QR Code?** Links de texto deixam rastros fáceis de serem interceptados por agressores que monitoram o celular da vítima. 
>
>**Como agir:** Se for imprimir cartazes ou banners, certifique-se de que o QR Code está visível e em alta resolução. Isso permite que a usuária aponte a câmera e acesse o projeto diretamente, minimizando o histórico de digitação e mensagens trocadas.

a pessoa instala pelo github releases!

e quem publica e atualiza essa release é o proprio workflow!

[📦link do packpage!](https://github.com/mulher-amparada/mulher-amparada-app/pkgs/npm/divulgacao)

