package com.rhappdeveloper.dreidelgameapp.mvi

sealed class DreidelEffect {
    object SpinSound : DreidelEffect()
    object ResultSound : DreidelEffect()

}