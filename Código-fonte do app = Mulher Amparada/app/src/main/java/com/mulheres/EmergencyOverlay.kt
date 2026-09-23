package com.mulheres

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun EmergencyOverlay(
    visivel: Boolean,
    aoClicar: () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AnimatedVisibility(

            visible = visivel,

            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(500)
            ),

            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(400)
            ),

            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {

            IconButton(

                onClick = aoClicar,

                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = Color.Red,
                        shape = CircleShape
                    )

            ) {

                Icon(

                    painter = painterResource(
                        id = R.drawable.emergency
                    ),

                    contentDescription =
                        "Compartilhar localização de emergência",

                    tint = Color.White,

                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}