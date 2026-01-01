package com.rhappdeveloper.dreidelgameapp.domain

import com.rhappdeveloper.dreidelgameapp.model.DreidelOutcome

data class DreidelRuleResult(
    val newPot: Int,
    val potDelta: Int = 0
)

class DreidelRules {
    fun apply(pot: Int, side: DreidelOutcome): DreidelRuleResult {
        return when (side) {
            DreidelOutcome.DO_NOTHING ->
                DreidelRuleResult(
                    newPot = pot,
                    potDelta = 0
                )

            DreidelOutcome.TAKE_POT ->
                DreidelRuleResult(
                    newPot = 0,
                    potDelta = -pot
                )

            DreidelOutcome.TAKE_HALF_POT -> {
                val taken = pot / 2
                DreidelRuleResult(
                    newPot = pot - taken,
                    potDelta = -taken
                )
            }

            DreidelOutcome.PUT_ONE ->
                DreidelRuleResult(
                    newPot = pot + 1,
                    potDelta = +1
                )
        }
    }
}