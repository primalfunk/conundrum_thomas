# CT-V2-05 pathway graphs

## Hierarchy

```text
SafetyScopeGate --ORDINARY_POLICY_ALLOWED--> OrdinaryTherapyPermit
    -> route selection
        -> active goal
            -> eligible action rules
                -> unique action or typed stop
                    -> progression guard
                        -> RenderRequest or no output
```

Every node consumes typed structured evidence. A missing/stale permit ends evaluation before route selection.

## Listen/support

```text
LISTEN preference
  -> missing content: INVITE_EXPRESSION
  -> new established content: REFLECT_ESTABLISHED_CONTENT
  -> wants more / reflection received: INVITE_FURTHER_EXPRESSION
  -> expression complete: SUMMARIZE_LISTENING
  -> summary delivered: CHECK_FURTHER_OR_CLOSE
  -> explicit close/pause/refusal: CONSOLIDATE_CLOSE
```

No action enters the practical route unless changed structured preference requests it.

## Understand/clarify

```text
UNDERSTAND preference
  -> correction unhandled: ACKNOWLEDGE_CORRECTION
  -> concern missing: ASK_PRESENT_CONCERN
  -> important gap identified: ASK_ONE_MISSING_PIECE
  -> tentative understanding: VERIFY_TENTATIVE_UNDERSTANDING
  -> confirmed and not summarized: SUMMARIZE_SHARED_UNDERSTANDING
  -> summary delivered: CHECK_NEXT_DIRECTION
```

Correction requires withdrawal of the referenced tentative interpretation. Confirmation must be user-established before a summary is treated as shared understanding.

## Practical problem solving

```text
PRACTICAL_HELP preference
  -> correction unhandled: ACKNOWLEDGE_CORRECTION
  -> problem missing/vague: ASK_PROBLEM_DESCRIPTION
  -> tentative problem: VERIFY_PROBLEM_UNDERSTANDING
  -> influence unknown: ASK_INFLUENCEABLE_PART
  -> non-influenceable: OUT_OF_SCOPE
  -> willingness unknown: ASK_READINESS_FOR_OPTIONS
  -> no options: INVITE_USER_OPTIONS (advice forbidden)
  -> options, no choice: ASK_USER_TO_CHOOSE
  -> choice, no plan: DEVELOP_BOUNDED_PLAN
  -> plan, no outcome: NO_RESPONSE / WAIT
  -> reported outcome: REVIEW_OUTCOME
  -> review complete: CONSOLIDATE_LEARNING
```

Choice is owned by the user. The renderer cannot turn option elicitation into direction or advice.

## Consolidate/close

```text
CLOSE_REQUESTED -> ACKNOWLEDGE_CLOSE
PAUSE / TOPIC_REFUSAL / UNWILLING -> NO_RESPONSE
```

Re-entry requires a later evidence revision and a new explicit supported preference.

## Cross-route transitions

The 19 catalogued transitions are:

- each of listen, understand, and practical to the other two;
- each conversational route to close and clarify-preference;
- clarify-preference to each conversational route and close;
- close to each conversational route, subject to the re-entry guard.

A same-route continuation is valid without a transition record. Every cross-route transition requires changed structured evidence. Unsupported transitions return `INVALID_INPUT`.

## Stagnation branch

```text
candidate never executed at current revision -> select candidate
same candidate already executed
  -> exact bounded repeat authorization -> one repeat
  -> candidate is NO_RESPONSE -> NO_AUTHORIZED_ACTION
  -> direction choice already used -> NO_AUTHORIZED_ACTION
  -> otherwise -> OFFER_DIRECTION_CHOICE once
```
