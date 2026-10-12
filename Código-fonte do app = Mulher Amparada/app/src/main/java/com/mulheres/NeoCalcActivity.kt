Package com.mulheres

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.view.WindowManager
import androidx.compose.ui.res.painterResource
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class NeoCalcActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            NeoCalcScreen()
        }
    }
}

private data class CoresNeoCalc(
    val fundo: Color,
    val botao: Color,
    val botaoHover: Color,
    val acao: Color,
    val acaoHover: Color,
    val operador: Color,
    val operadorHover: Color,
    val texto: Color,
    val textoSecundario: Color
)

private fun obterCoresNeoCalc(escuro: Boolean): CoresNeoCalc {
    return if (escuro) {
        CoresNeoCalc(
            fundo = Color.Black,
            botao = Color(0xFF151515),
            botaoHover = Color(0xFF202020),
            acao = Color(0xFF6F7AFF),
            acaoHover = Color(0xFF686DFF),
            operador = Color(0xFF3DA7FF),
            operadorHover = Color(0xFF61B7FF),
            texto = Color.White,
            textoSecundario = Color(0xFFD0D0D0)
        )
    } else {
        CoresNeoCalc(
            fundo = Color(0xFFFFFFFF),
            botao = Color(0xFFE9E9E9),
            botaoHover = Color(0xFFDCDCDC),
            acao = Color(0xFF5964E8),
            acaoHover = Color(0xFF4F59D6),
            operador = Color(0xFF1976D2),
            operadorHover = Color(0xFF1565C0),
            texto = Color(0xFF111111),
            textoSecundario = Color(0xFF555555)
        )
    }
}

private const val CHAVE_SENHA = "senha"
private const val CHAVE_RECUPERACAO = "nome_recuperacao"

private enum class TipoDialogo {
    ALERTA,
    PROMPT,
    CONFIRMACAO,
    RECUPERACAO
}

private data class Dialogo(
    val tipo: TipoDialogo,
    val titulo: String,
    val mensagem: String,
    val valorInicial: String = "",
    val textoConfirmar: String = "OK"
)

@Composable
private fun NeoCalcScreen() {
    val context = LocalContext.current
    val cripto = remember { Cripto(context) }
    val escuro = isSystemInDarkTheme()
    val cores = remember(escuro) { obterCoresNeoCalc(escuro) }
    
    var expr by remember { mutableStateOf("") }
    var mensagemTemporaria by remember { mutableStateOf<String?>(null) }
    var dialogo by remember { mutableStateOf<Dialogo?>(null) }
    var valorDialogo by remember { mutableStateOf("") }
    var contadorToques by remember { mutableStateOf(0) }
    var ultimoToque by remember { mutableLongStateOf(0L) }

    val visorScroll = rememberScrollState()

    LaunchedEffect(expr, mensagemTemporaria) {
        delay(1)
        visorScroll.scrollTo(visorScroll.maxValue)
    }

    fun registrarToque() {
        val agora = System.currentTimeMillis()
        if (agora - ultimoToque > 2000) {
            contadorToques = 0
        }
        ultimoToque = agora
        contadorToques++

        if (contadorToques >= 5) {
            contadorToques = 0
            recuperarSenha(
                context = context,
                cripto = cripto,
                mostrarDialogo = {
                    dialogo = it
                    valorDialogo = it.valorInicial
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

    fun atualizar() {
        mensagemTemporaria = null
    }

    fun adicionar(valor: String) {
        if (valor == ".") {
            val partes = expr.split(Regex("[+\\-*/()]"))
            val parteAtual = partes.lastOrNull() ?: ""
            if (parteAtual.contains(".")) {
                return
            }
        }
        expr += valor
        atualizar()
    }

    fun operacao(op: String) {
        if (expr.isEmpty()) {
            if (op == "-") {
                expr = "-"
                atualizar()
            }
            return
        }

        if (expr.lastOrNull()?.toString()?.matches(Regex("[+\\-*/]")) == true) {
            expr = expr.dropLast(1) + op
        } else {
            expr += op
        }
        atualizar()
    }

    fun porcentagem() {
        if (expr.isEmpty()) return
        expr = expr.replace(Regex("(\\d+(?:\\.\\d+)?)$"), "($1/100)")
        atualizar()
    }

    fun limpar() {
        expr = ""
        atualizar()
    }

    fun apagar() {
        if (expr.isNotEmpty()) {
            expr = expr.dropLast(1)
            atualizar()
        }
    }

    fun calcular() {
        val senha = try {
            cripto.carregar(CHAVE_SENHA)?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }

        if (expr.trim().isNotEmpty() && expr.trim() == senha) {
            try {
                context.startActivity(Intent(context, HubActivity::class.java))
            } catch (e: Exception) {
                mensagemTemporaria = "Acesso"
            }
            return
        }

        if (expr.trim().isEmpty()) return

        try {
            val resultado = avaliarExpressao(expr)
            if (resultado.isFinite()) {
                expr = formatarNumero(resultado)
                mensagemTemporaria = null
                return
            }
        } catch (e: Exception) {}

        mensagemTemporaria = "Erro"
        expr = ""
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(cores.fundo)
                .padding(horizontal = 20.dp)
        ) {
            if (maxWidth > maxHeight) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val botoes = listOf(
                            listOf(
                                BotaoCalculadora("AC", TipoBotao.ACAO) { limpar() },
                                BotaoCalculadora("", TipoBotao.ACAO, R.drawable.ic_erase) { apagar() },
                                BotaoCalculadora("%", TipoBotao.ACAO) { porcentagem() },
                                BotaoCalculadora("÷", TipoBotao.OPERADOR) { operacao("/") }
                            ),
                            listOf(
                                BotaoCalculadora("7") { adicionar("7") },
                                BotaoCalculadora("8") { adicionar("8") },
                                BotaoCalculadora("9") { adicionar("9") },
                                BotaoCalculadora("×", TipoBotao.OPERADOR) { operacao("*") }
                            ),
                            listOf(
                                BotaoCalculadora("4") { adicionar("4") },
                                BotaoCalculadora("5") { adicionar("5") },
                                BotaoCalculadora("6") { adicionar("6") },
                                BotaoCalculadora("−", TipoBotao.OPERADOR) { operacao("-") }
                            ),
                            listOf(
                                BotaoCalculadora("1") { adicionar("1") },
                                BotaoCalculadora("2") { adicionar("2") },
                                BotaoCalculadora("3") { adicionar("3") },
                                BotaoCalculadora("+", TipoBotao.OPERADOR) { operacao("+") }
                            ),
                            listOf(
                                BotaoCalculadora("(") { adicionar("(") },
                                BotaoCalculadora(")") { adicionar(")") },
                                BotaoCalculadora("0") { adicionar("0") },
                                BotaoCalculadora(".") { adicionar(".") },
                                BotaoCalculadora("=", TipoBotao.IGUAL) { calcular() }
                            )
                        )

                        botoes.forEach { linha ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                linha.forEach { botao ->
                                    val cor = when (botao.tipo) {
                                        TipoBotao.ACAO -> cores.acao
                                        TipoBotao.OPERADOR, TipoBotao.IGUAL -> cores.operador
                                        TipoBotao.NORMAL -> cores.botao
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(CircleShape)
                                            .background(cor)
                                            .clickable { botao.acao() },
                                        contentAlignment = Alignment.Center
                                    ) {
