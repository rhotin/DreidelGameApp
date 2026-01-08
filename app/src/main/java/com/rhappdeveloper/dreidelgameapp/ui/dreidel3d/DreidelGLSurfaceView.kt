package com.rhappdeveloper.dreidelgameapp.ui.dreidel3d

import android.content.Context
import android.opengl.GLSurfaceView
import com.rhappdeveloper.dreidelgameapp.model.DreidelRuleSet
import com.rhappdeveloper.dreidelgameapp.model.DreidelSpinAnimationState

class DreidelGLSurfaceView(
    context: Context,
) : GLSurfaceView(context) {

    private val renderer = DreidelRenderer()
    var onSpinFinished: (() -> Unit)? = null

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY

        renderer.onSpinFinished = {
            post {
                onSpinFinished?.invoke()
            }
        }
    }

    fun updateSpinState(state: DreidelSpinAnimationState) {
        queueEvent {
            renderer.updateSpinState(state)
        }
    }

    fun setRuleMode(ruleMode: DreidelRuleSet) {
        queueEvent {
            renderer.setRuleModeInternal(ruleMode)
        }
    }
}
