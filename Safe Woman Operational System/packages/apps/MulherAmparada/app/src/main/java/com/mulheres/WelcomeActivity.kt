package com.mulheres

import android.content.Intent
import android.view.WindowManager
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.delay

private val RoxoClaro = Color(0xFF7048D8)
private val RoxoEscuro = Color(0xFFB9A0FF)

private val EsquemaClaro = lightColorScheme(
    primary = RoxoClaro,
    onPrimary = Color.White,
    background = Color(0xFFF8F7FC),
    onBackground = Color(0xFF191725),
    surface = Color(0xFFFCFBFF),
    onSurface = Color(0xFF191725),
    secondary = Color(0xFF777383)
)

private val EsquemaEscuro = darkColorScheme(
    primary = RoxoEscuro,
    onPrimary = Color(0xFF241044),
    background = Color(0xFF111016),
    onBackground = Color(0xFFF4F0FC),
    surface = Color(0xFF191720),
    onSurface = Color(0xFFF4F0FC),
    secondary = Color(0xFFB8B1C7)
)

class WelcomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

if (android.os.Build.VERSION.SDK_INT >=
    android.os.Build.VERSION_CODES.Q
) {
    window.isNavigationBarContrastEnforced = false
}

        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT

window.setFlags(
    android.view.WindowManager.LayoutParams.FLAG_SECURE,
    android.view.WindowManager.LayoutParams.FLAG_SECURE
)

        setContent {
            val modoEscuro = isSystemInDarkTheme()

            SideEffect {
                @Suppress("DEPRECATION")
                run {
                    val flags =
                        android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                            android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

                    window.decorView.systemUiVisibility =
                        if (modoEscuro) {
                            window.decorView.systemUiVisibility and flags.inv()
                        } else {
                            window.decorView.systemUiVisibility or flags
                        }
                }
            }

            MaterialTheme(
                colorScheme = if (modoEscuro) {
                    EsquemaEscuro
                } else {
                    EsquemaClaro
                }
            ) {
                TelaBoasVindas(
    onComecar = {
        startActivity(
            Intent(
                this,
                AssistenteActivity::class.java
            )
        )

        finish()
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
   
    val modoEscuro = isSystemInDarkTheme()
    val esquema = MaterialTheme.colorScheme

    val fundo = esquema.background
    val textoPrincipal = esquema.onBackground
    val textoSecundario = esquema.secondary
    val roxo = esquema.primary

    val gradiente = if (modoEscuro) {
        listOf(
            Color(0xFF17141F),
            Color(0xFF111016),
            Color(0xFF211832)
        )
    } else {
        listOf(
            Color(0xFFFCFBFF),
            Color(0xFFF8F7FC),
            Color(0xFFF0EAFB)
        )
    }

    var visivel by remember {
        mutableStateOf(false)
    }

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
        color = fundo
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(gradiente)
                )
        ) {
            val modoDeitado = maxWidth > maxHeight

            if (modoDeitado) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(
                            horizontal = 32.dp,
                            vertical = 16.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LADO ESQUERDO: ícone, boas-vindas e nome
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedVisibility(
                            visible = visivel,
                            enter = fadeIn(
                                animationSpec = tween(650)
                            ) + slideInVertically(
                                initialOffsetY = { it / 7 },
                                animationSpec = tween(
                                    durationMillis = 650,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        ) {
                            Column(
                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .graphicsLayer {
                                            scaleX = escala
                                            scaleY = escala
                                        }
                                        .clip(
                                            RoundedCornerShape(24.dp)
                                        )
                                        .background(
                                            if (modoEscuro) {
                                                Color.Black
                                            } else {
                                                Color.White
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AndroidView(
                                        factory = { ctx ->
                                            ImageView(ctx).apply {
                                                scaleType =
                                                    ImageView.ScaleType.FIT_CENTER

                                                setImageDrawable(
                                                    ctx.packageManager
                                                        .getApplicationIcon(
                                                            ctx.packageName
                                                        )
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(
                                                RoundedCornerShape(24.dp)
                                            )
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(18.dp)
                                )

                                Text(
                                    text = "Boas-vindas ao",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textoSecundario,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text = "Assistente",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textoPrincipal,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 32.sp,
                                    letterSpacing = (-0.6).sp
                                )

                                Text(
                                    text = "UI 2",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = roxo,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 34.sp
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.width(28.dp)
                    )

                    // LADO DIREITO: descrição e botão
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedVisibility(
                            visible = visivel,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 700,
                                    delayMillis = 150
                                )
                            ) + slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = tween(
                                    durationMillis = 700,
                                    delayMillis = 150,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Uma nova experiência de proteção, cuidado e simplicidade começa aqui.",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = textoSecundario,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 23.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(28.dp)
                                )

                                Button(
                                    onClick = onComecar,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = roxo,
                                        contentColor = esquema.onPrimary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 2.dp
                                    )
                                ) {
                                    Text(
                                        text = "Vamos começar",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(18.dp)
                                )

                                Text(
                                    text = "Assistente  •  UI 2",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textoSecundario.copy(
                                        alpha = 0.75f
                                    ),
                                    letterSpacing = 1.2.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                // MODO RETRATO: mantém a organização vertical
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(
                        modifier = Modifier.weight(0.75f)
                    )

                    AnimatedVisibility(
                        visible = visivel,
                        enter = fadeIn(
                            animationSpec = tween(650)
                        ) + slideInVertically(
                            initialOffsetY = { it / 7 },
                            animationSpec = tween(
                                durationMillis = 650,
                                easing = FastOutSlowInEasing
                            )
                        )
                    ) {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(144.dp)
                                    .graphicsLayer {
                                        scaleX = escala
                                        scaleY = escala
                                    }
                                    .clip(
                                        RoundedCornerShape(36.dp)
                                    )
                                    .background(
                                        if (modoEscuro) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        ImageView(ctx).apply {
                                            scaleType =
                                                ImageView.ScaleType.FIT_CENTER

                                            setImageDrawable(
                                                ctx.packageManager
                                                    .getApplicationIcon(
                                                        ctx.packageName
                                                    )
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(
                                            RoundedCornerShape(36.dp)
                                        )
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(48.dp)
                            )

                            Text(
                                text = "Boas-vindas ao",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = textoSecundario,
                                textAlign = TextAlign.Center,
                                letterSpacing = 0.1.sp
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Text(
                                text = "Assistente",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = textoPrincipal,
                                textAlign = TextAlign.Center,
                                lineHeight = 40.sp,
                                letterSpacing = (-0.8).sp
                            )

                            Text(
                                text = "UI 2",
                                fontSize = 35.sp,
                                fontWeight = FontWeight.Bold,
                                color = roxo,
                                textAlign = TextAlign.Center,
                                lineHeight = 42.sp,
                                letterSpacing = (-0.6).sp
                            )

                            Spacer(
                                modifier = Modifier.height(22.dp)
                            )

                            Text(
                                text = "Uma nova experiência de proteção, cuidado e simplicidade começa aqui.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = textoSecundario,
                                textAlign = TextAlign.Center,
                                lineHeight = 25.sp
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedVisibility(
                        visible = visivel,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 700,
                                delayMillis = 150
                            )
                        ) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(
                                durationMillis = 700,
                                delayMillis = 150,
                                easing = FastOutSlowInEasing
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = onComecar,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = roxo,
                                    contentColor = esquema.onPrimary
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

                            Spacer(
                                modifier = Modifier.height(18.dp)
                            )

                            Text(
                                text = "Assistente  •  UI 2",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = textoSecundario.copy(
                                    alpha = 0.75f
                                ),
                                letterSpacing = 1.4.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(
                                modifier = Modifier.height(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    }