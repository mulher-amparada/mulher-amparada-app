package com.mulheres

import android.app.Activity
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.webkit.JavascriptInterface
import android.webkit.WebView

class TiltBrightnessController(
    private val activity: Activity,
    private val sensorManager: SensorManager,
    private val webView: WebView
) : SensorEventListener {

    private var isDark = false
    private var enabled = false

    private var originalBrightness: Float? = null

    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

fun start(): Boolean {

    if (enabled) return true

    // Verifica se o aparelho possui sensor de gravidade
    if (gravitySensor == null) {
        return false
    }

    // Tenta registrar o sensor
    val registrado = sensorManager.registerListener(
        this,
        gravitySensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )

    // Se o sistema não conseguiu registrar, não ativa
    if (!registrado) {
        return false
    }

    enabled = true
    isDark = false

    originalBrightness =
        activity.window.attributes.screenBrightness

    return true
}
    
    fun setDarkBrightness(value: Float) {
        // Mantido somente para compatibilidade
        // com o JavaScript.
        //
        // O modo escuro utiliza brilho 0.
    }

    override fun onSensorChanged(event: SensorEvent) {

        if (!enabled || isDark) {
            return
        }

        val z = event.values[2]

        if (z < -8f) {

            isDark = true

            activity.runOnUiThread {

                // =================================================
                // 1. BRILHO ZERO
                // =================================================

                setBrightness(0f)


                // =================================================
                // 2. CHAMAR O FULLSCREEN DA MAINACTIVITY
                // =================================================

                if (activity is MainActivity) {
                    activity.ativarFullscreen()
                }


                // =================================================
                // 3. DEIXAR O WEBVIEW PRETO
                // =================================================

                webView.setBackgroundColor(
                    Color.BLACK
                )

                webView.evaluateJavascript(
                    """
                    document.documentElement.style.backgroundColor = 'black';
                    document.body.style.backgroundColor = 'black';
                    document.body.style.visibility = 'hidden';
                    document.body.style.opacity = '0';
                    """.trimIndent(),
                    null
                )


                // =================================================
                // 4. AVISAR O JAVASCRIPT
                // =================================================

                webView.evaluateJavascript(
                    """
                    window.dispatchEvent(
                        new CustomEvent(
                            'tiltbrightness',
                            { detail: 'dark' }
                        )
                    );
                    """.trimIndent(),
                    null
                )
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // Não utilizado.
    }


    // =============================================================
    // BRILHO
    // =============================================================

    private fun setBrightness(value: Float) {

        val params =
            activity.window.attributes

        params.screenBrightness =
            value.coerceIn(0f, 1f)

        activity.window.attributes = params
    }


    // =============================================================
    // PARAR
    // =============================================================

    fun stop() {

        enabled = false
        isDark = false

        sensorManager.unregisterListener(this)

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
            // Restaurar WebView
            // -----------------------------------------------------

            webView.setBackgroundColor(
                Color.BLACK
            )

            webView.evaluateJavascript(
                """
                document.body.style.visibility = 'visible';
                document.body.style.opacity = '1';
                """.trimIndent(),
                null
            )


            // -----------------------------------------------------
            // CHAMAR O MÉTODO DA MAINACTIVITY
            // -----------------------------------------------------

            if (activity is MainActivity) {
                activity.ativarFullscreen()
            }


            // -----------------------------------------------------
            // Avisar JavaScript
            // -----------------------------------------------------

            webView.evaluateJavascript(
                """
                window.dispatchEvent(
                    new CustomEvent(
                        'tiltbrightness',
                        { detail: 'normal' }
                    )
                );
                """.trimIndent(),
                null
            )
        }
    }


    // =============================================================
    // JAVASCRIPT INTERFACE
    // =============================================================

    class WebAppInterface(
        private val controller: TiltBrightnessController
    ) {

@JavascriptInterface
fun startTiltBrightness(): Boolean {
    return controller.start()
}

        @JavascriptInterface
        fun setDarkBrightness(value: Float) {
            controller.setDarkBrightness(value)
        }

        @JavascriptInterface
        fun stopTiltBrightness() {
            controller.stop()
        }
    }
}