package com.rhappdeveloper.dreidelgameapp.model

enum class Face(val angle: Float,
                val results: Set<DreidelLandingResult>) {
    FRONT(0f, setOf(DreidelLandingResult.NUN)),
    LEFT(90f, setOf(DreidelLandingResult.GIMEL)),
    BACK(180f, setOf(DreidelLandingResult.HEI)),
    RIGHT(270f, setOf(
        DreidelLandingResult.SHIN,
        DreidelLandingResult.PEY
    ))
}