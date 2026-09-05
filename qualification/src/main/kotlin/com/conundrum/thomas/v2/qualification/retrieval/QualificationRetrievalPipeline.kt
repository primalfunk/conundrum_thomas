package com.conundrum.thomas.v2.qualification.retrieval

import com.conundrum.thomas.v2.contextpacket.ContextPacketBuildRequest
import com.conundrum.thomas.v2.contextpacket.ContextPacketBuildResult
import com.conundrum.thomas.v2.contextpacket.DeterministicContextPacketBuilder
import com.conundrum.thomas.v2.longitudinal.admission.StoreDataClassification
import com.conundrum.thomas.v2.longitudinal.store.LongitudinalReader
import com.conundrum.thomas.v2.longitudinal.store.QualificationLongitudinalStore
import com.conundrum.thomas.v2.retrieval.DeterministicLongitudinalRetriever
import com.conundrum.thomas.v2.retrieval.GovernedLongitudinalReadPort
import com.conundrum.thomas.v2.retrieval.RetrievalArchiveSnapshot
import com.conundrum.thomas.v2.retrieval.RetrievalOpenQuestion
import com.conundrum.thomas.v2.retrieval.RetrievalRecurrenceCandidate

/** CT-V2-11 qualification composition over the shared read-only governed adapter. */
class QualificationRetrievalPipeline(
    store: QualificationLongitudinalStore,
    openQuestions: (Long) -> List<RetrievalOpenQuestion> = { emptyList() },
    recurrences: (Long) -> List<RetrievalRecurrenceCandidate> = { emptyList() },
) {
    private val builder = DeterministicContextPacketBuilder(
        DeterministicLongitudinalRetriever(
            GovernedLongitudinalReadPort(
                store.reader,
                StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY,
                openQuestions,
                recurrences,
            ),
        ),
    )

    fun build(request: ContextPacketBuildRequest): ContextPacketBuildResult = builder.build(request)
}

class QualificationLongitudinalReadPort(
    reader: LongitudinalReader,
    openQuestions: (Long) -> List<RetrievalOpenQuestion> = { emptyList() },
    recurrences: (Long) -> List<RetrievalRecurrenceCandidate> = { emptyList() },
) : com.conundrum.thomas.v2.retrieval.LongitudinalRetrievalReadPort {
    private val delegate = GovernedLongitudinalReadPort(
        reader,
        StoreDataClassification.SYNTHETIC_QUALIFICATION_ONLY,
        openQuestions,
        recurrences,
    )

    override fun read(asOfRevision: Long): RetrievalArchiveSnapshot = delegate.read(asOfRevision)
}
