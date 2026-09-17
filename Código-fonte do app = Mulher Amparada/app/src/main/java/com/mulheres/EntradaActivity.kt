package com.mulheres

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
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

        window.decorView.alpha = 1f

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

                            // Sem controles
                            setMediaController(null)

                            // Toca automaticamente e apenas uma vez
                            setOnPreparedListener { mediaPlayer ->
                                mediaPlayer.isLooping = false
                                start()
                            }

                            // Quando o vídeo terminar
                            setOnCompletionListener {

                                // Fade out somente pela opacidade
                                window.decorView.animate()
                                    .alpha(0f)
                                    .setDuration(350)
                                    .setInterpolator(
                                        AccelerateDecelerateInterpolator()
                                    )
                                    .setListener(
                                        object : AnimatorListenerAdapter() {
                                            override fun onAnimationEnd(
                                                animation: Animator
                                            ) {
                                                startActivity(
                                                    Intent(
                                                        this@EntradaActivity,
                                                        MainActivity::class.java
                                                    )
                                                )

                                                finish()
                                            }
                                        }
                                    )
                                    .start()
                            }
                        }
                    }
                )
            }
        }
    }
}