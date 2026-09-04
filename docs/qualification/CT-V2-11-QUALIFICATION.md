# CT-V2-11 Qualification

## Disposition

`CT_V2_11_LONGITUDINAL_RETRIEVAL_CONTEXT_PACKETS_COMPLETE`

CT-V2-11 is qualified only for deterministic synthetic use. It grants no production retrieval, Android integration, model search, longitudinal-write, therapeutic-route, Biographer-target, clinical, real-user, or production-security authority.

## Accepted baseline

- branch: `main`
- starting HEAD: `56fd7fe03ce2470c9889f0dcea2a0710611b40dd`
- starting tree: `a946958a86732406817d903372e9d95b025750b0`
- accepted tag: annotated `ct-v2-10-biographer-coverage-engine`
- tag object: `7e6b91dbbd934db7f5b8a6f97ba630b8ea761938`
- tag target: `56fd7fe03ce2470c9889f0dcea2a0710611b40dd`
- worktree before implementation: clean
- remotes: zero
- canonical forward plan: 25,031 bytes, SHA-256 `bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886`
- canonical forward-plan copies: one; root `fdp.txt`: absent
- V1 migration register: 24 components, 24 `DENIED`
- canonical Git root verifier: passed; root `C:/Android Studio Projects/ConundrumThomasV2`; metadata `.git`; temporary metadata directories zero
- pre-change full qualification: `gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain`, successful, 335 actionable tasks executed

## Qualified architecture

The compile-enforced pure-JVM graph is:

```text
:thomas:context-packet
    -> :thomas:retrieval
        -> :thomas:longitudinal

:qualification
    -> :thomas:context-packet
    -> :thomas:retrieval
    -> :thomas:longitudinal-store
```

`:thomas:retrieval` owns the persistence-independent read port, typed request and anchor contracts, eligibility, deterministic structural/lexical ranking, bounded evidence-neighborhood completion, source-span validation, and reason traces. `:thomas:context-packet` owns immutable typed layers, budget enforcement, excerpt-data authority, safe truncation, and the canonical logical digest.

`QualificationRetrievalPipeline` is the sole CT-V2-11 composition root. It receives the CT-V2-07 `LongitudinalReader`; no admission controller, SQL/JDBC connection, DAO, transaction, projection mutator, or model interface enters either new module. No Android, app, runtime, engine, safety, renderer, speech, Journal, Biographer, Therapy, network, model, or V1 module depends on the new modules.

## Retrieval contract and policy

Requests carry a stable request ID, intent, active mode, explicit store revision, policy version, structured anchors, context budget, optional preselected Biographer target, privacy authority, and `SYNTHETIC_QUALIFICATION_ONLY` authority. Qualified intents are:

- `ORDINARY_MODE_CONTEXT`
- `EXPLICIT_LOOK_BACK`
- `BIOGRAPHER_TARGET_CONTEXT`
- `EXPLAIN_DERIVED_OBJECT`
- `EXPLICIT_SOURCE_RECALL`

Eligibility precedes ranking. Ordinary selection excludes private, retired, superseded, review-required, dependency-blocked, invalidated, non-current-source, declined-as-substance, assistant-authored, and identity-assumption-dependent material. Explanatory and explicit-source audit requests can expose historical lifecycle status without presenting it as current truth. No cache exists, so correction, privacy, retirement, source revision, and identity changes affect the next request at its declared revision.

Ranking uses an inspectable ordered tuple: explicit target; direct dependency; resolved entity; same event; relationship; bounded period; typed predicate; explicit Look Back lexical overlap; coarse temporal relation; mode preference; stable-ID tie-break. Lexical normalization uses `Locale.ROOT`, Unicode alphanumeric tokens, a fixed stop-word set, exact normalized-token matching, and no phrase bonus. It is subordinate to structural relations and never resolves identity.

Journal ordinary historical retrieval is empty. Explicit Look Back may retrieve only eligible, narrowly matched prior Journal material. Biographer retrieval requires an already selected CT-V2-10 target and cannot reprioritize it. Therapy packet formation is qualification-only and cannot affect CT-V2-04 permits, CT-V2-05 route/technique selection, or evidence lifecycle.

## Evidence neighborhood and source excerpts

Selected hypotheses and contradiction objects use deterministic bounded traversal. A selected hypothesis with known eligible counterevidence carries representative support and counterevidence; when the budget cannot preserve this balance, the hypothesis is omitted. Unresolved identities remain separate. Approximate, unknown, and relative time remains typed without invented distances.

Every excerpt carries source ID, exact source revision, acquisition provenance, report time, event time where available, source offsets, revision fingerprint, epistemic role, lifecycle/eligibility, truncation state, and exact source text. Span/revision mismatches are rejected. Truncation is refused when it would drop a material negation or uncertainty marker. Historical text is typed `USER_SOURCE_EXCERPT`, not authority or instruction.

## Context packet

The immutable packet has typed layers for mode authority, supplied higher-authority safety constraints, immediate conversation, eligible runtime state, selected longitudinal objects, source excerpts, and retrieval metadata. Immediate assistant turns can preserve local continuity but cannot become longitudinal evidence.

The versioned default `ContextBudget` is exactly:

- maximum total textual characters: 4,096
- maximum longitudinal objects: 8
- maximum source excerpts: 4
- maximum excerpt characters: 320
- maximum dependency-neighborhood depth: 2
- maximum immediate-conversation items: 6
- maximum runtime-state items: 4

Budget exhaustion causes deterministic omission, not failure or archive expansion. Authority, explicit target state, correction/current lifecycle, epistemic qualifiers, counterevidence, and minimal grounding are protected ahead of secondary context.

The SHA-256 logical digest covers normalized typed packet content and ignores database row order, map/set iteration order, file paths, process identity, and wall-clock timing. Identical requests against identical compatible snapshots produce identical packets and digests. Legitimate correction/privacy changes produce different packets and digests. Packets are ephemeral and are not persisted.

## Synthetic qualification corpus

All 96 named acceptance cases passed. They cover basic selection, privacy and lifecycle exclusions, corrections, contradictions/counterevidence, unresolved and revised identities, honest temporal selection, conservative Journal and explicit Look Back behavior, target-local Biographer packets, bounded Therapy qualification packets, explanation packets, exact excerpts, historical prompt-injection attempts, all budget edges, large archives, mode separation, close/reopen, empty-store replay, as-of revision, and CT-V2-04 through CT-V2-10 regressions.

Adversarial fixtures include common names, unrelated repeated keywords, positive/negated claims, interpretations versus assertions, obsolete beliefs, high-overlap private material, emotionally intense irrelevant material, approximate dates, unresolved pronouns, source revisions, retired hypotheses, contradictory memory, assistant prose, and user-authored instruction-like source text. The deterministic policy omits unsupported candidates.

No randomized/property seed was used; all qualification inputs are fixed and repeatable.

Large synthetic-history evidence:

```text
sources=400
derived_objects=400
candidates=400
selected_objects=8
packet_text_characters=372
source_excerpts=4
maximum_traversal_depth_used=0
elapsed_qualification_time_ms=2
```

Elapsed time is desktop/JVM qualification evidence only and establishes no production latency SLA. Archive growth remained disconnected from packet growth: the selected object and excerpt limits remained fixed when unrelated history was added.

## Final build evidence

Exact command:

```text
.\gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
```

Result:

- `BUILD SUCCESSFUL in 1m 20s`
- 341 actionable tasks; 341 executed
- 49 JUnit XML suites
- 612 tests total
- 96 new CT-V2-11 acceptance executions
- failures: 0
- errors: 0
- skipped: 0, including zero skipped CT-V2-11 cases
- CT-V2-11 test time: 2.172 seconds
- lint tasks: successful; zero new errors/fatals
- debug and unsigned release APKs assembled
- runtime provenance database generation successful

APK identities:

- `app-debug.apk`: 31,205,813 bytes; SHA-256 `e5dc7ca705ce5e6e3bda3173eee255d0abe4632241b4564b0dd20a1dfceea049`
- `app-release-unsigned.apk`: 22,738,930 bytes; SHA-256 `8b392652a6f3b8ae35be1f8c942a03eb3600841c74a64f215482503aadd5d560`

No device, emulator, hardware, Android retrieval, real-user retrieval, model-context-quality, clinical, therapeutic-efficacy, production-security, or production-performance qualification was performed or claimed.

## Artifact audit

- APK retrieval-fixture/context-packet/database/model/embedding filename hits: 0
- tracked build outputs: 0
- tracked database files: 0
- tracked packet dumps: 0
- tracked model/embedding artifacts: 0
- tracked PDF/DOC/DOCX/RTF raw-source artifacts: 0
- generated qualification databases tracked: 0
- application backup: disabled (`allowBackup=false`)
- canonical forward-plan size/hash: unchanged
- V1 register: 24/24 `DENIED`

## Authority and bypass audit

- production retrieval composition roots: 0
- Android/app retrieval composition roots: 0
- model-selected retrieval paths: 0
- model-generated database-query paths: 0
- retrieval write paths: 0
- context-packet write paths: 0
- persisted context-packet paths: 0
- private-to-ordinary-packet paths: 0
- source-excerpt-to-authority paths: 0
- retrieval-to-Therapy-route mutation paths: 0
- retrieval-to-Biographer-target mutation paths: 0
- direct SQL/JDBC paths escaping the qualification adapter: 0
- V1 retrieval paths: 0
- real-user records: 0
- production longitudinal authority: 0
- qualification retrieval composition roots: exactly 1 (`QualificationRetrievalPipeline`)

Source-code scans found no database, admission, model, embedding, prompt, or mutation APIs in retrieval/context production code. Tests prove packets cannot alter mode authority; correction changes current retrieval immediately; private data cannot be selected or excerpted; retired/review-required material cannot masquerade as current; historical instruction-like text remains data; assistant prose cannot support longitudinal claims; stale packet caching is absent; and selection cannot alter evidence, Therapy route, or Biographer target state.

## VCS and integrity

- canonical project root: `C:/Android Studio Projects/ConundrumThomasV2`
- canonical Git directory: `.git`
- temporary Git metadata directories: 0
- `.git` was never relocated or redirected during CT-V2-11
- remotes: zero
- `git diff --check`: passed
- canonical-root verifier: passed

Final tag identity, clean-worktree state, and post-seal `git fsck --full --strict` are recorded in the Principal completion report because sealing occurs after this evidence document is committed.

## Limitations

The result is synthetic-only and deterministic. It has no embeddings, vector search, semantic model, model query/reranking authority, prompt construction, production conversation orchestration, Android retrieval, real-user memory, Therapy runtime integration, new Biographer selection, automatic Journal history recall, production personal-data store, production privacy/security lifecycle, or production longitudinal authority.

## Recommendation

The canonical forward plan names the next phase **CT-V2-12 — Longitudinal Therapist Integration**. The qualified CT-V2-11 foundation is ready for Principal consideration of CT-V2-12. CT-V2-12 is not opened here.
