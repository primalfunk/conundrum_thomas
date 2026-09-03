# ADR 0013: governed longitudinal admission and synthetic store

**Status:** Accepted for CT-V2-07 qualification

## Decision

CT-V2-07 separates longitudinal mechanics into three pure JVM modules:

```text
:thomas:longitudinal-store
    -> :thomas:longitudinal-admission
        -> :thomas:longitudinal
```

The domain remains persistence-free. Admission defines structured requests, deterministic policy, authority and lifecycle semantics. The store owns the only governed write façade, SQLite implementation, schema migration, append-only ledger, current projection, store clock, transactions, replay, and read boundary. Only `:qualification` may depend on the complete store.

Every accepted operation appends one immutable ledger event and updates its projection in the same transaction. Idempotency uses a logical request fingerprint; optimistic concurrency uses the expected store revision. Record time comes only from an injected store clock. Accepted-receipt and ledger-event implementations are not publicly constructible.

The adapter is structurally classified `SYNTHETIC_QUALIFICATION_ONLY`. It accepts only explicitly synthetic requests and paths under Gradle build output or the operating-system temporary directory. It is not an Android store and is not packaged with the application.

Privacy transitions immediately remove affected material and its derived dependents from ordinary eligibility. Dependency consequences mark derived material for review or block it; they do not decide whether a psychological hypothesis is true or false.

## Deferred

Production encryption, Android Keystore, Android persistence, backup, export, irreversible deletion, retention, recovery, safety-state storage, natural-language extraction, retrieval, mode wiring, and all production authority remain deferred. No LLM or model interface is admitted.

## Consequences

- Application and runtime code cannot obtain a store, DAO, SQL connection, transaction, or accepted receipt.
- Current state is rebuildable from accepted events and comparable through a canonical logical digest.
- Original evidence, revisions, corrections, contradictions, supersessions and identity decisions remain historically inspectable.
- Qualification proves storage mechanics with synthetic fixtures only; it makes no security-lifecycle or production-use claim.
