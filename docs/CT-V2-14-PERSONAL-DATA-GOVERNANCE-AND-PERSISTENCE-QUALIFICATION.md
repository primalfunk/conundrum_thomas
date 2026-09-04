# CT-V2-14 — Personal Data Governance & Persistence Qualification

CT-V2-14 introduces the first production-capable local persistence boundary for governed longitudinal personal data. It does not wire that boundary into the Android application and does not authorize collection of real personal or psychological information.

## Resulting architecture

```text
Journal / Biographer / Therapy user-source adapters
                    |
                    v
         CT-V2-07 governed admission port
                    |
                    v
      :thomas:personal-data-persistence
       | admission policy + encrypted document
       | immutable active ledger + deletion checkpoint
       | rebuildable projection + lineage digests
       | export / protected backup / restore / reset
                    |
                    v
       :platform:persistence-android
       AndroidKeyStore + noBackupFilesDir + AtomicFile
```

The platform adapter is production-capable but deliberately has no dependency edge from `:app` or `:thomas:runtime`. The qualification SQLite store remains a synthetic adapter. Both implement the same narrow admission/read contracts.

## Governing decisions

- Persistent user material is admitted only with `PROTECTED_PERSONAL_DATA` classification through CT-V2-07.
- Accepted events form a revision-ordered authenticated logical ledger. Projection state is checked against deterministic replay and rebuilt only when the ledger remains trustworthy.
- Source deletion is an entire source-history erasure. Revision-only erasure is denied because it would create deceptive lineage.
- A deletion creates a content-free checkpoint, redacts prior event requests, tombstones the source identity, and invalidates dependent objects.
- Primary artifacts and backups use AES-256-GCM with random 96-bit nonces and 128-bit tags. Android primary keys live in AndroidKeyStore and are not exported through the application port.
- Human and machine exports are explicit user-facing representations, not internal database dumps. They are intentionally plaintext at the API boundary and must be placed only at a user-authorized destination by a later integration phase.
- Generic Android cloud backup and device transfer remain disabled. Thomas backup is an explicit, independently protected artifact.

## Authority retained upstream

Persistence cannot choose safety, mode, Therapy route or technique, Journal posture, Biographer target, retrieval result, or rendered language. Renderer and realizer modules have no persistence handle. Store output does not turn assistant text into user evidence.

## Lifecycle behavior

Committed source revisions and correction lineage are revision-retained until governed deletion. Derived state and indexes are recomputable and subordinate. Context packets, retrieval working sets, and render fingerprints are ephemeral. Drafts, raw audio, renderer candidates, prompts, model output, and narrative logs are never persisted by this boundary.

## Qualification boundary

All data used by CT-V2-14 tests is synthetic and non-identifying. Device Keystore runtime behavior is not claimed without an attached emulator/device; compile-enforced Android adapter structure, JVM authenticated-protection behavior, atomic desktop qualification, and static platform audits are distinct evidence.

See:

- [Persistent store architecture](persistence/CT-V2-14-PERSISTENT-STORE-ARCHITECTURE.md)
- [Encryption and key boundary](persistence/CT-V2-14-ENCRYPTION-AND-KEY-BOUNDARY.md)
- [Lifecycle and retention](persistence/CT-V2-14-DATA-LIFECYCLE-AND-RETENTION.md)
- [Backup, export, and restore](persistence/CT-V2-14-BACKUP-EXPORT-AND-RESTORE.md)
- [Migration and recovery](persistence/CT-V2-14-MIGRATION-CORRUPTION-AND-RECOVERY.md)
- [Threat and failure model](security/CT-V2-14-PERSONAL-DATA-THREAT-MODEL.md)
- [Qualification evidence](qualification/CT-V2-14-QUALIFICATION.md)
