# CT-V2-14 Migration, Corruption, and Recovery

## Version policy

The current document schema is 2; schema 1 is the minimum supported version. Version framing and serialized-class allowlisting are checked before state is admitted. Future, zero, malformed, or truncated versions fail closed. A supported migration replays the evidence ledger and preserves logical digest, source/revision identity, privacy, corrections, idempotency receipts, and uncertainty.

Migration writes only after full in-memory verification. An interruption before the atomic commit leaves the prior schema intact and retryable. A failed migration never initializes an empty replacement.

## Recovery classes

| Damage | Disposition |
| --- | --- |
| Projection or derived material differs from replay | Rebuild deterministically from trusted checkpoint + ledger |
| Rebuildable index/cache absent | Recompute; never grant authority to stale copy |
| Event digest/fingerprint/revision chain invalid | Ledger integrity failure; state unavailable |
| Source/dependency graph invalid | Fail closed; do not manufacture source continuity |
| Protected bytes modified/wrong key | Authentication/integrity failure |
| Unsupported schema/policy | Unsupported; no fallback initialization |
| Atomic write interrupted before replacement | Prior state remains authoritative |
| Failure after replacement | Reread/verify receipt; report committed only when proven |
| Restore interrupted before commit | Empty target remains; retry permitted |

Deletion checkpoints are a deliberate exception to full historical event replay: erased requests are replaced by content-free headers and the post-deletion aggregate becomes the earliest available as-of revision. Older state is intentionally unavailable rather than reconstructed from erased content.

The desktop NIO adapter requires an atomic move; it does not degrade to a non-atomic replacement. Android uses `AtomicFile`. Device/emulator restart behavior requires a separately available Android runtime environment and is not inferred from JVM restart tests.
