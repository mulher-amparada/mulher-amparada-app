package com.mulheres

import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView

class StableWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(
    context,
    attrs,
    defStyleAttr
) {

    private var ultimoY = 0f

    private var ultimoScroll = 0L

    private var ultimoFrame = 0L

    private var renderizacaoPendente = false

    private var anomaliaDetectada = false


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

        setPadding(
            0,
            0,
            0,
            0
        )

        elevation = 0f
        translationZ = 0f

        isFocusable = true
        isFocusableInTouchMode = true
    }


    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                ultimoY = event.y

                ultimoScroll =
                    SystemClock.uptimeMillis()

                ultimoFrame =
                    ultimoScroll

                anomaliaDetectada = false
            }


            MotionEvent.ACTION_MOVE -> {

                val agora =
                    SystemClock.uptimeMillis()

                val distancia =
                    event.y - ultimoY

                ultimoY =
                    event.y

                ultimoScroll =
                    agora

                if (distancia != 0f) {
                    solicitarRenderizacao()
                }
            }


            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                ultimoY = 0f
            }
        }

        return super.onTouchEvent(event)
    }


    private fun solicitarRenderizacao() {

        if (renderizacaoPendente) {
            return
        }

        renderizacaoPendente = true

        postOnAnimation {

            renderizacaoPendente = false

            val agora =
                SystemClock.uptimeMillis()

            val intervalo =
                if (ultimoFrame == 0L) {
                    0L
                } else {
                    agora - ultimoFrame
                }

            ultimoFrame = agora

            /*
             * A própria WebView é invalidada
             * no próximo frame.
             */
            invalidate()

            /*
             * Detecta uma possível quebra
             * na sequência de renderização.
             */
            if (intervalo > 32L) {

                anomaliaDetectada = true

                reagirAAnomalia()
            }
        }
    }


    private fun reagirAAnomalia() {

        /*
         * Aqui não existe:
         *
         * - Logcat
         * - Toast
         * - JavaScript
         * - alteração visual
         *
         * Apenas uma nova solicitação de
         * composição no próximo frame.
         */

        if (!renderizacaoPendente) {

            renderizacaoPendente = true

            postOnAnimation {

                renderizacaoPendente = false

                invalidate()
            }
        }
    }
}