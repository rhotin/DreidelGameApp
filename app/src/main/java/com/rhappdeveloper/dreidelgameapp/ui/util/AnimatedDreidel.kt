package com.rhappdeveloper.dreidelgameapp.ui.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult

@Composable
fun AnimatedDreidel(
    isSpinning: Boolean,
    side: DreidelLandingResult?
) {

    val rotation by animateFloatAsState(
        targetValue = if (isSpinning) 360f else 0f,
        animationSpec = if (isSpinning)
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 300,
                    easing = LinearEasing
                )
            )
        else
            tween(durationMillis = 300),
        label = "dreidelRotation"
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        Text(side?.hebrew ?: "▲", fontSize = 48.sp)
    }
}