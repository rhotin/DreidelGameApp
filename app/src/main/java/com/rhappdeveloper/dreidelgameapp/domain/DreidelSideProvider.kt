package com.rhappdeveloper.dreidelgameapp.domain

import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult

fun interface DreidelSideProvider {
    fun next(): DreidelLandingResult
}