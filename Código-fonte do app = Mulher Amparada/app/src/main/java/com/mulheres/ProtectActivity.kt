package com.mulheres

import android.graphics.Color as AndroidColor
import androidx.core.view.WindowCompat
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class ProtectActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        window.statusBarColor =
            AndroidColor.TRANSPARENT

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = false

        setContent {

            androidx.compose.runtime.CompositionLocalProvider(
                LocalOverscrollFactory provides null
            ) {

                MeusAcessos(

                    abrirNavegador = {
                        startActivity(
                            Intent(
                                this,
                                MainActivity::class.java
                            )
                        )
                    },

                    abrirMapa = {
                        startActivity(
                            Intent(
                                this,
                                MapaActivity::class.java
                            )
                        )
                    },

                    abrirDiario = {
                        startActivity(
                            Intent(
                                this,
                                DiarioActivity::class.java
                            )
                        )
                    },

                    abrirCalculadora = {
                        startActivity(
                            Intent(
                                this,
                                CalcActivity::class.java
                            )
                        )
                    },

                    abrirRelogio = {
                        startActivity(
                            Intent(
                                this,
                                RelogioActivity::class.java
                            )
                        )
                    },

                    abrirGravador = {
                        startActivity(
                            Intent(
                                this,
                                GravarActivity::class.java
                            )
                        )
                    },

                    abrirArquivos = {
                        startActivity(
                            Intent(
                                this,
                                FileActivity::class.java
                            )
                        )
                    },

                    abrirApps = {
                        startActivity(
                            Intent(
                                this,
                                AppsActivity::class.java
                            )
                        )
                    }
                )
            }
        }
    }
}


/* =========================================================
   FONTE
========================================================= */

private val Quicksand = FontFamily(

    Font(
        R.font.quicksand,
        FontWeight.Normal
    ),

    Font(
        R.font.quicksand,
        FontWeight.Medium
    ),

    Font(
        R.font.quicksand,
        FontWeight.SemiBold
    ),

    Font(
        R.font.quicksand,
        FontWeight.Bold
    ),

    Font(
        R.font.quicksand,
        FontWeight.ExtraBold
    )
)

/* =========================================================
   CORES — ESCURO
========================================================= */

private val FundoEscuro =
    Color(0xFF000000)

private val TextoEscuro =
    Color(0xFFF8F8FA)

private val TextoSuaveEscuro =
    Color(0xFFB7B7C0)

private val TextoMutedEscuro =
    Color(0xFF777782)

private val SecaoEscuro =
    Color(0xFF8F8F99)

private val CartaoEscuro =
    Color(0xFF000000)

private val BordaCartaoEscuro =
    Color.White.copy(
        alpha = 0.06f
    )

private val CirculoEscuro =
    Color.White.copy(
        alpha = 0.025f
    )

private val SetaEscuro =
    Color(0xFF666671)


/* =========================================================
   CORES — CLARO
========================================================= */

private val FundoClaro =
    Color(0xFFF7F7FA)

private val TextoClaro =
    Color(0xFF17171B)

private val TextoSuaveClaro =
    Color(0xFF666671)

private val TextoMutedClaro =
    Color(0xFF85858F)

private val SecaoClaro =
    Color(0xFF74747E)

private val CartaoClaro =
    Color(0xFFFFFFFF)

private val BordaCartaoClaro =
    Color(0x14000000)

private val CirculoClaro =
    Color(0x08000000)

private val SetaClaro =
    Color(0xFF8A8A94)


/* =========================================================
   CORES GERAIS
========================================================= */

private val Rosa =
    Color(0xFFFF3F82)

private val RosaClaro =
    Color(0xFFFF3F82)

private val RoxoInicio =
    Color(0xFF8B5CF6)

private val RoxoFim =
    Color(0xFF6425D9)
    
/* =========================================================
   MODELO
========================================================= */

private data class Acesso(
    val titulo: String,
    val descricao: String
)


/* =========================================================
   TELA
========================================================= */

@androidx.compose.runtime.Composable
private fun MeusAcessos(

    abrirNavegador: () -> Unit,

    abrirMapa: () -> Unit,

    abrirDiario: () -> Unit,

    abrirGravador: () -> Unit,

    abrirArquivos: () -> Unit,

    abrirCalculadora: () -> Unit,

    abrirRelogio: () -> Unit,

    abrirApps: () -> Unit

) {
    val dark =
        isSystemInDarkTheme()


    val fundo =
        if (dark)
            FundoEscuro
        else
            FundoClaro


    val recursos = listOf(

        Acesso(
            "Calendário menstrual",
            "Acompanhe seu ciclo"
        ),

        Acesso(
            "Calendário de eventos",
            "Organize seus compromissos"
        ),

        Acesso(
            "Mapa da sua região",
            "Visualize locais próximos"
        ),

        Acesso(
            "Diário e anotações",
            "Escreva e guarde seus registros"
        ),

        Acesso(
            "Rotina gamificada",
            "Transforme tarefas em desafios"
        ),

        Acesso(
            "Relógio + Localização",
            "Horário e posição atual"
        ),

        Acesso(
            "Calculadora",
            "Faça seus cálculos rapidamente"
        ),

        Acesso(
            "Minhas tarefas",
            "Organize o que precisa fazer"
        ),

        Acesso(
            "Navegador",
            "Acesse páginas da internet"
        )
    )


    val outrasFuncoes = listOf(

        Acesso(
            "Gravador de voz",
            "Grave áudios rapidamente"
        ),

        Acesso(
            "Meus arquivos",
            "Acesse seus arquivos"
        ),

        Acesso(
            "Tela de aplicativos",
            "Acesse seus apps"
        )
    )


    val especiais = listOf(

        Acesso(
            "Feliz Dia das Mulheres!",
            "Um jogo gamificado"
        ),

        Acesso(
            "100 dos seus direitos",
            "Seja amparada pela lei"
        ),

        Acesso(
            "Dicas de como recuperar sua autonomia financeira",
            "Conhecimentos valiosos"
        ),

        Acesso(
            "Uma carta para você, mulher!",
            "Uma mensagem especial"
        )
    )


    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    fundo
                )
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                )
    ) {

        LazyColumn(

            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 22.dp,
                    bottom = 70.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            /* =================================================
               HERO
            ================================================= */

            item {

                Hero(
                    dark = dark
                )
            }


            /* =================================================
               RECURSOS
            ================================================= */

            item {

                TituloSecao(
                    titulo = "Recursos",
                    dark = dark
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )
            }


            items(

                items = recursos,

                key = {
                    it.titulo
                }

            ) { acesso ->

                Cartao(

                    acesso = acesso,

                    dark = dark,

                    onClick = {

                        when (acesso.titulo) {

                            "Mapa da sua região" -> {
                                abrirMapa()
                            }
                            
                            "Diário e anotações" -> {
    abrirDiario()
}

                            "Relógio + Localização" -> {
                                abrirRelogio()
                            }

                            "Calculadora" -> {
                                abrirCalculadora()
                            }

                            "Navegador" -> {
                                abrirNavegador()
                            }
                            
                            

                        }
                    }
                )
            }


            /* =================================================
               OUTRAS FUNÇÕES
            ================================================= */

            item {

                Spacer(
                    modifier =
                        Modifier.height(22.dp)
                )

                TituloSecao(
                    titulo = "Outras funções",
                    dark = dark
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )
            }


            items(

                items = outrasFuncoes,

                key = {
                    it.titulo
                }

            ) { acesso ->

                Cartao(

                    acesso = acesso,

                    dark = dark,

                    onClick = {

                        when (acesso.titulo) {

                            "Gravador de voz" -> {
                                abrirGravador()
                            }

                            "Meus arquivos" -> {
                                abrirArquivos()
                            }
                            
                                                        "Tela de aplicativos" -> {
    abrirApps()
}
                        }
                    }
                )
            }


            /* =================================================
               ESPECIAL
            ================================================= */

            item {

                Spacer(
                    modifier =
                        Modifier.height(22.dp)
                )

                TituloSecao(
                    titulo = "Especial, só para você!",
                    dark = dark
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )
            }


            items(

                items = especiais,

                key = {
                    it.titulo
                }

            ) { acesso ->

                Cartao(

                    acesso = acesso,

                    dark = dark,

                    onClick = {}
                )
            }


            item {

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )
            }
        }
    }
}


/* =========================================================
   HERO
========================================================= */

@androidx.compose.runtime.Composable
private fun Hero(
    dark: Boolean
) {

    val texto =
        if (dark)
            TextoEscuro
        else
            TextoClaro


    val textoSuave =
        if (dark)
            TextoSuaveEscuro
        else
            TextoSuaveClaro


    val selo =
        if (dark)
            Color(0xFF91889F)
        else
            Color(0xFF77717F)


    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(310.dp)
                .padding(
                    top = 20.dp,
                    bottom = 70.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Box(

            modifier =
                Modifier
                    .size(104.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape =
                            RoundedCornerShape(
                                30.dp
                            )
                    )
                    .clip(
                        RoundedCornerShape(
                            30.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(
                                RoxoInicio,
                                RoxoFim
                            )
                        )
                    )
                    .border(
                        width = 1.dp,

                        color =
                            Color.White.copy(
                                alpha =
                                    if (dark)
                                        0.16f
                                    else
                                        0.30f
                            ),

                        shape =
                            RoundedCornerShape(
                                30.dp
                            )
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Image(

                painter =
                    painterResource(
                        id =
                            R.drawable.ic_lock
                    ),

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        50.dp
                    ),

                contentScale =
                    ContentScale.Fit
            )
        }


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        androidx.compose.material3.Text(

            text =
                "Meus Acessos",

            color =
                texto,

            fontFamily =
                Quicksand,

            fontSize =
                38.sp,

            fontWeight =
                FontWeight.ExtraBold,

            letterSpacing =
                (-1.2).sp,

            lineHeight =
                40.sp,

            textAlign =
                TextAlign.Center
        )


        Spacer(
            modifier =
                Modifier.height(15.dp)
        )


        androidx.compose.material3.Text(

            text =
                "Tudo o que você precisa, organizado em um só lugar.",

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp
                    ),

            color =
                textoSuave,

            fontFamily =
                Quicksand,

            fontSize =
                15.sp,

            fontWeight =
                FontWeight.Medium,

            lineHeight =
                23.sp,

            textAlign =
                TextAlign.Center
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        androidx.compose.material3.Text(

            text =
                "•  ÁREA PROTEGIDA",

            color =
                selo,

            fontFamily =
                Quicksand,

            fontSize =
                10.sp,

            fontWeight =
                FontWeight.Bold,

            letterSpacing =
                1.7.sp
        )
    }
}


/* =========================================================
   TÍTULO DA SEÇÃO
========================================================= */

@androidx.compose.runtime.Composable
private fun TituloSecao(

    titulo: String,

    dark: Boolean

) {

    val corTexto =
        if (dark)
            SecaoEscuro
        else
            SecaoClaro


    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 4.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(

            modifier =
                Modifier
                    .size(6.dp)
                    .background(
                        Rosa,
                        CircleShape
                    )
        )


        Spacer(
            modifier =
                Modifier.width(10.dp)
        )


        androidx.compose.material3.Text(

            text =
                titulo.uppercase(),

            color =
                corTexto,

            fontFamily =
                Quicksand,

            fontSize =
                10.sp,

            fontWeight =
                FontWeight.ExtraBold,

            letterSpacing =
                1.8.sp
        )
    }
}


/* =========================================================
   CARTÃO
========================================================= */

@androidx.compose.runtime.Composable
private fun Cartao(

    acesso: Acesso,

    dark: Boolean,

    onClick: () -> Unit = {}

) {

    val fundoCartao =
        if (dark)
            CartaoEscuro
        else
            CartaoClaro


    val borda =
        if (dark)
            BordaCartaoEscuro
        else
            BordaCartaoClaro


    Row(

        modifier =
            Modifier
                .fillMaxWidth()

                .clip(
                    RoundedCornerShape(
                        21.dp
                    )
                )

                .background(
                    fundoCartao
                )

                .border(

                    width = 1.dp,

                    color =
                        borda,

                    shape =
                        RoundedCornerShape(
                            21.dp
                        )
                )

                .clickable {
                    onClick()
                }

                .padding(
                    horizontal = 16.dp,
                    vertical = 15.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        ConteudoCartao(

            acesso =
                acesso,

            dark =
                dark,

            modifier =
                Modifier.fillMaxWidth()
        )
    }
}


/* =========================================================
   CONTEÚDO DO CARTÃO
========================================================= */

@androidx.compose.runtime.Composable
private fun ConteudoCartao(

    acesso: Acesso,

    dark: Boolean,

    modifier: Modifier

) {

    val texto =
        if (dark)
            TextoEscuro
        else
            TextoClaro


    val descricao =
        if (dark)
            TextoMutedEscuro
        else
            TextoMutedClaro


    val circulo =
        if (dark)
            CirculoEscuro
        else
            CirculoClaro


    val seta =
        if (dark)
            SetaEscuro
        else
            SetaClaro


    Row(

        modifier =
            modifier,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Spacer(
            modifier =
                Modifier.width(4.dp)
        )


        Column(

            modifier =
                Modifier.weight(1f)
        ) {

            androidx.compose.material3.Text(

                text =
                    acesso.titulo,

                color =
                    texto,

                fontFamily =
                    Quicksand,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold,

                lineHeight =
                    19.sp
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            androidx.compose.material3.Text(

                text =
                    acesso.descricao,

                color =
                    descricao,

                fontFamily =
                    Quicksand,

                fontSize =
                    12.sp,

                fontWeight =
                    FontWeight.Medium,

                lineHeight =
                    16.sp
            )
        }


        Spacer(
            modifier =
                Modifier.width(10.dp)
        )


        Box(
    modifier =
        Modifier
            .size(34.dp)
            .clip(
                CircleShape
            )
            .background(
                circulo
            ),

    contentAlignment =
        Alignment.Center
) {

    Icon(
        painter =
            painterResource(
                R.drawable.ic_arrow
            ),

        contentDescription =
            null,

        tint =
            seta,

        modifier =
            Modifier.size(18.dp)
    )
}
    }
}
}