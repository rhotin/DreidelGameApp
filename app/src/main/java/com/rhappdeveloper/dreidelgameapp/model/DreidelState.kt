package com.rhappdeveloper.dreidelgameapp.model

data class DreidelState(
    val pot: Int = 10,
    val potBefore: Int = 10,
    val potDelta: Int = 0,
    val isResolvingSpin: Boolean = false,
    val lastSide: DreidelLandingResult? = null,
    val renderMode: DreidelRenderMode = DreidelRenderMode.TWO_D,
    val ruleMode: DreidelRuleSet = DreidelRuleSet.CLASSIC
)