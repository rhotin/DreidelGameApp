package com.rhappdeveloper.dreidelgameapp.model

data class DreidelSpinAnimationState(
    val spinId: Long = 0L,
    val spinning: Boolean = false,
    val initialVelocity: Float = 0f,
    val spins: Int = 0,
    val landingFace: Face? = null
)
