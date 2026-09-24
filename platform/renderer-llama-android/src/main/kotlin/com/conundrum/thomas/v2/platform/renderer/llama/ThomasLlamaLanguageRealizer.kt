package com.conundrum.thomas.v2.platform.renderer.llama

import android.content.Context
import com.conundrum.thomas.v2.languagerenderer.CandidateManifest
import com.conundrum.thomas.v2.languagerenderer.CandidateRealization
import com.conundrum.thomas.v2.languagerenderer.CandidateRealizationOutcome
import com.conundrum.thomas.v2.languagerenderer.LanguageRealizer
import com.conundrum.thomas.v2.languagerenderer.RendererInput
import com.conundrum.thomas.v2.languagerenderer.SemanticAuthorityLabel
import com.conundrum.thomas.v2.languagerenderer.GovernedRenderMode
import java.util.concurrent.atomic.AtomicReference

/**
 * Local Thomas realizer. It has no access to policy, safety, persistence, retrieval, or mode
 * selection. Missing/corrupt custody is observable and falls through the governed renderer.
 */
class ThomasLlamaLanguageRealizer(
    context: Context,
    private val artifact: ThomasModelArtifact = ThomasModelLocation.artifact(context),
) : LanguageRealizer, AutoCloseable {
    private val verifier = ThomasModelArtifactVerifier(context.noBackupFilesDir)
    private val statusRef = AtomicReference(ThomasRealizerStatus.UNLOADED)
    private var loaded = false

    @Volatile
    var lastDiagnostic: String? = null
        private set

    val status: ThomasRealizerStatus
        get() = statusRef.get()

    override fun realize(input: RendererInput, attempt: Int): CandidateRealizationOutcome = synchronized(this) {
        if (input.mode == GovernedRenderMode.SAFETY) {
            // Fixed safety wording stays deterministic; a model never gets a safety decision path.
            return@synchronized CandidateRealizationOutcome.Unavailable("SAFETY_FIXED_REALIZATION")
        }
        if (attempt !in 1..2) return@synchronized CandidateRealizationOutcome.Failed("INVALID_ATTEMPT")
        try {
            ensureLoaded()
            val raw = NativeLlamaBridge.nativeGenerate(
                ThomasRealizationPrompt.system(input),
                ThomasRealizationPrompt.user(input),
                maximumTokens(input),
            ).trim()
            if (raw.isBlank()) return@synchronized CandidateRealizationOutcome.Failed("EMPTY_LOCAL_REALIZATION")
            statusRef.set(ThomasRealizerStatus.READY)
            lastDiagnostic = "LOCAL_MODEL_INVOKED:${RecoveredThomasQ6K.SHA256}"
            CandidateRealizationOutcome.Candidate(
                CandidateRealization(
                    text = raw,
                    adapterId = "ct-v2-thomas-llama",
                    adapterVersion = "${RecoveredThomasQ6K.MODEL_ID}-q6k-${RecoveredThomasQ6K.LLAMA_CPP_RELEASE}",
                    manifest = manifest(input),
                ),
            )
        } catch (error: Throwable) {
            statusRef.set(ThomasRealizerStatus.ERROR)
            lastDiagnostic = "LOCAL_MODEL_FAILURE:${error.javaClass.simpleName}"
            CandidateRealizationOutcome.Unavailable("LOCAL_MODEL_UNAVAILABLE")
        }
    }

    override fun close() = synchronized(this) {
        if (loaded) NativeLlamaBridge.nativeUnload()
        loaded = false
        statusRef.set(ThomasRealizerStatus.UNLOADED)
    }

    private fun ensureLoaded() {
        if (loaded) return
        statusRef.set(ThomasRealizerStatus.VERIFYING)
        val verified = verifier.verify(artifact)
        val identity = NativeLlamaBridge.nativeRuntimeIdentity().split('\t')
        check(identity.size >= 2 && identity[0] == RecoveredThomasQ6K.LLAMA_CPP_RELEASE &&
            identity[1] == RecoveredThomasQ6K.LLAMA_CPP_COMMIT) {
            "Pinned llama.cpp runtime identity mismatch"
        }
        statusRef.set(ThomasRealizerStatus.LOADING)
        NativeLlamaBridge.nativeLoad(verified.artifact.path.absolutePath)
        loaded = true
        statusRef.set(ThomasRealizerStatus.READY)
        lastDiagnostic = "MODEL_VERIFIED:${verified.actualSha256}:${verified.verifiedBytes}"
    }

    private fun maximumTokens(input: RendererInput): Int =
        when (input.budget.maximumSentences) {
            0 -> 1
            1, 2 -> 96
            else -> 160
        }

    private fun manifest(input: RendererInput): CandidateManifest = CandidateManifest(
        declaredMode = input.mode,
        declaredSemanticAct = input.semanticAct,
        referencedSemanticUnitIds = input.semanticUnits
            .filter { it.authorityLabel == SemanticAuthorityLabel.GOVERNED_SEMANTIC_MEANING }
            .map { it.id }.toSet(),
        referencedMemoryIds = input.historicalSupport.map { it.memoryObjectId }.toSet(),
    )
}

enum class ThomasRealizerStatus { UNLOADED, VERIFYING, LOADING, READY, ERROR }

internal object NativeLlamaBridge {
    init { System.loadLibrary("thomas_renderer_llama") }

    external fun nativeRuntimeIdentity(): String
    external fun nativeLoad(path: String): String
    external fun nativeGenerate(systemPrompt: String, userPrompt: String, maximumTokens: Int): String
    external fun nativeUnload()
}
