package com.mulheres

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun EmergencyOverlay(
    visivel: Boolean,
    aoClicar: () -> Unit
) {

    if (!visivel) {
        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 22.dp,
                    bottom = 115.dp
                )
                .size(86.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = Color.Red,
                    spotColor = Color.Red
                )
                .background(
                    color = Color(0xFFB51224),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFFFF8585),
                    shape = CircleShape
                )
                .padding(5.dp)
                .background(
                    color = Color(0xFFE52A3C),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.28f),
                    shape = CircleShape
                )
        ) {

            IconButton(
                onClick = aoClicar,
                modifier = Modifier.fillMaxSize()
            ) {

                Icon(
                    painter = painterResource(
                        id = R.drawable.emergency
                    ),
                    contentDescription =
                        "Compartilhar localização de emergência",
                    tint = Color.White,
                    modifier = Modifier.size(43.dp)
                )
            }
        }
    }
}