package com.mulheres

import android.content.Context
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.CompositionLocalProvider
import android.content.SharedPreferences
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.window.DialogProperties

import androidx.core.view.WindowCompat

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.activity.enableEdgeToEdge


private val Quicksand = FontFamily(

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


private data class Phase(
    val title: String,
    val icon: Int,
    val description: String,
    val unlock: Double,
    val story: String,
    val highlight: String,
    val timeline: List<Pair<String, String>>
)


private data class Upgrade(
    val id: String,
    val icon: Int,
    val name: String,
    val description: String,
    val baseCost: Double,
    val growth: Double
)


private data class GameState(
    val points: Double = 0.0,
    val legacy: Int = 0,
    val clickLevel: Int = 0,
    val cpsLevel: Int = 0,
    val educationLevel: Int = 0,
    val organizationLevel: Int = 0,
    val pressLevel: Int = 0,
    val memoryLevel: Int = 0,
    val combo: Int = 0,
    val lastClick: Long = 0L
)


private val phases = listOf(

    Phase(
        "Antiguidade",
        R.drawable.ic_history_pottery,
        "A humanidade era formada por sociedades muito diferentes entre si. " +
                "Em muitas delas, mulheres tinham suas vidas fortemente determinadas " +
                "pela família, pelo casamento, pelas leis e pela posição social.",
        0.0,
        "Mulheres participaram ativamente da vida econômica, familiar, religiosa " +
                "e cultural das sociedades antigas. Ao mesmo tempo, muitas estruturas " +
                "sociais colocavam homens em posições de maior autoridade jurídica e política.",
        "Existiram mulheres governantes, escritoras, sacerdotisas, comerciantes e líderes.",
        listOf(
            "Antiguidade" to
                    "Mulheres exerceram diferentes funções sociais, econômicas e religiosas.",

            "Séculos V–IV a.C." to
                    "Filósofas e intelectuais aparecem em registros do mundo grego.",

            "Antigo Egito" to
                    "Algumas mulheres podiam possuir propriedades e administrar bens."
        )
    ),

    Phase(
        "Idade Média",
        R.drawable.ic_history_castle,
        "A Idade Média reuniu sociedades muito diferentes. Para muitas mulheres, " +
                "casamento, família, religião e posição social influenciavam profundamente " +
                "suas possibilidades de vida.",
        500.0,
        "Mulheres trabalhavam na agricultura, no comércio, no artesanato e dentro dos lares. " +
                "Algumas chegaram a administrar propriedades, oficinas e negócios.",
        "Mulheres medievais também foram escritoras, abadessas, governantes, artesãs, " +
                "comerciantes e intelectuais.",
        listOf(
            "Séculos V–XV" to
                    "Mulheres participaram de atividades agrícolas, artesanais, comerciais e religiosas.",

            "Século XII" to
                    "A vida intelectual feminina esteve presente especialmente em comunidades religiosas.",

            "Final da Idade Média" to
                    "Mulheres participaram de atividades comerciais e urbanas."
        )
    ),

    Phase(
        "Idade Moderna",
        R.drawable.ic_history_scroll,
        "Entre os séculos XV e XVIII, transformações políticas, econômicas e culturais " +
                "alteraram as sociedades.",
        2500.0,
        "A expansão da educação e da imprensa abriu novas possibilidades para algumas mulheres. " +
                "Escritoras e intelectuais questionaram ideias sobre a capacidade feminina.",
        "Pensadoras passaram a defender de forma mais explícita a educação e os direitos das mulheres.",
        listOf(
            "Séculos XV–XVI" to
                    "A imprensa ampliou a circulação de ideias.",

            "Séculos XVI–XVII" to
                    "Mulheres participaram de movimentos religiosos, culturais e políticos.",

            "Século XVIII" to
                    "Pensadoras defenderam educação e direitos das mulheres."
        )
    ),

    Phase(
        "Revolução Industrial",
        R.drawable.ic_history_factory,
        "A industrialização mudou profundamente o trabalho. Muitas mulheres passaram " +
                "a trabalhar em fábricas e outros empregos urbanos.",
        10000.0,
        "O crescimento das cidades criou novas oportunidades de trabalho, mas também trouxe " +
                "jornadas extensas e pouca proteção trabalhista.",
        "O século XIX foi marcado pelo crescimento de movimentos que defendiam educação, " +
                "direitos trabalhistas e participação política feminina.",
        listOf(
            "Século XIX" to
                    "A industrialização ampliou o trabalho assalariado feminino.",

            "Final do século XIX" to
                    "Movimentos organizados passaram a exigir direitos civis e educação.",

            "Final do século XIX / início do XX" to
                    "O movimento sufragista ganhou força em diversos países."
        )
    ),

    Phase(
        "Século XX e XXI",
        R.drawable.ic_history_world,
        "O século XX trouxe grandes transformações: expansão do direito ao voto, maior " +
                "acesso à educação, entrada em diferentes profissões e crescimento da " +
                "participação política feminina.",
        25000.0,
        "Conquistas importantes foram obtidas em diferentes países, mas não aconteceram " +
                "todas ao mesmo tempo.",
        "A história continua: mulheres participam de movimentos sociais, ciência, política, " +
                "cultura, tecnologia e inúmeras outras áreas.",
        listOf(
            "Século XX" to
                    "O direito ao voto feminino foi conquistado em diferentes países.",

            "Século XX" to
                    "A presença feminina cresceu em universidades, profissões, ciência e política.",

            "Século XXI" to
                    "Debates sobre igualdade, violência, trabalho, educação e participação política continuam."
        )
    )
)


private val upgrades = listOf(

    Upgrade(
        "click",
        R.drawable.ic_upgrade_voice,
        "Voz coletiva",
        "Aumenta os pontos recebidos por clique.",
        25.0,
        1.55
    ),

    Upgrade(
        "cps",
        R.drawable.ic_upgrade_movement,
        "Movimento",
        "Gera pontos automaticamente.",
        50.0,
        1.60
    ),

    Upgrade(
        "education",
        R.drawable.ic_upgrade_education,
        "Educação",
        "Aumenta todos os ganhos em 10%.",
        250.0,
        1.75
    ),

    Upgrade(
        "organization",
        R.drawable.ic_upgrade_organization,
        "Organização",
        "Aumenta a produção automática em 15%.",
        500.0,
        1.80
    ),

    Upgrade(
        "press",
        R.drawable.ic_upgrade_press,
        "Imprensa",
        "Adiciona +5 pontos ao valor base do clique.",
        2500.0,
        1.90
    ),

    Upgrade(
        "memory",
        R.drawable.ic_upgrade_memory,
        "Memória histórica",
        "Aumenta todos os ganhos em 25%.",
        10000.0,
        2.00
    )
)


private class DiaStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            "historiaDasMulheres",
            Context.MODE_PRIVATE
        )

    fun save(game: GameState) {

        prefs.edit()
            .putLong(
                "points",
                java.lang.Double.doubleToRawLongBits(
                    game.points
                )
            )
            .putInt(
                "legacy",
                game.legacy
            )
            .putInt(
                "clickLevel",
                game.clickLevel
            )
            .putInt(
                "cpsLevel",
                game.cpsLevel
            )
            .putInt(
                "educationLevel",
                game.educationLevel
            )
            .putInt(
                "organizationLevel",
                game.organizationLevel
            )
            .putInt(
                "pressLevel",
                game.pressLevel
            )
            .putInt(
                "memoryLevel",
                game.memoryLevel
            )
            .apply()
    }

    fun load(): GameState {

        return GameState(

            points =
                java.lang.Double.longBitsToDouble(
                    prefs.getLong(
                        "points",
                        java.lang.Double.doubleToRawLongBits(
                            0.0
                        )
                    )
                ),

            legacy =
                prefs.getInt(
                    "legacy",
                    0
                ),

            clickLevel =
                prefs.getInt(
                    "clickLevel",
                    0
                ),

            cpsLevel =
                prefs.getInt(
                    "cpsLevel",
                    0
                ),

            educationLevel =
                prefs.getInt(
                    "educationLevel",
                    0
                ),

            organizationLevel =
                prefs.getInt(
                    "organizationLevel",
                    0
                ),

            pressLevel =
                prefs.getInt(
                    "pressLevel",
                    0
                ),

            memoryLevel =
                prefs.getInt(
                    "memoryLevel",
                    0
                )
        )
    }

    fun clear() {
        prefs.edit()
            .clear()
            .apply()
    }
}


class DiaActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        enableEdgeToEdge()

        super.onCreate(
            savedInstanceState
        )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            true
        )

        setContent {

    CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {

        DiaApp()
    }
}
    }
}


@Composable
private fun DiaApp() {

    val context =
        LocalContext.current

    val storage =
        remember {
            DiaStorage(context)
        }

    /*
     * O TEMA É CONTROLADO EXCLUSIVAMENTE
     * PELO TEMA DO SISTEMA.
     */
    val darkMode =
        isSystemInDarkTheme()

    val background =
        if (darkMode)
            Color(0xFF050507)
        else
            Color(0xFFF5F5F7)

    val card =
        if (darkMode)
            Color(0xFF101014)
        else
            Color.White

    val card2 =
        if (darkMode)
            Color(0xFF17171D)
        else
            Color(0xFFEDEDF2)

    val text =
        if (darkMode)
            Color(0xFFF7F4F8)
        else
            Color(0xFF17171B)

    val muted =
        if (darkMode)
            Color(0xFFAAA6B1)
        else
            Color(0xFF6C6972)

    val border =
        if (darkMode)
            Color.White.copy(
                alpha = .08f
            )
        else
            Color.Black.copy(
                alpha = .09f
            )

    val pink =
        Color(0xFFFF8EAF)

    val pinkLight =
        Color(0xFFFFC1D2)

    val purple =
        Color(0xFFA978FF)

    val purpleDark =
        Color(0xFF7044C7)

    val statusBarColor =
        if (darkMode)
            Color.Black
        else
            Color.White

    DisposableEffect(
        darkMode
    ) {

        windowStatusBars(
            activity =
                context as ComponentActivity,
            color =
                statusBarColor,
            darkIcons =
                !darkMode
        )

        onDispose { }
    }

    Surface(
        modifier =
            Modifier.fillMaxSize(),
        color =
            background
    ) {

        DiaScreen(

            darkMode =
                darkMode,

            storage =
                storage,

            card =
                card,

            card2 =
                card2,

            background =
                background,

            text =
                text,

            muted =
                muted,

            border =
                border,

            pink =
                pink,

            pinkLight =
                pinkLight,

            purple =
                purple,

            purpleDark =
                purpleDark
        )
    }
}


private fun windowStatusBars(
    activity: ComponentActivity,
    color: Color,
    darkIcons: Boolean
) {

    activity.window.statusBarColor =
        AndroidColor.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )

    activity.window.navigationBarColor =
        AndroidColor.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )

    WindowCompat.getInsetsController(
        activity.window,
        activity.window.decorView
    ).apply {

        isAppearanceLightStatusBars =
            darkIcons

        isAppearanceLightNavigationBars =
            darkIcons
    }
}


@Composable
private fun DiaScreen(

    darkMode: Boolean,

    storage: DiaStorage,

    card: Color,

    card2: Color,

    background: Color,

    text: Color,

    muted: Color,

    border: Color,

    pink: Color,

    pinkLight: Color,

    purple: Color,

    purpleDark: Color

) {

    var game by remember {
        mutableStateOf(
            storage.load()
        )
    }

    var showResetDialog by remember {
        mutableStateOf(false)
    }

    var showPrestigeDialog by remember {
        mutableStateOf(false)
    }

    var showFloating by remember {
        mutableStateOf(false)
    }

    var floatingAmount by remember {
        mutableStateOf(0.0)
    }

    var buttonScale by remember {
        mutableStateOf(1f)
    }

    val animatedButtonScale by
        animateFloatAsState(
            targetValue =
                buttonScale,
            animationSpec =
                tween(100),
            label =
                "buttonScale"
        )

    val currentPhase =
        getCurrentPhase(game)

    val scope =
        rememberCoroutineScope()

    /*
     * PRODUÇÃO AUTOMÁTICA
     */

    LaunchedEffect(Unit) {

        while (true) {

            delay(1000)

            val cps =
                getPerSecond(game)

            if (cps > 0) {

                game =
                    game.copy(
                        points =
                            game.points + cps
                    )

                storage.save(game)
            }
        }
    }

    /*
     * EXPIRAÇÃO DO COMBO
     */

    LaunchedEffect(
        game.combo,
        game.lastClick
    ) {

        if (game.combo <= 0) {
            return@LaunchedEffect
        }

        delay(1200)

        if (
            System.currentTimeMillis() -
            game.lastClick >= 1200
        ) {

            game =
                game.copy(
                    combo = 0
                )

            storage.save(game)
        }
    }

    /*
     * VOLTA O BOTÃO AO TAMANHO NORMAL
     */

    LaunchedEffect(
        buttonScale
    ) {

        if (buttonScale < 1f) {

            delay(90)

            buttonScale = 1f
        }
    }

    val listState =
        rememberLazyListState()

    LazyColumn(

        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .background(background)
                .padding(
                    start = 17.dp,
                    end = 17.dp
                ),

        state =
            listState,

        verticalArrangement =
            Arrangement.spacedBy(
                14.dp
            )
    ) {

        item {

            Spacer(
                Modifier.height(
                    18.dp
                )
            )

            Header(
                text =
                    text,
                muted =
                    muted,
                darkMode =
                    darkMode,
                pink =
                    pink,
                purple =
                    purple
            )
        }

        item {

            ScoreCard(
                game =
                    game,
                card =
                    card,
                border =
                    border,
                text =
                    text,
                muted =
                    muted,
                pink =
                    pink,
                purple =
                    purple
            )
        }

        item {

            LegacyCard(
                game =
                    game,
                card =
                    card,
                border =
                    border,
                text =
                    text,
                muted =
                    muted,
                pink =
                    pink,
                purple =
                    purple,
                onPrestige = {

                    if (
                        game.points >=
                        getPrestigeRequirement(
                            game
                        )
                    ) {

                        showPrestigeDialog =
                            true
                    }
                }
            )
        }

        item {

            ClickArea(
                scale =
                    animatedButtonScale,
                game =
                    game,
                pink =
                    pink,
                purple =
                    purple,
                pinkLight =
                    pinkLight,
                onClick = {

                    val now =
                        System.currentTimeMillis()

                    val newCombo =
                        if (
                            now -
                            game.lastClick <
                            1000
                        ) {

                            game.combo + 1

                        } else {

                            1
                        }

                    val comboMultiplier =
                        min(
                            1 +
                                    (
                                        newCombo *
                                                .02
                                    ),
                            2.0
                        )

                    val amount =
                        getPerClick(game) *
                                getMultiplier(game) *
                                comboMultiplier

                    game =
                        game.copy(

                            points =
                                game.points +
                                        amount,

                            combo =
                                newCombo,

                            lastClick =
                                now
                        )

                    floatingAmount =
                        amount

                    showFloating =
                        true

                    buttonScale =
                        .92f

                    storage.save(
                        game
                    )

                    scope.launch {

                        delay(800)

                        showFloating =
                            false
                    }
                }
            )
        }

        item {

            PhaseCard(
                game =
                    game,
                phase =
                    phases[currentPhase],
                phaseIndex =
                    currentPhase,
                card =
                    card,
                border =
                    border,
                text =
                    text,
                muted =
                    muted,
                pink =
                    pink,
                purple =
                    purple
            )
        }

        item {

            SectionTitle(
                title =
                    "Melhorias",
                text =
                    text
            )
        }

        itemsIndexed(
            upgrades.chunked(2)
        ) { _, rowItems ->

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                rowItems.forEach { upgrade ->

                    UpgradeCard(

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        upgrade =
                            upgrade,

                        game =
                            game,

                        card =
                            card,

                        card2 =
                            card2,

                        border =
                            border,

                        text =
                            text,

                        muted =
                            muted,

                        pink =
                            pink,

                        pinkLight =
                            pinkLight,

                        purple =
                            purple,

                        onBuy = {

                            val price =
                                getUpgradePrice(
                                    game,
                                    upgrade
                                )

                            if (
                                game.points >=
                                price
                            ) {

                                game =
                                    buyUpgrade(
                                        game,
                                        upgrade
                                    )

                                storage.save(
                                    game
                                )
                            }
                        }
                    )
                }

                if (
                    rowItems.size == 1
                ) {

                    Spacer(
                        Modifier.weight(
                            1f
                        )
                    )
                }
            }
        }

        item {

            SectionTitle(
                title =
                    "As cinco fases",
                text =
                    text
            )
        }

        itemsIndexed(
            phases
        ) { index, phase ->

            StoryCard(

                phase =
                    phase,

                index =
                    index,

                unlocked =
                    game.points >=
                            phase.unlock,

                card =
                    card,

                border =
                    border,

                text =
                    text,

                muted =
                    muted,

                pink =
                    pink,

                pinkLight =
                    pinkLight,

                purple =
                    purple
            )
        }

        item {

            SourcesCard(

                card =
                    card,

                border =
                    border,

                text =
                    text,

                muted =
                    muted,

                blue =
                    Color(0xFF4D8DFF)
            )
        }

        item {

            Button(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            bottom = 20.dp
                        ),

                onClick = {
                    showResetDialog =
                        true
                },

                shape =
                    RoundedCornerShape(
                        15.dp
                    ),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color.Transparent,
                        contentColor =
                            muted
                    ),

                border =
                    BorderStroke(
                        1.dp,
                        border
                    )
            ) {

                Text(
                    text =
                        "Reiniciar progresso",
                    fontFamily =
                        Quicksand,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }

    /*
     * NÚMERO FLUTUANTE
     */

    AnimatedVisibility(
        visible =
            showFloating
    ) {

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    "+" +
                            formatNumber(
                                floatingAmount
                            ),
                color =
                    Color.White,
                fontFamily =
                    Quicksand,
                fontWeight =
                    FontWeight.ExtraBold,
                fontSize =
                    23.sp
            )
        }
    }

    /*
     * PRESTÍGIO
     */

    if (showPrestigeDialog) {

        val reward =
            getPrestigeReward(
                game
            )

        ConfirmDialog(

            title =
                "Realizar o prestígio?",

            message =
                "Pontos atuais: " +
                        formatNumber(
                            game.points
                        ) +
                        "\n\n" +
                        "Os upgrades normais serão resetados.\n\n" +
                        "O Legado permanecerá.\n\n" +
                        "Recompensa: +" +
                        reward +
                        " Legado.",

            confirmText =
                "Prestigiar",

            onDismiss = {
                showPrestigeDialog =
                    false
            },

            onConfirm = {

                game =
                    GameState(

                        points =
                            0.0,

                        legacy =
                            game.legacy +
                                    reward
                    )

                storage.save(
                    game
                )

                showPrestigeDialog =
                    false
            },

            pink =
                pink,

            purple =
                purple,

            card =
                card,

            text =
                text,

            muted =
                muted
        )
    }

    /*
     * RESET
     */

    if (showResetDialog) {

        ConfirmDialog(

            title =
                "Reiniciar progresso",

            message =
                "Reiniciar todo o progresso?\n\n" +
                        "Isso apagará:\n" +
                        "• Pontos\n" +
                        "• Upgrades\n" +
                        "• Legado\n" +
                        "• Bônus permanentes\n\n" +
                        "Esta ação não pode ser desfeita.",

            confirmText =
                "Reiniciar",

            onDismiss = {
                showResetDialog =
                    false
            },

            onConfirm = {

                storage.clear()

                game =
                    GameState()

                showResetDialog =
                    false
            },

            pink =
                pink,

            purple =
                purple,

            card =
                card,

            text =
                text,

            muted =
                muted
        )
    }
}


@Composable
private fun Header(

    text: Color,

    muted: Color,

    darkMode: Boolean,

    pink: Color,

    purple: Color

) {

    Column(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(

            text =
                "História das Mulheres",

            style =
                TextStyle(

                    fontFamily =
                        Quicksand,

                    fontWeight =
                        FontWeight.ExtraBold,

                    fontSize =
                        32.sp,

                    textAlign =
                        TextAlign.Center,

                    brush =
                        Brush.horizontalGradient(
                            listOf(
                                pink,
                                purple,
                                pink
                            )
                        )
                )
        )

        Spacer(
            Modifier.height(
                8.dp
            )
        )

        Text(

            text =
                "Avance através de cinco grandes períodos " +
                        "e descubra lutas, transformações e conquistas " +
                        "ao longo da história.",

            color =
                muted,

            fontFamily =
                Quicksand,

            fontSize =
                14.sp,

            lineHeight =
                22.sp,

            textAlign =
                TextAlign.Center
        )

        Spacer(
            Modifier.height(
                12.dp
            )
        )
    }
}


@Composable
private fun ScoreCard(

    game: GameState,

    card: Color,

    border: Color,

    text: Color,

    muted: Color,

    pink: Color,

    purple: Color

) {

    CardContainer(
        color =
            card,
        border =
            border,
        radius =
            28.dp
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    "PONTOS DE HISTÓRIA",
                color =
                    muted,
                fontFamily =
                    Quicksand,
                fontWeight =
                    FontWeight.ExtraBold,
                fontSize =
                    12.sp,
                letterSpacing =
                    1.5.sp
            )

            Text(
                text =
                    formatNumber(
                        game.points
                    ),
                color =
                    pink,
                fontFamily =
                    Quicksand,
                fontWeight =
                    FontWeight.ExtraBold,
                fontSize =
                    58.sp
            )

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.Center
            ) {

                Stat(
                    "Por clique:",
                    formatNumber(
                        getPerClick(game) *
                                getMultiplier(game)
                    ),
                    text,
                    muted
                )

                Spacer(
                    Modifier.width(
                        8.dp
                    )
                )

                Stat(
                    "Por segundo:",
                    formatNumber(
                        getPerSecond(game)
                    ),
                    text,
                    muted
                )
            }

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.Center
            ) {

                Stat(
                    "Multiplicador:",
                    getMultiplier(game)
                        .toFixed(1) +
                            "x",
                    text,
                    muted
                )

                Spacer(
                    Modifier.width(
                        8.dp
                    )
                )

                Stat(
                    "Combo:",
                    "x" +
                            max(
                                1,
                                game.combo
                            ),
                    text,
                    muted
                )
            }
        }
    }
}


@Composable
private fun Stat(

    label: String,

    value: String,

    text: Color,

    muted: Color

) {

    Row(

        modifier =
            Modifier
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(
                    Color.White.copy(
                        .035f
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(
                        .08f
                    ),
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .padding(
                    horizontal = 11.dp,
                    vertical = 8.dp
                )
    ) {

        Text(
            text =
                label,
            color =
                muted,
            fontFamily =
                Quicksand,
            fontSize =
                11.sp
        )

        Spacer(
            Modifier.width(
                4.dp
            )
        )

        Text(
            text =
                value,
            color =
                text,
            fontFamily =
                Quicksand,
            fontWeight =
                FontWeight.Bold,
            fontSize =
                11.sp
        )
    }
}


@Composable
private fun LegacyCard(

    game: GameState,

    card: Color,

    border: Color,

    text: Color,

    muted: Color,

    pink: Color,

    purple: Color,

    onPrestige: () -> Unit

) {

    val requirement =
        getPrestigeRequirement(
            game
        )

    val canPrestige =
        game.points >=
                requirement

    val reward =
        getPrestigeReward(
            game
        )

    CardContainer(

        color =
            Brush.linearGradient(
                listOf(
                    purple.copy(.13f),
                    pink.copy(.08f)
                )
            ),

        border =
            purple.copy(.20f),

        radius =
            21.dp
    ) {

        Column {

            Text(
                text =
                    "LEGADO PERMANENTE",
                color =
                    purple,
                fontFamily =
                    Quicksand,
                fontWeight =
                    FontWeight.ExtraBold,
                fontSize =
                    11.sp,
                letterSpacing =
                    1.5.sp
            )

            Spacer(
                Modifier.height(
                    2.dp
                )
            )

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {

                    Text(
                        text =
                            formatNumber(
                                game.legacy.toDouble()
                            ),
                        color =
                            text,
                        fontFamily =
                            Quicksand,
                        fontWeight =
                            FontWeight.ExtraBold,
                        fontSize =
                            25.sp
                    )

                    Text(
                        text =
                            if (canPrestige)
                                "Você receberá +$reward Legado."
                            else
                                "Necessário " +
                                        formatNumber(
                                            requirement
                                        ) +
                                        " pontos.",
                        color =
                            muted,
                        fontFamily =
                            Quicksand,
                        fontSize =
                            11.sp
                    )
                }

                Spacer(
                    Modifier.width(
                        12.dp
                    )
                )

                Button(

                    onClick =
                        onPrestige,

                    enabled =
                        canPrestige,

                    shape =
                        RoundedCornerShape(
                            15.dp
                        ),

                    colors =
                        ButtonDefaults.buttonColors(

                            containerColor =
                                purple,

                            disabledContainerColor =
                                purple.copy(
                                    .30f
                                ),

                            contentColor =
                                Color.White
                        )
                ) {

                    Text(
                        text =
                            if (canPrestige)
                                "Prestigiar"
                            else
                                "Prestígio",

                        fontFamily =
                            Quicksand,

                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}


@Composable
private fun ClickArea(

    scale: Float,

    game: GameState,

    pink: Color,

    purple: Color,

    pinkLight: Color,

    onClick: () -> Unit

) {

    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 10.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            if (game.combo >= 3) {

                Text(
                    text =
                        "COMBO x" +
                                game.combo,

                    color =
                        pinkLight,

                    fontFamily =
                        Quicksand,

                    fontWeight =
                        FontWeight.ExtraBold,

                    fontSize =
                        13.sp
                )

                Spacer(
                    Modifier.height(
                        5.dp
                    )
                )
            }

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .scale(scale)
                        .clip(
                            RoundedCornerShape(
                                30.dp
                            )
                        )
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFD5E1),
                                    pink,
                                    purple
                                )
                            )
                        )
                        .clickable(
                            onClick =
                                onClick
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        "+ HISTÓRIA",
                    color =
                        Color.White,
                    fontFamily =
                        Quicksand,
                    fontWeight =
                        FontWeight.ExtraBold,
                    fontSize =
                        21.sp
                )
            }
        }
    }
}


@Composable
private fun PhaseCard(

    game: GameState,

    phase: Phase,

    phaseIndex: Int,

    card: Color,

    border: Color,

    text: Color,

    muted: Color,

    pink: Color,

    purple: Color

) {

    CardContainer(

        color =
            card,

        border =
            border,

        radius =
            22.dp
    ) {

        Text(
            text =
                "FASE ${phaseIndex + 1}",
            color =
                pink,
            fontFamily =
                Quicksand,
            fontWeight =
                FontWeight.ExtraBold,
            fontSize =
                12.sp,
            letterSpacing =
                1.sp
        )

        Text(
            text =
                phase.title,
            color =
                text,
            fontFamily =
                Quicksand,
            fontWeight =
                FontWeight.ExtraBold,
            fontSize =
                25.sp,
            modifier =
                Modifier.padding(
                    top = 5.dp,
                    bottom = 10.dp
                )
        )

        Text(
            text =
                phase.description,
            color =
                muted,
            fontFamily =
                Quicksand,
            fontSize =
                14.sp,
            lineHeight =
                23.sp
        )

        Spacer(
            Modifier.height(
                18.dp
            )
        )

        val progress =
            if (
                phaseIndex >=
                phases.lastIndex
            ) {

                1f

            } else {

                val current =
                    phase.unlock

                val next =
                    phases[
                        phaseIndex + 1
                    ].unlock

                (
                    (
                        game.points -
                                current
                    ) /
                            (
                                next -
                                        current
                            )
                    )
                    .coerceIn(
                        0.0,
                        1.0
                    )
                    .toFloat()
            }

        Box(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(
                        CircleShape
                    )
                    .background(
                        Color(0xFF222229)
                    )
        ) {

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth(
                            progress
                        )
                        .height(9.dp)
                        .clip(
                            CircleShape
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    pink,
                                    purple
                                )
                            )
                        )
            )
        }
    }
}


@Composable
private fun SectionTitle(
    title: String,
    text: Color
) {

    Text(
        text =
            title,
        color =
            text,
        fontFamily =
            Quicksand,
        fontWeight =
            FontWeight.ExtraBold,
        fontSize =
            20.sp,
        modifier =
            Modifier.padding(
                top = 10.dp,
                bottom = 2.dp
            )
    )
}


@Composable
private fun UpgradeCard(

    modifier: Modifier,

    upgrade: Upgrade,

    game: GameState,

    card: Color,

    card2: Color,

    border: Color,

    text: Color,

    muted: Color,

    pink: Color,

    pinkLight: Color,

    purple: Color,

    onBuy: () -> Unit

) {

    val level =
        getUpgradeLevel(
            game,
            upgrade.id
        )

    val price =
        getUpgradePrice(
            game,
            upgrade
        )

    val enabled =
        game.points >=
                price

    Column(

        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        20.dp
                    )
                )
                .background(
                    if (enabled)
                        card
                    else
                        card.copy(
                            alpha = .55f
                        )
                )
                .border(
                    1.dp,
                    if (enabled)
                        border
                    else
                        border.copy(.5f),
                    RoundedCornerShape(
                        20.dp
                    )
                )
                .clickable(
                    enabled =
                        enabled,
                    onClick =
                        onBuy
                )
                .padding(
                    18.dp
                )
    ) {

        Image(

            painter =
                painterResource(
                    upgrade.icon
                ),

            contentDescription =
                upgrade.name,

            modifier =
                Modifier.size(
                    30.dp
                ),

            contentScale =
                ContentScale.Fit
        )

        Spacer(
            Modifier.height(
                9.dp
            )
        )

        Text(
            text =
                upgrade.name,
            color =
                text,
            fontFamily =
                Quicksand,
            fontWeight =
                FontWeight.ExtraBold,
            fontSize =
                16.sp
        )

        Spacer(
            Modifier.height(
                5.dp
            )
        )

        Text(
            text =
                upgrade.description,
            color =
                muted,
            fontFamily =
                Quicksand,
            fontSize =
                12.sp,
            lineHeight =
                18.sp
        )

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text =
                    "Custo: " +
                            formatNumber(
                                price
                            ),
                color =
                    pinkLight,
                fontFamily =
                    Quicksand,
                fontWeight =
                    FontWeight.ExtraBold,
                fontSize =
                    11.sp
            )

            Text(
                text =
                    "Nv. $level",
                color =
                    purple,
                fontFamily =
                    Quicksand,
                fontWeight =
                    FontWeight.ExtraBold,
                fontSize =
                    11.sp
            )
        }
    }
}


@Composable
private fun StoryCard(

    phase: Phase,

    index: Int,

    unlocked: Boolean,

    card: Color,

    border: Color,

    text: Color,

    muted: Color,

    pink: Color,

    pinkLight: Color,

    purple: Color

) {

    CardContainer(

        color =
            card,

        border =
            border,

        radius =
            22.dp,

        modifier =
            Modifier.alpha(
                if (unlocked)
                    1f
                else
                    .4f
            )
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            if (unlocked) {

                Image(

                    painter =
                        painterResource(
                            phase.icon
                        ),

                    contentDescription =
                        phase.title,

                    modifier =
                        Modifier.size(
                            30.dp
                        )
                )

            } else {

                Image(

                    painter =
                        painterResource(
                            R.drawable.ic_lock
                        ),

                    contentDescription =
                        "Bloqueado",

                    modifier =
                        Modifier.size(
                            28.dp
                        )
                )
            }

            Spacer(
                Modifier.width(
                    10.dp
                )
            )

            Text(

                text =
                    "Fase ${index + 1} — " +
                            phase.title,

                color =
                    text,

                fontFamily =
                    Quicksand,

                fontWeight =
                    FontWeight.ExtraBold,

                fontSize =
                    19.sp
            )
        }

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        if (!unlocked) {

            Text(
                text =
                    "Esta fase ainda está bloqueada.",
                color =
                    muted,
                fontFamily =
                    Quicksand,
                fontSize =
                    14.sp
            )

            Spacer(
                Modifier.height(
                    15.dp
                )
            )

            Highlight(

                text =
                    "Necessário: " +
                            formatNumber(
                                phase.unlock
                            ) +
                            " pontos.",

                pink =
                    pink,

                pinkLight =
                    pinkLight
            )

        } else {

            Text(
                text =
                    phase.story,
                color =
                    muted,
                fontFamily =
                    Quicksand,
                fontSize =
                    14.sp,
                lineHeight =
                    24.sp
            )

            Spacer(
                Modifier.height(
                    15.dp
                )
            )

            Highlight(

                text =
                    phase.highlight,

                pink =
                    pink,

                pinkLight =
                    pinkLight
            )

            Spacer(
                Modifier.height(
                    20.dp
                )
            )

            Timeline(

                timeline =
                    phase.timeline,

                muted =
                    muted,

                pink =
                    pink,

                purple =
                    purple
            )
        }
    }
}


@Composable
private fun Highlight(

    text: String,

    pink: Color,

    pinkLight: Color

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topEnd = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .background(
                    pink.copy(.05f)
                )
                .padding(
                    start = 12.dp,
                    top = 11.dp,
                    bottom = 11.dp,
                    end = 11.dp
                )
    ) {

        Box(

            modifier =
                Modifier
                    .width(3.dp)
                    .height(35.dp)
                    .clip(
                        CircleShape
                    )
                    .background(
                        pink
                    )
        )

        Spacer(
            Modifier.width(
                10.dp
            )
        )

        Text(
            text =
                text,
            color =
                pinkLight,
            fontFamily =
                Quicksand,
            fontSize =
                13.sp,
            lineHeight =
                19.sp
        )
    }
}


@Composable
private fun Timeline(

    timeline:
        List<Pair<String, String>>,

    muted: Color,

    pink: Color,

    purple: Color

) {

    Column {

        timeline.forEachIndexed {
                index,
                item ->

            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Box(

                        modifier =
                            Modifier
                                .size(
                                    14.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .background(
                                    pink
                                )
                    )

                    if (
                        index <
                        timeline.lastIndex
                    ) {

                        Box(

                            modifier =
                                Modifier
                                    .width(
                                        2.dp
                                    )
                                    .height(
                                        70.dp
                                    )
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                pink,
                                                purple
                                            )
                                        )
                                    )
                        )
                    }
                }

                Spacer(
                    Modifier.width(
                        14.dp
                    )
                )

                Column(

                    modifier =
                        Modifier.padding(
                            bottom = 15.dp
                        )
                ) {

                    Text(
                        text =
                            item.first,
                        color =
                            pink,
                        fontFamily =
                            Quicksand,
                        fontWeight =
                            FontWeight.ExtraBold,
                        fontSize =
                            12.sp
                    )

                    Spacer(
                        Modifier.height(
                            4.dp
                        )
                    )

                    Text(
                        text =
                            item.second,
                        color =
                            muted,
                        fontFamily =
                            Quicksand,
                        fontSize =
                            13.sp,
                        lineHeight =
                            20.sp
                    )
                }
            }
        }
    }
}


@Composable
private fun SourcesCard(

    card: Color,

    border: Color,

    text: Color,

    muted: Color,

    blue: Color

) {

    CardContainer(

        color =
            card,

        border =
            border,

        radius =
            22.dp
    ) {

        Text(
            text =
                "Fontes e referências",
            color =
                text,
            fontFamily =
                Quicksand,
            fontWeight =
                FontWeight.ExtraBold,
            fontSize =
                21.sp
        )

        Spacer(
            Modifier.height(
                10.dp
            )
        )

        Text(
            text =
                "Conteúdo histórico elaborado com base em fontes " +
                        "bibliográficas e institucionais.",
            color =
                muted,
            fontFamily =
                Quicksand,
            fontSize =
                14.sp,
            lineHeight =
                22.sp
        )

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        val sources =
            listOf(

                "https://www.metmuseum.org/-/media/files/learn/for-educators/publications-for-educators/the-art-of-ancient-egypt.pdf",

                "https://www.britishmuseum.org/blog/mary-beards-top-five-powerful-women-ancient-greece-and-rome",

                "https://www.worldhistory.org/trans/pt/2-2081/mulheres-na-antiga-mesopotamia/",

                "https://www.worldhistory.org/trans/pt/2-927/as-mulheres-na-grecia-antiga/"
            )

        sources.forEach { source ->

            Text(

                text =
                    source,

                color =
                    blue,

                fontFamily =
                    Quicksand,

                fontSize =
                    11.sp,

                lineHeight =
                    17.sp,

                modifier =
                    Modifier.padding(
                        bottom = 8.dp
                    )
            )
        }
    }
}


@Composable
private fun CardContainer(

    color: Color,

    border: Color,

    radius: Dp,

    modifier: Modifier =
        Modifier,

    content:
        @Composable ColumnScope.() -> Unit

) {

    Column(

        modifier =
            modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        radius
                    )
                )
                .background(
                    color
                )
                .border(
                    1.dp,
                    border,
                    RoundedCornerShape(
                        radius
                    )
                )
                .padding(
                    22.dp
                ),

        content =
            content
    )
}


@Composable
private fun CardContainer(

    color: Brush,

    border: Color,

    radius: Dp,

    modifier: Modifier =
        Modifier,

    content:
        @Composable ColumnScope.() -> Unit

) {

    Column(

        modifier =
            modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        radius
                    )
                )
                .background(
                    color
                )
                .border(
                    1.dp,
                    border,
                    RoundedCornerShape(
                        radius
                    )
                )
                .padding(
                    17.dp
                ),

        content =
            content
    )
}


@Composable
private fun ConfirmDialog(

    title: String,

    message: String,

    confirmText: String,

    onDismiss: () -> Unit,

    onConfirm: () -> Unit,

    pink: Color,

    purple: Color,

    card: Color,

    text: Color,

    muted: Color

) {

    AlertDialog(

        onDismissRequest =
            onDismiss,

        properties =
            DialogProperties(
                dismissOnBackPress =
                    true,
                dismissOnClickOutside =
                    true
            ),

        containerColor =
            card,

        shape =
            RoundedCornerShape(
                25.dp
            ),

        title = {

            Text(
                text =
                    title,
                color =
                    text,
                fontFamily =
                    Quicksand,
                fontWeight =
                    FontWeight.ExtraBold,
                fontSize =
                    18.sp
            )
        },

        text = {

            Text(
                text =
                    message,
                color =
                    muted,
                fontFamily =
                    Quicksand,
                fontSize =
                    12.sp,
                lineHeight =
                    19.sp
            )
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    text =
                        "Cancelar",
                    color =
                        muted,
                    fontFamily =
                        Quicksand,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        },

        confirmButton = {

            Button(

                onClick =
                    onConfirm,

                shape =
                    RoundedCornerShape(
                        14.dp
                    ),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            purple,
                        contentColor =
                            Color.White
                    )
            ) {

                Text(
                    text =
                        confirmText,
                    fontFamily =
                        Quicksand,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }
    )
}


private fun getMultiplier(
    game: GameState
): Double {

    var value =
        1.0

    value +=
        game.educationLevel *
                0.10

    value +=
        game.memoryLevel *
                0.25

    value +=
        game.legacy *
                0.10

    return value
}


private fun getPerClick(
    game: GameState
): Double {

    var value =
        1.0

    value +=
        game.clickLevel

    value +=
        game.pressLevel *
                5

    return value
}


private fun getPerSecond(
    game: GameState
): Double {

    val base =
        game.cpsLevel.toDouble()

    val organization =
        1.0 +
                (
                    game.organizationLevel *
                            0.15
                )

    return base *
            organization *
            getMultiplier(
                game
            )
}


private fun getUpgradeLevel(
    game: GameState,
    id: String
): Int {

    return when (id) {

        "click" ->
            game.clickLevel

        "cps" ->
            game.cpsLevel

        "education" ->
            game.educationLevel

        "organization" ->
            game.organizationLevel

        "press" ->
            game.pressLevel

        "memory" ->
            game.memoryLevel

        else ->
            0
    }
}


private fun getUpgradePrice(
    game: GameState,
    upgrade: Upgrade
): Double {

    val level =
        getUpgradeLevel(
            game,
            upgrade.id
        )

    return floor(
        upgrade.baseCost *
                upgrade.growth.pow(
                    level
                )
    )
}


private fun buyUpgrade(
    game: GameState,
    upgrade: Upgrade
): GameState {

    val price =
        getUpgradePrice(
            game,
            upgrade
        )

    if (
        game.points <
        price
    ) {

        return game
    }

    return when (
        upgrade.id
    ) {

        "click" ->

            game.copy(

                points =
                    game.points -
                            price,

                clickLevel =
                    game.clickLevel +
                            1
            )

        "cps" ->

            game.copy(

                points =
                    game.points -
                            price,

                cpsLevel =
                    game.cpsLevel +
                            1
            )

        "education" ->

            game.copy(

                points =
                    game.points -
                            price,

                educationLevel =
                    game.educationLevel +
                            1
            )

        "organization" ->

            game.copy(

                points =
                    game.points -
                            price,

                organizationLevel =
                    game.organizationLevel +
                            1
            )

        "press" ->

            game.copy(

                points =
                    game.points -
                            price,

                pressLevel =
                    game.pressLevel +
                            1
            )

        "memory" ->

            game.copy(

                points =
                    game.points -
                            price,

                memoryLevel =
                    game.memoryLevel +
                            1
            )

        else ->
            game
    }
}


private fun getCurrentPhase(
    game: GameState
): Int {

    var current =
        0

    phases.forEachIndexed {
            index,
            phase ->

        if (
            game.points >=
            phase.unlock
        ) {

            current =
                index
        }
    }

    return current
}


private fun getPrestigeRequirement(
    game: GameState
): Double {

    return 100000.0 *
            2.0.pow(
                game.legacy
            )
}


private fun getPrestigeReward(
    game: GameState
): Int {

    return max(

        1,

        floor(

            sqrt(
                game.points /
                        100000.0
            )

        ).toInt()
    )
}


private fun formatNumber(
    number: Double
): String {

    if (
        !number.isFinite()
    ) {

        return "0"
    }

    if (
        number < 1000
    ) {

        return number
            .toInt()
            .toString()
    }

    if (
        number < 1_000_000
    ) {

        return (

            number /
                    1000

        )
            .toFixed(1)
            .replace(
                ".0",
                ""
            ) +
                " mil"
    }

    if (
        number < 1_000_000_000
    ) {

        return (

            number /
                    1_000_000

        )
            .toFixed(1)
            .replace(
                ".0",
                ""
            ) +
                " mi"
    }

    if (
        number < 1_000_000_000_000
    ) {

        return (

            number /
                    1_000_000_000

        )
            .toFixed(1)
            .replace(
                ".0",
                ""
            ) +
                " bi"
    }

    return (

        number /
                1_000_000_000_000

    )
        .toFixed(1)
        .replace(
            ".0",
            ""
        ) +
            " tri"
}


private fun Double.toFixed(
    digits: Int
): String {

    return "%.${digits}f".format(
        java.util.Locale.US,
        this
    )
}