package com.mulheres 

import java.io.File
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.webkit.JavascriptInterface
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class Cripto(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("dados_seguro", Context.MODE_PRIVATE)

    private val alias = "Cripto_AES256_Key"

    private val keyStore: KeyStore =
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

    private fun getOrCreateKey(): SecretKey {

        if (keyStore.containsAlias(alias)) {
            return (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            "AES",
            "AndroidKeyStore"
        )

        keyGenerator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                alias,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(
                    android.security.keystore.KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .build()
        )

        return keyGenerator.generateKey()
    }

    private fun criptografar(valor: String): String {

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val iv = cipher.iv
        val encrypted = cipher.doFinal(
            valor.toByteArray(StandardCharsets.UTF_8)
        )

        // Guarda IV + conteúdo criptografado
        val resultado = ByteArray(iv.size + encrypted.size)

        System.arraycopy(
            iv,
            0,
            resultado,
            0,
            iv.size
        )

        System.arraycopy(
            encrypted,
            0,
            resultado,
            iv.size,
            encrypted.size
        )

        return Base64.encodeToString(
            resultado,
            Base64.NO_WRAP
        )
    }
    
    fun criptografarArquivo(
    arquivoEntrada: File,
    arquivoSaida: File
) {

    val cipher = Cipher.getInstance(
        "AES/GCM/NoPadding"
    )

    cipher.init(
        Cipher.ENCRYPT_MODE,
        getOrCreateKey()
    )

    val iv = cipher.iv

    arquivoEntrada.inputStream().use { input ->

        arquivoSaida.outputStream().use { output ->

            // Primeiro grava o IV
            output.write(iv.size)
            output.write(iv)

            // Depois grava o conteúdo criptografado
            val cipherOutput =
                javax.crypto.CipherOutputStream(
                    output,
                    cipher
                )

            cipherOutput.use { encryptedOutput ->

                val buffer = ByteArray(8192)

                var bytesLidos: Int

                while (
                    input.read(buffer).also {
                        bytesLidos = it
                    } != -1
                ) {

                    encryptedOutput.write(
                        buffer,
                        0,
                        bytesLidos
                    )
                }
            }
        }
    }
}


fun descriptografarArquivo(
    arquivoEntrada: File,
    arquivoSaida: File
) {

    arquivoEntrada.inputStream().use { input ->

        val ivSize = input.read()

        if (ivSize <= 0) {
            throw IllegalStateException(
                "IV inválido"
            )
        }

        val iv = ByteArray(ivSize)

        var total = 0

        while (total < ivSize) {

            val lidos =
                input.read(
                    iv,
                    total,
                    ivSize - total
                )

            if (lidos == -1) {
                throw IllegalStateException(
                    "Arquivo criptografado incompleto"
                )
            }

            total += lidos
        }

        val cipher =
            Cipher.getInstance(
                "AES/GCM/NoPadding"
            )

        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(
                128,
                iv
            )
        )

        val cipherInput =
            javax.crypto.CipherInputStream(
                input,
                cipher
            )

        arquivoSaida.outputStream().use { output ->

            cipherInput.use { encryptedInput ->

                val buffer = ByteArray(8192)

                var bytesLidos: Int

                while (
                    encryptedInput.read(buffer).also {
                        bytesLidos = it
                    } != -1
                ) {

                    output.write(
                        buffer,
                        0,
                        bytesLidos
                    )
                }
            }
        }
    }
}

    private fun descriptografar(valor: String): String {

        val dados = Base64.decode(
            valor,
            Base64.NO_WRAP
        )

        // GCM normalmente usa IV de 12 bytes
        val iv = dados.copyOfRange(0, 12)

        val encrypted = dados.copyOfRange(
            12,
            dados.size
        )

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(128, iv)
        )

        return String(
            cipher.doFinal(encrypted),
            StandardCharsets.UTF_8
        )
    }

    
    fun salvar(chave: String, valor: String) {

        val valorCriptografado = criptografar(valor)

        prefs.edit()
            .putString(chave, valorCriptografado)
            .apply()
    }

    
    fun carregar(chave: String): String {

        val valorCriptografado = prefs.getString(chave, null)
            ?: return ""

        return try {
            descriptografar(valorCriptografado)
        } catch (e: Exception) {
            ""
        }
    }

    
    fun remover(chave: String) {

        prefs.edit()
            .remove(chave)
            .apply()
    }

    
    fun limparTudo() {

        prefs.edit()
            .clear()
            .apply()
    }
}