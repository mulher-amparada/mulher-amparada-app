package com.mulheres

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.runtime.mutableStateOf
import android.os.BatteryManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_CODE = 100
    }

    // =========================================================
    // VARIÁVEIS
    // =========================================================
    private lateinit var cripto: Cripto

    var destinoBiometria: Int = 0

    

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var webView: WebView

    private lateinit var emergencyComposeView: ComposeView

    private var emergenciaVisivel by mutableStateOf(false)

    // =========================================================
// =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )
WindowCompat.setDecorFitsSystemWindows(
    window,
    true
)

window.statusBarColor =
    Color.TRANSPARENT

window.navigationBarColor =
    Color.TRANSPARENT

if (
    Build.VERSION.SDK_INT >=
    Build.VERSION_CODES.Q
) {
    window.isStatusBarContrastEnforced =
        false

    window.isNavigationBarContrastEnforced =
        false
}
        val controller =
            WindowInsetsControllerCompat(
                window,
                window.decorView
            )

        val isDark =
    (resources.configuration.uiMode and
        android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
        android.content.res.Configuration.UI_MODE_NIGHT_YES

controller.isAppearanceLightStatusBars =
    !isDark

controller.isAppearanceLightNavigationBars =
    !isDark

        setContentView(
            R.layout.activity_main
        )


webView = findViewById(R.id.webview)


locationClient =
    LocationServices
        .getFusedLocationProviderClient(
            this
        )
        
        


ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->

    val barras = insets.getInsets(
        WindowInsetsCompat.Type.systemBars()
    )

    val params =
        view.layoutParams as ViewGroup.MarginLayoutParams

    params.topMargin = barras.top
    params.bottomMargin = barras.bottom

    view.layoutParams = params

    insets
}
        /*
         * Inicializa uma única instância do Cripto
         * antes de configurar a WebView.
         */
        cripto =
            Cripto(this)

        configurarWebView()


        // =====================================================
        // ABERTURA INICIAL
        // =====================================================

        val pagina =
    intent?.getStringExtra(
        "pagina"
    )

val pastaUsuario =
    if (
        (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
    ) {
        "user1"
    } else {
        "user2"
    }

if (!pagina.isNullOrEmpty()) {

    webView.loadUrl(
        "file:///android_asset/$pastaUsuario/$pagina"
    )

} else {

    webView.loadUrl(
        "file:///android_asset/$pastaUsuario/index1.html"
    )
}


        // =====================================================
        // BOTÃO VOLTAR
        // =====================================================

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
        
        
    }
    

    // =========================================================
    // WEBVIEW
    // =========================================================

    private fun configurarWebView() {

    webView.setBackgroundColor(
        Color.TRANSPARENT
    )

    
    


        webView.addJavascriptInterface(
            WebAppInterface(this),
            "Android"
        )

        
        webView.addJavascriptInterface(
            cripto,
            "Cripto"
        )

        webView.addJavascriptInterface(
            DownloadInterface(this),
            "Downloader"
        )
        
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
            android.webkit.WebSettings
                .MIXED_CONTENT_NEVER_ALLOW

        webView.overScrollMode =
            View.OVER_SCROLL_NEVER

        webView.isVerticalScrollBarEnabled =
            false

        webView.isFocusable =
            true

        webView.isFocusableInTouchMode =
            true

        webView.setOnFocusChangeListener {
                _, _ ->
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


        // =====================================================
        // WEB CHROME CLIENT
        // =====================================================

        webView.webChromeClient =
            object : WebChromeClient() {

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback:
                    GeolocationPermissions.Callback?
                ) {

                    callback?.invoke(
                        origin,
                        true,
                        false
                    )
                }


                override fun onPermissionRequest(
                    request: PermissionRequest
                ) {

                    runOnUiThread {

                        val resources =
                            request.resources

                        if (
                            resources.contains(
                                PermissionRequest
                                    .RESOURCE_AUDIO_CAPTURE
                            )
                        ) {

                            request.grant(
                                arrayOf(
                                    PermissionRequest
                                        .RESOURCE_AUDIO_CAPTURE
                                )
                            )

                        } else {

                            request.deny()
                        }
                    }
                }
            }


        // =====================================================
        // WEBVIEW CLIENT
        // =====================================================

                webView.webViewClient =
            object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    val url =
                        request?.url?.toString()
                            ?: return false

                    if (
                        url.startsWith("tel:")
                    ) {

                        startActivity(
                            Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse(url)
                            )
                        )

                        return true
                    }

                    if (
                        url.startsWith(
                            "https://wa.me"
                        )
                    ) {

                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url)
                            )
                        )

                        return true
                    }

                    return false
                }


            }
            
            }
    // =========================================================
    // CARREGAR PÁGINAS
    // =========================================================

    private fun obterPastaTema(): String {

    return if (
        (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
    ) {
        "user1"
    } else {
        "user2"
    }
}


private fun carregarWebView1() {

    webView.loadUrl(
        "file:///android_asset/${obterPastaTema()}/index1.html"
    )
}


private fun carregarWebView2() {

    webView.loadUrl(
        "file:///android_asset/${obterPastaTema()}/carteira.html"
    )
}


private fun carregarWebView4() {

    webView.loadUrl(
        "file:///android_asset/${obterPastaTema()}/botao.html"
    )
}



    // =========================================================
    // BIOMETRIA
    // =========================================================

    @JavascriptInterface
    fun iniciarBiometria() {

        runOnUiThread {

            val biometricManager =
                BiometricManager.from(
                    this
                )

            val authenticators =
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL

            val canAuth =
                biometricManager.canAuthenticate(
                    authenticators
                )

if (
    canAuth !=
    BiometricManager.BIOMETRIC_SUCCESS
) {

    Toast.makeText(
        this,
        "Biometria não disponível",
        Toast.LENGTH_SHORT
    ).show()

    when (
        destinoBiometria
    ) {

        1 ->
            carregarWebView2()

        2 ->
            carregarWebView4()
    }

    return@runOnUiThread
}

            val biometricPrompt =
                BiometricPrompt(
                    this,
                    ContextCompat.getMainExecutor(
                        this
                    ),
                    object :
                        BiometricPrompt.AuthenticationCallback() {

                        override fun onAuthenticationSucceeded(
                            result:
                            BiometricPrompt.AuthenticationResult
                        ) {

                            super.onAuthenticationSucceeded(
                                result
                            )

                            when (
                                destinoBiometria
                            ) {

                                1 ->
                                    carregarWebView2()

                                2 ->
                                    carregarWebView4()
                            }
                        }


                        override fun onAuthenticationFailed() {

                            super.onAuthenticationFailed()
                        }


                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {

                            super.onAuthenticationError(
                                errorCode,
                                errString
                            )
                        }
                    }
                )

            val promptInfo =
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(
                        "Desbloquear a área protegida"
                    )
                    .setDescription(
                        "🌸 Apenas a usuária cadastrada pode acessar este local"
                    )
                    .setAllowedAuthenticators(
                        authenticators
                    )
                    .build()

            biometricPrompt.authenticate(
                promptInfo
            )
        }
    }


    // =========================================================
    // BIOMETRIA — AMPARO
    // =========================================================

    @JavascriptInterface
    fun iniciarBiometriaAmparo() {

        destinoBiometria =
            1

        iniciarBiometria()
    }


    // =========================================================
    // BIOMETRIA — ÁREA PROTEGIDA
    // =========================================================

    @JavascriptInterface
    fun IniciarBiometriaÁreaProtegida() {

        destinoBiometria =
            2

        iniciarBiometria()
    }


    // =========================================================
    // FULLSCREEN — API MODERNA
    // =========================================================

    @JavascriptInterface
    fun ativarFullscreen() {

        val window =
            window

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        val controller =
            WindowCompat.getInsetsController(
                window,
                window.decorView
            )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat
                .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        controller.hide(
            WindowInsetsCompat.Type.systemBars()
        )
    }


    
    // =========================================================
    // PEGAR LOCALIZAÇÃO
    // =========================================================

    @JavascriptInterface
    fun pegarLocalizacao() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(
                this,
                "Permissão de localização não concedida",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        locationClient.lastLocation
            .addOnSuccessListener { location ->

                if (
                    location != null
                ) {

                    val lat =
                        location.latitude

                    val lng =
                        location.longitude

                    val js =
                        "receberLocalizacao($lat,$lng)"

                    webView.evaluateJavascript(
                        js,
                        null
                    )

                } else {

                    Toast.makeText(
                        this,
                        "Não foi possível obter a localização.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Erro ao obter localização.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    // =========================================================
    // CICLO DE VIDA
    // =========================================================

override fun onDestroy() {
super.onDestroy()
}
}
