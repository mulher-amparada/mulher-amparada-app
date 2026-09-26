package com.mulheres

import android.os.Bundle
import android.content.Intent
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.roundToInt

/* =========================================================
   ACTIVITY
========================================================= */

class AssistenteActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        window.statusBarColor =
            AndroidColor.TRANSPARENT

        window.navigationBarColor =
            AndroidColor.TRANSPARENT

        if (android.os.Build.VERSION.SDK_INT >= 29) {

            window.isNavigationBarContrastEnforced =
                false

            window.isStatusBarContrastEnforced =
                false
        }

        setContent {

            val modoEscuro =
                isSystemInDarkTheme()

            val controller =
                WindowInsetsControllerCompat(
                    window,
                    window.decorView
                )

            controller.isAppearanceLightStatusBars =
                !modoEscuro

            controller.isAppearanceLightNavigationBars =
                !modoEscuro

            AssistenteTheme {
                AssistenteSaude()
            }
        }
    }
}


/* =========================================================
   CORES
========================================================= */

data class CoresAssistente(

    val fundo: Color,
    val card: Color,
    val cardSecundario: Color,
    val borda: Color,
    val bordaForte: Color,

    val textoPrincipal: Color,
    val textoSecundario: Color,
    val textoEscuro: Color,
    val textoMuitoEscuro: Color,

    val icone: Color,
    val fundoIcone: Color,
    val bordaIcone: Color,

    val azul: Color,
    val azulClaro: Color,
    val linhaFundo: Color
)


private val CoresEscuras =
    CoresAssistente(

        fundo =
            Color.Black,

        card =
            Color(0xFF050505),

        cardSecundario =
            Color(0xFF040404),

        borda =
            Color(0xFF181818),

        bordaForte =
            Color(0xFF1D1D1D),

        textoPrincipal =
            Color(0xFFF5F5F5),

        textoSecundario =
            Color(0xFF999999),

        textoEscuro =
            Color(0xFF454545),

        textoMuitoEscuro =
            Color(0xFF3B3B3B),

        icone =
            Color.White,

        fundoIcone =
            Color(0xFF0C0C0C),

        bordaIcone =
            Color(0xFF202020),

        azul =
            Color(0xFF5666FF),

        azulClaro =
            Color(0xFF6070FF),

        linhaFundo =
            Color(0xFF111111)
    )


private val CoresClaras =
    CoresAssistente(

        fundo =
            Color(0xFFF7F7F7),

        card =
            Color.White,

        cardSecundario =
            Color(0xFFF2F2F2),

        borda =
            Color(0xFFE2E2E2),

        bordaForte =
            Color(0xFFD7D7D7),

        textoPrincipal =
            Color(0xFF111111),

        textoSecundario =
            Color(0xFF777777),

        textoEscuro =
            Color(0xFF777777),

        textoMuitoEscuro =
            Color(0xFF999999),

        icone =
            Color.Black,

        fundoIcone =
            Color(0xFFF1F1F1),

        bordaIcone =
            Color(0xFFDCDCDC),

        azul =
            Color(0xFF5666FF),

        azulClaro =
            Color(0xFF6070FF),

        linhaFundo =
            Color(0xFFE8E8E8)
    )


private val LocalCoresAssistente =
    compositionLocalOf<CoresAssistente> {
        CoresEscuras
    }


/* =========================================================
   TEMA
========================================================= */

@Composable
private fun AssistenteTheme(
    content: @Composable () -> Unit
) {

    val modoEscuro =
        isSystemInDarkTheme()

    val cores =
        if (modoEscuro) {
            CoresEscuras
        } else {
            CoresClaras
        }

    CompositionLocalProvider(
        LocalCoresAssistente provides cores
    ) {
        content()
    }
}


@Composable
private fun coresAssistente(): CoresAssistente {
    return LocalCoresAssistente.current
}


/* =========================================================
   COR DOS ÍCONES
========================================================= */

@Composable
private fun corIcone(): Color {

    return if (
        isSystemInDarkTheme()
    ) {
        Color.White
    } else {
        Color.Black
    }
}


/* =========================================================
   FONTE
========================================================= */

private val Quicksand =
    FontFamily(
        Font(
            resId =
                com.mulheres.R.font.quicksand,

            weight =
                FontWeight.Bold
        )
    )


/* =========================================================
   DADOS
========================================================= */

data class Indicador(

    val valor: Float,

    val min: Float,

    val max: Float,

    val passo: Float
)


data class DadosSaude(

    val batimentos: Indicador =
        Indicador(
            72f,
            65f,
            82f,
            2f
        ),

    val respiracao: Indicador =
        Indicador(
            16f,
            12f,
            20f,
            .5f
        ),

    val temperatura: Indicador =
        Indicador(
            36.5f,
            36.1f,
            36.9f,
            .05f
        ),

    val hidratacao: Indicador =
        Indicador(
            68f,
            55f,
            82f,
            1f
        ),

    val bemEstar: Indicador =
        Indicador(
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
        (
            Random.nextFloat() * 2f - 1f
        ) * indicador.passo

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

    val cores =
        coresAssistente()
        
        val context = androidx.compose.ui.platform.LocalContext.current

    val estado = remember {
    DadosSaude()
}


    CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {

        LazyColumn(

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        cores.fundo
                    )
                    .padding(
    start = 15.dp,
    end = 15.dp,
    top = 25.dp
)
.windowInsetsPadding(
    WindowInsets.statusBars
)

.padding(
    bottom = 45.dp
),

            verticalArrangement =
                Arrangement.spacedBy(
                    0.dp
                )
        ) {
        
       

            item {

                Cabecalho(
    onSecretClick = {
        context.startActivity(
            Intent(
                context,
                NeoCalcActivity::class.java
            )
        )
    }
)

                Spacer(
                    modifier =
                        Modifier.height(
                            28.dp
                        )
                )
            }


            item {

                Hero(
                    bemEstar =
                        estado.bemEstar.valor
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )
            }


            item {

                Resumo(

                    batimentos =
                        estado.batimentos.valor,

                    respiracao =
                        estado.respiracao.valor,

                    temperatura =
                        estado.temperatura.valor
                )
            }


            item {
            
            Spacer(
        modifier = Modifier.height(36.dp)
    )

                SecaoTitulo(

                    titulo =
                        "Indicadores",

                    direita =
                        "Atualização contínua"
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                Indicadores(
                    dados =
                        estado
                )
            }


            item {

                Spacer(
                    modifier =
                        Modifier.height(
                            36.dp
                        )
                )

                SecaoTitulo(

                    titulo =
                        "Bem-estar",

                    direita =
                        "Índice geral"
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                BemEstar(
                    valor =
                        estado.bemEstar.valor
                )
            }


            item {

                Spacer(
                    modifier =
                        Modifier.height(
                            36.dp
                        )
                )

                SecaoTitulo(
                    titulo =
                        "Atividade"
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                Atividade()

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )
            }
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

    val cores =
        coresAssistente()


    Row(

        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {

            Text(

                text =
                    "Assistente de Saúde",

                color =
                    cores.textoPrincipal,

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
                    Modifier.height(
                        9.dp
                    )
            )


            Text(

                text =
                    "Um espaço para acompanhar seu bem-estar.",

                color =
                    cores.textoEscuro,

                fontFamily =
                    Quicksand,

                fontSize =
                    9.sp,

                fontWeight =
                    FontWeight.Bold,

                lineHeight =
                    14.sp,

                maxLines =
                    2
            )
        }


        Spacer(
            modifier =
                Modifier.width(
                    20.dp
                )
        )


        Card(

            onClick =
                onSecretClick,

            modifier =
                Modifier.size(
                    42.dp
                ),

            shape =
                RoundedCornerShape(
                    14.dp
                ),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        cores.fundoIcone
                ),

            border =
                BorderStroke(
                    1.dp,
                    cores.bordaIcone
                )
        ) {

            Box(

                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Image(

                    painter =
                        painterResource(
                            R.drawable.ic_8
                        ),

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(
                            19.dp
                        ),

                    colorFilter =
                        ColorFilter.tint(
                            corIcone()
                        )
                )
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

    val cores =
        coresAssistente()


    

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    315.dp
                ),

        shape =
            RoundedCornerShape(
                26.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    cores.card
            ),

        border =
            BorderStroke(
                1.dp,
                cores.bordaForte
            )
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

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
                        cores.textoEscuro,

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
                                    cores.azul.copy(
                                        alpha =
                                            opacity
                                    )
                                )
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                6.dp
                            )
                    )


                    Text(

                        text =
                            "ATIVO",

                        color =
                            cores.textoEscuro,

                        fontFamily =
                            Quicksand,

                        fontSize =
                            7.sp,

                        fontWeight =
                            FontWeight.Bold,

                        letterSpacing =
                            .8.sp
                    )
                }
            }


            Column(

                modifier =
                    Modifier
                        .align(
                            Alignment.Center
                        )
                        .padding(
                            top = 5.dp
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
                                if (
                                    isSystemInDarkTheme()
                                ) {
                                    Color(
                                        0xFF0B0D17
                                    )
                                } else {
                                    Color(
                                        0xFFF0F1F8
                                    )
                                }
                            )
                            .border(

                                1.dp,

                                if (
                                    isSystemInDarkTheme()
                                ) {
                                    Color(
                                        0xFF282B40
                                    )
                                } else {
                                    Color(
                                        0xFFD9DBE8
                                    )
                                },

                                RoundedCornerShape(
                                    20.dp
                                )
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Image(

                        painter =
                            painterResource(
                                R.drawable.assistente1
                            ),

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(
                                30.dp
                            ),

                        contentScale =
                            ContentScale.Fit,

                        colorFilter =
                            ColorFilter.tint(
                                corIcone()
                            )
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            17.dp
                        )
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

                        color =
                            cores.textoPrincipal,

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
                            Modifier.width(
                                4.dp
                            )
                    )


                    Text(

                        text =
                            "/ 100",

                        color =
                            cores.textoEscuro,

                        fontFamily =
                            Quicksand,

                        fontSize =
                            8.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


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
                        cores.textoPrincipal,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            6.dp
                        )
                )


                Text(

                    text =
                        "Acompanhamento em andamento",

                    color =
                        cores.textoEscuro,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        8.sp,

                    fontWeight =
                        FontWeight.Bold
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

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        ResumoItem(

            modifier =
                Modifier.weight(
                    1f
                ),

            label =
                "BATIMENTOS",

            value =
                batimentos
                    .roundToInt()
                    .toString(),

            unit =
                "bpm"
        )


        ResumoItem(

            modifier =
                Modifier.weight(
                    1f
                ),

            label =
                "RESPIRAÇÃO",

            value =
                "%.1f".format(
                    respiracao
                ),

            unit =
                "rpm"
        )


        ResumoItem(

            modifier =
                Modifier.weight(
                    1f
                ),

            label =
                "TEMPERATURA",

            value =
                "%.1f".format(
                    temperatura
                ),

            unit =
                "°C"
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

    val cores =
        coresAssistente()


    Card(

        modifier =
            modifier.height(
                79.dp
            ),

        shape =
            RoundedCornerShape(
                17.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    cores.card
            ),

        border =
            BorderStroke(
                1.dp,
                cores.borda
            )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        13.dp
                    ),

            verticalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(

                text =
                    label,

                color =
                    cores.textoEscuro,

                fontFamily =
                    Quicksand,

                fontSize =
                    7.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    .7.sp
            )


            Row(

                verticalAlignment =
                    Alignment.Bottom
            ) {

                Text(

                    text =
                        value,

                    color =
                        cores.textoPrincipal,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        17.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            4.dp
                        )
                )


                Text(

                    text =
                        unit,

                    color =
                        cores.textoEscuro,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        7.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


/* =========================================================
   TÍTULO
========================================================= */

@Composable
private fun SecaoTitulo(

    titulo: String,

    direita: String? = null
) {

    val cores =
        coresAssistente()


    Row(

        modifier =
            Modifier
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

            text =
                titulo,

            color =
                cores.textoPrincipal,

            fontFamily =
                Quicksand,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Bold,

            letterSpacing =
                (-.25).sp
        )


        if (direita != null) {

            Text(

                text =
                    direita,

                color =
                    cores.textoEscuro,

                fontFamily =
                    Quicksand,

                fontSize =
                    7.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    .7.sp
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

            IndicadorCard(

                modifier =
                    Modifier.weight(
                        1f
                    ),

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

                percentual =
                    percentual(
                        dados.batimentos
                    ),

                icone =
                    R.drawable.batimentos1
            )


            IndicadorCard(

                modifier =
                    Modifier.weight(
                        1f
                    ),

                numero =
                    "02",

                nome =
                    "Respiração",

                valor =
                    "%.1f".format(
                        dados.respiracao.valor
                    ),

                unidade =
                    "rpm",

                percentual =
                    percentual(
                        dados.respiracao
                    ),

                icone =
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

            IndicadorCard(

                modifier =
                    Modifier.weight(
                        1f
                    ),

                numero =
                    "03",

                nome =
                    "Temperatura",

                valor =
                    "%.1f".format(
                        dados.temperatura.valor
                    ),

                unidade =
                    "°C",

                percentual =
                    percentual(
                        dados.temperatura
                    ),

                icone =
                    R.drawable.temperatura1
            )


            IndicadorCard(

                modifier =
                    Modifier.weight(
                        1f
                    ),

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

                percentual =
                    percentual(
                        dados.hidratacao
                    ),

                icone =
                    R.drawable.hidratacao1
            )
        }
    }
}


private fun percentual(
    indicador: Indicador
): Float {

    return (

        (indicador.valor - indicador.min) /
            (
                indicador.max -
                    indicador.min
            )

        ).coerceIn(
            0f,
            1f
        )
}


/* =========================================================
   INDICADOR CARD
========================================================= */

@Composable
private fun IndicadorCard(

    modifier: Modifier,

    numero: String,

    nome: String,

    valor: String,

    unidade: String,

    percentual: Float,

    icone: Int
) {

    val cores =
        coresAssistente()


    Card(

        modifier =
            modifier.height(
                148.dp
            ),

        shape =
            RoundedCornerShape(
                20.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    cores.card
            ),

        border =
            BorderStroke(
                1.dp,
                cores.borda
            )
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            16.dp
                        )
            ) {

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
                                    cores.fundoIcone
                                )
                                .border(
                                    1.dp,
                                    cores.bordaIcone,
                                    RoundedCornerShape(
                                        12.dp
                                    )
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Image(

                            painter =
                                painterResource(
                                    icone
                                ),

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(
                                    21.dp
                                ),

                            contentScale =
                                ContentScale.Fit,

                            colorFilter =
                                ColorFilter.tint(
                                    corIcone()
                                )
                        )
                    }


                    Text(

                        text =
                            numero,

                        color =
                            cores.textoMuitoEscuro,

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
                        Modifier.height(
                            15.dp
                        )
                )


                Text(

                    text =
                        nome,

                    color =
                        cores.textoSecundario,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        9.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
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
                            cores.textoPrincipal,

                        fontFamily =
                            Quicksand,

                        fontSize =
                            22.sp,

                        fontWeight =
                            FontWeight.Bold,

                        letterSpacing =
                            (-.8).sp
                    )


                    Text(

                        text =
                            unidade,

                        color =
                            cores.textoEscuro,

                        fontFamily =
                            Quicksand,

                        fontSize =
                            8.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            /*
             * TRILHO DA BARRA:
             * totalmente transparente.
             */
            Box(

                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 10.dp
                        )
                        .height(
                            2.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                999.dp
                            )
                        )
                        .background(
                            Color.Transparent
                        )
            ) {

                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth(
                                percentual
                                    .coerceIn(
                                        .08f,
                                        1f
                                    )
                            )
                            .height(
                                2.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    999.dp
                                )
                            )
                            .background(
                                cores.azul.copy(
                                    alpha =
                                        .5f
                                )
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

    val cores =
        coresAssistente()


    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                22.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    cores.card
            ),

        border =
            BorderStroke(
                1.dp,
                cores.borda
            )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        20.dp
                    )
        ) {

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
                        Modifier.weight(
                            1f
                        ),

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
                                    cores.fundoIcone
                                )
                                .border(
                                    1.dp,
                                    cores.bordaIcone,
                                    RoundedCornerShape(
                                        13.dp
                                    )
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Image(

                            painter =
                                painterResource(
                                    R.drawable.bemestar1
                                ),

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(
                                    22.dp
                                ),

                            colorFilter =
                                ColorFilter.tint(
                                    corIcone()
                                )
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.width(
                                12.dp
                            )
                    )


                    Column {

                        Text(

                            text =
                                "Estado geral",

                            color =
                                cores.textoPrincipal,

                            fontFamily =
                                Quicksand,

                            fontSize =
                                10.sp,

                            fontWeight =
                                FontWeight.Bold
                        )


                        Spacer(
                            modifier =
                                Modifier.height(
                                    6.dp
                                )
                        )


                        Text(

                            text =
                                "Estimativa demonstrativa",

                            color =
                                cores.textoEscuro,

                            fontFamily =
                                Quicksand,

                            fontSize =
                                7.sp,

                            fontWeight =
                                FontWeight.Bold
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
                            cores.textoPrincipal,

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
                            Modifier.width(
                                3.dp
                            )
                    )


                    Text(

                        text =
                            "/100",

                        color =
                            cores.textoEscuro,

                        fontFamily =
                            Quicksand,

                        fontSize =
                            8.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        25.dp
                    )
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


            Spacer(
                modifier =
                    Modifier.height(
                        19.dp
                    )
            )


            /*
             * TRILHO DA BARRA:
             * totalmente transparente.
             */
            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            5.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                999.dp
                            )
                        )
                        .background(
                            Color.Transparent
                        )
            ) {

                val progresso =
                    animateFloatAsState(

                        targetValue =
                            (valor / 100f)
                                .coerceIn(
                                    0f,
                                    1f
                                ),

                        animationSpec =
                            tween(
                                800
                            ),

                        label =
                            "bem-estar"
                    )


                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth(
                                progresso.value
                            )
                            .height(
                                5.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    999.dp
                                )
                            )
                            .background(
                                cores.azulClaro
                            )
                )
            }
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

    val cores =
        coresAssistente()


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
                cores.textoMuitoEscuro,

            fontFamily =
                Quicksand,

            fontSize =
                7.sp,

            fontWeight =
                FontWeight.Bold
        )


        Text(

            text =
                valor,

            color =
                cores.textoSecundario,

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

    val cores =
        coresAssistente()


    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    65.dp
                ),

        shape =
            RoundedCornerShape(
                17.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    cores.cardSecundario
            ),

        border =
            BorderStroke(
                1.dp,
                cores.borda
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxSize()
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
                            cores.azul
                        )
            )


            Spacer(
                modifier =
                    Modifier.width(
                        13.dp
                    )
            )


            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(

                    text =
                        titulo,

                    color =
                        cores.textoSecundario,

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
                        Modifier.height(
                            5.dp
                        )
                )


                Text(

                    text =
                        descricao,

                    color =
                        cores.textoMuitoEscuro,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        7.sp,

                    fontWeight =
                        FontWeight.Bold,

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
                    Modifier.width(
                        13.dp
                    )
            )


            Text(

                text =
                    tempo,

                color =
                    cores.textoMuitoEscuro,

                fontFamily =
                    Quicksand,

                fontSize =
                    7.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}