package com.mulheres

import androidx.compose.foundation.layout.navigationBarsPadding
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.util.UUID

class ArquivoActivity : ComponentActivity() {

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

        window.navigationBarColor =
            AndroidColor.TRANSPARENT

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q
        ) {
            window.isNavigationBarContrastEnforced =
                false
        }

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            ArquivoSeguro()
        }
    }
}

private val Quicksand = FontFamily(
    Font(R.font.quicksand, FontWeight.Normal),
    Font(R.font.quicksand, FontWeight.Medium),
    Font(R.font.quicksand, FontWeight.SemiBold),
    Font(R.font.quicksand, FontWeight.Bold),
    Font(R.font.quicksand, FontWeight.ExtraBold)
)

private val FundoEscuro =
    Color(0xFF000000)

private val TextoEscuro =
    Color(0xFFF8F8FA)

private val TextoSuaveEscuro =
    Color(0xFFB7B7C0)

private val TextoMutedEscuro =
    Color(0xFF777782)

private val CartaoEscuro =
    Color(0xFF000000)

private val BordaCartaoEscuro =
    Color.White.copy(alpha = 0.07f)

private val CirculoEscuro =
    Color.White.copy(alpha = 0.035f)

private val SetaEscuro =
    Color(0xFF777782)

private val FundoClaro =
    Color(0xFFF7F7FA)

private val TextoClaro =
    Color(0xFF17171B)

private val TextoSuaveClaro =
    Color(0xFF666671)

private val TextoMutedClaro =
    Color(0xFF85858F)

private val CartaoClaro =
    Color.White

private val BordaCartaoClaro =
    Color(0x14000000)

private val CirculoClaro =
    Color(0x08000000)

private val SetaClaro =
    Color(0xFF8A8A94)

private val Rosa =
    Color(0xFFFF3F82)

private val RoxoInicio =
    Color(0xFF8B5CF6)

private val RoxoFim =
    Color(0xFF6425D9)

private data class ArquivoSeguroItem(
    val arquivo: File,
    val nome: String,
    val tamanho: Long
)

@Composable
private fun ArquivoSeguro() {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val dark =
        isSystemInDarkTheme()

    val fundo =
        if (dark)
            FundoEscuro
        else
            FundoClaro

    val arquivos =
        remember {
            mutableStateListOf<ArquivoSeguroItem>()
        }

    var processando by remember {
        mutableStateOf(false)
    }

    var textoProcessando by remember {
        mutableStateOf("Protegendo arquivo…")
    }

    var mensagem by remember {
        mutableStateOf<String?>(null)
    }

    var arquivoParaExportar by remember {
        mutableStateOf<ArquivoSeguroItem?>(null)
    }

    val diretorio =
        remember {
            File(
                context.filesDir,
                "arquivo_seguro"
            ).apply {
                mkdirs()
            }
        }

    fun atualizarArquivos() {

        arquivos.clear()

        diretorio
            .listFiles()
            ?.filter {
                it.isFile &&
                    it.extension == "seguro"
            }
            ?.forEach { file ->

                val nome =
                    file.nameWithoutExtension
                        .substringAfter(
                            "__",
                            "Arquivo protegido"
                        )

                arquivos.add(
                    ArquivoSeguroItem(
                        arquivo = file,
                        nome = nome,
                        tamanho = file.length()
                    )
                )
            }
    }

    LaunchedEffect(Unit) {
        atualizarArquivos()
    }

    val seletorArquivos =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->

            if (uris.isEmpty()) {
                return@rememberLauncherForActivityResult
            }

            processando = true
            textoProcessando = "Protegendo arquivo…"
            mensagem = null

            CoroutineScope(
                SupervisorJob() +
                    Dispatchers.Main.immediate
            ).launch {

                val resultado =
                    withContext(
                        Dispatchers.IO
                    ) {

                        val cripto =
                            Cripto(context)

                        var quantidade = 0

                        uris.forEach { uri ->

                            var temporario: File? = null
                            var pacote: File? = null

                            try {

                                val nomeOriginal =
                                    obterNomeArquivo(
                                        context,
                                        uri
                                    )

                                val mimeType =
                                    context.contentResolver
                                        .getType(uri)
                                        ?: "application/octet-stream"

                                temporario =
                                    File(
                                        context.cacheDir,
                                        "arquivo_${UUID.randomUUID()}.tmp"
                                    )

                                pacote =
                                    File(
                                        context.cacheDir,
                                        "pacote_${UUID.randomUUID()}.tmp"
                                    )

                                val input =
                                    context.contentResolver
                                        .openInputStream(uri)

                                if (input == null) {
                                    return@forEach
                                }

                                input.use { entrada ->

                                    temporario
                                        .outputStream()
                                        .use { output ->

                                            entrada.copyTo(
                                                output
                                            )
                                        }
                                }

                                DataOutputStream(
                                    pacote.outputStream()
                                ).use { output ->

                                    output.writeUTF(
                                        nomeOriginal
                                    )

                                    output.writeUTF(
                                        mimeType
                                    )

                                    temporario
                                        .inputStream()
                                        .use { inputStream ->

                                            inputStream.copyTo(
                                                output
                                            )
                                        }
                                }

                                val nomeLimpo =
                                    nomeOriginal
                                        .replace(
                                            Regex(
                                                """[\\/:*?"<>|]"""
                                            ),
                                            "_"
                                        )
                                        .take(180)
                                        .ifBlank {
                                            "Arquivo"
                                        }

                                val nomeSeguro =
                                    "${UUID.randomUUID()}__${nomeLimpo}.seguro"

                                val arquivoSeguro =
                                    File(
                                        diretorio,
                                        nomeSeguro
                                    )

                                cripto.criptografarArquivo(
                                    pacote,
                                    arquivoSeguro
                                )

                                quantidade++

                            } catch (
                                e: Exception
                            ) {

                                e.printStackTrace()

                            } finally {

                                temporario?.delete()
                                pacote?.delete()
                            }
                        }

                        quantidade
                    }

                atualizarArquivos()

                processando = false

                mensagem =
                    when {
                        resultado == 0 ->
                            "Não foi possível proteger os arquivos."

                        resultado == 1 ->
                            "Arquivo protegido com sucesso."

                        else ->
                            "$resultado arquivos protegidos com sucesso."
                    }
            }
        }

    val exportador =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(
                "application/octet-stream"
            )
        ) { uri ->

            val item =
                arquivoParaExportar

            if (
                uri == null ||
                item == null
            ) {
                arquivoParaExportar = null
                return@rememberLauncherForActivityResult
            }

            processando = true
            textoProcessando = "Baixando arquivo…"
            mensagem = null

            CoroutineScope(
                SupervisorJob() +
                    Dispatchers.Main.immediate
            ).launch {

                val resultado =
                    withContext(
                        Dispatchers.IO
                    ) {

                        val cripto =
                            Cripto(context)

                        val temporario =
                            File(
                                context.cacheDir,
                                "descriptografado_${UUID.randomUUID()}.tmp"
                            )

                        try {

                            cripto.descriptografarArquivo(
                                item.arquivo,
                                temporario
                            )

                            DataInputStream(
                                temporario.inputStream()
                            ).use { input ->

                                input.readUTF()
                                input.readUTF()

                                val output =
                                    context.contentResolver
                                        .openOutputStream(uri)

                                if (output == null) {
                                    throw IllegalStateException(
                                        "Não foi possível criar o arquivo."
                                    )
                                }

                                output.use { destino ->

                                    input.copyTo(
                                        destino
                                    )
                                }
                            }

                            true

                        } catch (
                            e: Exception
                        ) {

                            e.printStackTrace()

                            false

                        } finally {

                            temporario.delete()
                        }
                    }

                arquivoParaExportar = null
                processando = false

                mensagem =
                    if (resultado)
                        "Arquivo exportado com sucesso."
                    else
                        "Não foi possível exportar o arquivo."
            }
        }

    CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(fundo)
        ) {

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),

                contentPadding =
                    PaddingValues(
                        start = 18.dp,
                        end = 18.dp,
                        top = 24.dp,
                        bottom = 60.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                item {

                    HeroArquivo(
                        dark = dark
                    )
                }

                item {

                    BotaoAdicionar(
                        dark = dark,
                        enabled = !processando,
                        onClick = {
                            seletorArquivos.launch(
                                arrayOf("*/*")
                            )
                        }
                    )
                }

                if (mensagem != null) {

                    item {

                        Mensagem(
                            texto = mensagem!!,
                            dark = dark
                        )
                    }
                }

                item {

                    TituloSecao(
                        titulo =
                            "Arquivos protegidos",
                        dark = dark
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )
                }

                if (
                    arquivos.isEmpty() &&
                    !processando
                ) {

                    item {

                        EstadoVazio(
                            dark = dark
                        )
                    }

                } else {

                    items(
                        items = arquivos,
                        key = {
                            it.arquivo.name
                        }
                    ) { item ->

                        CartaoArquivo(
                            item = item,
                            dark = dark,
                            enabled = !processando,
                            onExportar = {

                                arquivoParaExportar =
                                    item

                                exportador.launch(
                                    item.nome
                                )
                            },
                            onExcluir = {

                                item.arquivo.delete()

                                atualizarArquivos()

                                mensagem =
                                    "Arquivo removido."
                            }
                        )
                    }
                }
            }

            if (processando) {

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(
                                    alpha = 0.30f
                                )
                            )
                            .padding(
                                horizontal = 18.dp,
                                vertical = 18.dp
                            )
                            .navigationBarsPadding(),

                    contentAlignment =
                        Alignment.BottomCenter
                ) {

                    Box(
                        modifier =
                            Modifier
                                .clip(
                                    RoundedCornerShape(
                                        24.dp
                                    )
                                )
                                .background(
                                    if (dark)
                                        Color(0xFF111111)
                                    else
                                        Color.White
                                )
                                .padding(
                                    horizontal = 28.dp,
                                    vertical = 22.dp
                                )
                    ) {

                        Text(
                            text =
                                textoProcessando,

                            color =
                                if (dark)
                                    TextoEscuro
                                else
                                    TextoClaro,

                            fontFamily =
                                Quicksand,

                            fontSize =
                                14.sp,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroArquivo(
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

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 18.dp,
                    bottom = 20.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            modifier =
                Modifier
                    .size(92.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape =
                            RoundedCornerShape(
                                28.dp
                            )
                    )
                    .clip(
                        RoundedCornerShape(
                            28.dp
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
                                alpha = 0.20f
                            ),
                        shape =
                            RoundedCornerShape(
                                28.dp
                            )
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            androidx.compose.foundation.Image(
                painter =
                    painterResource(
                        R.drawable.ic_lock
                    ),

                contentDescription =
                    null,

                modifier =
                    Modifier.size(46.dp),

                contentScale =
                    ContentScale.Fit
            )
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(
            text = "Arquivo seguro",

            color = texto,

            fontFamily =
                Quicksand,

            fontSize = 32.sp,

            fontWeight =
                FontWeight.ExtraBold,

            letterSpacing =
                (-1.0).sp
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                "Seus arquivos protegidos em um espaço privado.",

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp
                    ),

            color = textoSuave,

            fontFamily =
                Quicksand,

            fontSize = 14.sp,

            fontWeight =
                FontWeight.Medium,

            lineHeight = 21.sp,

            textAlign =
                TextAlign.Center
        )
    }
}

@Composable
private fun BotaoAdicionar(
    dark: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {

    val fundo =
        if (dark)
            Color(0xFF17121A)
        else
            Color(0xFFFFF1F6)

    val texto =
        if (dark)
            TextoEscuro
        else
            TextoClaro

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        20.dp
                    )
                )
                .background(fundo)
                .border(
                    1.dp,
                    Rosa.copy(
                        alpha = 0.22f
                    ),
                    RoundedCornerShape(
                        20.dp
                    )
                )
                .clickable(
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(
                    horizontal = 18.dp,
                    vertical = 17.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Rosa.copy(
                            alpha = 0.12f
                        )
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

                tint = Rosa,

                modifier =
                    Modifier.size(20.dp)
            )
        }

        Spacer(
            modifier =
                Modifier.width(14.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text =
                    "Adicionar arquivos",

                color = texto,

                fontFamily =
                    Quicksand,

                fontSize = 15.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )

            Text(
                text =
                    "Escolha arquivos para proteger",

                color =
                    if (dark)
                        TextoMutedEscuro
                    else
                        TextoMutedClaro,

                fontFamily =
                    Quicksand,

                fontSize = 12.sp,

                fontWeight =
                    FontWeight.Medium
            )
        }

        Icon(
            painter =
                painterResource(
                    R.drawable.ic_arrow
                ),

            contentDescription =
                null,

            tint =
                if (dark)
                    SetaEscuro
                else
                    SetaClaro,

            modifier =
                Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CartaoArquivo(
    item: ArquivoSeguroItem,
    dark: Boolean,
    enabled: Boolean,
    onExportar: () -> Unit,
    onExcluir: () -> Unit
) {

    val fundo =
        if (dark)
            CartaoEscuro
        else
            CartaoClaro

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

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        21.dp
                    )
                )
                .background(fundo)
                .border(
                    1.dp,
                    if (dark)
                        BordaCartaoEscuro
                    else
                        BordaCartaoClaro,
                    RoundedCornerShape(
                        21.dp
                    )
                )
                .padding(16.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(
                            RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(circulo),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    painter =
                        painterResource(
                            R.drawable.ic_lock
                        ),

                    contentDescription =
                        null,

                    tint = Rosa,

                    modifier =
                        Modifier.size(22.dp)
                )
            }

            Spacer(
                modifier =
                    Modifier.width(13.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        item.nome,

                    color = texto,

                    fontFamily =
                        Quicksand,

                    fontSize = 14.sp,

                    fontWeight =
                        FontWeight.Bold,

                    maxLines = 2
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        tamanhoFormatado(
                            item.tamanho
                        ),

                    color = descricao,

                    fontFamily =
                        Quicksand,

                    fontSize = 11.sp,

                    fontWeight =
                        FontWeight.Medium
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(13.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            AcaoArquivo(
                texto =
                    "Descriptografar e baixar",

                dark = dark,

                modifier =
                    Modifier.weight(1f),

                enabled = enabled,

                onClick =
                    onExportar
            )

            AcaoArquivo(
                texto =
                    "Excluir",

                dark = dark,

                modifier =
                    Modifier.weight(0.42f),

                enabled = enabled,

                onClick =
                    onExcluir
            )
        }
    }
}

@Composable
private fun AcaoArquivo(
    texto: String,
    dark: Boolean,
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {

    val fundo =
        if (dark)
            Color.White.copy(alpha = 0.035f)
        else
            Color.Black.copy(alpha = 0.025f)

    val cor =
        if (texto.startsWith("Des"))
            Rosa
        else
            if (dark)
                TextoMutedEscuro
            else
                TextoMutedClaro

    Box(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(fundo)
                .border(
                    1.dp,
                    cor.copy(alpha = 0.12f),
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .clickable(
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(
                    horizontal = 10.dp,
                    vertical = 11.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text = texto,

            color = cor,

            fontFamily =
                Quicksand,

            fontSize = 11.sp,

            fontWeight =
                FontWeight.Bold,

            textAlign =
                TextAlign.Center
        )
    }
}

@Composable
private fun EstadoVazio(
    dark: Boolean
) {

    val texto =
        if (dark)
            TextoSuaveEscuro
        else
            TextoSuaveClaro

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 28.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text =
                "Nenhum arquivo protegido ainda.",

            color = texto,

            fontFamily =
                Quicksand,

            fontSize = 13.sp,

            fontWeight =
                FontWeight.Medium,

            textAlign =
                TextAlign.Center
        )
    }
}

@Composable
private fun Mensagem(
    texto: String,
    dark: Boolean
) {

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        15.dp
                    )
                )
                .background(
                    Rosa.copy(
                        alpha = 0.08f
                    )
                )
                .padding(
                    horizontal = 15.dp,
                    vertical = 12.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text = texto,

            color =
                if (dark)
                    TextoSuaveEscuro
                else
                    TextoSuaveClaro,

            fontFamily =
                Quicksand,

            fontSize = 12.sp,

            fontWeight =
                FontWeight.SemiBold,

            textAlign =
                TextAlign.Center
        )
    }
}

@Composable
private fun TituloSecao(
    titulo: String,
    dark: Boolean
) {

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

        Text(
            text =
                titulo.uppercase(),

            color =
                if (dark)
                    Color(0xFF8F8F99)
                else
                    Color(0xFF74747E),

            fontFamily =
                Quicksand,

            fontSize = 10.sp,

            fontWeight =
                FontWeight.ExtraBold,

            letterSpacing =
                1.8.sp
        )
    }
}

private fun obterNomeArquivo(
    context: android.content.Context,
    uri: Uri
): String {

    var nome =
        "Arquivo"

    context.contentResolver
        .query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME
            ),
            null,
            null,
            null
        )
        ?.use { cursor ->

            if (cursor.moveToFirst()) {

                val indice =
                    cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )

                if (indice >= 0) {

                    nome =
                        cursor.getString(
                            indice
                        )
                }
            }
        }

    return nome
}

private fun tamanhoFormatado(
    bytes: Long
): String {

    if (bytes < 1024) {
        return "$bytes B"
    }

    if (bytes < 1024 * 1024) {
        return "${bytes / 1024} KB"
    }

    if (bytes < 1024 * 1024 * 1024) {
        return "${bytes / (1024 * 1024)} MB"
    }

    return "${bytes / (1024 * 1024 * 1024)} GB"
}