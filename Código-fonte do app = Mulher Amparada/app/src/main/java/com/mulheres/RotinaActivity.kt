package com.mulheres

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.compose.foundation.layout.ColumnScope
import android.view.WindowManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import androidx.activity.enableEdgeToEdge

class RotinaActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        /*
         * Impede capturas de tela.
         */
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        /*
         * Conteúdo respeita as barras do sistema.
         * Diferente do edge-to-edge, não deixa os cartões
         * ficarem por baixo da barra superior/inferior.
         */
        WindowCompat.setDecorFitsSystemWindows(
            window,
            true
        )

        setContent {

            CompositionLocalProvider(
                LocalOverscrollFactory provides null
            ) {

                RotinaTheme {

                    RotinaScreen(
                        context = this
                    )
                }
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
   CORES
========================================================= */

private val Rosa =
    Color(0xFFFF7AA8)

private val RosaClaro =
    Color(0xFFFFB5CD)

private val Verde =
    Color(0xFF6FFFB0)

private val Vermelho =
    Color(0xFFFF6678)


/* =========================================================
   TEMA
========================================================= */

@Composable
private fun RotinaTheme(
    content: @Composable () -> Unit
) {

    val dark =
        isSystemInDarkTheme()

    val background =
        if (dark)
            Color.Black
        else
            Color(0xFFF7F7FA)

    val surface =
        if (dark)
            Color(0xFF111116)
        else
            Color.White

    val text =
        if (dark)
            Color.White
        else
            Color(0xFF17171B)

    val primary =
        if (dark)
            Rosa
        else
            Color(0xFFFF4F88)

    val view =
    androidx.compose.ui.platform.LocalView.current

val window =
    (view.context as ComponentActivity).window

androidx.compose.runtime.SideEffect {

    WindowCompat.setDecorFitsSystemWindows(
        window,
        true
    )

    window.statusBarColor =
        if (dark)
            AndroidColor.BLACK
        else
            AndroidColor.WHITE

    window.navigationBarColor =
        if (dark)
            AndroidColor.BLACK
        else
            AndroidColor.WHITE

    if (
        android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.Q
    ) {
        window.isNavigationBarContrastEnforced =
            false
    }

    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).apply {

        isAppearanceLightStatusBars =
            !dark

        isAppearanceLightNavigationBars =
            !dark
    }
}

    MaterialTheme(

        colorScheme =
            if (dark) {

                darkColorScheme(
                    primary = primary,
                    background = background,
                    surface = surface,
                    onBackground = text,
                    onSurface = text
                )

            } else {

                lightColorScheme(
                    primary = primary,
                    background = background,
                    surface = surface,
                    onBackground = text,
                    onSurface = text
                )
            },

        typography =
            MaterialTheme.typography.copy(
                bodyLarge =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = Quicksand
                    ),
                bodyMedium =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = Quicksand
                    ),
                bodySmall =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = Quicksand
                    )
            ),

        content = content
    )
}


/* =========================================================
   MODELO
========================================================= */

private data class Conquista(
    val id: String,
    val nome: String,
    val descricao: String
)


private data class Exercicio(
    val nome: String,
    val descricao: String
)


/* =========================================================
   EXERCÍCIOS
========================================================= */

private val exerciciosIniciante =
    listOf(

        Exercicio(
            "Caminhada leve",
            "Faça no seu próprio ritmo."
        ),

        Exercicio(
            "Agachamento",
            "Faça movimentos confortáveis."
        ),

        Exercicio(
            "Polichinelo",
            "Mantenha um ritmo confortável."
        )
    )


private val exerciciosIntermediario =
    listOf(

        Exercicio(
            "Corrida",
            "Mantenha um ritmo confortável."
        ),

        Exercicio(
            "Flexão",
            "Faça apenas o que for confortável."
        ),

        Exercicio(
            "Abdominal",
            "Mantenha movimentos controlados."
        )
    )


private val exerciciosAvancado =
    listOf(

        Exercicio(
            "Sprint",
            "Faça somente se for apropriado para você."
        ),

        Exercicio(
            "Burpee",
            "Mantenha movimentos controlados."
        ),

        Exercicio(
            "Treino intenso",
            "Respeite seus limites."
        )
    )


/* =========================================================
   SCREEN
========================================================= */

@Composable
private fun RotinaScreen(
    context: Context
) {

    val dark =
        isSystemInDarkTheme()

    val prefs =
        remember {

            context.getSharedPreferences(
                "rotina",
                Context.MODE_PRIVATE
            )
        }


    var xp by remember {
        mutableIntStateOf(
            prefs.getInt(
                "xp",
                0
            )
        )
    }


    var nivel by remember {
        mutableIntStateOf(
            prefs.getInt(
                "nivel",
                1
            )
        )
    }


    var streak by remember {
        mutableIntStateOf(
            prefs.getInt(
                "streak",
                0
            )
        )
    }


    var peso by remember {
        mutableStateOf(
            prefs.getString(
                "peso",
                ""
            ) ?: ""
        )
    }


    var conquistas by remember {

        mutableStateOf(
            prefs.getStringSet(
                "conquistas",
                emptySet()
            ) ?: emptySet()
        )
    }


    var exercicios by remember {
        mutableStateOf(
            emptyList<Exercicio>()
        )
    }


    var exercicioAtual by remember {
        mutableIntStateOf(0)
    }


    var tempo by remember {
        mutableIntStateOf(30)
    }


    var treinando by remember {
        mutableStateOf(false)
    }


    var popupTitulo by remember {
        mutableStateOf<String?>(null)
    }


    var popupTexto by remember {
        mutableStateOf("")
    }


    var confirmacaoReset by remember {
        mutableStateOf(false)
    }


    val fundo =
        if (dark)
            Color.Black
        else
            Color(0xFFF7F7FA)


    /*
     * Contagem do exercício.
     */
    LaunchedEffect(
        treinando,
        exercicioAtual
    ) {

        if (!treinando)
            return@LaunchedEffect

        tempo = 30

        while (tempo > 0) {

            delay(1000)

            tempo--
        }

        if (
            exercicioAtual + 1 <
            exercicios.size
        ) {

            exercicioAtual++

            popupTitulo =
                "Boa 🔥"

            popupTexto =
                "Próximo exercício!"

        } else {

            treinando = false

            ganharXPLocal(
                quantidade = 20,
                xpAtual = xp,
                nivelAtual = nivel,
                prefs = prefs,
                onXp = {
                    xp = it
                },
                onNivel = {
                    nivel = it
                },
                onConquista = {
                    conquistas =
                        conquistas + it
                },
                mostrarPopup = { titulo, texto ->
                    popupTitulo = titulo
                    popupTexto = texto
                }
            )

            streak++

            prefs.edit()
                .putInt(
                    "streak",
                    streak
                )
                .apply()

            popupTitulo =
                "Treino concluído 💪"

            popupTexto =
                "A atividade foi concluída."

        }
    }


    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(fundo)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                )
    ) {

        LazyColumn(

            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 18.dp,
                    bottom = 40.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {

            item {

                Topo(
                    dark = dark
                )
            }


            item {

                ProgressoCard(
                    dark = dark,
                    xp = xp,
                    nivel = nivel,
                    streak = streak
                )
            }


            item {

                TreinoCard(
                    dark = dark,
                    treinando = treinando,
                    exercicios = exercicios,
                    exercicioAtual = exercicioAtual,
                    tempo = tempo,

                    iniciar = { nivelEscolhido ->

                        exercicios =
                            when (nivelEscolhido) {

                                "Iniciante" ->
                                    exerciciosIniciante

                                "Intermediário" ->
                                    exerciciosIntermediario

                                else ->
                                    exerciciosAvancado
                            }

                        exercicioAtual = 0
                        tempo = 30
                        treinando = true
                    }
                )
            }


            item {

                HidratacaoCard(

                    dark = dark,

                    agua = {

                        ganharXPLocal(
                            quantidade = 5,
                            xpAtual = xp,
                            nivelAtual = nivel,
                            prefs = prefs,
                            onXp = {
                                xp = it
                            },
                            onNivel = {
                                nivel = it
                            },
                            onConquista = {
                                conquistas =
                                    conquistas + it
                            },
                            mostrarPopup = { titulo, texto ->
                                popupTitulo = titulo
                                popupTexto = texto
                            }
                        )

                        popupTitulo =
                            "Boa 💧"

                        popupTexto =
                            "Registro de hidratação adicionado."
                    },

                    refrigerante = {

                        popupTitulo =
                            "Registro"

                        popupTexto =
                            "O registro foi feito."
                    }
                )
            }


            item {

                AlimentacaoCard(

                    dark = dark,

                    saudavel = {

                        ganharXPLocal(
                            quantidade = 5,
                            xpAtual = xp,
                            nivelAtual = nivel,
                            prefs = prefs,
                            onXp = {
                                xp = it
                            },
                            onNivel = {
                                nivel = it
                            },
                            onConquista = {
                                conquistas =
                                    conquistas + it
                            },
                            mostrarPopup = { titulo, texto ->
                                popupTitulo = titulo
                                popupTexto = texto
                            }
                        )

                        popupTitulo =
                            "Registro"

                        popupTexto =
                            "Sua escolha foi registrada."
                    },

                    fast = {

                        popupTitulo =
                            "Registro"

                        popupTexto =
                            "Sua escolha foi registrada."
                    }
                )
            }


            item {

                PesoCard(

                    dark = dark,

                    peso = peso,

                    onPesoChange = {
                        peso = it
                    },

                    salvar = {

                        prefs.edit()
                            .putString(
                                "peso",
                                peso
                            )
                            .apply()

                        popupTitulo =
                            "Peso salvo 💾"

                        popupTexto =
                            "O valor foi atualizado."
                    }
                )
            }


            item {

                ConquistasCard(

                    dark = dark,

                    conquistas = conquistas
                )
            }


            item {

                Button(

                    onClick = {

                        if (!confirmacaoReset) {

                            confirmacaoReset =
                                true

                            popupTitulo =
                                "Atenção"

                            popupTexto =
                                "Toque novamente em \"Resetar tudo\" para confirmar."

                        } else {

                            prefs.edit()
                                .clear()
                                .apply()

                            xp = 0
                            nivel = 1
                            streak = 0
                            peso = ""
                            conquistas =
                                emptySet()

                            confirmacaoReset =
                                false

                            popupTitulo =
                                "Dados apagados"

                            popupTexto =
                                "Tudo foi resetado."
                        }
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(54.dp),

                    shape =
                        RoundedCornerShape(
                            999.dp
                        ),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Vermelho
                        )
                ) {

                    Text(
                        text =
                            if (confirmacaoReset)
                                "Confirmar reset"
                            else
                                "Resetar tudo",

                        fontFamily =
                            Quicksand,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }


        if (popupTitulo != null) {

            AlertDialog(

                onDismissRequest = {
                    popupTitulo = null
                },

                title = {

                    Text(
                        text =
                            popupTitulo ?: "",

                        fontFamily =
                            Quicksand,

                        fontWeight =
                            FontWeight.Bold
                    )
                },

                text = {

                    Text(
                        text =
                            popupTexto,

                        fontFamily =
                            Quicksand
                    )
                },

                confirmButton = {

                    TextButton(

                        onClick = {
                            popupTitulo = null
                        }
                    ) {

                        Text(
                            text = "OK",

                            fontFamily =
                                Quicksand,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}


/* =========================================================
   TOPO
========================================================= */

@Composable
private fun Topo(
    dark: Boolean
) {

    val texto =
        if (dark)
            Color.White
        else
            Color(0xFF17171B)

    val suave =
        if (dark)
            Color.White.copy(
                alpha = .52f
            )
        else
            Color(0xFF666671)

    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    start = 8.dp,
                    end = 8.dp
                )
    ) {

        Text(

            text =
                "Rotina gamificada",

            color =
                texto,

            fontFamily =
                Quicksand,

            fontSize =
                30.sp,

            fontWeight =
                FontWeight.Bold,

            letterSpacing =
                (-.8).sp
        )


        Spacer(
            modifier =
                Modifier.height(7.dp)
        )


        Text(

            text =
                "Pequenos registros para acompanhar sua rotina.",

            color =
                suave,

            fontFamily =
                Quicksand,

            fontSize =
                13.sp,

            fontWeight =
                FontWeight.Medium
        )
    }
}


/* =========================================================
   PROGRESSO
========================================================= */

@Composable
private fun ProgressoCard(

    dark: Boolean,

    xp: Int,

    nivel: Int,

    streak: Int

) {

    CardBase(
        dark = dark
    ) {

        val progresso =
            (
                xp.toFloat() /
                    (nivel * 100).toFloat()
                )
                .coerceIn(
                    0f,
                    1f
                )


        Box(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(
                        RoundedCornerShape(
                            999.dp
                        )
                    )
                    .background(
                        if (dark)
                            Color.White.copy(
                                alpha = .055f
                            )
                        else
                            Color.Black.copy(
                                alpha = .06f
                            )
                    )
        ) {

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth(
                            progresso
                        )
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                999.dp
                            )
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Rosa,
                                    RosaClaro
                                )
                            )
                        )
            )
        }


        Spacer(
            modifier =
                Modifier.height(17.dp)
        )
Row(

    modifier =
        Modifier.fillMaxWidth(),

    horizontalArrangement =
        Arrangement.spacedBy(
            10.dp
        )
) {

    Estatistica(
        titulo = xp.toString(),
        legenda = "XP",
        dark = dark,
        modifier =
            Modifier.weight(1f)
    )

    Estatistica(
        titulo = nivel.toString(),
        legenda = "Nível",
        dark = dark,
        modifier =
            Modifier.weight(1f)
    )

    Estatistica(
        titulo = streak.toString(),
        legenda = "Streak",
        dark = dark,
        modifier =
            Modifier.weight(1f)
    )
}
    }
}

@Composable
private fun Estatistica(

    titulo: String,

    legenda: String,

    dark: Boolean,

    modifier: Modifier = Modifier

) {

    val text =
        if (dark)
            Color.White
        else
            Color(0xFF17171B)

    val muted =
        if (dark)
            Color.White.copy(
                alpha = .52f
            )
        else
            Color(0xFF85858F)


    Box(

        modifier =
            modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        17.dp
                    )
                )
                .background(
                    if (dark)
                        Color.White.copy(
                            alpha = .035f
                        )
                    else
                        Color.Black.copy(
                            alpha = .025f
                        )
                )
                .border(
                    width = 1.dp,
                    color =
                        if (dark)
                            Color.White.copy(
                                alpha = .08f
                            )
                        else
                            Color.Black.copy(
                                alpha = .07f
                            ),
                    shape =
                        RoundedCornerShape(
                            17.dp
                        )
                )
                .padding(
                    vertical = 15.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = titulo,
                color = text,
                fontFamily = Quicksand,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text = legenda,
                color = muted,
                fontFamily = Quicksand,
                fontSize = 11.sp
            )
        }
    }
}

/* =========================================================
   TREINO
========================================================= */

@Composable
private fun TreinoCard(

    dark: Boolean,

    treinando: Boolean,

    exercicios: List<Exercicio>,

    exercicioAtual: Int,

    tempo: Int,

    iniciar: (String) -> Unit

) {

    CardBase(
        dark = dark
    ) {

        TituloCard(
            texto = "Treino",
            icon = R.drawable.ic_17,
            dark = dark
        )


        BotaoPrincipal(
            texto = "Iniciante",
            onClick = {
                iniciar("Iniciante")
            }
        )


        BotaoPrincipal(
            texto = "Intermediário",
            onClick = {
                iniciar("Intermediário")
            }
        )


        BotaoPrincipal(
            texto = "Avançado",
            onClick = {
                iniciar("Avançado")
            }
        )


        ExercicioBox(

            dark = dark,

            exercicio =
                if (
                    exercicios.isNotEmpty() &&
                    exercicioAtual <
                    exercicios.size
                )
                    exercicios[exercicioAtual]
                else
                    null,

            tempo = tempo,

            treinando = treinando
        )
    }
}


/* =========================================================
   EXERCÍCIO
========================================================= */

@Composable
private fun ExercicioBox(

    dark: Boolean,

    exercicio: Exercicio?,

    tempo: Int,

    treinando: Boolean

) {

    val text =
        if (dark)
            Color.White
        else
            Color(0xFF17171B)

    val muted =
        if (dark)
            Color.White.copy(
                alpha = .52f
            )
        else
            Color(0xFF666671)


    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 15.dp
                )
                .clip(
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .background(
                    if (dark)
                        Color.White.copy(
                            alpha = .035f
                        )
                    else
                        Color.Black.copy(
                            alpha = .025f
                        )
                )
                .border(
                    1.dp,
                    if (dark)
                        Color.White.copy(
                            alpha = .08f
                        )
                    else
                        Color.Black.copy(
                            alpha = .07f
                        ),
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .padding(20.dp)
    ) {

        Column {

            Text(

                text =
                    exercicio?.nome
                        ?: "Nenhum exercício iniciado",

                color =
                    text,

                fontFamily =
                    Quicksand,

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(

                text =
                    exercicio?.descricao
                        ?: "Escolha um nível para começar",

                color =
                    muted,

                fontFamily =
                    Quicksand,

                fontSize =
                    13.sp,

                lineHeight =
                    20.sp
            )


            Spacer(
                modifier =
                    Modifier.height(15.dp)
            )


            Text(

                text =
                    if (treinando)
                        tempo.toString()
                    else
                        "30",

                modifier =
                    Modifier.fillMaxWidth(),

                textAlign =
                    TextAlign.Center,

                color =
                    RosaClaro,

                fontFamily =
                    Quicksand,

                fontSize =
                    52.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    (-2).sp
            )
        }
    }
}


/* =========================================================
   HIDRATAÇÃO
========================================================= */

@Composable
private fun HidratacaoCard(

    dark: Boolean,

    agua: () -> Unit,

    refrigerante: () -> Unit

) {

    CardBase(
        dark = dark
    ) {

        TituloCard(
            texto = "Hidratação",
            icon = R.drawable.ic_18,
            dark = dark
        )

        BotaoPrincipal(
            texto = "Beber água",
            onClick = agua
        )

        BotaoSecundario(
            texto = "Refrigerante",
            onClick = refrigerante
        )
    }
}


/* =========================================================
   ALIMENTAÇÃO
========================================================= */

@Composable
private fun AlimentacaoCard(

    dark: Boolean,

    saudavel: () -> Unit,

    fast: () -> Unit

) {

    CardBase(
        dark = dark
    ) {

        TituloCard(
            texto = "Alimentação",
            icon = R.drawable.ic_19,
            dark = dark
        )

        BotaoPrincipal(
            texto = "Comida saudável",
            onClick = saudavel
        )

        BotaoSecundario(
            texto = "Fast food",
            onClick = fast
        )
    }
}


/* =========================================================
   PESO
========================================================= */

@Composable
private fun PesoCard(

    dark: Boolean,

    peso: String,

    onPesoChange: (String) -> Unit,

    salvar: () -> Unit

) {

    CardBase(
        dark = dark
    ) {

        TituloCard(
            texto = "Peso",
            icon = R.drawable.ic_20,
            dark = dark
        )


        OutlinedTextField(

            value = peso,

            onValueChange = {
                onPesoChange(
                    it.filter {
                        char ->
                        char.isDigit() ||
                            char == ',' ||
                            char == '.'
                    }
                )
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 2.dp
                    ),

            placeholder = {

                Text(
                    text =
                        "Digite seu peso",

                    fontFamily =
                        Quicksand
                )
            },

            singleLine = true,

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Decimal
                ),

            shape =
                RoundedCornerShape(
                    17.dp
                )
        )


        BotaoPrincipal(
            texto = "Salvar peso",
            onClick = salvar
        )


        if (peso.isNotBlank()) {

            Text(

                text =
                    "Peso registrado: $peso kg",

                modifier =
                    Modifier.padding(
                        top = 15.dp
                    ),

                color =
                    if (dark)
                        Color.White.copy(
                            alpha = .52f
                        )
                    else
                        Color(0xFF666671),

                fontFamily =
                    Quicksand,

                fontSize =
                    13.sp
            )
        }
    }
}


/* =========================================================
   CONQUISTAS
========================================================= */

@Composable
private fun ConquistasCard(

    dark: Boolean,

    conquistas: Set<String>

) {

    val lista =
        listOf(

            Conquista(
                "nivel2",
                "Subindo 🚀",
                "Chegou ao nível 2"
            ),

            Conquista(
                "nivel5",
                "Focada 🔥",
                "Chegou ao nível 5"
            ),

            Conquista(
                "desafio1",
                "Primeiro desafio 🎯",
                "Concluiu um desafio"
            )
        )


    val text =
        if (dark)
            Color.White
        else
            Color(0xFF17171B)


    val muted =
        if (dark)
            Color.White.copy(
                alpha = .52f
            )
        else
            Color(0xFF666671)


    CardBase(
        dark = dark
    ) {

        TituloCard(
            texto = "Conquistas",
            icon = R.drawable.ic_21,
            dark = dark
        )


        if (conquistas.isEmpty()) {

            Text(

                text =
                    "Nenhuma conquista ainda.",

                color =
                    muted,

                fontFamily =
                    Quicksand,

                fontSize =
                    13.sp
            )

        } else {

            lista
                .filter {
                    conquistas.contains(
                        it.id
                    )
                }
                .forEach { conquista ->

                    Column(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 9.dp
                                )
                                .clip(
                                    RoundedCornerShape(
                                        15.dp
                                    )
                                )
                                .background(
                                    if (dark)
                                        Color.White.copy(
                                            alpha = .035f
                                        )
                                    else
                                        Color.Black.copy(
                                            alpha = .025f
                                        )
                                )
                                .border(
                                    1.dp,
                                    if (dark)
                                        Color.White.copy(
                                            alpha = .07f
                                        )
                                    else
                                        Color.Black.copy(
                                            alpha = .06f
                                        ),
                                    RoundedCornerShape(
                                        15.dp
                                    )
                                )
                                .padding(
                                    15.dp
                                )
                    ) {

                        Text(
                            text =
                                conquista.nome,

                            color =
                                text,

                            fontFamily =
                                Quicksand,

                            fontWeight =
                                FontWeight.Bold
                        )


                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )


                        Text(
                            text =
                                conquista.descricao,

                            color =
                                muted,

                            fontFamily =
                                Quicksand,

                            fontSize =
                                12.sp
                        )
                    }
                }
        }
    }
}


/* =========================================================
   CARD BASE
========================================================= */

@Composable
private fun CardBase(

    dark: Boolean,

    content: @Composable ColumnScope.() -> Unit

) {

    val background =
        if (dark)
            Brush.linearGradient(
                listOf(
                    Color.White.copy(
                        alpha = .075f
                    ),
                    Color.White.copy(
                        alpha = .025f
                    )
                )
            )
        else
            Brush.linearGradient(
                listOf(
                    Color.White,
                    Color(0xFFF4F3F7)
                )
            )


    val border =
        if (dark)
            Color.White.copy(
                alpha = .09f
            )
        else
            Color.Black.copy(
                alpha = .08f
            )


    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        28.dp
                    )
                )
                .background(
                    background
                )
                .border(
                    width = 1.dp,
                    color = border,
                    shape =
                        RoundedCornerShape(
                            28.dp
                        )
                )
                .padding(22.dp),

        content = content
    )
}


/* =========================================================
   TÍTULO DO CARD
========================================================= */

@Composable
private fun TituloCard(

    texto: String,

    icon: Int,

    dark: Boolean

) {

    val color =
        if (dark)
            Color.White
        else
            Color(0xFF17171B)


    Row(

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(

            painter =
                painterResource(
                    id = icon
                ),

            contentDescription =
                null,

            tint =
                Rosa,

            modifier =
                Modifier.size(24.dp)
        )


        Spacer(
            modifier =
                Modifier.width(9.dp)
        )


        Text(

            text =
                texto,

            color =
                color,

            fontFamily =
                Quicksand,

            fontSize =
                21.sp,

            fontWeight =
                FontWeight.Bold,

            letterSpacing =
                (-.3).sp
        )
    }


    Spacer(
        modifier =
            Modifier.height(7.dp)
    )
}


/* =========================================================
   BOTÕES
========================================================= */

@Composable
private fun BotaoPrincipal(

    texto: String,

    onClick: () -> Unit

) {

    Button(

        onClick = onClick,

        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(
                    top = 10.dp
                ),

        shape =
            RoundedCornerShape(
                999.dp
            ),

        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    Rosa
            )
    ) {

        Text(

            text =
                texto,

            color =
                Color.White,

            fontFamily =
                Quicksand,

            fontWeight =
                FontWeight.Bold,

            fontSize =
                14.sp
        )
    }
}


@Composable
private fun BotaoSecundario(

    texto: String,

    onClick: () -> Unit

) {

    val dark =
        isSystemInDarkTheme()


    Button(

        onClick = onClick,

        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(
                    top = 10.dp
                ),

        shape =
            RoundedCornerShape(
                999.dp
            ),

        colors =
            ButtonDefaults.buttonColors(

                containerColor =
                    if (dark)
                        Color.White.copy(
                            alpha = .055f
                        )
                    else
                        Color.Black.copy(
                            alpha = .055f
                        ),

                contentColor =
                    if (dark)
                        Color.White.copy(
                            alpha = .85f
                        )
                    else
                        Color(0xFF333338)
            )
    ) {

        Text(

            text =
                texto,

            fontFamily =
                Quicksand,

            fontWeight =
                FontWeight.Bold,

            fontSize =
                14.sp
        )
    }
}


/* =========================================================
   XP
========================================================= */

private fun ganharXPLocal(

    quantidade: Int,

    xpAtual: Int,

    nivelAtual: Int,

    prefs: android.content.SharedPreferences,

    onXp: (Int) -> Unit,

    onNivel: (Int) -> Unit,

    onConquista: (String) -> Unit,

    mostrarPopup: (String, String) -> Unit

) {

    var novoXp =
        xpAtual + quantidade

    var novoNivel =
        nivelAtual

    var novaConquista: String? =
        null


    if (
        novoXp >=
        novoNivel * 100
    ) {

        novoXp = 0

        novoNivel++

        mostrarPopup(
            "🚀 Novo nível!",
            "Agora você está no nível $novoNivel."
        )

        if (
            novoNivel == 2
        ) {
            novaConquista =
                "nivel2"
        }

        if (
            novoNivel == 5
        ) {
            novaConquista =
                "nivel5"
        }
    }


    onXp(novoXp)

    onNivel(novoNivel)


    prefs.edit()

        .putInt(
            "xp",
            novoXp
        )

        .putInt(
            "nivel",
            novoNivel
        )

        .apply()


    if (
        novaConquista != null
    ) {

        val atual =
            prefs.getStringSet(
                "conquistas",
                emptySet()
            )?.toMutableSet()
                ?: mutableSetOf()


        if (
            atual.add(
                novaConquista
            )
        ) {

            prefs.edit()
                .putStringSet(
                    "conquistas",
                    atual
                )
                .apply()

            onConquista(
                novaConquista
            )
        }
    }
}