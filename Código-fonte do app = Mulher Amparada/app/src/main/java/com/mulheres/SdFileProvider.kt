package com.mulheres

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileNotFoundException
import java.util.Base64
import java.util.Locale

class SdFileProvider : ContentProvider() {

    companion object {

        private const val COLUNA_NOME =
            OpenableColumns.DISPLAY_NAME

        private const val COLUNA_TAMANHO =
            OpenableColumns.SIZE

        /*
         * Transforma o caminho real em uma string segura
         * para ser colocada dentro do content://
         */
        fun getUriForFile(
            authority: String,
            file: File
        ): Uri {

            val caminho =
                file.canonicalPath

            val bytes =
                caminho.toByteArray(Charsets.UTF_8)

            val codificado =
                Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(bytes)

            return Uri.Builder()
                .scheme("content")
                .authority(authority)
                .appendPath(codificado)
                .build()
        }
    }

    // =========================================================
    // CRIAÇÃO
    // =========================================================

    override fun onCreate(): Boolean {
        return true
    }

    // =========================================================
    // MIME TYPE
    // =========================================================

    override fun getType(
        uri: Uri
    ): String? {

        val file =
            obterArquivo(uri)

        if (
            !file.exists() ||
            !file.isFile
        ) {
            return null
        }

        val extensao =
            file.extension
                .lowercase(Locale.ROOT)

        return MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(
                extensao
            )
            ?: "application/octet-stream"
    }

    // =========================================================
    // ABRIR ARQUIVO
    // =========================================================

    override fun openFile(
        uri: Uri,
        mode: String
    ): ParcelFileDescriptor {

        /*
         * O provider é somente leitura.
         */
        if (
            mode != "r" &&
            mode != "rt"
        ) {

            throw FileNotFoundException(
                "O arquivo é somente leitura"
            )
        }

        val file =
            obterArquivo(uri)

        if (!file.exists()) {

            throw FileNotFoundException(
                "Arquivo não encontrado"
            )
        }

        if (!file.isFile) {

            throw FileNotFoundException(
                "O caminho não é um arquivo"
            )
        }

        if (!file.canRead()) {

            throw FileNotFoundException(
                "Não foi possível ler o arquivo"
            )
        }

        return ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
    }

    // =========================================================
    // INFORMAÇÕES DO ARQUIVO
    // =========================================================

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {

        val file =
            obterArquivo(uri)

        if (
            !file.exists() ||
            !file.isFile
        ) {

            throw FileNotFoundException(
                "Arquivo não encontrado"
            )
        }

        val colunas =
            projection
                ?: arrayOf(
                    COLUNA_NOME,
                    COLUNA_TAMANHO
                )

        val cursor =
            MatrixCursor(colunas)

        val valores =
            Array<Any?>(
                colunas.size
            ) {
                null
            }

        for (
            i in colunas.indices
        ) {

            when (colunas[i]) {

                COLUNA_NOME -> {
                    valores[i] =
                        file.name
                }

                COLUNA_TAMANHO -> {
                    valores[i] =
                        file.length()
                }
            }
        }

        cursor.addRow(valores)

        return cursor
    }

    // =========================================================
    // STREAM TYPES
    // =========================================================

    override fun getStreamTypes(
        uri: Uri,
        mimeTypeFilter: String
    ): Array<String>? {

        val tipo =
            getType(uri)
                ?: return null

        if (
            mimeTypeFilter == "*/*"
        ) {
            return arrayOf(tipo)
        }

        val prefixo =
            mimeTypeFilter
                .substringBefore(
                    "/"
                )

        val tipoPrincipal =
            tipo.substringBefore(
                "/"
            )

        return if (
            prefixo == tipoPrincipal
        ) {
            arrayOf(tipo)
        } else {
            null
        }
    }

    // =========================================================
    // INSERT
    // =========================================================

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? {

        throw UnsupportedOperationException(
            "Somente leitura"
        )
    }

    // =========================================================
    // DELETE
    // =========================================================

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {

        throw UnsupportedOperationException(
            "Somente leitura"
        )
    }

    // =========================================================
    // UPDATE
    // =========================================================

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {

        throw UnsupportedOperationException(
            "Somente leitura"
        )
    }

    // =========================================================
    // RECUPERAR ARQUIVO
    // =========================================================

    private fun obterArquivo(
        uri: Uri
    ): File {

        val segmento =
            uri.pathSegments
                .firstOrNull()
                ?: throw FileNotFoundException(
                    "URI inválida"
                )

        val caminho =

            try {

                val bytes =
                    Base64.getUrlDecoder()
                        .decode(segmento)

                String(
                    bytes,
                    Charsets.UTF_8
                )

            } catch (e: Exception) {

                throw FileNotFoundException(
                    "Caminho inválido"
                )
            }

        val arquivo =
            File(caminho)
                .canonicalFile

        /*
         * Aceita armazenamento externo:
         *
         * /storage/emulated/0/...
         *
         * /storage/XXXX-XXXX/...
         */
        if (
            !arquivo.absolutePath
                .startsWith(
                    "/storage/"
                )
        ) {

            throw SecurityException(
                "Arquivo fora do armazenamento externo"
            )
        }

        return arquivo
    }
}