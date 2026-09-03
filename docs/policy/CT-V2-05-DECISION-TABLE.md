# CT-V2-05 deterministic decision table

Unless a row says otherwise, inputs are synthetic, Therapist mode is active, voluntary adult engagement and ordinary scope are established, and a current revision-matching permit is supplied.

| Case | Structured state distinction | Exact expected result |
|---|---|---|
| Unknown preference | No established supported preference | `CLARIFY_PREFERENCE` / `core-ask-support-preference` |
| Standalone decision | Preference `DECISION_SUPPORT` | `UNSUPPORTED_PATHWAY`; `DECISION_SUPPORT_SOURCE_REQUIRED` |
| Engagement unknown | Voluntary engagement not established | `INSUFFICIENT_INFORMATION`; `VOLUNTARY_ENGAGEMENT_EVIDENCE_REQUIRED` |
| Topic refused | Engagement `DOES_NOT_WANT_TOPIC` | `CONSOLIDATE_CLOSE` / `core-pause-without-response` |
| Close requested | Engagement `CLOSE_REQUESTED` | `CONSOLIDATE_CLOSE` / `core-acknowledge-close`; zero questions |
| Listen, no content | Listen preference; no bounded concern | `core-invite-expression` |
| Listen, new content | Bounded concern; `NEW_CONTENT_AVAILABLE` | `core-reflect-established-content` |
| Listen complete | `EXPRESSION_COMPLETE` | `core-summarize-listening` |
| Listen summarized | `SUMMARY_DELIVERED` | `core-check-further-or-close` |
| Understand, no concern | Understand preference; concern missing | `core-ask-present-concern` |
| Understand, named gap | Bounded concern; one important missing subject | `core-ask-important-missing-piece` |
| Understand, tentative | Tentative Thomas/shared understanding | `core-verify-tentative-understanding`; tentative renderer constraint |
| Understand, confirmed | User-confirmed; not summarized | `core-summarize-shared-understanding` |
| Understand, summarized | Confirmed; summary delivered | `core-check-understanding-next-direction` |
| Correction | Correction unhandled; prior tentative reference withdrawn | `core-acknowledge-correction` |
| Practical, no problem | Practical preference; bounded problem missing | `core-ask-problem-description` |
| Practical, tentative | Bounded problem; tentative understanding | `core-verify-problem-understanding` |
| Influence unknown | Confirmed problem; influence unknown | `core-ask-influenceable-part` |
| Not influenceable | Confirmed `NOT_INFLUENCEABLE` | `OUT_OF_SCOPE`; `NON_INFLUENCEABLE_PROBLEM_POLICY_REQUIRED` |
| Readiness unknown | Confirmed influenceable part | `core-ask-readiness-for-options` |
| Options missing | Willing; no established options | `core-invite-user-options`; advice forbidden |
| Options present | User options; no choice | `core-ask-user-to-choose-option` |
| Choice present | Selected option; no plan | `core-develop-bounded-plan` |
| Plan present | Outcome missing | `core-wait-for-outcome`; `NO_RESPONSE` |
| Outcome present | Not reviewed | `core-review-reported-outcome` |
| Outcome reviewed | Review complete | `core-consolidate-plan-learning` |
| Conflicting evidence | Any material structured field conflicts | `POLICY_CONFLICT` before route/action selection |
| Malformed state | Plan violates state invariants | `INVALID_INPUT` |
| Stale permit | Permit and safety revision differ | `INVALID_INPUT`; fresh gate decision required |
| Identical evaluation | Same state, permit, policy version | Entire decisions equal |
| Repeated action | Same substantive action at same revision | Substitute direction once, then stop |
| Repeated silence | Same `NO_RESPONSE` at same revision | `NO_AUTHORIZED_ACTION`; await change |

Each exact case is asserted in `CoreOrdinaryDecisionMatrixTest`, `CoreOrdinaryProgressionTest`, `CoreOrdinaryConversationTest`, or the permit invariant suite. Tests verify route, action, disposition, handoff, and renderer constraints rather than merely asserting a non-null result.
