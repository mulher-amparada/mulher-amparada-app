package com.mulheres

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import android.app.Activity
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class FinancasActivity : ComponentActivity() {

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

        setContent {

            CompositionLocalProvider(
                LocalOverscrollFactory provides null
            ) {

                FinancasTheme()
            }
        }
    }
}


/* =========================================================
   TEMA
========================================================= */

@Composable
private fun FinancasTheme() {

    val dark =
        isSystemInDarkTheme()

    val view =
        LocalView.current

    val window =
        (view.context as Activity).window

    SideEffect {

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
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

    MaterialTheme {

        FinancasScreen(
            dark = dark
        )
    }
}

/* =========================================================
   FONTE
========================================================= */

private val Quicksand =
    FontFamily(

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

private val Roxo =
    Color(0xFF9B5CFF)

private val RoxoClaro =
    Color(0xFF9B5CFF)

private val FundoEscuro =
    Color(0xFF000000)

private val CartaoEscuro =
    Color(0xFF0A0A0D)

private val CartaoInternoEscuro =
    Color(0xFF0D0D12)

private val BordaEscura =
    Color(0xFF1A1A20)

private val TextoEscuro =
    Color(0xFFFFFFFF)

private val TextoSuaveEscuro =
    Color(0xFF85858C)

private val FundoClaro =
    Color(0xFFF7F7FA)

private val CartaoClaro =
    Color(0xFFFFFFFF)

private val CartaoInternoClaro =
    Color(0xFFF0EFF4)

private val BordaClara =
    Color(0x1817171B)

private val TextoClaro =
    Color(0xFF17171B)

private val TextoSuaveClaro =
    Color(0xFF666671)


/* =========================================================
   TELA
========================================================= */

@Composable
private fun FinancasScreen(
    dark: Boolean
) {

    val fundo =
        if (dark)
            FundoEscuro
        else
            FundoClaro

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

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(fundo)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 22.dp,
                        vertical = 28.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    0.dp
                )
        ) {

            /* =================================================
               MARCA
            ================================================= */

            Marca(
                dark = dark
            )

            Spacer(
                modifier =
                    Modifier.height(25.dp)
            )


            /* =================================================
               DESTAQUE
            ================================================= */

            Destaque(
                dark = dark
            )


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            /* =================================================
               FINAL
            ================================================= */

            Spacer(
                modifier =
                    Modifier.height(15.dp)
            )

            Text(
                text =
                    "Conhecimento financeiro também é uma forma de proteção. 🌷",

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 10.dp,
                            vertical = 20.dp
                        ),

                color =
                    texto,

                fontFamily =
                    Quicksand,

                fontSize =
                    16.sp,

                fontWeight =
                    FontWeight.Bold,

                lineHeight =
                    24.sp,

                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(40.dp)
            )
        }
    }
}


/* =========================================================
   MARCA
========================================================= */

@Composable
private fun Marca(
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
            Modifier.fillMaxWidth(),

        horizontalAlignment =
            Alignment.Start
    ) {

        Box(
            modifier =
                Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(
                        RoundedCornerShape(
                            999.dp
                        )
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Roxo,
                                Color(0xFF6425D9)
                            )
                        )
                    )
        )

        Spacer(
            modifier =
                Modifier.height(13.dp)
        )

        Text(
            text =
                "Mulher Amparada",

            color =
                texto,

            fontFamily =
                Quicksand,

            fontSize =
                38.sp,

            lineHeight =
                40.sp,

            fontWeight =
                FontWeight.Bold,

            letterSpacing =
                (-1.5).sp
        )

        Spacer(
            modifier =
                Modifier.height(13.dp)
        )

        Text(
            text =
                "Educação financeira",

            color =
                textoSuave,

            fontFamily =
                Quicksand,

            fontSize =
                15.sp,

            lineHeight =
                23.sp,

            fontWeight =
                FontWeight.Medium
        )
    }
}


/* =========================================================
   DESTAQUE
========================================================= */

@Composable
private fun Destaque(
    dark: Boolean
) {

    val fundo =
        if (dark)
            CartaoEscuro
        else
            CartaoClaro

    val borda =
        if (dark)
            BordaEscura
        else
            BordaClara

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        30.dp
                    )
                )
                .background(
                    if (dark)
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color(0x2B9B5CFF),
                                    fundo
                                )
                        )
                    else
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color(0x229B5CFF),
                                    fundo
                                )
                        )
                )
                .border(
                    1.dp,
                    borda,
                    RoundedCornerShape(
                        30.dp
                    )
                )
                .padding(
                    30.dp
                )
    ) {

        TituloGrande(
            texto =
                "💰 Educação financeira e proteção do seu dinheiro",

            dark =
                dark
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Paragrafo(
            texto =
                "Usuárias, vou ensinar tudo o que eu sei sobre finanças para vocês, começando pelo mais urgente: como proteger o seu dinheiro.",

            dark =
                dark,

            negrito =
                true
        )

        Aviso(
            texto =
                "⚠️ Importante: os recursos disponíveis mudam de um banco para outro. Nem todas as funções abaixo existem em todos os aplicativos.",

            dark =
                dark
        )


        /* =====================================================
           CARTÃO
        ===================================================== */

        TituloGrande(
            texto =
                "🚨 E se o agressor estiver com o seu cartão?",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Se você ainda consegue acessar sua conta com segurança, existem algumas medidas que podem ajudar.",

            dark =
                dark
        )

        Subtitulo(
            texto =
                "1. Desative o pagamento por aproximação",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Alguns bancos permitem ativar ou desativar a função de pagamento por aproximação.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Assim, dependendo do banco, novos pagamentos por aproximação podem exigir outra forma de autenticação.",

            dark =
                dark
        )

        Subtitulo(
            texto =
                "2. Bloqueie o cartão",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Muitos aplicativos permitem bloquear temporariamente o cartão pelo próprio aplicativo.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "Em algumas situações, também é possível solicitar o cancelamento definitivo e pedir outro cartão.",

            dark =
                dark
        )


        /* =====================================================
           OUTRAS FORMAS
        ===================================================== */

        TituloGrande(
            texto =
                "🔐 Outras formas de proteger sua conta",

            dark =
                dark
        )

        Subtitulo(
            texto =
                "1. Defina limites",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Alguns bancos permitem configurar limites para determinadas operações ou pagamentos.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Isso pode ajudar a reduzir o impacto de uma movimentação indevida.",

            dark =
                dark
        )

        Aviso(
            texto =
                "⚠️ Lembre-se: se você colocar um limite muito baixo, você também poderá ficar limitada ao usar sua própria conta.",

            dark =
                dark
        )

        Subtitulo(
            texto =
                "2. Proteja o acesso ao aplicativo",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Alguns aplicativos permitem utilizar biometria, senha ou outros métodos de autenticação.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "Sempre que estiver disponível e for seguro para você, considere ativar a biometria.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Nunca compartilhe sua senha ou código de autenticação.",

            dark =
                dark,

            negrito =
                true
        )

        Subtitulo(
            texto =
                "3. Recursos baseados em localização",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Algumas instituições oferecem recursos de segurança que utilizam a localização do aparelho ou identificam situações consideradas diferentes do comportamento habitual.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Dependendo do serviço, isso pode ajudar a aplicar proteções adicionais.",

            dark =
                dark
        )

        Aviso(
            texto =
                "⚠️ Esse recurso não existe necessariamente em todos os bancos e pode depender de análise e configuração.",

            dark =
                dark,

            negrito =
                true
        )

        Subtitulo(
            texto =
                "4. Autorize seu aparelho",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Alguns bancos permitem cadastrar ou reconhecer o celular utilizado para acessar a conta.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Quando alguém tenta utilizar outro aparelho, podem ser solicitadas verificações adicionais de identidade.",

            dark =
                dark
        )

        Subtitulo(
            texto =
                "5. Utilize um cartão virtual",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Alguns bancos oferecem cartões virtuais que podem ser utilizados para determinadas compras.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Também existem formas de pagamento pelo celular, como carteiras digitais e aproximação pelo NFC.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Mas atenção: cartão virtual e pagamento por aproximação são coisas diferentes. O funcionamento depende do banco, do cartão e do celular.",

            dark =
                dark,

            negrito =
                true
        )


        /* =====================================================
           DICA
        ===================================================== */

        TituloGrande(
            texto =
                "🔎 Uma dica MUITO importante",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Se você estiver com pressa, não fique procurando desesperadamente pelo menu do aplicativo.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "Procure pelo botão de lupa 🔍 e pesquise palavras como:",

            dark =
                dark
        )

        Lista(
            itens =
                listOf(
                    "bloquear cartão",
                    "limite",
                    "segurança",
                    "cartão virtual",
                    "aproximação",
                    "dispositivo",
                    "biometria"
                ),

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Os nomes das funções podem mudar de banco para banco.",

            dark =
                dark
        )


        /* =====================================================
           DINHEIRO
        ===================================================== */

        TituloGrande(
            texto =
                "💵 Agora vamos falar sobre cuidar do seu dinheiro",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Uma coisa importante:",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "dinheiro físico e dinheiro digital possuem riscos diferentes.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "O dinheiro físico pode ser levado fisicamente por outra pessoa.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Já uma conta digital pode possuir várias camadas de segurança, como:",

            dark =
                dark
        )

        Lista(
            itens =
                listOf(
                    "senha",
                    "biometria",
                    "confirmação de identidade",
                    "limites",
                    "bloqueio do cartão",
                    "dispositivo autorizado",
                    "notificações de movimentação"
                ),

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Por isso, para quem precisa de maior controle sobre o próprio dinheiro, uma conta com bons recursos de segurança pode ser bastante útil.",

            dark =
                dark,

            negrito =
                true
        )

        Aviso(
            texto =
                "⚠️ Mas isso não significa que dinheiro digital seja impossível de movimentar por outra pessoa. Se alguém tiver acesso à sua senha, aparelho ou métodos de autenticação, ainda pode existir risco.",

            dark =
                dark
        )


        /* =====================================================
           JUROS
        ===================================================== */

        TituloGrande(
            texto =
                "📚 E os juros?",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Agora vem uma coisa que muita gente aprende apenas quando começa a lidar com dinheiro:",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "nem todo juro trabalha contra você.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "Existem situações em que você paga juros e outras em que você recebe juros.",

            dark =
                dark
        )

        Subtitulo(
            texto =
                "😟 Quando você deve dinheiro",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Imagine que você pegou R$ 1.000 emprestados.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Se houver juros e você demorar para pagar, a dívida poderá aumentar.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Nesse caso, os juros estão trabalhando contra você.",

            dark =
                dark,

            negrito =
                true
        )

        Subtitulo(
            texto =
                "🙂 Quando seu dinheiro está investido",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Agora imagine o contrário.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Você coloca dinheiro em um investimento que remunera você.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Nesse caso, você pode receber rendimentos ao longo do tempo.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Aqui, os juros estão trabalhando a seu favor.",

            dark =
                dark,

            negrito =
                true
        )

        Aviso(
            texto =
                "💸 Dívida: você pode pagar juros.\n\n💰 Investimento: você pode receber rendimentos.",

            dark =
                dark
        )


        /* =====================================================
           CDB
        ===================================================== */

        TituloGrande(
            texto =
                "🏦 E o CDB?",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "CDB significa Certificado de Depósito Bancário.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "Alguns aplicativos chamam determinadas funções de \"cofrinho\", \"caixinha\" ou nomes parecidos, mas isso não significa que todo cofrinho seja um CDB.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Um CDB é um investimento emitido por uma instituição financeira.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Por exemplo, você pode encontrar um CDB que ofereça uma rentabilidade de 110% do CDI.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Isso significa que a rentabilidade é calculada com base em 110% da variação do CDI, conforme as condições daquele produto.",

            dark =
                dark
        )

        Aviso(
            texto =
                "⚠️ Não significa que R$ 445 vão necessariamente render exatamente R$ 5 por mês. A rentabilidade depende das taxas do período, do produto e de outros fatores.",

            dark =
                dark
        )


        /* =====================================================
           IMPOSTOS
        ===================================================== */

        TituloGrande(
            texto =
                "🧾 E os impostos?",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Alguns investimentos possuem impostos sobre os rendimentos.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "Isso significa que você não deve olhar somente para a rentabilidade anunciada.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Sempre procure saber:",

            dark =
                dark
        )

        Aviso(
            texto =
                "\"Quanto vou receber depois dos impostos e das taxas?\"",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "O valor líquido é o que realmente importa para comparar investimentos.",

            dark =
                dark
        )


        /* =====================================================
           RESERVA
        ===================================================== */

        TituloGrande(
            texto =
                "🚑 CDB pode servir para uma reserva de emergência?",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Pode, dependendo do CDB.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "Para uma reserva de emergência, uma das características mais importantes é a liquidez — ou seja, a facilidade e o prazo para conseguir transformar o investimento em dinheiro.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Por isso, não basta olhar para \"110% do CDI\".",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Também é importante verificar:",

            dark =
                dark
        )

        Lista(
            itens =
                listOf(
                    "se existe liquidez diária",
                    "quando o dinheiro pode ser resgatado",
                    "quais são os impostos",
                    "quais são as taxas",
                    "qual é a instituição emissora",
                    "quais proteções se aplicam ao produto"
                ),

            dark =
                dark
        )


        /* =====================================================
           FUNDOS
        ===================================================== */

        TituloGrande(
            texto =
                "📊 E os fundos de investimento?",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Agora vamos para outro tipo de investimento.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Imagine uma grande \"caixinha\" onde várias pessoas colocam dinheiro.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Esse patrimônio é dividido em cotas, e o fundo investe o dinheiro conforme suas regras.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "Dependendo do fundo, ele pode investir em:",

            dark =
                dark
        )

        Lista(
            itens =
                listOf(
                    "títulos",
                    "ações",
                    "moedas",
                    "outros fundos",
                    "títulos de empresas",
                    "outros ativos"
                ),

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Você não está simplesmente \"emprestando dinheiro para os profissionais\".",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Você está adquirindo cotas de um fundo, cujo patrimônio é investido de acordo com uma política determinada.",

            dark =
                dark,

            negrito =
                true
        )


        /* =====================================================
           RISCO
        ===================================================== */

        TituloGrande(
            texto =
                "⚠️ Fundos podem perder dinheiro?",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Sim.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "E isso é muito importante entender.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Dependendo do fundo e dos investimentos que ele possui, o valor da sua cota pode subir ou cair.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Se os ativos do fundo se valorizarem, o fundo pode ganhar valor.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Se eles perderem valor, o fundo também pode perder valor.",

            dark =
                dark
        )

        Subtitulo(
            texto =
                "E a liquidez?",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Liquidez não significa chance de ganhar ou perder.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "Liquidez significa, principalmente, quanto tempo e quais condições são necessários para transformar seu investimento em dinheiro.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Alguns fundos permitem resgate rapidamente.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Outros possuem prazos maiores.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "E existem investimentos com prazo determinado em que você pode não conseguir retirar o dinheiro livremente antes do vencimento.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Sempre confira essas condições antes de investir.",

            dark =
                dark,

            negrito =
                true
        )


        /* =====================================================
           QUAL ESCOLHER
        ===================================================== */

        TituloGrande(
            texto =
                "👩‍🏫 Então qual escolher?",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Não existe um investimento que seja simplesmente \"o melhor\".",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Depende do objetivo.",

            dark =
                dark,

            negrito =
                true
        )

        Subtitulo(
            texto =
                "🟢 Para dinheiro que você pode precisar rapidamente",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Priorize entender:",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "segurança + liquidez + previsibilidade.",

            dark =
                dark,

            negrito =
                true
        )

        Subtitulo(
            texto =
                "🟡 Para objetivos de médio prazo",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Pode haver mais opções, dependendo da sua tolerância a risco.",

            dark =
                dark
        )

        Subtitulo(
            texto =
                "🔴 Para investimentos que podem oscilar bastante",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "É necessário entender que você pode ganhar, mas também pode perder dinheiro.",

            dark =
                dark,

            negrito =
                true
        )


        /* =====================================================
           FINALIZAÇÃO
        ===================================================== */

        TituloGrande(
            texto =
                "🌷 Para finalizar",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Seu dinheiro é seu.",

            dark =
                dark,

            negrito =
                true
        )

        Paragrafo(
            texto =
                "Você não precisa investir porque alguém mandou.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Não precisa escolher o investimento que promete o maior rendimento.",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "E não precisa ter vergonha de perguntar:",

            dark =
                dark
        )

        Aviso(
            texto =
                "\"Como exatamente meu dinheiro vai ser investido?\"",

            dark =
                dark
        )

        Paragrafo(
            texto =
                "Antes de colocar seu dinheiro em qualquer produto, procure entender:",

            dark =
                dark
        )

        Lista(
            itens =
                listOf(
                    "📌 Rentabilidade: quanto pode render?",
                    "📌 Risco: quanto posso perder?",
                    "📌 Liquidez: quando posso retirar?",
                    "📌 Impostos: quanto será descontado?",
                    "📌 Taxas: quanto vou pagar?",
                    "📌 Proteção: existe alguma garantia ou proteção aplicável?"
                ),

            dark =
                dark
        )

        Aviso(
            texto =
                "Conhecimento financeiro também é uma forma de proteção. 🌷",

            dark =
                dark,

            negrito =
                true
        )


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Bolhas()
    }
}


/* =========================================================
   TÍTULO GRANDE
========================================================= */

@Composable
private fun TituloGrande(
    texto: String,
    dark: Boolean
) {

    val cor =
        if (dark)
            TextoEscuro
        else
            TextoClaro

    Text(
        text =
            texto,

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 35.dp,
                    bottom = 18.dp
                ),

        color =
            cor,

        fontFamily =
            Quicksand,

        fontSize =
            24.sp,

        lineHeight =
            30.sp,

        fontWeight =
            FontWeight.Bold,

        letterSpacing =
            (-0.6).sp
    )
}


/* =========================================================
   SUBTÍTULO
========================================================= */

@Composable
private fun Subtitulo(
    texto: String,
    dark: Boolean
) {

    val cor =
        if (dark)
            TextoEscuro
        else
            TextoClaro

    Text(
        text =
            texto,

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 30.dp,
                    bottom = 12.dp
                ),

        color =
            cor,

        fontFamily =
            Quicksand,

        fontSize =
            18.sp,

        lineHeight =
            24.sp,

        fontWeight =
            FontWeight.Bold
    )
}


/* =========================================================
   PARÁGRAFO
========================================================= */

@Composable
private fun Paragrafo(
    texto: String,
    dark: Boolean,
    negrito: Boolean = false
) {

    val cor =
        if (dark)
            TextoSuaveEscuro
        else
            TextoSuaveClaro

    Text(
        text =
            texto,

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 9.dp
                ),

        color =
            cor,

        fontFamily =
            Quicksand,

        fontSize =
            14.sp,

        lineHeight =
            23.sp,

        fontWeight =
            if (negrito)
                FontWeight.Bold
            else
                FontWeight.Medium
    )
}


/* =========================================================
   AVISO / BLOCKQUOTE
========================================================= */

@Composable
private fun Aviso(
    texto: String,
    dark: Boolean,
    negrito: Boolean = false
) {

    val fundo =
        if (dark)
            CartaoInternoEscuro
        else
            CartaoInternoClaro

    val cor =
        if (dark)
            TextoSuaveEscuro
        else
            TextoSuaveClaro

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 14.dp
                )
                .clip(
                    RoundedCornerShape(
                        topEnd = 18.dp,
                        bottomEnd = 18.dp
                    )
                )
                .background(
                    fundo
                )
                .border(
                    width = 1.dp,
                    color =
                        if (dark)
                            Color(0xFF202027)
                        else
                            Color(0x12000000),
                    shape =
                        RoundedCornerShape(
                            topEnd = 18.dp,
                            bottomEnd = 18.dp
                        )
                )
    ) {

        Box(
            modifier =
                Modifier
                    .width(3.dp)
                    .height(1.dp)
                    .background(Roxo)
        )

        Text(
            text =
                texto,

            modifier =
                Modifier
                    .padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    ),

            color =
                cor,

            fontFamily =
                Quicksand,

            fontSize =
                14.sp,

            lineHeight =
                23.sp,

            fontWeight =
                if (negrito)
                    FontWeight.Bold
                else
                    FontWeight.Medium
        )
    }
}


/* =========================================================
   LISTA
========================================================= */

@Composable
private fun Lista(
    itens: List<String>,
    dark: Boolean
) {

    val cor =
        if (dark)
            TextoSuaveEscuro
        else
            TextoSuaveClaro

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 8.dp
                )
    ) {

        itens.forEach { item ->

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 5.dp
                        ),

                verticalAlignment =
                    Alignment.Top
            ) {

                Text(
                    text =
                        "•",

                    color =
                        Roxo,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        17.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.width(10.dp)
                )

                Text(
                    text =
                        item,

                    modifier =
                        Modifier.weight(1f),

                    color =
                        cor,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        14.sp,

                    lineHeight =
                        22.sp,

                    fontWeight =
                        FontWeight.Medium
                )
            }
        }
    }
}


/* =========================================================
   BOLHAS FINAIS
========================================================= */

@Composable
private fun Bolhas() {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 25.dp
                ),

        horizontalArrangement =
            Arrangement.spacedBy(
                7.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .clip(
                        RoundedCornerShape(
                            50.dp
                        )
                    )
                    .background(
                        Roxo.copy(
                            alpha = 0.8f
                        )
                    )
        )

        Box(
            modifier =
                Modifier
                    .size(6.dp)
                    .clip(
                        RoundedCornerShape(
                            50.dp
                        )
                    )
                    .background(
                        Roxo.copy(
                            alpha = 0.45f
                        )
                    )
        )

        Box(
            modifier =
                Modifier
                    .size(11.dp)
                    .clip(
                        RoundedCornerShape(
                            50.dp
                        )
                    )
                    .background(
                        Roxo.copy(
                            alpha = 0.2f
                        )
                    )
        )
    }
}