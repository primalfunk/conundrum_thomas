# Conundrum Thomas V2

## Procedural Therapy Architecture & Implementation Plan

**Status:** Governing founding architecture

**Generation:** Conundrum Thomas V2

**Primary target:** Mobile-first, local-capable psychological-support system

**Core architectural decision:** Therapeutic reasoning belongs to Thomas. The language model provides conversational expression.

> **THOMAS SELECTS AND GOVERNS THERAPEUTIC BEHAVIOR.**
>
> **THE MODEL ONLY RENDERS AUTHORIZED BEHAVIOR INTO LANGUAGE.**

Architecture wins over implementation convenience and over reuse of V1 assets.

## 1. V2 thesis

Conundrum Thomas V2 does not treat a language model as the therapeutic agent. Thomas itself is the therapeutic agent.

The system explicitly implements perception of the user's communication, structured psychological formulation, uncertainty, therapeutic procedure, intervention selection, conversational sequencing, safety and scope boundaries, longitudinal understanding, response evaluation, and escalation or deferral.

A language model is used principally to convert structured communicative intent into natural human language.

> The LLM does not decide what Thomas should do. It helps Thomas say what Thomas has already decided to do.

## 2. Product objective

Thomas is not intended to reproduce the unrestricted capabilities of a human psychotherapist. The initial objective is narrower:

> Build a psychologically informed conversational system that performs a useful, safe, well-defined subset of therapeutic and supportive interactions extremely consistently, recognizes when it lacks sufficient understanding or authority, and declines, clarifies, escalates, or refers rather than improvising beyond its competence.

Success requires strong behavior inside defined competence, reliable recognition of uncertainty, conservative behavior outside competence, predictable safety boundaries, and sufficiently natural conversation that procedural structure does not feel mechanical.

## 3. Fundamental design principles

### 3.1 Procedure before generation

Whenever therapeutic behavior can be represented explicitly, it will be represented explicitly. The preferred path is:

`state -> rule -> therapeutic act -> language`

not:

`conversation -> LLM -> answer`

### 3.2 Structured uncertainty

Thomas must be allowed not to know. Uncertain interpretations remain uncertain. Low-confidence states generally cause Thomas to clarify, reflect tentatively, gather more information, avoid strong interpretations, and avoid premature advice. Thomas must not invent coherence merely because a response is expected.

### 3.3 One therapeutic move at a time

Thomas should normally select a small number of intentional dialogue acts per turn, such as `REFLECT_EMOTION` plus `ASK_SINGLE_OPEN_QUESTION`, rather than letting a generator independently add reflection, diagnosis, advice, reassurance, multiple questions, and an exercise.

### 3.4 Intervention follows formulation

Thomas should generally understand before attempting to change. The system distinguishes information gathering, emotional acknowledgment, formulation, intervention, intervention evaluation, and closure. A keyword alone does not justify advice or technique.

### 3.5 Safety is independent authority

The therapeutic policy engine does not have final authority. A separate Safety and Scope Governor may allow, constrain, suppress, replace, clarify, defer, encourage human support, or urgently escalate a proposed action. A generative model cannot override this layer, and safety logic cannot exist solely as an LLM prompt.

### 3.6 Source-grounded therapeutic behavior

Procedural behavior will be derived from identifiable clinical and psychological authorities rather than ad hoc prompting. Initial authority families may include WHO/UNICEF EQUIP, WHO Foundational Helping Skills, WHO Problem Management Plus and psychological self-help interventions, WHO mhGAP, NICE clinical guidelines, structured CBT resources, Motivational Interviewing resources, relevant suicide/self-harm safety frameworks, and later selected specialty guidance.

Every material therapeutic rule requires provenance. Licensing and adaptation restrictions must be tracked from the beginning.

### 3.7 Test behavior, not vibes

Qualification asks deterministic questions: what Thomas observed; how confident he was; which state was active; which actions were eligible or prohibited; why one was selected; whether safety intervened; whether the renderer preserved the authorized intent; and whether the next user response indicates that the intervention landed appropriately. Conversational quality remains a test surface, not the definition of correctness.

## 4. V2 system architecture

The governed pipeline is:

`User Input -> Perception -> Working Formulation -> Therapeutic Policy -> Safety and Scope -> Intervention -> Conversational Renderer -> Output Validator -> User -> Outcome Observer -> updated state`

### A. Perception Engine

Produces structured observations from the current utterance and authorized recent context, including expressed emotion and intensity, explicit request, problem domain, people, temporal references, behavior, beliefs, goals, uncertainty, change talk, contradiction, avoidance, possible cognitive patterns, possible safety signals, and response to Thomas's previous intervention. It reports observations; it does not decide therapeutic meaning or action.

### B. Working Formulation Engine

Maintains mutable, evidence-linked, confidence-bearing, auditable, and revisable hypotheses. Competing interpretations and an explicit unknown state are first-class. Thomas must be able to reverse an earlier interpretation and must not silently convert a hypothesis into fact.

### C. Therapeutic Policy Engine

Determines the appropriate class of behavior from structured state. Candidate classes include listening, clarification, reflection, summary, exploration, validation, normalization, pattern identification, discrepancy development, thought examination, problem solving, distress regulation, behavioral activation, values exploration, planning, progress review, rupture repair, closure, deferral, and escalation.

The policy engine does not accept raw user prose as its decision interface.

### D. Safety and Scope Governor

Independently evaluates immediate safety, self-harm or suicide signals, violence, abuse or coercion, possible medical emergency, severe impairment, relevant psychosis or mania indicators, age and dependency issues, human-evaluation boundaries, interventions outside Thomas's authority, excessive certainty, and prohibited diagnostic or prescriptive behavior.

Its dispositions include `ALLOW`, `ALLOW_WITH_CONSTRAINTS`, `REPLACE_ACTION`, `CLARIFY_FIRST`, `DEFER`, `ENCOURAGE_HUMAN_SUPPORT`, and `URGENT_ESCALATION`. Ordinary policy cannot bypass it.

CT-V2-04 provides the first qualification-only implementation of this boundary. It evaluates typed established facts, not raw text or predicted risk, and issues a revision-bound `OrdinaryTherapyPermit` only for `ORDINARY_POLICY_ALLOWED`. Every other disposition withholds the capability. Specialized and emergency procedures remain unopened; no production safety authority is granted.

CT-V2-05 proves that the permit can govern a broader ordinary repertoire. The ordinary engine hierarchically selects listening/support, understanding/clarification, bounded practical problem solving, or consolidation/close from typed preference and state, then selects one sourced action. It records action/evidence revision, honors correction, and stops rather than repeating against unchanged evidence. This remains qualification-only and is not wired to runtime.

### E. Intervention Engine

Converts the governed therapeutic strategy into a precise communicative instruction and explicit constraints. The result states what acts to express and what is forbidden, including advice, diagnosis, reassurance, question count, interpretation count, certainty, and target length.

### F. Conversational Renderer

The renderer receives only a `RenderCommand` and explicitly authorized supporting evidence or text. It may vary wording, rhythm, contractions, warmth, sentence structure, and transitions. It may not vary therapeutic objective, safety classification, intervention, prohibited behavior, factual claims, or certainty. It has no unrestricted transcript, profile, provenance, or database access and no therapeutic authority.

### G. Output Validator

Checks required dialogue acts, forbidden acts, unsupported diagnosis, invented facts, question count, verbosity, unwanted advice, inappropriate certainty, safety-language compliance, response length, and repetition. A failed rendering is regenerated under stricter constraints or replaced by a governed deterministic fallback.

### H. Outcome Observer

The next user response provides evidence about the previous intervention. Outcomes may include `ENGAGED`, `DISCLOSED_MORE`, `CORRECTED_THOMAS`, `REJECTED_INTERPRETATION`, `BECAME_DEFENSIVE`, `REQUESTED_ADVICE`, `DISTRESS_INCREASED`, `DISTRESS_DECREASED`, `CHANGED_TOPIC`, `RUPTURE`, and `UNCLEAR`. Outcome evidence updates formulation and policy, making Thomas a closed-loop system rather than a sequence of unrelated answers.

## 5. Major therapeutic subsystems

Procedural families are implemented separately and admitted incrementally, not blended indiscriminately:

- Foundational helping: active listening, emotional reflection, validation, collaboration, open questions, summaries, hope, respect, autonomy, and avoidance of harmful helping behavior.
- Supportive exploration: immediate concern, event/interpretation/emotion/behavior differentiation, meaning, unmet needs, relational exploration, and patterns.
- Problem solving: define the problem, determine controllability, identify an objective, generate and evaluate options, choose a manageable action, and follow up.
- CBT operators: automatic thoughts, thought/emotion relationships, evidence examination, thinking patterns, alternative interpretations, balanced appraisal, behavioral experiments, and behavioral activation.
- Motivational operators: ambivalence, importance, confidence, values, change talk, sustain talk without argument, discrepancy, autonomy, and readiness-based planning.
- Regulation and coping: grounding, acute-arousal reduction, attentional redirection, structured coping, stress management, and activity selection.
- Longitudinal work: recurring themes, unresolved concerns, progress, repeated triggers, changed beliefs, emerging patterns, and prior strategy outcomes.

The foundation implemented none of these capabilities. CT-V2-05 now implements only a qualification slice of foundational listening, bounded understanding, and PM+ problem solving; the remaining families stay unopened.

## 6. Relationship to Thomas modes

Journal remains user-led and supports no response, minimal acknowledgment, or conversational reflection; it generally avoids unsolicited therapeutic intervention. Therapy activates the governed V2 procedural stack and may choose silence or minimal response. Biographer remains primarily information acquisition; it may share perception, durable profile, provenance, contradiction, and uncertainty structures, but does not inherit Therapy's intervention authority. The modes may share cognition while retaining distinct behavioral policy.

## 7. Durable state model

V2 separates literal transcript, extracted observations, working formulation, durable profile, therapeutic history, open questions, safety state, and session state. This separation prevents inference from silently becoming historical fact. No production psychological or user state is persisted during CT-V2-00.

## 8. Provenance architecture

Every future therapeutic rule must be representable with a stable rule identifier, authority, source location and version, license, clinical principle, Thomas-native abstraction, applicability, contraindications, required state, resulting action, confidence, review status, tests, and implementation version.

The accepted storage direction is a versioned relational provenance schema with migrations, reviewable seed records, and a generated runtime database. Clinical literature is not copied into prompts. Raw source documents remain outside runtime artifacts and outside Git unless redistribution rights are explicitly established. CT-V2-00 creates structure only; CT-V2-01 records are not admitted.

## 9. Implementation program

The numbered list below is the founding program. Subsequent Principal work orders control actual phase content: CT-V2-04 was authorized as the Safety & Scope Gate and CT-V2-05 as the Core Ordinary Therapeutic Repertoire. Those sealed work orders supersede the founding placeholder labels without changing the governing architecture.

### CT-V2-00 — Architecture foundation and V1 preservation

Seal the untouched V2 origin, record exact V1 references, preserve V1 by non-modification, establish governing documents and authority boundaries, inventory reuse candidates under deny-by-default migration governance, and retire autonomous model authority. Exit requires a clean V2 baseline with no accidental loss or migration.

### CT-V2-01 — Therapeutic source and provenance corpus

Build the controlled source library and provenance database, emphasizing foundational helping, general support, problem solving, digital self-help, assessment and routing, CBT, motivational methods, and crisis or safety boundaries. Each source requires authority, version, scope, license, concepts, implementation suitability, and restrictions.

### CT-V2-02 — Thomas therapeutic ontology

Define the finite canonical vocabulary for observations, states, goals, dialogue acts, therapeutic interventions, contraindications, outcomes, and scope states. A human must be able to describe Thomas's behavior without referring to an LLM.

### CT-V2-03 — Minimal procedural vertical slice

Prove a deliberately small domain: ordinary distress involving a manageable life problem with no safety complication. Initially support listening, reflection, clarification, collaborative problem definition, simple problem solving, and follow-up. Compare a V1 autonomous model with procedural Thomas plus the same or smaller renderer on matched conversations. Proceed only if the procedural design is materially more varied, coherent, intentional, and inspectable.

The authorized CT-V2-03 work order narrows this milestone to deterministic architecture qualification with structured input and a non-LLM renderer stub. No V1 model or implementation comparison is authorized in this phase. The ruleset remains production-denied pending later review and admission.

### CT-V2-04 — Perception Engine

Develop uncertainty-bearing structured inference for measurable signals such as explicit questions, advice requests, emotion, topic, people, event, time, goal, negation, disagreement, uncertainty, and response to Thomas. Rules, lexicons, parsers, classifiers, compact models, and justified LLM extraction remain implementation options; measured accuracy and cost decide.

### CT-V2-05 — Working Formulation Engine

Implement evidence-linked hypothesis creation, confidence, competing interpretations, unresolved questions, revision, contradiction, rejection, user correction, and explicit unknown state across extended conversations.

### CT-V2-06 — Therapeutic Policy Engine

Use a hierarchical controller: interaction family, stage, eligible actions, contraindications, ranking, and selection. It may combine hard rules, decision tables, state machines, scoring, confidence, and limited learned classification while remaining reproducible and explainable.

### CT-V2-07 — Safety and Scope Governor

Build independently from ordinary policy, including ambiguity handling, distress escalation, self-harm and suicide response, emergency boundaries, severe-symptom boundaries, abuse and coercion, professional or medical referral, refusal of inappropriate authority, and uncertainty escalation. Every pathway needs adversarial tests. Cost asymmetry may warrant false deferral over dangerous improvisation.

### CT-V2-08 — Intervention Library

Implement bounded modules with admission criteria, contraindications, stages, state, permissible acts, completion and abandonment criteria, outcome assessment, and follow-up. Suggested order: foundational helping, supportive exploration, simple problem solving, behavioral activation, basic CBT thought examination, motivational interviewing, and stress management.

### CT-V2-09 — Conversational Renderer Program

Select the smallest renderer that faithfully realizes Thomas's authorized act. Evaluate instruction fidelity, naturalness, warmth, repetition, unsupported additions, forbidden-act violations, latency, memory, power, and mobile resources against deterministic and hybrid templates. Model size is not a proxy for therapeutic intelligence.

### CT-V2-10 — Longitudinal Thomas Integration

Integrate durable recurring stressors, successful coping strategies, unresolved themes, preferences, values, interpersonal patterns, goals, exercises, and corrections while distinguishing direct fact, user belief, Thomas observation, Thomas hypothesis, and resolved historical pattern.

### CT-V2-11 — Qualification Laboratory

Build governed scenarios covering straightforward cases, ambiguity, conflict, incomplete information, misleading keywords, inappropriate advice requests, resistance, rupture, correction, intervention failure, repeated topics, safety edges, adversarial prompts, and renderer failures. Define expected observations, formulation, eligible and prohibited actions, intervention range, and safety disposition for independent layer evaluation.

### CT-V2-12 — Android V2 Experience

Finalize the mobile interface only after the therapeutic core behaves correctly. Expose `Journal | Therapy | Biographer`, appropriate response preferences, distinct done-speaking and cancel behavior, and understandable waiting, thinking, silence, or escalation without unnecessarily exposing clinical machinery.

### CT-V2-13 — Human Review and Bounded Release

Require specialist review of therapeutic rules and safety pathways, structured usability testing, longitudinal pilot, failure review, false- and missed-escalation analysis, renderer regression, privacy review, and product-claims review. Public descriptions must match demonstrated capability.

## 10. Qualification philosophy

Every layer is qualified independently. A final response may fail because rendering failed after perception, formulation, policy, safety, and intervention construction passed; or perception may fail and every downstream layer becomes not evaluated. This supports causal debugging of therapeutic behavior.

## 11. Renderer failure strategy

The fallback hierarchy is normal LLM rendering, regenerated rendering with stricter constraints, hybrid deterministic template, and fully deterministic safe response. Model failure may reduce eloquence without destroying therapeutic correctness.

## 12. Consequence for model development

V1 asked how capable a therapist could fit on-device. V2 asks how small a model can faithfully verbalize the decisions of a capable procedural Thomas. Specialization therefore emphasizes instruction fidelity, concise conversational language, natural emotional reflection, variation without semantic drift, style continuity, and accurate realization of dialogue acts. Model capacity is not spent recreating facts and procedures already represented explicitly.

## 13. Near-term scope

The intended first scope is ordinary adult psychological distress involving work, relationships, decisions, worry, discouragement, self-criticism, manageable behavioral problems, mild-to-moderate dysregulation, motivation and ambivalence, and recurring non-emergency life problems. Complexity is added only with suitable authority, procedural implementation, qualification, safety boundaries, and demonstrated performance.

## 14. Definition of success

Thomas should feel conversationally human while remaining substantially more controlled than an ordinary chatbot: careful listening, memory, uncertainty, useful questions, minimal canned language, clear explanation, longitudinal patterns, correction, appropriate limits, and no pretended certainty.

The inspectable loop is:

`observe -> formulate -> choose -> govern -> intervene -> express -> observe outcome -> revise`

## 15. Immediate development decision

The autonomous Therapist-model architecture is not the V2 target. Existing models and training artifacts remain possible research inputs for later perception, classification, formulation assistance, rendering, or validation, but no generative model has unrestricted therapeutic authority.

The first objective is CT-V2-00 through CT-V2-03: preserve the platform by reference, establish evidence/provenance and ontology, then prove one procedural vertical slice. CT-V2-01 remains held until Principal authorization.

## 16. Governing maxim

> **Thomas decides what to do. The model helps him say it.**

This principle must survive every subsequent implementation decision.
