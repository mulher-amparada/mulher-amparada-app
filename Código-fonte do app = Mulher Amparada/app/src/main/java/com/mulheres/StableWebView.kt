package com.mulheres

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView

class StableWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {
        // Renderização estável
        setLayerType(
            View.LAYER_TYPE_HARDWARE,
            null
        )

        // Fundo sólido para evitar áreas transparentes
        setBackgroundColor(Color.BLACK)

        alpha = 1f
        visibility = View.VISIBLE

        // Rolagem
        overScrollMode = View.OVER_SCROLL_NEVER
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        // Foco e interação
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        isLongClickable = true

        // Evita que o WebView tente desenhar fora dos próprios limites
        clipToOutline = true

        // Configurações de renderização
        settings.apply {
            cacheMode = WebSettings.LOAD_DEFAULT

            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false

            domStorageEnabled = true
            databaseEnabled = true

            loadsImagesAutomatically = true
            blockNetworkImage = false

            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = false

            mediaPlaybackRequiresUserGesture = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }
        }

        // Mantém o conteúdo dentro da área visível
        setPadding(0, 0, 0, 0)

        // Não exibe a barra de rolagem durante a movimentação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isForceDarkAllowed = false
        }

        // Remove efeitos de borda ao alcançar o limite
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = 0f
            translationZ = 0f
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        alpha = 1f
        visibility = View.VISIBLE
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)

        if (hasWindowFocus) {
            alpha = 1f
        }
    }
}