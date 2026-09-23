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

        // Hardware
        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Rolagem
        overScrollMode = View.OVER_SCROLL_NEVER
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        isScrollbarFadingEnabled = false

        setVerticalFadingEdgeEnabled(false)
        setHorizontalFadingEdgeEnabled(false)

        // Nested Scrolling
        isNestedScrollingEnabled = true

        // Evita efeitos de elevação
        elevation = 0f
        translationZ = 0f

        setPadding(0, 0, 0, 0)

        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun hasNestedScrollingParent(): Boolean {
        return super.hasNestedScrollingParent()
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return super.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        super.stopNestedScroll()
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return super.dispatchNestedPreScroll(
            dx,
            dy,
            consumed,
            offsetInWindow
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return super.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow
        )
    }
}