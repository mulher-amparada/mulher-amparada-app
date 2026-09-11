package com.mulheres

import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
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

    private enum class TipoArmazenamento {
        INTERNO,
        SD,
        USB
    }

    private lateinit var adapter: FolderAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var pathText: TextView
    private lateinit var itemCount: TextView
    private lateinit var storageButton: ImageButton

    private val internalHistory = ArrayList<File>()
    private val externalHistories = HashMap<String, ArrayList<File>>()
    private val externalIndexes = HashMap<String, Int>()

    private var usandoArmazenamentoExterno = false

    private var volumesExternos = ArrayList<File>()
    private var indiceVolumeExterno = 0
    private var internalIndex = -1

    /*
     * Os três modos ficam sempre disponíveis:
     *
     * 0 = interno
     * 1 = SD
     * 2 = USB
     *
     * Se SD ou USB não estiverem conectados,
     * o botão continua existindo, mas a tentativa
     * de entrar naquele armazenamento simplesmente
     * volta para o próximo modo disponível.
     */
    private var modoArmazenamento = 0

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

        aplicarFonte(
            findViewById(android.R.id.content)
        )

        recycler = findViewById(R.id.recycler)
        pathText = findViewById(R.id.pathText)
        itemCount = findViewById(R.id.itemCount)
        storageButton = findViewById(R.id.storageButton)

        configurarRecycler()
        configurarBack()
        configurarArmazenamento()

        atualizarVolumesExternos()

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

            atualizarVolumesExternos()

            alternarArmazenamento()
        }

        atualizarBotaoArmazenamento()
    }

    /*
     * Procura os volumes removíveis montados.
     *
     * Não usamos a existência deles para decidir
     * qual ícone mostrar.
     *
     * O USB continua sendo um modo permanente.
     */
    private fun atualizarVolumesExternos() {

        val encontrados =
            LinkedHashMap<String, File>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            try {

                val storageManager =
                    getSystemService(
                        StorageManager::class.java
                    )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    for (
                        volume in storageManager.storageVolumes
                    ) {

                        val directory =
                            volume.directory
                                ?: continue

                        if (
                            !directory.exists() ||
                            !directory.isDirectory ||
                            !directory.canRead()
                        ) {
                            continue
                        }

                        val path =
                            obterCaminhoSeguro(directory)

                        if (
                            path.contains(
                                "/emulated/",
                                ignoreCase = true
                            ) ||
                            path.equals(
                                "/storage/emulated",
                                ignoreCase = true
                            )
                        ) {
                            continue
                        }

                        val interno =
                            obterCaminhoSeguro(
                                Environment.getExternalStorageDirectory()
                            )

                        if (path == interno) {
                            continue
                        }

                        encontrados[path] = directory
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /*
         * Fallback /storage.
         */
        try {

            val storage = File("/storage")
            val arquivos = storage.listFiles()

            if (arquivos != null) {

                val interno =
                    obterCaminhoSeguro(
                        Environment.getExternalStorageDirectory()
                    )

                for (arquivo in arquivos) {

                    if (!arquivo.isDirectory) {
                        continue
                    }

                    val nome =
                        arquivo.name.lowercase(Locale.ROOT)

                    if (
                        nome == "emulated" ||
                        nome == "self"
                    ) {
                        continue
                    }

                    if (!arquivo.canRead()) {
                        continue
                    }

                    val caminho =
                        obterCaminhoSeguro(arquivo)

                    if (caminho == interno) {
                        continue
                    }

                    encontrados[caminho] = arquivo
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        volumesExternos =
            ArrayList(
                encontrados.values
                    .filter {
                        it.exists() &&
                        it.isDirectory &&
                        it.canRead()
                    }
                    .sortedBy {
                        it.absolutePath
                    }
            )

        if (
            indiceVolumeExterno >=
            volumesExternos.size
        ) {
            indiceVolumeExterno = 0
        }
    }

    // =========================================================
    // IDENTIFICAR VOLUME
    // =========================================================

    private fun identificarTipoArmazenamento(
        root: File
    ): TipoArmazenamento {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            try {

                val storageManager =
                    getSystemService(
                        StorageManager::class.java
                    )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    val volume =
                        storageManager.storageVolumes
                            .firstOrNull {

                                val directory =
                                    it.directory

                                directory != null &&
                                obterCaminhoSeguro(
                                    directory
                                ) ==
                                obterCaminhoSeguro(
                                    root
                                )
                            }

                    if (volume != null) {

                        val descricao =
                            volume
                                .getDescription(this)
                                ?.lowercase(Locale.ROOT)
                                ?: ""

                        if (
                            descricao.contains("usb") ||
                            descricao.contains("pendrive") ||
                            descricao.contains("pen drive") ||
                            descricao.contains("flash drive") ||
                            descricao.contains("usb drive") ||
                            descricao.contains("otg")
                        ) {
                            return TipoArmazenamento.USB
                        }

                        if (
                            descricao.contains("sd") ||
                            descricao.contains("cartão") ||
                            descricao.contains("cartao") ||
                            descricao.contains("memory card") ||
                            descricao.contains("card")
                        ) {
                            return TipoArmazenamento.SD
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val caminho =
            obterCaminhoSeguro(root)
                .lowercase(Locale.ROOT)

        val nome =
            root.name.lowercase(Locale.ROOT)

        if (
            caminho.contains("usb") ||
            caminho.contains("otg") ||
            caminho.contains("pendrive") ||
            nome.contains("usb") ||
            nome.contains("otg") ||
            nome.contains("pendrive")
        ) {
            return TipoArmazenamento.USB
        }

        return TipoArmazenamento.SD
    }

    private fun obterCaminhoSeguro(
        file: File
    ): String {

        return try {
            file.canonicalPath
        } catch (e: Exception) {
            file.absolutePath
        }
    }

    // =========================================================
    // NOME
    // =========================================================

    private fun obterNomeVolume(
        root: File
    ): String {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    val storageManager =
                        getSystemService(
                            StorageManager::class.java
                        )

                    val volume =
                        storageManager.storageVolumes
                            .firstOrNull {

                                val directory =
                                    it.directory

                                directory != null &&
                                obterCaminhoSeguro(
                                    directory
                                ) ==
                                obterCaminhoSeguro(
                                    root
                                )
                            }

                    val descricao =
                        volume?.getDescription(this)

                    if (!descricao.isNullOrBlank()) {
                        return descricao
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return root.name.ifBlank {
            "Armazenamento externo"
        }
    }

    // =========================================================
    // BOTÃO
    // =========================================================

    private fun atualizarBotaoArmazenamento() {

        /*
         * IMPORTANTE:
         *
         * O ícone não depende de o USB estar conectado.
         *
         * 0 = interno
         * 1 = SD
         * 2 = USB
         */
        when (modoArmazenamento) {

            0 -> {

                storageButton.setImageResource(
                    R.drawable.ic_storage_internal
                )

                storageButton.contentDescription =
                    "Armazenamento interno. Toque para trocar"
            }

            1 -> {

                storageButton.setImageResource(
                    R.drawable.ic_sd_card
                )

                storageButton.contentDescription =
                    "Cartão SD. Toque para trocar"
            }

            2 -> {

                storageButton.setImageResource(
                    R.drawable.ic_usb
                )

                storageButton.contentDescription =
                    "Pendrive USB OTG. Toque para trocar"
            }
        }
    }

    // =========================================================
    // ALTERNAR
    // =========================================================

    private fun alternarArmazenamento() {

        /*
         * Sempre percorre:
         *
         * INTERNO → SD → USB → INTERNO
         *
         * Mesmo que o USB não esteja conectado,
         * o modo USB continua existindo.
         */
        modoArmazenamento++

        if (modoArmazenamento > 2) {
            modoArmazenamento = 0
        }

        atualizarBotaoArmazenamento()

        when (modoArmazenamento) {

            0 -> {

                usandoArmazenamentoExterno = false

                abrirInternoAtual()
            }

            1 -> {

                usandoArmazenamentoExterno = true

                abrirSDAtual()
            }

            2 -> {

                usandoArmazenamentoExterno = true

                abrirUSBAtual()
            }
        }
    }

    // =========================================================
    // ENCONTRAR SD
    // =========================================================

    private fun encontrarVolumeSD(): File? {

        atualizarVolumesExternos()

        return volumesExternos.firstOrNull {
            identificarTipoArmazenamento(it) ==
                    TipoArmazenamento.SD
        }
    }

    // =========================================================
    // ENCONTRAR USB
    // =========================================================

    private fun encontrarVolumeUSB(): File? {

        atualizarVolumesExternos()

        return volumesExternos.firstOrNull {
            identificarTipoArmazenamento(it) ==
                    TipoArmazenamento.USB
        }
    }

    // =========================================================
    // ABRIR SD
    // =========================================================

    private fun abrirSDAtual() {

        val root =
            encontrarVolumeSD()

        if (root == null) {

            pathText.text =
                "Cartão SD não conectado"

            atualizarContador(0)

            return
        }

        abrirVolumeExterno(root)
    }

    // =========================================================
    // ABRIR USB
    // =========================================================

    private fun abrirUSBAtual() {

        val root =
            encontrarVolumeUSB()

        if (root == null) {

            pathText.text =
                "Pendrive USB não conectado"

            atualizarContador(0)

            return
        }

        abrirVolumeExterno(root)
    }

    // =========================================================
    // ABRIR VOLUME EXTERNO
    // =========================================================

    private fun abrirVolumeExterno(
        root: File
    ) {

        val chave =
            obterCaminhoSeguro(root)

        val history =
            externalHistories.getOrPut(
                chave
            ) {
                ArrayList()
            }

        var index =
            externalIndexes[chave] ?: -1

        if (history.isEmpty()) {

            history.add(root)
            index = 0

        } else if (
            index < 0 ||
            index >= history.size
        ) {

            history.clear()
            history.add(root)
            index = 0
        }

        externalIndexes[chave] = index

        val atual =
            history[index]

        if (
            atual.exists() &&
            atual.isDirectory
        ) {

            atualizarLista(atual)

        } else {

            history.clear()
            history.add(root)

            externalIndexes[chave] = 0

            atualizarLista(root)
        }
    }

    // =========================================================
    // HISTÓRICO
    // =========================================================

    private fun obterHistoricoAtual():
        ArrayList<File> {

        if (!usandoArmazenamentoExterno) {
            return internalHistory
        }

        /*
         * Descobre o volume correspondente ao modo atual.
         */
        val root =
            when (modoArmazenamento) {

                1 -> encontrarVolumeSD()
                2 -> encontrarVolumeUSB()
                else -> null
            }

        if (root == null) {
            return ArrayList()
        }

        return externalHistories.getOrPut(
            obterCaminhoSeguro(root)
        ) {
            ArrayList()
        }
    }

    private fun obterIndiceAtual(): Int {

        if (!usandoArmazenamentoExterno) {
            return internalIndex
        }

        val root =
            when (modoArmazenamento) {

                1 -> encontrarVolumeSD()
                2 -> encontrarVolumeUSB()
                else -> null
            } ?: return -1

        return externalIndexes[
            obterCaminhoSeguro(root)
        ] ?: -1
    }

    private fun definirIndiceAtual(
        index: Int
    ) {

        if (!usandoArmazenamentoExterno) {

            internalIndex = index

        } else {

            val root =
                when (modoArmazenamento) {

                    1 -> encontrarVolumeSD()
                    2 -> encontrarVolumeUSB()
                    else -> null
                } ?: return

            externalIndexes[
                obterCaminhoSeguro(root)
            ] = index
        }
    }

    // =========================================================
    // RECYCLER
    // =========================================================

    private fun configurarRecycler() {

        recycler.layoutManager =
            LinearLayoutManager(this)

        adapter =
            FolderAdapter { file ->
                abrirArquivoOuPasta(file)
            }

        recycler.adapter = adapter
    }

    // =========================================================
    // VOLTAR
    // =========================================================

    private fun configurarBack() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    val indice =
                        obterIndiceAtual()

                    if (indice > 0) {

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

        atualizarVolumesExternos()

        modoArmazenamento = 0
        usandoArmazenamentoExterno = false

        internalHistory.clear()
        internalIndex = -1

        atualizarBotaoArmazenamento()

        abrirInternoAtual()
    }

    private fun abrirInternoAtual() {

        val root =
            Environment.getExternalStorageDirectory()

        if (internalHistory.isEmpty()) {

            internalHistory.add(root)
            internalIndex = 0

        } else if (
            internalIndex < 0 ||
            internalIndex >= internalHistory.size
        ) {

            internalHistory.clear()
            internalHistory.add(root)
            internalIndex = 0
        }

        val atual =
            internalHistory[internalIndex]

        if (
            atual.exists() &&
            atual.isDirectory
        ) {

            atualizarLista(atual)

        } else {

            internalHistory.clear()
            internalHistory.add(root)
            internalIndex = 0

            atualizarLista(root)
        }
    }

    // =========================================================
    // ARQUIVO OU PASTA
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
    // DIRETÓRIO
    // =========================================================

    private fun abrirDiretorio(
        file: File
    ) {

        if (!file.isDirectory) {
            return
        }

        val history =
            obterHistoricoAtual()

        var currentIndex =
            obterIndiceAtual()

        if (currentIndex < 0) {

            history.add(file)
            currentIndex = history.lastIndex

        } else {

            if (
                currentIndex <
                history.size - 1
            ) {

                history.subList(
                    currentIndex + 1,
                    history.size
                ).clear()
            }

            if (
                currentIndex < history.size &&
                obterCaminhoSeguro(
                    history[currentIndex]
                ) ==
                obterCaminhoSeguro(file)
            ) {

                atualizarLista(file)
                return
            }

            history.add(file)
            currentIndex = history.lastIndex
        }

        definirIndiceAtual(currentIndex)

        atualizarLista(file)
    }

    // =========================================================
    // LISTA
    // =========================================================

    private fun atualizarLista(
        directory: File
    ) {

        if (
            !directory.exists() ||
            !directory.isDirectory
        ) {

            adapter.update(emptyList())
            atualizarContador(0)

            return
        }

        val files =
            try {

                directory
                    .listFiles()
                    ?.filter {
                        it.exists()
                    }
                    ?.toList()
                    ?: emptyList()

            } catch (e: Exception) {

                emptyList()
            }

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

                else ->
                    "$quantidade itens"
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

        if (!usandoArmazenamentoExterno) {

            val root =
                Environment
                    .getExternalStorageDirectory()

            val currentPath =
                obterCaminhoSeguro(directory)

            val rootPath =
                obterCaminhoSeguro(root)

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

                "Armazenamento interno / $relativo"

            } else {

                directory.name
            }
        }

        val root =
            when (modoArmazenamento) {

                1 -> encontrarVolumeSD()
                2 -> encontrarVolumeUSB()
                else -> null
            }

        if (root == null) {
            return when (modoArmazenamento) {
                1 -> "Cartão SD não conectado"
                2 -> "Pendrive USB não conectado"
                else -> "Armazenamento"
            }
        }

        val rootPath =
            obterCaminhoSeguro(root)

        val currentPath =
            obterCaminhoSeguro(directory)

        val nome =
            obterNomeVolume(root)

        if (currentPath == rootPath) {
            return nome
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

            "$nome / $relativo"

        } else {

            directory.name
        }
    }

    // =========================================================
    // AVANÇAR
    // =========================================================

    private fun avancarDiretorio() {

        val history =
            obterHistoricoAtual()

        var currentIndex =
            obterIndiceAtual()

        if (
            currentIndex >=
            history.size - 1
        ) {
            return
        }

        currentIndex++

        definirIndiceAtual(currentIndex)

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
            obterIndiceAtual()

        if (currentIndex <= 0) {
            return
        }

        currentIndex--

        definirIndiceAtual(currentIndex)

        val history =
            obterHistoricoAtual()

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

        /*
         * Não usa canRead() como requisito para criar
         * a URI. Em armazenamento USB removível,
         * algumas implementações do Android podem
         * retornar informações inconsistentes nesse
         * ponto mesmo que o arquivo esteja acessível.
         */
        if (!file.exists() || !file.isFile) {

            pathText.text =
                "Arquivo não encontrado"

            return
        }

        try {

            val arquivoReal =
                try {
                    file.canonicalFile
                } catch (e: Exception) {
                    file.absoluteFile
                }

            if (
                !arquivoReal.exists() ||
                !arquivoReal.isFile
            ) {

                pathText.text =
                    "Arquivo não encontrado"

                return
            }

            val extensao =
                arquivoReal.extension
                    .lowercase(Locale.ROOT)

            val mime =
                MimeTypeMap
                    .getSingleton()
                    .getMimeTypeFromExtension(
                        extensao
                    )
                    ?: "application/octet-stream"

            val uri =
                SdFileProvider.getUriForFile(
                    "$packageName.provider",
                    arquivoReal
                )

            val intent =
                Intent(
                    Intent.ACTION_VIEW
                ).apply {

                    setDataAndType(
                        uri,
                        mime
                    )

                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    )

                    clipData =
                        ClipData.newRawUri(
                            arquivoReal.name,
                            uri
                        )
                }

            /*
             * Não usa createChooser().
             *
             * Assim o Android pode mostrar:
             *
             * "Só uma vez"
             * "Sempre"
             *
             * quando o sistema/app receptor oferecer
             * essa escolha.
             */
            startActivity(intent)

        } catch (e: android.content.ActivityNotFoundException) {

            pathText.text =
                "Nenhum aplicativo pode abrir este arquivo"

        } catch (e: SecurityException) {

            e.printStackTrace()

            pathText.text =
                "O Android não permitiu abrir este arquivo"

        } catch (e: Exception) {

            e.printStackTrace()

            pathText.text =
                "Não foi possível abrir o arquivo"
        }
    }

    // =========================================================
    // PERMISSÕES
    // =========================================================

    private fun pedirPermissao() {

        if (Build.VERSION.SDK_INT >= 30) {

            if (
                !Environment.isExternalStorageManager()
            ) {

                val intent =
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
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

        return if (Build.VERSION.SDK_INT >= 30) {

            Environment.isExternalStorageManager()

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
    // RESULTADO PERMISSÃO
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
            requestCode ==
            PERMISSION_CODE &&
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

        val fonte =
            try {

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