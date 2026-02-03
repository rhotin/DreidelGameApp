package com.rhappdeveloper.dreidelgameapp.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhappdeveloper.dreidelgameapp.domain.DreidelRules
import com.rhappdeveloper.dreidelgameapp.domain.DreidelSideProvider
import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult
import com.rhappdeveloper.dreidelgameapp.model.DreidelRuleSet
import com.rhappdeveloper.dreidelgameapp.model.DreidelState
import com.rhappdeveloper.dreidelgameapp.model.Face
import com.rhappdeveloper.dreidelgameapp.model.DreidelSpinAnimationState
import com.rhappdeveloper.dreidelgameapp.model.toDisplaySide
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

@HiltViewModel
class DreidelViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val outcomeProvider: DreidelSideProvider,
    private val rules: DreidelRules
) : ViewModel() {

    companion object {
        private const val KEY_POT = "pot"
        private const val KEY_LAST_SIDE = "last_side"
    }

    private val _state = MutableStateFlow(
        DreidelState(
            pot = savedStateHandle.get<Int>(KEY_POT) ?: 10,
            lastSide = savedStateHandle.get<DreidelLandingResult>(KEY_LAST_SIDE),
        )
    )
    val state: StateFlow<DreidelState> = _state

    private val _spinState = MutableStateFlow(DreidelSpinAnimationState())
    val spinState: StateFlow<DreidelSpinAnimationState> = _spinState

    private val _effects = Channel<DreidelEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: DreidelIntent) {
        when (intent) {
            is DreidelIntent.Spin -> spin(intent.ruleSet)
            DreidelIntent.Reset -> reset()
            is DreidelIntent.ToggleRenderMode -> updateState(
                _state.value.copy(
                    renderMode = intent.mode
                )
            )

            is DreidelIntent.ToggleRuleMode -> updateState(
                _state.value.copy(
                    ruleMode = intent.ruleMode
                )
            )
        }
    }

    private fun reset() {
        updateState(
            _state.value.copy(
                lastSide = null,
                potDelta = 0,
                pot = 10,
                isResolvingSpin = false
            )
        )
        _spinState.value = DreidelSpinAnimationState() // reset spin state
    }

    private fun spin(ruleSet: DreidelRuleSet) {

        if (_state.value.isResolvingSpin) return

        updateState(
            _state.value.copy(
                isResolvingSpin = true,
            )
        )

        // Determine spin result
        val side = outcomeProvider.next()
        val landingSide = side.toDisplaySide(ruleSet)

        // Update spin state for renderer
        _spinState.value = DreidelSpinAnimationState(
            spinId = System.nanoTime(),
            spinning = true,
            initialVelocity = Random.nextFloat() * 720f + 720f, // random start speed
            spins = 4,                                           // extra spins
            landingFace = faceFor(landingSide)
        )

        viewModelScope.launch {
            _effects.send(DreidelEffect.SpinSound)
            delay(1200)

            val current = _state.value
            val result = rules.apply(current.pot, side)

            updateState(
                current.copy(
                    pot = result.newPot,
                    potBefore = current.pot,
                    potDelta = result.potDelta,
                    lastSide = landingSide,
                    isResolvingSpin = false
                )
            )
            _effects.send(DreidelEffect.ResultSound)
        }
    }

    private fun updateState(newState: DreidelState) {
        _state.value = newState
        savedStateHandle[KEY_POT] = _state.value.pot
    }

    fun onSpinAnimationFinished() {
        _spinState.value = DreidelSpinAnimationState()
    }

    private fun faceFor(result: DreidelLandingResult): Face =
        Face.entries.first { result in it.results }

}