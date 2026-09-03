package com.conundrum.thomas.v2.longitudinal

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LongitudinalValidationTest {
    private val sourceId = SourceRecordId.parse("source-test-one")
    private val assertionId = AssertionId.parse("assertion-test-one")

    @Test fun `one source may yield multiple assertions`() {
        val source = source(sourceId)
        val a = assertion(assertionId, sourceId)
        val b = assertion(AssertionId.parse("assertion-test-two"), sourceId)
        val snapshot = LongitudinalEvidenceSnapshot(sources = listOf(source), assertions = listOf(a, b)).requireValid()
        assertEquals(listOf(a, b), snapshot.assertionsFrom(sourceId))
    }

    @Test fun `assertion without source is invalid`() {
        val snapshot = LongitudinalEvidenceSnapshot(assertions = listOf(assertion(assertionId, sourceId)))
        assertTrue(snapshot.validationIssues().any { it.code == "ASSERTION_SOURCE_MISSING" })
    }

    @Test fun `duplicate stable identifiers are invalid`() {
        val source = source(sourceId)
        val snapshot = LongitudinalEvidenceSnapshot(sources = listOf(source, source))
        assertTrue(snapshot.validationIssues().any { it.code == "DUPLICATE_SOURCE_ID" })
    }

    @Test fun `source revision cycle is invalid`() {
        val firstId = SourceRecordId.parse("source-revision-one")
        val secondId = SourceRecordId.parse("source-revision-two")
        fun revised(id: SourceRecordId, revision: Int, previous: SourceRecordId) = SourceRecord(
            id, PersonalEvidenceProvenance(AcquisitionMode.JOURNAL, sourceRevision = revision, previousRevisionId = previous),
            ReportTime(Instant.parse("2026-09-03T10:00:00Z")), RecordTime(Instant.parse("2026-09-03T10:00:01Z")),
            OriginalSourceContent.Inline("synthetic revision"),
        )
        val snapshot = LongitudinalEvidenceSnapshot(sources = listOf(revised(firstId, 2, secondId), revised(secondId, 3, firstId)))
        assertTrue(snapshot.validationIssues().any { it.code == "SOURCE_REVISION_CYCLE" })
    }

    @Test fun `direct fact about third party internal state cannot be constructed`() {
        assertThrows(IllegalArgumentException::class.java) {
            EvidenceAssertion(
                assertionId, sourceId, AssertionSubject.User,
                predicate(PredicateSemantics.THIRD_PARTY_INTERNAL_STATE), AssertionValue.Text("hidden state"),
                UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
            )
        }
    }

    @Test fun `correction must reference existing assertions and correction provenance`() {
        val source = source(sourceId)
        val original = assertion(assertionId, sourceId)
        val correctionId = AssertionId.parse("assertion-correction")
        val correction = assertion(correctionId, sourceId)
        val relation = CorrectionRelation(
            EvidenceRelationId.parse("relation-correction"), correctionId, assertionId,
            CorrectionEffect.CORRECTS_DETAIL, "synthetic correction",
        )
        val issues = LongitudinalEvidenceSnapshot(sources = listOf(source), assertions = listOf(original, correction), corrections = listOf(relation)).validationIssues()
        assertTrue(issues.any { it.code == "CORRECTION_SOURCE_MODE_INVALID" })
    }

    @Test fun `correction referencing nonexistent assertion is invalid`() {
        val correctionSourceId = SourceRecordId.parse("source-correction")
        val correctionSource = source(correctionSourceId, AcquisitionMode.USER_CORRECTION)
        val correctionId = AssertionId.parse("assertion-correction")
        val correction = assertion(correctionId, correctionSourceId)
        val relation = CorrectionRelation(
            EvidenceRelationId.parse("relation-missing-original"), correctionId, AssertionId.parse("assertion-missing"),
            CorrectionEffect.CORRECTS_DETAIL, "synthetic correction with missing target",
        )
        val issues = LongitudinalEvidenceSnapshot(
            sources = listOf(correctionSource), assertions = listOf(correction), corrections = listOf(relation),
        ).validationIssues()
        assertTrue(issues.any { it.code == "CORRECTED_ASSERTION_MISSING" })
    }

    @Test fun `supersession cycle is rejected without deleting claims`() {
        val source = source(sourceId)
        val a = assertion(assertionId, sourceId)
        val bId = AssertionId.parse("assertion-test-two")
        val b = assertion(bId, sourceId)
        val first = SupersessionRelation(EvidenceRelationId.parse("relation-first"), ClaimReference.Assertion(bId), ClaimReference.Assertion(assertionId), SupersessionKind.REFINES, "refines")
        val second = SupersessionRelation(EvidenceRelationId.parse("relation-second"), ClaimReference.Assertion(assertionId), ClaimReference.Assertion(bId), SupersessionKind.REPLACES, "would create a cycle")
        val snapshot = LongitudinalEvidenceSnapshot(sources = listOf(source), assertions = listOf(a, b), supersessions = listOf(first, second))
        assertTrue(snapshot.validationIssues().any { it.code == "SUPERSESSION_CYCLE" })
        assertEquals(2, snapshot.assertions.size)
    }

    @Test fun `hypothesis needs visible dependencies`() {
        val hypothesis = hypothesis(HypothesisId.parse("hypothesis-one"))
        val snapshot = LongitudinalEvidenceSnapshot(hypotheses = listOf(hypothesis))
        assertTrue(snapshot.validationIssues().any { it.code == "HYPOTHESIS_WITHOUT_DEPENDENCY" })
    }

    @Test fun `hypothesis dependency cycle is rejected`() {
        val firstId = HypothesisId.parse("hypothesis-one")
        val secondId = HypothesisId.parse("hypothesis-two")
        val first = hypothesis(firstId)
        val second = hypothesis(secondId)
        val dependencies = listOf(
            HypothesisDependency(EvidenceRelationId.parse("dependency-first"), firstId, ClaimReference.Hypothesis(secondId), DependencyRole.SUPPORTS, "second supports first"),
            HypothesisDependency(EvidenceRelationId.parse("dependency-second"), secondId, ClaimReference.Hypothesis(firstId), DependencyRole.SUPPORTS, "first supports second"),
        )
        val snapshot = LongitudinalEvidenceSnapshot(hypotheses = listOf(first, second), hypothesisDependencies = dependencies)
        assertTrue(snapshot.validationIssues().any { it.code == "HYPOTHESIS_DEPENDENCY_CYCLE" })
    }

    @Test fun `established same and different identity chain is invalid`() {
        val source = source(sourceId)
        val a = assertion(assertionId, sourceId)
        val ids = listOf("person-a", "person-b", "person-c").map(LifeEntityId::parse)
        val entities = ids.map { Person(it, it.value, setOf(assertionId)) }
        val links = listOf(
            EntityIdentityLink(IdentityLinkId.parse("identity-ab"), ids[0], ids[1], EntityIdentityStatus.ESTABLISHED_SAME_ENTITY, setOf(assertionId), "verified same"),
            EntityIdentityLink(IdentityLinkId.parse("identity-bc"), ids[1], ids[2], EntityIdentityStatus.ESTABLISHED_SAME_ENTITY, setOf(assertionId), "verified same"),
            EntityIdentityLink(IdentityLinkId.parse("identity-ac-different"), ids[0], ids[2], EntityIdentityStatus.ESTABLISHED_DIFFERENT_ENTITY, setOf(assertionId), "incompatible different"),
        )
        val snapshot = LongitudinalEvidenceSnapshot(listOf(source), listOf(a), entities, identityLinks = links)
        assertTrue(snapshot.validationIssues().any { it.code == "SELF_CONTRADICTORY_IDENTITY_LINKAGE" })
    }

    @Test fun `missing entity references are invalid`() {
        val source = source(sourceId)
        val assertion = EvidenceAssertion(
            assertionId, sourceId, AssertionSubject.Entity(LifeEntityId.parse("missing-person")),
            predicate(PredicateSemantics.OTHER), AssertionValue.Text("synthetic"),
            UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
        )
        val snapshot = LongitudinalEvidenceSnapshot(sources = listOf(source), assertions = listOf(assertion))
        assertTrue(snapshot.validationIssues().any { it.code == "ASSERTION_ENTITY_MISSING" })
    }

    @Test fun `require valid reports deterministic ordered issues`() {
        val invalid = LongitudinalEvidenceSnapshot(assertions = listOf(assertion(assertionId, sourceId)))
        val first = invalid.validationIssues()
        val second = invalid.validationIssues()
        assertEquals(first, second)
        assertThrows(IllegalArgumentException::class.java) { invalid.requireValid() }
    }

    private fun source(id: SourceRecordId, mode: AcquisitionMode = AcquisitionMode.JOURNAL) = SourceRecord(
        id, PersonalEvidenceProvenance(mode), ReportTime(Instant.parse("2026-09-03T10:00:00Z")),
        RecordTime(Instant.parse("2026-09-03T10:00:01Z")), OriginalSourceContent.Inline("synthetic source"),
    )

    private fun assertion(id: AssertionId, source: SourceRecordId) = EvidenceAssertion(
        id, source, AssertionSubject.User, predicate(PredicateSemantics.OTHER), AssertionValue.Text("synthetic assertion"),
        UserEvidenceKind.EXPLICIT_USER_ASSERTION, AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
    )

    private fun predicate(semantics: PredicateSemantics) = AssertionPredicate(PersonalConceptId.parse("test.concept"), semantics)

    private fun hypothesis(id: HypothesisId) = ThomasHypothesis(
        id, AssertionSubject.User, predicate(PredicateSemantics.EVALUATION_OR_MEANING), AssertionValue.Text("synthetic hypothesis"),
        HypothesisStatus.TENTATIVE, RecordTime(Instant.parse("2026-09-03T10:00:02Z")), "Synthetic hypothesis rationale.",
    )
}
