package com.rhappdeveloper.dreidelgameapp.model

import com.rhappdeveloper.dreidelgameapp.domain.MessageKey

data class DreidelState(
    val pot: Int = 10,
    val potDelta: Int = 0,
    val isSpinning: Boolean = false,
    val lastSide: DreidelLandingResult? = null,
    val messageKey: MessageKey? = null,
)