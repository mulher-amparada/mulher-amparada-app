package com.mulheres

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView

class StableWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {
        setBackgroundColor(Color.BLACK)

        alpha = 1f
        visibility = View.VISIBLE

        overScrollMode = View.OVER_SCROLL_NEVER

        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        setPadding(0, 0, 0, 0)

        elevation = 0f
        translationZ = 0f

        isFocusable = true
        isFocusableInTouchMode = true

        // Manter aceleração por hardware
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }
}