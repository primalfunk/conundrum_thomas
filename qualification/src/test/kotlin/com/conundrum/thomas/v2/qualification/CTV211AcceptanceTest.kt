package com.conundrum.thomas.v2.qualification

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CTV211AcceptanceTest(private val scenario: Scenario) {
    data class Scenario(val id: Int, val name: String) {
        override fun toString() = id.toString().padStart(2, '0') + "_" + name
    }

    @Test fun acceptanceScenario() = CTV211AcceptanceScenarios.run(scenario.id)

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            Scenario(1, "empty_archive"),
            Scenario(2, "one_relevant_source"),
            Scenario(3, "unrelated_sources_omitted"),
            Scenario(4, "repeat_digest"),
            Scenario(5, "stable_tie_break"),
            Scenario(6, "private_source_excluded"),
            Scenario(7, "retired_hypothesis_excluded"),
            Scenario(8, "superseded_revision_excluded"),
            Scenario(9, "review_required_hypothesis_not_current"),
            Scenario(10, "dependency_blocked_excluded"),
            Scenario(11, "declined_is_not_evidence"),
            Scenario(12, "privacy_change_immediate"),
            Scenario(13, "original_retrieved_before_correction"),
            Scenario(14, "correction_admitted"),
            Scenario(15, "retrieval_reflects_correction"),
            Scenario(16, "original_only_historical"),
            Scenario(17, "correction_changes_digest"),
            Scenario(18, "hypothesis_support_and_counterevidence"),
            Scenario(19, "tight_budget_omits_one_sided_hypothesis"),
            Scenario(20, "contradiction_includes_both_claims"),
            Scenario(21, "contradiction_has_no_winner"),
            Scenario(22, "unresolved_same_name_not_merged"),
            Scenario(23, "resolved_identity_groups_only_authorized"),
            Scenario(24, "revised_identity_follows_current_state"),
            Scenario(25, "lexical_name_cannot_resolve_identity"),
            Scenario(26, "current_temporal_preference"),
            Scenario(27, "explicit_1998_temporal_retrieval"),
            Scenario(28, "approximate_time_preserved"),
            Scenario(29, "unknown_time_not_discarded"),
            Scenario(30, "change_over_time_not_contradiction"),
            Scenario(31, "journal_ordinary_empty"),
            Scenario(32, "journal_explicit_look_back"),
            Scenario(33, "look_back_does_not_trigger_therapy"),
            Scenario(34, "look_back_private_excluded"),
            Scenario(35, "look_back_does_not_create_hypothesis"),
            Scenario(36, "biographer_identity_local_mentions"),
            Scenario(37, "biographer_contradiction_both_sides"),
            Scenario(38, "biographer_gap_both_boundaries"),
            Scenario(39, "biographer_target_unchanged"),
            Scenario(40, "biographer_private_declined_not_revived"),
            Scenario(41, "therapy_relevant_self_report"),
            Scenario(42, "therapy_weak_matches_no_dump"),
            Scenario(43, "therapy_hypothesis_balanced"),
            Scenario(44, "therapy_route_unchanged"),
            Scenario(45, "keyword_does_not_select_technique"),
            Scenario(46, "therapy_private_excluded"),
            Scenario(47, "explain_active_hypothesis"),
            Scenario(48, "explain_includes_counterevidence"),
            Scenario(49, "explain_retired_status"),
            Scenario(50, "explain_private_dependency_ineligible"),
            Scenario(51, "supporting_excerpt_provenance"),
            Scenario(52, "exact_span_round_trip"),
            Scenario(53, "truncated_excerpt_offsets"),
            Scenario(54, "truncation_preserves_negation"),
            Scenario(55, "wrong_revision_span_rejected"),
            Scenario(56, "private_excerpt_never_enters"),
            Scenario(57, "historical_ignore_instruction_is_data"),
            Scenario(58, "historical_mode_instruction_is_data"),
            Scenario(59, "historical_diagnose_instruction_no_authority"),
            Scenario(60, "assistant_immediate_only_continuity"),
            Scenario(61, "assistant_response_not_hypothesis_support"),
            Scenario(62, "candidate_set_budget"),
            Scenario(63, "excerpt_count_budget"),
            Scenario(64, "dependency_depth_budget"),
            Scenario(65, "budget_edge_tie_stable"),
            Scenario(66, "no_archive_dump"),
            Scenario(67, "large_multi_year_history"),
            Scenario(68, "bounded_as_archive_grows"),
            Scenario(69, "packet_size_not_linear"),
            Scenario(70, "core_selection_survives_unrelated_growth"),
            Scenario(71, "same_truth_different_mode_pressure"),
            Scenario(72, "journal_conservative"),
            Scenario(73, "biographer_locally_aggressive"),
            Scenario(74, "therapy_bounded_relevance_first"),
            Scenario(75, "mode_does_not_change_lifecycle"),
            Scenario(76, "close_reopen_same_packet"),
            Scenario(77, "ledger_replay_same_packet"),
            Scenario(78, "as_of_revision_honest"),
            Scenario(79, "current_after_correction_differs"),
            Scenario(80, "safety_permit_regression"),
            Scenario(81, "therapy_progression_regression"),
            Scenario(82, "anti_repetition_regression"),
            Scenario(83, "longitudinal_invariants_regression"),
            Scenario(84, "admission_store_regression"),
            Scenario(85, "epistemic_state_regression"),
            Scenario(86, "journal_regression"),
            Scenario(87, "biographer_regression"),
            Scenario(88, "journal_default_no_response"),
            Scenario(89, "biographer_target_authority_unchanged"),
            Scenario(90, "production_writers_zero"),
            Scenario(91, "android_writers_zero"),
            Scenario(92, "model_retrieval_decisions_zero"),
            Scenario(93, "model_evidence_writers_zero"),
            Scenario(94, "v1_register_24_denied"),
            Scenario(95, "canonical_git_root_valid"),
            Scenario(96, "temporary_git_metadata_zero"),
        ).map { arrayOf(it) }
    }
}

internal object CTV211AcceptanceScenarios {
    fun run(id: Int) = when (id) {
        in 1..32 -> CTV211ScenariosA.run(id)
        in 33..64 -> CTV211ScenariosB.run(id)
        in 65..96 -> CTV211ScenariosC.run(id)
        else -> error("Unknown CT-V2-11 acceptance scenario $id")
    }
}
