# CT-V2-07 governed admission, revision, and longitudinal store

## Status

Implemented for synthetic qualification only. No production longitudinal or therapeutic authority is granted.

## Purpose

CT-V2-07 adds durable mechanics around the CT-V2-06 evidence model. It proves that structured synthetic evidence can be admitted, revised, persisted, reopened, audited, replayed, and reconstructed without allowing an application, renderer, model, mode, or migrated V1 component to mutate longitudinal state.

The write path is:

```text
LongitudinalAdmissionRequest
  -> LongitudinalAdmissionController
  -> deterministic LongitudinalAdmissionPolicy
  -> one SQLite transaction
       -> immutable ledger event
       -> current projection
       -> immutable-object index
       -> lifecycle history
       -> idempotency record
  -> accepted receipt or typed rejection
```

The module graph is:

```text
:thomas:longitudinal-store
  -> :thomas:longitudinal-admission
       -> :thomas:longitudinal

:qualification
  -> :thomas:longitudinal-store
```

`longitudinal` remains a persistence-free domain. `longitudinal-admission` has no SQL or platform dependency and can only plan a mutation. `longitudinal-store` owns the qualification-only SQLite connection, clock, schema, transaction, ledger, projection, receipts, replay, and read boundary. No production module depends on either new module.

## Governing boundaries

- The only public external write operation is `LongitudinalAdmissionController.submit`.
- The controller accepts typed structures only; there is no text extractor, prompt, model, or inference interface.
- Requests cannot contain record time. The store assigns it from an injected trusted clock.
- Accepted receipt implementations and ledger-event representations are private to the store.
- SQL connections, statements, DAOs, mutable rows, and transaction handles do not cross the store boundary.
- Every accepted request produces exactly one immutable ledger event and a projection update in the same transaction.
- Rejection and injected write failure cannot partially mutate domain state.
- User corrections, source revisions, supersessions, identity decisions, and lifecycle changes append history; they do not rewrite it.
- Contradictions coexist. Recency does not select a winner.
- PRIVATE and DECLINED material is ineligible for ordinary derivation. PRIVATE changes deterministically block dependents.
- All data and store instances are classified `SYNTHETIC_QUALIFICATION_ONLY`.

## Supported mechanics

The admission policy supports admitting an immutable source, appending a source revision, atomically admitting a structured evidence bundle, recording an explicit user correction, recording supersession or contradiction, revising an identity decision, changing coverage or privacy, and retiring a claim.

The reader supports current and as-of snapshots, source/revision history, assertion and entity reads, lifecycle history, dependency traversal, correction/contradiction/supersession reads, identity decision history, coverage and privacy eligibility, redacted admission history, and a canonical logical-state digest.

Logical replay uses the accepted request, fixed record time, policy version, and ledger order. It does not use physical row IDs, row order, wall-clock time, a model, or network access.

## Authority and data classification

This adapter is deliberately unsuitable for real psychological information. File-backed databases are accepted only below a Gradle `build` path or the operating-system temporary directory and must use the `.sqlite` suffix. Qualification cleanup removes fixtures. The module is not reachable from `app`, `runtime`, Android persistence, safety, engine, speech, or renderer code and is not packaged in the application.

No claim is made for encryption at rest, Android Keystore, backup, export, deletion, retention, recovery, lost-device handling, or production security. Those remain deferred to CT-V2-14. Safety-state persistence is explicitly excluded.

## Unopened work

CT-V2-08 language-to-evidence formation, natural-language extraction, retrieval, ranking, embeddings, pattern generation, mode behavior, Android storage, production persistence, real-user operation, V1 import, and production authority remain unopened.

Detailed contracts are in:

- [Admission contract](longitudinal/CT-V2-07-ADMISSION-CONTRACT.md)
- [Revision and dependency state machine](longitudinal/CT-V2-07-REVISION-AND-DEPENDENCY-STATE-MACHINE.md)
- [Relational schema](longitudinal/CT-V2-07-RELATIONAL-STORE-SCHEMA.md)
- [Synthetic-store security boundary](security/CT-V2-07-SYNTHETIC-STORE-SECURITY-BOUNDARY.md)
