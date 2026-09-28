package com.mulheres

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.BorderStroke
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.mulheres.R
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val Quicksand = FontFamily(
    Font(R.font.quicksand)
)

private val Rosa = Color(0xFFFF7F9F)
private val Rosa2 = Color(0xFFFF7F9F)
private val Rosa3 = Color(0xFFFFB1C4)

private data class CicloRegistro(
    val data: String,
    val dor: Int,
    val humor: String,
    val agua: Boolean,
    val cafe: Boolean,
    val chocolate: Boolean,
    val exercicio: Boolean,
    val sonoRuim: Boolean,
    val apetite: Boolean,
    val cansaco: Boolean,
    val irritacao: Boolean,
    val ansiedade: Boolean,
    val colica: Boolean,
    val dorCabeca: Boolean,
    val inchaco: Boolean,
    val acne: Boolean,
    val nausea: Boolean,
    val tontura: Boolean,
    val energiaBaixa: Boolean,
    val estresse: Boolean,
    val tristeza: Boolean,
    val felicidade: Boolean,
    val sensibilidade: Boolean,
    val concentracao: Boolean,
    val libido: Boolean
)

private enum class Ordem {
    NOVO,
    ANTIGO,
    DOR
}

private fun CicloRegistro.toJson(): JSONObject {
    return JSONObject().apply {
        put("data", data)
        put("dor", dor)
        put("humorTexto", humor)

        put("agua", agua)
        put("cafe", cafe)
        put("chocolate", chocolate)
        put("exercicio", exercicio)
        put("sonoRuim", sonoRuim)
        put("apetite", apetite)

        put("cansaco", cansaco)
        put("irritacao", irritacao)
        put("ansiedade", ansiedade)
        put("colica", colica)
        put("dorCabeca", dorCabeca)
        put("inchaco", inchaco)
        put("acne", acne)
        put("nausea", nausea)
        put("tontura", tontura)
        put("energiaBaixa", energiaBaixa)

        put("estresse", estresse)
        put("tristeza", tristeza)
        put("felicidade", felicidade)
        put("sensibilidade", sensibilidade)
        put("concentracao", concentracao)
        put("libido", libido)
    }
}

private fun JSONObject.bool(nome: String): Boolean {
    return optBoolean(nome, false)
}

private fun JSONObject.toCicloRegistro(): CicloRegistro {
    return CicloRegistro(
        data = optString("data"),
        dor = optInt("dor", 0),
        humor = optString("humorTexto", "feliz"),

        agua = bool("agua"),
        cafe = bool("cafe"),
        chocolate = bool("chocolate"),
        exercicio = bool("exercicio"),
        sonoRuim = bool("sonoRuim"),
        apetite = bool("apetite"),

        cansaco = bool("cansaco"),
        irritacao = bool("irritacao"),
        ansiedade = bool("ansiedade"),
        colica = bool("colica"),
        dorCabeca = bool("dorCabeca"),
        inchaco = bool("inchaco"),
        acne = bool("acne"),
        nausea = bool("nausea"),
        tontura = bool("tontura"),
        energiaBaixa = bool("energiaBaixa"),

        estresse = bool("estresse"),
        tristeza = bool("tristeza"),
        felicidade = bool("felicidade"),
        sensibilidade = bool("sensibilidade"),
        concentracao = bool("concentracao"),
        libido = bool("libido")
    )
}

private fun carregarCiclo(
    context: Context
): List<CicloRegistro> {
    return try {
        val cripto = Cripto(context)
        val bruto = cripto.carregar("ciclo")

        if (bruto.isBlank()) {
            emptyList()
        } else {
            val array = JSONArray(bruto)

            buildList {
                for (i in 0 until array.length()) {
                    add(
                        array
                            .getJSONObject(i)
                            .toCicloRegistro()
                    )
                }
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun salvarCiclo(
    context: Context,
    registros: List<CicloRegistro>
) {
    try {
        val cripto = Cripto(context)
        val array = JSONArray()

        registros.forEach {
            array.put(it.toJson())
        }

        cripto.salvar(
            "ciclo",
            array.toString()
        )
    } catch (_: Exception) {
    }
}

private fun apagarCiclo(
    context: Context
) {
    try {
        val cripto = Cripto(context)
        cripto.remover("ciclo")
    } catch (_: Exception) {
    }
}

private fun formatarData(
    data: String
): String {
    if (data.isBlank()) {
        return "—"
    }

    val partes = data.split("-")

    if (partes.size != 3) {
        return data
    }

    return "${partes[2]}/${partes[1]}/${partes[0]}"
}

private fun emojiHumor(
    humor: String
): String {
    return when (humor) {
        "feliz" -> "😊"
        "neutro" -> "😐"
        "desconfortavel" -> "😣"
        "triste" -> "😢"
        else -> "😐"
    }
}

private fun nomeHumor(
    humor: String
): String {
    return when (humor) {
        "feliz" -> "Feliz"
        "neutro" -> "Neutro"
        "desconfortavel" -> "Desconfortável"
        "triste" -> "Triste"
        else -> "Neutro"
    }
}

private fun hojeISO(): String {
    val formato = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    )

    return formato.format(
        Calendar.getInstance().time
    )
}

private fun booleanValue(
    v: Boolean
): Float {
    return if (v) 1f else 0f
}

class CicloActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            true
        )

        setContent {
            CicloApp()
        }
    }
}

@Composable
private fun CicloApp() {

    val ctx = LocalContext.current
    val dark = isSystemInDarkTheme()

    val fundo =
        if (dark) {
            Color.Black
        } else {
            Color.White
        }

    val texto =
        if (dark) {
            Color.White
        } else {
            Color(0xFF161216)
        }

    val texto2 =
        if (dark) {
            Color.White.copy(alpha = .68f)
        } else {
            Color.Black.copy(alpha = .62f)
        }

    val texto3 =
        if (dark) {
            Color.White.copy(alpha = .42f)
        } else {
            Color.Black.copy(alpha = .42f)
        }

    val borda =
        if (dark) {
            Color.White.copy(alpha = .10f)
        } else {
            Color.Black.copy(alpha = .09f)
        }

    val card =
        if (dark) {
            Color.White.copy(alpha = .075f)
        } else {
            Color.White
        }

    val card2 =
        if (dark) {
            Color.White.copy(alpha = .11f)
        } else {
            Color(0xFFF0F0F3)
        }

    SideEffect {
        val corBarra =
            if (dark) {
                AndroidColor.BLACK
            } else {
                AndroidColor.WHITE
            }

        windowColor(
            window = ctx as CicloActivity,
            color = corBarra,
            dark = dark
        )
    }

    val scheme =
        if (dark) {
            darkColorScheme(
                primary = Rosa,
                background = fundo,
                surface = card,
                onBackground = texto,
                onSurface = texto
            )
        } else {
            lightColorScheme(
                primary = Rosa2,
                background = fundo,
                surface = card,
                onBackground = texto,
                onSurface = texto
            )
        }

    MaterialTheme(
        colorScheme = scheme
    ) {

        var registros by remember {
            mutableStateOf(
                carregarCiclo(ctx)
            )
        }

        var ordem by remember {
            mutableStateOf(
                Ordem.NOVO
            )
        }

        var pesquisa by remember {
            mutableStateOf("")
        }

        var telaDetalhes by remember {
            mutableStateOf<CicloRegistro?>(null)
        }

        var mostrarNovo by remember {
            mutableStateOf(false)
        }

        var confirmarApagar by remember {
            mutableStateOf(false)
        }

        val filtrados =
            remember(
                registros,
                pesquisa,
                ordem
            ) {

                var lista =
                    registros.filter { registro ->

                        if (pesquisa.isBlank()) {
                            true
                        } else {

                            val textoBusca =
                                listOf(
                                    registro.data,
                                    registro.humor,
                                    registro.dor.toString()
                                )
                                    .joinToString(" ")
                                    .lowercase()

                            textoBusca.contains(
                                pesquisa.lowercase()
                            )
                        }
                    }

                lista =
                    when (ordem) {

                        Ordem.NOVO ->
                            lista.sortedByDescending {
                                it.data
                            }

                        Ordem.ANTIGO ->
                            lista.sortedBy {
                                it.data
                            }

                        Ordem.DOR ->
                            lista.sortedByDescending {
                                it.dor
                            }
                    }

                lista
            }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fundo)
                .safeDrawingPadding()
        ) {

            if (telaDetalhes != null) {

                TelaDetalhes(
                    registro = telaDetalhes!!,
                    dark = dark,
                    texto = texto,
                    texto2 = texto2,
                    texto3 = texto3,
                    borda = borda,
                    card = card,
                    onBack = {
                        telaDetalhes = null
                    }
                )

            } else {

                TelaLista(
                    registros = registros,
                    filtrados = filtrados,
                    pesquisa = pesquisa,
                    ordem = ordem,
                    dark = dark,
                    texto = texto,
                    texto2 = texto2,
                    texto3 = texto3,
                    borda = borda,
                    card = card,
                    card2 = card2,
                    onPesquisa = {
                        pesquisa = it
                    },
                    onOrdem = {
                        ordem = it
                    },
                    onAdicionar = {
                        mostrarNovo = true
                    },
                    onApagar = {
                        if (registros.isNotEmpty()) {
                            confirmarApagar = true
                        }
                    },
                    onAbrir = {
                        telaDetalhes = it
                    }
                )
            }

            if (mostrarNovo) {

                NovoRegistroDialog(
                    dark = dark,
                    texto = texto,
                    texto2 = texto2,
                    borda = borda,
                    card = card,
                    onFechar = {
                        mostrarNovo = false
                    },
                    onSalvar = { novo ->

                        val novaLista =
                            registros + novo

                        salvarCiclo(
                            ctx,
                            novaLista
                        )

                        registros = novaLista
                        mostrarNovo = false
                    }
                )
            }

            if (confirmarApagar) {

                AlertDialog(
                    onDismissRequest = {
                        confirmarApagar = false
                    },
                    title = {

                        Text(
                            text = "Apagar registros",
                            fontFamily = Quicksand,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    text = {

                        Text(
                            text =
                                "Apagar todos os registros?\n\nEssa ação não pode ser desfeita.",
                            fontFamily = Quicksand
                        )
                    },
                    confirmButton = {

                        TextButton(
                            onClick = {

                                apagarCiclo(ctx)

                                registros = emptyList()

                                confirmarApagar = false
                            }
                        ) {

                            Text(
                                text = "Apagar",
                                color = Rosa2,
                                fontFamily = Quicksand,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {

                        TextButton(
                            onClick = {
                                confirmarApagar = false
                            }
                        ) {

                            Text(
                                text = "Cancelar",
                                fontFamily = Quicksand
                            )
                        }
                    }
                )
            }
        }
    }
}

private fun windowColor(
    window: CicloActivity,
    color: Int,
    dark: Boolean
) {

    window.window.statusBarColor = color
    window.window.navigationBarColor = color

    WindowCompat.getInsetsController(
        window.window,
        window.window.decorView
    ).apply {

        isAppearanceLightStatusBars = !dark
        isAppearanceLightNavigationBars = !dark
    }
}

@Composable
private fun TelaLista(
    registros: List<CicloRegistro>,
    filtrados: List<CicloRegistro>,
    pesquisa: String,
    ordem: Ordem,
    dark: Boolean,
    texto: Color,
    texto2: Color,
    texto3: Color,
    borda: Color,
    card: Color,
    card2: Color,
    onPesquisa: (String) -> Unit,
    onOrdem: (Ordem) -> Unit,
    onAdicionar: () -> Unit,
    onApagar: () -> Unit,
    onAbrir: (CicloRegistro) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        item {

            Spacer(
                Modifier.height(4.dp)
            )

            TopoCiclo(
                texto = texto,
                texto3 = texto3,
                dark = dark,
                onAdicionar = onAdicionar,
                onApagar = onApagar
            )

            Spacer(
                Modifier.height(4.dp)
            )

            Resumo(
                registros = registros,
                texto = texto,
                texto3 = texto3,
                borda = borda,
                card = card
            )

            Spacer(
                Modifier.height(4.dp)
            )

            Controles(
                pesquisa = pesquisa,
                ordem = ordem,
                dark = dark,
                texto = texto,
                texto2 = texto2,
                borda = borda,
                card = card,
                onPesquisa = onPesquisa,
                onOrdem = onOrdem
            )

            Spacer(
                Modifier.height(5.dp)
            )
        }

        if (filtrados.isEmpty()) {

            item {

                Vazio(
                    pesquisa = pesquisa,
                    texto = texto,
                    texto3 = texto3,
                    borda = borda,
                    card = card
                )
            }

        } else {

            items(
                items = filtrados,
                key = {
                    "${it.data}_${it.dor}_${it.humor}_${filtrados.indexOf(it)}"
                }
            ) { registro ->

                RegistroCard(
                    registro = registro,
                    texto = texto,
                    texto3 = texto3,
                    borda = borda,
                    card = card,
                    onClick = {
                        onAbrir(registro)
                    }
                )
            }
        }

        item {

            Spacer(
                Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun TopoCiclo(
    texto: Color,
    texto3: Color,
    dark: Boolean,
    onAdicionar: () -> Unit,
    onApagar: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(CircleShape)
            .background(
                if (dark)
                    Color.Black
                else
                    Color.White
            )
            .padding(horizontal = 8.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        ) {

            Text(
                text = "Ciclo",
                fontFamily = Quicksand,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = texto
            )

            Text(
                text = "Acompanhe seus registros",
                fontFamily = Quicksand,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = texto3
            )
        }

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            IconButton(
                onClick = onAdicionar,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Rosa,
                                Rosa2
                            )
                        )
                    )
            ) {

                Icon(
                    painter = painterResource(
                        R.drawable.ic_add
                    ),
                    contentDescription =
                        "Adicionar registro",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = onApagar,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (dark)
                            Color.White.copy(alpha = .055f)
                        else
                            Color.Black.copy(alpha = .055f)
                    )
                    .border(
                        1.dp,
                        if (dark)
                            Color.White.copy(alpha = .10f)
                        else
                            Color.Black.copy(alpha = .10f),
                        CircleShape
                    )
            ) {

                Icon(
                    painter = painterResource(
                        R.drawable.ic_delete_outline
                    ),
                    contentDescription =
                        "Apagar todos os registros",
                    tint = texto,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

@Composable
private fun Resumo(
    registros: List<CicloRegistro>,
    texto: Color,
    texto3: Color,
    borda: Color,
    card: Color
) {

    val media =
        if (registros.isEmpty()) {
            0.0
        } else {

            registros.sumOf {
                it.dor
            }.toDouble() /
                registros.size
        }

    val ultimo =
        registros
            .maxByOrNull {
                it.data
            }
            ?.data
            ?.let(::formatarData)
            ?: "—"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {

        ResumoCard(
            modifier = Modifier.weight(1f),
            label = "Registros",
            valor = registros.size.toString(),
            detalhe = "registrados",
            texto = texto,
            texto3 = texto3,
            borda = borda,
            card = card
        )

        ResumoCard(
            modifier = Modifier.weight(1f),
            label = "Dor média",
            valor = "%.1f".format(media),
            detalhe = "de 10",
            texto = texto,
            texto3 = texto3,
            borda = borda,
            card = card
        )

        ResumoCard(
            modifier = Modifier.weight(1f),
            label = "Último",
            valor = ultimo,
            detalhe = "registro",
            texto = texto,
            texto3 = texto3,
            borda = borda,
            card = card,
            valorPequeno = true
        )
    }
}

@Composable
private fun ResumoCard(
    modifier: Modifier,
    label: String,
    valor: String,
    detalhe: String,
    texto: Color,
    texto3: Color,
    borda: Color,
    card: Color,
    valorPequeno: Boolean = false
) {

    Box(
        modifier = modifier
            .height(88.dp)
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(card)
            .border(
                1.dp,
                borda,
                RoundedCornerShape(22.dp)
            )
            .padding(14.dp)
    ) {

        Column {

            Text(
                text = label,
                fontFamily = Quicksand,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = texto3
            )

            Text(
                text = valor,
                fontFamily = Quicksand,
                fontSize =
                    if (valorPequeno)
                        10.sp
                    else
                        23.sp,
                fontWeight = FontWeight.ExtraBold,
                color = texto,
                modifier = Modifier.padding(
                    top = 6.dp
                )
            )

            Text(
                text = detalhe,
                fontFamily = Quicksand,
                fontSize = 9.sp,
                color = texto3
            )
        }
    }
}

@Composable
private fun Controles(
    pesquisa: String,
    ordem: Ordem,
    dark: Boolean,
    texto: Color,
    texto2: Color,
    borda: Color,
    card: Color,
    onPesquisa: (String) -> Unit,
    onOrdem: (Ordem) -> Unit
) {

    var aberto by remember {
        mutableStateOf(false)
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(9.dp)
    ) {

        OutlinedTextField(
            value = pesquisa,
            onValueChange = onPesquisa,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {

                Text(
                    "Pesquisar registros...",
                    fontFamily = Quicksand,
                    color = texto2
                )
            },
            leadingIcon = {

                Icon(
                    painter = painterResource(
                        R.drawable.ic_search
                    ),
                    contentDescription = null,
                    tint = texto2
                )
            },
            shape = RoundedCornerShape(999.dp)
        )

        Box {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clickable {
                        aberto = true
                    },
                shape =
                    RoundedCornerShape(999.dp),
                color =
                    if (dark)
                        Color(0xFF111111)
                    else
                        Color.White,
                border =
                    BorderStroke(
                        1.dp,
                        borda
                    )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp),
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text = when (ordem) {

                            Ordem.NOVO ->
                                "Mais recentes"

                            Ordem.ANTIGO ->
                                "Mais antigos"

                            Ordem.DOR ->
                                "Maior dor"
                        },
                        fontFamily = Quicksand,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = texto
                    )

                    Icon(
                        painter = painterResource(
                            R.drawable.ic_expand_more
                        ),
                        contentDescription = null,
                        tint = Rosa3,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = aberto,
                onDismissRequest = {
                    aberto = false
                },
                modifier = Modifier
                    .fillMaxWidth(.5f)
            ) {

                DropdownMenuItem(
                    text = {
                        Text(
                            "Mais recentes",
                            fontFamily = Quicksand
                        )
                    },
                    onClick = {

                        onOrdem(Ordem.NOVO)
                        aberto = false
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Mais antigos",
                            fontFamily = Quicksand
                        )
                    },
                    onClick = {

                        onOrdem(Ordem.ANTIGO)
                        aberto = false
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Maior dor",
                            fontFamily = Quicksand
                        )
                    },
                    onClick = {

                        onOrdem(Ordem.DOR)
                        aberto = false
                    }
                )
            }
        }
    }
}

@Composable
private fun Vazio(
    pesquisa: String,
    texto: Color,
    texto3: Color,
    borda: Color,
    card: Color
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(28.dp)
            )
            .background(card)
            .border(
                1.dp,
                borda,
                RoundedCornerShape(28.dp)
            )
            .padding(
                vertical = 45.dp,
                horizontal = 20.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                if (pesquisa.isNotBlank())
                    "🔎"
                else
                    "🌸",
            fontSize = 36.sp
        )

        Spacer(
            Modifier.height(10.dp)
        )

        Text(
            text =
                if (pesquisa.isNotBlank())
                    "Nenhum resultado"
                else
                    "Nenhum registro ainda",
            fontFamily = Quicksand,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = texto
        )

        Spacer(
            Modifier.height(5.dp)
        )

        Text(
            text =
                if (pesquisa.isNotBlank())
                    "Tente pesquisar por outra data ou informação."
                else
                    "Toque no botão + para criar seu primeiro registro.",
            fontFamily = Quicksand,
            fontSize = 11.sp,
            color = texto3
        )
    }
}

@Composable
private fun RegistroCard(
    registro: CicloRegistro,
    texto: Color,
    texto3: Color,
    borda: Color,
    card: Color,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(
                RoundedCornerShape(26.dp)
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        card,
                        card.copy(alpha = .5f)
                    )
                )
            )
            .border(
                1.dp,
                borda,
                RoundedCornerShape(26.dp)
            )
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 15.dp,
                vertical = 14.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(
                    RoundedCornerShape(19.dp)
                )
                .background(
                    Rosa.copy(alpha = .20f)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    emojiHumor(
                        registro.humor
                    ),
                fontSize = 26.sp
            )
        }

        Spacer(
            Modifier.width(14.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text =
                    formatarData(
                        registro.data
                    ),
                fontFamily = Quicksand,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = texto
            )

            Text(
                text =
                    nomeHumor(
                        registro.humor
                    ),
                fontFamily = Quicksand,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = texto3
            )
        }

        Column(
            horizontalAlignment =
                Alignment.End,
            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        texto.copy(alpha = .08f)
                    )
                    .padding(
                        horizontal = 9.dp,
                        vertical = 5.dp
                    )
            ) {

                Text(
                    text =
                        "Dor ${registro.dor}/10",
                    fontFamily = Quicksand,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color =
                        texto2Safe(texto)
                )
            }
        }
    }
}

private fun texto2Safe(
    texto: Color
): Color {
    return texto.copy(
        alpha = .75f
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovoRegistroDialog(
    dark: Boolean,
    texto: Color,
    texto2: Color,
    borda: Color,
    card: Color,
    onFechar: () -> Unit,
    onSalvar: (CicloRegistro) -> Unit
) {

    val ctx = LocalContext.current

    var data by remember {
        mutableStateOf(hojeISO())
    }

    var dor by remember {
        mutableStateOf(0)
    }

    var humor by remember {
        mutableStateOf("feliz")
    }

    var agua by remember { mutableStateOf(false) }
    var cafe by remember { mutableStateOf(false) }
    var chocolate by remember { mutableStateOf(false) }
    var exercicio by remember { mutableStateOf(false) }
    var sonoRuim by remember { mutableStateOf(false) }
    var apetite by remember { mutableStateOf(false) }

    var cansaco by remember { mutableStateOf(false) }
    var irritacao by remember { mutableStateOf(false) }
    var ansiedade by remember { mutableStateOf(false) }
    var colica by remember { mutableStateOf(false) }
    var dorCabeca by remember { mutableStateOf(false) }
    var inchaco by remember { mutableStateOf(false) }
    var acne by remember { mutableStateOf(false) }
    var nausea by remember { mutableStateOf(false) }
    var tontura by remember { mutableStateOf(false) }
    var energiaBaixa by remember { mutableStateOf(false) }

    var estresse by remember { mutableStateOf(false) }
    var tristeza by remember { mutableStateOf(false) }
    var felicidade by remember { mutableStateOf(false) }
    var sensibilidade by remember { mutableStateOf(false) }
    var concentracao by remember { mutableStateOf(false) }
    var libido by remember { mutableStateOf(false) }

    var mostrarData by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onFechar,
        modifier = Modifier.fillMaxWidth(),
        title = {

            Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
) {

    OutlinedTextField(
        value = formatarData(data),
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        label = {
            Text(
                "Data",
                fontFamily = Quicksand
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    mostrarData = true
                }
            ) {
                Text(
                    text = "📅",
                    fontSize = 20.sp
                )
            }
        },
        shape = RoundedCornerShape(17.dp)
    )

    OutlinedTextField(
        value = dor.toString(),
        onValueChange = { valor ->

            dor = valor
                .filter { it.isDigit() }
                .toIntOrNull()
                ?.coerceIn(0, 10)
                ?: 0
        },
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        label = {
            Text(
                "Dor — 0 a 10",
                fontFamily = Quicksand
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        shape = RoundedCornerShape(17.dp)
    )
}

                Text(
                    text = "Como você se sentiu?",
                    fontFamily = Quicksand,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = texto2
                )

                HumorOpcoes(
                    selecionado = humor,
                    onSelecionar = {
                        humor = it
                    },
                    borda = borda,
                    card = card,
                    texto = texto
                )

                CheckSection(
                    titulo = "Hábitos",
                    items = listOf(
                        "💧 Bebeu água" to agua,
                        "☕ Tomou café" to cafe,
                        "🍫 Comeu chocolate" to chocolate,
                        "🏃 Fez exercício" to exercicio,
                        "😴 Sono ruim" to sonoRuim,
                        "🍽️ Apetite alterado" to apetite
                    ),
                    onChange = { index, value ->

                        when (index) {
                            0 -> agua = value
                            1 -> cafe = value
                            2 -> chocolate = value
                            3 -> exercicio = value
                            4 -> sonoRuim = value
                            5 -> apetite = value
                        }
                    },
                    borda = borda,
                    card = card,
                    texto = texto
                )

                CheckSection(
                    titulo = "Sintomas",
                    items = listOf(
                        "😮‍💨 Cansaço" to cansaco,
                        "😤 Irritação" to irritacao,
                        "🌫️ Ansiedade" to ansiedade,
                        "🌀 Cólica" to colica,
                        "🤕 Dor de cabeça" to dorCabeca,
                        "🫧 Inchaço" to inchaco,
                        "✨ Acne" to acne,
                        "🤢 Náusea" to nausea,
                        "🌀 Tontura" to tontura,
                        "🔋 Energia baixa" to energiaBaixa
                    ),
                    onChange = { index, value ->

                        when (index) {
                            0 -> cansaco = value
                            1 -> irritacao = value
                            2 -> ansiedade = value
                            3 -> colica = value
                            4 -> dorCabeca = value
                            5 -> inchaco = value
                            6 -> acne = value
                            7 -> nausea = value
                            8 -> tontura = value
                            9 -> energiaBaixa = value
                        }
                    },
                    borda = borda,
                    card = card,
                    texto = texto
                )

                CheckSection(
                    titulo =
                        "Emocional e concentração",
                    items = listOf(
                        "⚡ Estresse" to estresse,
                        "🌧️ Tristeza" to tristeza,
                        "☀️ Felicidade" to felicidade,
                        "💗 Sensibilidade" to sensibilidade,
                        "🧠 Concentração ruim" to concentracao,
                        "💫 Libido alterada" to libido
                    ),
                    onChange = { index, value ->

                        when (index) {
                            0 -> estresse = value
                            1 -> tristeza = value
                            2 -> felicidade = value
                            3 -> sensibilidade = value
                            4 -> concentracao = value
                            5 -> libido = value
                        }
                    },
                    borda = borda,
                    card = card,
                    texto = texto
                )
            }
        },
        confirmButton = {

            Button(
    onClick = {
        onSalvar(
            CicloRegistro(
                data = data,
                dor = dor,
                humor = humor,
                agua = agua,
                cafe = cafe,
                chocolate = chocolate,
                exercicio = exercicio,
                sonoRuim = sonoRuim,
                apetite = apetite,
                cansaco = cansaco,
                irritacao = irritacao,
                ansiedade = ansiedade,
                colica = colica,
                dorCabeca = dorCabeca,
                inchaco = inchaco,
                acne = acne,
                nausea = nausea,
                tontura = tontura,
                energiaBaixa = energiaBaixa,
                estresse = estresse,
                tristeza = tristeza,
                felicidade = felicidade,
                sensibilidade = sensibilidade,
                concentracao = concentracao,
                libido = libido
            )
        )
    },
    modifier = Modifier.fillMaxWidth(),
    shape = CircleShape,
    colors = ButtonDefaults.buttonColors(
        containerColor = Rosa2,
        contentColor = Color.White
    )
) {
    Text(
        text = "Salvar registro",
        fontFamily = Quicksand,
        fontWeight = FontWeight.ExtraBold
    )
}
        },
        dismissButton = null
    )

    if (mostrarData) {

        val partes = data.split("-")

        val ano =
            partes
                .getOrNull(0)
                ?.toIntOrNull()
                ?: Calendar.getInstance()
                    .get(Calendar.YEAR)

        val mes =
            (
                partes
                    .getOrNull(1)
                    ?.toIntOrNull()
                    ?: 1
            ) - 1

        val dia =
            partes
                .getOrNull(2)
                ?.toIntOrNull()
                ?: 1

        DatePickerDialog(
            ctx,
            { _, a, m, d ->

                data =
                    "%04d-%02d-%02d".format(
                        a,
                        m + 1,
                        d
                    )

                mostrarData = false
            },
            ano,
            mes,
            dia
        ).apply {

            setOnCancelListener {
                mostrarData = false
            }

        }.show()
    }
}

@Composable
private fun HumorOpcoes(
    selecionado: String,
    onSelecionar: (String) -> Unit,
    borda: Color,
    card: Color,
    texto: Color
) {

    val opcoes =
        listOf(
            "feliz" to "😊 Feliz",
            "neutro" to "😐 Neutro",
            "desconfortavel" to "😣 Desconfortável",
            "triste" to "😢 Triste"
        )

    Column(
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        opcoes.chunked(2).forEach { linha ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                linha.forEach {
                    (valor, nome) ->

                    val ativa =
                        selecionado == valor

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(
                                RoundedCornerShape(17.dp)
                            )
                            .background(
                                if (ativa)
                                    Rosa.copy(alpha = .16f)
                                else
                                    card
                            )
                            .border(
                                1.dp,
                                if (ativa)
                                    Rosa.copy(alpha = .5f)
                                else
                                    borda,
                                RoundedCornerShape(17.dp)
                            )
                            .clickable {
                                onSelecionar(valor)
                            }
                            .padding(
                                horizontal = 10.dp,
                                vertical = 9.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                                nome.substring(
                                    0,
                                    2
                                ),
                            fontSize = 19.sp
                        )

                        Spacer(
                            Modifier.width(7.dp)
                        )

                        Text(
                            text =
                                nome.substring(3),
                            fontFamily = Quicksand,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = texto
                        )

                        if (ativa) {

                            Spacer(
                                Modifier.weight(1f)
                            )

                            Icon(
                                painter =
                                    painterResource(
                                        R.drawable.ic_check
                                    ),
                                contentDescription = null,
                                tint = Rosa2,
                                modifier =
                                    Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (linha.size == 1) {

                    Spacer(
                        Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckSection(
    titulo: String,
    items: List<Pair<String, Boolean>>,
    onChange: (Int, Boolean) -> Unit,
    borda: Color,
    card: Color,
    texto: Color
) {

    Column {

        Text(
            text = titulo,
            fontFamily = Quicksand,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                bottom = 8.dp
            )
        )

        items
            .chunked(2)
            .forEachIndexed {
                linhaIndex,
                linha ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(bottom = 8.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    linha.forEachIndexed {
                        colunaIndex,
                        item ->

                        val indice =
                            linhaIndex * 2 +
                                colunaIndex

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(
                                    RoundedCornerShape(15.dp)
                                )
                                .background(card)
                                .border(
                                    1.dp,
                                    borda,
                                    RoundedCornerShape(15.dp)
                                )
                                .clickable {

                                    onChange(
                                        indice,
                                        !item.second
                                    )
                                }
                                .padding(
                                    horizontal = 7.dp,
                                    vertical = 5.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked =
                                    item.second,
                                onCheckedChange = {
                                    onChange(
                                        indice,
                                        it
                                    )
                                }
                            )

                            Text(
                                text = item.first,
                                fontFamily = Quicksand,
                                fontSize = 10.sp,
                                fontWeight =
                                    FontWeight.SemiBold,
                                color = texto
                            )
                        }
                    }

                    if (linha.size == 1) {

                        Spacer(
                            modifier =
                                Modifier.weight(1f)
                        )
                    }
                }
            }
    }
}

@Composable
private fun TelaDetalhes(
    registro: CicloRegistro,
    dark: Boolean,
    texto: Color,
    texto2: Color,
    texto3: Color,
    borda: Color,
    card: Color,
    onBack: () -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            Spacer(
                Modifier.height(4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (dark)
                                Color.White.copy(alpha = .075f)
                            else
                                Color.Black.copy(alpha = .055f)
                        )
                ) {

                    Icon(
                        painter = painterResource(
                            R.drawable.ic_arrow_back
                        ),
                        contentDescription = "Voltar",
                        tint = texto,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(
                    Modifier.width(12.dp)
                )

                Column {

                    Text(
                        text = formatarData(
                            registro.data
                        ),
                        fontFamily = Quicksand,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = texto
                    )

                    Text(
                        text = "${emojiHumor(registro.humor)} ${
                            nomeHumor(
                                registro.humor
                            )
                        }",
                        fontFamily = Quicksand,
                        fontSize = 10.sp,
                        color = texto3
                    )
                }
            }
        }

        item {

            DorDestaque(
                dor = registro.dor,
                texto = texto,
                texto2 = texto2,
                texto3 = texto3,
                borda = borda
            )
        }

        item {

            GraficoCard(
                titulo = "Consumo",
                subtitulo = "Itens registrados neste dia",
                texto = texto,
                texto2 = texto2,
                borda = borda,
                card = card
            ) {

                IndicadoresBooleanos(
                    itens = listOf(
                        "Água" to registro.agua,
                        "Café" to registro.cafe,
                        "Chocolate" to registro.chocolate
                    ),
                    texto = texto,
                    texto2 = texto2,
                    dark = dark
                )
            }
        }

        item {

            GraficoCard(
                titulo = "Sintomas",
                subtitulo = "Sintomas registrados neste dia",
                texto = texto,
                texto2 = texto2,
                borda = borda,
                card = card
            ) {

                IndicadoresBooleanos(
                    itens = listOf(
                        "Cólica" to registro.colica,
                        "Dor de cabeça" to registro.dorCabeca,
                        "Inchaço" to registro.inchaco,
                        "Náusea" to registro.nausea,
                        "Tontura" to registro.tontura
                    ),
                    texto = texto,
                    texto2 = texto2,
                    dark = dark
                )
            }
        }

        item {

            GraficoCard(
                titulo = "Estado emocional",
                subtitulo = "Como você se sentiu neste dia",
                texto = texto,
                texto2 = texto2,
                borda = borda,
                card = card
            ) {

                IndicadoresBooleanos(
                    itens = listOf(
                        "Ansiedade" to registro.ansiedade,
                        "Estresse" to registro.estresse,
                        "Tristeza" to registro.tristeza,
                        "Felicidade" to registro.felicidade,
                        "Sensibilidade" to registro.sensibilidade
                    ),
                    texto = texto,
                    texto2 = texto2,
                    dark = dark
                )
            }
        }

        item {

            GraficoCard(
                titulo = "Corpo",
                subtitulo = "Alterações físicas registradas",
                texto = texto,
                texto2 = texto2,
                borda = borda,
                card = card
            ) {

                IndicadoresBooleanos(
                    itens = listOf(
                        "Acne" to registro.acne,
                        "Cansaço" to registro.cansaco,
                        "Energia baixa" to registro.energiaBaixa,
                        "Apetite alterado" to registro.apetite,
                        "Sono ruim" to registro.sonoRuim
                    ),
                    texto = texto,
                    texto2 = texto2,
                    dark = dark
                )
            }
        }

        item {

            GraficoCard(
                titulo = "Rotina",
                subtitulo = "Hábitos e estado geral do dia",
                texto = texto,
                texto2 = texto2,
                borda = borda,
                card = card
            ) {

                IndicadoresRotina(
                    registro = registro,
                    texto = texto,
                    texto2 = texto2,
                    texto3 = texto3,
                    dark = dark
                )
            }
        }

        item {

            Spacer(
                Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun DorDestaque(
    dor: Int,
    texto: Color,
    texto2: Color,
    texto3: Color,
    borda: Color
) {

    val intensidade = when {
        dor == 0 -> "Sem dor"
        dor <= 3 -> "Dor leve"
        dor <= 6 -> "Dor moderada"
        dor <= 8 -> "Dor forte"
        else -> "Dor muito forte"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(
                RoundedCornerShape(28.dp)
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        Rosa.copy(alpha = .22f),
                        texto.copy(alpha = .04f)
                    )
                )
            )
            .border(
                1.dp,
                Rosa.copy(alpha = .20f),
                RoundedCornerShape(28.dp)
            )
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            Text(
                text = "Intensidade da dor",
                fontFamily = Quicksand,
                fontSize = 11.sp,
                color = texto2
            )

            Row(
                verticalAlignment = Alignment.Bottom
            ) {

                Text(
                    text = dor.toString(),
                    fontFamily = Quicksand,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = texto
                )

                Spacer(
                    Modifier.width(5.dp)
                )

                Text(
                    text = "/ 10",
                    fontFamily = Quicksand,
                    fontSize = 11.sp,
                    color = texto3,
                    modifier = Modifier.padding(
                        bottom = 8.dp
                    )
                )
            }

            Text(
                text = intensidade,
                fontFamily = Quicksand,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Rosa2
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(.43f)
        ) {

            Text(
                text = "${dor * 10}%",
                fontFamily = Quicksand,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = texto2
            )

            Spacer(
                Modifier.height(6.dp)
            )

            BarraProgresso(
                valor = dor / 10f,
                texto = texto
            )
        }
    }
}

@Composable
private fun GraficoCard(
    titulo: String,
    subtitulo: String,
    texto: Color,
    texto2: Color,
    borda: Color,
    card: Color,
    content: @Composable () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(25.dp)
            )
            .background(card)
            .border(
                1.dp,
                borda,
                RoundedCornerShape(25.dp)
            )
            .padding(14.dp)
    ) {

        Text(
            text = titulo,
            fontFamily = Quicksand,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = texto
        )

        Spacer(
            Modifier.height(2.dp)
        )

        Text(
            text = subtitulo,
            fontFamily = Quicksand,
            fontSize = 9.sp,
            color = texto2.copy(alpha = .65f)
        )

        Spacer(
            Modifier.height(12.dp)
        )

        content()
    }
}

@Composable
private fun IndicadoresBooleanos(
    itens: List<Pair<String, Boolean>>,
    texto: Color,
    texto2: Color,
    dark: Boolean
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        itens.forEach { (nome, ativo) ->

            IndicadorBooleano(
                nome = nome,
                ativo = ativo,
                texto = texto,
                texto2 = texto2,
                dark = dark
            )
        }
    }
}

@Composable
private fun IndicadorBooleano(
    nome: String,
    ativo: Boolean,
    texto: Color,
    texto2: Color,
    dark: Boolean
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = nome,
            fontFamily = Quicksand,
            fontSize = 10.sp,
            fontWeight = if (ativo)
                FontWeight.Bold
            else
                FontWeight.Normal,
            color = if (ativo)
                texto
            else
                texto2.copy(alpha = .65f),
            modifier = Modifier.width(125.dp)
        )

        Spacer(
            Modifier.width(8.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(CircleShape)
                .background(
                    if (dark)
                        Color.White.copy(alpha = .08f)
                    else
                        Color.Black.copy(alpha = .07f)
                )
        ) {

            if (ativo) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Rosa3,
                                    Rosa2
                                )
                            )
                        )
                )
            }
        }

        Spacer(
            Modifier.width(10.dp)
        )

        Text(
            text = if (ativo) "Sim" else "Não",
            fontFamily = Quicksand,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (ativo)
                Rosa2
            else
                texto2.copy(alpha = .5f),
            modifier = Modifier.width(27.dp)
        )
    }
}

@Composable
private fun IndicadoresRotina(
    registro: CicloRegistro,
    texto: Color,
    texto2: Color,
    texto3: Color,
    dark: Boolean
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        IndicadorBooleano(
            nome = "Exercício",
            ativo = registro.exercicio,
            texto = texto,
            texto2 = texto2,
            dark = dark
        )

        IndicadorBooleano(
            nome = "Concentração ruim",
            ativo = registro.concentracao,
            texto = texto,
            texto2 = texto2,
            dark = dark
        )

        IndicadorBooleano(
            nome = "Libido alterada",
            ativo = registro.libido,
            texto = texto,
            texto2 = texto2,
            dark = dark
        )

        IndicadorBooleano(
            nome = "Irritação",
            ativo = registro.irritacao,
            texto = texto,
            texto2 = texto2,
            dark = dark
        )

        Spacer(
            Modifier.height(3.dp)
        )

        Text(
            text = "Dor",
            fontFamily = Quicksand,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = texto
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "${registro.dor}/10",
                fontFamily = Quicksand,
                fontSize = 9.sp,
                color = texto2,
                modifier = Modifier.width(45.dp)
            )

            BarraProgresso(
                valor = registro.dor / 10f,
                texto = texto,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = when {
                registro.dor == 0 ->
                    "Nenhuma dor registrada"
                registro.dor <= 3 ->
                    "Intensidade baixa"
                registro.dor <= 6 ->
                    "Intensidade moderada"
                else ->
                    "Intensidade alta"
            },
            fontFamily = Quicksand,
            fontSize = 9.sp,
            color = texto3
        )
    }
}

@Composable
private fun BarraProgresso(
    valor: Float,
    texto: Color,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .height(8.dp)
            .clip(CircleShape)
            .background(
                texto.copy(alpha = .09f)
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth(
                    valor.coerceIn(
                        0f,
                        1f
                    )
                )
                .fillMaxHeight()
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Rosa3,
                            Rosa2
                        )
                    )
                )
        )
    }
}