package com.mulheres

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val Fundo = Color(0xFF050507)
private val SurfaceDark = Color(0xFF111116)
private val SurfaceLight = Color(0xFF18181F)
private val Branco = Color(0xFFFFFFFF)
private val Texto = Color(0xFFEEEEF4)
private val Secundario = Color(0xFFAAAAB8)
private val Muted = Color(0xFF70707E)
private val Rosa = Color(0xFFFF3D82)
private val RosaClara = Color(0xFFFF8FB5)
private val Roxo = Color(0xFF9A7CFF)
private val Ciano = Color(0xFF54DED4)
private val Verde = Color(0xFF69DFA1)
private val Laranja = Color(0xFFFFB45E)

class AssistenteActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Fundo
                ) {

                    AssistenteSaude()
                }
            }
        }
    }
}


/* =========================================================
   FONTE DOS ASSETS
========================================================= */

@Composable
fun FonteQuicksand(): FontFamily {

    return remember {

        FontFamily(
            Font(
                "font.ttf",
                FontWeight.Normal
            ),
            Font(
                "font.ttf",
                FontWeight.Medium
            ),
            Font(
                "font.ttf",
                FontWeight.SemiBold
            ),
            Font(
                "font.ttf",
                FontWeight.Bold
            ),
            Font(
                "font.ttf",
                FontWeight.ExtraBold
            )
        )
    }
}


/* =========================================================
   MODELOS
========================================================= */

data class Indicador(
    val nome: String,
    val valor: String,
    val unidade: String,
    val cor: Color
)

data class DadosSaude(
    val batimentos: Int,
    val respiracao: Int,
    val temperatura: Double,
    val hidratacao: Int
)


/* =========================================================
   TELA PRINCIPAL
========================================================= */

@Composable
fun AssistenteSaude() {

    val fonte = FonteQuicksand()

    var dados by remember {

        mutableStateOf(
            DadosSaude(
                batimentos = 72,
                respiracao = 16,
                temperatura = 36.5,
                hidratacao = 78
            )
        )
    }

    /*
     * Valores apenas demonstrativos para a interface.
     */

    LaunchedEffect(Unit) {

        while (true) {

            delay(3000)

            dados = dados.copy(

                batimentos =
                    Random.nextInt(
                        68,
                        83
                    ),

                respiracao =
                    Random.nextInt(
                        14,
                        20
                    ),

                temperatura =
                    Random.nextDouble(
                        36.2,
                        36.9
                    ),

                hidratacao =
                    Random.nextInt(
                        70,
                        91
                    )
            )
        }
    }


    LazyColumn(

        modifier =
            Modifier
                .fillMaxSize()
                .background(Fundo)
                .padding(
                    horizontal = 17.dp,
                    vertical = 20.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(18.dp)
    ) {

        item {

            Cabecalho(
                fonte = fonte
            )
        }


        item {

            HeroSaude(
                fonte = fonte
            )
        }


        item {

            ResumoSaude(
                fonte = fonte,
                dados = dados
            )
        }


        item {

            IndicadoresSaude(
                fonte = fonte,
                dados = dados
            )
        }


        item {

            BemEstar(
                fonte = fonte,
                dados = dados
            )
        }


        item {

            Atividades(
                fonte = fonte
            )
        }


        item {

            BotaoCalculadora(
                fonte = fonte
            )
        }


        item {

            Spacer(
                modifier =
                    Modifier.height(70.dp)
            )
        }
    }
}


/* =========================================================
   CABEÇALHO
========================================================= */

@Composable
fun Cabecalho(
    fonte: FontFamily
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Column {

            Text(
                text = "ASSISTENTE",
                fontFamily = fonte,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = RosaClara
            )

            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )

            Text(
                text = "Saúde",
                fontFamily = fonte,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Branco
            )
        }


        Box(
            modifier =
                Modifier
                    .size(50.dp)
                    .background(
                        color = SurfaceLight,
                        shape = RoundedCornerShape(18.dp)
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.HealthAndSafety,

                contentDescription =
                    "Assistente de saúde",

                tint =
                    Rosa,

                modifier =
                    Modifier.size(27.dp)
            )
        }
    }
}


/* =========================================================
   HERO
========================================================= */

@Composable
fun HeroSaude(
    fonte: FontFamily
) {

    val transicao =
        rememberInfiniteTransition(
            label = "pulso"
        )

    val alpha by
        transicao.animateFloat(

            initialValue = 0.45f,

            targetValue = 1f,

            animationSpec =
                infiniteRepeatable(

                    animation =
                        tween(
                            durationMillis = 1800
                        ),

                    repeatMode =
                        RepeatMode.Reverse
                ),

            label = "alpha"
        )


    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(30.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF19151F)
            )
    ) {

        Column(

            modifier =
                Modifier.padding(23.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(9.dp)
                            .alpha(alpha)
                            .background(
                                Verde,
                                RoundedCornerShape(50)
                            )
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text = "SISTEMA ATIVO",
                    fontFamily = fonte,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = Verde
                )
            }


            Spacer(
                modifier =
                    Modifier.height(15.dp)
            )


            Text(
                text = "Seu bem-estar\nem um só lugar.",
                fontFamily = fonte,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 29.sp,
                color = Branco
            )


            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )


            Text(
                text =
                    "Acompanhe indicadores demonstrativos " +
                    "e visualize seu estado geral.",

                fontFamily = fonte,

                fontSize = 11.sp,

                fontWeight =
                    FontWeight.Medium,

                lineHeight = 17.sp,

                color =
                    Secundario
            )
        }
    }
}


/* =========================================================
   RESUMO
========================================================= */

@Composable
fun ResumoSaude(
    fonte: FontFamily,
    dados: DadosSaude
) {

    Column {

        TituloSecao(
            texto = "VISÃO GERAL",
            fonte = fonte
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            ResumoCard(
                modifier =
                    Modifier.weight(1f),

                titulo = "BATIMENTOS",
                valor =
                    dados.batimentos.toString(),
                unidade = "BPM",
                cor = Rosa,
                fonte = fonte
            )


            ResumoCard(
                modifier =
                    Modifier.weight(1f),

                titulo = "RESPIRAÇÃO",
                valor =
                    dados.respiracao.toString(),
                unidade = "RPM",
                cor = Roxo,
                fonte = fonte
            )
        }
    }
}


@Composable
fun ResumoCard(
    modifier: Modifier,
    titulo: String,
    valor: String,
    unidade: String,
    cor: Color,
    fonte: FontFamily
) {

    Card(

        modifier = modifier,

        shape =
            RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    SurfaceDark
            )
    ) {

        Column(
            modifier =
                Modifier.padding(17.dp)
        ) {

            Text(
                text = titulo,
                fontFamily = fonte,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = Muted
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Row(
                verticalAlignment =
                    Alignment.Bottom
            ) {

                Text(
                    text = valor,
                    fontFamily = fonte,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Branco
                )

                Spacer(
                    modifier =
                        Modifier.width(4.dp)
                )

                Text(
                    text = unidade,
                    fontFamily = fonte,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = cor,
                    modifier =
                        Modifier.padding(
                            bottom = 5.dp
                        )
                )
            }
        }
    }
}


/* =========================================================
   INDICADORES
========================================================= */

@Composable
fun IndicadoresSaude(
    fonte: FontFamily,
    dados: DadosSaude
) {

    val indicadores =
        listOf(

            Indicador(
                "Frequência cardíaca",
                dados.batimentos.toString(),
                "BPM",
                Rosa
            ),

            Indicador(
                "Respiração",
                dados.respiracao.toString(),
                "RPM",
                Roxo
            ),

            Indicador(
                "Temperatura",
                String.format(
                    "%.1f",
                    dados.temperatura
                ),
                "°C",
                Laranja
            ),

            Indicador(
                "Hidratação",
                dados.hidratacao.toString(),
                "%",
                Ciano
            )
        )


    Column {

        TituloSecao(
            texto = "INDICADORES",
            fonte = fonte
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        indicadores.forEach { indicador ->

            IndicadorCard(
                indicador = indicador,
                fonte = fonte
            )

            Spacer(
                modifier =
                    Modifier.height(9.dp)
            )
        }
    }
}


@Composable
fun IndicadorCard(
    indicador: Indicador,
    fonte: FontFamily
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(21.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    SurfaceDark
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(17.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(43.dp)
                        .background(
                            indicador.cor.copy(
                                alpha = .12f
                            ),
                            RoundedCornerShape(15.dp)
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(8.dp)
                            .background(
                                indicador.cor,
                                RoundedCornerShape(50)
                            )
                )
            }


            Spacer(
                modifier =
                    Modifier.width(13.dp)
            )


            Text(
                text =
                    indicador.nome,

                modifier =
                    Modifier.weight(1f),

                fontFamily =
                    fonte,

                fontSize =
                    12.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Texto
            )


            Text(
                text =
                    indicador.valor,

                fontFamily =
                    fonte,

                fontSize =
                    19.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                color =
                    Branco
            )


            Spacer(
                modifier =
                    Modifier.width(4.dp)
            )


            Text(
                text =
                    indicador.unidade,

                fontFamily =
                    fonte,

                fontSize =
                    8.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    indicador.cor
            )
        }
    }
}


/* =========================================================
   BEM-ESTAR
========================================================= */

@Composable
fun BemEstar(
    fonte: FontFamily,
    dados: DadosSaude
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(27.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF151A1A)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(21.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(47.dp)
                            .background(
                                Ciano.copy(
                                    alpha = .12f
                                ),
                                RoundedCornerShape(16.dp)
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.WaterDrop,

                        contentDescription =
                            null,

                        tint =
                            Ciano,

                        modifier =
                            Modifier.size(24.dp)
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(13.dp)
                )


                Column {

                    Text(
                        text =
                            "BEM-ESTAR",

                        fontFamily =
                            fonte,

                        fontSize =
                            8.sp,

                        fontWeight =
                            FontWeight.ExtraBold,

                        letterSpacing =
                            1.5.sp,

                        color =
                            Ciano
                    )


                    Text(
                        text =
                            "Estado geral",

                        fontFamily =
                            fonte,

                        fontSize =
                            18.sp,

                        fontWeight =
                            FontWeight.ExtraBold,

                        color =
                            Branco
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(17.dp)
            )


            Text(
                text =
                    "Hidratação demonstrativa",

                fontFamily =
                    fonte,

                fontSize =
                    10.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Secundario
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .background(
                            Color(0xFF292D2D),
                            RoundedCornerShape(99.dp)
                        )
            ) {

                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth(
                                dados.hidratacao /
                                    100f
                            )
                            .height(9.dp)
                            .background(
                                Ciano,
                                RoundedCornerShape(99.dp)
                            )
                )
            }
        }
    }
}


/* =========================================================
   ATIVIDADES
========================================================= */

@Composable
fun Atividades(
    fonte: FontFamily
) {

    Column {

        TituloSecao(
            texto = "ATIVIDADE",
            fonte = fonte
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        AtividadeLinha(
            titulo =
                "Monitoramento ativo",

            descricao =
                "Assistente funcionando",

            cor =
                Verde,

            fonte =
                fonte
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        AtividadeLinha(
            titulo =
                "Última atualização",

            descricao =
                "Agora",

            cor =
                Rosa,

            fonte =
                fonte
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        AtividadeLinha(
            titulo =
                "Proteção",

            descricao =
                "Sistema disponível",

            cor =
                Roxo,

            fonte =
                fonte
        )
    }
}


@Composable
fun AtividadeLinha(
    titulo: String,
    descricao: String,
    cor: Color,
    fonte: FontFamily
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    SurfaceDark
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(15.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .background(
                            cor,
                            RoundedCornerShape(50)
                        )
            )


            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )


            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        titulo,

                    fontFamily =
                        fonte,

                    fontSize =
                        11.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Texto
                )


                Text(
                    text =
                        descricao,

                    fontFamily =
                        fonte,

                    fontSize =
                        9.sp,

                    fontWeight =
                        FontWeight.Medium,

                    color =
                        Muted
                )
            }


            Icon(
                imageVector =
                    Icons.Default.KeyboardArrowRight,

                contentDescription =
                    null,

                tint =
                    Muted
            )
        }
    }
}


/* =========================================================
   BOTÃO DA CALCULADORA
   AO CLICAR -> MainActivity
========================================================= */

@Composable
fun BotaoCalculadora(
    fonte: FontFamily
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current


    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {

                    val intent =
                        Intent(
                            context,
                            MainActivity::class.java
                        )

                    context.startActivity(
                        intent
                    )
                },

        shape =
            RoundedCornerShape(25.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    SurfaceLight
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(19.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(

                modifier =
                    Modifier
                        .size(55.dp)
                        .background(
                            Rosa.copy(
                                alpha = .12f
                            ),
                            RoundedCornerShape(18.dp)
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(

                    imageVector =
                        Icons.Default.Calculate,

                    contentDescription =
                        "Calculadora",

                    tint =
                        Rosa,

                    modifier =
                        Modifier.size(27.dp)
                )
            }


            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )


            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "Calculadora",

                    fontFamily =
                        fonte,

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    color =
                        Branco
                )


                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )


                Text(
                    text =
                        "Abrir a tela principal",

                    fontFamily =
                        fonte,

                    fontSize =
                        9.sp,

                    fontWeight =
                        FontWeight.Medium,

                    color =
                        Secundario
                )
            }


            Icon(
                imageVector =
                    Icons.Default.KeyboardArrowRight,

                contentDescription =
                    null,

                tint =
                    Muted
            )
        }
    }
}


/* =========================================================
   TÍTULO DE SEÇÃO
========================================================= */

@Composable
fun TituloSecao(
    texto: String,
    fonte: FontFamily
) {

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier =
                Modifier
                    .size(7.dp)
                    .background(
                        Rosa,
                        RoundedCornerShape(2.dp)
                    )
        )


        Spacer(
            modifier =
                Modifier.width(9.dp)
        )


        Text(
            text =
                texto,

            fontFamily =
                fonte,

            fontSize =
                9.sp,

            fontWeight =
                FontWeight.ExtraBold,

            letterSpacing =
                1.8.sp,

            color =
                Muted
        )
    }
}