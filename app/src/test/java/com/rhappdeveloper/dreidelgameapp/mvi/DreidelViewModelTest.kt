package com.rhappdeveloper.dreidelgameapp.mvi

import androidx.lifecycle.SavedStateHandle
import com.rhappdeveloper.dreidelgameapp.fakes.FakeDreidelSideProvider
import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
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

    private fun createViewModel(
        result: DreidelLandingResult
    ): DreidelViewModel {
        return DreidelViewModel(
            savedStateHandle = SavedStateHandle(),
            sideProvider = FakeDreidelSideProvider(result)
        )
    }

    @Test
    fun initial_state_is_correct() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.NUN)
        val state = viewModel.state.value
        Assert.assertEquals(10, state.pot)
        Assert.assertEquals(10, state.previousPot)
        Assert.assertNull(state.lastSide)
        Assert.assertFalse(state.isSpinning)
    }

    @Test
    fun spin_sets_isSpinning_true_then_false() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.NUN)
        viewModel.onIntent(DreidelIntent.Spin)
        Assert.assertTrue(viewModel.state.value.isSpinning)
        advanceUntilIdle()
        Assert.assertFalse(viewModel.state.value.isSpinning)
    }

    @Test
    fun gimel_takes_full_pot() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.GIMEL)
        viewModel.onIntent(DreidelIntent.Spin)
        advanceUntilIdle()
        val state = viewModel.state.value
        Assert.assertEquals(0, state.pot)
        Assert.assertEquals(10, state.previousPot)
        Assert.assertEquals(DreidelLandingResult.GIMEL, state.lastSide)
        Assert.assertFalse(state.isSpinning)
    }

    @Test
    fun nun_does_nothing() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.NUN)
        viewModel.onIntent(DreidelIntent.Spin)
        advanceUntilIdle()
        val state = viewModel.state.value
        Assert.assertEquals(10, state.pot)
        Assert.assertEquals(state.pot, state.previousPot)
        Assert.assertEquals(DreidelLandingResult.NUN, state.lastSide)
    }

    @Test
    fun hei_takes_half_pot() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.HEI)
        viewModel.onIntent(DreidelIntent.Spin)
        advanceUntilIdle()
        val state = viewModel.state.value
        Assert.assertEquals(5, state.pot)
        Assert.assertEquals(10, state.previousPot)
    }

    @Test
    fun shin_adds_one_to_pot() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.SHIN)
        viewModel.onIntent(DreidelIntent.Spin)
        advanceUntilIdle()
        val state = viewModel.state.value
        Assert.assertEquals(11, state.pot)
        Assert.assertEquals(10, state.previousPot)
    }

    @Test
    fun reset_button_resets_pot_gimel() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.GIMEL)
        viewModel.onIntent(DreidelIntent.Reset)
        advanceUntilIdle()
        val state = viewModel.state.value
        Assert.assertEquals(10, state.pot)
        Assert.assertEquals(10, state.previousPot)
        Assert.assertEquals(null, state.lastSide)
    }

    @Test
    fun reset_button_resets_pot_hei() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.HEI)
        viewModel.onIntent(DreidelIntent.Reset)
        advanceUntilIdle()
        val state = viewModel.state.value
        Assert.assertEquals(10, state.pot)
        Assert.assertEquals(10, state.previousPot)
        Assert.assertEquals(null, state.lastSide)
    }

    @Test
    fun reset_button_resets_pot_nun() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.NUN)
        viewModel.onIntent(DreidelIntent.Reset)
        advanceUntilIdle()
        val state = viewModel.state.value
        Assert.assertEquals(10, state.pot)
        Assert.assertEquals(10, state.previousPot)
        Assert.assertEquals(null, state.lastSide)
    }

    @Test
    fun reset_button_resets_pot_shin() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.SHIN)
        viewModel.onIntent(DreidelIntent.Reset)
        advanceUntilIdle()
        val state = viewModel.state.value
        Assert.assertEquals(10, state.pot)
        Assert.assertEquals(10, state.previousPot)
        Assert.assertEquals(null, state.lastSide)
    }
}