package com.conundrum.thomas.v2.platform.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

enum class SpeechCaptureState {
    IDLE,
    LISTENING,
    FINALIZING,
    TRANSCRIPT_READY,
    UNAVAILABLE,
    ERROR,
}

enum class SpeechFailure {
    PERMISSION_DENIED,
    RECOGNIZER_UNAVAILABLE,
    NO_SPEECH,
    TIMEOUT,
    CANCELLED,
    INTERRUPTED,
    PROVIDER_ERROR,
    EMPTY_RESULT,
    UNKNOWN,
}

fun SpeechFailure.userMessage(): String = when (this) {
    SpeechFailure.PERMISSION_DENIED -> "Microphone permission denied; typing remains available"
    SpeechFailure.RECOGNIZER_UNAVAILABLE -> "Speech is unavailable; typing remains available"
    SpeechFailure.NO_SPEECH,
    SpeechFailure.TIMEOUT,
    SpeechFailure.EMPTY_RESULT,
    -> "No speech recognized; type or try speaking again"
    SpeechFailure.CANCELLED -> "Speech cancelled; existing text kept"
    SpeechFailure.INTERRUPTED -> "Speech was interrupted; typing remains available"
    SpeechFailure.PROVIDER_ERROR,
    SpeechFailure.UNKNOWN,
    -> "Speech failed; typing remains available"
}

sealed interface SpeechInputEvent {
    data object Listening : SpeechInputEvent
    data class Partial(val text: String) : SpeechInputEvent
    data class Final(val text: String) : SpeechInputEvent
    data class Failed(val failure: SpeechFailure) : SpeechInputEvent
    data object Cancelled : SpeechInputEvent
}

fun interface SpeechInputListener {
    fun onSpeechEvent(event: SpeechInputEvent)
}

sealed interface SpeechStartResult {
    data object Started : SpeechStartResult
    data object AlreadyActive : SpeechStartResult
    data object PermissionRequired : SpeechStartResult
    data object Unavailable : SpeechStartResult
    data class Failed(val failure: SpeechFailure) : SpeechStartResult
}

interface SpeechInputController : AutoCloseable {
    fun start(): SpeechStartResult
    fun stop()
    fun cancel()
}

/**
 * Owns the Android recognizer only. It emits text and failure events; it has
 * no access to Thomas runtime, persistence, policy, or conversation state.
 */
class AndroidSpeechInputController(
    context: Context,
    private val listener: SpeechInputListener,
) : SpeechInputController {
    private val applicationContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var active = false
    private var suppressCancellationError = false

    override fun start(): SpeechStartResult {
        if (active) return SpeechStartResult.AlreadyActive
        if (applicationContext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            listener.onSpeechEvent(SpeechInputEvent.Failed(SpeechFailure.PERMISSION_DENIED))
            return SpeechStartResult.PermissionRequired
        }
        if (!SpeechRecognizer.isRecognitionAvailable(applicationContext)) {
            listener.onSpeechEvent(SpeechInputEvent.Failed(SpeechFailure.RECOGNIZER_UNAVAILABLE))
            return SpeechStartResult.Unavailable
        }
        val instance = recognizer ?: SpeechRecognizer.createSpeechRecognizer(applicationContext).also {
            it.setRecognitionListener(RecognitionListenerAdapter())
            recognizer = it
        }
        return try {
            suppressCancellationError = false
            active = true
            listener.onSpeechEvent(SpeechInputEvent.Listening)
            instance.startListening(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                },
            )
            SpeechStartResult.Started
        } catch (_: RuntimeException) {
            active = false
            listener.onSpeechEvent(SpeechInputEvent.Failed(SpeechFailure.PROVIDER_ERROR))
            SpeechStartResult.Failed(SpeechFailure.PROVIDER_ERROR)
        }
    }

    override fun stop() {
        if (!active) return
        recognizer?.stopListening()
    }

    override fun cancel() {
        if (!active) return
        suppressCancellationError = true
        active = false
        recognizer?.cancel()
        listener.onSpeechEvent(SpeechInputEvent.Cancelled)
    }

    override fun close() {
        suppressCancellationError = true
        active = false
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
    }

    private inner class RecognitionListenerAdapter : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit

        override fun onError(error: Int) {
            if (!active) {
                suppressCancellationError = false
                return
            }
            if (suppressCancellationError) {
                suppressCancellationError = false
                return
            }
            active = false
            listener.onSpeechEvent(SpeechInputEvent.Failed(error.toSpeechFailure()))
        }

        override fun onResults(results: Bundle?) {
            if (!active) return
            active = false
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (text.isBlank()) {
                listener.onSpeechEvent(SpeechInputEvent.Failed(SpeechFailure.EMPTY_RESULT))
            } else {
                listener.onSpeechEvent(SpeechInputEvent.Final(text))
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            if (!active) return
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (text.isNotBlank()) listener.onSpeechEvent(SpeechInputEvent.Partial(text))
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun Int.toSpeechFailure(): SpeechFailure = when (this) {
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechFailure.PERMISSION_DENIED
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechFailure.TIMEOUT
        SpeechRecognizer.ERROR_NO_MATCH -> SpeechFailure.NO_SPEECH
        SpeechRecognizer.ERROR_CLIENT -> SpeechFailure.INTERRUPTED
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
        SpeechRecognizer.ERROR_SERVER,
        SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
        SpeechRecognizer.ERROR_TOO_MANY_REQUESTS,
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
        SpeechRecognizer.ERROR_AUDIO,
        -> SpeechFailure.PROVIDER_ERROR
        else -> SpeechFailure.UNKNOWN
    }
}

/**
 * Pure session semantics shared by the Android adapter and qualification.
 * A capture always starts from the existing draft; cancellation restores that
 * exact draft, while finish/error retains recognized material for review.
 */
class SpeechDraftSession {
    private var existingDraft = ""
    private var recognizedDraft = ""
    private var currentDraft = ""
    private var active = false

    var state: SpeechCaptureState = SpeechCaptureState.IDLE
        private set

    fun setDraft(draft: String) {
        if (!active) currentDraft = draft
    }

    fun begin(draft: String): Boolean {
        if (active) return false
        existingDraft = draft
        recognizedDraft = ""
        currentDraft = draft
        active = true
        state = SpeechCaptureState.LISTENING
        return true
    }

    fun partial(text: String): String {
        if (!active) return currentDraft
        recognizedDraft = text.trim()
        currentDraft = append(existingDraft, recognizedDraft)
        return currentDraft
    }

    fun stopRequested() {
        if (active) state = SpeechCaptureState.FINALIZING
    }

    fun finish(text: String): String {
        if (!active) return currentDraft
        recognizedDraft = text.trim()
        currentDraft = append(existingDraft, recognizedDraft)
        active = false
        state = SpeechCaptureState.TRANSCRIPT_READY
        return currentDraft
    }

    fun fail(failure: SpeechFailure): String {
        if (active) {
            currentDraft = append(existingDraft, recognizedDraft)
            active = false
        }
        state = when (failure) {
            SpeechFailure.RECOGNIZER_UNAVAILABLE -> SpeechCaptureState.UNAVAILABLE
            else -> SpeechCaptureState.ERROR
        }
        return currentDraft
    }

    fun cancel(): String {
        active = false
        recognizedDraft = ""
        currentDraft = existingDraft
        state = SpeechCaptureState.IDLE
        return currentDraft
    }

    fun reset(draft: String = "") {
        active = false
        existingDraft = draft
        recognizedDraft = ""
        currentDraft = draft
        state = SpeechCaptureState.IDLE
    }

    companion object {
        fun append(existing: String, recognized: String): String {
            val left = existing.trimEnd()
            val right = recognized.trimStart()
            return when {
                left.isBlank() -> right
                right.isBlank() -> left
                else -> "$left $right"
            }
        }
    }
}
