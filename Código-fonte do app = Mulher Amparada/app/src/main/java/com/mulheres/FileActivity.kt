package com.mulheres

import android.animation.ValueAnimator
import android.content.ClipData
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
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
import android.view.animation.DecelerateInterpolator
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.Collator
import java.util.Locale

private class StorageCircleDrawable(
    private var color: Int
) : Drawable() {

    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG
    )

    init {
        paint.style = Paint.Style.FILL
    }

    fun setCircleColor(
        newColor: Int
    ) {
        color = newColor
        invalidateSelf()
    }

    override fun draw(
        canvas: Canvas
    ) {
        paint.color = color

        val radius =
            minOf(
                bounds.width(),
                bounds.height()
            ) / 2f

        canvas.drawCircle(
            bounds.centerX().toFloat(),
            bounds.centerY().toFloat(),
            radius,
            paint
        )
    }

    override fun setAlpha(
        alpha: Int
    ) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(
        colorFilter: ColorFilter?
    ) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Drawable API")
    override fun getOpacity(): Int =
        PixelFormat.TRANSLUCENT
}

class FileActivity : AppCompatActivity() {

    companion object {

        const val MANAGE_STORAGE_CODE = 101
        const val PERMISSION_CODE = 100

        private const val USB_PICKER_CODE = 300

        private const val PREFS =
            "mulheres_storage"

        private const val USB_URI =
            "usb_uri"
    }

    private enum class TipoArmazenamento {
        INTERNO,
        SD,
        USB
    }
    
    private lateinit var storageCircle: StorageCircleDrawable
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

    private var internalIndex =
        -1

    private var usandoArmazenamentoExterno =
        false

    private var volumesExternos =
        ArrayList<File>()

    /*
     * 0 = interno
     * 1 = SD
     * 2 = USB
     */
    private var modoArmazenamento =
        0

    /*
     * Histórico específico do USB.
     *
     * Diferente do SD, o USB pode ser acessado
     * através do Storage Access Framework.
     */
    private val usbHistory =
        ArrayList<DocumentFile>()

    private var usbIndex =
        -1

    private var usbRoot:
        DocumentFile? = null

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        configurarSistema()

        setContentView(
            R.layout.activity_file
        )

        aplicarFonte(
            findViewById(
                android.R.id.content
            )
        )

        recycler =
            findViewById(
                R.id.recycler
            )

        pathText =
            findViewById(
                R.id.pathText
            )

pathText.isSingleLine = true
pathText.maxLines = 1
pathText.ellipsize = null



pathText.isHorizontalScrollBarEnabled = false
pathText.isVerticalScrollBarEnabled = false

pathText.overScrollMode = View.OVER_SCROLL_NEVER

        itemCount =
            findViewById(
                R.id.itemCount
            )

        storageButton =
            findViewById(
                R.id.storageButton
            )

        configurarRecycler()
        configurarBack()
        configurarArmazenamento()

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

    storageCircle =
        StorageCircleDrawable(
            Color.rgb(124, 77, 255)
        )

    storageButton.background =
        storageCircle

    storageButton.setOnClickListener {

        alternarArmazenamento()
    }

    atualizarBotaoArmazenamento(
        animar = false
    )
}

    private fun atualizarBotaoArmazenamento(
    animar: Boolean = true
) {

    when (modoArmazenamento) {

        0 -> {

            storageCircle.setCircleColor(
                Color.rgb(124, 77, 255)
            )

            storageButton.setImageResource(
                R.drawable.ic_storage_internal
            )

            storageButton.contentDescription =
                "Armazenamento interno. Toque para trocar"
        }

        1 -> {

            storageCircle.setCircleColor(
                Color.rgb(255, 152, 0)
            )

            storageButton.setImageResource(
                R.drawable.ic_sd_card
            )

            storageButton.contentDescription =
                "Cartão SD. Toque para trocar"
        }

        2 -> {

            storageCircle.setCircleColor(
                Color.rgb(244, 67, 54)
            )

            storageButton.setImageResource(
                R.drawable.ic_usb
            )

            storageButton.contentDescription =
                "Pendrive USB OTG. Toque para trocar"
        }
    }

    if (animar) {
        animarBotaoArmazenamento()
    }
}

private fun animarBotaoArmazenamento() {

    storageButton.animate()
        .cancel()

    storageButton.animate()
        .rotationBy(120f)
        .scaleX(0.86f)
        .scaleY(0.86f)
        .setDuration(180L)
        .setInterpolator(
            DecelerateInterpolator()
        )
        .withEndAction {

            storageButton.animate()
                .rotationBy(60f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(180L)
                .setInterpolator(
                    DecelerateInterpolator()
                )
                .start()
        }
        .start()
}

    private fun alternarArmazenamento() {

        modoArmazenamento++

        if (modoArmazenamento > 2) {
            modoArmazenamento = 0
        }

        atualizarBotaoArmazenamento()

        when (modoArmazenamento) {

            0 -> {

                usandoArmazenamentoExterno =
                    false

                abrirInternoAtual()
            }

            1 -> {

                usandoArmazenamentoExterno =
                    true

                abrirSDAtual()
            }

            2 -> {

                usandoArmazenamentoExterno =
                    true

                abrirUSBAtual()
            }
        }
    }

    // =========================================================
    // VOLUMES NORMAIS
    // =========================================================

    private fun atualizarVolumesExternos() {

        val encontrados =
            LinkedHashMap<String, File>()

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R
        ) {

            try {

                val storageManager =
                    getSystemService(
                        StorageManager::class.java
                    )

                for (
                    volume in
                    storageManager.storageVolumes
                ) {

                    val directory =
                        volume.directory
                            ?: continue

                    if (
                        !directory.exists() ||
                        !directory.isDirectory
                    ) {
                        continue
                    }

                    val caminho =
                        obterCaminhoSeguro(
                            directory
                        )

                    if (
                        caminho.contains(
                            "/emulated/",
                            ignoreCase = true
                        ) ||
                        caminho.equals(
                            "/storage/emulated",
                            ignoreCase = true
                        )
                    ) {
                        continue
                    }

                    encontrados[caminho] =
                        directory
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }

        volumesExternos =
            ArrayList(
                encontrados.values
                    .filter {
                        it.exists() &&
                        it.isDirectory
                    }
                    .sortedBy {
                        it.absolutePath
                    }
            )
    }

    // =========================================================
    // IDENTIFICAR VOLUME
    // =========================================================

    private fun identificarTipoArmazenamento(
        root: File
    ): TipoArmazenamento {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R
        ) {

            try {

                val storageManager =
                    getSystemService(
                        StorageManager::class.java
                    )

                val caminhoRoot =
                    obterCaminhoSeguro(root)

                val volume =
                    storageManager
                        .storageVolumes
                        .firstOrNull {

                            val directory =
                                it.directory

                            directory != null &&
                            obterCaminhoSeguro(
                                directory
                            ) == caminhoRoot
                        }

                if (volume != null) {

                    val descricao =
                        volume
                            .getDescription(this)
                            ?.lowercase(
                                Locale.ROOT
                            )
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

        return TipoArmazenamento.INTERNO
    }

    // =========================================================
    // SD
    // =========================================================

    private fun encontrarVolumeSD():
        File? {

        atualizarVolumesExternos()

        return volumesExternos.firstOrNull {

            identificarTipoArmazenamento(it) ==
                    TipoArmazenamento.SD
        }
    }

    // =========================================================
    // USB FILE
    // =========================================================

    private fun encontrarVolumeUSB():
        File? {

        atualizarVolumesExternos()

        return volumesExternos.firstOrNull {

            identificarTipoArmazenamento(it) ==
                    TipoArmazenamento.USB
        }
    }

    // =========================================================
    // USB SAF
    // =========================================================

    private fun obterUsbUriSalvo():
        Uri? {

        val texto =
            getSharedPreferences(
                PREFS,
                MODE_PRIVATE
            ).getString(
                USB_URI,
                null
            )

        if (texto.isNullOrBlank()) {
            return null
        }

        return try {
            Uri.parse(texto)
        } catch (e: Exception) {
            null
        }
    }

    private fun obterUsbRootSalvo():
        DocumentFile? {

        val uri =
            obterUsbUriSalvo()
                ?: return null

        return try {

            DocumentFile
                .fromTreeUri(
                    this,
                    uri
                )

        } catch (e: Exception) {

            e.printStackTrace()

            null
        }
    }

    private fun salvarUsbUri(
        uri: Uri
    ) {

        try {

            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

        } catch (e: Exception) {

            /*
             * Alguns dispositivos não permitem
             * persistência daquela URI. Ainda podemos
             * utilizá-la durante esta sessão.
             */
            e.printStackTrace()
        }

        getSharedPreferences(
            PREFS,
            MODE_PRIVATE
        )
            .edit()
            .putString(
                USB_URI,
                uri.toString()
            )
            .apply()
    }

    private fun abrirUSBAtual() {

        /*
         * Primeiro tenta o USB já autorizado pelo SAF.
         */
        val salvo =
            obterUsbRootSalvo()

        if (
            salvo != null &&
            salvo.exists() &&
            salvo.isDirectory
        ) {

            usbRoot = salvo

            if (usbHistory.isEmpty()) {

                usbHistory.add(salvo)
                usbIndex = 0

            } else if (
                usbIndex < 0 ||
                usbIndex >= usbHistory.size
            ) {

                usbHistory.clear()
                usbHistory.add(salvo)
                usbIndex = 0
            }

            atualizarListaUSB(
                usbHistory[usbIndex]
            )

            return
        }

        /*
         * Se o StorageVolume fornecer um File USB,
         * também aceitamos esse caminho.
         */
        val fileUsb =
            encontrarVolumeUSB()

        if (fileUsb != null) {

            abrirVolumeExterno(
                fileUsb
            )

            return
        }

        /*
         * Nenhum USB conhecido.
         *
         * Mostramos vazio, conforme solicitado,
         * mas oferecemos o seletor quando o usuário
         * estiver tentando acessar o USB.
         */
        mostrarListaVazia()

        abrirSeletorUSB()
    }

    private fun abrirSeletorUSB() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_OPEN_DOCUMENT_TREE
                ).apply {

                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                    )
                }

            startActivityForResult(
                intent,
                USB_PICKER_CODE
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // =========================================================
    // LISTA USB
    // =========================================================

    private fun atualizarListaUSB(
        directory: DocumentFile
    ) {

        if (
            !directory.exists() ||
            !directory.isDirectory
        ) {

            usbRoot = null
            usbHistory.clear()
            usbIndex = -1

            mostrarListaVazia()

            return
        }

        val documentos =
            try {

                directory
                    .listFiles()
                    .filter {
                        it.exists()
                    }

            } catch (e: Exception) {

                emptyList()
            }

        val collator =
            Collator.getInstance(
                Locale("pt", "BR")
            )

        val sorted =
            documentos.sortedWith(
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
                        a.name ?: "",
                        b.name ?: ""
                    )
                }
            )

        val lista =
            sorted.map {
                FolderAdapter.StorageItem.Document(
                    it
                )
            }

        adapter.update(lista)

        atualizarCaminhoUSB(
            directory
        )

        atualizarContador(
            lista.size
        )
    }

    // =========================================================
    // CAMINHO USB
    // =========================================================

    private fun atualizarCaminhoUSB(
        directory: DocumentFile
    ) {

        val raiz =
            usbRoot

        if (raiz == null) {

            pathText.text = ""

            return
        }

        val nomeRaiz =
            raiz.name
                ?.ifBlank {
                    "Pendrive USB"
                }
                ?: "Pendrive USB"

        if (
            directory.uri == raiz.uri
        ) {

            pathText.text =
                nomeRaiz

            return
        }

        /*
         * Para SAF não dependemos de caminhos físicos.
         * Reconstruímos o caminho usando os históricos.
         */
        val partes =
            ArrayList<String>()

        val limite =
            minOf(
                usbIndex,
                usbHistory.lastIndex
            )

        if (limite >= 0) {

            for (i in 0..limite) {

                val nome =
                    usbHistory[i]
                        .name
                        ?.takeIf {
                            it.isNotBlank()
                        }

                if (
                    nome != null &&
                    i > 0
                ) {

                    partes.add(nome)
                }
            }
        }

        pathText.text =
            if (partes.isEmpty()) {

                nomeRaiz

            } else {

                "$nomeRaiz / ${partes.joinToString(" / ")}"
            }
    }

    // =========================================================
    // SD
    // =========================================================

    private fun abrirSDAtual() {

        val root =
            encontrarVolumeSD()

        if (root == null) {

            mostrarListaVazia()

            return
        }

        abrirVolumeExterno(
            root
        )
    }

    // =========================================================
    // VOLUME EXTERNO FILE
    // =========================================================

    private fun abrirVolumeExterno(
        root: File
    ) {

        if (
            !root.exists() ||
            !root.isDirectory
        ) {

            mostrarListaVazia()

            return
        }

        val tipo =
            identificarTipoArmazenamento(
                root
            )

        if (
            tipo != TipoArmazenamento.SD &&
            tipo != TipoArmazenamento.USB
        ) {

            mostrarListaVazia()

            return
        }

        val chave =
            obterCaminhoSeguro(root)

        val history =
            externalHistories.getOrPut(
                chave
            ) {
                ArrayList()
            }

        var index =
            externalIndexes[chave]
                ?: -1

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

        externalIndexes[chave] =
            index

        val atual =
            history[index]

        if (
            !atual.exists() ||
            !atual.isDirectory
        ) {

            externalHistories.remove(
                chave
            )

            externalIndexes.remove(
                chave
            )

            mostrarListaVazia()

            return
        }

        atualizarLista(
            atual
        )
    }

    // =========================================================
    // HISTÓRICO
    // =========================================================

    private fun obterHistoricoAtual():
        ArrayList<File> {

        if (!usandoArmazenamentoExterno) {
            return internalHistory
        }

        if (modoArmazenamento == 2) {
            return ArrayList()
        }

        val root =
            encontrarVolumeSD()
                ?: return ArrayList()

        return externalHistories.getOrPut(
            obterCaminhoSeguro(root)
        ) {
            ArrayList()
        }
    }

    private fun obterIndiceAtual():
        Int {

        if (!usandoArmazenamentoExterno) {
            return internalIndex
        }

        if (modoArmazenamento == 2) {
            return usbIndex
        }

        val root =
            encontrarVolumeSD()
                ?: return -1

        return externalIndexes[
            obterCaminhoSeguro(root)
        ] ?: -1
    }

    private fun definirIndiceAtual(
        index: Int
    ) {

        if (!usandoArmazenamentoExterno) {

            internalIndex = index

            return
        }

        if (modoArmazenamento == 2) {

            usbIndex = index

            return
        }

        val root =
            encontrarVolumeSD()
                ?: return

        externalIndexes[
            obterCaminhoSeguro(root)
        ] = index
    }

    // =========================================================
    // RECYCLER
    // =========================================================

    private fun configurarRecycler() {

        recycler.layoutManager =
            LinearLayoutManager(this)

        adapter =
            FolderAdapter { item ->

                abrirItem(
                    item
                )
            }

        recycler.adapter =
            adapter
    }

    // =========================================================
    // BACK
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

        modoArmazenamento = 0

        usandoArmazenamentoExterno =
            false

        internalHistory.clear()

        internalIndex = -1

        usbHistory.clear()

        usbIndex = -1

        usbRoot = null

        atualizarBotaoArmazenamento(
    animar = false
)

        abrirInternoAtual()
    }

    // =========================================================
    // INTERNO
    // =========================================================

    private fun abrirInternoAtual() {

        val root =
            Environment
                .getExternalStorageDirectory()

        if (
            !root.exists() ||
            !root.isDirectory
        ) {

            mostrarListaVazia()

            return
        }

        if (internalHistory.isEmpty()) {

            internalHistory.add(root)
            internalIndex = 0

        } else if (
            internalIndex < 0 ||
            internalIndex >=
            internalHistory.size
        ) {

            internalHistory.clear()
            internalHistory.add(root)
            internalIndex = 0
        }

        val atual =
            internalHistory[
                internalIndex
            ]

        if (
            atual.exists() &&
            atual.isDirectory
        ) {

            atualizarLista(
                atual
            )

        } else {

            mostrarListaVazia()
        }
    }

    // =========================================================
    // ITEM
    // =========================================================

    private fun abrirItem(
        item: FolderAdapter.StorageItem
    ) {

        when (item) {

            is FolderAdapter.StorageItem.Local -> {

                val file =
                    item.file

                if (
                    !file.exists()
                ) {

                    mostrarListaVazia()

                    return
                }

                if (file.isDirectory) {

                    abrirDiretorio(
                        file
                    )

                } else {

                    abrirExterno(
                        file
                    )
                }
            }

            is FolderAdapter.StorageItem.Document -> {

                val document =
                    item.document

                if (
                    !document.exists()
                ) {

                    mostrarListaVazia()

                    return
                }

                if (
                    document.isDirectory
                ) {

                    abrirDiretorioUSB(
                        document
                    )

                } else {

                    abrirExternoUSB(
                        document
                    )
                }
            }
        }
    }

    // =========================================================
    // DIRETÓRIO USB
    // =========================================================

    private fun abrirDiretorioUSB(
        directory: DocumentFile
    ) {

        if (
            !directory.exists() ||
            !directory.isDirectory
        ) {

            return
        }

        if (
            usbIndex <
            usbHistory.lastIndex
        ) {

            usbHistory.subList(
                usbIndex + 1,
                usbHistory.size
            ).clear()
        }

        usbHistory.add(
            directory
        )

        usbIndex =
            usbHistory.lastIndex

        atualizarListaUSB(
            directory
        )
    }

    // =========================================================
    // DIRETÓRIO FILE
    // =========================================================

    private fun abrirDiretorio(
        file: File
    ) {

        if (
            !file.exists() ||
            !file.isDirectory
        ) {
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
                obterCaminhoSeguro(
                    history[currentIndex]
                ) ==
                obterCaminhoSeguro(file)
            ) {

                atualizarLista(
                    file
                )

                return
            }

            history.add(file)

            currentIndex =
                history.lastIndex
        }

        definirIndiceAtual(
            currentIndex
        )

        atualizarLista(
            file
        )
    }

    // =========================================================
    // LISTA FILE
    // =========================================================

    private fun atualizarLista(
        directory: File
    ) {

        if (
            !directory.exists() ||
            !directory.isDirectory
        ) {

            mostrarListaVazia()

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

        val lista =
            sorted.map {

                FolderAdapter.StorageItem.Local(
                    it
                )
            }

        adapter.update(
            lista
        )

        atualizarCaminho(
            directory
        )

        atualizarContador(
            lista.size
        )
    }

    // =========================================================
    // VAZIO
    // =========================================================

    private fun mostrarListaVazia() {

        adapter.update(
            emptyList()
        )

        recycler.scrollToPosition(
            0
        )

        itemCount.text = ""

        pathText.text = ""
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
    // CAMINHO FILE
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

        if (!usandoArmazenamentoExterno) {

            val root =
                Environment
                    .getExternalStorageDirectory()

            val currentPath =
                obterCaminhoSeguro(
                    directory
                )

            val rootPath =
                obterCaminhoSeguro(
                    root
                )

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

                "Armazenamento interno / $relativo"

            } else {

                directory.name
            }
        }

        val root =
            encontrarVolumeSD()

        if (root == null) {
            return ""
        }

        if (
            !root.exists() ||
            !root.isDirectory
        ) {
            return ""
        }

        val rootPath =
            obterCaminhoSeguro(
                root
            )

        val currentPath =
            obterCaminhoSeguro(
                directory
            )

        val nome =
            obterNomeVolume(
                root
            )

        if (
            currentPath ==
            rootPath
        ) {

            return nome
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

            "$nome / $relativo"

        } else {

            ""
        }
    }

    // =========================================================
    // VOLTAR
    // =========================================================

    private fun voltarDiretorio() {

        if (
            usandoArmazenamentoExterno &&
            modoArmazenamento == 2
        ) {

            if (usbIndex <= 0) {
                return
            }

            usbIndex--

            val directory =
                usbHistory[
                    usbIndex
                ]

            if (
                directory.exists() &&
                directory.isDirectory
            ) {

                atualizarListaUSB(
                    directory
                )

            } else {

                mostrarListaVazia()
            }

            return
        }

        var currentIndex =
            obterIndiceAtual()

        if (currentIndex <= 0) {
            return
        }

        currentIndex--

        definirIndiceAtual(
            currentIndex
        )

        val history =
            obterHistoricoAtual()

        if (
            currentIndex >=
            history.size
        ) {

            mostrarListaVazia()

            return
        }

        val directory =
            history[
                currentIndex
            ]

        if (
            directory.exists() &&
            directory.isDirectory
        ) {

            atualizarLista(
                directory
            )

        } else {

            mostrarListaVazia()
        }
    }

    // =========================================================
    // AVANÇAR
    // =========================================================

    private fun avancarDiretorio() {

        if (
            usandoArmazenamentoExterno &&
            modoArmazenamento == 2
        ) {

            if (
                usbIndex >=
                usbHistory.lastIndex
            ) {
                return
            }

            usbIndex++

            atualizarListaUSB(
                usbHistory[
                    usbIndex
                ]
            )

            return
        }

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
            history[
                currentIndex
            ]

        if (
            directory.exists() &&
            directory.isDirectory
        ) {

            atualizarLista(
                directory
            )

        } else {

            mostrarListaVazia()
        }
    }

    // =========================================================
    // ABRIR ARQUIVO FILE
    // =========================================================

    private fun abrirExterno(
        file: File
    ) {

        if (
            !file.exists() ||
            !file.isFile
        ) {
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
                return
            }

            val extensao =
                arquivoReal.extension
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
                SdFileProvider
                    .getUriForFile(
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
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    clipData =
                        ClipData.newRawUri(
                            arquivoReal.name,
                            uri
                        )
                }

            startActivity(
                intent
            )

        } catch (
            e: android.content.ActivityNotFoundException
        ) {

            pathText.text =
                "Nenhum aplicativo pode abrir este arquivo"

        } catch (
            e: SecurityException
        ) {

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
    // ABRIR ARQUIVO USB
    // =========================================================

    private fun abrirExternoUSB(
        document: DocumentFile
    ) {

        if (
            !document.exists() ||
            !document.isFile
        ) {
            return
        }

        try {

            val uri =
                document.uri

            val mime =
                document.type
                    ?: "application/octet-stream"

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
                            document.name ?: "arquivo",
                            uri
                        )
                }

            startActivity(
                intent
            )

        } catch (
            e: android.content.ActivityNotFoundException
        ) {

            pathText.text =
                "Nenhum aplicativo pode abrir este arquivo"

        } catch (
            e: SecurityException
        ) {

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
    // NOME DO VOLUME
    // =========================================================

    private fun obterNomeVolume(
        root: File
    ): String {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R
        ) {

            try {

                val storageManager =
                    getSystemService(
                        StorageManager::class.java
                    )

                val caminhoRoot =
                    obterCaminhoSeguro(
                        root
                    )

                val volume =
                    storageManager
                        .storageVolumes
                        .firstOrNull {

                            val directory =
                                it.directory

                            directory != null &&
                            obterCaminhoSeguro(
                                directory
                            ) == caminhoRoot
                        }

                val descricao =
                    volume?.getDescription(
                        this
                    )

                if (
                    !descricao.isNullOrBlank()
                ) {

                    return descricao
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
    // CAMINHO SEGURO
    // =========================================================

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

    private fun temPermissao():
        Boolean {

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
    // RESULTADOS
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

        when (requestCode) {

            MANAGE_STORAGE_CODE -> {

                if (temPermissao()) {
                    iniciar()
                }
            }

            USB_PICKER_CODE -> {

                if (
                    resultCode ==
                    RESULT_OK &&
                    data?.data != null
                ) {

                    val uri =
                        data.data!!

                    salvarUsbUri(
                        uri
                    )

                    val root =
                        DocumentFile
                            .fromTreeUri(
                                this,
                                uri
                            )

                    if (
                        root != null &&
                        root.exists() &&
                        root.isDirectory
                    ) {

                        usbRoot =
                            root

                        usbHistory.clear()

                        usbHistory.add(
                            root
                        )

                        usbIndex = 0

                        atualizarListaUSB(
                            root
                        )

                    } else {

                        mostrarListaVazia()
                    }
                }
            }
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