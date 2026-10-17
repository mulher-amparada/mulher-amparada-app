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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class HabilidadesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DistintivosScreen()
        }
    }
}

data class Distintivo(
    val id: Int,
    val titulo: String,
    val descricao: String,
    val desbloqueado: Boolean
)

@Composable
fun DistintivosScreen() {

    var distintivoSelecionado by remember {
        mutableStateOf<Distintivo?>(null)
    }

    val distintivos = listOf(

        Distintivo(
            id = 1,
            titulo = "Primeiro passo",
            descricao = "Você instalou o Mulher Amparada.",
            desbloqueado = true
        ),

        Distintivo(
            id = 2,
            titulo = "Conhecendo o amparo",
            descricao = "Você começou a conhecer os recursos do aplicativo.",
            desbloqueado = true
        ),

        Distintivo(
            id = 3,
            titulo = "Proteção",
            descricao = "Conheça os recursos de proteção disponíveis.",
            desbloqueado = false
        ),

        Distintivo(
            id = 4,
            titulo = "Emergência",
            descricao = "Conheça os recursos de emergência.",
            desbloqueado = false
        ),

        Distintivo(
            id = 5,
            titulo = "Privacidade",
            descricao = "Aprenda sobre proteção e privacidade.",
            desbloqueado = false
        ),

        Distintivo(
            id = 6,
            titulo = "Conhecimento",
            descricao = "Aprenda sobre violência e seus ciclos.",
            desbloqueado = false
        ),

        Distintivo(
            id = 7,
            titulo = "Rede de apoio",
            descricao = "Conheça a importância de uma rede de apoio.",
            desbloqueado = false
        ),

        Distintivo(
            id = 8,
            titulo = "Autonomia",
            descricao = "Continue construindo conhecimento e autonomia.",
            desbloqueado = false
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
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Conquistas que você desbloqueia ao longo da sua jornada.",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 40.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                items(
                    items = distintivos,
                    key = {
                        it.id
                    }
                ) { distintivo ->

                    DistintivoCard(
                        distintivo = distintivo,
                        onClick = {
                            if (distintivo.desbloqueado) {
                                distintivoSelecionado = distintivo
                            }
                        }
                    )
                }
            }
        }

        distintivoSelecionado?.let { distintivo ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.78f)
                    ),
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

                    DistintivoImagem(
                        distintivo = distintivo,
                        tamanho = 100.dp
                    )

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = distintivo.titulo,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = distintivo.descricao,
                        color = Color.LightGray,
                        fontSize = 15.sp
                    )

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    Text(
                        text = "FECHAR",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            distintivoSelecionado = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DistintivoCard(
    distintivo: Distintivo,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = distintivo.desbloqueado,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        DistintivoImagem(
            distintivo = distintivo,
            tamanho = 125.dp
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = distintivo.titulo,
            color = if (distintivo.desbloqueado) {
                Color.White
            } else {
                Color.Gray
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = if (distintivo.desbloqueado) {
                "DESBLOQUEADO"
            } else {
                "BLOQUEADO"
            },
            color = if (distintivo.desbloqueado) {
                Color(0xFFBDBDBD)
            } else {
                Color(0xFF555555)
            },
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DistintivoImagem(
    distintivo: Distintivo,
    tamanho: Dp
) {

    Box(
        modifier = Modifier
            .size(tamanho)
            .rotate(45f)
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(
                if (distintivo.desbloqueado) {
                    Color(0xFF7C4DFF)
                } else {
                    Color(0xFF242424)
                }
            ),
        contentAlignment = Alignment.Center
    ) {

        if (distintivo.desbloqueado) {

            Image(
                painter = painterResource(
                    id = R.drawable.distintivo
                ),
                contentDescription = distintivo.titulo,
                modifier = Modifier
                    .size(tamanho * 0.70f)
                    .rotate(-45f),
                contentScale = ContentScale.Fit
            )

        } else {

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Distintivo bloqueado",
                tint = Color.Gray,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(-45f)
            )
        }
    }
}