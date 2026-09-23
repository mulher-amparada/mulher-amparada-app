package com.mulheres

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.webkit.WebView

class StableWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {
        setBackgroundColor(Color.BLACK)

        alpha = 1f
        visibility = VISIBLE

        overScrollMode =
            OVER_SCROLL_NEVER

        isVerticalScrollBarEnabled =
            false

        isHorizontalScrollBarEnabled =
            false

        scrollBarStyle =
            SCROLLBARS_INSIDE_OVERLAY

        isFocusable = true
        isFocusableInTouchMode = true

        setLayerType(
            LAYER_TYPE_HARDWARE,
            null
        )
    }
}