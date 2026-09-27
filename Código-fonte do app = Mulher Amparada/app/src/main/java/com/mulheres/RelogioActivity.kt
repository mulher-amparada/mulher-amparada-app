package com.mulheres

import androidx.compose.material3.Icon
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

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

private val Rosa = Color(0xFFFF8FAB)
private val Rosa2 = Color(0xFFFF4F78)
private val Rosa3 = Color(0xFFFFC2D1)

private val Verde = Color(0xFF63F59A)
private val VerdeTexto = Color(0xFF9DFFBD)

private val FundoEscuro = Color(0xFF08080A)
private val CartaoEscuro = Color(0xFF111115)
private val CartaoEscuro2 = Color(0xFF151519)

private val TextoEscuro = Color.White
private val TextoSuaveEscuro = Color(0xFF9E9EA8)
private val TextoMutedEscuro = Color(0xFF777782)

private val FundoClaro = Color(0xFFF7F7FA)
private val CartaoClaro = Color.White
private val TextoClaro = Color(0xFF17171B)
private val TextoSuaveClaro = Color(0xFF686873)
private val TextoMutedClaro = Color(0xFF92929D)

class RelogioActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            CompositionLocalProvider(
                LocalOverscrollFactory provides null
            ) {
                RelogioApp()
            }
        }
    }
}

data class DadosRelogio(
    val hora: String,
    val minuto: String,
    val segundo: String,
    val data: String,
    val fuso: String,
    val ano: Int,
    val semestre: String,
    val mes: String,
    val semana: Int,
    val bimestre: String,
    val quinzena: String,
    val diaDoAno: Int,
    val utc: String,
    val progresso: Float
)

@Composable
private fun RelogioApp() {

    val dark = isSystemInDarkTheme()

    val fundo =
        if (dark) FundoEscuro else FundoClaro

    CompositionLocalProvider(
        androidx.compose.material3.LocalTextStyle provides
            MaterialTheme.typography.bodyMedium.copy(
                fontFamily = Quicksand
            )
    ) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = fundo
        ) {

            RelogioTela(
                dark = dark,
                fundo = fundo
            )
        }
    }
}

@Composable
private fun RelogioTela(
    dark: Boolean,
    fundo: Color
) {

    /*
     * IMPORTANTE:
     * LocalContext.current precisa ser obtido diretamente
     * dentro de uma função @Composable.
     */
    val context =
        androidx.compose.ui.platform.LocalContext.current

    val fusedLocationClient =
        remember(context) {
            LocationServices.getFusedLocationProviderClient(
                context
            )
        }

    var dados by remember {
        mutableStateOf(
            obterDadosRelogio()
        )
    }

    var pais by remember {
        mutableStateOf("Localização atual")
    }

    var cidade by remember {
        mutableStateOf("Obtendo localização...")
    }

    var statusLocalizacao by remember {
        mutableStateOf("Detectando localização")
    }

    var textoStatus by remember {
        mutableStateOf(
            "O relógio usa somente o horário da sua localização atual."
        )
    }

    var carregandoLocalizacao by remember {
        mutableStateOf(false)
    }

    var mostrarToast by remember {
        mutableStateOf(false)
    }

    val permissaoLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { concedida ->

            if (concedida) {

                obterLocalizacao(
                    context = context,
                    fusedLocationClient = fusedLocationClient,
                    onLoading = {
                        carregandoLocalizacao = true
                    },
                    onResult = { novoPais, novaCidade ->

                        pais = novoPais
                        cidade = novaCidade

                        statusLocalizacao =
                            "Localização detectada"

                        textoStatus =
                            "O relógio está usando somente o horário desta localização."

                        carregandoLocalizacao = false
                        mostrarToast = true
                    },
                    onError = {

                        pais = "Localização atual"
                        cidade = "GPS detectado"

                        statusLocalizacao =
                            "GPS ativo"

                        textoStatus =
                            "A localização foi detectada, mas o nome da cidade não pôde ser obtido."

                        carregandoLocalizacao = false
                    }
                )

            } else {

                pais =
                    "Localização não autorizada"

                cidade =
                    "Ative o acesso à localização"

                statusLocalizacao =
                    "Permissão necessária"

                textoStatus =
                    "Permita o acesso à localização para identificar sua cidade."

                carregandoLocalizacao = false
            }
        }

    LaunchedEffect(Unit) {

        while (true) {

            dados =
                obterDadosRelogio()

            delay(1000)
        }
    }

    LaunchedEffect(Unit) {

        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            obterLocalizacao(
                context = context,
                fusedLocationClient = fusedLocationClient,
                onLoading = {
                    carregandoLocalizacao = true
                },
                onResult = { novoPais, novaCidade ->

                    pais = novoPais
                    cidade = novaCidade

                    statusLocalizacao =
                        "Localização detectada"

                    textoStatus =
                        "O relógio está usando somente o horário desta localização."

                    carregandoLocalizacao = false
                },
                onError = {

                    pais = "Localização atual"
                    cidade = "GPS detectado"

                    statusLocalizacao =
                        "GPS ativo"

                    textoStatus =
                        "A localização foi detectada, mas o nome da cidade não pôde ser obtido."

                    carregandoLocalizacao = false
                }
            )

        } else {

            permissaoLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    LaunchedEffect(mostrarToast) {

        if (mostrarToast) {

            delay(2500)

            mostrarToast = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                androidx.compose.foundation.layout.PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 20.dp,
                    bottom = 30.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(0.dp)
        ) {

            item {

                Topo(
                    dark = dark,
                    aoAtualizar = {

                        if (
                            ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) ==
                            PackageManager.PERMISSION_GRANTED
                        ) {

                            obterLocalizacao(
                                context = context,
                                fusedLocationClient = fusedLocationClient,
                                onLoading = {
                                    carregandoLocalizacao = true
                                },
                                onResult = { novoPais, novaCidade ->

                                    pais = novoPais
                                    cidade = novaCidade

                                    statusLocalizacao =
                                        "Localização detectada"

                                    textoStatus =
                                        "O relógio está usando somente o horário desta localização."

                                    carregandoLocalizacao = false
                                    mostrarToast = true
                                },
                                onError = {

                                    statusLocalizacao =
                                        "GPS ativo"

                                    textoStatus =
                                        "A localização foi detectada, mas o nome da cidade não pôde ser obtido."

                                    carregandoLocalizacao = false
                                }
                            )

                        } else {

                            permissaoLauncher.launch(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }

            item {

                HeroRelogio(
                    dados = dados,
                    pais = pais,
                    cidade = cidade,
                    status = statusLocalizacao,
                    dark = dark
                )

                Spacer(
                    modifier = Modifier.height(22.dp)
                )
            }

            item {

                Text(
                    text = "Informações de hoje",
                    color =
                        if (dark)
                            TextoEscuro
                        else
                            TextoClaro,
                    fontFamily = Quicksand,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(
                        start = 2.dp,
                        bottom = 11.dp
                    )
                )
            }

            item {

                Informacoes(
                    dados = dados,
                    dark = dark
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }

            item {

                StatusCard(
                    texto = textoStatus,
                    dark = dark,
                    carregando = carregandoLocalizacao,
                    aoAtualizar = {

                        if (
                            ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) ==
                            PackageManager.PERMISSION_GRANTED
                        ) {

                            obterLocalizacao(
                                context = context,
                                fusedLocationClient = fusedLocationClient,
                                onLoading = {
                                    carregandoLocalizacao = true
                                },
                                onResult = { novoPais, novaCidade ->

                                    pais = novoPais
                                    cidade = novaCidade

                                    statusLocalizacao =
                                        "Localização detectada"

                                    textoStatus =
                                        "O relógio está usando somente o horário desta localização."

                                    carregandoLocalizacao = false
                                    mostrarToast = true
                                },
                                onError = {

                                    carregandoLocalizacao = false
                                }
                            )

                        } else {

                            permissaoLauncher.launch(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }
                    }
                )
            }
        }

        if (mostrarToast) {

            ToastRelogio(
                dark = dark,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = 24.dp,
                        start = 15.dp,
                        end = 15.dp
                    )
            )
        }
    }
}

@Composable
private fun Topo(
    dark: Boolean,
    aoAtualizar: () -> Unit
) {

    val texto =
        if (dark) TextoEscuro else TextoClaro

    val suave =
        if (dark)
            TextoSuaveEscuro
        else
            TextoSuaveClaro

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "Meu Relógio",
                fontFamily = Quicksand,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                letterSpacing = (-2).sp,
                color = texto
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Horário da sua localização atual",
                fontFamily = Quicksand,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = suave
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(
                    RoundedCornerShape(15.dp)
                )
                .background(
                    if (dark)
                        CartaoEscuro
                    else
                        CartaoClaro
                )
                .border(
                    width = 1.dp,
                    color =
                        if (dark)
                            Color.White.copy(alpha = .09f)
                        else
                            Color.Black.copy(alpha = .07f),
                    shape =
                        RoundedCornerShape(15.dp)
                )
                .clickable {
                    aoAtualizar()
                },
            contentAlignment = Alignment.Center
        ) {

            Icon(
    painter = painterResource(
        id = R.drawable.ic_refresh
    ),
    contentDescription = "Atualizar localização",
    tint =
        if (dark)
            Color.White
        else
            TextoClaro,
    modifier = Modifier.size(21.dp)
)
        }
    }
}

@Composable
private fun HeroRelogio(
    dados: DadosRelogio,
    pais: String,
    cidade: String,
    status: String,
    dark: Boolean
) {

    val card =
        if (dark)
            CartaoEscuro
        else
            CartaoClaro

    val texto =
        if (dark)
            TextoEscuro
        else
            TextoClaro

    val suave =
        if (dark)
            TextoSuaveEscuro
        else
            TextoSuaveClaro

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(32.dp)
            )
            .background(card)
            .border(
                width = 1.dp,
                color =
                    if (dark)
                        Color.White.copy(alpha = .09f)
                    else
                        Color.Black.copy(alpha = .07f),
                shape =
                    RoundedCornerShape(32.dp)
            )
    ) {

        Box(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(
                    Rosa.copy(alpha = .07f)
                )
        )

        Column(
            modifier = Modifier.padding(25.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.Top,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = pais,
                        color = texto,
                        fontFamily = Quicksand,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        lineHeight = 23.sp
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = cidade,
                        color = suave,
                        fontFamily = Quicksand,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier = Modifier.height(7.dp)
                    )

                    Text(
                        text = status,
                        color =
                            if (dark)
                                TextoMutedEscuro
                            else
                                TextoMutedClaro,
                        fontFamily = Quicksand,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }

                LiveBadge()
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Row(
                verticalAlignment = Alignment.Bottom
            ) {

                Text(
                    text =
                        "${dados.hora}:${dados.minuto}",
                    color = texto,
                    fontFamily = Quicksand,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 67.sp,
                    letterSpacing = (-4).sp,
                    maxLines = 1
                )

                Text(
                    text = ":${dados.segundo}",
                    color = Rosa3,
                    fontFamily = Quicksand,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 67.sp,
                    letterSpacing = (-4).sp,
                    maxLines = 1
                )
            }

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Text(
                text = dados.data,
                color = suave,
                fontFamily = Quicksand,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Spacer(
                modifier = Modifier.height(11.dp)
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (dark)
                            CartaoEscuro2
                        else
                            Color(0xFFF0F0F4)
                    )
                    .border(
                        1.dp,
                        if (dark)
                            Color.White.copy(alpha = .07f)
                        else
                            Color.Black.copy(alpha = .06f),
                        CircleShape
                    )
                    .padding(
                        horizontal = 11.dp,
                        vertical = 7.dp
                    )
            ) {

                Text(
                    text =
                        "${dados.fuso} · ${dados.utc}",
                    color = suave,
                    fontFamily = Quicksand,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            Spacer(
                modifier = Modifier.height(23.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Progresso do dia",
                    color =
                        if (dark)
                            TextoMutedEscuro
                        else
                            TextoMutedClaro,
                    fontFamily = Quicksand,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )

                Text(
                    text =
                        "${String.format(Locale.US, "%.1f", dados.progresso)}%",
                    color =
                        if (dark)
                            TextoMutedEscuro
                        else
                            TextoMutedClaro,
                    fontFamily = Quicksand,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (dark)
                            Color(0xFF202025)
                        else
                            Color(0xFFE4E4E9)
                    )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            dados.progresso / 100f
                        )
                        .height(7.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Rosa,
                                    Rosa2
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun LiveBadge() {

    val infinite =
        rememberInfiniteTransition(
            label = "live"
        )

    val scale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(750),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "liveScale"
    )

    val alpha by infinite.animateFloat(
        initialValue = 1f,
        targetValue = .55f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(750),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "liveAlpha"
    )

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                Color(0xFF101B15)
            )
            .border(
                1.dp,
                Verde.copy(alpha = .14f),
                CircleShape
            )
            .padding(
                horizontal = 9.dp,
                vertical = 6.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(7.dp)
                .scale(scale)
                .alpha(alpha)
                .clip(CircleShape)
                .background(Verde)
        )

        Spacer(
            modifier = Modifier.width(7.dp)
        )

        Text(
            text = "AO VIVO",
            color = VerdeTexto,
            fontFamily = Quicksand,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun Informacoes(
    dados: DadosRelogio,
    dark: Boolean
) {

    val itens =
        listOf(
            "ANO" to dados.ano.toString(),
            "SEMESTRE" to dados.semestre,
            "MÊS" to dados.mes,
            "SEMANA" to dados.semana.toString(),
            "BIMESTRE" to dados.bimestre,
            "QUINZENA" to dados.quinzena,
            "DIA DO ANO" to dados.diaDoAno.toString(),
            "UTC" to dados.utc
        )

    Column(
        verticalArrangement =
            Arrangement.spacedBy(9.dp)
    ) {

        for (linha in 0 until 4) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(9.dp)
            ) {

                InfoCard(
                    label = itens[linha * 2].first,
                    value = itens[linha * 2].second,
                    dark = dark,
                    modifier =
                        Modifier.weight(1f)
                )

                InfoCard(
                    label = itens[linha * 2 + 1].first,
                    value = itens[linha * 2 + 1].second,
                    dark = dark,
                    modifier =
                        Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    dark: Boolean,
    modifier: Modifier = Modifier
) {

    val card =
        if (dark)
            CartaoEscuro
        else
            CartaoClaro

    val texto =
        if (dark)
            TextoEscuro
        else
            TextoClaro

    val muted =
        if (dark)
            TextoMutedEscuro
        else
            TextoMutedClaro

    Box(
        modifier = modifier
            .height(77.dp)
            .clip(
                RoundedCornerShape(20.dp)
            )
            .background(card)
            .border(
                1.dp,
                if (dark)
                    Color.White.copy(alpha = .09f)
                else
                    Color.Black.copy(alpha = .07f),
                RoundedCornerShape(20.dp)
            )
    ) {

        Box(
            modifier = Modifier
                .size(45.dp)
                .align(Alignment.BottomEnd)
                .offset(
                    x = 25.dp,
                    y = 25.dp
                )
                .clip(CircleShape)
                .background(
                    Rosa.copy(alpha = .06f)
                )
        )

        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 12.dp
            )
        ) {

            Text(
                text = label,
                color = muted,
                fontFamily = Quicksand,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 8.sp
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = value,
                color = texto,
                fontFamily = Quicksand,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatusCard(
    texto: String,
    dark: Boolean,
    carregando: Boolean,
    aoAtualizar: () -> Unit
) {

    val card =
        if (dark)
            CartaoEscuro
        else
            CartaoClaro

    val textoPrincipal =
        if (dark)
            TextoEscuro
        else
            TextoClaro

    val suave =
        if (dark)
            TextoSuaveEscuro
        else
            TextoSuaveClaro

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(23.dp)
            )
            .background(card)
            .border(
                1.dp,
                if (dark)
                    Color.White.copy(alpha = .09f)
                else
                    Color.Black.copy(alpha = .07f),
                RoundedCornerShape(23.dp)
            )
            .padding(18.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text = "Sua localização",
                color = textoPrincipal,
                fontFamily = Quicksand,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Verde)
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = "ATIVA",
                    color = VerdeTexto,
                    fontFamily = Quicksand,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Text(
            text = texto,
            color = suave,
            fontFamily = Quicksand,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )

        Spacer(
            modifier = Modifier.height(13.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Rosa,
                            Rosa2
                        )
                    )
                )
                .clickable {
                    aoAtualizar()
                },
            contentAlignment = Alignment.Center
        ) {

            Text(
                text =
                    if (carregando)
                        "Obtendo localização..."
                    else
                        "Atualizar localização",
                color = Color.White,
                fontFamily = Quicksand,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ToastRelogio(
    dark: Boolean,
    modifier: Modifier
) {

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (dark)
                    Color(0xFF19191D)
                else
                    Color(0xFF27272D)
            )
            .border(
                1.dp,
                Color.White.copy(alpha = .10f),
                CircleShape
            )
            .padding(
                horizontal = 15.dp,
                vertical = 11.dp
            )
    ) {

        Text(
            text = "📍 Localização atualizada",
            color = Color.White,
            fontFamily = Quicksand,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

private fun obterDadosRelogio(): DadosRelogio {

    val agora = Date()

    val zona =
        TimeZone.getDefault()

    val calendar =
        Calendar.getInstance(zona).apply {
            time = agora
        }

    val hora =
        SimpleDateFormat(
            "HH",
            Locale.getDefault()
        ).format(agora)

    val minuto =
        SimpleDateFormat(
            "mm",
            Locale.getDefault()
        ).format(agora)

    val segundo =
        SimpleDateFormat(
            "ss",
            Locale.getDefault()
        ).format(agora)

    var data =
        SimpleDateFormat(
            "EEEE, dd 'de' MMMM 'de' yyyy",
            Locale("pt", "BR")
        ).format(agora)

    data =
        data.replaceFirstChar {
            it.uppercase()
        }

    val ano =
        calendar.get(Calendar.YEAR)

    val mesNumero =
        calendar.get(Calendar.MONTH) + 1

    val dia =
        calendar.get(Calendar.DAY_OF_MONTH)

    val mes =
        SimpleDateFormat(
            "MMMM",
            Locale("pt", "BR")
        ).format(agora)
            .replaceFirstChar {
                it.uppercase()
            }

    val semana =
        calendar.get(
            Calendar.WEEK_OF_YEAR
        )

    val semestre =
        if (mesNumero <= 6)
            "1º"
        else
            "2º"

    val bimestre =
        "${((mesNumero - 1) / 2) + 1}º"

    val quinzena =
        if (dia <= 15)
            "1ª"
        else
            "2ª"

    val diaDoAno =
        calendar.get(
            Calendar.DAY_OF_YEAR
        )

    val offsetMillis =
        zona.getOffset(agora.time)

    val sinal =
        if (offsetMillis >= 0)
            "+"
        else
            "-"

    val totalMinutos =
        kotlin.math.abs(
            TimeUnit.MILLISECONDS
                .toMinutes(
                    offsetMillis.toLong()
                )
        )

    val horasUtc =
        totalMinutos / 60

    val minutosUtc =
        totalMinutos % 60

    val utc =
        if (minutosUtc == 0L) {
            "UTC${sinal}${horasUtc}"
        } else {
            "UTC${sinal}${horasUtc}:${
                String.format(
                    Locale.US,
                    "%02d",
                    minutosUtc
                )
            }"
        }

    val fuso =
        zona.id

    val segundosDoDia =
        calendar.get(Calendar.HOUR_OF_DAY) * 3600 +
            calendar.get(Calendar.MINUTE) * 60 +
            calendar.get(Calendar.SECOND)

    val progresso =
        segundosDoDia / 86400f

    return DadosRelogio(
        hora = hora,
        minuto = minuto,
        segundo = segundo,
        data = data,
        fuso = fuso,
        ano = ano,
        semestre = semestre,
        mes = mes,
        semana = semana,
        bimestre = bimestre,
        quinzena = quinzena,
        diaDoAno = diaDoAno,
        utc = utc,
        progresso = progresso
    )
}

@SuppressLint("MissingPermission")
private fun obterLocalizacao(
    context: android.content.Context,
    fusedLocationClient:
        com.google.android.gms.location.FusedLocationProviderClient,
    onLoading: () -> Unit,
    onResult: (String, String) -> Unit,
    onError: () -> Unit
) {

    onLoading()

    fusedLocationClient
        .getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
        .addOnSuccessListener { location ->

            if (location == null) {

                onError()
                return@addOnSuccessListener
            }

            try {

                val geocoder =
                    Geocoder(
                        context,
                        Locale("pt", "BR")
                    )

                if (
                    android.os.Build.VERSION.SDK_INT >=
                    android.os.Build.VERSION_CODES.TIRAMISU
                ) {

                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ) { enderecos ->

                        val endereco =
                            enderecos.firstOrNull()

                        val pais =
                            endereco?.countryName
                                ?: "País não identificado"

                        val cidade =
                            endereco?.locality
                                ?: endereco?.subAdminArea
                                ?: endereco?.adminArea
                                ?: "Localização atual"

                        onResult(
                            pais,
                            cidade
                        )
                    }

                } else {

                    @Suppress("DEPRECATION")
                    val enderecos =
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )

                    val endereco =
                        enderecos?.firstOrNull()

                    val pais =
                        endereco?.countryName
                            ?: "País não identificado"

                    val cidade =
                        endereco?.locality
                            ?: endereco?.subAdminArea
                            ?: endereco?.adminArea
                            ?: "Localização atual"

                    onResult(
                        pais,
                        cidade
                    )
                }

            } catch (
                exception: Exception
            ) {

                onError()
            }
        }
        .addOnFailureListener {

            onError()
        }
}