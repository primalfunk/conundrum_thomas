# CT-V2-05 core ordinary therapeutic repertoire

## Status and authority

CT-V2-05 implements a deterministic, qualification-only ordinary Therapist repertoire. It has no production or user-facing therapeutic authority. The ruleset is `ct-v2-05-core-ordinary-therapy-1.0.0`.

The policy consumes typed structured state. It does not accept raw conversation text, infer safety, call a language model, diagnose, predict, or mutate a profile.

```text
structured evidence
  -> SafetyScopeGate
  -> revision-bound OrdinaryTherapyPermit
  -> route
  -> goal
  -> eligible actions and exclusions
  -> one selected action
  -> bounded RenderRequest
  -> expected information
  -> evidence revision and reassessment
```

## Supported qualification context

The slice is limited to a voluntarily engaged adult, a non-emergency and non-specialized presentation, psychologically informed ordinary conversation, and an explicit safety/scope permit. Specialized cases remain governed stops.

Four substantive pathways are open:

1. `LISTEN_SUPPORT`: invite expression, reflect new established content, invite continuation, summarize, and offer continuation or closure without forcing problem solving.
2. `UNDERSTAND_CLARIFY`: ask for the concern or one identified missing fact, verify tentative understanding, honor correction, summarize confirmed understanding, and reassess direction.
3. `PRACTICAL_PROBLEM_SOLVING`: define and verify a bounded problem, identify its influenceable part, seek willingness, elicit user options, preserve the user's choice, form one bounded plan, wait, review, and consolidate.
4. `CONSOLIDATE_CLOSE`: acknowledge an explicit close or respect pause/refusal with `NO_RESPONSE`.

`CLARIFY_PREFERENCE` is an internal routing state for one bounded question when the supported direction is unknown.

## Route authority and transitions

Explicit user preference controls ordinary route selection. Safety/scope remains superior. Listening, understanding, and practical-help preferences select their corresponding routes. An explicit close, pause, refusal, or unwillingness selects consolidate/close. An unhandled correction can reopen understanding when there is no competing explicit preference.

The catalog contains 19 explicit cross-route transitions. Re-entry from close requires both changed structured evidence and a new explicit preference. Unsupported and missing routes fail visibly.

The standalone `DECISION_SUPPORT` preference returns `UNSUPPORTED_PATHWAY`. The governed PM+ material supports user choice inside its bounded manageable-problem procedure; it does not establish a general autonomous-software decision-support method. This gap is recorded as `need-ordinary-standalone-decision-support` with no source or rule authority.

## Epistemic and correction discipline

State distinguishes established, tentative, unknown, conflicting, and user-reported evidence through the existing ontology structures. A renderer receives tentative Thomas understanding separately from confirmed understanding and must preserve that status.

An unhandled correction is higher priority than continued exploration or problem solving. The corrected tentative evidence reference must be present in `withdrawnInterpretationReferences`; the original interpretation cannot remain an established fact. The selected action acknowledges withdrawal and asks for no defensive justification.

## Progression and anti-repetition

Each action execution records its action, route, and conversation-evidence revision. The evaluator counts repetition only against the same revision.

- First execution of an eligible action proceeds.
- One additional execution is possible only through an explicit action- and revision-bound authorization.
- A repeated generated action is replaced once with `core-offer-direction-choice`.
- A repeated `NO_RESPONSE` returns `NO_AUTHORIZED_ACTION` and awaits changed evidence.
- If the direction choice was already used against unchanged evidence, the policy returns `NO_AUTHORIZED_ACTION`.

Thus unchanged state cannot create indefinite repetition. New wording cannot disguise unchanged policy.

## Renderer boundary

The renderer receives a `RenderRequest` containing the selected act, goal, required and prohibited meanings, output form, question limit, advice permission, epistemic constraint, agency constraint, brevity target, response-required flag, and only explicitly authorized supporting text.

The renderer cannot choose or change the act. It cannot convert tentative content to fact, introduce diagnosis, add advice when prohibited, reopen a refused topic, or access a transcript/profile/database. The qualification renderer is deterministic and contains no model.

## Source derivation

Source-derived behavior uses only two already governed artifacts:

- WHO/UNICEF, *Foundational helping skills training manual* (2025), version `who-fhs-2025`: Module 1 verbal communication (publication pp. 27-31; PDF pp. 38-42), Module 8 collaborative goal-setting (publication pp. 118-120; PDF pp. 129-131), and Module 8 eliciting feedback (publication pp. 126-127; PDF pp. 137-138).
- WHO, *Problem management plus (PM+): individual psychological help...*, generic field-trial version 1.1 (2018), version `who-pm-plus-v1-1-2018`: Chapter 3 advice/reluctance boundaries (publication p. 24 and pp. 26-27; PDF p. 26 and pp. 28-29) and Chapter 7 Managing Problems steps 1-7 (publication pp. 46-51; PDF pp. 48-53).

Only abstract policy semantics are represented. No source scripts, worksheets, examples, or long excerpts are tracked. All source-derived rules retain pending clinical, rights, implementation-scope, software-autonomy, and production review; PM+ rules also retain unresolved human training/supervision assumptions.

## Unopened areas

Standalone choice/decision support and the optional emotional exploration, behavioral activation, coping planning, grounding/orientation, motivational, and interpersonal pathways are not opened. Specialized safety, diagnosis, disorder treatment, trauma treatment, medication advice, NLP inference, production persistence, and LLM integration remain outside this phase.
