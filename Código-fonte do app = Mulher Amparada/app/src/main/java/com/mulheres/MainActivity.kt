package com.mulheres

import android.Manifest
import android.app.DownloadManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.log10
import kotlin.math.sqrt

private val Black = Color(0xFF000000)
private val White = Color(0xFFFFFFFF)

private val TextMain = Color(0xFFF7F7FA)
private val Soft = Color(0xFFA0A4B0)
private val Muted = Color(0xFF686D79)

private val Pink = Color(0xFFFF3F82)
private val PinkLight = Color(0xFFFF6B9F)
private val PinkDark = Color(0xFFDB2C6D)

private val Purple = Color(0xFF8B5CF6)
private val PurpleLight = Color(0xFFB79AFF)

private val Orange = Color(0xFFFF9F43)
private val OrangeLight = Color(0xFFFFC071)

private val Cyan = Color(0xFF27C9C1)
private val Red = Color(0xFFFF4054)

val quicksandTypeface = remember {
    Typeface.createFromAsset(
        context.assets,
        "font.ttf"
    )
}

val Quicksand = remember(quicksandTypeface) {
    FontFamily(
        Font(
            ResId = 0
        )
    )
}

class MainActivity : ComponentActivity() {

    companion object {
        const val PERMISSION_CODE = 100
        const val PICK_CONTACT = 1
    }

    // =========================================================
    // VARIÁVEIS
    // =========================================================

    private var palmasEmExecucao = false

    private lateinit var telephonyManager: TelephonyManager

    private var telefoneListener: PhoneStateListener? = null

    private var acelerometro: Sensor? = null

    private lateinit var cripto: Cripto

    var destinoBiometria: Int = 0

    private lateinit var tiltBrightness: TiltBrightnessController

    private lateinit var locationClient: FusedLocationProviderClient

    private var protecaoAtiva = false

    private lateinit var sensorManager: SensorManager

    private lateinit var shakeListener: SensorEventListener

    private var ultimoShake: Long = 0

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
    // ESTADO DA UI
    // =========================================================

    private var uiPalmasAtiva by mutableStateOf(false)
    private var uiMovimentoAtivo by mutableStateOf(false)
    private var uiBrilhoAtivo by mutableStateOf(false)
    private var uiBloqueioAtivo by mutableStateOf(false)

    private var mostrarPermissoes by mutableStateOf(true)

    // =========================================================
    // RESULTADO DO CONTATO
    // =========================================================

    private val escolherContato =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (
                result.resultCode == RESULT_OK &&
                result.data?.data != null
            ) {

                val contactUri = result.data!!.data!!

                val cursor = contentResolver.query(
                    contactUri,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    null
                )

                cursor?.use {

                    if (it.moveToFirst()) {

                        val numero =
                            it.getString(0)

                        cripto.salvar(
                            "contatos_lista",
                            numero
                        )

                        Toast.makeText(
                            this,
                            "Contato adicionado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE
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
            AndroidColor.TRANSPARENT

        window.navigationBarColor =
            AndroidColor.TRANSPARENT

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q
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

        locationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

        cripto =
            Cripto(this)

        tiltBrightness =
            TiltBrightnessController(
                this,
                sensorManager,
                null
            )

        criarShakeListener()

        setContent {

            MulherAmparadaScreen(
                activity = this
            )
        }
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
    // CARREGAR PÁGINA — MANTIDO COMO COMPATIBILIDADE
    // =========================================================

    private fun carregarWebView1() {
        // A interface agora é Compose.
        setContent {
            MulherAmparadaScreen(
                activity = this
            )
        }
    }

    private fun carregarWebView4() {
        setContent {
            MulherAmparadaScreen(
                activity = this
            )
        }
    }

    fun atualizarPaginaInicial() {
        setContent {
            MulherAmparadaScreen(
                activity = this
            )
        }
    }

    // =========================================================
    // SENSOR / CHACOALHAR
    // =========================================================

    private fun iniciarSensor() {

        acelerometro =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            )

        if (acelerometro != null) {

            val registrado =
                sensorManager.registerListener(
                    shakeListener,
                    acelerometro,
                    SensorManager.SENSOR_DELAY_GAME
                )

            if (!registrado) {
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

                    if (!protecaoAtiva) {
                        return
                    }

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val aceleracao =
                        sqrt(
                            (
                                x * x +
                                y * y +
                                z * z
                            ).toDouble()
                        )

                    if (aceleracao > 18.0) {
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

        ultimoShake = agora

        try {

            val intent =
                Intent(
                    Intent.ACTION_DIAL
                ).apply {

                    data =
                        Uri.parse("tel:180")
                }

            startActivity(intent)

            protecaoAtiva = false
            uiMovimentoAtivo = false

            pararSensor()

        } catch (e: Exception) {

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
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (shakeBufferSize <= 0) {
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
                shakeAudioRecord = null

                return
            }

            shakeMicrophoneRunning = true

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

                            if (read > 0) {

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

                    } catch (_: Exception) {

                    } finally {

                        pararMicrofoneShake()
                    }

                }.apply {

                    name =
                        "MulherAmparada-ShakeMicrophone"

                    start()
                }

        } catch (_: Exception) {

            shakeAudioRecord?.release()
            shakeAudioRecord = null
            shakeMicrophoneRunning = false
        }
    }

    // =========================================================
    // DECIBÉIS
    // =========================================================

    private fun calcularDecibeisShake(
        buffer: ShortArray,
        length: Int
    ): Double {

        if (length <= 0) {
            return -100.0
        }

        var soma = 0.0

        for (i in 0 until length) {

            val sample =
                buffer[i].toDouble()

            soma += sample * sample
        }

        val rms =
            sqrt(
                soma / length
            )

        if (rms <= 0.0) {
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

        shakeMicrophoneRunning = false

        try {
            shakeAudioRecord?.stop()
        } catch (_: Exception) {
        }

        try {
            shakeAudioRecord?.release()
        } catch (_: Exception) {
        }

        shakeAudioRecord = null
        shakeMicrophoneThread = null
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

    fun abrirContatos() {

        val intent =
            Intent(
                Intent.ACTION_PICK,
                ContactsContract
                    .CommonDataKinds
                    .Phone
                    .CONTENT_URI
            )

        escolherContato.launch(intent)
    }

    // =========================================================
    // PALMAS
    // =========================================================

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

            uiPalmasAtiva = false

            return
        }

        palmasEmExecucao = false

        monitorarFimDaLigacao()

        val intent =
            Intent(
                this,
                PalmaService::class.java
            )

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.O
        ) {

            startForegroundService(intent)

        } else {

            startService(intent)
        }

        uiPalmasAtiva = true
    }

    fun desativarPalmas() {

        stopService(
            Intent(
                this,
                PalmaService::class.java
            )
        )

        uiPalmasAtiva = false

        Toast.makeText(
            this,
            "Proteção por palmas desativada",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun monitorarFimDaLigacao() {

        if (!::telephonyManager.isInitialized) {

            telephonyManager =
                getSystemService(
                    Context.TELEPHONY_SERVICE
                ) as TelephonyManager
        }

        telefoneListener =
            object : PhoneStateListener() {

                override fun onCallStateChanged(
                    state: Int,
                    phoneNumber: String?
                ) {

                    super.onCallStateChanged(
                        state,
                        phoneNumber
                    )

                    if (
                        state ==
                        TelephonyManager.CALL_STATE_OFFHOOK
                    ) {

                        palmasEmExecucao = true
                    }

                    if (
                        state ==
                        TelephonyManager.CALL_STATE_IDLE &&
                        palmasEmExecucao
                    ) {

                        palmasEmExecucao = false

                        uiPalmasAtiva = false

                        try {

                            telephonyManager.listen(
                                telefoneListener,
                                PhoneStateListener.LISTEN_NONE
                            )

                        } catch (_: Exception) {
                        }

                        telefoneListener = null
                    }
                }
            }

        try {

            telephonyManager.listen(
                telefoneListener,
                PhoneStateListener.LISTEN_CALL_STATE
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // =========================================================
    // PROTEÇÃO POR CHACOALHAR
    // =========================================================

    fun ativarProtecao() {

        if (!protecaoAtiva) {

            protecaoAtiva = true

            iniciarSensor()

            uiMovimentoAtivo = true
        }
    }

    fun desativarProtecao() {

        protecaoAtiva = false

        pararSensor()

        uiMovimentoAtivo = false
    }

    // =========================================================
    // SOS
    // =========================================================

    fun enviarSOS() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
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

    fun iniciarBiometria() {

        runOnUiThread {

            val biometricManager =
                BiometricManager.from(this)

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
                    "Biometria indisponível.",
                    Toast.LENGTH_SHORT
                ).show()

                return@runOnUiThread
            }

            val biometricPrompt =
                BiometricPrompt(
                    this,
                    ContextCompat.getMainExecutor(this),
                    object :
                        BiometricPrompt.AuthenticationCallback() {

                        override fun onAuthenticationSucceeded(
                            result:
                            BiometricPrompt.AuthenticationResult
                        ) {

                            super.onAuthenticationSucceeded(
                                result
                            )

                            Toast.makeText(
                                this@MainActivity,
                                "Acesso autorizado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onAuthenticationFailed() {

                            super.onAuthenticationFailed()

                            Toast.makeText(
                                this@MainActivity,
                                "Biometria não reconhecida",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )

            val promptInfo =
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(
                        "Desbloquear a área protegida"
                    )
                    .setDescription(
                        "Apenas a usuária cadastrada pode acessar este local"
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

    fun iniciarBiometriaPrincesa() {
        destinoBiometria = 1
        iniciarBiometria()
    }

    fun iniciarBiometriaPrincipe() {
        destinoBiometria = 2
        iniciarBiometria()
    }

    fun iniciarBiometriaAmor() {
        destinoBiometria = 3
        iniciarBiometria()
    }

    fun iniciarBiometriaMusica() {
        destinoBiometria = 4
        iniciarBiometria()
    }

    // =========================================================
    // FULLSCREEN
    // =========================================================

    fun ativarFullscreen() {

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

    fun desativarFullscreen() {

        val controller =
            WindowCompat.getInsetsController(
                window,
                window.decorView
            )

        controller.show(
            WindowInsetsCompat.Type.systemBars()
        )

        controller.isAppearanceLightStatusBars =
            false

        controller.isAppearanceLightNavigationBars =
            false
    }

    // =========================================================
    // LIGAÇÃO DIRETA
    // =========================================================

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

                startActivity(intent)

            } else {

                Toast.makeText(
                    this,
                    "Permissão de ligação não concedida",
                    Toast.LENGTH_SHORT
                ).show()

                val fallback =
                    Intent(
                        Intent.ACTION_DIAL
                    ).apply {

                        data =
                            Uri.parse(
                                "tel:$numero"
                            )
                    }

                startActivity(fallback)
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

            startActivity(fallback)
        }
    }

    // =========================================================
    // LOCALIZAÇÃO
    // =========================================================

    fun pegarLocalizacao() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
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

                    val intent =
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                "https://maps.google.com/?q=$lat,$lng"
                            )
                        )

                    startActivity(intent)

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
    // PERMISSÕES
    // =========================================================

    fun verificarPermissoes() {

        val faltando =
            mutableListOf<String>()

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            faltando.add(
                Manifest.permission.READ_CONTACTS
            )
        }

        val fine =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarse =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) {

            faltando.add(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            faltando.add(
                Manifest.permission.RECORD_AUDIO
            )
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            faltando.add(
                Manifest.permission.CALL_PHONE
            )
        }

        mostrarPermissoes =
            faltando.isNotEmpty()
    }

    override fun onResume() {

        super.onResume()

        verificarPermissoes()
    }

    override fun onDestroy() {

        pararSensor()

        try {

            if (
                ::telephonyManager.isInitialized &&
                telefoneListener != null
            ) {

                telephonyManager.listen(
                    telefoneListener,
                    PhoneStateListener.LISTEN_NONE
                )
            }

        } catch (_: Exception) {
        }

        telefoneListener = null

        super.onDestroy()
    }
}

// =============================================================
// INTERFACE COMPOSE
// =============================================================

@Composable
fun MulherAmparadaScreen(
    activity: MainActivity
) {

    var palmasAtiva by remember {
        mutableStateOf(activity.uiPalmasAtiva)
    }

    var movimentoAtivo by remember {
        mutableStateOf(activity.uiMovimentoAtivo)
    }

    var brilhoAtivo by remember {
        mutableStateOf(activity.uiBrilhoAtivo)
    }

    var bloqueioAtivo by remember {
        mutableStateOf(activity.uiBloqueioAtivo)
    }

    var mostrarPermissoes by remember {
        mutableStateOf(activity.mostrarPermissoes)
    }

    LaunchedEffect(Unit) {

        while (true) {

            palmasAtiva =
                activity.uiPalmasAtiva

            movimentoAtivo =
                activity.uiMovimentoAtivo

            brilhoAtivo =
                activity.uiBrilhoAtivo

            bloqueioAtivo =
                activity.uiBloqueioAtivo

            mostrarPermissoes =
                activity.mostrarPermissoes

            kotlinx.coroutines.delay(100)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Black)
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    )
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 24.dp,
                        bottom = 100.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(31.dp)
        ) {

            Hero()

            Section("Emergência") {

                PanicButton(
                    onClick = {
                        activity.enviarSOS()
                    }
                )
            }

            Section("Barulho alto") {

                ControlCard(
                    title = "Proteção por barulho",
                    description =
                        "Detecta sons altos e situações suspeitas para acionar ajuda.",
                    active = palmasAtiva,
                    accent = Pink,
                    onToggle = {

                        if (palmasAtiva) {
                            activity.desativarPalmas()
                        } else {
                            activity.ativarPalmas()
                        }
                    }
                )
            }

            Section("Movimento") {

                ControlCard(
                    title = "Balançar o celular",
                    description =
                        "Detecta o movimento do celular. Se o sensor não estiver disponível ou der qualquer outro erro, o microfone será usado como alternativa.",
                    active = movimentoAtivo,
                    accent = Purple,
                    onToggle = {

                        if (movimentoAtivo) {
                            activity.desativarProtecao()
                        } else {
                            activity.ativarProtecao()
                        }
                    }
                )
            }

            Section("Brilho") {

                ControlCard(
                    title = "Escurecimento por inclinação",
                    description =
                        "Escurece a tela automaticamente quando a função estiver ativa.",
                    active = brilhoAtivo,
                    accent = Orange,
                    onToggle = {

                        brilhoAtivo =
                            !brilhoAtivo

                        activity.uiBrilhoAtivo =
                            brilhoAtivo
                    }
                )
            }

            Section("Desligar o celular") {

                ControlCard(
                    title = "Bloqueio por barulho",
                    description =
                        "Detecta sons altos e bloqueia o celular automaticamente.",
                    active = bloqueioAtivo,
                    accent = White,
                    whiteSwitch = true,
                    onToggle = {

                        bloqueioAtivo =
                            !bloqueioAtivo

                        activity.uiBloqueioAtivo =
                            bloqueioAtivo
                    }
                )
            }

            Section("Ajuda rápida") {

                ActionCard(
                    title = "Polícia",
                    description = "Emergência policial",
                    icon = Icons.Default.Security,
                    background =
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF182744),
                                Color(0xFF0F1829)
                            )
                        ),
                    iconBackground =
                        Color(0x224F8CFF),
                    iconColor =
                        Color(0xFF8DB4FF),
                    onClick = {
                        activity.ligarDireto("190")
                    }
                )

                Spacer(
                    Modifier.height(24.dp)
                )

                ActionCard(
                    title = "SAMU",
                    description =
                        "Atendimento médico de emergência",
                    icon = Icons.Default.LocalHospital,
                    background =
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF2A2116),
                                Color(0xFF171109)
                            )
                        ),
                    iconBackground =
                        Color(0x20FF9F43),
                    iconColor =
                        OrangeLight,
                    onClick = {
                        activity.ligarDireto("192")
                    }
                )

                Spacer(
                    Modifier.height(24.dp)
                )

                ActionCard(
                    title = "Central da Mulher",
                    description =
                        "Orientação e atendimento",
                    icon = Icons.Default.Woman,
                    background =
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF281521),
                                Color(0xFF170C12)
                            )
                        ),
                    iconBackground =
                        Color(0x20FF3F82),
                    iconColor =
                        PinkLight,
                    onClick = {
                        activity.ligarDireto("180")
                    }
                )
            }

            Section("Localização") {

                ActionCard(
                    title = "Enviar localização",
                    description =
                        "Compartilhe sua localização atual",
                    icon = Icons.Default.LocationOn,
                    background =
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF112B2B),
                                Color(0xFF0B1717)
                            )
                        ),
                    iconBackground =
                        Color(0x2027C9C1),
                    iconColor =
                        Cyan,
                    minHeight = 96.dp,
                    onClick = {
                        activity.pegarLocalizacao()
                    }
                )
            }

            Section("Contatos de confiança") {

                ActionCard(
                    title = "Adicionar contato",
                    description =
                        "Escolha uma pessoa de confiança",
                    icon = Icons.Default.Person,
                    background =
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF21183A),
                                Color(0xFF120E20)
                            )
                        ),
                    iconBackground =
                        Color(0x228B5CF6),
                    iconColor =
                        PurpleLight,
                    onClick = {
                        activity.abrirContatos()
                    }
                )

                Spacer(
                    Modifier.height(24.dp)
                )

                ActionCard(
                    title = "SOS contatos",
                    description =
                        "Enviar alerta para seus contatos",
                    icon = Icons.Default.CrisisAlert,
                    background =
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF2B171D),
                                Color(0xFF170B0E)
                            )
                        ),
                    iconBackground =
                        Color(0x22FF4054),
                    iconColor =
                        Red,
                    onClick = {
                        activity.enviarSOS()
                    }
                )
            }

            Section("Área protegida") {

                ProtectedArea(
                    onClick = {
                        activity.iniciarBiometria()
                    }
                )
            }
        }

        if (mostrarPermissoes) {

            PermissionOverlay(
                onOpen = {
                    activity.abrirConfiguracoes()
                }
            )
        }
    }
}

// =============================================================
// HERO
// =============================================================

@Composable
private fun Hero() {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
    ) {

        Box(
            modifier =
                Modifier
                    .padding(bottom = 18.dp)
                    .size(
                        width = 43.dp,
                        height = 3.dp
                    )
                    .clip(
                        RoundedCornerShape(99.dp)
                    )
                    .background(Pink)
        )

        Text(
            text = "Mulher\nAmparada",
            color = White,
            fontFamily = Quicksand,
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 45.sp,
            letterSpacing = (-1.6).sp
        )

        Text(
            text =
                "Proteção, assistência e recursos reunidos em um só lugar.",
            modifier =
                Modifier
                    .padding(top = 14.dp)
                    .fillMaxWidth(0.72f),
            color = Color(0xFF85858E),
            fontFamily = Quicksand,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp
        )
    }
}

// =============================================================
// SECTION
// =============================================================

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Row(
            modifier =
                Modifier.padding(
                    start = 4.dp,
                    bottom = 13.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Pink)
            )

            Spacer(
                Modifier.size(9.dp)
            )

            Text(
                text = title.uppercase(),
                color = Muted,
                fontFamily = Quicksand,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }

        content()
    }
}

// =============================================================
// SOS
// =============================================================

@Composable
private fun PanicButton(
    onClick: () -> Unit
) {

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(
                    RoundedCornerShape(32.dp)
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFF4A5E),
                            Color(0xFFDC263D)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(0xFFFF6470),
                    RoundedCornerShape(32.dp)
                )
                .clickable {
                    onClick()
                }
    ) {

        Box(
            modifier =
                Modifier
                    .size(410.dp)
                    .offset(
                        x = 135.dp,
                        y = (-300).dp
                    )
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(
                            alpha = 0.075f
                        )
                    )
        )

        Column(
            modifier =
                Modifier.align(
                    Alignment.Center
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "SOS",
                color = White,
                fontFamily = Quicksand,
                fontSize = 88.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                letterSpacing = 6.sp,
                lineHeight = 75.sp
            )

            Text(
                text =
                    "toque para pedir ajuda",
                modifier =
                    Modifier.padding(top = 14.dp),
                color =
                    Color.White.copy(
                        alpha = 0.76f
                    ),
                fontFamily = Quicksand,
                fontSize = 11.sp,
                fontWeight =
                    FontWeight.SemiBold,
                letterSpacing = 0.6.sp
            )
        }
    }
}

// =============================================================
// CONTROL CARD
// =============================================================

@Composable
private fun ControlCard(
    title: String,
    description: String,
    active: Boolean,
    accent: Color,
    whiteSwitch: Boolean = false,
    onToggle: () -> Unit
) {

    val background =
        when {

            !active ->
                Brush.linearGradient(
                    listOf(
                        Color(0xFF111111),
                        Color(0xFF000000)
                    )
                )

            accent == Purple ->
                Brush.linearGradient(
                    listOf(
                        Color(0xFF211936),
                        Color(0xFF110D1D)
                    )
                )

            accent == Orange ->
                Brush.linearGradient(
                    listOf(
                        Color(0xFF241B0D),
                        Color(0xFF151008)
                    )
                )

            accent == White ->
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1B1C20),
                        Color(0xFF0E0F12)
                    )
                )

            else ->
                Brush.linearGradient(
                    listOf(
                        Color(0xFF241520),
                        Color(0xFF120D12)
                    )
                )
        }

    val borderColor =
        if (active) {

            accent.copy(
                alpha =
                    if (accent == White)
                        0.25f
                    else
                        0.33f
            )

        } else {

            Color.White.copy(
                alpha = 0.07f
            )
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = 128.dp
                )
                .clip(
                    RoundedCornerShape(26.dp)
                )
                .background(background)
                .border(
                    1.dp,
                    borderColor,
                    RoundedCornerShape(26.dp)
                )
                .padding(21.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(20.dp)
    ) {

        Column(
            modifier =
                Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {

            Text(
                text = title,
                color = White,
                fontFamily = Quicksand,
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                lineHeight = 24.sp
            )

            Text(
                text = description,
                color = Soft,
                fontFamily = Quicksand,
                fontSize = 11.sp,
                fontWeight =
                    FontWeight.SemiBold,
                lineHeight = 17.sp
            )

            Text(
                text =
                    if (active)
                        "ATIVADO"
                    else
                        "DESATIVADO",
                color =
                    if (active) {

                        when {

                            accent == Purple ->
                                PurpleLight

                            accent == Orange ->
                                OrangeLight

                            accent == White ->
                                White

                            else ->
                                PinkLight
                        }

                    } else {

                        Muted
                    },
                fontFamily = Quicksand,
                fontSize = 9.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )
        }

        CustomSwitch(
            checked = active,
            activeColor = accent,
            whiteMode = whiteSwitch,
            onClick = onToggle
        )
    }
}

// =============================================================
// SWITCH
// =============================================================

@Composable
private fun CustomSwitch(
    checked: Boolean,
    activeColor: Color,
    whiteMode: Boolean,
    onClick: () -> Unit
) {

    val position by
        animateFloatAsState(
            targetValue =
                if (checked) 24f else 0f,
            label = "switch"
        )

    Box(
        modifier =
            Modifier
                .size(
                    width = 58.dp,
                    height = 34.dp
                )
                .clip(
                    RoundedCornerShape(100.dp)
                )
                .background(
                    if (checked)
                        activeColor
                    else
                        Color(0xFF292C35)
                )
                .border(
                    1.dp,
                    if (checked)
                        activeColor
                    else
                        Color.White.copy(
                            alpha = 0.07f
                        ),
                    RoundedCornerShape(100.dp)
                )
                .clickable {
                    onClick()
                }
    ) {

        Box(
            modifier =
                Modifier
                    .size(26.dp)
                    .offset(
                        x = position.dp,
                        y = 3.dp
                    )
                    .clip(CircleShape)
                    .background(
                        when {

                            !checked ->
                                Color(0xFF9297A3)

                            whiteMode ->
                                Black

                            else ->
                                White
                        }
                    )
        )
    }
}

// =============================================================
// ACTION CARD
// =============================================================

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    background: Brush,
    iconBackground: Color,
    iconColor: Color,
    minHeight: Dp = 88.dp,
    onClick: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = minHeight
                )
                .clip(
                    RoundedCornerShape(26.dp)
                )
                .background(background)
                .border(
                    1.dp,
                    Color.White.copy(
                        alpha = 0.07f
                    ),
                    RoundedCornerShape(26.dp)
                )
                .clickable {
                    onClick()
                }
                .padding(17.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier =
                Modifier
                    .size(52.dp)
                    .clip(
                        RoundedCornerShape(17.dp)
                    )
                    .background(
                        iconBackground
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier =
                    Modifier.size(28.dp),
                tint = iconColor
            )
        }

        Spacer(
            Modifier.size(13.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = title,
                color = White,
                fontFamily = Quicksand,
                fontSize = 15.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                maxLines = 2,
                overflow =
                    TextOverflow.Clip
            )

            Text(
                text = description,
                color =
                    Color.White.copy(
                        alpha = 0.72f
                    ),
                fontFamily = Quicksand,
                fontSize = 10.sp,
                fontWeight =
                    FontWeight.SemiBold,
                lineHeight = 13.sp,
                maxLines = 3,
                overflow =
                    TextOverflow.Clip
            )
        }

        Spacer(
            Modifier.size(13.dp)
        )

        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        Color.Black.copy(
                            alpha = 0.16f
                        )
                    )
                    .border(
                        1.dp,
                        Color.White.copy(
                            alpha = 0.06f
                        ),
                        RoundedCornerShape(14.dp)
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.ChevronRight,
                contentDescription = null,
                modifier =
                    Modifier.size(19.dp),
                tint =
                    Color.White.copy(
                        alpha = 0.78f
                    )
            )
        }
    }
}

// =============================================================
// ÁREA PROTEGIDA
// =============================================================

@Composable
private fun ProtectedArea(
    onClick: () -> Unit
) {

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = 138.dp
                )
                .clip(
                    RoundedCornerShape(30.dp)
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF211C48),
                            Color(0xFF151329),
                            Color(0xFF0D0C16)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(0xFF8D79FF),
                    RoundedCornerShape(30.dp)
                )
                .clickable {
                    onClick()
                }
                .padding(18.dp)
    ) {

        Box(
            modifier =
                Modifier
                    .size(250.dp)
                    .offset(
                        x = 95.dp,
                        y = (-175).dp
                    )
                    .clip(CircleShape)
                    .background(
                        Color(0xFF38286D)
                    )
        )

        Box(
            modifier =
                Modifier
                    .size(115.dp)
                    .offset(
                        x = (-38).dp,
                        y = 78.dp
                    )
                    .clip(CircleShape)
                    .background(
                        Color(0xFF552052)
                    )
        )

        Row(
            modifier =
                Modifier.fillMaxSize(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(
                            RoundedCornerShape(21.dp)
                        )
                        .background(
                            Color(0xFF30245A)
                        )
                        .border(
                            1.dp,
                            Color(0xFF6252A8),
                            RoundedCornerShape(21.dp)
                        ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Lock,
                    contentDescription = null,
                    modifier =
                        Modifier.size(32.dp),
                    tint = White
                )
            }

            Spacer(
                Modifier.size(16.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                Text(
                    text = "Área protegida",
                    color = White,
                    fontFamily = Quicksand,
                    fontSize = 23.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    lineHeight = 25.sp
                )

                Text(
                    text =
                        "Acesso protegido por senha",
                    color =
                        Color(0xFFA49CAF),
                    fontFamily = Quicksand,
                    fontSize = 10.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    text = "PROTEÇÃO ATIVA",
                    color =
                        Color(0xFF756D7D),
                    fontFamily = Quicksand,
                    fontSize = 7.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            }

            Box(
                modifier =
                    Modifier
                        .size(43.dp)
                        .clip(
                            RoundedCornerShape(14.dp)
                        )
                        .background(
                            Color(0xFF211B3B)
                        )
                        .border(
                            1.dp,
                            Color(0xFF493D78),
                            RoundedCornerShape(14.dp)
                        ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier =
                        Modifier.size(19.dp),
                    tint =
                        Color(0xFFA395FF)
                )
            }
        }
    }
}

// =============================================================
// PERMISSÕES
// =============================================================

@Composable
private fun PermissionOverlay(
    onOpen: () -> Unit
) {

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Black.copy(
                        alpha = 0.98f
                    )
                ),
        contentAlignment =
            Alignment.Center
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp
                    )
                    .clip(
                        RoundedCornerShape(28.dp)
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF151318),
                                Color(0xFF0B0A0E)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color(0xFF302B35),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 28.dp,
                        bottom = 24.dp
                    )
        ) {

            Box(
                modifier =
                    Modifier
                        .size(
                            width = 42.dp,
                            height = 3.dp
                        )
                        .clip(
                            RoundedCornerShape(99.dp)
                        )
                        .background(Pink)
            )

            Text(
                text =
                    "Permissões necessárias",
                modifier =
                    Modifier.padding(
                        top = 22.dp
                    ),
                color = White,
                fontFamily = Quicksand,
                fontSize = 24.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                lineHeight = 25.sp
            )

            Text(
                text =
                    "Para aproveitar todos os recursos do Mulher Amparada, verifique as permissões do aplicativo nas configurações do Android.",
                modifier =
                    Modifier.padding(
                        top = 13.dp
                    ),
                color =
                    Color(0xFF85858E),
                fontFamily = Quicksand,
                fontSize = 11.sp,
                fontWeight =
                    FontWeight.SemiBold,
                lineHeight = 17.sp
            )

            Spacer(
                Modifier.height(20.dp)
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(
                            RoundedCornerShape(17.dp)
                        )
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Pink,
                                    PinkDark
                                )
                            )
                        )
                        .border(
                            1.dp,
                            PinkLight,
                            RoundedCornerShape(17.dp)
                        )
                        .clickable {
                            onOpen()
                        },
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        "Abrir permissões",
                    color = White,
                    fontFamily = Quicksand,
                    fontSize = 12.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }
    }
}