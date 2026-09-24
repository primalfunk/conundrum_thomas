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

/**
 * Product-owned, offline Google voice identities for the TCL 9491G generation.
 * These identifiers are intentionally not presentation labels: ordinary UI exposes
 * only [ThomasVoiceProfile].  The secondary entries are the previously qualified
 * per-profile fallbacks, not candidates for ordinary audition.
 */
object ThomasSystemVoiceMappings {
    private val primary = mapOf(
        ThomasVoiceProfile.MALE to "en-us-x-iob-local",
        ThomasVoiceProfile.FEMALE to "en-us-x-iog-local",
        ThomasVoiceProfile.NEUTRAL to "en-us-x-iol-local",
    )
    private val qualifiedFallback = mapOf(
        ThomasVoiceProfile.MALE to "en-us-x-iom-local",
        ThomasVoiceProfile.FEMALE to "en-us-x-sfg-local",
        ThomasVoiceProfile.NEUTRAL to "en-us-x-tpc-local",
    )

    fun primaryFor(profile: ThomasVoiceProfile): String = checkNotNull(primary[profile])
    fun qualifiedFallbackFor(profile: ThomasVoiceProfile): String = checkNotNull(qualifiedFallback[profile])
}

/** Android-independent voice facts, retained solely to make selection testable. */
data class ThomasSystemVoiceCandidate(
    val name: String,
    val locale: Locale,
    val networkConnectionRequired: Boolean,
    val installed: Boolean,
)

enum class ThomasVoiceSelectionKind { PREFERRED, QUALIFIED_FALLBACK, SAFE_ENGLISH_FALLBACK, UNAVAILABLE }

data class ThomasVoiceSelection(
    val candidate: ThomasSystemVoiceCandidate?,
    val kind: ThomasVoiceSelectionKind,
)

/**
 * Resolves only installed, non-network voices. Exact product mappings must be US
 * English; the final safety net remains an installed offline English system voice.
 */
fun selectThomasSystemVoice(
    profile: ThomasVoiceProfile,
    catalog: Collection<ThomasSystemVoiceCandidate>,
): ThomasVoiceSelection {
    fun isOffline(candidate: ThomasSystemVoiceCandidate) =
        candidate.installed && !candidate.networkConnectionRequired
    fun exact(name: String) = catalog.firstOrNull {
        it.name == name && it.locale == Locale.US && isOffline(it)
    }

    exact(ThomasSystemVoiceMappings.primaryFor(profile))?.let {
        return ThomasVoiceSelection(it, ThomasVoiceSelectionKind.PREFERRED)
    }
    exact(ThomasSystemVoiceMappings.qualifiedFallbackFor(profile))?.let {
        return ThomasVoiceSelection(it, ThomasVoiceSelectionKind.QUALIFIED_FALLBACK)
    }
    return catalog.asSequence()
        .filter(::isOffline)
        .filter { it.locale.language == Locale.ENGLISH.language }
        .sortedBy { it.name }
        .firstOrNull()
        ?.let { ThomasVoiceSelection(it, ThomasVoiceSelectionKind.SAFE_ENGLISH_FALLBACK) }
        ?: ThomasVoiceSelection(null, ThomasVoiceSelectionKind.UNAVAILABLE)
}

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
                val catalog = voiceCatalog(engine).filter { candidate ->
                    candidate.installed && !candidate.networkConnectionRequired
                }.sortedBy { it.name }
                    .joinToString(" | ") { voice ->
                    "${voice.name};${voice.locale};network=${voice.networkConnectionRequired};installed=${voice.installed}"
                }
                Log.i(TAG, "SYSTEM_TTS_CATALOG $catalog")
                ThomasVoiceProfile.entries.forEach { profile ->
                    val selection = selectThomasSystemVoice(profile, voiceCatalog(engine))
                    Log.i(
                        TAG,
                        "SYSTEM_TTS_STARTUP_SELECTION profile=$profile kind=${selection.kind} " +
                            "voice=${selection.candidate?.name ?: "none"}",
                    )
                }
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
        val selection = selectThomasSystemVoice(presentation.profile, voiceCatalog(tts))
        val voice = selection.candidate?.let { selected ->
            tts.voices.orEmpty().firstOrNull { it.name == selected.name }
        }
        if (voice != null) tts.voice = voice
        tts.setSpeechRate(presentation.rate.multiplier)
        val id = "thomas-${UUID.randomUUID()}"
        activeId = id
        activeListener = listener
        activeStartedAtMs = SystemClock.elapsedRealtime()
        Log.i(
            TAG,
            "SYSTEM_TTS_REQUEST profile=${presentation.profile} selection=${selection.kind} " +
                "voice=${voice?.name ?: "engine-default"}",
        )
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

    private fun voiceCatalog(tts: TextToSpeech?): List<ThomasSystemVoiceCandidate> =
        tts?.voices.orEmpty().map { voice ->
            ThomasSystemVoiceCandidate(
                name = voice.name,
                locale = voice.locale,
                networkConnectionRequired = voice.isNetworkConnectionRequired,
                installed = "notInstalled" !in voice.features.orEmpty(),
            )
        }

    private companion object { const val TAG = "ThomasSystemTts" }
}
