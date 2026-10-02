package com.mulheres

import androidx.compose.foundation.layout.width
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.activity.enableEdgeToEdge

/* =========================================================
   CORES
========================================================= */

private val Purple = Color(0xFF9B5CFF)

private val DarkBackground = Color(0xFF000000)
private val DarkCard = Color(0xFF0A0A0D)
private val DarkCardSecondary = Color(0xFF0D0D12)
private val DarkBorder = Color(0xFF1A1A20)
private val DarkBorderSecondary = Color(0xFF202027)
private val DarkText = Color(0xFFFFFFFF)
private val DarkMuted = Color(0xFF85858C)

private val LightBackground = Color(0xFFF7F4F5)
private val LightCard = Color(0xFFFFFFFF)
private val LightCardSecondary = Color(0xFFF0ECEE)
private val LightBorder = Color(0xFFE2DDE0)
private val LightText = Color(0xFF292127)
private val LightMuted = Color(0xFF777177)

/* =========================================================
   QUICKSAND
========================================================= */

private val Quicksand = FontFamily(
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.SemiBold
    ),
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.Bold
    )
)

/* =========================================================
   ACTIVITY
========================================================= */

class DireitosActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        /* =====================================================
           SEGURANÇA
        ===================================================== */

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        /* =====================================================
           WINDOW COMPAT
        ===================================================== */

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        window.statusBarColor =
            android.graphics.Color.TRANSPARENT

        window.navigationBarColor =
            android.graphics.Color.TRANSPARENT

        /* =====================================================
           COMPOSE
        ===================================================== */

        setContent {

            DireitosTheme {

                DireitosScreen()

            }
        }
    }
}

/* =========================================================
   TEMA
========================================================= */

@Composable
private fun DireitosTheme(
    content: @Composable () -> Unit
) {

    val dark =
        isSystemInDarkTheme()

    val colors =
        if (dark) {

            darkColorScheme(
                primary = Purple,
                secondary = Purple,
                background = DarkBackground,
                surface = DarkCard,
                onBackground = DarkText,
                onSurface = DarkText
            )

        } else {

            lightColorScheme(
                primary = Purple,
                secondary = Purple,
                background = LightBackground,
                surface = LightCard,
                onBackground = LightText,
                onSurface = LightText
            )
        }

    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography.copy(

            bodyLarge =
                MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = Quicksand
                ),

            bodyMedium =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = Quicksand
                ),

            bodySmall =
                MaterialTheme.typography.bodySmall.copy(
                    fontFamily = Quicksand
                ),

            titleLarge =
                MaterialTheme.typography.titleLarge.copy(
                    fontFamily = Quicksand
                ),

            titleMedium =
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = Quicksand
                ),

            titleSmall =
                MaterialTheme.typography.titleSmall.copy(
                    fontFamily = Quicksand
                )
        ),
        content = content
    )
}

/* =========================================================
   MODELO
========================================================= */

private data class Direito(
    val numero: Int,
    val texto: String,
    val lei: String
)

/* =========================================================
   TELA
========================================================= */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DireitosScreen() {

    val dark =
        isSystemInDarkTheme()

    val direitos =
        remember {
            listaDeDireitos()
        }

    CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {

        Surface(
            modifier =
                Modifier.fillMaxSize(),
            color =
                if (dark)
                    DarkBackground
                else
                    LightBackground
        ) {

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize(),
                        
                        

                contentPadding =
                    PaddingValues(
                        start = 22.dp,
                        end = 22.dp,
                        top = 58.dp,
                        bottom = 60.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {

                item {

                    Marca(
                        dark = dark
                    )
                }

                item {

                    DireitosCard(
                        dark = dark,
                        direitos = direitos
                    )
                }

            }
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

    val text =
        if (dark)
            DarkText
        else
            LightText

    val muted =
        if (dark)
            DarkMuted
        else
            LightMuted

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 27.dp
                )
    ) {

        Box(
            modifier =
                Modifier
                    .width44()
                    .height(4.dp)
                    .clip(
                        RoundedCornerShape(999.dp)
                    )
                    .background(Purple)
        )

        Spacer(
            modifier =
                Modifier.height(13.dp)
        )

        Text(
            text = "Mulher Amparada",

            color = text,

            fontFamily = Quicksand,

            fontWeight =
                FontWeight.Bold,

            fontSize = 36.sp,

            lineHeight = 38.sp,

            letterSpacing = (-1.5).sp
        )

        Spacer(
            modifier =
                Modifier.height(13.dp)
        )

        Text(
            text = "100 dos seus direitos",

            color = muted,

            fontFamily = Quicksand,

            fontSize = 15.sp,

            lineHeight = 23.sp
        )
    }
}

/* =========================================================
   CARD PRINCIPAL
========================================================= */

@Composable
private fun DireitosCard(
    dark: Boolean,
    direitos: List<Direito>
) {

    val background =
        if (dark)
            DarkCard
        else
            LightCard

    val border =
        if (dark)
            DarkBorder
        else
            LightBorder

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(30.dp)
                )
                .background(background)
                .border(
                    width = 1.dp,
                    color = border,
                    shape =
                        RoundedCornerShape(30.dp)
                )
    ) {

        /* =====================================================
           DECORAÇÃO SUPERIOR
        ===================================================== */

        Box(
            modifier =
                Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 0.dp,
                        end = 0.dp
                    )
                    .clip(CircleShape)
                    .background(
                        if (dark)
                            Color(0x1A9B5CFF)
                        else
                            Color(0x169B5CFF)
                    )
        )

        /* =====================================================
           DECORAÇÃO INFERIOR
        ===================================================== */

        Box(
            modifier =
                Modifier
                    .size(70.dp)
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 35.dp,
                        bottom = 0.dp
                    )
                    .clip(CircleShape)
                    .background(
                        if (dark)
                            Color(0x07FFFFFF)
                        else
                            Color(0x10000000)
                    )
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
        ) {

            Text(
                text =
                    "100 DIREITOS DAS MULHERES " +
                        "GARANTIDOS PELA LEGISLAÇÃO " +
                        "BRASILEIRA",

                color =
                    if (dark)
                        DarkText
                    else
                        LightText,

                fontFamily =
                    Quicksand,

                fontWeight =
                    FontWeight.Bold,

                fontSize = 25.sp,

                lineHeight = 31.sp,

                letterSpacing =
                    (-0.6).sp
            )

            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )

            direitos.forEach { direito ->

                DireitoItem(
                    dark = dark,
                    direito = direito
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )
            }

            /* =================================================
               BOLHAS
            ================================================= */

            RowBolhas()
        }
    }
}

/* =========================================================
   DIREITO
========================================================= */

@Composable
private fun DireitoItem(
    dark: Boolean,
    direito: Direito
) {

    val text =
        if (dark)
            DarkMuted
        else
            LightMuted

    val strong =
        if (dark)
            DarkText
        else
            LightText

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text =
                "${direito.numero}. ${direito.texto}",

            color = strong,

            fontFamily =
                Quicksand,

            fontWeight =
                FontWeight.Medium,

            fontSize = 14.sp,

            lineHeight = 23.sp
        )

        Spacer(
            modifier =
                Modifier.height(7.dp)
        )

        Text(
            text = direito.lei,

            color = text,

            fontFamily =
                Quicksand,

            fontSize = 13.sp,

            lineHeight = 22.sp
        )
    }
}

/* =========================================================
   BOLHAS
========================================================= */

@Composable
private fun RowBolhas() {

    androidx.compose.foundation.layout.Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {

        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        Purple.copy(alpha = 0.8f)
                    )
        )

        Box(
            modifier =
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        Purple.copy(alpha = 0.45f)
                    )
        )

        Box(
            modifier =
                Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(
                        Purple.copy(alpha = 0.2f)
                    )
        )
    }
}

/* =========================================================
   LISTA DOS 100 DIREITOS
========================================================= */

private fun listaDeDireitos(): List<Direito> {

    return listOf(

        Direito(
            1,
            "Direito à igualdade perante a lei.",
            "Lei: Constituição Federal, art. 5º, caput."
        ),

        Direito(
            2,
            "Direito de não sofrer discriminação por motivo de sexo.",
            "Lei: Constituição Federal, art. 3º, IV."
        ),

        Direito(
            3,
            "Direito à igualdade de direitos e obrigações entre mulheres e homens.",
            "Lei: Constituição Federal, art. 5º, I."
        ),

        Direito(
            4,
            "Direito à vida.",
            "Lei: Constituição Federal, art. 5º, caput."
        ),

        Direito(
            5,
            "Direito à liberdade.",
            "Lei: Constituição Federal, art. 5º, caput."
        ),

        Direito(
            6,
            "Direito à segurança.",
            "Lei: Constituição Federal, art. 5º, caput."
        ),

        Direito(
            7,
            "Direito à dignidade.",
            "Lei: Constituição Federal, art. 1º, III."
        ),

        Direito(
            8,
            "Direito à intimidade.",
            "Lei: Constituição Federal, art. 5º, X."
        ),

        Direito(
            9,
            "Direito à vida privada.",
            "Lei: Constituição Federal, art. 5º, X."
        ),

        Direito(
            10,
            "Direito à honra.",
            "Lei: Constituição Federal, art. 5º, X."
        ),

        Direito(
            11,
            "Direito à imagem.",
            "Lei: Constituição Federal, art. 5º, X."
        ),

        Direito(
            12,
            "Direito de acesso à Justiça.",
            "Lei: Constituição Federal, art. 5º, XXXV."
        ),

        Direito(
            13,
            "Direito à assistência jurídica integral e gratuita quando preencher os requisitos legais.",
            "Lei: Constituição Federal, art. 5º, LXXIV."
        ),

        Direito(
            14,
            "Direito ao devido processo legal.",
            "Lei: Constituição Federal, art. 5º, LIV."
        ),

        Direito(
            15,
            "Direito ao contraditório e à ampla defesa.",
            "Lei: Constituição Federal, art. 5º, LV."
        ),

        Direito(
            16,
            "Direito de não ser submetida à tortura ou a tratamento desumano ou degradante.",
            "Lei: Constituição Federal, art. 5º, III."
        ),

        Direito(
            17,
            "Direito à liberdade de expressão.",
            "Lei: Constituição Federal, art. 5º, IV e IX."
        ),

        Direito(
            18,
            "Direito à liberdade de consciência e de crença.",
            "Lei: Constituição Federal, art. 5º, VI."
        ),

        Direito(
            19,
            "Direito à liberdade de associação.",
            "Lei: Constituição Federal, art. 5º, XVII."
        ),

        Direito(
            20,
            "Direito de reunião pacífica.",
            "Lei: Constituição Federal, art. 5º, XVI."
        ),

        Direito(
            21,
            "Direito à educação.",
            "Lei: Constituição Federal, arts. 6º e 205."
        ),

        Direito(
            22,
            "Direito à saúde.",
            "Lei: Constituição Federal, arts. 6º e 196."
        ),

        Direito(
            23,
            "Direito à alimentação.",
            "Lei: Constituição Federal, art. 6º."
        ),

        Direito(
            24,
            "Direito à moradia.",
            "Lei: Constituição Federal, art. 6º."
        ),

        Direito(
            25,
            "Direito ao trabalho.",
            "Lei: Constituição Federal, arts. 6º e 7º."
        ),

        Direito(
            26,
            "Direito ao lazer.",
            "Lei: Constituição Federal, art. 6º."
        ),

        Direito(
            27,
            "Direito à previdência social.",
            "Lei: Constituição Federal, art. 6º."
        ),

        Direito(
            28,
            "Direito à assistência aos desamparados.",
            "Lei: Constituição Federal, art. 6º."
        ),

        Direito(
            29,
            "Direito à proteção à maternidade.",
            "Lei: Constituição Federal, art. 6º."
        ),

        Direito(
            30,
            "Direito à proteção da família.",
            "Lei: Constituição Federal, art. 226."
        ),

        Direito(
            31,
            "Direito à igualdade de direitos e deveres na sociedade conjugal.",
            "Lei: Constituição Federal, art. 226, §5º."
        ),

        Direito(
            32,
            "Direito ao divórcio.",
            "Lei: Constituição Federal, art. 226, §6º; Código Civil, arts. 1.571 e seguintes."
        ),

        Direito(
            33,
            "Direito ao planejamento familiar livre.",
            "Lei: Constituição Federal, art. 226, §7º."
        ),

        Direito(
            34,
            "Direito de não sofrer violência no âmbito familiar.",
            "Lei: Constituição Federal, art. 226, §8º."
        ),

        Direito(
            35,
            "Direito a mecanismos estatais de proteção contra a violência doméstica.",
            "Lei: Constituição Federal, art. 226, §8º; Lei nº 11.340/2006."
        ),

        Direito(
            36,
            "Direito de viver sem violência doméstica e familiar.",
            "Lei: Lei nº 11.340/2006, arts. 2º e 3º."
        ),

        Direito(
            37,
            "Direito à proteção contra violência física.",
            "Lei: Lei nº 11.340/2006, art. 7º, I."
        ),

        Direito(
            38,
            "Direito à proteção contra violência psicológica.",
            "Lei: Lei nº 11.340/2006, art. 7º, II."
        ),

        Direito(
            39,
            "Direito à proteção contra violência sexual.",
            "Lei: Lei nº 11.340/2006, art. 7º, III."
        ),

        Direito(
            40,
            "Direito à proteção contra violência patrimonial.",
            "Lei: Lei nº 11.340/2006, art. 7º, IV."
        ),

        Direito(
            41,
            "Direito à proteção contra violência moral.",
            "Lei: Lei nº 11.340/2006, art. 7º, V."
        ),

        Direito(
            42,
            "Direito a medidas protetivas de urgência em situação de violência doméstica e familiar.",
            "Lei: Lei nº 11.340/2006, arts. 18 e seguintes."
        ),

        Direito(
            43,
            "Direito de solicitar medidas protetivas de urgência.",
            "Lei: Lei nº 11.340/2006, art. 18."
        ),

        Direito(
            44,
            "Direito à proteção contra o contato do agressor, quando determinada judicialmente.",
            "Lei: Lei nº 11.340/2006, art. 22."
        ),

        Direito(
            45,
            "Direito à proteção contra a aproximação do agressor, quando determinada judicialmente.",
            "Lei: Lei nº 11.340/2006, art. 22."
        ),

        Direito(
            46,
            "Direito à proteção contra a frequência do agressor a determinados lugares, quando determinada judicialmente.",
            "Lei: Lei nº 11.340/2006, art. 22."
        ),

        Direito(
            47,
            "Direito à assistência jurídica quando necessário em situação de violência doméstica.",
            "Lei: Lei nº 11.340/2006, art. 9º, §2º, III."
        ),

        Direito(
            48,
            "Direito à manutenção do vínculo trabalhista quando o afastamento do trabalho for necessário por violência doméstica.",
            "Lei: Lei nº 11.340/2006, art. 9º, §2º, II."
        ),

        Direito(
            49,
            "Direito à remoção prioritária, quando servidora pública, em determinadas situações de violência doméstica.",
            "Lei: Lei nº 11.340/2006, art. 9º, §2º, I."
        ),

        Direito(
            50,
            "Direito à assistência prioritária no SUS e no sistema de segurança pública em situação de violência doméstica.",
            "Lei: Lei nº 11.340/2006, art. 9º."
        ),

        Direito(
            51,
            "Direito ao acesso a serviços de contracepção de emergência nos casos previstos em lei após violência sexual.",
            "Lei: Lei nº 11.340/2006, art. 9º, §3º; Lei nº 12.845/2013."
        ),

        Direito(
            52,
            "Direito à profilaxia de infecções sexualmente transmissíveis após violência sexual.",
            "Lei: Lei nº 12.845/2013, art. 3º, V."
        ),

        Direito(
            53,
            "Direito ao atendimento médico, psicológico e social imediato após violência sexual.",
            "Lei: Lei nº 12.845/2013, art. 3º, II."
        ),

        Direito(
            54,
            "Direito ao atendimento emergencial e integral após violência sexual.",
            "Lei: Lei nº 12.845/2013, arts. 1º e 3º."
        ),

        Direito(
            55,
            "Direito à coleta de material para exame de HIV após violência sexual, conforme atendimento previsto em lei.",
            "Lei: Lei nº 12.845/2013, art. 3º, VI."
        ),

        Direito(
            56,
            "Direito a receber informações sobre seus direitos e serviços disponíveis após violência sexual.",
            "Lei: Lei nº 12.845/2013, art. 3º, VII."
        ),

        Direito(
            57,
            "Direito à facilitação do registro da ocorrência após violência sexual.",
            "Lei: Lei nº 12.845/2013, art. 3º, III."
        ),

        Direito(
            58,
            "Direito ao atendimento especializado no SUS em caso de violência.",
            "Lei: Lei nº 8.080/1990, art. 7º, XIV."
        ),

        Direito(
            59,
            "Direito à privacidade no atendimento de saúde em caso de violência.",
            "Lei: Lei nº 8.080/1990, art. 7º, XIV, parágrafo único."
        ),

        Direito(
            60,
            "Direito de ter acompanhante em consultas, exames e procedimentos de saúde.",
            "Lei: Lei nº 8.080/1990, art. 19-J, incluído pela Lei nº 14.737/2023."
        ),

        Direito(
            61,
            "Direito de escolher o acompanhante em atendimento de saúde.",
            "Lei: Lei nº 8.080/1990, art. 19-J, §1º."
        ),

        Direito(
            62,
            "Direito a acompanhante durante atendimento com sedação, nas condições previstas em lei.",
            "Lei: Lei nº 8.080/1990, art. 19-J, §2º."
        ),

        Direito(
            63,
            "Direito de recusar o acompanhante indicado pelo estabelecimento de saúde em atendimento com sedação.",
            "Lei: Lei nº 8.080/1990, art. 19-J, §2º."
        ),

        Direito(
            64,
            "Direito à prevenção, detecção e tratamento do câncer do colo do útero.",
            "Lei: Lei nº 11.664/2008, com alterações da Lei nº 14.335/2022."
        ),

        Direito(
            65,
            "Direito à prevenção, detecção e tratamento do câncer de mama.",
            "Lei: Lei nº 11.664/2008, com alterações da Lei nº 14.335/2022."
        ),

        Direito(
            66,
            "Direito à prevenção, detecção e tratamento do câncer colorretal, conforme a legislação do SUS.",
            "Lei: Lei nº 11.664/2008, com alterações da Lei nº 14.335/2022."
        ),

        Direito(
            67,
            "Direito à realização de exames previstos na legislação para prevenção e detecção desses cânceres.",
            "Lei: Lei nº 11.664/2008, art. 2º, II."
        ),

        Direito(
            68,
            "Direito à atenção integral quando diagnosticada com câncer do colo do útero, mama ou colorretal.",
            "Lei: Lei nº 11.664/2008, art. 2º, III-A."
        ),

        Direito(
            69,
            "Direito a encaminhamento para serviço de maior complexidade quando necessário.",
            "Lei: Lei nº 11.664/2008, art. 2º, IV."
        ),

        Direito(
            70,
            "Direito a condições e equipamentos adequados para mulheres com deficiência na prevenção e tratamento desses cânceres.",
            "Lei: Lei nº 11.664/2008, art. 2º, §2º."
        ),

        Direito(
            71,
            "Direito à proteção contra discriminação no acesso ao trabalho por motivo de sexo.",
            "Lei: Lei nº 9.029/1995, art. 1º."
        ),

        Direito(
            72,
            "Direito à proteção contra discriminação na manutenção do emprego por motivo de sexo.",
            "Lei: Lei nº 9.029/1995, art. 1º."
        ),

        Direito(
            73,
            "Direito de não ser submetida a exigência discriminatória relacionada à gravidez para acesso ao emprego.",
            "Lei: Lei nº 9.029/1995, arts. 1º e 2º."
        ),

        Direito(
            74,
            "Direito de não ser submetida a exigência discriminatória relacionada à esterilização para acesso ao emprego.",
            "Lei: Lei nº 9.029/1995, arts. 1º e 2º."
        ),

        Direito(
            75,
            "Direito à igualdade salarial entre mulheres e homens que exerçam trabalho de igual valor.",
            "Lei: Constituição Federal, art. 7º, XXX; Lei nº 14.611/2023."
        ),

        Direito(
            76,
            "Direito à igualdade de critérios de admissão no trabalho, sem discriminação por sexo.",
            "Lei: Constituição Federal, art. 7º, XXX."
        ),

        Direito(
            77,
            "Direito à igualdade no exercício das funções profissionais, sem discriminação por sexo.",
            "Lei: Constituição Federal, art. 7º, XXX."
        ),

        Direito(
            78,
            "Direito à licença-maternidade.",
            "Lei: Constituição Federal, art. 7º, XVIII; CLT, art. 392."
        ),

        Direito(
            79,
            "Direito à licença-maternidade sem prejuízo do emprego.",
            "Lei: Constituição Federal, art. 7º, XVIII; CLT, art. 392."
        ),

        Direito(
            80,
            "Direito à licença-maternidade sem prejuízo do salário, nos termos da legislação.",
            "Lei: Constituição Federal, art. 7º, XVIII; CLT, art. 392."
        ),

        Direito(
            81,
            "Direito à estabilidade provisória da gestante nos termos da Constituição e da legislação trabalhista.",
            "Lei: ADCT, art. 10, II, \"b\"; CLT, art. 391-A."
        ),

        Direito(
            82,
            "Direito à estabilidade mesmo quando a gravidez é confirmada durante o aviso-prévio, nas condições legais.",
            "Lei: CLT, art. 391-A; Lei nº 12.812/2013."
        ),

        Direito(
            83,
            "Direito à transferência de função durante a gravidez quando as condições de saúde exigirem, nos termos da CLT.",
            "Lei: CLT, art. 392, §4º, I."
        ),

        Direito(
            84,
            "Direito a dispensa do horário de trabalho para consultas e exames durante a gravidez, nas condições previstas na CLT.",
            "Lei: CLT, art. 392, §4º, II."
        ),

        Direito(
            85,
            "Direito de afastamento de atividades insalubres durante a gestação e lactação, conforme as condições estabelecidas na legislação.",
            "Lei: CLT, art. 394-A."
        ),

        Direito(
            86,
            "Direito a pausas para amamentação durante a jornada de trabalho.",
            "Lei: CLT, art. 396."
        ),

        Direito(
            87,
            "Direito à licença-maternidade em caso de adoção ou guarda judicial para adoção, nos termos legais.",
            "Lei: CLT, art. 392-A."
        ),

        Direito(
            88,
            "Direito à proteção contra discriminação no trabalho por situação familiar.",
            "Lei: Lei nº 9.029/1995, art. 1º."
        ),

        Direito(
            89,
            "Direito à proteção contra assédio sexual no ambiente de trabalho, conforme a legislação aplicável.",
            "Lei: Código Penal, art. 216-A; Lei nº 14.540/2023, quando aplicável."
        ),

        Direito(
            90,
            "Direito à proteção contra importunação sexual.",
            "Lei: Código Penal, art. 215-A, incluído pela Lei nº 13.718/2018."
        ),

        Direito(
            91,
            "Direito à proteção contra perseguição (stalking).",
            "Lei: Código Penal, art. 147-A, incluído pela Lei nº 14.132/2021."
        ),

        Direito(
            92,
            "Direito à proteção contra divulgação não autorizada de cena de sexo, nudez ou pornografia nas hipóteses previstas em lei.",
            "Lei: Código Penal, art. 218-C, incluído pela Lei nº 13.718/2018."
        ),

        Direito(
            93,
            "Direito à proteção penal específica contra feminicídio.",
            "Lei: Código Penal, art. 121-A, incluído pela Lei nº 14.994/2024."
        ),

        Direito(
            94,
            "Direito à proteção contra violência política de gênero.",
            "Lei: Lei nº 14.192/2021."
        ),

        Direito(
            95,
            "Direito de participar da vida política sem sofrer violência ou discriminação política em razão de ser mulher.",
            "Lei: Lei nº 14.192/2021."
        ),

        Direito(
            96,
            "Direito à proteção contra violência doméstica independentemente da condição pessoal da vítima ou do agressor, nas hipóteses abrangidas pela Lei Maria da Penha.",
            "Lei: Lei nº 11.340/2006, com alterações da Lei nº 14.550/2023."
        ),

        Direito(
            97,
            "Direito à aplicação das medidas protetivas de urgência independentemente da existência de inquérito policial, ação penal ou registro de boletim de ocorrência, nas condições previstas na Lei Maria da Penha.",
            "Lei: Lei nº 11.340/2006, art. 19, conforme alterações posteriores."
        ),

        Direito(
            98,
            "Direito de receber atendimento gratuito pelo Ligue 180 em situações de violência contra a mulher.",
            "Base legal: Lei nº 11.340/2006; Decreto nº 7.393/2010, com alterações posteriores."
        ),

        Direito(
            99,
            "Direito à informação e às políticas públicas de prevenção e enfrentamento da violência contra as mulheres.",
            "Lei: Lei nº 14.232/2021."
        ),

        Direito(
            100,
            "Direito de viver com proteção contra negligência, discriminação, exploração, violência, crueldade e opressão nas relações domésticas e familiares.",
            "Lei: Lei nº 11.340/2006, arts. 2º e 3º."
        )
    )
}

/* =========================================================
   EXTENSÃO
========================================================= */

private fun Modifier.width44(): Modifier {
    return this.then(
        Modifier.width(44.dp)
    )
}