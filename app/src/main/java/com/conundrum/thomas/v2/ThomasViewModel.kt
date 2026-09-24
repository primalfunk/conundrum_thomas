package com.conundrum.thomas.v2

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.platform.speech.AndroidSpeechInputController
import com.conundrum.thomas.v2.platform.speech.SpeechCaptureState
import com.conundrum.thomas.v2.platform.speech.SpeechDraftSession
import com.conundrum.thomas.v2.platform.speech.SpeechFailure
import com.conundrum.thomas.v2.platform.speech.SpeechInputController
import com.conundrum.thomas.v2.platform.speech.SpeechInputEvent
import com.conundrum.thomas.v2.platform.renderer.llama.ThomasRealizerStatus
import com.conundrum.thomas.v2.platform.speech.SpeechStartResult
import com.conundrum.thomas.v2.platform.speech.AndroidSystemThomasVoiceProvider
import com.conundrum.thomas.v2.platform.speech.ThomasSpeechRate
import com.conundrum.thomas.v2.platform.speech.ThomasVoiceEvent
import com.conundrum.thomas.v2.platform.speech.ThomasVoicePresentation
import com.conundrum.thomas.v2.platform.speech.ThomasVoiceProfile
import com.conundrum.thomas.v2.platform.speech.ValidatedThomasResponse
import com.conundrum.thomas.v2.platform.speech.userMessage
import com.conundrum.thomas.v2.runtime.ProductionInputOrigin
import com.conundrum.thomas.v2.runtime.ProductionSourceSummary
import com.conundrum.thomas.v2.runtime.ProductionThomasMode
import com.conundrum.thomas.v2.runtime.ProductionTurnPrivacy
import com.conundrum.thomas.v2.runtime.ProductionTurnRequest
import com.conundrum.thomas.v2.runtime.TherapySafetyDeclaration
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryIntent
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

enum class TranscriptRole { USER, THOMAS, SYSTEM }

enum class ThomasModelActivity { WAKING, THINKING }

data class TranscriptItem(
    val id: String,
    val role: TranscriptRole,
    val mode: ProductionThomasMode,
    val text: String,
    val committed: Boolean = true,
)

data class ThomasUiState(
    val mode: ProductionThomasMode = ProductionThomasMode.JOURNAL,
    val draft: String = "",
    val transcript: List<TranscriptItem> = emptyList(),
    val sourceSummaries: List<ProductionSourceSummary> = emptyList(),
    val processing: Boolean = false,
    val modelActivity: ThomasModelActivity? = null,
    val privateTurn: Boolean = false,
    val journalPreference: JournalResponsePreference = JournalResponsePreference.NO_RESPONSE,
    val therapySupport: RequestedOrdinarySupport = RequestedOrdinarySupport.LISTEN,
    val explicitRecall: Boolean = false,
    val runtimeAvailable: Boolean = true,
    val status: String = "Ready",
    val draftOrigin: ProductionInputOrigin = ProductionInputOrigin.TYPED,
    val speechState: SpeechCaptureState = SpeechCaptureState.IDLE,
    val speechMessage: String? = null,
    val speechPermissionDenied: Boolean = false,
    val voicePreferences: ThomasVoicePreferences = ThomasVoicePreferences(),
    val voicePlaying: Boolean = false,
    val lastValidatedThomasResponse: String? = null,
)

class ThomasViewModel(application: Application) : AndroidViewModel(application) {
    private val root = ThomasAndroidCompositionRoot.open(application)
    private val drafts = ProductionThomasMode.entries.associateWith { "" }.toMutableMap()
    private val draftOrigins = ProductionThomasMode.entries
        .associateWith { ProductionInputOrigin.TYPED }
        .toMutableMap()
    private val speechSession = SpeechDraftSession()
    private val speechController: SpeechInputController = AndroidSpeechInputController(
        application,
        ::onSpeechEvent,
    )
    private val voicePreferencesStore = AndroidThomasVoicePreferences(application)
    private val backgroundCustody = ThomasBackgroundCustody(application)
    private val voiceProvider = AndroidSystemThomasVoiceProvider(application)
    private val mutableState = MutableStateFlow(
        ThomasUiState(
            runtimeAvailable = root.runtime != null,
            status = root.unavailableReason?.let { "Personal data unavailable: $it" } ?: "Ready",
            sourceSummaries = root.runtime?.sourceSummaries().orEmpty(),
            voicePreferences = voicePreferencesStore.read(),
        ),
    )

    val state: StateFlow<ThomasUiState> = mutableState.asStateFlow()

    fun updateDraft(value: String) {
        if (value.length > 4_096) return
        val current = mutableState.value
        if (isSpeechActive(current.speechState)) return
        drafts[current.mode] = value
        val origin = if (value.isBlank()) ProductionInputOrigin.TYPED else current.draftOrigin
        draftOrigins[current.mode] = origin
        mutableState.value = current.copy(
            draft = value,
            draftOrigin = origin,
            speechState = if (current.speechState == SpeechCaptureState.ERROR ||
                current.speechState == SpeechCaptureState.UNAVAILABLE
            ) SpeechCaptureState.IDLE else current.speechState,
            speechMessage = if (current.speechState == SpeechCaptureState.TRANSCRIPT_READY) {
                "Transcript edited; review before sending"
            } else {
                null
            },
        )
    }

    fun selectMode(mode: ProductionThomasMode) {
        val current = mutableState.value
        if (current.processing || current.mode == mode || isSpeechActive(current.speechState)) return
        stopVoice()
        drafts[current.mode] = current.draft
        draftOrigins[current.mode] = current.draftOrigin
        mutableState.value = current.copy(
            mode = mode,
            draft = drafts[mode].orEmpty(),
            draftOrigin = draftOrigins[mode] ?: ProductionInputOrigin.TYPED,
            speechState = SpeechCaptureState.IDLE,
            speechMessage = null,
            status = "Mode: ${mode.displayName()}",
        )
        if (mode == ProductionThomasMode.BIOGRAPHER &&
            current.transcript.none { it.mode == ProductionThomasMode.BIOGRAPHER && it.role == TranscriptRole.THOMAS }
        ) {
            requestBiographerPrompt()
        }
    }

    fun setPrivate(value: Boolean) {
        mutableState.value = mutableState.value.copy(privateTurn = value)
    }

    fun setJournalPreference(value: JournalResponsePreference) {
        mutableState.value = mutableState.value.copy(journalPreference = value)
    }

    fun setTherapySupport(value: RequestedOrdinarySupport) {
        mutableState.value = mutableState.value.copy(therapySupport = value)
    }

    fun setExplicitRecall(value: Boolean) {
        mutableState.value = mutableState.value.copy(explicitRecall = value)
    }

    fun startSpeech(permissionGranted: Boolean) {
        val before = mutableState.value
        if (before.processing || isSpeechActive(before.speechState)) return
        if (before.voicePreferences.stopWhenMicrophoneStarts) stopVoice()
        if (!permissionGranted) {
            mutableState.value = before.copy(
                speechState = SpeechCaptureState.ERROR,
                speechMessage = "Microphone permission is off; typing remains available",
                speechPermissionDenied = true,
            )
            return
        }
        if (!speechSession.begin(before.draft)) return
        when (val result = speechController.start()) {
            SpeechStartResult.Started,
            SpeechStartResult.AlreadyActive,
            -> Unit
            SpeechStartResult.PermissionRequired -> speechPermissionDenied()
            SpeechStartResult.Unavailable -> {
                if (mutableState.value.speechState == SpeechCaptureState.IDLE) {
                    onSpeechEvent(SpeechInputEvent.Failed(SpeechFailure.RECOGNIZER_UNAVAILABLE))
                }
            }
            is SpeechStartResult.Failed -> {
                if (mutableState.value.speechState == SpeechCaptureState.IDLE) {
                    onSpeechEvent(SpeechInputEvent.Failed(result.failure))
                }
            }
        }
    }

    fun stopSpeech() {
        if (mutableState.value.speechState != SpeechCaptureState.LISTENING) return
        speechSession.stopRequested()
        mutableState.value = mutableState.value.copy(
            speechState = SpeechCaptureState.FINALIZING,
            speechMessage = "Finalizing transcript; review before sending",
        )
        speechController.stop()
    }

    fun cancelSpeech() {
        if (!isSpeechActive(mutableState.value.speechState)) return
        speechController.cancel()
        if (isSpeechActive(mutableState.value.speechState)) {
            onSpeechEvent(SpeechInputEvent.Cancelled)
        }
    }

    fun stopVoice() {
        voiceProvider.stop()
        mutableState.value = mutableState.value.copy(voicePlaying = false)
    }

    fun replayLastThomasResponse() {
        val current = mutableState.value
        val text = current.lastValidatedThomasResponse ?: return
        speakValidatedResponse(text, current.voicePreferences)
    }

    fun setAutoSpeak(value: Boolean) = updateVoicePreferences { it.copy(autoSpeak = value) }

    fun setVoiceProfile(value: ThomasVoiceProfile) = updateVoicePreferences { it.copy(profile = value) }

    fun setSpeechRate(value: ThomasSpeechRate) = updateVoicePreferences { it.copy(rate = value) }

    fun setStopWhenMicrophoneStarts(value: Boolean) =
        updateVoicePreferences { it.copy(stopWhenMicrophoneStarts = value) }

    fun setColorProfile(value: ThomasColorProfile) = updateVoicePreferences { it.copy(colorProfile = value) }
    fun setTextSize(value: ThomasTextSize) = updateVoicePreferences { it.copy(textSize = value) }
    fun setReduceMotion(value: Boolean) = updateVoicePreferences { it.copy(reduceMotion = value) }
    fun setBackgroundDim(value: BackgroundDim) = updateVoicePreferences { it.copy(backgroundDim = value) }
    fun setBackgroundBlur(value: Boolean) = updateVoicePreferences { it.copy(backgroundBlur = value) }
    fun setBackgroundCrop(value: BackgroundCrop) = updateVoicePreferences { it.copy(backgroundCrop = value) }
    fun importBackground(uri: Uri) {
        val path = backgroundCustody.import(uri) ?: return
        updateVoicePreferences { it.copy(backgroundPath = path) }
    }
    fun removeBackground() {
        backgroundCustody.remove()
        updateVoicePreferences { it.copy(backgroundPath = null) }
    }

    fun previewVoice(profile: ThomasVoiceProfile) {
        val current = mutableState.value
        speakValidatedResponse(
            "Hi. I'm Thomas. Take your time, and tell me what's on your mind.",
            current.voicePreferences.copy(profile = profile),
        )
    }

    fun onBackgrounded() {
        cancelSpeech()
        stopVoice()
    }

    fun speechPermissionDenied() {
        mutableState.value = mutableState.value.copy(
            speechState = SpeechCaptureState.ERROR,
            speechMessage = "Microphone permission denied; typing remains available",
            speechPermissionDenied = true,
        )
    }

    fun submit(origin: ProductionInputOrigin? = null) {
        val before = mutableState.value
        if (before.processing || !before.runtimeAvailable || before.draft.isBlank() ||
            isSpeechActive(before.speechState)
        ) return
        val text = before.draft
        val inputOrigin = origin ?: before.draftOrigin
        val turnIndex = allocateTurnIndex()
        val submittedAt = SystemClock.elapsedRealtime()
        Log.i("ThomasTiming", "THOMAS_UI_TIMING stage=send_received turn=$turnIndex origin=$inputOrigin")
        val activityObserver = beginModelActivity(before)
        viewModelScope.launch {
            val result = try {
                withContext(Dispatchers.Default) {
                    root.runtime?.submit(
                        ProductionTurnRequest(
                            clientTurnIndex = turnIndex,
                            mode = before.mode,
                            committedText = text,
                            inputOrigin = inputOrigin,
                            privacy = if (before.privateTurn) {
                                ProductionTurnPrivacy.PRIVATE
                            } else {
                                ProductionTurnPrivacy.ELIGIBLE
                            },
                            journalResponsePreference = before.journalPreference,
                            requestedTherapySupport = before.therapySupport,
                            therapyMemoryIntent = if (before.explicitRecall) {
                                TherapyMemoryIntent.EXPLICIT_RECALL
                            } else {
                                TherapyMemoryIntent.ORDINARY
                            },
                            therapySafetyDeclaration = TherapySafetyDeclaration.UNSPECIFIED,
                            committedAt = Instant.now(),
                        ),
                    )
                }
            } finally {
                activityObserver.cancel()
            }
            val current = mutableState.value
            Log.i("ThomasTiming", "THOMAS_UI_TIMING stage=ui_ready turn=$turnIndex elapsed_ms=${SystemClock.elapsedRealtime() - submittedAt} result=${result?.disposition}")
            if (result == null) {
                mutableState.value = current.copy(
                    processing = false,
                    runtimeAvailable = false,
                    status = "Protected persistence is unavailable",
                )
            } else {
                val response = result.assistantArtifact?.text
                val messages = buildList {
                    addAll(current.transcript)
                    add(
                        TranscriptItem(
                            result.turnIdentity,
                            TranscriptRole.USER,
                            before.mode,
                            text,
                            result.committedSourceId != null,
                        ),
                    )
                    result.assistantArtifact?.let {
                        add(TranscriptItem("${result.turnIdentity}-thomas", TranscriptRole.THOMAS, before.mode, it.text))
                    }
                }
                drafts[before.mode] = ""
                draftOrigins[before.mode] = ProductionInputOrigin.TYPED
                speechSession.reset("")
                mutableState.value = current.copy(
                    draft = "",
                    draftOrigin = ProductionInputOrigin.TYPED,
                    transcript = messages,
                    sourceSummaries = root.runtime?.sourceSummaries().orEmpty(),
                    processing = false,
                    modelActivity = null,
                    speechState = SpeechCaptureState.IDLE,
                    speechMessage = null,
                    speechPermissionDenied = false,
                    status = result.disposition.name.replace('_', ' ').lowercase()
                        .replaceFirstChar(Char::uppercase),
                    lastValidatedThomasResponse = response ?: current.lastValidatedThomasResponse,
                )
                if (response != null && mutableState.value.voicePreferences.autoSpeak) {
                    speakValidatedResponse(response, mutableState.value.voicePreferences)
                }
            }
        }
    }

    fun resetAllPersonalData() {
        if (mutableState.value.processing || root.runtime == null) return
        mutableState.value = mutableState.value.copy(processing = true, status = "Resetting…")
        viewModelScope.launch {
            val succeeded = withContext(Dispatchers.Default) {
                runCatching { root.resetAndReopen() }.isSuccess
            }
            drafts.keys.forEach { drafts[it] = "" }
            draftOrigins.keys.forEach { draftOrigins[it] = ProductionInputOrigin.TYPED }
            speechController.cancel()
            speechSession.reset("")
            mutableState.value = ThomasUiState(
                runtimeAvailable = root.runtime != null,
                status = if (succeeded) "All local Thomas personal data was reset" else "Reset failed closed",
            )
        }
    }

    fun createCustodyController(resolver: ContentResolver) = AndroidDataCustodyController(resolver, root)

    fun notifyCustodyStatus(message: String) {
        mutableState.value = mutableState.value.copy(
            status = message,
            sourceSummaries = root.runtime?.sourceSummaries().orEmpty(),
        )
    }

    fun changeSourcePrivacy(summary: ProductionSourceSummary) {
        performSourceLifecycle("Updating privacy") { index ->
            root.runtime?.changeSourcePrivacy(summary.stableSourceId, summary.eligibleForOrdinaryUse, index)
        }
    }

    fun deleteSource(summary: ProductionSourceSummary) {
        performSourceLifecycle("Deleting source and dependent state") { index ->
            root.runtime?.deleteSource(summary.stableSourceId, index)
        }
    }

    fun reviseSource(summary: ProductionSourceSummary, correctedText: String) {
        if (mutableState.value.processing || root.runtime == null || correctedText.isBlank()) return
        mutableState.value = mutableState.value.copy(processing = true, status = "Recording source revision")
        val index = allocateTurnIndex()
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                root.runtime?.reviseSource(
                    summary.stableSourceId,
                    correctedText,
                    index,
                    com.conundrum.thomas.v2.longitudinal.ReportTime(Instant.now()),
                )
            }
            mutableState.value = mutableState.value.copy(
                processing = false,
                sourceSummaries = root.runtime?.sourceSummaries().orEmpty(),
                status = when {
                    result == null -> "Personal-data runtime unavailable"
                    result.accepted -> "Source revision recorded; prior wording retained as history"
                    else -> "Source revision rejected: ${result.admissionDisposition.name}"
                },
            )
        }
    }

    override fun onCleared() {
        voiceProvider.close()
        speechController.close()
        root.close()
    }

    private fun onSpeechEvent(event: SpeechInputEvent) {
        val current = mutableState.value
        when (event) {
            SpeechInputEvent.Listening -> mutableState.value = current.copy(
                speechState = SpeechCaptureState.LISTENING,
                speechMessage = "Listening; tap Done speaking when finished",
                speechPermissionDenied = false,
            )
            is SpeechInputEvent.Partial -> {
                val draft = speechSession.partial(event.text)
                drafts[current.mode] = draft
                draftOrigins[current.mode] = ProductionInputOrigin.SPEECH_TRANSCRIPT
                mutableState.value = current.copy(
                    draft = draft,
                    draftOrigin = ProductionInputOrigin.SPEECH_TRANSCRIPT,
                    speechState = SpeechCaptureState.LISTENING,
                    speechMessage = "Listening; review the transcript before sending",
                )
            }
            is SpeechInputEvent.Final -> {
                val draft = speechSession.finish(event.text)
                drafts[current.mode] = draft
                draftOrigins[current.mode] = ProductionInputOrigin.SPEECH_TRANSCRIPT
                Log.i("ThomasTiming", "THOMAS_UI_TIMING stage=stt_finalized composer_ready=true")
                mutableState.value = current.copy(
                    draft = draft,
                    draftOrigin = ProductionInputOrigin.SPEECH_TRANSCRIPT,
                    speechState = SpeechCaptureState.TRANSCRIPT_READY,
                    speechMessage = "Transcript ready; review or edit before sending",
                )
            }
            is SpeechInputEvent.Failed -> {
                val draft = speechSession.fail(event.failure)
                drafts[current.mode] = draft
                val permissionDenied = event.failure == SpeechFailure.PERMISSION_DENIED
                val origin = if (draft.isBlank()) {
                    ProductionInputOrigin.TYPED
                } else {
                    current.draftOrigin
                }
                draftOrigins[current.mode] = origin
                mutableState.value = current.copy(
                    draft = draft,
                    draftOrigin = origin,
                    speechState = speechSession.state,
                    speechMessage = event.failure.userMessage(),
                    speechPermissionDenied = permissionDenied,
                )
            }
            SpeechInputEvent.Cancelled -> {
                val draft = speechSession.cancel()
                drafts[current.mode] = draft
                val origin = if (draft.isBlank()) {
                    ProductionInputOrigin.TYPED
                } else {
                    current.draftOrigin
                }
                draftOrigins[current.mode] = origin
                mutableState.value = current.copy(
                    draft = draft,
                    draftOrigin = origin,
                    speechState = SpeechCaptureState.IDLE,
                    speechMessage = "Speech cancelled; existing text kept",
                )
            }
        }
    }

    private fun updateVoicePreferences(transform: (ThomasVoicePreferences) -> ThomasVoicePreferences) {
        val value = transform(mutableState.value.voicePreferences)
        voicePreferencesStore.write(value)
        mutableState.value = mutableState.value.copy(voicePreferences = value)
    }

    /** Only the already-rendered final assistant artifact reaches this method. */
    private fun speakValidatedResponse(text: String, preferences: ThomasVoicePreferences) {
        if (text.isBlank()) return
        voiceProvider.speak(
            ValidatedThomasResponse(text),
            ThomasVoicePresentation(preferences.profile, preferences.rate),
        ) { event ->
            val current = mutableState.value
            when (event) {
                ThomasVoiceEvent.Started -> mutableState.value = current.copy(voicePlaying = true)
                ThomasVoiceEvent.Completed, ThomasVoiceEvent.Stopped ->
                    mutableState.value = current.copy(voicePlaying = false)
                is ThomasVoiceEvent.Unavailable, is ThomasVoiceEvent.Failed ->
                    mutableState.value = current.copy(voicePlaying = false)
            }
        }
    }

    private fun isSpeechActive(state: SpeechCaptureState): Boolean =
        state == SpeechCaptureState.LISTENING || state == SpeechCaptureState.FINALIZING

    private fun requestBiographerPrompt() {
        val runtime = root.runtime ?: return
        val before = mutableState.value
        if (before.processing || before.mode != ProductionThomasMode.BIOGRAPHER) return
        // nextBiographerPrompt can enter the bounded local realizer.  It must never run
        // from the mode-selector click handler on the Android main thread.
        val activityObserver = beginModelActivity(before)
        viewModelScope.launch {
            val prompt = try {
                withContext(Dispatchers.Default) {
                    val index = runtime.allocateTurnIndex()
                    index to runCatching { runtime.nextBiographerPrompt(index) }.getOrNull()
                }
            } finally {
                activityObserver.cancel()
            }
            val current = mutableState.value
            if (current.mode != ProductionThomasMode.BIOGRAPHER) return@launch
            val (index, rendered) = prompt
            mutableState.value = current.copy(
                transcript = rendered?.let {
                    current.transcript + TranscriptItem(
                        "biographer-prompt-$index",
                        TranscriptRole.THOMAS,
                        ProductionThomasMode.BIOGRAPHER,
                        it.text,
                    )
                } ?: current.transcript,
                processing = false,
                modelActivity = null,
                status = if (rendered == null) {
                    "No governed biographer prompt available"
                } else {
                    "Biographer prompt ready"
                },
            )
        }
    }

    private fun beginModelActivity(before: ThomasUiState): Job {
        val initialActivity = currentModelActivity()
        mutableState.value = before.copy(
            processing = true,
            modelActivity = initialActivity,
            status = waitingStatus(initialActivity),
        )
        return viewModelScope.launch {
            while (isActive && mutableState.value.processing) {
                val current = mutableState.value
                val activity = currentModelActivity()
                if (current.modelActivity != activity) {
                    mutableState.value = current.copy(
                        modelActivity = activity,
                        status = waitingStatus(activity),
                    )
                }
                delay(80)
            }
        }
    }

    private fun currentModelActivity(): ThomasModelActivity = when (root.localModelStatus) {
        ThomasRealizerStatus.VERIFYING,
        ThomasRealizerStatus.LOADING,
        -> ThomasModelActivity.WAKING
        else -> ThomasModelActivity.THINKING
    }

    private fun waitingStatus(activity: ThomasModelActivity): String = when (activity) {
        ThomasModelActivity.WAKING -> "Thomas is waking up…"
        ThomasModelActivity.THINKING -> "Working…"
    }

    private fun allocateTurnIndex(): Long = requireNotNull(root.runtime).allocateTurnIndex()

    private fun performSourceLifecycle(
        pendingStatus: String,
        operation: (Long) -> com.conundrum.thomas.v2.runtime.ProductionSourceLifecycleResult?,
    ) {
        if (mutableState.value.processing || root.runtime == null) return
        mutableState.value = mutableState.value.copy(processing = true, status = pendingStatus)
        val index = allocateTurnIndex()
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) { operation(index) }
            mutableState.value = mutableState.value.copy(
                processing = false,
                sourceSummaries = root.runtime?.sourceSummaries().orEmpty(),
                status = when {
                    result == null -> "Personal-data runtime unavailable"
                    result.accepted -> result.action.name.replace('_', ' ').lowercase()
                        .replaceFirstChar(Char::uppercase)
                    else -> "Lifecycle request rejected: ${result.admissionDisposition.name}"
                },
            )
        }
    }
}

fun ProductionThomasMode.displayName(): String = when (this) {
    ProductionThomasMode.JOURNAL -> "Journal"
    ProductionThomasMode.BIOGRAPHER -> "Biographer"
    ProductionThomasMode.THERAPY -> "Therapy"
}
