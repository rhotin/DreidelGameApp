package com.rhappdeveloper.dreidelgameapp.model

enum class DreidelLandingResult(
    val hebrew: String,
    val english: String
) {
    NUN("נ", "Nun"),
    GIMEL("ג", "Gimel"),
    HEI("ה", "Hei"),
    SHIN("ש", "Shin"),
    PEY("פ", "Pey");

    fun message(potDelta: Int, potBefore: Int): String =
        when (this) {
            NUN -> "$english – nothing happens"

            GIMEL -> {
                if (potBefore == 0) "$english – the pot is empty"
                else "$english – you took the whole pot!"
            }

            HEI -> {
                val taken = kotlin.math.abs(potDelta)
                when (taken) {
                    0 -> "$english – nothing to take"
                    1 -> "$english – you took 1 coin from the pot"
                    else -> "$english – you took $taken coins from the pot"
                }
            }

            SHIN -> "$english – you added 1 to the pot"
            PEY -> "$english – you added 1 to the pot"
        }
}