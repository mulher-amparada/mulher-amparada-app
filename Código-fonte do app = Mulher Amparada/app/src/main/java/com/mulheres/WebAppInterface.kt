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

class WebAppInterface(
    private val activity: Activity
) {

    private fun safeMainActivityCall(action: (MainActivity) -> Unit) {
        try {
            val act = activity
            if (act is MainActivity) {
                action(act)
            }
        } catch (_: Exception) {
        }
    }
    

@JavascriptInterface
fun desligarPorBarulho(): Boolean {

    return try {

        val gravador =
            android.media.MediaRecorder()

        gravador.setAudioSource(
            android.media.MediaRecorder.AudioSource.MIC
        )

        gravador.setOutputFormat(
            android.media.MediaRecorder.OutputFormat.THREE_GPP
        )

        gravador.setAudioEncoder(
            android.media.MediaRecorder.AudioEncoder.AMR_NB
        )

        gravador.setOutputFile(
            activity.cacheDir.absolutePath + "/temp.3gp"
        )

        gravador.prepare()
        gravador.start()

        Thread {

            try {

                while (true) {

                    val amplitude =
                        gravador.maxAmplitude

                    if (amplitude >= 18000) {

                        gravador.stop()
                        gravador.release()

                        val intent =
                            Intent(
                                activity,
                                MyDeviceAdminReceiver::class.java
                            ).apply {
                                action =
                                    "com.mulheres.BLOQUEAR_CELULAR"
                            }

                        activity.sendBroadcast(intent)

                        break
                    }

                    Thread.sleep(100)
                }

            } catch (_: Exception) {

                try {
                    gravador.release()
                } catch (_: Exception) {
                }

            }

        }.start()

        true

    } catch (_: Exception) {

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

        val component =
            ComponentName(
                activity,
                MyDeviceAdminReceiver::class.java
            )


        val intent =
            Intent(
                DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
            )


        intent.putExtra(
            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
            component
        )


        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Este aplicativo precisa da permissão de Administrador do dispositivo."
        )


        activity.startActivity(intent)


    } catch (e: Exception) {

        e.printStackTrace()

    }

}

    @JavascriptInterface
    fun bloquearTela() {
        val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE)
                as DevicePolicyManager

        val component = ComponentName(
            activity,
            MyDeviceAdminReceiver::class.java
        )

        if (dpm.isAdminActive(component)) {
            dpm.lockNow()
        }
    }

    @JavascriptInterface
    fun openFiles() {
        activity.startActivity(
            Intent(activity, FileActivity::class.java)
        )
    }



@JavascriptInterface
fun abrirOrdem() {
    activity.startActivity(
        Intent(
            activity,
            OrdemActivity::class.java
        )
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
    fun iniciarBiometriaAmor() {
        safeMainActivityCall {
            it.iniciarBiometriaAmor()
        }
    }

    @JavascriptInterface
    fun iniciarBiometriaMusica() {
        safeMainActivityCall {
            it.iniciarBiometriaMusica()
        }
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincesa() {
        safeMainActivityCall {
            it.iniciarBiometriaPrincesa()
        }
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincipe() {
        safeMainActivityCall {
            it.iniciarBiometriaPrincipe()
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
    fun ligarDireto(numero: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$numero")
        }

        activity.startActivity(intent)
    }
    
    @JavascriptInterface
fun ConcederPermissoes() {

    safeMainActivityCall { act ->

        act.runOnUiThread {

            if (act.temPermissoes()) {

                act.atualizarPaginaInicial()

                return@runOnUiThread
            }

            act.pedirPermissoes()
        }
    }
}

@JavascriptInterface
fun verificarPermissoes(): Boolean {

    return if (activity is MainActivity) {
        (activity as MainActivity).temPermissoes()
    } else {
        false
    }
}

}