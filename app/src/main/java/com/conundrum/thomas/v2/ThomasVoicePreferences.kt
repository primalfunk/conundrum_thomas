package com.conundrum.thomas.v2

import android.content.Context
import com.conundrum.thomas.v2.platform.speech.ThomasSpeechRate
import com.conundrum.thomas.v2.platform.speech.ThomasVoiceProfile

/** Presentation-only preferences. They are deliberately outside Thomas history. */
data class ThomasVoicePreferences(
    val autoSpeak: Boolean = true,
    val profile: ThomasVoiceProfile = ThomasVoiceProfile.NEUTRAL,
    val rate: ThomasSpeechRate = ThomasSpeechRate.NORMAL,
    val stopWhenMicrophoneStarts: Boolean = true,
)

class AndroidThomasVoicePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("thomas_voice_presentation", Context.MODE_PRIVATE)

    fun read() = ThomasVoicePreferences(
        autoSpeak = preferences.getBoolean("auto_speak", true),
        profile = enum("profile", ThomasVoiceProfile.NEUTRAL),
        rate = enum("rate", ThomasSpeechRate.NORMAL),
        stopWhenMicrophoneStarts = preferences.getBoolean("stop_on_mic", true),
    )

    fun write(value: ThomasVoicePreferences) {
        preferences.edit()
            .putBoolean("auto_speak", value.autoSpeak)
            .putString("profile", value.profile.name)
            .putString("rate", value.rate.name)
            .putBoolean("stop_on_mic", value.stopWhenMicrophoneStarts)
            .apply()
    }

    private inline fun <reified T : Enum<T>> enum(key: String, fallback: T): T =
        preferences.getString(key, fallback.name)?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: fallback
}
