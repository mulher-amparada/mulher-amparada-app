package com.mulheres

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.RoundingMode


/* =========================================================
   ACTIVITY
========================================================= */

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
         * Impede screenshots e gravação da tela.
         */
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        /*
         * O Compose respeita as barras do sistema.
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

private val DarkBackground =
    Color(0xFF000000)

private val DarkDisplay =
    Color(0xFF000000)

private val DarkButton =
    Color(0xFF202020)

private val DarkFunction =
    Color(0xFF2B2B2B)

private val DarkOperator =
    Color(0xFF252525)

private val DarkEqual =
    Color(0xFFFFFFFF)


private val LightBackground =
    Color(0xFFF5F5F5)

private val LightDisplay =
    Color(0xFFFFFFFF)

private val LightButton =
    Color(0xFFE1E1E1)

private val LightFunction =
    Color(0xFFD5D5D5)

private val LightOperator =
    Color(0xFFDCDCDC)

private val LightEqual =
    Color(0xFF111111)


/* =========================================================
   APP
========================================================= */

@Composable
private fun CalculadoraApp() {

    val dark =
        isSystemInDarkTheme()

    val background =
        if (dark) {
            DarkBackground
        } else {
            LightBackground
        }

    /*
     * Remove completamente o efeito de overscroll
     * do Compose.
     */
    CompositionLocalProvider(
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

    /*
     * Scope correto para executar delay()
     * após o erro.
     */
    val scope =
        rememberCoroutineScope()


    val background =
        if (dark) {
            DarkBackground
        } else {
            LightBackground
        }


    val displayBackground =
        if (dark) {
            DarkDisplay
        } else {
            LightDisplay
        }


    val displayText =
        if (dark) {
            Color.White
        } else {
            Color(0xFF111111)
        }


    val historyText =
        if (dark) {
            Color.White.copy(alpha = .35f)
        } else {
            Color.Black.copy(alpha = .40f)
        }


    /*
     * Teclado físico.
     */
    val processKey: (androidx.compose.ui.input.key.KeyEvent) -> Unit =
        remember(
            expression,
            acabouDeCalcular
        ) {
            {

                event ->

                if (
                    event.type != KeyEventType.KeyDown
                ) {
                    return@remember
                }


                when {

                    /*
                     * Números
                     */
                    event.utf16CodePoint
                        in 48..57 -> {

                        var novo =
                            expression

                        if (acabouDeCalcular) {

                            novo = ""

                            history = ""

                            acabouDeCalcular = false
                        }

                        novo +=
                            event.utf16CodePoint
                                .toChar()

                        expression = novo
                    }


                    /*
                     * + - * /
                     */
                    event.key == Key.Plus -> {

                        expression =
                            adicionarOperador(
                                expression,
                                "+"
                            )

                        acabouDeCalcular = false
                    }


                    event.key == Key.Minus -> {

                        expression =
                            adicionarOperador(
                                expression,
                                "-"
                            )

                        acabouDeCalcular = false
                    }


                    event.key == Key.Asterisk -> {

                        expression =
                            adicionarOperador(
                                expression,
                                "*"
                            )

                        acabouDeCalcular = false
                    }


                    event.key == Key.Slash -> {

                        expression =
                            adicionarOperador(
                                expression,
                                "/"
                            )

                        acabouDeCalcular = false
                    }


                    /*
                     * Ponto
                     */
                    event.key == Key.Period ||
                            event.key == Key.Comma -> {

                        expression =
                            adicionarDecimal(
                                expression
                            )
                    }


                    /*
                     * Backspace
                     */
                    event.key == Key.Backspace -> {

                        expression =
                            expression.dropLast(1)

                        acabouDeCalcular = false
                    }


                    /*
                     * Enter
                     */
                    event.key == Key.Enter -> {

                        val result =
                            calcularExpressao(
                                expression
                            )

                        if (result == null) {

                            erro = true

                            scope.launch {

                                delay(700)

                                expression = ""
                                history = ""
                                acabouDeCalcular = false
                                erro = false
                            }

                        } else {

                            history =
                                "$expression ="

                            expression =
                                result

                            acabouDeCalcular = true
                        }
                    }


                    /*
                     * Escape
                     */
                    event.key == Key.Escape -> {

                        expression = ""

                        history = ""

                        acabouDeCalcular = false

                        erro = false
                    }
                }
            }
        }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .navigationBarsPadding()
            .background(background)
            .then(
                Modifier
            ),
        contentAlignment = Alignment.Center
    ) {

        /*
         * Container principal.
         */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            /*
             * =================================================
             * DISPLAY
             * =================================================
             */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        when {
                            /*
                             * Altura menor em telas pequenas.
                             */
                            expression.length > 0 &&
                                    expression.length > 12 ->
                                160.dp

                            else ->
                                150.dp
                        }
                    )
                    .background(
                        color = displayBackground,
                        shape = RoundedCornerShape(
                            28.dp
                        )
                    )
                    .padding(
                        horizontal = 18.dp,
                        vertical = 18.dp
                    ),
                contentAlignment =
                    Alignment.BottomEnd
            ) {

                Column(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalAlignment =
                        Alignment.End,

                    verticalArrangement =
                        Arrangement.Bottom
                ) {

                    /*
                     * Histórico.
                     */
                    if (
                        history.isNotEmpty()
                    ) {

                        Text(
                            text = history,

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        bottom = 7.dp
                                    ),

                            color =
                                historyText,

                            fontSize =
                                14.sp,

                            lineHeight =
                                17.sp,

                            textAlign =
                                TextAlign.End,

                            maxLines = 1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }


                    /*
                     * Resultado / expressão.
                     */
                    Text(
                        text =
                            if (erro) {
                                "Erro"
                            } else {
                                expression.ifEmpty {
                                    "0"
                                }
                            },

                        modifier =
                            if (erro) {
                                Modifier
                                    .fillMaxWidth()
                                    .shake()
                            } else {
                                Modifier
                                    .fillMaxWidth()
                            },

                        color =
                            displayText,

                        fontSize =
                            when {

                                expression.length > 16 ->
                                    34.sp

                                expression.length > 13 ->
                                    40.sp

                                expression.length > 10 ->
                                    46.sp

                                else ->
                                    58.sp
                            },

                        fontWeight =
                            FontWeight.SemiBold,

                        lineHeight =
                            58.sp,

                        textAlign =
                            TextAlign.End,

                        maxLines = 1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }


            /*
             * Espaçamento entre display e botões.
             */
            androidx.compose.foundation.layout.Spacer(
                modifier =
                    Modifier.height(16.dp)
            )


            /*
             * =================================================
             * BOTÕES
             * =================================================
             */

            val rows =
                listOf(

                    listOf(
                        ButtonData(
                            "C",
                            ButtonType.FUNCTION
                        ),

                        ButtonData(
                            "back",
                            ButtonType.FUNCTION
                        ),

                        ButtonData(
                            "%",
                            ButtonType.FUNCTION
                        ),

                        ButtonData(
                            "÷",
                            ButtonType.OPERATOR
                        )
                    ),


                    listOf(
                        ButtonData("7"),
                        ButtonData("8"),
                        ButtonData("9"),

                        ButtonData(
                            "×",
                            ButtonType.OPERATOR
                        )
                    ),


                    listOf(
                        ButtonData("4"),
                        ButtonData("5"),
                        ButtonData("6"),

                        ButtonData(
                            "−",
                            ButtonType.OPERATOR
                        )
                    ),


                    listOf(
                        ButtonData("1"),
                        ButtonData("2"),
                        ButtonData("3"),

                        ButtonData(
                            "+",
                            ButtonType.OPERATOR
                        )
                    ),


                    listOf(
                        ButtonData(
                            "=",
                            ButtonType.EQUAL
                        ),

                        ButtonData("()"),

                        ButtonData(","),

                        ButtonData("0")
                    )
                )


            Column(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                rows.forEach { row ->

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {

                        row.forEach { button ->

                            CalculatorButton(
                                data = button,

                                dark = dark,

                                modifier =
                                    Modifier.weight(
                                        1f
                                    ),

                                onClick = {

                                    when (
                                        button.value
                                    ) {

                                        /*
                                         * C
                                         */
                                        "C" -> {

                                            expression = ""

                                            history = ""

                                            acabouDeCalcular =
                                                false

                                            erro = false
                                        }


                                        /*
                                         * Apagar
                                         */
                                        "back" -> {

                                            expression =
                                                expression.dropLast(
                                                    1
                                                )

                                            acabouDeCalcular =
                                                false
                                        }


                                        /*
                                         * Igual
                                         */
                                        "=" -> {

                                            val result =
                                                calcularExpressao(
                                                    expression
                                                )

                                            if (
                                                result == null
                                            ) {

                                                erro = true

                                                scope.launch {

                                                    delay(700)

                                                    expression =
                                                        ""

                                                    history =
                                                        ""

                                                    acabouDeCalcular =
                                                        false

                                                    erro =
                                                        false
                                                }

                                            } else {

                                                history =
                                                    "$expression ="

                                                expression =
                                                    result

                                                acabouDeCalcular =
                                                    true

                                                erro =
                                                    false
                                            }
                                        }


                                        /*
                                         * Parênteses
                                         */
                                        "()" -> {

                                            expression =
                                                adicionarParenteses(
                                                    expression,
                                                    acabouDeCalcular
                                                )

                                            acabouDeCalcular =
                                                false
                                        }


                                        /*
                                         * Decimal
                                         */
                                        "," -> {

                                            if (
                                                acabouDeCalcular
                                            ) {

                                                expression =
                                                    ""

                                                history =
                                                    ""

                                                acabouDeCalcular =
                                                    false
                                            }

                                            expression =
                                                adicionarDecimal(
                                                    expression
                                                )
                                        }


                                        /*
                                         * Porcentagem
                                         */
                                        "%" -> {

                                            if (
                                                expression.isNotEmpty() &&
                                                expression.last()
                                                    .isDigit() ||
                                                expression.endsWith(
                                                    ")"
                                                )
                                            ) {

                                                expression +=
                                                    "%"
                                            }
                                        }


                                        /*
                                         * Operadores.
                                         */
                                        "+",
                                        "−",
                                        "×",
                                        "÷" -> {

                                            val operator =
                                                when (
                                                    button.value
                                                ) {

                                                    "−" ->
                                                        "-"

                                                    "×" ->
                                                        "*"

                                                    "÷" ->
                                                        "/"

                                                    else ->
                                                        "+"
                                                }

                                            expression =
                                                adicionarOperador(
                                                    expression,
                                                    operator
                                                )

                                            acabouDeCalcular =
                                                false
                                        }


                                        /*
                                         * Números.
                                         */
                                        else -> {

                                            var novo =
                                                expression

                                            if (
                                                acabouDeCalcular
                                            ) {

                                                novo =
                                                    ""

                                                history =
                                                    ""

                                                acabouDeCalcular =
                                                    false
                                            }

                                            novo +=
                                                button.value

                                            expression =
                                                novo
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
   BOTÃO
========================================================= */

private data class ButtonData(
    val value: String,
    val type: ButtonType =
        ButtonType.NUMBER
)


private enum class ButtonType {

    NUMBER,

    FUNCTION,

    OPERATOR,

    EQUAL
}


/* =========================================================
   BOTÃO COMPOSE
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
                if (dark) {
                    DarkFunction
                } else {
                    LightFunction
                }


            ButtonType.OPERATOR ->
                if (dark) {
                    DarkOperator
                } else {
                    LightOperator
                }


            ButtonType.EQUAL ->
                if (dark) {
                    DarkEqual
                } else {
                    LightEqual
                }


            ButtonType.NUMBER ->
                if (dark) {
                    DarkButton
                } else {
                    LightButton
                }
        }


    val textColor =
        when (data.type) {

            ButtonType.EQUAL ->
                if (dark) {
                    Color.Black
                } else {
                    Color.White
                }


            else ->
                if (dark) {
                    Color.White
                } else {
                    Color(0xFF111111)
                }
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
                interactionSource =
                    remember {
                        MutableInteractionSource()
                    },
                onClick = onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {

        if (
            data.value == "back"
        ) {

            /*
             * Ícone de apagar.
             *
             * Não usa ImageVector/addPath,
             * portanto não depende de
             * material-icons.
             */
            Text(
                text = "⌫",

                color =
                    textColor,

                fontSize =
                    29.sp,

                fontWeight =
                    FontWeight.Normal,

                textAlign =
                    TextAlign.Center
            )

        } else {

            Text(
                text =
                    data.value,

                color =
                    textColor,

                fontSize =
                    when {

                        data.value == "=" ->
                            27.sp

                        data.value in
                                listOf(
                                    "+",
                                    "−",
                                    "×",
                                    "÷"
                                ) ->
                            27.sp

                        else ->
                            23.sp
                    },

                fontWeight =
                    FontWeight.SemiBold,

                textAlign =
                    TextAlign.Center
            )
        }
    }
}


/* =========================================================
   ADICIONAR OPERADOR
========================================================= */

private fun adicionarOperador(
    expression: String,
    operator: String
): String {

    if (
        expression.isEmpty()
    ) {

        return if (
            operator == "-"
        ) {
            "-"
        } else {
            expression
        }
    }


    var result =
        expression


    val last =
        result.last()


    if (
        "+-*/".contains(last)
    ) {

        result =
            result.dropLast(1)
    }


    return result + operator
}


/* =========================================================
   ADICIONAR DECIMAL
========================================================= */

private fun adicionarDecimal(
    expression: String
): String {

    val partes =
        expression.split(
            Regex("[+\\-*/()]")
        )


    val atual =
        partes.lastOrNull()
            ?: ""


    if (
        atual.contains(".")
    ) {

        return expression
    }


    var result =
        expression


    if (
        result.isEmpty() ||
        "+-*/(".contains(
            result.last()
        )
    ) {

        result += "0"
    }


    result += "."

    return result
}


/* =========================================================
   PARENTÊSES
========================================================= */

private fun adicionarParenteses(
    expression: String,
    acabouDeCalcular: Boolean
): String {

    var expr =
        if (acabouDeCalcular) {
            ""
        } else {
            expression
        }


    val ultimo =
        expr.lastOrNull()
            ?.toString()
            ?: ""


    val abertos =
        expr.count {
            it == '('
        }


    val fechados =
        expr.count {
            it == ')'
        }


    /*
     * Começa um parêntese.
     */
    if (
        expr.isEmpty() ||
        "+-*/(".contains(
            ultimo
        )
    ) {

        expr += "("

        return expr
    }


    /*
     * Fecha um parêntese
     * se ainda houver algum aberto.
     */
    if (
        abertos > fechados
    ) {

        if (
            !"+-*/(".contains(
                ultimo
            )
        ) {

            expr += ")"
        }

        return expr
    }


    /*
     * Caso contrário,
     * começa uma multiplicação.
     */
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
        Regex(
            "(\\d+(?:\\.\\d+)?)%"
        ),
        "($1/100)"
    )
}


/* =========================================================
   CALCULAR EXPRESSÃO
========================================================= */

private fun calcularExpressao(
    original: String
): String? {

    if (
        original.isEmpty()
    ) {

        return null
    }


    try {

        var expression =
            original.replace(
                ",",
                "."
            )


        /*
         * Último caractere.
         */
        val ultimo =
            expression.lastOrNull()


        if (
            ultimo != null &&
            "+-*/(".contains(
                ultimo
            )
        ) {

            return null
        }


        /*
         * Parênteses.
         */
        val abertos =
            expression.count {
                it == '('
            }


        val fechados =
            expression.count {
                it == ')'
            }


        if (
            abertos != fechados
        ) {

            return null
        }


        /*
         * Só permite caracteres matemáticos.
         */
        if (
            !Regex(
                "^[0-9+\\-*/().%\\s]+$"
            ).matches(
                expression
            )
        ) {

            return null
        }


        /*
         * Porcentagens.
         */
        expression =
            prepararPorcentagem(
                expression
            )


        /*
         * Parser nativo.
         */
        val resultado =
            SimpleExpressionParser(
                expression
            ).parse()


        if (
            !resultado.isFinite()
        ) {

            return null
        }


        /*
         * Mesmo comportamento aproximado
         * do toFixed(10) do JavaScript.
         */
        return resultado
            .toBigDecimal()
            .setScale(
                10,
                RoundingMode.HALF_UP
            )
            .stripTrailingZeros()
            .toPlainString()

    } catch (
        _: Exception
    ) {

        return null
    }
}


/* =========================================================
   PARSER MATEMÁTICO
========================================================= */

private class SimpleExpressionParser(
    private val text: String
) {

    private var position =
        0


    fun parse(): Double {

        val result =
            parseExpression()


        skipSpaces()


        if (
            position != text.length
        ) {

            throw IllegalArgumentException()
        }


        return result
    }


    /*
     * Soma e subtração.
     */
    private fun parseExpression(): Double {

        var value =
            parseTerm()


        while (true) {

            skipSpaces()


            if (
                match('+')
            ) {

                value +=
                    parseTerm()

            } else if (
                match('-')
            ) {

                value -=
                    parseTerm()

            } else {

                break
            }
        }


        return value
    }


    /*
     * Multiplicação e divisão.
     */
    private fun parseTerm(): Double {

        var value =
            parseFactor()


        while (true) {

            skipSpaces()


            if (
                match('*')
            ) {

                value *=
                    parseFactor()

            } else if (
                match('/')
            ) {

                val divisor =
                    parseFactor()


                if (
                    divisor == 0.0
                ) {

                    throw ArithmeticException()
                }


                value /=
                    divisor

            } else {

                break
            }
        }


        return value
    }


    /*
     * Números, sinais e parênteses.
     */
    private fun parseFactor(): Double {

        skipSpaces()


        if (
            match('+')
        ) {

            return parseFactor()
        }


        if (
            match('-')
        ) {

            return -parseFactor()
        }


        if (
            match('(')
        ) {

            val value =
                parseExpression()


            if (
                !match(')')
            ) {

                throw IllegalArgumentException()
            }


            return value
        }


        return parseNumber()
    }


    /*
     * Número.
     */
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


        if (
            start == position
        ) {

            throw IllegalArgumentException()
        }


        return text
            .substring(
                start,
                position
            )
            .toDouble()
    }


    /*
     * Verifica caractere.
     */
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


    /*
     * Ignora espaços.
     */
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

    var scaleValue by remember {
        mutableStateOf(1f)
    }


    LaunchedEffect(Unit) {

        scaleValue =
            0.97f

        delay(70)


        scaleValue =
            1.03f

        delay(70)


        scaleValue =
            0.98f

        delay(70)


        scaleValue =
            1f
    }


    return this.scale(
        scaleValue
    )
}