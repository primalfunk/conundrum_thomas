# CT-V2-07 relational store schema

## Identity

- Schema version: `1`
- Data classification: `SYNTHETIC_QUALIFICATION_ONLY`
- Deterministic schema fingerprint: `cec67480788273b329cf0c6b1e916a1511dd90fc8886c434e2ad2fccc8f15db0`
- Engine: SQLite JDBC, JVM qualification only
- Migration path implemented: empty database to version 1

The fingerprint is SHA-256 over the ordered normalized schema statements. A stored version, fingerprint, or classification mismatch fails closed. A nonempty unversioned database and a higher schema version are not silently migrated or repaired.

## Persisted record families

| Table | Authority/classification | Purpose |
|---|---|---|
| `store_metadata` | store authority / control | Schema version, schema fingerprint, and synthetic-only classification. |
| `ledger_event` | canonical accepted history | Ordered immutable accepted operations, fixed record time, policy and actor metadata, redacted fingerprint, private encoded operation, affected IDs, and decision codes. |
| `current_state_projection` | derived projection | Rebuildable aggregate state, ledger revision/anchor, and canonical logical digest. It is not independent evidence. |
| `immutable_object_index` | derived integrity index | Prevents stable-object identity reuse and links first admission to a ledger revision. |
| `lifecycle_history` | canonical revision audit | Append-only lifecycle transitions and eligibility causes for stable objects. |
| `idempotency_record` | authority/control index | Logical request fingerprint to original accepted ledger event and revision. |
| `rejection_audit` | redacted audit material | Rejected identifiers, operation, policy, disposition, reason codes, fingerprint, and record time. No submitted source text. |

Foreign keys are enabled for every connection. STRICT tables constrain stored types. Triggers reject updates and deletes against `ledger_event` and `lifecycle_history`.

## Ledger and projection transaction

An accepted operation increments the store revision by one. The event row, projection, indexes, lifecycle rows, and idempotency row commit together. A fault or constraint violation rolls the transaction back. There is no successful projection mutation without the corresponding accepted event, and no accepted event without the matching projection.

The binary payload codec is private to the store and permits only the longitudinal/admission domain plus narrowly required Java/Kotlin value types. Payloads are not an external interchange format and do not grant production compatibility.

## Replay and as-of reads

Replay reads events in store-revision order, verifies each request fingerprint, and evaluates the same policy with the event's fixed record time. Replaying into a compatible empty store reproduces the canonical logical-state digest. As-of reads use the same deterministic reconstruction for any valid revision.

The digest is calculated from a canonical typed encoding with stable ordering of maps and sets. Database row IDs, physical order, connection timing, and fixture path do not participate.

On open, the implementation verifies projection payload digest, projection revision, ledger anchor, maximum ledger revision, schema metadata, and classification. Corrupt or inconsistent state is reported; it is not rewritten into a different meaning.

## Deliberate limits

Version 1 is a qualification schema, not a production personal-data design. There is no encryption claim, Android/Room adapter, Keystore, backup, export, purge, retention, cloud synchronization, safety-state storage, or application packaging.
