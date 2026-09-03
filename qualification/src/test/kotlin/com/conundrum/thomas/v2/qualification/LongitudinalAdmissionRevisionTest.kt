package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.AssertionPredicate
import com.conundrum.thomas.v2.longitudinal.AssertionSubject
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.AssertionValue
import com.conundrum.thomas.v2.longitudinal.ClaimReference
import com.conundrum.thomas.v2.longitudinal.ContradictionRelation
import com.conundrum.thomas.v2.longitudinal.CorrectionEffect
import com.conundrum.thomas.v2.longitudinal.CorrectionRelation
import com.conundrum.thomas.v2.longitudinal.CoverageTopic
import com.conundrum.thomas.v2.longitudinal.CoverageTopicId
import com.conundrum.thomas.v2.longitudinal.DependencyRole
import com.conundrum.thomas.v2.longitudinal.EntityIdentityLink
import com.conundrum.thomas.v2.longitudinal.EntityIdentityStatus
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceAssertion
import com.conundrum.thomas.v2.longitudinal.EvidenceRelationId
import com.conundrum.thomas.v2.longitudinal.HypothesisDependency
import com.conundrum.thomas.v2.longitudinal.HypothesisId
import com.conundrum.thomas.v2.longitudinal.HypothesisStatus
import com.conundrum.thomas.v2.longitudinal.IdentityLinkId
import com.conundrum.thomas.v2.longitudinal.InformationCoverageStatus
import com.conundrum.thomas.v2.longitudinal.LifeEntityId
import com.conundrum.thomas.v2.longitudinal.OriginalSourceContent
import com.conundrum.thomas.v2.longitudinal.Person
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.PredicateSemantics
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceAuthorRole
import com.conundrum.thomas.v2.longitudinal.SourceIdentityId
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.longitudinal.SupersessionKind
import com.conundrum.thomas.v2.longitudinal.SupersessionRelation
import com.conundrum.thomas.v2.longitudinal.UserEvidenceKind
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionActor
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionDisposition
import com.conundrum.thomas.v2.longitudinal.admission.AdmissionOrigin
import com.conundrum.thomas.v2.longitudinal.admission.EvidenceBundle
import com.conundrum.thomas.v2.longitudinal.admission.HypothesisDraft
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalLifecycleStatus
import com.conundrum.thomas.v2.longitudinal.admission.LongitudinalWriteOperation
import com.conundrum.thomas.v2.longitudinal.admission.SourcePrivacy
import com.conundrum.thomas.v2.longitudinal.admission.assertionRef
import com.conundrum.thomas.v2.longitudinal.admission.hypothesisRef
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.admitAssertion
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.admitSource
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.assertAccepted
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.assertion
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.path
import com.conundrum.thomas.v2.qualification.CTV207TestSupport.sourceDraft
import com.conundrum.thomas.v2.qualification.longitudinalstore.SyntheticLongitudinalStoreHarness
import java.time.Instant
import java.time.Year
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LongitudinalAdmissionRevisionTest {
    @Test fun `source wording revision appends and preserves source semantics`() {
        SyntheticLongitudinalStoreHarness(path("source-revision")).use { harness ->
            val draft = sourceDraft("revision", eventTime = EventTime.ApproximateYear(Year.of(2012)))
            assertAccepted(admitSource(harness, draft))
            val operation = LongitudinalWriteOperation.AppendSourceRevision(
                draft.stableSourceId, draft.revisionId, SourceRecordId.parse("source-revision-rev-2"),
                OriginalSourceContent.Inline("Revised synthetic wording."), ReportTime(Instant.parse("2039-01-02T00:00:00Z")),
            )
            assertAccepted(harness.store.admission.submit(harness.request(operation, AdmissionActor.USER, AdmissionOrigin.JOURNAL)))
            val history = harness.store.reader.sourceRevisionHistory(draft.stableSourceId)
            assertEquals(2, history.size)
            assertEquals(draft.eventTime, history[0].eventTime)
            assertEquals(history[0].eventTime, history[1].eventTime)
            assertEquals(history[0].stableSourceId, history[1].stableSourceId)
            assertEquals(history[0].provenance.acquisitionMode, history[1].provenance.acquisitionMode)
            assertEquals(LongitudinalLifecycleStatus.SUPERSEDED, harness.store.reader.lifecycle(com.conundrum.thomas.v2.longitudinal.admission.LongitudinalObjectRef(com.conundrum.thomas.v2.longitudinal.admission.StoredObjectType.SOURCE_REVISION, draft.revisionId.value))!!.status)
        }
    }

    @Test fun `source revision cannot move report time backward`() {
        SyntheticLongitudinalStoreHarness(path("source-revision-time")).use { harness ->
            val draft = sourceDraft("revision-time")
            assertAccepted(admitSource(harness, draft))
            val operation = LongitudinalWriteOperation.AppendSourceRevision(
                draft.stableSourceId, draft.revisionId, SourceRecordId.parse("source-revision-time-rev-2"),
                OriginalSourceContent.Inline("Synthetic wording."), ReportTime(Instant.parse("2038-01-01T00:00:00Z")),
            )
            val result = harness.store.admission.submit(harness.request(operation, AdmissionActor.USER, AdmissionOrigin.JOURNAL))
            assertEquals(AdmissionDisposition.REJECTED_TEMPORAL_DISHONESTY, result.disposition)
            assertEquals(1, harness.store.reader.sourceRevisionHistory(draft.stableSourceId).size)
        }
    }

    @Test fun `system origin cannot issue user correction`() {
        SyntheticLongitudinalStoreHarness(path("correction-authority")).use { harness ->
            val originalDraft = sourceDraft("correction-original")
            assertAccepted(admitSource(harness, originalDraft))
            val original = assertion("correction-original", originalDraft.revisionId)
            assertAccepted(admitAssertion(harness, original))
            val operation = correctionOperation(original.id, "authority")
            val result = harness.store.admission.submit(harness.request(operation, AdmissionActor.SYSTEM, AdmissionOrigin.USER_CORRECTION))
            assertEquals(AdmissionDisposition.REJECTED_AUTHORITY, result.disposition)
            assertEquals(2, harness.store.reader.currentStoreRevision())
        }
    }

    @Test fun `user correction preserves original and marks dependent hypothesis review required`() {
        SyntheticLongitudinalStoreHarness(path("correction-dependent")).use { harness ->
            val draft = sourceDraft("dependent-original")
            assertAccepted(admitSource(harness, draft))
            val original = assertion("dependent-original", draft.revisionId)
            assertAccepted(admitAssertion(harness, original))
            val hypothesisId = HypothesisId.parse("hypothesis-dependent")
            val hypothesis = HypothesisDraft(
                hypothesisId, AssertionSubject.User, predicate("hypothesis.dependent"), AssertionValue.Text("Synthetic tentative interpretation"),
                HypothesisStatus.TENTATIVE, "Synthetic hypothesis with explicit source support.",
            )
            val dependency = HypothesisDependency(EvidenceRelationId.parse("dependency-dependent"), hypothesisId, ClaimReference.Assertion(original.id), DependencyRole.SUPPORTS, "Synthetic source support")
            assertAccepted(harness.submitBundle(EvidenceBundle(hypothesisDrafts = listOf(hypothesis), hypothesisDependencies = listOf(dependency)), AdmissionActor.THOMAS))
            val correction = correctionOperation(original.id, "dependent")
            assertAccepted(harness.store.admission.submit(harness.request(correction, AdmissionActor.USER, AdmissionOrigin.USER_CORRECTION)))
            val snapshot = harness.store.reader.snapshot()
            assertNotNull(snapshot.assertions.firstOrNull { it.id == original.id })
            assertNotNull(snapshot.assertions.firstOrNull { it.id == correction.correctingAssertion.id })
            assertEquals(LongitudinalLifecycleStatus.SUPERSEDED, harness.store.reader.lifecycle(assertionRef(original.id))!!.status)
            assertEquals(LongitudinalLifecycleStatus.REVIEW_REQUIRED, harness.store.reader.lifecycle(hypothesisRef(hypothesisId))!!.status)
            assertFalse(harness.store.reader.isEligible(hypothesisRef(hypothesisId)))
        }
    }

    @Test fun `contradictory evidence remains simultaneously inspectable`() {
        SyntheticLongitudinalStoreHarness(path("contradiction")).use { harness ->
            val draft = sourceDraft("contradiction")
            assertAccepted(admitSource(harness, draft))
            val left = assertion("contradiction-left", draft.revisionId)
            val right = assertion("contradiction-right", draft.revisionId)
            assertAccepted(harness.submitBundle(EvidenceBundle(assertions = listOf(left, right))))
            val relation = ContradictionRelation(EvidenceRelationId.parse("relation-contradiction"), left.id, right.id, rationale = "Synthetic statements conflict.")
            assertAccepted(harness.store.admission.submit(harness.request(LongitudinalWriteOperation.RecordContradiction(relation), AdmissionActor.SYSTEM, AdmissionOrigin.QUALIFICATION_HARNESS)))
            assertEquals(2, harness.store.reader.snapshot().assertions.size)
            assertEquals(relation, harness.store.reader.snapshot().contradictions.single())
            assertTrue(harness.store.reader.isEligible(assertionRef(left.id)))
            assertTrue(harness.store.reader.isEligible(assertionRef(right.id)))
        }
    }

    @Test fun `supersession cycle is rejected without deleting history`() {
        SyntheticLongitudinalStoreHarness(path("supersession-cycle")).use { harness ->
            val draft = sourceDraft("supersession-cycle")
            assertAccepted(admitSource(harness, draft))
            val first = assertion("supersession-a", draft.revisionId)
            val second = assertion("supersession-b", draft.revisionId)
            assertAccepted(harness.submitBundle(EvidenceBundle(assertions = listOf(first, second))))
            val forward = SupersessionRelation(EvidenceRelationId.parse("supersession-forward"), ClaimReference.Assertion(second.id), ClaimReference.Assertion(first.id), SupersessionKind.REPLACES, "Synthetic replacement")
            assertAccepted(harness.store.admission.submit(harness.request(LongitudinalWriteOperation.RecordSupersession(forward), AdmissionActor.SYSTEM, AdmissionOrigin.QUALIFICATION_HARNESS)))
            val cycle = SupersessionRelation(EvidenceRelationId.parse("supersession-cycle-back"), ClaimReference.Assertion(first.id), ClaimReference.Assertion(second.id), SupersessionKind.REPLACES, "Would create a cycle")
            val result = harness.store.admission.submit(harness.request(LongitudinalWriteOperation.RecordSupersession(cycle), AdmissionActor.SYSTEM, AdmissionOrigin.QUALIFICATION_HARNESS))
            assertEquals(AdmissionDisposition.REJECTED_RELATION_CYCLE, result.disposition)
            assertEquals(1, harness.store.reader.snapshot().supersessions.size)
            assertEquals(2, harness.store.reader.snapshot().assertions.size)
        }
    }

    @Test fun `hypothesis without ultimate source support is rejected`() {
        SyntheticLongitudinalStoreHarness(path("hypothesis-no-source")).use { harness ->
            val hypothesisId = HypothesisId.parse("hypothesis-no-source")
            val draft = HypothesisDraft(hypothesisId, AssertionSubject.User, predicate("hypothesis.no-source"), AssertionValue.Text("Synthetic unsupported"), HypothesisStatus.TENTATIVE, "Synthetic unsupported hypothesis rationale.")
            val missing = HypothesisDependency(EvidenceRelationId.parse("dependency-no-source"), hypothesisId, ClaimReference.Assertion(AssertionId.parse("assertion-missing")), DependencyRole.SUPPORTS, "Missing support")
            val result = harness.submitBundle(EvidenceBundle(hypothesisDrafts = listOf(draft), hypothesisDependencies = listOf(missing)), AdmissionActor.THOMAS)
            assertEquals(AdmissionDisposition.REJECTED_DEPENDENCY, result.disposition)
            assertTrue(harness.store.reader.snapshot().hypotheses.isEmpty())
        }
    }

    @Test fun `Thomas origin cannot admit explicit user assertion`() {
        SyntheticLongitudinalStoreHarness(path("thomas-user-assertion")).use { harness ->
            val draft = sourceDraft("thomas-user-assertion")
            assertAccepted(admitSource(harness, draft))
            val operation = LongitudinalWriteOperation.AdmitEvidenceBundle(EvidenceBundle(assertions = listOf(assertion("thomas-user-assertion", draft.revisionId))))
            val result = harness.store.admission.submit(harness.request(operation, AdmissionActor.THOMAS, AdmissionOrigin.THOMAS_DERIVATION))
            assertEquals(AdmissionDisposition.REJECTED_AUTHORITY, result.disposition)
        }
    }

    @Test fun `new hypothesis cannot depend on private evidence`() {
        SyntheticLongitudinalStoreHarness(path("private-new-hypothesis")).use { harness ->
            val draft = sourceDraft("private-new-hypothesis")
            assertAccepted(admitSource(harness, draft))
            val evidence = assertion("private-new-hypothesis", draft.revisionId)
            assertAccepted(admitAssertion(harness, evidence))
            assertAccepted(harness.store.admission.submit(harness.request(LongitudinalWriteOperation.ChangePrivacy(draft.stableSourceId, SourcePrivacy.PRIVATE), AdmissionActor.SYSTEM, AdmissionOrigin.QUALIFICATION_HARNESS)))
            val hypothesisId = HypothesisId.parse("hypothesis-private")
            val hypothesis = HypothesisDraft(hypothesisId, AssertionSubject.User, predicate("hypothesis.private"), AssertionValue.Text("Synthetic private-derived"), HypothesisStatus.TENTATIVE, "Synthetic private dependency hypothesis.")
            val dependency = HypothesisDependency(EvidenceRelationId.parse("dependency-private"), hypothesisId, ClaimReference.Assertion(evidence.id), DependencyRole.SUPPORTS, "Private support")
            val result = harness.submitBundle(EvidenceBundle(hypothesisDrafts = listOf(hypothesis), hypothesisDependencies = listOf(dependency)), AdmissionActor.THOMAS)
            assertEquals(AdmissionDisposition.REJECTED_DEPENDENCY, result.disposition)
        }
    }

    @Test fun `making source private blocks existing derived material immediately`() {
        SyntheticLongitudinalStoreHarness(path("private-existing")).use { harness ->
            val draft = sourceDraft("private-existing")
            assertAccepted(admitSource(harness, draft))
            val evidence = assertion("private-existing", draft.revisionId)
            assertAccepted(admitAssertion(harness, evidence))
            val hypothesisId = HypothesisId.parse("hypothesis-private-existing")
            val hypothesis = HypothesisDraft(hypothesisId, AssertionSubject.User, predicate("hypothesis.private-existing"), AssertionValue.Text("Synthetic derived material"), HypothesisStatus.TENTATIVE, "Synthetic eligible hypothesis before privacy.")
            val dependency = HypothesisDependency(EvidenceRelationId.parse("dependency-private-existing"), hypothesisId, ClaimReference.Assertion(evidence.id), DependencyRole.SUPPORTS, "Eligible support")
            assertAccepted(harness.submitBundle(EvidenceBundle(hypothesisDrafts = listOf(hypothesis), hypothesisDependencies = listOf(dependency)), AdmissionActor.THOMAS))
            assertTrue(harness.store.reader.isEligible(hypothesisRef(hypothesisId)))
            assertAccepted(harness.store.admission.submit(harness.request(LongitudinalWriteOperation.ChangePrivacy(draft.stableSourceId, SourcePrivacy.PRIVATE), AdmissionActor.SYSTEM, AdmissionOrigin.QUALIFICATION_HARNESS)))
            assertEquals(LongitudinalLifecycleStatus.PRIVATE_INELIGIBLE, harness.store.reader.lifecycle(assertionRef(evidence.id))!!.status)
            assertEquals(LongitudinalLifecycleStatus.DEPENDENCY_BLOCKED, harness.store.reader.lifecycle(hypothesisRef(hypothesisId))!!.status)
        }
    }

    @Test fun `declined coverage cannot carry substantive source evidence`() {
        SyntheticLongitudinalStoreHarness(path("declined-evidence")).use { harness ->
            val topic = CoverageTopic(CoverageTopicId.parse("coverage-declined-with-source"), "Synthetic declined", InformationCoverageStatus.DECLINED, setOf(SourceRecordId.parse("source-implied")))
            val operation = LongitudinalWriteOperation.ChangeCoverage(topic)
            val result = harness.store.admission.submit(harness.request(operation, AdmissionActor.SYSTEM, AdmissionOrigin.QUALIFICATION_HARNESS))
            assertEquals(AdmissionDisposition.REJECTED_PRIVACY, result.disposition)
            assertTrue(harness.store.reader.coverage().isEmpty())
        }
    }

    @Test fun `private and declined coverage are valid non-factual states`() {
        SyntheticLongitudinalStoreHarness(path("coverage-valid")).use { harness ->
            val privateTopic = CoverageTopic(CoverageTopicId.parse("coverage-private"), "Synthetic private", InformationCoverageStatus.PRIVATE)
            val declinedTopic = CoverageTopic(CoverageTopicId.parse("coverage-declined"), "Synthetic declined", InformationCoverageStatus.DECLINED)
            assertAccepted(harness.store.admission.submit(harness.request(LongitudinalWriteOperation.ChangeCoverage(privateTopic), AdmissionActor.SYSTEM, AdmissionOrigin.QUALIFICATION_HARNESS)))
            assertAccepted(harness.store.admission.submit(harness.request(LongitudinalWriteOperation.ChangeCoverage(declinedTopic), AdmissionActor.SYSTEM, AdmissionOrigin.QUALIFICATION_HARNESS)))
            assertEquals(setOf(InformationCoverageStatus.PRIVATE, InformationCoverageStatus.DECLINED), harness.store.reader.coverage().map { it.status }.toSet())
            assertTrue(harness.store.reader.snapshot().assertions.isEmpty())
        }
    }

    @Test fun `hypothesis dependency cycle is rejected atomically`() {
        SyntheticLongitudinalStoreHarness(path("dependency-cycle")).use { harness ->
            val firstId = HypothesisId.parse("hypothesis-cycle-one")
            val secondId = HypothesisId.parse("hypothesis-cycle-two")
            val first = HypothesisDraft(firstId, AssertionSubject.User, predicate("hypothesis.cycle-one"), AssertionValue.Text("Synthetic one"), HypothesisStatus.TENTATIVE, "Synthetic first cyclic hypothesis.")
            val second = HypothesisDraft(secondId, AssertionSubject.User, predicate("hypothesis.cycle-two"), AssertionValue.Text("Synthetic two"), HypothesisStatus.TENTATIVE, "Synthetic second cyclic hypothesis.")
            val dependencies = listOf(
                HypothesisDependency(EvidenceRelationId.parse("dependency-cycle-one"), firstId, ClaimReference.Hypothesis(secondId), DependencyRole.SUPPORTS, "Cycle one"),
                HypothesisDependency(EvidenceRelationId.parse("dependency-cycle-two"), secondId, ClaimReference.Hypothesis(firstId), DependencyRole.SUPPORTS, "Cycle two"),
            )
            val result = harness.submitBundle(EvidenceBundle(hypothesisDrafts = listOf(first, second), hypothesisDependencies = dependencies), AdmissionActor.THOMAS)
            assertEquals(AdmissionDisposition.REJECTED_RELATION_CYCLE, result.disposition)
            assertTrue(harness.store.reader.snapshot().hypotheses.isEmpty())
        }
    }

    @Test fun `identity decision can be revised without merging or deleting entities`() {
        SyntheticLongitudinalStoreHarness(path("identity-revision")).use { harness ->
            val source = sourceDraft("identity")
            assertAccepted(admitSource(harness, source))
            val a = assertion("identity-a", source.revisionId)
            val b = assertion("identity-b", source.revisionId)
            val left = Person(LifeEntityId.parse("person-sam-one"), "Synthetic Sam", setOf(a.id))
            val right = Person(LifeEntityId.parse("person-sam-two"), "Synthetic friend Sam", setOf(b.id))
            val unresolved = EntityIdentityLink(IdentityLinkId.parse("identity-sam-unresolved"), left.id, right.id, EntityIdentityStatus.UNRESOLVED, rationale = "Synthetic unresolved reference")
            assertAccepted(harness.submitBundle(EvidenceBundle(assertions = listOf(a, b), entities = listOf(left, right), identityLinks = listOf(unresolved))))
            val same = EntityIdentityLink(IdentityLinkId.parse("identity-sam-same"), left.id, right.id, EntityIdentityStatus.ESTABLISHED_SAME_ENTITY, setOf(a.id, b.id), "Synthetic explicit resolution")
            assertAccepted(harness.store.admission.submit(harness.request(LongitudinalWriteOperation.ReviseIdentityLink(same, unresolved.id), AdmissionActor.USER, AdmissionOrigin.QUALIFICATION_HARNESS)))
            val different = EntityIdentityLink(IdentityLinkId.parse("identity-sam-different"), left.id, right.id, EntityIdentityStatus.ESTABLISHED_DIFFERENT_ENTITY, setOf(a.id, b.id), "Synthetic later checked correction")
            assertAccepted(harness.store.admission.submit(harness.request(LongitudinalWriteOperation.ReviseIdentityLink(different, same.id), AdmissionActor.USER, AdmissionOrigin.QUALIFICATION_HARNESS)))
            harness.reopen()
            assertEquals(EntityIdentityStatus.ESTABLISHED_DIFFERENT_ENTITY, harness.store.reader.identityLinks().single().status)
            assertEquals(3, harness.store.reader.identityDecisionHistory(left.id, right.id).size)
            assertEquals(2, harness.store.reader.snapshot().entities.size)
        }
    }

    @Test fun `retirement preserves claim and marks dependent hypothesis review required`() {
        SyntheticLongitudinalStoreHarness(path("retirement")).use { harness ->
            val source = sourceDraft("retirement")
            assertAccepted(admitSource(harness, source))
            val evidence = assertion("retirement", source.revisionId)
            assertAccepted(admitAssertion(harness, evidence))
            val hypothesisId = HypothesisId.parse("hypothesis-retirement")
            val hypothesis = HypothesisDraft(hypothesisId, AssertionSubject.User, predicate("hypothesis.retirement"), AssertionValue.Text("Synthetic derived"), HypothesisStatus.TENTATIVE, "Synthetic retirement dependency hypothesis.")
            val dependency = HypothesisDependency(EvidenceRelationId.parse("dependency-retirement"), hypothesisId, ClaimReference.Assertion(evidence.id), DependencyRole.SUPPORTS, "Synthetic support")
            assertAccepted(harness.submitBundle(EvidenceBundle(hypothesisDrafts = listOf(hypothesis), hypothesisDependencies = listOf(dependency)), AdmissionActor.THOMAS))
            assertAccepted(harness.store.admission.submit(harness.request(LongitudinalWriteOperation.RetireClaim(ClaimReference.Assertion(evidence.id)), AdmissionActor.SYSTEM, AdmissionOrigin.QUALIFICATION_HARNESS)))
            assertNotNull(harness.store.reader.assertion(evidence.id))
            assertEquals(LongitudinalLifecycleStatus.RETIRED, harness.store.reader.lifecycle(assertionRef(evidence.id))!!.status)
            assertEquals(LongitudinalLifecycleStatus.REVIEW_REQUIRED, harness.store.reader.lifecycle(hypothesisRef(hypothesisId))!!.status)
        }
    }

    @Test fun `newer source does not infer supersession by recency`() {
        SyntheticLongitudinalStoreHarness(path("no-recency-supersession")).use { harness ->
            assertAccepted(admitSource(harness, sourceDraft("recency-one")))
            assertAccepted(admitSource(harness, sourceDraft("recency-two")))
            assertTrue(harness.store.reader.snapshot().supersessions.isEmpty())
            assertEquals(2, harness.store.reader.snapshot().sources.size)
        }
    }

    private fun correctionOperation(corrected: AssertionId, suffix: String): LongitudinalWriteOperation.RecordUserCorrection {
        val source = sourceDraft("correction-$suffix", "Synthetic explicit correction.", mode = AcquisitionMode.USER_CORRECTION)
        val correcting = EvidenceAssertion(
            AssertionId.parse("assertion-correction-$suffix"), source.revisionId, AssertionSubject.User,
            predicate("correction.$suffix"), AssertionValue.Text("Synthetic corrected value"), UserEvidenceKind.EXPLICIT_USER_ASSERTION,
            AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
        )
        val relation = CorrectionRelation(EvidenceRelationId.parse("correction-relation-$suffix"), correcting.id, corrected, CorrectionEffect.CORRECTS_AND_SUPERSEDES, "Synthetic explicit correction")
        val supersession = SupersessionRelation(EvidenceRelationId.parse("correction-supersession-$suffix"), ClaimReference.Assertion(correcting.id), ClaimReference.Assertion(corrected), SupersessionKind.CORRECTS, "Synthetic correction supersession")
        return LongitudinalWriteOperation.RecordUserCorrection(source, correcting, relation, supersession)
    }

    private fun predicate(id: String) = AssertionPredicate(PersonalConceptId.parse(id), PredicateSemantics.EVALUATION_OR_MEANING)
}
