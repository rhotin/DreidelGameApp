package com.rhappdeveloper.dreidelgameapp.mvi

import androidx.lifecycle.SavedStateHandle
import com.rhappdeveloper.dreidelgameapp.domain.DreidelRules
import com.rhappdeveloper.dreidelgameapp.fakes.FakeDreidelOutcomeProvider
import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult
import com.rhappdeveloper.dreidelgameapp.model.DreidelOutcome
import com.rhappdeveloper.dreidelgameapp.model.DreidelRenderMode
import com.rhappdeveloper.dreidelgameapp.model.DreidelRuleSet
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

    private fun createViewModel(outcome: DreidelOutcome) =
        DreidelViewModel(
            savedStateHandle = SavedStateHandle(),
            outcomeProvider = FakeDreidelOutcomeProvider(outcome),
            rules = DreidelRules()
        )

    @Test
    fun initial_state_is_correct() = runTest {
        val viewModel = createViewModel(DreidelOutcome.DO_NOTHING)
        val state = viewModel.state.value
        Assert.assertEquals(10, state.pot)
        Assert.assertEquals(0, state.potDelta)
        Assert.assertNull(state.lastSide)
        Assert.assertFalse(state.isResolvingSpin)
    }

    @Test
    fun spin_sets_isSpinning_true_then_false() = runTest {
        val viewModel = createViewModel(DreidelOutcome.DO_NOTHING)
        viewModel.onIntent(DreidelIntent.Spin(DreidelRuleSet.CLASSIC))
        Assert.assertTrue(viewModel.state.value.isResolvingSpin)
        advanceUntilIdle()
        Assert.assertFalse(viewModel.state.value.isResolvingSpin)
    }

    @Test
    fun spin_results_are_correct() = runTest {
        val cases = listOf(
            DreidelOutcome.TAKE_POT to Triple(0, -10, DreidelLandingResult.GIMEL),
            DreidelOutcome.DO_NOTHING to Triple(10, 0, DreidelLandingResult.NUN),
            DreidelOutcome.TAKE_HALF_POT to Triple(5, -5, DreidelLandingResult.HEI),
            DreidelOutcome.PUT_ONE to Triple(11, 1, DreidelLandingResult.SHIN)
        )

        cases.forEach { (outcome, expected) ->
            val (expectedPot, expectedDelta, expectedSide) = expected

            val viewModel = createViewModel(outcome)
            viewModel.onIntent(DreidelIntent.Spin(DreidelRuleSet.CLASSIC))
            advanceUntilIdle()

            val state = viewModel.state.value
            Assert.assertEquals(expectedPot, state.pot)
            Assert.assertEquals(expectedDelta, state.potDelta)
            Assert.assertEquals(10, state.potBefore)
            Assert.assertEquals(expectedSide, state.lastSide)
            Assert.assertFalse(state.isResolvingSpin)
        }
    }

    @Test
    fun reset_sets_state_to_initial() = runTest {
        val viewModel = createViewModel(DreidelOutcome.PUT_ONE)
        viewModel.onIntent(DreidelIntent.Reset)
        advanceUntilIdle()
        val state = viewModel.state.value
        Assert.assertEquals(10, state.pot)
        Assert.assertEquals(0, state.potDelta)
        Assert.assertEquals(10, state.potBefore)
        Assert.assertNull(state.lastSide)
        Assert.assertFalse(state.isResolvingSpin)
    }

    @Test
    fun toggle_render_mode_updates_state() = runTest {
        val viewModel = createViewModel(DreidelOutcome.DO_NOTHING)

        viewModel.onIntent(
            DreidelIntent.ToggleRenderMode(DreidelRenderMode.THREE_D)
        )

        Assert.assertEquals(
            DreidelRenderMode.THREE_D,
            viewModel.state.value.renderMode
        )
    }
}