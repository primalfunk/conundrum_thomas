# CT-V2-05 qualification record

**Qualified:** 2026-09-03

**Starting HEAD:** `8c19147fa8d7ade8a149af453e1f0324f958b1e4`

**Starting tree:** `654061136a9c97830d5a79818472a8b491a55cf1`

**Starting tag:** `ct-v2-04-safety-scope-gate`

## Disposition

CT-V2-05 is complete as a deterministic, qualification-only core ordinary therapeutic repertoire. It supports listening, understanding, bounded PM+ problem solving, and consolidation/closure behind the mandatory CT-V2-04 permit.

It grants no production therapeutic authority, safety authority, clinical approval, commercial permission, or autonomous LLM authority. It does not open CT-V2-06 or any later phase.

## Implementation

- Added typed immutable ordinary-conversation state, route preference, engagement, expression progress, correction, plan-review, information-requirement, action-history, and explicit repeat-authorization structures.
- Added 9 route rules, 26 action rules, 4 progression guards, 25 action definitions, 12 active goal concepts, and 19 explicit cross-route transitions.
- Added exact considered/matched/rejected rule traces and compact route-to-outcome explanations.
- Added renderer fields for tentative interpretation, explicit agency preservation, and response-required semantics without changing renderer authority.
- Added a deterministic qualification renderer and seven synthetic multi-turn conversations.
- Added 56 CT-V2-05 engine/renderer/conversation tests plus 2 CT-V2-05 provenance binding tests.
- Added a governed source-discovery need for standalone ordinary decision support and two pending software-autonomy review requirements. No source or rule authority was granted.
- Advanced only the V1 register phase marker; all 24 components remain denied.

Implementation commit:

- `a4d3977` - `Implement CT-V2-05 core ordinary repertoire`

The final sealing commit and annotated tag are reported at handoff because a commit cannot contain its own object ID.

## Source derivation and review

| Governed source | Artifact SHA-256 | Used subjects | Rule bindings |
|---|---|---|---:|
| WHO/UNICEF Foundational Helping Skills 2025, `who-fhs-2025` | `b3dde55d3e1a601699a41b4aaaef4d020cc9416fbc6773848396db81df52cbe9` | Module 1 verbal communication; Module 8 collaborative goals and eliciting feedback | Route/action/progression bindings |
| WHO Individual PM+ v1.1 (2018), `who-pm-plus-v1-1-2018` | `aaa6ce06dacc1058ba8b695b7d8146d33abfdfcf8a13ea7d888f71a01a833cbe` | Chapter 3 reluctance/advice; Chapter 7 steps 1-7 | Practical and pause boundaries |

Every source-derived rule resolves through exact document, version, section, locator, artifact hash, rights state, and pending review records. Rights remain `COMMERCIAL_PERMISSION_REQUIRED`. Clinical, rights, implementation-scope, software-autonomy, human training/supervision where applicable, and production reviews remain pending. No review was fabricated.

Standalone decision support is not implemented. `need-ordinary-standalone-decision-support` records that PM+ choice is bounded to its manageable-problem procedure and provides no general decision-support/autonomous-software authority. Optional emotional, behavioral activation, coping, grounding, motivational, and interpersonal pathways remain unopened.

## Deterministic behavior evidence

The canonical fixtures prove:

- a listen request stays on listening acts and forbids advice;
- understanding moves from one explicit information gap through tentative verification to a user-confirmed summary;
- practical help moves through problem, confirmation, influence, willingness, user-generated options, user choice, bounded plan, wait, outcome review, and consolidation;
- an explicit preference change transitions route with a recorded reason;
- correction withdraws the rejected tentative evidence before acknowledgment and reassessment;
- unchanged evidence substitutes one direction choice, then returns `NO_AUTHORIZED_ACTION`;
- a revised safety state revokes the old permit and blocks ordinary action.

Identical state, policy version, and permit produce equal decision objects. A selected action has an active goal, passes every prerequisite, and has no active exclusion. Missing graph coverage, malformed evidence, conflicts, unsupported routes, and stale permits fail visibly.

## Qualification results

Clean combined command:

```text
gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks
```

Result: `BUILD SUCCESSFUL` in 56 seconds; all 309 actionable tasks executed.

The final suite contains 174 tests:

- CT-V2-05 core conversation, decision, invariant, progression, and renderer tests: 56;
- CT-V2-05 source-binding tests: 2;
- prior qualification regressions (foundation, ontology authority, CT-V2-03, CT-V2-04): 86;
- provenance regression tests excluding the new binding suite: 19;
- ontology structural tests: 10;
- application foundation: 1.

Final result: 174 passed, 0 failed, 0 errors, 0 skipped.

Android lint: 0 errors and 11 advisory warnings: seven unused starter colors, two obsolete min-SDK resources/guards, one redundant starter label, and the expected arm64-only ChromeOS ABI advisory.

Both Android variants assembled. The SDK/NDK sandbox could not execute `llvm-strip`; Android Gradle Plugin therefore packaged the third-party `libandroidx.graphics.path.so` unstripped and completed successfully. This is recorded as a tooling warning, not claimed as a clean native-strip qualification.

- Debug APK: 31,205,813 bytes; SHA-256 `89861b0bc764ae49b885ee79d14f6ab83f1f8e5e86effb50c49b9114a0caa1b9`.
- Unsigned release APK: 22,738,930 bytes; SHA-256 `de981cf99df06ebaca9d95a9cbd5bb523b38ba4d9a4dd1a31af64d61b3caa617`.
- Generated ignored provenance database: 364,544 bytes; SHA-256 `4b7240a8b8adadf80de7db0b694fd8a4030449de76230f3068fb3de4e4a8a199`.

No Android device or emulator was used. No device, hardware, clinical, legal, rights, software-autonomy, or production qualification is claimed.

## Authority audit

| Authority surface | Count |
|---|---:|
| Ordinary therapeutic pathways implemented | 4 |
| Therapeutic/source-derived rules | 32 |
| Engineering authority/control rules | 7 |
| Total CT-V2-05 rules | 39 |
| Rules with exact provenance | 39 |
| Rules lacking provenance | 0 |
| Qualification-executable rules | 39 |
| Production-runtime-authorized rules | 0 |
| Dialogue actions | 25 |
| Active goal concepts used | 12 |
| Explicit pathway transitions | 19 |
| Anti-repetition/progression guards | 4 |
| Diagnostic classifiers | 0 |
| Predictive risk systems | 0 |
| Safety bypass paths | 0 |
| LLM route decisions | 0 |
| LLM goal decisions | 0 |
| LLM therapeutic-action decisions | 0 |
| Direct profile mutation paths | 0 |
| V1 migration authorities | 0 |

## Repository and artifact integrity

- Raw/restricted clinical artifacts tracked: 0.
- Model artifacts tracked: 0.
- Build outputs or generated databases tracked: 0.
- New raw source artifacts acquired: 0.
- V1 register: 24 components, 24 `DENIED`, 0 migration commits, V1 code migration authority false.
- V1 implementation files migrated: 0.
- Review events fabricated: 0.
- Production psychological persistence introduced: 0.
- Android application backup: unchanged and disabled.
- Remote/push: none / not performed.
- Final worktree, tag, tree, and `git fsck --full --strict` are verified at seal and reported at handoff.

## Recommendation

The next limiting factor is no longer basic route selection or deterministic progression. It is trustworthy upstream state formation and governed outcome/formulation updates: the current repertoire consumes structured evidence but does not derive it from natural conversation or admit it into durable state.

The repository is ready for the Principal to consider a separately authorized next phase, while remaining unfit for production therapeutic use. Standalone decision support and optional therapeutic families require governed sources and review before implementation. No next phase is opened by this record.
