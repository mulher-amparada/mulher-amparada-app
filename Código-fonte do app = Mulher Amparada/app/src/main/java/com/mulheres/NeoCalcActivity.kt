package com.mulheres

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class NeoCalcActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NeoCalcScreen()
        }
    }
}

/* =========================================================
   CORES
========================================================= */

private val Fundo = Color.Black

private val Botao =
    Color(0xFF151515)

private val BotaoHover =
    Color(0xFF202020)

private val Acao =
    Color(0xFF6F7AFF)

private val AcaoHover =
    Color(0xFF686DFF)

private val Operador =
    Color(0xFF3DA7FF)

private val OperadorHover =
    Color(0xFF61B7FF)

private val Texto =
    Color.White

private val TextoSecundario =
    Color(0xFFD0D0D0)

/* =========================================================
   CHAVES DO CRIPTO
========================================================= */

private const val CHAVE_SENHA =
    "senha"

private const val CHAVE_RECUPERACAO =
    "nome_recuperacao"

/* =========================================================
   FONTE
========================================================= */

private fun carregarQuicksand(
    context: android.content.Context
): FontFamily {

    val typeface =
        Typeface.createFromAsset(
            context.assets,
            "font.ttf"
        )

    return FontFamily(
        androidx.compose.ui.text.font.Typeface(
            typeface
        )
    )
}

/* =========================================================
   TIPOS DE DIÁLOGO
========================================================= */

private enum class TipoDialogo {
    ALERTA,
    PROMPT,
    CONFIRMACAO
}

private data class Dialogo(
    val tipo: TipoDialogo,
    val titulo: String,
    val mensagem: String,
    val valorInicial: String = "",
    val textoConfirmar: String = "OK"
)

/* =========================================================
   TELA
========================================================= */

@Composable
private fun NeoCalcScreen() {

    val context =
        LocalContext.current

    val fonte =
        remember {
            carregarQuicksand(context)
        }

    var expr by remember {
        mutableStateOf("")
    }

    var mensagemTemporaria by remember {
        mutableStateOf<String?>(null)
    }

    var dialogo by remember {
        mutableStateOf<Dialogo?>(null)
    }

    var valorDialogo by remember {
        mutableStateOf("")
    }

    var contadorToques by remember {
        mutableStateOf(0)
    }

    var ultimoToque by remember {
        mutableLongStateOf(0L)
    }

    val visorScroll =
        rememberScrollState()

    /*
     * =====================================================
     * VISOR
     * =====================================================
     */

    LaunchedEffect(
        expr,
        mensagemTemporaria
    ) {

        delay(1)

        visorScroll.scrollTo(
            visorScroll.maxValue
        )
    }

    /*
     * =====================================================
     * CINCO TOQUES
     * =====================================================
     */

    fun registrarToque() {

        val agora =
            System.currentTimeMillis()

        if (
            agora - ultimoToque > 2000
        ) {
            contadorToques = 0
        }

        ultimoToque = agora

        contadorToques++

        if (
            contadorToques >= 5
        ) {

            contadorToques = 0

            recuperarSenha(
                context = context,
                fonte = fonte,
                mostrarDialogo = {
                    dialogo = it

                    valorDialogo =
                        it.valorInicial
                },
                fecharDialogo = {
                    dialogo = null
                },
                mostrarMensagem = {
                    mensagemTemporaria = it
                }
            )
        }
    }

    /*
     * =====================================================
     * ATUALIZAR VISOR
     * =====================================================
     */

    fun atualizar() {

        mensagemTemporaria = null
    }

    /*
     * =====================================================
     * ADICIONAR
     * =====================================================
     */

    fun adicionar(
        valor: String
    ) {

        if (
            valor == "."
        ) {

            val partes =
                expr.split(
                    Regex("[+\\-*/()]")
                )

            val parteAtual =
                partes.lastOrNull()
                    ?: ""

            if (
                parteAtual.contains(".")
            ) {
                return
            }
        }

        expr += valor

        atualizar()
    }

    /*
     * =====================================================
     * OPERAÇÃO
     * =====================================================
     */

    fun operacao(
        op: String
    ) {

        if (expr.isEmpty()) {

            if (op == "-") {

                expr = "-"

                atualizar()
            }

            return
        }

        if (
            expr.lastOrNull()
                ?.toString()
                ?.matches(
                    Regex("[+\\-*/]")
                ) == true
        ) {

            expr =
                expr.dropLast(1) + op

        } else {

            expr += op
        }

        atualizar()
    }

    /*
     * =====================================================
     * PORCENTAGEM
     * =====================================================
     */

    fun porcentagem() {

        if (
            expr.isEmpty()
        ) {
            return
        }

        expr =
            expr.replace(
                Regex("(\\d+(?:\\.\\d+)?)$"),
                "($1/100)"
            )

        atualizar()
    }

    /*
     * =====================================================
     * LIMPAR
     * =====================================================
     */

    fun limpar() {

        expr = ""

        atualizar()
    }

    /*
     * =====================================================
     * APAGAR
     * =====================================================
     */

    fun apagar() {

        if (
            expr.isNotEmpty()
        ) {

            expr =
                expr.dropLast(1)

            atualizar()
        }
    }

    /*
     * =====================================================
     * CALCULAR
     * =====================================================
     */

    fun calcular() {

        val senha =
            try {

                String(
                    Cripto.carregar(
                        CHAVE_SENHA
                    ) ?: ""
                ).trim()

            } catch (
                e: Exception
            ) {

                ""
            }

        /*
         * SENHA ESPECIAL
         */

        if (
            expr.trim().isNotEmpty() &&
            expr.trim() == senha
        ) {

            try {

                context.startActivity(
                    Intent(
                        context,
                        User1Activity::class.java
                    )
                )

            } catch (
                e: Exception
            ) {

                /*
                 * Caso a Activity ainda não
                 * exista no projeto.
                 *
                 * Mantém a calculadora funcionando
                 * sem quebrar o aplicativo.
                 */

                mensagemTemporaria =
                    "Acesso"

            }

            return
        }

        if (
            expr.trim().isEmpty()
        ) {
            return
        }

        try {

            /*
             * Avaliação matemática simples.
             *
             * A expressão é previamente construída
             * apenas pelos botões da calculadora.
             */

            val resultado =
                avaliarExpressao(
                    expr
                )

            if (
                resultado.isFinite()
            ) {

                expr =
                    formatarNumero(
                        resultado
                    )

                mensagemTemporaria =
                    null

                return
            }

        } catch (
            e: Exception
        ) {
        }

        mensagemTemporaria =
            "Erro"

        expr = ""
    }

    /*
     * =====================================================
     * COMPOSE
     * =====================================================
     */

    androidx.compose.runtime.CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Fundo)
                    .padding(
                        horizontal = 20.dp
                    )
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(
                            Alignment.Center
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        18.dp
                    )
            ) {

                /*
                 * =========================================
                 * VISOR
                 * =========================================
                 */

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(
                                RoundedCornerShape(
                                    20.dp
                                )
                            )
                            .background(
                                Color.Transparent
                            )
                            .horizontalScroll(
                                visorScroll
                            )
                            .pointerInput(Unit) {

                                detectTapGestures {

                                    registrarToque()
                                }
                            }
                            .padding(
                                20.dp
                            ),
                    contentAlignment =
                        Alignment.CenterEnd
                ) {

                    Text(
                        text =
                            mensagemTemporaria
                                ?: if (
                                    expr.isEmpty()
                                ) {
                                    "0"
                                } else {
                                    expr
                                },

                        color =
                            Texto,

                        fontFamily =
                            fonte,

                        fontWeight =
                            FontWeight.Bold,

                        fontSize =
                            54.sp,

                        lineHeight =
                            58.sp,

                        textAlign =
                            TextAlign.End,

                        maxLines =
                            4
                    )
                }

                /*
                 * =========================================
                 * TECLADO
                 * =========================================
                 */

                Column(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {

                    LinhaBotoes(
                        fonte = fonte,

                        botoes = listOf(

                            BotaoCalculadora(
                                texto = "AC",
                                tipo = TipoBotao.ACAO
                            ) {
                                limpar()
                            },

                            BotaoCalculadora(
                                texto = "⌫",
                                tipo = TipoBotao.ACAO
                            ) {
                                apagar()
                            },

                            BotaoCalculadora(
                                texto = "%",
                                tipo = TipoBotao.ACAO
                            ) {
                                porcentagem()
                            },

                            BotaoCalculadora(
                                texto = "÷",
                                tipo = TipoBotao.OPERADOR
                            ) {
                                operacao("/")
                            }
                        )
                    )

                    LinhaBotoes(
                        fonte = fonte,

                        botoes = listOf(

                            BotaoCalculadora(
                                texto = "7"
                            ) {
                                adicionar("7")
                            },

                            BotaoCalculadora(
                                texto = "8"
                            ) {
                                adicionar("8")
                            },

                            BotaoCalculadora(
                                texto = "9"
                            ) {
                                adicionar("9")
                            },

                            BotaoCalculadora(
                                texto = "×",
                                tipo = TipoBotao.OPERADOR
                            ) {
                                operacao("*")
                            }
                        )
                    )

                    LinhaBotoes(
                        fonte = fonte,

                        botoes = listOf(

                            BotaoCalculadora(
                                texto = "4"
                            ) {
                                adicionar("4")
                            },

                            BotaoCalculadora(
                                texto = "5"
                            ) {
                                adicionar("5")
                            },

                            BotaoCalculadora(
                                texto = "6"
                            ) {
                                adicionar("6")
                            },

                            BotaoCalculadora(
                                texto = "−",
                                tipo = TipoBotao.OPERADOR
                            ) {
                                operacao("-")
                            }
                        )
                    )

                    LinhaBotoes(
                        fonte = fonte,

                        botoes = listOf(

                            BotaoCalculadora(
                                texto = "1"
                            ) {
                                adicionar("1")
                            },

                            BotaoCalculadora(
                                texto = "2"
                            ) {
                                adicionar("2")
                            },

                            BotaoCalculadora(
                                texto = "3"
                            ) {
                                adicionar("3")
                            },

                            BotaoCalculadora(
                                texto = "+",
                                tipo = TipoBotao.OPERADOR
                            ) {
                                operacao("+")
                            }
                        )
                    )

                    LinhaBotoes(
                        fonte = fonte,

                        botoes = listOf(

                            BotaoCalculadora(
                                texto = "()"
                            ) {
                                adicionar("(")
                            },

                            BotaoCalculadora(
                                texto = "0"
                            ) {
                                adicionar("0")
                            },

                            BotaoCalculadora(
                                texto = "."
                            ) {
                                adicionar(".")
                            },

                            BotaoCalculadora(
                                texto = "=",
                                tipo = TipoBotao.IGUAL
                            ) {
                                calcular()
                            }
                        )
                    )
                }
            }

            /*
             * =================================================
             * DIÁLOGOS
             * =================================================
             */

            dialogo?.let { atual ->

                AlertDialog(
                    onDismissRequest = {

                        if (
                            atual.tipo !=
                                TipoDialogo.ALERTA
                        ) {

                            dialogo = null
                        }
                    },

                    title = {

                        Text(
                            text =
                                atual.titulo,

                            fontFamily =
                                fonte,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Texto
                        )
                    },

                    text = {

                        Column {

                            Text(
                                text =
                                    atual.mensagem,

                                fontFamily =
                                    fonte,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    TextoSecundario,

                                lineHeight =
                                    23.sp
                            )

                            if (
                                atual.tipo ==
                                    TipoDialogo.PROMPT
                            ) {

                                Spacer(
                                    Modifier.height(
                                        16.dp
                                    )
                                )

                                OutlinedTextField(

                                    value =
                                        valorDialogo,

                                    onValueChange = {
                                        valorDialogo =
                                            it
                                    },

                                    singleLine =
                                        true,

                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    colors =
                                        OutlinedTextFieldDefaults.colors(

                                            focusedTextColor =
                                                Texto,

                                            unfocusedTextColor =
                                                Texto,

                                            focusedBorderColor =
                                                Acao,

                                            unfocusedBorderColor =
                                                Color(0xFF353535),

                                            cursorColor =
                                                Acao
                                        )
                                )
                            }
                        }
                    },

                    confirmButton = {

                        Button(
                            onClick = {

                                val tipo =
                                    atual.tipo

                                when (
                                    tipo
                                ) {

                                    TipoDialogo.ALERTA -> {

                                        dialogo =
                                            null
                                    }

                                    TipoDialogo.PROMPT -> {

                                        val valor =
                                            valorDialogo

                                        dialogo =
                                            null

                                        continuarPrompt(
                                            valor = valor,
                                            titulo = atual.titulo,
                                            context = context,
                                            fonte = fonte,
                                            mostrarDialogo = {
                                                dialogo = it

                                                valorDialogo =
                                                    it.valorInicial
                                            },
                                            mostrarMensagem = {
                                                mensagemTemporaria =
                                                    it
                                            }
                                        )
                                    }

                                    TipoDialogo.CONFIRMACAO -> {

                                        dialogo =
                                            null

                                        continuarConfirmacao(
                                            context = context,
                                            fonte = fonte,
                                            mostrarDialogo = {
                                                dialogo = it

                                                valorDialogo =
                                                    it.valorInicial
                                            }
                                        )
                                    }
                                }
                            },

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Acao
                                )
                        ) {

                            Text(
                                text =
                                    atual.textoConfirmar,

                                fontFamily =
                                    fonte,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    },

                    dismissButton = {

                        if (
                            atual.tipo !=
                                TipoDialogo.ALERTA
                        ) {

                            TextButton(
                                onClick = {

                                    dialogo =
                                        null
                                }
                            ) {

                                Text(
                                    text =
                                        "Cancelar",

                                    fontFamily =
                                        fonte,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        Texto
                                )
                            }
                        }
                    },

                    containerColor =
                        Botao
                )
            }
        }
    }
}

/* =========================================================
   BOTÃO
========================================================= */

private enum class TipoBotao {
    NORMAL,
    ACAO,
    OPERADOR,
    IGUAL
}

private data class BotaoCalculadora(
    val texto: String,
    val tipo: TipoBotao = TipoBotao.NORMAL,
    val acao: () -> Unit
)

@Composable
private fun LinhaBotoes(
    fonte: FontFamily,
    botoes: List<BotaoCalculadora>
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        botoes.forEach { botao ->

            val cor =
                when (
                    botao.tipo
                ) {

                    TipoBotao.ACAO ->
                        Acao

                    TipoBotao.OPERADOR,
                    TipoBotao.IGUAL ->
                        Operador

                    TipoBotao.NORMAL ->
                        Botao
                }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(
                            CircleShape
                        )
                        .background(
                            cor
                        )
                        .clickable {
                            botao.acao()
                        },

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        botao.texto,

                    color =
                        Texto,

                    fontFamily =
                        fonte,

                    fontWeight =
                        FontWeight.Bold,

                    fontSize =
                        if (
                            botao.texto == "⌫"
                        ) {
                            28.sp
                        } else {
                            25.sp
                        }
                )
            }
        }
    }
}

/* =========================================================
   RECUPERAÇÃO
========================================================= */

private fun recuperarSenha(
    context: android.content.Context,
    fonte: FontFamily,
    mostrarDialogo: (Dialogo) -> Unit,
    fecharDialogo: () -> Unit,
    mostrarMensagem: (String) -> Unit
) {

    val respostaSalva =
        try {

            String(
                Cripto.carregar(
                    CHAVE_RECUPERACAO
                ) ?: ""
            )
                .lowercase()
                .trim()

        } catch (
            e: Exception
        ) {

            mostrarMensagem("Erro")

            return
        }

    mostrarDialogo(
        Dialogo(
            tipo =
                TipoDialogo.PROMPT,

            titulo =
                "Recuperar senha",

            mensagem =
                "Como você gosta de ser chamada?",

            textoConfirmar =
                "OK"
        )
    )

    /*
     * A continuação é feita por
     * continuarPrompt().
     */
}

/* =========================================================
   CONTINUAÇÃO DO PROMPT
========================================================= */

private fun continuarPrompt(
    valor: String,
    titulo: String,
    context: android.content.Context,
    fonte: FontFamily,
    mostrarDialogo: (Dialogo) -> Unit,
    mostrarMensagem: (String) -> Unit
) {

    /*
     * Aqui tratamos a criação inicial
     * e a recuperação de senha usando
     * diretamente o Cripto.
     */

    val senhaSalva =
        try {

            String(
                Cripto.carregar(
                    CHAVE_SENHA
                ) ?: ""
            ).trim()

        } catch (
            e: Exception
        ) {

            ""
        }

    val respostaSalva =
        try {

            String(
                Cripto.carregar(
                    CHAVE_RECUPERACAO
                ) ?: ""
            )
                .lowercase()
                .trim()

        } catch (
            e: Exception
        ) {

            ""
        }

    if (
        titulo == "Criar senha"
    ) {

        if (
            valor.trim().isEmpty()
        ) {

            mostrarMensagem(
                "Senha inválida"
            )

            return
        }

        Cripto.salvar(
            CHAVE_SENHA,
            valor.trim()
        )

        mostrarDialogo(
            Dialogo(
                tipo =
                    TipoDialogo.PROMPT,

                titulo =
                    "Recuperação",

                mensagem =
                    "Como você gosta de ser chamada?",

                textoConfirmar =
                    "OK"
            )
        )

        return
    }

    if (
        respostaSalva.isNotEmpty() &&
        valor
            .trim()
            .lowercase() ==
        respostaSalva
    ) {

        mostrarDialogo(
            Dialogo(
                tipo =
                    TipoDialogo.PROMPT,

                titulo =
                    "Nova senha",

                mensagem =
                    "Digite a nova senha",

                textoConfirmar =
                    "OK"
            )
        )

        return
    }

    mostrarMensagem(
        "Resposta incorreta"
    )
}

/* =========================================================
   CONFIRMAÇÃO
========================================================= */

private fun continuarConfirmacao(
    context: android.content.Context,
    fonte: FontFamily,
    mostrarDialogo: (Dialogo) -> Unit
) {

    mostrarDialogo(
        Dialogo(
            tipo =
                TipoDialogo.PROMPT,

            titulo =
                "Nova resposta",

            mensagem =
                "Como você gosta de ser chamada?",

            textoConfirmar =
                "OK"
        )
    )
}

/* =========================================================
   AVALIADOR
========================================================= */

private fun avaliarExpressao(
    expressao: String
): Double {

    val tokens =
        mutableListOf<String>()

    var numero = ""

    for (
        caractere in expressao
    ) {

        if (
            caractere.isDigit() ||
            caractere == '.'
        ) {

            numero += caractere

        } else {

            if (
                numero.isNotEmpty()
            ) {

                tokens.add(numero)

                numero = ""
            }

            tokens.add(
                caractere.toString()
            )
        }
    }

    if (
        numero.isNotEmpty()
    ) {

        tokens.add(numero)
    }

    val valores =
        mutableListOf<Double>()

    val operadores =
        mutableListOf<Char>()

    fun prioridade(
        operador: Char
    ): Int {

        return when (
            operador
        ) {

            '+', '-' ->
                1

            '*', '/' ->
                2

            else ->
                0
        }
    }

    fun aplicar() {

        if (
            valores.size < 2 ||
            operadores.isEmpty()
        ) {
            throw IllegalArgumentException()
        }

        val b =
            valores.removeAt(
                valores.lastIndex
            )

        val a =
            valores.removeAt(
                valores.lastIndex
            )

        val op =
            operadores.removeAt(
                operadores.lastIndex
            )

        valores.add(
            when (op) {

                '+' ->
                    a + b

                '-' ->
                    a - b

                '*' ->
                    a * b

                '/' -> {

                    if (
                        b == 0.0
                    ) {
                        throw ArithmeticException()
                    }

                    a / b
                }

                else ->
                    throw IllegalArgumentException()
            }
        )
    }

    for (
        token in tokens
    ) {

        when {

            token.toDoubleOrNull()
                != null -> {

                valores.add(
                    token.toDouble()
                )
            }

            token == "(" -> {

                operadores.add('(')
            }

            token == ")" -> {

                while (
                    operadores.isNotEmpty() &&
                    operadores.last() != '('
                ) {

                    aplicar()
                }

                if (
                    operadores.isEmpty()
                ) {
                    throw IllegalArgumentException()
                }

                operadores.removeAt(
                    operadores.lastIndex
                )
            }

            token.length == 1 &&
                token[0] in
                charArrayOf(
                    '+',
                    '-',
                    '*',
                    '/'
                ) -> {

                val op =
                    token[0]

                while (
                    operadores.isNotEmpty() &&
                    operadores.last() != '(' &&
                    prioridade(
                        operadores.last()
                    ) >= prioridade(op)
                ) {

                    aplicar()
                }

                operadores.add(op)
            }

            else -> {

                throw IllegalArgumentException()
            }
        }
    }

    while (
        operadores.isNotEmpty()
    ) {

        if (
            operadores.last() == '('
        ) {
            throw IllegalArgumentException()
        }

        aplicar()
    }

    if (
        valores.size != 1
    ) {

        throw IllegalArgumentException()
    }

    return valores[0]
}

/* =========================================================
   FORMATAÇÃO
========================================================= */

private fun formatarNumero(
    numero: Double
): String {

    if (
        numero == numero.roundToInt().toDouble()
    ) {

        return numero
            .roundToInt()
            .toString()
    }

    return numero.toString()
}