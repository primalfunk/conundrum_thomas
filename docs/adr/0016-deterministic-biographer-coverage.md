# ADR 0016: Deterministic Biographer coverage and one-question authority

- Status: Accepted for CT-V2-10 qualification
- Date: 2026-09-03

## Decision

Add pure Kotlin `:thomas:biographer`. It consumes persistence-neutral structured coverage evidence and immutable operational history, deterministically selects at most one target, and emits a constrained semantic question plan. Its answer engine commits source first through abstract ports.

Only `:qualification` composes the module with the CT-V2-07 store and CT-V2-08 language pipeline. Investigation history remains distinct from personal evidence. Private/declined areas are automatic exclusions; unchanged offered targets cannot loop; `NO_TARGET` is valid.

## Consequences

The application, runtime, Therapy, Journal response policy, renderer implementation, models, and V1 remain unwired. No production storage or Biographer authority is granted. Fluent wording, Android integration, retrieval, and Therapist use of history require later authorization.
