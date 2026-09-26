package com.mulheres

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import android.Manifest
import androidx.compose.runtime.setValue
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.log10
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import androidx.compose.runtime.CompositionLocalProvider
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat


class HubActivity : ComponentActivity() {

var palmasAtivas by mutableStateOf(false)
    private set

private var palmasEmExecucao = false

var permissoesConcedidas by mutableStateOf(false)
    private set

private lateinit var telephonyManager: TelephonyManager

private lateinit var sensorManager: SensorManager

private lateinit var shakeListener: SensorEventListener

var protecaoMovimentoAtiva by mutableStateOf(false)
    private set

private var ultimoShake: Long = 0L

private var shakeAudioRecord: AudioRecord? = null

private var shakeMicrophoneThread: Thread? = null

private var shakeMicrophoneRunning = false

private val shakeSampleRate = 44100

private val shakeBufferSize =
    AudioRecord.getMinBufferSize(
        shakeSampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

private val shakeLoudSoundThreshold = -50.0

private lateinit var tiltBrightness: TiltBrightnessController

var escurecimentoAtivo by mutableStateOf(false)
    private set

private var acelerometro: Sensor? = null

private var telephonyCallback: TelephonyCallback? = null

    /* =========================================================
       PERMISSÕES
    ========================================================= */

    fun verificarPermissoes() {

        val contatos =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED

        val localizacaoFine =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val localizacaoCoarse =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val microfone =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        val telefone =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED

        permissoesConcedidas =
            contatos &&
            (localizacaoFine || localizacaoCoarse) &&
            microfone &&
            telefone
    }

fun ativarEscurecimento() {

    if (escurecimentoAtivo) {
        return
    }

    escurecimentoAtivo = true

    tiltBrightness.startTiltBrightness()
}

fun desativarEscurecimento() {

    if (!escurecimentoAtivo) {
        return
    }

    escurecimentoAtivo = false

    tiltBrightness.stopTiltBrightness()
}

    fun abrirPermissoes() {

        val intent =
            Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
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

fun ativarFullscreen() {

    WindowCompat.setDecorFitsSystemWindows(
        window,
        false
    )

    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).apply {

        hide(
            androidx.core.view.WindowInsetsCompat.Type.systemBars()
        )

        systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun desativarFullscreen() {

    WindowCompat.setDecorFitsSystemWindows(
        window,
        false
    )

    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(
        androidx.core.view.WindowInsetsCompat.Type.systemBars()
    )
}

    /* =========================================================
       CICLO DE VIDA
    ========================================================= */

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        verificarPermissoes()

sensorManager =
    getSystemService(
        Context.SENSOR_SERVICE
    ) as SensorManager


criarShakeListener()
        setContent {

            MulherAmparadaTheme {

                MulherAmparadaScreen()
            }
        }
        
        tiltBrightness =
    TiltBrightnessController(
        activity = this,
        sensorManager = sensorManager,

        onEnterFullscreen = {
            ativarFullscreen()
        },

        onExitFullscreen = {
            desativarFullscreen()
        }
    )   
    
    }


    override fun onResume() {
        super.onResume()

        verificarPermissoes()
    }

override fun onDestroy() {

if (::tiltBrightness.isInitialized) {
        tiltBrightness.stop()
    }


if (::sensorManager.isInitialized) {

    if (::shakeListener.isInitialized) {

        sensorManager.unregisterListener(
            shakeListener
        )
    }
}

pararMicrofoneShake()

    telephonyCallback?.let {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {

            try {

                telephonyManager
                    .unregisterTelephonyCallback(it)

            } catch (
                _: Exception
            ) {
            }
        }
    }

    telephonyCallback = null

    super.onDestroy()
}

private fun criarShakeListener() {

    shakeListener =
        object : SensorEventListener {

            override fun onSensorChanged(
                event: SensorEvent
            ) {

                if (!protecaoMovimentoAtiva) {
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

fun ativarProtecaoMovimento() {

    if (protecaoMovimentoAtiva) {
        return
    }

    protecaoMovimentoAtiva = true

    acelerometro =
        sensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER
        )

    if (acelerometro == null) {

        iniciarMicrofoneShake()

        return
    }

    val registrado =
        sensorManager.registerListener(
            shakeListener,
            acelerometro,
            SensorManager.SENSOR_DELAY_GAME
        )

    if (!registrado) {
        iniciarMicrofoneShake()
    }
}


fun desativarProtecaoMovimento() {

    protecaoMovimentoAtiva = false

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
// AÇÃO DO BALANÇAR
// =========================================================

private fun executarAcaoShake() {

    if (!protecaoMovimentoAtiva) {
        return
    }

    val agora =
        System.currentTimeMillis()

    if (
        agora - ultimoShake <= 4000
    ) {
        return
    }

    ultimoShake =
        agora

    runOnUiThread {

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

            desativarProtecaoMovimento()

        } catch (
            e: Exception
        ) {

            e.printStackTrace()
        }
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
// MICROFONE — FALLBACK
// =========================================================

private fun iniciarMicrofoneShake() {

    if (
        !protecaoMovimentoAtiva ||
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
                        protecaoMovimentoAtiva
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

    /* =========================================================
       LIGAÇÃO DIRETA
    ========================================================= */

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
            }

        } catch (
            e: Exception
        ) {

            e.printStackTrace()

            try {

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

            } catch (
                _: Exception
            ) {
            }
        }
    }


    /* =========================================================
       PALMAS
    ========================================================= */

    fun ativarPalmas() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(
                this,
                "Ative o microfone nas configurações do aplicativo.",
                Toast.LENGTH_LONG
            ).show()

            palmasAtivas = false

            return
        }

        palmasAtivas = true

        palmasEmExecucao = false

        monitorarFimDaLigacao()

        val intent =
            Intent(
                this,
                PalmaService::class.java
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            startForegroundService(intent)

        } else {

            startService(intent)
        }
    }


    fun desativarPalmas() {

        stopService(
            Intent(
                this,
                PalmaService::class.java
            )
        )

        palmasAtivas = false

        Toast.makeText(
            this,
            "Proteção por palmas desativada",
            Toast.LENGTH_SHORT
        ).show()
    }


    /* =========================================================
       RECEPTOR
    ========================================================= */

    private val receptorPalmasConcluidas =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                if (
                    intent?.action ==
                    "com.mulheres.PALMAS_CONCLUIDAS"
                ) {

                    palmasAtivas = false
                }
            }
        }


    /* =========================================================
       TELEFONIA
    ========================================================= */

    private fun monitorarFimDaLigacao() {

        if (
            !::telephonyManager.isInitialized
        ) {

            telephonyManager =
                getSystemService(
                    Context.TELEPHONY_SERVICE
                ) as TelephonyManager
        }

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.S
        ) {
            return
        }

        telephonyCallback?.let {

            try {

                telephonyManager
                    .unregisterTelephonyCallback(it)

            } catch (
                _: Exception
            ) {
            }
        }

        val callback =
            object :
                TelephonyCallback(),
                TelephonyCallback.CallStateListener {

                override fun onCallStateChanged(
                    state: Int
                ) {

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

                        palmasAtivas = false

                        telephonyCallback?.let {

                            try {

                                telephonyManager
                                    .unregisterTelephonyCallback(
                                        it
                                    )

                            } catch (
                                _: Exception
                            ) {
                            }
                        }

                        telephonyCallback = null
                    }
                }
            }

        telephonyCallback = callback

        try {

            telephonyManager.registerTelephonyCallback(
                mainExecutor,
                callback
            )

        } catch (
            e: Exception
        ) {

            e.printStackTrace()

            telephonyCallback = null
        }
    }
}


/* =========================================================
   CORES
========================================================= */

private data class AppColors(

    val background: Color,

    val surface: Color,

    val surfaceLight: Color,

    val white: Color,

    val text: Color,

    val secondary: Color,

    val muted: Color,

    val border: Color,

    val borderLight: Color,

    val pink: Color,

    val pinkLight: Color,

    val red: Color,

    val purple: Color,

    val orange: Color,

    val cyan: Color,

    val blue: Color,

    val green: Color,

    val black: Color,

    val actionBlueGradient: List<Color>,

    val actionOrangeGradient: List<Color>,

    val actionPinkGradient: List<Color>,

    val actionCyanGradient: List<Color>,

    val actionPurpleGradient: List<Color>,

    val actionRedGradient: List<Color>,

    val emergencyGradient: List<Color>,

    val protectedGradient: List<Color>,

    val amparoGradient: List<Color>
)


@Composable
private fun colors(): AppColors {

    val dark =
        isSystemInDarkTheme()

    return if (dark) {

        AppColors(

            background =
                Color(0xFF050507),

            surface =
                Color(0xFF111116),

            surfaceLight =
                Color(0xFF18181F),

            white =
                Color.White,

            text =
                Color(0xFFEEEEF4),

            secondary =
                Color(0xFFAAAAB8),

            muted =
                Color(0xFF70707E),

            border =
                Color(0x14FFFFFF),

            borderLight =
                Color(0x25FFFFFF),

            pink =
                Color(0xFFFF3D82),

            pinkLight =
                Color(0xFFFF8FB5),

            red =
                Color(0xFFFF465B),

            purple =
                Color(0xFF9A7CFF),

            orange =
                Color(0xFFFFB45E),

            cyan =
                Color(0xFF54DED4),

            blue =
                Color(0xFF729FFF),

            green =
                Color(0xFF69DFA1),

            black =
                Color.Black,

            actionBlueGradient =
                listOf(
                    Color(0xFF182844),
                    Color(0xFF101A2D),
                    Color(0xFF0D1423)
                ),

            actionOrangeGradient =
                listOf(
                    Color(0xFF3A1D19),
                    Color(0xFF281512),
                    Color(0xFF17100E)
                ),

            actionPinkGradient =
                listOf(
                    Color(0xFF3C1B2C),
                    Color(0xFF281522),
                    Color(0xFF1A1018)
                ),

            actionCyanGradient =
                listOf(
                    Color(0xFF153434),
                    Color(0xFF102727),
                    Color(0xFF0B1B1B)
                ),

            actionPurpleGradient =
                listOf(
                    Color(0xFF2D214B),
                    Color(0xFF1E1733),
                    Color(0xFF151024)
                ),

            actionRedGradient =
                listOf(
                    Color(0xFF3C1B25),
                    Color(0xFF2B131B),
                    Color(0xFF1C0D13)
                ),

            emergencyGradient =
                listOf(
                    Color(0xFF432018),
                    Color(0xFF2D1710),
                    Color(0xFF1A0C09)
                ),

            protectedGradient =
                listOf(
                    Color(0xFF292052),
                    Color(0xFF211A3C),
                    Color(0xFF151329)
                ),

            amparoGradient =
                listOf(
                    Color(0xFF3B2047),
                    Color(0xFF2B1936),
                    Color(0xFF1B1124)
                )
        )

    } else {

        AppColors(

            background =
                Color(0xFFF8F7FA),

            surface =
                Color(0xFFFFFFFF),

            surfaceLight =
                Color(0xFFF1EFF4),

            white =
                Color(0xFF17151A),

            text =
                Color(0xFF17151A),

            secondary =
                Color(0xFF66636D),

            muted =
                Color(0xFF85818B),

            border =
                Color(0x18000000),

            borderLight =
                Color(0x25000000),

            pink =
                Color(0xFFE82E70),

            pinkLight =
                Color(0xFFD92D68),

            red =
                Color(0xFFE7354D),

            purple =
                Color(0xFF765BE0),

            orange =
                Color(0xFFD98726),

            cyan =
                Color(0xFF159E96),

            blue =
                Color(0xFF4678DB),

            green =
                Color(0xFF269A68),

            black =
                Color.Black,

            actionBlueGradient =
                listOf(
                    Color(0xFFEAF1FF),
                    Color(0xFFF3F6FC),
                    Color(0xFFFFFFFF)
                ),

            actionOrangeGradient =
                listOf(
                    Color(0xFFFFEEE9),
                    Color(0xFFFFF5F2),
                    Color(0xFFFFFFFF)
                ),

            actionPinkGradient =
                listOf(
                    Color(0xFFFFEAF2),
                    Color(0xFFFFF3F7),
                    Color(0xFFFFFFFF)
                ),

            actionCyanGradient =
                listOf(
                    Color(0xFFE5F8F7),
                    Color(0xFFF1FBFA),
                    Color(0xFFFFFFFF)
                ),

            actionPurpleGradient =
                listOf(
                    Color(0xFFF0EBFF),
                    Color(0xFFF7F4FF),
                    Color(0xFFFFFFFF)
                ),

            actionRedGradient =
                listOf(
                    Color(0xFFFFEAED),
                    Color(0xFFFFF3F4),
                    Color(0xFFFFFFFF)
                ),

            emergencyGradient =
                listOf(
                    Color(0xFFFFF0E8),
                    Color(0xFFFFF6F2),
                    Color(0xFFFFFFFF)
                ),

            protectedGradient =
                listOf(
                    Color(0xFFF0EBFF),
                    Color(0xFFF6F3FF),
                    Color(0xFFFFFFFF)
                ),

            amparoGradient =
                listOf(
                    Color(0xFFF8EAFB),
                    Color(0xFFFCF4FD),
                    Color(0xFFFFFFFF)
                )
        )
    }
}


/* =========================================================
   TEMA
========================================================= */

@Composable
private fun MulherAmparadaTheme(
    content: @Composable () -> Unit
) {

    val c =
        colors()

    val context =
        LocalContext.current

    val dark =
        isSystemInDarkTheme()

    androidx.compose.runtime.SideEffect {

    val window =
        (context as ComponentActivity).window

    WindowCompat.setDecorFitsSystemWindows(
        window,
        false
    )

    window.statusBarColor =
        AndroidColor.TRANSPARENT

    window.navigationBarColor =
        AndroidColor.TRANSPARENT

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }

    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).apply {

        isAppearanceLightStatusBars =
            !dark

        isAppearanceLightNavigationBars =
            !dark
    }
}

    MaterialTheme(

        colorScheme =
            if (dark) {

                darkColorScheme(
                    background = c.background,
                    surface = c.surface,
                    primary = c.pink
                )

            } else {

                lightColorScheme(
                    background = c.background,
                    surface = c.surface,
                    primary = c.pink
                )
            },

        content = content
    )
}


/* =========================================================
   FONTE
========================================================= */

@Composable
private fun quicksand(): FontFamily {

    return FontFamily(

        Font(
            resId = R.font.quicksand,
            weight = FontWeight.Normal
        ),

        Font(
            resId = R.font.quicksand,
            weight = FontWeight.Medium
        ),

        Font(
            resId = R.font.quicksand,
            weight = FontWeight.SemiBold
        ),

        Font(
            resId = R.font.quicksand,
            weight = FontWeight.Bold
        ),

        Font(
            resId = R.font.quicksand,
            weight = FontWeight.ExtraBold
        )
    )
}


/* =========================================================
   TELA PRINCIPAL
========================================================= */

@Composable
private fun MulherAmparadaScreen() {

    val c =
        colors()

    val font =
        quicksand()

    val context =
        LocalContext.current

    val activity =
        context as? HubActivity

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        CompositionLocalProvider(
    LocalOverscrollFactory provides null
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(c.background)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    start = 14.dp,
                    end = 14.dp
                )
                .padding(
                    top =
                        WindowInsets.statusBars
                            .asPaddingValues()
                            .calculateTopPadding() + 20.dp,

                    bottom =
                        WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding() + 90.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(30.dp)
    ) {

        // TODO: todo o seu conteúdo atual
   

            Hero(
                c = c,
                font = font
            )

            SystemOverview(
                c = c,
                font = font
            )

            SectionTitle(
                text = "Resposta imediata",
                c = c,
                font = font
            )

            PanicCard(
                c = c,
                font = font
            )

            SectionTitle(
                text = "Proteções automáticas",
                c = c,
                font = font
            )

            SensorCard(
                number = "01 / ÁUDIO",
                title = "Proteção por barulho",
                description =
                    "Detecta sons altos e situações suspeitas para acionar os mecanismos de proteção.",
                icon = R.drawable.ic_0003,
                color = c.pink,
                active =
                    activity?.palmasAtivas
                        ?: false,
                onClick = {

                    activity?.let {

                        if (
                            it.palmasAtivas
                        ) {

                            it.desativarPalmas()

                        } else {

                            it.ativarPalmas()
                        }
                    }
                },
                c = c,
                font = font
            )

            SensorCard(
    number = "02 / MOVIMENTO",
    title = "Proteção por movimento",
    description =
        "Detecta movimentos bruscos no celular e pode iniciar uma resposta de emergência.",
    icon = R.drawable.ic_0004,
    color = c.purple,
    active =
        activity?.protecaoMovimentoAtiva
            ?: false,
    onClick = {

        activity?.let {

            if (
                it.protecaoMovimentoAtiva
            ) {

                it.desativarProtecaoMovimento()

            } else {

                it.ativarProtecaoMovimento()
            }
        }
    },
    c = c,
    font = font
)

            SensorCard(
    number = "03 / VISIBILIDADE",
    title = "Escurecimento por inclinação",
    description =
        "Escurece a tela automaticamente quando o dispositivo identifica a posição configurada.",
    icon = R.drawable.ic_0005,
    color = c.orange,
    active =
        activity?.escurecimentoAtivo
            ?: false,

    onClick = {

        activity?.let {

            if (it.escurecimentoAtivo) {
                it.desativarEscurecimento()
            } else {
                it.ativarEscurecimento()
            }
        }
    },

    c = c,
    font = font
)

            SensorCard(
                number = "04 / BLOQUEIO",
                title = "Bloqueio por barulho",
                description =
                    "Detecta sons altos e bloqueia o celular automaticamente quando ativado.",
                icon = R.drawable.ic_0006,
                color = c.cyan,
                active = false,
                onClick = {},
                c = c,
                font = font
            )

            SectionTitle(
                text = "Serviços de emergência",
                c = c,
                font = font
            )

            ActionCard(
                title = "Polícia — 190",
                description =
                    "Emergência policial e atendimento imediato",
                icon = R.drawable.ic_0007,
                arrow = R.drawable.ic_arrow,
                gradient = c.actionBlueGradient,
                accent = c.blue,
                c = c,
                font = font
            )

            ActionCard(
                title = "SAMU — 192",
                description =
                    "Atendimento médico de emergência",
                icon = R.drawable.ic_0008,
                arrow = R.drawable.ic_arrow,
                gradient = c.actionOrangeGradient,
                accent = c.orange,
                c = c,
                font = font
            )

            ActionCard(
                title = "Central da Mulher — 180",
                description =
                    "Orientação, acolhimento e atendimento",
                icon = R.drawable.ic_0009,
                arrow = R.drawable.ic_arrow,
                gradient = c.actionPinkGradient,
                accent = c.pinkLight,
                c = c,
                font = font
            )

            SectionTitle(
                text = "Recursos de apoio",
                c = c,
                font = font
            )

            ActionCard(
                title = "Enviar localização",
                description =
                    "Compartilhe sua localização atual",
                icon = R.drawable.ic_0010,
                arrow = R.drawable.ic_arrow,
                gradient = c.actionCyanGradient,
                accent = c.cyan,
                c = c,
                font = font
            )

            SectionTitle(
                text = "Contatos de confiança",
                c = c,
                font = font
            )

            ActionCard(
                title = "Adicionar contato",
                description =
                    "Escolha uma pessoa de confiança",
                icon = R.drawable.ic_0011,
                arrow = R.drawable.ic_arrow,
                gradient = c.actionPurpleGradient,
                accent = c.purple,
                c = c,
                font = font
            )

            ActionCard(
                title = "SOS para contatos",
                description =
                    "Envie um alerta para pessoas de confiança",
                icon = R.drawable.ic_0012,
                arrow = R.drawable.ic_arrow,
                gradient = c.actionRedGradient,
                accent = c.red,
                c = c,
                font = font
            )

            SectionTitle(
                text = "Acesso rápido",
                c = c,
                font = font
            )

            EmergencyAccess(
                c = c,
                font = font
            )

            SectionTitle(
                text = "Espaços protegidos",
                purple = true,
                c = c,
                font = font
            )

            ProtectedArea(
                c = c,
                font = font
            )

            SectionTitle(
                text = "Espaço de acolhimento",
                purple = true,
                c = c,
                font = font
            )

            AmparoArea(
                c = c,
                font = font
            )
        }


        /* =====================================================
           BLOQUEIO DE PERMISSÕES
        ===================================================== */

        if (
            activity != null &&
            !activity.permissoesConcedidas
        ) {

            Box(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .zIndex(100f),

                contentAlignment =
                    Alignment.Center
            ) {

                PermissionCard(
                    onAbrirPermissoes = {
                        activity.abrirPermissoes()
                    }
                )
            }
        }
    }
}
}


/* =========================================================
   POPUP DE PERMISSÕES
========================================================= */

@Composable
private fun PermissionCard(
    onAbrirPermissoes: () -> Unit
) {

    val font =
        quicksand()

    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .widthIn(max = 430.dp)
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
                    top = 28.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp
                )
    ) {

        Box(

            modifier =
                Modifier
                    .width(42.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFFFF3F82)
                    )
        )

        Spacer(
            Modifier.height(22.dp)
        )

        Text(
            text = "Permissões necessárias",
            color = Color.White,
            fontFamily = font,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 25.sp,
            letterSpacing = (-0.5).sp
        )

        Spacer(
            Modifier.height(13.dp)
        )

        Text(
            text =
                "Para aproveitar todos os recursos do Mulher Amparada, " +
                "verifique as permissões do aplicativo nas configurações " +
                "do Android.",
            color = Color(0xFF85858E),
            fontFamily = font,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 17.sp
        )

        Spacer(
            Modifier.height(24.dp)
        )

        Button(

            onClick =
                onAbrirPermissoes,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp),

            shape =
                RoundedCornerShape(17.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFFFF3F82)
                ),

            contentPadding =
                PaddingValues(0.dp)
        ) {

            Box(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFFF3F82),
                                    Color(0xFFDB2C6D)
                                )
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "Abrir permissões",
                    color = Color.White,
                    fontFamily = font,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}


/* =========================================================
   HERO
========================================================= */

@Composable
private fun Hero(
    c: AppColors,
    font: FontFamily
) {

    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 3.dp,
                    end = 3.dp,
                    top = 8.dp,
                    bottom = 25.dp
                )
    ) {

        Box(

            modifier =
                Modifier
                    .width(48.dp)
                    .height(5.dp)
                    .clip(
                        RoundedCornerShape(50)
                    )
                    .background(c.pink)
        )

        Spacer(
            Modifier.height(21.dp)
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(

                modifier =
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(c.pink)
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                text =
                    "SISTEMA DE PROTEÇÃO PESSOAL",
                color = c.pinkLight,
                fontFamily = font,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }

        Spacer(
            Modifier.height(13.dp)
        )

        Text(
            text =
                "Mulher\nAmparada.",
            color = c.white,
            fontFamily = font,
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 43.sp,
            letterSpacing = (-3).sp
        )

        Spacer(
            Modifier.height(20.dp)
        )

        Text(
            text =
                "Tecnologia criada para oferecer proteção, assistência e resposta rápida quando você precisar.",
            modifier =
                Modifier.widthIn(
                    max = 370.dp
                ),
            color = c.secondary,
            fontFamily = font,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp
        )
    }
}


/* =========================================================
   VISÃO GERAL
========================================================= */

@Composable
private fun SystemOverview(
    c: AppColors,
    font: FontFamily
) {

    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(32.dp)
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            c.surfaceLight,
                            c.surface
                        )
                    )
                )
                .border(
                    1.dp,
                    c.borderLight,
                    RoundedCornerShape(32.dp)
                )
                .padding(22.dp)
    ) {

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column(

                modifier =
                    Modifier.weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    "ESTADO DO SISTEMA",
                    color = c.muted,
                    fontFamily = font,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )

                Text(
                    "Central de proteção",
                    color = c.text,
                    fontFamily = font,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    "Gerencie seus recursos de segurança e mantenha suas ferramentas de emergência sempre acessíveis.",
                    color = c.secondary,
                    fontFamily = font,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(

                        modifier =
                            Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(c.green)
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text(
                        "SISTEMA DISPONÍVEL",
                        color = c.green,
                        fontFamily = font,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = .8.sp
                    )
                }
            }

            Box(

                modifier =
                    Modifier
                        .size(67.dp)
                        .clip(
                            RoundedCornerShape(24.dp)
                        )
                        .background(
                            c.pink.copy(.10f)
                        )
                        .border(
                            1.dp,
                            c.pink.copy(.35f),
                            RoundedCornerShape(24.dp)
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Box(

                    modifier =
                        Modifier
                            .size(49.dp)
                            .border(
                                1.dp,
                                c.pink.copy(.25f),
                                RoundedCornerShape(19.dp)
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Image(
                        painter =
                            painterResource(
                                R.drawable.ic_0001
                            ),
                        contentDescription = null,
                        modifier =
                            Modifier.size(32.dp),
                        contentScale =
                            ContentScale.Fit
                    )
                }
            }
        }
    }
}


/* =========================================================
   TÍTULO DE SEÇÃO
========================================================= */

@Composable
private fun SectionTitle(
    text: String,
    c: AppColors,
    font: FontFamily,
    purple: Boolean = false
) {

    Row(

        verticalAlignment =
            Alignment.CenterVertically,

        modifier =
            Modifier.padding(
                start = 3.dp
            )
    ) {

        Box(

            modifier =
                Modifier
                    .size(7.dp)
                    .clip(
                        RoundedCornerShape(2.dp)
                    )
                    .background(
                        if (purple)
                            c.purple
                        else
                            c.pink
                    )
        )

        Spacer(
            Modifier.width(10.dp)
        )

        Text(
            text =
                text.uppercase(),
            color = c.muted,
            fontFamily = font,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
    }
}


/* =========================================================
   PANIC
========================================================= */

@Composable
private fun PanicCard(
    c: AppColors,
    font: FontFamily
) {

    val context =
        LocalContext.current

    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(
                    RoundedCornerShape(32.dp)
                )
                .background(
                    if (
                        isSystemInDarkTheme()
                    )
                        Color(0xFFFF315D)
                    else
                        Color(0xFFE7354D)
                )
                .border(
                    1.dp,
                    if (
                        isSystemInDarkTheme()
                    )
                        Color(0xFFFF6B87)
                    else
                        Color(0xFFFF7184),
                    RoundedCornerShape(32.dp)
                )
                .clickable {

                    (context as? HubActivity)
                        ?.ligarDireto("180")
                },

        contentAlignment =
            Alignment.Center
    ) {

        Box(

            modifier =
                Modifier
                    .size(210.dp)
                    .offset(
                        x = 75.dp,
                        y = (-125).dp
                    )
                    .clip(CircleShape)
                    .background(
                        if (
                            isSystemInDarkTheme()
                        )
                            Color(0xFFFF6684)
                        else
                            Color(0xFFF75A76)
                    )
        )

        Box(

            modifier =
                Modifier
                    .size(250.dp)
                    .offset(
                        x = (-105).dp,
                        y = 170.dp
                    )
                    .clip(CircleShape)
                    .background(
                        if (
                            isSystemInDarkTheme()
                        )
                            Color(0xFFE91E4F)
                        else
                            Color(0xFFD82C49)
                    )
        )

        Column(

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            Image(
                painter =
                    painterResource(
                        R.drawable.ic_0002
                    ),
                contentDescription = null,
                modifier =
                    Modifier.size(62.dp)
            )

            Text(
                "Precisa de ajuda?",
                color = Color.White,
                fontFamily = font,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center
            )

            Text(
                "Toque aqui para acionar o suporte de emergência.",
                modifier =
                    Modifier.widthIn(
                        max = 280.dp
                    ),
                color =
                    Color.White.copy(
                        alpha = .92f
                    ),
                fontFamily = font,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


/* =========================================================
   SENSOR
========================================================= */
@Composable
private fun SensorCard(
    number: String,
    title: String,
    description: String,
    icon: Int,
    color: Color,
    active: Boolean,
    onClick: () -> Unit,
    c: AppColors,
    font: FontFamily
) {

    val background =
        if (active) {
            Brush.linearGradient(
                listOf(
                    color.copy(.25f),
                    color.copy(.12f),
                    c.surface
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    c.surface,
                    c.surface
                )
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(
                    RoundedCornerShape(25.dp)
                )
                .background(background)
                .border(
                    1.dp,
                    color.copy(
                        alpha =
                            if (active)
                                .56f
                            else
                                .19f
                    ),
                    RoundedCornerShape(25.dp)
                )
                .clickable {
                    onClick()
                }
                .padding(22.dp),

        verticalArrangement =
            Arrangement.SpaceBetween
    ) {

        /* =================================================
           TOPO
        ================================================= */

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.Top
        ) {

            Column(
                modifier =
                    Modifier.weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = number,
                    color = c.muted,
                    fontFamily = font,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = title,
                    color = c.text,
                    fontFamily = font,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 25.sp,
                    letterSpacing = (-.7).sp,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

            Spacer(
                Modifier.width(14.dp)
            )

            /* ÍCONE */

            Box(
                modifier =
                    Modifier
                        .size(55.dp)
                        .clip(
                            RoundedCornerShape(20.dp)
                        )
                        .background(
                            color.copy(.075f)
                        )
                        .border(
                            1.dp,
                            color.copy(.27f),
                            RoundedCornerShape(20.dp)
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(39.dp)
                            .border(
                                1.dp,
                                c.borderLight,
                                RoundedCornerShape(16.dp)
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Image(
                        painter =
                            painterResource(icon),

                        contentDescription = null,

                        modifier =
                            Modifier.size(24.dp)
                    )
                }
            }
        }

        /* =================================================
           DESCRIÇÃO
        ================================================= */

        Text(
            text = description,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 4.dp,
                        end = 4.dp
                    ),

            color = c.secondary,

            fontFamily = font,

            fontSize = 10.sp,

            fontWeight =
                FontWeight.SemiBold,

            lineHeight = 16.sp,

            maxLines = 3
        )

        /* =================================================
           RODAPÉ
        ================================================= */

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (active)
                                    color
                                else
                                    c.muted
                            )
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    text =
                        if (active)
                            "ATIVADO"
                        else
                            "DESATIVADO",

                    color =
                        if (active)
                            color
                        else
                            c.muted,

                    fontFamily = font,

                    fontSize = 9.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    letterSpacing = 1.2.sp
                )
            }

            FakeSwitch(
                color = color,
                active = active,
                c = c
            )
        }
    }
}


/* =========================================================
   SWITCH
========================================================= */

@Composable
private fun FakeSwitch(
    color: Color,
    active: Boolean,
    c: AppColors
) {

    Box(

        modifier =
            Modifier
                .width(55.dp)
                .height(36.dp)
                .clip(CircleShape)
                .background(
                    if (active)
                        color.copy(.35f)
                    else
                        c.surfaceLight
                )
                .border(
                    1.dp,
                    if (active)
                        color.copy(.75f)
                    else
                        c.borderLight,
                    CircleShape
                )
                .padding(3.dp),

        contentAlignment =
            if (active)
                Alignment.CenterEnd
            else
                Alignment.CenterStart
    ) {

        Box(

            modifier =
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (active)
                            color
                        else
                            c.muted
                    )
        )
    }
}


/* =========================================================
   CARDS DE AÇÃO
========================================================= */

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: Int,
    arrow: Int,
    gradient: List<Color>,
    accent: Color,
    c: AppColors,
    font: FontFamily
) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(128.dp)
                .clip(
                    RoundedCornerShape(27.dp)
                )
                .background(
                    Brush.linearGradient(gradient)
                )
                .border(
                    1.dp,
                    accent.copy(.31f),
                    RoundedCornerShape(27.dp)
                )
                .padding(20.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(

            modifier =
                Modifier
                    .size(66.dp)
                    .clip(
                        RoundedCornerShape(23.dp)
                    )
                    .background(
                        accent.copy(.09f)
                    )
                    .border(
                        1.dp,
                        accent.copy(.32f),
                        RoundedCornerShape(23.dp)
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Image(
                painter =
                    painterResource(icon),
                contentDescription = null,
                modifier =
                    Modifier.size(35.dp)
            )
        }

        Spacer(
            Modifier.width(16.dp)
        )

        Column(

            modifier =
                Modifier.weight(1f),

            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Text(
                title,
                color =
                    if (
                        isSystemInDarkTheme()
                    )
                        Color.White
                    else
                        c.text,
                fontFamily = font,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 18.sp
            )

            Text(
                description,
                color =
                    if (
                        isSystemInDarkTheme()
                    )
                        Color.White.copy(.66f)
                    else
                        c.secondary,
                fontFamily = font,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 15.sp
            )

            Text(
                "ATENDIMENTO DISPONÍVEL",
                color = accent,
                fontFamily = font,
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        Spacer(
            Modifier.width(16.dp)
        )

        Box(

            modifier =
                Modifier
                    .size(43.dp)
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
                    .background(
                        if (
                            isSystemInDarkTheme()
                        )
                            Color.Black.copy(.13f)
                        else
                            Color.Black.copy(.05f)
                    )
                    .border(
                        1.dp,
                        accent.copy(.22f),
                        RoundedCornerShape(16.dp)
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Image(
                painter =
                    painterResource(arrow),
                contentDescription = null,
                modifier =
                    Modifier.size(19.dp)
            )
        }
    }
}


/* =========================================================
   ACESSO DE EMERGÊNCIA
========================================================= */

@Composable
private fun EmergencyAccess(
    c: AppColors,
    font: FontFamily
) {

    ActionPortal(

        title =
            "Acesso de emergência",

        subtitle =
            "Exiba o botão flutuante para pedir ajuda mesmo fora do aplicativo.",

        meta =
            "ACESSO EXTERNO",

        icon =
            R.drawable.ic_0013,

        accent =
            if (isSystemInDarkTheme())
                Color(0xFFFFCF66)
            else
                Color(0xFFB87900),

        gradient =
            c.emergencyGradient,

        c = c,
        font = font
    )
}


/* =========================================================
   ÁREA PROTEGIDA
========================================================= */

@Composable
private fun ProtectedArea(
    c: AppColors,
    font: FontFamily
) {

    ActionPortal(

        title =
            "Área protegida",

        subtitle =
            "Acesso protegido por biometria",

        meta =
            "AMBIENTE SEGURO",

        icon =
            R.drawable.ic_lock,

        accent =
            if (isSystemInDarkTheme())
                Color(0xFFB9A7FF)
            else
                Color(0xFF684EC4),

        gradient =
            c.protectedGradient,

        c = c,
        font = font
    )
}


/* =========================================================
   ÁREA DO AMPARO
========================================================= */

@Composable
private fun AmparoArea(
    c: AppColors,
    font: FontFamily
) {

    ActionPortal(

        title =
            "Área do amparo",

        subtitle =
            "Acesso protegido por biometria",

        meta =
            "ESPAÇO RESERVADO",

        icon =
            R.drawable.ic_0015,

        accent =
            if (isSystemInDarkTheme())
                Color(0xFFE2A2F0)
            else
                Color(0xFFAD62C2),

        gradient =
            c.amparoGradient,

        c = c,
        font = font
    )
}


/* =========================================================
   PORTAL
========================================================= */

@Composable
private fun ActionPortal(
    title: String,
    subtitle: String,
    meta: String,
    icon: Int,
    accent: Color,
    gradient: List<Color>,
    c: AppColors,
    font: FontFamily
) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(185.dp)
                .clip(
                    RoundedCornerShape(28.dp)
                )
                .background(
                    Brush.linearGradient(gradient)
                )
                .border(
                    1.dp,
                    accent.copy(.43f),
                    RoundedCornerShape(28.dp)
                )
                .padding(20.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(

            modifier =
                Modifier
                    .size(59.dp)
                    .clip(
                        RoundedCornerShape(20.dp)
                    )
                    .background(
                        accent.copy(.09f)
                    )
                    .border(
                        1.dp,
                        accent.copy(.45f),
                        RoundedCornerShape(20.dp)
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Box(

                modifier =
                    Modifier
                        .size(47.dp)
                        .border(
                            1.dp,
                            accent.copy(.25f),
                            RoundedCornerShape(16.dp)
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Image(
                    painter =
                        painterResource(icon),
                    contentDescription = null,
                    modifier =
                        Modifier.size(31.dp)
                )
            }
        }

        Spacer(
            Modifier.width(12.dp)
        )

        Column(

            modifier =
                Modifier.weight(1f),

            verticalArrangement =
                Arrangement.spacedBy(9.dp)
        ) {

            Text(
                title,
                color =
                    if (
                        isSystemInDarkTheme()
                    )
                        Color.White
                    else
                        c.text,
                fontFamily = font,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )

            Text(
                subtitle,
                color = accent.copy(.85f),
                fontFamily = font,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 14.sp
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(

                    modifier =
                        Modifier
                            .size(6.dp)
                            .border(
                                1.dp,
                                accent,
                                RoundedCornerShape(2.dp)
                            )
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    meta,
                    color = accent,
                    fontFamily = font,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.3.sp
                )
            }
        }

        Spacer(
            Modifier.width(12.dp)
        )

        Box(

            modifier =
                Modifier
                    .size(39.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        accent.copy(.08f)
                    )
                    .border(
                        1.dp,
                        accent.copy(.32f),
                        RoundedCornerShape(14.dp)
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Image(
                painter =
                    painterResource(
                        R.drawable.ic_arrow
                    ),
                contentDescription = null,
                modifier =
                    Modifier.size(17.dp)
            )
        }
    }
    
    

}