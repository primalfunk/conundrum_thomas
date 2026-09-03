# CT-V2-03 — Procedural Therapist vertical slice

**Policy:** `ct-v2-03-bounded-problem-1.0.0`

**Authority:** qualification execution only

**Production therapeutic authority:** not granted

## Selected situation

This slice handles one structured situation: Therapist mode has received an upstream disposition permitting the ordinary slice, the user is voluntarily participating, and the subject is a bounded, non-emergency personal problem. It supports three user-selected directions:

- listening;
- establishing shared understanding;
- opening a bounded practical problem-solving sequence.

It does not infer those facts from prose. The evaluator accepts typed state whose uncertainty and evidence references remain visible.

The situation was chosen because it proves the complete procedural chain while remaining narrow enough to expose unsupported assumptions. It exercises foundational communication, user-goal alignment, one-question information gathering, problem definition, user-generated options, a bounded plan, silence while awaiting an outcome, and reassessment.

## Source derivation

Two controlled CT-V2-01 artifacts were inspected. Their local files remain ignored and untracked.

| Governed version | Artifact identity | Exact areas used | Authority limits |
|---|---|---|---|
| WHO/UNICEF, *Foundational helping skills training manual*, 2025 electronic edition with 25 August 2025 corrigendum (`who-fhs-2025`) | 3,177,305 bytes; SHA-256 `b3dde55d3e1a601699a41b4aaaef4d020cc9416fbc6773848396db81df52cbe9` | Verbal communication, publication pp. 27–31 / PDF pp. 38–42; collaborative goal-setting, pp. 118–120 / PDF pp. 129–131; eliciting feedback, pp. 126–127 / PDF pp. 137–138 | Human-helper competency training; clinical, software-autonomy, and commercial-rights review pending. |
| WHO, *Problem management plus (PM+): individual psychological help for adults impaired by distress in communities exposed to adversity*, generic field-trial v1.1, 2018 (`who-pm-plus-v1-1-2018`) | 1,314,251 bytes; SHA-256 `aaa6ce06dacc1058ba8b695b7d8146d33abfdfcf8a13ea7d888f71a01a833cbe` | Helper reluctance, publication pp. 26–27 / PDF pp. 28–29; advice boundary, p. 24 / PDF p. 26; Managing Problems, pp. 46–51 / PDF pp. 48–53; delivery assumptions, pp. 8–12 / PDF pp. 10–14 | Adult adversity-context PM+ with trained, supervised human helpers; not autonomous-software or general-population authority; clinical, implementation, software-autonomy, and commercial-rights review pending. |

The corpus now holds 11 additional exact source-section records. These records add addressability only. No raw source text, intervention script, assessment, case example, or worksheet was copied.

The rules are original Thomas abstractions for controlled architecture qualification. No conclusion is made about whether a future commercial abstraction would be a derivative work. Both artifacts remain `COMMERCIAL_PERMISSION_REQUIRED` under the recorded CC BY-NC-SA 3.0 IGO rights boundary.

## Structured input

`BoundedProblemPolicyState` contains:

- active Thomas mode;
- upstream safety disposition;
- bounded-slice scope disposition;
- epistemically qualified support intent;
- problem statement and clarity;
- shared-understanding status;
- participation/readiness;
- influenceability;
- user-generated options;
- user selection;
- bounded plan;
- reported outcome;
- evidence references and explicit conflict state;
- optional outstanding information and response preference.

`PolicyEvidence<T>` distinguishes established user report, tentative evidence, conflicting evidence, insufficient evidence, unresolved evidence, and unknown. Tentative intent cannot drive a downstream intervention. There is no numeric psychological score.

## Policy graph

```text
typed safety/scope/mode gate
  ├─ unknown or specialized -> typed stop/handoff
  ├─ wrong mode/out of scope -> typed stop
  └─ bounded Therapist slice
       ↓
establish support intent
       ↓
understand one present problem
       ↓
verify shared understanding
       ├─ listening -> reflect
       ├─ understanding -> summarize established content
       └─ practical help
             ↓
          identify influenceable part
             ├─ not influenceable -> leave slice
             └─ influenceable
                   ↓
                confirm readiness
                   ↓
                invite user-generated options
                   ↓
                user chooses an option
                   ↓
                ask for one bounded plan step
                   ↓
                NO_RESPONSE while awaiting outcome
                   ↓
                review reported outcome and reassess
```

The graph has 13 procedural stages, 11 goal concepts, eight dialogue-act concepts, 13 action definitions, and 21 rules. A decision selects at most one action.

## Eligibility, exclusions, and failure states

Every rule carries stable identity, priority, typed preconditions, typed exclusions, result, exact provenance, unresolved reviews, qualification authority, and explicit production denial.

Rules are evaluated deterministically. All matching rules remain visible. The unique highest-priority rule wins. Equal-priority winners produce `POLICY_CONFLICT`; there is no implicit rule-ID fallback. Other terminal results include `INSUFFICIENT_INFORMATION`, `REVIEW_BLOCKED`, `NO_AUTHORIZED_ACTION`, `OUT_OF_SCOPE`, `SPECIALIZED_POLICY_REQUIRED`, and `INVALID_INPUT`.

Source-derived rules without provenance cannot be constructed. A matching `CANDIDATE_RULE` cannot execute and produces `REVIEW_BLOCKED`. The shipped catalog marks all 21 rules `EXECUTABLE_FOR_QUALIFICATION` and all 21 `NOT_GRANTED` for production therapeutic authority.

## Renderer boundary

The evaluator returns a `PolicyDecision`. `PolicyRenderRequestFactory` converts only a selected qualification action into the narrow domain contract:

- policy decision reference;
- selected policy action ID;
- selected dialogue-act ID;
- therapeutic goal ID;
- already-decided realization instruction;
- required, allowed, and prohibited semantic content;
- tone, word, question, advice, and output-form constraints;
- only explicitly authorized supporting text.

The renderer receives no policy catalog, profile, transcript, provenance database, persistence handle, safety authority, or ability to select another act. `DeterministicQualificationRenderer` lives in `:qualification`, switches only on the selected action ID, and needs no model.

## Outcome, reassessment, and profile boundary

Every action has one `OutcomeContract` naming expected next information and ontology concepts to reassess. The policy result contains no durable-profile mutation operation. Structured user reports and Thomas hypotheses remain evidence requiring later governed admission.

## Unresolved areas

- All clinical reviews remain pending.
- WHO commercial-rights review or permission remains pending.
- Human-helper training, supervision, competency, and implementation assumptions are not satisfied by this architecture proof.
- Population and setting transfer to ordinary mobile users is unadjudicated.
- Safety and all specialized policies remain unopened.
- Natural-language perception and formulation remain unopened.
- No production orchestration or renderer is wired.

The slice is therefore evidence-bearing and executable for tests, but not a deployable therapeutic capability.
