package com.mulheres

import androidx.compose.foundation.isSystemInDarkTheme
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

class RankingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Impede capturas de tela e gravações comuns da tela.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Permite que o conteúdo ocupe a área das barras do sistema.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Fundo do sistema combinando com a tela.
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT

        // Remove a faixa de contraste que pode deixar a navegação cinza.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

        // Mantém os ícones do sistema escuros sobre o fundo claro.
        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContent {
    val modoEscuro = androidx.compose.foundation.isSystemInDarkTheme()

    androidx.compose.runtime.SideEffect {
        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = !modoEscuro
            isAppearanceLightNavigationBars = !modoEscuro
        }
    }

    MaterialTheme {
        RankingScreen()
    }
}
        
    }
}

@androidx.compose.runtime.Composable
private fun RankingScreen() {
    var mostrarConteudo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120)
        mostrarConteudo = true
    }

    
    val modoEscuro = isSystemInDarkTheme()

val fundo = if (modoEscuro) {
    Color(0xFF181218)
} else {
    Color(0xFFFFF7FA)
}

val rosa = if (modoEscuro) {
    Color(0xFFFFA6CA)
} else {
    Color(0xFFCE668F)
}

val texto = if (modoEscuro) {
    Color(0xFFF3E8EF)
} else {
    Color(0xFF493640)
}

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = fundo
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(fundo)
                .padding(
                    start = 12.dp,
                    end = 24.dp,
                    top = 14.dp,
                    bottom = 28.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            val paisagem = maxWidth > maxHeight

            AnimatedVisibility(
                visible = mostrarConteudo,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 850,
                        easing = FastOutSlowInEasing
                    )
                ) + slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(
                        durationMillis = 850,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                if (paisagem) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImagemRanking(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        MensagemAcolhimento(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            rosa = rosa,
                            texto = texto,
                            paisagem = true
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ImagemRanking(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        Spacer(modifier = Modifier.height(9.dp))

                        MensagemAcolhimento(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.8f),
                            rosa = rosa,
                            texto = texto,
                            paisagem = false
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ImagemRanking(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ranking1),
            contentDescription = "Ilustração do ranking",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

@androidx.compose.runtime.Composable
private fun MensagemAcolhimento(
    modifier: Modifier = Modifier,
    rosa: Color,
    texto: Color,
    paisagem: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Você é bem-vinda!",
            color = rosa,
            fontSize = if (paisagem) 27.sp else 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "O Mulher Amparada foi criado pensando em você. " +
                "Agradecemos por confiar em nosso aplicativo e permitir " +
                "que ele faça parte da sua jornada.", 
            color = texto,
            fontSize = if (paisagem) 15.sp else 17.sp,
            lineHeight = if (paisagem) 23.sp else 27.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Você merece acolhimento, respeito e segurança. " +
                "Estamos aqui para ajudar.",
            color = rosa,
            fontSize = if (paisagem) 14.sp else 16.sp,
            lineHeight = if (paisagem) 21.sp else 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}
