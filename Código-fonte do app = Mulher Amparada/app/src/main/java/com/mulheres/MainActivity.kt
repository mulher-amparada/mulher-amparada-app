package com.mulheres

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.Collator
import java.util.Locale

class FileActivity : AppCompatActivity() {

    companion object {
        const val MANAGE_STORAGE_CODE = 101
        const val PERMISSION_CODE = 100
    }

    private lateinit var adapter: FolderAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var pathText: TextView
    private lateinit var itemCount: TextView
    private lateinit var storageButton: ImageButton

    /*
     * Histórico do armazenamento interno.
     */
    private val internalHistory = ArrayList<File>()

    /*
     * Histórico do cartão SD.
     */
    private val sdHistory = ArrayList<File>()

    private var internalIndex = -1
    private var sdIndex = -1

    /*
     * false = armazenamento interno
     * true  = cartão SD
     */
    private var usandoCartaoSd = false

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        configurarSistema()

        setContentView(R.layout.activity_file)

        val raiz = findViewById<View>(
            android.R.id.content
        )

        aplicarFonte(raiz)

        recycler = findViewById(R.id.recycler)
        pathText = findViewById(R.id.pathText)
        itemCount = findViewById(R.id.itemCount)
        storageButton = findViewById(R.id.storageButton)

        configurarRecycler()
        configurarBack()
        configurarArmazenamento()

        /*
         * Estado inicial:
         * armazenamento interno.
         */
        atualizarBotaoArmazenamento()

        if (!temPermissao()) {
            pedirPermissao()
        } else {
            iniciar()
        }
    }

    // =========================================================
    // SISTEMA
    // =========================================================

    private fun configurarSistema() {

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val controller =
            WindowInsetsControllerCompat(
                window,
                window.decorView
            )

        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
    }

    // =========================================================
    // BOTÃO DE ARMAZENAMENTO
    // =========================================================

    private fun configurarArmazenamento() {

        storageButton.setOnClickListener {

            alternarArmazenamento()
        }
    }

    private fun alternarArmazenamento() {

        /*
         * Descobre o cartão SD antes de trocar.
         */
        val sdRoot = obterRaizCartaoSd()

        if (!usandoCartaoSd) {

            /*
             * Está no interno.
             * Tenta mudar para o cartão SD.
             */
            if (sdRoot == null) {

                pathText.text =
                    "Cartão SD não disponível"

                return
            }

            usandoCartaoSd = true

            atualizarBotaoArmazenamento()

            /*
             * Se já existe histórico do SD,
             * volta para onde o usuário estava.
             *
             * Caso contrário, abre a raiz.
             */
            if (sdHistory.isEmpty()) {

                sdHistory.add(sdRoot)
                sdIndex = 0

                atualizarLista(sdRoot)

            } else {

                if (sdIndex < 0) {
                    sdHistory.add(sdRoot)
                    sdIndex = 0
                }

                val atual =
                    sdHistory[sdIndex]

                atualizarLista(atual)
            }

        } else {

            /*
             * Está no cartão SD.
             * Volta para o armazenamento interno.
             */
            usandoCartaoSd = false

            atualizarBotaoArmazenamento()

            val root =
                Environment
                    .getExternalStorageDirectory()

            if (internalHistory.isEmpty()) {

                internalHistory.add(root)
                internalIndex = 0

                atualizarLista(root)

            } else {

                if (internalIndex < 0) {
                    internalHistory.add(root)
                    internalIndex = 0
                }

                val atual =
                    internalHistory[internalIndex]

                atualizarLista(atual)
            }
        }
    }

    private fun atualizarBotaoArmazenamento() {

        if (usandoCartaoSd) {

            storageButton.setImageResource(
                R.drawable.ic_sd_card
            )

            storageButton.contentDescription =
                "Cartão SD. Toque para trocar para o armazenamento interno"

        } else {

            storageButton.setImageResource(
                R.drawable.ic_storage_internal
            )

            storageButton.contentDescription =
                "Armazenamento interno. Toque para trocar para o cartão SD"
        }
    }

    // =========================================================
    // ENCONTRAR CARTÃO SD
    // =========================================================

    private fun obterRaizCartaoSd(): File? {

        /*
         * O Android normalmente expõe os volumes externos
         * através de getExternalFilesDirs().
         *
         * Procuramos o volume que não corresponde ao
         * armazenamento externo principal.
         */
        val volumes =
            getExternalFilesDirs(null)

        for (volume in volumes) {

            if (volume == null) {
                continue
            }

            val caminho =
                volume.absolutePath

            val caminhoInterno =
                getExternalFilesDir(null)
                    ?.absolutePath

            if (
                caminhoInterno != null &&
                caminho.startsWith(caminhoInterno)
            ) {
                continue
            }

            /*
             * Normalmente:
             *
             * /storage/XXXX-XXXX/Android/data/com.mulheres/files
             *
             * Queremos:
             *
             * /storage/XXXX-XXXX
             */
            val marker =
                "/Android/"

            val posicao =
                caminho.indexOf(marker)

            if (posicao > 0) {

                val raiz =
                    caminho.substring(
                        0,
                        posicao
                    )

                val file =
                    File(raiz)

                if (
                    file.exists() &&
                    file.isDirectory
                ) {
                    return file
                }
            }
        }

        /*
         * Segunda tentativa para alguns aparelhos.
         */
        val diretorio =
            File("/storage")

        val arquivos =
            diretorio.listFiles()

        if (arquivos != null) {

            for (arquivo in arquivos) {

                if (!arquivo.isDirectory) {
                    continue
                }

                if (
                    arquivo.name.equals(
                        "emulated",
                        ignoreCase = true
                    )
                ) {
                    continue
                }

                if (
                    arquivo.name.equals(
                        "self",
                        ignoreCase = true
                    )
                ) {
                    continue
                }

                if (arquivo.canRead()) {
                    return arquivo
                }
            }
        }

        return null
    }

    // =========================================================
    // RECYCLER
    // =========================================================

    private fun configurarRecycler() {

        recycler.layoutManager =
            LinearLayoutManager(this)

        adapter = FolderAdapter { file ->
            abrirArquivoOuPasta(file)
        }

        recycler.adapter = adapter
    }

    // =========================================================
    // BOTÃO VOLTAR
    // =========================================================

    private fun configurarBack() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    val indiceAtual =
                        if (usandoCartaoSd) {
                            sdIndex
                        } else {
                            internalIndex
                        }

                    if (indiceAtual > 0) {

                        voltarDiretorio()

                    } else {

                        finish()
                    }
                }
            }
        )
    }

    // =========================================================
    // INÍCIO
    // =========================================================

    private fun iniciar() {

        val root =
            Environment
                .getExternalStorageDirectory()

        usandoCartaoSd = false

        internalHistory.clear()

        internalIndex = -1

        abrirDiretorioInicial(root)
    }

    private fun abrirDiretorioInicial(
        file: File
    ) {

        if (!usandoCartaoSd) {

            internalHistory.clear()
            internalIndex = -1

        } else {

            sdHistory.clear()
            sdIndex = -1
        }

        abrirDiretorio(file)
    }

    // =========================================================
    // ABRIR PASTA OU ARQUIVO
    // =========================================================

    private fun abrirArquivoOuPasta(
        file: File
    ) {

        if (file.isDirectory) {

            abrirDiretorio(file)

        } else {

            abrirExterno(file)
        }
    }

    // =========================================================
    // ABRIR DIRETÓRIO
    // =========================================================

    private fun abrirDiretorio(
        file: File
    ) {

        if (!file.isDirectory) {
            return
        }

        val history =
            if (usandoCartaoSd) {
                sdHistory
            } else {
                internalHistory
            }

        var currentIndex =
            if (usandoCartaoSd) {
                sdIndex
            } else {
                internalIndex
            }

        if (currentIndex < 0) {

            history.add(file)

            currentIndex =
                history.lastIndex

        } else {

            if (currentIndex < history.size - 1) {

                history.subList(
                    currentIndex + 1,
                    history.size
                ).clear()
            }

            if (
                history[currentIndex].absolutePath ==
                file.absolutePath
            ) {

                atualizarLista(file)

                return
            }

            history.add(file)

            currentIndex =
                history.lastIndex
        }

        if (usandoCartaoSd) {
            sdIndex = currentIndex
        } else {
            internalIndex = currentIndex
        }

        atualizarLista(file)
    }

    // =========================================================
    // ATUALIZAR LISTA
    // =========================================================

    private fun atualizarLista(
        directory: File
    ) {

        val files =
            directory
                .listFiles()
                ?.toList()
                ?: emptyList()

        val collator =
            Collator.getInstance(
                Locale("pt", "BR")
            )

        val sorted =
            files.sortedWith(
                Comparator { a, b ->

                    if (
                        a.isDirectory &&
                        !b.isDirectory
                    ) {
                        return@Comparator -1
                    }

                    if (
                        !a.isDirectory &&
                        b.isDirectory
                    ) {
                        return@Comparator 1
                    }

                    collator.compare(
                        a.name,
                        b.name
                    )
                }
            )

        adapter.update(sorted)

        atualizarCaminho(directory)

        atualizarContador(sorted.size)
    }

    // =========================================================
    // CONTADOR
    // =========================================================

    private fun atualizarContador(
        quantidade: Int
    ) {

        itemCount.text =
            when (quantidade) {

                0 -> ""

                1 -> "1 item"

                else -> "$quantidade itens"
            }
    }

    // =========================================================
    // CAMINHO
    // =========================================================

    private fun atualizarCaminho(
        directory: File
    ) {

        pathText.text =
            obterCaminhoBonito(directory)
    }

    private fun obterCaminhoBonito(
        directory: File
    ): String {

        val currentPath =
            directory.absolutePath

        /*
         * CARTÃO SD
         */
        if (usandoCartaoSd) {

            val sdRoot =
                obterRaizCartaoSd()

            if (sdRoot != null) {

                val sdPath =
                    sdRoot.absolutePath

                if (currentPath == sdPath) {

                    return "Cartão SD"
                }

                if (
                    currentPath.startsWith(
                        "$sdPath/"
                    )
                ) {

                    val relativo =
                        currentPath
                            .removePrefix(sdPath)
                            .trim('/')

                    return if (
                        relativo.isEmpty()
                    ) {
                        "Cartão SD"
                    } else {
                        "Cartão SD / $relativo"
                    }
                }
            }

            return directory.name
        }

        /*
         * ARMAZENAMENTO INTERNO
         */
        val root =
            Environment
                .getExternalStorageDirectory()

        val rootPath =
            root.absolutePath

        if (currentPath == rootPath) {
            return "Armazenamento interno"
        }

        return if (
            currentPath.startsWith(
                "$rootPath/"
            )
        ) {

            val relativo =
                currentPath
                    .removePrefix(rootPath)
                    .trim('/')

            if (relativo.isEmpty()) {

                "Armazenamento interno"

            } else {

                "Armazenamento interno / $relativo"
            }

        } else {

            directory.name
        }
    }

    // =========================================================
    // AVANÇAR
    // =========================================================

    private fun avancarDiretorio() {

        val history =
            if (usandoCartaoSd) {
                sdHistory
            } else {
                internalHistory
            }

        var currentIndex =
            if (usandoCartaoSd) {
                sdIndex
            } else {
                internalIndex
            }

        if (
            currentIndex >=
            history.size - 1
        ) {
            return
        }

        currentIndex++

        if (usandoCartaoSd) {
            sdIndex = currentIndex
        } else {
            internalIndex = currentIndex
        }

        val directory =
            history[currentIndex]

        if (directory.isDirectory) {
            atualizarLista(directory)
        }
    }

    // =========================================================
    // VOLTAR DIRETÓRIO
    // =========================================================

    private fun voltarDiretorio() {

        var currentIndex =
            if (usandoCartaoSd) {
                sdIndex
            } else {
                internalIndex
            }

        if (currentIndex <= 0) {
            return
        }

        currentIndex--

        if (usandoCartaoSd) {
            sdIndex = currentIndex
        } else {
            internalIndex = currentIndex
        }

        val history =
            if (usandoCartaoSd) {
                sdHistory
            } else {
                internalHistory
            }

        val directory =
            history[currentIndex]

        if (directory.isDirectory) {
            atualizarLista(directory)
        }
    }

    // =========================================================
    // ABRIR ARQUIVO
    // =========================================================

    private fun abrirExterno(
        file: File
    ) {

        try {

            val uri =
                androidx.core.content.FileProvider
                    .getUriForFile(
                        this,
                        "$packageName.provider",
                        file
                    )

            val extensao =
                file.extension
                    .lowercase(Locale.ROOT)

            val mime =
                MimeTypeMap
                    .getSingleton()
                    .getMimeTypeFromExtension(
                        extensao
                    )
                    ?: "*/*"

            val intent =
                Intent(
                    Intent.ACTION_VIEW
                ).apply {

                    setDataAndType(
                        uri,
                        mime
                    )

                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

            startActivity(
                Intent.createChooser(
                    intent,
                    "Abrir com"
                )
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // =========================================================
    // PERMISSÕES
    // =========================================================

    private fun pedirPermissao() {

        if (Build.VERSION.SDK_INT >= 30) {

            if (
                !Environment
                    .isExternalStorageManager()
            ) {

                val intent =
                    Intent(
                        Settings
                            .ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse(
                            "package:$packageName"
                        )
                    )

                startActivityForResult(
                    intent,
                    MANAGE_STORAGE_CODE
                )

            } else {

                iniciar()
            }

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission
                        .READ_EXTERNAL_STORAGE
                ),
                PERMISSION_CODE
            )
        }
    }

    private fun temPermissao(): Boolean {

        return if (
            Build.VERSION.SDK_INT >= 30
        ) {

            Environment
                .isExternalStorageManager()

        } else {

            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission
                    .READ_EXTERNAL_STORAGE
            ) ==
                android.content.pm.PackageManager
                    .PERMISSION_GRANTED
        }
    }

    // =========================================================
    // RESULTADO DA PERMISSÃO
    // =========================================================

    @Deprecated(
        "Compatibilidade com versões antigas"
    )
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode ==
            MANAGE_STORAGE_CODE &&
            temPermissao()
        ) {

            iniciar()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            android.content.pm.PackageManager
                .PERMISSION_GRANTED
        ) {

            iniciar()
        }
    }

    // =========================================================
    // FONTE
    // =========================================================

    private fun aplicarFonte(
        view: View
    ) {

        val fonte = try {

            Typeface.createFromAsset(
                assets,
                "font.ttf"
            )

        } catch (e: Exception) {

            Typeface.DEFAULT
        }

        if (view is TextView) {
            view.typeface = fonte
        }

        if (view is ViewGroup) {

            for (
                i in 0 until view.childCount
            ) {

                aplicarFonte(
                    view.getChildAt(i)
                )
            }
        }
    }
}