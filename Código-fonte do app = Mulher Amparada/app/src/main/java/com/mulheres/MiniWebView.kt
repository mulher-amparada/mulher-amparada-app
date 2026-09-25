package com.mulheres

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader

class MiniWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ScrollView(context, attrs) {

    private val pagina =
        LinearLayout(context).apply {

            orientation =
                LinearLayout.VERTICAL

            setPadding(
                0,
                0,
                0,
                0
            )

            setBackgroundColor(
                Color.TRANSPARENT
            )
        }

    init {

        setBackgroundColor(
            Color.TRANSPARENT
        )

        isFillViewport =
            true

        overScrollMode =
            View.OVER_SCROLL_NEVER

        addView(
            pagina,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        )
    }

    fun loadUrl(
        url: String
    ) {

        if (
            !url.startsWith(
                "file:///android_asset/"
            )
        ) {
            return
        }

        val caminho =
            url.removePrefix(
                "file:///android_asset/"
            )

        try {

            val html =
                assetsOpen(
                    caminho
                )

            carregarHtml(
                html
            )

        } catch (
            _: Exception
        ) {

            pagina.removeAllViews()
        }
    }

    private fun assetsOpen(
        caminho: String
    ): String {

        val input =
            context.assets.open(
                caminho
            )

        return BufferedReader(
            InputStreamReader(
                input,
                Charsets.UTF_8
            )
        ).use {
            it.readText()
        }
    }

    private fun carregarHtml(
        html: String
    ) {

        pagina.removeAllViews()

        val corpo =
            extrairBody(
                html
            )

        renderizar(
            corpo,
            pagina
        )
    }

    private fun extrairBody(
        html: String
    ): String {

        val inicio =
            Regex(
                "<body[^>]*>",
                RegexOption.IGNORE_CASE
            ).find(html)

        val fim =
            Regex(
                "</body>",
                RegexOption.IGNORE_CASE
            ).find(html)

        if (
            inicio != null &&
            fim != null
        ) {

            return html.substring(
                inicio.range.last + 1,
                fim.range.first
            )
        }

        return html
    }

    private fun renderizar(
        html: String,
        container: ViewGroup
    ) {

        val regex =
            Regex(
                """<([a-zA-Z0-9]+)([^>]*)>(.*?)</\1\s*>""",
                setOf(
                    RegexOption.IGNORE_CASE,
                    RegexOption.DOT_MATCHES_ALL
                )
            )

        var posicao = 0

        regex.findAll(html).forEach { match ->

            val textoAntes =
                html.substring(
                    posicao,
                    match.range.first
                )

            adicionarTextoSolto(
                textoAntes,
                container
            )

            val tag =
                match.groupValues[1]
                    .lowercase()

            val atributos =
                match.groupValues[2]

            val conteudo =
                match.groupValues[3]

            adicionarElemento(
                tag,
                atributos,
                conteudo,
                container
            )

            posicao =
                match.range.last + 1
        }

        val restante =
            html.substring(
                posicao
            )

        adicionarTextoSolto(
            restante,
            container
        )
    }

    private fun adicionarElemento(
        tag: String,
        atributos: String,
        conteudo: String,
        container: ViewGroup
    ) {

        when (tag) {

            "div",
            "section",
            "main",
            "header",
            "footer",
            "article" -> {

                val layout =
                    LinearLayout(context).apply {

                        orientation =
                            LinearLayout.VERTICAL

                        layoutParams =
                            LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT
                            )

                        aplicarEstilo(
                            this,
                            atributos
                        )
                    }

                container.addView(
                    layout
                )

                renderizar(
                    conteudo,
                    layout
                )
            }

            "h1",
            "h2",
            "h3",
            "h4",
            "h5",
            "h6",
            "p",
            "span",
            "label",
            "button" -> {

                val texto =
                    TextView(context)

                texto.text =
                    limparTexto(
                        conteudo
                    )

                texto.setTextColor(
                    Color.WHITE
                )

                texto.textSize =
                    when (tag) {

                        "h1" -> 30f
                        "h2" -> 26f
                        "h3" -> 22f
                        "h4" -> 20f
                        "h5" -> 18f
                        "h6" -> 16f
                        else -> 16f
                    }

                texto.setPadding(
                    0,
                    8,
                    0,
                    8
                )

                if (
                    tag.startsWith("h")
                ) {

                    texto.setTypeface(
                        null,
                        Typeface.BOLD
                    )
                }

                aplicarEstilo(
                    texto,
                    atributos
                )

                if (
                    tag == "button"
                ) {

                    texto.gravity =
                        Gravity.CENTER

                    texto.setOnClickListener {
                        executarClique(
                            atributos
                        )
                    }
                }

                container.addView(
                    texto
                )
            }

            "img" -> {

                val src =
                    extrairAtributo(
                        atributos,
                        "src"
                    )

                if (
                    src.isNotEmpty()
                ) {

                    val imagem =
                        ImageView(context)

                    imagem.scaleType =
                        ImageView.ScaleType.CENTER_CROP

                    val caminho =
                        src.removePrefix(
                            "file:///android_asset/"
                        )

                    try {

                        context.assets
                            .open(caminho)
                            .use { stream ->

                                imagem.setImageBitmap(
                                    android.graphics.BitmapFactory
                                        .decodeStream(stream)
                                )
                            }

                        container.addView(
                            imagem,
                            LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT
                            )
                        )

                    } catch (
                        _: Exception
                    ) {
                    }
                }
            }

            "br" -> {

                val espaco =
                    View(context)

                espaco.layoutParams =
                    LinearLayout.LayoutParams(
                        1,
                        12
                    )

                container.addView(
                    espaco
                )
            }
        }
    }

    private fun adicionarTextoSolto(
        texto: String,
        container: ViewGroup
    ) {

        val limpo =
            limparTexto(
                texto
            ).trim()

        if (
            limpo.isEmpty()
        ) {
            return
        }

        val view =
            TextView(context)

        view.text =
            limpo

        view.textSize =
            16f

        view.setTextColor(
            Color.WHITE
        )

        view.setPadding(
            0,
            4,
            0,
            4
        )

        container.addView(
            view
        )
    }

    private fun limparTexto(
        texto: String
    ): String {

        return texto
            .replace(
                Regex("<[^>]+>"),
                ""
            )
            .replace(
                "&nbsp;",
                " "
            )
            .replace(
                "&amp;",
                "&"
            )
            .replace(
                "&lt;",
                "<"
            )
            .replace(
                "&gt;",
                ">"
            )
            .trim()
    }

    private fun extrairAtributo(
        atributos: String,
        nome: String
    ): String {

        val regex =
            Regex(
                """$nome\s*=\s*["']([^"']*)["']""",
                RegexOption.IGNORE_CASE
            )

        return regex
            .find(atributos)
            ?.groupValues
            ?.getOrNull(1)
            ?: ""
    }

    private fun aplicarEstilo(
        view: View,
        atributos: String
    ) {

        val style =
            extrairAtributo(
                atributos,
                "style"
            )

        if (
            style.isEmpty()
        ) {
            return
        }

        val propriedades =
            style.split(";")

        var fundo =
            Color.TRANSPARENT

        var raio =
            0f

        var padding =
            0

        var margem =
            0

        var corTexto =
            Color.WHITE

        propriedades.forEach { propriedade ->

            val partes =
                propriedade.split(
                    ":",
                    limit = 2
                )

            if (
                partes.size != 2
            ) {
                return@forEach
            }

            val nome =
                partes[0]
                    .trim()
                    .lowercase()

            val valor =
                partes[1]
                    .trim()

            when (nome) {

                "background",
                "background-color" -> {

                    parseColor(
                        valor
                    )?.let {
                        fundo = it
                    }
                }

                "color" -> {

                    parseColor(
                        valor
                    )?.let {
                        corTexto = it
                    }
                }

                "border-radius" -> {

                    raio =
                        valor
                            .replace(
                                "px",
                                ""
                            )
                            .toFloatOrNull()
                            ?: 0f
                }

                "padding" -> {

                    padding =
                        valor
                            .replace(
                                "px",
                                ""
                            )
                            .toIntOrNull()
                            ?: 0
                }

                "margin" -> {

                    margem =
                        valor
                            .replace(
                                "px",
                                ""
                            )
                            .toIntOrNull()
                            ?: 0
                }
            }
        }

        if (
            view is TextView
        ) {

            view.setTextColor(
                corTexto
            )
        }

        if (
            fundo != Color.TRANSPARENT ||
            raio > 0f
        ) {

            val drawable =
                GradientDrawable()

            drawable.setColor(
                fundo
            )

            drawable.cornerRadius =
                raio

            view.background =
                drawable
        }

        view.setPadding(
            padding,
            padding,
            padding,
            padding
        )

        val params =
            view.layoutParams

        if (
            params is ViewGroup.MarginLayoutParams
        ) {

            params.setMargins(
                margem,
                margem,
                margem,
                margem
            )

            view.layoutParams =
                params
        }
    }

    private fun parseColor(
        valor: String
    ): Int? {

        return try {

            Color.parseColor(
                valor
            )

        } catch (
            _: Exception
        ) {

            null
        }
    }

    private fun executarClique(
        atributos: String
    ) {

        val onclick =
            extrairAtributo(
                atributos,
                "onclick"
            )

        /*
         * Nesta primeira versão não
         * executamos JavaScript.
         *
         * Os eventos serão ligados
         * posteriormente ao nosso
         * próprio sistema de eventos.
         */
    }
}