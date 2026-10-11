package com.mulheres

import androidx.compose.ui.text.withStyle
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/* =========================================================
   CORES
========================================================= */

private val Pink = Color(0xFFFF8EAF)
private val PinkLight = Color(0xFFFFC1D2)
private val PinkDark = Color(0xFFE96D92)

private val DarkBackground = Color(0xFF000000)
private val DarkSurface = Color(0xFF101014)
private val DarkSurface2 = Color(0xFF111116)
private val DarkBorder = Color(0x1AFFFFFF)
private val DarkText = Color.White
private val DarkMuted = Color(0x7AFFFFFF)

private val LightBackground = Color(0xFFFFFFFF)
private val LightSurface = Color.White
private val LightBorder = Color(0x18000000)
private val LightText = Color(0xFF292127)
private val LightMuted = Color(0x7A292127)

private val DarkPage = Color(0xFF17151A)
private val DarkPageBorder = Color(0x18FFFFFF)

private val LightPage = Color(0xFFFFC0D0)
private val LightPageText = Color(0xFF292127)

/* =========================================================
   ACTIVITY
========================================================= */

class DiarioActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        window.statusBarColor =
            android.graphics.Color.TRANSPARENT

        window.navigationBarColor =
            android.graphics.Color.TRANSPARENT

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q
        ) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            DiarioTheme {
                DiarioScreen()
            }
        }
    }
}

/* =========================================================
   TEMA
========================================================= */

@Composable
private fun DiarioTheme(
    content: @Composable () -> Unit
) {
    val dark = isSystemInDarkTheme()

    val colors =
        if (dark) {
            darkColorScheme(
                primary = Pink,
                secondary = PinkLight,
                background = DarkBackground,
                surface = DarkSurface,
                onBackground = DarkText,
                onSurface = DarkText
            )
        } else {
            lightColorScheme(
                primary = PinkDark,
                secondary = Pink,
                background = LightBackground,
                surface = LightSurface,
                onBackground = LightText,
                onSurface = LightText
            )
        }

    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography.copy(
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(),
            bodySmall = MaterialTheme.typography.bodySmall.copy(),
            titleLarge = MaterialTheme.typography.titleLarge.copy(),
            titleMedium = MaterialTheme.typography.titleMedium.copy(),
            titleSmall = MaterialTheme.typography.titleSmall.copy()
        ),
        content = content
    )
}

/* =========================================================
   STORAGE
========================================================= */

private class DiarioStorage(
    context: Context
) {
    private val cripto = Cripto(context)

    fun loadPages(): MutableList<String> {
        val saved = cripto.carregar("diario")

        if (saved.isBlank()) {
            return mutableListOf("")
        }

        return try {
            val array = JSONArray(saved)
            val result = mutableListOf<String>()

            for (i in 0 until array.length()) {
                result.add(array.optString(i, ""))
            }

            if (result.isEmpty()) {
                result.add("")
            }

            result
        } catch (_: Exception) {
            mutableListOf("")
        }
    }

    fun savePages(pages: List<String>) {
        val array = JSONArray()

        pages.forEach { page ->
            array.put(page)
        }

        cripto.salvar(
            "diario",
            array.toString()
        )
    }

    fun loadDate(index: Int): String? {
        val date = cripto.carregar("diario_data_$index")

        return date.takeIf { it.isNotBlank() }
    }

    fun saveDate(
        index: Int,
        date: String
    ) {
        cripto.salvar(
            "diario_data_$index",
            date
        )
    }

    fun removeDate(index: Int) {
        cripto.remover("diario_data_$index")
    }

    fun clear() {
        cripto.remover("diario")

        var index = 0

        while (true) {
            val key = "diario_data_$index"
            val value = cripto.carregar(key)

            if (value.isBlank()) {
                break
            }

            cripto.remover(key)
            index++
        }
    }
}

/* =========================================================
   DATA
========================================================= */

private fun today(): String {
    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale("pt", "BR")
    ).format(Date())
}

/* =========================================================
   INTERPRETADOR MARKDOWN INLINE
========================================================= */

private val markdownInlineRegex = Regex(
    """(\*\*.+?\*\*|__.+?__|~~.+?~~|\*.+?\*|_.+?_|`[^`]+`)"""
)

private fun parseMarkdownInline(
    text: String,
    color: Color
): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0

        markdownInlineRegex.findAll(text).forEach { match ->
            if (match.range.first > cursor) {
                append(
                    text.substring(
                        cursor,
                        match.range.first
                    )
                )
            }

            val token = match.value

            when {
                token.startsWith("**") &&
                    token.endsWith("**") -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(token.substring(2, token.length - 2))
                    }
                }

                token.startsWith("__") &&
                    token.endsWith("__") -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(token.substring(2, token.length - 2))
                    }
                }

                token.startsWith("~~") &&
                    token.endsWith("~~") -> {
                    withStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    ) {
                        append(token.substring(2, token.length - 2))
                    }
                }

                token.startsWith("`") &&
                    token.endsWith("`") -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = color.copy(alpha = 0.10f)
                        )
                    ) {
                        append(token.substring(1, token.length - 1))
                    }
                }

                token.startsWith("*") &&
                    token.endsWith("*") -> {
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic
                        )
                    ) {
                        append(token.substring(1, token.length - 1))
                    }
                }

                token.startsWith("_") &&
                    token.endsWith("_") -> {
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic
                        )
                    ) {
                        append(token.substring(1, token.length - 1))
                    }
                }

                else -> append(token)
            }

            cursor = match.range.last + 1
        }

        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}

/* =========================================================
   INTERPRETADOR MARKDOWN POR LINHA
========================================================= */

@Composable
private fun MarkdownLine(
    line: String,
    color: Color
) {
    val trimmed = line.trimStart()

    if (trimmed.isBlank()) {
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        return
    }

    val headingMatch = Regex(
        """^(#{1,6})\s+(.+)$"""
    ).matchEntire(trimmed)

    val orderedMatch = Regex(
        """^(\d+)[.)]\s+(.+)$"""
    ).matchEntire(trimmed)

    val horizontalRule = Regex(
        """^(---+|___+|\*\*\*+)\s*$"""
    ).matches(trimmed)

    when {
        horizontalRule -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(1.dp)
                    .background(color.copy(alpha = 0.25f))
            )
        }

        headingMatch != null -> {
            val level = headingMatch.groupValues[1].length
            val content = headingMatch.groupValues[2]

            val headingSize = when (level) {
                1 -> 25.sp
                2 -> 22.sp
                3 -> 19.sp
                4 -> 17.sp
                5 -> 16.sp
                else -> 15.sp
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontSize = headingSize,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            parseMarkdownInline(
                                content,
                                color
                            )
                        )
                    }
                },
                color = color,
                lineHeight = headingSize * 1.4f,
                modifier = Modifier.padding(
                    top = 5.dp,
                    bottom = 3.dp
                )
            )
        }

        trimmed.startsWith("> ") -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(24.dp)
                        .background(
                            PinkDark,
                            RoundedCornerShape(3.dp)
                        )
                )

                Text(
                    text = parseMarkdownInline(
                        trimmed.removePrefix("> "),
                        color
                    ),
                    modifier = Modifier.padding(
                        start = 9.dp
                    ),
                    color = color.copy(alpha = 0.85f),
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    lineHeight = 23.sp
                )
            }
        }

        trimmed.startsWith("- ") ||
            trimmed.startsWith("* ") ||
            trimmed.startsWith("+ ") -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 3.dp, bottom = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    color = PinkDark,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Text(
                    text = parseMarkdownInline(
                        trimmed.substring(2),
                        color
                    ),
                    color = color,
                    fontSize = 15.sp,
                    lineHeight = 25.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        orderedMatch != null -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 3.dp, bottom = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${orderedMatch.groupValues[1]}.",
                    color = PinkDark,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Text(
                    text = parseMarkdownInline(
                        orderedMatch.groupValues[2],
                        color
                    ),
                    color = color,
                    fontSize = 15.sp,
                    lineHeight = 25.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        else -> {
            Text(
                text = parseMarkdownInline(
                    line,
                    color
                ),
                color = color,
                fontSize = 15.sp,
                lineHeight = 25.sp
            )
        }
    }
}

/* =========================================================
   VISUALIZAÇÃO MARKDOWN
========================================================= */

@Composable
private fun MarkdownPreview(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        text.split('\n').forEachIndexed { index, line ->
            MarkdownLine(
                line = line,
                color = color
            )
        }
    }
}

/* =========================================================
   TELA
========================================================= */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiarioScreen() {
    val context =
        androidx.compose.ui.platform.LocalContext.current

    val storage = remember {
        DiarioStorage(context)
    }

    val dark = isSystemInDarkTheme()

    val pages = remember {
        mutableStateListOf<String>().also { list ->
            list.addAll(storage.loadPages())
        }
    }

    val dates = remember {
        mutableStateListOf<String?>().also { list ->
            repeat(pages.size) { index ->
                list.add(storage.loadDate(index))
            }
        }
    }

    var currentPage by rememberSaveable {
        mutableIntStateOf(0)
    }

    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var dragOffset by remember {
        mutableStateOf(0f)
    }

    var saving by remember {
        mutableStateOf(false)
    }

    CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (dark) DarkBackground else LightBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        horizontal = 11.dp,
                        vertical = 10.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                DiarioTopBar(
                    dark = dark,
                    onDownload = {
                        val baixou = exportAllPages(
                            context = context,
                            pages = pages,
                            dates = dates
                        )

                        Toast.makeText(
                            context,
                            if (baixou) {
                                "Diário baixado"
                            } else {
                                "Não foi possível baixar o diário"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onDelete = {
                        showDeleteDialog = true
                    }
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .navigationBarsPadding()
                        .pointerInput(
                            currentPage,
                            pages.size
                        ) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    dragOffset = 0f
                                },
                                onHorizontalDrag = { change, amount ->
                                    change.consume()
                                    dragOffset += amount
                                },
                                onDragEnd = {
                                    val threshold = 55f

                                    when {
                                        dragOffset < -threshold -> {
                                            if (
                                                currentPage ==
                                                pages.lastIndex
                                            ) {
                                                pages.add("")
                                                dates.add(null)
                                            }

                                            currentPage = min(
                                                currentPage + 1,
                                                pages.lastIndex
                                            )
                                        }

                                        dragOffset > threshold -> {
                                            currentPage = max(
                                                currentPage - 1,
                                                0
                                            )
                                        }
                                    }

                                    dragOffset = 0f
                                },
                                onDragCancel = {
                                    dragOffset = 0f
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    DiarioBook(
                        pages = pages,
                        dates = dates,
                        currentPage = currentPage,
                        dragOffset = dragOffset,
                        dark = dark,
                        onTextChanged = { index, text ->
                            while (pages.size <= index) {
                                pages.add("")
                            }

                            while (dates.size <= index) {
                                dates.add(null)
                            }

                            pages[index] = text

                            storage.savePages(pages)

                            if (text.isNotEmpty()) {
                                val date = today()
                                dates[index] = date
                                storage.saveDate(index, date)
                            }

                            saving = true
                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text(text = "Apagar diário")
            },
            text = {
                Text(
                    text = "Apagar todas as páginas do diário?\n\n" +
                        "Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        storage.clear()

                        pages.clear()
                        pages.add("")

                        dates.clear()
                        dates.add(null)

                        currentPage = 0
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = "Apagar",
                        color = PinkDark
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    LaunchedEffect(saving) {
        if (saving) {
            delay(1000)
            saving = false
        }
    }
}

/* =========================================================
   TOPO
========================================================= */

@Composable
private fun DiarioTopBar(
    dark: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val surface =
        if (dark) Color(0xFF111111) else Color.White

    val border =
        if (dark) DarkBorder else LightBorder

    val text =
        if (dark) DarkText else LightText

    val muted =
        if (dark) DarkMuted else LightMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(61.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(surface)
            .border(
                1.dp,
                border,
                RoundedCornerShape(999.dp)
            )
            .padding(
                start = 13.dp,
                end = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(39.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (dark) Color.Black else Color(0xFFF0EDEF)
                )
                .border(
                    1.dp,
                    border,
                    RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.diario),
                contentDescription = null,
                tint = if (dark) Color.White else PinkDark,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(11.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Meu Diário",
                color = text,
                fontSize = 15.sp
            )

            Text(
                text = "Suas ideias estão seguras",
                color = muted,
                fontSize = 8.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DiarioTopButton(
                dark = dark,
                onClick = onDownload
            ) {
                Icon(
                    painter = painterResource(R.drawable.download),
                    contentDescription = "Baixar todas as páginas",
                    modifier = Modifier.size(19.dp)
                )
            }

            DiarioTopButton(
                dark = dark,
                onClick = onDelete
            ) {
                Icon(
                    painter = painterResource(R.drawable.lixeira),
                    contentDescription = "Apagar diário",
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

/* =========================================================
   BOTÕES DO TOPO
========================================================= */

@Composable
private fun DiarioTopButton(
    dark: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val background =
        if (dark) Color(0x0BFFFFFF) else Color(0x08000000)

    val border =
        if (dark) DarkBorder else LightBorder

    val tint =
        if (dark) Color(0xCCFFFFFF) else Color(0xCC292127)

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(39.dp)
            .clip(RoundedCornerShape(50))
            .background(background)
            .border(
                1.dp,
                border,
                RoundedCornerShape(50)
            )
    ) {
        CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides tint
        ) {
            content()
        }
    }
}

/* =========================================================
   LIVRO
========================================================= */

@Composable
private fun DiarioBook(
    pages: SnapshotStateList<String>,
    dates: SnapshotStateList<String?>,
    currentPage: Int,
    dragOffset: Float,
    dark: Boolean,
    onTextChanged: (Int, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 4.dp,
                    top = 8.dp
                )
                .clip(RoundedCornerShape(25.dp))
                .background(
                    if (dark) Color(0xFF0C0C10) else Color(0xFFE1DADD)
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 1.dp,
                    top = 4.dp
                )
                .clip(RoundedCornerShape(25.dp))
                .background(
                    if (dark) Color(0xFF151519) else Color(0xFFEDE7E9)
                )
        )

        CurrentPage(
            text = pages.getOrElse(currentPage) { "" },
            date = dates.getOrElse(currentPage) { null },
            pageIndex = currentPage,
            dragOffset = dragOffset,
            dark = dark,
            onTextChanged = {
                onTextChanged(currentPage, it)
            }
        )
    }
}

/* =========================================================
   PÁGINA
========================================================= */

@Composable
private fun CurrentPage(
    text: String,
    date: String?,
    pageIndex: Int,
    dragOffset: Float,
    dark: Boolean,
    onTextChanged: (String) -> Unit
) {
    var editing by rememberSaveable(pageIndex) {
        mutableStateOf(true)
    }

    val pageBackground =
        if (dark) DarkPage else LightPage

    val pageText =
        if (dark) DarkText else LightPageText

    val pageBorder =
        if (dark) DarkPageBorder else Color(0x18000000)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationY = (dragOffset / 1.8f)
                    .coerceIn(-180f, 0f)

                transformOrigin = TransformOrigin(0f, 0.5f)
                cameraDistance = 24f * density
            }
            .clip(RoundedCornerShape(24.dp))
            .background(pageBackground)
            .border(
                1.dp,
                pageBorder,
                RoundedCornerShape(24.dp)
            )
            .padding(
                start = 21.dp,
                end = 21.dp,
                top = 20.dp,
                bottom = 22.dp
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        editing = !editing
                    }
                ) {
                    Text(
                        text = if (editing) {
                            "Visualizar Markdown"
                        } else {
                            "Editar Markdown"
                        },
                        color = pageText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Text(
                text = "Página ${pageIndex + 1} • ${date ?: "Sem data"}",
                color = pageText,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (editing) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    textStyle = TextStyle(
                        color = pageText,
                        fontSize = 15.sp,
                        lineHeight = 28.sp,
                        letterSpacing = 0.1.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    ),
                    cursorBrush = SolidColor(
                        if (dark) Pink else PinkDark
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "Digite o que está preso no seu coração...",
                                    color = pageText.copy(alpha = 0.5f),
                                    fontSize = 15.sp,
                                    lineHeight = 28.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            innerTextField()
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    MarkdownPreview(
                        text = text,
                        color = pageText,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/* =========================================================
   EXPORTAR
========================================================= */

private fun exportAllPages(
    context: Context,
    pages: List<String>,
    dates: List<String?>
): Boolean {
    val resolver = context.contentResolver

    var algumDownload = false

    pages.forEachIndexed { index, content ->
        val number = String.format(
            Locale.US,
            "%02d",
            index + 1
        )

        val date = dates.getOrNull(index) ?: "Sem data"

        val safeDate = date
            .replace("/", "-")
            .replace("\\", "-")

        val fileName = "pagina-$number = $safeDate.txt"

        val fileContent = "Dia ($date):\n\n$content"

        val values = ContentValues().apply {
            put(
                MediaStore.Downloads.DISPLAY_NAME,
                fileName
            )

            put(
                MediaStore.Downloads.MIME_TYPE,
                "text/plain"
            )

            put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
        }

        val uri = resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        )

        if (uri != null) {
            try {
                resolver
                    .openOutputStream(uri)
                    ?.use { output ->
                        output.write(
                            fileContent.toByteArray(Charsets.UTF_8)
                        )
                    }

                algumDownload = true
            } catch (_: Exception) {
                resolver.delete(uri, null, null)
            }
        }
    }

    return algumDownload
}
