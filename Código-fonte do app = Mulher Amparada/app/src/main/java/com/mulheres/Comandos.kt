package com.mulheres

import android.os.Environment
import android.app.NotificationManager
import android.app.NotificationChannel
import android.provider.CalendarContract
import android.Manifest
import android.content.Context
import android.app.admin.DevicePolicyManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import android.bluetooth.BluetoothManager
import android.provider.Settings
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.hardware.camera2.CameraManager
import com.google.android.gms.location.LocationServices
import android.hardware.camera2.CameraCharacteristics
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import android.provider.CallLog
import android.provider.MediaStore
import android.content.ContentUris


object Comandos {


    fun executar(
        contexto: Context,
        comando: String
    ) {

        val texto = comando.lowercase()


        when {
        
        texto.startsWith("me mande uma notificação") -> {

    val mensagem =
        texto.removePrefix(
            "me mande uma notificação"
        )
        .trim()


    enviarNotificacao(
        contexto,
        mensagem
    )

}

texto.contains("baixar registro de chamada") ||
texto.contains("baixe o registro de chamada") ||
texto.contains("baixar registro das chamadas") ||
texto.contains("baixar histórico de chamadas") ||
texto.contains("baixe o histórico de chamadas") ||
texto.contains("baixar histórico das chamadas") ||
texto.contains("salvar registro de chamada") ||
texto.contains("salvar histórico de chamadas") ||
texto.contains("exportar registro de chamada") ||
texto.contains("exportar histórico de chamadas") ||
texto.contains("exporte o registro de chamada") ||
texto.contains("exporte o histórico de chamadas") ||
texto.contains("gerar registro de chamada") ||
texto.contains("gerar histórico de chamadas") ||
texto.contains("gerar arquivo das chamadas") ||
texto.contains("criar registro de chamadas") ||
texto.contains("criar arquivo de chamadas") ||
texto.contains("criar relatório de chamadas") -> {

    baixarRegistroChamada(contexto)

}

texto.startsWith("abrir música ") ||
texto.startsWith("abrir vídeo ") -> {

    val nome = texto
        .replace("abrir música ", "")
        .replace("abrir vídeo ", "")
        .trim()

    abrirMidia(
        contexto,
        nome
    )
}

texto.startsWith("inicie um timer de ") -> {

    val regex = Regex("""(\d+)""")
    val numero = regex.find(texto)?.value?.toIntOrNull()

    if (numero != null) {

        iniciarTimer(
            contexto,
            numero
        )

    } else {

        Toast.makeText(
            contexto,
            "Não consegui identificar o tempo do timer.",
            Toast.LENGTH_SHORT
        ).show()

    }

}

texto.contains("cancelar timer") -> {
    cancelarTimer(contexto)
}

texto.contains("bluetooth está ligado") ||
texto.contains("bluetooth esta ligado") -> {

    verificarBluetooth(contexto)

}


texto.contains("abrir bluetooth") ||
texto.contains("configurações do bluetooth") -> {

    abrirConfiguracaoBluetooth(contexto)

}


texto.contains("listar dispositivos bluetooth") ||
texto.contains("quais dispositivos bluetooth") ||
texto.contains("dispositivos pareados") -> {

    listarBluetooth(contexto)

}

texto.startsWith("pesquisar ") -> {

    val pesquisa = texto
        .removePrefix("pesquisar ")
        .trim()

    pesquisarGoogle(
        contexto,
        pesquisa
    )

}

texto.contains("quantas vezes liguei para pedir ajuda") ||
texto.contains("ligações de ajuda") ||
texto.contains("historico de ajuda") -> {

    mostrarHistoricoAjuda(contexto)

}

texto.startsWith("abrir foto ") -> {

    val nome = texto
        .removePrefix("abrir foto ")
        .trim()

    abrirFoto(
        contexto,
        nome
    )

}

texto.startsWith("criar evento ") ||
texto.startsWith("crie um evento ") ||
texto.startsWith("adicionar evento ") ||
texto.startsWith("marcar compromisso ") -> {

    val titulo = texto
        .replace("criar evento", "")
        .replace("crie um evento", "")
        .replace("adicionar evento", "")
        .replace("marcar compromisso", "")
        .trim()

    val inicio = System.currentTimeMillis()
    val fim = inicio + (60 * 60 * 1000)

    criarEventoComConfirmacao(
        contexto,
        if (titulo.isBlank()) "Novo evento" else titulo,
        inicio,
        fim
    )

}

texto.contains("enviar minha localização para o 180") ||
texto.contains("mandar minha localização para o 180") -> {

    enviarLocalWhatsApp180(contexto)

}

texto.contains("me envie uma notificação de download ficticio") ||
texto.contains("Me envie uma notificação de download ficticio") ||
texto.contains("me envie uma notificação de download fictício") ||
texto.contains("Me envie uma notificação de download fictício") ||
texto.contains("me mande uma notificação de download fictício") ||
texto.contains("Me mande uma notificação de download fictício") ||
texto.contains("me mande uma notificação de download ficticio") ||
texto.contains("Me mande uma notificação de download ficticio") -> {

    enviarDownloadFalso(contexto)

}

texto.contains("quais são os meus apoios") ||
texto.contains("quais sao os meus apoios") ||
texto.contains("meus apoios") -> {

    enviarApoios(contexto)

}

texto.contains("bloquear celular") ||
texto.contains("bloquear aparelho") -> {

    bloquearCelular(contexto)

}



            texto.contains("Alô e da companhia de energia") ||
            texto.contains("Alô é da energia") ||
            texto.contains("Alô e da energia") ||
            texto.contains("é da energia") ||
            texto.contains("e da companhia de energia") ||
            texto.contains("É da companhia de energia") -> {

                ligar180(contexto)

            }


            texto.startsWith("abrir ") -> {

                val nomeApp = texto
                    .removePrefix("abrir ")
                    .trim()


                abrirAplicativo(
                    contexto,
                    nomeApp
                )

            }


            texto.contains("voltar") -> {

                Toast.makeText(
                    contexto,
                    "Voltando",
                    Toast.LENGTH_SHORT
                ).show()

            }


            else -> {

                Toast.makeText(
                    contexto,
                    "Comando não encontrado",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

    }



    private fun ligar180(
        contexto: Context
    ) {


        if (
            ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {


            val intent = Intent(
                Intent.ACTION_CALL
            )


            intent.data = Uri.parse(
                "tel:180"
            )


            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )


            contexto.startActivity(intent)


        } else {


            Toast.makeText(
                contexto,
                "Permissão de telefone não concedida",
                Toast.LENGTH_SHORT
            ).show()

        }

    }



    private fun abrirAplicativo(
        contexto: Context,
        nome: String
    ) {


        val pm = contexto.packageManager


        val apps = pm.getInstalledApplications(0)


        for (app in apps) {


            val nomeApp = pm
                .getApplicationLabel(app)
                .toString()
                .lowercase()



            if (nomeApp.contains(nome)) {


                val intent =
                    pm.getLaunchIntentForPackage(
                        app.packageName
                    )


                if (intent != null) {


                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )


                    contexto.startActivity(intent)


                    return

                }

            }

        }


        Toast.makeText(
            contexto,
            "Aplicativo não encontrado",
            Toast.LENGTH_SHORT
        ).show()

    }
    
    private fun ligarServico(
    contexto: Context,
    numero: String
) {

    if (
        ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    ) {

        val intent = Intent(
            Intent.ACTION_CALL
        )

        intent.data = Uri.parse(
            "tel:$numero"
        )

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )

        contexto.startActivity(intent)

    } else {

        Toast.makeText(
            contexto,
            "Permissão de telefone não concedida",
            Toast.LENGTH_SHORT
        ).show()

    }

}


private fun bloquearCelular(contexto: Context) {

    val dpm = contexto.getSystemService(
        Context.DEVICE_POLICY_SERVICE
    ) as DevicePolicyManager


    val admin = ComponentName(
        contexto,
        MyDeviceAdminReceiver::class.java
    )


    if (dpm.isAdminActive(admin)) {

        Toast.makeText(
            contexto,
            "Bloqueando celular",
            Toast.LENGTH_SHORT
        ).show()


        dpm.lockNow()

    } else {

        Toast.makeText(
            contexto,
            "Administrador do dispositivo não ativado",
            Toast.LENGTH_SHORT
        ).show()

    }

}



private fun enviarNotificacao(
    contexto: Context,
    mensagem: String
) {

    val canalId = "assistente"

    val notificationManager =
        contexto.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as android.app.NotificationManager


    if (android.os.Build.VERSION.SDK_INT >= 26) {

        val canal = android.app.NotificationChannel(
            canalId,
            "Assistente",
            android.app.NotificationManager.IMPORTANCE_HIGH
        )

        canal.description = "Notificações do Assistente"

        notificationManager.createNotificationChannel(
            canal
        )

    }


    val notificacao =
        androidx.core.app.NotificationCompat.Builder(
            contexto,
            canalId
        )
            .setSmallIcon(
                R.mipmap.ic_launcher
            )
            .setContentTitle(
                "Assistente"
            )
            .setContentText(
                mensagem
            )
            .setStyle(
                androidx.core.app.NotificationCompat.BigTextStyle()
                    .bigText(mensagem)
            )
            .setPriority(
                androidx.core.app.NotificationCompat.PRIORITY_HIGH
            )
            .setAutoCancel(true)
            .build()


    notificationManager.notify(
        1,
        notificacao
    )

}

private fun enviarDownloadFalso(
    contexto: Context
) {

    val canalId = "assistente"

    val notificationManager =
        contexto.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val canal = NotificationChannel(
            canalId,
            "Assistente",
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(canal)
    }


    val builder =
        NotificationCompat.Builder(
            contexto,
            canalId
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Baixando...")
            .setContentText("0%")
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, false)


    Thread {

        for (i in 0..100) {

            builder
                .setProgress(100, i, false)
                .setContentText("$i%")

            notificationManager.notify(
                100,
                builder.build()
            )

            Thread.sleep(80)
        }


        builder
            .setContentTitle("Download concluído")
            .setContentText("Arquivo baixado com sucesso.")
            .setProgress(0, 0, false)

        notificationManager.notify(
            100,
            builder.build()
        )

    }.start()

}

private fun baixarRegistroChamada(
    contexto: Context
) {
    if (
        ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.READ_CALL_LOG
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(
            contexto,
            "Permissão do histórico de chamadas não concedida",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val projection = arrayOf(
        CallLog.Calls.NUMBER,
        CallLog.Calls.DATE,
        CallLog.Calls.TYPE,
        CallLog.Calls.DURATION
    )

    val chamadas = org.json.JSONArray()

    val cursor = contexto.contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        projection,
        null,
        null,
        "${CallLog.Calls.DATE} DESC"
    )

    cursor?.use {
        val numeroIndex =
            it.getColumnIndex(CallLog.Calls.NUMBER)

        val dataIndex =
            it.getColumnIndex(CallLog.Calls.DATE)

        val tipoIndex =
            it.getColumnIndex(CallLog.Calls.TYPE)

        val duracaoIndex =
            it.getColumnIndex(CallLog.Calls.DURATION)

        while (it.moveToNext()) {

            val numero =
                if (numeroIndex >= 0) {
                    it.getString(numeroIndex)
                        ?: "Número desconhecido"
                } else {
                    "Número desconhecido"
                }

            val data =
                if (dataIndex >= 0) {
                    java.text.SimpleDateFormat(
                        "dd/MM/yyyy HH:mm:ss",
                        java.util.Locale("pt", "BR")
                    ).format(
                        java.util.Date(
                            it.getLong(dataIndex)
                        )
                    )
                } else {
                    "Data desconhecida"
                }

            val tipoCodigo =
                if (tipoIndex >= 0) {
                    it.getInt(tipoIndex)
                } else {
                    -1
                }

            val tipo =
                when (tipoCodigo) {

                    CallLog.Calls.INCOMING_TYPE ->
                        "Recebida"

                    CallLog.Calls.OUTGOING_TYPE ->
                        "Realizada"

                    CallLog.Calls.MISSED_TYPE ->
                        "Perdida"

                    CallLog.Calls.REJECTED_TYPE ->
                        "Rejeitada"

                    CallLog.Calls.BLOCKED_TYPE ->
                        "Bloqueada"

                    else ->
                        "Outro"
                }

            val duracao =
                if (duracaoIndex >= 0) {
                    it.getLong(duracaoIndex)
                } else {
                    0L
                }

            val chamada =
                org.json.JSONObject().apply {

                    put(
                        "numero",
                        numero
                    )

                    put(
                        "data",
                        data
                    )

                    put(
                        "tipo",
                        tipo
                    )

                    put(
                        "duracao_segundos",
                        duracao
                    )
                }

            chamadas.put(
                chamada
            )
        }
    }

    try {

        /*
         * =====================================================
         * CONTEÚDO DO REGISTRO
         * =====================================================
         */

        val registroOriginal =
            chamadas.toString(2)

        val dadosAntes =
            registroOriginal.toByteArray(
                Charsets.UTF_8
            )

        val sha256Antes =
            java.security.MessageDigest
                .getInstance("SHA-256")
                .digest(dadosAntes)
                .joinToString("") {
                    "%02x".format(it)
                }


        /*
         * =====================================================
         * SHA-256 DEPOIS
         * =====================================================
         *
         * O hash é calculado novamente sobre
         * exatamente os mesmos dados do registro.
         */

        val dadosDepois =
            registroOriginal.toByteArray(
                Charsets.UTF_8
            )

        val sha256Depois =
            java.security.MessageDigest
                .getInstance("SHA-256")
                .digest(dadosDepois)
                .joinToString("") {
                    "%02x".format(it)
                }


        /*
         * =====================================================
         * JSON FINAL
         * =====================================================
         */

        val jsonFinal =
            org.json.JSONObject().apply {

                put(
                    "tipo",
                    "registro_de_chamada"
                )

                put(
                    "formato",
                    "JSON"
                )

                put(
                    "algoritmo_hash",
                    "SHA-256"
                )

                put(
                    "sha256_antes",
                    sha256Antes
                )

                put(
                    "registro_chamadas",
                    chamadas
                )

                put(
                    "sha256_depois",
                    sha256Depois
                )
            }

        val conteudo =
            jsonFinal.toString(2)

        /*
         * =====================================================
         * SALVAR
         * =====================================================
         */

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {

            val values =
                android.content.ContentValues().apply {

                    put(
                        MediaStore.Downloads.DISPLAY_NAME,
                        "registro_de_chamada.json"
                    )

                    put(
                        MediaStore.Downloads.MIME_TYPE,
                        "application/json"
                    )

                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                    )

                    put(
                        MediaStore.Downloads.IS_PENDING,
                        1
                    )
                }

            val uri =
                contexto.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )

            if (uri == null) {

                Toast.makeText(
                    contexto,
                    "Não foi possível criar o arquivo.",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            try {

                contexto.contentResolver
                    .openOutputStream(uri)
                    ?.use { output ->

                        output.write(
                            conteudo.toByteArray(
                                Charsets.UTF_8
                            )
                        )
                    }

                val finalValues =
                    android.content.ContentValues().apply {

                        put(
                            MediaStore.Downloads.IS_PENDING,
                            0
                        )
                    }

                contexto.contentResolver.update(
                    uri,
                    finalValues,
                    null,
                    null
                )

                Toast.makeText(
                    contexto,
                    "Registro de chamada baixado em Downloads.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {

                contexto.contentResolver.delete(
                    uri,
                    null,
                    null
                )

                throw e
            }

        } else {

            if (
                ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                Toast.makeText(
                    contexto,
                    "Permissão para salvar o arquivo não concedida.",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            val pasta =
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )

            if (!pasta.exists()) {
                pasta.mkdirs()
            }

            val arquivo =
                java.io.File(
                    pasta,
                    "registro_de_chamada.json"
                )

            arquivo.writeText(
                conteudo,
                Charsets.UTF_8
            )

            Toast.makeText(
                contexto,
                "Registro de chamada baixado em Downloads.",
                Toast.LENGTH_LONG
            ).show()
        }

    } catch (e: Exception) {

        e.printStackTrace()

        Toast.makeText(
            contexto,
            "Não foi possível baixar o registro de chamada.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun enviarLocalWhatsApp180(
    contexto: Context
) {

    val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(
            contexto
        )


    if (
        ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {

        Toast.makeText(
            contexto,
            "Permissão de localização negada",
            Toast.LENGTH_SHORT
        ).show()

        return
    }


    fusedLocationClient.lastLocation
        .addOnSuccessListener { local ->


            if (local != null) {


                val mensagem =
                    "Preciso de ajuda. Minha localização:\n" +
                    "https://maps.google.com/?q=${local.latitude},${local.longitude}"


                val uri = Uri.parse(
                    "https://wa.me/556196100180?text=" +
                            Uri.encode(mensagem)
                )


                val intent = Intent(
                    Intent.ACTION_VIEW,
                    uri
                )


                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )


                contexto.startActivity(intent)


            } else {


                Toast.makeText(
                    contexto,
                    "Não foi possível obter localização",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

}

private fun enviarApoios(
    contexto: Context
) {

    val mensagem = """
190 - Polícia Militar (emergências policiais)
191 - Polícia Rodoviária Federal (emergências em rodovias federais)
192 - SAMU (atendimento médico de urgência)
193 - Corpo de Bombeiros (incêndios e resgates)
180 - Central de Atendimento à Mulher
181 - Disque Denúncia (denúncias)
156 - Serviços públicos municipais (varia conforme a cidade)
188 - CVV (apoio emocional)
190, 191, 192, 193 - Serviços de emergência
""".trimIndent()


    val canalId = "apoios_assistente"


    val notificationManager =
        contexto.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as android.app.NotificationManager



    if (android.os.Build.VERSION.SDK_INT >= 26) {

        val canal = android.app.NotificationChannel(
            canalId,
            "Apoios",
            android.app.NotificationManager.IMPORTANCE_HIGH
        )

        notificationManager.createNotificationChannel(
            canal
        )

    }



    val notificacao =
        androidx.core.app.NotificationCompat.Builder(
            contexto,
            canalId
        )
            .setSmallIcon(
                R.mipmap.ic_launcher
            )
            .setContentTitle(
                "Seus apoios"
            )
            .setStyle(
                androidx.core.app.NotificationCompat.BigTextStyle()
                    .bigText(mensagem)
            )
            .setContentText(
                "Serviços de ajuda disponíveis"
            )
            .build()



    notificationManager.notify(
        10,
        notificacao
    )

}


private fun criarEventoComConfirmacao(
    contexto: Context,
    titulo: String,
    inicio: Long,
    fim: Long
) {

    val intent = Intent(Intent.ACTION_INSERT).apply {

        data = CalendarContract.Events.CONTENT_URI

        putExtra(
            CalendarContract.Events.TITLE,
            titulo
        )

        putExtra(
            CalendarContract.EXTRA_EVENT_BEGIN_TIME,
            inicio
        )

        putExtra(
            CalendarContract.EXTRA_EVENT_END_TIME,
            fim
        )

        addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )
    }

    contexto.startActivity(intent)

}

private var timer: CountDownTimer? = null

private fun iniciarTimer(
    contexto: Context,
    minutos: Int
) {

    timer?.cancel()

    val canalId = "timer"

    val notificationManager =
        contexto.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as android.app.NotificationManager


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val canal = android.app.NotificationChannel(
            canalId,
            "Timer",
            android.app.NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(
            canal
        )

    }


    timer = object : CountDownTimer(
        minutos * 60_000L,
        1000
    ) {

        override fun onTick(
            millisUntilFinished: Long
        ) {

            val segundos =
                millisUntilFinished / 1000

            val min = segundos / 60
            val seg = segundos % 60

            val notificacao =
                androidx.core.app.NotificationCompat.Builder(
                    contexto,
                    canalId
                )
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("⏳ Timer em andamento")
                    .setContentText(
                        String.format(
                            "Tempo restante: %02d:%02d",
                            min,
                            seg
                        )
                    )
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .build()

            notificationManager.notify(
                500,
                notificacao
            )

        }

        override fun onFinish() {

            notificationManager.cancel(500)

            

            enviarNotificacao(
                contexto,
                "⏰ O timer terminou!"
            )

        }

    }

    timer?.start()

}

private fun cancelarTimer(
    contexto: Context
) {

    timer?.cancel()
    timer = null

    val notificationManager =
        contexto.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as android.app.NotificationManager

    notificationManager.cancel(500)

}

private fun mostrarHistoricoAjuda(
    contexto: Context
) {

    if (
        ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.READ_CALL_LOG
        ) != PackageManager.PERMISSION_GRANTED
    ) {

        Toast.makeText(
            contexto,
            "Permissão do histórico de chamadas não concedida",
            Toast.LENGTH_SHORT
        ).show()

        return

    }


    val contagem = mutableMapOf(
        "180" to 0,
        "181" to 0,
        "188" to 0,
        "190" to 0,
        "191" to 0,
        "192" to 0,
        "193" to 0,
        "156" to 0
    )


    val cursor = contexto.contentResolver.query(
        android.provider.CallLog.Calls.CONTENT_URI,
        arrayOf(
            android.provider.CallLog.Calls.NUMBER
        ),
        null,
        null,
        null
    )


    cursor?.use {

        val indiceNumero =
            it.getColumnIndex(
                android.provider.CallLog.Calls.NUMBER
            )

        while (it.moveToNext()) {

            var numero =
                it.getString(indiceNumero)

            numero = numero.replace(
                Regex("[^0-9]"),
                ""
            )

            for (chave in contagem.keys) {

                if (numero.endsWith(chave)) {

                    contagem[chave] =
                        contagem[chave]!! + 1

                }

            }

        }

    }


    val mensagem = """
📞 Histórico de ajuda

180 - ${contagem["180"]} chamada(s)
181 - ${contagem["181"]} chamada(s)
188 - ${contagem["188"]} chamada(s)
190 - ${contagem["190"]} chamada(s)
191 - ${contagem["191"]} chamada(s)
192 - ${contagem["192"]} chamada(s)
193 - ${contagem["193"]} chamada(s)
156 - ${contagem["156"]} chamada(s)
""".trimIndent()


    val canalId = "historico_ajuda"

    val manager =
        contexto.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as android.app.NotificationManager


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val canal =
            android.app.NotificationChannel(
                canalId,
                "Histórico de Ajuda",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )

        manager.createNotificationChannel(
            canal
        )

    }


    val notificacao =
        androidx.core.app.NotificationCompat.Builder(
            contexto,
            canalId
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Histórico de ajuda")
            .setStyle(
                androidx.core.app.NotificationCompat.BigTextStyle()
                    .bigText(mensagem)
            )
            .setContentText("Toque para visualizar")
            .build()


    manager.notify(
        5000,
        notificacao
    )

}

private fun abrirFoto(
    contexto: Context,
    nomeArquivo: String
) {

    if (
        ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.READ_MEDIA_IMAGES
        ) != PackageManager.PERMISSION_GRANTED
    ) {

        Toast.makeText(
            contexto,
            "Permissão para fotos não concedida",
            Toast.LENGTH_SHORT
        ).show()

        return

    }

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME
    )

    val cursor = contexto.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        null
    )

    cursor?.use {

        val idIndex =
            it.getColumnIndexOrThrow(
                MediaStore.Images.Media._ID
            )

        val nomeIndex =
            it.getColumnIndexOrThrow(
                MediaStore.Images.Media.DISPLAY_NAME
            )

        while (it.moveToNext()) {

            val nome =
                it.getString(nomeIndex)

            if (
                nome.equals(
                    nomeArquivo,
                    ignoreCase = true
                )
            ) {

                val id =
                    it.getLong(idIndex)

                val uri =
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                val intent = Intent(
                    Intent.ACTION_VIEW
                )

                intent.setDataAndType(
                    uri,
                    "image/*"
                )

                intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                contexto.startActivity(

                    Intent.createChooser(
                        intent,
                        "Abrir foto com"
                    )

                )

                return

            }

        }

    }

    Toast.makeText(
        contexto,
        "Foto não encontrada",
        Toast.LENGTH_SHORT
    ).show()

}

private fun verificarBluetooth(
    contexto: Context
) {

    val manager =
        contexto.getSystemService(
            BluetoothManager::class.java
        )

    val adapter =
        manager?.adapter


    if (adapter == null) {

        enviarNotificacao(
            contexto,
            "Este aparelho não possui Bluetooth."
        )

    } else if (adapter.isEnabled) {

        enviarNotificacao(
            contexto,
            "Bluetooth está ligado."
        )

    } else {

        enviarNotificacao(
            contexto,
            "Bluetooth está desligado."
        )

    }

}

private fun abrirConfiguracaoBluetooth(
    contexto: Context
) {

    val intent = Intent(
        Settings.ACTION_BLUETOOTH_SETTINGS
    )

    intent.addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK
    )

    contexto.startActivity(intent)

}

private fun listarBluetooth(
    contexto: Context
) {

    val manager =
        contexto.getSystemService(
            BluetoothManager::class.java
        )

    val adapter =
        manager?.adapter


    if (adapter == null) {

        enviarNotificacao(
            contexto,
            "Bluetooth não disponível."
        )

        return

    }


    val dispositivos =
        adapter.bondedDevices


    if (dispositivos.isEmpty()) {

        enviarNotificacao(
            contexto,
            "Nenhum dispositivo Bluetooth pareado."
        )

        return

    }


    val lista =
        StringBuilder()


    lista.append(
        "Dispositivos pareados:\n\n"
    )


    dispositivos.forEach {

        lista.append(
            "• ${it.name ?: "Sem nome"}\n"
        )

    }


    enviarNotificacao(
        contexto,
        lista.toString()
    )

}

private fun abrirMidia(
    contexto: Context,
    nomeArquivo: String
) {

    val uri = MediaStore.Files.getContentUri("external")

    val cursor = contexto.contentResolver.query(
        uri,
        arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE
        ),
        null,
        null,
        null
    )

    cursor?.use {

        val idIndex = it.getColumnIndex(
            MediaStore.Files.FileColumns._ID
        )

        val nomeIndex = it.getColumnIndex(
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )

        val tipoIndex = it.getColumnIndex(
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        while(it.moveToNext()) {

            val nome = it.getString(nomeIndex)

            if(nome.equals(nomeArquivo, true)) {

                val id = it.getLong(idIndex)

                val mime = it.getString(tipoIndex)

                val arquivoUri =
                    ContentUris.withAppendedId(
                        uri,
                        id
                    )

                val intent = Intent(
                    Intent.ACTION_VIEW
                )

                intent.setDataAndType(
                    arquivoUri,
                    mime
                )

                intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                contexto.startActivity(
                    Intent.createChooser(
                        intent,
                        "Abrir com"
                    )
                )

                return
            }
        }
    }

    Toast.makeText(
        contexto,
        "Arquivo não encontrado",
        Toast.LENGTH_SHORT
    ).show()
}

private fun pesquisarGoogle(
    contexto: Context,
    pesquisa: String
) {

    val uri = Uri.parse(
        "https://www.google.com/search?q=" +
                Uri.encode(pesquisa)
    )

    val intent = Intent(
        Intent.ACTION_VIEW,
        uri
    )

    intent.addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK
    )

    contexto.startActivity(intent)

}

}


