package com.rhappdeveloper.dreidelgameapp.ui.util

import android.media.AudioManager
import android.media.ToneGenerator

/*
ToneGenerator.TONE_PROP_BEEP
ToneGenerator.TONE_PROP_ACK
ToneGenerator.TONE_PROP_NACK
ToneGenerator.TONE_SUP_CONFIRM
ToneGenerator.TONE_SUP_ERROR
ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
 */
object SystemSoundPlayer {

    private val tone = ToneGenerator(
        AudioManager.STREAM_MUSIC,
        80 // volume (0–100)
    )

    fun playSpin() {
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    fun playResult() {
        tone.startTone(ToneGenerator.TONE_PROP_ACK, 200)
    }
}