package com.rhappdeveloper.dreidelgameapp.mvi

sealed class DreidelIntent {
    object Spin : DreidelIntent()
    object Reset : DreidelIntent()
}