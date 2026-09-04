# ADR 0020: Authenticated protected-blob persistence behind governed ports

- Status: Accepted for CT-V2-14 qualification
- Date: 2026-09-04

## Decision

Use a pure-Kotlin versioned longitudinal document protected as one AES-GCM authenticated blob. Bind Android through a narrow AndroidKeyStore key provider and `AtomicFile` storage under `noBackupFilesDir`. Keep the app unwired until CT-V2-15. Keep the CT-V2-07 admission controller as the only semantic write gate.

## Rationale

The existing longitudinal aggregate and deterministic admission policy are already the semantic authority. A protected document retains those exact types without introducing a competing Room/DAO profile model. Whole-document atomic replacement makes event/projection consistency explicit and keeps plaintext SQL side files, WALs, and generic DAOs out of the first sensitive-data boundary.

## Consequences

- Store size is conservatively capped and every write rewrites one protected artifact; scaling requires a future migration preserving the same ports and lineage.
- Android key behavior is platform-dependent and needs device qualification before product composition.
- Projection damage can be rebuilt; ledger/source damage cannot be guessed around.
- Deletion checkpoints trade historical as-of access before deletion for actual source-body erasure.
- Generic platform backup stays disabled; protected user backup is explicit.
