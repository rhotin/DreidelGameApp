package com.rhappdeveloper.dreidelgameapp.model

enum class DreidelOutcome {
    DO_NOTHING,
    TAKE_POT,
    TAKE_HALF_POT,
    PUT_ONE
}

fun DreidelOutcome.toDisplaySide(ruleSet: DreidelRuleSet): DreidelLandingResult =
    when (this) {
        DreidelOutcome.DO_NOTHING -> DreidelLandingResult.NUN
        DreidelOutcome.TAKE_POT -> DreidelLandingResult.GIMEL
        DreidelOutcome.TAKE_HALF_POT -> DreidelLandingResult.HEI
        DreidelOutcome.PUT_ONE -> if (ruleSet == DreidelRuleSet.ISRAEL) DreidelLandingResult.PEY
        else DreidelLandingResult.SHIN
    }