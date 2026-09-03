# CT-V2-02 — Therapeutic ontology

**Release:** `ct-v2-02-ontology` version `1.0.0`

**Scope:** Typed vocabulary and provenance bindings only

**Runtime-authorized concepts:** 0

## Philosophy

CT-V2-02 defines the nouns and verbs that later procedural policy may use. It does not define a transition from observation to action. Concepts are explicit, typed, inspectable without a language model, versioned by stable identifiers, and either architecturally defined or visibly candidate/unopened.

The governing distinction is:

> A concept can be defined without being authorized for runtime use.

Every catalog concept and source binding is `NOT_AUTHORIZED`. Intervention and safety concepts are candidates. The production engine, safety, runtime, app, renderer, persistence, and speech modules do not depend on `:thomas:ontology` in CT-V2-02.

## Concept families

The catalog contains 109 concepts in seven composable families:

| Family | Count | Meaning |
|---|---:|---|
| Observed/user state | 19 | Structured dimensions that may later describe what appears to be occurring. |
| Therapeutic goals | 24 | Possible purposes of a future turn; a goal is not an intervention. |
| Dialogue acts | 20 | Classes of conversational expression, including first-class silence. |
| Intervention families | 12 | High-level candidates with mandatory governance gates and no procedure. |
| Constraints | 13 | Vocabulary for prerequisites, scope, contraindications, reviews, rights, and exact source versions. |
| Safety contexts | 7 | Candidate safety-relevant contexts with no score, rank, keyword mapping, or response. |
| Procedural vocabulary | 14 | Generic grammar for a future policy engine: observation, state, goal, candidate, eligibility, exclusion, selection result, outcome, reassessment, termination, and handoff. |

Stable identifiers are lowercase and namespaced, such as `observation.stated-concern`, `goal.understand`, `dialogue.no-response`, and `intervention.problem-solving`. Unknown future identifiers can be preserved as unrecognized rather than reinterpreted.

## Observed/user-state vocabulary

The observation family covers stated concern, conversational intent, emotional content, subjective distress, activation, uncertainty, perceived agency, cognitive load, readiness to explore, readiness to act, ambivalence, problem clarity, immediate practical pressure, interpersonal context, recurrence/chronicity, recent change, requested response level, safety-relevant observation, and missing information.

These are possible dimensions, not detectors or classifiers. They do not contain thresholds or numerical psychological scores. Their epistemic status is carried separately.

## Epistemic model

`EpistemicRecord` preserves five independent concerns:

1. The ontology concept being described.
2. Evidence kind.
3. Resolution state.
4. Non-numeric epistemic strength.
5. Evidence, contradiction, and supersession references.

Evidence kinds distinguish direct user statement, current-interaction observation, historical user report, external fact, derived structured fact, Thomas hypothesis, contradiction, unresolved material, and unknown material.

Resolution explicitly supports reported/resolved, tentative, conflicting, insufficient evidence, unresolved, and unknown. An unknown record must remain valueless and explicitly unknown. A contradiction must identify conflicting evidence. A Thomas hypothesis cannot be marked as user-established fact.

Every record has `GOVERNED_ADMISSION_REQUIRED`. There is deliberately no conversion function from evidence or hypothesis into a durable profile fact.

## Goals, dialogue acts, and interventions

Goals describe possible future purposes, such as understanding, clarification, shared understanding, expression, orientation, agency, exploration, problem solving, planning, checking understanding, outside support, and pausing. They select nothing.

Dialogue acts describe expression classes. `NO_RESPONSE` is a first-class act. Safety inquiry, escalation, exercise guidance, planning, and external-support acts are merely names; their presence does not permit their use.

All 12 intervention families are `CANDIDATE` and require future policy authorization plus appropriate clinical, rights, and implementation review. Safety-related families add software-autonomy or legal gates. No steps, scripts, exercises, admission criteria, contraindications, or selection rules were imported.

## Constraint grammar

`ConceptConstraintLink` can represent:

- prerequisite;
- population and setting constraints;
- deliverer assumption;
- contraindication concept;
- information or safety requirement;
- autonomy restriction;
- clinical, rights, implementation-scope, or legal review requirement;
- exact source-version dependency.

Targets can be another concept, a governed review requirement, an exact source version, or a declared scope. The CT-V2-02 catalog contains zero applied constraint links because applying those links to behavior would require later adjudication.

## Safety boundary

The safety vocabulary contains candidate concepts for ordinary distress context, safety-relevant disclosure, insufficient safety information, direct-clarification requirement, outside-support consideration, urgent external-intervention consideration, and emergency context.

These are not mutually exclusive risk bands. They define no low/medium/high stratification, prediction, keyword mapping, diagnostic conclusion, threshold, or escalation. The CT-V2-01 boundary between NIMH screening and NICE negative guidance remains recorded as different scope and unresolved for software design.

## Source provenance

Thirteen concepts have structured bindings to exact CT-V2-01 documents, versions, sections, locators, identity-review states, rights states, pending review requirements, conflicts, and scope limitations. Bindings cover candidate aspects of foundational listening, problem solving, structured self-help, motivational approaches, CCI-listed cognitive/behavioral/emotion resource domains, safety planning, outside support, safety disclosure, and safety-information boundaries.

Every binding is `PROVENANCE_ONLY`. All linked clinical and rights reviews remain pending. The following remain `SOURCE_REVIEW_NEEDED` without a binding: coping planning, grounding/orientation, interpersonal exploration, ordinary-distress context, urgent external-intervention consideration, and emergency context. This is an explicit source need, not permission to fill the gap from model knowledge.

## Examples

```text
observation.interpersonal-context
  evidence kind: THOMAS_HYPOTHESIS
  resolution: TENTATIVE
  durable profile admission: GOVERNED_ADMISSION_REQUIRED
```

```text
intervention.problem-solving
  definition status: CANDIDATE
  source: PM+ individual v1.1, exact governed section
  clinical review: PENDING
  rights review: PENDING
  runtime authorization: NOT_AUTHORIZED
```

```text
mode.journal
  primary function: CAPTURE
  default response: NO_RESPONSE
```

## Explicitly unopened

CT-V2-02 leaves unopened all therapeutic transitions, eligibility determinations, exclusions, scoring, clinical formulation, intervention stages, safety decisions, escalation logic, mode procedures, profile admission, renderer invocation, and production persistence. CT-V2-03 is not opened by this document.
