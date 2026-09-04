# CT-V2-12 Qualification

## Disposition

`CT_V2_12_LONGITUDINAL_THERAPIST_INTEGRATION_COMPLETE`

CT-V2-12 is qualified only for deterministic synthetic use. It grants no final-language renderer, model, Android Therapy integration, real-user storage, new therapeutic route or technique, longitudinal safety prediction, production privacy/security lifecycle, or production longitudinal authority.

## Accepted baseline

- branch: `main`
- starting HEAD: `20559d2637b454c4bc86f362a1101b1b804a6f8c`
- starting tree: `fa82db9cc9edd353ee8717df7ff8c82e3a6242b4`
- accepted tag: annotated `ct-v2-11-longitudinal-retrieval-context-packets`
- tag object: `3126ceb29800c734ee2ac82b197f68ec0252cc93`
- tag target: `20559d2637b454c4bc86f362a1101b1b804a6f8c`
- worktree before implementation: clean
- remotes: zero
- canonical forward plan: 25,031 bytes; SHA-256 `bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886`
- canonical forward-plan copies: one; root `fdp.txt`: absent
- V1 migration register: 24 components, 24 `DENIED`, zero approved
- canonical Git verifier: passed; root `C:/Android Studio Projects/ConundrumThomasV2`; metadata `.git`; temporary metadata directories zero
- pre-change full qualification: `gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain`, successful, 341 actionable tasks executed

The canonical `.git` directory was never moved, renamed, redirected, hidden, or replaced during CT-V2-12.

## Architecture and ordered authority

The pure-JVM composition graph is:

```text
:thomas:therapy-longitudinal
    -> :thomas:domain
    -> :thomas:engine
    -> :thomas:safety
    -> :thomas:context-packet
    -> :thomas:retrieval
    -> :thomas:language-evidence
    -> :thomas:longitudinal-admission
    -> :thomas:longitudinal
    -> :thomas:ontology
    -> :thomas:provenance

:qualification
    -> :thomas:therapy-longitudinal
    -> :thomas:longitudinal-store
```

`GovernedLongitudinalTherapyPipeline` is the sole complete CT-V2-12 composition root. The integration module receives only typed capture, language-processing, safety, Therapy-policy, and context-packet ports. No store implementation, JDBC/SQL type, DAO, transaction, or projection mutator crosses into it.

The execution trace enforces:

1. establish the pre-turn historical revision;
2. attempt exact current user-source capture;
3. process eligible committed text through CT-V2-08;
4. evaluate CT-V2-04 current-turn safety;
5. select the CT-V2-05 route and progression state;
6. request CT-V2-11 Therapy context at the pre-turn revision;
7. apply the independent CT-V2-12 memory-use gate;
8. construct a typed plan and renderer-support envelope.

The route evaluator has no retrieval input, the retrieval port is not called until route selection has completed, and each is called exactly once. A large archive, relevant history, misleading lexical history, contradiction, or private match therefore cannot alter safety, permit, route, action, technique, or progression. Safety preemption ends ordinary route and retrieval processing.

## Current-turn capture and provenance

The qualification adapter maps each accepted current user turn to one stable CT-V2-07 source identity and revision with:

- acquisition mode `THERAPIST_CONVERSATION`;
- author `USER`;
- capture origin `TYPED` or `SPEECH_TRANSCRIPT`;
- exact committed text;
- session and turn references;
- report time and store-assigned record time;
- honest extracted event-time semantics;
- explicit `ELIGIBLE` or `PRIVATE` state;
- synthetic-qualification-only classification.

Its only durable source write is one call to `store.admission.submit`. Eligible language-derived evidence continues through the existing CT-V2-08 pipeline and CT-V2-07 gate. No separate Therapy database or source history exists.

The current source is admitted before retrieval but is not historical memory for its own turn: retrieval is pinned to `preTurnHistoricalRevision`, and the integration rejects a packet containing the current source revision. The following turn may retrieve it when otherwise eligible.

A private current turn remains available as immediate conversational input, skips ordinary CT-V2-08 derivation, and is excluded from future ordinary retrieval. Capture failure is reported and degrades to a valid memoryless CT-V2-05 plan. Post-capture processing failure preserves the source and also degrades without ambiguous retrieval. Assistant plans, questions, rendered responses, memory references, routes, and session operational state have no source-admission path.

## Longitudinal memory policy

Typed intents are `ORDINARY`, `EXPLICIT_RECALL`, and `EXPLAIN_THOMAS_VIEW`. Retrieval availability and memory surfacing are separate decisions. Dispositions distinguish absent, unavailable, irrelevant, planner-only, surfaced, explicit-recall, explanation, safety-suppressed, and policy-suppressed context.

Ordinary automatic surfacing requires a direct resolved structural relation:

- entity continuity;
- event continuity;
- relationship continuity;
- a qualified reported-recurrence relation; or
- an explicit user target.

Lexical overlap, temporal proximity, shared emotion, generic topic, city-name equality, intensity, and unresolved identity are insufficient. Ordinary output is capped at one surfaced historical object; zero is normal. Hypotheses and contradiction objects are not automatically surfaced as ordinary truth. Recurrence remains a neutral reported recurrence and cannot become a trait. Contradictory or contested material is omitted when it cannot be represented honestly within budget.

Explicit recall and explanation remain CT-V2-11-bounded at four items. Explanation preserves lifecycle, support, counterevidence, corrections, provenance, and uncertainty. It does not strengthen a hypothesis. Retired, review-required, corrected, private, identity-ambiguous, approximate, and dated material retains its governed status.

The ephemeral session state records surfaced object, turn, reason, relation, evidence-meaning token, reinvocation, and connection rejection. Ordinary automatic use surfaces the same object at most once per session unless the user explicitly invokes it or materially changed evidence changes its meaning. A rejected tentative connection is suppressed for later ordinary turns; explicit user recall may reopen it without altering evidence. This state is operational and has no longitudinal write path.

## Plan and renderer boundary

`LongitudinalTherapyPlan` is immutable typed data containing current capture, pre-turn revision, CT-V2-04 safety reference, CT-V2-05 route/progression reference, CT-V2-11 packet summary and digest, memory-use result, surfaced grounding, degradation states, ordered trace, next ephemeral session state, policy version, and canonical SHA-256 logical digest.

The plan digest excludes database row order, machine path, process identity, map/set iteration order, and execution duration. Same logical inputs at the same pre-turn revision produce the same plan; correction, privacy, or governed history changes may change it.

The future renderer receives only `TherapyRenderSupportEnvelope`: the already-authorized CT-V2-05 render command, current-turn text, policy-authorized support, and explicitly surfaced memory references. It never receives the complete ContextPacket, store, retrieval port, private or unsurfaced items, safety implementation, route implementation, or write authority. Historical excerpts remain source data and cannot become instructions. No prompt string, final prose, renderer, model, or Android integration is implemented.

## Failure and degradation

- capture failure: explicit `CURRENT_SOURCE_CAPTURE_FAILED` plus `BASE_THERAPY_PLAN_AVAILABLE`; no retrieval
- language failure after capture: source survives; explicit processing degradation; no retrieval
- retrieval unavailable: unchanged route plus memoryless base plan
- invalid/self-containing packet: rejected packet plus memoryless base plan
- empty or weak context: valid plan with no surfaced memory
- memory-use rejection/suppression: valid plan without unauthorized reference
- safety preemption: safety result retained; ordinary route and retrieval do not execute

Memory removal never makes the base Therapy decision undefined. Retrieval or surfacing frequency never alters confidence, lifecycle, recurrence, route, technique, safety, or evidence.

## Synthetic qualification corpus

All 129 named CT-V2-12 acceptance executions passed. They cover memoryless operation, typed and speech-transcript capture, assistant non-evidence, capture and processing failure, same-turn exclusion, safety/route/progression independence, direct versus weak memory relations, one-memory maximum, recurrence discipline, session anti-repetition and rejected connections, corrections, contradictions/counterevidence, identity uncertainty, temporal honesty, private current and historical sources, explicit recall, explanation, mode/provenance separation, response/evidence-loop exclusion, large histories, multi-turn progression, replay/reopen, plan-digest reproducibility, and CT-V2-04 through CT-V2-11 regressions.

Adversarial fixtures include emotionally intense irrelevant history, common-name collisions, corrected beliefs, retired hypotheses, assistant text, user-authored prompt-injection and mode/technique/doctor instructions, private high-relevance material, contradictory narratives, one-off events, recurrence-as-trait attempts, third-party motive beliefs, diagnostic self-labels, `always`/`never`, and historical crisis vocabulary. Each remains data and fails safely at the appropriate authority boundary.

No randomized or property-test seed was used; all qualification inputs are fixed and deterministic.

Large-history evidence:

```text
sources=400
derived_objects=400
candidates=1
selected=1
packet_text_characters=104
source_excerpts_max=4
maximum_traversal_depth_used=0
surfaced_memories=1
logical_plan_characters=5957
elapsed_qualification_time_ms=5
```

Elapsed time is desktop/JVM fixture evidence only, not an Android or production SLA. Hundreds of unrelated additions did not alter the CT-V2-05 route, selected core memory, packet bound, or ordinary one-memory ceiling.

Close/reopen, empty-store replay, and repeated use of the same pre-turn revision produced equivalent logical plans and digests. A governed correction changed the next packet and plan digest while retaining historical auditability.

## Final build evidence

Exact command:

```text
.\gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
```

Result:

- `BUILD SUCCESSFUL in 1m 31s`
- 344 actionable tasks; 344 executed
- 50 JUnit XML suites
- 741 tests total
- 129 new CT-V2-12 acceptance executions
- failures: 0
- errors: 0
- skipped: 0, including zero skipped CT-V2-12 cases
- aggregate test time: 15.502 seconds
- CT-V2-12 test time: 0.594 seconds
- lint tasks: successful; zero new errors/fatals
- debug and unsigned release APKs assembled
- runtime provenance database generation successful

APK identities:

- `app-debug.apk`: 31,205,813 bytes; SHA-256 `e5dc7ca705ce5e6e3bda3173eee255d0abe4632241b4564b0dd20a1dfceea049`
- `app-release-unsigned.apk`: 22,738,930 bytes; SHA-256 `93cc9a613966ebb757c3bf6db363f338c5368363c8b2d8aab1c12386bb838a1d`

No device, emulator, Android longitudinal Therapy, real-user memory, model-rendering-quality, clinical, therapeutic-efficacy, production-security, or production-performance qualification was performed or claimed.

## Artifact audit

- APK Therapy-longitudinal-fixture/context-packet-dump/qualification-database hits: 0
- APK model/embedding artifact hits: 0
- tracked databases: 0
- tracked packet dumps: 0
- tracked model/embedding artifacts: 0
- tracked build outputs: 0
- tracked PDF/DOC/DOCX/RTF raw-source artifacts: 0
- generated qualification databases tracked: 0
- application backup: disabled (`allowBackup=false`)
- forward-plan size/hash: unchanged
- V1 register: 24/24 `DENIED`, zero approved

## Authority and bypass audit

- production longitudinal Therapy composition roots: 0
- Android/app longitudinal Therapy roots: 0
- qualification longitudinal Therapy roots: exactly 1 (`GovernedLongitudinalTherapyPipeline`)
- Therapy source-admission sites in that root: exactly 1, delegated to CT-V2-07
- model Therapy-route paths: 0
- model memory-selection paths: 0
- model retrieval-query paths: 0
- memory-to-safety mutation paths: 0
- memory-to-Therapy-route mutation paths: 0
- memory-to-technique-selection paths: 0
- retrieved-item-to-evidence-write paths: 0
- assistant-response-to-user-evidence paths: 0
- current-turn self-history paths: 0
- session-memory-state-to-evidence paths: 0
- direct Therapy SQL/JDBC writers: 0
- Android API dependencies: 0
- V1 Therapy integration paths: 0
- real-user records: 0
- production longitudinal authority: 0

The single CT-V2-12 engine call sequence is safety, policy, then context packet. The policy call has no packet parameter. Source scans found no SQL/JDBC, SQLite, Android, model, GGUF, prompt, renderer, network, or V1 implementation dependency in the module or qualification adapter. App, platform, and runtime references to CT-V2-12 are zero.

## VCS and integrity

- canonical project root: `C:/Android Studio Projects/ConundrumThomasV2`
- canonical Git directory: `.git`
- temporary Git metadata directories: 0
- `.git` was never relocated or redirected during CT-V2-12
- remotes: zero
- `git diff --check`: passed before sealing
- canonical-root verifier: passed before sealing

Final tag identity, clean-worktree state, and post-seal `git fsck --full --strict` are recorded in the Principal completion report because sealing occurs after this evidence document is committed.

## Limitations

The result is synthetic-only and deterministic. It contains no final language renderer, model, prompt, GGUF, embeddings, Android Therapy integration, real-user longitudinal data, production database, new therapeutic route or technique, new safety procedure, longitudinal risk prediction, Pattern Engine, diagnostic authority, production privacy/security lifecycle, or production longitudinal authority.

## Recommendation

The canonical forward plan names the next phase **CT-V2-13 — Governed Language Renderer**. The qualified CT-V2-12 foundation is ready for Principal consideration of CT-V2-13. CT-V2-13 is not opened here.
