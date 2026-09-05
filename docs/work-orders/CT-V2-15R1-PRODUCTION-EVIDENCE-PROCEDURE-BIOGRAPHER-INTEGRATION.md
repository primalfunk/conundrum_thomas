# CT-V2-15R1 — Production evidence-to-procedure and Biographer coverage integration

Status: AUTHORIZED FOR IMPLEMENTATION by the Principal, 2026-09-05.

This preserves the operative scope and acceptance contract of the Principal's development order. It is a bounded integration repair following CT-V2-15, not CT-V2-16.

## Entry and custody

Expected and verified entry: main, HEAD `c987fbf3e3742124408a8e6f855d82d19e82f0d1`, tree `2081b1868cfa9979c63baa5d333d32ace7badfac`; annotated predecessor `ct-v2-15-android-production-integration`, object `da376b133bc4cdcc5a77fb3a9eeac5e92ec88bcf`, targeting HEAD. The sole untracked artifact is [the reconnaissance report](../reconnaissance/CT-V2-CURRENT-STATE-AND-TEST-READINESS.md), preserved without alteration and to be committed with this work. No entry discrepancy. Root verification, diff whitespace check, and strict full Git integrity check pass. V1 remains deny-by-default with migration unauthorized.

No reset, clean, stash, unknown-work overwrite, history rewrite, Git relocation, or push. Use coherent commits; finish clean. Preserve historical qualification reports and the canonical forward plan.

## Central invariant and authority

UNKNOWN MUST REMAIN UNKNOWN UNTIL AUTHORIZED EVIDENCE CHANGES IT. Declaration remains declaration; user report remains user report; interpretation remains interpretation. Policy state derives from authorized evidence or explicit governed session state. Do not manufacture state to reach an action.

Reuse CT-V2-04 safety, CT-V2-05 ordinary procedure, CT-V2-07 admission, CT-V2-09 Journal, CT-V2-10 Biographer, CT-V2-11 retrieval, CT-V2-12 longitudinal Therapy, CT-V2-13 renderer, CT-V2-14 protected custody, and CT-V2-15 runtime. Renderer, UI, database, and adapters acquire no technique or target-selection authority.

Model admission, speech, Pattern Engine/CT-V2-16, new Therapy techniques/routes/goals, clinical sources, screening questions, crisis protocols, safety doctrine, clinical authority, and release authority remain unauthorized. Keep DeterministicReferenceRealizer. Models, packaged model artifacts, remote inference, LM Studio, model-to-policy/safety/writes, V1 therapeutic paths, and speech implementation remain zero. Do not change pending clinical review statuses.

## Required integration

1. Observe current typed turns conservatively with inspectable evidence provenance. Bridge actual concern/boundedness, engagement, missing information, tentative understanding, confirmation/correction, delivered summaries, influenceability, willingness, user options/choice/plan, attempted outcome, pending information, and current route to existing CT-V2-05 state.
2. Maintain ephemeral procedural state separately from durable personal evidence. Document process-death loss; do not reconstruct procedure by guessing from the corpus.
3. Deterministically distinguish material evidence revisions from sends. Unchanged/rephrased evidence must not automatically reset stagnation. Relevant answers and corrections can advance; unsupported equivalence or novelty remains uncertain. Reach existing justified-repeat/stagnation guards.
4. Make LISTEN, UNDERSTAND, and PRACTICAL progression reachable through the actual Android input boundary. Preserve agency, never select an option for the user or treat a discussed plan as attempted. Honor correction, pause, stop, reluctance, resume, and supported direction changes without sentiment classification.
5. Replace the ordinary-scope checkbox's unrelated safety fact expansion with field-specific provenance. Distinguish established report/declaration, tentative, unknown, refused, conflicting, and stale/inapplicable. Current contradictions supersede any earlier ordinary declaration. Historical/quoted/instruction-like content is not current safety observation. CT-V2-04 remains unchanged; leave fields UNKNOWN where current authority cannot establish them. Inspect cross-mode safety contracts; report undefined handling rather than invent it.
6. Supply CT-V2-10 with actual eligible protected-corpus coverage: represented history, gaps, roles/places/relationships, unresolved identity/corrections, and private/declined exclusions. Use its existing deterministic ranking and one-question contract, never a second biography engine.
7. Retain target identity through answer interpretation. Connect material answer, no change, skip, defer, decline, private, stop, changed evidence, and correction outcomes. Questions/nonanswers are not evidence and do not cover gaps. Private/declined targets must not recur immediately or leak. Rebuild coverage after reopen from current eligible evidence; do not persist prompt history as psychological evidence.
8. Preserve Journal policy and prove Journal→Therapy and Biographer→Therapy admission, persistence, relevant bounded recall, source provenance, uncertainty, correction/privacy/deletion propagation, and current-route/current-safety authority.

## Acceptance and qualification

Acceptance scenarios A–N use user-like typed input → production adapter → governed state → unchanged engine → actual result. No manual internal Therapy state or CoverageEvidence injection after turns; assert semantic/action state and visible behavior, not merely non-null artifacts.

- A LISTEN: invitation/reflection/follow-up/summary/closure as applicable.
- B UNDERSTAND: missing piece, supplied answer, tentative understanding, confirmation, summary, next direction.
- C PRACTICAL: bounded concern, shared understanding, influenceability, readiness, options, user choice, concrete plan, actual later outcome and review/consolidation.
- D correction withdraws wrong understanding and changes later state.
- E pause/stop/reluctance and resume are honored.
- F unchanged evidence reaches stagnation; G new evidence permits progression.
- H unknown safety does not become safe; I later conflicting current evidence blocks ordinary policy.
- J a real stored gap selects a non-null target; K answering changes coverage/next selection.
- L declined/private targets neither recur immediately nor leak.
- M Biographer→Therapy and N Journal→Therapy preserve provenance and boundaries.

Preserve all 946 predecessor tests (59 suites, zero failures/errors/skips), strengthen permissive CT-V2-15 assertions, and add needed production-path tests without weakening accepted-result criteria. Report externally visible and typed traces for important conversations.

Canonical qualification from root, using installed Studio JDK if needed:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
git diff --check
.\tools\verify-canonical-git-root.ps1
git fsck --full --strict
```

Also verify V1 governance, APK artifact scans, authority paths, and safe instrumentation if available. Report suites/tests/new tests/failures/errors/skips, lint counts, tasks/duration, APK size/SHA-256, device results, and final Git metadata. Do not claim byte reproducibility from unsigned APK hashes containing changed HEAD metadata.

Do not mutate a connected device until ADB authorization and disposable synthetic-corpus custody are established. Physically exercise all three routes, targeted Biographer/answer-dependent target, both recall crossovers, safety unknown/conflict, and close/reopen durability. No valued-corpus reset/restore. If access alone remains blocked, report device qualification pending, not passed.

## Exclusions and completion

Do not absorb reconnaissance debts in restore rollback, reset error UI, transcript cleanup, broad concurrency, failed-open UI, SAF bounds, API re-exports, broad parser/render semantic generality, screen security, CI, signing, scaling, accessibility, thermal/battery, model, or speech. Only the smallest directly blocking repair is permitted and must be explained.

Create `docs/qualification/CT-V2-15R1-QUALIFICATION.md`; update architecture only for changed reality. Audit canonical Therapy observation/evaluator paths, unsupported assignments, safety provenance, real coverage/target suppliers, qualification suppliers, UI→technique/target, renderer/model→policy, response→evidence, private-unsurfaced→renderer, and V1 paths. Unsupported manufactured facts must be zero; real coverage/target paths nonzero.

Tag only after mandatory integration gates pass: annotated `ct-v2-15r1-production-evidence-procedure-biographer-integration`; record object/target/type. Never create a CT-V2-16 tag.

Successful disposition: `CT_V2_15R1_PRODUCTION_EVIDENCE_PROCEDURE_BIOGRAPHER_INTEGRATION_COMPLETE`. Bounded alternatives are `CT_V2_15R1_IMPLEMENTED_DEVICE_QUALIFICATION_PENDING`, `CT_V2_15R1_BLOCKED_BY_MISSING_POLICY_AUTHORITY`, `CT_V2_15R1_SAFETY_OBSERVATION_AUTHORITY_BLOCKED`, `CT_V2_15R1_THERAPY_BRIDGE_COMPLETE_BIOGRAPHER_INTEGRATION_BLOCKED`, or `CT_V2_15R1_BIOGRAPHER_INTEGRATION_COMPLETE_THERAPY_BRIDGE_BLOCKED`, with exact evidence. Do not force completion.

Final report must cover repository/reconnaissance preservation, architecture, three production conversations, safety, Biographer, cross-mode evidence, authority audit, regressions, device, limitations, and exactly one testing-readiness disposition followed by recommended next order and actual Principal decision (or NONE).
