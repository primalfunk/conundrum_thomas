# CT-V2-05 canonical synthetic conversations

All content is fictional. Every ordinary turn first obtains a matching permit from the deterministic qualification safety gate, then creates a bounded render request. The renderer output is test prose, not product dialogue.

## A. I just need to talk

```text
LISTEN + no content
  -> support expression -> core-invite-expression
new established content
  -> support expression -> core-reflect-established-content
user wants to continue
  -> support expression -> core-invite-further-expression
expression complete
  -> shared understanding -> core-summarize-listening
summary delivered
  -> close or pause -> core-check-further-or-close
close requested
  -> close or pause -> core-acknowledge-close
```

No problem-solving action is eligible in the first five turns and every renderer contract forbids advice.

## B. Help me understand why this is bothering me

```text
bounded fictional omission + one named information gap
  -> clarify -> core-ask-important-missing-piece
tentative meaning supplied as Thomas hypothesis
  -> shared understanding -> core-verify-tentative-understanding
user confirms corrected/precise meaning
  -> shared understanding -> core-summarize-shared-understanding
summary recorded delivered
  -> check understanding -> core-check-understanding-next-direction
```

The tentative render command requires tentative language. Only user-confirmed understanding may be summarized as established.

## C. I need to figure out what to do

```text
problem missing -> core-ask-problem-description
tentative bounded problem -> core-verify-problem-understanding
confirmed problem, influence unknown -> core-ask-influenceable-part
influenceable, willingness unknown -> core-ask-readiness-for-options
willing, options absent -> core-invite-user-options
user options present -> core-ask-user-to-choose-option
user choice present -> core-develop-bounded-plan
plan present, outcome absent -> core-wait-for-outcome / NO_RESPONSE
reported outcome -> core-review-reported-outcome
review complete -> core-consolidate-plan-learning
```

Option elicitation forbids advice and requires preservation of user agency.

## D. Preference change

```text
LISTEN -> core-invite-expression
new structured preference UNDERSTAND + named gap
  -> transition LISTEN_SUPPORT to UNDERSTAND_CLARIFY
  -> core-ask-important-missing-piece
```

The route changes because evidence changes, not because the policy varies randomly.

## E. Correction

```text
tentative interpretation -> core-verify-tentative-understanding
user rejects interpretation
  -> old evidence reference moved to withdrawnInterpretationReferences
  -> Thomas understanding reset to unknown
  -> core-acknowledge-correction
new tentative interpretation -> core-verify-tentative-understanding
```

The deterministic acknowledgment begins `I had that wrong` and contains no argumentative qualification.

## F. Stagnation

```text
new content -> core-reflect-established-content
same evidence revision + reflection already executed
  -> ctv205-g003 -> core-offer-direction-choice
same evidence revision + direction choice already executed
  -> ctv205-g004 -> NO_AUTHORIZED_ACTION
```

No third paraphrase is generated.

## G. Safety-gate revocation

```text
safety revision 1 established ordinary
  -> permit(revision 1)
  -> core ordinary action selected
safety revision 2 contains structured self-harm relevance
  -> SafetyScopeGate: SPECIALIZED_POLICY_REQUIRED
stale permit(revision 1) + state(revision 2)
  -> CT-V2-05: INVALID_INPUT / FRESH_SAFETY_SCOPE_GATE_DECISION_REQUIRED
```

The ordinary evaluator cannot continue by caller convention or by retaining the older permit.
