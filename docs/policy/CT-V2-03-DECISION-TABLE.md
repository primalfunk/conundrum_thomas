# CT-V2-03 decision table

All inputs are structured. No row begins with raw user prose. The expected winner is exact.

| Case | Material structured input | Expected disposition | Exact selected action / handoff |
|---|---|---|---|
| Initial valid entry | Therapist; ordinary slice permitted; bounded scope; willing; intent unknown | `ACTION_SELECTED` | `ask-support-preference` |
| Intent already known | Practical intent; problem unknown | `ACTION_SELECTED` | `ask-problem-description`; support preference not asked again |
| Tentative intent | Practical intent only tentative | `ACTION_SELECTED` | `ask-support-preference` |
| Problem vague | Intent known; vague or missing problem | `ACTION_SELECTED` | `ask-problem-description` |
| Tentative understanding | Bounded problem; practical/understanding intent; shared understanding tentative | `ACTION_SELECTED` | `verify-problem-understanding` |
| Listening requested | Bounded problem; listening intent | `ACTION_SELECTED` | `reflect-for-listening`; no problem-solving family selected |
| Understanding requested | Bounded problem; understanding confirmed | `ACTION_SELECTED` | `summarize-for-understanding` |
| Practical help requested | Bounded problem confirmed; influence unknown | `ACTION_SELECTED` | `ask-influenceable-part` |
| Not ready to act | Influenceable problem; willingness not established | `ACTION_SELECTED` | `ask-readiness-for-options` |
| Ready, options missing | Influenceable problem; willing to act | `ACTION_SELECTED` | `invite-user-options` |
| Options already known | User options present; selection missing | `ACTION_SELECTED` | `ask-user-to-choose-option`; options not requested again |
| Option selected | Selected option present; plan missing | `ACTION_SELECTED` | `develop-bounded-plan` |
| Waiting for result | Plan present; reported outcome missing | `ACTION_SELECTED` | `wait-for-outcome`; exact no-response |
| Reassessment | Plan and outcome present | `ACTION_SELECTED` | `review-reported-outcome` |
| User unwilling | Refusal established | `ACTION_SELECTED` | `pause-without-response` |
| Conflicting evidence | Any material policy field has conflict with two evidence references | `POLICY_CONFLICT` | `EVIDENCE_ADJUDICATION_REQUIRED` |
| Unknown safety | Upstream safety disposition unknown | `INSUFFICIENT_INFORMATION` | `UPSTREAM_SAFETY_AUTHORITY_REQUIRED` |
| Specialized safety | Upstream safety requires specialized policy | `SPECIALIZED_POLICY_REQUIRED` | `SPECIALIZED_SAFETY_POLICY_REQUIRED` |
| Out of scope | Explicit outside-slice disposition | `OUT_OF_SCOPE` | `OUTSIDE_SLICE_POLICY_REQUIRED` |
| Specialized scope | Explicit specialized-policy disposition | `SPECIALIZED_POLICY_REQUIRED` | `SPECIALIZED_POLICY_REQUIRED` |
| Non-influenceable problem | Problem represented as not influenceable | `OUT_OF_SCOPE` | `NON_INFLUENCEABLE_PROBLEM_POLICY_REQUIRED` |
| Wrong mode | Journal or Biographer | `OUT_OF_SCOPE` | `ACTIVE_MODE_POLICY_REQUIRED` |
| Review-blocked rule | Matching rule is only `CANDIDATE_RULE` | `REVIEW_BLOCKED` | `RULE_REVIEW_REQUIRED` |
| Multiple eligible, distinct priority | Two matches; one uniquely higher | `ACTION_SELECTED` | Higher priority; both eligible actions reported |
| Equal highest priority | Two equally applicable winners | `POLICY_CONFLICT` | `POLICY_ADJUDICATION_REQUIRED` |
| Undefined graph | No matching rule | `NO_AUTHORIZED_ACTION` | `POLICY_DEFINITION_REQUIRED` |
| Malformed state | Invalid state ID or inconsistent structured fields | `INVALID_INPUT` | `STRUCTURED_INPUT_CORRECTION_REQUIRED` |
| Repeatability | Same immutable state and policy version evaluated twice | Identical | Entire `PolicyDecision` equal |

The executable tests verify selected action IDs, disposition, handoff, active goal, expected information, rule rationale, and tie-break structure—not merely the presence of a result.
