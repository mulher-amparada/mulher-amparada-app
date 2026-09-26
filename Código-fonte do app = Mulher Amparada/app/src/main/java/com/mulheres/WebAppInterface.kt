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
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.webkit.JavascriptInterface
class WebAppInterface(
    private val activity: Activity
) {

    
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
fun openRecorder() {
    activity.startActivity(
        Intent(activity, GravarActivity::class.java)
    )
}


}
