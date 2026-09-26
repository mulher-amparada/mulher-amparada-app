package com.mulheres

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.layout.ContentScale

class HubActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(
            window,
            true
        )

        setContent {
            MulherAmparadaTheme {
                MulherAmparadaScreen()
            }
        }
    }
}

/* =========================================================
   CORES
========================================================= */

private data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceLight: Color,
    val white: Color,
    val text: Color,
    val secondary: Color,
    val muted: Color,
    val border: Color,
    val borderLight: Color,

    val pink: Color,
    val pinkLight: Color,
    val red: Color,
    val purple: Color,
    val orange: Color,
    val cyan: Color,
    val blue: Color,
    val green: Color
)

@Composable
private fun colors(): AppColors {

    val dark = isSystemInDarkTheme()

    return if (dark) {

        AppColors(
            background = Color(0xFF050507),
            surface = Color(0xFF111116),
            surfaceLight = Color(0xFF18181F),
            white = Color.White,
            text = Color(0xFFEEEEF4),
            secondary = Color(0xFFAAAAB8),
            muted = Color(0xFF70707E),
            border = Color(0x14FFFFFF),
            borderLight = Color(0x25FFFFFF),

            pink = Color(0xFFFF3D82),
            pinkLight = Color(0xFFFF8FB5),
            red = Color(0xFFFF465B),
            purple = Color(0xFF9A7CFF),
            orange = Color(0xFFFFB45E),
            cyan = Color(0xFF54DED4),
            blue = Color(0xFF729FFF),
            green = Color(0xFF69DFA1)
        )

    } else {

        AppColors(
            background = Color(0xFFF8F7FA),
            surface = Color(0xFFFFFFFF),
            surfaceLight = Color(0xFFF1EFF4),
            white = Color(0xFF17151A),
            text = Color(0xFF17151A),
            secondary = Color(0xFF66636D),
            muted = Color(0xFF85818B),
            border = Color(0x18000000),
            borderLight = Color(0x25000000),

            pink = Color(0xFFE82E70),
            pinkLight = Color(0xFFD92D68),
            red = Color(0xFFE7354D),
            purple = Color(0xFF765BE0),
            orange = Color(0xFFD98726),
            cyan = Color(0xFF159E96),
            blue = Color(0xFF4678DB),
            green = Color(0xFF269A68)
        )
    }
}

/* =========================================================
   TEMA
========================================================= */

@Composable
private fun MulherAmparadaTheme(
    content: @Composable () -> Unit
) {

    val c = colors()

    val context = LocalContext.current
    val dark = isSystemInDarkTheme()

    androidx.compose.runtime.SideEffect {

        val window = (context as ComponentActivity).window

        window.statusBarColor =
            c.background.toArgbCompat()

        window.navigationBarColor =
            c.background.toArgbCompat()

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {

            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }

    androidx.compose.material3.MaterialTheme(
        colorScheme =
            if (dark) {
                androidx.compose.material3.darkColorScheme(
                    background = c.background,
                    surface = c.surface,
                    primary = c.pink
                )
            } else {
                androidx.compose.material3.lightColorScheme(
                    background = c.background,
                    surface = c.surface,
                    primary = c.pink
                )
            },
        content = content
    )
}

private fun Color.toArgbCompat(): Int {
    return AndroidColor.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}

/* =========================================================
   FONTE
========================================================= */

@Composable
private fun quicksand(): FontFamily {

    return FontFamily(
        Font(
            resId = com.mulheres.R.font.quicksand,
            weight = FontWeight.Normal
        ),
        Font(
            resId = com.mulheres.R.font.quicksand,
            weight = FontWeight.Medium
        ),
        Font(
            resId = com.mulheres.R.font.quicksand,
            weight = FontWeight.SemiBold
        ),
        Font(
            resId = com.mulheres.R.font.quicksand,
            weight = FontWeight.Bold
        ),
        Font(
            resId = com.mulheres.R.font.quicksand,
            weight = FontWeight.ExtraBold
        )
    )
}

/* =========================================================
   TELA
========================================================= */

@Composable
private fun MulherAmparadaScreen() {

    val c = colors()
    val font = quicksand()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 20.dp,
                bottom = 90.dp
            ),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {

        Hero(
            c = c,
            font = font
        )

        SystemOverview(
            c = c,
            font = font
        )

        SectionTitle(
            text = "Resposta imediata",
            c = c,
            font = font
        )

        PanicCard(
            c = c,
            font = font
        )

        SectionTitle(
            text = "Proteções automáticas",
            c = c,
            font = font
        )

        SensorCard(
            number = "01 / ÁUDIO",
            title = "Proteção por barulho",
            description =
                "Detecta sons altos e situações suspeitas para acionar os mecanismos de proteção.",
            icon = R.drawable.ic_0003,
            color = c.pink,
            active = false,
            c = c,
            font = font
        )

        SensorCard(
            number = "02 / MOVIMENTO",
            title = "Proteção por movimento",
            description =
                "Detecta movimentos bruscos no celular e pode iniciar uma resposta de emergência.",
            icon = R.drawable.ic_0004,
            color = c.purple,
            active = false,
            c = c,
            font = font
        )

        SensorCard(
            number = "03 / VISIBILIDADE",
            title = "Escurecimento por inclinação",
            description =
                "Escurece a tela automaticamente quando o dispositivo identifica a posição configurada.",
            icon = R.drawable.ic_0005,
            color = c.orange,
            active = false,
            c = c,
            font = font
        )

        SensorCard(
            number = "04 / BLOQUEIO",
            title = "Bloqueio por barulho",
            description =
                "Detecta sons altos e bloqueia o celular automaticamente quando ativado.",
            icon = R.drawable.ic_0006,
            color = c.cyan,
            active = false,
            c = c,
            font = font
        )

        SectionTitle(
            text = "Serviços de emergência",
            c = c,
            font = font
        )

        ActionCard(
            title = "Polícia — 190",
            description =
                "Emergência policial e atendimento imediato",
            icon = R.drawable.ic_0007,
            arrow = R.drawable.ic_arrow,
            gradient = listOf(
                Color(0xFF182844),
                Color(0xFF101A2D),
                Color(0xFF0D1423)
            ),
            accent = c.blue,
            c = c,
            font = font
        )

        ActionCard(
            title = "SAMU — 192",
            description =
                "Atendimento médico de emergência",
            icon = R.drawable.ic_0008,
            arrow = R.drawable.ic_arrow,
            gradient = listOf(
                Color(0xFF3A1D19),
                Color(0xFF281512),
                Color(0xFF17100E)
            ),
            accent = Color(0xFFFF6347),
            c = c,
            font = font
        )

        ActionCard(
            title = "Central da Mulher — 180",
            description =
                "Orientação, acolhimento e atendimento",
            icon = R.drawable.ic_0009,
            arrow = R.drawable.ic_arrow,
            gradient = listOf(
                Color(0xFF3C1B2C),
                Color(0xFF281522),
                Color(0xFF1A1018)
            ),
            accent = c.pinkLight,
            c = c,
            font = font
        )

        SectionTitle(
            text = "Recursos de apoio",
            c = c,
            font = font
        )

        ActionCard(
            title = "Enviar localização",
            description =
                "Compartilhe sua localização atual",
            icon = R.drawable.ic_0010,
            arrow = R.drawable.ic_arrow,
            gradient = listOf(
                Color(0xFF153434),
                Color(0xFF102727),
                Color(0xFF0B1B1B)
            ),
            accent = c.cyan,
            c = c,
            font = font
        )

        SectionTitle(
            text = "Contatos de confiança",
            c = c,
            font = font
        )

        ActionCard(
            title = "Adicionar contato",
            description =
                "Escolha uma pessoa de confiança",
            icon = R.drawable.ic_0011,
            arrow = R.drawable.ic_arrow,
            gradient = listOf(
                Color(0xFF2D214B),
                Color(0xFF1E1733),
                Color(0xFF151024)
            ),
            accent = c.purple,
            c = c,
            font = font
        )

        ActionCard(
            title = "SOS para contatos",
            description =
                "Envie um alerta para pessoas de confiança",
            icon = R.drawable.ic_0012,
            arrow = R.drawable.ic_arrow,
            gradient = listOf(
                Color(0xFF3C1B25),
                Color(0xFF2B131B),
                Color(0xFF1C0D13)
            ),
            accent = c.red,
            c = c,
            font = font
        )

        SectionTitle(
            text = "Acesso rápido",
            c = c,
            font = font
        )

        EmergencyAccess(
            c = c,
            font = font
        )

        SectionTitle(
            text = "Espaços protegidos",
            purple = true,
            c = c,
            font = font
        )

        ProtectedArea(
            c = c,
            font = font
        )

        SectionTitle(
            text = "Espaço de acolhimento",
            purple = true,
            c = c,
            font = font
        )

        AmparoArea(
            c = c,
            font = font
        )
    }
}

/* =========================================================
   CABEÇALHO
========================================================= */

@Composable
private fun Hero(
    c: AppColors,
    font: FontFamily
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                c.border,
                RectangleShape
            )
            .padding(
                start = 3.dp,
                end = 3.dp,
                top = 8.dp,
                bottom = 25.dp
            )
    ) {

        Box(
            modifier = Modifier
                .width(48.dp)
                .height(5.dp)
                .clip(
                    RoundedCornerShape(50)
                )
                .background(c.pink)
        )

        Spacer(
            Modifier.height(21.dp)
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(c.pink)
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                text =
                    "SISTEMA DE PROTEÇÃO PESSOAL",
                color = c.pinkLight,
                fontFamily = font,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }

        Spacer(
            Modifier.height(13.dp)
        )

        Text(
            text = "Mulher\nAmparada.",
            color = c.white,
            fontFamily = font,
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 43.sp,
            letterSpacing = (-3).sp
        )

        Spacer(
            Modifier.height(20.dp)
        )

        Text(
            text =
                "Tecnologia criada para oferecer proteção, assistência e resposta rápida quando você precisar.",
            modifier = Modifier.widthIn(
                max = 370.dp
            ),
            color = c.secondary,
            fontFamily = font,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp
        )
    }
}

/* =========================================================
   VISÃO GERAL
========================================================= */

@Composable
private fun SystemOverview(
    c: AppColors,
    font: FontFamily
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(155.dp)
            .clip(
                RoundedCornerShape(32.dp)
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1B1823),
                        Color(0xFF101016)
                    )
                )
            )
            .border(
                1.dp,
                Color(0x18FFFFFF),
                RoundedCornerShape(32.dp)
            )
            .padding(22.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    "ESTADO DO SISTEMA",
                    color = c.muted,
                    fontFamily = font,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )

                Text(
                    "Central de proteção",
                    color = Color.White,
                    fontFamily = font,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    "Gerencie seus recursos de segurança e mantenha suas ferramentas de emergência sempre acessíveis.",
                    color = c.secondary,
                    fontFamily = font,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(c.green)
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text(
                        "SISTEMA DISPONÍVEL",
                        color = c.green,
                        fontFamily = font,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = .8.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(67.dp)
                    .clip(
                        RoundedCornerShape(24.dp)
                    )
                    .background(
                        Color(0x12FF3D82)
                    )
                    .border(
                        1.dp,
                        Color(0x40FF3D82),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .size(49.dp)
                        .border(
                            1.dp,
                            Color(0x35FF3D82),
                            RoundedCornerShape(19.dp)
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Image(
                        painter =
                            painterResource(
                                R.drawable.ic_0001
                            ),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        contentScale =
                            ContentScale.Fit
                    )
                }
            }
        }
    }
}

/* =========================================================
   TÍTULO
========================================================= */

@Composable
private fun SectionTitle(
    text: String,
    c: AppColors,
    font: FontFamily,
    purple: Boolean = false
) {

    Row(
        verticalAlignment =
            Alignment.CenterVertically,
        modifier = Modifier.padding(
            start = 3.dp
        )
    ) {

        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(
                    RoundedCornerShape(2.dp)
                )
                .background(
                    if (purple)
                        c.purple
                    else
                        c.pink
                )
        )

        Spacer(
            Modifier.width(10.dp)
        )

        Text(
            text = text.uppercase(),
            color = c.muted,
            fontFamily = font,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
    }
}

/* =========================================================
   PANIC
========================================================= */

@Composable
private fun PanicCard(
    c: AppColors,
    font: FontFamily
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(
                RoundedCornerShape(32.dp)
            )
            .background(
                Color(0xFFFF315D)
            )
            .border(
                1.dp,
                Color(0xFFFF6B87),
                RoundedCornerShape(32.dp)
            ),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(210.dp)
                .offset(
                    x = 75.dp,
                    y = (-125).dp
                )
                .clip(CircleShape)
                .background(
                    Color(0xFFFF6684)
                )
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(
                    x = (-105).dp,
                    y = 170.dp
                )
                .clip(CircleShape)
                .background(
                    Color(0xFFE91E4F)
                )
        )

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            Image(
                painter =
                    painterResource(
                        R.drawable.ic_0002
                    ),
                contentDescription = null,
                modifier = Modifier.size(62.dp)
            )

            Text(
                "Precisa de ajuda?",
                color = Color.White,
                fontFamily = font,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center
            )

            Text(
                "Toque aqui para acionar o suporte de emergência.",
                modifier = Modifier.widthIn(
                    max = 280.dp
                ),
                color = Color.White.copy(
                    alpha = .92f
                ),
                fontFamily = font,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/* =========================================================
   SENSOR
========================================================= */

@Composable
private fun SensorCard(
    number: String,
    title: String,
    description: String,
    icon: Int,
    color: Color,
    active: Boolean,
    c: AppColors,
    font: FontFamily
) {

    val background =
        if (active) {
            Brush.linearGradient(
                listOf(
                    color.copy(.25f),
                    color.copy(.12f),
                    c.surface
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    c.surface,
                    c.surface
                )
            )
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(
                RoundedCornerShape(25.dp)
            )
            .background(background)
            .border(
                1.dp,
                color.copy(
                    alpha =
                        if (active) .56f
                        else .19f
                ),
                RoundedCornerShape(25.dp)
            )
            .padding(22.dp),
        verticalArrangement =
            Arrangement.SpaceBetween
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.Top
        ) {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    number,
                    color = c.muted,
                    fontFamily = font,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )

                Text(
                    title,
                    modifier =
                        Modifier.widthIn(
                            max = 320.dp
                        ),
                    color = c.white,
                    fontFamily = font,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 25.sp,
                    letterSpacing = (-.8).sp
                )
            }

            Box(
                modifier = Modifier
                    .size(55.dp)
                    .clip(
                        RoundedCornerShape(20.dp)
                    )
                    .background(
                        color.copy(.075f)
                    )
                    .border(
                        1.dp,
                        color.copy(.27f),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .size(39.dp)
                        .border(
                            1.dp,
                            Color.White.copy(.07f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Image(
                        painter =
                            painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Text(
            description,
            color = c.secondary,
            fontFamily = font,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            Color(0xFF555560)
                        )
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    "DESATIVADO",
                    color = c.muted,
                    fontFamily = font,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            }

            FakeSwitch(
                color = color
            )
        }
    }
}

/* =========================================================
   SWITCH VISUAL
========================================================= */

@Composable
private fun FakeSwitch(
    color: Color
) {

    Box(
        modifier = Modifier
            .width(55.dp)
            .height(36.dp)
            .clip(CircleShape)
            .background(
                Color(0xFF292A33)
            )
            .border(
                1.dp,
                Color.White.copy(.09f),
                CircleShape
            )
    ) {

        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Color(0xFF777986)
                )
        )
    }
}

/* =========================================================
   CARDS DE AÇÃO
========================================================= */

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: Int,
    arrow: Int,
    gradient: List<Color>,
    accent: Color,
    c: AppColors,
    font: FontFamily
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(
                RoundedCornerShape(27.dp)
            )
            .background(
                Brush.linearGradient(gradient)
            )
            .border(
                1.dp,
                accent.copy(.31f),
                RoundedCornerShape(27.dp)
            )
            .padding(20.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(
                    RoundedCornerShape(23.dp)
                )
                .background(
                    accent.copy(.09f)
                )
                .border(
                    1.dp,
                    accent.copy(.32f),
                    RoundedCornerShape(23.dp)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Image(
                painter =
                    painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(35.dp)
            )
        }

        Spacer(
            Modifier.width(16.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Text(
                title,
                color = Color.White,
                fontFamily = font,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 18.sp
            )

            Text(
                description,
                color = Color.White.copy(.66f),
                fontFamily = font,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 15.sp
            )

            Text(
                "ATENDIMENTO DISPONÍVEL",
                color = accent,
                fontFamily = font,
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        Spacer(
            Modifier.width(16.dp)
        )

        Box(
            modifier = Modifier
                .size(43.dp)
                .clip(
                    RoundedCornerShape(16.dp)
                )
                .background(
                    Color.Black.copy(.13f)
                )
                .border(
                    1.dp,
                    accent.copy(.22f),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Image(
                painter =
                    painterResource(arrow),
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

/* =========================================================
   ACESSO DE EMERGÊNCIA
========================================================= */

@Composable
private fun EmergencyAccess(
    c: AppColors,
    font: FontFamily
) {

    ActionPortal(
        title = "Acesso de emergência",
        subtitle =
            "Exiba o botão flutuante para pedir ajuda mesmo fora do aplicativo.",
        meta = "ACESSO EXTERNO",
        icon = R.drawable.ic_0013,
        accent = Color(0xFFFFCF66),
        gradient = listOf(
            Color(0xFF432018),
            Color(0xFF2D1710),
            Color(0xFF1A0C09)
        ),
        c = c,
        font = font
    )
}

/* =========================================================
   ÁREA PROTEGIDA
========================================================= */

@Composable
private fun ProtectedArea(
    c: AppColors,
    font: FontFamily
) {

    ActionPortal(
        title = "Área protegida",
        subtitle =
            "Acesso protegido por biometria",
        meta = "AMBIENTE SEGURO",
        icon = R.drawable.ic_lock,
        accent = Color(0xFFB9A7FF),
        gradient = listOf(
            Color(0xFF292052),
            Color(0xFF211A3C),
            Color(0xFF151329)
        ),
        c = c,
        font = font
    )
}

/* =========================================================
   ÁREA DO AMPARO
========================================================= */

@Composable
private fun AmparoArea(
    c: AppColors,
    font: FontFamily
) {

    ActionPortal(
        title = "Área do amparo",
        subtitle =
            "Acesso protegido por biometria",
        meta = "ESPAÇO RESERVADO",
        icon = R.drawable.ic_0015,
        accent = Color(0xFFE2A2F0),
        gradient = listOf(
            Color(0xFF3B2047),
            Color(0xFF2B1936),
            Color(0xFF1B1124)
        ),
        c = c,
        font = font
    )
}

/* =========================================================
   PORTAL
========================================================= */

@Composable
private fun ActionPortal(
    title: String,
    subtitle: String,
    meta: String,
    icon: Int,
    accent: Color,
    gradient: List<Color>,
    c: AppColors,
    font: FontFamily
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .clip(
                RoundedCornerShape(28.dp)
            )
            .background(
                Brush.linearGradient(gradient)
            )
            .border(
                1.dp,
                accent.copy(.43f),
                RoundedCornerShape(28.dp)
            )
            .padding(20.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(59.dp)
                .clip(
                    RoundedCornerShape(20.dp)
                )
                .background(
                    accent.copy(.09f)
                )
                .border(
                    1.dp,
                    accent.copy(.45f),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(47.dp)
                    .border(
                        1.dp,
                        accent.copy(.25f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Image(
                    painter =
                        painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(31.dp)
                )
            }
        }

        Spacer(
            Modifier.width(12.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(9.dp)
        ) {

            Text(
                title,
                color = Color.White,
                fontFamily = font,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )

            Text(
                subtitle,
                color = accent.copy(.85f),
                fontFamily = font,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 14.sp
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .border(
                            1.dp,
                            accent,
                            RoundedCornerShape(2.dp)
                        )
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    meta,
                    color = accent,
                    fontFamily = font,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.3.sp
                )
            }
        }

        Spacer(
            Modifier.width(12.dp)
        )

        Box(
            modifier = Modifier
                .size(39.dp)
                .clip(
                    RoundedCornerShape(14.dp)
                )
                .background(
                    accent.copy(.08f)
                )
                .border(
                    1.dp,
                    accent.copy(.32f),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Image(
                painter =
                    painterResource(
                        R.drawable.ic_arrow
                    ),
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}