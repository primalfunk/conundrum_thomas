# CT-V2-10 Qualification

## Disposition

`CT_V2_10_BIOGRAPHER_COVERAGE_ENGINE_COMPLETE`

CT-V2-10 is qualified only for deterministic synthetic use. No Android, production longitudinal, therapeutic, clinical, model, or real-user authority is granted.

## Accepted baseline

- branch: `main`
- starting HEAD: `52cc385f2acc2fdf6f1e93a6b89594c8a87cb7c0`
- starting tree: `184c163d358ead8955e9340f23694b8f0dd7b50f`
- accepted tag: annotated `ct-v2-09-journal-capture-engine`
- tag object: `0546cfef5c89804527c81af75ebd1c6f3aef7eca`
- tag target: `52cc385f2acc2fdf6f1e93a6b89594c8a87cb7c0`
- worktree before implementation: clean
- remotes: zero
- canonical forward plan: 25,031 bytes, SHA-256 `bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886`
- canonical forward-plan copies: one; root `fdp.txt`: absent
- V1 migration register: 24 components, 24 `DENIED`
- pre-change regression: `gradlew test --rerun-tasks --console=plain`, successful, 91 actionable tasks

## Qualified architecture

The pure `:thomas:biographer` module depends only on `:thomas:language-evidence`, whose governed dependency direction leads to longitudinal admission and domain contracts. It contains:

- `OPEN_STORY` and `TARGETED_COVERAGE` posture authority;
- 13 typed structural target kinds;
- a reproducible coverage map and canonical digest;
- deterministic target eligibility, priority, and stable-ID tie-breaking;
- immutable operational investigation history;
- one-question semantic plans with explicit forbidden assumptions;
- source-first Biographer answer-capture contracts;
- typed answer provenance for typed and speech-transcript text;
- no persistence implementation, Android API, model, renderer implementation, Therapy policy, or Journal response policy.

`GovernedBiographerPipeline` is confined to `:qualification`. It composes the Biographer contracts with CT-V2-08 perception/state formation and the CT-V2-07 qualification store. Every durable source, coverage transition, identity decision, and explicit correction is submitted to the CT-V2-07 admission controller.

The application and production-facing runtime graph do not depend on `:thomas:biographer` or `:thomas:longitudinal-store`.

## Deterministic selection and anti-repetition

The qualified priority order is:

1. explicit current user target;
2. unresolved correction target;
3. unresolved entity identity;
4. existing open evidentiary question;
5. unresolved comparable contradiction;
6. unresolved event time;
7. structural temporal gap;
8. sparse represented period;
9. role gap;
10. place gap;
11. relationship context;
12. event detail.

Stable target ID is the deterministic tie-break. Private, declined, covered, unanswerable, unchanged deferred, and unchanged recently asked targets are excluded. A materially changed grounding token or explicit user reopening can restore eligibility where allowed. A user-named topic cannot override a still-private state. `NO_TARGET` is a valid result.

No severity, diagnosis, trauma probability, therapeutic leverage, emotional intensity, model output, or random value enters ranking.

## Question and answer boundaries

- Every authorized question plan permits at most one question.
- Plans carry target kind and ID, grounding IDs, safe structural facts, uncertainty constraints, reason code, rule version, and prohibited acts.
- The renderer cannot select or alter a target or eligibility.
- Renderer failure causes no source mutation.
- Question text is never user evidence.
- Answers are admitted first as `BIOGRAPHER_OPEN_NARRATIVE` or `BIOGRAPHER_GUIDED_TIMELINE` source evidence.
- Event time remains distinct from report and store-assigned record time.
- Downstream perception failure preserves the committed source.
- Private answers skip ordinary language derivation.
- Identity and correction outcomes use existing CT-V2-07 governed operations and preserve prior history.
- Operational offer/defer/skip/decline/stop history remains separate from personal evidence.

## Synthetic qualification corpus

All 74 required acceptance cases passed. The corpus covers open story, temporal coverage, event/role/place coverage, unresolved identity, contradiction and correction, user-named topics, deterministic selection, anti-repetition, provenance, privacy, answer capture, mode separation, and all CT-V2-04 through CT-V2-09 regressions.

Additional focused tests prove:

- coverage-digest repeatability and change after operational history;
- source-first capture and survival of downstream processing failure;
- private-answer language exclusion;
- synthetic-only authority rejection before admission;
- user-named targets cannot override private coverage;
- renderer enforcement of the one-question maximum.

No randomized/property seed was used; the phase uses only fixed deterministic cases.

## Final build evidence

Exact command:

```text
gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
```

Result:

- `BUILD SUCCESSFUL in 1m 9s`
- 335 actionable tasks; 335 executed
- 516 tests total
- 92 new CT-V2-10 tests
- failures: 0
- errors: 0
- skipped: 0, including zero skipped CT-V2-10 acceptance tests
- lint tasks: successful; zero errors/fatals and no CT-V2-10 lint regression
- debug and unsigned release APKs assembled
- runtime provenance database generation successful

Test distribution:

- `BiographerCoverageQualificationTest`: 25
- `BiographerSelectionQualificationTest`: 25
- `BiographerAuthorityQualificationTest`: 26
- `DeterministicBiographerCoverageEngineTest`: 13
- `BiographerAnswerCaptureEngineTest`: 3

APK identities:

- `app-debug.apk`: 31,205,813 bytes; SHA-256 `e5dc7ca705ce5e6e3bda3173eee255d0abe4632241b4564b0dd20a1dfceea049`
- `app-release-unsigned.apk`: 22,738,930 bytes; SHA-256 `d9b960bdb8636c88b8323b2d51432af3c48bda81afa02e884e806e7f147d051e`

No device or hardware execution was performed.

## Artifact and authority audit

- APK Biographer/fixture/longitudinal database/model filename hits: 0
- APK synthetic-corpus/Biographer-class/provenance-database content probes: 0
- tracked build outputs: 0
- tracked database files: 0
- tracked model artifacts: 0
- tracked raw Biographer fixture corpora: 0
- application backup: disabled
- production module references to Biographer: 0
- application runtime dependency-graph Biographer/language-evidence/longitudinal-store hits: 0
- direct Biographer SQL/JDBC paths: 0
- Biographer model/prompt paths: 0
- Biographer operational logging paths: 0
- Biographer-to-Therapy routes: 0
- Biographer-to-Journal response routes: 0
- question-to-user-evidence paths: 0
- production Biographer writers: 0
- Android/app Biographer writers: 0
- model-authorized Biographer writers: 0
- V1 Biographer writers: 0
- real user records: 0
- production longitudinal authority: 0
- qualification complete composition roots: 1 (`GovernedBiographerPipeline`)
- qualification CT-V2-07 submit call sites: 4; all use the sole admission controller

## VCS and repository checks

- canonical project root: `C:/Android Studio Projects/ConundrumThomasV2`
- canonical Git directory: `.git`
- temporary Git metadata directories: 0
- `.git` was never relocated during CT-V2-10
- remote count: 0
- `git diff --check`: passed
- canonical-root verifier: passed
- forward-plan size/hash: unchanged
- V1 register: 24/24 `DENIED`

Final tag and post-seal `git fsck --full --strict` identities are recorded in the Principal completion report because they are created after this evidence document is committed.

## Limitations

The engine is synthetic-only. It has no Android Biographer wiring, real user history, model target selection, therapeutic formulation, semantic retrieval, production store, production privacy lifecycle, production longitudinal authority, clinical qualification, or therapeutic-efficacy claim.

## Recommendation

The canonical forward plan identifies the next phase as **CT-V2-11 — Longitudinal Retrieval & Context Packets**. The sealed CT-V2-10 result is ready for Principal consideration of that phase; CT-V2-11 is not opened here.
