package com.mulheres

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException
import java.util.Locale

class SdFileProvider : ContentProvider() {

    companion object {

        private const val DISPLAY_NAME =
            OpenableColumns.DISPLAY_NAME

        private const val SIZE =
            OpenableColumns.SIZE

        fun getUriForFile(
            authority: String,
            file: File
        ): Uri {

            val caminho =
                file.canonicalPath

            return Uri.Builder()
                .scheme("content")
                .authority(authority)
                .encodedPath(
                    Uri.encode(
                        caminho,
                        "/"
                    )
                )
                .build()
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun getType(uri: Uri): String? {

        val file =
            obterArquivo(uri)

        if (!file.exists() || !file.isFile) {
            return null
        }

        val extensao =
            file.extension.lowercase(Locale.ROOT)

        return android.webkit.MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(extensao)
            ?: "application/octet-stream"
    }

    override fun openFile(
        uri: Uri,
        mode: String
    ): ParcelFileDescriptor {

        /*
         * Este provider é somente para leitura.
         */
        if (mode != "r" && mode != "rt") {
            throw FileNotFoundException(
                "Somente leitura é permitida"
            )
        }

        val file =
            obterArquivo(uri)

        if (!file.exists()) {
            throw FileNotFoundException(
                "Arquivo não encontrado: ${file.absolutePath}"
            )
        }

        if (!file.isFile) {
            throw FileNotFoundException(
                "O caminho não é um arquivo"
            )
        }

        if (!file.canRead()) {
            throw FileNotFoundException(
                "Sem permissão para ler o arquivo"
            )
        }

        return ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {

        val file =
            obterArquivo(uri)

        if (!file.exists() || !file.isFile) {
            throw FileNotFoundException(
                "Arquivo não encontrado"
            )
        }

        val colunas =
            projection ?: arrayOf(
                DISPLAY_NAME,
                SIZE
            )

        val cursor =
            MatrixCursor(colunas)

        val linha =
            Array<Any?>(colunas.size) {
                null
            }

        for (i in colunas.indices) {

            when (colunas[i]) {

                DISPLAY_NAME -> {
                    linha[i] = file.name
                }

                SIZE -> {
                    linha[i] = file.length()
                }
            }
        }

        cursor.addRow(linha)

        return cursor
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? {
        throw UnsupportedOperationException(
            "Somente leitura"
        )
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException(
            "Somente leitura"
        )
    }

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

    private fun obterArquivo(
        uri: Uri
    ): File {

        val caminho =
            uri.encodedPath
                ?.let {
                    Uri.decode(it)
                }
                ?: throw FileNotFoundException(
                    "Caminho inválido"
                )

        if (caminho.isBlank()) {
            throw FileNotFoundException(
                "Caminho vazio"
            )
        }

        val file =
            File(caminho).canonicalFile

        /*
         * Permite somente arquivos em volumes
         * externos reais.
         *
         * Exemplos:
         *
         * /storage/emulated/0/...
         * /storage/1234-5678/...
         */
        val caminhoAbsoluto =
            file.absolutePath

        if (
            !caminhoAbsoluto.startsWith(
                "/storage/"
            )
        ) {

            throw SecurityException(
                "Caminho fora do armazenamento externo"
            )
        }

        return file
    }
}