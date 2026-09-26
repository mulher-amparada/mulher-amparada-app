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
import android.view.WindowManager
import androidx.core.content.ContextCompat
import kotlin.math.log10
import kotlin.math.sqrt

class TiltBrightnessController(
    private val activity: Activity,
    private val sensorManager: SensorManager,
    private val onEnterFullscreen: () -> Unit,
    private val onExitFullscreen: () -> Unit
) {

    private var ativo = false

    private var gravitySensor: Sensor? = null

    private var gravityListener: SensorEventListener? = null

    private var overlay: View? = null

    private var brilhoAnterior: Float? = null

    // =========================================================
    // MICROFONE
    // =========================================================

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


    // =========================================================
    // INICIAR
    // =========================================================

    fun startTiltBrightness() {

        if (ativo) {
            return
        }

        ativo = true

        gravitySensor =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_GRAVITY
            )

        if (gravitySensor == null) {

            iniciarMicrofone()

            return
        }

        if (gravityListener == null) {

            gravityListener =
                object : SensorEventListener {

                    override fun onSensorChanged(
                        event: SensorEvent
                    ) {

                        if (!ativo) {
                            return
                        }

                        /*
                         * Uma vez que o estado de escurecimento
                         * foi ativado, ele permanece até o
                         * controller ser parado.
                         */
                        if (overlay != null) {
                            return
                        }

                        val z =
                            event.values[2]

                        if (z < -8f) {

                            ativarEscurecimento()
                        }
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor?,
                        accuracy: Int
                    ) {
                    }
                }
        }

        val registrado =
            sensorManager.registerListener(
                gravityListener,
                gravitySensor,
                SensorManager.SENSOR_DELAY_GAME
            )

        /*
         * Se o sensor não puder ser registrado,
         * utiliza o microfone como fallback.
         */
        if (!registrado) {

            iniciarMicrofone()
        }
    }


    // =========================================================
    // PARAR
    // =========================================================

    fun stopTiltBrightness() {

        ativo = false

        gravityListener?.let {

            sensorManager.unregisterListener(
                it
            )
        }

        pararMicrofone()

        /*
         * Aqui sim o preto é removido.
         *
         * Portanto, ele só sai quando o controller
         * é realmente parado, normalmente quando
         * a Activity é destruída/encerrada.
         */
        desativarEscurecimento()
    }


    fun start() {
        startTiltBrightness()
    }


    fun stop() {
        stopTiltBrightness()
    }


    // =========================================================
    // ESCURECER
    // =========================================================

    private fun ativarEscurecimento() {

        if (!ativo) {
            return
        }

        /*
         * Já está preto.
         */
        if (overlay != null) {
            return
        }

        /*
         * Guarda o brilho original somente uma vez.
         */
        brilhoAnterior =
            activity.window.attributes.screenBrightness

        /*
         * Brilho físico da janela no mínimo.
         */
        activity.window.attributes =
            activity.window.attributes.apply {

                screenBrightness = 0f
            }

        /*
         * Entra em fullscreen.
         */
        onEnterFullscreen()

        /*
         * Camada preta cobrindo absolutamente
         * toda a janela.
         */
        val view =
            View(activity).apply {

                setBackgroundColor(
                    Color.BLACK
                )

                alpha = 1f

                /*
                 * Impede que os elementos abaixo
                 * recebam toques.
                 */
                isClickable = true
                isFocusable = true
            }

        overlay = view

        activity.addContentView(
            view,
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        )
    }


    // =========================================================
    // DESFAZER ESCURECIMENTO
    // =========================================================

    private fun desativarEscurecimento() {

        overlay?.let {

            val parent =
                it.parent

            if (parent is android.view.ViewGroup) {

                parent.removeView(it)
            }
        }

        overlay = null

        brilhoAnterior?.let {

            activity.window.attributes =
                activity.window.attributes.apply {

                    screenBrightness = it
                }
        }

        brilhoAnterior = null

        onExitFullscreen()
    }


    // =========================================================
    // MICROFONE
    // =========================================================

    private fun iniciarMicrofone() {

        if (!ativo) {
            return
        }

        if (microphoneRunning) {
            return
        }

        if (
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (bufferSize <= 0) {
            return
        }

        try {

            audioRecord =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
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

            microphoneRunning = true

            microphoneThread =
                Thread {

                    try {

                        val buffer =
                            ShortArray(
                                bufferSize
                            )

                        audioRecord?.startRecording()

                        while (
                            microphoneRunning &&
                            ativo
                        ) {

                            /*
                             * Se já escureceu pelo sensor,
                             * não precisa mais analisar áudio.
                             */
                            if (overlay != null) {
                                break
                            }

                            val read =
                                audioRecord?.read(
                                    buffer,
                                    0,
                                    buffer.size
                                ) ?: 0

                            if (read > 0) {

                                val db =
                                    calcularDecibeis(
                                        buffer,
                                        read
                                    )

                                if (
                                    db >=
                                    loudSoundThreshold
                                ) {

                                    activity.runOnUiThread {

                                        if (
                                            ativo &&
                                            overlay == null
                                        ) {

                                            ativarEscurecimento()
                                        }
                                    }
                                }
                            }
                        }

                    } catch (
                        _: Exception
                    ) {
                    } finally {

                        pararMicrofone()
                    }

                }.apply {

                    name =
                        "MulherAmparada-TiltMicrophone"

                    start()
                }

        } catch (
            _: Exception
        ) {

            audioRecord?.release()

            audioRecord = null

            microphoneRunning = false
        }
    }


    // =========================================================
    // DECIBÉIS
    // =========================================================

    private fun calcularDecibeis(
        buffer: ShortArray,
        length: Int
    ): Double {

        if (length <= 0) {
            return -100.0
        }

        var soma = 0.0

        for (i in 0 until length) {

            val sample =
                buffer[i].toDouble()

            soma +=
                sample * sample
        }

        val rms =
            sqrt(
                soma / length
            )

        if (rms <= 0.0) {
            return -100.0
        }

        return 20.0 *
            log10(
                rms / 32768.0
            )
    }


    // =========================================================
    // PARAR MICROFONE
    // =========================================================

    private fun pararMicrofone() {

        microphoneRunning = false

        try {

            audioRecord?.stop()

        } catch (
            _: Exception
        ) {
        }

        try {

            audioRecord?.release()

        } catch (
            _: Exception
        ) {
        }

        audioRecord = null

        microphoneThread = null
    }
}