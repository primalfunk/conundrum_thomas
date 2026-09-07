# Direct checkpoint-16 adjudication

## Finding

Thomas selects **core-verify-problem-understanding**, not core-summarize-shared-understanding. The latter is the last successfully delivered action from checkpoint 15. Rendering the newly selected verification fails with REPEATED_OPENING, so the delivered-action history remains unchanged.

This document records the direct controls. The accompanying physical qualification report must separately confirm the actual UI boundary; a direct replay alone does not establish what a historical uninstrumented UI click delivered.

## Exact governing state

The replay uses the unchanged production runtime submit boundary, the full 15-input UI prefix, fixed turn identities 69–83 and controlled timestamps. It does not inject an action or modify procedural state. The archived fixture's source-history prerequisite is not recreated in this JVM harness: its store contains the 15 current UI sources, while the physical store also contains the earlier A–N corpus. That difference is explicit; equivalence of the inputs that actually govern decision/rendering must be checked against the physical trace.

Before checkpoint 16:

| Field | Value |
| --- | --- |
| Thomas UI mode / engine mode | THERAPY / THERAPIST |
| Prior support / last delivered route | UNDERSTAND / UNDERSTAND_CLARIFY |
| State ID / safety revision / material revision | android-therapy-state-83 / 83 / 15 |
| Concern | the UI meeting was delayed, resolved as reported |
| Thomas understanding / shared understanding | same text, resolved as reported / CONFIRMED |
| Understanding summary delivered | true |
| Correction | ACKNOWLEDGED; corrected meaning retained |
| Pending information | CLOSURE_OR_NEW_DIRECTION |
| Last delivered action | core-summarize-shared-understanding at revision 15 |
| Delivered history | 14 actions; unchanged-evidence third UI turn had no action |
| Repeat authorization | null |
| Influence, willingness, options, selection, plan, outcome, review | unknown |
| Recent render window | turns 80, 81, 82, 83 |
| Verification opening occurrences in window | 2: turns 80 and 82, both beginning “I might be” |

The complete serialized state, all evidence references, history and render fingerprints are in targeted-adjudication.xml and CTV215R1PracticalAdjudicationTest.xml. The input observer's lastMeaning excludes evidence-reference IDs and history bookkeeping; the new material concern/support advances revision to 16.

Checkpoint request: identity android-therapy-84, mode THERAPY, requestedTherapySupport=PRACTICAL_HELP, ordinary memory intent, eligible privacy, typed input. Exact historical specimen has no terminal period; the Principal's terminal-period form is separately exercised, as is a different concern. All produce the same decision/rejection mechanism.

The input observer trims the text, normalizes case/spacing/terminal punctuation for comparison, recognizes the existing “My specific concern is: ” declaration, and sees a materially different concern. It resets the previous problem's derived state, retains delivered history/last route, establishes the new bounded concern and engagement, and forms a tentative literal restatement for the non-Listen request. No phrase-specific branch is added.

At the decision boundary:

- routePreference = PRACTICAL_HELP, RESOLVED_AS_REPORTED;
- concern = arranging a new UI meeting, RESOLVED_AS_REPORTED;
- problemClarity = BOUNDED;
- engagement = ENGAGED;
- expressionProgress = NEW_CONTENT_AVAILABLE;
- Thomas understanding and shared understanding = TENTATIVE;
- summary-delivered, correction, influence, willingness, options, selection, plan, outcome and review = UNKNOWN;
- pendingInformation = null;
- activeRoute remains UNDERSTAND_CLARIFY as the last delivered route, not the new route selection;
- safety authority = ORDINARY_POLICY_ALLOWED with the matching fresh permit;
- conversationRevision = 16; history still ends at revision 15.

## Competing actions and authority

| Action | Actual rule/predicates | Result |
| --- | --- | --- |
| Verify Practical problem | ctv205-a017-problem-verify, priority 1000: PRACTICAL_PROBLEM_SOLVING route, established bounded concern, TENTATIVE shared understanding and TENTATIVE Thomas understanding | All match; sole eligible candidate |
| Summarize shared understanding | ctv205-a013-understand-summarize-confirmed, priority 700: UNDERSTAND_CLARIFY route, user-confirmed established understanding, summary not yet delivered | Route and confirmed-understanding predicates reject it |

Route rule ctv205-r007-practical-route matches the explicit PRACTICAL_HELP preference. Understand route rule ctv205-r006-understand-route fails. The route transition UNDERSTAND_CLARIFY → PRACTICAL_PROBLEM_SOLVING is explicitly supported by CoreRouteTransitionCatalog.

There is no priority competition between the two actions. Verification is eligible; summary is ineligible. Progression is NEW_ACTION, occurrencesAtCurrentRevision=0, no progression guard selected, no repeat authorization or direction-choice substitution. All action-rule evaluations/rejection reasons are captured by the actual CoreOrdinaryTherapyEvaluator.

The governing CT-V2-05 decision table, row “Practical, tentative”, requires verification. Its “Understand, confirmed” summary row has different prerequisites. The pathway graphs agree. CT-V2-15R1's observation/coverage specification explicitly says failed rendering must not record a delivered action or create pending confirmation.

## Oracle lineage

Feature commit 98be998 originally clicked Practical and expected the response to contain “Is that right”. Commit d0ca6c4 replaced lexical assertions with the semantic expected action core-verify-problem-understanding, and read session.actionHistory.last().actionId. Later observation/collision changes retained that expectation.

The expected semantic action is consistent with the governing rules and existing JVM Practical progression tests. The assertion's observation method conflates “new action delivered” with “action selected now” when rendering fails. It is a delivery gate whose failure was initially described as a competing-decision result. Replacing its expectation with summary would mask a real delivery failure and is not an authorized oracle correction.

## Renderer boundary

The plan selects core-verify-problem-understanding / dialogue.verify-understanding / goal.establish-shared-understanding. Its render support passes exactly that action to TherapyRenderCommandAdapter, which produces CLARIFYING_QUESTION and the authorized tentative text:

“I might be understanding this as arranging a new UI meeting. Is that right?”

The verification action has only one authorized realization. therapyVariants supplies additional invitation/reflective forms but no alternate verification form. The deterministic fallback is the same text.

RenderText.openingFingerprint hashes the first three normalized words. The unchanged validator rejects an opening already occurring twice in the last four delivered responses. Turns 80 and 82 share the fingerprint f291cc5548ffd3d194f178b8eb72c7609ca669d536ba8b1d961f78ac0d73e15f for “i might be”. The checkpoint's full response is not an exact recent duplicate; the rejection is REPEATED_OPENING.

The renderer searches the command-authorized space and exhausts its single unique realization. It returns RENDERING_UNAVAILABLE, finalText=null, CLARIFYING_QUESTION; no assistant artifact. The runtime consequently does not call therapyInput.delivered. The source is nevertheless accepted and committed under identity 84.

## Controls and limits

Four new diagnostic tests pass:

1. Ten exact reconstructions: one identical CorePolicyDecision and one identical canonical render digest; verification selected every time; repeated opening rejected every time; prior delivered history unchanged.
2. Original specimen, terminal-period specimen and a different concern: same predicate outcome and rejection mechanism.
3. Cold reopen: source identities/digest preserved, ephemeral procedure and render history empty; fresh current declarations plus Practical input select and successfully deliver verification; source 84 survives another reopen and next allocation is 85.
4. Adjacent Practical order without the exhausted opening history: verification → influenceable part → readiness → user options, with faithful accepted rendering.

These diagnostic assertions expose the blocked behavior; they do not redefine rendering failure as a passing delivery gate. Existing physical semantic assertions remain unchanged. No production repair, semantic-oracle replacement, validator relaxation or new renderer alternative has been made.

Support preference is active UI/request state, not a durable global mode field. The corpus persists source acquisition mode/content, not ephemeral procedural authority. Cold reopen resets procedure/safety/render history under the existing documented contract; it must not be represented as restoration of that earlier confirmed understanding.
