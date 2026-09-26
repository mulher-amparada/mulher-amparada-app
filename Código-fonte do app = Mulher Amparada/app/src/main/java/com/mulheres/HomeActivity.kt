package com.mulheres

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class HomeActivity : AppCompatActivity() {

    private lateinit var cripto: Cripto
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var webView: WebView

    private var emergenciaVisivel by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            true
        )

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        val controller =
            WindowInsetsControllerCompat(
                window,
                window.decorView
            )

        val isDark =
            (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark

        setContentView(
            R.layout.activity_main
        )

        webView =
            findViewById(
                R.id.webview
            )

        locationClient =
            LocationServices
                .getFusedLocationProviderClient(
                    this
                )

        cripto =
            Cripto(this)

        configurarWebView()

        ViewCompat.setOnApplyWindowInsetsListener(
            webView
        ) { view, insets ->

            val barras =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            val params =
                view.layoutParams as ViewGroup.MarginLayoutParams

            params.topMargin =
                barras.top

            params.bottomMargin =
                barras.bottom

            view.layoutParams =
                params

            insets
        }

        // =====================================================
        // ABRE DIRETAMENTE A CARTEIRA
        // =====================================================

        webView.loadUrl(
            "file:///android_asset/${obterPastaTema()}/carteira.html"
        )

        // =====================================================
        // BOTÃO VOLTAR
        // =====================================================

        onBackPressedDispatcher.addCallback(
            this,
            object : androidx.activity.OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (webView.canGoBack()) {

                        webView.goBack()

                    } else {

                        isEnabled = false

                        onBackPressedDispatcher
                            .onBackPressed()
                    }
                }
            }
        )
    }

    // =========================================================
    // TEMA / PASTA
    // =========================================================

    private fun obterPastaTema(): String {

        return if (
            (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        ) {
            "user1"
        } else {
            "user2"
        }
    }

    // =========================================================
    // WEBVIEW
    // =========================================================

    private fun configurarWebView() {

        webView.setBackgroundColor(
            Color.TRANSPARENT
        )





        val settings =
            webView.settings

        settings.cacheMode =
            android.webkit.WebSettings.LOAD_DEFAULT

        settings.loadsImagesAutomatically = true
        settings.blockNetworkImage = false
        settings.databaseEnabled = true

        settings.displayZoomControls = false
        settings.builtInZoomControls = false
        settings.setSupportZoom(false)

        settings.textZoom = 100
        settings.defaultTextEncodingName = "UTF-8"

        settings.mixedContentMode =
            android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW

        settings.javaScriptEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.domStorageEnabled = true

        settings.setGeolocationEnabled(true)

        settings.allowFileAccess = true
        settings.allowContentAccess = false

        settings.allowFileAccessFromFileURLs = false
        settings.allowUniversalAccessFromFileURLs = false

        settings.javaScriptCanOpenWindowsAutomatically = false
        settings.setSupportMultipleWindows(false)

        webView.overScrollMode =
            View.OVER_SCROLL_NEVER

        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        webView.isFocusable = true
        webView.isFocusableInTouchMode = true

        webView.scrollBarStyle =
            View.SCROLLBARS_INSIDE_OVERLAY

        // =====================================================
        // WEB CHROME CLIENT
        // =====================================================

        webView.webChromeClient =
            object : WebChromeClient() {

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                ) {

                    callback?.invoke(
                        origin,
                        true,
                        false
                    )
                }

                override fun onPermissionRequest(
                    request: PermissionRequest
                ) {

                    runOnUiThread {

                        val resources =
                            request.resources

                        if (
                            resources.contains(
                                PermissionRequest
                                    .RESOURCE_AUDIO_CAPTURE
                            )
                        ) {

                            request.grant(
                                arrayOf(
                                    PermissionRequest
                                        .RESOURCE_AUDIO_CAPTURE
                                )
                            )

                        } else {

                            request.deny()
                        }
                    }
                }
            }

        // =====================================================
        // WEBVIEW CLIENT
        // =====================================================

        webView.webViewClient =
            object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    val url =
                        request?.url?.toString()
                            ?: return false

                    if (
                        url.startsWith("tel:")
                    ) {

                        startActivity(
                            Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse(url)
                            )
                        )

                        return true
                    }

                    if (
                        url.startsWith("https://wa.me")
                    ) {

                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url)
                            )
                        )

                        return true
                    }

                    return false
                }
            }
    }

    // =========================================================
    // LOCALIZAÇÃO
    // =========================================================

    fun pegarLocalizacao() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(
                this,
                "Permissão de localização não concedida",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        locationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    val lat =
                        location.latitude

                    val lng =
                        location.longitude

                    val js =
                        "receberLocalizacao($lat,$lng)"

                    webView.evaluateJavascript(
                        js,
                        null
                    )

                } else {

                    Toast.makeText(
                        this,
                        "Não foi possível obter a localização.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Erro ao obter localização.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }



    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()

        super.onDestroy()
    }
}