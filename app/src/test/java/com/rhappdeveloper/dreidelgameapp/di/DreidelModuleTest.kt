package com.rhappdeveloper.dreidelgameapp.di

import com.rhappdeveloper.dreidelgameapp.domain.DreidelSideProvider
import com.rhappdeveloper.dreidelgameapp.domain.RandomDreidelSideProvider
import com.rhappdeveloper.dreidelgameapp.model.DreidelOutcome
import org.junit.Assert.assertTrue
import org.junit.Test

class DreidelModuleTest {

    @Test
    fun randomDreidelSideProvider_returns_only_valid_outcomes() {
        val provider: DreidelSideProvider = RandomDreidelSideProvider()

        repeat(100) { // run multiple times to cover randomness
            val result = provider.next()
            assertTrue(result in DreidelOutcome.entries)
        }
    }
}