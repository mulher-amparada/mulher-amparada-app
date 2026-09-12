package com.mulheres

import android.widget.Toast
import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build

import androidx.core.view.WindowInsetsCompat
import android.os.Bundle

import android.speech.RecognizerIntent

import android.view.View
import android.view.WindowManager

import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.graphics.Typeface

import android.view.ViewGroup




class OrdemActivity : AppCompatActivity() {


    private val REQUEST_PERMISSIONS = 100

    private lateinit var adminComponent: ComponentName

    


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

window.addFlags(
        WindowManager.LayoutParams.FLAG_SECURE
    )

        WindowCompat.setDecorFitsSystemWindows(
    window,
    false
)

window.apply {

    addFlags(
        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
    )

    statusBarColor = Color.TRANSPARENT
    navigationBarColor = Color.TRANSPARENT

    if (Build.VERSION.SDK_INT >= 28) {
        navigationBarDividerColor = Color.TRANSPARENT
    }

    if (Build.VERSION.SDK_INT >= 29) {
        isStatusBarContrastEnforced = false
        isNavigationBarContrastEnforced = false
    }
}

WindowInsetsControllerCompat(
    window,
    window.decorView
).apply {

    // Mantém status bar e navigation bar VISÍVEIS
    show(WindowInsetsCompat.Type.systemBars())

    // Ícones claros
    isAppearanceLightStatusBars = false
    isAppearanceLightNavigationBars = false
}

val root = FrameLayout(this)

root.setBackgroundColor(Color.BLACK)

setContentView(root)
        
        

val raiz = findViewById<View>(
    android.R.id.content
)

aplicarFonte(raiz)


        abrirBiometria()



        

    }


    private fun pedirPermissoes() {

    val permissoes = arrayOf(

        
        
        Manifest.permission.READ_CALENDAR,
        
        
        
        Manifest.permission.WRITE_CALENDAR,

        Manifest.permission.POST_NOTIFICATIONS,

        Manifest.permission.RECORD_AUDIO,

        Manifest.permission.READ_CONTACTS,

        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,

        
        
        Manifest.permission.READ_MEDIA_IMAGES,
Manifest.permission.READ_MEDIA_VIDEO,
Manifest.permission.READ_MEDIA_AUDIO,

Manifest.permission.BLUETOOTH_SCAN,
Manifest.permission.BLUETOOTH_CONNECT,
Manifest.permission.BLUETOOTH_ADVERTISE,

        Manifest.permission.READ_PHONE_STATE,

Manifest.permission.READ_CALL_LOG,

        Manifest.permission.CALL_PHONE

    )


    val faltando = permissoes.filter {

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

} else {

    ativarAdministrador()
    iniciarReconhecimento()

}
}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

if (requestCode == REQUEST_NOTIFICACAO) {

    if (grantResults.isNotEmpty() &&
        grantResults[0] == PackageManager.PERMISSION_GRANTED
    ) {

        // notificações liberadas



    }

}

        if (requestCode == REQUEST_PERMISSIONS) {

            if (grantResults.all {
                    it == PackageManager.PERMISSION_GRANTED
                }) {

                ativarAdministrador()
                iniciarReconhecimento()

            }

        }
    }


private fun aplicarFonte(view: View) {

    val fonte = Typeface.createFromAsset(
        assets,
        "font.ttf"
    )


    if (view is TextView) {

        view.typeface = fonte

    }


    if (view is ViewGroup) {

        for (i in 0 until view.childCount) {

            aplicarFonte(
                view.getChildAt(i)
            )

        }

    }

}


    private fun ativarAdministrador() {

        adminComponent = ComponentName(
            this,
            MyDeviceAdminReceiver::class.java
        )


        val intent = Intent(
            DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
        )


        intent.putExtra(
            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
            adminComponent
        )


        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Ative o administrador para liberar funções de segurança"
        )


        startActivity(intent)

    }



    


    private fun iniciarReconhecimento() {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "pt-BR"
        )
        putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Fale agora"
        )
    }

    startActivityForResult(intent, 1)
}
private val REQUEST_NOTIFICACAO = 200


private fun pedirPermissaoNotificacao() {

    if (android.os.Build.VERSION.SDK_INT >= 33) {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                REQUEST_NOTIFICACAO
            )

        }

    }

}
    
    
    override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == 1 && resultCode == RESULT_OK) {
        val resultado = data?.getStringArrayListExtra(
            RecognizerIntent.EXTRA_RESULTS
        )

        if (!resultado.isNullOrEmpty()) {
            Comandos.executar(this, resultado[0])
        }
    }
}

private fun abrirBiometria() {

    val biometricManager =
        BiometricManager.from(this)

    val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL


    when (
        biometricManager.canAuthenticate(
            authenticators
        )
    ) {

        BiometricManager.BIOMETRIC_SUCCESS -> {
            // Continua e mostra a biometria
        }

        else -> {

            // Não há biometria nem bloqueio de tela compatível
            iniciarApp()

            return
        }
    }


    val executor =
        ContextCompat.getMainExecutor(this)


    val biometricPrompt =
        BiometricPrompt(
            this,
            executor,
            object :
                BiometricPrompt.AuthenticationCallback() {


                // =================================================
                // AUTENTICAÇÃO BEM-SUCEDIDA
                // =================================================

                override fun onAuthenticationSucceeded(
                    result:
                    BiometricPrompt.AuthenticationResult
                ) {

                    super.onAuthenticationSucceeded(
                        result
                    )

                    iniciarApp()
                }


                // =================================================
                // BIOMETRIA NÃO RECONHECIDA
                // =================================================

                override fun onAuthenticationFailed() {
    super.onAuthenticationFailed()

    Toast.makeText(
        this@OrdemActivity,
        "Biometria não reconhecida, abrindo a área protegida.",
        Toast.LENGTH_SHORT
    ).show()
}


                // =================================================
                // ERRO / CANCELAMENTO
                // =================================================

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {

                    super.onAuthenticationError(
                        errorCode,
                        errString
                    )

                    finish()
                }
            }
        )


    // =============================================================
    // TEXTO DO BIOMETRIC PROMPT
    // =============================================================

    val info =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(
                "Desbloquear o acesso ao assistente de voz"
            )
            .setDescription(
                "🌸 Apenas a usuária cadastrada pode acessar este local\n\n" +
                "Use a sua impressão digital\n" +
                "Use seu registro facial\n" +
                "Use seu PIN, padrão ou senha"
            )
            .setAllowedAuthenticators(
                authenticators
            )
            .build()


    biometricPrompt.authenticate(
        info
    )
}

private fun iniciarApp() {

    pedirPermissoes()

}

}