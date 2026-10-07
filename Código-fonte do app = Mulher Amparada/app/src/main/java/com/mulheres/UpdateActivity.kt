package com.mulheres

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val DarkBackground = Color(0xFF070709)
private val DarkCard = Color(0x0FFFFFFF)
private val DarkBorder = Color(0x18FFFFFF)
private val DarkText = Color(0xFFF5F5F7)
private val DarkMuted = Color(0x80FFFFFF)

private val LightBackground = Color(0xFFFFFBFC)
private val LightCard = Color(0xFFFFFFFF)
private val LightBorder = Color(0x18000000)
private val LightText = Color(0xFF151519)
private val LightMuted = Color(0x88000000)

private val Accent = Color(0xFFFF8FB5)
private val AccentSoft = Color(0x24FF8FB5)
private val Success = Color(0xFF6DFFAD)

class UpdateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        window.isNavigationBarContrastEnforced = false

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContent {

            val dark = isSystemInDarkTheme()

            val controller =
                WindowCompat.getInsetsController(
                    window,
                    window.decorView
                )

            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark

            UpdateScreen()
        }
    }
}

@Composable
private fun UpdateScreen() {

    val dark = isSystemInDarkTheme()

    val background =
        if (dark) {
            DarkBackground
        } else {
            LightBackground
        }

    val card =
        if (dark) {
            DarkCard
        } else {
            LightCard
        }

    val border =
        if (dark) {
            DarkBorder
        } else {
            LightBorder
        }

    val textColor =
        if (dark) {
            DarkText
        } else {
            LightText
        }

    val muted =
        if (dark) {
            DarkMuted
        } else {
            LightMuted
        }

    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE

    CompositionLocalProvider(
        LocalOverscrollFactory provides null
    ) {

        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(background)
                    .padding(paddingValues)
                    .padding(
                        start = 18.dp,
                        end = 18.dp,
                        top = 20.dp,
                        bottom = 20.dp
                    ),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 2.dp
                        ),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(
                                RoundedCornerShape(34.dp)
                            )
                            .background(
                                AccentSoft
                            )
                            .border(
                                1.dp,
                                Accent.copy(.28f),
                                RoundedCornerShape(34.dp)
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Image(
                            painter = painterResource(
                                id = R.drawable.ic_launcher_foreground
                            ),
                            contentDescription =
                                "Mulher Amparada",
                            modifier = Modifier
                                .size(72.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Text(
                        text = "Atualização",
                        color = textColor,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(7.dp)
                    )

                    Text(
                        text = "Mulher Amparada",
                        color = Accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(25.dp)
                            )
                            .background(card)
                            .border(
                                1.dp,
                                border,
                                RoundedCornerShape(25.dp)
                            )
                            .padding(20.dp)
                    ) {

                        Text(
                            text = "Versão instalada",
                            color = muted,
                            fontSize = 11.sp
                        )

                        Spacer(
                            modifier = Modifier.height(7.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Success
                                        )
                                )

                                Spacer(
                                    modifier = Modifier.width(9.dp)
                                )

                                Text(
                                    text = "Versão $versionName",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight =
                                        FontWeight.SemiBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        AccentSoft
                                    )
                                    .padding(
                                        horizontal = 11.dp,
                                        vertical = 6.dp
                                    )
                            ) {

                                Text(
                                    text = "Build $versionCode",
                                    color = Accent,
                                    fontSize = 10.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(border)
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Seu aplicativo está pronto para receber atualizações.",
                            color = muted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(25.dp)
                            )
                            .background(card)
                            .border(
                                1.dp,
                                border,
                                RoundedCornerShape(25.dp)
                            )
                            .padding(20.dp)
                    ) {

                        Text(
                            text = "Verificar atualização",
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight =
                                FontWeight.SemiBold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = "Confira se existe uma versão mais recente do Mulher Amparada.",
                            color = muted,
                            fontSize = 11.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Button(
                            onClick = {
                                // Ação de verificar atualização
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape =
                                RoundedCornerShape(16.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Accent,
                                    contentColor = Color.Black
                                )
                        ) {

                            Text(
                                text = "Verificar atualização",
                                fontSize = 13.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        OutlinedButton(
                            onClick = {
                                finish()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape =
                                RoundedCornerShape(16.dp),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor =
                                        if (dark) {
                                            Color.White
                                        } else {
                                            LightText
                                        }
                                ),
                            border =
                                androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    border
                                )
                        ) {

                            Text(
                                text = "Voltar",
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "Mulher Amparada • Build $versionCode",
                        color = muted.copy(.65f),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}