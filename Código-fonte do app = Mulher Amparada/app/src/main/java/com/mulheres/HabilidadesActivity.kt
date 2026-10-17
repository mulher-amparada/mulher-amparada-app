package com.mulheres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class HabilidadesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ArvoreDeHabilidades()
        }
    }
}

data class Habilidade(
    val id: Int,
    val titulo: String,
    val descricao: String,
    val desbloqueada: Boolean,
    val icone: String
)

@Composable
fun ArvoreDeHabilidades() {

    var habilidadeSelecionada by remember {
        mutableStateOf<Habilidade?>(null)
    }

    val habilidades = listOf(

        Habilidade(
            id = 1,
            titulo = "Primeiro passo",
            descricao = "Você instalou o Mulher Amparada.",
            desbloqueada = true,
            icone = "🌱"
        ),

        Habilidade(
            id = 2,
            titulo = "Conhecer o amparo",
            descricao = "Conheça os recursos disponíveis no aplicativo.",
            desbloqueada = true,
            icone = "❤️"
        ),

        Habilidade(
            id = 3,
            titulo = "Proteção",
            descricao = "Conheça os recursos de proteção disponíveis.",
            desbloqueada = false,
            icone = "🛡️"
        ),

        Habilidade(
            id = 4,
            titulo = "Emergência",
            descricao = "Conheça os recursos para situações de emergência.",
            desbloqueada = false,
            icone = "🚨"
        ),

        Habilidade(
            id = 5,
            titulo = "Privacidade",
            descricao = "Aprenda sobre proteção e privacidade.",
            desbloqueada = false,
            icone = "🔐"
        ),

        Habilidade(
            id = 6,
            titulo = "Conhecimento",
            descricao = "Aprenda sobre violência e seus ciclos.",
            desbloqueada = false,
            icone = "📚"
        ),

        Habilidade(
            id = 7,
            titulo = "Rede de apoio",
            descricao = "Conheça a importância de uma rede de apoio.",
            desbloqueada = false,
            icone = "🤝"
        ),

        Habilidade(
            id = 8,
            titulo = "Autonomia",
            descricao = "Continue construindo conhecimento e autonomia.",
            desbloqueada = false,
            icone = "✨"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "🌳 Árvore de habilidades",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cada habilidade representa um passo no conhecimento e na preparação.",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(35.dp))

            HabilidadeNode(
                habilidade = habilidades[0],
                onClick = {
                    habilidadeSelecionada = habilidades[0]
                }
            )

            LinhaVertical()

            HabilidadeNode(
                habilidade = habilidades[1],
                onClick = {
                    habilidadeSelecionada = habilidades[1]
                }
            )

            LinhaVertical()

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                HabilidadeNode(
                    habilidade = habilidades[2],
                    onClick = {
                        habilidadeSelecionada = habilidades[2]
                    }
                )

                HabilidadeNode(
                    habilidade = habilidades[6],
                    onClick = {
                        habilidadeSelecionada = habilidades[6]
                    }
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                HabilidadeNode(
                    habilidade = habilidades[3],
                    onClick = {
                        habilidadeSelecionada = habilidades[3]
                    }
                )

                HabilidadeNode(
                    habilidade = habilidades[4],
                    onClick = {
                        habilidadeSelecionada = habilidades[4]
                    }
                )

                HabilidadeNode(
                    habilidade = habilidades[5],
                    onClick = {
                        habilidadeSelecionada = habilidades[5]
                    }
                )
            }

            LinhaVertical()

            HabilidadeNode(
                habilidade = habilidades[7],
                onClick = {
                    habilidadeSelecionada = habilidades[7]
                }
            )

            Spacer(modifier = Modifier.height(50.dp))
        }

        habilidadeSelecionada?.let { habilidade ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .background(
                            Color(0xFF151515),
                            MaterialTheme.shapes.large
                        )
                        .padding(25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = habilidade.icone,
                        fontSize = 48.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = habilidade.titulo,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = habilidade.descricao,
                        color = Color.LightGray,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "FECHAR",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            habilidadeSelecionada = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HabilidadeNode(
    habilidade: Habilidade,
    onClick: () -> Unit
) {

    val ativo = habilidade.desbloqueada

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(105.dp)
            .clickable(enabled = ativo) {
                onClick()
            }
    ) {

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    if (ativo) {
                        Color(0xFF7C4DFF)
                    } else {
                        Color(0xFF242424)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {

            if (ativo) {

                Text(
                    text = habilidade.icone,
                    fontSize = 30.sp
                )

            } else {

                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Bloqueada",
                    tint = Color.Gray,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = habilidade.titulo,
            color = if (ativo) Color.White else Color.Gray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LinhaVertical() {

    Canvas(
        modifier = Modifier
            .height(35.dp)
            .width(2.dp)
    ) {

        drawLine(
            color = Color(0xFF555555),
            start = Offset(
                size.width / 2,
                0f
            ),
            end = Offset(
                size.width / 2,
                size.height
            ),
            strokeWidth = 5f
        )
    }
}