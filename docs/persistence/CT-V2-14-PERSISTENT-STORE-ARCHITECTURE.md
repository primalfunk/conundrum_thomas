# CT-V2-14 Persistent Store Architecture

## Boundaries

`:thomas:personal-data-persistence` is a pure-Kotlin implementation of the shared `LongitudinalAdmissionController` and `LongitudinalReader` ports. It depends on `:thomas:longitudinal-admission`; the longitudinal domain remains unaware of persistence. `:thomas:longitudinal-store` keeps SQLite/JDBC confined to synthetic qualification. `:platform:persistence-android` owns only Android key and atomic-file adapters.

The Android application does not consume the platform adapter in CT-V2-14. This is intentional: the persistence boundary is qualified before real-user composition.

## Logical store

Schema version 2 contains:

- a checkpointed longitudinal aggregate;
- content-free archived event headers before a deletion checkpoint;
- active immutable admission events after the checkpoint;
- idempotency receipts;
- redacted rejected-admission metadata;
- a materialized projection and its canonical logical digest.

Every active event includes its request fingerprint, predecessor digest, event digest, revision, record time, and affected IDs. Open verifies framing, schema and policy versions, receipt/event cardinality, revisions, fingerprints, chain integrity, and deterministic replay. A projection mismatch is rebuildable. A ledger mismatch is not.

## Writes

The only general mutation method is `LongitudinalAdmissionController.submit(LongitudinalAdmissionRequest)`. The admission policy decides whether a mutation is valid. The store protects a candidate document and atomically replaces the prior protected blob before exposing the new in-memory state. A post-write failure rereads and verifies the artifact to distinguish a committed write from failure without commit.

No DAO, SQL connection, Android context, or transaction implementation appears in the public domain ports.

## Deletion

`DeleteSource` requires the user actor, personal-data lifecycle origin, and entire-history scope. It removes all source revisions, direct assertions, and dependent objects whose grounding no longer survives; records non-eligible lifecycle tombstones; removes source privacy mappings; and creates a checkpoint so deleted source bodies are no longer present in active or archived ledger requests. The deleted stable source ID cannot be silently reused.

Deletion cannot erase a separately held backup. A later restore of an older backup is an explicit user recovery action and is never silently merged into a non-empty current store.
