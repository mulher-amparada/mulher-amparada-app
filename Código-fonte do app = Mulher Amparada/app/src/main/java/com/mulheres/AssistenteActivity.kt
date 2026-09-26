package com.mulheres

import android.content.Intent
import android.os.Bundle
import android.provider.Settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.view.WindowCompat

import kotlinx.coroutines.delay

import java.util.Locale

import kotlin.math.roundToInt
import kotlin.random.Random


/* =========================================================
   ACTIVITY
========================================================= */

class AssistenteActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /*
         * Equivalente ao:
         *
         * viewport-fit=cover
         *
         * + safe-area-inset do HTML.
         */
        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {

            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {

            MaterialTheme {

                Surface(
                    modifier =
                        Modifier.fillMaxSize(),

                    color =
                        Color.Black
                ) {

                    AssistenteSaude()
                }
            }
        }
    }
}


/* =========================================================
   CORES
========================================================= */

private val Fundo =
    Color(0xFF000000)

private val HeroFundo =
    Color(0xFF050505)

private val CardFundo =
    Color(0xFF050505)

private val ActivityFundo =
    Color(0xFF040404)

private val BordaHero =
    Color(0xFF1D1D1D)

private val Borda =
    Color(0xFF181818)

private val BordaIcone =
    Color(0xFF202020)

private val TextoBranco =
    Color(0xFFF5F5F5)

private val TextoHero =
    Color(0xFFEDEDED)

private val TextoCinza =
    Color(0xFF999999)

private val TextoEscuro =
    Color(0xFF494949)

private val TextoMuitoEscuro =
    Color(0xFF3B3B3B)

private val Azul =
    Color(0xFF5666FF)

private val AzulBemEstar =
    Color(0xFF6070FF)


/* =========================================================
   FONTE
========================================================= */

private val Quicksand =
    FontFamily(

        Font(
            resId = R.font.quicksand,
            weight = FontWeight.Normal
        ),

        Font(
            resId = R.font.quicksand,
            weight = FontWeight.Bold
        )
    )


/* =========================================================
   DADOS
========================================================= */

private data class Dado(

    var valor: Float,

    val min: Float,

    val max: Float,

    val passo: Float
)


private class DadosSaude {

    var batimentos =
        Dado(
            valor = 72f,
            min = 65f,
            max = 82f,
            passo = 2f
        )

    var respiracao =
        Dado(
            valor = 16f,
            min = 12f,
            max = 20f,
            passo = 0.5f
        )

    var temperatura =
        Dado(
            valor = 36.5f,
            min = 36.1f,
            max = 36.9f,
            passo = 0.05f
        )

    var hidratacao =
        Dado(
            valor = 68f,
            min = 55f,
            max = 82f,
            passo = 1f
        )

    var bemEstar =
        Dado(
            valor = 72f,
            min = 60f,
            max = 88f,
            passo = 2f
        )
}


/* =========================================================
   FUNÇÃO VARIAR
========================================================= */

private fun variar(
    valor: Float,
    min: Float,
    max: Float,
    passo: Float
): Float {

    val variacao =
        (
            Random.nextFloat() * 2f - 1f
        ) * passo

    return (
        valor + variacao
    ).coerceIn(
        min,
        max
    )
}


/* =========================================================
   FORMATADORES
========================================================= */

private fun formatar(
    valor: Float
): String {

    return String.format(
        Locale.US,
        "%.1f",
        valor
    )
}


private fun percentual(

    valor: Float,

    min: Float,

    max: Float

): Float {

    return (
        (valor - min) /
            (max - min)
        )
        .coerceIn(
            0f,
            1f
        )
}


/* =========================================================
   TELA
========================================================= */

@Composable
private fun AssistenteSaude() {

    val context =
        LocalContext.current

    val dados =
        remember {
            DadosSaude()
        }

    /*
     * Estados usados para provocar recomposição.
     *
     * O HTML alterava textContent diretamente.
     * No Compose precisamos atualizar estado.
     */
    var atualizacao by
        remember {
            mutableFloatStateOf(
                dados.bemEstar.valor
            )
        }


    /*
     * Equivalente ao setInterval de 3000 ms.
     */
    LaunchedEffect(Unit) {

        while (true) {

            delay(3000)


            dados.batimentos.valor =
                variar(
                    dados.batimentos.valor,
                    dados.batimentos.min,
                    dados.batimentos.max,
                    dados.batimentos.passo
                )


            dados.respiracao.valor =
                variar(
                    dados.respiracao.valor,
                    dados.respiracao.min,
                    dados.respiracao.max,
                    dados.respiracao.passo
                )


            dados.temperatura.valor =
                variar(
                    dados.temperatura.valor,
                    dados.temperatura.min,
                    dados.temperatura.max,
                    dados.temperatura.passo
                )


            dados.hidratacao.valor =
                variar(
                    dados.hidratacao.valor,
                    dados.hidratacao.min,
                    dados.hidratacao.max,
                    dados.hidratacao.passo
                )


            dados.bemEstar.valor =
                variar(
                    dados.bemEstar.valor,
                    dados.bemEstar.min,
                    dados.bemEstar.max,
                    dados.bemEstar.passo
                )


            atualizacao =
                dados.bemEstar.valor
        }
    }


    /*
     * equivalente ao:
     *
     * @media (max-width: 430px)
     *
     * e
     *
     * @media (max-width: 360px)
     *
     * O Compose não precisa esconder os elementos.
     * A largura disponível determina a disposição.
     */
    LazyColumn(

        modifier =
            Modifier
                .fillMaxSize()
                .background(Fundo)

                .windowInsetsPadding(
                    WindowInsets.statusBars
                )

                .windowInsetsPadding(
                    WindowInsets.navigationBars
                )

                .padding(
                    start = 15.dp,
                    end = 15.dp,
                    top = 25.dp,
                    bottom = 45.dp
                ),

        verticalArrangement =
            Arrangement.Top
    ) {


        /* =================================================
           CABEÇALHO
        ================================================== */

        item {

            Cabecalho(

                onSecretClick = {

                    /*
                     * Equivalente a:
                     *
                     * window.location.href =
                     * "index11.html"
                     *
                     * No Android, a tela correspondente
                     * é aberta pela Activity.
                     */

                    context.startActivity(
                        Intent(
                            context,
                            MainActivity::class.java
                        )
                    )
                }
            )

            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )
        }


        /* =================================================
           HERO
        ================================================== */

        item {

            Hero(

                valor =
                    dados.bemEstar.valor
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )
        }


        /* =================================================
           RESUMO
        ================================================== */

        item {

            Resumo(

                batimentos =
                    dados.batimentos.valor,

                respiracao =
                    dados.respiracao.valor,

                temperatura =
                    dados.temperatura.valor
            )
        }


        /* =================================================
           INDICADORES
        ================================================== */

        item {

            Spacer(
                modifier =
                    Modifier.height(36.dp)
            )

            CabecalhoSecao(

                titulo =
                    "Indicadores",

                descricao =
                    "Atualização contínua"
            )

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            Indicadores(
                dados =
                    dados
            )
        }


        /* =================================================
           BEM-ESTAR
        ================================================== */

        item {

            Spacer(
                modifier =
                    Modifier.height(36.dp)
            )

            CabecalhoSecao(

                titulo =
                    "Bem-estar",

                descricao =
                    "Índice geral"
            )

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            BemEstar(
                valor =
                    dados.bemEstar.valor
            )
        }


        /* =================================================
           ATIVIDADE
        ================================================== */

        item {

            Spacer(
                modifier =
                    Modifier.height(36.dp)
            )

            CabecalhoSecao(
                titulo =
                    "Atividade"
            )

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            Atividade()

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )
        }
    }
}


/* =========================================================
   CABEÇALHO
========================================================= */

@Composable
private fun Cabecalho(
    onSecretClick: () -> Unit
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(

                text =
                    "CUIDADO E ACOMPANHAMENTO",

                color =
                    Color(0xFF4B4B4B),

                fontFamily =
                    Quicksand,

                fontSize =
                    7.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    1.5.sp
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(

                text =
                    "Assistente de Saúde",

                color =
                    TextoBranco,

                fontFamily =
                    Quicksand,

                fontSize =
                    24.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    (-1.1).sp,

                lineHeight =
                    25.sp
            )


            Spacer(
                modifier =
                    Modifier.height(9.dp)
            )


            Text(

                text =
                    "Um espaço para acompanhar seu bem-estar.",

                color =
                    Color(0xFF4B4B4B),

                fontFamily =
                    Quicksand,

                fontSize =
                    9.sp,

                lineHeight =
                    14.sp
            )
        }


        Spacer(
            modifier =
                Modifier.width(20.dp)
        )


        /* =================================================
           SECRET BUTTON
        ================================================== */

        Box(

            modifier =
                Modifier
                    .size(42.dp)
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )
                    .background(
                        Color(0xFF080808)
                    )
                    .border(
                        1.dp,
                        Color(0xFF1E1E1E),
                        RoundedCornerShape(
                            14.dp
                        )
                    )
                    .clickable(
                        onClick =
                            onSecretClick
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            /*
             * 8.svg
             *
             * Coloque o SVG convertido para Vector
             * em res/drawable como:
             *
             * icone_8.xml
             */

            ImageOriginal(

                resource =
                    R.drawable.icone_8,

                contentDescription =
                    "Abrir",

                modifier =
                    Modifier.size(19.dp)
            )
        }
    }
}


/* =========================================================
   HERO
========================================================= */

@Composable
private fun Hero(
    valor: Float
) {

    /*
     * Equivalente ao:
     *
     * animation:
     * respirar 2s ease-in-out infinite;
     */

    val transition =
        rememberInfiniteTransition(
            label =
                "respirar"
        )


    val liveOpacity by
        transition.animateFloat(

            initialValue =
                0.4f,

            targetValue =
                1f,

            animationSpec =
                infiniteRepeatable(

                    animation =
                        tween(
                            durationMillis =
                                2000,
                            easing =
                                LinearEasing
                        ),

                    repeatMode =
                        RepeatMode.Reverse
                ),

            label =
                "live-opacity"
        )


    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(330.dp)
                .clip(
                    RoundedCornerShape(
                        28.dp
                    )
                )
                .background(
                    HeroFundo
                )
                .border(
                    1.dp,
                    BordaHero,
                    RoundedCornerShape(
                        28.dp
                    )
                )
    ) {


        /* =================================================
           HERO TOP
        ================================================== */

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 19.dp,
                        start = 21.dp,
                        end = 21.dp
                    ),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(

                text =
                    "ESTADO GERAL",

                color =
                    Color(0xFF494949),

                fontFamily =
                    Quicksand,

                fontSize =
                    7.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    1.2.sp
            )


            Row(

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(

                    modifier =
                        Modifier
                            .size(5.dp)
                            .clip(
                                CircleShape
                            )
                            .background(
                                Color(
                                    0xFF6D7CFF
                                )
                                    .copy(
                                        alpha =
                                            liveOpacity
                                    )
                            )
                )


                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )


                Text(

                    text =
                        "ATIVO",

                    color =
                        Color(0xFF494949),

                    fontFamily =
                        Quicksand,

                    fontSize =
                        7.sp,

                    fontWeight =
                        FontWeight.Bold,

                    letterSpacing =
                        0.8.sp
                )
            }
        }


        /* =================================================
           CENTRO
        ================================================== */

        Column(

            modifier =
                Modifier.align(
                    Alignment.Center
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Box(

                modifier =
                    Modifier
                        .size(62.dp)
                        .clip(
                            RoundedCornerShape(
                                20.dp
                            )
                        )
                        .background(
                            Color(0xFF0B0D17)
                        )
                        .border(
                            1.dp,
                            Color(0xFF282B40),
                            RoundedCornerShape(
                                20.dp
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                ImageOriginal(

                    resource =
                        R.drawable.assistente1,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(30.dp)
                )
            }


            Spacer(
                modifier =
                    Modifier.height(17.dp)
            )


            Row(
                verticalAlignment =
                    Alignment.Bottom
            ) {

                Text(

                    text =
                        valor
                            .roundToInt()
                            .toString(),

                    color =
                        Color.White,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        30.sp,

                    fontWeight =
                        FontWeight.Bold,

                    letterSpacing =
                        (-1.2).sp
                )


                Spacer(
                    modifier =
                        Modifier.width(4.dp)
                )


                Text(

                    text =
                        "/ 100",

                    color =
                        Color(0xFF454545),

                    fontFamily =
                        Quicksand,

                    fontSize =
                        8.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }


        /* =================================================
           HERO STATUS
        ================================================== */

        Column(

            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .padding(
                        bottom = 30.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(

                text =
                    "Tudo tranquilo.",

                color =
                    TextoHero,

                fontFamily =
                    Quicksand,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    (-0.25).sp
            )


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Text(

                text =
                    "Acompanhamento em andamento",

                color =
                    Color(0xFF494949),

                fontFamily =
                    Quicksand,

                fontSize =
                    8.sp
            )
        }
    }
}


/* =========================================================
   RESUMO
========================================================= */

@Composable
private fun Resumo(

    batimentos: Float,

    respiracao: Float,

    temperatura: Float

) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        ResumoItem(

            modifier =
                Modifier.weight(1f),

            label =
                "BATIMENTOS",

            valor =
                batimentos
                    .roundToInt()
                    .toString(),

            unidade =
                "bpm"
        )


        ResumoItem(

            modifier =
                Modifier.weight(1f),

            label =
                "RESPIRAÇÃO",

            valor =
                formatar(
                    respiracao
                ),

            unidade =
                "rpm"
        )


        ResumoItem(

            modifier =
                Modifier.weight(1f),

            label =
                "TEMPERATURA",

            valor =
                formatar(
                    temperatura
                ),

            unidade =
                "°C"
        )
    }
}


/* =========================================================
   RESUMO ITEM
========================================================= */

@Composable
private fun ResumoItem(

    modifier: Modifier,

    label: String,

    valor: String,

    unidade: String

) {

    Column(

        modifier =
            modifier
                .height(79.dp)
                .clip(
                    RoundedCornerShape(
                        17.dp
                    )
                )
                .background(
                    CardFundo
                )
                .border(
                    1.dp,
                    Borda,
                    RoundedCornerShape(
                        17.dp
                    )
                )
                .padding(13.dp),

        verticalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(

            text =
                label,

            color =
                Color(0xFF444444),

            fontFamily =
                Quicksand,

            fontSize =
                7.sp,

            fontWeight =
                FontWeight.Bold,

            letterSpacing =
                0.7.sp
        )


        Row(
            verticalAlignment =
                Alignment.Bottom
        ) {

            Text(

                text =
                    valor,

                color =
                    Color(0xFFEEEEEE),

                fontFamily =
                    Quicksand,

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.width(4.dp)
            )


            Text(

                text =
                    unidade,

                color =
                    Color(0xFF444444),

                fontFamily =
                    Quicksand,

                fontSize =
                    7.sp
            )
        }
    }
}


/* =========================================================
   CABEÇALHO DA SEÇÃO
========================================================= */

@Composable
private fun CabecalhoSecao(

    titulo: String,

    descricao: String? = null

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 3.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(

            text =
                titulo,

            color =
                Color(0xFFE9E9E9),

            fontFamily =
                Quicksand,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Bold,

            letterSpacing =
                (-0.25).sp
        )


        if (descricao != null) {

            Text(

                text =
                    descricao.uppercase(),

                color =
                    Color(0xFF3E3E3E),

                fontFamily =
                    Quicksand,

                fontSize =
                    7.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    0.7.sp
            )
        }
    }
}


/* =========================================================
   INDICADORES
========================================================= */

@Composable
private fun Indicadores(
    dados: DadosSaude
) {

    /*
     * No HTML:
     *
     * grid-template-columns: repeat(2, 1fr)
     *
     * Aqui reproduzimos a grade 2 x 2.
     */

    Column(

        verticalArrangement =
            Arrangement.spacedBy(
                11.dp
            )
    ) {

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    11.dp
                )
        ) {

            Indicador(

                modifier =
                    Modifier.weight(1f),

                numero =
                    "01",

                nome =
                    "Batimentos",

                valor =
                    dados.batimentos.valor
                        .roundToInt()
                        .toString(),

                unidade =
                    "bpm",

                progresso =
                    percentual(
                        dados.batimentos.valor,
                        dados.batimentos.min,
                        dados.batimentos.max
                    ),

                imagem =
                    R.drawable.batimentos1
            )


            Indicador(

                modifier =
                    Modifier.weight(1f),

                numero =
                    "02",

                nome =
                    "Respiração",

                valor =
                    formatar(
                        dados.respiracao.valor
                    ),

                unidade =
                    "rpm",

                progresso =
                    percentual(
                        dados.respiracao.valor,
                        dados.respiracao.min,
                        dados.respiracao.max
                    ),

                imagem =
                    R.drawable.respiracao1
            )
        }


        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    11.dp
                )
        ) {

            Indicador(

                modifier =
                    Modifier.weight(1f),

                numero =
                    "03",

                nome =
                    "Temperatura",

                valor =
                    formatar(
                        dados.temperatura.valor
                    ),

                unidade =
                    "°C",

                progresso =
                    percentual(
                        dados.temperatura.valor,
                        dados.temperatura.min,
                        dados.temperatura.max
                    ),

                imagem =
                    R.drawable.temperatura1
            )


            Indicador(

                modifier =
                    Modifier.weight(1f),

                numero =
                    "04",

                nome =
                    "Hidratação",

                valor =
                    dados.hidratacao.valor
                        .roundToInt()
                        .toString(),

                unidade =
                    "%",

                progresso =
                    percentual(
                        dados.hidratacao.valor,
                        dados.hidratacao.min,
                        dados.hidratacao.max
                    ),

                imagem =
                    R.drawable.hidratacao1
            )
        }
    }
}


/* =========================================================
   CARD INDICADOR
========================================================= */

@Composable
private fun Indicador(

    modifier: Modifier,

    numero: String,

    nome: String,

    valor: String,

    unidade: String,

    progresso: Float,

    imagem: Int

) {

    Column(

        modifier =
            modifier
                .height(148.dp)
                .clip(
                    RoundedCornerShape(
                        20.dp
                    )
                )
                .background(
                    CardFundo
                )
                .border(
                    1.dp,
                    Borda,
                    RoundedCornerShape(
                        20.dp
                    )
                )
                .padding(16.dp)
    ) {

        /* -----------------------------------------
           TOPO
        ----------------------------------------- */

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(

                modifier =
                    Modifier
                        .size(38.dp)
                        .clip(
                            RoundedCornerShape(
                                12.dp
                            )
                        )
                        .background(
                            Color(0xFF0C0C0C)
                        )
                        .border(
                            1.dp,
                            BordaIcone,
                            RoundedCornerShape(
                                12.dp
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                ImageOriginal(

                    resource =
                        imagem,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(19.dp)
                )
            }


            Text(

                text =
                    numero,

                color =
                    Color(0xFF383838),

                fontFamily =
                    Quicksand,

                fontSize =
                    7.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }


        Spacer(
            modifier =
                Modifier.height(15.dp)
        )


        Text(

            text =
                nome,

            color =
                Color(0xFF666666),

            fontFamily =
                Quicksand,

            fontSize =
                9.sp,

            fontWeight =
                FontWeight.SemiBold
        )


        Spacer(
            modifier =
                Modifier.weight(1f)
        )


        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 8.dp
                    ),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.Bottom
        ) {

            Text(

                text =
                    valor,

                color =
                    TextoBranco,

                fontFamily =
                    Quicksand,

                fontSize =
                    22.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    (-0.8).sp
            )


            Text(

                text =
                    unidade,

                color =
                    Color(0xFF444444),

                fontFamily =
                    Quicksand,

                fontSize =
                    8.sp,

                fontWeight =
                    FontWeight.SemiBold
            )
        }


        /* -----------------------------------------
           LINHA
        ----------------------------------------- */

        Box(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(
                        RoundedCornerShape(
                            999.dp
                        )
                    )
                    .background(
                        Color(0xFF111111)
                    )
        ) {

            val largura by
                animateFloatAsState(

                    targetValue =
                        progresso
                            .coerceIn(
                                0.08f,
                                1f
                            ),

                    animationSpec =
                        tween(
                            durationMillis =
                                700
                        ),

                    label =
                        "linha"
                )


            Box(

                modifier =
                    Modifier
                        .fillMaxWidth(
                            largura
                        )
                        .fillMaxHeight()
                        .clip(
                            RoundedCornerShape(
                                999.dp
                            )
                        )
                        .background(
                            Azul.copy(
                                alpha =
                                    0.5f
                            )
                        )
            )
        }
    }
}


/* =========================================================
   BEM-ESTAR
========================================================= */

@Composable
private fun BemEstar(
    valor: Float
) {

    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .background(
                    CardFundo
                )
                .border(
                    1.dp,
                    Color(0xFF1A1A1A),
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .padding(20.dp)
    ) {

        /* -----------------------------------------
           TOPO
        ----------------------------------------- */

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Row(

                modifier =
                    Modifier.weight(1f),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(

                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(
                                RoundedCornerShape(
                                    13.dp
                                )
                            )
                            .background(
                                Color(0xFF0C0C0C)
                            )
                            .border(
                                1.dp,
                                BordaIcone,
                                RoundedCornerShape(
                                    13.dp
                                )
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    ImageOriginal(

                        resource =
                            R.drawable.bemestar1,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(20.dp)
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )


                Column {

                    Text(

                        text =
                            "Estado geral",

                        color =
                            Color(0xFFDDDDDD),

                        fontFamily =
                            Quicksand,

                        fontSize =
                            10.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )


                    Text(

                        text =
                            "Estimativa demonstrativa",

                        color =
                            Color(0xFF454545),

                        fontFamily =
                            Quicksand,

                        fontSize =
                            7.sp
                    )
                }
            }


            Row(
                verticalAlignment =
                    Alignment.Bottom
            ) {

                Text(

                    text =
                        valor
                            .roundToInt()
                            .toString(),

                    color =
                        Color(0xFFF3F3F3),

                    fontFamily =
                        Quicksand,

                    fontSize =
                        27.sp,

                    fontWeight =
                        FontWeight.Bold,

                    letterSpacing =
                        (-1.2).sp
                )


                Spacer(
                    modifier =
                        Modifier.width(3.dp)
                )


                Text(

                    text =
                        "/100",

                    color =
                        Color(0xFF444444),

                    fontFamily =
                        Quicksand,

                    fontSize =
                        8.sp
                )
            }
        }


        /* -----------------------------------------
           INFO
        ----------------------------------------- */

        Spacer(
            modifier =
                Modifier.height(25.dp)
        )


        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            BemEstarInfo(
                titulo =
                    "MÍNIMO",

                valor =
                    "60"
            )


            BemEstarInfo(
                titulo =
                    "ATUAL",

                valor =
                    valor
                        .roundToInt()
                        .toString()
            )


            BemEstarInfo(
                titulo =
                    "MÁXIMO",

                valor =
                    "88"
            )
        }


        /* -----------------------------------------
           BARRA
        ----------------------------------------- */

        Spacer(
            modifier =
                Modifier.height(19.dp)
        )


        val largura by
            animateFloatAsState(

                targetValue =
                    (
                        valor / 100f
                    ).coerceIn(
                        0f,
                        1f
                    ),

                animationSpec =
                    tween(
                        durationMillis =
                            800
                    ),

                label =
                    "bem-estar"
            )


        Box(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(
                        RoundedCornerShape(
                            999.dp
                        )
                    )
                    .background(
                        Color(0xFF111111)
                    )
        ) {

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth(
                            largura
                        )
                        .height(5.dp)
                        .clip(
                            RoundedCornerShape(
                                999.dp
                            )
                        )
                        .background(
                            AzulBemEstar
                        )
            )
        }
    }
}


/* =========================================================
   BEM-ESTAR INFO
========================================================= */

@Composable
private fun BemEstarInfo(

    titulo: String,

    valor: String

) {

    Column(

        verticalArrangement =
            Arrangement.spacedBy(
                5.dp
            )
    ) {

        Text(

            text =
                titulo,

            color =
                Color(0xFF3B3B3B),

            fontFamily =
                Quicksand,

            fontSize =
                7.sp,

            fontWeight =
                FontWeight.SemiBold
        )


        Text(

            text =
                valor,

            color =
                Color(0xFF999999),

            fontFamily =
                Quicksand,

            fontSize =
                9.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


/* =========================================================
   ATIVIDADE
========================================================= */

@Composable
private fun Atividade() {

    Column(

        verticalArrangement =
            Arrangement.spacedBy(
                9.dp
            )
    ) {

        AtividadeRow(

            titulo =
                "Indicadores atualizados",

            descricao =
                "Dados renovados automaticamente",

            tempo =
                "agora"
        )


        AtividadeRow(

            titulo =
                "Bem-estar recalculado",

            descricao =
                "Índice atualizado",

            tempo =
                "3 min"
        )


        AtividadeRow(

            titulo =
                "Monitoramento iniciado",

            descricao =
                "Sessão atual do assistente",

            tempo =
                "hoje"
        )
    }
}


/* =========================================================
   ATIVIDADE ROW
========================================================= */

@Composable
private fun AtividadeRow(

    titulo: String,

    descricao: String,

    tempo: String

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(65.dp)
                .clip(
                    RoundedCornerShape(
                        17.dp
                    )
                )
                .background(
                    ActivityFundo
                )
                .border(
                    1.dp,
                    Color(0xFF171717),
                    RoundedCornerShape(
                        17.dp
                    )
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {


        Box(

            modifier =
                Modifier
                    .size(7.dp)
                    .clip(
                        CircleShape
                    )
                    .background(
                        Azul
                    )
        )


        Spacer(
            modifier =
                Modifier.width(13.dp)
        )


        Column(

            modifier =
                Modifier.weight(1f)
        ) {

            Text(

                text =
                    titulo,

                color =
                    Color(0xFF7F7F7F),

                fontFamily =
                    Quicksand,

                fontSize =
                    9.sp,

                fontWeight =
                    FontWeight.Bold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            Text(

                text =
                    descricao,

                color =
                    TextoMuitoEscuro,

                fontFamily =
                    Quicksand,

                fontSize =
                    7.sp,

                lineHeight =
                    10.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }


        Spacer(
            modifier =
                Modifier.width(13.dp)
        )


        Text(

            text =
                tempo,

            color =
                TextoMuitoEscuro,

            fontFamily =
                Quicksand,

            fontSize =
                7.sp,

            fontWeight =
                FontWeight.SemiBold
        )
    }
}


/* =========================================================
   IMAGEM ORIGINAL
========================================================= */

@Composable
private fun ImageOriginal(

    resource: Int,

    contentDescription: String?,

    modifier: Modifier

) {

    androidx.compose.foundation.Image(

        painter =
            painterResource(
                id = resource
            ),

        contentDescription =
            contentDescription,

        modifier =
            modifier
    )
}