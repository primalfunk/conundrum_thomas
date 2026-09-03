# CT-V2-05 rule catalog

**Ruleset:** `ct-v2-05-core-ordinary-therapy-1.0.0`

**Total rules:** 39 (9 route, 26 action, 4 progression)

**Source-derived:** 32

**Engineering authority/control:** 7

**Qualification-executable:** 39

**Production-runtime-authorized:** 0

All rules have exact provenance. `ARCH` is the Principal CT-V2-05 authority-scope record. `FHS-V`, `FHS-G`, and `FHS-F` are WHO/UNICEF FHS 2025 Module 1 verbal communication, Module 8 collaborative goals, and Module 8 feedback respectively. `PM-R`, `PM-D`, `PM-P`, `PM-O`, and `PM-F` are WHO PM+ v1.1 reluctance/advice, problem definition, option generation, action planning, and follow-up.

Source-derived rules retain clinical, rights, implementation-scope, software-autonomy, and production restrictions. PM+ rules additionally retain human training/supervision restrictions. Architecture rules retain production denial.

## Route rules

| Rule | Priority | Match | Result | Provenance |
|---|---:|---|---|---|
| `ctv205-r001-close-route` | 2200 | Explicit close request | `CONSOLIDATE_CLOSE` | ARCH |
| `ctv205-r002-pause-or-refusal-route` | 2190 | Pause, topic refusal, or unwillingness | `CONSOLIDATE_CLOSE` | PM-R |
| `ctv205-r003-engagement-required` | 2100 | Voluntary engagement not established | `INSUFFICIENT_INFORMATION` | ARCH |
| `ctv205-r004-decision-path-unopened` | 2050 | Standalone decision-support preference | `UNSUPPORTED_PATHWAY` | ARCH |
| `ctv205-r005-listen-route` | 1900 | Established listen preference | `LISTEN_SUPPORT` | FHS-G |
| `ctv205-r006-understand-route` | 1890 | Established understand preference | `UNDERSTAND_CLARIFY` | FHS-G |
| `ctv205-r007-practical-route` | 1880 | Established practical-help preference | `PRACTICAL_PROBLEM_SOLVING` | FHS-G |
| `ctv205-r008-correction-understand-route` | 1800 | Unhandled correction and no explicit preference | `UNDERSTAND_CLARIFY` | FHS-F |
| `ctv205-r009-clarify-preference-route` | 1700 | Supported preference not established | `CLARIFY_PREFERENCE` | FHS-G |

The evaluator requires a unique highest-priority route. Equal highest priorities produce `POLICY_CONFLICT`; display ordering never adjudicates a tie.

## Action rules

| Rule | Route and prerequisites | Result | Provenance |
|---|---|---|---|
| `ctv205-a001-ask-support-preference` | Clarify-preference route | `core-ask-support-preference` | FHS-G |
| `ctv205-a002-acknowledge-close` | Close route; explicit close | `core-acknowledge-close` | ARCH |
| `ctv205-a003-pause-without-response` | Close route; pause, refusal, or unwillingness | `core-pause-without-response` | PM-R |
| `ctv205-a004-listen-invite-expression` | Listen; bounded concern missing | `core-invite-expression` | FHS-V |
| `ctv205-a005-listen-reflect-new-content` | Listen; bounded concern and new content | `core-reflect-established-content` | FHS-V |
| `ctv205-a006-listen-invite-more` | Listen; reflection received or user wants to continue | `core-invite-further-expression` | FHS-V |
| `ctv205-a007-listen-summarize` | Listen; expression complete | `core-summarize-listening` | FHS-V |
| `ctv205-a008-listen-check-close` | Listen; summary delivered | `core-check-further-or-close` | FHS-G |
| `ctv205-a009-understand-correction` | Understand; unhandled correction | `core-acknowledge-correction` | FHS-F |
| `ctv205-a010-understand-ask-concern` | Understand; bounded concern missing | `core-ask-present-concern` | FHS-V |
| `ctv205-a011-understand-missing-piece` | Understand; one established important gap | `core-ask-important-missing-piece` | FHS-V |
| `ctv205-a012-understand-verify-tentative` | Understand; tentative Thomas understanding | `core-verify-tentative-understanding` | FHS-F |
| `ctv205-a013-understand-summarize-confirmed` | Understand; user-confirmed understanding, not summarized | `core-summarize-shared-understanding` | FHS-V |
| `ctv205-a014-understand-next-direction` | Understand; confirmed understanding summarized | `core-check-understanding-next-direction` | FHS-G |
| `ctv205-a015-problem-correction` | Practical; unhandled correction | `core-acknowledge-correction` | FHS-F |
| `ctv205-a016-problem-define` | Practical; bounded concern missing | `core-ask-problem-description` | PM-D |
| `ctv205-a017-problem-verify` | Practical; bounded concern and tentative understanding | `core-verify-problem-understanding` | FHS-F + PM-D |
| `ctv205-a018-problem-influence` | Practical; confirmed understanding, influence unknown | `core-ask-influenceable-part` | PM-D |
| `ctv205-a019-problem-non-influenceable` | Practical; confirmed non-influenceable problem | `OUT_OF_SCOPE` | PM-D |
| `ctv205-a020-problem-readiness` | Practical; influenceable part established, willingness unknown | `core-ask-readiness-for-options` | FHS-G |
| `ctv205-a021-problem-options` | Practical; willing and no options | `core-invite-user-options` | PM-P + PM-R |
| `ctv205-a022-problem-select` | Practical; user options present, selection missing | `core-ask-user-to-choose-option` | PM-P |
| `ctv205-a023-problem-plan` | Practical; user's selected option, plan missing | `core-develop-bounded-plan` | PM-O |
| `ctv205-a024-problem-wait` | Practical; plan present, outcome absent | `core-wait-for-outcome` (`NO_RESPONSE`) | PM-F |
| `ctv205-a025-problem-review` | Practical; reported outcome not reviewed | `core-review-reported-outcome` | PM-F |
| `ctv205-a026-problem-consolidate` | Practical; outcome reviewed | `core-consolidate-plan-learning` | PM-F + FHS-G |

Action rules also require their selected route. Preconditions and exclusions are evaluated independently and retained in the trace. The current catalog has no implicit exclusion list: unavailable alternatives fail explicit prerequisites.

## Progression guards

| Rule | Trigger | Result | Provenance |
|---|---|---|---|
| `ctv205-g001-explicit-repeat-once` | Same action/revision plus matching explicit authorization | Permit at most one additional execution | ARCH |
| `ctv205-g002-no-response-loop-stop` | Repeated `NO_RESPONSE` against unchanged evidence | `NO_AUTHORIZED_ACTION`; await changed evidence | ARCH |
| `ctv205-g003-stagnation-offer-direction` | Repeated substantive action against unchanged evidence | Substitute `core-offer-direction-choice` once | FHS-G |
| `ctv205-g004-stagnation-stop` | Direction choice already used without new evidence | `NO_AUTHORIZED_ACTION`; await change or close | ARCH |

## Authority semantics

A source link explains why a rule exists; it does not approve autonomous software or production use. Missing provenance is structurally invalid. Automated qualification cannot complete clinical, rights, legal, implementation, software-autonomy, or supervision review.
