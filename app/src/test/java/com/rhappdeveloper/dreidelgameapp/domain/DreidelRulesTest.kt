package com.rhappdeveloper.dreidelgameapp.domain

import com.rhappdeveloper.dreidelgameapp.model.DreidelOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

class DreidelRulesTest {
    private val rules = DreidelRules()

    @Test
    fun do_nothing_keeps_pot_same() {
        val result = rules.apply(
            pot = 10,
            side = DreidelOutcome.DO_NOTHING
        )

        assertEquals(10, result.newPot)
        assertEquals(0, result.potDelta)
    }

    @Test
    fun take_pot_clears_pot() {
        val result = rules.apply(
            pot = 10,
            side = DreidelOutcome.TAKE_POT
        )

        assertEquals(0, result.newPot)
        assertEquals(-10, result.potDelta)
    }

    @Test
    fun take_half_even_pot() {
        val result = rules.apply(
            pot = 10,
            side = DreidelOutcome.TAKE_HALF_POT
        )

        assertEquals(5, result.newPot)
        assertEquals(-5, result.potDelta)
    }

    @Test
    fun take_half_odd_pot_rounds_down() {
        val result = rules.apply(
            pot = 9,
            side = DreidelOutcome.TAKE_HALF_POT
        )

        assertEquals(5, result.newPot)
        assertEquals(-4, result.potDelta)
    }

    @Test
    fun shin_adds_one_to_pot() {
        val result = rules.apply(
            pot = 10,
            side = DreidelOutcome.PUT_ONE
        )

        assertEquals(11, result.newPot)
        assertEquals(1, result.potDelta)
    }

    @Test
    fun take_half_of_one_takes_nothing() {
        val result = rules.apply(1, DreidelOutcome.TAKE_HALF_POT)
        assertEquals(1, result.newPot)
        assertEquals(0, result.potDelta)
    }

    @Test
    fun take_pot_when_empty_does_nothing() {
        val result = rules.apply(
            pot = 0,
            side = DreidelOutcome.TAKE_POT
        )

        assertEquals(0, result.newPot)
        assertEquals(0, result.potDelta)
    }
}