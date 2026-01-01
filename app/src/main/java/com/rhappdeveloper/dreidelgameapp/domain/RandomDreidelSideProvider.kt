package com.rhappdeveloper.dreidelgameapp.domain

import com.rhappdeveloper.dreidelgameapp.model.DreidelOutcome
import javax.inject.Inject

class RandomDreidelSideProvider @Inject constructor() : DreidelSideProvider {
    override fun next(): DreidelOutcome = DreidelOutcome.entries.random()
}