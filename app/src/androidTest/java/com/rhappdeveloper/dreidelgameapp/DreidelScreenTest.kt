package com.rhappdeveloper.dreidelgameapp

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DreidelScreenTest {

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rhappdeveloper.dreidelgameapp", appContext.packageName)
    }

    @Test
    fun initial_pot_text_is_correct() {
        composeRule.onNodeWithTag("pot_text")
            .assertTextContains("10")
    }

    @Test
    fun spin_updates_pot_text() {
        composeRule.onNodeWithTag("pot_text")
            .assertTextContains("10")
        composeRule.onNodeWithTag("spin_button")
            .performClick()
        composeRule.waitForIdle()

        // Read the result text
        val sideText = composeRule.onNodeWithTag("side_result_text")
            .fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)?.joinToString("") ?: ""

        // Map the text to DreidelLandingResult
        val result = when (sideText.uppercase()) {
            "GIMEL" -> DreidelLandingResult.GIMEL
            "SHIN" -> DreidelLandingResult.SHIN
            "NUN" -> DreidelLandingResult.NUN
            "HEI" -> DreidelLandingResult.HEI
            else -> null
        }

        // Assert pot based on result
        when (result) {
            DreidelLandingResult.GIMEL -> {
                composeRule.onNodeWithTag("pot_text")
                    .assertTextContains("0") // GIMEL clears pot
            }

            DreidelLandingResult.SHIN -> {
                composeRule.onNodeWithTag("pot_text")
                    .assertTextContains("11") // SHIN adds 1
            }

            DreidelLandingResult.NUN -> {
                composeRule.onNodeWithTag("pot_text")
                    .assertTextContains("10") // NUN does nothing
            }

            DreidelLandingResult.HEI -> {
                composeRule.onNodeWithTag("pot_text")
                    .assertTextContains("5") // HEI takes half
            }

            else -> {

            }
        }
    }

    @Test
    fun reset_restores_initial_state() {
        composeRule
            .onNodeWithTag("spin_button")
            .performClick()

        composeRule
            .onNodeWithTag("reset_button")
            .performClick()

        composeRule
            .onNodeWithTag("pot_text")
            .assertTextContains("10")
    }
}