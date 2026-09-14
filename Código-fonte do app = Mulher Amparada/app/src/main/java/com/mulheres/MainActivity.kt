package com.mulheres

import android.Manifest
import android.app.DownloadManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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

import kotlin.math.log10
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {

    companion object {

        const val PERMISSION_CODE = 100
        const val PICK_CONTACT = 1
    }


    // =========================================================
    // VARIÁVEIS
    // =========================================================

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
    // MICROFONE — FALLBACK DO CHACOALHAR
    // =========================================================

    private var shakeAudioRecord: AudioRecord? = null

    private var shakeMicrophoneThread: Thread? = null

    @Volatile
    private var shakeMicrophoneRunning = false

    private val shakeSampleRate = 44100

    private val shakeBufferSize =
        AudioRecord.getMinBufferSize(
            shakeSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

    private val shakeLoudSoundThreshold = -50.0


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


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


        controller.isAppearanceLightStatusBars =
            false

        controller.isAppearanceLightNavigationBars =
            false


        setContentView(
            R.layout.activity_main
        )


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
            findViewById(
                R.id.webview
            )


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


        criarShakeListener()


        locationClient =
            LocationServices
                .getFusedLocationProviderClient(
                    this
                )


        configurarWebView()


        cripto =
            Cripto(this)


        // =====================================================
        // ABERTURA INICIAL
        // =====================================================

        val pagina =
            intent?.getStringExtra(
                "pagina"
            )


        if (
            !pagina.isNullOrEmpty()
        ) {

            webView.loadUrl(
                pagina
            )

        } else {

            atualizarPaginaInicial()
        }


        // =====================================================
        // BOTÃO VOLTAR
        // =====================================================

        onBackPressedDispatcher.addCallback(
            this,
            object :
                OnBackPressedCallback(true) {

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
                    Uri.parse(
                        "smsto:"
                    )

                putExtra(
                    "address",
                    lista
                )

                putExtra(
                    "sms_body",
                    mensagem
                )
            }


        startActivity(
            intent
        )
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


        startActivity(
            intent
        )
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

            } catch (
                e: Exception
            ) {

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
                        url.startsWith(
                            "tel:"
                        )
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

                            let style =
                                document.getElementById(
                                    'androidTouchFix'
                                );

                            if (!style) {

                                style =
                                    document.createElement(
                                        'style'
                                    );

                                style.id =
                                    'androidTouchFix';

                                document.head.appendChild(
                                    style
                                );
                            }

                            style.textContent = `

                                *,
                                *::before,
                                *::after {

                                    -webkit-tap-highlight-color:
                                        transparent !important;

                                    -webkit-touch-callout:
                                        none !important;
                                }

                                *:focus,
                                *:focus-visible,
                                *:active {

                                    outline:
                                        none !important;

                                    -webkit-tap-highlight-color:
                                        transparent !important;
                                }

                            `;
                        })();
                        """.trimIndent(),
                        null
                    )


                    view?.evaluateJavascript(
                        """
                        if (
                            typeof mostrarConteudo ===
                            'function'
                        ) {
                            mostrarConteudo();
                        }
                        """.trimIndent(),
                        null
                    )
                    
                                                verificarPermissoesParaJS()
                }

            }

    }


    // =========================================================
    // CARREGAR PÁGINAS
    // =========================================================

    private fun carregarWebView1() {

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
    // PÁGINA INICIAL
    // =========================================================

    fun atualizarPaginaInicial() {

        val prefs =
            getSharedPreferences(
                "app_config",
                MODE_PRIVATE
            )


        val pagina =
            prefs.getString(
                "launcher",
                "index1.html"
            ) ?: "index1.html"


        webView.loadUrl(
            "file:///android_asset/user1/$pagina"
        )
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


        if (
            acelerometro != null
        ) {

            val registrado =
                sensorManager.registerListener(
                    shakeListener,
                    acelerometro,
                    SensorManager.SENSOR_DELAY_GAME
                )


            if (
                !registrado
            ) {

                iniciarMicrofoneShake()
            }

        } else {

            iniciarMicrofoneShake()
        }
    }


    // =========================================================
    // LISTENER DO ACELERÔMETRO
    // =========================================================

    private fun criarShakeListener() {

        shakeListener =
            object : SensorEventListener {

                override fun onSensorChanged(
                    event: SensorEvent
                ) {

                    if (
                        !protecaoAtiva
                    ) {
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

                        executarAcaoShake()
                    }
                }


                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int
                ) {
                }
            }
    }


    // =========================================================
    // AÇÃO DO BALANÇAR
    // =========================================================

    private fun executarAcaoShake() {

    if (!protecaoAtiva) {
        return
    }

    val agora =
        System.currentTimeMillis()

    if (agora - ultimoShake <= 4000) {
        return
    }

    ultimoShake =
        agora

    try {

        val intent =
            Intent(
                Intent.ACTION_DIAL
            ).apply {

                data =
                    Uri.parse(
                        "tel:180"
                    )
            }

        startActivity(intent)

        // =====================================================
        // AÇÃO CONCLUÍDA → DESATIVAR PROTEÇÃO
        // =====================================================

        protecaoAtiva = false

        pararSensor()

        avisarEstadoAoHtml(
            "movimento",
            false
        )

    } catch (
        e: Exception
    ) {

        e.printStackTrace()
    }
}

    // =========================================================
    // MICROFONE — FALLBACK
    // =========================================================

    private fun iniciarMicrofoneShake() {

        if (
            !protecaoAtiva ||
            shakeMicrophoneRunning
        ) {
            return
        }


        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            return
        }


        if (
            shakeBufferSize <= 0
        ) {

            return
        }


        try {

            shakeAudioRecord =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    shakeSampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    shakeBufferSize
                )


            if (
                shakeAudioRecord?.state !=
                AudioRecord.STATE_INITIALIZED
            ) {

                shakeAudioRecord?.release()

                shakeAudioRecord =
                    null

                return
            }


            shakeMicrophoneRunning =
                true


            shakeMicrophoneThread =
                Thread {

                    try {

                        val buffer =
                            ShortArray(
                                shakeBufferSize
                            )


                        shakeAudioRecord?.startRecording()


                        while (
                            shakeMicrophoneRunning &&
                            protecaoAtiva
                        ) {

                            val read =
                                shakeAudioRecord?.read(
                                    buffer,
                                    0,
                                    buffer.size
                                ) ?: 0


                            if (
                                read > 0
                            ) {

                                val db =
                                    calcularDecibeisShake(
                                        buffer,
                                        read
                                    )


                                if (
                                    db >=
                                    shakeLoudSoundThreshold
                                ) {

                                    runOnUiThread {

                                        executarAcaoShake()
                                    }
                                }
                            }
                        }

                    } catch (
                        _: Exception
                    ) {

                    } finally {

                        pararMicrofoneShake()
                    }

                }.apply {

                    name =
                        "MulherAmparada-ShakeMicrophone"

                    start()
                }

        } catch (
            _: Exception
        ) {

            shakeAudioRecord?.release()

            shakeAudioRecord =
                null

            shakeMicrophoneRunning =
                false
        }
    }


    // =========================================================
    // DECIBÉIS
    // =========================================================

    private fun calcularDecibeisShake(
        buffer: ShortArray,
        length: Int
    ): Double {

        if (
            length <= 0
        ) {

            return -100.0
        }


        var soma =
            0.0


        for (
            i in 0 until length
        ) {

            val sample =
                buffer[i].toDouble()


            soma +=
                sample * sample
        }


        val rms =
            sqrt(
                soma / length
            )


        if (
            rms <= 0.0
        ) {

            return -100.0
        }


        return 20.0 *
            log10(
                rms / 32768.0
            )
    }


    // =========================================================
    // PARAR MICROFONE
    // =========================================================

    private fun pararMicrofoneShake() {

        shakeMicrophoneRunning =
            false


        try {

            shakeAudioRecord?.stop()

        } catch (
            _: Exception
        ) {
        }


        try {

            shakeAudioRecord?.release()

        } catch (
            _: Exception
        ) {
        }


        shakeAudioRecord =
            null


        shakeMicrophoneThread =
            null
    }


    // =========================================================
    // PARAR SENSOR
    // =========================================================

    private fun pararSensor() {

        if (
            ::sensorManager.isInitialized &&
            ::shakeListener.isInitialized
        ) {

            sensorManager.unregisterListener(
                shakeListener
            )
        }


        pararMicrofoneShake()
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
    // PALMAS
    // =========================================================
    

@JavascriptInterface
fun ativarPalmas() {

    if (
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
    ) {

        Toast.makeText(
            this,
            "Ative o microfone nas configurações do aplicativo.",
            Toast.LENGTH_LONG
        ).show()

        avisarEstadoAoHtml(
            "palmas",
            false
        )

        return
    }

    val intent =
        Intent(
            this,
            PalmaService::class.java
        )

    startForegroundService(intent)

    avisarEstadoAoHtml(
        "palmas",
        true
    )
}

@JavascriptInterface
fun desativarPalmas() {

    stopService(
        Intent(
            this,
            PalmaService::class.java
        )
    )

    avisarEstadoAoHtml(
        "palmas",
        false
    )

    Toast.makeText(
        this,
        "Proteção por palmas desativada",
        Toast.LENGTH_SHORT
    ).show()
}

    // =========================================================
    // PROTEÇÃO POR CHACOALHAR
    // =========================================================
@JavascriptInterface
fun ativarProtecao() {

    if (!protecaoAtiva) {

        protecaoAtiva = true

        iniciarSensor()

        avisarEstadoAoHtml(
            "movimento",
            true
        )
    }
}

@JavascriptInterface
fun desativarProtecao() {

    protecaoAtiva = false

    pararSensor()

    avisarEstadoAoHtml(
        "movimento",
        false
    )
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
            .addOnSuccessListener {

                location ->

                if (
                    location != null
                ) {

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
                                    carregarWebView1()

                                3 ->
                                    carregarWebView1()

                                else ->
                                    carregarWebView4()
                            }
                        }


                        override fun onAuthenticationFailed() {

                            super.onAuthenticationFailed()


                            Toast.makeText(
                                this@MainActivity,
                                "Biometria não reconhecida, abrindo a área protegida.",
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
    // BIOMETRIA — PRINCESA
    // =========================================================

    @JavascriptInterface
    fun iniciarBiometriaPrincesa() {

        destinoBiometria =
            1


        iniciarBiometria()
    }


    // =========================================================
    // BIOMETRIA — PRÍNCIPE
    // =========================================================

    @JavascriptInterface
    fun iniciarBiometriaPrincipe() {

        destinoBiometria =
            2


        iniciarBiometria()
    }


    // =========================================================
    // BIOMETRIA — AMOR
    // =========================================================

    @JavascriptInterface
    fun iniciarBiometriaAmor() {

        destinoBiometria =
            3


        iniciarBiometria()
    }


    // =========================================================
    // BIOMETRIA — MÚSICA
    // =========================================================

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

        if (
            numero.isBlank()
        ) {

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

        } catch (
            e: Exception
        ) {

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
    // AVISAR AO HTML 
    // =========================================================
    
    // =========================================================
// AVISAR ESTADO AO HTML
// =========================================================

private fun avisarEstadoAoHtml(
    recurso: String,
    ativo: Boolean
) {

    webView.post {

        webView.evaluateJavascript(
            """
            window.dispatchEvent(
                new CustomEvent(
                    "estadoAndroid",
                    {
                        detail: {
                            recurso: "$recurso",
                            ativo: $ativo
                        }
                    }
                )
            );
            """.trimIndent(),
            null
        )
    }
}

// =========================================================
// PERMISSÕES
// =========================================================

@JavascriptInterface
fun verificarPermissoes() {

    verificarPermissoesParaJS()
}


private fun verificarPermissoesParaJS() {

    val permissoes = arrayOf(

        Manifest.permission.READ_CONTACTS,

        Manifest.permission.ACCESS_FINE_LOCATION,

        Manifest.permission.ACCESS_COARSE_LOCATION,

        Manifest.permission.RECORD_AUDIO,

        Manifest.permission.CALL_PHONE

    )


    val faltando =
        permissoes.filter { permissao ->

            ContextCompat.checkSelfPermission(
                this,
                permissao
            ) != PackageManager.PERMISSION_GRANTED

        }


    webView.post {

        if (faltando.isNotEmpty()) {

            val json =
                faltando.joinToString(
                    prefix = "[\"",
                    postfix = "\"]",
                    separator = "\",\""
                )


            webView.evaluateJavascript(
                """
                window.dispatchEvent(
                    new CustomEvent(
                        "permissoesFaltando",
                        {
                            detail: {
                                permissoes: $json
                            }
                        }
                    )
                );
                """.trimIndent(),
                null
            )

        } else {

            webView.evaluateJavascript(
                """
                window.dispatchEvent(
                    new CustomEvent(
                        "todasPermissoesOK"
                    )
                );
                """.trimIndent(),
                null
            )
        }
    }
}

// =========================================================
// VERIFICAR PERMISSÕES AO VOLTAR PARA O APP
// =========================================================

override fun onResume() {

    super.onResume()

    if (
        ::webView.isInitialized &&
        webView.url != null
    ) {

        verificarPermissoesParaJS()
    }
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
            .addOnSuccessListener {

                location ->

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
}