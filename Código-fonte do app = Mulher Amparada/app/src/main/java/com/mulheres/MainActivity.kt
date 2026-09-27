package com.mulheres

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.webkit.WebChromeClient
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat


class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView


    override fun onCreate(
    savedInstanceState: Bundle?
) {
    super.onCreate(savedInstanceState)

    // =====================================================
    // SEGURANÇA DA JANELA
    // =====================================================

    window.addFlags(
        WindowManager.LayoutParams.FLAG_SECURE
    )

    // =====================================================
    // CONFIGURAÇÃO DA JANELA
    // =====================================================

    WindowCompat.setDecorFitsSystemWindows(
        window,
        true
    )

    window.statusBarColor =
        Color.TRANSPARENT

    window.navigationBarColor =
        Color.TRANSPARENT

    // =====================================================
    // LAYOUT
    // =====================================================

    setContentView(
        R.layout.activity_main
    )

    webView =
        findViewById(
            R.id.webview
        )

    // =====================================================
    // CONFIGURAÇÃO DA WEBVIEW
    // =====================================================

    configurarWebView()

    // =====================================================
    // BARRAS DO SISTEMA
    // =====================================================

    ViewCompat.setOnApplyWindowInsetsListener(
    webView
) { view, insets ->

    val barras =
        insets.getInsets(
            WindowInsetsCompat.Type.systemBars()
        )

    view.setPadding(
        0,
        barras.top,
        0,
        barras.bottom
    )

    insets
}

    // =====================================================
    // PÁGINA INICIAL
    // =====================================================

    webView.loadUrl(
        "https://www.google.com"
    )

    // =====================================================
    // ON BACK PRESSED
    // =====================================================

    onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {

                val urlAtual =
                    webView.url

                if (
                    urlAtual != null &&
                    urlAtual.contains("google.com")
                ) {

                    webView.clearHistory()

                    finish()

                } else {

                    if (
                        webView.canGoBack()
                    ) {

                        webView.goBack()

                    } else {

                        isEnabled = false

                        onBackPressedDispatcher
                            .onBackPressed()
                    }
                }
            }
        }
    )
}


    // =========================================================
    // CONFIGURAÇÃO DA WEBVIEW
    // =========================================================
private fun configurarWebView() {

    webView.setBackgroundColor(
        Color.TRANSPARENT
    )

    webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->

    val request = android.app.DownloadManager.Request(
        Uri.parse(url)
    )

    request.setMimeType(mimeType)

    request.addRequestHeader(
        "User-Agent",
        userAgent
    )

    request.setDescription(
        "Baixando arquivo..."
    )

    request.setTitle(
        android.webkit.URLUtil.guessFileName(
            url,
            contentDisposition,
            mimeType
        )
    )

    request.setNotificationVisibility(
        android.app.DownloadManager
            .Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
    )

    request.setDestinationInExternalPublicDir(
        android.os.Environment.DIRECTORY_DOWNLOADS,
        android.webkit.URLUtil.guessFileName(
            url,
            contentDisposition,
            mimeType
        )
    )

    val downloadManager =
        getSystemService(
            android.content.Context.DOWNLOAD_SERVICE
        ) as android.app.DownloadManager

    downloadManager.enqueue(request)

    Toast.makeText(
        this,
        "Download iniciado",
        Toast.LENGTH_SHORT
    ).show()
}
    



        val settings =
            webView.settings

        settings.cacheMode =
            android.webkit.WebSettings.LOAD_DEFAULT

        settings.loadsImagesAutomatically =
            true

        settings.blockNetworkImage =
            false

        settings.databaseEnabled =
            true

        settings.displayZoomControls =
            false

        settings.builtInZoomControls =
            false

        settings.setSupportZoom(
            false
        )

        settings.textZoom =
            100

        settings.defaultTextEncodingName =
            "UTF-8"

        settings.mixedContentMode =
            android.webkit.WebSettings
                .MIXED_CONTENT_NEVER_ALLOW

        webView.overScrollMode =
            View.OVER_SCROLL_NEVER

        webView.isVerticalScrollBarEnabled =
            false

        webView.isFocusable =
            true

        webView.isFocusableInTouchMode =
            true

        webView.setOnFocusChangeListener {
                _, _ ->
        }

        webView.isHorizontalScrollBarEnabled =
            false

        webView.scrollBarStyle =
            View.SCROLLBARS_INSIDE_OVERLAY

        settings.javaScriptEnabled =
            true

        settings.mediaPlaybackRequiresUserGesture =
            false

        settings.domStorageEnabled =
            true

        settings.setGeolocationEnabled(
            true
        )

        settings.allowFileAccess =
            true

        settings.allowContentAccess =
            false

        settings.allowFileAccessFromFileURLs =
            false

        settings.allowUniversalAccessFromFileURLs =
            false

        settings.javaScriptCanOpenWindowsAutomatically =
            false

        settings.setSupportMultipleWindows(
            false
        )


        // =====================================================
        // WEB CHROME CLIENT
        // =====================================================

        webView.webChromeClient =
            object : WebChromeClient() {

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback:
                    GeolocationPermissions.Callback?
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
                        url.startsWith(
                            "https://wa.me"
                        )
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
    // LIMPEZA
    // =========================================================

    override fun onDestroy() {

        webView.stopLoading()

        webView.loadUrl(
            "about:blank"
        )

        webView.clearHistory()

        webView.removeAllViews()

        webView.destroy()

        super.onDestroy()
    }
}