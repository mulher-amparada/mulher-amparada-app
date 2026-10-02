package com.mulheres

import androidx.compose.foundation.layout.offset
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.enableEdgeToEdge

private val Quicksand =
    FontFamily(
        Font(R.font.quicksand, FontWeight.Normal),
        Font(R.font.quicksand, FontWeight.Medium),
        Font(R.font.quicksand, FontWeight.SemiBold),
        Font(R.font.quicksand, FontWeight.Bold),
        Font(R.font.quicksand, FontWeight.ExtraBold)
    )

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AboutTheme {
                AboutScreen()
            }
        }
    }
}

@Composable
private fun AboutTheme(
    content: @Composable () -> Unit
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()

    val fundo =
        if (dark) {
            Color.Black
        } else {
            Color(0xFFF7F7FA)
        }

    val view = androidx.compose.ui.platform.LocalView.current

    SideEffect {
        val window =
            (view.context as android.app.Activity).window

        

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = fundo
        ) {
            content()
        }
    }
}

@Composable
private fun AboutScreen() {

    val dark = androidx.compose.foundation.isSystemInDarkTheme()

    val fundo =
        if (dark) Color.Black
        else Color(0xFFF7F7FA)

    val card =
        if (dark) Color(0xFF0A0A0D)
        else Color.White

    val borda =
        if (dark) Color(0xFF19191E)
        else Color(0xFFE4E3E9)

    val texto =
        if (dark) Color.White
        else Color(0xFF17171B)

    val secundario =
        if (dark) Color(0xFF85858B)
        else Color(0xFF66666E)

    val roxo = Color(0xFF9B5CFF)

    androidx.compose.runtime.CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fundo)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 22.dp,
                        end = 22.dp,
                        top = 58.dp,
                        bottom = 60.dp
                    )
            ) {

                Marca(
                    texto = texto,
                    secundario = secundario,
                    roxo = roxo
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Destaque(
                    dark = dark,
                    card = card,
                    borda = borda,
                    texto = texto,
                    secundario = secundario,
                    roxo = roxo
                )
            }
        }
    }
}

@Composable
private fun Marca(
    texto: Color,
    secundario: Color,
    roxo: Color
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 27.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Box(
            modifier = Modifier
                .width(44.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(roxo)
        )

        Spacer(
            modifier = Modifier.height(13.dp)
        )

        Text(
            text = "Mulher Amparada",
            color = texto,
            fontFamily = Quicksand,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1.5).sp,
            lineHeight = 40.sp
        )

        Spacer(
            modifier = Modifier.height(13.dp)
        )

        Text(
            text = "Uma carta para você!",
            color = secundario,
            fontFamily = Quicksand,
            fontSize = 15.sp,
            lineHeight = 23.sp
        )
    }
}

@Composable
private fun Destaque(
    dark: Boolean,
    card: Color,
    borda: Color,
    texto: Color,
    secundario: Color,
    roxo: Color
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        roxo.copy(alpha = if (dark) 0.17f else 0.10f),
                        card
                    ),
                    radius = 650f
                )
            )
            .border(
                BorderStroke(1.dp, borda),
                RoundedCornerShape(30.dp)
            )
    ) {

        // Bolha superior direita
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(
                    x = 45.dp,
                    y = (-85).dp
                )
                .clip(CircleShape)
                .background(
                    roxo.copy(
                        alpha = if (dark) 0.09f else 0.06f
                    )
                )
        )

        // Bolha inferior
        Box(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.BottomStart)
                .offset(
                    x = 35.dp,
                    y = 32.dp
                )
                .clip(CircleShape)
                .background(
                    Color.White.copy(
                        alpha = if (dark) 0.025f else 0.08f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp)
        ) {

            TituloGrande(
                "Uma carta para você, mulher!:",
                texto
            )

            Paragrafo(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            "E eu me inspiro em Martin Luther King para lhes dizer " +
                                "(não é uma citação direta dele!):"
                        )
                    }
                },
                secundario
            )

            Citacao(
                texto = "\"eu tenho um sonho, que as mulheres vivam em paz, sem nenhuma violência, porque todos somos iguais...\"",
                textoCor = secundario,
                fundo = if (dark) Color(0xFF0D0D12) else Color(0xFFF0EFF5),
                roxo = roxo
            )

            Paragrafo(
                "e eu me sinto lisongeado de fazer esse projeto para todas essas usuárias, " +
                    "(vocês são mulheres do jeitinho que são!)",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "e honestamente, cada uma de vocês são importantes (tanto em talento e etc), " +
                            "e pelo amor de Deus, não deixem que esses agressores tirem o brilho de vocês..., " +
                            "vocês são as pessoas mais maravilhosas!, cada uma de vocês existe um valor que " +
                            "as vezes não conseguem perceber pela violência, mas ele existe, ele está aí, " +
                            "então nunca desista de você mesma, ainda tem gente que te ama e quer o seu bem!, " +
                            "(não precisa ter culpa ou vergonha por essa violência, você "
                    )

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("NÃO TEM CULPA DISSO!")
                    }

                    append(
                        "), lembre-se que você é forte, só de pedir ajuda já fez um grande passo!, e lembre-se " +
                            "que você é uma pessoa virtuosa, e que seu valor excede a de muitos rubis, e fica " +
                            "com esse fato aqui: "
                    )

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            "QUEM TEM CULPA DA VIOLÊNCIA É O AGRESSOR, NÃO VOCÊ!"
                        )
                    }
                },
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "Geralmente, o padrão do agressor é assim, mas isso varia: te humilha, pede desculpas " +
                            "e fala que vai mudar, você acredita, te humilha denovo... " +
                            "(Esse comportamento é um padrão reconhecido pela psicologia como o "
                    )

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Ciclo da Violência")
                    }

                    append(")")
                },
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "então, não acredite nas mentiras dele, acredite em si mesma e na esperança que "
                    )

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            "VOCÊ NÃO ESTÁ SOZINHA E QUE PODE SEMPRE PEDIR AJUDA!"
                        )
                    }
                },
                secundario
            )

            TituloGrande(
                "Números de apoio:",
                texto
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "uma dica valiosa: se você estiver em sofrimento, ligue para o "
                    )

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            "CVV = Centro de Valorização da Vida = 188"
                        )
                    }

                    append(
                        ", gratuito, sigiloso, funciona 24 horas, e você não precisa de créditos no chip " +
                            "para ligar pra esse número e tem profissionais treinados para ouvir você!, " +
                            "mas ele serve para apoio emocional, e não um canal de denúncia como a polícia!"
                    )
                },
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "se quiser denunciar algo, ligue para o "
                    )

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("190 = polícia")
                    }

                    append(
                        ", e "
                    )

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("180 = Central de Atendimento à Mulher")
                    }

                    append(".")
                },
                secundario
            )

            TituloGrande(
                "A minha palavra favorita do português!:",
                texto
            )

            Paragrafo(
                "e se eu fosse escolher a palavra mais bonita da língua portuguesa, seria:",
                secundario
            )

            Citacao(
                texto = "\"Mulheres\"",
                textoCor = texto,
                fundo = if (dark) Color(0xFF0D0D12) else Color(0xFFF0EFF5),
                roxo = roxo,
                negrito = true
            )

            Paragrafo(
                "é serio, e bom de pronunciar, lindo de ouvir e pensar no significado, " +
                    "e maravilhoso de escrever, além que me lembra de pessoas muito valiosas!",
                secundario
            )

            TituloGrande(
                "Autoridades políticas, escutem bem!:",
                texto
            )

            Paragrafo(
                "Voces tem que entender que as mulheres não precisam de discursos longos e frios, " +
                    "elas precisam de ações, acolhimento sem julgamento, um tanque de guerra blindado " +
                    "contra a violência e uma armadura calorosa.",
                secundario
            )

            Paragrafo(
                "Elas não precisam dos seus votos, elas precisam de políticas públicas fortes " +
                    "que não quebram na primeira brecha.",
                secundario
            )

            Paragrafo(
                "Elas não precisam ser inferiorizadas, elas precisam ser empoderadas " +
                    "(que já são né)!",
                secundario
            )

            Paragrafo(
                "Elas não precisam de softwares e programas burocráticos e inseguros, " +
                    "elas precisam de uma rede de proteção!",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "E as estudantes, elas precisam de voz!, elas precisam ter um ambiente seguro "
                    )

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            "SEM ASSÉDIO, SEM STALKING E SEM AS OUTRAS VIOLÊNCIAS."
                        )
                    }
                },
                secundario
            )

            Paragrafo(
                "E as trabalhadoras, elas não querem ser escravas de um sistema falho, " +
                    "elas querem igualdade salarial.",
                secundario
            )

            Paragrafo(
                "E as autistas, elas nao precisam ser inferiorizadas e sofrerem bullyng, " +
                    "elas precisam ter disponível terapias e remédios (se prescrito, e se o médico " +
                    "deu o seu parecer!), elas precisam de diagnóstico quando for preciso, " +
                    "atendimento rápido e ágil sem fazer elas se sentirem desreguladas.",
                secundario
            )

            Paragrafo(
                "E para as juizas, elas precisam de leis duras contra esses agressores, " +
                    "precisam de ferramentas precisas!",
                secundario
            )

            Paragrafo(
                "E para as mulheres que estão no topo de autoridades: elas precisam de um ambiente " +
                    "onde não tenha esses homens misóginos e machistas, elas precisam de um ambiente " +
                    "onde possam se desenvolver e defender os nossos direitos " +
                    "(os das mulheres, dos autistas, e etc!)",
                secundario
            )

            Paragrafo(
                "E para as mulheres que trabalham com a área (puxado para mercados, varejo e etc), " +
                    "elas precisam de um ambiente com mais salário e carga horária adequada.",
                secundario
            )

            Paragrafo(
                "E para as professoras: eu mesmo tenho professoras mulheres que me orgulho muito, " +
                    "e elas precisam (nao so elas mas como todos e todas), salários mais altos, " +
                    "independencia (por exemplo, sem essas provas do governo decidindo a nota e eles " +
                    "não podendo decidir), lembre-se que são eles que nos formaram!",
                secundario
            )

            Paragrafo(
                "E para mães grávidas: elas querem ter folgas do trabalho quando for preciso, " +
                    "ter pré-natal, remédios, acompanhamento médico, vacinas, um parto tranquilo, " +
                    "e assentos garantidos em lugares publicos e transportes públicos.",
                secundario
            )

            Paragrafo(
                "Elas não querem o voto de vocês, elas querem ação, direitos, políticas públicas " +
                    "e leis garantidas e fortes que não quebram ao mudar os mandatos!",
                secundario
            )

            Paragrafo(
                "E elas querem saneamento básico que inúmeras vezes falta " +
                    "(elas querem esgoto tratato, córregos fechados, segurança alimentar, " +
                    "uma casa onde não dá enchente toda hora, água limpa sem doenças, " +
                    "comida de qualidade!, remédios nos hospitais sempre!)",
                secundario
            )

            Paragrafo(
                "E pra esses agressores: eles tem que ser punidos pelo que fizeram e fazem, " +
                    "sem simplesmente sair pro mundo só porque a lei os deixa impunes!",
                secundario
            )

            Paragrafo(
                "E para as mães de mulheres: elas querem segurança para suas filhas, " +
                    "elas querem escolas com infraestrutura, sem estar caindo aos pedaços, " +
                    "elas querem empregos sem a escala 6x1, e principalmente, elas querem viver " +
                    "num mundo onde não tenham um medo extremo de pensar que suas filhas estão em perigo!",
                secundario
            )

            Paragrafo(
                "Para cientistas: pelo que já estudei, aparece mais nos livros cientistas homens " +
                    "do que mulheres, precisamos mudar isso!, e isso aconteceu porque por muito tempo " +
                    "mulheres não tinham direito de entrar nesse trabalho, temos que fazer com que " +
                    "elas possam sim, porque o lugar delas é onde elas quiserem!",
                secundario
            )

            Paragrafo(
                "Mulheres que trabalham na internet: redes sociais tem que banir conteúdos " +
                    "explícitos, e banir comentários maldosos sobre elas!",
                secundario
            )

            Paragrafo(
                "Mulheres prodígias: temos que dar estrutura de recursos para elas voarem longe, " +
                    "e temos que parar de, por exemplo, criar leis de menores que criam o Family Link " +
                    "que excluem a pasta segura e o trabalho delas, essas leis deveriam proteger, " +
                    "não machucar.",
                secundario
            )

            Paragrafo(
                "Para a indústria da música: parem de fazer com que a imagem da mulher seja " +
                    "uma máquina de coisas inapropriadas (a não ser que elas queiram).",
                secundario
            )

            Paragrafo(
                "Muita gente nao sabe, mas na época de colonização, elas foram violentadas, " +
                    "temos uma grande dívida histórica com elas!, e temos muito o que aprender ainda.",
                secundario
            )

            Paragrafo(
                "Então se vocês homens querem ser homens de verdade, parem de machucar elas, " +
                    "virem cidadãos e lutem ao lado delas, não contra elas.",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            "E para todas: ELAS PRECISAM SER CONSIDERADAS COMO PESSOAS, " +
                                "NÃO SÓ MAIS UMA ESTATÍSTICA!"
                        )
                    }
                },
                secundario
            )

            TituloGrande(
                "Às Meretíssimas Juízas, Advogadas e outras profissionais do Direito",
                texto
            )

            Paragrafo(
                "Olha, os homens costumam humilhar vocês e tentar, sabe?, intimidar e desvalorizar " +
                    "seu trabalho só por vocês serem mulheres.",
                secundario
            )

            Paragrafo(
                "Mas eu, como homem de 15 anos, lhes digo:",
                secundario
            )

            Citacao(
                texto = "Vocês têm todo o meu respeito. Eu tiro o meu chapéu para vocês.",
                textoCor = texto,
                fundo = if (dark) Color(0xFF0D0D12) else Color(0xFFF0EFF5),
                roxo = roxo,
                negrito = true
            )

            Paragrafo(
                "Cara, enfrentar a violência contra a mulher nos tribunais não é fácil.",
                secundario
            )

            Paragrafo(
                "Pelo lado da lei, é difícil aplicar a lei quando não sabemos o que fazer " +
                    "porque não temos provas concretas.",
                secundario
            )

Paragrafo(
    buildAnnotatedString {
        append(
            "E tenho certeza que chegam até vocês casos extremamente violentos, né? " +
                "Casos pesados. Então, para mim, vocês representam uma força emocional " +
                "extremamente determinada — maior que "
        )

        withStyle(
            SpanStyle(fontWeight = FontWeight.Bold)
        ) {
            append("10¹⁰⁰")
        }

        append(".")
    },
    secundario
)

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "Os homens — os covardes, né? Porque homem de verdade não faz isso — " +
                            "tentam humilhar vocês, mas, na verdade, eles têm medo de vocês, " +
                            "da força de vocês."
                    )
                },
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "Vocês não sabem, mas eu tenho "
                    )

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("temor de respeito extremo por vocês")
                    }

                    append(".")
                },
                secundario
            )

            TituloSecundario(
                "Já vou adiantando:",
                texto
            )

            TituloTerciario(
                "Vocês NÃO SÃO:",
                texto
            )

            Lista(
                listOf(
                    "Loucas;",
                    "Histéricas;",
                    "Desequilibradas;",
                    "Ou emocionais demais."
                ),
                secundario,
                roxo
            )

            TituloTerciario(
                "VOCÊS SÃO:",
                texto
            )

            Lista(
                listOf(
                    "Fortes;",
                    "Guerreiras;",
                    "Determinadas em level elevado;",
                    "E, principalmente, mulheres!"
                ),
                secundario,
                roxo
            )

            Paragrafo(
                "Não tenham vergonha de serem mulheres. Vocês carregam um gênero que representa " +
                    "força e determinação — ou o que vocês decidirem que seja!",
                secundario
            )

            Paragrafo(
                "É difícil mesmo quando um homem corta a sua fala, né?",
                secundario
            )

            Paragrafo(
                "Mas sabe o que é?",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "Isso demonstra "
                    )

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("o caráter deles, não o de vocês")
                    }

                    append(".")
                },
                secundario
            )

            Paragrafo(
                "Isso demonstra que vocês são mais inteligentes que eles!",
                secundario
            )

            Paragrafo(
                "E, sinceramente, juízas, eu posso dizer com tranquilidade que, quando estudei " +
                    "filosofia, eu digo:",
                secundario
            )

            Citacao(
                texto = "Quem tenta explicar algo óbvio que vocês já sabem, na filosofia, é ignorante — isto é, não sabe — e é o oposto de filósofo.",
                textoCor = secundario,
                fundo = if (dark) Color(0xFF0D0D12) else Color(0xFFF0EFF5),
                roxo = roxo
            )

            Paragrafo(
                "E outra: temos que reconhecer que não sabemos de nada ainda e que ainda estamos aprendendo.",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "Eu digo com tranquilidade que vocês "
                    )

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("não precisam das explicações deles!")
                    }
                },
                secundario
            )

            TituloSecundario(
                "E NÃO DUVIDEM DE SI MESMAS",
                texto
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "Quando alguém fizer gaslighting jurídico, "
                    )

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("vocês são mais fortes que isso!")
                    }
                },
                secundario
            )

            Paragrafo(
                "E lembrem-se:",
                secundario
            )

            Citacao(
                texto = "O valor de uma mulher excede o de muitos rubis; o de um homem machista é menos do que a escória da terra.",
                textoCor = secundario,
                fundo = if (dark) Color(0xFF0D0D12) else Color(0xFFF0EFF5),
                roxo = roxo,
                negrito = true
            )

            TituloGrande(
                "Mulheres na Pedagogia",
                texto
            )

            Paragrafo(
                "Primeiro, é... saibam que existia aquela ideia de que o homem busca o sustento " +
                    "e a mulher faz o serviço de casa.",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "Agora, essa ideia está sendo quebrada — porque "
                    )

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append(
                            "a mulher tem que decidir por si mesma o que ela quer!"
                        )
                    }
                },
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "Deve ser "
                    )

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("beeeeeem difícil")
                    }

                    append(
                        " trabalhar. Vamos combinar que é praticamente o dia todo, desde a madrugada " +
                            "até a noite, e isso se repete."
                    )
                },
                secundario
            )

            Paragrafo(
                "Talvez a escola não seja 6x1, mas, na prática, vocês trabalham em um nível " +
                    "de desgaste muito grande.",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "Eu entendo vocês. Eu sei como seria um trabalho desgastante. Talvez não seja igual, " +
                            "mas passei muito tempo trabalhando para criar este projeto, horas nas minhas " +
                            "atividades e muitas outras horas em lições de escola — e é nível "
                    )

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("modo inferno desbloqueado")
                    }

                    append(", sabe?")
                },
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "E eu sei, sim, que vocês trabalham em uma profissão que é exercida "
                    )

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("antes, durante a carga horária e depois dela!")
                    }
                },
                secundario
            )

            Paragrafo(
                "Mas sabe por que tudo isso?",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("Por culpa dos políticos do Brasil!")
                    }
                },
                secundario
            )

            Paragrafo(
                "Vocês precisam e merecem descanso.",
                secundario
            )

            Paragrafo(
                "Então, se não puderem descansar fisicamente, descansem na certeza de que esse trabalho " +
                    "está sendo notado por alguém:",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("Por mim.")
                    }
                },
                secundario
            )

            Paragrafo(
                "E é verdade: nas escolas tem bituca de cigarro no banheiro, às vezes não tem papel higiênico " +
                    "— mas eu oro a Deus para que tenham absorventes disponíveis nos banheiros das meninas! — " +
                    "e é toda hora banheiro sendo entupido.",
                secundario
            )

            Paragrafo(
                "Nas escolas já teve goteira na lâmpada e, em muitas delas, existem paredes sujas.",
                secundario
            )

            Paragrafo(
                "Mas eu tenho que dizer que parte de tudo isso também é causada pelos alunos que não cuidam da escola.",
                secundario
            )

            Paragrafo(
                "E outra parte gigantesca é o governo querendo voto e deixando de cuidar das vidas das pessoas!",
                secundario
            )

            Paragrafo(
                "E, com certeza, vocês são as primeiras a perceberem violências.",
                secundario
            )

            Paragrafo(
                "Muitas de vocês são mães, então conseguem perceber determinadas situações rapidamente — até comigo!",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "E essa é uma das partes mais bonitas da alma de vocês. "
                    )

                    append(
                        "É uma parte do coração de Deus: "
                    )

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("esse instinto de mãe!")
                    }
                },
                secundario
            )

            Paragrafo(
                "E é verdade: é difícil ensinar sobre cidadania e conduta.",
                secundario
            )

            Paragrafo(
                "Mas, sinceramente:",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append(
                            "Isso é um peso que vocês não deveriam carregar sozinhas."
                        )
                    }
                },
                secundario
            )

            Paragrafo(
                "São os pais que deveriam carregar a responsabilidade de ensinar como seus filhos " +
                    "devem se comportar e ter uma boa conduta.",
                secundario
            )

            Paragrafo(
                buildAnnotatedString {
                    append(
                        "Afinal, "
                    )

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("a educação básica também vem de casa.")
                    }
                },
                secundario
            )

            Bolhas(roxo)
        }
    }
}

@Composable
private fun TituloTerciario(
    texto: String,
    cor: Color
) {
    Text(
        text = texto,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 24.dp,
                bottom = 12.dp
            ),
        color = cor,
        fontFamily = Quicksand,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp
    )
}

@Composable
private fun TituloGrande(
    texto: String,
    cor: Color
) {
    Text(
        text = texto,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 45.dp,
                bottom = 25.dp
            ),
        color = cor,
        fontFamily = Quicksand,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 34.sp,
        letterSpacing = (-0.8).sp
    )
}

@Composable
private fun TituloSecundario(
    texto: String,
    cor: Color
) {
    Text(
        text = texto,
        modifier = Modifier.padding(
            top = 35.dp,
            bottom = 18.dp
        ),
        color = cor,
        fontFamily = Quicksand,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 30.sp,
        letterSpacing = (-0.6).sp
    )
}

@Composable
private fun Paragrafo(
    texto: String,
    cor: Color
) {
    Paragrafo(
        texto = AnnotatedString(texto),
        cor = cor
    )
}

@Composable
private fun Paragrafo(
    texto: AnnotatedString,
    cor: Color
) {
    Text(
        text = texto,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 20.dp,
                bottom = 20.dp
            ),
        color = cor,
        fontFamily = Quicksand,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp
    )
}

@Composable
private fun Citacao(
    texto: String,
    textoCor: Color,
    fundo: Color,
    roxo: Color,
    negrito: Boolean = false
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 25.dp,
                bottom = 25.dp
            )
            .clip(
                RoundedCornerShape(
                    topEnd = 18.dp,
                    bottomEnd = 18.dp
                )
            )
            .background(fundo)
    ) {

        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(roxo)
        )

        Text(
            text = texto,
            modifier = Modifier.padding(20.dp),
            color = textoCor,
            fontFamily = Quicksand,
            fontSize = 14.sp,
            fontWeight =
                if (negrito) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun Lista(
    itens: List<String>,
    cor: Color,
    roxo: Color
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 15.dp,
                bottom = 25.dp
            ),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        itens.forEach { item ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Text(
                    text = "•",
                    color = roxo,
                    fontFamily = Quicksand,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = item,
                    color = cor,
                    fontFamily = Quicksand,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun Bolhas(
    roxo: Color
) {

    Row(
        modifier = Modifier
            .padding(
                top = 25.dp,
                bottom = 5.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(roxo.copy(alpha = 0.8f))
        )

        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(roxo.copy(alpha = 0.45f))
        )

        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(roxo.copy(alpha = 0.2f))
        )
    }
}