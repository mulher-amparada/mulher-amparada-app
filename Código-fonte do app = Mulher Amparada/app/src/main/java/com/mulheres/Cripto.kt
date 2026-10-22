
package com.mulheres

import android.os.Bundle
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import androidx.fragment.app.FragmentActivity

class NeoCalcActivity : FragmentActivity() {

    private lateinit var cripto: Cripto

    private var biometricPrompt: BiometricPrompt? = null

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
            MaterialTheme {
                NeoCalcScreen(
                    cripto = cripto,
                    onFiveTaps = { abrirBiometria() }
                )
            }
        }
    }

    private fun configurarBiometria() {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    // Autenticação biométrica concluída.
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // A biometria não foi reconhecida.
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    // O usuário cancelou ou ocorreu um erro.
                }
            }
        )
    }

    private fun abrirBiometria() {
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val manager = BiometricManager.from(this)

        when (manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Autenticação")
                    .setSubtitle("Confirme sua identidade para continuar")
                    .setAllowedAuthenticators(authenticators)
                    .build()

                biometricPrompt?.authenticate(promptInfo)
            }

            else -> {
                // Não há biometria ou credencial compatível configurada.
            }
        }
    }
}

@Composable
private fun NeoCalcScreen(
    cripto: Cripto,
    onFiveTaps: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf(false) }
    var screenTaps by remember { mutableIntStateOf(0) }

    val configuration = LocalConfiguration.current
    val landscape =
        configuration.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val background = Color.Black
    val keyColor = Color(0xFF202124)
    val keyPressed = Color(0xFF45464A)
    val textColor = Color(0xFFF5F5F5)
    val secondaryColor = Color(0xFFAAAAAA)

    fun addDigit(digit: String) {
        if (pin.length >= 6) return

        val newPin = pin + digit
        pin = newPin
        savedMessage = false

        if (newPin.length == 6) {
            cripto.salvar("neocalc_pin", newPin)
            savedMessage = true
        }
    }

    fun deleteDigit() {
        if (pin.isNotEmpty()) {
            pin = pin.dropLast(1)
            savedMessage = false
        }
    }

    BackHandler(enabled = pin.isNotEmpty()) {
        deleteDigit()
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

            Spacer(Modifier.height(30.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    Box(
                        Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < pin.length) textColor
                                else Color(0xFF383838)
                            )
                    )
                }
            }

            if (savedMessage) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "PIN salvo com segurança",
                    color = secondaryColor,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Toque cinco vezes aqui para autenticar",
                color = Color(0xFF626262),
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
                            onClick = { addDigit(number) }
                        )
                    }
                }

                Spacer(Modifier.height(if (landscape) 5.dp else 13.dp))
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
                        text = "Emergência",
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
                    onClick = { addDigit("0") }
                )

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .clickable { deleteDigit() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⌫",
                        color = textColor,
                        fontSize = 27.sp
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
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val animatedColor by animateColorAsState(
        targetValue = if (pressed) pressedColor else color,
        label = "keyColor"
    )

    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(animatedColor)
            .clickable {
                onClick()
            },
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
