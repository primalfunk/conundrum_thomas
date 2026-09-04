package com.conundrum.thomas.v2.qualification

import com.conundrum.thomas.v2.biographer.BiographerPosture
import com.conundrum.thomas.v2.biographer.BiographerQuestionPlan
import com.conundrum.thomas.v2.biographer.BiographerQuestionSemanticAct
import com.conundrum.thomas.v2.biographer.InvestigationTargetId
import com.conundrum.thomas.v2.biographer.InvestigationTargetKind
import com.conundrum.thomas.v2.domain.rendering.AuthorizedSupportingText
import com.conundrum.thomas.v2.domain.rendering.RenderCommand
import com.conundrum.thomas.v2.domain.rendering.RenderForm
import com.conundrum.thomas.v2.journal.JournalProhibitedResponseAct
import com.conundrum.thomas.v2.journal.JournalResponseGrounding
import com.conundrum.thomas.v2.journal.JournalResponsePlan
import com.conundrum.thomas.v2.journal.JournalResponsePreference
import com.conundrum.thomas.v2.journal.JournalResponseSemanticAct
import com.conundrum.thomas.v2.languagerenderer.AuthorizedSemanticUnit
import com.conundrum.thomas.v2.languagerenderer.CandidateManifest
import com.conundrum.thomas.v2.languagerenderer.CandidateRealization
import com.conundrum.thomas.v2.languagerenderer.CandidateRealizationOutcome
import com.conundrum.thomas.v2.languagerenderer.GovernedLanguageRenderer
import com.conundrum.thomas.v2.languagerenderer.GovernedRenderCommand
import com.conundrum.thomas.v2.languagerenderer.GovernedRenderMode
import com.conundrum.thomas.v2.languagerenderer.GovernedResponsePosture
import com.conundrum.thomas.v2.languagerenderer.GovernedSemanticAct
import com.conundrum.thomas.v2.languagerenderer.LanguageRealizer
import com.conundrum.thomas.v2.languagerenderer.MemoryReferencePermission
import com.conundrum.thomas.v2.languagerenderer.ProhibitedRenderedClaim
import com.conundrum.thomas.v2.languagerenderer.RenderAttribution
import com.conundrum.thomas.v2.languagerenderer.RenderBudget
import com.conundrum.thomas.v2.languagerenderer.RenderCommandId
import com.conundrum.thomas.v2.languagerenderer.RenderDirectness
import com.conundrum.thomas.v2.languagerenderer.RenderEpistemicConstraint
import com.conundrum.thomas.v2.languagerenderer.RenderEpistemicStatus
import com.conundrum.thomas.v2.languagerenderer.RenderFallbackAuthority
import com.conundrum.thomas.v2.languagerenderer.RenderHistoryState
import com.conundrum.thomas.v2.languagerenderer.RenderQualificationAuthority
import com.conundrum.thomas.v2.languagerenderer.RenderQuestionStyle
import com.conundrum.thomas.v2.languagerenderer.RenderStyleContract
import com.conundrum.thomas.v2.languagerenderer.RenderTemporalConstraint
import com.conundrum.thomas.v2.languagerenderer.SemanticAuthorityLabel
import com.conundrum.thomas.v2.languagerenderer.SemanticUnitKind
import com.conundrum.thomas.v2.languagerenderer.SemanticUnitUse
import com.conundrum.thomas.v2.longitudinal.AcquisitionMode
import com.conundrum.thomas.v2.longitudinal.AssertionId
import com.conundrum.thomas.v2.longitudinal.AssertionPolarity
import com.conundrum.thomas.v2.longitudinal.AssertionUncertainty
import com.conundrum.thomas.v2.longitudinal.EventTime
import com.conundrum.thomas.v2.longitudinal.EvidenceEpistemicClass
import com.conundrum.thomas.v2.longitudinal.PersonalConceptId
import com.conundrum.thomas.v2.longitudinal.ReportTime
import com.conundrum.thomas.v2.longitudinal.SourceRecordId
import com.conundrum.thomas.v2.retrieval.RetrievalItemKind
import com.conundrum.thomas.v2.retrieval.RetrievalLifecycleStatus
import com.conundrum.thomas.v2.retrieval.RetrievalReason
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryReference
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemoryRelation
import com.conundrum.thomas.v2.therapylongitudinal.TherapyMemorySemanticAct
import com.conundrum.thomas.v2.therapylongitudinal.TherapyRenderSupportEnvelope
import com.conundrum.thomas.v2.therapylongitudinal.TherapyTurnId
import java.time.Instant

internal object CTV213TestSupport {
    val renderer = GovernedLanguageRenderer()

    fun id(value: String = "render.test") = RenderCommandId.parse(value)

    fun basicCommand(
        commandId: String = "render.test",
        turn: Int = 1,
        mode: GovernedRenderMode = GovernedRenderMode.THERAPY,
        act: GovernedSemanticAct = GovernedSemanticAct.BRIEF_REFLECTION,
        posture: GovernedResponsePosture = GovernedResponsePosture.THERAPY,
        meaning: String = "repeated interruptions at work",
        markers: List<Set<String>> = listOf(setOf("interruptions")),
        forms: List<String> = listOf(
            "You described repeated interruptions at work.",
            "What stood out was the repeated interruptions at work.",
            "Repeated interruptions at work were central in what you described.",
        ),
        fallback: String = forms.first(),
        budget: RenderBudget = RenderBudget.BRIEF_REFLECTION,
        advice: Boolean = false,
        epistemic: RenderEpistemicStatus = RenderEpistemicStatus.USER_ASSERTED,
        attribution: RenderAttribution = RenderAttribution.CURRENT_USER,
        epistemicConstraints: List<RenderEpistemicConstraint> = emptyList(),
        temporalConstraints: List<RenderTemporalConstraint> = emptyList(),
        allowedEntities: Set<String> = emptySet(),
        allowedTimes: Set<String> = emptySet(),
        prohibitedPhrases: Set<String> = emptySet(),
        authority: RenderQualificationAuthority = RenderQualificationAuthority.SYNTHETIC_QUALIFICATION_ONLY,
    ): GovernedRenderCommand {
        val unit = AuthorizedSemanticUnit(
            "unit.primary", SemanticUnitKind.AUTHORIZED_REFLECTION_TARGET, meaning,
            epistemic, attribution, SemanticAuthorityLabel.GOVERNED_SEMANTIC_MEANING,
            allowedUses = setOf(SemanticUnitUse.REFLECTION, SemanticUnitUse.QUESTION_GROUNDING,
                SemanticUnitUse.ACTION_GROUNDING, SemanticUnitUse.EVIDENCE_EXPLANATION),
            requiredMarkerGroups = markers,
        )
        return GovernedRenderCommand(
            id(commandId), turn, mode, act, responsePosture = posture,
            semanticUnits = listOf(unit), epistemicConstraints = epistemicConstraints,
            temporalConstraints = temporalConstraints,
            prohibitedClaims = ProhibitedRenderedClaim.entries.toSet(), budget = budget,
            style = RenderStyleContract(questionStyle = if (budget.maximumQuestions == 0)
                RenderQuestionStyle.NONE else RenderQuestionStyle.OPTIONAL),
            directivenessLimit = RenderDirectness.GENTLE, advicePermitted = advice,
            memoryReferencePermission = MemoryReferencePermission.NONE,
            allowedEntityNames = allowedEntities, allowedTemporalLiterals = allowedTimes,
            prohibitedLiteralPhrases = prohibitedPhrases,
            authorizedReferenceRealizations = forms, deterministicFallbackText = fallback,
            fallbackAuthority = RenderFallbackAuthority.DETERMINISTIC_FALLBACK,
            qualificationAuthority = authority,
        )
    }

    fun manifest(command: GovernedRenderCommand) = CandidateManifest(
        command.mode, command.semanticAct,
        command.semanticUnits.filter { it.authorityLabel == SemanticAuthorityLabel.GOVERNED_SEMANTIC_MEANING }
            .map { it.id }.toSet(),
        command.historicalSupport.map { it.memoryObjectId }.toSet(),
        command.historicalSupport.flatMap { it.sourceIds }.toSet(),
    )

    fun candidate(
        command: GovernedRenderCommand,
        text: String,
        manifest: CandidateManifest = manifest(command),
    ) = CandidateRealization(text, "scripted-realizer", "scripted-realizer.v1", manifest)

    class ScriptedRealizer(private val outcomes: List<CandidateRealizationOutcome>) : LanguageRealizer {
        var calls: Int = 0
            private set

        override fun realize(input: com.conundrum.thomas.v2.languagerenderer.RendererInput, attempt: Int): CandidateRealizationOutcome {
            calls += 1
            return outcomes.getOrElse(attempt - 1) { outcomes.last() }
        }
    }

    fun scripted(vararg outcomes: CandidateRealizationOutcome) = ScriptedRealizer(outcomes.toList())

    fun candidateOutcome(command: GovernedRenderCommand, text: String, manifest: CandidateManifest = manifest(command)) =
        CandidateRealizationOutcome.Candidate(candidate(command, text, manifest))

    fun renderExternal(
        command: GovernedRenderCommand,
        text: String,
        manifest: CandidateManifest = manifest(command),
        history: RenderHistoryState = RenderHistoryState(),
    ) = renderer.render(command, history, scripted(candidateOutcome(command, text, manifest)))

    fun journalPlan(
        preference: JournalResponsePreference,
        epistemic: EvidenceEpistemicClass = EvidenceEpistemicClass.EXPLICIT_SELF_REPORT,
        uncertainty: AssertionUncertainty = AssertionUncertainty.STATED_WITHOUT_QUALIFICATION,
    ): JournalResponsePlan {
        require(preference != JournalResponsePreference.NO_RESPONSE)
        val act = if (preference == JournalResponsePreference.REFLECT)
            JournalResponseSemanticAct.BRIEF_REFLECTION else JournalResponseSemanticAct.ONE_GROUNDED_QUESTION
        return JournalResponsePlan(
            preference, true, act,
            JournalResponseGrounding(
                SourceRecordId.parse("journal.test.rev-1"), AssertionId.parse("assertion.journal-test"),
                PersonalConceptId.parse("concept.journal-test"), epistemic, uncertainty,
                AssertionPolarity.AFFIRMATIVE,
            ),
            if (act == JournalResponseSemanticAct.ONE_GROUNDED_QUESTION) 1 else 0,
            preserveUserAttribution = true, preserveUncertainty = true,
            prohibitedActs = JournalProhibitedResponseAct.entries.toSet(), reasonCode = "QUALIFIED_TEST",
        )
    }

    fun biographerPlan(
        act: BiographerQuestionSemanticAct,
        kind: InvestigationTargetKind = InvestigationTargetKind.PERIOD_DETAIL,
    ) = BiographerQuestionPlan(
        posture = if (act == BiographerQuestionSemanticAct.OPEN_HISTORICAL_INVITATION)
            BiographerPosture.OPEN_STORY else BiographerPosture.TARGETED_COVERAGE,
        targetId = if (kind == InvestigationTargetKind.OPEN_STORY) null else InvestigationTargetId.parse("target.test"),
        targetKind = kind, groundingIds = listOf("grounding.test"), safeFacts = emptyList(),
        uncertaintyConstraints = emptyList(), semanticAct = act, reasonCode = "QUALIFIED_TEST",
    )

    fun domainCommand(
        action: String = "core-reflect-established-content",
        form: RenderForm = RenderForm.REFLECTIVE,
        questions: Int = 0,
        advice: Boolean = false,
        ordinary: Boolean = true,
    ) = RenderCommand(
        policyDecisionReference = "policy.test", selectedPolicyActionId = action,
        selectedDialogueActId = "core.reflect", therapeuticGoalId = "goal.understand",
        instruction = "Express only the selected governed act.", requiredSemanticContent = emptyList(),
        allowedSemanticContent = emptyList(), prohibitedSemanticContent = listOf("new behavior"),
        toneConstraints = listOf("calm"), maximumWords = 100, maximumQuestions = questions,
        advicePermitted = advice, form = form, ordinaryTherapeuticContentPermitted = ordinary,
    )

    fun therapyEnvelope(
        memory: List<TherapyMemoryReference> = emptyList(),
        command: RenderCommand = domainCommand(),
        support: List<AuthorizedSupportingText> = listOf(
            AuthorizedSupportingText("established-concern", "work felt exhausting"),
        ),
    ) = TherapyRenderSupportEnvelope(
        command = command, currentTurnId = TherapyTurnId.parse("turn-1"),
        currentUserText = "Work felt exhausting today.", policySupportingText = support,
        surfacedMemorySupport = memory, completeContextPacketDisclosed = false,
    )

    fun memory(
        acquisitionMode: AcquisitionMode = AcquisitionMode.JOURNAL,
        semanticAct: TherapyMemorySemanticAct = TherapyMemorySemanticAct.DIRECT_RECALL,
        excerpt: String = "work was exhausting last week",
        contradiction: Boolean = false,
        identityUnresolved: Boolean = false,
        eventTime: EventTime? = null,
        id: String = "assertion.memory",
    ) = TherapyMemoryReference(
        stableObjectId = id, itemKind = RetrievalItemKind.ASSERTION,
        sourceRevisionIds = listOf(SourceRecordId.parse("source.memory.rev-1")),
        relation = TherapyMemoryRelation.DIRECT_ENTITY_CONTINUITY, semanticAct = semanticAct,
        retrievalReasons = listOf(RetrievalReason.SAME_RESOLVED_ENTITY), acquisitionMode = acquisitionMode,
        reportTime = ReportTime(Instant.parse("2040-01-01T00:00:00Z")), eventTime = eventTime,
        epistemicRole = EvidenceEpistemicClass.EXPLICIT_SELF_REPORT, uncertainty = null,
        lifecycle = RetrievalLifecycleStatus.ACTIVE, currentAuthority = true,
        relationIsExplicit = semanticAct != TherapyMemorySemanticAct.TENTATIVE_CONNECTION,
        contradictionPresent = contradiction, identityUnresolved = identityUnresolved,
        exactSourceExcerpt = excerpt,
    )

    fun questionCommand(questions: Int = 1, fallback: String = "What part of the interruptions mattered most?") =
        basicCommand(
            act = GovernedSemanticAct.CLARIFYING_QUESTION,
            meaning = "interruptions at work", markers = listOf(setOf("interruptions")),
            forms = listOf(fallback), fallback = fallback,
            budget = RenderBudget(320, 2, questions),
        )
}
