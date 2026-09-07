package com.conundrum.thomas.v2

import android.app.Application
import android.content.ContentResolver
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.conundrum.thomas.v2.engine.ordinary.RequestedOrdinarySupport
import com.conundrum.thomas.v2.journal.JournalResponsePreference
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
)

class ThomasViewModel(application: Application) : AndroidViewModel(application) {
    private val root = ThomasAndroidCompositionRoot.open(application)
    private val drafts = ProductionThomasMode.entries.associateWith { "" }.toMutableMap()
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
        drafts[mutableState.value.mode] = value
        mutableState.value = mutableState.value.copy(draft = value)
    }

    fun selectMode(mode: ProductionThomasMode) {
        val current = mutableState.value
        if (current.processing || current.mode == mode) return
        drafts[current.mode] = current.draft
        mutableState.value = current.copy(
            mode = mode,
            draft = drafts[mode].orEmpty(),
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

    fun submit(origin: ProductionInputOrigin = ProductionInputOrigin.TYPED) {
        val before = mutableState.value
        if (before.processing || !before.runtimeAvailable || before.draft.isBlank()) return
        val text = before.draft
        val turnIndex = allocateTurnIndex()
        mutableState.value = before.copy(processing = true, status = "Processing governed turn…")
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                root.runtime?.submit(
                    ProductionTurnRequest(
                        clientTurnIndex = turnIndex,
                        mode = before.mode,
                        committedText = text,
                        inputOrigin = origin,
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
                mutableState.value = current.copy(
                    draft = "",
                    transcript = messages,
                    sourceSummaries = root.runtime?.sourceSummaries().orEmpty(),
                    processing = false,
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
        root.close()
    }

    private fun requestBiographerPrompt() {
        val runtime = root.runtime ?: return
        val index = allocateTurnIndex()
        val prompt = runCatching { runtime.nextBiographerPrompt(index) }.getOrNull() ?: return
        mutableState.value = mutableState.value.copy(
            transcript = mutableState.value.transcript + TranscriptItem(
                "biographer-prompt-$index",
                TranscriptRole.THOMAS,
                ProductionThomasMode.BIOGRAPHER,
                prompt.text,
            ),
        )
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
