# CT-V2-04 procedural safety and scope gate

## Governing question

> May the ordinary Therapist policy continue from the presently established facts?

The gate reasons about Thomas's software authority. It does not estimate the probability that a person will act, assign risk bands, diagnose, screen unrestricted text, or substitute for professional assessment.

## Architectural position

```text
typed structured safety/scope evidence
    ↓
SafetyScopeGate 1.0.0
    ├─ ORDINARY_POLICY_ALLOWED → revision-bound OrdinaryTherapyPermit
    ├─ CLARIFICATION_REQUIRED → one selected safety-oriented inquiry → reassess
    ├─ SPECIALIZED_POLICY_REQUIRED → ordinary policy prohibited
    ├─ EMERGENCY_BOUNDARY_REACHED → ordinary policy prohibited
    └─ invalid / unsupported / conflict / review blocked → ordinary policy prohibited

OrdinaryTherapyPermit + matching structured Therapist state
    ↓
CT-V2-03 bounded ordinary-problem policy
```

The ordinary evaluator has no `evaluate(state)` entry point. It accepts `evaluate(state, permit)`. The permit constructor is internal to `:thomas:safety`, and the token is bound to a state ID and positive evidence revision. This is compile-visible authority control, not cryptographic security.

The gate is not wired into `:thomas:runtime` or the Android app in CT-V2-04. Its rules are executable only inside qualification and controlled developer harnesses.

## Structured inputs

The gate consumes no raw prose. It consumes seven required evidence fields:

1. explicitly established current emergency circumstance;
2. acute medical-emergency presence or absence;
3. self-harm relevance;
4. harm-to-others relevance;
5. specialized-scope condition;
6. population/setting applicability;
7. presenting scope.

Each field preserves a value, evidence origin, evidence references, and one epistemic resolution:

`ESTABLISHED`, `NOT_ASKED`, `UNKNOWN`, `USER_DECLINED`, `TENTATIVE`, or `CONTRADICTORY`.

`ABSENT` is an established value. It is not equivalent to `UNKNOWN`, `NOT_ASKED`, refusal, tentative evidence, or contradiction.

No input represents intent probability, future behavior, diagnosis, severity score, or a global risk grade. Current intent, means, or other clinically meaningful facts were not added as improvised fields; later specialist policy may introduce only source-adjudicated concepts.

## Authority states

- `ORDINARY_POLICY_ALLOWED`: every required fact is explicitly established for the bounded adult qualification context; a permit is issued.
- `CLARIFICATION_REQUIRED`: exactly one unresolved required field is selected; no permit is issued.
- `SPECIALIZED_POLICY_REQUIRED`: the ordinary slice lacks authority and identifies the missing policy family.
- `EMERGENCY_BOUNDARY_REACHED`: an emergency fact was supplied as established upstream; ordinary policy stops, but CT-V2-04 does not invent the external procedure.
- `INSUFFICIENT_INFORMATION`: required information is unavailable, including user refusal.
- `OUT_OF_SUPPORTED_POPULATION` and `OUT_OF_SCOPE`: the admitted qualification slice does not apply.
- `REVIEW_BLOCKED`, `POLICY_CONFLICT`, `NO_AUTHORIZED_ACTION`, and `INVALID_INPUT`: explicit fail-closed engineering states.
- `EXTERNAL_SUPPORT_REQUIRED` is structurally representable but no CT-V2-04 rule selects it because the necessary specialized referral policy is unopened.

These are software authority states, not clinical risk levels.

## Clarification discipline

The gate selects one field using the stable field order encoded in `SafetyField`. Contradiction takes precedence over refusal, refusal over tentative evidence, and tentative evidence over unknown/not-asked evidence. A refusal blocks ordinary policy and does not trigger repeated questioning.

The single qualification renderer action is `clarify-required-safety-fact` using `dialogue.safety-oriented-inquiry`. The renderer receives only the selected information-requirement identifier. It receives no raw safety evidence, transcript, profile, score, or discretion to choose a different act.

The response contract permits one direct question, forbids ordinary therapy and reassurance, and forbids diagnosis, prediction, scoring, risk bands, screening-instrument wording, and unselected emergency instructions.

## Priority and failure behavior

Priority is explicit and traceable:

1. established emergency and acute medical boundaries;
2. self-harm, harm-to-others, specialized-condition, specialized-scope, population, and mode boundaries;
3. contradiction, refusal, tentative evidence, and missing information;
4. ordinary-policy permit.

Every evaluation returns all observation states, every rule result, rejected reasons, eligible rules, the unique winner, provenance, review blockers, missing facts, permitted next step, prohibited continuation path, and reassessment requirement. Equal highest priorities produce `POLICY_CONFLICT`; no match produces `NO_AUTHORIZED_ACTION`.

## Screening and specialized procedures

The NIMH ASQ is registered as bibliographic governance only. It contains no question items, score, threshold, or disposition implementation. Implemented screening instruments: zero. Runtime-authorized screening instruments: zero.

Self-harm relevance leads only to `SPECIALIZED_POLICY_REQUIRED`. Harm-to-others has an independent boundary and explicitly borrows no self-harm algorithm. Psychosis, mania, severe intoxication, abuse, trauma treatment, complex substance-use treatment, acute medical emergency, unsupported age/population, and unsupported setting stop the ordinary slice without attempting to solve the specialized condition.

## Authority status

All 14 rules are `EXECUTABLE_FOR_QUALIFICATION` and `PRODUCTION_AUTHORITY_NOT_GRANTED`. The single clinically sourced rule retains every pending review. No gate rule, renderer action, screening reference, ontology concept, or ordinary therapeutic rule has production authority.
