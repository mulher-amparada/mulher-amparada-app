package com.mulheres

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

private val Quicksand = FontFamily(
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.SemiBold
    ),
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.Bold
    ),
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.ExtraBold
    )
)

private val Pink = Color(0xFFFF3F82)

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {

            MaterialTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {

                    CarteiraScreen(
                        onExigirClick = {

                            startActivity(
                                Intent(
                                    this,
                                    ExigirActivity::class.java
                                )
                            )

                        },

                        onGestoClick = {

                            startActivity(
                                Intent(
                                    this,
                                    GestoActivity::class.java
                                )
                            )

                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun CarteiraScreen(
    onExigirClick: () -> Unit,
    onGestoClick: () -> Unit
) {

    val dark = androidx.compose.foundation.isSystemInDarkTheme()

    val background = if (dark) {
        Color(0xFF000000)
    } else {
        Color(0xFFFFFFFF)
    }

    val text = if (dark) {
        Color(0xFFF5F5F7)
    } else {
        Color(0xFF18181C)
    }

    val soft = if (dark) {
        Color(0xFFB4B4BD)
    } else {
        Color(0xFF686870)
    }

    val muted = if (dark) {
        Color(0xFF9999A3)
    } else {
        Color(0xFF777780)
    }

    val cardBackground = if (dark) {
        Color(0xFF18181C)
    } else {
        Color.White
    }

    val border = if (dark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.Black.copy(alpha = 0.09f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 24.dp,
                bottom = 100.dp
            ),
        verticalArrangement = Arrangement.spacedBy(31.dp)
    ) {

        /*
         * =====================================================
         * HERO
         * =====================================================
         */

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 4.dp,
                    end = 4.dp
                )
        ) {

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
                text = "Carteira",

                color = text,

                fontFamily = Quicksand,

                fontSize = 44.sp,

                fontWeight = FontWeight.ExtraBold,

                lineHeight = 43.sp,

                letterSpacing = (-1.6).sp
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text =
                    "Suas carteirinhas reunidas em um só lugar.",

                modifier = Modifier
                    .fillMaxWidth()
                    .width(410.dp),

                color = soft,

                fontFamily = Quicksand,

                fontSize = 11.sp,

                fontWeight = FontWeight.SemiBold,

                lineHeight = 16.sp,

                letterSpacing = 0.15.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "Recurso informativo do aplicativo para apresentação " +
                    "à equipe de transporte. Estas carteirinhas não são " +
                    "um documento governamental nem são emitidas por " +
                    "órgãos públicos.",

                modifier = Modifier
                    .fillMaxWidth()
                    .width(410.dp),

                color = soft,

                fontFamily = Quicksand,

                fontSize = 11.sp,

                fontWeight = FontWeight.SemiBold,

                lineHeight = 16.sp,

                letterSpacing = 0.15.sp
            )
        }


        /*
         * =====================================================
         * PRIMEIRA SEÇÃO
         * =====================================================
         */

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            CarteiraLabel(
                text = "Exigindo seus direitos no transporte",
                color = muted
            )

            Spacer(
                modifier = Modifier.height(13.dp)
            )

            CarteiraCard(
                image = R.drawable.cartao,
                contentDescription = "Carteirinha sobre transporte",
                background = cardBackground,
                border = border,
                onClick = onExigirClick
            )
        }


        /*
         * =====================================================
         * SEGUNDA SEÇÃO
         * =====================================================
         */

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            CarteiraLabel(
                text = "Sobre o gesto de combate da violência",
                color = muted
            )

            Spacer(
                modifier = Modifier.height(13.dp)
            )

            CarteiraCard(
                image = R.drawable.cartao1,
                contentDescription = "Carteirinha sobre o gesto",
                background = cardBackground,
                border = border,
                onClick = onGestoClick
            )
        }
    }
}


@Composable
private fun CarteiraLabel(
    text: String,
    color: Color
) {

    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .width(5.dp)
                .height(5.dp)
                .clip(
                    RoundedCornerShape(50)
                )
                .background(Pink)
        )

        Spacer(
            modifier = Modifier.width(9.dp)
        )

        Text(
            text = text.uppercase(),

            color = color,

            fontFamily = Quicksand,

            fontSize = 10.sp,

            fontWeight = FontWeight.ExtraBold,

            letterSpacing = 2.sp,

            maxLines = 1,

            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun CarteiraCard(
    image: Int,
    contentDescription: String,
    background: Color,
    border: Color,
    onClick: () -> Unit
) {

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val pressed by interactionSource.collectIsPressedAsStateCompat()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        label = "cardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(
                RoundedCornerShape(27.dp)
            )
            .background(background)
            .border(
                width = 1.dp,
                color = border,
                shape = RoundedCornerShape(27.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {

        Image(
            painter = painterResource(image),

            contentDescription = contentDescription,

            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(27.dp)
                ),

            contentScale = ContentScale.FillWidth
        )
    }
}


/*
 * Compatibilidade para obter o estado de pressionamento.
 */
@Composable
private fun MutableInteractionSource.collectIsPressedAsStateCompat(): androidx.compose.runtime.State<Boolean> {
    return androidx.compose.foundation.interaction.collectIsPressedAsState()
}