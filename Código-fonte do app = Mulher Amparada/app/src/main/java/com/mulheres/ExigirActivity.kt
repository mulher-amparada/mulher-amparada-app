package com.mulheres

import android.os.Bundle
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.CompositionLocalProvider
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import android.view.WindowManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars


private val Pink = Color(0xFFFF3F82)
private val PinkSoft = Color(0xFFFF6B9F)

class ExigirActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)

window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )
    
    window.statusBarColor = android.graphics.Color.BLACK
window.navigationBarColor = android.graphics.Color.BLACK
        
        WindowCompat.setDecorFitsSystemWindows(
    window,
    true
)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    ExigirScreen()
                }
            }
        }
    }
}

@Composable
private fun ExigirScreen() {

    val dark =
        androidx.compose.foundation.isSystemInDarkTheme()

    val background =
        if (dark) Color(0xFF000000)
        else Color(0xFFFFFFFF)

    val surface =
        if (dark) Color(0xFF101116)
        else Color(0xFFF5F5F7)

    val surface2 =
        if (dark) Color(0xFF171820)
        else Color(0xFFFFFFFF)

    val text =
        if (dark) Color.White
        else Color(0xFF18181C)

    val soft =
        if (dark) Color(0xFF92949E)
        else Color(0xFF686870)

    val muted =
        if (dark) Color(0xFF686D79)
        else Color(0xFF777780)

    val cardText =
        if (dark) Color(0xFFB4B5BD)
        else Color(0xFF55565E)

    val border =
        if (dark) Color.White.copy(alpha = 0.07f)
        else Color.Black.copy(alpha = 0.09f)

    val conditionsBackground =
        if (dark) Color.White.copy(alpha = 0.02f)
        else Color.Black.copy(alpha = 0.025f)

    val noticeBackground =
        if (dark) Color(0xFF0C0D11)
        else Color(0xFFF0F0F2)

    CompositionLocalProvider(
    LocalOverscrollFactory provides null
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 25.dp,
                bottom = 70.dp
            )
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
    ) {



        /*
         * ================================================
         * CABEÇALHO
         * ================================================
         */

        Box(
            modifier = Modifier
                .width(43.dp)
                .height(3.dp)
                .clip(
                    RoundedCornerShape(999.dp)
                )
                .background(Pink)
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text = "Desembarque seguro",

            color = text,


            fontSize = 38.sp,
lineHeight = 37.sp,

            letterSpacing = (-1.5).sp
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Text(
            text =
                "Uma forma simples de informar ao motorista que você " +
                "deseja desembarcar em um local mais seguro, conforme " +
                "as regras aplicáveis ao transporte coletivo urbano da " +
                "Cidade de São Paulo.",

            modifier = Modifier.fillMaxWidth(),

            color = soft,


            fontSize = 12.sp,
lineHeight = 18.6.sp
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )


        /*
         * ================================================
         * CARTEIRINHA
         * ================================================
         */

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(30.dp)
                )
                .background(surface)
                .padding(27.dp)
        ) {

            /*
             * LABEL
             */

            Text(
                text = "DIREITO DA PASSAGEIRA",

                color = PinkSoft,


                fontSize = 9.sp,
letterSpacing = 2.sp
            )

            Spacer(
                modifier = Modifier.height(13.dp)
            )


            /*
             * TÍTULO
             */

            Text(
                text = "Solicitação de desembarque",

                color = text,


                fontSize = 30.sp,
lineHeight = 31.5.sp,

                letterSpacing = (-0.5).sp
            )

            Spacer(
                modifier = Modifier.height(13.dp)
            )


            /*
             * TEXTO
             */

            Text(
                text =
                    "Apresente esta tela ao motorista quando quiser " +
                    "solicitar o desembarque em um local mais seguro, " +
                    "dentro das condições previstas pela legislação " +
                    "municipal.",

                color = cardText,


                fontSize = 12.sp,
lineHeight = 19.8.sp
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )


            /*
             * ============================================
             * FRASE PARA MOSTRAR
             * ============================================
             */

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(22.dp)
                    )
                    .background(
                        Pink.copy(alpha = 0.045f)
                    )
                    .padding(21.dp)
            ) {

                Text(
                    text = "PARA MOSTRAR AO MOTORISTA",

                    color = PinkSoft,


                    fontSize = 9.sp,
letterSpacing = 1.7.sp
                )

                Spacer(
                    modifier = Modifier.height(11.dp)
                )

                Text(
                    text =
                        "“Por favor, gostaria de desembarcar " +
                        "em um local mais seguro.”",

                    color = text,


                    fontSize = 21.sp,
lineHeight = 26.25.sp
                )


                /*
                 * HORÁRIO
                 */

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(17.dp)
                        )
                        .background(
                            Color.White.copy(
                                alpha =
                                    if (dark) 0.03f
                                    else 0.06f
                            )
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 15.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .background(Pink),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        ClockIcon()
                    }

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column {

                        Text(
                            text = "Das 22h às 5h",

                            color = text,


                            fontSize = 13.sp,
)

                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )

                        Text(
                            text =
                                "Horário previsto pela legislação municipal.",

                            color = soft,


                            fontSize = 10.sp,
lineHeight = 14.sp
                        )
                    }
                }
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            /*
             * ============================================
             * FUNDAMENTAÇÃO LEGAL
             * ============================================
             */

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(22.dp)
                    )
                    .background(surface2)
                    .padding(21.dp)
            ) {

                Text(
                    text = "Fundamentação legal",

                    color = text,


                    fontSize = 15.sp,
)

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text =
                        "LEI MUNICIPAL Nº 16.490/2016",

                    color = PinkSoft,


                    fontSize = 10.sp,
)

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LawParagraph(
                    text =
                        "A Lei Municipal nº 16.490/2016, da Cidade de " +
                        "São Paulo, permite que mulheres e idosos usuários " +
                        "do transporte coletivo urbano optem por um local " +
                        "mais seguro e acessível para desembarque entre " +
                        "22h e 5h, desde que o local esteja no trajeto " +
                        "regular da linha e não seja proibida a parada " +
                        "de veículos.",
                    color = cardText
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LawParagraph(
                    text =
                        "O Decreto nº 57.399/2016 regulamenta a lei e " +
                        "estabelece as condições para o desembarque fora " +
                        "dos pontos preestabelecidos. A parada deve ocorrer " +
                        "em local adequado, com espaço suficiente e " +
                        "observando as regras de segurança de trânsito.",
                    color = cardText
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LawParagraph(
                    text =
                        "O desembarque fora do ponto não é permitido " +
                        "em determinadas situações, incluindo viadutos, " +
                        "pontes, túneis e trechos especificamente previstos " +
                        "na regulamentação.",
                    color = cardText
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LawParagraph(
                    text =
                        "Esta tela é informativa e não substitui a " +
                        "legislação oficial nem garante que qualquer local " +
                        "solicitado possa ser utilizado para desembarque.",
                    color = cardText
                )


                /*
                 * ========================================
                 * CONDIÇÕES
                 * ========================================
                 */

                Spacer(
                    modifier = Modifier.height(17.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(9.dp)
                ) {

                    ConditionCard(
                        modifier = Modifier.weight(1f),
                        title = "22h — 5h",
                        description =
                            "Período do desembarque noturno.",
                        textColor = text,
                        mutedColor = muted,
                        background = conditionsBackground,
                        border = border
                    )

                    ConditionCard(
                        modifier = Modifier.weight(1f),
                        title = "Local seguro",
                        description =
                            "A parada deve ser adequada e segura.",
                        textColor = text,
                        mutedColor = muted,
                        background = conditionsBackground,
                        border = border
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(17.dp)
            )


            /*
             * ============================================
             * AVISO
             * ============================================
             */

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(20.dp)
                    )
                    .background(noticeBackground)
                    .padding(18.dp)
            ) {

                Text(
                    text = "Importante",

                    color = text,


                    fontSize = 11.sp,
)

                Spacer(
                    modifier = Modifier.height(7.dp)
                )

                Text(
                    text =
                        "A solicitação deve ser feita com antecedência. " +
                        "O motorista deve avaliar se a parada pode ser " +
                        "realizada com segurança e dentro das regras de " +
                        "trânsito. Há locais onde o desembarque fora do " +
                        "ponto não é permitido, como viadutos, pontes, " +
                        "túneis e determinados corredores exclusivos.",

                    color = soft,


                    fontSize = 10.sp,
lineHeight = 16.sp
                )
            }
        }
    }
}
}

@Composable
private fun LawParagraph(
    text: String,
    color: Color
) {

    Text(
        text = text,

        color = color,


        fontSize = 11.sp,
lineHeight = 18.15.sp
    )
}


@Composable
private fun ConditionCard(
    modifier: Modifier,
    title: String,
    description: String,
    textColor: Color,
    mutedColor: Color,
    background: Color,
    border: Color
) {

    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(15.dp)
            )
            .background(background)
            .padding(14.dp)
    ) {

        Text(
            text = title,

            color = textColor,


            fontSize = 10.sp,
)

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = description,

            color = mutedColor,


            fontSize = 9.sp,
lineHeight = 12.6.sp
        )
    }
}


@Composable
private fun ClockIcon() {

    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(19.dp)
    ) {

        val strokeWidth = 2.dp.toPx()

        drawCircle(
            color = Color.White,
            radius = size.minDimension / 2 -
                strokeWidth / 2,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth
            )
        )

        drawLine(
            color = Color.White,
            start = androidx.compose.ui.geometry.Offset(
                x = size.width / 2,
                y = size.height * 0.25f
            ),
            end = androidx.compose.ui.geometry.Offset(
                x = size.width / 2,
                y = size.height / 2
            ),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        drawLine(
            color = Color.White,
            start = androidx.compose.ui.geometry.Offset(
                x = size.width / 2,
                y = size.height / 2
            ),
            end = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.68f,
                y = size.height * 0.62f
            ),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}