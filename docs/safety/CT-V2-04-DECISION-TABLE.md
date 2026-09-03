# CT-V2-04 deterministic decision table

All fixtures are synthetic. Unless overridden, fields are established as: no current emergency, no acute medical emergency, no self-harm or harm-to-others relevance, no specialized condition, supported adult qualification population/setting, bounded ordinary problem, Therapist mode.

| Case | Structured change | Exact authority result | Selected rule / next step |
|---:|---|---|---|
| 1 | Baseline established facts | `ORDINARY_POLICY_ALLOWED` | `r014`; permit issued |
| 2 | Self-harm relevance `UNKNOWN` | `CLARIFICATION_REQUIRED` | `r013`; ask only `SELF_HARM_RELEVANCE` |
| 3 | Self-harm relevance `NOT_ASKED` | `CLARIFICATION_REQUIRED` | `r013`; absence is not assumed |
| 4 | Required fact `USER_DECLINED` | `INSUFFICIENT_INFORMATION` | `r011`; no repeated question, no permit |
| 5 | Self-harm relevance established present | `SPECIALIZED_POLICY_REQUIRED` | `r003`; ordinary path prohibited |
| 6 | Self-harm evidence contradictory | `CLARIFICATION_REQUIRED` | `r010`; contradiction retained |
| 7 | Self-harm absence tentative | `CLARIFICATION_REQUIRED` | `r012`; tentative is not established |
| 8 | Psychosis supplied as established specialized condition | `SPECIALIZED_POLICY_REQUIRED` | `r005` |
| 9 | Acute medical emergency established present | `EMERGENCY_BOUNDARY_REACHED` | `r002` |
| 10 | Explicit current emergency plus self-harm relevance | `EMERGENCY_BOUNDARY_REACHED` | `r001` wins visibly over `r003` |
| 11 | Harm-to-others relevance established present | `SPECIALIZED_POLICY_REQUIRED` | `r004`; no borrowed self-harm algorithm |
| 12 | Unsupported age/population | `OUT_OF_SUPPORTED_POPULATION` | `r008` |
| 13 | Unsupported setting | `OUT_OF_SUPPORTED_POPULATION` | `r008` |
| 14 | Presenting scope specialized | `SPECIALIZED_POLICY_REQUIRED` | `r006` |
| 15 | Presenting scope out of scope | `OUT_OF_SCOPE` | `r007` |
| 16 | Matching permit rule changed to candidate-only | `REVIEW_BLOCKED` | no permit |
| 17 | Two equal-priority matching rules | `POLICY_CONFLICT` | conflict is not guessed |
| 18 | Invalid state ID | `INVALID_INPUT` | no rules execute |
| 19 | Same input repeated | Equal `SafetyScopeDecision` | deterministic equality |
| 20 | Ordinary gate pass followed by CT-V2-03 | CT-V2-03 selects `ask-support-preference` | matching permit required |
| 21 | Gate denial followed by CT-V2-03 attempt | No permit exists | no state-only evaluator overload |
| 22 | Clarification changes absent fact at revision 2 | `ORDINARY_POLICY_ALLOWED` after reassessment | fresh revision-2 permit |
| 23 | Empty policy graph | `NO_AUTHORIZED_ACTION` | no implicit fallback |
| 24 | Permit revision does not match policy state | CT-V2-03 `INVALID_INPUT` | fresh gate decision required |

The full tests also assert every precondition, rejection reason, selected provenance record, prohibited continuation path, and renderer constraint.
