package com.mulheres  
  
import android.Manifest  
import android.app.DownloadManager  
import android.app.role.RoleManager  
import android.content.Context  
import android.content.Intent  
import android.content.pm.PackageManager  
import android.graphics.Color  
import android.hardware.Sensor  
import android.hardware.SensorEvent  
import android.hardware.SensorEventListener  
import android.hardware.SensorManager  
import android.net.Uri  
import android.os.Build  
import android.os.Bundle  
import android.os.Environment  
import android.provider.ContactsContract  
import android.provider.Settings  
import android.view.View  
import android.view.WindowManager  
import android.webkit.GeolocationPermissions  
import android.webkit.JavascriptInterface  
import android.webkit.PermissionRequest  
import android.webkit.URLUtil  
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
  
import com.google.android.gms.location.FusedLocationProviderClient  
import com.google.android.gms.location.LocationServices  
import com.google.android.gms.location.Priority  
  
import kotlin.math.sqrt  
  
  
class MainActivity : AppCompatActivity() {  
  
    companion object {  
        const val PERMISSION_CODE = 100  
        const val PICK_CONTACT = 1  
    }  
  
    private var acelerometro: Sensor? = null  
  
  private lateinit var cripto: Cripto
  
    var destinoBiometria: Int = 0  
  
    private lateinit var tiltBrightness: TiltBrightnessController  
  
    private lateinit var locationClient: FusedLocationProviderClient  
  
    private var protecaoAtiva = false  
  
    private lateinit var sensorManager: SensorManager  
  
    private lateinit var shakeListener: SensorEventListener  
  
    private var ultimoShake: Long = 0  
  
    private lateinit var webView: WebView  
  
  
    // =========================================================  
    // ON CREATE  
    // =========================================================  
  
    override fun onCreate(savedInstanceState: Bundle?) {  
        super.onCreate(savedInstanceState)  
  
        window.addFlags(  
            WindowManager.LayoutParams.FLAG_SECURE  
        )  
  
        sensorManager =  
            getSystemService(  
                Context.SENSOR_SERVICE  
            ) as SensorManager  
  
        WindowCompat.setDecorFitsSystemWindows(  
            window,  
            false  
        )  
  
        window.statusBarColor = Color.TRANSPARENT  
        window.navigationBarColor = Color.TRANSPARENT  
  
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  
            window.isStatusBarContrastEnforced = false  
            window.isNavigationBarContrastEnforced = false  
        }  
  
        val controller =  
            WindowInsetsControllerCompat(  
                window,  
                window.decorView  
            )  
  
        controller.isAppearanceLightStatusBars = false  
        controller.isAppearanceLightNavigationBars = false  
  
        setContentView(R.layout.activity_main)  
  
        ViewCompat.setOnApplyWindowInsetsListener(  
            window.decorView  
        ) { view, insets ->  
  
            view.setPadding(  
                0,  
                0,  
                0,  
                0  
            )  
  
            insets  
        }  
  
        webView =  
            findViewById(R.id.webview)  
  
        webView.setBackgroundColor(  
            Color.BLACK  
        )  
  
        webView.visibility =  
            View.VISIBLE  
  
        tiltBrightness =  
            TiltBrightnessController(  
                this,  
                sensorManager,  
                webView  
            )  
  
        locationClient =  
            LocationServices  
                .getFusedLocationProviderClient(  
                    this  
                )  
  
        configurarWebView()  
        
        cripto = Cripto(this)
  
  
        // =====================================================  
        // ABERTURA INICIAL  
        // =====================================================  
  
        /*  
         * Se o Android iniciou o aplicativo como  
         * Launcher/Home padrão, abre SEMPRE o index11.html.  
         */  
        if (  
            intent?.action ==  
                Intent.ACTION_MAIN &&  
            intent?.hasCategory(  
                Intent.CATEGORY_HOME  
            ) == true  
        ) {  
  
            abrirLauncher()  
  
        } else {  
  
            /*  
             * Abertura normal do aplicativo.  
             */  
            val pagina =  
                intent?.getStringExtra(  
                    "pagina"  
                )  
  
            if (!pagina.isNullOrEmpty()) {  
  
                webView.loadUrl(  
                    pagina  
                )  
  
            } else {  
  
                atualizarPaginaInicial()  
            }  
        }  
  
  
        // =====================================================  
        // PERMISSÕES  
        // =====================================================  
  
        if (!temPermissoes()) {  
            pedirPermissoes()  
        }  
  
  
        // =====================================================  
        // LOCALIZAÇÃO VIA INTENT  
        // =====================================================  
  
          
  
  
        // =====================================================  
        // BOTÃO VOLTAR  
        // =====================================================  
  
        onBackPressedDispatcher.addCallback(  
            this,  
            object : OnBackPressedCallback(true) {  
  
                override fun handleOnBackPressed() {  
  
                    if (  
                        webView.canGoBack()  
                    ) {  
  
                        webView.goBack()  
                    }  
                }  
            }  
        )  
    }  
  
  
    // =========================================================  
    // PERMISSÕES  
    // =========================================================  
  
    override fun onRequestPermissionsResult(  
        requestCode: Int,  
        permissions: Array<out String>,  
        grantResults: IntArray  
    ) {  
  
        super.onRequestPermissionsResult(  
            requestCode,  
            permissions,  
            grantResults  
        )  
  
        if (  
            requestCode ==  
                PERMISSION_CODE  
        ) {  
  
            atualizarPaginaInicial()  
  
            if (!temPermissoes()) {  
  
                Toast.makeText(  
                    this,  
                    "Algumas permissões não foram concedidas.",  
                    Toast.LENGTH_LONG  
                ).show()  
            }  
        }  
    }  
  
  
    private fun pedirPermissoes() {  
  
        ActivityCompat.requestPermissions(  
            this,  
            arrayOf(  
                Manifest.permission.ACCESS_FINE_LOCATION,  
                Manifest.permission.READ_CONTACTS,  
                Manifest.permission.CALL_PHONE,  
                Manifest.permission.RECORD_AUDIO  
            ),  
            PERMISSION_CODE  
        )  
    }  
  
  
    private fun temPermissoes(): Boolean {  
  
        return ContextCompat.checkSelfPermission(  
            this,  
            Manifest.permission.ACCESS_FINE_LOCATION  
        ) ==  
            PackageManager.PERMISSION_GRANTED &&  
  
        ContextCompat.checkSelfPermission(  
            this,  
            Manifest.permission.READ_CONTACTS  
        ) ==  
            PackageManager.PERMISSION_GRANTED &&  
  
        ContextCompat.checkSelfPermission(  
            this,  
            Manifest.permission.CALL_PHONE  
        ) ==  
            PackageManager.PERMISSION_GRANTED &&  
  
        ContextCompat.checkSelfPermission(  
            this,  
            Manifest.permission.RECORD_AUDIO  
        ) ==  
            PackageManager.PERMISSION_GRANTED  
    }  
  
  
    private fun temPermissoesProtecao(): Boolean {  
  
        return ContextCompat.checkSelfPermission(  
            this,  
            Manifest.permission.RECORD_AUDIO  
        ) ==  
            PackageManager.PERMISSION_GRANTED  
    }  
  
  
    // =========================================================  
    // SMS  
    // =========================================================  
  
    private fun abrirIntentSMS(  
        mensagem: String  
    ) {  
  
        val lista =
    cripto.carregar(
        "contatos_lista"
    )
  
        if (  
            lista.trim().isEmpty()  
        ) {  
  
            Toast.makeText(  
                this,  
                "Nenhum contato cadastrado",  
                Toast.LENGTH_SHORT  
            ).show()  
  
            return  
        }  
  
        val intent =  
            Intent(  
                Intent.ACTION_SENDTO  
            ).apply {  
  
                data =  
                    Uri.parse("smsto:")  
  
                putExtra(  
                    "address",  
                    lista  
                )  
  
                putExtra(  
                    "sms_body",  
                    mensagem  
                )  
            }  
  
        startActivity(intent)  
    }  
  
  
    // =========================================================  
    // CONFIGURAÇÕES  
    // =========================================================  
  
    private fun abrirConfiguracoes() {  
  
        val intent =  
            Intent(  
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS  
            ).apply {  
  
                data =  
                    Uri.fromParts(  
                        "package",  
                        packageName,  
                        null  
                    )  
            }  
  
        startActivity(intent)  
    }  
  
  
    // =========================================================  
    // WEBVIEW  
    // =========================================================  
  
    private fun configurarWebView() {  
  
        webView.addJavascriptInterface(  
            WebAppInterface(this),  
            "Android"  
        )  
  
        webView.addJavascriptInterface(  
            TiltBrightnessController.WebAppInterface(  
                tiltBrightness  
            ),  
            "TiltBrightness"  
        )  
  
        webView.addJavascriptInterface(  
            Cripto(this),  
            "Cripto"  
        )  



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

  
  
        // =====================================================  
        // DOWNLOAD  
        // =====================================================  
  
        webView.setDownloadListener {  
                url,  
                userAgent,  
                contentDisposition,  
                mimeType,  
                _ ->  
  
            try {  
  
                val fileName =  
                    URLUtil.guessFileName(  
                        url,  
                        contentDisposition,  
                        mimeType  
                    )  
  
                val request =  
                    DownloadManager.Request(  
                        Uri.parse(url)  
                    ).apply {  
  
                        setMimeType(  
                            mimeType  
                        )  
  
                        addRequestHeader(  
                            "User-Agent",  
                            userAgent  
                        )  
  
                        setDescription(  
                            "Baixando arquivo..."  
                        )  
  
                        setTitle(  
                            fileName  
                        )  
  
                        setNotificationVisibility(  
                            DownloadManager  
                                .Request  
                                .VISIBILITY_VISIBLE_NOTIFY_COMPLETED  
                        )  
  
                        setDestinationInExternalPublicDir(  
                            Environment  
                                .DIRECTORY_DOWNLOADS,  
                            fileName  
                        )  
                    }  
  
                val downloadManager =  
                    getSystemService(  
                        Context.DOWNLOAD_SERVICE  
                    ) as DownloadManager  
  
                downloadManager.enqueue(  
                    request  
                )  
  
                Toast.makeText(  
                    this,  
                    "Download iniciado",  
                    Toast.LENGTH_SHORT  
                ).show()  
  
            } catch (e: Exception) {  
  
                Toast.makeText(  
                    this,  
                    "Não foi possível iniciar o download",  
                    Toast.LENGTH_SHORT  
                ).show()  
  
                e.printStackTrace()  
            }  
        }  
  
  
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
                        request?.url  
                            ?.toString()  
                            ?: ""  
  
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
  
  
                override fun onPageFinished(  
                    view: WebView?,  
                    url: String?  
                ) {  
  
                    super.onPageFinished(  
                        view,  
                        url  
                    )  
  
  view?.evaluateJavascript(
    """
    (function() {
        let style = document.getElementById('androidTouchFix');

        if (!style) {
            style = document.createElement('style');
            style.id = 'androidTouchFix';
            document.head.appendChild(style);
        }

        style.textContent = `
            *,
            *::before,
            *::after {
                -webkit-tap-highlight-color: transparent !important;
                -webkit-touch-callout: none !important;
            }

            *:focus,
            *:focus-visible,
            *:active {
                outline: none !important;
                -webkit-tap-highlight-color: transparent !important;
            }
        `;
    })();
    """.trimIndent(),
    null
)

                    view?.evaluateJavascript(  
                        """  
                        if (typeof mostrarConteudo === 'function') {  
                            mostrarConteudo();  
                        }  
                        """.trimIndent(),  
                        null  
                    )  
                }  
            }  
    }  
  
  
    // =========================================================  
    // CARREGAR PÁGINAS  
    // =========================================================  
  
    private fun carregarWebView() {  
  
        webView.loadUrl(  
            "file:///android_asset/user1/index1.html"  
        )  
  
        webView.visibility =  
            View.VISIBLE  
    }  
  
  
    private fun carregarWebView1() {  
  
        webView.loadUrl(  
            "file:///android_asset/user1/index1.html"  
        )  
  
        webView.visibility =  
            View.VISIBLE  
    }  
  
  
    private fun carregarWebView2() {  
  
        webView.loadUrl(  
            "file:///android_asset/user1/index1.html"  
        )  
  
        webView.visibility =  
            View.VISIBLE  
    }  
  
  
    private fun carregarWebView3() {  
  
        webView.loadUrl(  
            "file:///android_asset/user1/index1.html"  
        )  
  
        webView.visibility =  
            View.VISIBLE  
    }  
  
  
    private fun carregarWebView4() {  
  
        webView.loadUrl(  
            "file:///android_asset/user1/botao.html"  
        )  
  
        webView.visibility =  
            View.VISIBLE  
    }  
  
  
    // =========================================================  
    // SENSOR / CHACOALHAR  
    // =========================================================  
  
    private fun iniciarSensor() {  
  
        sensorManager =  
            getSystemService(  
                Context.SENSOR_SERVICE  
            ) as SensorManager  
  
        acelerometro =  
            sensorManager.getDefaultSensor(  
                Sensor.TYPE_ACCELEROMETER  
            )  
  
        shakeListener =  
            object : SensorEventListener {  
  
                override fun onSensorChanged(  
                    event: SensorEvent  
                ) {  
  
                    if (!protecaoAtiva) {  
                        return  
                    }  
  
                    val x =  
                        event.values[0]  
  
                    val y =  
                        event.values[1]  
  
                    val z =  
                        event.values[2]  
  
                    val aceleracao =  
                        sqrt(  
                            (  
                                x * x +  
                                y * y +  
                                z * z  
                            ).toDouble()  
                        )  
  
                    if (  
                        aceleracao > 18.0  
                    ) {  
  
                        val agora =  
                            System.currentTimeMillis()  
  
                        if (  
                            agora -  
                            ultimoShake >  
                            4000  
                        ) {  
  
                            ultimoShake =  
                                agora  
  
                            val intent =  
                                Intent(  
                                    Intent.ACTION_DIAL  
                                ).apply {  
  
                                    data =  
                                        Uri.parse(  
                                            "tel:180"  
                                        )  
                                }  
  
                            startActivity(  
                                intent  
                            )  
                        }  
                    }  
                }  
  
  
                override fun onAccuracyChanged(  
                    sensor: Sensor?,  
                    accuracy: Int  
                ) {  
                    // Nada  
                }  
            }  
  
  
        acelerometro?.let {  
  
            sensorManager.registerListener(  
                shakeListener,  
                it,  
                SensorManager.SENSOR_DELAY_GAME  
            )  
        }  
    }  
  
  
    private fun pararSensor() {  
  
        if (  
            ::sensorManager.isInitialized &&  
            ::shakeListener.isInitialized  
        ) {  
  
            sensorManager.unregisterListener(  
                shakeListener  
            )  
        }  
    }  
      
  
    // =========================================================  
    // CONTATOS  
    // =========================================================  
  
    @JavascriptInterface  
    fun abrirContatos() {  
  
        val intent =  
            Intent(  
                Intent.ACTION_PICK,  
                ContactsContract  
                    .CommonDataKinds  
                    .Phone  
                    .CONTENT_URI  
            )  
  
        startActivityForResult(  
            intent,  
            PICK_CONTACT  
        )  
    }  
  
  
    // =========================================================  
    // PROTEÇÃO POR PALMAS  
    // =========================================================  
  
    @JavascriptInterface  
    fun ativarPalmas() {  
  
        if (!temPermissoesProtecao()) {  
  
            Toast.makeText(  
                this,  
                "Permissão de microfone não concedida",  
                Toast.LENGTH_SHORT  
            ).show()  
  
            return  
        }  
  
        val intent =  
            Intent(  
                this,  
                PalmaService::class.java  
            )  
  
        startForegroundService(  
            intent  
        )  
    }  
  
  
    // =========================================================  
    // PROTEÇÃO POR CHACOALHAR  
    // =========================================================  
  
    @JavascriptInterface  
    fun ativarProtecao() {  
  
        if (!protecaoAtiva) {  
  
            protecaoAtiva =  
                true  
  
            iniciarSensor()  
        }  
    }  
  
  
    @JavascriptInterface  
    fun desativarPalmas() {  
  
        stopService(  
            Intent(  
                this,  
                PalmaService::class.java  
            )  
        )  
  
        Toast.makeText(  
            this,  
            "Proteção por palmas desativada",  
            Toast.LENGTH_SHORT  
        ).show()  
    }  
  
  
    @JavascriptInterface  
    fun desativarProtecao() {  
  
        protecaoAtiva =  
            false  
  
        pararSensor()  
    }  
  
  
    // =========================================================  
    // SOS  
    // =========================================================  
  
    @JavascriptInterface  
    fun enviarSOS() {  
  
        if (  
            ActivityCompat.checkSelfPermission(  
                this,  
                Manifest.permission.ACCESS_FINE_LOCATION  
            ) !=  
            PackageManager.PERMISSION_GRANTED  
        ) {  
  
            return  
        }  
  
        locationClient.lastLocation  
            .addOnSuccessListener { location ->  
  
                if (location != null) {  
  
                    val lat =  
                        location.latitude  
  
                    val lng =  
                        location.longitude  
  
                    val link =  
                        "https://maps.google.com/?q=$lat,$lng"  
  
                    val mensagem =  
                        "🚨 SOCORRO! Estou aqui: $link"  
  
                    abrirIntentSMS(  
                        mensagem  
                    )  
                }  
            }  
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
                BiometricManager.Authenticators  
                    .BIOMETRIC_WEAK or  
                BiometricManager.Authenticators  
                    .DEVICE_CREDENTIAL  
  
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
                    "Biometria indisponível, mas abriremos o serviço pra você!",  
                    Toast.LENGTH_SHORT  
                ).show()  
  
                carregarWebView4()  
  
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
                                    carregarWebView1()  
  
                                2 ->  
                                    carregarWebView2()  
  
                                3 ->  
                                    carregarWebView3()  
  
                                else ->  
                                    carregarWebView4()  
                            }  
                        }  
  
  
                        override fun onAuthenticationFailed() {  
  
                            super.onAuthenticationFailed()  
  
                            Toast.makeText(  
                                this@MainActivity,  
                                "Biometria não disponível, recomendo ativar a biometria no seu aparelho, porém mesmo assim abriremos os seus acessos!",  
                                Toast.LENGTH_SHORT  
                            ).show()  
  
                            carregarWebView4()  
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
                        "Desbloquear"  
                    )  
                    .setDescription(  
                        "Use biometria, PIN ou senha"  
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
  
  
    @JavascriptInterface  
    fun iniciarBiometriaPrincesa() {  
  
        destinoBiometria =  
            1  
  
        iniciarBiometria()  
    }  
  
  
    @JavascriptInterface  
    fun iniciarBiometriaPrincipe() {  
  
        destinoBiometria =  
            2  
  
        iniciarBiometria()  
    }  
  
  
    @JavascriptInterface  
    fun iniciarBiometriaAmor() {  
  
        destinoBiometria =  
            3  
  
        iniciarBiometria()  
    }  
  
  
    @JavascriptInterface  
    fun iniciarBiometriaMusica() {  
  
        destinoBiometria =  
            4  
  
        iniciarBiometria()  
    }  
  
  
    // =========================================================  
    // FULLSCREEN  
    // =========================================================  
  
    @JavascriptInterface  
    fun ativarFullscreen() {  
  
        val window =  
            window  
  
        val decorView =  
            window.decorView  
  
        WindowCompat.setDecorFitsSystemWindows(  
            window,  
            false  
        )  
  
        val controller =  
            WindowCompat.getInsetsController(  
                window,  
                decorView  
            )  
  
        controller.systemBarsBehavior =  
            WindowInsetsControllerCompat  
                .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE  
  
        controller.hide(  
            WindowInsetsCompat.Type.systemBars()  
        )  
  
        @Suppress("DEPRECATION")  
        decorView.systemUiVisibility =  
            View.SYSTEM_UI_FLAG_FULLSCREEN or  
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or  
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or  
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or  
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or  
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE  
  
        @Suppress("DEPRECATION")  
        window.addFlags(  
            WindowManager.LayoutParams.FLAG_FULLSCREEN  
        )  
  
        decorView.post {  
  
            controller.hide(  
                WindowInsetsCompat.Type.systemBars()  
            )  
  
            @Suppress("DEPRECATION")  
            decorView.systemUiVisibility =  
                View.SYSTEM_UI_FLAG_FULLSCREEN or  
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or  
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or  
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or  
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or  
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE  
        }  
  
        decorView.postDelayed({  
  
            controller.hide(  
                WindowInsetsCompat.Type.systemBars()  
            )  
  
        }, 100)  
    }  
  
  
    @JavascriptInterface  
    fun desativarFullscreen() {  
  
        val window =  
            window  
  
        val decorView =  
            window.decorView  
  
        val controller =  
            WindowCompat.getInsetsController(  
                window,  
                decorView  
            )  
  
        controller.show(  
            WindowInsetsCompat.Type.systemBars()  
        )  
  
        controller.isAppearanceLightStatusBars =  
            false  
  
        controller.isAppearanceLightNavigationBars =  
            false  
  
        @Suppress("DEPRECATION")  
        decorView.systemUiVisibility =  
            View.SYSTEM_UI_FLAG_VISIBLE  
  
        @Suppress("DEPRECATION")  
        window.clearFlags(  
            WindowManager.LayoutParams.FLAG_FULLSCREEN  
        )  
    }  
  
  
    // =========================================================  
    // LIGAÇÃO DIRETA  
    // =========================================================  
  
    @JavascriptInterface  
    fun ligarDireto(  
        numero: String  
    ) {  
  
        if (numero.isBlank()) {  
            return  
        }  
  
        try {  
  
            val intent =  
                Intent(  
                    Intent.ACTION_CALL  
                ).apply {  
  
                    data =  
                        Uri.parse(  
                            "tel:$numero"  
                        )  
                }  
  
            if (  
                ContextCompat.checkSelfPermission(  
                    this,  
                    Manifest.permission.CALL_PHONE  
                ) ==  
                PackageManager.PERMISSION_GRANTED  
            ) {  
  
                startActivity(  
                    intent  
                )  
  
            } else {  
  
                Toast.makeText(  
                    this,  
                    "Permissão de ligação não concedida",  
                    Toast.LENGTH_SHORT  
                ).show()  
            }  
  
        } catch (e: Exception) {  
  
            e.printStackTrace()  
  
            val fallback =  
                Intent(  
                    Intent.ACTION_DIAL  
                ).apply {  
  
                    data =  
                        Uri.parse(  
                            "tel:$numero"  
                        )  
                }  
  
            startActivity(  
                fallback  
            )  
        }  
    }  
  
  
    // =========================================================  
    // RESULTADO DO CONTATO  
    // =========================================================  
  
    override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
) {

    super.onActivityResult(
        requestCode,
        resultCode,
        data
    )

    if (
        requestCode ==
            PICK_CONTACT &&
        resultCode ==
            RESULT_OK
    ) {

        val uri =
            data?.data ?: return

        val cursor =
            contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )

        if (
            cursor != null &&
            cursor.moveToFirst()
        ) {

            val numeroIndex =
                cursor.getColumnIndex(
                    "data1"
                )

            val nomeIndex =
                cursor.getColumnIndex(
                    "display_name"
                )

            val numero =
                if (
                    numeroIndex >= 0
                ) {

                    cursor.getString(
                        numeroIndex
                    )
                        ?.replace(
                            Regex("\\s"),
                            ""
                        )
                        ?.replace(
                            "-",
                            ""
                        )
                        ?: ""

                } else {

                    ""
                }

            val nome =
                if (
                    nomeIndex >= 0
                ) {

                    cursor.getString(
                        nomeIndex
                    ) ?: "Contato"

                } else {

                    "Contato"
                }


            /*
             * =====================================================
             * CRIPTOGRAFIA
             * =====================================================
             *
             * Os dados são armazenados através da classe Cripto.
             *
             * NÃO usar mais:
             *
             * getSharedPreferences("contatos", ...)
             */


            val listaAtual =
                cripto.carregar(
                    "contatos_lista"
                )

            val nomesAtual =
                cripto.carregar(
                    "contatos_nomes"
                )


            /*
             * =====================================================
             * ADICIONA O NÚMERO À LISTA
             * =====================================================
             */

            val novaLista =
                if (
                    listaAtual.isEmpty()
                ) {

                    numero

                } else {

                    "$listaAtual,$numero"
                }


            /*
             * =====================================================
             * ADICIONA NOME + NÚMERO
             * =====================================================
             */

            val novosNomes =
                if (
                    nomesAtual.isEmpty()
                ) {

                    "$nome - $numero"

                } else {

                    "$nomesAtual\n$nome - $numero"
                }


            /*
             * =====================================================
             * SALVA CRIPTOGRAFADO
             * =====================================================
             */

            cripto.salvar(
                "contatos_lista",
                novaLista
            )

            cripto.salvar(
                "contatos_nomes",
                novosNomes
            )


            cursor.close()


            Toast.makeText(
                this,
                "Contato salvo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
    // =========================================================  
    // LAUNCHER  
    // =========================================================  
  
    private fun atualizarPaginaInicial() {  
  
        val prefs =  
            getSharedPreferences(  
                "app_config",  
                MODE_PRIVATE  
            )  
  
        val launcher =  
            prefs.getString(  
                "launcher",  
                "index1.html"  
            ) ?: "index1.html"  
  
        webView.loadUrl(  
            "file:///android_asset/user1/$launcher"  
        )  
    }  
  
  
    fun abrirLauncher() {  
  
        webView.loadUrl(  
            "file:///android_asset/user1/index11.html"  
        )  
  
        webView.visibility =  
            View.VISIBLE  
    }  
  
  
    fun abrirCalculadora() {  
  
        webView.loadUrl(  
            "file:///android_asset/user1/index1.html"  
        )  
  
        webView.visibility =  
            View.VISIBLE  
    }  
  
  
    fun definirLauncher() {  
  
        getSharedPreferences(  
            "app_config",  
            MODE_PRIVATE  
        )  
            .edit()  
            .putString(  
                "launcher",  
                "index11.html"  
            )  
            .apply()  
    }  
  
  
    fun definirCalculadoraComoLauncher() {  
  
        getSharedPreferences(  
            "app_config",  
            MODE_PRIVATE  
        )  
            .edit()  
            .putString(  
                "launcher",  
                "index1.html"  
            )  
            .apply()  
    }  
  
  
    // =========================================================  
    // ROLE MANAGER  
    // =========================================================  
  
    fun verificarSeEhLauncherPadrao(): Boolean {  
  
        if (  
            Build.VERSION.SDK_INT <  
            Build.VERSION_CODES.Q  
        ) {  
            return false  
        }  
  
        val roleManager =  
            getSystemService(  
                RoleManager::class.java  
            )  
  
        return roleManager.isRoleHeld(  
            RoleManager.ROLE_HOME  
        )  
    }  
  
  
    fun solicitarLauncherPadrao() {  
  
        if (  
            Build.VERSION.SDK_INT <  
            Build.VERSION_CODES.Q  
        ) {  
            return  
        }  
  
        val roleManager =  
            getSystemService(  
                RoleManager::class.java  
            )  
  
        if (  
            !roleManager.isRoleHeld(  
                RoleManager.ROLE_HOME  
            )  
        ) {  
  
            val intent =  
                roleManager.createRequestRoleIntent(  
                    RoleManager.ROLE_HOME  
                )  
  
            startActivityForResult(  
                intent,  
                200  
            )  
        }  
    }  
  
  
    // =========================================================  
    // NOVA INTENT  
    // =========================================================  
  
    override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    // seu código aqui
  
        if (intent == null) {  
            return  
        }  
  
        setIntent(intent)  
  
  
        // =====================================================  
        // LAUNCHER / HOME  
        // =====================================================  
  
        /*  
         * Quando o Android chama o app como HOME/Launcher,  
         * o index11.html é aberto.  
         */  
        if (  
            intent.action ==  
                Intent.ACTION_MAIN &&  
            intent.hasCategory(  
                Intent.CATEGORY_HOME  
            )  
        ) {  
  
            abrirLauncher()  
  
            return  
        }  
  
  
        // =====================================================  
        // ÍCONE NORMAL  
        // =====================================================  
  
        if (  
            intent.action ==  
                Intent.ACTION_MAIN &&  
            intent.hasCategory(  
                Intent.CATEGORY_LAUNCHER  
            )  
        ) {  
  
            atualizarPaginaInicial()  
  
            return  
        }  
  
  
        // =====================================================  
        // PÁGINA ENVIADA PELO JAVASCRIPT  
        // =====================================================  
  
        val pagina =  
            intent.getStringExtra(  
                "PAGINA_JS"  
            )  
  
        if (  
            !pagina.isNullOrEmpty()  
        ) {  
  
            val paginaSegura =  
                pagina  
                    .replace(  
                        "\\",  
                        "\\\\"  
                    )  
                    .replace(  
                        "'",  
                        "\\'"  
                    )  
  
            webView.evaluateJavascript(  
                """  
                if (typeof trocarPaginaPeloIcone === 'function') {  
                    trocarPaginaPeloIcone('$paginaSegura');  
                }  
                """.trimIndent(),  
                null  
            )  
        }  
    }  
  
  
    // =========================================================  
    // PEGAR LOCALIZAÇÃO  
    //  
    // IMPORTANTE:  
    // ESTA FUNÇÃO ESTÁ DENTRO DA MainActivity.  
    //  
    // ELA NÃO ESTÁ DENTRO DE onNewIntent().  
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
  
                if (location != null) {  
  
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
}