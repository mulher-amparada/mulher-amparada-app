package com.mulheres

import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebSettings

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

    private var scrollControllerInjected = false

    private var pageVisible = false

    private var lastStableFrame = 0L

    private var pendingAction: (() -> Unit)? = null


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

            sincronizarEstadoVisual()
        }


    /*
     * =========================================================
     * FRAME DE OPERAÇÃO
     * =========================================================
     */

    private val frameCallbackAction =
        Choreographer.FrameCallback {

            framePending = false

            if (
                destroyed ||
                !rendererAlive
            ) {
                pendingAction = null
                return@FrameCallback
            }

            val action =
                pendingAction

            pendingAction = null

            action?.invoke()

            sincronizarEstadoVisual()
        }


    /*
     * =========================================================
     * INICIALIZAÇÃO
     * =========================================================
     */

    init {

        configurarWebView()

        configurarPrioridadeRenderer()
    }


    /*
     * =========================================================
     * CONFIGURAÇÃO DA WEBVIEW
     * =========================================================
     */

    private fun configurarWebView() {

        webView.setBackgroundColor(
            Color.TRANSPARENT
        )

        webView.overScrollMode =
            WebView.OVER_SCROLL_NEVER

        webView.isVerticalScrollBarEnabled =
            false

        webView.isHorizontalScrollBarEnabled =
            false

        webView.settings.apply {

            javaScriptEnabled = true

            domStorageEnabled = true

            builtInZoomControls = false

            displayZoomControls = false

            /*
             * Não usamos cache agressivo para tentar
             * resolver problemas de renderização.
             *
             * O Chromium continua administrando
             * seu próprio cache.
             */

            cacheMode =
                WebSettings.LOAD_DEFAULT

            /*
             * Mantém a viewport controlada pela página.
             */

            useWideViewPort = true

            loadWithOverviewMode = false
        }
    }


    /*
     * =========================================================
     * PRIORIDADE DO RENDERER
     * =========================================================
     */

    private fun configurarPrioridadeRenderer() {

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
     * RESTAURAR PRIORIDADE
     * =========================================================
     */

    fun restaurarPrioridade() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

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

        navigationGeneration++

        val generation =
            navigationGeneration

        visualStatePending = true

        pageVisible = false

        scrollControllerInjected = false

        agendarFrame {

            if (
                destroyed ||
                !rendererAlive ||
                generation != navigationGeneration
            ) {
                return@agendarFrame
            }

            webView.loadUrl(url)
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

        navigationGeneration++

        val generation =
            navigationGeneration

        visualStatePending = true

        pageVisible = false

        scrollControllerInjected = false

        agendarFrame {

            if (
                destroyed ||
                !rendererAlive ||
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
     * =========================================================
     * AGENDAR OPERAÇÃO NO VSYNC
     * =========================================================
     */

    private fun agendarFrame(
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

                framePending = true

                choreographer.postFrameCallback(
                    frameCallbackAction
                )
            }
        }
    }


    /*
     * =========================================================
     * PÁGINA COMEÇOU
     * =========================================================
     */

    fun onPageStarted() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        pageVisible = false

        visualStatePending = true

        scrollControllerInjected = false

        solicitarFrame()
    }


    /*
     * =========================================================
     * PÁGINA TERMINOU
     * =========================================================
     */

    fun onPageFinished() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        visualStatePending = true

        solicitarFrame()
    }


    /*
     * =========================================================
     * PRIMEIRO CONTEÚDO VISÍVEL
     * =========================================================
     */

    fun onPageCommitVisible() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        pageVisible = true

        visualStatePending = true

        solicitarFrame()

        /*
         * Espera o DOM estar disponível antes
         * de instalar o controlador.
         */

        mainHandler.postDelayed({

            if (
                destroyed ||
                !rendererAlive
            ) {
                return@postDelayed
            }

            instalarControladorScroll()

        }, 16)
    }


    /*
     * =========================================================
     * SINCRONIZAÇÃO VISUAL
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

            visualStatePending = false

            return
        }

        val generation =
            navigationGeneration

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
                            !rendererAlive ||
                            requestId !=
                            navigationGeneration
                        ) {
                            return@postFrameCallback
                        }

                        visualStatePending = false

                        pageVisible = true

                        lastStableFrame =
                            System.nanoTime()
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

        framePending = true

        choreographer.postFrameCallback(
            frameCallback
        )
    }


    /*
     * =========================================================
     * CONTROLADOR AUTOMÁTICO DE SCROLL
     *
     * TODOS OS ELEMENTOS DO DOM.
     *
     * NÃO PRECISA DE:
     *
     * data-render-fade
     * classes
     * IDs
     * alterações no HTML
     *
     * =========================================================
     */

    private fun instalarControladorScroll() {

        if (
            destroyed ||
            !rendererAlive ||
            scrollControllerInjected
        ) {
            return
        }

        scrollControllerInjected = true


        val script = """

            (function() {

                if (
                    window.__mulheresRenderController
                ) {
                    return;
                }


                window.__mulheresRenderController =
                    true;


                /*
                 * =================================================
                 * ESTADO
                 * =================================================
                 */

                let ultimoY =
                    window.scrollY;

                let ultimoTempo =
                    performance.now();

                let processando =
                    false;

                let timeoutRestauracao =
                    null;


                /*
                 * Guarda os estilos originais
                 * para não destruir o CSS da página.
                 */

                const estilos =
                    new WeakMap();


                /*
                 * =================================================
                 * CAPTURA ELEMENTOS
                 * =================================================
                 *
                 * Todos os elementos do documento.
                 */

                function obterElementos() {

                    return Array.from(
                        document.querySelectorAll('*')
                    );

                }


                /*
                 * =================================================
                 * PREPARAR ELEMENTO
                 * =================================================
                 */

                function prepararElemento(
                    elemento
                ) {

                    if (
                        !elemento ||
                        elemento === document.documentElement ||
                        elemento === document.body
                    ) {
                        return;
                    }


                    if (
                        !estilos.has(elemento)
                    ) {

                        estilos.set(
                            elemento,
                            {
                                opacity:
                                    elemento.style.opacity,

                                transition:
                                    elemento.style.transition,

                                willChange:
                                    elemento.style.willChange
                            }
                        );

                    }

                }


                /*
                 * =================================================
                 * APLICAR FADE
                 * =================================================
                 */

                function aplicarFade(
                    velocidade
                ) {

                    const velocidadeAbs =
                        Math.abs(
                            velocidade
                        );


                    /*
                     * Scroll extremamente lento.
                     */

                    let duracao =
                        180;


                    /*
                     * Conforme o scroll aumenta,
                     * o fade fica mais rápido.
                     */

                    if (
                        velocidadeAbs >= 300
                    ) {
                        duracao = 165;
                    }

                    if (
                        velocidadeAbs >= 600
                    ) {
                        duracao = 140;
                    }

                    if (
                        velocidadeAbs >= 1000
                    ) {
                        duracao = 110;
                    }

                    if (
                        velocidadeAbs >= 1600
                    ) {
                        duracao = 85;
                    }

                    if (
                        velocidadeAbs >= 2400
                    ) {
                        duracao = 65;
                    }

                    if (
                        velocidadeAbs >= 3500
                    ) {
                        duracao = 50;
                    }

                    if (
                        velocidadeAbs >= 5000
                    ) {
                        duracao = 35;
                    }


                    /*
                     * Fade proporcional à velocidade.
                     */

                    let opacidade =
                        0.96;


                    if (
                        velocidadeAbs >= 600
                    ) {
                        opacidade = 0.94;
                    }

                    if (
                        velocidadeAbs >= 1200
                    ) {
                        opacidade = 0.91;
                    }

                    if (
                        velocidadeAbs >= 2200
                    ) {
                        opacidade = 0.88;
                    }

                    if (
                        velocidadeAbs >= 3500
                    ) {
                        opacidade = 0.84;
                    }

                    if (
                        velocidadeAbs >= 5000
                    ) {
                        opacidade = 0.80;
                    }


                    /*
                     * Todos os elementos.
                     */

                    const elementos =
                        obterElementos();


                    elementos.forEach(
                        function(elemento) {

                            prepararElemento(
                                elemento
                            );


                            /*
                             * Não usamos display:none,
                             * visibility ou filtros pesados.
                             *
                             * Somente opacity.
                             */

                            elemento.style.transition =
                                'opacity ' +
                                duracao +
                                'ms linear';


                            elemento.style.willChange =
                                'opacity';


                            elemento.style.opacity =
                                String(
                                    opacidade
                                );

                        }
                    );


                    /*
                     * Próximo frame:
                     * começa a recuperação.
                     */

                    requestAnimationFrame(
                        function() {

                            requestAnimationFrame(
                                function() {

                                    const elementos =
                                        obterElementos();


                                    elementos.forEach(
                                        function(elemento) {

                                            if (
                                                elemento ===
                                                document.documentElement ||
                                                elemento ===
                                                document.body
                                            ) {
                                                return;
                                            }


                                            if (
                                                !estilos.has(
                                                    elemento
                                                )
                                            ) {
                                                return;
                                            }


                                            elemento.style.opacity =
                                                '1';

                                        }
                                    );

                                }
                            );

                        }
                    );

                }


                /*
                 * =================================================
                 * PROCESSAR SCROLL
                 * =================================================
                 */

                function processarScroll() {

                    processando =
                        false;


                    const agora =
                        performance.now();


                    const y =
                        window.scrollY;


                    const deltaY =
                        y - ultimoY;


                    const deltaTempo =
                        Math.max(
                            agora -
                            ultimoTempo,
                            1
                        );


                    /*
                     * Pixels por segundo.
                     */

                    const velocidade =
                        (
                            deltaY /
                            deltaTempo
                        ) *
                        1000;


                    ultimoY =
                        y;


                    ultimoTempo =
                        agora;


                    aplicarFade(
                        velocidade
                    );

                }


                /*
                 * =================================================
                 * EVENTO SCROLL
                 * =================================================
                 */

                window.addEventListener(
                    'scroll',
                    function() {

                        if (
                            processando
                        ) {
                            return;
                        }


                        processando =
                            true;


                        requestAnimationFrame(
                            processarScroll
                        );


                        clearTimeout(
                            timeoutRestauracao
                        );


                        timeoutRestauracao =
                            setTimeout(
                                restaurarTudo,
                                120
                            );

                    },
                    {
                        passive: true
                    }
                );


                /*
                 * =================================================
                 * RESTAURAR TUDO
                 * =================================================
                 */

                function restaurarTudo() {

                    const elementos =
                        obterElementos();


                    elementos.forEach(
                        function(elemento) {

                            if (
                                elemento ===
                                document.documentElement ||
                                elemento ===
                                document.body
                            ) {
                                return;
                            }


                            const original =
                                estilos.get(
                                    elemento
                                );


                            if (!original) {
                                return;
                            }


                            elemento.style.transition =
                                'opacity 180ms ease-out';


                            elemento.style.opacity =
                                original.opacity ||
                                '1';


                            elemento.style.willChange =
                                original.willChange ||
                                '';

                        }
                    );

                }


                /*
                 * =================================================
                 * RESTAURAÇÃO QUANDO A PÁGINA FICA INVISÍVEL
                 * =================================================
                 */

                document.addEventListener(
                    'visibilitychange',
                    function() {

                        if (
                            document.hidden
                        ) {
                            restaurarTudo();
                        }

                    }
                );


            })();

        """.trimIndent()


        webView.evaluateJavascript(
            script,
            null
        )
    }


    /*
     * =========================================================
     * REINICIALIZAR RENDERIZAÇÃO
     * =========================================================
     */

    fun reinicializarRenderizacao() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        scrollControllerInjected =
            false

        restaurarPrioridade()

        mainHandler.postDelayed({

            if (
                destroyed ||
                !rendererAlive
            ) {
                return@postDelayed
            }

            instalarControladorScroll()

        }, 16)
    }


    /*
     * =========================================================
     * ACTIVITY VOLTOU A FICAR ATIVA
     * =========================================================
     */

    fun onResume() {

        if (
            destroyed ||
            !rendererAlive
        ) {
            return
        }

        restaurarPrioridade()

        solicitarFrame()

        mainHandler.postDelayed({

            if (
                destroyed ||
                !rendererAlive
            ) {
                return@postDelayed
            }

            reinicializarRenderizacao()

        }, 16)
    }


    /*
     * =========================================================
     * ACTIVITY FOI PAUSADA
     * =========================================================
     */

    fun onPause() {

        if (
            destroyed
        ) {
            return
        }

        /*
         * Não destruímos o renderer.
         *
         * Apenas cancelamos operações pendentes.
         */

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
            false;


        visualStatePending =
            false;


        framePending =
            false;


        pendingAction =
            null;


        scrollControllerInjected =
            false;


        navigationGeneration++;


        /*
         * O renderer antigo não pode mais receber
         * nenhuma operação.
         */

        mainHandler.post {

            if (destroyed) {
                return@post
            }


            rendererAlive =
                true;


            navigationGeneration++;


            restaurarPrioridade();


            /*
             * O WebView pode precisar ser reconstruído
             * pela Activity caso o renderer tenha realmente
             * sido encerrado.
             */

            solicitarFrame();

        }


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


    /*
     * =========================================================
     * CANCELAR OPERAÇÕES
     * =========================================================
     */

    fun cancelPending() {

        pendingAction =
            null;


        if (framePending) {

            choreographer.removeFrameCallback(
                frameCallback
            )

            choreographer.removeFrameCallback(
                frameCallbackAction
            )

            framePending =
                false
        }


        visualStatePending =
            false
    }


    /*
     * =========================================================
     * DESTRUIÇÃO DEFINITIVA
     * =========================================================
     */

    fun destroy() {

        if (destroyed) {
            return
        }


        destroyed =
            true;


        rendererAlive =
            false;


        navigationGeneration++;


        cancelPending();


        mainHandler.removeCallbacksAndMessages(
            null
        )
    }
}