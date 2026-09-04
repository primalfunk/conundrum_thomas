package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.ContradictionAdjudication
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.CorrectionEffect
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.DependencyRole
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.HypothesisDependency
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.IdentityLinkId
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.Person
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.RecordTime
import com.conundrum.thomas.v2.longitudinal.ThomasHypothesis
import com.conundrum.thomas.v2.retrieval.ContextBudget
import com.conundrum.thomas.v2.retrieval.DeterministicLongitudinalRetriever
import com.conundrum.thomas.v2.retrieval.RetrievalAnchors
import com.conundrum.thomas.v2.retrieval.RetrievalDisposition
import com.conundrum.thomas.v2.retrieval.RetrievalIntent
import com.conundrum.thomas.v2.retrieval.RetrievalItemKind
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalMode
import com.conundrum.thomas.v2.retrieval.RetrievalObjectType
import com.conundrum.thomas.v2.retrieval.RetrievedPayload
import java.time.Instant
import java.time.Year
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue

internal object CTV211ScenariosA {
    private val S = CTV211TestSupport

    fun run(id: Int) = when (id) {
        1 -> assertEquals(RetrievalDisposition.EMPTY, S.retrieve(S.archive()).disposition)
        2 -> assertEquals(listOf("assertion.relevant"), selected(basic()))
        3 -> assertEquals(listOf("assertion.relevant"), selected(basicWithUnrelated(10)))
        4 -> {
            val archive = basic()
            assertEquals(S.packet(archive).packet!!.metadata.packetDigest, S.packet(archive).packet!!.metadata.packetDigest)
        }
        5 -> {
            val result = S.retrieve(S.archive(CTV211TestSupport.EvidenceParts(listOf(
                CTV211TestSupport.Claim("zeta", "Work Z", "topic.work"), CTV211TestSupport.Claim("alpha", "Work A", "topic.work"),
            ))))
            assertEquals("assertion.alpha", result.selectedItems.first().stableId)
        }
        6 -> {
            val claim = CTV211TestSupport.Claim("private", "Relevant private work")
            val lifecycle = mapOf(S.lifecycle(RetrievalObjectType.SOURCE_REVISION, S.sourceRevisionId(claim).value,
                RetrievalLifecycleStatus.PRIVATE_INELIGIBLE, false))
            val result = S.retrieve(S.archive(CTV211TestSupport.EvidenceParts(listOf(claim)), lifecycle = lifecycle))
            assertTrue(result.selectedItems.isEmpty()); assertTrue(result.exclusions.privateCount > 0)
        }
        7 -> assertHypothesisExcluded(RetrievalLifecycleStatus.RETIRED)
        8 -> assertSupersededRevisionExcluded()
        9 -> {
            val parts = hypothesisParts(HypothesisStatus.REQUIRES_REVIEW)
            assertTrue(S.retrieve(S.archive(parts), ordinaryHypothesisRequest()).selectedItems.none { it.kind == RetrievalItemKind.HYPOTHESIS })
        }
        10 -> assertHypothesisExcluded(RetrievalLifecycleStatus.DEPENDENCY_BLOCKED)
        11 -> assertTrue(S.retrieve(S.archive(), S.request(anchors = RetrievalAnchors(openQuestionIds = setOf("coverage.declined")))).selectedItems.isEmpty())
        12 -> assertPrivacyRevisionImmediatelyChangesResult()
        13 -> assertTrue(selected(correctionBefore()).contains("assertion.move-old"))
        14 -> assertEquals(1, correctionAfter().evidence.corrections.size)
        15 -> assertTrue(selected(correctionAfter()).contains("assertion.move-new"))
        16 -> assertHistoricalOriginalIsAuditOnly()
        17 -> assertNotEquals(S.packet(correctionBefore()).packet!!.metadata.packetDigest,
            S.packet(correctionAfter()).packet!!.metadata.packetDigest)
        18 -> assertBalancedHypothesis()
        19 -> assertTightBudgetOmitsHypothesis()
        20 -> assertContradictionCarriesBothClaims()
        21 -> assertEquals(ContradictionAdjudication.UNRESOLVED,
            ((contradictionResult().selectedItems.first { it.kind == RetrievalItemKind.CONTRADICTION }.payload)
                as RetrievedPayload.Contradiction).value.adjudication)
        22 -> assertIdentity(false)
        23 -> assertIdentity(true)
        24 -> assertIdentityRevisionChangesGrouping()
        25 -> assertLexicalNameDoesNotMerge()
        26 -> assertEquals("assertion.current", temporalResult(EventTime.ApproximateYear(Year.of(2040))).selectedItems.first().stableId)
        27 -> assertEquals("assertion.old", temporalResult(EventTime.ApproximateYear(Year.of(1998))).selectedItems.first().stableId)
        28 -> assertTrue(temporalResult(EventTime.ApproximateYear(Year.of(1998))).selectedItems.first().eventTime is EventTime.ApproximateYear)
        29 -> assertTrue(selected(S.archive(CTV211TestSupport.EvidenceParts(listOf(CTV211TestSupport.Claim("unknown", "Work date unknown"))))).contains("assertion.unknown"))
        30 -> assertTrue(S.archive(CTV211TestSupport.EvidenceParts(listOf(
            CTV211TestSupport.Claim("liked", "I liked the job at first", time = EventTime.ApproximateYear(Year.of(2010))),
            CTV211TestSupport.Claim("hated", "I disliked the job later", time = EventTime.ApproximateYear(Year.of(2015))),
        ))).evidence.contradictions.isEmpty())
        31 -> assertEquals(RetrievalDisposition.EMPTY, S.retrieve(basic(), S.request(mode = RetrievalMode.JOURNAL)).disposition)
        32 -> {
            val result = S.retrieve(basic(), S.request(intent = RetrievalIntent.EXPLICIT_LOOK_BACK,
                mode = RetrievalMode.JOURNAL, anchors = RetrievalAnchors(lexicalTerms = setOf("relevant")), userDirected = true))
            assertTrue(result.selectedItems.isNotEmpty())
        }
        else -> error("Scenario A does not own $id")
    }

    private fun basic() = S.archive(CTV211TestSupport.EvidenceParts(listOf(CTV211TestSupport.Claim("relevant", "Relevant work decision"))))
    private fun basicWithUnrelated(count: Int) = S.archive(CTV211TestSupport.EvidenceParts(
        listOf(CTV211TestSupport.Claim("relevant", "Relevant work decision")) + (1..count).map {
            CTV211TestSupport.Claim("unrelated-$it", "Unrelated archive item $it", "topic.unrelated")
        },
    ))
    private fun selected(archive: com.conundrum.thomas.v2.retrieval.RetrievalArchiveSnapshot) =
        S.retrieve(archive, S.request(revision = archive.storeRevision)).selectedItems.map { it.stableId }

    private fun hypothesisParts(status: HypothesisStatus = HypothesisStatus.TENTATIVE): CTV211TestSupport.EvidenceParts {
        val claims = listOf(CTV211TestSupport.Claim("support", "I reported a work concern"))
        val hypothesis = ThomasHypothesis(
            HypothesisId.parse("hypothesis.work"), AssertionSubject.User,
            AssertionPredicate(PersonalConceptId.parse("topic.work"), PredicateSemantics.OTHER),
            AssertionValue.Text("A tentative structural observation"), status,
            RecordTime(Instant.parse("2040-09-03T12:00:02Z")), "Synthetic evidence-bounded rationale",
        )
        val dependency = HypothesisDependency(
            EvidenceRelationId.parse("dependency.support"), hypothesis.id,
            ClaimReference.Assertion(S.assertionId("support")), DependencyRole.SUPPORTS, "Direct synthetic support",
        )
        return CTV211TestSupport.EvidenceParts(claims, hypotheses = listOf(hypothesis), dependencies = listOf(dependency))
    }

    private fun balancedHypothesisParts(): CTV211TestSupport.EvidenceParts {
        val base = hypothesisParts()
        val counter = CTV211TestSupport.Claim("counter", "A contrary work report")
        val weaken = HypothesisDependency(
            EvidenceRelationId.parse("dependency.counter"), base.hypotheses.single().id,
            ClaimReference.Assertion(S.assertionId("counter")), DependencyRole.WEAKENS, "Direct synthetic counterevidence",
        )
        return base.copy(claims = base.claims + counter, dependencies = base.dependencies + weaken)
    }

    private fun hypothesisRequest(budget: ContextBudget = ContextBudget()) = S.request(
        intent = RetrievalIntent.EXPLAIN_DERIVED_OBJECT,
        anchors = RetrievalAnchors(hypothesisIds = setOf(HypothesisId.parse("hypothesis.work"))),
        budget = budget,
    )
    private fun ordinaryHypothesisRequest() = S.request(
        anchors = RetrievalAnchors(hypothesisIds = setOf(HypothesisId.parse("hypothesis.work"))),
    )

    private fun assertHypothesisExcluded(status: RetrievalLifecycleStatus) {
        val lifecycle = mapOf(S.lifecycle(RetrievalObjectType.HYPOTHESIS, "hypothesis.work", status, false))
        val result = S.retrieve(S.archive(hypothesisParts(), lifecycle = lifecycle), ordinaryHypothesisRequest())
        assertTrue(result.selectedItems.none { it.kind == RetrievalItemKind.HYPOTHESIS })
    }

    private fun revisionClaims() = listOf(
        CTV211TestSupport.Claim("wording-old", "Old wording", stableSource = "wording", revision = 1),
        CTV211TestSupport.Claim("wording-new", "New wording", stableSource = "wording", revision = 2,
            previousRevision = "source.wording-old.rev1"),
    )

    private fun assertSupersededRevisionExcluded() {
        val parts = CTV211TestSupport.EvidenceParts(revisionClaims())
        val archive = S.archive(parts)
        val request = S.request(intent = RetrievalIntent.EXPLICIT_LOOK_BACK, mode = RetrievalMode.JOURNAL,
            anchors = RetrievalAnchors(lexicalTerms = setOf("wording")), userDirected = true)
        val result = S.retrieve(archive, request)
        assertFalse(result.selectedItems.any { it.stableId == "source.wording-old.rev1" })
        assertTrue(result.exclusions.supersededRevisionCount > 0)
    }

    private fun assertPrivacyRevisionImmediatelyChangesResult() {
        val claim = CTV211TestSupport.Claim("privacy", "Relevant work evidence")
        val eligible = S.archive(CTV211TestSupport.EvidenceParts(listOf(claim)), revision = 1, digestSeed = "eligible")
        val private = S.archive(CTV211TestSupport.EvidenceParts(listOf(claim)), revision = 2, lifecycle = mapOf(
            S.lifecycle(RetrievalObjectType.SOURCE_REVISION, S.sourceRevisionId(claim).value,
                RetrievalLifecycleStatus.PRIVATE_INELIGIBLE, false, 2),
        ), digestSeed = "private")
        val retriever = DeterministicLongitudinalRetriever(CTV211TestSupport.Port(mapOf(1L to eligible, 2L to private)))
        assertTrue(retriever.retrieve(S.request(revision = 1)).selectedItems.isNotEmpty())
        assertTrue(retriever.retrieve(S.request(revision = 2)).selectedItems.isEmpty())
    }

    private fun correctionParts(): CTV211TestSupport.EvidenceParts {
        val old = CTV211TestSupport.Claim("move-old", "I moved in 2012", "topic.work")
        val corrected = CTV211TestSupport.Claim("move-new", "I moved in 2013", "topic.work", mode = com.conundrum.thomas.v2.longitudinal.AcquisitionMode.USER_CORRECTION)
        return CTV211TestSupport.EvidenceParts(
            claims = listOf(old, corrected),
            corrections = listOf(CorrectionRelation(EvidenceRelationId.parse("correction.move"),
                S.assertionId("move-new"), S.assertionId("move-old"), CorrectionEffect.CORRECTS_DETAIL,
                "User explicitly corrected the year")),
        )
    }

    private fun correctionBefore() = S.archive(CTV211TestSupport.EvidenceParts(listOf(CTV211TestSupport.Claim("move-old", "I moved in 2012"))), digestSeed = "before")
    private fun correctionAfter() = S.archive(correctionParts(), revision = 2, lifecycle = mapOf(
        S.lifecycle(RetrievalObjectType.ASSERTION, "assertion.move-old", RetrievalLifecycleStatus.SUPERSEDED, false, 2),
    ), digestSeed = "after")

    private fun assertHistoricalOriginalIsAuditOnly() {
        val result = S.retrieve(correctionAfter(), S.request(revision = 2, intent = RetrievalIntent.EXPLICIT_SOURCE_RECALL,
            anchors = RetrievalAnchors(sourceRevisionIds = setOf(S.sourceRevisionId(CTV211TestSupport.Claim("move-old", "I moved in 2012")))),
            userDirected = true))
        assertTrue(result.selectedItems.any { it.stableId == "source.move-old.rev1" })
    }

    private fun assertBalancedHypothesis() {
        val result = S.retrieve(S.archive(balancedHypothesisParts()), hypothesisRequest())
        assertTrue(result.selectedItems.any { it.kind == RetrievalItemKind.HYPOTHESIS })
        assertTrue(result.selectedItems.any { com.conundrum.thomas.v2.retrieval.RetrievalReason.DIRECT_EVIDENCE in it.reasons })
        assertTrue(result.selectedItems.any { com.conundrum.thomas.v2.retrieval.RetrievalReason.REPRESENTATIVE_COUNTEREVIDENCE in it.reasons })
    }

    private fun assertTightBudgetOmitsHypothesis() {
        val result = S.retrieve(S.archive(balancedHypothesisParts()), hypothesisRequest(ContextBudget(maximumLongitudinalObjects = 1)))
        assertTrue(result.selectedItems.none { it.kind == RetrievalItemKind.HYPOTHESIS })
    }

    private fun contradictionParts() = CTV211TestSupport.EvidenceParts(
        claims = listOf(CTV211TestSupport.Claim("year-2012", "The move was in 2012"), CTV211TestSupport.Claim("year-2013", "The move was in 2013")),
        contradictions = listOf(ContradictionRelation(EvidenceRelationId.parse("contradiction.move-year"),
            S.assertionId("year-2012"), S.assertionId("year-2013"), rationale = "Mutually exclusive reported year")),
    )
    private fun contradictionResult() = S.retrieve(S.archive(contradictionParts()), S.request(
        anchors = RetrievalAnchors(assertionIds = setOf(S.assertionId("year-2012"))),
    ))
    private fun assertContradictionCarriesBothClaims() {
        val ids = contradictionResult().selectedItems.map { it.stableId }
        assertTrue("assertion.year-2012" in ids && "assertion.year-2013" in ids && "contradiction.move-year" in ids)
    }

    private fun identityParts(resolved: Boolean): Pair<CTV211TestSupport.EvidenceParts, LifeEntityId> {
        val left = LifeEntityId.parse("person.sam-work")
        val right = LifeEntityId.parse("person.sam-roommate")
        val claims = listOf(
            CTV211TestSupport.Claim("sam-work", "Sam from work", value = AssertionValue.EntityReference(left)),
            CTV211TestSupport.Claim("sam-roommate", "Sam my old roommate", value = AssertionValue.EntityReference(right)),
        )
        val people = listOf(Person(left, "Sam from work", setOf(S.assertionId("sam-work"))),
            Person(right, "Sam the old roommate", setOf(S.assertionId("sam-roommate"))))
        val status = if (resolved) EntityIdentityStatus.ESTABLISHED_SAME_ENTITY else EntityIdentityStatus.UNRESOLVED
        val link = EntityIdentityLink(IdentityLinkId.parse("identity.sam"), left, right, status,
            if (resolved) setOf(S.assertionId("sam-work")) else emptySet(), "Synthetic identity decision")
        return CTV211TestSupport.EvidenceParts(claims, people, identities = listOf(link)) to left
    }
    private fun assertIdentity(resolved: Boolean) {
        val (parts, left) = identityParts(resolved)
        val result = S.retrieve(S.archive(parts), S.request(anchors = RetrievalAnchors(entityIds = setOf(left))))
        val entityItems = result.selectedItems.filter { it.kind == RetrievalItemKind.ENTITY }
        assertEquals(if (resolved) 2 else 1, entityItems.size)
        if (!resolved) assertTrue(entityItems.single().unresolvedIdentity)
    }
    private fun assertIdentityRevisionChangesGrouping() {
        val (unresolvedParts, left) = identityParts(false)
        val (resolvedParts, _) = identityParts(true)
        val one = S.retrieve(S.archive(unresolvedParts, 1), S.request(1, anchors = RetrievalAnchors(entityIds = setOf(left))))
        val two = S.retrieve(S.archive(resolvedParts, 2), S.request(2, anchors = RetrievalAnchors(entityIds = setOf(left))))
        assertTrue(one.selectedItems.count { it.kind == RetrievalItemKind.ENTITY } < two.selectedItems.count { it.kind == RetrievalItemKind.ENTITY })
    }
    private fun assertLexicalNameDoesNotMerge() {
        val (parts, _) = identityParts(false)
        val result = S.retrieve(S.archive(parts), S.request(intent = RetrievalIntent.EXPLICIT_LOOK_BACK,
            mode = RetrievalMode.JOURNAL, anchors = RetrievalAnchors(lexicalTerms = setOf("sam")), userDirected = true))
        assertEquals(2, result.selectedItems.filter { it.kind == RetrievalItemKind.SOURCE }.map { it.stableId }.distinct().size)
    }

    private fun temporalResult(anchor: EventTime) = S.retrieve(S.archive(CTV211TestSupport.EvidenceParts(listOf(
        CTV211TestSupport.Claim("old", "Old work evidence", time = EventTime.ApproximateYear(Year.of(1998))),
        CTV211TestSupport.Claim("current", "Current work evidence", time = EventTime.ApproximateYear(Year.of(2040))),
    ))), S.request(anchors = RetrievalAnchors(predicateIds = setOf(PersonalConceptId.parse("topic.work")), temporalBounds = listOf(anchor))))
}
