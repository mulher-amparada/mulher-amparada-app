package com.mulheres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class HabilidadesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DistintivoScreen()
        }
    }
}

@Composable
fun DistintivoScreen() {

    var mostrarDetalhes by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Distintivos",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(34.dp)
            )

            /*
             * DISTINTIVO
             *
             * O losango é rotacionado.
             * A imagem ocupa TODO o espaço interno
             * e usa Crop para ser cortada nas bordas.
             */
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .rotate(45f)
                    .clip(
                        RoundedCornerShape(34.dp)
                    )
                    .clickable {
                        mostrarDetalhes = true
                    },
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(
                        id = R.drawable.distintivo
                    ),
                    contentDescription = "Distintivo Primeiro passo",
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(-45f),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(
                modifier = Modifier.height(34.dp)
            )

            Text(
                text = "Primeiro passo",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Você instalou o Mulher Amparada.",
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        if (mostrarDetalhes) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.82f)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .clip(
                            RoundedCornerShape(30.dp)
                        )
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF202020),
                                    Color(0xFF111111)
                                )
                            )
                        )
                        .padding(
                            horizontal = 26.dp,
                            vertical = 30.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    /*
                     * Miniatura do distintivo
                     */
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .rotate(45f)
                            .clip(
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Image(
                            painter = painterResource(
                                id = R.drawable.distintivo
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(-45f),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(28.dp)
                    )

                    Text(
                        text = "Distintivo desbloqueado",
                        color = Color(0xFFB8B8B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(7.dp)
                    )

                    Text(
                        text = "Primeiro passo",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "Você instalou o Mulher Amparada.",
                        color = Color(0xFFBDBDBD),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(
                        modifier = Modifier.height(28.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(16.dp)
                            )
                            .background(
                                Color.White.copy(alpha = 0.08f)
                            )
                            .clickable {
                                mostrarDetalhes = false
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "FECHAR",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}