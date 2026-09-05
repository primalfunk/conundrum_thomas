package com.conundrum.thomas.v2.qualification.languageevidence

import com.conundrum.thomas.v2.languageevidence.GovernedLanguageEvidencePipeline as SharedPipeline
import com.conundrum.thomas.v2.languageevidence.perception.PerceptionContext
import com.conundrum.thomas.v2.languageevidence.stateformation.FormedLongitudinalState
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStore

typealias LanguagePipelineDisposition = com.conundrum.thomas.v2.languageevidence.LanguagePipelineDisposition
typealias GovernedLanguageProcessingResult = com.conundrum.thomas.v2.languageevidence.GovernedLanguageProcessingResult

/** Backward-compatible CT-V2-08 qualification adapter over the shared governed pipeline. */
class GovernedLanguageEvidencePipeline(store: QualificationLongitudinalStore) {
    private val delegate = SharedPipeline(
        store.admission,
        store.reader,
        StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY,
    )

    fun process(
        sourceRevisionId: SourceRecordId,
        context: PerceptionContext = PerceptionContext(),
    ): GovernedLanguageProcessingResult = delegate.process(sourceRevisionId, context)

    fun formState(): FormedLongitudinalState = delegate.formState()
}
