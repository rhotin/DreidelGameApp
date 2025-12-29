package com.rhappdeveloper.dreidelgameapp.mvi

import androidx.lifecycle.SavedStateHandle
import com.rhappdeveloper.dreidelgameapp.domain.DreidelRules
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

    private fun createViewModel(result: DreidelLandingResult) =
        DreidelViewModel(
            savedStateHandle = SavedStateHandle(),
            sideProvider = FakeDreidelSideProvider(result),
            rules = DreidelRules()
        )

    @Test
    fun initial_state_is_correct() = runTest {
        val viewModel = createViewModel(DreidelLandingResult.NUN)
        val state = viewModel.state.value
        Assert.assertEquals(10, state.pot)
        Assert.assertEquals(0, state.potDelta)
        Assert.assertNull(state.lastSide)
        Assert.assertFalse(state.isSpinning)
        Assert.assertNull(state.messageKey)
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
    fun spin_results_are_correct() = runTest {
        val cases = listOf(
            DreidelLandingResult.GIMEL to Triple(0, 10, "GIMEL"),
            DreidelLandingResult.NUN to Triple(10, 10, "NUN"),
            DreidelLandingResult.HEI to Triple(5, 10, "HEI"),
            DreidelLandingResult.SHIN to Triple(11, 10, "SHIN")
        )

        cases.forEach { (result, expected) ->
            val (expectedPot, expectedPrev, _) = expected
            val viewModel = createViewModel(result)
            viewModel.onIntent(DreidelIntent.Spin)
            advanceUntilIdle()
            val state = viewModel.state.value
            Assert.assertEquals(expectedPot, state.pot)
            Assert.assertEquals(expectedPrev, state.potDelta)
            Assert.assertEquals(result, state.lastSide)
            Assert.assertFalse(state.isSpinning)
        }
    }

    @Test
    fun reset_sets_state_to_initial() = runTest {
        val results = listOf(
            DreidelLandingResult.GIMEL,
            DreidelLandingResult.NUN,
            DreidelLandingResult.HEI,
            DreidelLandingResult.SHIN
        )
        results.forEach { result ->
            val viewModel = createViewModel(result)
            viewModel.onIntent(DreidelIntent.Reset)
            advanceUntilIdle()
            val state = viewModel.state.value
            Assert.assertEquals(10, state.pot)
            Assert.assertEquals(10, state.potDelta)
            Assert.assertNull(state.lastSide)
            Assert.assertFalse(state.isSpinning)
        }
    }
}