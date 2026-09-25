package com.mulheres

import android.app.role.RoleManager
import org.json.JSONArray
import android.webkit.WebView
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.io.ByteArrayOutputStream
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.webkit.JavascriptInterface
import android.media.MediaRecorder

class WebAppInterface(
    private val activity: Activity
) {

    // =========================================================
    // BLOQUEIO POR BARULHO
    // =========================================================

    private var bloqueioRecorder: MediaRecorder? = null

    private var bloqueioThread: Thread? = null

    @Volatile
    private var bloqueioRodando = false
    
    private fun safeMainActivityCall(
    action: (MainActivity) -> Unit
) {
    val mainActivity =
        activity as? MainActivity
            ?: return

    mainActivity.runOnUiThread {
        action(mainActivity)
    }
}

@JavascriptInterface
fun desligarPorBarulho(): Boolean {

    /*
     * Solicita o Administrador do dispositivo.
     *
     * A função continua tentando iniciar o detector,
     * mas o bloqueio somente será realizado se o
     * Administrador estiver realmente ativo.
     */
    solicitarAdministrador()

    return try {

        /*
         * Evita criar dois detectores ao mesmo tempo.
         */
        if (bloqueioRodando) {
            return true
        }


        /*
         * Verifica o microfone.
         */
        if (
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return false
        }


        val arquivoTemporario =
            java.io.File(
                activity.cacheDir,
                "temp_bloqueio.3gp"
            )


        /*
         * Remove gravação temporária anterior.
         */
        try {
            if (arquivoTemporario.exists()) {
                arquivoTemporario.delete()
            }
        } catch (_: Exception) {
        }


        val gravador =
            MediaRecorder()


        gravador.setAudioSource(
            MediaRecorder.AudioSource.MIC
        )


        gravador.setOutputFormat(
            MediaRecorder.OutputFormat.THREE_GPP
        )


        gravador.setAudioEncoder(
            MediaRecorder.AudioEncoder.AMR_NB
        )


        gravador.setOutputFile(
            arquivoTemporario.absolutePath
        )


        gravador.prepare()
        gravador.start()


        bloqueioRecorder =
            gravador


        bloqueioRodando =
            true


        bloqueioThread =
            Thread {

                try {

                    while (
                        bloqueioRodando
                    ) {

                        val amplitude =
                            try {
                                gravador.maxAmplitude
                            } catch (_: Exception) {
                                0
                            }


                        /*
                         * Som alto detectado.
                         */
                        if (
                            amplitude >= 18000
                        ) {

                            /*
                             * Primeiro encerramos o detector.
                             */
                            bloqueioRodando =
                                false


                            try {
                                gravador.stop()
                            } catch (_: Exception) {
                            }


                            try {
                                gravador.release()
                            } catch (_: Exception) {
                            }


                            bloqueioRecorder =
                                null


                            /*
                             * Executa o bloqueio.
                             */
                            val intent =
                                Intent(
                                    activity,
                                    MyDeviceAdminReceiver::class.java
                                ).apply {

                                    action =
                                        "com.mulheres.BLOQUEAR_CELULAR"
                                }


                            activity.sendBroadcast(
                                intent
                            )


                            /*
                             * A ação foi concluída:
                             * o detector não deve continuar ativo.
                             *
                             * Avisamos a página HTML para
                             * atualizar o switch e salvar false.
                             */
                            activity.runOnUiThread {

                                try {

                                    activity
                                        .findViewById<WebView>(
                                            R.id.webview
                                        )
                                        ?.evaluateJavascript(
                                            """
                                            if (
                                                typeof window.bloqueioPorBarulhoConcluido ===
                                                'function'
                                            ) {
                                                window.bloqueioPorBarulhoConcluido();
                                            }
                                            """.trimIndent(),
                                            null
                                        )

                                } catch (_: Exception) {
                                }
                            }


                            break
                        }


                        Thread.sleep(100)
                    }

                } catch (_: Exception) {

                    /*
                     * Se ocorrer algum erro, encerra
                     * o detector para não deixá-lo preso.
                     */

                } finally {

                    if (
                        !bloqueioRodando
                    ) {

                        try {
                            bloqueioRecorder?.release()
                        } catch (_: Exception) {
                        }

                        bloqueioRecorder =
                            null
                    }
                }

            }.apply {

                name =
                    "MulherAmparada-BloqueioPorBarulho"

                start()
            }


        true

    } catch (e: Exception) {

        e.printStackTrace()


        bloqueioRodando =
            false


        try {
            bloqueioRecorder?.release()
        } catch (_: Exception) {
        }


        bloqueioRecorder =
            null


        false
    }
}

@JavascriptInterface
fun pararBloqueio(): Boolean {

    return try {

        /*
         * Se já estiver parado, não há nada para fazer.
         */
        if (!bloqueioRodando) {

            bloqueioRecorder = null

            return true
        }


        /*
         * Sinaliza para a thread encerrar.
         */
        bloqueioRodando =
            false


        /*
         * Para e libera o MediaRecorder.
         */
        try {
            bloqueioRecorder?.stop()
        } catch (_: Exception) {
        }


        try {
            bloqueioRecorder?.reset()
        } catch (_: Exception) {
        }


        try {
            bloqueioRecorder?.release()
        } catch (_: Exception) {
        }


        bloqueioRecorder =
            null


        bloqueioThread =
            null


        /*
         * Remove a gravação temporária.
         */
        try {

            val arquivo =
                java.io.File(
                    activity.cacheDir,
                    "temp_bloqueio.3gp"
                )

            if (arquivo.exists()) {
                arquivo.delete()
            }

        } catch (_: Exception) {
        }


        true

    } catch (e: Exception) {

        e.printStackTrace()

        false
    }
}

@JavascriptInterface
fun obterApps(): String {

    val pm = activity.packageManager
    val lista = JSONArray()

    pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .sortedBy {
            pm.getApplicationLabel(it).toString().lowercase()
        }
        .forEach { app ->

            val pacote = app.packageName.lowercase()

            if (
                pacote.contains("systemui") ||
                pacote.contains("knox")
            ) {
                return@forEach
            }

            try {
                lista.put(JSONObject().apply {
                    put(
                        "nome",
                        pm.getApplicationLabel(app).toString()
                    )

                    put(
                        "pacote",
                        app.packageName
                    )

                    put(
                        "icone",
                        drawableToBase64(
                            pm.getApplicationIcon(app)
                        )
                    )
                })
            } catch (_: Exception) {
            }
        }

    return lista.toString()
}

@JavascriptInterface
fun abrirApp(pacote: String) {
    activity.packageManager
        .getLaunchIntentForPackage(pacote)
        ?.let(activity::startActivity)
}

private fun drawableToBase64(drawable: Drawable): String {
    val bitmap = if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }

    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

    return "data:image/png;base64," +
        Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
}

    
@JavascriptInterface
fun solicitarAdministrador() {

    try {

        val mainActivity =
            activity as? MainActivity
                ?: return

        mainActivity.runOnUiThread {

            mainActivity.marcarSolicitacaoAdministrador()

            val component =
                ComponentName(
                    activity,
                    MyDeviceAdminReceiver::class.java
                )

            val intent =
                Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                ).apply {

                    putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        component
                    )

                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Este aplicativo precisa da permissão de Administrador do dispositivo."
                    )
                }

            activity.startActivityForResult(
                intent,
                1001
            )
        }

    } catch (e: Exception) {

        e.printStackTrace()

    }
}

@JavascriptInterface
fun bloquearTela(): Boolean {

    return try {

        val dpm =
            activity.getSystemService(
                Context.DEVICE_POLICY_SERVICE
            ) as DevicePolicyManager

        val component =
            ComponentName(
                activity,
                MyDeviceAdminReceiver::class.java
            )

        if (!dpm.isAdminActive(component)) {

            val mainActivity =
                activity as? MainActivity
                    ?: return false

            mainActivity.runOnUiThread {

                mainActivity.marcarSolicitacaoAdministrador()

                val intent =
                    Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                    ).apply {

                        putExtra(
                            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            component
                        )

                        putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "Permite que o Mulher Amparada bloqueie a tela imediatamente quando necessário."
                        )
                    }

                activity.startActivityForResult(
                    intent,
                    1001
                )
            }

            return false
        }

        dpm.lockNow()

        true

    } catch (e: Exception) {

        e.printStackTrace()

        false
    }
}

    @JavascriptInterface
    fun openFiles() {
        activity.startActivity(
            Intent(activity, FileActivity::class.java)
        )
    }



    @JavascriptInterface
    fun salvar(chave: String, valor: String) {
        val prefs = activity.getSharedPreferences(
            "cripto",
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putString(chave, valor)
            .apply()
    }

@JavascriptInterface
fun mostrarBotaoEmergencia() {
    safeMainActivityCall {
        it.mostrarBotaoEmergencia()
    }
}

@JavascriptInterface
fun ocultarBotaoEmergencia() {
    safeMainActivityCall {
        it.ocultarBotaoEmergencia()
    }
}

    @JavascriptInterface
    fun carregar(chave: String): String {
        val prefs = activity.getSharedPreferences(
            "cripto",
            Context.MODE_PRIVATE
        )

        return prefs.getString(chave, "") ?: ""
    }

    @JavascriptInterface
    fun remover(chave: String) {
        val prefs = activity.getSharedPreferences(
            "cripto",
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .remove(chave)
            .apply()
    }

    @JavascriptInterface
    fun limparTudo() {
        val prefs = activity.getSharedPreferences(
            "cripto",
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .clear()
            .apply()
    }

    @JavascriptInterface
    fun abrirContatos() {
        safeMainActivityCall {
            it.abrirContatos()
        }
    }

    @JavascriptInterface
    fun selecionarContato() {
        safeMainActivityCall {
            it.abrirContatos()
        }
    }

    @JavascriptInterface
    fun ativarPalmas() {
        safeMainActivityCall {
            it.ativarPalmas()
        }
    }

    @JavascriptInterface
    fun desativarPalmas() {
        safeMainActivityCall {
            it.desativarPalmas()
        }
    }

    @JavascriptInterface
    fun ativarProtecao() {
        safeMainActivityCall {
            it.ativarProtecao()
        }
    }

    @JavascriptInterface
    fun desativarProtecao() {
        safeMainActivityCall {
            it.desativarProtecao()
        }
    }

    @JavascriptInterface
    fun enviarSOS() {
        safeMainActivityCall {
            it.enviarSOS()
        }
    }

    @JavascriptInterface
    fun iniciarBiometria() {
        safeMainActivityCall {
            it.iniciarBiometria()
        }
    }

    @JavascriptInterface
    fun IniciarBiometriaÁreaProtegida() {
        safeMainActivityCall {
            it.IniciarBiometriaÁreaProtegida()
        }
    }

    @JavascriptInterface
    fun iniciarBiometriaAmparo() {
        safeMainActivityCall {
            it.iniciarBiometriaAmparo()
        }
    }

    @JavascriptInterface
    fun pegarLocalizacao() {
        safeMainActivityCall {
            it.pegarLocalizacao()
        }
    }

    @JavascriptInterface
    fun salvarContatos(lista: String) {
        val prefs = activity.getSharedPreferences(
            "contatos",
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putString("lista", lista)
            .apply()
    }
    
    @JavascriptInterface
fun openRecorder() {
    activity.startActivity(
        Intent(activity, GravarActivity::class.java)
    )
}

@JavascriptInterface
fun abrirPermissoes() {
    try {
        val intent = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        ).apply {
            data = Uri.parse("package:${activity.packageName}")
        }

        activity.startActivity(intent)

    } catch (_: Exception) {
    }
}

    @JavascriptInterface
    fun ligarDireto(numero: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$numero")
        }

        activity.startActivity(intent)
    }
    

}