package com.mulheres

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class Cripto(context: Context) {

    private val appContext =
        context.applicationContext

    companion object {

        private const val DATASTORE_NAME =
            "dados_seguro"

        private const val KEYSTORE_ALIAS =
            "Cripto_AES256_Key"

        @Volatile
        private var dataStoreInstance:
            DataStore<Preferences>? = null

        private fun getDataStore(
            context: Context
        ): DataStore<Preferences> {

            return dataStoreInstance
                ?: synchronized(this) {

                    dataStoreInstance
                        ?: PreferenceDataStoreFactory.create(
                            migrations = listOf(
                                SharedPreferencesMigration(
                                    context.applicationContext,
                                    DATASTORE_NAME
                                )
                            ),
                            produceFile = {
                                context.applicationContext
                                    .preferencesDataStoreFile(
                                        DATASTORE_NAME
                                    )
                            }
                        ).also {
                            dataStoreInstance = it
                        }
                }
        }
    }

    private val dataStore:
        DataStore<Preferences> by lazy {

        getDataStore(
            appContext
        )
    }

    private val keyStore: KeyStore =
        KeyStore.getInstance(
            "AndroidKeyStore"
        ).apply {
            load(null)
        }

    private fun getOrCreateKey(): SecretKey {

        if (
            keyStore.containsAlias(
                KEYSTORE_ALIAS
            )
        ) {

            return (
                keyStore.getEntry(
                    KEYSTORE_ALIAS,
                    null
                ) as KeyStore.SecretKeyEntry
            ).secretKey
        }

        val keyGenerator =
            KeyGenerator.getInstance(
                "AES",
                "AndroidKeyStore"
            )

        keyGenerator.init(
            android.security.keystore
                .KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    android.security.keystore
                        .KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore
                            .KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeySize(256)
                    .setBlockModes(
                        android.security.keystore
                            .KeyProperties.BLOCK_MODE_GCM
                    )
                    .setEncryptionPaddings(
                        android.security.keystore
                            .KeyProperties.ENCRYPTION_PADDING_NONE
                    )
                    .build()
        )

        return keyGenerator.generateKey()
    }

    private fun criptografar(
        valor: String
    ): String {

        val cipher =
            Cipher.getInstance(
                "AES/GCM/NoPadding"
            )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val iv =
            cipher.iv

        val encrypted =
            cipher.doFinal(
                valor.toByteArray(
                    StandardCharsets.UTF_8
                )
            )

        val resultado =
            ByteArray(
                iv.size +
                    encrypted.size
            )

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

    private fun descriptografar(
        valor: String
    ): String {

        val dados =
            Base64.decode(
                valor,
                Base64.NO_WRAP
            )

        if (
            dados.size < 13
        ) {
            throw IllegalStateException(
                "Dados criptografados inválidos."
            )
        }

        val iv =
            dados.copyOfRange(
                0,
                12
            )

        val encrypted =
            dados.copyOfRange(
                12,
                dados.size
            )

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

        return String(
            cipher.doFinal(
                encrypted
            ),
            StandardCharsets.UTF_8
        )
    }

    fun salvar(
        chave: String,
        valor: String
    ) {

        runBlocking(
            Dispatchers.IO
        ) {

            val valorCriptografado =
                criptografar(
                    valor
                )

            dataStore.edit { preferences ->

                preferences[
                    stringPreferencesKey(
                        chave
                    )
                ] =
                    valorCriptografado
            }
        }
    }

    fun carregar(
        chave: String
    ): String {

        return runBlocking(
            Dispatchers.IO
        ) {

            val preferences =
                dataStore.data.first()

            val valorCriptografado =
                preferences[
                    stringPreferencesKey(
                        chave
                    )
                ]

            if (
                valorCriptografado.isNullOrEmpty()
            ) {
                return@runBlocking ""
            }

            try {

                descriptografar(
                    valorCriptografado
                )

            } catch (
                e: Exception
            ) {

                ""
            }
        }
    }

    fun remover(
        chave: String
    ) {

        runBlocking(
            Dispatchers.IO
        ) {

            dataStore.edit { preferences ->

                preferences.remove(
                    stringPreferencesKey(
                        chave
                    )
                )
            }
        }
    }

    fun limparTudo() {

        runBlocking(
            Dispatchers.IO
        ) {

            dataStore.edit { preferences ->

                preferences.clear()
            }
        }
    }

    fun criptografarArquivo(
        arquivoEntrada: File,
        arquivoSaida: File
    ) {

        val cipher =
            Cipher.getInstance(
                "AES/GCM/NoPadding"
            )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val iv =
            cipher.iv

        arquivoEntrada.inputStream().use { input ->

            arquivoSaida.outputStream().use { output ->

                output.write(
                    iv.size
                )

                output.write(
                    iv
                )

                CipherOutputStream(
                    output,
                    cipher
                ).use { encryptedOutput ->

                    val buffer =
                        ByteArray(
                            8192
                        )

                    var bytesLidos: Int

                    while (
                        input.read(
                            buffer
                        ).also {
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

            val ivSize =
                input.read()

            if (
                ivSize <= 0
            ) {
                throw IllegalStateException(
                    "IV inválido."
                )
            }

            val iv =
                ByteArray(
                    ivSize
                )

            var total =
                0

            while (
                total < ivSize
            ) {

                val lidos =
                    input.read(
                        iv,
                        total,
                        ivSize - total
                    )

                if (
                    lidos == -1
                ) {
                    throw IllegalStateException(
                        "Arquivo criptografado incompleto."
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

            CipherInputStream(
                input,
                cipher
            ).use { encryptedInput ->

                arquivoSaida.outputStream().use { output ->

                    val buffer =
                        ByteArray(
                            8192
                        )

                    var bytesLidos: Int

                    while (
                        encryptedInput.read(
                            buffer
                        ).also {
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
}