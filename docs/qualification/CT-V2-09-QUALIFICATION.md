# CT-V2-09 qualification

## Lineage and entry gate

- Accepted start HEAD: `0ea9df3131f1da31ca3479d530fd96eeb71910ee`
- Accepted start tree: `5172149c0c6ad96bcf8224bfd8e82cd005bb561f`
- Accepted tag: `ct-v2-08-language-evidence-state-formation`
- Tag object: `1cb6412c89be1c0eef92c60c17fe28de9dd59e77`
- Tag target: accepted start HEAD
- Branch: `main`
- Remote: none
- Pre-change worktree: clean
- Fully qualified implementation HEAD: `2ec3eeec7835287706de123814e5bbc914af6e73`
- Fully qualified implementation tree: `b346436b94f4bdbc190636719eba5cd60302ea18`

The baseline HEAD/tree/tag, branch, empty remote set, clean worktree, canonical Git root, plan hash, root `fdp.txt` absence, single plan copy, and 24/24 denied V1 components were verified before tracked changes. The pre-change `gradlew test --rerun-tasks --console=plain` run succeeded with 87 actionable tasks.

The Principal work order is preserved at `docs/work-orders/CT-V2-09-JOURNAL-CAPTURE-ENGINE.md`. The forward plan was not modified.

## Implementation qualified

- Pure Kotlin `:thomas:journal`, directly dependent only on `:thomas:language-evidence`.
- Typed commit, revision, privacy, capture-origin, response-posture, failure, receipt, and rendering contracts.
- One Journal source identity backed by CT-V2-07 source identity and append-only revisions.
- Source admission before CT-V2-08 language processing; response planning occurs after both.
- Default `NO_RESPONSE` planner bypass; bounded `REFLECT` and `ASK_ONE_QUESTION` plans.
- Private source retention with derivation and response suppression.
- Response-posture-independent source operation and canonical capture fingerprint.
- Qualification-only `GovernedJournalCapturePipeline` with three CT-V2-07 controller submission sites: admit source, append source revision, change privacy.
- Qualification-only deterministic renderer, downstream of capture and without source text.
- Narrow CT-V2-08 grammar additions for the authorized synthetic Journal corpus and structural unresolved-reference preservation.

No app, Android, runtime, Therapy, safety, Biographer, persistence adapter, renderer implementation, model, speech implementation, or V1 module consumes `:thomas:journal`.

## Journal fixture consequences

| Synthetic case | Durable source revisions | Assertions | Formed-state consequence |
|---|---:|---:|---|
| Typed current feeling | 1 | 1 | one explicit self-report |
| Unsupported prose | 1 | 0 | valid empty derived state |
| Same commit replayed under all postures | 1 | 1 | identical sources, assertions, evidence IDs, and state digest |
| Private extractable entry | 1 | 0 | no active derived state |
| Date wording revision | 2 | 2 historical | prior assertion `REVIEW_REQUIRED`; new current revision |
| Three independent reports | 3 | 3 | one neutral recurrence candidate with three source IDs |
| Three comparable clauses in one entry | 1 | 3 | zero recurrence candidates |
| Same-time incompatible residences | 2 | 2 | one unresolved contradiction; both assertions retained |

Typed and speech-transcript inputs produce semantically equivalent assertions while preserving distinct capture-origin provenance. Retrospective Journal and Biographer sources coexist without source collapse.

## Response posture proof

- `NO_RESPONSE`: no plan, zero planner invocation through the guarded path, zero renderer invocation, no assistant artifact.
- `REFLECT`: brief current-entry assertion grounding, no question, attribution/uncertainty constraints retained.
- `ASK_ONE_QUESTION`: at most one current-entry-grounded optional question; unsupported content degrades to no plan.
- Planner and renderer fault fixtures retain one committed source.
- Rendering leaves the complete longitudinal snapshot byte-for-logically equal.
- The same idempotency key with changed text or privacy fails closed; a posture-only replay creates no revision.

## Regression found and resolved

The first full clean run reached 316 qualification tests and found one failure in the sealed CT-V2-08 boundary test. That test encoded the former consumer set for `:thomas:language-evidence`. It was updated to admit the Principal-authorized pure `:thomas:journal` consumer while adding an explicit assertion that `:qualification` remains the sole `:thomas:longitudinal-store` consumer. No behavioral or admission test failed. Subsequent complete clean runs passed.

## Final clean qualification

Command:

    gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain

Final result: `BUILD SUCCESSFUL in 1m 6s`; 330 actionable tasks, all executed.

- Repository tests: 424; failures 0; errors 0; skipped 0.
- New CT-V2-09 tests: 76 total: 12 Journal unit tests plus 64 Journal qualification tests.
- CT-V2-09 qualification breakdown: capture 30, mode/language 19, authority/bypass 15.
- CT-V2-09 skipped tests: 0.
- Deterministic/property-test seed: none; all named fixtures are deterministic.
- Lint SARIF reports: 4; error-severity results: 0.
- Debug APK: 31,205,813 bytes; SHA-256 `e5dc7ca705ce5e6e3bda3173eee255d0abe4632241b4564b0dd20a1dfceea049`.
- Unsigned release APK: 22,738,930 bytes; SHA-256 `084da8c058d663114dec69c7e0d084e23942093d388dd8ef234b91c9bf7f7559`.
- APK archive entries: debug 152, release 143.
- APK forbidden-name matches: 0 for CT-V2-09/qualification/fixture/longitudinal databases/models.
- Tracked databases: 0; model artifacts: 0; PDFs/raw source artifacts: 0; build outputs: 0.
- Generated provenance database: present, ignored, and untracked.
- Application backup: disabled in the manifest.
- Close/reopen and empty-store replay reproduce the CT-V2-07 logical digest and the CT-V2-08 formed-state digest.
- Forward plan: 25,031 bytes, SHA-256 `bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886`; tracked copies 1.
- V1 register: 24 components, 24 `DENIED`, authorization true count 0.
- Hardware/device qualification: not performed or claimed.

## Authority and bypass audit

| Authority path | Count |
|---|---:|
| Production Journal writers | 0 |
| Android/app Journal writers | 0 |
| Direct Journal SQL/JDBC writers | 0 |
| Model-authorized Journal writers/evidence writers | 0 |
| Raw draft-to-evidence paths | 0 |
| Response-to-user-evidence paths | 0 |
| Journal-to-Therapy routing paths | 0 |
| Journal-to-Biographer routing paths | 0 |
| Semantic/historical retrieval paths | 0 |
| V1 Journal/migration writers | 0 |
| Real user records | 0 |
| Production longitudinal writers | 0 |
| Qualification Journal capture pipelines | 1 |
| Journal adapter CT-V2-07 submission sites | 3 |

The one composition root exposes commit, revision, and privacy operations. The source commit is Journal's only source-creation path. CT-V2-08 may subsequently submit grounded evidence and structural relations, but CT-V2-07 remains the sole durable admission gate.

## VCS and IDE disposition

- Git top-level: `C:/Android Studio Projects/ConundrumThomasV2`.
- Git directory: `.git`.
- Inside worktree: true.
- Temporary Git metadata directories: 0.
- `.idea/vcs.xml` maps `$PROJECT_DIR$` to `Git`; `.idea` tracking policy is unchanged.
- `VCS_ROOT_ON_DISK_VALID`.
- `IDE_VISUAL_CONFIRMATION_AVAILABLE = false`.
- `IDE_INVALID_VCS_WARNING_OBSERVED = unknown`.
- The Android Studio warning was not visually observable from automated qualification and is not claimed absent.
- Canonical `.git` was never relocated, hidden, or redirected during CT-V2-09.
- `git diff --check` passed.
- `git fsck --full --strict` passed; pre-existing unreachable blobs were reported without corruption.

## Limits and recommendation

This phase is synthetic-only. It provides no production Journal storage, Android Journal wiring, real-user data authority, model requirement, retrieval, Biographer investigation, Therapy expansion, production encryption, backup/export/deletion lifecycle, or therapeutic efficacy claim.

The canonical forward plan names the next phase `CT-V2-10 — Biographer Coverage Engine`. The Journal foundation leaves that phase ready for Principal consideration only; it remains unopened.
