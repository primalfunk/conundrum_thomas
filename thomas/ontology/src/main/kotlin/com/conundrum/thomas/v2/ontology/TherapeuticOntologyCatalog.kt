package com.conundrum.thomas.v2.ontology

import com.conundrum.thomas.v2.provenance.AuthorityDomain
import com.conundrum.thomas.v2.provenance.CommercialUseStatus
import com.conundrum.thomas.v2.provenance.GovernedSourceReference
import com.conundrum.thomas.v2.provenance.ReviewRequirementId
import com.conundrum.thomas.v2.provenance.ReviewState
import com.conundrum.thomas.v2.provenance.SourceConflictId
import com.conundrum.thomas.v2.provenance.SourceDocumentId
import com.conundrum.thomas.v2.provenance.SourceLocatorId
import com.conundrum.thomas.v2.provenance.SourceSectionId
import com.conundrum.thomas.v2.provenance.SourceVersionId

private fun id(value: String) = OntologyConceptId.parse(value)

private fun observation(name: String, label: String, definition: String) =
    ObservationConcept(id("observation.$name"), label, definition)

private fun goal(name: String, label: String, definition: String) =
    GoalConcept(id("goal.$name"), label, definition)

private fun dialogue(name: String, label: String, definition: String) =
    DialogueActConcept(id("dialogue.$name"), label, definition)

private val standardInterventionGates = setOf(
    GovernanceGate.CLINICAL_REVIEW,
    GovernanceGate.RIGHTS_REVIEW,
    GovernanceGate.IMPLEMENTATION_SCOPE_REVIEW,
    GovernanceGate.FUTURE_POLICY_AUTHORIZATION,
)

private fun intervention(
    name: String,
    label: String,
    definition: String,
    sourceSupport: SourceSupportStatus,
    extraGates: Set<GovernanceGate> = emptySet(),
) = InterventionFamilyConcept(
    id = id("intervention.$name"),
    label = label,
    definition = definition,
    sourceSupportStatus = sourceSupport,
    governanceGates = standardInterventionGates + extraGates,
)

private fun constraint(name: String, label: String, definition: String) =
    ConstraintConcept(id("constraint.$name"), label, definition)

private val standardSafetyGates = setOf(
    GovernanceGate.CLINICAL_REVIEW,
    GovernanceGate.SOFTWARE_AUTONOMY_REVIEW,
    GovernanceGate.FUTURE_POLICY_AUTHORIZATION,
)

private fun safety(
    name: String,
    label: String,
    definition: String,
    sourceSupport: SourceSupportStatus,
) = SafetyContextConcept(
    id = id("safety.$name"),
    label = label,
    definition = definition,
    sourceSupportStatus = sourceSupport,
    governanceGates = standardSafetyGates,
)

private fun procedural(name: String, label: String, definition: String) =
    ProceduralConcept(id("procedure.$name"), label, definition)

data class OntologyCatalogSnapshot(
    val release: OntologyRelease,
    val concepts: List<OntologyConcept>,
    val sourceBindings: List<OntologySourceBinding>,
    val constraintLinks: List<ConceptConstraintLink> = emptyList(),
) {
    init { OntologyCatalogValidator.validate(this) }

    fun lookup(conceptId: OntologyConceptId): ConceptLookup =
        concepts.firstOrNull { it.id == conceptId }
            ?.let(ConceptLookup::Known)
            ?: ConceptLookup.Unrecognized(conceptId)
}

object OntologyCatalogValidator {
    fun validate(catalog: OntologyCatalogSnapshot) {
        require(catalog.concepts.isNotEmpty())
        require(catalog.concepts.map { it.id }.distinct().size == catalog.concepts.size) {
            "Ontology concept IDs must be unique."
        }
        require(catalog.concepts == catalog.concepts.sortedBy { it.id }) {
            "Ontology concepts require deterministic ID ordering."
        }
        require(catalog.concepts.all { it.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED }) {
            "CT-V2-02 cannot contain runtime-authorized concepts."
        }

        val conceptsById = catalog.concepts.associateBy { it.id }
        require(catalog.sourceBindings.map { it.bindingId }.distinct().size == catalog.sourceBindings.size)
        require(catalog.sourceBindings == catalog.sourceBindings.sortedBy { it.bindingId })

        catalog.sourceBindings.forEach { binding ->
            val concept = requireNotNull(conceptsById[binding.conceptId]) {
                "Source binding ${binding.bindingId} references an unknown concept."
            }
            require(binding.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED)
            require(binding.bindingAuthority == SourceBindingAuthority.PROVENANCE_ONLY)
            require(binding.clinicalReviewStatus != GovernanceReviewStatus.COMPLETE)
            require(binding.rightsReviewStatus != GovernanceReviewStatus.COMPLETE)

            val engineeringSource = binding.sourceAuthorityDomain == AuthorityDomain.ENGINEERING_GOVERNANCE
            val engineeringConcept = concept.domain == OntologyDomain.ENGINEERING_GOVERNANCE
            require(engineeringSource == engineeringConcept) {
                "Clinical and engineering authority domains cannot be crossed."
            }
        }

        catalog.concepts
            .filter { it.sourceSupportStatus == SourceSupportStatus.SOURCE_LINKED_CANDIDATE }
            .forEach { concept ->
                require(catalog.sourceBindings.any { it.conceptId == concept.id }) {
                    "Source-linked concept ${concept.id.value} requires a governed binding."
                }
            }

        catalog.concepts.filterIsInstance<InterventionFamilyConcept>().forEach { intervention ->
            require(intervention.definitionStatus == DefinitionStatus.CANDIDATE)
            require(intervention.governanceGates.contains(GovernanceGate.FUTURE_POLICY_AUTHORIZATION))
        }
        catalog.concepts.filterIsInstance<SafetyContextConcept>().forEach { safety ->
            require(safety.definitionStatus == DefinitionStatus.CANDIDATE)
        }

        require(catalog.constraintLinks.map { it.linkId }.distinct().size == catalog.constraintLinks.size)
        catalog.constraintLinks.forEach { link ->
            require(link.runtimeAuthorization == RuntimeAuthorizationStatus.NOT_AUTHORIZED)
            require(conceptsById.containsKey(link.subjectConceptId))
            if (link.target is ConstraintTarget.Concept) {
                require(conceptsById.containsKey(link.target.conceptId))
            }
        }
    }
}

object TherapeuticOntologyCatalog {
    val release = OntologyRelease("ct-v2-02-ontology", SemanticVersion(1, 0, 0), "CT-V2-02")

    val observations: List<ObservationConcept> = listOf(
        observation("ambivalence", "Ambivalence", "Coexisting or competing expressed orientations, without inferring motive."),
        observation("arousal-activation", "Arousal or activation", "Reported or interaction-visible activation represented without a numerical clinical score."),
        observation("cognitive-load", "Cognitive load", "Possible current burden on attention, organization, or information processing."),
        observation("conversational-intent", "Conversational intent", "What the user appears to be seeking from the current interaction."),
        observation("emotional-content", "Emotional content", "Emotion explicitly reported or tentatively observed in the interaction."),
        observation("immediate-practical-pressure", "Immediate practical pressure", "Time-sensitive or concrete demands described in the present situation."),
        observation("interpersonal-context", "Interpersonal context", "People, roles, or relationships relevant to the stated concern."),
        observation("missing-information", "Missing information", "A material gap that remains explicitly unknown."),
        observation("perceived-agency", "Perceived agency", "The user's stated or tentatively observed sense of influence or choice."),
        observation("problem-clarity", "Problem clarity", "How clearly the concern, boundary, or desired change is currently expressed."),
        observation("readiness-to-act", "Readiness to act", "Expressed willingness to consider action, distinct from readiness to explore."),
        observation("readiness-to-explore", "Readiness to explore", "Expressed willingness to examine experience without implying readiness for action."),
        observation("recent-change", "Recent change", "A reported shift in circumstances, experience, or functioning."),
        observation("recurrence-chronicity", "Recurrence or chronicity", "Reported repetition or duration, without diagnostic interpretation."),
        observation("requested-response-level", "Requested response level", "The user's explicit preference for silence, acknowledgment, reflection, questions, or other engagement."),
        observation("safety-relevant-observation", "Safety-relevant observation", "Evidence that may require later governed safety consideration; it is not a risk classification."),
        observation("stated-concern", "Stated concern", "The concern directly expressed by the user."),
        observation("subjective-distress", "Subjective distress", "Distress as reported or tentatively observed, without a predictive score."),
        observation("uncertainty", "Uncertainty", "Uncertainty expressed by the user or attached to Thomas's own interpretation."),
    ).sortedBy { it.id }

    val goals: List<GoalConcept> = listOf(
        goal("assess-safety", "Assess safety", "A future aim of obtaining sufficient governed safety information; no assessment method is selected here."),
        goal("check-understanding", "Check understanding", "Determine whether a communicated meaning was understood as intended."),
        goal("clarify", "Clarify", "Reduce a specific ambiguity without assuming an answer."),
        goal("close-or-pause", "Close or pause", "Bring an interaction to a bounded pause or conclusion."),
        goal("consolidate-learning", "Consolidate learning", "Gather what the user identifies as useful or learned."),
        goal("encourage-outside-support", "Encourage appropriate outside support", "A future aim concerning support beyond Thomas; no referral criterion is defined here."),
        goal("establish-safety", "Establish safety", "A restricted future safety aim; no safety procedure or threshold is defined."),
        goal("establish-shared-understanding", "Establish shared understanding", "Develop a mutually checkable description of the current concern."),
        goal("examine-alternatives", "Examine alternatives", "Consider more than one possible account or course without choosing for the user."),
        goal("explore-behavior", "Explore behavior", "Understand relevant actions or patterns as reported."),
        goal("explore-context", "Explore context", "Understand situational and relational context."),
        goal("explore-emotion", "Explore emotion", "Understand emotional experience without presuming its meaning."),
        goal("explore-thought", "Explore thought", "Understand reported thoughts, appraisals, or interpretations."),
        goal("identify-pattern", "Identify pattern", "Tentatively examine recurrence while preserving uncertainty and correction."),
        goal("increase-agency", "Increase agency", "Support awareness of choice and ownership without directing a decision."),
        goal("increase-orientation", "Increase orientation", "Support a clearer grasp of the immediate situation or next bounded task."),
        goal("reduce-conversational-burden", "Reduce conversational burden", "Make the interaction easier to continue, including through brevity or silence."),
        goal("strengthen-motivation", "Strengthen motivation", "A candidate future aim concerning motivation; no method is selected here."),
        goal("support-behavioral-planning", "Support behavioral planning", "A future aim of helping form a bounded plan."),
        goal("support-decision-making", "Support decision-making", "Help the user examine a decision while preserving autonomy."),
        goal("support-expression", "Support expression", "Make room for the user to express experience in their own terms."),
        goal("support-problem-solving", "Support problem solving", "A future aim of working with a defined practical problem."),
        goal("support-skill-practice", "Support skill practice", "A future aim of practicing an admitted skill; no skill is authorized here."),
        goal("understand", "Understand", "Improve Thomas's tentative account of what the user is communicating."),
    ).sortedBy { it.id }

    val dialogueActs: List<DialogueActConcept> = listOf(
        dialogue("acknowledge", "Acknowledge", "Briefly mark receipt or recognition without adding an interpretation."),
        dialogue("affirm-agency", "Affirm agency", "Recognize the user's ownership, effort, choice, or stated capacity."),
        dialogue("ask-focused-question", "Ask a focused question", "Request one bounded item of information."),
        dialogue("ask-open-question", "Ask an open question", "Invite the user to choose the substance or direction of an answer."),
        dialogue("boundary-statement", "Boundary statement", "State a relevant limit on Thomas's knowledge, scope, or authority."),
        dialogue("clarify", "Clarify", "Seek or express a more precise meaning."),
        dialogue("close", "Close", "End or pause the interaction in a bounded way."),
        dialogue("develop-plan", "Develop plan", "Participate in forming a plan already authorized by future procedure."),
        dialogue("encourage-external-support", "Encourage external support", "Communicate a future governed outside-support action."),
        dialogue("guide-exercise", "Guide exercise", "Render steps of a separately admitted and selected exercise."),
        dialogue("handoff-escalation", "Handoff or escalation", "Communicate a separately governed handoff or escalation disposition."),
        dialogue("no-response", "No response", "Intentionally produce no generated conversational response."),
        dialogue("offer-choices", "Offer choices", "Present a bounded set of options without selecting for the user."),
        dialogue("provide-structured-information", "Provide structured information", "Present authorized information in an organized form."),
        dialogue("reflect", "Reflect", "Restate content, emotion, or possible meaning with governed certainty."),
        dialogue("review-plan", "Review plan", "Examine a previously formed plan without presuming success or failure."),
        dialogue("safety-oriented-inquiry", "Safety-oriented inquiry", "Render a question selected by a future safety authority."),
        dialogue("suggest-exercise", "Suggest exercise", "Offer a separately admitted exercise after future policy selection."),
        dialogue("summarize", "Summarize", "Bring together selected material without adding unsupported facts."),
        dialogue("verify-understanding", "Verify understanding", "Invite correction of Thomas's current understanding."),
    ).sortedBy { it.id }

    val interventionFamilies: List<InterventionFamilyConcept> = listOf(
        intervention("behavioral-activation", "Behavioral activation", "Candidate family concerning activity and behavior patterns; no procedure is imported.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
        intervention("cognitive-approaches", "Cognitive approaches", "Candidate family concerning relationships among thoughts, interpretations, emotions, and behavior.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
        intervention("coping-planning", "Coping planning", "Candidate family for planning bounded coping responses; source analysis remains unopened.", SourceSupportStatus.SOURCE_REVIEW_NEEDED),
        intervention("emotional-regulation", "Emotional regulation", "Candidate family concerning ways of relating to or regulating emotional experience.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
        intervention("external-support-referral", "External support or referral", "Restricted candidate family involving support outside Thomas; no routing conditions are defined.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE, setOf(GovernanceGate.SOFTWARE_AUTONOMY_REVIEW)),
        intervention("grounding-orientation", "Grounding or orientation", "Candidate family for present-focused orientation; a governed source basis remains to be established.", SourceSupportStatus.SOURCE_REVIEW_NEEDED),
        intervention("interpersonal-exploration", "Interpersonal exploration", "Candidate family concerning relational context and patterns; source analysis remains unopened.", SourceSupportStatus.SOURCE_REVIEW_NEEDED),
        intervention("motivational-approaches", "Motivational approaches", "Candidate family concerning ambivalence, motivation, and autonomy.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
        intervention("problem-solving", "Problem solving", "Candidate structured family concerning a manageable practical problem; no stages are encoded.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
        intervention("safety-planning", "Safety planning", "Restricted candidate family; no plan, pathway, or crisis logic is reproduced.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE, setOf(GovernanceGate.SOFTWARE_AUTONOMY_REVIEW, GovernanceGate.LEGAL_REVIEW)),
        intervention("structured-self-help", "Structured self-help", "Candidate family for bounded guided or self-guided material with delivery assumptions preserved.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
        intervention("supportive-listening", "Supportive listening", "Candidate family centered on foundational supportive communication.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
    ).sortedBy { it.id }

    val constraints: List<ConstraintConcept> = listOf(
        constraint("autonomy-restriction", "Autonomy restriction", "A limit on autonomous software use or decision authority."),
        constraint("clinical-review-requirement", "Clinical-review requirement", "A required specialist review gate."),
        constraint("contraindication", "Contraindication concept", "A future concept describing circumstances that may exclude use; no contraindication is adjudicated here."),
        constraint("deliverer-assumption", "Deliverer assumption", "A source assumption about the person or system delivering an activity."),
        constraint("implementation-scope-requirement", "Implementation-scope requirement", "A required review of delivery context and implementation assumptions."),
        constraint("information-requirement", "Information requirement", "Information that a future procedure may require before adjudication."),
        constraint("legal-requirement", "Legal requirement", "A legal or licensing gate outside Builder authority."),
        constraint("population-constraint", "Population constraint", "A source-defined population boundary."),
        constraint("prerequisite", "Prerequisite", "A future requirement that must be represented before eligibility can be adjudicated."),
        constraint("rights-requirement", "Rights requirement", "A rights or commercial-use gate."),
        constraint("safety-constraint", "Safety constraint", "A future safety-authority limitation, not a risk rule."),
        constraint("setting-constraint", "Setting constraint", "A source-defined delivery setting boundary."),
        constraint("source-version-dependency", "Source-version dependency", "A dependency on an exact governed publication version."),
    ).sortedBy { it.id }

    val safetyContexts: List<SafetyContextConcept> = listOf(
        safety("direct-clarification-requirement", "Direct-clarification requirement", "Candidate state indicating that safety information may be insufficient and clarification may require future safety authority.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
        safety("emergency-context", "Emergency context", "Unopened candidate context whose definition and response authority require specialist adjudication.", SourceSupportStatus.SOURCE_REVIEW_NEEDED),
        safety("insufficient-safety-information", "Insufficient safety information", "Candidate representation of an explicit information gap, not a risk level.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
        safety("ordinary-distress-context", "Ordinary distress context", "Unopened candidate context; it must not be inferred merely from absence of detected keywords.", SourceSupportStatus.SOURCE_REVIEW_NEEDED),
        safety("outside-support-consideration", "Outside-support consideration", "Candidate state preserving possible need for human support without selecting a disposition.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
        safety("safety-relevant-disclosure", "Safety-relevant disclosure", "Candidate evidence context that carries no prediction, stratification, or automatic action.", SourceSupportStatus.SOURCE_LINKED_CANDIDATE),
        safety("urgent-external-intervention-consideration", "Urgent external-intervention consideration", "Unopened candidate context; no threshold or escalation behavior is defined.", SourceSupportStatus.SOURCE_REVIEW_NEEDED),
    ).sortedBy { it.id }

    val proceduralVocabulary: List<ProceduralConcept> = listOf(
        procedural("candidate-action", "Candidate action", "An action available for future eligibility adjudication, not a selected response."),
        procedural("eligibility", "Eligibility", "A future determination that prerequisites and constraints permit consideration."),
        procedural("exclusion", "Exclusion", "A represented reason a candidate cannot proceed."),
        procedural("goal", "Goal", "The intended purpose of a future turn or procedure."),
        procedural("handoff", "Handoff", "A transfer concept whose conditions and recipient remain undefined."),
        procedural("observation", "Observation", "A structured evidence-bearing description of input or outcome."),
        procedural("outcome-observation", "Outcome observation", "Evidence about what followed a future action."),
        procedural("prerequisite", "Prerequisite", "A represented requirement preceding future eligibility."),
        procedural("priority", "Priority", "A future ordering consideration without a ranking implementation."),
        procedural("reassessment", "Reassessment", "A future return from outcome evidence to structured state."),
        procedural("selected-action", "Selected action", "The result type of a future authorized selection process; CT-V2-02 cannot produce one."),
        procedural("state", "State", "A structured and revisable representation assembled from governed evidence."),
        procedural("termination", "Termination", "A future end condition for a procedure or interaction."),
        procedural("unresolved-requirement", "Unresolved requirement", "A prerequisite, review, conflict, or information need that remains open."),
    ).sortedBy { it.id }

    private fun source(
        document: String,
        version: String,
        section: String,
        locator: String,
    ) = GovernedSourceReference(
        SourceDocumentId.parse(document),
        SourceVersionId.parse(version),
        SourceSectionId.parse(section),
        SourceLocatorId.parse(locator),
    )

    private fun binding(
        bindingId: String,
        conceptId: String,
        source: GovernedSourceReference,
        relationship: ConceptSourceRelationship,
        authorityDomain: AuthorityDomain,
        commercialUseStatus: CommercialUseStatus,
        clinicalReviewId: String,
        rightsReviewId: String,
        limitations: List<String>,
        conflictState: BindingConflictState = BindingConflictState.NONE_RECORDED,
        conflicts: Set<String> = emptySet(),
    ) = OntologySourceBinding(
        bindingId = bindingId,
        conceptId = id(conceptId),
        source = source,
        sourceAuthorityDomain = authorityDomain,
        relationship = relationship,
        sourceIdentityReviewState = ReviewState.VERIFIED,
        commercialUseStatus = commercialUseStatus,
        clinicalReviewRequirementId = ReviewRequirementId.parse(clinicalReviewId),
        clinicalReviewStatus = GovernanceReviewStatus.PENDING,
        rightsReviewRequirementId = ReviewRequirementId.parse(rightsReviewId),
        rightsReviewStatus = GovernanceReviewStatus.PENDING,
        conflictState = conflictState,
        conflictIds = conflicts.mapTo(sortedSetOf(compareBy { it.value })) { SourceConflictId.parse(it) },
        scopeLimitations = limitations,
    )

    val sourceBindings: List<OntologySourceBinding> = listOf(
        binding(
            "binding-behavioral-activation-cci",
            "intervention.behavioral-activation",
            source("cci-self-help-collection", "cci-self-help-web-2024", "section-cci-depression", "loc-cci-depression"),
            ConceptSourceRelationship.CANDIDATE_SUBJECT_ONLY,
            AuthorityDomain.CLINICAL_RESOURCE_PROVIDER,
            CommercialUseStatus.COPYRIGHTED_REFERENCE_ONLY,
            "review-clinical-cci",
            "review-rights-cci-self-help-web-2024",
            listOf("Structured resource candidate only; evidence basis, population, currency, and rights remain under review."),
            BindingConflictState.DIFFERENT_SCOPE_RECORDED,
            setOf("scope-cci-vs-who-self-help"),
        ),
        binding(
            "binding-cognitive-approaches-cci",
            "intervention.cognitive-approaches",
            source("cci-self-help-collection", "cci-self-help-web-2024", "section-cci-anxiety", "loc-cci-anxiety"),
            ConceptSourceRelationship.CANDIDATE_SUBJECT_ONLY,
            AuthorityDomain.CLINICAL_RESOURCE_PROVIDER,
            CommercialUseStatus.COPYRIGHTED_REFERENCE_ONLY,
            "review-clinical-cci",
            "review-rights-cci-self-help-web-2024",
            listOf("No worksheet, method sequence, or commercial adaptation is admitted."),
            BindingConflictState.DIFFERENT_SCOPE_RECORDED,
            setOf("scope-cci-vs-who-self-help"),
        ),
        binding(
            "binding-direct-clarification-nimh",
            "safety.direct-clarification-requirement",
            source("nimh-asq-toolkit", "nimh-asq-toolkit-web-2026", "section-nimh-toolkit-positive", "loc-nimh-toolkit-page"),
            ConceptSourceRelationship.BOUNDS_SCOPE,
            AuthorityDomain.CLINICAL_GOVERNMENT,
            CommercialUseStatus.PUBLIC_DOMAIN,
            "review-clinical-nimh-toolkit",
            "review-rights-nimh-asq-toolkit-web-2026",
            listOf("Medical-setting screening and trained-clinician assessment cannot be generalized into Thomas behavior."),
            BindingConflictState.DIFFERENT_SCOPE_RECORDED,
            setOf("scope-asq-vs-ng225"),
        ),
        binding(
            "binding-emotional-regulation-cci",
            "intervention.emotional-regulation",
            source("cci-self-help-collection", "cci-self-help-web-2024", "section-cci-distress", "loc-cci-distress"),
            ConceptSourceRelationship.CANDIDATE_SUBJECT_ONLY,
            AuthorityDomain.CLINICAL_RESOURCE_PROVIDER,
            CommercialUseStatus.COPYRIGHTED_REFERENCE_ONLY,
            "review-clinical-cci",
            "review-rights-cci-self-help-web-2024",
            listOf("The high-level family name does not import CCI exercises, worksheets, or clinical claims."),
            BindingConflictState.DIFFERENT_SCOPE_RECORDED,
            setOf("scope-cci-vs-who-self-help"),
        ),
        binding(
            "binding-external-support-nimh",
            "intervention.external-support-referral",
            source("nimh-asq-toolkit", "nimh-asq-toolkit-web-2026", "section-nimh-toolkit-positive", "loc-nimh-toolkit-page"),
            ConceptSourceRelationship.IDENTIFIES_RESTRICTION,
            AuthorityDomain.CLINICAL_GOVERNMENT,
            CommercialUseStatus.PUBLIC_DOMAIN,
            "review-clinical-nimh-toolkit",
            "review-rights-nimh-asq-toolkit-web-2026",
            listOf("A positive medical screen leads to trained-clinician assessment; no software routing rule is inferred."),
            BindingConflictState.DIFFERENT_SCOPE_RECORDED,
            setOf("scope-asq-vs-ng225"),
        ),
        binding(
            "binding-insufficient-safety-information-nice",
            "safety.insufficient-safety-information",
            source("nice-ng225", "nice-ng225-2025", "section-ng225-assessment-focus", "loc-nice-ng225-recommendations"),
            ConceptSourceRelationship.IDENTIFIES_RESTRICTION,
            AuthorityDomain.CLINICAL_GOVERNMENT,
            CommercialUseStatus.LEGAL_REVIEW_REQUIRED,
            "review-clinical-ng225",
            "review-rights-nice-ng225-2025",
            listOf("Professional psychosocial-assessment concepts do not establish autonomous software assessment authority."),
            BindingConflictState.DIFFERENT_SCOPE_RECORDED,
            setOf("scope-asq-vs-ng225"),
        ),
        binding(
            "binding-motivational-approaches-tip35",
            "intervention.motivational-approaches",
            source("samhsa-tip35", "samhsa-tip35-2019", "section-tip35-ch3", "artifact-samhsa-tip35"),
            ConceptSourceRelationship.CANDIDATE_SUBJECT_ONLY,
            AuthorityDomain.CLINICAL_GOVERNMENT,
            CommercialUseStatus.COMMERCIAL_PERMISSION_REQUIRED,
            "review-clinical-tip35",
            "review-rights-samhsa-tip35-2019",
            listOf("Substance-use treatment scope cannot be generalized; primary Motivational Interviewing rights and authority remain separate."),
        ),
        binding(
            "binding-outside-support-nimh",
            "safety.outside-support-consideration",
            source("nimh-asq-toolkit", "nimh-asq-toolkit-web-2026", "section-nimh-toolkit-positive", "loc-nimh-toolkit-page"),
            ConceptSourceRelationship.BOUNDS_SCOPE,
            AuthorityDomain.CLINICAL_GOVERNMENT,
            CommercialUseStatus.PUBLIC_DOMAIN,
            "review-clinical-nimh-toolkit",
            "review-rights-nimh-asq-toolkit-web-2026",
            listOf("This binding preserves the human-clinician boundary and selects no referral or disposition."),
            BindingConflictState.DIFFERENT_SCOPE_RECORDED,
            setOf("scope-asq-vs-ng225"),
        ),
        binding(
            "binding-problem-solving-pm-plus",
            "intervention.problem-solving",
            source("who-pm-plus-individual", "who-pm-plus-v1-1-2018", "section-pm-manual", "artifact-who-pm"),
            ConceptSourceRelationship.CANDIDATE_SUBJECT_ONLY,
            AuthorityDomain.CLINICAL_PUBLIC_HEALTH,
            CommercialUseStatus.COMMERCIAL_PERMISSION_REQUIRED,
            "review-clinical-pm",
            "review-rights-who-pm-plus-v1-1-2018",
            listOf("PM+ stages, helper competence, supervision, population fit, and implementation scope remain unopened."),
        ),
        binding(
            "binding-safety-disclosure-nice",
            "safety.safety-relevant-disclosure",
            source("nice-ng225", "nice-ng225-2025", "section-ng225-risk-tools", "loc-nice-ng225-recommendations"),
            ConceptSourceRelationship.IDENTIFIES_RESTRICTION,
            AuthorityDomain.CLINICAL_GOVERNMENT,
            CommercialUseStatus.LEGAL_REVIEW_REQUIRED,
            "review-clinical-ng225",
            "review-rights-nice-ng225-2025",
            listOf("Negative guidance is preserved without creating predictive scoring, global stratification, or keyword routing."),
            BindingConflictState.DIFFERENT_SCOPE_RECORDED,
            setOf("scope-asq-vs-ng225"),
        ),
        binding(
            "binding-safety-planning-va",
            "intervention.safety-planning",
            source("va-safety-planning", "va-safety-planning-web-2026", "section-va-safety-plan", "loc-va-safety-page"),
            ConceptSourceRelationship.CANDIDATE_SUBJECT_ONLY,
            AuthorityDomain.CLINICAL_GOVERNMENT,
            CommercialUseStatus.RIGHTS_UNCLEAR,
            "review-clinical-va-safety",
            "review-rights-va-safety-planning-web-2026",
            listOf("Provider collaboration, method evidence, population, software suitability, and third-party rights remain unresolved."),
        ),
        binding(
            "binding-structured-self-help-who",
            "intervention.structured-self-help",
            source("who-psychological-self-help", "who-self-help-2026", "section-self-help-family", "artifact-who-self-help"),
            ConceptSourceRelationship.CANDIDATE_SUBJECT_ONLY,
            AuthorityDomain.CLINICAL_PUBLIC_HEALTH,
            CommercialUseStatus.COMMERCIAL_PERMISSION_REQUIRED,
            "review-clinical-self-help",
            "review-rights-who-self-help-2026",
            listOf("Guided, self-guided, digital, and service-supported delivery assumptions remain distinct and unadjudicated."),
        ),
        binding(
            "binding-supportive-listening-fhs",
            "intervention.supportive-listening",
            source("who-unicef-foundational-helping", "who-fhs-2025", "section-fhs-overview", "loc-who-fhs-page"),
            ConceptSourceRelationship.SUPPORTS_CANDIDATE_DEFINITION,
            AuthorityDomain.CLINICAL_PUBLIC_HEALTH,
            CommercialUseStatus.COMMERCIAL_PERMISSION_REQUIRED,
            "review-clinical-fhs",
            "review-rights-who-fhs-2025",
            listOf("A human-helper training framework is not automatically valid for autonomous software or commercial adaptation."),
        ),
    ).sortedBy { it.bindingId }

    val snapshot = OntologyCatalogSnapshot(
        release = release,
        concepts = (
            observations + goals + dialogueActs + interventionFamilies + constraints + safetyContexts + proceduralVocabulary
        ).sortedBy { it.id },
        sourceBindings = sourceBindings,
    )
}
