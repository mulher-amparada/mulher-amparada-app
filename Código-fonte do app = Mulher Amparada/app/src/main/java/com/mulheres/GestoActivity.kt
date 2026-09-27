package com.mulheres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import android.view.WindowManager

private val Quicksand = FontFamily(
    Font(R.font.quicksand, FontWeight.Normal),
    Font(R.font.quicksand, FontWeight.Medium),
    Font(R.font.quicksand, FontWeight.SemiBold),
    Font(R.font.quicksand, FontWeight.Bold),
    Font(R.font.quicksand, FontWeight.ExtraBold)
)

private val Pink = Color(0xFFFF3F82)
private val PinkSoft = Color(0xFFFF6B9F)

class GestoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)

window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )
    
        window.statusBarColor = android.graphics.Color.BLACK
window.navigationBarColor = android.graphics.Color.BLACK

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    GestoScreen()
                }
            }
        }
    }
}

@Composable
private fun GestoScreen() {

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
        if (dark) Color(0xFFFFFFFF)
        else Color(0xFF18181C)

    val soft =
        if (dark) Color(0xFF92949E)
        else Color(0xFF686870)

    val muted =
        if (dark) Color(0xFF686D79)
        else Color(0xFF777780)

    val bodyText =
        if (dark) Color(0xFFB4B5BD)
        else Color(0xFF55565E)

    val border =
        if (dark) {
            Color.White.copy(alpha = 0.07f)
        } else {
            Color.Black.copy(alpha = 0.09f)
        }

    val stepBackground =
        if (dark) {
            Color.White.copy(alpha = 0.02f)
        } else {
            Color.Black.copy(alpha = 0.025f)
        }

    val noticeBackground =
        if (dark) {
            Color(0xFF0C0D11)
        } else {
            Color(0xFFF0F0F2)
        }

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
         * =====================================================
         * CABEÇALHO
         * =====================================================
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
            text = "Gesto de ajuda",

            color = text,

            fontFamily = Quicksand,

            fontSize = 38.sp,

            fontWeight = FontWeight.ExtraBold,

            lineHeight = 37.sp,

            letterSpacing = (-1.5).sp
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Text(
            text =
                "Aprenda a reconhecer e utilizar o gesto da mão " +
                "criado para sinalizar silenciosamente que você " +
                "precisa de ajuda em uma situação de violência.",

            modifier = Modifier.fillMaxWidth(),

            color = soft,

            fontFamily = Quicksand,

            fontSize = 12.sp,

            fontWeight = FontWeight.SemiBold,

            lineHeight = 18.6.sp
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )


        /*
         * =====================================================
         * CARD PRINCIPAL
         * =====================================================
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
                text = "SINAL INTERNACIONAL DE AJUDA",

                color = PinkSoft,

                fontFamily = Quicksand,

                fontSize = 9.sp,

                fontWeight = FontWeight.ExtraBold,

                letterSpacing = 2.sp
            )

            Spacer(
                modifier = Modifier.height(13.dp)
            )


            /*
             * TÍTULO
             */

            Text(
                text = "O gesto da mão",

                color = text,

                fontFamily = Quicksand,

                fontSize = 30.sp,

                fontWeight = FontWeight.ExtraBold,

                lineHeight = 31.5.sp
            )

            Spacer(
                modifier = Modifier.height(13.dp)
            )


            /*
             * DESCRIÇÃO
             */

            Text(
                text =
                    "O Signal for Help é um gesto simples feito com " +
                    "uma mão. Ele foi criado para permitir que uma " +
                    "pessoa peça ajuda de forma discreta quando não " +
                    "consegue falar livremente.",

                color = bodyText,

                fontFamily = Quicksand,

                fontSize = 12.sp,

                fontWeight = FontWeight.SemiBold,

                lineHeight = 19.8.sp
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )


            /*
             * =================================================
             * GESTO
             * =================================================
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
                    text = "APRENDA O GESTO",

                    color = PinkSoft,

                    fontFamily = Quicksand,

                    fontSize = 9.sp,

                    fontWeight = FontWeight.ExtraBold,

                    letterSpacing = 1.7.sp
                )

                Spacer(
                    modifier = Modifier.height(17.dp)
                )

                Text(
                    text = "Palma aberta, polegar dobrado",

                    modifier = Modifier.fillMaxWidth(),

                    color = text,

                    fontFamily = Quicksand,

                    fontSize = 21.sp,

                    fontWeight = FontWeight.ExtraBold,

                    lineHeight = 26.25.sp,

                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(9.dp)
                )

                Text(
                    text =
                        "O polegar é colocado sobre a palma e os " +
                        "outros dedos são fechados sobre ele.",

                    modifier = Modifier.fillMaxWidth(),

                    color = soft,

                    fontFamily = Quicksand,

                    fontSize = 10.sp,

                    fontWeight = FontWeight.SemiBold,

                    lineHeight = 16.sp,

                    textAlign = TextAlign.Center
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )


            /*
             * =================================================
             * PASSO A PASSO
             * =================================================
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
                    text = "Como fazer",

                    color = text,

                    fontFamily = Quicksand,

                    fontSize = 15.sp,

                    fontWeight = FontWeight.ExtraBold
                )

                GestureStep(
                    number = "PASSO 01",
                    title = "Mostre a palma da mão",
                    description =
                        "Mantenha a mão aberta e voltada para " +
                        "a pessoa que você deseja sinalizar.",
                    textColor = text,
                    mutedColor = muted,
                    background = stepBackground,
                    border = border
                )

                GestureStep(
                    number = "PASSO 02",
                    title = "Dobre o polegar",
                    description =
                        "Coloque o polegar para dentro da palma " +
                        "da mão.",
                    textColor = text,
                    mutedColor = muted,
                    background = stepBackground,
                    border = border
                )

                GestureStep(
                    number = "PASSO 03",
                    title = "Feche os dedos",
                    description =
                        "Feche os quatro dedos sobre o polegar, " +
                        "formando o sinal.",
                    textColor = text,
                    mutedColor = muted,
                    background = stepBackground,
                    border = border
                )

                GestureStep(
                    number = "PASSO 04",
                    title = "Repita",
                    description =
                        "Se necessário, volte ao primeiro passo " +
                        "e repita o gesto quando for seguro.",
                    textColor = text,
                    mutedColor = muted,
                    background = stepBackground,
                    border = border
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )


            /*
             * =================================================
             * EXPLICAÇÃO
             * =================================================
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
                    text = "Para que serve?",

                    color = text,

                    fontFamily = Quicksand,

                    fontSize = 15.sp,

                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(
                    modifier = Modifier.height(11.dp)
                )

                Text(
                    text =
                        "O gesto pode ser usado quando uma pessoa " +
                        "precisa comunicar discretamente que está " +
                        "em uma situação de violência ou que precisa " +
                        "de ajuda.",

                    color = bodyText,

                    fontFamily = Quicksand,

                    fontSize = 11.sp,

                    fontWeight = FontWeight.SemiBold,

                    lineHeight = 18.15.sp
                )

                Spacer(
                    modifier = Modifier.height(11.dp)
                )

                Text(
                    text =
                        "Ele não substitui uma conversa segura nem " +
                        "garante uma resposta imediata. Se alguém " +
                        "perceber o sinal, é importante procurar uma " +
                        "forma segura de oferecer ajuda ou acionar " +
                        "os serviços apropriados.",

                    color = bodyText,

                    fontFamily = Quicksand,

                    fontSize = 11.sp,

                    fontWeight = FontWeight.SemiBold,

                    lineHeight = 18.15.sp
                )
            }

            Spacer(
                modifier = Modifier.height(17.dp)
            )


            /*
             * =================================================
             * AVISO
             * =================================================
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

                    fontFamily = Quicksand,

                    fontSize = 11.sp,

                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(
                    modifier = Modifier.height(7.dp)
                )

                Text(
                    text =
                        "O gesto deve ser utilizado somente quando " +
                        "for seguro fazê-lo. Se a pessoa que está " +
                        "cometendo a violência puder perceber o " +
                        "sinal, priorize a segurança e procure ajuda " +
                        "de uma maneira que não aumente o risco.",

                    color = soft,

                    fontFamily = Quicksand,

                    fontSize = 10.sp,

                    fontWeight = FontWeight.SemiBold,

                    lineHeight = 16.sp
                )
            }
        }
    }
    }
}

@Composable
private fun GestureStep(
    number: String,
    title: String,
    description: String,
    textColor: Color,
    mutedColor: Color,
    background: Color,
    border: Color
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(background)
            .padding(15.dp)
    ) {

        Text(
            text = number,

            color = PinkSoft,

            fontFamily = Quicksand,

            fontSize = 9.sp,

            fontWeight = FontWeight.ExtraBold,

            letterSpacing = 1.5.sp
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = title,

            color = textColor,

            fontFamily = Quicksand,

            fontSize = 11.sp,

            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = description,

            color = mutedColor,

            fontFamily = Quicksand,

            fontSize = 9.sp,

            fontWeight = FontWeight.SemiBold,

            lineHeight = 13.5.sp
        )
    }
}