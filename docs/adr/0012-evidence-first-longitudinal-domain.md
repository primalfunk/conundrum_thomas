# ADR 0012: Evidence-first longitudinal domain

**Status:** Accepted for CT-V2-06 qualification

## Context

Journal, Biographer, and Therapist will eventually contribute to or consume understanding of one person's life. A mutable profile would erase source, time, disagreement, and epistemic boundaries.

## Decision

Create a dependency-free `:thomas:longitudinal` module organized around immutable personal source records, user-derived assertions, separately typed Thomas hypotheses, life entities, explicit evidence relationships, and validation.

Event time belongs to an assertion and remains independent from the source's report and record times. Contemporary Journal and retrospective Biographer sources remain distinct even when linked to the same event.

Contradiction preserves both assertions. Correction preserves both assertions and requires correction provenance. Supersession is an explicit acyclic relationship. Hypotheses are not assertions and require inspectable dependencies. Entity identity may remain unresolved or merely candidate-level.

The aggregate is a snapshot of typed evidence, not a writer, store, graph database, profile, admission engine, or retrieval system.

## Consequences

- Present Journal evidence naturally becomes historical without provenance migration.
- Original user communication remains the anchor for every derived assertion.
- Later admission and persistence work has a strong domain target but no authority is granted early.
- Turn-policy state remains separate from longitudinal evidence.
