# CT-V2-09 Journal response postures

## Authority model

Response preference is transient conversational metadata. It does not alter source content, provenance, admission, evidence proposals, formed state, contradiction handling, temporal interpretation, identity, or privacy.

| Posture | Plan | Questions | Renderer invocation |
|---|---|---:|---:|
| NO_RESPONSE | none | 0 | 0 |
| REFLECT | optional BRIEF_REFLECTION | 0 | only if explicitly executed |
| ASK_ONE_QUESTION | optional ONE_GROUNDED_QUESTION | at most 1 | only if explicitly executed |

## NO_RESPONSE

This is the enum's first value, the command default, and the locked Journal mode default. The engine commits and processes eligible evidence, then returns NONE_SELECTED with no plan. It does not call the intent planner, renderer, or model and does not create a placeholder artifact.

## REFLECT

The deterministic planner selects the first admitted current-entry proposal (or grounded correction assertion) and carries source revision, assertion, concept, epistemic class, uncertainty, and polarity into the plan. Attribution is mandatory for self-belief, user interpretation, and third-party report. Uncertainty is preserved.

The plan does not contain or retrieve prior Journal text. A qualification renderer can mechanically realize the act, but it cannot change it.

## ASK_ONE_QUESTION

The same local grounding discipline applies. maximumQuestionCount is exactly one, and the downstream executor enforces at most one question mark. When perception yields no grounded assertion, disposition is NO_SAFE_GROUNDED_RESPONSE and no plan is emitted.

This is not Biographer gap selection and does not inspect longitudinal coverage.

## Prohibitions

Every permitted plan contains all of these prohibitions:

- diagnosis;
- psychological formulation;
- therapy technique;
- cognitive challenge;
- hidden-motive inference;
- historical retrieval;
- Biographer gap pursuit;
- multiple questions;
- directive advice;
- evidence mutation.

JournalResponsePlan is a conversation artifact, not evidence. Rendered text has no path back into Journal capture or CT-V2-07 admission.
