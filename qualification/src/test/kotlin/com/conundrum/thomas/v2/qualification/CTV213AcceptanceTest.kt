package com.conundrum.thomas.v2.qualification

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CTV213AcceptanceTest(private val scenario: Scenario) {
    data class Scenario(val id: Int, val name: String) {
        override fun toString() = id.toString().padStart(3, '0') + "_" + name
    }

    @Test fun acceptanceScenario() = CTV213AcceptanceScenarios.run(scenario.id)

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = names.mapIndexed { index, name -> arrayOf(Scenario(index + 1, name)) }

        private val names = listOf(
            "journal_no_response_zero_calls", "journal_reflect_grounded", "journal_reflect_question_rejected",
            "journal_ask_one_question", "journal_three_questions_rejected", "unsupported_journal_silence",
            "journal_technique_rejected", "journal_history_invention_rejected",
            "biographer_open_story_neutral", "biographer_temporal_gap_one_question",
            "biographer_identity_uncertainty", "biographer_contradiction_no_winner",
            "biographer_trauma_premise_rejected", "biographer_two_questions_rejected",
            "biographer_target_change_rejected", "biographer_failure_fallback",
            "therapy_memoryless_reflection", "therapy_memoryless_clarifying_question",
            "therapy_authorized_action_only", "therapy_reflection_advice_rejected",
            "therapy_route_meaning_preserved", "therapy_diagnosis_rejected",
            "renderer_without_memory_sees_none", "journal_memory_attribution", "biographer_memory_attribution",
            "therapy_memory_attribution", "tentative_connection_language", "certainty_inflation_rejected",
            "unselected_memory_rejected", "private_memory_rejected", "one_surfaced_memory_visibility",
            "self_report_rendering", "user_belief_rendering", "user_interpretation_rendering",
            "third_party_report_rendering", "tentative_hypothesis_explanation", "sam_hates_not_fact",
            "nobody_likes_not_fact", "always_fail_not_recurrence", "recurrence_not_trait",
            "exact_event_date", "approximate_date", "ranged_date", "relative_date", "ongoing_state",
            "uncertain_date", "unknown_date", "corrected_current_meaning", "obsolete_wording_not_used",
            "explanation_correction_history", "contradiction_uncertainty_preserved",
            "contradiction_winner_rejected", "zero_question_declarative_accepts",
            "zero_question_one_rejected", "one_question_one_accepts", "one_question_two_rejected",
            "quoted_question_mark_ignored", "rhetorical_question_counted",
            "candidate_under_budget", "candidate_over_character_budget", "candidate_over_sentence_budget",
            "brief_reflection_verbosity_rejected", "fallback_fits_budget",
            "consecutive_duplicate_rejected", "repeated_opening_varied", "repeated_semantic_act_varied",
            "distinct_acts_not_generic_empathy", "fixed_safety_phrase_exemption",
            "adversarial_invented_person", "adversarial_invented_date", "adversarial_diagnosis",
            "adversarial_motive", "adversarial_history", "adversarial_advice", "adversarial_mode_switch",
            "adversarial_extra_question", "adversarial_multiple_questions", "adversarial_private_memory",
            "adversarial_unselected_memory", "adversarial_certainty", "adversarial_identity_merge",
            "adversarial_source_instruction", "adversarial_system_disclosure", "adversarial_doctor_claim",
            "adversarial_policy_mutation", "adversarial_control_character",
            "historical_ignore_instructions_is_data", "historical_quit_instruction_no_authority",
            "historical_diagnosis_mode_no_authority", "current_instruction_text_no_authority",
            "adapter_unavailable", "adapter_exception", "adapter_timeout", "empty_candidate",
            "invalid_candidate", "first_invalid_second_valid", "all_attempts_invalid_fallback",
            "fallback_no_response_only", "accepted_response_zero_writes", "rejected_candidate_zero_writes",
            "fallback_zero_writes", "render_history_no_evidence", "user_quote_requires_new_upstream_turn",
            "same_phrase_journal_mode", "same_phrase_biographer_mode", "same_phrase_therapy_mode",
            "renderer_cannot_change_journal_posture", "renderer_cannot_change_biographer_target",
            "renderer_cannot_change_therapy_route", "renderer_cannot_change_safety",
            "therapy_one_memory_max", "upstream_memory_suppression_respected",
            "rejected_connection_not_resurrected", "explicit_recall_rendered",
            "explain_view_tentative", "ct_v2_04_regression", "ct_v2_05_progression_regression",
            "ct_v2_05_semantic_repetition_regression", "ct_v2_06_regression", "ct_v2_07_regression",
            "ct_v2_08_regression", "ct_v2_09_regression", "ct_v2_10_regression", "ct_v2_11_regression",
            "ct_v2_12_regression", "journal_default_no_response", "v1_twenty_four_denied",
            "production_model_root_admitted", "android_renderer_root_admitted", "canonical_git_root_valid",
            "temporary_git_metadata_zero",
        ).also { require(it.size == 131) }
    }
}

internal object CTV213AcceptanceScenarios {
    fun run(id: Int) = when (id) {
        in 1..31 -> CTV213ScenariosA.run(id)
        in 32..68 -> CTV213ScenariosB.run(id)
        in 69..103 -> CTV213ScenariosC.run(id)
        in 104..131 -> CTV213ScenariosD.run(id)
        else -> error("Unknown CT-V2-13 acceptance scenario $id")
    }
}
