package com.rhappdeveloper.dreidelgameapp.fakes

import com.rhappdeveloper.dreidelgameapp.domain.DreidelSideProvider
import com.rhappdeveloper.dreidelgameapp.model.DreidelOutcome

class FakeDreidelOutcomeProvider(
    private val outcome: DreidelOutcome
) : DreidelSideProvider {
    override fun next() = outcome
}