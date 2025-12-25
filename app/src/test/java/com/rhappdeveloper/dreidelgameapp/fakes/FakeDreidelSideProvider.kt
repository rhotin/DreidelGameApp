package com.rhappdeveloper.dreidelgameapp.fakes

import com.rhappdeveloper.dreidelgameapp.domain.DreidelSideProvider
import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult

class FakeDreidelSideProvider(
    private val result: DreidelLandingResult
) : DreidelSideProvider {
    override fun next() = result
}