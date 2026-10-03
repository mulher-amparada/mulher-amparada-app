package com.mulheres

import androidx.compose.foundation.isSystemInDarkTheme
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale
import kotlin.math.roundToInt

private val PinkDark = Color(0xFFE96D92)

private val Danger = Color(0xFFE53935)

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

class GeoActivity : ComponentActivity() {

    private lateinit var locationManager: LocationManager

    private var locationListener: LocationListener? = null

    private var myLocationOverlay: MyLocationNewOverlay? = null

    private var mapView: MapView? = null

    private var safeMarker: Marker? = null

    private var safeCircle: Polygon? = null

    private var lastLocation: Location? = null

    private var centeredOnLocation = false

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (hasLocationPermission()) {
                startLocation()
            }
        }

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

        window.statusBarColor =
            AndroidColor.TRANSPARENT

        window.navigationBarColor =
            AndroidColor.TRANSPARENT

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
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        Configuration.getInstance().load(
            this,
            getSharedPreferences(
                "osmdroid",
                MODE_PRIVATE
            )
        )

        Configuration.getInstance().userAgentValue =
            packageName

        locationManager =
            getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager

        setContent {
            GeoScreen()
        }

        if (hasLocationPermission()) {
            startLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()

        mapView?.onResume()

        if (hasLocationPermission()) {
            startLocation()
        }
    }

    override fun onPause() {
        stopLocation()
        mapView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        stopLocation()
        mapView?.onDetach()
        super.onDestroy()
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocation() {

        if (!hasLocationPermission()) {
            return
        }

        stopLocation()

        val listener =
            object : LocationListener {

                override fun onLocationChanged(
                    location: Location
                ) {
                    lastLocation = location

                    updateMapLocation(
                        location
                    )

                    checkSafeRadius(
                        location
                    )
                }
            }

        locationListener = listener

        val providers =
            locationManager.allProviders

        if (
            providers.contains(
                LocationManager.GPS_PROVIDER
            )
        ) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L,
                    5f,
                    listener
                )
            } catch (_: SecurityException) {
            }
        }

        if (
            providers.contains(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000L,
                    10f,
                    listener
                )
            } catch (_: SecurityException) {
            }
        }

        val lastGps =
            try {
                locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER
                )
            } catch (_: Exception) {
                null
            }

        val lastNetwork =
            try {
                locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER
                )
            } catch (_: Exception) {
                null
            }

        val initial =
            when {
                lastGps != null &&
                    lastNetwork != null -> {
                    if (
                        lastGps.time >=
                            lastNetwork.time
                    ) {
                        lastGps
                    } else {
                        lastNetwork
                    }
                }

                lastGps != null ->
                    lastGps

                else ->
                    lastNetwork
            }

        if (initial != null) {
            lastLocation = initial

            updateMapLocation(
                initial
            )

            checkSafeRadius(
                initial
            )
        }
    }

    private fun stopLocation() {

        val listener =
            locationListener
                ?: return

        try {
            locationManager.removeUpdates(
                listener
            )
        } catch (_: Exception) {
        }

        locationListener = null
    }

    private fun updateMapLocation(
        location: Location
    ) {

        runOnUiThread {

            val map =
                mapView
                    ?: return@runOnUiThread

            val point =
                GeoPoint(
                    location.latitude,
                    location.longitude
                )

            myLocationOverlay?.let {
                if (!map.overlays.contains(it)) {
                    map.overlays.add(it)
                }

                it.enableMyLocation()
            }

            if (!centeredOnLocation) {
                map.controller.setZoom(17.0)
                map.controller.animateTo(point)
                centeredOnLocation = true
            }

            map.invalidate()
        }
    }

    private fun checkSafeRadius(
        location: Location
    ) {

        val safe =
            loadSafeArea()
                ?: return

        val distance =
            FloatArray(1)

        Location.distanceBetween(
            location.latitude,
            location.longitude,
            safe.latitude,
            safe.longitude,
            distance
        )

        val outside =
            distance[0] > safe.radius

        runOnUiThread {

            currentGeoState?.let {
                it.outsideSafeArea = outside
                it.distance = distance[0]
            }
        }
    }

    private data class SafeArea(
        val latitude: Double,
        val longitude: Double,
        val radius: Float
    )

    private fun loadSafeArea(): SafeArea? {

        val saved =
            Cripto(this).carregar(
                "geo_area_segura"
            )

        if (saved.isBlank()) {
            return null
        }

        return try {

            val json =
                JSONObject(saved)

            SafeArea(
                latitude =
                    json.getDouble(
                        "latitude"
                    ),
                longitude =
                    json.getDouble(
                        "longitude"
                    ),
                radius =
                    json.getDouble(
                        "radius"
                    ).toFloat()
            )

        } catch (_: Exception) {
            null
        }
    }

    private fun saveSafeArea(
        latitude: Double,
        longitude: Double,
        radius: Float
    ) {

        val json =
            JSONObject().apply {

                put(
                    "latitude",
                    latitude
                )

                put(
                    "longitude",
                    longitude
                )

                put(
                    "radius",
                    radius
                )
            }

        Cripto(this).salvar(
            "geo_area_segura",
            json.toString()
        )

        drawSafeArea(
            latitude,
            longitude,
            radius
        )

        checkSafeRadius(
            lastLocation ?: return
        )
    }

    private fun drawSafeArea(
        latitude: Double,
        longitude: Double,
        radius: Float
    ) {

        val map =
            mapView
                ?: return

        safeMarker?.let {
            map.overlays.remove(it)
        }

        safeCircle?.let {
            map.overlays.remove(it)
        }

        val center =
            GeoPoint(
                latitude,
                longitude
            )

        val marker =
            Marker(map).apply {

                position =
                    center

                title =
                    "Área segura"

                setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
                )
            }

        val circle =
            Polygon(map).apply {

                points =
                    Polygon.pointsAsCircle(
                        center,
                        radius.toDouble()
                    )

                fillColor =
                    AndroidColor.argb(
                        35,
                        255,
                        142,
                        175
                    )

                strokeColor =
                    AndroidColor.argb(
                        180,
                        233,
                        109,
                        146
                    )

                strokeWidth =
                    3f
            }

        safeMarker =
            marker

        safeCircle =
            circle

        map.overlays.add(circle)
        map.overlays.add(marker)

        map.invalidate()
    }

    private var currentGeoState:
        GeoState? = null

    private class GeoState {
        var outsideSafeArea = false
        var distance = 0f
    }

    private fun openHelp() {

        try {

            startActivity(
                Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:190")
                )
            )

        } catch (_: Exception) {
        }
    }

    @Composable
    private fun GeoScreen() {

        val context =
            LocalContext.current
            
            val darkTheme = isSystemInDarkTheme()

val surfaceColor =
    if (darkTheme) Color.Black else Color.White

val contentColor =
    if (darkTheme) Color.White else Color.Black

val mutedColor =
    if (darkTheme) {
        Color(0xB3FFFFFF)
    } else {
        Color(0xB3000000)
    }

        var showRadiusDialog by remember {
            mutableStateOf(false)
        }

        var radius by remember {
            mutableFloatStateOf(
                loadSafeArea()?.radius
                    ?: 500f
            )
        }

        var outside by remember {
            mutableStateOf(false)
        }

        var distance by remember {
            mutableFloatStateOf(0f)
        }

        val state =
            remember {
                GeoState()
            }

        currentGeoState =
            state

        LaunchedEffect(Unit) {

            while (true) {

                outside =
                    state.outsideSafeArea

                distance =
                    state.distance

                delay(500)
            }
        }

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            AndroidView(
                factory = {

                    MapView(context).apply {

                        mapView = this

                        setTileSource(
                            TileSourceFactory.MAPNIK
                        )

                        setMultiTouchControls(
                            true
                        )

                        zoomController.setVisibility(
                            org.osmdroid.views
                                .CustomZoomButtonsController
                                .Visibility.NEVER
                        )

                        controller.setZoom(
                            17.0
                        )

                        val overlay =
                            MyLocationNewOverlay(
                                GpsMyLocationProvider(
                                    context
                                ),
                                this
                            )

                        overlay.enableMyLocation()

                        myLocationOverlay =
                            overlay

                        overlays.add(
                            overlay
                        )

                        loadSafeArea()?.let {
                            drawSafeArea(
                                it.latitude,
                                it.longitude,
                                it.radius
                            )
                        }
                    }
                },
                modifier =
                    Modifier.fillMaxSize()
            )

            Column(
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .navigationBarsPadding()
                        .padding(
                            horizontal = 6.dp,
                            vertical = 6.dp
                        ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                if (outside) {

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(
                                    RoundedCornerShape(
                                        20.dp
                                    )
                                )
                                .background(
                                    Surface
                                )
                                .padding(
                                    14.dp
                                ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text =
                                "Você saiu da área segura",
                            color = contentColor,
                            fontFamily = Quicksand,
                            fontWeight =
                                FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.size(5.dp)
                        )

                        Text(
                            text =
                                String.format(
                                    Locale(
                                        "pt",
                                        "BR"
                                    ),
                                    "Distância: %.0f m",
                                    distance
                                ),
                            color = mutedColor,
                            fontFamily = Quicksand,
                            fontSize = 11.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.size(8.dp)
                        )

                        Button(
                            onClick = {
                                openHelp()
                            },
                            modifier =
                                Modifier.fillMaxWidth(),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Danger
                                )
                        ) {

                            Text(
                                text =
                                    "Pedir ajuda",
                                fontFamily =
                                    Quicksand,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.size(6.dp)
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    999.dp
                                )
                            )
                            .background(
                                SurfaceColor
                            )
                            .padding(
                                horizontal = 5.dp,
                                vertical = 4.dp
                            ),
                    horizontalArrangement =
                        Arrangement.Center,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    OutlinedButton(
                        onClick = {

                            lastLocation?.let {

                                saveSafeArea(
                                    latitude =
                                        it.latitude,
                                    longitude =
                                        it.longitude,
                                    radius =
                                        radius
                                )
                            }
                        }
                    ) {

                        Text(
                            text =
                                "Atualizar",
                            fontFamily =
                                Quicksand
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(6.dp)
                    )

                    Button(
                        onClick = {
                            showRadiusDialog = true
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    PinkDark
                            )
                    ) {

                        Text(
                            text =
                                "Raio: ${
                                    radius.roundToInt()
                                } m",
                            fontFamily =
                                Quicksand,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }

            if (showRadiusDialog) {

                AlertDialog(
                    onDismissRequest = {
                        showRadiusDialog = false
                    },
                    title = {

                        Text(
                            text =
                                "Raio da área segura",
                            fontFamily =
                                Quicksand,
                            fontWeight =
                                FontWeight.Bold
                        )
                    },
                    text = {

                        Column {

                            Text(
                                text =
                                    "${radius.roundToInt()} metros",
                                fontFamily =
                                    Quicksand
                            )

                            Slider(
                                value =
                                    radius,
                                onValueChange = {
                                    radius = it
                                },
                                valueRange =
                                    50f..5000f
                            )
                        }
                    },
                    confirmButton = {

                        TextButton(
                            onClick = {

                                lastLocation?.let {

                                    saveSafeArea(
                                        it.latitude,
                                        it.longitude,
                                        radius
                                    )
                                }

                                showRadiusDialog =
                                    false
                            }
                        ) {

                            Text(
                                text =
                                    "Salvar",
                                color =
                                    PinkDark,
                                fontFamily =
                                    Quicksand,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {

                        TextButton(
                            onClick = {
                                showRadiusDialog =
                                    false
                            }
                        ) {

                            Text(
                                text =
                                    "Cancelar",
                                fontFamily =
                                    Quicksand
                            )
                        }
                    }
                )
            }
        }
    }
}