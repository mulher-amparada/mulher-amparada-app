package com.mulheres

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay

class PinPadActivity : FragmentActivity() {

    private lateinit var cripto: Cripto

    private var biometricPrompt: BiometricPrompt? = null

    private val resetSignal = mutableIntStateOf(0)
    private val biometricError = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        cripto = Cripto(applicationContext)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).show(WindowInsetsCompat.Type.systemBars())

        configurarBiometria()

    setContent {
    PinPadTheme {
        NeoCalcScreen(
            cripto = cripto,
            resetSignal = resetSignal.intValue,
            biometricError = biometricError.value,
            onFiveTaps = { abrirBiometria() },
            onSuccess = { abrirHub() }
        )
    }
}
    }

    private fun configurarBiometria() {
        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    // Autoriza o reset somente após autenticação.
                    biometricError.value = null
                    resetSignal.intValue++
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // O usuário pode tentar novamente.
                }

                override fun onAuthenticationError(
    errorCode: Int,
    errString: CharSequence
) {
    super.onAuthenticationError(errorCode, errString)

    biometricError.value = null
}
            }
        )
    }

    private fun abrirBiometria() {
        biometricError.value = null

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val manager = BiometricManager.from(this)

        when (manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo =
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Redefinir PIN")
                        .setSubtitle(
                            "Confirme sua identidade para cadastrar um novo PIN"
                        )
                        .setAllowedAuthenticators(authenticators)
                        .build()

                biometricPrompt?.authenticate(promptInfo)
            }

            else -> {
                biometricError.value =
                    "Configure a biometria ou o bloqueio do dispositivo."
            }
        }
    }

    private fun abrirHub() {
        startActivity(
            Intent(this, RankingActivity::class.java)
        )
        finish()
    }
}

@Composable
private fun PinPadTheme(
    content: @Composable () -> Unit
) {
    val darkMode = isSystemInDarkTheme()

    val colors = if (darkMode) {
        darkColorScheme(
            background = Color(0xFF000000),
            surface = Color(0xFF000000),
            onBackground = Color(0xFFF5F5F5),
            onSurface = Color(0xFFF5F5F5)
        )
    } else {
        lightColorScheme(
            background = Color(0xFFF8F8FC),
            surface = Color(0xFFF8F8FC),
            onBackground = Color(0xFF17171C),
            onSurface = Color(0xFF17171C)
        )
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@Composable
private fun NeoCalcScreen(
    cripto: Cripto,
    resetSignal: Int,
    biometricError: String?,
    onFiveTaps: () -> Unit,
    onSuccess: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("neutral") }
    var screenTaps by remember { mutableIntStateOf(0) }

    val configuration = LocalConfiguration.current
    val landscape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val darkMode = isSystemInDarkTheme()

val background = MaterialTheme.colorScheme.background
val keyColor = if (darkMode) {
    Color(0xFF202124)
} else {
    Color(0xFFE4E5EC)
}
val keyPressed = if (darkMode) {
    Color(0xFF45464A)
} else {
    Color(0xFFCFD1DC)
}
val textColor = MaterialTheme.colorScheme.onBackground
val secondaryColor = if (darkMode) {
    Color(0xFFAAAAAA)
} else {
    Color(0xFF666671)
}

    // Após autenticação biométrica, remove o PIN salvo.
    LaunchedEffect(resetSignal) {
        if (resetSignal > 0) {
            cripto.remover("neocalc_pin")
            pin = ""
            status = "neutral"
        }
    }

    // Dá tempo para o usuário ver as bolinhas vermelhas ou verdes.
    LaunchedEffect(status) {
        when (status) {
            "wrong" -> {
                delay(850)
                pin = ""
                status = "neutral"
            }

            "correct" -> {
                delay(450)
                onSuccess()
            }
        }
    }

    fun addDigit(digit: String) {
        if (status == "correct" || status == "wrong") return
        if (pin.length >= 6) return

        val newPin = pin + digit
        pin = newPin

        if (newPin.length == 6) {
            val savedPin = cripto.carregar("neocalc_pin")

            if (savedPin.isEmpty()) {
                // Primeiro uso: cadastra o PIN.
                cripto.salvar("neocalc_pin", newPin)
                status = "correct"
            } else if (newPin == savedPin) {
                status = "correct"
            } else {
                status = "wrong"
            }
        }
    }

    fun deleteDigit() {
        if (status == "correct" || status == "wrong") return

        if (pin.isNotEmpty()) {
            pin = pin.dropLast(1)
        }
    }

    val dotColor = when (status) {
        "correct" -> Color(0xFF36D66B)
        "wrong" -> Color(0xFFFF453A)
        else -> textColor
    }

    val titleArea: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Insira seu PIN",
                color = textColor,
                fontSize = 25.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Use o teclado para continuar",
                color = secondaryColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

Text(
    text = "Toque 5 vezes aqui para redefinir o PIN",
    color = Color(0xFF777777),
    fontSize = 11.sp,
    textAlign = TextAlign.Center,
    modifier = Modifier.pointerInput(Unit) {
        detectTapGestures {
            screenTaps++
            if (screenTaps >= 5) {
                screenTaps = 0
                onFiveTaps()
            }
        }
    }
)

            Spacer(Modifier.height(30.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < pin.length) dotColor
                                else Color(0xFF383838)
                            )
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = when (status) {
                    "correct" -> "PIN correto"
                    "wrong" -> "PIN incorreto"
                    else -> " "
                },
                color = when (status) {
                    "correct" -> Color(0xFF36D66B)
                    "wrong" -> Color(0xFFFF453A)
                    else -> secondaryColor
                },
                fontSize = 13.sp
            )

            Spacer(Modifier.height(14.dp))

            

            if (biometricError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = biometricError,
                    color = Color(0xFFFF453A),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    val keypad: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { number ->
                        NeoCalcKey(
                            number = number,
                            subtitle = when (number) {
                                "2" -> "ABC"
                                "3" -> "DEF"
                                "4" -> "GHI"
                                "5" -> "JKL"
                                "6" -> "MNO"
                                "7" -> "PQRS"
                                "8" -> "TUV"
                                "9" -> "WXYZ"
                                else -> ""
                            },
                            color = keyColor,
                            pressedColor = keyPressed,
                            textColor = textColor,
                            enabled = status == "neutral",
                            onClick = { addDigit(number) }
                        )
                    }
                }

                Spacer(
                    Modifier.height(
                        if (landscape) 4.dp else 13.dp
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OK",
                        color = secondaryColor,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                NeoCalcKey(
                    number = "0",
                    subtitle = "+",
                    color = keyColor,
                    pressedColor = keyPressed,
                    textColor = textColor,
                    enabled = status == "neutral",
                    onClick = { addDigit("0") }
                )

Box(
    modifier = Modifier
        .size(76.dp)
        .clip(CircleShape)
        .clickable(
            enabled = status == "neutral",
            onClick = { deleteDigit() }
        ),
    contentAlignment = Alignment.Center
) {
    Image(
        painter = painterResource(id = R.drawable.ic_apagarsenha),
        contentDescription = "Apagar",
        modifier = Modifier.size(30.dp)
    )
}
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(
                horizontal = if (landscape) 18.dp else 28.dp,
                vertical = if (landscape) 4.dp else 8.dp
            )
    ) {
        if (landscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    titleArea(Modifier.fillMaxWidth())
                }

                Box(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    keypad(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(0.7f))

                titleArea(Modifier.fillMaxWidth())

                Spacer(Modifier.weight(0.6f))

                keypad(Modifier.fillMaxWidth())

                Spacer(Modifier.weight(0.5f))

                Text(
                    text = "Este recurso não substitui qualquer tela do sistema!",
                    color = Color(0xFF626262),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun NeoCalcKey(
    number: String,
    subtitle: String,
    color: Color,
    pressedColor: Color,
    textColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = color,
        label = "keyColor"
    )

    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
.background(
    if (enabled) animatedColor
    else if (isSystemInDarkTheme()) {
        Color(0xFF181818)
    } else {
        Color(0xFFE0E0E6)
    }
)
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                color = textColor,
                fontSize = 29.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 32.sp
            )

            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = Color(0xFFBDBDBD),
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}
