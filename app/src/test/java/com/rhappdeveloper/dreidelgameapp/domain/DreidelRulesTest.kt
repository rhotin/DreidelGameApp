package com.rhappdeveloper.dreidelgameapp.domain

import com.rhappdeveloper.dreidelgameapp.model.DreidelLandingResult
import org.junit.Assert.assertEquals
import org.junit.Test

class DreidelRulesTest {
    private val rules = DreidelRules()

    @Test
    fun nun_does_nothing() {
        val result = rules.apply(
            pot = 10,
            side = DreidelLandingResult.NUN
        )

        assertEquals(10, result.newPot)
        assertEquals(0, result.potDelta)
        assertEquals(MessageKey.NUN, result.messageKey)
    }

    @Test
    fun gimel_takes_entire_pot() {
        val result = rules.apply(
            pot = 10,
            side = DreidelLandingResult.GIMEL
        )

        assertEquals(0, result.newPot)
        assertEquals(-10, result.potDelta)
        assertEquals(MessageKey.GIMEL, result.messageKey)
    }

    @Test
    fun hei_takes_half_of_even_pot() {
        val result = rules.apply(
            pot = 10,
            side = DreidelLandingResult.HEI
        )

        assertEquals(5, result.newPot)
        assertEquals(-5, result.potDelta)
        assertEquals(MessageKey.HEI, result.messageKey)
    }

    @Test
    fun hei_takes_half_of_odd_pot_rounded_down() {
        val result = rules.apply(
            pot = 9,
            side = DreidelLandingResult.HEI
        )

        assertEquals(5, result.newPot)
        assertEquals(-4, result.potDelta)
        assertEquals(MessageKey.HEI, result.messageKey)
    }

    @Test
    fun shin_adds_one_to_pot() {
        val result = rules.apply(
            pot = 10,
            side = DreidelLandingResult.SHIN
        )

        assertEquals(11, result.newPot)
        assertEquals(1, result.potDelta)
        assertEquals(MessageKey.SHIN, result.messageKey)
    }

    @Test
    fun hei_with_pot_of_one_takes_nothing() {
        val result = rules.apply(1, DreidelLandingResult.HEI)
        assertEquals(1, result.newPot)
        assertEquals(0, result.potDelta)
    }

    @Test
    fun gimel_with_empty_pot_takes_nothing() {
        val result = rules.apply(
            pot = 0,
            side = DreidelLandingResult.GIMEL
        )

        assertEquals(0, result.newPot)
        assertEquals(0, result.potDelta)
        assertEquals(MessageKey.GIMEL, result.messageKey)
    }
}