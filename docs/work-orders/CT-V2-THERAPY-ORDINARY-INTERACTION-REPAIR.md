# CT-V2 Therapy Ordinary Interaction Repair

Disposition: `CT_V2_THERAPY_ORDINARY_INTERACTION_RESTORED_SAFETY_GUARD_CONDITIONAL`

## Provenance

This repair began from the recovered STT working tree at `bed63ba` (`feat: restore primary speech input`), with a clean working tree. No published history was rewritten and no dependency drift was introduced.

## Reconnaissance finding

The ordinary spoken transcript reaches the same submission path as typed text:

`STT final transcript -> editable draft -> ThomasViewModel.submit -> ProductionTurnRequest -> ThomasProductionRuntime.submitTherapy -> ProductionSafetyObservationBoundary -> SafetyScopeGate -> therapy progression/rendering`

The transcript was correct for the reproduced ordinary phrase. The failure was in the common production safety path, not STT. A fresh `SafetyScopeInput` contained `UNKNOWN` for every safety field. The generic missing-evidence rule `ctv204-r013-missing-evidence-clarification` then selected `CURRENT_EMERGENCY_STATUS` as the first clarification. This made absence of evidence behave as an emergency intake gate. The same behavior was present for typed input and was not limited to a restored session or to the renderer.

## Repair boundary

`UNKNOWN` remains a distinct safety value. Production Therapy now declares an explicit unknown-evidence policy that permits ordinary conversation while evidence is absent. The gate still honors explicit current-emergency evidence, tentative or contradictory evidence, declined evidence, and legitimately pending clarification. Qualification callers retain the strict default that requires clarification.

Ordinary nonblank Therapy content is admitted as the current concern with bounded clarity and engagement only; it does not infer diagnosis, risk, or hidden structure. Safety declaration lines remain owned by the safety boundary and are not duplicated into ordinary concern capture.

Pending current-emergency clarification is created only from an authorized safety observation. A contextual negative answer satisfies that pending field, and restart after either a pending or satisfied clarification does not manufacture or resurrect it.

## Evidence

- `CTV2TherapyOrdinaryInteractionQualificationTest`: eight ordinary exemplars, consecutive-turn anti-repetition, typed/speech parity, safe phrase without lexical state creation, legitimate pending clarification, contextual negative, explicit boundary, and restart cases.
- Updated `CTV215R1ProductionConversationTest`: preserves contextual pending-reply behavior while changing stale unconditional-gate expectations.
- `TherapyOrdinaryInteractionInstrumentedTest`: canonical-package device regression; enters Therapy, submits `Work has been frustrating lately.`, verifies the recognized text, waits for a Thomas response, and asserts the emergency interrogation is absent.
- Focused JVM qualification passed: 49 ordinary/conversation cases and 13 practical/runtime cases.
- `:app:assembleDebug` passed.
- Canonical device regression passed on TCL `9491G` / Android API 35 using the exact debug candidate.

The broad legacy connected suite still contains pre-existing fixture-package and old STT expectations, and the broad JVM suite is affected by ignored `out/` workspace inventory artifacts. Those unrelated qualification harness issues were not hidden or deleted.

## STT disposition

This repair restores the Therapy behavior required for meaningful speech qualification. It does not by itself claim `CT_V2_PRIMARY_SPEECH_INPUT_RESTORED_AND_QUALIFIED`; the remaining STT gates and Principal-facing audio checks remain open until separately completed.
