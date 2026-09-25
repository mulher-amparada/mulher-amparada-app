package com.mulheres

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.Choreographer

class WebViewRenderManager(
    private val webView: WebView
) {

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val choreographer =
        Choreographer.getInstance()

    private var destroyed = false
    private var navigationGeneration = 0L
    private var framePending = false
    private var visualStatePending = false

    private var lastStableFrame = 0L

    private val frameCallback =
        Choreographer.FrameCallback { frameTimeNanos ->

            framePending = false

            if (destroyed) {
                return@FrameCallback
            }

            lastStableFrame = frameTimeNanos

            sincronizarEstadoVisual()
        }

    init {
        configurarPrioridadeRenderer()
    }

    /*
     * Mantém o renderer da WebView na prioridade máxima
     * permitida pela API pública.
     *
     * IMPORTANTE:
     * não usamos WAIVED nem BOUND.
     *
     * IMPORTANT é a prioridade máxima.
     */
    private fun configurarPrioridadeRenderer() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            webView.setRendererPriorityPolicy(
                WebView.RENDERER_PRIORITY_IMPORTANT,
                false
            )
        }
    }

    /*
     * Carregamento controlado.
     *
     * A chamada passa primeiro pelo ciclo de frame
     * para evitar iniciar várias operações de renderização
     * no mesmo instante.
     */
    fun loadUrl(url: String) {

        if (destroyed) {
            return
        }

        navigationGeneration++

        val generation =
            navigationGeneration

        visualStatePending = true

        agendarFrame {

            if (
                destroyed ||
                generation != navigationGeneration
            ) {
                return@agendarFrame
            }

            webView.loadUrl(url)
        }
    }

    /*
     * Carregamento de dados.
     */
    fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String,
        encoding: String?,
        historyUrl: String?
    ) {

        if (destroyed) {
            return
        }

        navigationGeneration++

        val generation =
            navigationGeneration

        visualStatePending = true

        agendarFrame {

            if (
                destroyed ||
                generation != navigationGeneration
            ) {
                return@agendarFrame
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
     * Agenda operações no VSYNC.
     *
     * Se já existe uma operação esperando o próximo frame,
     * não criamos outra.
     */
    private fun agendarFrame(
        action: () -> Unit
    ) {

        if (destroyed) {
            return
        }

        mainHandler.post {

            if (destroyed) {
                return@post
            }

            pendingAction = action

            if (!framePending) {

                framePending = true

                choreographer.postFrameCallback(
                    frameCallbackAction
                )
            }
        }
    }

    private var pendingAction:
        (() -> Unit)? = null

    private val frameCallbackAction =
        Choreographer.FrameCallback {

            framePending = false

            if (destroyed) {
                pendingAction = null
                return@FrameCallback
            }

            val action =
                pendingAction

            pendingAction = null

            action?.invoke()

            /*
             * Depois da operação, sincronizamos novamente
             * com o estado visual da WebView.
             */
            sincronizarEstadoVisual()
        }

    /*
     * Espera o Chromium informar que o estado visual
     * atual está pronto para ser desenhado.
     */
    private fun sincronizarEstadoVisual() {

        if (
            destroyed ||
            !visualStatePending
        ) {
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            visualStatePending = false
            return
        }

        if (visualStatePending) {

            val generation =
                navigationGeneration

            webView.postVisualStateCallback(
                generation,
                object : WebView.VisualStateCallback() {

                    override fun onComplete(
                        requestId: Long
                    ) {

                        if (destroyed) {
                            return
                        }

                        if (
                            requestId !=
                            navigationGeneration
                        ) {
                            return
                        }

                        /*
                         * O callback informa que o estado
                         * visual está pronto para o próximo draw.
                         *
                         * Damos um ciclo adicional de VSYNC
                         * antes de considerar a composição estável.
                         */
                        choreographer.postFrameCallback {

                            if (
                                destroyed ||
                                requestId !=
                                navigationGeneration
                            ) {
                                return@postFrameCallback
                            }

                            visualStatePending = false
                            lastStableFrame =
                                System.nanoTime()
                        }
                    }
                }
            )
        }
    }

    /*
     * Deve ser chamado pelo WebViewClient quando
     * uma navegação realmente começa.
     */
    fun onPageStarted() {

        if (destroyed) {
            return
        }

        visualStatePending = true

        solicitarFrame()
    }

    /*
     * Página terminou de carregar.
     *
     * Não consideramos "carregou" como sinônimo de
     * "está visualmente pronto".
     */
    fun onPageFinished() {

        if (destroyed) {
            return
        }

        visualStatePending = true

        solicitarFrame()
    }

    /*
     * Informa que houve mudança de composição.
     */
    fun onPageCommitVisible() {

        if (destroyed) {
            return
        }

        visualStatePending = true

        solicitarFrame()
    }

    private fun solicitarFrame() {

        if (
            destroyed ||
            framePending
        ) {
            return
        }

        framePending = true

        choreographer.postFrameCallback(
            frameCallback
        )
    }

    /*
     * Permite consultar se o gerenciador está esperando
     * uma composição visual.
     */
    fun isRenderingPending(): Boolean {
        return visualStatePending
    }

    /*
     * Garante que o renderer continue com prioridade máxima.
     *
     * Pode ser chamado depois de mudanças importantes
     * de estado da Activity.
     */
    fun restaurarPrioridade() {

        if (
            destroyed ||
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        webView.setRendererPriorityPolicy(
            WebView.RENDERER_PRIORITY_IMPORTANT,
            false
        )
    }

    /*
     * Tratamento de renderer morto/crashado.
     *
     * Retorna true porque a Activity continua viva.
     */
    fun onRenderProcessGone(
        detail: RenderProcessGoneDetail
    ): Boolean {

        if (destroyed) {
            return true
        }

        visualStatePending = false
        framePending = false

        /*
         * A WebView não deve continuar sendo tratada
         * como se o renderer antigo ainda estivesse vivo.
         */
        navigationGeneration++

        return true
    }

    /*
     * Cancela operações pendentes.
     */
    fun cancelPending() {

        pendingAction = null

        if (framePending) {
            choreographer.removeFrameCallback(
                frameCallbackAction
            )

            framePending = false
        }

        visualStatePending = false
    }

    /*
     * Liberação definitiva.
     */
    fun destroy() {

        if (destroyed) {
            return
        }

        destroyed = true

        navigationGeneration++

        cancelPending()

        mainHandler.removeCallbacksAndMessages(
            null
        )
    }
}
