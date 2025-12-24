package com.rhappdeveloper.dreidelgameapp.ui.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedPot(
    pot: Int,
    previousPot: Int,
) {
    val changed = pot != previousPot
    val diff = pot - previousPot

    val scale by animateFloatAsState(
        targetValue = if (changed) 1.2f else 1f,
        animationSpec = tween(300),
        label = "potScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Pot:",
            fontSize = 18.sp
        )

        Text(
            text = pot.toString(),
            fontSize = 28.sp,
            modifier = Modifier.scale(scale)
        )

        AnimatedVisibility(
            visible = changed && diff != 0,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Text(
                text = if (diff > 0) "+$diff" else diff.toString(),
                fontSize = 16.sp,
                color = if (diff > 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}