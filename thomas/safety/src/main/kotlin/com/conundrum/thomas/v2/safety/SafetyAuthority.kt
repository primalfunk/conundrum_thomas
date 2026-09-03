package com.conundrum.thomas.v2.safety

/** Marker for structured input to the independently authoritative safety boundary. */
interface SafetyReviewInput

/** Marker for the safety-governed result that may allow, constrain, replace, defer, or escalate. */
interface SafetyGovernedResult

/** Authority contract only; CT-V2-00 provides no safety classification or clinical pathway. */
fun interface SafetyGovernor<INPUT : SafetyReviewInput, RESULT : SafetyGovernedResult> {
    fun govern(input: INPUT): RESULT
}
