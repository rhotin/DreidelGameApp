package com.rhappdeveloper.dreidelgameapp.domain

import com.rhappdeveloper.dreidelgameapp.model.DreidelOutcome

fun interface DreidelSideProvider {
    fun next(): DreidelOutcome
}