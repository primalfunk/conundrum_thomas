package com.conundrum.thomas.v2.qualification

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CTV212AcceptanceTest(private val scenario: Scenario) {
    data class Scenario(val id: Int, val name: String) {
        override fun toString() = id.toString().padStart(3, '0') + "_" + name
    }

    @Test fun acceptanceScenario() = CTV212AcceptanceScenarios.run(scenario.id)

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = names.mapIndexed { index, name -> arrayOf(Scenario(index + 1, name)) }

        private val names = listOf(
            "empty_history_memoryless_therapy", "retrieval_unavailable_memoryless_therapy",
            "four_hundred_unrelated_objects_omitted", "repeat_plan_digest_deterministic",
            "typed_therapist_conversation_capture", "speech_transcript_capture_provenance",
            "assistant_plan_not_evidence", "rendered_response_not_evidence", "capture_failure_degrades",
            "derivation_failure_preserves_source", "pre_turn_archive_empty", "current_turn_admitted",
            "same_turn_retrieval_uses_pre_turn_revision", "same_turn_source_not_historical",
            "next_turn_may_retrieve_prior_turn", "route_without_history", "route_with_relevant_history",
            "route_with_lexical_history", "route_with_contradictory_history", "route_with_private_history",
            "same_entity_memory_may_surface", "same_event_memory_may_surface",
            "same_relationship_memory_may_surface", "recurrence_not_trait", "lexical_only_not_surfaced",
            "same_emotion_not_surfaced", "same_city_not_surfaced", "intense_irrelevant_not_surfaced",
            "best_memory_deterministic", "ordinary_max_one_memory", "memory_surfaced_turn_one",
            "memory_not_repeated_turn_two", "memory_not_repeated_later_session",
            "explicit_recall_overrides_repetition", "direct_continuation_not_memory_flex",
            "material_change_allows_reconsideration", "connection_rejected_operationally",
            "rejected_connection_suppressed", "rejection_does_not_rewrite_evidence",
            "explicit_revisit_reopens_connection", "old_memory_initially_current", "user_correction_controls",
            "corrected_state_retrieved", "obsolete_claim_not_current_truth", "explanation_shows_correction_chain",
            "hypothesis_support_only", "hypothesis_counterevidence_honest", "tight_budget_omits_one_sided_view",
            "explanation_balanced", "contradiction_no_winner", "unresolved_sams_not_merged",
            "resolved_sam_can_surface", "identity_revision_followed", "name_equality_no_identity_authority",
            "dated_stress_remains_dated", "past_state_not_trait", "approximate_event_preserved",
            "report_and_event_time_distinct", "private_journal_excluded", "private_biographer_excluded",
            "private_therapy_history_excluded", "current_private_immediate_only",
            "privacy_restore_requires_review", "explicit_recall_intent", "explicit_recall_one_memory",
            "explicit_recall_bounded_many_sources", "explicit_recall_private_excluded",
            "explicit_recall_identity_uncertainty", "explicit_recall_honest_no_result",
            "explain_active_tentative_hypothesis", "explain_with_counterevidence",
            "explain_retired_hypothesis", "explain_review_required_hypothesis",
            "explain_private_dependency", "explanation_does_not_strengthen",
            "benign_current_history_does_not_escalate", "current_safety_history_cannot_suppress",
            "safety_turn_suppresses_retrieval", "historical_risk_words_no_prediction",
            "safety_preemption_stops_route", "meeting_history_no_technique_trigger",
            "retrieval_cannot_choose_technique", "route_same_with_or_without_memory",
            "old_coping_fact_not_prescription", "journal_provenance_preserved_in_therapy",
            "biographer_provenance_preserved_in_therapy", "therapy_provenance_preserved",
            "journal_posture_unchanged", "biographer_target_unchanged", "therapy_does_not_investigate_coverage",
            "memory_reference_plan_not_write", "rendered_memory_reference_not_write",
            "user_agreement_may_be_future_evidence", "user_disagreement_may_be_future_evidence",
            "thomas_connection_never_user_evidence", "large_archive_one_strong_relation",
            "large_archive_route_unchanged", "unrelated_growth_core_memory_stable",
            "budget_exhaustion_base_plan_valid", "archive_size_memory_count_bounded",
            "multiturn_session_progression", "ct_v2_05_progression_deterministic",
            "ct_v2_05_anti_repetition_intact", "memory_anti_repetition_independent",
            "session_without_history_continues", "context_available_can_remain_unsurfaced",
            "close_reopen_equivalent_plan", "ledger_replay_equivalent_plan", "same_revision_same_plan",
            "correction_changes_plan_digest", "ct_v2_04_regression", "ct_v2_05_progression_regression",
            "ct_v2_05_repetition_regression", "ct_v2_06_regression", "ct_v2_07_regression",
            "ct_v2_08_regression", "ct_v2_09_regression", "ct_v2_10_regression", "ct_v2_11_regression",
            "journal_default_no_response", "biographer_authority_unchanged", "retrieval_authority_unchanged",
            "production_writers_zero", "android_writers_zero", "model_therapy_paths_zero",
            "model_retrieval_paths_zero", "v1_twenty_four_denied", "canonical_git_root_valid",
            "temporary_git_metadata_zero",
        ).also { require(it.size == 129) }
    }
}

internal object CTV212AcceptanceScenarios {
    fun run(id: Int) = when (id) {
        in 1..32 -> CTV212ScenariosA.run(id)
        in 33..64 -> CTV212ScenariosB.run(id)
        in 65..96 -> CTV212ScenariosC.run(id)
        in 97..129 -> CTV212ScenariosD.run(id)
        else -> error("Unknown CT-V2-12 acceptance scenario $id")
    }
}
