package com.mulheres

import android.content.Context
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay

class NeoCalcActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightNavigationBars = false
            isAppearanceLightStatusBars = false
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

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
            fundo = Color.White,
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

private enum class AcaoTelaPreta {
    HUB,
    RECUPERACAO,
    RESETAR
}

@Composable
private fun NeoCalcScreen() {
    val context = LocalContext.current
    val cripto = remember { Cripto(context) }
    val escuro = isSystemInDarkTheme()
    val cores = remember(escuro) { obterCoresNeoCalc(escuro) }

    var telaPreta by remember { mutableStateOf(false) }
    var acaoTelaPreta by remember {
        mutableStateOf(AcaoTelaPreta.HUB)
    }

    var expr by remember { mutableStateOf("") }
    var mensagemTemporaria by remember { mutableStateOf<String?>(null) }
    var dialogo by remember { mutableStateOf<Dialogo?>(null) }
    var valorDialogo by remember { mutableStateOf("") }

    var contadorToques by remember { mutableIntStateOf(0) }
    var ultimoToque by remember { mutableLongStateOf(0L) }
    val visorScroll = rememberScrollState()
val visorScrollVertical = rememberScrollState()

    
    LaunchedEffect(expr, mensagemTemporaria) {
    delay(1)

    visorScroll.scrollTo(visorScroll.maxValue)
    visorScrollVertical.scrollTo(
        visorScrollVertical.maxValue
    )
}

    fun atualizar() {
        mensagemTemporaria = null
    }

    fun mostrarDialogo(novo: Dialogo) {
        dialogo = novo
        valorDialogo = novo.valorInicial
    }

    fun ativarTelaPreta(acao: AcaoTelaPreta) {
        acaoTelaPreta = acao
        telaPreta = true
    }

    fun registrarToque() {
        val agora = System.currentTimeMillis()

        if (agora - ultimoToque > 2000L) {
            contadorToques = 0
        }

        ultimoToque = agora
        contadorToques++

        if (contadorToques >= 5) {
            contadorToques = 0

            recuperarSenha(
                context = context,
                cripto = cripto,
                mostrarDialogo = { mostrarDialogo(it) },
                fecharDialogo = { dialogo = null },
                mostrarMensagem = { mensagemTemporaria = it }
            )
        }
    }

    fun adicionar(valor: String) {
        if (valor == ".") {
            val partes = expr.split(Regex("[+\\-*/()]"))
            val parteAtual = partes.lastOrNull() ?: ""
            if (parteAtual.contains(".")) return
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

        if (
            expr.lastOrNull()?.toString()
                ?.matches(Regex("[+\\-*/]")) == true
        ) {
            expr = expr.dropLast(1) + op
        } else {
            expr += op
        }

        atualizar()
    }

    fun porcentagem() {
        if (expr.isEmpty()) return

        expr = expr.replace(
            Regex("(\\d+(?:\\.\\d+)?)$"),
            "($1/100)"
        )

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
        } catch (_: Exception) {
            ""
        }

        if (expr.trim().isNotEmpty() && expr.trim() == senha) {
            expr = ""
            mensagemTemporaria = null
            ativarTelaPreta(AcaoTelaPreta.HUB)
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
        } catch (_: Exception) {
            // A expressão será tratada como erro abaixo.
        }

        mensagemTemporaria = "Erro"
        expr = ""
    }

    if (telaPreta) {
        var toquesTela by remember { mutableIntStateOf(0) }

        val corTelaBloqueio =
            if (escuro)
                Color.Black
            else
                Color.White

        LaunchedEffect(corTelaBloqueio) {
            val activity = context as? FragmentActivity
            activity?.let { act ->
                WindowCompat.getInsetsController(
                    act.window,
                    act.window.decorView
                ).apply {
                    val claro = corTelaBloqueio == Color.White
                    isAppearanceLightStatusBars = claro
                    isAppearanceLightNavigationBars = claro
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(corTelaBloqueio)
                .pointerInput(Unit) {
                    detectTapGestures {
                        toquesTela++

                        if (toquesTela >= 5) {
                            telaPreta = false

                            when (acaoTelaPreta) {
                                AcaoTelaPreta.HUB -> {
                                    context.startActivity(
                                        Intent(
                                            context,
                                            HubActivity::class.java
                                        )
                                    )
                                }

                                AcaoTelaPreta.RECUPERACAO -> {
                                    mostrarDialogo(
                                        Dialogo(
                                            tipo = TipoDialogo.PROMPT,
                                            titulo = "Nova senha",
                                            mensagem = "Digite a nova senha.",
                                            textoConfirmar = "Salvar"
                                        )
                                    )
                                }

                                AcaoTelaPreta.RESETAR -> {
                                    continuarConfirmacao(
                                        context = context,
                                        cripto = cripto,
                                        mostrarDialogo = {
                                            mostrarDialogo(it)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
        )

        return
    }

    CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(cores.fundo)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
            val paisagem = maxWidth > maxHeight

            if (paisagem) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
    modifier = Modifier
        .weight(0.9f)
        .fillMaxHeight()
        .pointerInput(Unit) {
            detectTapGestures {
                registrarToque()
            }
        }
        .padding(start = 14.dp, end = 4.dp)
        .verticalScroll(visorScrollVertical)
        .horizontalScroll(visorScroll),
    contentAlignment = Alignment.CenterEnd
) {
    Text(
        text = mensagemTemporaria ?: expr.ifEmpty { "0" },
        color = cores.texto,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        textAlign = TextAlign.End,
        softWrap = true
    )
}

                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .padding(vertical = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val botoes = listaBotoes(
                            adicionar = ::adicionar,
                            operacao = ::operacao,
                            limpar = ::limpar,
                            apagar = ::apagar,
                            porcentagem = ::porcentagem,
                            calcular = ::calcular
                        )

                        botoes.forEach { linha ->
                            LinhaBotoes(
                                cores = cores,
                                botoes = linha,
                                modifier = Modifier.weight(1f),
                                paisagem = true
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Transparent)
                            .horizontalScroll(visorScroll)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    registrarToque()
                                }
                            }
                            .padding(20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = mensagemTemporaria ?: expr.ifEmpty { "0" },
                            color = cores.texto,
                            fontSize = 54.sp,
                            lineHeight = 58.sp,
                            textAlign = TextAlign.End,
                            maxLines = 4
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val botoes = listaBotoes(
                            adicionar = ::adicionar,
                            operacao = ::operacao,
                            limpar = ::limpar,
                            apagar = ::apagar,
                            porcentagem = ::porcentagem,
                            calcular = ::calcular
                        )

                        botoes.forEach { linha ->
                            LinhaBotoes(
                                cores = cores,
                                botoes = linha
                            )
                        }
                    }
                }
            }

            dialogo?.let { atual ->
                AlertDialog(
                    onDismissRequest = {
                        if (atual.tipo != TipoDialogo.ALERTA) {
                            dialogo = null
                        }
                    },
                    title = {
                        Text(atual.titulo, color = cores.texto)
                    },
                    text = {
                        Column {
                            Text(
                                atual.mensagem,
                                color = cores.textoSecundario,
                                lineHeight = 23.sp
                            )

                            if (
                                atual.tipo == TipoDialogo.PROMPT ||
                                atual.tipo == TipoDialogo.RECUPERACAO
                            ) {
                                Spacer(Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = valorDialogo,
                                    onValueChange = { valorDialogo = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = cores.texto,
                                        unfocusedTextColor = cores.texto,
                                        focusedBorderColor = cores.acao,
                                        unfocusedBorderColor = if (escuro) {
                                            Color(0xFF353535)
                                        } else {
                                            Color(0xFFBDBDBD)
                                        },
                                        cursorColor = cores.acao
                                    )
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                when (atual.tipo) {
                                    TipoDialogo.ALERTA -> {
                                        if (atual.titulo == "Esqueci tudo") {
                                            dialogo = null

                                            autenticarParaRedefinir(
                                                activity = context as FragmentActivity,
                                                mostrarDialogo = {
                                                    mostrarDialogo(it)
                                                },
                                                mostrarMensagem = {
                                                    mensagemTemporaria = it
                                                }
                                            )
                                        } else {
                                            dialogo = null
                                        }
                                    }

                                    TipoDialogo.PROMPT -> {
                                        val valor = valorDialogo
                                        dialogo = null

                                        continuarPrompt(
                                            valor = valor,
                                            titulo = atual.titulo,
                                            context = context,
                                            cripto = cripto,
                                            mostrarDialogo = {
                                                mostrarDialogo(it)
                                            },
                                            mostrarMensagem = {
                                                mensagemTemporaria = it
                                            }
                                        )
                                    }

                                    TipoDialogo.CONFIRMACAO -> {
                                        dialogo = null
                                        ativarTelaPreta(AcaoTelaPreta.RESETAR)
                                    }

                                    TipoDialogo.RECUPERACAO -> {
                                        val valor = valorDialogo
                                        dialogo = null

                                        continuarRecuperacao(
                                            valor = valor,
                                            context = context,
                                            cripto = cripto,
                                            mostrarDialogo = {
                                                mostrarDialogo(it)
                                            },
                                            mostrarMensagem = {
                                                mensagemTemporaria = it
                                            },
                                            ativarTelaPreta = {
                                                ativarTelaPreta(
                                                    AcaoTelaPreta.RECUPERACAO
                                                )
                                            }
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cores.acao
                            )
                        ) {
                            Text(atual.textoConfirmar, color = Color.White)
                        }
                    },
                    dismissButton = {
                        when (atual.tipo) {
                            TipoDialogo.RECUPERACAO -> {
                                TextButton(
                                    onClick = {
                                        dialogo = Dialogo(
                                            tipo = TipoDialogo.ALERTA,
                                            titulo = "Esqueci tudo",
                                            mensagem = "Use o bloqueio de tela do dispositivo para confirmar sua identidade e permitir a redefinição da senha.",
                                            textoConfirmar = "Continuar"
                                        )
                                    }
                                ) {
                                    Text("Esqueci tudo", color = cores.texto)
                                }
                            }

                            TipoDialogo.PROMPT,
                            TipoDialogo.CONFIRMACAO -> {
                                TextButton(onClick = { dialogo = null }) {
                                    Text("Cancelar", color = cores.texto)
                                }
                            }

                            TipoDialogo.ALERTA -> Unit
                        }
                    },
                    containerColor = cores.botao
                )
            }
        }
    }
}

private fun listaBotoes(
    adicionar: (String) -> Unit,
    operacao: (String) -> Unit,
    limpar: () -> Unit,
    apagar: () -> Unit,
    porcentagem: () -> Unit,
    calcular: () -> Unit
): List<List<BotaoCalculadora>> {
    return listOf(
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
            BotaoCalculadora("()") { adicionar("()") },
            BotaoCalculadora("0") { adicionar("0") },
            BotaoCalculadora(".") { adicionar(".") },
            BotaoCalculadora("=", TipoBotao.IGUAL) { calcular() }
        )
    )
}

private enum class TipoBotao {
    NORMAL,
    ACAO,
    OPERADOR,
    IGUAL
}

private data class BotaoCalculadora(
    val texto: String,
    val tipo: TipoBotao = TipoBotao.NORMAL,
    val icone: Int? = null,
    val acao: () -> Unit
)

@Composable
private fun LinhaBotoes(
    cores: CoresNeoCalc,
    botoes: List<BotaoCalculadora>,
    modifier: Modifier = Modifier,
    paisagem: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            if (paisagem) 5.dp else 10.dp
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        botoes.forEach { botao ->
            val cor = when (botao.tipo) {
                TipoBotao.ACAO -> cores.acao
                TipoBotao.OPERADOR,
                TipoBotao.IGUAL -> cores.operador
                TipoBotao.NORMAL -> cores.botao
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (paisagem) {
                            Modifier.fillMaxHeight()
                        } else {
                            Modifier.aspectRatio(1f)
                        }
                    )
                    .clip(CircleShape)
                    .background(cor)
                    .clickable { botao.acao() },
                contentAlignment = Alignment.Center
            ) {
                if (botao.icone != null) {
                    Image(
                        painter = painterResource(botao.icone),
                        contentDescription = "Apagar",
                        modifier = Modifier.size(
                            if (paisagem) 18.dp else 28.dp
                        )
                    )
                } else {
                    Text(
                        text = botao.texto,
                        color = cores.texto,
                        fontSize = if (paisagem) 17.sp else 25.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun recuperarSenha(
    context: Context,
    cripto: Cripto,
    mostrarDialogo: (Dialogo) -> Unit,
    fecharDialogo: () -> Unit,
    mostrarMensagem: (String) -> Unit
) {
    val senhaSalva = try {
        cripto.carregar(CHAVE_SENHA)?.trim() ?: ""
    } catch (_: Exception) {
        mostrarMensagem("Erro")
        return
    }

    if (senhaSalva.isEmpty()) {
        mostrarDialogo(
            Dialogo(
                tipo = TipoDialogo.PROMPT,
                titulo = "Criar senha",
                mensagem = "Digite uma senha para proteger esta área.",
                textoConfirmar = "Criar"
            )
        )
        return
    }

    mostrarDialogo(
        Dialogo(
            tipo = TipoDialogo.RECUPERACAO,
            titulo = "Recuperar senha",
            mensagem = "Como você gosta de ser chamada?",
            textoConfirmar = "OK"
        )
    )
}

private fun continuarPrompt(
    valor: String,
    titulo: String,
    context: Context,
    cripto: Cripto,
    mostrarDialogo: (Dialogo) -> Unit,
    mostrarMensagem: (String) -> Unit
) {
    val valorLimpo = valor.trim()

    if (titulo == "Criar senha") {
        if (valorLimpo.isEmpty()) {
            mostrarMensagem("Senha inválida")
            return
        }

        cripto.salvar(CHAVE_SENHA, valorLimpo)

        mostrarDialogo(
            Dialogo(
                tipo = TipoDialogo.PROMPT,
                titulo = "Recuperação",
                mensagem = "Como você gosta de ser chamada?",
                textoConfirmar = "Salvar"
            )
        )
        return
    }

    if (titulo == "Recuperação") {
        if (valorLimpo.isEmpty()) {
            mostrarMensagem("Resposta inválida")
            return
        }

        cripto.salvar(CHAVE_RECUPERACAO, valorLimpo.lowercase())
        mostrarMensagem("Senha criada")
        return
    }

    if (titulo == "Nova senha") {
        if (valorLimpo.isEmpty()) {
            mostrarMensagem("Senha inválida")
            return
        }

        cripto.salvar(CHAVE_SENHA, valorLimpo)

        mostrarDialogo(
            Dialogo(
                tipo = TipoDialogo.PROMPT,
                titulo = "Nova recuperação",
                mensagem = "Como você gosta de ser chamada?",
                textoConfirmar = "Salvar"
            )
        )
        return
    }

    if (titulo == "Nova recuperação") {
        if (valorLimpo.isEmpty()) {
            mostrarMensagem("Resposta inválida")
            return
        }

        cripto.salvar(CHAVE_RECUPERACAO, valorLimpo.lowercase())
        mostrarMensagem("Senha alterada")
    }
}

private fun continuarRecuperacao(
    valor: String,
    context: Context,
    cripto: Cripto,
    mostrarDialogo: (Dialogo) -> Unit,
    mostrarMensagem: (String) -> Unit,
    ativarTelaPreta: () -> Unit
) {
    val valorLimpo = valor.trim()

    val respostaSalva = try {
        cripto.carregar(CHAVE_RECUPERACAO)?.trim() ?: ""
    } catch (_: Exception) {
        ""
    }

    if (
        respostaSalva.isNotEmpty() &&
        valorLimpo.equals(respostaSalva, ignoreCase = true)
    ) {
        ativarTelaPreta()
        return
    }

    mostrarDialogo(
        Dialogo(
            tipo = TipoDialogo.ALERTA,
            titulo = "Resposta incorreta",
            mensagem = "A resposta de recuperação não corresponde à cadastrada.",
            textoConfirmar = "OK"
        )
    )
}

private fun autenticarParaRedefinir(
    activity: FragmentActivity,
    mostrarDialogo: (Dialogo) -> Unit,
    mostrarMensagem: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)

    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)

                mostrarDialogo(
                    Dialogo(
                        tipo = TipoDialogo.CONFIRMACAO,
                        titulo = "Redefinir senha",
                        mensagem = "Deseja redefinir a senha e a pergunta de recuperação?",
                        textoConfirmar = "Redefinir"
                    )
                )
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)

                if (
                    errorCode !=
                    BiometricPrompt.ERROR_USER_CANCELED
                ) {
                    mostrarMensagem("Autenticação não concluída")
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                mostrarMensagem("Autenticação não concluída")
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Confirmar identidade")
        .setSubtitle("Use o bloqueio de tela do dispositivo")
        .setDescription(
            "Confirme sua identidade para redefinir a senha."
        )
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    prompt.authenticate(promptInfo)
}

private fun continuarConfirmacao(
    context: Context,
    cripto: Cripto,
    mostrarDialogo: (Dialogo) -> Unit
) {
    cripto.salvar(CHAVE_SENHA, "")
    cripto.salvar(CHAVE_RECUPERACAO, "")

    mostrarDialogo(
        Dialogo(
            tipo = TipoDialogo.PROMPT,
            titulo = "Nova senha",
            mensagem = "Digite a nova senha.",
            textoConfirmar = "Salvar"
        )
    )
}

private fun avaliarExpressao(expressao: String): Double {
    val tokens = mutableListOf<String>()
    var numero = ""

    for (caractere in expressao) {
        if (caractere.isDigit() || caractere == '.') {
            numero += caractere
        } else {
            if (numero.isNotEmpty()) {
                tokens.add(numero)
                numero = ""
            }
            tokens.add(caractere.toString())
        }
    }

    if (numero.isNotEmpty()) {
        tokens.add(numero)
    }

    val valores = mutableListOf<Double>()
    val operadores = mutableListOf<Char>()

    fun prioridade(operador: Char): Int {
        return when (operador) {
            '+', '-' -> 1
            '*', '/' -> 2
            else -> 0
        }
    }

    fun aplicar() {
        if (valores.size < 2 || operadores.isEmpty()) {
            throw IllegalArgumentException()
        }

        val b = valores.removeAt(valores.lastIndex)
        val a = valores.removeAt(valores.lastIndex)
        val op = operadores.removeAt(operadores.lastIndex)

        valores.add(
            when (op) {
                '+' -> a + b
                '-' -> a - b
                '*' -> a * b
                '/' -> {
                    if (b == 0.0) {
                        throw ArithmeticException()
                    }
                    a / b
                }
                else -> throw IllegalArgumentException()
            }
        )
    }

    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> {
                valores.add(token.toDouble())
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

                if (operadores.isEmpty()) {
                    throw IllegalArgumentException()
                }

                operadores.removeAt(operadores.lastIndex)
            }

            token.length == 1 &&
                token[0] in charArrayOf('+', '-', '*', '/') -> {
                val op = token[0]

                while (
                    operadores.isNotEmpty() &&
                    operadores.last() != '(' &&
                    prioridade(operadores.last()) >= prioridade(op)
                ) {
                    aplicar()
                }

                operadores.add(op)
            }

            else -> throw IllegalArgumentException()
        }
    }

    while (operadores.isNotEmpty()) {
        if (operadores.last() == '(') {
            throw IllegalArgumentException()
        }
        aplicar()
    }

    if (valores.size != 1) {
        throw IllegalArgumentException()
    }

    return valores[0]
}

private fun formatarNumero(numero: Double): String {
    if (!numero.isFinite()) return "Erro"

    return numero
        .toBigDecimal()
        .stripTrailingZeros()
        .toPlainString()
}