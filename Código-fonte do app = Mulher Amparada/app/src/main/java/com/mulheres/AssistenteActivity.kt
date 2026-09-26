package com.mulheres

import android.os.Bundle
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

class AssistenteActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
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

private val Fundo = Color.Black
private val Card = Color(0xFF050505)
private val CardSecundario = Color(0xFF040404)

private val Borda = Color(0xFF181818)
private val BordaClara = Color(0xFF1D1D1D)

private val TextoPrincipal = Color(0xFFF5F5F5)
private val TextoSecundario = Color(0xFF999999)
private val TextoEscuro = Color(0xFF454545)
private val TextoMuitoEscuro = Color(0xFF3B3B3B)

private val Azul = Color(0xFF5666FF)
private val AzulClaro = Color(0xFF6070FF)


/* =========================================================
   FONTE
========================================================= */

/*
 * Coloque sua font.ttf em:
 *
 * app/src/main/res/font/quicksand.ttf
 *
 * Se o arquivo tiver outro nome, altere aqui.
 */

private val Quicksand = FontFamily(
    Font(
        resId = com.mulheres.R.font.quicksand,
        weight = FontWeight.Normal
    ),
    Font(
        resId = com.mulheres.R.font.quicksand,
        weight = FontWeight.Bold
    )
)


/* =========================================================
   DADOS
========================================================= */

data class Indicador(
    var valor: Float,
    val min: Float,
    val max: Float,
    val passo: Float
)

data class DadosSaude(
    var batimentos: Indicador = Indicador(
        72f,
        65f,
        82f,
        2f
    ),

    var respiracao: Indicador = Indicador(
        16f,
        12f,
        20f,
        .5f
    ),

    var temperatura: Indicador = Indicador(
        36.5f,
        36.1f,
        36.9f,
        .05f
    ),

    var hidratacao: Indicador = Indicador(
        68f,
        55f,
        82f,
        1f
    ),

    var bemEstar: Indicador = Indicador(
        72f,
        60f,
        88f,
        2f
    )
)


/* =========================================================
   VARIAÇÃO
========================================================= */

private fun variar(
    indicador: Indicador
): Float {

    val variacao =
        (Random.nextFloat() * 2f - 1f) *
                indicador.passo

    return (
        indicador.valor + variacao
    ).coerceIn(
        indicador.min,
        indicador.max
    )
}


/* =========================================================
   TELA PRINCIPAL
========================================================= */

@Composable
fun AssistenteSaude() {

    var dados by remember {
        mutableFloatStateOf(72f)
    }

    val estado = remember {
        DadosSaude()
    }

    LaunchedEffect(Unit) {

        while (true) {

            delay(3000)

            estado.batimentos.valor =
                variar(estado.batimentos)

            estado.respiracao.valor =
                variar(estado.respiracao)

            estado.temperatura.valor =
                variar(estado.temperatura)

            estado.hidratacao.valor =
                variar(estado.hidratacao)

            estado.bemEstar.valor =
                variar(estado.bemEstar)

            /*
             * Força recomposição.
             */
            dados = estado.bemEstar.valor
        }
    }

    LazyColumn(
        modifier = Modifier
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

        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        item {

            Cabecalho(
                onSecretClick = {
                    /*
                     * Equivalente ao:
                     *
                     * window.location.href =
                     * "index11.html"
                     *
                     * Aqui você pode abrir outra Activity.
                     */
                }
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )
        }


        item {

            Hero(
                bemEstar = estado.bemEstar.valor
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }


        item {

            Resumo(
                batimentos = estado.batimentos.valor,
                respiracao = estado.respiracao.valor,
                temperatura = estado.temperatura.valor
            )
        }


        item {

            SecaoTitulo(
                titulo = "Indicadores",
                direita = "Atualização contínua"
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Indicadores(
                dados = estado
            )
        }


        item {

            Spacer(
                modifier = Modifier.height(36.dp)
            )

            SecaoTitulo(
                titulo = "Bem-estar",
                direita = "Índice geral"
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            BemEstar(
                valor = estado.bemEstar.valor
            )
        }


        item {

            Spacer(
                modifier = Modifier.height(36.dp)
            )

            SecaoTitulo(
                titulo = "Atividade"
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Atividade()

            Spacer(
                modifier = Modifier.height(14.dp)
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "CUIDADO E ACOMPANHAMENTO",
                color = Color(0xFF4B4B4B),
                fontFamily = Quicksand,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Assistente de Saúde",
                color = TextoPrincipal,
                fontFamily = Quicksand,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.1).sp,
                lineHeight = 25.sp
            )

            Spacer(
                modifier = Modifier.height(9.dp)
            )

            Text(
                text = "Um espaço para acompanhar seu bem-estar.",
                color = Color(0xFF4B4B4B),
                fontFamily = Quicksand,
                fontSize = 9.sp,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }


        Spacer(
            modifier = Modifier.width(20.dp)
        )


        Card(
            onClick = onSecretClick,

            modifier = Modifier.size(42.dp),

            shape = RoundedCornerShape(14.dp),

            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF080808)
            ),

            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFF1E1E1E)
            )
        ) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                /*
                 * Coloque o SVG 8.svg em drawable
                 * e altere para R.drawable.oito
                 */

                /*
                Image(
                    painter = painterResource(R.drawable.oito),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
                */
            }
        }
    }
}


/* =========================================================
   HERO
========================================================= */

@Composable
private fun Hero(
    bemEstar: Float
) {

    val pulse = rememberInfiniteTransition(
        label = "pulse"
    )

    val opacity by pulse.animateFloat(
        initialValue = .4f,
        targetValue = 1f,

        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 1000,
                        easing = LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),

        label = "live"
    )


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(315.dp),

        shape = RoundedCornerShape(26.dp),

        colors = CardDefaults.cardColors(
            containerColor = Card
        ),

        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BordaClara
        )
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {


            /* -----------------------------------------
               TOPO
            ----------------------------------------- */

            Row(
                modifier = Modifier
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
                    text = "ESTADO GERAL",
                    color = Color(0xFF494949),
                    fontFamily = Quicksand,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )


                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                Azul.copy(alpha = opacity)
                            )
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text(
                        text = "ATIVO",
                        color = Color(0xFF494949),
                        fontFamily = Quicksand,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = .8.sp
                    )
                }
            }


            /* -----------------------------------------
               CENTRO
            ----------------------------------------- */

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 5.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(
                            RoundedCornerShape(20.dp)
                        )
                        .background(
                            Color(0xFF0B0D17)
                        )
                        .border(
                            1.dp,
                            Color(0xFF282B40),
                            RoundedCornerShape(20.dp)
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    /*
                    Image(
                        painter =
                            painterResource(
                                R.drawable.assistente1
                            ),
                        contentDescription = null,
                        modifier =
                            Modifier.size(30.dp)
                    )
                    */
                }


                Spacer(
                    modifier = Modifier.height(17.dp)
                )


                Row(
                    verticalAlignment =
                        Alignment.Bottom
                ) {

                    Text(
                        text =
                            bemEstar
                                .roundToInt()
                                .toString(),

                        color = Color.White,
                        fontFamily = Quicksand,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.2).sp
                    )

                    Spacer(
                        modifier = Modifier.width(4.dp)
                    )

                    Text(
                        text = "/ 100",
                        color = Color(0xFF454545),
                        fontFamily = Quicksand,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }


            /* -----------------------------------------
               STATUS
            ----------------------------------------- */

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Tudo tranquilo.",
                    color = Color(0xFFEDEDED),
                    fontFamily = Quicksand,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Acompanhamento em andamento",
                    color = Color(0xFF494949),
                    fontFamily = Quicksand,
                    fontSize = 8.sp
                )
            }
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        ResumoItem(
            modifier = Modifier.weight(1f),
            label = "BATIMENTOS",
            value = batimentos.roundToInt().toString(),
            unit = "bpm"
        )

        ResumoItem(
            modifier = Modifier.weight(1f),
            label = "RESPIRAÇÃO",
            value = "%.1f".format(respiracao),
            unit = "rpm"
        )

        ResumoItem(
            modifier = Modifier.weight(1f),
            label = "TEMPERATURA",
            value = "%.1f".format(temperatura),
            unit = "°C"
        )
    }
}


@Composable
private fun ResumoItem(
    modifier: Modifier,
    label: String,
    value: String,
    unit: String
) {

    Card(
        modifier = modifier.height(79.dp),

        shape = RoundedCornerShape(17.dp),

        colors = CardDefaults.cardColors(
            containerColor = Card
        ),

        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Borda
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),

            verticalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text = label,
                color = Color(0xFF444444),
                fontFamily = Quicksand,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = .7.sp
            )


            Row(
                verticalAlignment =
                    Alignment.Bottom
            ) {

                Text(
                    text = value,
                    color = Color(0xFFEEEEEE),
                    fontFamily = Quicksand,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = unit,
                    color = Color(0xFF444444),
                    fontFamily = Quicksand,
                    fontSize = 7.sp
                )
            }
        }
    }
}


/* =========================================================
   TÍTULO DE SEÇÃO
========================================================= */

@Composable
private fun SecaoTitulo(
    titulo: String,
    direita: String? = null
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 3.dp,
                end = 3.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = titulo,
            color = Color(0xFFE9E9E9),
            fontFamily = Quicksand,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-.25).sp
        )


        if (direita != null) {

            Text(
                text = direita,
                color = Color(0xFF3E3E3E),
                fontFamily = Quicksand,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = .7.sp
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

    Column(
        verticalArrangement =
            Arrangement.spacedBy(11.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(11.dp)
        ) {

            IndicadorCard(
                modifier = Modifier.weight(1f),
                numero = "01",
                nome = "Batimentos",
                valor =
                    dados.batimentos.valor
                        .roundToInt()
                        .toString(),
                unidade = "bpm",
                percentual =
                    percentual(
                        dados.batimentos
                    )
            )

            IndicadorCard(
                modifier = Modifier.weight(1f),
                numero = "02",
                nome = "Respiração",
                valor =
                    "%.1f".format(
                        dados.respiracao.valor
                    ),
                unidade = "rpm",
                percentual =
                    percentual(
                        dados.respiracao
                    )
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(11.dp)
        ) {

            IndicadorCard(
                modifier = Modifier.weight(1f),
                numero = "03",
                nome = "Temperatura",
                valor =
                    "%.1f".format(
                        dados.temperatura.valor
                    ),
                unidade = "°C",
                percentual =
                    percentual(
                        dados.temperatura
                    )
            )

            IndicadorCard(
                modifier = Modifier.weight(1f),
                numero = "04",
                nome = "Hidratação",
                valor =
                    dados.hidratacao.valor
                        .roundToInt()
                        .toString(),
                unidade = "%",
                percentual =
                    percentual(
                        dados.hidratacao
                    )
            )
        }
    }
}


private fun percentual(
    indicador: Indicador
): Float {

    return (
        (indicador.valor - indicador.min) /
                (indicador.max - indicador.min)
        )
        .coerceIn(0f, 1f)
}


@Composable
private fun IndicadorCard(
    modifier: Modifier,
    numero: String,
    nome: String,
    valor: String,
    unidade: String,
    percentual: Float
) {

    Card(
        modifier = modifier.height(148.dp),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Card
        ),

        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Borda
        )
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                Color(0xFF0C0C0C)
                            )
                            .border(
                                1.dp,
                                Color(0xFF202020),
                                RoundedCornerShape(12.dp)
                            )
                    )


                    Text(
                        text = numero,
                        color = Color(0xFF383838),
                        fontFamily = Quicksand,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


                Spacer(
                    modifier = Modifier.height(15.dp)
                )


                Text(
                    text = nome,
                    color = Color(0xFF666666),
                    fontFamily = Quicksand,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )


                Spacer(
                    modifier = Modifier.weight(1f)
                )


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.Bottom
                ) {

                    Text(
                        text = valor,
                        color = TextoPrincipal,
                        fontFamily = Quicksand,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-.8).sp
                    )


                    Text(
                        text = unidade,
                        color = Color(0xFF444444),
                        fontFamily = Quicksand,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }


            /* Linha inferior */

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 10.dp
                    )
                    .height(2.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF111111))
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            percentual
                                .coerceIn(.08f, 1f)
                        )
                        .height(2.dp)
                        .clip(
                            RoundedCornerShape(999.dp)
                        )
                        .background(
                            Azul.copy(alpha = .5f)
                        )
                )
            }
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

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(22.dp),

        colors = CardDefaults.cardColors(
            containerColor = Card
        ),

        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF1A1A1A)
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier.weight(1f),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(
                                RoundedCornerShape(13.dp)
                            )
                            .background(
                                Color(0xFF0C0C0C)
                            )
                            .border(
                                1.dp,
                                Color(0xFF202020),
                                RoundedCornerShape(13.dp)
                            )
                    )


                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )


                    Column {

                        Text(
                            text = "Estado geral",
                            color = Color(0xFFDDDDDD),
                            fontFamily = Quicksand,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = "Estimativa demonstrativa",
                            color = Color(0xFF454545),
                            fontFamily = Quicksand,
                            fontSize = 7.sp
                        )
                    }
                }


                Row(
                    verticalAlignment =
                        Alignment.Bottom
                ) {

                    Text(
                        text =
                            valor.roundToInt().toString(),

                        color = Color(0xFFF3F3F3),
                        fontFamily = Quicksand,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.2).sp
                    )

                    Spacer(
                        modifier = Modifier.width(3.dp)
                    )

                    Text(
                        text = "/100",
                        color = Color(0xFF444444),
                        fontFamily = Quicksand,
                        fontSize = 8.sp
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(25.dp)
            )


            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                BemEstarInfo(
                    "MÍNIMO",
                    "60"
                )

                BemEstarInfo(
                    "ATUAL",
                    valor.roundToInt().toString()
                )

                BemEstarInfo(
                    "MÁXIMO",
                    "88"
                )
            }


            Spacer(
                modifier = Modifier.height(19.dp)
            )


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(
                        RoundedCornerShape(999.dp)
                    )
                    .background(
                        Color(0xFF111111)
                    )
            ) {

                val progresso =
                    animateFloatAsState(
                        targetValue =
                            (valor / 100f)
                                .coerceIn(0f, 1f),

                        animationSpec =
                            tween(
                                800
                            ),

                        label = "bem-estar"
                    )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            progresso.value
                        )
                        .height(5.dp)
                        .clip(
                            RoundedCornerShape(999.dp)
                        )
                        .background(AzulClaro)
                )
            }
        }
    }
}


@Composable
private fun BemEstarInfo(
    titulo: String,
    valor: String
) {

    Column(
        verticalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {

        Text(
            text = titulo,
            color = Color(0xFF3B3B3B),
            fontFamily = Quicksand,
            fontSize = 7.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = valor,
            color = Color(0xFF999999),
            fontFamily = Quicksand,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
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
            Arrangement.spacedBy(9.dp)
    ) {

        AtividadeRow(
            titulo = "Indicadores atualizados",
            descricao =
                "Dados renovados automaticamente",
            tempo = "agora"
        )

        AtividadeRow(
            titulo = "Bem-estar recalculado",
            descricao =
                "Índice atualizado",
            tempo = "3 min"
        )

        AtividadeRow(
            titulo = "Monitoramento iniciado",
            descricao =
                "Sessão atual do assistente",
            tempo = "hoje"
        )
    }
}


@Composable
private fun AtividadeRow(
    titulo: String,
    descricao: String,
    tempo: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),

        shape = RoundedCornerShape(17.dp),

        colors = CardDefaults.cardColors(
            containerColor = CardSecundario
        ),

        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF171717)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Azul)
            )


            Spacer(
                modifier = Modifier.width(13.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = titulo,
                    color = Color(0xFF7F7F7F),
                    fontFamily = Quicksand,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = descricao,
                    color = TextoMuitoEscuro,
                    fontFamily = Quicksand,
                    fontSize = 7.sp,
                    lineHeight = 10.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }


            Spacer(
                modifier = Modifier.width(13.dp)
            )


            Text(
                text = tempo,
                color = TextoMuitoEscuro,
                fontFamily = Quicksand,
                fontSize = 7.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}