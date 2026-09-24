package com.conundrum.thomas.v2

import android.app.Application
import android.content.ContentResolver
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
import com.conundrum.thomas.v2.platform.speech.SpeechStartResult
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class TranscriptRole { USER, THOMAS, SYSTEM }

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
    private val mutableState = MutableStateFlow(
        ThomasUiState(
            runtimeAvailable = root.runtime != null,
            status = root.unavailableReason?.let { "Personal data unavailable: $it" } ?: "Ready",
            sourceSummaries = root.runtime?.sourceSummaries().orEmpty(),
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
        mutableState.value = before.copy(processing = true, status = "Processing governed turn…")
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
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
            val current = mutableState.value
            if (result == null) {
                mutableState.value = current.copy(
                    processing = false,
                    runtimeAvailable = false,
                    status = "Protected persistence is unavailable",
                )
            } else {
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
                    speechState = SpeechCaptureState.IDLE,
                    speechMessage = null,
                    speechPermissionDenied = false,
                    status = result.disposition.name.replace('_', ' ').lowercase()
                        .replaceFirstChar(Char::uppercase),
                )
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

    private fun isSpeechActive(state: SpeechCaptureState): Boolean =
        state == SpeechCaptureState.LISTENING || state == SpeechCaptureState.FINALIZING

    private fun requestBiographerPrompt() {
        val runtime = root.runtime ?: return
        val before = mutableState.value
        if (before.processing || before.mode != ProductionThomasMode.BIOGRAPHER) return
        // nextBiographerPrompt can enter the bounded local realizer.  It must never run
        // from the mode-selector click handler on the Android main thread.
        mutableState.value = before.copy(
            processing = true,
            status = "Preparing governed biographer prompt…",
        )
        viewModelScope.launch {
            val prompt = withContext(Dispatchers.Default) {
                val index = runtime.allocateTurnIndex()
                index to runCatching { runtime.nextBiographerPrompt(index) }.getOrNull()
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
                status = if (rendered == null) {
                    "No governed biographer prompt available"
                } else {
                    "Biographer prompt ready"
                },
            )
        }
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
