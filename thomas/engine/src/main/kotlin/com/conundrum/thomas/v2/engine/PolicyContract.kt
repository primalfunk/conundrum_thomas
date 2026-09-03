package com.conundrum.thomas.v2.engine

/** Marker for a structured, provenance-aware policy input. Raw user prose is not a policy input. */
interface StructuredPolicyState

/** Marker for an action proposal produced by policy and still subject to safety governance. */
interface TherapeuticActionProposal

/** Structured selection contract. Implementations must make their authority scope explicit. */
fun interface TherapeuticPolicy<STATE : StructuredPolicyState, ACTION : TherapeuticActionProposal> {
    fun select(state: STATE): ACTION
}
