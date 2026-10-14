package com.mulheres

import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.webkit.ValueCallback
import android.content.res.Configuration
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
import androidx.activity.enableEdgeToEdge


class MainActivity : AppCompatActivity() {

private val handler = Handler(Looper.getMainLooper())
private var toqueLongoExecutado = false

private val longPressRunnable = Runnable {
    toqueLongoExecutado = true
    mostrarDialogoModo()
}

private fun configurarToqueLongo() {
    webView.setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                toqueLongoExecutado = false
                handler.postDelayed(longPressRunnable, 5000)
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
            }
        }
        false
    }
}

private fun mostrarDialogoModo() {
    val opcoes = arrayOf("📱 Mobile", "🖥️ Desktop")

    AlertDialog.Builder(this)
        .setTitle("Modo de visualização")
        .setMessage("Escolha como deseja visualizar a página.")
        .setItems(opcoes) { _, which ->
            val settings = webView.settings

            if (which == 1) {
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.userAgentString =
                    WebSettings.getDefaultUserAgent(this)
                        .replace(" Mobile", "")
                        .replace(" Android", "")
            } else {
                settings.userAgentString =
                    WebSettings.getDefaultUserAgent(this)
                settings.useWideViewPort = false
                settings.loadWithOverviewMode = false
            }

            webView.reload()
        }
        .setNegativeButton("Cancelar", null)
        .show()
}

private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

private val FILE_CHOOSER_REQUEST_CODE = 1001

    private lateinit var webView: WebView

private fun Int.dp(): Int =
    (this * resources.displayMetrics.density).toInt()
    
    override fun onCreate(
    savedInstanceState: Bundle?
) {
        enableEdgeToEdge()
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

if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
    window.isNavigationBarContrastEnforced = false
}

    // =====================================================
    // LAYOUT
    // =====================================================

    setContentView(
        R.layout.activity_main
    )
    
    val paisagem =
    resources.configuration.orientation ==
        Configuration.ORIENTATION_LANDSCAPE

    webView =
        findViewById(
            R.id.webview
        )

    // =====================================================
    // CONFIGURAÇÃO DA WEBVIEW
    // =====================================================

    configurarWebView()

configurarToqueLongo()

    // =====================================================
    // BARRAS DO SISTEMA
    // =====================================================

    ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->

    val barras = insets.getInsets(
        WindowInsetsCompat.Type.systemBars()
    )

    val paisagem =
        resources.configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE

    val params =
        view.layoutParams as ViewGroup.MarginLayoutParams

    if (paisagem) {
        params.setMargins(
            64.dp() + barras.left,
            28.dp() + barras.top,
            32.dp() + barras.right,
            32.dp() + barras.bottom
        )
 } else {
    params.setMargins(
        8.dp(),
        barras.top,
        8.dp(),
        barras.bottom
    )
}
    view.layoutParams = params

    insets
}

ViewCompat.requestApplyInsets(webView)

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
            true

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

override fun onJsAlert(
    view: WebView?,
    url: String?,
    message: String?,
    result: android.webkit.JsResult?
): Boolean {
    AlertDialog.Builder(this@MainActivity)
        .setTitle("Aviso")
        .setMessage(message.orEmpty())
        .setPositiveButton("OK") { _, _ ->
            result?.confirm()
        }
        .setOnCancelListener {
            result?.cancel()
        }
        .show()

    return true
}

override fun onJsConfirm(
    view: WebView?,
    url: String?,
    message: String?,
    result: android.webkit.JsResult?
): Boolean {
    AlertDialog.Builder(this@MainActivity)
        .setTitle("Confirmação")
        .setMessage(message.orEmpty())
        .setPositiveButton("Sim") { _, _ ->
            result?.confirm()
        }
        .setNegativeButton("Não") { _, _ ->
            result?.cancel()
        }
        .setOnCancelListener {
            result?.cancel()
        }
        .show()

    return true
}

override fun onJsPrompt(
    view: WebView?,
    url: String?,
    message: String?,
    defaultValue: String?,
    result: android.webkit.JsPromptResult?
): Boolean {
    val campo = android.widget.EditText(this@MainActivity)
    campo.setText(defaultValue.orEmpty())

    AlertDialog.Builder(this@MainActivity)
        .setTitle("Entrada de dados")
        .setMessage(message.orEmpty())
        .setView(campo)
        .setPositiveButton("OK") { _, _ ->
            result?.confirm(campo.text.toString())
        }
        .setNegativeButton("Cancelar") { _, _ ->
            result?.cancel()
        }
        .setOnCancelListener {
            result?.cancel()
        }
        .show()

    return true
}

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
                
                override fun onShowFileChooser(
    webView: WebView?,
    filePathCallback: ValueCallback<Array<Uri>>?,
    fileChooserParams: WebChromeClient.FileChooserParams?
): Boolean {

    fileChooserCallback?.onReceiveValue(null)
    fileChooserCallback = filePathCallback

    return try {
        val intent = fileChooserParams?.createIntent()
            ?: throw IllegalStateException("Seletor indisponível")

        startActivityForResult(
            intent,
            FILE_CHOOSER_REQUEST_CODE
        )

        true
    } catch (e: Exception) {
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = null
        false
    }
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

@Deprecated("Usado para receber o resultado do seletor de arquivos")
override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
        val result = if (resultCode == RESULT_OK) {
            WebChromeClient.FileChooserParams.parseResult(
                resultCode,
                data
            )
        } else {
            null
        }

        fileChooserCallback?.onReceiveValue(result)
        fileChooserCallback = null
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