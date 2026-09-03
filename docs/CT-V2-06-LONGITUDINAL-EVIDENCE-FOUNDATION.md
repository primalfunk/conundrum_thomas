# CT-V2-06 longitudinal evidence foundation

## Purpose and authority

CT-V2-06 defines what it means for Thomas to hold structured evidence about one person's life. It is a qualification-only domain foundation. It does not extract, admit, store, retrieve, interpret, or act on real user information.

The governing product relationship is:

> Biographer backfills the user's life. Journal records the life now being lived. Therapist later draws carefully governed help from both.

Those modes will share one evidence architecture, not one mutable psychological profile. No mode behavior is implemented here.

## Logical layers

```text
Original SourceRecord
  -> EvidenceAssertion
      -> LifeEntity structure
      -> User interpretation
  -> ThomasHypothesis with explicit dependencies
  -> future derived pattern
  -> future bounded retrieval view
```

Only the first four structural layers are defined. A derived structure never substitutes for its `SourceRecord`.

## Pure module boundary

`:thomas:longitudinal` has zero project dependencies and no Android, Room, SQLite, network, LLM, embedding, policy-engine, or production-persistence dependency. No production module consumes it during CT-V2-06.

The module exposes immutable data and deterministic validation. `LongitudinalEvidenceSnapshot` is an evidence aggregate, not a store or mutation interface.

## Source model

`SourceRecord` preserves:

- stable identity;
- acquisition mode;
- optional interaction identity;
- source revision and prior-revision reference;
- report and record time;
- exact inline content or a future-safe content reference;
- validated provenance metadata.

Acquisition modes are `JOURNAL`, `BIOGRAPHER_OPEN_NARRATIVE`, `BIOGRAPHER_GUIDED_TIMELINE`, `THERAPIST_CONVERSATION`, and `USER_CORRECTION`. These are personal-evidence provenance and are intentionally separate from clinical-authority provenance.

## Assertions and epistemic separation

An `EvidenceAssertion` must reference a source. It contains typed subject, predicate semantics, typed value, uncertainty, and event time.

User evidence has two non-interchangeable kinds:

- `EXPLICIT_USER_ASSERTION`: a proposition explicitly reported by the user;
- `USER_INTERPRETATION`: the user's belief, suspicion, evaluation, or interpretation.

`ThomasHypothesis` is a separate Kotlin type. It is not an `EvidenceAssertion`, cannot claim a user source, and must have one or more explicit `HypothesisDependency` records. A direct assertion about a third party's hidden internal state is rejected; it must remain a user interpretation or separately governed Thomas hypothesis.

## Life structure

Typed life entities include `LifeEvent`, `Person`, `Relationship`, `Place`, `LifePeriod`, `Role`, `Decision`, `Behavior`, `CopingResponse`, and `Outcome`. Each structure has stable identity and supporting assertion references. Cross-entity relationships are validated without requiring premature canonicalization.

Two references can remain separate through `EntityIdentityLink` with `UNRESOLVED` or `CANDIDATE_SAME_ENTITY`. Established same/different decisions require evidence, and self-contradictory identity chains are invalid.

## Contradiction, correction, and supersession

- `ContradictionRelation` preserves both assertion sources and an explicit adjudication status. Unresolved is valid.
- `CorrectionRelation` identifies the correction and corrected assertion. The correcting assertion must originate from `USER_CORRECTION`.
- `SupersessionRelation` records refinement, replacement, correction, or invalidation without deleting its predecessor. Cycles are invalid.

A correction marked as superseding requires a matching supersession relation. History therefore remains inspectable even when the current account changes.

## Dependent hypotheses

`HypothesisDependency` identifies the dependent hypothesis, prerequisite assertion or hypothesis, dependency role, and rationale. Missing prerequisites, self-dependency, and dependency cycles are invalid. Later phases can use these links to mark dependent hypotheses for review after evidence changes; CT-V2-06 performs no invalidation behavior.

## Unknown, private, and declined

`InformationCoverageStatus` represents `UNKNOWN`, `NOT_EXPLORED`, `PARTIAL`, `PRIVATE`, `DECLINED`, `IRRELEVANT`, `UNRESOLVED`, and `SUFFICIENTLY_UNDERSTOOD`.

These are legitimate evidence/coverage states. They grant no Biographer retry behavior. In particular, unknown does not mean ask and declined does not mean ask later.

## Turn state versus longitudinal state

CT-V2-05 turn state is ephemeral procedural evidence used to select the next action. CT-V2-06 longitudinal state represents durable evidence about a life. Neither module depends on the other's concrete state type. A later authorized projector may create bounded turn evidence from admitted longitudinal material; no such projection exists now.

## Validation boundary

Deterministic validation rejects missing source links, duplicate identifiers, missing correction targets, invalid correction provenance, invalid ranges, missing entity references, missing hypothesis dependencies, cycles, and contradictory established identity links. Invalid structures are reported; they are never silently normalized.

No serialization was introduced because CT-V2-06 has no persistence or transport requirement.
