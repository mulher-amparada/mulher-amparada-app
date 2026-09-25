package com.mulheres

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONArray

class DownloadInterface(
    private val activity: MainActivity
) {

    @JavascriptInterface
    fun baixarPaginas(json: String) {

        try {

            val array = JSONArray(json)

            for (i in 0 until array.length()) {

                val arquivo = array.getJSONObject(i)

                val nome =
                    arquivo.getString("nome")

                val conteudo =
                    arquivo.getString("conteudo")

                salvarArquivo(
                    nome,
                    conteudo
                )
            }

            activity.runOnUiThread {

                Toast.makeText(
                    activity,
                    "Páginas baixadas",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (e: Exception) {

            e.printStackTrace()

            activity.runOnUiThread {

                Toast.makeText(
                    activity,
                    "Não foi possível iniciar o download",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun salvarArquivo(
        nome: String,
        conteudo: String
    ) {

        val resolver =
            activity.contentResolver

        val valores =
            ContentValues().apply {

                put(
                    MediaStore.Downloads.DISPLAY_NAME,
                    nome
                )

                put(
                    MediaStore.Downloads.MIME_TYPE,
                    "text/plain"
                )

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q
                ) {

                    put(
                        MediaStore.Downloads.IS_PENDING,
                        1
                    )
                }
            }

        val uri =
            resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                valores
            )
                ?: throw Exception(
                    "Não foi possível criar o arquivo"
                )

        resolver
            .openOutputStream(uri)
            ?.use { output ->

                output.write(
                    conteudo.toByteArray(
                        Charsets.UTF_8
                    )
                )

                output.flush()
            }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {

            val valoresFinais =
                ContentValues().apply {

                    put(
                        MediaStore.Downloads.IS_PENDING,
                        0
                    )
                }

            resolver.update(
                uri,
                valoresFinais,
                null,
                null
            )
        }
    }
}