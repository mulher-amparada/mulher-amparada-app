package com.mulheres

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat

class EntradaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.setStatusBarColor(android.graphics.Color.TRANSPARENT)
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT)

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->

                        VideoView(context).apply {

                            setVideoPath(
                                "android.resource://${context.packageName}/${R.raw.entrada}"
                            )

                            setMediaController(null)

                            setOnPreparedListener { mediaPlayer ->
                                mediaPlayer.isLooping = false
                                start()
                            }

                            setOnCompletionListener {
                                startActivity(
                                    Intent(
                                        this@EntradaActivity,
                                        MainActivity::class.java
                                    )
                                )

                                finish()
                            }
                        }
                    }
                )
            }
        }
    }
}