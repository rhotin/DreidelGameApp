package com.rhappdeveloper.dreidelgameapp.domain

import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult

data class DreidelRuleResult(
    val newPot: Int,
    val messageKey: MessageKey,
    val potDelta: Int = 0
)

enum class MessageKey {
    NUN,
    GIMEL,
    HEI,
    SHIN,
    SPINNING
}

class DreidelRules {
    fun apply(pot: Int, side: DreidelLandingResult): DreidelRuleResult {
        return when (side) {
            DreidelLandingResult.NUN ->
                DreidelRuleResult(
                    newPot = pot,
                    messageKey = MessageKey.NUN,
                    potDelta = 0
                )

            DreidelLandingResult.GIMEL ->
                DreidelRuleResult(
                    newPot = 0,
                    messageKey = MessageKey.GIMEL,
                    potDelta = -pot
                )

            DreidelLandingResult.HEI -> {
                val taken = pot / 2
                DreidelRuleResult(
                    newPot = pot - taken,
                    messageKey = MessageKey.HEI,
                    potDelta = -taken
                )
            }

            DreidelLandingResult.SHIN ->
                DreidelRuleResult(
                    newPot = pot + 1,
                    messageKey = MessageKey.SHIN,
                    potDelta = +1
                )
        }
    }
}