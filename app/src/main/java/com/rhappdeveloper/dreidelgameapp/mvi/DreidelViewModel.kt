package com.rhappdeveloper.dreidelgameapp.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhappdeveloper.dreidelgameapp.domain.DreidelRules
import com.rhappdeveloper.dreidelgameapp.domain.DreidelSideProvider
import com.rhappdeveloper.dreidelgameapp.domain.MessageKey
import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult
import com.rhappdeveloper.dreidelgameapp.model.DreidelState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DreidelViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sideProvider: DreidelSideProvider,
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

    private val _effects = Channel<DreidelEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: DreidelIntent) {
        when (intent) {
            DreidelIntent.Spin -> spin()
            DreidelIntent.Reset -> reset()
        }
    }

    private fun reset() {
        updateState(
            _state.value.copy(
                lastSide = null,
                potDelta = 0,
                pot = 10,
                messageKey = null,
                isSpinning = false
            )
        )
    }

    private fun spin() {

        if (_state.value.isSpinning) return

        updateState(
            _state.value.copy(
                isSpinning = true,
                messageKey = MessageKey.SPINNING
            )
        )

        viewModelScope.launch {
            _effects.send(DreidelEffect.SpinSound)
            delay(1200)

            val current = _state.value
            val side = sideProvider.next()
            val result = rules.apply(current.pot, side)

            updateState(
                current.copy(
                    pot = result.newPot,
                    potDelta = result.potDelta,
                    lastSide = side,
                    messageKey = result.messageKey,
                    isSpinning = false
                )
            )
            _effects.send(DreidelEffect.ResultSound)
        }
    }

    private fun updateState(newState: DreidelState) {
        _state.value = newState
        savedStateHandle[KEY_POT] = _state.value.pot
        savedStateHandle[KEY_LAST_SIDE] = _state.value.lastSide
    }

}