package com.mulheres

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon


private val Quicksand =
    FontFamily(
        Font(R.font.quicksand, FontWeight.Normal),
        Font(R.font.quicksand, FontWeight.Medium),
        Font(R.font.quicksand, FontWeight.SemiBold),
        Font(R.font.quicksand, FontWeight.Bold),
        Font(R.font.quicksand, FontWeight.ExtraBold)
    )


private val FundoEscuro =
    Color(0xFF050507)

private val FundoClaro =
    Color(0xFFF7F7FA)

private val CartaoEscuro =
    Color(0xD90A0A0D)

private val CartaoClaro =
    Color(0xFFFDFDFE)

private val TextoEscuro =
    Color(0xFFF7F7FA)

private val TextoClaro =
    Color(0xFF17171B)

private val TextoSuaveEscuro =
    Color(0xFF9B9BA5)

private val TextoSuaveClaro =
    Color(0xFF686873)

private val Rosa =
    Color(0xFFFF8FC7)

private val Azul =
    Color(0xFF6EB5FF)

private val Verde =
    Color(0xFF7DFFB2)


private val CentroPadrao =
    GeoPoint(
        -23.5505,
        -46.6333
    )

private const val ZOOM_PADRAO =
    12.0


class MapaActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        Configuration
            .getInstance()
            .load(
                this,
                getSharedPreferences(
                    "osmdroid",
                    MODE_PRIVATE
                )
            )

        Configuration
            .getInstance()
            .userAgentValue =
            packageName

        setContent {

            CompositionLocalProvider(
                LocalOverscrollFactory provides null
            ) {

                MapaApp()
            }
        }
    }
}


@Composable
private fun MapaApp() {

    val dark =
        isSystemInDarkTheme()

    val fundo =
        if (dark)
            FundoEscuro
        else
            FundoClaro

    CompositionLocalProvider(

        androidx.compose.material3.LocalTextStyle provides
            MaterialTheme.typography.bodyMedium.copy(
                fontFamily = Quicksand
            )

    ) {

        Surface(
            modifier =
                Modifier.fillMaxSize(),

            color =
                fundo

        ) {

            MapaTela(
                dark = dark
            )
        }
    }
}


@Composable
private fun MapaTela(
    dark: Boolean
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    var mapView by remember {
        mutableStateOf<MapView?>(null)
    }

    var toast by remember {
        mutableStateOf<String?>(null)
    }

    var userMarker by remember {
        mutableStateOf<Marker?>(null)
    }

    var userAccuracy by remember {
        mutableStateOf<Polygon?>(null)
    }

    val locationClient =
        remember(context) {

            LocationServices
                .getFusedLocationProviderClient(
                    context
                )
        }


    val permissionLauncher =
        rememberLauncherForActivityResult(

            ActivityResultContracts
                .RequestMultiplePermissions()

        ) { permissions ->

            val granted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||

                    permissions[
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ] == true


            if (granted) {

                mapView?.let { map ->

                    localizarUsuario(

                        context = context,

                        map = map,

                        locationClient =
                            locationClient,

                        onMarkerChanged = {
                            marker ->
                            userMarker =
                                marker
                        },

                        onAccuracyChanged = {
                            accuracy ->
                            userAccuracy =
                                accuracy
                        },

                        onToast = {
                            mensagem ->
                            toast =
                                mensagem
                        }
                    )
                }

            } else {

                toast =
                    "Permissão de localização negada."
            }
        }


    fun localizar() {

        val permissaoPrecisa =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val permissaoAproximada =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        if (
            permissaoPrecisa ||
            permissaoAproximada
        ) {

            mapView?.let { map ->

                localizarUsuario(

                    context = context,

                    map = map,

                    locationClient =
                        locationClient,

                    onMarkerChanged = {
                        marker ->
                        userMarker =
                            marker
                    },

                    onAccuracyChanged = {
                        accuracy ->
                        userAccuracy =
                            accuracy
                    },

                    onToast = {
                        mensagem ->
                        toast =
                            mensagem
                    }
                )
            }

        } else {

            permissionLauncher.launch(

                arrayOf(

                    Manifest.permission
                        .ACCESS_FINE_LOCATION,

                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    LaunchedEffect(toast) {

        if (toast != null) {

            delay(2500)

            toast = null
        }
    }


    Box(
    modifier =
        Modifier
            .fillMaxSize()
            .background(
                if (dark)
                    FundoEscuro
                else
                    FundoClaro
            )
            .windowInsetsPadding(
                WindowInsets.systemBars
            )
) {


        AndroidView(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 92.dp,
                        bottom = 22.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            22.dp
                        )
                    )
                    .border(
                        1.dp,

                        if (dark)
                            Color.White.copy(
                                alpha = .07f
                            )
                        else
                            Color.Black.copy(
                                alpha = .07f
                            ),

                        RoundedCornerShape(
                            22.dp
                        )
                    ),

            factory = { ctx ->

                MapView(ctx).apply {

                    mapView =
                        this


                    setTileSource(
                        TileSourceFactory.MAPNIK
                    )


                    setMultiTouchControls(
                        true
                    )


                    minZoomLevel =
                        3.0

                    maxZoomLevel =
                        19.0


                    controller.setZoom(
                        ZOOM_PADRAO
                    )


                    controller.setCenter(
                        CentroPadrao
                    )


                    isTilesScaledToDpi =
                        true


                    setBuiltInZoomControls(
                        false
                    )


                    adicionarPontos(
                        this
                    )
                }
            },

            update = { view ->

                mapView =
                    view
            }
        )


        CabecalhoMapa(

            dark = dark,

            aoLocalizar = {
                localizar()
            }
        )


        ControlesMapa(

            dark = dark,

            aoZoomIn = {

                mapView
                    ?.controller
                    ?.zoomIn()
            },

            aoZoomOut = {

                mapView
                    ?.controller
                    ?.zoomOut()
            },

            aoLocalizar = {
                localizar()
            }
        )


        toast?.let { mensagem ->

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
            ) {

                ToastMapa(

                    mensagem =
                        mensagem,

                    dark =
                        dark,

                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomStart
                            )
                            .padding(
                                start = 22.dp,
                                bottom = 22.dp
                            )
                )
            }
        }
    }


    DisposableEffect(Unit) {

        onDispose {

            mapView?.onPause()

            mapView?.onDetach()
        }
    }
}


@Composable
private fun CabecalhoMapa(

    dark: Boolean,

    aoLocalizar: () -> Unit
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


    Row(

        modifier =
            Modifier
                .fillMaxWidth()

                .padding(
                    start = 12.dp,
                    top = 10.dp,
                    end = 12.dp,
                    bottom = 0.dp
                )

                .clip(
                    RoundedCornerShape(
                        17.dp
                    )
                )

                .background(
                    card
                )

                .border(
                    1.dp,

                    if (dark)
                        Color.White.copy(
                            alpha = .10f
                        )
                    else
                        Color.Black.copy(
                            alpha = .08f
                        ),

                    RoundedCornerShape(
                        17.dp
                    )
                )

                .padding(
                    start = 13.dp,
                    end = 8.dp,
                    top = 7.dp,
                    bottom = 7.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically

    ) {


        Box(

            modifier =
                Modifier
                    .size(34.dp)
                    .clip(
                        RoundedCornerShape(
                            11.dp
                        )
                    )
                    .background(
                        Rosa.copy(
                            alpha = .10f
                        )
                    )
                    .border(
                        1.dp,

                        Rosa.copy(
                            alpha = .16f
                        ),

                        RoundedCornerShape(
                            11.dp
                        )
                    ),

            contentAlignment =
                Alignment.Center

        ) {

            Icon(

                painter =
                    painterResource(
                        R.drawable.ic_location
                    ),

                contentDescription =
                    null,

                tint =
                    Rosa,

                modifier =
                    Modifier.size(
                        21.dp
                    )
            )
        }


        Spacer(
            modifier =
                Modifier.width(
                    9.dp
                )
        )


        Column(

            modifier =
                Modifier.weight(
                    1f
                )

        ) {

            Text(

                text =
                    "Mapa",

                color =
                    texto,

                fontFamily =
                    Quicksand,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    16.sp
            )


            Row(

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Box(

                    modifier =
                        Modifier
                            .size(6.dp)
                            .clip(
                                CircleShape
                            )
                            .background(
                                Verde
                            )
                )


                Spacer(

                    modifier =
                        Modifier.width(
                            5.dp
                        )
                )


                Text(

                    text =
                        "Mapa online",

                    color =
                        suave,

                    fontFamily =
                        Quicksand,

                    fontWeight =
                        FontWeight.Medium,

                    fontSize =
                        10.sp
                )
            }
        }


        BotaoMapa(

            dark =
                dark,

            icone =
                R.drawable.ic_map,

            descricao =
                "Minha localização",

            aoClicar =
                aoLocalizar
        )
    }
}


@Composable
private fun ControlesMapa(

    dark: Boolean,

    aoZoomIn: () -> Unit,

    aoZoomOut: () -> Unit,

    aoLocalizar: () -> Unit

) {

    Box(

        modifier =
            Modifier.fillMaxSize()

    ) {

        Column(

            modifier =
                Modifier
                    .align(
                        Alignment.BottomEnd
                    )
.padding(
    end = 14.dp,
    bottom = 24.dp
),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally

        ) {


            BotaoMapa(

                dark =
                    dark,

                icone =
                    R.drawable.ic_add,

                descricao =
                    "Aumentar zoom",

                aoClicar =
                    aoZoomIn
            )


            BotaoMapa(

                dark =
                    dark,

                icone =
                    R.drawable.ic_remove,

                descricao =
                    "Diminuir zoom",

                aoClicar =
                    aoZoomOut
            )


            BotaoMapa(

                dark =
                    dark,

                icone =
                    R.drawable.ic_my_location,

                descricao =
                    "Encontrar localização",

                aoClicar =
                    aoLocalizar
            )
        }
    }
}


@Composable
private fun BotaoMapa(

    dark: Boolean,

    icone: Int,

    descricao: String,

    aoClicar: () -> Unit

) {

    Box(

        modifier =
            Modifier
                .size(44.dp)

                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )

                .background(

                    if (dark)

                        Color(
                            0xD60A0A0D
                        )

                    else

                        Color.White.copy(
                            alpha = .94f
                        )
                )

                .border(

                    1.dp,

                    if (dark)

                        Color.White.copy(
                            alpha = .10f
                        )

                    else

                        Color.Black.copy(
                            alpha = .08f
                        ),

                    RoundedCornerShape(
                        14.dp
                    )
                )

                .clickable {
                    aoClicar()
                },

        contentAlignment =
            Alignment.Center

    ) {

        Icon(

            painter =
                painterResource(
                    icone
                ),

            contentDescription =
                descricao,

            tint =

                if (dark)

                    Color.White

                else

                    TextoClaro,

            modifier =
                Modifier.size(
                    22.dp
                )
        )
    }
}


@Composable
private fun ToastMapa(

    mensagem: String,

    dark: Boolean,

    modifier: Modifier

) {

    Box(

        modifier =
            modifier

                .clip(
                    RoundedCornerShape(
                        13.dp
                    )
                )

                .background(

                    if (dark)

                        Color(
                            0xE60C0C0F
                        )

                    else

                        Color(
                            0xE62A2A2F
                        )
                )

                .border(

                    1.dp,

                    Color.White.copy(
                        alpha = .10f
                    ),

                    RoundedCornerShape(
                        13.dp
                    )
                )

                .padding(
                    horizontal = 16.dp,
                    vertical = 11.dp
                )

    ) {

        Text(

            text =
                mensagem,

            color =
                Color.White,

            fontFamily =
                Quicksand,

            fontWeight =
                FontWeight.Medium,

            fontSize =
                12.sp
        )
    }
}


private fun adicionarPontos(
    map: MapView
) {

    val pontos =

        listOf(

            GeoPoint(
                -23.5505,
                -46.6333
            ),

            GeoPoint(
                -23.5600,
                -46.6200
            ),

            GeoPoint(
                -23.5400,
                -46.6500
            )
        )


    pontos.forEachIndexed {
        index,
        ponto ->

        val marker =
            Marker(map)


        marker.position =
            ponto


        marker.setAnchor(

            Marker.ANCHOR_CENTER,

            Marker.ANCHOR_CENTER
        )


        marker.icon =
            criarMarcadorRosa(
                map.context
            )


        marker.title =
            "Ponto ${index + 1}"


        map.overlays.add(
            marker
        )
    }


    map.invalidate()
}


private fun criarMarcadorRosa(

    context: Context

): android.graphics.drawable.Drawable {

    val drawable =
        GradientDrawable()


    drawable.shape =
        GradientDrawable.OVAL


    drawable.setColor(

        AndroidColor.rgb(
            255,
            105,
            180
        )
    )


    drawable.setStroke(

        2,

        AndroidColor.WHITE
    )


    drawable.setSize(
        18,
        18
    )


    return drawable
}


@Suppress("MissingPermission")
private fun localizarUsuario(

    context: Context,

    map: MapView,

    locationClient:
        com.google.android.gms.location
            .FusedLocationProviderClient,

    onMarkerChanged:
        (Marker) -> Unit,

    onAccuracyChanged:
        (Polygon) -> Unit,

    onToast:
        (String) -> Unit

) {

    onToast(
        "Obtendo sua localização..."
    )


    locationClient

        .getCurrentLocation(

            com.google.android.gms.location
                .Priority
                .PRIORITY_HIGH_ACCURACY,

            null
        )

        .addOnSuccessListener { location ->


            if (location == null) {

                onToast(
                    "Localização indisponível."
                )

                return@addOnSuccessListener
            }


            val ponto =
                GeoPoint(

                    location.latitude,

                    location.longitude
                )


            val marker =
                Marker(map)


            marker.position =
                ponto


            marker.setAnchor(

                Marker.ANCHOR_CENTER,

                Marker.ANCHOR_CENTER
            )


            marker.icon =
                criarMarcadorUsuario(
                    context
                )


            marker.title =
                "Minha localização"


            map.overlays.removeAll {

                it is Marker &&
                    it.title ==
                    "Minha localização"
            }


            map.overlays.add(
                marker
            )


            val raio =
                Polygon(map)


            val circulo =
                Polygon.pointsAsCircle(

                    ponto,

                    location
                        .accuracy
                        .toDouble()
                )


            raio.points =
                circulo


            raio.fillColor =

                AndroidColor.argb(

                    15,

                    110,

                    181,

                    255
                )


            raio.strokeColor =

                AndroidColor.argb(

                    90,

                    110,

                    181,

                    255
                )


            raio.strokeWidth =
                1f


            map.overlays.removeAll {

                it is Polygon
            }


            map.overlays.add(
                raio
            )


            onMarkerChanged(
                marker
            )


            onAccuracyChanged(
                raio
            )


            map.controller.animateTo(
                ponto
            )


            map.controller.setZoom(

                maxOf(

                    map.zoomLevelDouble,

                    15.0
                )
            )


            map.invalidate()


            onToast(
                "Localização encontrada."
            )

        }

        .addOnFailureListener {

            onToast(
                "Não foi possível obter sua localização."
            )
        }
}


private fun criarMarcadorUsuario(

    context: Context

): android.graphics.drawable.Drawable {

    val drawable =
        GradientDrawable()


    drawable.shape =
        GradientDrawable.OVAL


    drawable.setColor(

        AndroidColor.rgb(

            110,

            181,

            255
        )
    )


    drawable.setStroke(

        3,

        AndroidColor.WHITE
    )


    drawable.setSize(

        18,

        18
    )


    return drawable
}