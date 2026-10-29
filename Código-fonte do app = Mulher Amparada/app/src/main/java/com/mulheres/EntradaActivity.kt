package com.mulheres

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class EntradaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        installSplashScreen()

        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val cripto = Cripto(this)

        val chave = "primeira_abertura_concluida"

        val primeiraVez = cripto.carregar(chave).isEmpty()

        if (primeiraVez) {
            cripto.salvar(chave, "sim")
        }

        val destino = if (primeiraVez) {
            WelcomeActivity::class.java
        } else {
            AssistenteActivity::class.java
        }

        startActivity(
            Intent(this, destino)
        )

        finish()
    }
}
