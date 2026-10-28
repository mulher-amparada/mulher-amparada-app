
package com.mulheres

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Fundo = Color(0xFFF8F7FC)
private val Roxo = Color(0xFF7048D8)
private val RoxoEscuro = Color(0xFF5932BB)
private val TextoPrincipal = Color(0xFF191725)
private val TextoSecundario = Color(0xFF777383)

class AssistenteActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT

        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        setContent {
            MaterialTheme {
                TelaBoasVindas(
                    onComecar = {
                        // Defina aqui a próxima tela do assistente.
                    }
                )
            }
        }
    }
}

@Composable
private fun TelaBoasVindas(
    onComecar: () -> Unit
) {
    var visivel by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120)
        visivel = true
    }

    val escala by animateFloatAsState(
        targetValue = if (visivel) 1f else 0.92f,
        animationSpec = tween(
            durationMillis = 650,
            easing = FastOutSlowInEasing
        ),
        label = "escalaIcone"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Fundo
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFCFBFF),
                            Fundo,
                            Color(0xFFF2EEFC)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.75f))

                AnimatedVisibility(
                    visible = visivel,
                    enter = fadeIn(
                        animationSpec = tween(650)
                    ) + slideInVertically(
                        initialOffsetY = { it / 7 },
                        animationSpec = tween(
                            650,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(144.dp)
                                .graphicsLayer {
                                    scaleX = escala
                                    scaleY = escala
                                }
                                .clip(RoundedCornerShape(36.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = R.mipmap.ic_launcher
                                ),
                                contentDescription =
                                    "Mulher Amparada",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(36.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        Text(
                            text = "Boas-vindas ao",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextoSecundario,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Mulher Amparada",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipal,
                            textAlign = TextAlign.Center,
                            lineHeight = 40.sp,
                            letterSpacing = (-0.8).sp
                        )

                        Text(
                            text = "UI 2",
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Bold,
                            color = Roxo,
                            textAlign = TextAlign.Center,
                            lineHeight = 42.sp,
                            letterSpacing = (-0.6).sp
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            text = "Uma nova experiência de proteção, cuidado e simplicidade começa aqui.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextoSecundario,
                            textAlign = TextAlign.Center,
                            lineHeight = 25.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                AnimatedVisibility(
                    visible = visivel,
                    enter = fadeIn(
                        animationSpec = tween(700, delayMillis = 150)
                    ) + slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(
                            700,
                            delayMillis = 150,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = onComecar,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Roxo,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            Text(
                                text = "Vamos começar",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "MULHER AMPARADA  •  UI 2",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextoSecundario.copy(alpha = 0.75f),
                            letterSpacing = 1.4.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}
