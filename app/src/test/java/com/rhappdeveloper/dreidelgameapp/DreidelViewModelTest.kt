@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rhappdeveloper.dreidelgameapp

import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult
import com.rhappdeveloper.dreidelgameapp.mvi.DreidelIntent
import com.rhappdeveloper.dreidelgameapp.mvi.DreidelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test

class DreidelViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun gimel_takes_full_pot() = runTest {
        val viewModel = DreidelViewModel(
            sideProvider = { DreidelLandingResult.GIMEL }
        )

        viewModel.onIntent(DreidelIntent.Spin)
        advanceUntilIdle()

        val state = viewModel.state.value

        assertEquals(0, state.pot)
        assertEquals(10, state.previousPot)
        assertEquals(DreidelLandingResult.GIMEL, state.lastSide)
        assertFalse(state.isSpinning)
    }

    @Test
    fun nun_does_nothing() = runTest {
        val viewModel = DreidelViewModel(
            sideProvider = { DreidelLandingResult.NUN }
        )

        viewModel.onIntent(DreidelIntent.Spin)
        advanceUntilIdle()

        val state = viewModel.state.value

        assertEquals(10, state.pot)
        assertEquals(state.pot, state.previousPot)
        assertEquals(DreidelLandingResult.NUN, state.lastSide)
    }

    @Test
    fun hei_takes_half_pot() = runTest {
        val viewModel = DreidelViewModel(
            sideProvider = { DreidelLandingResult.HEI }
        )

        viewModel.onIntent(DreidelIntent.Spin)
        advanceUntilIdle()

        val state = viewModel.state.value

        assertEquals(5, state.pot)
        assertEquals(10, state.previousPot)
    }

    @Test
    fun shin_adds_one_to_pot() = runTest {
        val viewModel = DreidelViewModel(
            sideProvider = { DreidelLandingResult.SHIN }
        )

        viewModel.onIntent(DreidelIntent.Spin)
        advanceUntilIdle()

        val state = viewModel.state.value

        assertEquals(11, state.pot)
        assertEquals(10, state.previousPot)
    }

    @Test
    fun reset_button_resets_pot_gimel() = runTest {
        val viewModel = DreidelViewModel(
            sideProvider = { DreidelLandingResult.GIMEL }
        )
        viewModel.onIntent(DreidelIntent.Reset)
        advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals(10, state.pot)
        assertEquals(10, state.previousPot)
        assertEquals(null, state.lastSide)
    }

    @Test
    fun reset_button_resets_pot_hei() = runTest {
        val viewModel = DreidelViewModel(
            sideProvider = { DreidelLandingResult.HEI }
        )
        viewModel.onIntent(DreidelIntent.Reset)
        advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals(10, state.pot)
        assertEquals(10, state.previousPot)
        assertEquals(null, state.lastSide)
    }

    @Test
    fun reset_button_resets_pot_nun() = runTest {
        val viewModel = DreidelViewModel(
            sideProvider = { DreidelLandingResult.NUN }
        )
        viewModel.onIntent(DreidelIntent.Reset)
        advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals(10, state.pot)
        assertEquals(10, state.previousPot)
        assertEquals(null, state.lastSide)
    }

    @Test
    fun reset_button_resets_pot_shin() = runTest {
        val viewModel = DreidelViewModel(
            sideProvider = { DreidelLandingResult.SHIN }
        )
        viewModel.onIntent(DreidelIntent.Reset)
        advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals(10, state.pot)
        assertEquals(10, state.previousPot)
        assertEquals(null, state.lastSide)
    }
}

