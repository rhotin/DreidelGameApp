package com.rhappdeveloper.dreidelgameapp.mvi

import com.rhappdeveloper.dreidelgameapp.model.DreidelRenderMode
import com.rhappdeveloper.dreidelgameapp.model.DreidelRuleSet

sealed class DreidelIntent {
    data class Spin(val ruleSet: DreidelRuleSet) : DreidelIntent()
    object Reset : DreidelIntent()
    data class ToggleRuleMode(val ruleMode: DreidelRuleSet) : DreidelIntent()
    data class ToggleRenderMode(val mode: DreidelRenderMode) : DreidelIntent()
}