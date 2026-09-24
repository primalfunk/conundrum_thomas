package com.conundrum.thomas.v2.platform.speech

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale
import java.util.UUID

/**
 * The only speech-output authority crossing into a provider.  This deliberately
 * contains presentation text, never prompts, policy state, traces, or tokens.
 */
data class ValidatedThomasResponse(val text: String) {
    init { require(text.isNotBlank()) }
}

enum class ThomasVoiceProfile { MALE, FEMALE, NEUTRAL }
enum class ThomasSpeechRate(val multiplier: Float) { SLOW(0.84f), NORMAL(1.0f), FAST(1.16f) }

data class ThomasVoicePresentation(
    val profile: ThomasVoiceProfile,
    val rate: ThomasSpeechRate,
)

sealed interface ThomasVoiceEvent {
    data object Started : ThomasVoiceEvent
    data object Completed : ThomasVoiceEvent
    data object Stopped : ThomasVoiceEvent
    data class Unavailable(val reason: String) : ThomasVoiceEvent
    data class Failed(val reason: String) : ThomasVoiceEvent
}

fun interface ThomasVoiceListener { fun onVoiceEvent(event: ThomasVoiceEvent) }

interface ThomasVoiceProvider : AutoCloseable {
    /** Replaces any active utterance. Providers must not persist generated audio. */
    fun speak(response: ValidatedThomasResponse, presentation: ThomasVoicePresentation, listener: ThomasVoiceListener)
    fun stop()
}

/**
 * Fully local system synthesis fallback. It prefers installed offline US-English
 * voices and picks stable, distinct candidates for product profiles when possible.
 */
class AndroidSystemThomasVoiceProvider(context: Context) : ThomasVoiceProvider {
    private val applicationContext = context.applicationContext
    private var engine: TextToSpeech? = null
    private var ready = false
    private var activeListener: ThomasVoiceListener? = null
    private var activeId: String? = null
    private var activeStartedAtMs = 0L

    init {
        engine = TextToSpeech(applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                engine?.language = Locale.US
                val catalog = engine?.voices.orEmpty()
                    .filter {
                        it.locale == Locale.US &&
                            !it.isNetworkConnectionRequired &&
                            "notInstalled" !in it.features.orEmpty()
                    }
                    .sortedBy { it.name }
                    .joinToString(" | ") { voice ->
                    "${voice.name};${voice.locale};network=${voice.isNetworkConnectionRequired};features=${voice.features.orEmpty().sorted()}"
                }
                Log.i(TAG, "SYSTEM_TTS_CATALOG $catalog")
            }
        }.also { tts ->
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    if (utteranceId == activeId) {
                        Log.i(TAG, "SYSTEM_TTS_STARTED latency_ms=${SystemClock.elapsedRealtime() - activeStartedAtMs}")
                        activeListener?.onVoiceEvent(ThomasVoiceEvent.Started)
                    }
                }
                override fun onDone(utteranceId: String) {
                    if (utteranceId == activeId) {
                        activeListener?.onVoiceEvent(ThomasVoiceEvent.Completed)
                        activeId = null
                    }
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String) = reportError(utteranceId)
                override fun onError(utteranceId: String, errorCode: Int) = reportError(utteranceId)
            })
        }
    }

    override fun speak(response: ValidatedThomasResponse, presentation: ThomasVoicePresentation, listener: ThomasVoiceListener) {
        val tts = engine
        if (!ready || tts == null) {
            listener.onVoiceEvent(ThomasVoiceEvent.Unavailable("System voice is still starting"))
            return
        }
        stop()
        val voice = selectVoice(tts, presentation.profile)
        if (voice != null) tts.voice = voice
        tts.setSpeechRate(presentation.rate.multiplier)
        val id = "thomas-${UUID.randomUUID()}"
        activeId = id
        activeListener = listener
        activeStartedAtMs = SystemClock.elapsedRealtime()
        Log.i(TAG, "SYSTEM_TTS_REQUEST profile=${presentation.profile} voice=${voice?.name ?: "engine-default"}")
        val result = tts.speak(response.text, TextToSpeech.QUEUE_FLUSH, Bundle(), id)
        if (result != TextToSpeech.SUCCESS) {
            activeId = null
            listener.onVoiceEvent(ThomasVoiceEvent.Failed("System voice could not start"))
        }
    }

    override fun stop() {
        val wasActive = activeId != null
        engine?.stop()
        activeId = null
        if (wasActive) {
            Log.i(TAG, "SYSTEM_TTS_STOP active_ms=${SystemClock.elapsedRealtime() - activeStartedAtMs}")
            activeListener?.onVoiceEvent(ThomasVoiceEvent.Stopped)
        }
    }

    override fun close() {
        stop()
        engine?.shutdown()
        engine = null
        ready = false
    }

    private fun reportError(id: String) {
        if (id == activeId) {
            activeId = null
            activeListener?.onVoiceEvent(ThomasVoiceEvent.Failed("System voice playback failed"))
        }
    }

    private fun selectVoice(tts: TextToSpeech, profile: ThomasVoiceProfile): Voice? {
        val offline = tts.voices.orEmpty().filter {
            it.locale == Locale.US &&
                !it.isNetworkConnectionRequired &&
                "notInstalled" !in it.features.orEmpty()
        }.sortedBy { it.name }
        if (offline.isEmpty()) return null
        val preferred = when (profile) {
            ThomasVoiceProfile.MALE -> "en-us-x-iob-local"
            ThomasVoiceProfile.FEMALE -> "en-us-x-iog-local"
            ThomasVoiceProfile.NEUTRAL -> "en-us-x-iol-local"
        }
        return offline.firstOrNull { it.name == preferred } ?: offline[profile.ordinal % offline.size]
    }

    private companion object { const val TAG = "ThomasSystemTts" }
}
