package com.mulheres

import android.graphics.Color as AndroidColor
import android.os.Bundle
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate

class RankingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

        setContent {
            val modoEscuro = androidx.compose.foundation.isSystemInDarkTheme()

            SideEffect {
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

@Composable
private fun RankingScreen() {
    val contexto = LocalContext.current.applicationContext
    var mostrarConteudo by remember { mutableStateOf(false) }
    var indiceRanking by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        indiceRanking = withContext(Dispatchers.IO) {
            obterRankingDoDia(Cripto(contexto))
        }

        delay(120)
        mostrarConteudo = true
    }

    val modoEscuro = isSystemInDarkTheme()

    val fundo = if (modoEscuro) {
       Color(0xFF000000)
    } else {
        Color(0xFFFFFFFF)
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
                    top = WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateTopPadding() + 14.dp,
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding() + 28.dp,
                    start = 12.dp,
                    end = 24.dp
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
                            indice = indiceRanking,
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
                            indice = indiceRanking,
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

private fun obterRankingDoDia(cripto: Cripto): Int {
    val hoje = LocalDate.now().toString()
    val ultimaData = cripto.carregar("ranking_ultima_data")
    val indiceSalvo = cripto.carregar("ranking_indice")
        .toIntOrNull()
        ?.coerceIn(1, 6)
        ?: 1

    val indice = when {
        ultimaData.isEmpty() -> indiceSalvo
        hoje > ultimaData -> (indiceSalvo + 1).coerceAtMost(6)
        else -> indiceSalvo
    }

    if (ultimaData != hoje) {
        cripto.salvar("ranking_indice", indice.toString())

        // Evita substituir a data mais recente por uma data anterior.
        if (ultimaData.isEmpty() || hoje > ultimaData) {
            cripto.salvar("ranking_ultima_data", hoje)
        }
    }

    return indice
}

@Composable
private fun ImagemRanking(
    indice: Int,
    modifier: Modifier = Modifier
) {
    val imagens = listOf(
        R.drawable.ranking1,
        R.drawable.ranking2,
        R.drawable.ranking3,
        R.drawable.ranking4,
        R.drawable.ranking5,
        R.drawable.ranking6
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = imagens[(indice - 1).coerceIn(0, 5)]
            ),
            contentDescription = "Ilustração do ranking",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun MensagemAcolhimento(
    modifier: Modifier = Modifier,
    rosa: Color,
    texto: Color,
    paisagem: Boolean
) {
    Column(
    modifier = modifier.padding(
        end = if (paisagem) 32.dp else 0.dp
    ),
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
