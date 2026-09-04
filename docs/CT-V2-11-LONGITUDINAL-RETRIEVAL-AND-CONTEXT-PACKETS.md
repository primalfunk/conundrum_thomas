# CT-V2-11 — Longitudinal Retrieval & Context Packets

## Disposition and scope

CT-V2-11 adds a synthetic-only, deterministic, read-only path from governed longitudinal history to an immutable, bounded `ContextPacket`. It grants no production retrieval, model, Android, longitudinal-write, therapeutic-route, Journal-recall, or Biographer-target authority.

The governing rule is: **the archive is not the context**. Historical objects are absent by default. Every selected item must be eligible, relevant to a typed purpose, permitted by the active mode, epistemically complete enough to present honestly, and within an explicit budget.

## Architecture

```text
:thomas:longitudinal
        ^
        |
:thomas:retrieval                 (pure JVM, read-only)
        ^
        |
:thomas:context-packet            (pure JVM, immutable packet)
        ^
        |
:qualification -> CT-V2-07 reader (the only composition root)
```

`:thomas:retrieval` owns the persistence-neutral read port, typed requests, intent/mode policies, eligibility filtering, structural and lexical ranking, direct evidence-neighborhood completion, source-span checks, and selection reason codes. `:thomas:context-packet` owns packet layers, budgets, source-data authority labels, truncation safeguards, and canonical packet digests.

The qualification adapter receives only `LongitudinalReader`; it receives no admission controller, SQL connection, DAO, transaction, or mutable projection. Retrieval and packet construction therefore have zero write path by construction.

## Ordered pipeline

```text
governed snapshot at an explicit revision
  -> lifecycle/privacy eligibility
  -> intent- and mode-bounded candidates
  -> deterministic relevance tuple
  -> direct support/counterevidence neighborhood
  -> exact source-span validation
  -> count/text/depth budget
  -> typed ContextPacket + SHA-256 logical digest
```

Corrections, source revisions, privacy, retirement, review-required state, dependency blocks, and identity decisions are read at the requested store revision. No cache exists. Consequently a new revision immediately changes retrieval without manual invalidation.

## Explicit non-authority

- Journal ordinary context is empty; only explicit `EXPLICIT_LOOK_BACK` may retrieve prior eligible Journal material.
- Biographer retrieval requires a preselected CT-V2-10 target and cannot choose or alter it.
- Therapy retrieval is qualification-only and cannot select a CT-V2-05 route or technique.
- Historical user text is typed `USER_SOURCE_EXCERPT`, never an instruction.
- Assistant immediate-context text is continuity data, never longitudinal evidence.
- No prompt string, model, embedding, vector index, semantic reranker, Android wiring, or packet persistence is present.

See the detailed contracts under [`retrieval/`](retrieval/) and the qualification record in [`qualification/CT-V2-11-QUALIFICATION.md`](qualification/CT-V2-11-QUALIFICATION.md).
