package com.mulheres

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            MeusAcessos()
        }
    }
}


/* =========================================================
   FONTE
========================================================= */

private val Quicksand = FontFamily(
    Font(R.font.quicksand, FontWeight.Normal),
    Font(R.font.quicksand, FontWeight.Medium),
    Font(R.font.quicksand, FontWeight.SemiBold),
    Font(R.font.quicksand, FontWeight.Bold),
    Font(R.font.quicksand, FontWeight.ExtraBold)
)


/* =========================================================
   CORES
========================================================= */

private val Fundo = Color.Black

private val Texto = Color.White
private val TextoSuave = Color(0xFFB7B7C0)
private val TextoMuted = Color(0xFF777782)

private val Rosa = Color(0xFFFF3F82)

private val Vermelho = Color(0xFFFF5368)


/* =========================================================
   MODELO
========================================================= */

private data class Acesso(
    val titulo: String,
    val descricao: String,
    val icone: ImageVector,
    val corIcone: Color = Color(0xFF8B5CF6),
    val emergencia: Boolean = false
)


/* =========================================================
   TELA
========================================================= */

@Composable
private fun MeusAcessos() {

    val recursos = remember {

        listOf(

            Acesso(
                titulo = "Calendário menstrual",
                descricao = "Acompanhe seu ciclo",
                icone = Icons.Outlined.Favorite,
                corIcone = Color(0xFFE83E9F)
            ),

            Acesso(
                titulo = "Calendário de eventos",
                descricao = "Organize seus compromissos",
                icone = Icons.Outlined.CalendarMonth,
                corIcone = Color(0xFF6655E8)
            ),

            Acesso(
                titulo = "Mapa da sua região",
                descricao = "Visualize locais próximos",
                icone = Icons.Outlined.LocationOn,
                corIcone = Color(0xFF28B873)
            ),

            Acesso(
                titulo = "Diário e anotações",
                descricao = "Escreva e guarde seus registros",
                icone = Icons.Outlined.NoteAlt,
                corIcone = Color(0xFFD94BC4)
            ),

            Acesso(
                titulo = "Rotina gamificada",
                descricao = "Transforme tarefas em desafios",
                icone = Icons.Outlined.SportsEsports,
                corIcone = Color(0xFFFF9D35)
            ),

            Acesso(
                titulo = "Relógio + Localização",
                descricao = "Horário e posição atual",
                icone = Icons.Outlined.Timer,
                corIcone = Color(0xFF3978EE)
            ),

            Acesso(
                titulo = "Calculadora",
                descricao = "Faça seus cálculos rapidamente",
                icone = Icons.Outlined.Calculate,
                corIcone = Color(0xFF28B978)
            ),

            Acesso(
                titulo = "Minhas tarefas",
                descricao = "Organize o que precisa fazer",
                icone = Icons.Outlined.TaskAlt,
                corIcone = Color(0xFF22AFC5)
            ),

            Acesso(
                titulo = "Navegador",
                descricao = "Acesse páginas da internet",
                icone = Icons.Outlined.Language,
                corIcone = Color(0xFF7147F4)
            )
        )
    }


    val outrasFuncoes = remember {

        listOf(

            Acesso(
                titulo = "Gravador de voz",
                descricao = "Grave áudios rapidamente",
                icone = Icons.Outlined.Mic,
                corIcone = Color(0xFF7D4BD8)
            ),

            Acesso(
                titulo = "Meus arquivos",
                descricao = "Acesse seus arquivos",
                icone = Icons.Outlined.Folder,
                corIcone = Color(0xFF438AEF)
            ),

            Acesso(
                titulo = "Tela de aplicativos",
                descricao = "Acesse seus apps",
                icone = Icons.Outlined.Person,
                corIcone = Color(0xFFD72AD5)
            )
        )
    }


    val especiais = remember {

        listOf(

            Acesso(
                titulo = "Feliz Dia das Mulheres!",
                descricao = "Um jogo gamificado",
                icone = Icons.Outlined.Favorite,
                corIcone = Color(0xFF9B6BEA)
            ),

            Acesso(
                titulo = "100 dos seus direitos",
                descricao = "seja amparada pela lei",
                icone = Icons.Outlined.Description,
                corIcone = Color(0xFF9B6BEA)
            ),

            Acesso(
                titulo = "Dicas de como recuperar sua autonomia financeira",
                descricao = "Conhecimentos valiosos",
                icone = Icons.Outlined.Calculate,
                corIcone = Color(0xFF9B6BEA)
            ),

            Acesso(
                titulo = "Uma carta para você, mulher!",
                descricao = "Uma mensagem especial",
                icone = Icons.Outlined.Description,
                corIcone = Color(0xFF9B6BEA)
            )
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .windowInsetsPadding(
                WindowInsets.safeDrawing
            )
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),

            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 22.dp,
                bottom = 70.dp
            )
        ) {

            item {

                Hero()

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }


            item {

                TituloSecao(
                    "Recursos do aplicativo"
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )
            }


            items(recursos) { acesso ->

                Cartao(acesso)

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }


            item {

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                TituloSecao(
                    "Outras funções"
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )
            }


            items(outrasFuncoes) { acesso ->

                Cartao(acesso)

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }


            /* =================================================
               EMERGÊNCIA
               
               SOMENTE BLOQUEAR DISPOSITIVO.
               NÃO EXISTE DESLIGAR.
            ================================================= */

            item {

                Spacer(
                    modifier = Modifier.height(28.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Vermelho.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(
                    modifier = Modifier.height(27.dp)
                )

                TituloSecao(
                    "Emergência"
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Cartao(
                    acesso = Acesso(
                        titulo = "Bloquear dispositivo",
                        descricao = "Bloqueie a tela imediatamente",
                        icone = Icons.Outlined.Lock,
                        corIcone = Vermelho,
                        emergencia = true
                    )
                )

                Spacer(
                    modifier = Modifier.height(32.dp)
                )
            }


            item {

                TituloSecao(
                    "Especial, só para você!"
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )
            }


            items(especiais) { acesso ->

                Cartao(acesso)

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }
        }
    }
}


/* =========================================================
   HERO
========================================================= */

@Composable
private fun Hero() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .padding(
                top = 20.dp,
                bottom = 70.dp
            ),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        /*
         * ÍCONE DO HERO
         * Também é ImageVector.
         * Nenhum drawable.
         */

        Box(
            modifier = Modifier
                .size(104.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(30.dp)
                )
                .clip(
                    RoundedCornerShape(30.dp)
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFF6425D9)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(30.dp)
                ),

            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Outlined.Person,

                contentDescription = null,

                modifier = Modifier.size(55.dp),

                tint = Color.White
            )
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        Text(
            text = "Meus Acessos",

            color = Texto,

            fontFamily = Quicksand,

            fontSize = 38.sp,

            fontWeight = FontWeight.ExtraBold,

            letterSpacing = (-1.2).sp,

            lineHeight = 40.sp,

            textAlign = TextAlign.Center
        )


        Spacer(
            modifier = Modifier.height(15.dp)
        )


        Text(
            text = "Tudo o que você precisa, organizado em um só lugar.",

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),

            color = TextoSuave,

            fontFamily = Quicksand,

            fontSize = 15.sp,

            fontWeight = FontWeight.Medium,

            lineHeight = 23.sp,

            textAlign = TextAlign.Center
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        Text(
            text = "•  ÁREA PROTEGIDA",

            color = Color(0xFF91889F),

            fontFamily = Quicksand,

            fontSize = 10.sp,

            fontWeight = FontWeight.Bold,

            letterSpacing = 1.7.sp
        )
    }
}


/* =========================================================
   TÍTULO DA SEÇÃO
========================================================= */

@Composable
private fun TituloSecao(
    titulo: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(6.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = CircleShape
                )
                .background(
                    Rosa,
                    CircleShape
                )
        )

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Text(
            text = titulo.uppercase(),

            color = Color(0xFF8F8F99),

            fontFamily = Quicksand,

            fontSize = 10.sp,

            fontWeight = FontWeight.ExtraBold,

            letterSpacing = 1.8.sp
        )
    }
}


/* =========================================================
   CARTÃO
========================================================= */

@Composable
private fun Cartao(
    acesso: Acesso
) {

    val fundo = if (acesso.emergencia) {

        Brush.linearGradient(
            colors = listOf(
                Color(0x16FF5368),
                Color(0x06FF5368)
            )
        )

    } else {

        Brush.linearGradient(
            colors = listOf(
                Color(0x09FFFFFF),
                Color(0x03FFFFFF)
            )
        )
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                if (acesso.emergencia) {
                    88.dp
                } else {
                    82.dp
                }
            )
            .clip(
                RoundedCornerShape(21.dp)
            )
            .background(fundo)
            .border(
                width = 1.dp,

                color =
                    if (acesso.emergencia) {
                        Color(0x38FF5368)
                    } else {
                        Color.White.copy(alpha = 0.075f)
                    },

                shape = RoundedCornerShape(21.dp)
            )
            .clickable {
                /*
                 * Por enquanto não abre nada.
                 */
            }
            .padding(
                horizontal = 16.dp,
                vertical = 15.dp
            ),

        verticalAlignment = Alignment.CenterVertically
    ) {

        if (acesso.emergencia) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(3.dp)
                        .height(58.dp)
                        .clip(
                            RoundedCornerShape(
                                topEnd = 8.dp,
                                bottomEnd = 8.dp
                            )
                        )
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFF5368),
                                    Color(0xFFFF304D)
                                )
                            )
                        )
                )

                ConteudoCartao(
                    acesso = acesso,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                )
            }

        } else {

            ConteudoCartao(
                acesso = acesso,

                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


/* =========================================================
   CONTEÚDO DO CARTÃO
========================================================= */

@Composable
private fun ConteudoCartao(
    acesso: Acesso,
    modifier: Modifier
) {

    Row(
        modifier = modifier,

        verticalAlignment = Alignment.CenterVertically
    ) {

        /*
         * ÍCONE
         *
         * Não existe Image.
         * Não existe painterResource.
         * Não existe drawable.
         */

        Box(
            modifier = Modifier.size(50.dp),

            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                acesso.corIcone,
                                acesso.corIcone.copy(alpha = 0.65f)
                            )
                        )
                    ),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = acesso.icone,

                    contentDescription = null,

                    modifier = Modifier.size(29.dp),

                    tint = Color.White
                )
            }
        }


        Spacer(
            modifier = Modifier.width(15.dp)
        )


        /*
         * TEXTO
         */

        Column(
            modifier = Modifier.weight(1f),

            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = acesso.titulo,

                color =
                    if (acesso.emergencia) {
                        Color(0xFFFF6C7D)
                    } else {
                        Color(0xFFF8F8FA)
                    },

                fontFamily = Quicksand,

                fontSize = 15.sp,

                fontWeight = FontWeight.Bold,

                lineHeight = 19.sp
            )


            Spacer(
                modifier = Modifier.height(5.dp)
            )


            Text(
                text = acesso.descricao,

                color =
                    if (acesso.emergencia) {
                        Color(0xFFA7656E)
                    } else {
                        TextoMuted
                    },

                fontFamily = Quicksand,

                fontSize = 12.sp,

                fontWeight = FontWeight.Medium,

                lineHeight = 16.sp
            )
        }


        /*
         * SETA
         */

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (acesso.emergencia) {
                        Color(0x10FF5368)
                    } else {
                        Color.White.copy(alpha = 0.025f)
                    }
                ),

            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Outlined.ArrowForwardIos,

                contentDescription = null,

                modifier = Modifier.size(17.dp),

                tint =
                    if (acesso.emergencia) {
                        Color(0xFFB64B5B)
                    } else {
                        Color(0xFF666671)
                    }
            )
        }
    }
}