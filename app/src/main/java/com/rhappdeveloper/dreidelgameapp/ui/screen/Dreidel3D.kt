package com.rhappdeveloper.dreidelgameapp.ui.screen

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.rhappdeveloper.dreidelgameapp.model.DreidelRuleSet
import com.rhappdeveloper.dreidelgameapp.model.DreidelSpinAnimationState
import com.rhappdeveloper.dreidelgameapp.ui.dreidel3d.DreidelGLSurfaceView

@Composable
fun Dreidel3D(
    spinState: DreidelSpinAnimationState,
    ruleMode: DreidelRuleSet,
    onSpinFinished: () -> Unit
) {
    AndroidView(
        factory = { context ->
            DreidelGLSurfaceView(context).apply {
//                updateSpinState(spinState)
//                setRuleMode(ruleMode)
                this.onSpinFinished = onSpinFinished
            }
        },
        update = { view ->
            view.updateSpinState(spinState)
            view.setRuleMode(ruleMode)
        },
        modifier = Modifier.size(180.dp)
    )
}