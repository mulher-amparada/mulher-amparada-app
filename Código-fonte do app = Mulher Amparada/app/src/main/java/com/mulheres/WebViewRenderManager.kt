package com.mulheres

import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebSettings
import android.webkit.WebView

class WebViewRenderManager(
    private val webView: WebView
) {

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val choreographer =
        Choreographer.getInstance()

    private var destroyed = false
    private var rendererAlive = true

    private var navigationGeneration = 0L

    private var framePending = false
    private var visualStatePending = false

    private var pageVisible = false
    private var pageCommitted = false

    private var lastStableFrame = 0L

    private var pendingAction: (() -> Unit)? = null

    private var stabilizationGeneration = 0L

    private var stableFrames = 0

    private var visualCallbackGeneration = -1L

    private var cssInjected = false

    private var warmingUp = false


    /*
     * =========================================================
     * CONFIGURAÇÃO
     * =========================================================
     */

    init {
        configurarWebView()
        configurarRenderer()
        aquecerComposicao()
    }


    /*
     * =========================================================
     * FRAME PRINCIPAL
     * =========================================================
     */

    private val frameCallback =
        Choreographer.FrameCallback { frameTimeNanos ->

            framePending = false

            if (
                destroyed ||
                !rendererAlive
            ) {
                return@FrameCallback
            }

            lastStableFrame =
                frameTimeNanos

            if (pageCommitted) {
                stableFrames++
            }

            sincronizarEstadoVisual()
        }


    /*
     * =========================================================
     * FRAME DE AÇÃO
     * =========================================================
     */

    private val actionFrameCallback =
        Choreographer.FrameCallback {

            framePending = false

            if (
                destroyed ||
                !rendererAlive
            ) {
                pendingAction = null
                return@Choreographer.FrameCallback
            }

            val action =
                pendingAction

            pendingAction = null

            action?.invoke()

            solicitarFrame()
        }


    /*
     * =========================================================
     * WEBVIEW
     * =========================================================
     */

    private fun configurarWebView() {

        /*
         * TRANSPARÊNCIA REAL
         */

        webView.setBackgroundColor(
            Color.TRANSPARENT
        )

        webView.alpha =
            1f

        webView.visibility =
            View.VISIBLE

        webView.isOpaque =
            false

        webView.setWillNotDraw(
            false
        )


        /*
         * HARDWARE
         */

        webView.setLayerType(
            View.LAYER_TYPE_HARDWARE,
            null
        )


        /*
         * COMPORTAMENTO VISUAL
         */

        webView.overScrollMode =
            View.OVER_SCROLL_NEVER

        webView.isVerticalScrollBarEnabled =
            false

        webView.isHorizontalScrollBarEnabled =
            false

        webView.isScrollbarFadingEnabled =
            false

        webView.scrollBarStyle =
            View.SCROLLBARS_INSIDE_OVERLAY


        /*
         * FOCO
         */

        webView.isFocusable =
            true

        webView.isFocusableInTouchMode =
            true


        /*
         * SETTINGS
         */

        webView.settings.apply {

            javaScriptEnabled =
                true

            domStorageEnabled =
                true

            databaseEnabled =
                true

            setSupportZoom(
                false
            )

            builtInZoomControls =
                false

            displayZoomControls =
                false

            textZoom =
                100

            defaultTextEncodingName =
                "UTF-8"

            useWideViewPort =
                true

            loadWithOverviewMode =
                false

            mediaPlaybackRequiresUserGesture =
                false

            cacheMode =
                WebSettings.LOAD_DEFAULT

            allowFileAccess =
                true

            allowContentAccess =
                false

            allowFileAccessFromFileURLs =
                false

            allowUniversalAccessFromFileURLs =
                false

            mixedContentMode =
                WebSettings.MIXED_CONTENT_NEVER_ALLOW

            javaScriptCanOpenWindowsAutomatically =
                false

            setSupportMultipleWindows(
                false
            )


            /*
             * PRÉ-RASTERIZAÇÃO
             */

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M
            ) {

                offscreenPreRaster =
                    true
            }
        }
    }


    /*
     * =========================================================
     * RENDERER PRIORITY
     * =========================================================
     */

    private fun configurarRenderer() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            webView.setRendererPriorityPolicy(
                WebView.RENDERER_PRIORITY_IMPORTANT,
                false
            )
        }
    }


    /*
     * =========================================================
     * AQUECIMENTO
     * =========================================================
     *
     * O objetivo é fazer o pipeline da WebView existir antes
     * da primeira navegação real.
     *
     * Não colocamos conteúdo visível.
     * =========================================================
     */

    private fun aquecerComposicao() {

        if (
            destroyed ||
            !rendererAlive ||
            warmingUp
        ) {
            return
        }

        warmingUp =
            true

        webView.setBackgroundColor(
            Color.TRANSPARENT
        )

        webView.post {

            if (
                destroyed ||
                !rendererAlive
            ) {
                return@post
            }

            webView.requestFocusFromTouch()

            webView.post {

                if (
                    destroyed ||
                    !rendererAlive
                ) {
                    return@post
                }

                warmingUp =
                    false

                solicitarFrame()
            }
        }
    }


    /*
     * =========================================================
     * LOAD URL
     * =========================================================
     */

    fun loadUrl(
        url: String
    ) {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        iniciarNovaNavegacao()

        val generation =
            navigationGeneration

        agendarAcao {

            if (
                destroyed ||
                !rendererAlive ||
                generation != navigationGeneration
            ) {
                return@agendarAcao
            }

            webView.loadUrl(
                url
            )
        }
    }


    /*
     * =========================================================
     * LOAD DATA
     * =========================================================
     */

    fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String,
        encoding: String?,
        historyUrl: String?
    ) {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        iniciarNovaNavegacao()

        val generation =
            navigationGeneration

        agendarAcao {

            if (
                destroyed ||
                !rendererAlive ||
                generation != navigationGeneration
            ) {
                return@agendarAcao
            }

            webView.loadDataWithBaseURL(
                baseUrl,
                data,
                mimeType,
                encoding,
                historyUrl
            )
        }
    }


    /*
     * =========================================================
     * NOVA NAVEGAÇÃO
     * =========================================================
     */

    private fun iniciarNovaNavegacao() {

        navigationGeneration++

        stabilizationGeneration++

        pageVisible =
            false

        pageCommitted =
            false

        visualStatePending =
            true

        visualCallbackGeneration =
            -1L

        stableFrames =
            0

        cssInjected =
            false

        /*
         * Nunca deixamos a WebView perder a transparência.
         */

        webView.setBackgroundColor(
            Color.TRANSPARENT
        )

        webView.alpha =
            1f

        webView.visibility =
            View.VISIBLE

        /*
         * CSS temporário de estabilização.
         */

        prepararCSSAntiFlash()

        solicitarFrame()
    }


    /*
     * =========================================================
     * AGENDAR AÇÃO NO VSYNC
     * =========================================================
     */

    private fun agendarAcao(
        action: () -> Unit
    ) {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        mainHandler.post {

            if (
                destroyed ||
                !rendererAlive
            ) {
                return@post
            }

            pendingAction =
                action

            if (!framePending) {

                framePending =
                    true

                choreographer.postFrameCallback(
                    actionFrameCallback
                )
            }
        }
    }


    /*
     * =========================================================
     * PAGE STARTED
     * =========================================================
     */

    fun onPageStarted() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        pageVisible =
            false

        pageCommitted =
            false

        visualStatePending =
            true

        stableFrames =
            0

        webView.setBackgroundColor(
            Color.TRANSPARENT
        )

        webView.alpha =
            1f

        prepararCSSAntiFlash()

        solicitarFrame()
    }


    /*
     * =========================================================
     * PAGE FINISHED
     * =========================================================
     */

    fun onPageFinished() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        visualStatePending =
            true

        /*
         * O DOM já existe.
         */

        prepararCSSAntiFlash()

        /*
         * Espera alguns frames antes de considerar
         * a composição estabilizada.
         */

        estabilizarFrames()

        solicitarFrame()
    }


    /*
     * =========================================================
     * PAGE COMMIT VISIBLE
     * =========================================================
     */

    fun onPageCommitVisible() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        pageCommitted =
            true

        visualStatePending =
            true

        stableFrames =
            0

        webView.setBackgroundColor(
            Color.TRANSPARENT
        )

        /*
         * Neste ponto o Chromium já confirmou que existe
         * conteúdo visual para a navegação.
         */

        prepararCSSAntiFlash()

        solicitarFrame()
    }


    /*
     * =========================================================
     * CSS ANTI-FLASH
     * =========================================================
     *
     * IMPORTANTE:
     *
     * Não coloca cor.
     * Não coloca opacity.
     * Não faz fade.
     * Não mexe no scroll.
     *
     * Ele apenas estabiliza a composição inicial.
     * =========================================================
     */

    private fun prepararCSSAntiFlash() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        val generation =
            stabilizationGeneration

        val script = """
            (function() {

                const ID =
                    "__mulheres_render_guard";

                let style =
                    document.getElementById(ID);

                if (!style) {

                    style =
                        document.createElement("style");

                    style.id =
                        ID;

                    style.textContent = `
                        html,
                        body {
                            background-color: transparent !important;
                            background-image: none !important;
                        }

                        html {
                            min-height: 100%;
                            overscroll-behavior: none;
                            -webkit-tap-highlight-color: transparent;
                        }

                        body {
                            min-height: 100%;
                            margin: 0;
                            -webkit-tap-highlight-color: transparent;
                            overscroll-behavior: none;
                        }
                    `;

                    (
                        document.head ||
                        document.documentElement
                    ).appendChild(style);
                }

            })();
        """.trimIndent()

        webView.evaluateJavascript(
            script
        ) {

            if (
                destroyed ||
                !rendererAlive ||
                generation != stabilizationGeneration
            ) {
                return@evaluateJavascript
            }

            cssInjected =
                true
        }
    }


    /*
     * =========================================================
     * ESTABILIZAÇÃO
     * =========================================================
     */

    private fun estabilizarFrames() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        val generation =
            stabilizationGeneration

        stableFrames =
            0

        fun esperar() {

            if (
                destroyed ||
                !rendererAlive ||
                generation != stabilizationGeneration
            ) {
                return
            }

            if (
                stableFrames >= 3
            ) {

                finalizarEstabilizacao(
                    generation
                )

                return
            }

            choreographer.postFrameCallback {

                if (
                    destroyed ||
                    !rendererAlive ||
                    generation != stabilizationGeneration
                ) {
                    return@postFrameCallback
                }

                stableFrames++

                esperar()
            }
        }

        esperar()
    }


    /*
     * =========================================================
     * FINALIZAR ESTABILIZAÇÃO
     * =========================================================
     */

    private fun finalizarEstabilizacao(
        generation: Long
    ) {

        if (
            destroyed ||
            !rendererAlive ||
            generation != stabilizationGeneration
        ) {
            return
        }

        pageVisible =
            true

        visualStatePending =
            true

        /*
         * Remove apenas a folha temporária criada pelo
         * próprio manager.
         *
         * Não altera CSS do usuário.
         */

        mainHandler.postDelayed({

            if (
                destroyed ||
                !rendererAlive ||
                generation != stabilizationGeneration
            ) {
                return@postDelayed
            }

            removerCSSGuard()

        }, 48)
    }


    /*
     * =========================================================
     * REMOVER CSS TEMPORÁRIO
     * =========================================================
     */

    private fun removerCSSGuard() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        val script = """
            (function() {

                const style =
                    document.getElementById(
                        "__mulheres_render_guard"
                    );

                if (style) {
                    style.remove();
                }

            })();
        """.trimIndent()

        webView.evaluateJavascript(
            script,
            null
        )

        cssInjected =
            false
    }


    /*
     * =========================================================
     * VISUAL STATE CALLBACK
     * =========================================================
     */

    private fun sincronizarEstadoVisual() {

        if (
            destroyed ||
            !rendererAlive ||
            !visualStatePending
        ) {
            return
        }

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.M
        ) {

            visualStatePending =
                false

            return
        }

        val generation =
            navigationGeneration

        if (
            visualCallbackGeneration ==
            generation
        ) {
            return
        }

        visualCallbackGeneration =
            generation

        webView.postVisualStateCallback(
            generation,
            object : WebView.VisualStateCallback() {

                override fun onComplete(
                    requestId: Long
                ) {

                    if (
                        destroyed ||
                        !rendererAlive
                    ) {
                        return
                    }

                    if (
                        requestId !=
                        navigationGeneration
                    ) {
                        return
                    }

                    choreographer.postFrameCallback {

                        if (
                            destroyed ||
                            !rendererAlive
                        ) {
                            return@postFrameCallback
                        }

                        if (
                            requestId !=
                            navigationGeneration
                        ) {
                            return@postFrameCallback
                        }

                        /*
                         * Mais um frame após a confirmação
                         * visual do Chromium.
                         */

                        choreographer.postFrameCallback {

                            if (
                                destroyed ||
                                !rendererAlive
                            ) {
                                return@postFrameCallback
                            }

                            if (
                                requestId !=
                                navigationGeneration
                            ) {
                                return@postFrameCallback
                            }

                            visualStatePending =
                                false

                            pageVisible =
                                true

                            lastStableFrame =
                                System.nanoTime()
                        }
                    }
                }
            }
        )
    }


    /*
     * =========================================================
     * SOLICITAR FRAME
     * =========================================================
     */

    private fun solicitarFrame() {

        if (
            destroyed ||
            !rendererAlive ||
            framePending
        ) {
            return
        }

        framePending =
            true

        choreographer.postFrameCallback(
            frameCallback
        )
    }


    /*
     * =========================================================
     * RESUME
     * =========================================================
     */

    fun onResume() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        webView.setBackgroundColor(
            Color.TRANSPARENT
        )

        webView.alpha =
            1f

        webView.visibility =
            View.VISIBLE

        webView.isOpaque =
            false

        configurarRenderer()

        /*
         * Reaquece a composição.
         */

        webView.post {

            if (
                destroyed ||
                !rendererAlive
            ) {
                return@post
            }

            webView.requestFocusFromTouch()

            solicitarFrame()

            mainHandler.postDelayed({

                if (
                    destroyed ||
                    !rendererAlive
                ) {
                    return@postDelayed
                }

                if (pageVisible) {
                    prepararCSSAntiFlash()
                }

            }, 16)
        }
    }


    /*
     * =========================================================
     * PAUSE
     * =========================================================
     */

    fun onPause() {

        if (destroyed) {
            return
        }

        cancelPending()
    }


    /*
     * =========================================================
     * RENDERER MORREU
     * =========================================================
     */

    fun onRenderProcessGone(
        detail: RenderProcessGoneDetail
    ): Boolean {

        if (destroyed) {
            return true
        }

        rendererAlive =
            false

        pageVisible =
            false

        pageCommitted =
            false

        visualStatePending =
            false

        framePending =
            false

        pendingAction =
            null

        navigationGeneration++

        stabilizationGeneration++

        cssGeneration++

        /*
         * Continua transparente mesmo durante o encerramento.
         */

        webView.setBackgroundColor(
            Color.TRANSPARENT
        )

        return true
    }


    /*
     * =========================================================
     * ESTADOS
     * =========================================================
     */

    fun isRenderingPending():
        Boolean {

        return visualStatePending
    }


    fun isRendererAlive():
        Boolean {

        return rendererAlive
    }


    fun isPageVisible():
        Boolean {

        return pageVisible
    }


    fun getLastStableFrame():
        Long {

        return lastStableFrame
    }


    fun isPageCommitted():
        Boolean {

        return pageCommitted
    }


    /*
     * =========================================================
     * CANCELAR
     * =========================================================
     */

    fun cancelPending() {

        pendingAction =
            null

        if (framePending) {

            choreographer.removeFrameCallback(
                frameCallback
            )

            choreographer.removeFrameCallback(
                actionFrameCallback
            )

            framePending =
                false
        }

        visualStatePending =
            false

        visualCallbackGeneration =
            -1L
    }


    /*
     * =========================================================
     * DESTROY
     * =========================================================
     */

    fun destroy() {

        if (destroyed) {
            return
        }

        destroyed =
            true

        rendererAlive =
            false

        navigationGeneration++

        stabilizationGeneration++

        cssGeneration++

        cancelPending()

        mainHandler.removeCallbacksAndMessages(
            null
        )
    }
}