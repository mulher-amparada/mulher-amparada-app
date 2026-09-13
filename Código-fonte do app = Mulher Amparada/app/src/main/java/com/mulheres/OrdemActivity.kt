package com.mulheres

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


class OrdemActivity : AppCompatActivity() {

    private val REQUEST_PERMISSIONS = 100
    private val REQUEST_RECONHECIMENTO = 1

    private lateinit var adminComponent: ComponentName

    private val permissoes = arrayOf(

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

    private var indicePermissao = 0


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

            show(
                WindowInsetsCompat.Type.systemBars()
            )

            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val root = FrameLayout(this)

        root.setBackgroundColor(
            Color.BLACK
        )

        setContentView(root)

        val raiz = findViewById<View>(
            android.R.id.content
        )

        aplicarFonte(raiz)

        abrirBiometria()
    }


    private fun aplicarFonte(view: View) {

        val fonte = try {

            Typeface.createFromAsset(
                assets,
                "font.ttf"
            )

        } catch (_: Exception) {

            return
        }

        aplicarFonteRecursivo(
            view,
            fonte
        )
    }


    private fun aplicarFonteRecursivo(
        view: View,
        fonte: Typeface
    ) {

        if (view is TextView) {
            view.typeface = fonte
        }

        if (view is ViewGroup) {

            for (i in 0 until view.childCount) {

                aplicarFonteRecursivo(
                    view.getChildAt(i),
                    fonte
                )
            }
        }
    }


    private fun abrirBiometria() {

        val biometricManager =
            BiometricManager.from(this)

        val resultadoBiometria =
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )

        when (resultadoBiometria) {

            BiometricManager.BIOMETRIC_SUCCESS -> {
                mostrarBiometria()
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {

                Toast.makeText(
                    this,
                    "Biometria não disponível, mas abriremos o serviço para você!",
                    Toast.LENGTH_LONG
                ).show()

                iniciarApp()
            }

            else -> {

                Toast.makeText(
                    this,
                    "Biometria não disponível, mas abriremos o serviço para você!",
                    Toast.LENGTH_LONG
                ).show()

                iniciarApp()
            }
        }
    }


    private fun mostrarBiometria() {

        val executor =
            ContextCompat.getMainExecutor(this)

        val biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object :
                    BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result:
                        BiometricPrompt.AuthenticationResult
                    ) {

                        super.onAuthenticationSucceeded(
                            result
                        )

                        iniciarApp()
                    }


                    override fun onAuthenticationFailed() {

                        super.onAuthenticationFailed()

                        Toast.makeText(
                            this@OrdemActivity,
                            "Biometria não reconhecida.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


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


        val info =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(
                    "Desbloquear o acesso ao assistente de voz"
                )
                .setDescription(
                    "Apenas a usuária cadastrada pode acessar este local"
                )
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .build()


        biometricPrompt.authenticate(
            info
        )
    }


    private fun iniciarApp() {

        indicePermissao = 0

        pedirProximaPermissao()
    }


    private fun pedirProximaPermissao() {

        while (
            indicePermissao < permissoes.size
        ) {

            val permissao =
                permissoes[indicePermissao]

            /*
             * Algumas permissões não existem em
             * determinadas versões do Android.
             */

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    permissao
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                indicePermissao++

                continue
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(permissao),
                REQUEST_PERMISSIONS
            )

            return
        }

        /*
         * Todas as permissões foram tratadas.
         */

        ativarAdministrador()
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

        if (
            requestCode != REQUEST_PERMISSIONS
        ) {
            return
        }

        if (
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {

            indicePermissao++

            pedirProximaPermissao()

        } else {

            /*
             * A usuária recusou esta permissão.
             *
             * Não avançamos automaticamente.
             */

            Toast.makeText(
                this,
                "Permissão não concedida.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun ativarAdministrador() {

        adminComponent =
            ComponentName(
                this,
                MyDeviceAdminReceiver::class.java
            )

        val intent =
            Intent(
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

        iniciarReconhecimento()
    }


    private fun iniciarReconhecimento() {

        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {

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

        try {

            startActivityForResult(
                intent,
                REQUEST_RECONHECIMENTO
            )

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Reconhecimento de voz não disponível.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    @Deprecated("Deprecated in Android API 30")
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

        if (
            requestCode ==
            REQUEST_RECONHECIMENTO &&
            resultCode ==
            RESULT_OK
        ) {

            val resultado =
                data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

            if (
                !resultado.isNullOrEmpty()
            ) {

                Comandos.executar(
                    this,
                    resultado[0]
                )
            }
        }
    }
}