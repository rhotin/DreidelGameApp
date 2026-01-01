package com.rhappdeveloper.dreidelgameapp.mvi

import com.rhappdeveloper.dreidelgameapp.model.DreidelRuleSet

sealed class DreidelIntent {
    data class Spin(val ruleSet: DreidelRuleSet) : DreidelIntent()
    object Reset : DreidelIntent()
}