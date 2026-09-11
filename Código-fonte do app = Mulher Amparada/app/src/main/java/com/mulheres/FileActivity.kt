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
import android.os.storage.StorageVolume
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
        USB,
        OUTRO
    }

    private lateinit var adapter: FolderAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var pathText: TextView
    private lateinit var itemCount: TextView
    private lateinit var storageButton: ImageButton

    private val internalHistory =
        ArrayList<File>()

    private val externalHistories =
        HashMap<String, ArrayList<File>>()

    private val externalIndexes =
        HashMap<String, Int>()

    private var usandoArmazenamentoExterno = false

    private var volumesExternos =
        ArrayList<File>()

    private var indiceVolumeExterno = 0

    private var internalIndex = -1

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        configurarSistema()

        setContentView(
            R.layout.activity_file
        )

        val raiz =
            findViewById<View>(
                android.R.id.content
            )

        aplicarFonte(raiz)

        recycler =
            findViewById(R.id.recycler)

        pathText =
            findViewById(R.id.pathText)

        itemCount =
            findViewById(R.id.itemCount)

        storageButton =
            findViewById(R.id.storageButton)

        configurarRecycler()
        configurarBack()
        configurarArmazenamento()

        atualizarVolumesExternos()
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

        window.statusBarColor =
            Color.TRANSPARENT

        window.navigationBarColor =
            Color.TRANSPARENT

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {

            window.isNavigationBarContrastEnforced =
                false
        }

        val controller =
            WindowInsetsControllerCompat(
                window,
                window.decorView
            )

        controller.isAppearanceLightStatusBars =
            false

        controller.isAppearanceLightNavigationBars =
            false
    }

    // =========================================================
    // ARMAZENAMENTO
    // =========================================================

    private fun configurarArmazenamento() {

        storageButton.setOnClickListener {

            atualizarVolumesExternos()

            alternarArmazenamento()
        }
    }

    /*
     * Procura todos os volumes removíveis montados.
     *
     * Pode encontrar:
     *
     * - cartão SD
     * - pendrive USB OTG
     * - outros volumes removíveis
     */
    private fun atualizarVolumesExternos() {

        val encontrados =
            LinkedHashMap<String, File>()

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.N
        ) {

            try {

                val storageManager =
                    getSystemService(
                        StorageManager::class.java
                    )

                val volumes =
                    storageManager.storageVolumes

                for (volume in volumes) {

                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.R
                    ) {

                        val directory =
                            volume.directory

                        if (
                            directory != null &&
                            directory.exists() &&
                            directory.isDirectory &&
                            directory.canRead()
                        ) {

                            val path =
                                try {
                                    directory.canonicalPath
                                } catch (e: Exception) {
                                    directory.absolutePath
                                }

                            val interno =
                                Environment
                                    .getExternalStorageDirectory()
                                    .canonicalPath

                            if (
                                path != interno &&
                                !path.contains(
                                    "/emulated/",
                                    ignoreCase = true
                                ) &&
                                !path.equals(
                                    "/storage/emulated",
                                    ignoreCase = true
                                )
                            ) {

                                encontrados[path] =
                                    directory
                            }
                        }
                    }
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }

        /*
         * Fallback para aparelhos que expõem o volume
         * diretamente em /storage.
         */
        try {

            val storage =
                File("/storage")

            val arquivos =
                storage.listFiles()

            if (arquivos != null) {

                val interno =
                    Environment
                        .getExternalStorageDirectory()
                        .canonicalPath

                for (arquivo in arquivos) {

                    if (!arquivo.isDirectory) {
                        continue
                    }

                    val nome =
                        arquivo.name
                            .lowercase(Locale.ROOT)

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
                        try {
                            arquivo.canonicalPath
                        } catch (e: Exception) {
                            arquivo.absolutePath
                        }

                    if (caminho == interno) {
                        continue
                    }

                    encontrados[caminho] =
                        arquivo
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }

        val novos =
            encontrados.values
                .filter {
                    it.exists() &&
                    it.isDirectory &&
                    it.canRead()
                }
                .sortedBy {
                    it.absolutePath
                }

        volumesExternos =
            ArrayList(novos)

        if (
            volumesExternos.isEmpty()
        ) {

            indiceVolumeExterno = 0

        } else if (
            indiceVolumeExterno >=
            volumesExternos.size
        ) {

            indiceVolumeExterno = 0
        }
    }

    // =========================================================
    // TIPO DO ARMAZENAMENTO
    // =========================================================

    /*
     * Identifica o tipo do volume.
     *
     * A API do Android não fornece em todos os aparelhos
     * uma propriedade universal "USB" ou "SD".
     *
     * Por isso:
     *
     * 1. procura informações do StorageVolume;
     * 2. verifica a descrição fornecida pelo sistema;
     * 3. usa o caminho como auxílio;
     * 4. se não conseguir diferenciar, retorna OUTRO.
     */
    private fun identificarTipoArmazenamento(
        root: File
    ): TipoArmazenamento {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.N
        ) {

            try {

                val storageManager =
                    getSystemService(
                        StorageManager::class.java
                    )

                val volume =
                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.R
                    ) {

                        storageManager
                            .storageVolumes
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

                    } else {

                        null
                    }

                if (volume != null) {

                    val descricao =
                        volume
                            .getDescription(this)
                            ?.lowercase(
                                Locale.ROOT
                            )
                            ?: ""

                    /*
                     * Algumas versões da Samsung/Android
                     * informam USB, drive, pendrive etc.
                     * na descrição.
                     */
                    if (
                        descricao.contains("usb") ||
                        descricao.contains("pendrive") ||
                        descricao.contains("pen drive") ||
                        descricao.contains("flash drive") ||
                        descricao.contains("usb drive")
                    ) {

                        return TipoArmazenamento.USB
                    }

                    /*
                     * Descrições que normalmente indicam
                     * cartão removível.
                     */
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

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }

        /*
         * Heurística adicional.
         *
         * Não considera qualquer /storage/XXXX-XXXX
         * automaticamente como SD, pois USB OTG também
         * costuma aparecer dessa maneira.
         */
        val caminho =
            obterCaminhoSeguro(root)
                .lowercase(Locale.ROOT)

        val nome =
            root.name
                .lowercase(Locale.ROOT)

        if (
            caminho.contains("usb") ||
            nome.contains("usb") ||
            caminho.contains("otg") ||
            nome.contains("otg") ||
            caminho.contains("pendrive") ||
            nome.contains("pendrive")
        ) {

            return TipoArmazenamento.USB
        }

        /*
         * Se não foi possível diferenciar, mantém
         * como armazenamento removível genérico.
         */
        return TipoArmazenamento.OUTRO
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
    // NOME DO VOLUME
    // =========================================================

    private fun obterNomeVolume(
        root: File
    ): String {

        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.N
            ) {

                val storageManager =
                    getSystemService(
                        StorageManager::class.java
                    )

                val volume =
                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.R
                    ) {

                        storageManager
                            .storageVolumes
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

                    } else {

                        null
                    }

                if (volume != null) {

                    val descricao =
                        volume.getDescription(this)

                    if (
                        !descricao.isNullOrBlank()
                    ) {

                        return descricao
                    }
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }

        return root.name.ifBlank {
            "Armazenamento externo"
        }
    }

    // =========================================================
    // ÍCONE DO ARMAZENAMENTO
    // =========================================================

    private fun atualizarBotaoArmazenamento() {

        if (
            usandoArmazenamentoExterno
        ) {

            val volume =
                obterArmazenamentoExternoAtual()

            if (volume == null) {

                storageButton.setImageResource(
                    R.drawable.ic_sd_card
                )

                storageButton.contentDescription =
                    "Armazenamento externo"

                return
            }

            val tipo =
                identificarTipoArmazenamento(
                    volume
                )

            val nome =
                obterNomeVolume(
                    volume
                )

            when (tipo) {

                TipoArmazenamento.USB -> {

                    storageButton.setImageResource(
                        R.drawable.ic_usb
                    )

                    storageButton.contentDescription =
                        "$nome. Pendrive USB. Toque para trocar de armazenamento"
                }

                TipoArmazenamento.SD -> {

                    storageButton.setImageResource(
                        R.drawable.ic_sd_card
                    )

                    storageButton.contentDescription =
                        "$nome. Cartão SD. Toque para trocar de armazenamento"
                }

                TipoArmazenamento.OUTRO -> {

                    /*
                     * Quando o Android não informa se é
                     * USB ou SD, mantém o ícone de armazenamento
                     * removível genérico.
                     */
                    storageButton.setImageResource(
                        R.drawable.ic_sd_card
                    )

                    storageButton.contentDescription =
                        "$nome. Armazenamento externo. Toque para trocar de armazenamento"
                }

                TipoArmazenamento.INTERNO -> {

                    storageButton.setImageResource(
                        R.drawable.ic_storage_internal
                    )

                    storageButton.contentDescription =
                        "Armazenamento interno. Toque para trocar de armazenamento"
                }
            }

        } else {

            storageButton.setImageResource(
                R.drawable.ic_storage_internal
            )

            if (
                volumesExternos.isNotEmpty()
            ) {

                storageButton.contentDescription =
                    "Armazenamento interno. Toque para trocar de armazenamento"

            } else {

                storageButton.contentDescription =
                    "Armazenamento interno. Nenhum armazenamento externo disponível"
            }
        }
    }

    // =========================================================
    // ARMAZENAMENTO EXTERNO ATUAL
    // =========================================================

    private fun obterArmazenamentoExternoAtual():
        File? {

        if (
            volumesExternos.isEmpty()
        ) {
            return null
        }

        if (
            indiceVolumeExterno < 0 ||
            indiceVolumeExterno >=
            volumesExternos.size
        ) {

            indiceVolumeExterno = 0
        }

        return volumesExternos[
            indiceVolumeExterno
        ]
    }

    // =========================================================
    // ALTERNAR ARMAZENAMENTO
    // =========================================================

    private fun alternarArmazenamento() {

        atualizarVolumesExternos()

        if (
            volumesExternos.isEmpty()
        ) {

            if (
                usandoArmazenamentoExterno
            ) {

                usandoArmazenamentoExterno =
                    false

                atualizarBotaoArmazenamento()

                abrirInternoAtual()

            } else {

                pathText.text =
                    "Nenhum armazenamento externo disponível"
            }

            return
        }

        if (
            !usandoArmazenamentoExterno
        ) {

            usandoArmazenamentoExterno =
                true

            indiceVolumeExterno = 0

            atualizarBotaoArmazenamento()

            abrirExternoAtual()

            return
        }

        if (
            indiceVolumeExterno <
            volumesExternos.lastIndex
        ) {

            indiceVolumeExterno++

            atualizarBotaoArmazenamento()

            abrirExternoAtual()

            return
        }

        indiceVolumeExterno = 0

        usandoArmazenamentoExterno =
            false

        atualizarBotaoArmazenamento()

        abrirInternoAtual()
    }

    // =========================================================
    // ABRIR INTERNO ATUAL
    // =========================================================

    private fun abrirInternoAtual() {

        val root =
            Environment
                .getExternalStorageDirectory()

        if (
            internalHistory.isEmpty()
        ) {

            internalHistory.add(root)

            internalIndex = 0

        } else {

            if (
                internalIndex < 0 ||
                internalIndex >=
                internalHistory.size
            ) {

                internalHistory.clear()

                internalHistory.add(root)

                internalIndex = 0
            }
        }

        val atual =
            internalHistory[
                internalIndex
            ]

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
    // ABRIR EXTERNO ATUAL
    // =========================================================

    private fun abrirExternoAtual() {

        val root =
            obterArmazenamentoExternoAtual()
                ?: return

        val chave =
            root.absolutePath

        val history =
            externalHistories.getOrPut(
                chave
            ) {
                ArrayList()
            }

        var index =
            externalIndexes[
                chave
            ] ?: -1

        if (history.isEmpty()) {

            history.add(root)

            index = 0

        } else {

            if (
                index < 0 ||
                index >= history.size
            ) {

                history.clear()

                history.add(root)

                index = 0
            }
        }

        externalIndexes[
            chave
        ] = index

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

            externalIndexes[
                chave
            ] = 0

            atualizarLista(root)
        }
    }

    // =========================================================
    // HISTÓRICO ATUAL
    // =========================================================

    private fun obterHistoricoAtual():
        ArrayList<File> {

        if (
            !usandoArmazenamentoExterno
        ) {

            return internalHistory
        }

        val root =
            obterArmazenamentoExternoAtual()

        if (root == null) {
            return ArrayList()
        }

        return externalHistories.getOrPut(
            root.absolutePath
        ) {
            ArrayList()
        }
    }

    private fun obterIndiceAtual(): Int {

        if (
            !usandoArmazenamentoExterno
        ) {

            return internalIndex
        }

        val root =
            obterArmazenamentoExternoAtual()
                ?: return -1

        return externalIndexes[
            root.absolutePath
        ] ?: -1
    }

    private fun definirIndiceAtual(
        index: Int
    ) {

        if (
            !usandoArmazenamentoExterno
        ) {

            internalIndex = index

        } else {

            val root =
                obterArmazenamentoExternoAtual()
                    ?: return

            externalIndexes[
                root.absolutePath
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
    // BOTÃO VOLTAR
    // =========================================================

    private fun configurarBack() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    val indiceAtual =
                        obterIndiceAtual()

                    if (
                        indiceAtual > 0
                    ) {

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

        usandoArmazenamentoExterno =
            false

        internalHistory.clear()

        internalIndex = -1

        val root =
            Environment
                .getExternalStorageDirectory()

        abrirDiretorioInicial(root)
    }

    private fun abrirDiretorioInicial(
        file: File
    ) {

        if (
            !usandoArmazenamentoExterno
        ) {

            internalHistory.clear()

            internalIndex = -1

        } else {

            val root =
                obterArmazenamentoExternoAtual()
                    ?: return

            externalHistories[
                root.absolutePath
            ]?.clear()

            externalIndexes[
                root.absolutePath
            ] = -1
        }

        abrirDiretorio(file)
    }

    // =========================================================
    // ABRIR ARQUIVO OU PASTA
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
            obterHistoricoAtual()

        var currentIndex =
            obterIndiceAtual()

        if (currentIndex < 0) {

            history.add(file)

            currentIndex =
                history.lastIndex

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
                currentIndex <
                history.size &&
                history[currentIndex]
                    .absolutePath ==
                file.absolutePath
            ) {

                atualizarLista(file)

                return
            }

            history.add(file)

            currentIndex =
                history.lastIndex
        }

        definirIndiceAtual(
            currentIndex
        )

        atualizarLista(file)
    }

    // =========================================================
    // ATUALIZAR LISTA
    // =========================================================

    private fun atualizarLista(
        directory: File
    ) {

        if (
            !directory.exists() ||
            !directory.isDirectory
        ) {

            adapter.update(
                emptyList()
            )

            atualizarContador(0)

            return
        }

        val files =
            try {

                directory
                    .listFiles()
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

        atualizarCaminho(
            directory
        )

        atualizarContador(
            sorted.size
        )
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
            obterCaminhoBonito(
                directory
            )
    }

    private fun obterCaminhoBonito(
        directory: File
    ): String {

        val currentPath =
            directory.absolutePath

        if (
            usandoArmazenamentoExterno
        ) {

            val root =
                obterArmazenamentoExternoAtual()

            if (root != null) {

                val rootPath =
                    root.absolutePath

                val nome =
                    obterNomeVolume(root)

                if (
                    currentPath ==
                    rootPath
                ) {

                    return nome
                }

                if (
                    currentPath.startsWith(
                        "$rootPath/"
                    )
                ) {

                    val relativo =
                        currentPath
                            .removePrefix(
                                rootPath
                            )
                            .trim('/')

                    return if (
                        relativo.isEmpty()
                    ) {

                        nome

                    } else {

                        "$nome / $relativo"
                    }
                }
            }

            return directory.name
        }

        val root =
            Environment
                .getExternalStorageDirectory()

        val rootPath =
            root.absolutePath

        if (
            currentPath ==
            rootPath
        ) {

            return "Armazenamento interno"
        }

        return if (
            currentPath.startsWith(
                "$rootPath/"
            )
        ) {

            val relativo =
                currentPath
                    .removePrefix(
                        rootPath
                    )
                    .trim('/')

            if (
                relativo.isEmpty()
            ) {

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

        definirIndiceAtual(
            currentIndex
        )

        val directory =
            history[currentIndex]

        if (
            directory.isDirectory
        ) {

            atualizarLista(
                directory
            )
        }
    }

    // =========================================================
    // VOLTAR DIRETÓRIO
    // =========================================================

    private fun voltarDiretorio() {

        var currentIndex =
            obterIndiceAtual()

        if (
            currentIndex <= 0
        ) {
            return
        }

        currentIndex--

        definirIndiceAtual(
            currentIndex
        )

        val history =
            obterHistoricoAtual()

        val directory =
            history[currentIndex]

        if (
            directory.isDirectory
        ) {

            atualizarLista(
                directory
            )
        }
    }

    // =========================================================
    // ABRIR ARQUIVO
    // =========================================================

    private fun abrirExterno(
        file: File
    ) {

        if (
            !file.exists() ||
            !file.isFile
        ) {

            pathText.text =
                "Arquivo não encontrado"

            return
        }

        try {

            val extensao =
                file.extension
                    .lowercase(
                        Locale.ROOT
                    )

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
                    file
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
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    clipData =
                        ClipData.newRawUri(
                            file.name,
                            uri
                        )
                }

            startActivity(intent)

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

        if (
            Build.VERSION.SDK_INT >= 30
        ) {

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

            view.typeface =
                fonte
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