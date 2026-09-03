# CT-V2-07 admission contract

## Authority envelope

Every write request contains a stable request ID, idempotency key, expected store revision, actor, origin, admission-policy version, `SYNTHETIC_QUALIFICATION_ONLY` classification, and one typed operation. Source and dependency references are fields of the operation. Record time is intentionally absent.

The sole qualification policy version is `ct-v2-07.admission.v1`. Unsupported policy or store versions fail closed.

## Operations

| Operation | Authority and effect |
|---|---|
| `ADMIT_SOURCE` | Commits an immutable first source revision independently of derived structure. Acquisition mode, author role, event time, report time, provenance, stable identity, and privacy are retained. |
| `APPEND_SOURCE_REVISION` | Appends a user-authorized revision to the current source revision. Stable source identity, acquisition mode, event time, and provenance chain remain fixed; structures derived from the prior wording require review. |
| `ADMIT_EVIDENCE_BUNDLE` | Atomically admits assertions, entities, relations, hypotheses, dependencies, identity candidates, and coverage. A failed member rejects the bundle. |
| `RECORD_USER_CORRECTION` | Requires USER actor, USER_CORRECTION origin and acquisition mode, and user authorship. Adds a source, assertion, correction, and optional matching supersession. |
| `RECORD_SUPERSESSION` | Adds an explicit acyclic relation and changes eligibility without deleting the predecessor. |
| `RECORD_CONTRADICTION` | Adds a relation while keeping both endpoints inspectable and without choosing a winner. |
| `REVISE_IDENTITY_LINK` | Appends an unresolved, resolved, or rejected decision. The previous decision remains in history and neither entity is merged or deleted. |
| `CHANGE_COVERAGE` | Appends a coverage state. PRIVATE and DECLINED are valid terminal information states, not negative facts. |
| `CHANGE_PRIVACY` | Changes source eligibility and deterministically blocks dependent current material when becoming PRIVATE. |
| `RETIRE_CLAIM` | Appends an ineligible lifecycle state while preserving evidence and revision history. |

## Results

Typed dispositions are `ACCEPTED`, `IDEMPOTENT_REPLAY`, validation, authority, stale-revision, idempotency-conflict, missing-reference, dependency, privacy, temporal-dishonesty, relation-cycle, schema/store-state rejection, and `FAILED_WITHOUT_COMMIT`.

An accepted receipt reports request and idempotency IDs, policy and operation, prior and resulting revisions, affected stable IDs, ledger-event IDs, store-assigned record time, stable decision codes, and a redacted SHA-256 payload fingerprint. The concrete receipt cannot be constructed through the public API.

A rejected request returns no receipt and makes no domain change. Rejection audit records contain identifiers, disposition, policy, reason codes, time, and fingerprint—not submitted source content.

## Idempotency and concurrency

The request fingerprint covers the logical envelope and payload but excludes request identity. Reusing an idempotency key with the same logical request returns the original event as `IDEMPOTENT_REPLAY`; it does not consume a revision or duplicate evidence. Reusing it with changed logical content returns `REJECTED_IDEMPOTENCY_CONFLICT`.

After the idempotency check, the expected revision must equal the current revision. A stale request returns `REJECTED_STALE_REVISION` without mutation. This is optimistic concurrency; there is no last-writer-wins path.

## Validation gates

The deterministic policy rejects unsupported authority, non-synthetic classification, duplicate stable IDs, missing references, invalid or cyclic relations, structurally invalid time, store time before report time, source-identity mutation, false source revision ancestry, unauthorized authorship, unsupported hypotheses, use of private evidence, and DECLINED coverage used as evidence.

Every admitted Thomas hypothesis must have inspectable dependencies that ultimately reach currently eligible source evidence. Superseded, retired, review-required, dependency-blocked, and private assertions cannot provide new active support. The controller never determines whether a hypothesis is psychologically true.

## Atomicity and trusted machinery

The store plans against one current aggregate and one injected clock value. It writes ledger, projection, index, lifecycle changes, and idempotency record inside one transaction. Fault injection at `AFTER_LEDGER_INSERT`, `AFTER_PROJECTION_WRITE`, or `BEFORE_COMMIT` rolls the entire transaction back.

The store-side clock fixes accepted record time. Callers cannot supply or backdate it. Fixture clocks make qualification deterministic.
