# CT-V2-12 — Longitudinal Therapist Integration

## Purpose

CT-V2-12 joins the already-qualified ordinary Therapy policy and longitudinal retrieval authorities without merging them. The invariant is:

> Memory may inform an authorized therapeutic move. Memory may not choose the therapeutic move.

This phase remains synthetic-only. It produces typed plans and renderer-support data, not final conversational prose, Android wiring, real-user persistence, a model integration, a new therapeutic technique, or production therapeutic authority.

## Ordered authority boundary

```text
pre-turn longitudinal revision
  -> current user source admission
  -> current-turn safety/scope decision
  -> CT-V2-05 route and action decision
  -> CT-V2-11 Therapy packet at the pre-turn revision
  -> deterministic memory-use gate
  -> immutable LongitudinalTherapyPlan
  -> narrow render-support envelope
```

The route evaluator receives only the existing structured Therapy state and the revision-bound `OrdinaryTherapyPermit`. It never receives a retrieval request, archive, packet, memory candidate, excerpt, or session memory-reference history. Retrieval is not called until the route/action decision is complete.

The current user turn is immediate conversation for its own response. Although its exact text is first admitted as `THERAPIST_CONVERSATION` evidence, same-turn retrieval is pinned to the pre-admission revision. This makes same-turn self-history retrieval structurally impossible.

## Degradation

Safety preemption stops ordinary policy and ordinary Therapy retrieval. Source-capture or language-processing failure is reported truthfully while the valid current-turn safety and base Therapy policy remain available. Retrieval failure, invalid packets, empty packets, and rejected memory use all reduce to a valid memoryless Therapy plan when CT-V2-05 selected an action.

## Memory use

Ordinary automatic surfacing requires a direct structural relation: resolved entity, event, relationship, explicitly qualified recurrence, or explicit user reference. Lexical-only, temporal-only, emotion-only, generic-topic, unresolved-identity, private, stale, unbalanced, or historical-hypothesis material is not surfaced automatically. Ordinary responses expose at most one historical reference; zero is normal.

Explicit recall and explanation are separately typed. Explanation uses CT-V2-11's balanced evidence neighborhood and preserves lifecycle, correction, counterevidence, provenance, identity uncertainty, and temporal scope. Retrieval and explanation never change the evidence or hypothesis being explained.

## Session boundary

Surfaced-memory history is ephemeral operational state, not psychological evidence. An automatically surfaced object is suppressed for the remainder of the session unless the user explicitly recalls it or materially changed evidence gives it a new meaning. A rejected tentative connection is also suppressed. Directly continued discussion is immediate continuity, not a repeated historical-memory display.

See the [memory influence matrix](therapy/CT-V2-12-MEMORY-INFLUENCE-MATRIX.md), the detailed Therapy contracts under `docs/therapy/`, and the [qualification record](qualification/CT-V2-12-QUALIFICATION.md).
