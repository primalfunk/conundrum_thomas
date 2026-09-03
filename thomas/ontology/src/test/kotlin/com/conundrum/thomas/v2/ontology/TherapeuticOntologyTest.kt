package com.conundrum.thomas.v2.ontology

import com.conundrum.thomas.v2.domain.mode.DefaultResponseDisposition
import com.conundrum.thomas.v2.domain.mode.DirectProfileMutationAuthority
import com.conundrum.thomas.v2.domain.mode.LanguageModelDecisionAuthority
import com.conundrum.thomas.v2.domain.mode.ModePrimaryFunction
import com.conundrum.thomas.v2.domain.mode.ThomasMode
import com.conundrum.thomas.v2.domain.mode.ThomasModeAuthorityContracts
import com.conundrum.thomas.v2.provenance.AuthorityDomain
import com.conundrum.thomas.v2.provenance.CommercialUseStatus
import com.conundrum.thomas.v2.provenance.GovernedSourceReference
import com.conundrum.thomas.v2.provenance.SourceDocumentId
import com.conundrum.thomas.v2.provenance.SourceLocatorId
import com.conundrum.thomas.v2.provenance.SourceSectionId
import com.conundrum.thomas.v2.provenance.SourceVersionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TherapeuticOntologyTest {
    private val catalog = TherapeuticOntologyCatalog.snapshot

    @Test
    fun `stable IDs are unique namespaced and deterministically ordered`() {
        val ids = catalog.concepts.map { it.id }
        assertEquals(ids.distinct(), ids)
        assertEquals(ids.sorted(), ids)
        assertTrue(ids.all { it.value.contains('.') })
        assertEquals(ConceptFamily.entries.toSet(), catalog.concepts.map { it.family }.toSet())
        assertEquals("1.0.0", catalog.release.version.toString())
    }

    @Test
    fun `every concept and source binding is denied runtime authority`() {
        assertTrue(catalog.concepts.all { it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED })
        assertTrue(catalog.sourceBindings.all { it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED })
        assertTrue(catalog.sourceBindings.all { it.bindingAuthority == SourceBindingAuthority.PROVENANCE_ONLY })
        assertTrue(TherapeuticOntologyCatalog.interventionFamilies.all { it.definitionStatus == DefinitionStatus.CANDIDATE })
        assertTrue(TherapeuticOntologyCatalog.safetyContexts.all { it.definitionStatus == DefinitionStatus.CANDIDATE })
    }

    @Test
    fun `three mode contracts remain distinct and model authority remains zero`() {
        val contracts = ThomasModeAuthorityContracts.all
        assertEquals(ThomasMode.entries.toSet(), contracts.map { it.mode }.toSet())
        assertEquals(
            setOf(ModePrimaryFunction.INTERVENTION, ModePrimaryFunction.INVESTIGATION, ModePrimaryFunction.CAPTURE),
            contracts.map { it.primaryFunction }.toSet(),
        )
        assertEquals(contracts.size, contracts.map { it.governingQuestion }.distinct().size)
        assertTrue(contracts.all { it.languageModelDecisionAuthority == LanguageModelDecisionAuthority.NONE })
        assertTrue(contracts.all { it.directProfileMutationAuthority == DirectProfileMutationAuthority.NONE })
    }

    @Test
    fun `Journal defaults to first class no response`() {
        assertEquals(DefaultResponseDisposition.NO_RESPONSE, ThomasModeAuthorityContracts.journal.defaultResponse)
        assertTrue(TherapeuticOntologyCatalog.dialogueActs.any { it.id.value == "dialogue.no-response" })
    }

    @Test
    fun `unknown tentative and conflicting evidence stay explicit`() {
        val unknown = EpistemicRecord<String>(
            id = EvidenceRecordId.parse("evidence-unknown"),
            conceptId = OntologyConceptId.parse("observation.missing-information"),
            value = null,
            evidenceKind = EvidenceKind.UNKNOWN,
            resolution = EpistemicResolution.UNKNOWN,
            strength = EpistemicStrength.UNSPECIFIED,
        )
        assertEquals(DurableProfileAdmission.GOVERNED_ADMISSION_REQUIRED, unknown.durableProfileAdmission)

        val hypothesis = EpistemicRecord(
            id = EvidenceRecordId.parse("evidence-hypothesis"),
            conceptId = OntologyConceptId.parse("observation.interpersonal-context"),
            value = "synthetic-test-hypothesis",
            evidenceKind = EvidenceKind.THOMAS_HYPOTHESIS,
            resolution = EpistemicResolution.TENTATIVE,
            strength = EpistemicStrength.TENTATIVE,
        )
        assertEquals(EvidenceKind.THOMAS_HYPOTHESIS, hypothesis.evidenceKind)
        assertEquals(DurableProfileAdmission.GOVERNED_ADMISSION_REQUIRED, hypothesis.durableProfileAdmission)

        val contradiction = EpistemicRecord<String>(
            id = EvidenceRecordId.parse("evidence-contradiction"),
            conceptId = OntologyConceptId.parse("observation.recent-change"),
            value = null,
            evidenceKind = EvidenceKind.CONTRADICTION,
            resolution = EpistemicResolution.CONFLICTING,
            strength = EpistemicStrength.UNSPECIFIED,
            conflictsWith = setOf(EvidenceRecordId.parse("evidence-prior")),
        )
        assertFalse(contradiction.conflictsWith.isEmpty())
    }

    @Test
    fun `Thomas hypothesis cannot masquerade as user established fact`() {
        assertThrows(IllegalArgumentException::class.java) {
            EpistemicRecord(
                id = EvidenceRecordId.parse("evidence-invalid"),
                conceptId = OntologyConceptId.parse("observation.stated-concern"),
                value = "synthetic",
                evidenceKind = EvidenceKind.THOMAS_HYPOTHESIS,
                resolution = EpistemicResolution.RESOLVED_AS_REPORTED,
                strength = EpistemicStrength.SUPPORTED,
            )
        }
    }

    @Test
    fun `constraint grammar covers required relationship kinds without evaluating them`() {
        val expected = setOf(
            ConstraintRelationshipKind.PREREQUISITE,
            ConstraintRelationshipKind.POPULATION_CONSTRAINT,
            ConstraintRelationshipKind.SETTING_CONSTRAINT,
            ConstraintRelationshipKind.DELIVERER_ASSUMPTION,
            ConstraintRelationshipKind.CONTRAINDICATION_CONCEPT,
            ConstraintRelationshipKind.INFORMATION_REQUIREMENT,
            ConstraintRelationshipKind.SAFETY_CONSTRAINT,
            ConstraintRelationshipKind.AUTONOMY_RESTRICTION,
            ConstraintRelationshipKind.CLINICAL_REVIEW_REQUIREMENT,
            ConstraintRelationshipKind.RIGHTS_REQUIREMENT,
            ConstraintRelationshipKind.IMPLEMENTATION_SCOPE_REQUIREMENT,
            ConstraintRelationshipKind.LEGAL_REQUIREMENT,
            ConstraintRelationshipKind.SOURCE_VERSION_DEPENDENCY,
        )
        assertEquals(expected, ConstraintRelationshipKind.entries.toSet())
        assertTrue(catalog.constraintLinks.isEmpty())
    }

    @Test
    fun `source linked candidates have pending provenance only bindings`() {
        val sourceLinked = catalog.concepts.filter {
            it.sourceSupportStatus == SourceSupportStatus.SOURCE_LINKED_CANDIDATE
        }
        assertTrue(sourceLinked.isNotEmpty())
        assertTrue(sourceLinked.all { concept -> catalog.sourceBindings.any { it.conceptId == concept.id } })
        assertTrue(catalog.sourceBindings.all { it.clinicalReviewStatus == GovernanceReviewStatus.PENDING })
        assertTrue(catalog.sourceBindings.all { it.rightsReviewStatus == GovernanceReviewStatus.PENDING })
    }

    @Test
    fun `invalid clinical engineering cross domain binding is rejected`() {
        val concept = ObservationConcept(
            id = OntologyConceptId.parse("observation.synthetic-test"),
            label = "Synthetic test",
            definition = "Synthetic validation fixture.",
        )
        val binding = OntologySourceBinding(
            bindingId = "binding-synthetic-test",
            conceptId = concept.id,
            source = GovernedSourceReference(
                SourceDocumentId.parse("nist-ai-rmf"),
                SourceVersionId.parse("nist-ai-rmf-1-0-2023"),
                SourceSectionId.parse("section-nist-rmf-core"),
                SourceLocatorId.parse("artifact-nist-rmf"),
            ),
            sourceAuthorityDomain = AuthorityDomain.ENGINEERING_GOVERNANCE,
            relationship = ConceptSourceRelationship.QUALIFICATION_REFERENCE,
            sourceIdentityReviewState = com.conundrum.thomas.v2.provenance.ReviewState.VERIFIED,
            commercialUseStatus = CommercialUseStatus.PUBLIC_DOMAIN,
            clinicalReviewRequirementId = null,
            clinicalReviewStatus = GovernanceReviewStatus.NOT_REQUIRED,
            rightsReviewRequirementId = null,
            rightsReviewStatus = GovernanceReviewStatus.NOT_REQUIRED,
            conflictState = BindingConflictState.NONE_RECORDED,
            scopeLimitations = listOf("Synthetic negative fixture."),
        )

        assertThrows(IllegalArgumentException::class.java) {
            OntologyCatalogSnapshot(
                release = OntologyRelease("ct-v2-02-test", SemanticVersion(1, 0, 0), "CT-V2-02"),
                concepts = listOf(concept),
                sourceBindings = listOf(binding),
            )
        }
    }

    @Test
    fun `unknown future concept IDs are preserved without reinterpretation`() {
        val futureId = OntologyConceptId.parse("future-domain.future-concept")
        val result = catalog.lookup(futureId)
        assertTrue(result is ConceptLookup.Unrecognized)
        assertEquals(futureId, (result as ConceptLookup.Unrecognized).preservedId)
    }
}
