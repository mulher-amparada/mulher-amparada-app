package com.mulheres

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

import org.json.JSONObject

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import kotlin.math.sqrt


class GravarActivity : AppCompatActivity(),
    SensorEventListener,
    LocationListener {

    // =========================================================
    // COMPONENTES
    // =========================================================

    private lateinit var btnRecord: ImageView
    private lateinit var list: LinearLayout
    private lateinit var cripto: Cripto

    // =========================================================
    // ÁUDIO
    // =========================================================

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private var currentFile = ""
    private var recording = false

    private var currentTempFile: File? = null

    // =========================================================
    // SENSOR
    // =========================================================

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var maxGForce = 0.0

    // =========================================================
    // LOCALIZAÇÃO
    // =========================================================

    private lateinit var locationManager: LocationManager

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var currentAccuracy: Float? = null
    private var currentProvider: String? = null

    // =========================================================
    // TEMPO
    // =========================================================

    private var recordingCreatedAt = 0L

    // =========================================================
    // BATERIA
    // =========================================================

    private var recordingBatteryLevel = -1

    // =========================================================
    // PERMISSÕES
    // =========================================================

    companion object {

        private const val REQUEST_PERMISSIONS = 1001

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }


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

        val isDark =
    (resources.configuration.uiMode and
        android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
        android.content.res.Configuration.UI_MODE_NIGHT_YES

controller.isAppearanceLightStatusBars =
    !isDark

controller.isAppearanceLightNavigationBars =
    !isDark

        setContentView(
            R.layout.activity_gravar
        )

        cripto =
            Cripto(this)

        val raiz =
            findViewById<View>(
                android.R.id.content
            )

        aplicarFonte(raiz)

        btnRecord =
            findViewById(
                R.id.btnRecord
            )

        list =
            findViewById(
                R.id.list
            )

        sensorManager =
            getSystemService(
                Context.SENSOR_SERVICE
            ) as SensorManager

        accelerometer =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            )

        locationManager =
            getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager

        carregarGravacoes()

        checkPermissions()

        btnRecord.setOnClickListener {

            if (recording) {
                stopRecord()
            } else {
                startRecord()
            }
        }
    }


    // =========================================================
    // PERMISSÕES
    // =========================================================

    private fun checkPermissions() {

        val faltando =
            REQUIRED_PERMISSIONS.filter {

                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }

        if (faltando.isNotEmpty()) {

            ActivityCompat.requestPermissions(
                this,
                faltando.toTypedArray(),
                REQUEST_PERMISSIONS
            )
        }
    }


    // =========================================================
    // SENSOR
    // =========================================================

    private fun iniciarSensores() {

        accelerometer?.let {

            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }


    private fun pararSensores() {

        sensorManager.unregisterListener(
            this
        )
    }


    override fun onSensorChanged(
        event: SensorEvent
    ) {

        if (!recording) {
            return
        }

        if (
            event.sensor.type ==
            Sensor.TYPE_ACCELEROMETER
        ) {

            val x =
                event.values[0].toDouble()

            val y =
                event.values[1].toDouble()

            val z =
                event.values[2].toDouble()

            val aceleracao =
                sqrt(
                    x * x +
                    y * y +
                    z * z
                )

            val g =
                aceleracao / 9.80665

            if (g > maxGForce) {
                maxGForce = g
            }
        }
    }


    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
    }


    // =========================================================
    // LOCALIZAÇÃO
    // =========================================================

    private fun iniciarLocalizacao() {

        val fine =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarse =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) {
            return
        }

        try {

            if (fine) {

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            }

            if (coarse) {

                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            }

            obterUltimaLocalizacao()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }


    private fun obterUltimaLocalizacao() {

        val fine =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarse =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) {
            return
        }

        try {

            if (fine) {

                val gps =
                    locationManager.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER
                    )

                if (gps != null) {
                    atualizarLocalizacao(gps)
                }
            }

            if (
                currentLatitude == null &&
                coarse
            ) {

                val network =
                    locationManager.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER
                    )

                if (network != null) {
                    atualizarLocalizacao(network)
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }


    private fun atualizarLocalizacao(
        location: Location
    ) {

        currentLatitude =
            location.latitude

        currentLongitude =
            location.longitude

        currentAccuracy =
            location.accuracy

        currentProvider =
            location.provider
    }


    override fun onLocationChanged(
        location: Location
    ) {

        if (recording) {
            atualizarLocalizacao(location)
        }
    }


    @Deprecated(
        "Compatibilidade com versões antigas"
    )
    override fun onStatusChanged(
        provider: String?,
        status: Int,
        extras: Bundle?
    ) {
    }


    override fun onProviderEnabled(
        provider: String
    ) {
    }


    override fun onProviderDisabled(
        provider: String
    ) {
    }


    private fun pararLocalizacao() {

        try {

            locationManager.removeUpdates(
                this
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }


    // =========================================================
    // BATERIA
    // =========================================================

    private fun obterBateria(): Int {

        return try {

            val intent =
                registerReceiver(
                    null,
                    IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED
                    )
                )

            val level =
                intent?.getIntExtra(
                    BatteryManager.EXTRA_LEVEL,
                    -1
                ) ?: -1

            val scale =
                intent?.getIntExtra(
                    BatteryManager.EXTRA_SCALE,
                    -1
                ) ?: -1

            if (
                level >= 0 &&
                scale > 0
            ) {

                ((level * 100f) / scale)
                    .toInt()

            } else {

                -1
            }

        } catch (e: Exception) {

            -1
        }
    }


    // =========================================================
    // HORA AUTOMÁTICA
    // =========================================================

    private fun verificarNtpSincronizado(): Boolean {

        return try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.JELLY_BEAN_MR1
            ) {

                Settings.Global.getInt(
                    contentResolver,
                    Settings.Global.AUTO_TIME,
                    0
                ) == 1

            } else {

                true
            }

        } catch (e: Exception) {

            false
        }
    }


    // =========================================================
    // SHA-256 DE ARQUIVO
    // =========================================================

    private fun sha256(
        file: File
    ): String {

        val digest =
            MessageDigest.getInstance(
                "SHA-256"
            )

        FileInputStream(file).use { input ->

            val buffer =
                ByteArray(8192)

            var lidos: Int

            while (
                input.read(buffer).also {
                    lidos = it
                } != -1
            ) {

                digest.update(
                    buffer,
                    0,
                    lidos
                )
            }
        }

        return digest.digest()
            .joinToString("") {
                "%02x".format(it)
            }
    }


    // =========================================================
    // SHA-256 DE TEXTO
    // =========================================================

    private fun sha256String(
        valor: String
    ): String {

        val digest =
            MessageDigest.getInstance(
                "SHA-256"
            )

        return digest.digest(
            valor.toByteArray(
                Charsets.UTF_8
            )
        ).joinToString("") {
            "%02x".format(it)
        }
    }


    // =========================================================
    // HASH DO DISPOSITIVO
    // =========================================================

    private fun gerarDeviceHash(): String {

        val androidId =
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown"

        return sha256String(
            "mulheres-device-$androidId"
        )
    }


    // =========================================================
    // UTC
    // =========================================================

    private fun utcIso(
        timestamp: Long
    ): String {

        val format =
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                Locale.US
            )

        format.timeZone =
            TimeZone.getTimeZone("UTC")

        return format.format(
            Date(timestamp)
        )
    }


    // =========================================================
    // INICIAR GRAVAÇÃO
    // =========================================================

    private fun startRecord() {

        val audioPermitido =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        if (!audioPermitido) {

            checkPermissions()

            return
        }

        try {

            val dir =
                getExternalFilesDir(null)
                    ?: throw IllegalStateException(
                        "Diretório indisponível"
                    )

            // =================================================
            // RESET
            // =================================================

            recordingCreatedAt =
                System.currentTimeMillis()

            maxGForce = 0.0

            currentLatitude = null
            currentLongitude = null
            currentAccuracy = null
            currentProvider = null

            recordingBatteryLevel =
                obterBateria()

            // =================================================
            // LOCALIZAÇÃO
            // =================================================

            iniciarLocalizacao()

            // =================================================
            // SENSOR
            // =================================================

            iniciarSensores()

            // =================================================
            // NOME DEFINITIVO
            //
            // O .3gp é somente temporário.
            // =================================================

            currentFile =
                "${dir.absolutePath}/rec_" +
                        recordingCreatedAt +
                        ".3gp"

            val output =
                File(currentFile)

            // =================================================
            // MEDIA RECORDER
            // =================================================

            recorder =
                MediaRecorder(this).apply {

                    setAudioSource(
                        MediaRecorder.AudioSource.MIC
                    )

                    setOutputFormat(
                        MediaRecorder.OutputFormat.THREE_GPP
                    )

                    setAudioEncoder(
                        MediaRecorder.AudioEncoder.AMR_NB
                    )

                    setOutputFile(
                        output.absolutePath
                    )

                    prepare()

                    start()
                }

            recording = true

            btnRecord.setImageResource(
                R.drawable.mic1
            )

        } catch (e: Exception) {

            e.printStackTrace()

            pararSensores()
            pararLocalizacao()

            recorder?.release()

            recorder = null

            recording = false

            btnRecord.setImageResource(
                R.drawable.mic
            )

            Toast.makeText(
                this,
                "Não foi possível iniciar a gravação",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // =========================================================
    // PARAR GRAVAÇÃO
    // =========================================================

    private fun stopRecord() {

        if (!recording) {
            return
        }

        val closedAt =
            System.currentTimeMillis()

        try {

            // =================================================
            // PARAR RECORDER
            // =================================================

            recorder?.stop()

            recorder?.release()

            recorder = null

            recording = false

            btnRecord.setImageResource(
                R.drawable.mic
            )

            // =================================================
            // PARAR SENSOR E GPS
            // =================================================

            pararSensores()
            pararLocalizacao()

            // =================================================
            // ORIGINAL .3GP
            // =================================================

            val original =
                File(currentFile)

            if (!original.exists()) {

                throw IllegalStateException(
                    "Arquivo .3gp não foi criado"
                )
            }

            // =================================================
            // HASH DO .3GP
            // =================================================

            val sha256Raw =
                sha256(original)

            // =================================================
            // .ENC
            // =================================================

            val criptografado =
                File(
                    original.parent,
                    original.nameWithoutExtension +
                            ".enc"
                )

            // =================================================
            // CRIPTOGRAFAR
            // =================================================

            cripto.criptografarArquivo(
                original,
                criptografado
            )

            if (!criptografado.exists()) {

                throw IllegalStateException(
                    "Arquivo criptografado não foi criado"
                )
            }

            // =================================================
            // HASH DO .ENC
            // =================================================

            val sha256Encrypted =
                sha256(criptografado)

            // =================================================
            // METADADOS
            //
            // O JSON é criado ANTES de apagar o .3gp.
            // =================================================

            salvarMetadados(
                arquivoCriptografado =
                    criptografado,
                createdAt =
                    recordingCreatedAt,
                closedAt =
                    closedAt,
                lastModified =
                    criptografado.lastModified(),
                sha256Raw =
                    sha256Raw,
                sha256Encrypted =
                    sha256Encrypted
            )

            // =================================================
            // APAGAR ORIGINAL
            // =================================================

            if (original.exists()) {
                original.delete()
            }

            // =================================================
            // MOSTRAR NA LISTA
            // =================================================

            addToList(
                criptografado.absolutePath
            )

            currentFile = ""

        } catch (e: Exception) {

            e.printStackTrace()

            recorder?.release()

            recorder = null

            recording = false

            pararSensores()
            pararLocalizacao()

            btnRecord.setImageResource(
                R.drawable.mic
            )

            Toast.makeText(
                this,
                "Erro ao salvar a gravação",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // =========================================================
    // METADADOS
    // =========================================================

    private fun salvarMetadados(
        arquivoCriptografado: File,
        createdAt: Long,
        closedAt: Long,
        lastModified: Long,
        sha256Raw: String,
        sha256Encrypted: String
    ) {

        val json =
            JSONObject()

        // =====================================================
        // IDENTIFICAÇÃO
        // =====================================================

        json.put(
            "metadata_version",
            2
        )

        json.put(
            "file_name",
            arquivoCriptografado.name
        )

        json.put(
            "file_format",
            "3GP"
        )

        json.put(
            "audio_codec",
            "AMR-NB"
        )

        json.put(
            "file_size_bytes",
            arquivoCriptografado.length()
        )

        // =====================================================
        // TEMPO
        // =====================================================

        json.put(
            "created_at",
            createdAt / 1000L
        )

        json.put(
            "created_at_utc",
            utcIso(createdAt)
        )

        json.put(
            "closed_at",
            closedAt / 1000L
        )

        json.put(
            "closed_at_utc",
            utcIso(closedAt)
        )

        json.put(
            "last_modified",
            lastModified / 1000L
        )

        json.put(
            "last_modified_utc",
            utcIso(lastModified)
        )

        json.put(
            "ntp_synced",
            verificarNtpSincronizado()
        )

        // =====================================================
        // INTEGRIDADE
        // =====================================================

        json.put(
            "sha256_raw",
            sha256Raw
        )

        json.put(
            "sha256_encrypted",
            sha256Encrypted
        )

        // =====================================================
        // CRIPTOGRAFIA
        // =====================================================

        json.put(
            "crypto_algorithm",
            "AES/GCM/NoPadding"
        )

        json.put(
            "crypto_key_size_bits",
            256
        )

        json.put(
            "key_provider",
            "AndroidKeyStore"
        )

        // =====================================================
        // LOCALIZAÇÃO
        // =====================================================

        if (currentLatitude != null) {

            json.put(
                "gps_latitude",
                currentLatitude
            )

        } else {

            json.put(
                "gps_latitude",
                JSONObject.NULL
            )
        }

        if (currentLongitude != null) {

            json.put(
                "gps_longitude",
                currentLongitude
            )

        } else {

            json.put(
                "gps_longitude",
                JSONObject.NULL
            )
        }

        if (currentAccuracy != null) {

            json.put(
                "gps_accuracy",
                currentAccuracy
            )

        } else {

            json.put(
                "gps_accuracy",
                JSONObject.NULL
            )
        }

        if (currentProvider != null) {

            json.put(
                "location_provider",
                currentProvider
            )

        } else {

            json.put(
                "location_provider",
                JSONObject.NULL
            )
        }

        // =====================================================
        // DISPOSITIVO
        // =====================================================

        json.put(
            "device_model",
            Build.MODEL
        )

        json.put(
            "device_brand",
            Build.BRAND
        )

        json.put(
            "android_version",
            Build.VERSION.RELEASE
        )

        json.put(
            "api_level",
            Build.VERSION.SDK_INT
        )

        json.put(
            "device_hash",
            gerarDeviceHash()
        )

        // =====================================================
        // ACELERÔMETRO
        // =====================================================

        json.put(
            "max_g_force",
            maxGForce
        )

        // =====================================================
        // BATERIA
        // =====================================================

        json.put(
            "battery_level",
            recordingBatteryLevel
        )

        // =====================================================
        // STATUS DOS DADOS
        // =====================================================

        json.put(
            "gps_available",
            currentLatitude != null &&
                    currentLongitude != null
        )

        json.put(
            "accelerometer_available",
            accelerometer != null
        )

        // =====================================================
        // CRIAR JSON
        // =====================================================

        val metadataFile =
            File(
                arquivoCriptografado.parent,
                arquivoCriptografado.nameWithoutExtension +
                        ".metadata.json"
            )

        metadataFile.writeText(
            json.toString(4),
            Charsets.UTF_8
        )

        // =====================================================
        // GARANTIR QUE REALMENTE FOI CRIADO
        // =====================================================

        if (!metadataFile.exists()) {

            throw IllegalStateException(
                "O arquivo JSON não foi criado"
            )
        }
    }


    // =========================================================
    // CARREGAR GRAVAÇÕES
    // =========================================================

    private fun carregarGravacoes() {

        val dir =
            getExternalFilesDir(null)
                ?: return

        val arquivos =
            dir.listFiles()
                ?: return

        arquivos
            .filter {

                it.isFile &&
                        it.extension.equals(
                            "enc",
                            ignoreCase = true
                        )
            }
            .sortedByDescending {
                it.lastModified()
            }
            .forEach {
                addToList(
                    it.absolutePath
                )
            }
    }


    // =========================================================
    // REPRODUÇÃO
    // =========================================================

    private fun playAudio(
        path: String
    ) {

        try {

            player?.release()
            player = null

            currentTempFile?.let {

                if (it.exists()) {
                    it.delete()
                }
            }

            currentTempFile = null

            val criptografado =
                File(path)

            if (!criptografado.exists()) {
                return
            }

            val temporario =
                File(
                    cacheDir,
                    "audio_" +
                            System.currentTimeMillis() +
                            ".3gp"
                )

            currentTempFile =
                temporario

            cripto.descriptografarArquivo(
                criptografado,
                temporario
            )

            if (!temporario.exists()) {

                currentTempFile = null

                return
            }

            player =
                MediaPlayer().apply {

                    setDataSource(
                        temporario.absolutePath
                    )

                    prepare()

                    setOnCompletionListener {

                        release()

                        player = null

                        if (temporario.exists()) {
                            temporario.delete()
                        }

                        if (
                            currentTempFile ==
                            temporario
                        ) {

                            currentTempFile = null
                        }
                    }

                    start()
                }

        } catch (e: Exception) {

            e.printStackTrace()

            player?.release()

            player = null

            currentTempFile?.let {

                if (it.exists()) {
                    it.delete()
                }
            }

            currentTempFile = null
        }
    }


    // =========================================================
    // DOWNLOAD
    // =========================================================

    private fun downloadAudio(
        path: String
    ) {

        val criptografado =
            File(path)

        if (!criptografado.exists()) {

            Toast.makeText(
                this,
                "Gravação não encontrada",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        var temporario: File? = null

        try {

            // =================================================
            // DESCRIPTOGRAFAR
            // =================================================

            temporario =
                File(
                    cacheDir,
                    "download_" +
                            System.currentTimeMillis() +
                            ".3gp"
                )

            cripto.descriptografarArquivo(
                criptografado,
                temporario
            )

            if (!temporario.exists()) {

                throw IllegalStateException(
                    "Falha ao descriptografar"
                )
            }

            val baseName =
                criptografado.nameWithoutExtension

            val nomeAudio =
                baseName + ".3gp"

            val metadata =
                File(
                    criptografado.parent,
                    baseName +
                            ".metadata.json"
                )

            // =================================================
            // ANDROID 10+
            // =================================================

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {

                salvarNoDownloadsQ(
                    temporario,
                    nomeAudio,
                    "audio/3gpp"
                )

                if (metadata.exists()) {

                    salvarNoDownloadsQ(
                        metadata,
                        baseName +
                                ".metadata.json",
                        "application/json"
                    )
                }

            } else {

                salvarNoDownloadsAntigo(
                    temporario,
                    nomeAudio
                )

                if (metadata.exists()) {

                    salvarNoDownloadsAntigo(
                        metadata,
                        baseName +
                                ".metadata.json"
                    )
                }
            }

            Toast.makeText(
                this,
                "Áudio e metadados salvos em Downloads",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(
                this,
                "Não foi possível baixar a gravação",
                Toast.LENGTH_SHORT
            ).show()

        } finally {

            temporario?.let {

                if (it.exists()) {
                    it.delete()
                }
            }
        }
    }


    // =========================================================
    // DOWNLOAD ANDROID 10+
    // =========================================================

    private fun salvarNoDownloadsQ(
        origem: File,
        nome: String,
        mime: String
    ) {

        val values =
            android.content.ContentValues().apply {

                put(
                    MediaStore.Downloads.DISPLAY_NAME,
                    nome
                )

                put(
                    MediaStore.Downloads.MIME_TYPE,
                    mime
                )

                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
                )

                put(
                    MediaStore.Downloads.IS_PENDING,
                    1
                )
            }

        val uri =
            contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            )
                ?: throw IllegalStateException(
                    "Não foi possível criar arquivo em Downloads"
                )

        try {

            contentResolver
                .openOutputStream(uri)
                .use { output ->

                    if (output == null) {

                        throw IllegalStateException(
                            "Saída inválida"
                        )
                    }

                    FileInputStream(
                        origem
                    ).use { input ->

                        input.copyTo(output)
                    }
                }

            val finalizar =
                android.content.ContentValues()

            finalizar.put(
                MediaStore.Downloads.IS_PENDING,
                0
            )

            contentResolver.update(
                uri,
                finalizar,
                null,
                null
            )

        } catch (e: Exception) {

            contentResolver.delete(
                uri,
                null,
                null
            )

            throw e
        }
    }


    // =========================================================
    // DOWNLOAD ANDROID ANTIGO
    // =========================================================

    private fun salvarNoDownloadsAntigo(
        origem: File,
        nome: String
    ) {

        val downloads =
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )

        if (!downloads.exists()) {
            downloads.mkdirs()
        }

        val destino =
            File(
                downloads,
                nome
            )

        FileInputStream(
            origem
        ).use { input ->

            FileOutputStream(
                destino
            ).use { output ->

                input.copyTo(output)
            }
        }

        sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(destino)
            )
        )
    }


    // =========================================================
    // ITEM DA LISTA
    // =========================================================

    private fun addToList(
        path: String
    ) {

        val file =
            File(path)

        if (!file.exists()) {
            return
        }

        val container =
            LinearLayout(this)

        container.orientation =
            LinearLayout.HORIZONTAL

        container.gravity =
            android.view.Gravity.CENTER_VERTICAL

        container.setPadding(
            24,
            24,
            20,
            24
        )

        container.setBackgroundResource(
            R.drawable.bg_record_item
        )

        val params =
            LinearLayout.LayoutParams(
                -1,
                -2
            )

        params.setMargins(
            0,
            0,
            0,
            16
        )

        container.layoutParams =
            params

        // =====================================================
        // ÁUDIO
        // =====================================================

        val icon =
            ImageView(this)

        icon.setImageResource(
            R.drawable.ic_audio
        )

        icon.contentDescription =
            "Reproduzir gravação"

        icon.layoutParams =
            LinearLayout.LayoutParams(
                56,
                56
            )

        // =====================================================
        // NOME
        // =====================================================

        val text =
            TextView(this)

        text.text =
            file.nameWithoutExtension

        text.textSize =
            16f

        text.setTextColor(
    ContextCompat.getColor(
        this,
        R.color.record_text
    )
)

        text.setPadding(
            18,
            0,
            12,
            0
        )

        text.typeface =
            Typeface.createFromAsset(
                assets,
                "font.ttf"
            )

        text.layoutParams =
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )

        // =====================================================
        // DOWNLOAD
        // =====================================================

        val download =
            ImageView(this)

        download.setImageResource(
            R.drawable.ic_download
        )

        download.contentDescription =
            "Baixar gravação"

        download.isClickable =
            true

        download.isFocusable =
            true

        download.setBackgroundColor(
            Color.TRANSPARENT
        )

        download.layoutParams =
            LinearLayout.LayoutParams(
                56,
                56
            )

        // =====================================================
        // EXCLUIR
        // =====================================================

        val delete =
            ImageView(this)

        delete.setImageResource(
            R.drawable.ic_delete
        )

        delete.contentDescription =
            "Excluir gravação"

        delete.isClickable =
            true

        delete.isFocusable =
            true

        delete.setBackgroundColor(
            Color.TRANSPARENT
        )

        delete.layoutParams =
            LinearLayout.LayoutParams(
                56,
                56
            )

        // =====================================================
        // ADICIONAR
        // =====================================================

        container.addView(download)
container.addView(icon)
container.addView(text)
container.addView(delete)

        // =====================================================
        // REPRODUZIR
        // =====================================================

        icon.setOnClickListener {

            playAudio(
                file.absolutePath
            )
        }

        text.setOnClickListener {

            playAudio(
                file.absolutePath
            )
        }

        // =====================================================
        // DOWNLOAD
        // =====================================================

        download.setOnClickListener {

            downloadAudio(
                file.absolutePath
            )
        }

        // =====================================================
        // EXCLUIR
        // =====================================================

        delete.setOnClickListener {

            player?.release()

            player = null

            currentTempFile?.let {

                if (it.exists()) {
                    it.delete()
                }
            }

            currentTempFile = null

            // ===============================================
            // ENC
            // ===============================================

            if (file.exists()) {
                file.delete()
            }

            // ===============================================
            // JSON
            // ===============================================

            val metadata =
                File(
                    file.parent,
                    file.nameWithoutExtension +
                            ".metadata.json"
                )

            if (metadata.exists()) {
                metadata.delete()
            }

            // ===============================================
            // LISTA
            // ===============================================

            list.removeView(
                container
            )
        }

        list.addView(
            container
        )
    }


    // =========================================================
    // VOLTAR
    // =========================================================

    override fun onBackPressed() {

        finish()
    }


    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        pararSensores()
        pararLocalizacao()

        recorder?.release()

        recorder = null

        player?.release()

        player = null

        currentTempFile?.let {

            if (it.exists()) {
                it.delete()
            }
        }

        currentTempFile = null

        super.onDestroy()
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


    // =========================================================
    // RESULTADO DAS PERMISSÕES
    // =========================================================

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
            REQUEST_PERMISSIONS
        ) {

            val audio =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

            if (!audio) {

                finish()

                return
            }

            val location =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

            if (location) {
                iniciarLocalizacao()
            }
        }
    }
}