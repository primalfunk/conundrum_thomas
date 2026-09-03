# CT-V2-06 qualification record

**Qualified:** 2026-09-03

**Starting HEAD:** `e93ca7e69c81443f62ac0c171decd5116d02aac5`

**Starting tree:** `a58d1ebc0018f31f17ede281adc46d3311d18953`

**Starting tag:** `ct-v2-05-core-ordinary-therapy`

## Disposition

CT-V2-06 is complete as a platform-independent, evidence-first longitudinal domain foundation. It represents original personal sources, source-backed assertions, typed life structures, independent event/report/record times, epistemic separation, uncertainty, contradiction, correction, supersession, hypothesis dependencies, unresolved identity, and coverage status without admitting or persisting real psychological data.

It grants no production longitudinal writer, persistence, extraction, retrieval, profile-mutation, therapeutic, or language-model authority. CT-V2-07 and all later phases remain unopened.

## Forward development plan

The Principal-provided root `fdp.txt` was the only starting worktree addition. Its complete working content was moved to `docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md`; the root file no longer exists.

- Before and after size: 25,031 bytes.
- Before and after SHA-256: `bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886`.
- Substantive changes: none.
- Canonical references: root `README.md` and `docs/CONUNDRUM-THOMAS-V2-ARCHITECTURE.md`.

Plan placement commit:

- `fcdcaaf5ead39b6b97cd76e99666ecdf2c242a58` - `Place canonical V2 forward development plan`

## Implementation

Added the zero-project-dependency Kotlin/JVM module `:thomas:longitudinal`. Only the qualification harness depends on it; `app`, `runtime`, `engine`, `safety`, persistence, speech, and renderer modules do not. The module has no Android, Room, SQLite, network, model, therapeutic-policy, or production-persistence dependency.

The domain defines:

- stable typed identifiers for sources, interactions, assertions, entities, personal concepts, hypotheses, evidence relations, identity links, and coverage topics;
- `SourceRecord` with acquisition mode, interaction/revision provenance, report time, record time, and either exact inline content or a future-safe content reference;
- five distinct acquisition modes: Journal, open-narrative Biographer, guided-timeline Biographer, Therapist conversation, and user correction;
- source-backed `EvidenceAssertion` values restricted to explicit user assertion or user interpretation, with typed subjects, predicates, values, uncertainty, and event time;
- a structurally separate `ThomasHypothesis` and inspectable evidence/hypothesis dependencies;
- ten life-structure entity forms: person, place, event, period, relationship, role, decision, behavior, coping response, and outcome;
- unresolved, candidate-same, established-same, and established-different identity links without ingestion-time merging;
- exact, calendar, approximate, range, relative, ongoing, before/after, uncertain-chronology, and unknown event-time forms;
- explicit contradiction, correction, and supersession relations that preserve predecessor claims and original sources;
- coverage statuses including unknown, not explored, partial, private, declined, irrelevant, unresolved, and sufficiently understood;
- an immutable `LongitudinalEvidenceSnapshot` that validates references, unique identities, temporal honesty, correction provenance, acyclic revision/supersession/dependency graphs, and entity-link consistency.

No serialization was introduced because qualification did not require it. No store, repository, writer, admission controller, extractor, resolver, retrieval projection, mutable profile, or pattern engine was introduced.

Implementation commit:

- `d4199faf0dfbbad72b2e07de4de6e5a2ad31c64e` - `Implement CT-V2-06 longitudinal evidence foundation`

The final sealing commit and annotated tag are reported at handoff because a commit cannot contain its own object ID.

## Synthetic fixtures

Nine synthetic fixtures demonstrate:

1. a contemporaneous Journal event with same-day event and report dates but distinct temporal fields;
2. a later retrospective Biographer account linked to the same event while the Journal source remains unchanged and separately attributable;
3. contradictory Journal and recollection details that coexist with an unresolved contradiction relation;
4. an explicit user correction whose predecessor, correction source, and correction/supersession links all survive;
5. a user's belief about another person's motivation represented as interpretation, never direct fact about that person's internal state;
6. a Thomas hypothesis derived from two assertions, kept separate from user claims with both dependencies visible;
7. “around 2012” represented as an approximate year without an invented date;
8. two references to “Sam” retained as separate people under an unresolved identity link;
9. private and declined coverage topics accepted as valid terminal information states rather than corrupt records.

All names and situations are synthetic. No actual user psychological information is present.

## Invariant and validation evidence

The tests prove that event, report, and record time remain independent; Journal and Biographer provenance remain distinguishable; many sources may support one entity; one source may yield many assertions; contradictions coexist; correction and supersession preserve history; user interpretation and Thomas hypothesis cannot masquerade as direct fact; dependencies remain inspectable; temporal uncertainty is retained; identity may remain unresolved; original sources survive derivation; and private/declined states are valid.

Invalid structures fail deterministically, including duplicate stable IDs, missing source/entity/assertion/relation references, invalid source revision order, revision cycles, missing hypothesis dependencies, dependency cycles, supersession cycles, a non-correction source used as a correction, correction/supersession mismatch, conflicting identity decisions, missing established-identity evidence, invalid temporal ranges, and a record time before report time.

The CT-V2-04 permit boundary, CT-V2-05 deterministic repertoire and anti-repetition behavior, Journal `NO_RESPONSE`, Biographer investigation-only authority, therapeutic-source provenance, ontology, and V1 denial governance all remain regression-qualified.

## Qualification results

Clean combined command:

```text
gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks
```

Result: `BUILD SUCCESSFUL` in 53 seconds; all 314 actionable tasks executed.

The complete suite contains 215 tests:

- CT-V2-06 pure longitudinal module tests: 23;
- CT-V2-06 fixture and architecture-boundary tests: 18;
- all CT-V2-05 and earlier regression tests: 174.

Final result: 215 passed, 0 failed, 0 errors, 0 skipped.

Android lint: 0 errors and 11 advisory warnings: ten in the unchanged starter application and one in the unchanged renderer adapter. Both Android variants assembled. The SDK/NDK sandbox could not execute `llvm-strip`; Android Gradle Plugin therefore packaged the existing third-party `libandroidx.graphics.path.so` unstripped and completed successfully. This is a tooling warning, not native-strip or hardware qualification.

- Debug APK: 31,205,813 bytes; SHA-256 `89861b0bc764ae49b885ee79d14f6ab83f1f8e5e86effb50c49b9114a0caa1b9`.
- Unsigned release APK: 22,738,930 bytes; SHA-256 `de981cf99df06ebaca9d95a9cbd5bb523b38ba4d9a4dd1a31af64d61b3caa617`.
- Final regenerated ignored provenance database: 364,544 bytes; SHA-256 `2af38317f8ef4cba857a500947a9a8c21a8b6d148902249d6ef2a30e7d4434b5`.

No Android runtime behavior changed. No device or emulator was used, and no device, hardware, clinical, legal, rights, software-autonomy, production-persistence, or production-therapy qualification is claimed.

## Authority audit

| Authority surface | Count |
|---|---:|
| Production longitudinal writers | 0 |
| Production psychological-persistence paths | 0 |
| Direct psychological-profile mutation paths | 0 |
| LLM evidence decisions | 0 |
| LLM therapeutic decisions | 0 |
| Production therapeutic rules | 0 |
| V1 migration authorities | 0 |

The V1 register contains 24 components: 24 remain `DENIED`, none is approved or migrated, and no eventual migration commit is recorded. The register's phase marker alone advanced to CT-V2-06. No V1 implementation or model crossed the gate.

## Repository and artifact integrity

- Raw/restricted clinical artifacts tracked: 0.
- Raw provenance-cache artifacts tracked: 0.
- Model artifacts tracked: 0.
- Generated databases, APKs, AARs, native libraries, or build outputs tracked: 0.
- New raw clinical/source artifacts acquired: 0.
- Production personal source records or assertions added: 0.
- Android application backup: unchanged and disabled in source and merged manifests.
- Remote/push: none / not performed.
- Final worktree, tag, tree, tracked-artifact audit, and `git fsck --full --strict` are verified at seal and reported at handoff.

## Recommendation

Thomas now has a trustworthy vocabulary for longitudinal evidence, but no mechanism is authorized to admit, revise, persist, query, or retrieve it. The next limiting factor is governed state transition and storage design with the psychological-data boundary intact.

The repository is ready for the Principal to consider **CT-V2-07 — Governed Admission, Revision & Longitudinal Store**. No later phase is opened by this record.
