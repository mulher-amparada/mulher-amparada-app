package com.mulheres

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlin.math.log10
import kotlin.math.sqrt

class TiltBrightnessController(
    private val activity: Activity,
    private val sensorManager: SensorManager,
    private val onEnterFullscreen: (() -> Unit)? = null,
    private val onExitFullscreen: (() -> Unit)? = null
) : SensorEventListener {

    private var isDark = false
    private var enabled = false

    private var originalBrightness: Float? = null

    private var protectionOverlay: View? = null

    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(
            Sensor.TYPE_GRAVITY
        )

    // =============================================================
    // ESTADO
    // =============================================================

    val isEnabled: Boolean
        get() = enabled

    val isDarkMode: Boolean
        get() = isDark

    // =============================================================
    // MICROFONE — FALLBACK
    // =============================================================

    private var audioRecord: AudioRecord? = null
    private var microphoneThread: Thread? = null
    private var microphoneRunning = false

    private val sampleRate = 44100

    private val bufferSize =
        AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

    private val loudSoundThreshold = -50.0

    // =============================================================
    // INICIAR
    // =============================================================

    fun start() {

        if (enabled) {
            return
        }

        enabled = true
        isDark = false

        originalBrightness =
            activity.window.attributes.screenBrightness

        if (gravitySensor != null) {

            val registered =
                sensorManager.registerListener(
                    this,
                    gravitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )

            if (!registered) {
                startMicrophoneFallback()
            }

        } else {

            startMicrophoneFallback()
        }
    }

    // =============================================================
    // CAMADA PRETA
    // =============================================================

    private fun showProtectionOverlay() {

        activity.runOnUiThread {

            if (protectionOverlay != null) {
                return@runOnUiThread
            }

            val overlay = View(activity)

            overlay.setBackgroundColor(
                Color.BLACK
            )

            overlay.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

            overlay.isClickable = true
            overlay.isFocusable = true

            val decorView =
                activity.window.decorView as ViewGroup

            decorView.addView(overlay)

            protectionOverlay = overlay
        }
    }

    private fun hideProtectionOverlay() {

        activity.runOnUiThread {

            protectionOverlay?.let { overlay ->

                val parent = overlay.parent

                if (parent is ViewGroup) {
                    parent.removeView(overlay)
                }
            }

            protectionOverlay = null
        }
    }

    // =============================================================
    // BRILHO
    // =============================================================

    fun setDarkBrightness(value: Float) {
        // Mantido para compatibilidade
        // com chamadas antigas.
    }

    // =============================================================
    // SENSOR DE GRAVIDADE
    // =============================================================

    override fun onSensorChanged(
        event: SensorEvent
    ) {

        if (!enabled || isDark) {
            return
        }

        val z = event.values[2]

        if (z < -8f) {
            activateProtection()
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // Não utilizado.
    }

    // =============================================================
    // FALLBACK DO MICROFONE
    // =============================================================

    private fun startMicrophoneFallback() {

        try {

            if (
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            val taxa = 44100

            val bufferSize =
                AudioRecord.getMinBufferSize(
                    taxa,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

            if (bufferSize <= 0) {
                return
            }

            audioRecord =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    taxa,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

            if (
                audioRecord?.state !=
                AudioRecord.STATE_INITIALIZED
            ) {

                audioRecord?.release()
                audioRecord = null

                return
            }

            val buffer =
                ShortArray(bufferSize)

            audioRecord?.startRecording()

            microphoneRunning = true

            microphoneThread =
                Thread {

                    try {

                        while (microphoneRunning) {

                            val leitura =
                                audioRecord?.read(
                                    buffer,
                                    0,
                                    buffer.size
                                ) ?: 0

                            if (leitura <= 0) {
                                continue
                            }

                            var pico = 0

                            for (i in 0 until leitura) {

                                val valor =
                                    kotlin.math.abs(
                                        buffer[i].toInt()
                                    )

                                pico =
                                    maxOf(
                                        pico,
                                        valor
                                    )
                            }

                            if (pico > 14000) {

                                activity.runOnUiThread {

                                    if (!isDark) {
                                        activateProtection()
                                    }
                                }

                                break
                            }
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()

                    } finally {

                        try {
                            audioRecord?.stop()
                        } catch (_: Exception) {
                        }

                        try {
                            audioRecord?.release()
                        } catch (_: Exception) {
                        }

                        audioRecord = null
                        microphoneRunning = false
                    }
                }

            microphoneThread?.start()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // =============================================================
    // CALCULAR VOLUME
    // =============================================================

    private fun calculateDecibels(
        buffer: ShortArray,
        length: Int
    ): Double {

        if (length <= 0) {
            return -100.0
        }

        var sum = 0.0

        for (i in 0 until length) {

            val sample =
                buffer[i].toDouble()

            sum += sample * sample
        }

        val rms =
            sqrt(sum / length)

        if (rms <= 0.0) {
            return -100.0
        }

        return 20.0 *
                log10(rms / 32768.0)
    }

    // =============================================================
    // PARAR MICROFONE
    // =============================================================

    private fun stopMicrophone() {

        microphoneRunning = false

        try {
            audioRecord?.stop()
        } catch (_: Exception) {
        }

        try {
            audioRecord?.release()
        } catch (_: Exception) {
        }

        audioRecord = null
        microphoneThread = null
    }

    // =============================================================
    // AÇÃO DA PROTEÇÃO
    // =============================================================

    private fun activateProtection() {

        if (!enabled || isDark) {
            return
        }

        isDark = true
        enabled = false

        // ---------------------------------------------------------
        // Parar microfone
        // ---------------------------------------------------------

        stopMicrophone()

        // ---------------------------------------------------------
        // Parar sensor
        // ---------------------------------------------------------

        sensorManager.unregisterListener(this)

        activity.runOnUiThread {

            // =====================================================
            // 1. BRILHO ZERO
            // =====================================================

            setBrightness(0f)

            // =====================================================
            // 2. FULLSCREEN
            // =====================================================

            onEnterFullscreen?.invoke()

            // =====================================================
            // 3. CAMADA PRETA
            // =====================================================

            showProtectionOverlay()
        }
    }

    // =============================================================
    // BRILHO
    // =============================================================

    private fun setBrightness(
        value: Float
    ) {

        val params =
            activity.window.attributes

        params.screenBrightness =
            value.coerceIn(
                0f,
                1f
            )

        activity.window.attributes =
            params
    }

    // =============================================================
    // PARAR
    // =============================================================

    fun stop() {

        enabled = false
        isDark = false

        sensorManager.unregisterListener(this)

        stopMicrophone()

        activity.runOnUiThread {

            // -----------------------------------------------------
            // Restaurar brilho original
            // -----------------------------------------------------

            originalBrightness?.let { brightness ->

                val params =
                    activity.window.attributes

                params.screenBrightness =
                    brightness

                activity.window.attributes =
                    params
            }

            // -----------------------------------------------------
            // Remover camada preta
            // -----------------------------------------------------

            hideProtectionOverlay()

            // -----------------------------------------------------
            // Sair/restaurar fullscreen
            // -----------------------------------------------------

            onExitFullscreen?.invoke()
        }
    }

    // =============================================================
    // MÉTODOS MANTIDOS
    // =============================================================
    //
    // Não são mais interfaces JavaScript.
    // Continuam existindo para preservar a API
    // que você já tinha.

    fun startTiltBrightness() {
        start()
    }

    fun setDarkBrightnessFromInterface(
        value: Float
    ) {
        setDarkBrightness(value)
    }

    fun stopTiltBrightness() {
        stop()
    }
}

