package com.mulheres

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.addPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlin.math.min


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
         * Impede screenshots e gravação da tela,
         * caso você queira manter o mesmo comportamento
         * do restante do seu aplicativo.
         */
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        /*
         * Não desenha atrás da status bar.
         *
         * Isso faz o Compose respeitar as barras do sistema.
         */
        WindowCompat.setDecorFitsSystemWindows(
            window,
            true
        )

        setContent {
            CalculadoraApp()
        }
    }
}


/* =========================================================
   CORES
========================================================= */

private val DarkBackground = Color(0xFF000000)
private val DarkDisplay = Color(0xFF000000)
private val DarkButton = Color(0xFF202020)
private val DarkFunction = Color(0xFF2B2B2B)
private val DarkOperator = Color(0xFF252525)
private val DarkEqual = Color(0xFFFFFFFF)

private val LightBackground = Color(0xFFF5F5F5)
private val LightDisplay = Color(0xFFFFFFFF)
private val LightButton = Color(0xFFE1E1E1)
private val LightFunction = Color(0xFFD5D5D5)
private val LightOperator = Color(0xFFDCDCDC)
private val LightEqual = Color(0xFF111111)


/* =========================================================
   APP
========================================================= */

@Composable
fun CalculadoraApp() {

    val dark =
        androidx.compose.foundation.isSystemInDarkTheme()

    val background =
        if (dark) DarkBackground
        else LightBackground

    /*
     * Overscroll totalmente desativado.
     */
    androidx.compose.runtime.CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = background
        ) {

            Calculadora(
                dark = dark
            )
        }
    }
}


/* =========================================================
   CALCULADORA
========================================================= */

@Composable
private fun Calculadora(
    dark: Boolean
) {

    var expression by remember {
        mutableStateOf("")
    }

    var history by remember {
        mutableStateOf("")
    }

    var acabouDeCalcular by remember {
        mutableStateOf(false)
    }

    var erro by remember {
        mutableStateOf(false)
    }

    val background =
        if (dark) DarkBackground
        else LightBackground

    val displayBackground =
        if (dark) DarkDisplay
        else LightDisplay

    val displayText =
        if (dark) Color.White
        else Color(0xFF111111)

    val historyText =
        if (dark) {
            Color.White.copy(alpha = .35f)
        } else {
            Color.Black.copy(alpha = .40f)
        }

    /*
     * Toda a tela fica dentro de safeDrawingPadding().
     *
     * Isso impede que a calculadora fique colada
     * na status bar ou na barra de navegação.
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /*
             * DISPLAY
             */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        color = displayBackground,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(
                        horizontal = 18.dp,
                        vertical = 18.dp
                    ),
                contentAlignment = Alignment.BottomEnd
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {

                    if (history.isNotEmpty()) {

                        Text(
                            text = history,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 7.dp),
                            color = historyText,
                            fontSize = 14.sp,
                            lineHeight = 17.sp,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text =
                            if (erro) "Erro"
                            else expression.ifEmpty { "0" },

                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (erro) {
                                    Modifier.shake()
                                } else {
                                    Modifier
                                }
                            ),

                        color = displayText,

                        fontSize =
                            when {
                                expression.length > 14 -> 38.sp
                                expression.length > 10 -> 46.sp
                                else -> 58.sp
                            },

                        fontWeight = FontWeight.SemiBold,

                        lineHeight = 1.sp,

                        textAlign = TextAlign.End,

                        maxLines = 1,

                        overflow = TextOverflow.Ellipsis
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            /*
             * BOTÕES
             */

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                val rows = listOf(

                    listOf(
                        ButtonData("C", ButtonType.FUNCTION),
                        ButtonData("back", ButtonType.FUNCTION),
                        ButtonData("%", ButtonType.FUNCTION),
                        ButtonData("÷", ButtonType.OPERATOR)
                    ),

                    listOf(
                        ButtonData("7"),
                        ButtonData("8"),
                        ButtonData("9"),
                        ButtonData("×", ButtonType.OPERATOR)
                    ),

                    listOf(
                        ButtonData("4"),
                        ButtonData("5"),
                        ButtonData("6"),
                        ButtonData("−", ButtonType.OPERATOR)
                    ),

                    listOf(
                        ButtonData("1"),
                        ButtonData("2"),
                        ButtonData("3"),
                        ButtonData("+", ButtonType.OPERATOR)
                    ),

                    listOf(
                        ButtonData("=", ButtonType.EQUAL),
                        ButtonData("()"),
                        ButtonData(","),
                        ButtonData("0")
                    )
                )

                rows.forEach { row ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        row.forEach { button ->

                            CalculatorButton(
                                data = button,
                                dark = dark,
                                modifier = Modifier.weight(1f),
                                onClick = {

                                    when (button.value) {

                                        "C" -> {

                                            expression = ""
                                            history = ""
                                            acabouDeCalcular = false
                                            erro = false
                                        }


                                        "back" -> {

                                            expression =
                                                expression.dropLast(1)

                                            acabouDeCalcular = false
                                        }


                                        "=" -> {

                                            val result =
                                                calcularExpressao(
                                                    expression
                                                )

                                            if (result == null) {

                                                erro = true

                                                /*
                                                 * Remove o estado
                                                 * de erro depois de 700 ms.
                                                 */
                                                kotlinx.coroutines.MainScope()
                                                    .launch {
                                                        delay(700)

                                                        expression = ""
                                                        history = ""
                                                        acabouDeCalcular = false
                                                        erro = false
                                                    }

                                            } else {

                                                history =
                                                    "$expression ="

                                                expression = result

                                                acabouDeCalcular = true
                                                erro = false
                                            }
                                        }


                                        "()" -> {

                                            expression =
                                                adicionarParenteses(
                                                    expression,
                                                    acabouDeCalcular
                                                )

                                            acabouDeCalcular = false
                                        }


                                        "," -> {

                                            var novo =
                                                expression

                                            if (acabouDeCalcular) {

                                                novo = ""

                                                history = ""

                                                acabouDeCalcular = false
                                            }

                                            val partes =
                                                novo.split(
                                                    Regex("[+\\-*/()]")
                                                )

                                            val atual =
                                                partes.lastOrNull()
                                                    ?: ""

                                            if (!atual.contains(".")) {

                                                if (
                                                    novo.isEmpty() ||
                                                    "+-*/(".contains(
                                                        novo.last()
                                                    )
                                                ) {
                                                    novo += "0"
                                                }

                                                novo += "."
                                            }

                                            expression = novo
                                        }


                                        "%" -> {

                                            if (
                                                expression.isNotEmpty() &&
                                                Regex("[0-9)]$")
                                                    .containsMatchIn(expression)
                                            ) {

                                                expression += "%"
                                            }
                                        }


                                        "+",
                                        "−",
                                        "×",
                                        "÷" -> {

                                            val operator =
                                                when (button.value) {
                                                    "−" -> "-"
                                                    "×" -> "*"
                                                    "÷" -> "/"
                                                    else -> button.value
                                                }

                                            var novo =
                                                expression

                                            acabouDeCalcular = false

                                            if (novo.isEmpty()) {

                                                if (operator == "-") {
                                                    novo = "-"
                                                }

                                            } else {

                                                if (
                                                    "+-*/".contains(
                                                        novo.last()
                                                    )
                                                ) {

                                                    novo =
                                                        novo.dropLast(1)
                                                }

                                                novo += operator
                                            }

                                            expression = novo
                                        }


                                        else -> {

                                            /*
                                             * Números.
                                             */

                                            var novo =
                                                expression

                                            if (acabouDeCalcular) {

                                                novo = ""

                                                history = ""

                                                acabouDeCalcular = false
                                            }

                                            novo += button.value

                                            expression = novo
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


/* =========================================================
   DADOS DOS BOTÕES
========================================================= */

private data class ButtonData(
    val value: String,
    val type: ButtonType = ButtonType.NUMBER
)

private enum class ButtonType {
    NUMBER,
    FUNCTION,
    OPERATOR,
    EQUAL
}


/* =========================================================
   BOTÃO
========================================================= */

@Composable
private fun CalculatorButton(
    data: ButtonData,
    dark: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {

    val background =
        when (data.type) {

            ButtonType.FUNCTION ->
                if (dark) DarkFunction
                else LightFunction

            ButtonType.OPERATOR ->
                if (dark) DarkOperator
                else LightOperator

            ButtonType.EQUAL ->
                if (dark) DarkEqual
                else LightEqual

            ButtonType.NUMBER ->
                if (dark) DarkButton
                else LightButton
        }

    val textColor =
        when (data.type) {

            ButtonType.EQUAL ->
                if (dark) Color.Black
                else Color.White

            ButtonType.FUNCTION,
            ButtonType.OPERATOR,
            ButtonType.NUMBER ->
                if (dark) Color.White
                else Color(0xFF111111)
        }


    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                color = background,
                shape = CircleShape
            )
            .clickable(
                indication = null,
                interactionSource = remember {
                    androidx.compose.foundation.interaction.MutableInteractionSource()
                },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        if (data.value == "back") {

            androidx.compose.foundation.Image(
                painter = rememberVectorPainter(
                    image = BackspaceIcon
                ),
                contentDescription = "Apagar",
                modifier = Modifier
                    .fillMaxWidth(.35f)
                    .aspectRatio(1f)
            )

        } else {

            Text(
                text = data.value,
                color = textColor,

                fontSize =
                    when (data.value) {
                        "=",
                        "×",
                        "÷",
                        "−",
                        "+" -> 26.sp

                        else -> 23.sp
                    },

                fontWeight = FontWeight.SemiBold,

                textAlign = TextAlign.Center
            )
        }
    }
}


/* =========================================================
   ÍCONE APAGAR
========================================================= */

private val BackspaceIcon: ImageVector
    get() =
        ImageVector.Builder(
            name = "Backspace",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        )
            .addPath(
                pathData = PathParser()
                    .parsePathString(
                        "m560-424 76 76q11 11 28 11t28-11q11-11 11-28t-11-28l-76-76 76-76q11-11 11-28t-11-28q-11-11-28-11t-28 11l-76 76-76-76q-11-11-28-11t-28 11q-11 11-11 28t11 28l76 76-76 76q-11 11-11 28t11 28q11 11 28 11t28-11l76-76ZM360-160q-19 0-36-8.5T296-192L116-432q-16-21-16-48t16-48l180-240q11-15 28-23.5t36-8.5h440q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H360Zm0-80h440v-480H360L180-480l180 240Zm130-240Z"
                    )
                    .toNodes(),
                fill = androidx.compose.ui.graphics.SolidColor(
                    Color.White
                )
            )
            .build()


/* =========================================================
   PARENTÊSES
========================================================= */

private fun adicionarParenteses(
    expression: String,
    acabouDeCalcular: Boolean
): String {

    var expr =
        if (acabouDeCalcular) ""
        else expression

    val ultimo =
        expr.lastOrNull()?.toString() ?: ""

    val abertos =
        expr.count { it == '(' }

    val fechados =
        expr.count { it == ')' }

    if (
        expr.isEmpty() ||
        "+-*/(".contains(ultimo)
    ) {

        expr += "("

        return expr
    }

    if (abertos > fechados) {

        if (
            !"+-*/(".contains(ultimo)
        ) {

            expr += ")"
        }

        return expr
    }

    expr += "*("

    return expr
}


/* =========================================================
   PORCENTAGEM
========================================================= */

private fun prepararPorcentagem(
    expression: String
): String {

    return expression.replace(
        Regex("(\\d+(?:\\.\\d+)?)%"),
        "($1/100)"
    )
}


/* =========================================================
   CALCULADOR
========================================================= */

private fun calcularExpressao(
    original: String
): String? {

    if (original.isEmpty()) {
        return null
    }

    try {

        var expression =
            original.replace(",", ".")

        val ultimo =
            expression.lastOrNull()

        if (
            ultimo != null &&
            "+-*/(".contains(ultimo)
        ) {
            return null
        }

        val abertos =
            expression.count { it == '(' }

        val fechados =
            expression.count { it == ')' }

        if (abertos != fechados) {
            return null
        }

        /*
         * Aceita somente caracteres
         * matemáticos esperados.
         */
        if (
            !Regex(
                "^[0-9+\\-*/().%\\s]+$"
            ).matches(expression)
        ) {
            return null
        }

        expression =
            prepararPorcentagem(expression)

        val resultado =
            SimpleExpressionParser(
                expression
            ).parse()

        if (!resultado.isFinite()) {
            return null
        }

        return resultado
            .toBigDecimal()
            .setScale(
                10,
                java.math.RoundingMode.HALF_UP
            )
            .stripTrailingZeros()
            .toPlainString()

    } catch (_: Exception) {

        return null
    }
}


/* =========================================================
   PARSER MATEMÁTICO
========================================================= */

private class SimpleExpressionParser(
    private val text: String
) {

    private var position = 0


    fun parse(): Double {

        val result =
            parseExpression()

        skipSpaces()

        if (position != text.length) {
            throw IllegalArgumentException()
        }

        return result
    }


    private fun parseExpression(): Double {

        var value =
            parseTerm()

        while (true) {

            skipSpaces()

            if (match('+')) {

                value += parseTerm()

            } else if (match('-')) {

                value -= parseTerm()

            } else {

                break
            }
        }

        return value
    }


    private fun parseTerm(): Double {

        var value =
            parseFactor()

        while (true) {

            skipSpaces()

            if (match('*')) {

                value *= parseFactor()

            } else if (match('/')) {

                value /= parseFactor()

            } else {

                break
            }
        }

        return value
    }


    private fun parseFactor(): Double {

        skipSpaces()

        if (match('+')) {
            return parseFactor()
        }

        if (match('-')) {
            return -parseFactor()
        }

        if (match('(')) {

            val value =
                parseExpression()

            if (!match(')')) {
                throw IllegalArgumentException()
            }

            return value
        }

        return parseNumber()
    }


    private fun parseNumber(): Double {

        skipSpaces()

        val start =
            position

        while (
            position < text.length &&
            (
                text[position].isDigit() ||
                text[position] == '.'
            )
        ) {

            position++
        }

        if (start == position) {
            throw IllegalArgumentException()
        }

        return text
            .substring(start, position)
            .toDouble()
    }


    private fun match(
        character: Char
    ): Boolean {

        skipSpaces()

        if (
            position < text.length &&
            text[position] == character
        ) {

            position++

            return true
        }

        return false
    }


    private fun skipSpaces() {

        while (
            position < text.length &&
            text[position].isWhitespace()
        ) {

            position++
        }
    }
}


/* =========================================================
   ANIMAÇÃO DE ERRO
========================================================= */

@Composable
private fun Modifier.shake(): Modifier {

    var scale by remember {
        mutableStateOf(1f)
    }

    LaunchedEffect(Unit) {

        scale = .97f

        delay(70)

        scale = 1.03f

        delay(70)

        scale = .98f

        delay(70)

        scale = 1f
    }

    return this.scale(scale)
}