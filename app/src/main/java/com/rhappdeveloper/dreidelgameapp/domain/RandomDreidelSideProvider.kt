package com.rhappdeveloper.dreidelgameapp.domain

import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult
import javax.inject.Inject

class RandomDreidelSideProvider @Inject constructor() : DreidelSideProvider {
    override fun next(): DreidelLandingResult = DreidelLandingResult.entries.toTypedArray().random()
}