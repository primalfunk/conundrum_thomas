# CT-V2-07 qualification

## Disposition

`CT_V2_07_GOVERNED_ADMISSION_REVISION_STORE_COMPLETE`

Qualification establishes synthetic storage mechanics only. It grants no production longitudinal, psychological-data, safety, or therapeutic authority and does not open CT-V2-08.

## Lineage

- Starting HEAD: `074df53b21361a6051e61c7c14e3d78dc91de9fc`
- Starting tree: `b3d0efea7987770c62d9026988dd8fb7863004cb`
- Starting annotated tag object: `18260b701e5be85543178e187da8da5479f147c4`
- Starting tag target: `074df53b21361a6051e61c7c14e3d78dc91de9fc`
- Qualified implementation HEAD before this evidence record: `8a8571f5a46c3467d402782b84b3fd3878f813c5`
- Qualified implementation tree: `be4ebfc9197ec0bc38ea763dde3200bc55c5b6ab`
- Branch: `main`
- Remote: none
- Push performed: no

The canonical forward plan remained at `docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md`, 25,031 bytes, SHA-256 `bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886`. The before and after values are identical. Root `fdp.txt` is absent and no redundant plan copy exists.

## Implementation qualified

```text
:thomas:longitudinal-store
  -> :thomas:longitudinal-admission
       -> :thomas:longitudinal

:qualification
  -> :thomas:longitudinal-store
```

The application, runtime, engine, safety, Android persistence, renderer, speech, and provenance runtime do not depend on admission or store. The V1 register is byte-unchanged from the CT-V2-06 baseline: 24 components, 24 `DENIED`, migration authority false.

The only public external admission entry is `LongitudinalAdmissionController.submit`. `QualificationLongitudinalStore.replayIntoEmpty` is a separate controlled qualification maintenance operation: it targets an empty compatible store, verifies request fingerprints, re-evaluates the deterministic policy with fixed historical record times, and cannot originate new evidence.

Schema version 1 and fingerprint `cec67480788273b329cf0c6b1e916a1511dd90fc8886c434e2ad2fccc8f15db0` were fixed by invariant test. The persisted families are metadata, immutable accepted ledger, rebuildable current projection, immutable-object index, lifecycle history, idempotency index, and redacted rejection audit.

## Clean qualification

Exact command:

```text
gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
```

Result:

- Gradle: `BUILD SUCCESSFUL in 43s`
- Tasks: 320 actionable, 320 executed
- Tests: 282 total; 0 failures; 0 errors; 0 skipped
- New CT-V2-07 tests: 67; 0 failures; 0 errors; 0 skipped
- Lint: 0 errors; 16 existing app warnings and 1 existing renderer-adapter warning; the two new pure JVM modules add no Android lint surface
- Debug APK assembled: 31,205,813 bytes; SHA-256 `89861b0bc764ae49b885ee79d14f6ab83f1f8e5e86effb50c49b9114a0caa1b9`
- Unsigned release APK assembled: 22,738,930 bytes; SHA-256 `de981cf99df06ebaca9d95a9cbd5bb523b38ba4d9a4dd1a31af64d61b3caa617`
- Runtime provenance database generation: passed; generated output remained ignored and untracked

CT-V2-07 test distribution:

| Suite | Tests |
|---|---:|
| `GovernedLongitudinalStoreCoreTest` | 16 |
| `LongitudinalAdmissionRevisionTest` | 18 |
| `LongitudinalDurableFixtureTest` | 9 |
| `LongitudinalStoreBoundaryQualificationTest` | 13 |
| `LongitudinalStoreInvariantTest` | 7 |
| `LongitudinalTemporalPersistenceTest` | 4 |

No randomized operation-order test was needed for the bounded version-1 policy; property-test seed is therefore not applicable. Determinism is qualified through identical-request replay, fixed injected clocks, canonical encoding, repeated evaluation, close/reopen, ledger replay, as-of reconstruction, and fixed exact expectations.

## Acceptance evidence

- All nine CT-V2-06 fixtures were admitted through the governed controller, closed, reopened, and read back.
- File-backed and in-memory qualification stores use the same controller.
- Ledger replay into an empty store reproduced the canonical logical-state digest.
- Same-key/same-payload replay created no duplicate; changed-payload reuse failed closed.
- Stale revision, duplicate ID, missing reference, unsupported hypothesis, unauthorized authorship, invalid time, cycle, unsupported schema, and inconsistent-state cases rejected without domain mutation.
- User correction retained original evidence and changed dependent current eligibility without erasing history.
- Contradictory evidence remained simultaneously inspectable.
- Source revision, correction, retirement, identity revision, and privacy changes produced durable lifecycle consequences.
- A source changed from PRIVATE to ELIGIBLE remained review-required; the privacy toggle did not silently reactivate evidence.
- PRIVATE support could not admit a new hypothesis and blocked existing dependents. DECLINED coverage remained a non-factual state.
- Exact, date, approximate, range, relative, ongoing, uncertain, and unknown temporal forms round-tripped without precision change.
- Every fault point (`AFTER_LEDGER_INSERT`, `AFTER_PROJECTION_WRITE`, `BEFORE_COMMIT`) produced `FAILED_WITHOUT_COMMIT`, revision 0, and an empty projection.
- Unsupported or corrupt persistent state failed closed and was not silently repaired.
- Rejected unique fixture content was absent from persisted rejection audit text and ordinary diagnostic surfaces.

## Bypass and authority audit

Discovered write mechanisms were classified as follows:

| Mechanism | Count | Classification |
|---|---:|---|
| Public qualification admission entry | 1 | Governed `submit`; deterministic policy required |
| Controlled empty-store replay entry | 1 | Qualification maintenance; cannot originate evidence |
| Private transaction implementation paths | 2 | Internal accepted commit and verified replay application; not authority entry points |
| Production longitudinal writers | 0 | None |
| App/runtime longitudinal writers | 0 | None |
| Model-authorized write paths | 0 | None |
| Unauthorized direct projection mutation paths | 0 | None |
| V1 migration write paths | 0 | None |
| Persisted safety-state paths | 0 | None |
| Real user records | 0 | None |
| Packaged database files | 0 | None |

The accepted receipt implementation is private, request types have no record-time field, JDBC/SQL/DAO/transaction types do not escape the store, ledger and lifecycle rows reject update/delete, rejected transactions leave no partial state, and source/identity history is not physically merged or overwritten.

## Artifact and security-boundary audit

- Debug APK archive entries: 152; prohibited database/model/fixture/longitudinal matches: 0
- Release APK archive entries: 143; prohibited database/model/fixture/longitudinal matches: 0
- Qualification fixture databases remaining after tests: 0
- Repository database files: only ignored generated clinical-source provenance output
- Tracked build, generated database, raw/restricted source, source-cache, and model artifacts: 0
- App backup remains disabled by `android:allowBackup="false"` and existing extraction/backup exclusions
- New module model, prompt, Android, Room, and network references: 0

## Existing-governance regressions

The complete test suite preserved CT-V2-01 provenance, CT-V2-02 ontology/mode authority, CT-V2-03 deterministic policy, CT-V2-04 permit gating, CT-V2-05 route progression/anti-repetition, CT-V2-06 evidence invariants, Journal `NO_RESPONSE`, Biographer investigation-only authority, and deny-by-default V1 governance.

## Limitations

The store is synthetic-only and plaintext. It has no production encryption, Android store, Keystore, backup/export/deletion lifecycle, recovery, retention policy, safety-state persistence, real user data, model, extractor, retrieval, embeddings, context construction, mode wiring, V1 import, or production longitudinal/therapeutic authority. Full personal-data lifecycle qualification remains deferred to CT-V2-14.

## Recommendation

CT-V2-08 — Language-to-Evidence & State Formation is ready for Principal consideration on top of this governed admission boundary. It remains unopened until separately authorized.
