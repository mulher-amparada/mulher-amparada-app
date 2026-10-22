
package com.mulheres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class NeoCalcActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            MaterialTheme {
                NeoCalcScreen()
            }
        }
    }
}

@Composable
private fun NeoCalcScreen() {
    var pin by remember { mutableStateOf("") }

    val background = Color(0xFF000000)
    val keyColor = Color(0xFF202124)
    val keyPressed = Color(0xFF45464A)
    val textColor = Color(0xFFF5F5F5)
    val secondaryColor = Color(0xFFAAAAAA)

    BackHandler(enabled = pin.isNotEmpty()) {
        pin = pin.dropLast(1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.8f))

        Text(
            text = "Insira seu PIN",
            color = textColor,
            fontSize = 25.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Use o teclado para continuar",
            color = secondaryColor,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(34.dp))

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
                            if (index < pin.length) textColor
                            else Color(0xFF383838)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.7f))

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
                        onClick = {
                            if (pin.length < 6) {
                                pin += number
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(13.dp))
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
                onClick = {
                    if (pin.length < 6) pin += "0"
                }
            )

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⌫",
                    color = textColor,
                    fontSize = 27.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.65f))

        Text(
            text = "Desbloqueio visual demonstrativo",
            color = Color(0xFF626262),
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(18.dp))
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
                pressed = true
                onClick()
                pressed = false
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
