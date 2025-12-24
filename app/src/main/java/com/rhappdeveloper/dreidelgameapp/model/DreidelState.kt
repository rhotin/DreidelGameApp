package com.rhappdeveloper.dreidelgameapp.model

data class DreidelState(
    val pot: Int = 10,
    val previousPot: Int = 10,
    val isSpinning: Boolean = false,
    val lastSide: DreidelLandingResult? = null,
    val message: String = "Tap spin to play"
)