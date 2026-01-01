package com.rhappdeveloper.dreidelgameapp.model

data class DreidelState(
    val pot: Int = 10,
    val potBefore: Int = 10,
    val potDelta: Int = 0,
    val isSpinning: Boolean = false,
    val lastSide: DreidelLandingResult? = null,
)