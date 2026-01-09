package com.rhappdeveloper.dreidelgameapp.model

enum class Face(val angle: Float,
                val textureIndex: Int,
                val results: Set<DreidelLandingResult>) {
    FRONT(0f, 0, setOf(DreidelLandingResult.NUN)),
    LEFT(90f,1,  setOf(DreidelLandingResult.HEI)),
    BACK(180f,2, setOf(DreidelLandingResult.GIMEL)),
    RIGHT(270f,3, setOf(
        DreidelLandingResult.SHIN,
        DreidelLandingResult.PEY
    ))
}