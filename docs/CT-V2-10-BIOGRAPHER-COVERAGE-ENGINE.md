# CT-V2-10 — Biographer Coverage Engine

## Disposition and purpose

CT-V2-10 adds a synthetic-only deterministic Biographer that investigates structurally incomplete or unresolved history. It does not formulate, diagnose, retrieve conversational memories, choose Therapy, or produce Journal responses. Production longitudinal authority remains zero.

The two postures are:

- `OPEN_STORY`: authorize one neutral historical invitation with no hidden target.
- `TARGETED_COVERAGE`: select at most one eligible structural target and authorize one bounded question intent.

## Architecture

`:thomas:biographer` is pure Kotlin and depends only on `:thomas:language-evidence` and its transitive governed contracts. It owns immutable coverage evidence, target types, target eligibility, ranking, operational investigation history, question plans, answer-capture contracts, and source-first orchestration. It contains no database implementation.

The complete path exists only in `:qualification`:

```text
CT-V2-07/08 longitudinal state
  -> qualification coverage projection
  -> :thomas:biographer coverage engine
  -> one semantic question plan
  -> synthetic answer command
  -> CT-V2-07 AdmitSource
  -> CT-V2-08 perception/state formation
  -> recomputed coverage
```

Every durable user-evidence write still crosses the CT-V2-07 admission controller. Investigation history is a separate immutable operational value and is never asserted as personal evidence.

## Coverage semantics

Coverage describes represented history, not a percentage of a person. The map exposes represented and sparse periods, roles, places, relationships, unresolved temporal structure, identities, contradictions, corrections, open questions, private/declined/deferred states, investigation history, eligible targets, one selected target, versions, and a canonical digest.

`UNKNOWN`, `SPARSE`, `UNRESOLVED`, `PRIVATE`, and `DECLINED` never mean that an event did not happen. Private and declined targets are hard automatic exclusions. `NO_TARGET` is a successful result.

## Agency and progression

`ANSWER`, `SKIP`, `DEFER`, `DECLINE`, `PRIVATE`, `CHANGE_TOPIC`, and `STOP` are typed outcomes. An offered target cannot be selected again against the same material-change token. Re-entry requires explicit user reopening or materially changed governing evidence. There is no randomized question variation or keep-asking loop.

## Provenance and correction

Open answers use `BIOGRAPHER_OPEN_NARRATIVE`; targeted answers use `BIOGRAPHER_GUIDED_TIMELINE`. Typed and speech-transcript origins remain metadata; no audio path exists. Event, report, and record time remain independent.

An explicit correction answer remains a Biographer source. Promotion as an authoritative correction is a distinct CT-V2-07 `RecordUserCorrection` operation with `USER_CORRECTION` provenance, preserving both the interview answer and correction history. Identity decisions use `ReviseIdentityLink` and never physically merge source entities.

## Authority exclusions

The engine has no Android, Room, renderer implementation, speech implementation, network, model, Journal, Therapy, safety implementation, or V1 dependency. A structured higher-authority safety/scope block can return `NO_TARGET`. It adds no safety procedure.

No production Biographer writer, Android writer, direct SQL/JDBC writer, model writer, question-to-evidence path, Biographer-to-Therapy route, or Biographer-to-Journal response route is authorized.
