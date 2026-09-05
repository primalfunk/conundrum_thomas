# Conundrum Thomas V2: current state and rigorous-test readiness

Investigation date: **2026-09-05 (America/Los_Angeles)**. Examined baseline: **c987fbf3e3742124408a8e6f855d82d19e82f0d1**, tree **2081b1868cfa9979c63baa5d333d32ace7badfac**. This is reconnaissance, not a development-phase completion record. No production code, tests, configuration, therapeutic policy, model configuration, or forward plan was changed. No device data was changed. No commit, tag, or push was made.

## 1. Executive disposition

**CTV2_RIGOROUS_TESTING_NOT_READY**

Thomas is a buildable, locally persistent, deterministic Android research application with three visible modes, a substantial governed evidence system, bounded rendering, and a passing 946-test JVM baseline. It is **not yet a coherent three-mode procedural product for the requested full end-to-end campaign**. Two central production bridges are materially incomplete:

1. **Therapy does not project the conversation into the qualified procedural state.** `ProductionTherapyInputBoundary.state` sets every submitted concern to bounded, engaged, new content, no correction, and no summary delivered. It leaves shared understanding, influence, willingness, options, selected option, plan, outcome, pending information, and active route unset. “Listen” can select a reflection; “Understand” and “Practical” cannot traverse their qualified procedures. The input boundary assigns a fresh conversation revision per client submission, bypassing the intended unchanged-evidence repetition check.
2. **Biographer does not feed its qualified coverage engine the stored biography.** `ProductionBiographerPipeline.decide` passes `CoverageEvidence(storeRevision)` with all content/candidate collections default-empty. Runtime always requests `OPEN_STORY`. Targeted coverage, actual gap selection, and answer-dependent investigation progression exist in qualification composition, but are disconnected from the app.
3. **The current safety observation boundary is an explicit declaration adapter, not conversation-grounded assessment.** A persistent UI checkbox supplies all ordinary-scope facts, including absent self-harm/harm-to-others/medical emergency. Current text cannot contradict it in this path. The gate itself remains deterministic and conservative over typed inputs; those inputs are not sufficiently grounded for unrestricted conversational testing.

These are observable code-path limitations, not speculation about whether an LLM would improve the product. **No model or speech implementation is admitted or present. Their absence alone would not block a deterministic typed-product test campaign.**

The rational immediate order is **Production Evidence-to-Procedure and Biographer Coverage Integration**, narrowly completing those bridges and their acceptance checks while retaining the existing policy, parser discipline, protected store, and deterministic renderer. It is not a Pattern Engine, model-admission, speech, or clinical-policy expansion order. Focused adversarial, Journal, persistence, and renderer testing is useful now; it must not be labeled qualification of a complete longitudinal Therapist or guided Biographer.

Evidence anchors used throughout this report:

| ID | Repository evidence and relevant symbols |
| --- | --- |
| E1 | [ProductionTherapyInputBoundary.kt](../../thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ProductionTherapyInputBoundary.kt), `state` (line 27), `safety` (line 48) |
| E2 | [ThomasProductionRuntime.kt](../../thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ThomasProductionRuntime.kt), `submit`, `nextBiographerPrompt` (122), `submitBiographer`, `submitTherapy` (432), `anchors` |
| E3 | [ProductionBiographerPipeline.kt](../../thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ProductionBiographerPipeline.kt), `decide` (88), `captureAnswer` |
| E4 | [CoreOrdinaryPolicy.kt](../../thomas/engine/src/main/kotlin/com/conundrum/thomas/v2/engine/ordinary/CoreOrdinaryPolicy.kt), `CoreActionRuleCatalog`, `CoreOrdinaryTherapyEvaluator.evaluate/selectWithProgression`; [CoreOrdinaryState.kt](../../thomas/engine/src/main/kotlin/com/conundrum/thomas/v2/engine/ordinary/CoreOrdinaryState.kt) |
| E5 | [ThomasViewModel.kt](../../app/src/main/java/com/conundrum/thomas/v2/ThomasViewModel.kt), `selectMode`, `submit`, `resetAllPersonalData`, `notifyCustodyStatus`, `reviseSource`; [MainActivity.kt](../../app/src/main/java/com/conundrum/thomas/v2/MainActivity.kt), `TherapyControls` and `DataCustodyDialog` |
| E6 | [ThomasAndroidCompositionRoot.kt](../../app/src/main/java/com/conundrum/thomas/v2/ThomasAndroidCompositionRoot.kt), `openRuntime`, `resetAndReopen`, `replaceFromProtectedBackup`; [AndroidDataCustodyController.kt](../../app/src/main/java/com/conundrum/thomas/v2/AndroidDataCustodyController.kt) |
| E7 | [CT-V2-15 qualification](../qualification/CT-V2-15-QUALIFICATION.md) and [target-device evidence](../qualification/CT-V2-15-TARGET-DEVICE-EVIDENCE.md) |
| E8 | [CTV215ProductionRuntimeQualificationTest.kt](../../qualification/src/test/kotlin/com/conundrum/thomas/v2/qualification/CTV215ProductionRuntimeQualificationTest.kt) and [CTV215LifecycleAndAuthorityQualificationTest.kt](../../qualification/src/test/kotlin/com/conundrum/thomas/v2/qualification/CTV215LifecycleAndAuthorityQualificationTest.kt) |
| E9 | [GovernedBiographerPipeline.kt](../../qualification/src/main/kotlin/com/conundrum/thomas/v2/qualification/biographer/GovernedBiographerPipeline.kt), `coverageEvidence` (325), `captureAnswer`, `recordOperationalOutcome`, `changeCoverage` |
| E10 | [GovernedLanguageEvidencePipeline.kt](../../thomas/language-evidence/src/main/kotlin/com/conundrum/thomas/v2/languageevidence/GovernedLanguageEvidencePipeline.kt), `process/formState`; [ConservativeLanguagePerception.kt](../../thomas/language-evidence/src/main/kotlin/com/conundrum/thomas/v2/languageevidence/perception/ConservativeLanguagePerception.kt) |
| E11 | [LongitudinalTherapyIntegrationEngine.kt](../../thomas/therapy-longitudinal/src/main/kotlin/com/conundrum/thomas/v2/therapylongitudinal/LongitudinalTherapyIntegrationEngine.kt), `integrate/capture`; [ProductionTherapyPipeline.kt](../../thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ProductionTherapyPipeline.kt) |
| E12 | [GovernedLanguageRenderer.kt](../../thomas/language-renderer/src/main/kotlin/com/conundrum/thomas/v2/languagerenderer/GovernedLanguageRenderer.kt), `render/fallback`; [DeterministicRenderValidator.kt](../../thomas/language-renderer/src/main/kotlin/com/conundrum/thomas/v2/languagerenderer/DeterministicRenderValidator.kt) |
| E13 | [ProtectedPersonalDataStoreImpl.kt](../../thomas/personal-data-persistence/src/main/kotlin/com/conundrum/thomas/v2/personaldata/ProtectedPersonalDataStoreImpl.kt), `ProtectedPersonalDataStoreFactory`, `submit/reset`; [PersonalDataDocumentCodec.kt](../../thomas/personal-data-persistence/src/main/kotlin/com/conundrum/thomas/v2/personaldata/PersonalDataDocumentCodec.kt) |

**Evidence labels:** “current execution” means commands run in this investigation; “recorded device” means E7's preserved observations; “code-derived” means a trace through present source without a new behavior test. Passing tests establish their asserted cases, not every claim in their suite names. “PRODUCTION” below means reachable from the actual app, **not** clinical or commercial production authority.

## 2. Repository state

| Field | Observed value |
| --- | --- |
| Canonical root | `C:\Android Studio Projects\ConundrumThomasV2` |
| Git metadata | `C:\Android Studio Projects\ConundrumThomasV2\.git`; ordinary directory |
| Branch / HEAD | `main` / `c987fbf3e3742124408a8e6f855d82d19e82f0d1` |
| HEAD tree | `2081b1868cfa9979c63baa5d333d32ace7badfac` |
| Entry worktree | Clean; staged changes 0, unstaged changes 0, untracked files 0 |
| Remote | `origin` fetch/push: `https://github.com/primalfunk/conundrum_thomas.git` |
| Upstream | `origin/main` at `64fa3af9069f820fd465f219e5a21855a7d47047` |
| Ahead / behind | 14 / 0 against the **local remote-tracking ref**; no fetch performed |
| Latest commit | `c987fbf`, “docs(qualification): finalize CT-V2-15 seal evidence”, 2026-09-04 22:36:19 -07:00 |
| Latest annotated tag | `ct-v2-15-android-production-integration`, created 2026-09-04 22:37:01 -07:00; target equals HEAD |
| Reachable tags | All 17 tags in section 3.2: starter plus CT-V2-00 through CT-V2-15 |
| Non-reachable tags | 0, from `git tag --no-merged HEAD` |
| Visible history | 79 commits from `git rev-list --all --count`; no later V2 branch/tag frontier found |
| Canonical verifier | `.\tools\verify-canonical-git-root.ps1` passed; `VCS_ROOT_ON_DISK_VALID=true`, temporary metadata count 0 |
| Whitespace integrity | `git diff --check` exit 0 |
| Object integrity | `git fsck --full --strict` exit 0; no diagnostic output in this investigation |
| Local agent instructions | No `AGENTS.md` found in the workspace search |
| Exit change policy | Only this previously absent report is added, uncommitted. Build/provenance outputs remain ignored |

The sandbox initially failed before executing any shell command with `helper_unknown_error: setup refresh had errors`. Inspection and qualification then used approved host execution. The initial inventory was read-only. No reset, clean of Git, stash, historical checkout, metadata move, history rewrite, installation, or push occurred. Gradle's canonical `clean` ran only after repository/architecture/qualification inspection.

### Historical object identities are not current object identities

The repository contains `.git/filter-repo/commit-map` and `ref-map`. They explain old hashes still present in phase documents. For example:

| Historical reference | Current mapped object | Evidence |
| --- | --- | --- |
| Starter commit `33e3b601cf9dc535436c6874168dcb138208e1ae` | `e664f3a243e6c8565df7df56ad88c1a59460756c` | commit-map; same starter tree |
| Foundation completion `990bf8a61533134c5e713b5c384e6eaa10b955ee` | `102d609b508f8f24e5ded0196f296a86099558e9` | commit-map |
| CT-V2-13 accepted `36393ab51b4f10b5133f8342a25b6d27355c555a` | `49f4e9356341b8995b4d44e0c0129fe61b14c2a4` | commit-map; tree `2ea6f5f79e4a6aced2fc0bbdcc83e92e753c3da2` |
| CT-V2-13 tag object `636b535bbaa745d187fefdd495a116667a014bcc` | `9472c54020c5cfbee0c5e4d367c24102a52ca032` | ref-map |

[CT-V2-14 qualification](../qualification/CT-V2-14-QUALIFICATION.md) explicitly records the sanitized predecessor and intervening public preparation. Old hashes in CT-V2-00–13 reports are historical evidence, not evidence of a missing current seal. Current Git identities below take precedence. No sanitization was performed in this order.

## 3. Complete phase lineage

### 3.1 Capabilities, authority, and dispositions

The exact phase titles below follow the preserved phase documents. “WO” means a separately preserved work order or authority-scope record in `docs/work-orders`; foundation scope documentation is identified separately. Every phase has a qualification report. Some older reports use prose rather than a machine completion token: **no token is invented**. “No production authority” refers to the grant at that phase, before later technical Android composition.

All CT-V2-00–14 device fields are **NO** at their original qualification; CT-V2-15 is **PARTIAL** (specific TCL synthetic scenarios, not complete capability qualification). **Model involved: NO for every V2 phase**, including CT-V2-13/15; deterministic reference realization is not a model.

| Phase / exact title | WO / qualification | Final disposition in record | Main introduced capability | Production authority granted / explicitly denied | Limitations and later supersession |
| --- | --- | --- | --- | --- | --- |
| CT-V2-00 — foundation scope and gates | WO no separate file; [scope](../CT-V2-00-FOUNDATION.md); [Q00](../qualification/CT-V2-00-FOUNDATION-QUALIFICATION.md) yes | `PASS_WITH_DISCLOSED_DEVICE_LIMITATION` | Independent V2 starter, modules, mode/render authority skeleton, V1 denial register | Foundation only; `NO_V1_CODE_MIGRATION_AUTHORIZED`, `NO_THERAPEUTIC_IMPLEMENTATION_AUTHORIZED` | Starter Android shell; no device. Superseded app/runtime shell in 15; original boundary thesis retained |
| CT-V2-01 — Governed therapeutic source and provenance corpus | WO no separate file; [phase](../CT-V2-01-GOVERNED-SOURCE-CORPUS.md); [Q01](../qualification/CT-V2-01-QUALIFICATION.md) yes | No machine completion token recorded; governed corpus qualified | 19 documents, 21 versions, 8 authorities, source/rights/review schema and seeds | Sources/provenance only; no clinical approval, commercial permission, therapy, model authority | Reviews pending; later rule bindings consume records without completing reviews |
| CT-V2-02 — Therapeutic ontology | WO no separate file; [phase](../CT-V2-02-THERAPEUTIC-ONTOLOGY.md); [Q02](../qualification/CT-V2-02-QUALIFICATION.md) yes | No machine token; “meets its structural and anti-authority gates” | 109 concepts, 13 provenance bindings, three-mode contract | `PROVENANCE_ONLY` / `NOT_AUTHORIZED` bindings; runtime-authorized concepts 0 | Vocabulary not behavior. Later engine consumes concepts under bounded qualification authority |
| CT-V2-03 — Procedural Therapist vertical slice | [authority scope](../work-orders/CT-V2-03-AUTHORITY-SCOPE.md) yes; [Q03](../qualification/CT-V2-03-QUALIFICATION.md) yes | No machine token; complete deterministic qualification-only architecture proof | 21 rules, 13 actions/stages, deterministic bounded-problem sequence | `EXECUTABLE_FOR_QUALIFICATION`; `NOT_GRANTED` production therapy | No natural-language observation/specialized safety. 04 adds mandatory permit; 05 ordinary evaluator supersedes production use of vertical-slice evaluator |
| CT-V2-04 — procedural safety and scope gate | [scope](../work-orders/CT-V2-04-AUTHORITY-SCOPE.md) yes; [Q04](../qualification/CT-V2-04-QUALIFICATION.md) yes | No machine token; complete qualification-only safety/scope gate | 14 rules, revision-bound permit, one clarification action, typed boundaries | Qualification execution; `PRODUCTION_AUTHORITY_NOT_GRANTED` | No raw-prose risk classifier or crisis procedure. Mechanically composed in 15; reviews and clinical denial retained |
| CT-V2-05 — core ordinary therapeutic repertoire | [scope](../work-orders/CT-V2-05-AUTHORITY-SCOPE.md) yes; [Q05](../qualification/CT-V2-05-QUALIFICATION.md) yes | No machine token; complete qualification-only core repertoire | 9 route rules, 26 action rules, 4 progression guards, 25 actions, 12 goals, 19 transitions | `EXECUTABLE_FOR_QUALIFICATION`; `NOT_GRANTED` production therapy | Requires trustworthy structured state; standalone decision support denied. Composed in 12/15; production observation/progression remains incomplete |
| CT-V2-06 — longitudinal evidence foundation | [scope](../work-orders/CT-V2-06-AUTHORITY-SCOPE.md) yes; [Q06](../qualification/CT-V2-06-QUALIFICATION.md) yes | No machine token; complete platform-independent evidence-first domain | Sources, assertions, entities, three time axes, corrections, contradictions, hypotheses, dependencies | Domain/fixtures only; no writer, extraction, persistence, retrieval or model authority | Foundation retained and used by 07–15; ontology richness does not prove populated production state |
| CT-V2-07 — governed admission, revision, and longitudinal store | [WO07](../work-orders/CT-V2-07-GOVERNED-ADMISSION-REVISION-AND-LONGITUDINAL-STORE.md) yes; [Q07](../qualification/CT-V2-07-QUALIFICATION.md) yes | `CT_V2_07_GOVERNED_ADMISSION_REVISION_STORE_COMPLETE` | Sole admission gate, revision/lifecycle policy, synthetic SQLite ledger/projection | Synthetic qualification only; production longitudinal/therapeutic authority denied | Android uses 14 protected store instead of synthetic SQLite; admission contracts retained |
| CT-V2-08 — Language-to-Evidence & State Formation | [WO08](../work-orders/CT-V2-08-LANGUAGE-TO-EVIDENCE-AND-STATE-FORMATION.md) yes; [Q08](../qualification/CT-V2-08-QUALIFICATION.md) yes | No machine completion token in final report; successful qualification with limits | Conservative exact-span parsing, epistemic/temporal formation, contradiction/identity/gap state | Synthetic source-first proof; model perception explicitly prohibited; no production writer at phase | Implemented longitudinal formed state, not the forward plan's full CT04/05 observation bridge. Shared pipeline composed in 15 |
| CT-V2-09 — Journal Capture Engine | [WO09](../work-orders/CT-V2-09-JOURNAL-CAPTURE-ENGINE.md) yes; [Q09](../qualification/CT-V2-09-QUALIFICATION.md) yes | No machine completion token in final report; successful synthetic qualification | Source-first capture/revision/privacy, silence, reflection, one question | Synthetic only; no production/Android storage, Therapy expansion, model/speech admission | Technical app path introduced in 15; parser remains bounded; only three postures |
| CT-V2-10 — Biographer Coverage Engine | [WO10](../work-orders/CT-V2-10-BIOGRAPHER-COVERAGE-ENGINE.md) yes; [Q10](../qualification/CT-V2-10-QUALIFICATION.md) yes | `CT_V2_10_BIOGRAPHER_COVERAGE_ENGINE_COMPLETE` | Open story, 13 target kinds, deterministic coverage/ranking, answer/history semantics | Deterministic synthetic only; no Android, clinical, model or real-user authority | Open-story capture composed in 15; qualified evidence-to-coverage and targeted path not carried into production |
| CT-V2-11 — Longitudinal Retrieval & Context Packets | [WO11](../work-orders/CT-V2-11-LONGITUDINAL-RETRIEVAL-AND-CONTEXT-PACKETS.md) yes; [Q11](../qualification/CT-V2-11-QUALIFICATION.md) yes | `CT_V2_11_LONGITUDINAL_RETRIEVAL_CONTEXT_PACKETS_COMPLETE` | Purpose/eligibility/ranking, budgeted read-only packets | Synthetic read only; no write, route, target, model or production grant | Therapy uses this in 15; other mode-specific packet uses remain qualified capabilities |
| CT-V2-12 — Longitudinal Therapist Integration | [WO12](../work-orders/CT-V2-12-LONGITUDINAL-THERAPIST-INTEGRATION.md) yes; [Q12](../qualification/CT-V2-12-QUALIFICATION.md) yes | `CT_V2_12_LONGITUDINAL_THERAPIST_INTEGRATION_COMPLETE` | Source capture, safety, fixed route, retrieval, conservative memory gate, render-support envelope | Deterministic synthetic plans only; no new technique/safety, final renderer, model or Android authority | Route-first restriction intentionally narrower than original plan's memory-changing-procedure ambition. Runtime inputs remain incomplete |
| CT-V2-13 — Governed Language Renderer | [WO13](../work-orders/CT-V2-13-GOVERNED-LANGUAGE-RENDERER.md) yes; [Q13](../qualification/CT-V2-13-QUALIFICATION.md) yes | `CT_V2_13_GOVERNED_LANGUAGE_RENDERER_COMPLETE` | Typed semantic commands, reference forms, untrusted candidate validation, fallback | Synthetic deterministic architecture; no model, Android renderer, real-user/clinical grant | Deterministic renderer composed in 15; arbitrary model semantics never qualified |
| CT-V2-14 — Personal Data Governance & Persistence Qualification | [WO14](../work-orders/CT-V2-14-PERSONAL-DATA-GOVERNANCE-AND-PERSISTENCE-QUALIFICATION.md) yes; [Q14](../qualification/CT-V2-14-QUALIFICATION.md) yes | `CT_V2_14_PERSONAL_DATA_GOVERNANCE_PERSISTENCE_COMPLETE` | AES-GCM store, AndroidKeyStore/AtomicFile adapter, source deletion, backup/export/restore/reset, migration/recovery | Production-capable persistence boundary; Android product composition and real-user ingestion explicitly denied at this phase | JVM/security inspection only at 14; selected TCL behaviors qualified in 15; custody failure gaps remain |
| CT-V2-15 — Android Production Integration | [WO15](../work-orders/CT-V2-15-ANDROID-PRODUCTION-INTEGRATION.md) yes; [Q15](../qualification/CT-V2-15-QUALIFICATION.md) yes | `CT_V2_15_ANDROID_PRODUCTION_INTEGRATION_COMPLETE` | One Android runtime, typed input, deterministic three-mode UI, protected store and custody; TCL qualification | `ANDROID_PRODUCTION` technical composition in mode/render contracts; model/speech, clinical approval, new policy, real-data qualification, V1, cloud, signing/release denied | Latest seal; no later numbered phase. Seal accepted scope exists, but complete production behavior overclaimed (sections 7–10, 14, 20) |

### 3.2 Current immutable seals

Every row is an annotated tag. Completion commit is its dereferenced target, and tree was resolved from that target during this investigation. This table supplies the exact completion/tree/tag-object fields for section 3.1.

| Annotated tag / created date | Tag object | Completion commit / target | Tree |
| --- | --- | --- | --- |
| `ct-v2-root-baseline` (2026-09-03T00:46:44-07:00) | `52f64630f45242b27c38e6379e424b38eeca6a02` | `e664f3a243e6c8565df7df56ad88c1a59460756c` | `d1cd0b15cbc78438dc44997be4e34aadcc3abc6e` |
| `ct-v2-00-foundation` (2026-09-03T01:15:39-07:00) | `128cfa3d4fe80335257ef374ac1b3d627c96094d` | `102d609b508f8f24e5ded0196f296a86099558e9` | `de9bffa56f111f50093874bcc26ee84af955e11a` |
| `ct-v2-01-governed-source-corpus` (2026-09-03T09:32:36-07:00) | `4530a8dc3dcc8290ef6a99f96d932b3df5c76e7f` | `fc5646b05297e228514b55addb20b4ba72e65808` | `7c7556a229097960bf3216a0435a9e0ca37d3082` |
| `ct-v2-02-therapeutic-ontology` (2026-09-03T10:01:19-07:00) | `90737b5820423fdbc33ecc223294c027527a789c` | `e86543ae792d3ced66c25b687746b64e50756fad` | `8d20fe8bad6f04096c4e8f89af6f89ac9a704653` |
| `ct-v2-03-procedural-vertical-slice` (2026-09-03T10:47:29-07:00) | `7e666e9e6c73bf186c0605282a2f39c183239cf3` | `9247c22052598bf80477031df97f50b341b4a377` | `38cbc8b8e0035743b718f0e426216d13082a6267` |
| `ct-v2-04-safety-scope-gate` (2026-09-03T11:32:13-07:00) | `fd94f7d5b923a7986bdd2c5ebae7b13e20d9f19f` | `2a19d13c55a7d6a516fa014db21cb91cdb0b26fc` | `654061136a9c97830d5a79818472a8b491a55cf1` |
| `ct-v2-05-core-ordinary-therapy` (2026-09-03T12:25:37-07:00) | `95778b5abc2eac16ca13287dd05da2b164123b7c` | `762e5473bf9bb9290200438a7a0881acc3528b5a` | `a58d1ebc0018f31f17ede281adc46d3311d18953` |
| `ct-v2-06-longitudinal-foundation` (2026-09-03T12:57:52-07:00) | `4fa63a81c034dc5c34a9da31bdd81aa6a3200957` | `8f3633d93c81a44534c84b6c8215d99877278f06` | `b3d0efea7987770c62d9026988dd8fb7863004cb` |
| `ct-v2-07-governed-longitudinal-store` (2026-09-03T14:17:52-07:00) | `391b7fcc38d389090aa29e0a93fd4b578862bf52` | `69be80b2727b93f37ade68ad41d90e4be4855712` | `e0d0226f85eca603ed511d05a501ee2d09e04fd8` |
| `ct-v2-08-language-evidence-state-formation` (2026-09-03T16:19:32-07:00) | `26f4867697a175332321533182506399b7470ec1` | `ceca15078cf43d29a2a68c72865b44caa66185a6` | `5172149c0c6ad96bcf8224bfd8e82cd005bb561f` |
| `ct-v2-09-journal-capture-engine` (2026-09-03T17:58:53-07:00) | `bb9856a48061dad4c4c644d3abc3081412a632c6` | `303db32b483e28716107b9c8d1c7d139550e2db5` | `184c163d358ead8955e9340f23694b8f0dd7b50f` |
| `ct-v2-10-biographer-coverage-engine` (2026-09-03T20:00:49-07:00) | `8eb83b28845f4e8f74a7fd44258997c70e92b9f4` | `8dc05b3eea4918f69a70c04b25cd4b0d6bd9209d` | `a946958a86732406817d903372e9d95b025750b0` |
| `ct-v2-11-longitudinal-retrieval-context-packets` (2026-09-03T22:05:09-07:00) | `dc78bf2b0d7c8ab033c3129ba90bd5ea611ee441` | `d3e2c80b1c7c58991e92a82b6c9ecc04d35944d8` | `fa82db9cc9edd353ee8717df7ff8c82e3a6242b4` |
| `ct-v2-12-longitudinal-therapist-integration` (2026-09-04T11:34:13-07:00) | `a00e90886daa3df0c9a4a5cd828d2cdc1ddfee39` | `ed725cad3a40a8494b6890b4b6a8b88e5b4918d4` | `f380c99f1a1857a1e15b73068ab1b9480ba3b317` |
| `ct-v2-13-governed-language-renderer` (2026-09-04T13:32:59-07:00) | `9472c54020c5cfbee0c5e4d367c24102a52ca032` | `49f4e9356341b8995b4d44e0c0129fe61b14c2a4` | `2ea6f5f79e4a6aced2fc0bbdcc83e92e753c3da2` |
| `ct-v2-14-personal-data-governance-persistence` (2026-09-04T16:39:26-07:00) | `a5a70d722296d6340202b8e18006a9ae43aa4184` | `d8f4b5cb03a9a3a282ea8e8be438376a74a117b9` | `a3bd9fb0ca4ef25a1ed3716ef97cc1773fbe34d4` |
| `ct-v2-15-android-production-integration` (2026-09-04T22:37:01-07:00) | `da376b133bc4cdcc5a77fb3a9eeac5e92ec88bcf` | `c987fbf3e3742124408a8e6f855d82d19e82f0d1` | `2081b1868cfa9979c63baa5d333d32ace7badfac` |

### 3.3 Revisions, repairs, sanitization, and work outside numbered phases

All visible development is accounted for through CT-V2-15. No separate CT-V2-16+, V2 model experiment, later device phase, or unsealed implementation was found in all refs, tags, source inventory, or preserved orders.

| Work | Current commit(s) / evidence | Effect and status |
| --- | --- | --- |
| Starter and foundation compiler confinement | `e664f3a`; `4324afe` | Starter tree sealed; in-process Kotlin compiler behavior; included in 00 seal |
| Canonical forward-plan placement | `c1bc2cd35762d3a0b3333248953801e1c5e16118` | Moved supplied plan without byte changes before 06 |
| 07 transaction/lifecycle hardening | `1d3a1ad`, `ae91d91` | Transaction fault seams and lifecycle-eligible support; included in 07 |
| Android Studio/Git root repair | `7925a4c`; [workspace record](../workspace/CT-V2-08-ANDROID-STUDIO-VCS-ROOT.md) | On-disk canonical root/mapping verified; IDE warning disappearance never visually proven. No independent numbered phase/tag |
| 08 qualification artifact reconciliation | `f5a6573`, `ceca150` | Final artifact evidence reconciled; included in 08 |
| Journal boundary/assertion repairs | `60e8989`, `605f342` | Qualified consumer boundary and temporal test assertion; included in 09 |
| Biographer privacy/failure hardening | `a3f7f87` | Private targets/answer failure boundaries; included in 10 |
| Renderer fallback/fidelity/writer-boundary and digest work | `b17f261`, `1b43b8d`, `49f4e93` | Included in 13; no model admission |
| Public-history sanitization and publishing preparation | local filter-repo maps; `8d1c187`, `f93ddc8`, `64fa3af`; Q14 entry discrepancy | Ignore/public hygiene, Apache 2.0/NOTICE, README; rewritten older object identities with accepted trees retained. Not new therapeutic capability |
| Persistence interruption/export/reset hardening | `28efd4e`, `f84b873` | Included in 14 |
| Runtime and Android composition | `4d16acc`, `9553570` | Latest substantial code frontier, included in 15 |
| Final Android/audit repair | `534a1d2` | Changed ViewModel and Biographer authority test, including direct-dependency audit; title alone understates ViewModel changes |
| Seal-evidence normalization | `6e90064`, `1182179`, `c987fbf` | Documentation only; included in 15 |
| TCL qualification | 15 device evidence, `6e90064` | Physical synthetic evidence, not an independent device phase |
| V1 R009D/R009E, historical models/speech | [V1 lineage](../lineage/CT-V2-00-LINEAGE.md), [migration register](../../migration/v1-component-register.json) | Reference evidence only; R009D `QUALIFIED_NOT_ACCEPTED`, R009E `DIRTY_UNACCEPTED_EVIDENCE`; 24/24 V1 components `DENIED`; no V2 model/device claim inherited |

## 4. True current frontier

| Requested frontier | Evidence-based answer |
| --- | --- |
| Latest fully sealed phase | CT-V2-15, tag and HEAD exactly equal |
| Latest implemented but unsealed work | None at entry; this report is the only subsequent documentation work |
| Latest qualification report | Q15, last revised at `c987fbf`; device document last changed at `6e9006435e6d52ce1e7c1927cda6d1502c935dac` |
| Latest production composition change | Runtime `4d16acc3abd9c34bd69771c2475706e51721c361` and Android root/UI `9553570b7662fd200e57dfb55112b73c168c9473`; last app-source correction `534a1d225d33294b5878e43c7826faefe5dad1c7` |
| Latest device qualification | CT-V2-15, TCL 9491G, 2026-09-04 |
| Latest model qualification/admission | None |
| Latest therapeutic-policy change | CT-V2-05 ordinary policy, `08faf444801e844b7bbf56104cb4572c557d7b6f`. CT04 gate retained; CT12 adds memory-use policy, not new techniques. CT15 adapter defaults nevertheless affect actual behavior |
| Latest longitudinal-data change | CT14 protected-store/hardening through `f84b873`; CT15 shared protected classification/adapters in `4d16acc` and custody wiring in `9553570` |
| Latest UI/speech change | UI `9553570` and ViewModel `534a1d2`; speech still empty/disabled, no implementation frontier |

HEAD is a **sealed accepted integration state with a freshly passing engineering baseline**, but the seal does not establish the broader product behaviors asked about here. It is a useful **bounded component/integration test baseline**, not the requested complete product test candidate. “No phase beyond 15” is a discovered repository fact, not an assumption from historical conversation.

## 5. Forward plan versus reality

Canonical [forward plan](../planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md):

- Path: `docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md`.
- Size: **25,031 bytes**.
- SHA-256: **bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886**.
- Latest modification commit: **c1bc2cd35762d3a0b3333248953801e1c5e16118**, “Place canonical V2 forward development plan”.
- One canonical tracked copy; root `fdp.txt` absent. Unchanged by this investigation.

The plan was written from the CT-V2-05 frontier. Sequence: **06 evidence/time → 07 admission/store → 08 language/state → 09 Journal → 10 Biographer → 11 retrieval/packets → 12 longitudinal Therapist → 13 renderer → 14 data governance/persistence → 15 three-mode Android → 16 Pattern Engine → 17 authority admission/product qualification → 18+ broader capabilities**.

### A. Planned and completed

06 domain; 07 governed admission and synthetic durability; 08 bounded source-grounded evidence formation; 09 source-first Journal; 10 deterministic coverage engine in qualification; 11 retrieval/packets; 12 route-first integration; 13 deterministic renderer; 14 protected persistence/lifecycle; 15 Android technical composition and selected physical gates. “Completed” here means their actual narrowed phase scopes, not every original plan aspiration.

### B. Planned but still outstanding

- CT08 plan sections 9/11: language/evidence-to-CT04/05 procedural state bridge. Current formed state is longitudinal evidence; runtime fabricates several procedure fields instead.
- CT10/15: stored-biography-informed targeted investigation in the actual application.
- CT12 plan sections 17/18: prior attempts/outcomes meaningfully affecting procedure and governed formulation/outcome revision. Current route/action are intentionally fixed before retrieval; there is no conversation-driven formulation/outcome observer.
- Plan's four Journal response levels: no independent ACKNOWLEDGE control; current three postures are explicitly narrower.
- Speech finish versus discard and TTS: no implementations. Later WO15 explicitly permits disabled speech, so this is outstanding product scope, not an unfulfilled mandatory WO15 speech gate.
- Conversation/mode continuity after process death: only durable corpus survives; transcript/mode/draft/operational state are process-local.
- 16 Longitudinal Pattern Engine; 17 authority admission/product qualification; 18+ broader therapeutic/specialized capability: no implementation/order opened in current history.

### C. Completed work that diverged from or extended the plan

Conservative deterministic perception replaced any assumed model-extraction dependency (WO08 explicitly prohibits an LLM). CT12 deliberately made history unable to choose the route/technique. CT13 completed renderer architecture without a model. CT14 selected an encrypted atomic document store, leaving SQLite in qualification. CT15 admitted technical Android composition plus concrete custody/physical-device gates, without speech or a model. Canonical VCS repair, public-history sanitization/licensing, and failure hardening also materially extended the original sequence.

**Adjudication:** the plan remains useful as a map of ambitions and invariants; its “next authorized phase CT-V2-06” text is historical. Q15's recommendation to consider CT16 is also only a recommendation. Neither overrides the present integration gaps. The plan must not be used as a completion ledger.

## 6. Production architecture and dependency graph

There are **23 included Gradle modules**, from [settings.gradle.kts](../../settings.gradle.kts). Actual dependency declarations, not the older summaries, determine compilation.

| Module | Responsibility / current role | Direct project dependencies (configuration) |
| --- | --- | --- |
| `:app` | Android launch, Compose, ViewModel, composition/custody | runtime, persistence-android, speech-android (`implementation`) |
| `:thomas:runtime` | Sole complete production orchestration | domain, provenance, engine, safety, personal-data-persistence, language-evidence, journal, biographer, retrieval, context-packet, therapy-longitudinal, language-renderer (all `api`) |
| `:thomas:domain` | Mode and original render contracts | None |
| `:thomas:provenance` | Clinical-source identifiers/review authority | domain (`implementation`) |
| `:thomas:ontology` | Therapeutic vocabulary/epistemic/source bindings | domain, provenance (`implementation`) |
| `:thomas:engine` | Ordinary Therapy policy; older vertical-slice library | domain, provenance, ontology, safety (`implementation`) |
| `:thomas:safety` | Typed gate/permit, no ordinary-policy reverse dependency | domain, provenance, ontology (`implementation`) |
| `:thomas:longitudinal` | Personal source/evidence/life/temporal ontology | None |
| `:thomas:longitudinal-admission` | Governed admission, lifecycle/revision policy and shared ports | longitudinal (`api`) |
| `:thomas:personal-data-persistence` | Protected production-capable store, lifecycle/custody | longitudinal-admission (`api`) |
| `:platform:persistence-android` | AndroidKeyStore and AtomicFile | personal-data-persistence (`api`) |
| `:thomas:longitudinal-store` | **Qualification-only** SQLite/JDBC implementation | personal-data-persistence (`api`); SQLite external dependency |
| `:thomas:language-evidence` | Conservative parser, evidence proposals, formed longitudinal state | longitudinal-admission (`api`) |
| `:thomas:journal` | Capture/revision/privacy and response intent | language-evidence (`api`) |
| `:thomas:biographer` | Coverage/target/answer contracts and engine | language-evidence (`api`) |
| `:thomas:retrieval` | Purpose-bound eligible historical reads/ranking | longitudinal, longitudinal-admission (`api`) |
| `:thomas:context-packet` | Typed minimal budgeted packets | retrieval (`api`) |
| `:thomas:therapy-longitudinal` | Route-first capture/safety/policy/memory integration | domain, engine, safety, context-packet, retrieval, language-evidence, longitudinal-admission, longitudinal, ontology, provenance (`api`) |
| `:thomas:language-renderer` | Command adapters, deterministic realizer, validation/fallback | domain, journal, biographer, therapy-longitudinal, longitudinal (`api`) |
| `:platform:renderer-llama-android` | Empty reserved model adapter; **not app dependency** | domain (`implementation`) |
| `:platform:speech-android` | Empty reserved STT/TTS adapter; app dependency but no behavior | domain (`implementation`) |
| `:qualification` | Synthetic fixtures/compositions/adversarial tests | All relevant domain/policy/mode/evidence/store/retrieval/render modules (`implementation`); runtime `testImplementation` |
| `:tools:provenance` | Build-only SQLite provenance generation/tests | provenance `implementation`; ontology, engine, safety `testImplementation` |

No separate therapy-policy module exists: it lives in `engine/ordinary`; safety is separate. No STT/TTS implementation modules beyond the empty shared adapter exist. No V1 production dependency exists. Older CT03 evaluator and qualification renderers remain regression/reference code, not the active app evaluator. No active experimental model dependency was found.

**Boundary discrepancy:** [MODULE-BOUNDARIES.md](../MODULE-BOUNDARIES.md) says compile enforcement prevents broad component reach. Direct declarations are restricted, but the extensive `api` re-exports make policy/contract classes transitively visible to app and renderer. App imports engine enums through runtime; renderer sees engine/safety via therapy-longitudinal. No current renderer policy-evaluation or storage-write call was found, but “zero direct dependency” is **not** proof of “cannot compile a bypass.” The static audit E8 matches selected strings/call sites; it is not whole-program information-flow analysis. The graph is acyclic, yet contains broader compile visibility than the strongest documentation wording suggests.


## 7. Actual Android turn pipeline and feature status

Launch is `MainActivity.onCreate → ThomasApp → ThomasViewModel → ThomasAndroidCompositionRoot.open → AndroidPersonalDataPersistenceFactory.open → ProtectedPersonalDataStore → ThomasProductionRuntime`. There is one runtime owner per ViewModel/root. It opens the protected store before initial UI state. No service, server, model loader, STT, or TTS participates.

### Actual ordered paths

**Common input:** editable mode-local draft (maximum 4,096 characters) → explicit **Commit** → ViewModel snapshots mode/privacy/posture/support/scope checkbox → allocates client index → worker dispatcher → `ThomasProductionRuntime.submit` busy guard → selected mode. The source text is user-authored; mode controls are not evidence assertions. Results return as user transcript artifact, optional validated Thomas artifact, source summaries, and status.

**Journal:** exact source admitted through `JournalCaptureEngine` / production admission adapter → if eligible, conservative perception/proposal validation → governed evidence/contradiction admission → formed longitudinal state → current-entry response intent. Silence bypasses response planner/rendering; private Journal skips derivation and response. A supported reflection/question uses the first grounded assertion → Journal render adapter → deterministic reference realization → deterministic validation/fallback → final visible artifact or explicit no-output. Source capture survives unsupported prose or rendering failure.

**Biographer:** entering mode requests `nextBiographerPrompt → CoverageRequest(OPEN_STORY) + empty CoverageEvidence → open invitation plan → renderer/validator → prompt`. On Commit, exact answer → governed source admission with `BIOGRAPHER_OPEN_NARRATIVE` → eligible conservative evidence processing → clear pending plan → another open-story prompt. **No stored coverage candidates, targeted question selection, or operational answer-outcome update is supplied in the production adapter.** Compare E3 with E9, which does those operations in qualification.

**Therapy (the exact code order matters):**

1. Create CT05 state from current text, selected support, fixed/default values, and action history.
2. Create CT04 safety input from declaration; evaluate safety once in runtime for possible safety rendering.
3. Capture pre-turn store revision; calculate retrieval anchors from current text and eligible pre-turn entity labels.
4. Call `LongitudinalTherapyIntegrationEngine.integrate`: capture exact current source; process eligible text to **longitudinal evidence**, without feeding the result back into step 1/2.
5. Evaluate the same safety input a second time inside integration.
6. If allowed, evaluate CT05 with the already-created state. If no action is selected, stop ordinary rendering/retrieval.
7. If an action exists, retrieve a bounded packet at the pre-turn revision; reject self-containing/invalid packets; apply independent memory-use gate.
8. Update ephemeral action/memory state; adapt authorized policy/surfaced memory or selected safety clarification to a render command.
9. Reference realization → validator → bounded fallback or no-output → final artifact → Compose transcript. **No TTS.**

Thus the documentation's “capture → safety → route → retrieval” describes the integration engine but omits the outer pre-capture safety evaluation and pre-capture anchor read. There is one gate implementation instantiated twice, not two competing safety policies. The duplicate evaluation currently receives identical input. This sequencing and the real input-formation limitations must be represented in tests.

### Current feature classification

| Surface | Classification | Exact usable boundary |
| --- | --- | --- |
| Journal | **PRODUCTION**, bounded | Real source capture, silence, supported reflection/question |
| Biographer | **PARTIAL** | Open narrative capture/prompts; targeted acquisition qualification-only |
| Therapy | **PARTIAL** | Listening/reflection and bounded recall; richer procedures unreachable from normal UI state |
| Mode switching | **PRODUCTION** | Three explicit chips, mode-local drafts and filtered process-local transcript |
| Response controls | **PARTIAL** | Journal 3 postures; Therapy Listen/Understand/Practical controls exist but last two lack procedural state |
| Text input | **PRODUCTION** | Explicit bounded editable draft/commit |
| Speech input/output | **STUB** | Empty modules/disabled button; no recognizer/synthesizer |
| Longitudinal memory | **PARTIAL** | Real durable sources/eligible assertions and bounded Therapy recall; broad understanding/patterns/coverage missing |
| Protected persistence | **PRODUCTION** | AES-GCM protected file and governed writes |
| Corrections | **PARTIAL** | Explicit whole-source revision UI works; conversational interpretation/outcome corrections do not update Therapy state |
| Privacy | **PRODUCTION**, bounded | Private retention/exclusion; review-required re-eligibility; UI/ephemeral cleanup limitations below |
| Deletion | **PRODUCTION**, bounded | Whole source history/dependent durable state; current transcript not purged |
| Backup / restore / export | **PRODUCTION**, bounded | User-picked custody; replacement restore/failure/serialization risks remain |
| Safety behavior | **PARTIAL** | Typed declaration gate; no text-grounded cross-mode safety observation |
| Deterministic rendering | **PRODUCTION** | Reference forms, validation, fallback |
| Model rendering | **ABSENT** | Unused empty platform boundary only |

Evidence: E1–E6, E10–E13; [ProductionJournalPipeline.kt](../../thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ProductionJournalPipeline.kt); [JournalCaptureEngine.kt](../../thomas/journal/src/main/kotlin/com/conundrum/thomas/v2/journal/JournalCaptureEngine.kt).

## 8. Model status

**No language model is admitted, loaded, packaged, externally selected, locally inferred, or remotely called in the current V2 app.** The module name `renderer-llama-android` does not establish an inference backend; its README explicitly states no JNI, llama.cpp, adapter, or model implementation. Source files are absent there. App does not depend on it.

| Requested model fact | Present state |
| --- | --- |
| Family/name, artifact/revision, parameters | **N/A: no model** |
| Quantization / format / weights | N/A; no GGUF, ONNX, TFLite, safetensors or other admitted weight artifact |
| Packaged vs external / local vs remote | Neither; no inference path |
| Backend / adapter | None; reserved platform module only |
| Production realizer | `DeterministicReferenceRealizer : LanguageRealizer`, version `ct-v2-13.reference-realizer.v1` |
| Model-visible current/longitudinal inputs | None, because no model exists |
| Future port-visible inputs | `RendererInput` contains governed semantic units, allowed historical support, constraints/budgets/reference forms and recent opening fingerprints; no store/tools/retrieval authority |
| Model therapeutic/policy/safety/write authority | Zero exercised paths |
| Validation | Candidate mode/act/IDs, question/sentence/character budgets, epistemic/temporal markers, source/memory attribution, forbidden phrases/manifests, fixed safety text and repetition checks |
| Fallback | At most 2 external attempts if a future external realizer is passed; reference and explicit fallback candidates also validated; returns `RENDERING_UNAVAILABLE` if none passes |
| Token/context/generation parameters | No tokenizer/context window, temperature, top-p, seed, max-generated-tokens, GPU configuration, or model streaming |
| Realizer text budgets | Journal reflection 320 chars/2 sentences/0 questions; one question 320/2/1; Therapy nominal 640/4/1; explanation 900/6/2; safety 480/3/1, constrained by upstream action |
| Renderer history | Last 6 response fingerprints, last 4 opening fingerprints; no raw persisted render-history corpus |
| TTFT / tokens per second / model memory | Not measured and not applicable |
| Response latency | Deterministic device observations only, section 19 |

**Authority answer:** generation has not regained policy authority through a model; there is no model. The important present authority weakness is **upstream state fabrication in the runtime adapter**, not model leakage.

**Fidelity limits:** the validator does not prove arbitrary natural-language equivalence. It trusts candidate-declared manifest facts in part and supplements them with finite phrase/marker checks. A malicious candidate can misdeclare facts; unlisted semantic violations are not generally decidable by this validator. Reference-only production reduces exposure but does not validate an eventual LLM. E12 and [future admission contract](../renderer/CT-V2-13-FUTURE-MODEL-ADMISSION-CONTRACT.md) explicitly preserve that empirical gap.

**Repetition/fidelity risks already visible:** CT05's same-revision guard is ineffective across freshly indexed production sends; renderer alternatives are finite and can exhaust without a useful action change. Biographer repeatedly asks open invitations. Journal runtime grounding derives surface wording primarily from assertion value/time, not the full subject/predicate/polarity; third-party attribution and negation deserve focused testing. These are code-derived risks, not newly executed failing tests or historical model failures.

## 9. Therapist capability and honest envelope

### Policy inventory versus reachable product behavior

| Capability | Ontology/policy/test status | Production and device status |
| --- | --- | --- |
| Safety routing | CT04 14-rule gate, typed unknown/tentative/refused/conflicting facts, fresh permit; 40 safety qualification tests plus provenance tests | Declaration-driven Therapy path composed; “unspecified” preemption observed. No text-grounded self-harm/emergency handling proof |
| Listening/support | CT05 expression invitation/reflection/follow-up/summary/closure acts and conversation A | **Reflection path reachable**; new-content always set, so later listening stages are not naturally observed |
| Understanding/clarification | Concern, missing piece, tentative verification, confirmation, correction withdrawal, summary/next direction; conversation B/E | Controls exist, but required understanding/gap/correction fields remain unknown/fixed. **No complete procedure** |
| Practical problem solving | Bounded PM+ definition → confirmation → influence → readiness → user options → user choice → plan → wait → outcome review → consolidate; conversation C | No observer populates those fields. **No normal UI progression to plan/outcome** |
| Close/pause/refusal | Explicit engagement rules, source-backed reluctance boundary | Production engagement always ENGAGED; typed “stop/pause” alone does not populate it |
| Anti-repetition | Same evidence revision: one justified repeat, direction substitution, stop; conversation F | Client index treated as new evidence revision; ordinary sends bypass substantive guard. Linguistic fingerprint guard remains |
| Longitudinal retrieval | CT11 eligible purpose-bound packets, CT12 route-first memory gate | Real protected-store reads; Journal explicit recall/correction/privacy/deletion tested. State does not learn a treatment strategy from memory |
| Journal/Biographer evidence | Source modes and provenance preserved | Both sources can be admitted; Journal→Therapy explicitly tested. General Biographer→Therapy behavior plausible through shared store, not equivalent to guided-biography acquisition proof |
| Corrections/contradictions | Domain relations, lifecycle invalidation, conservative structural detector; extensive tests | Explicit source revision works. Conversational correction of Thomas's current formulation does not update CT05 state |
| Uncertainty/time/attribution | Typed sources, approximate/relative time, disputed/third-party status; packet/render constraints | Supported grammar and recall bounded; broad temporal reasoning and arbitrary prose fidelity unqualified |
| Session continuity | Ephemeral action history, memory use, renderer fingerprints | In-memory only; mode/draft/transcript survive ordinary ViewModel-preserving recreation, not process death/reboot |
| Decline/inability/escalation | Typed unsupported, no action, clarification and handoff requirements | May yield silence/status; no implemented specialist procedure, resource handoff, or real clinical service |

A particularly direct code-derived reachability result follows E1/E4: for a nonblank normal “Understand” turn, concern is available but no important missing piece, tentative understanding, confirmed understanding, or unhandled correction exists. Therefore none of the six understanding action rules matches. For “Practical,” concern is treated as already bounded while all later prerequisites are unknown, so no practical action matches. This is a present implementation issue, not merely a shortage of broad test data.

### Meaningful areas not handled

No autonomous diagnosis, suicide/self-harm treatment, violence assessment, crisis response, medical/medication advice, trauma processing, abuse protocol, psychosis/mania procedure, complex substance-use treatment, specialized child/adolescent care, or individualized emergency resource selection is implemented. No standalone decision counseling, behavioral activation, CBT/DBT program, exposure, grounding/breathing treatment, motivational interviewing procedure, relationship therapy, or broad coping repertoire is admitted merely because related ontology/source records exist. PM+ choice is limited to the qualified manageable-problem procedure, which itself lacks the production state bridge.

No clinical effectiveness, safety for unrestricted personal use, accurate comprehensive life understanding, or durable evolving psychological formulation is established.

## 10. Real-world procedural foundations

This inventory uses **existing repository admissions only**. No external clinical source was added or refreshed. “GOVERNING” means encoded source derivation in current code; it does **not** mean completed clinical/rights/software-autonomy review. No framework has a repository record granting general autonomous clinical production authority.

| Source/framework, recorded version | Classification | Encoded behaviors / actual dependency |
| --- | --- | --- |
| WHO/UNICEF **Foundational Helping Skills**, `who-fhs-2025`; 2025 electronic, 2025-08-25 corrigendum | **GOVERNING**, pending production review | CT03/05 verbal listening, reflection, collaborative goals, feedback, tentative verification, user direction and stagnation direction choice. `CoreOrdinaryProvenance.fhsVerbal/fhsGoals/fhsFeedback`; active evaluator depends on bindings |
| WHO **Individual Problem Management Plus**, generic field-trial v1.1 (2018), `who-pm-plus-v1-1-2018` | **GOVERNING**, pending production review | Chapter 3 reluctance/advice boundaries; chapter 7 manageable-problem steps 1–7, options/choice/plan/outcome. `CoreOrdinaryProvenance.pm*`; active library dependency, mostly unreachable from app state |
| NICE **NG225 Self-harm**, `nice-ng225-2025` (published 2022; current recorded PDF 2025) | **SAFETY-SCOPE ONLY** | Non-predictive boundary; executable rule `ctv204-r003-self-harm-specialized-boundary` references recommendations 1.6.5–1.6.6, stopping ordinary policy on established relevance. No assessment/risk formulation or treatment |
| NIMH **ASQ toolkit/tool**, living toolkit checked 2026 and screening PDF 2025-09-29 | **INFORMATIVE / SAFETY-SCOPE ONLY**, reference only | Screening-versus-follow-up distinction; `ScreeningInstrumentReferences` only. No items, score, threshold, executable ASQ rule |
| WHO/UNICEF **EQUIP**, 2026 | **INFORMATIVE** | Competency/source/ontology context; no independent production procedure or competence certification |
| WHO **PM+ training manual**, 2025 | **INFORMATIVE** | Training/deliverer/supervision context; not a separate executable procedure |
| WHO **Psychological interventions implementation manual**, 2024 | **INFORMATIVE** | Service/implementation scope, not autonomous app treatment authority |
| WHO **Psychological self-help interventions** and **Step-by-Step material**, 2026 | **SURVEYED ONLY** for executable therapy | Catalog/ontology references; no self-help or Step-by-Step production procedure |
| WHO **mhGAP guideline**, 2015 superseded / third edition 2023; **Intervention Guide**, v1 2010 superseded / v2 2016 | **SURVEYED ONLY**, safety family deferred | Health-worker/setting/autonomy and old-procedure/new-guideline conflict unresolved; no executable mhGAP treatment |
| WHO **LIVE LIFE**, 2021 | **SURVEYED ONLY / SAFETY-SCOPE ONLY** | Public-health framework; explicitly not used as an individual gate protocol |
| NICE **NG222 Depression**, reviewed 2026-01-30 | **SURVEYED ONLY** for executable treatment | Metadata/ontology context, no depression treatment implementation |
| VA **Safety Planning** living web resource, checked 2026 | **SURVEYED ONLY / SAFETY-SCOPE ONLY** | Incomplete protocol/method rights/provider assumptions; no safety-planning implementation |
| SAMHSA **TIP 35**, 2019 (recorded page update 2026-03-10) | **SURVEYED ONLY** | No admitted motivational or substance-use procedure |
| CCI **Looking After Yourself** collection, recorded 2024 web collection | **SURVEYED ONLY** | Metadata for depression/anxiety/worry/panic/etc.; no worksheet-derived active procedure |
| NIST **AI RMF 1.0**, 2023, and **GenAI Profile AI 600-1**, 2024 | **INFORMATIVE engineering governance** | Not clinical sources and cannot authorize a therapeutic rule |
| Principal/ADR architecture authorities CT03/04/05, later admission/retrieval/render policy | **GOVERNING engineering scope** | Mode, safety permit, no-prediction guards, privacy, admission, ordering, output budgets; not clinical evidence |

Evidence: [initial source inventory](../provenance/INITIAL-SOURCE-INVENTORY.md), [safety adjudication](../safety/CT-V2-04-SOURCE-ADJUDICATION.md), [CT05 rule catalog](../policy/CT-V2-05-RULE-CATALOG.md), `CoreOrdinaryProvenance`, `SafetyScopeRuleCatalog`, and passing `CoreOrdinaryRuleSourceBindingTest` / `SafetyRuleSourceBindingTest`.

Source corpus metadata reports 19 pending clinical and 19 rights reviews, plus legal/autonomy/scope requirements; later rules retain pending review restrictions. Source identity verification and an Apache software license do not settle the cited framework permissions or clinical authority. Exact restriction tokens remain in code: `CLINICAL_REVIEW_PENDING`, `RIGHTS_REVIEW_PENDING`, `IMPLEMENTATION_SCOPE_REVIEW_PENDING`, `SOFTWARE_AUTONOMY_REVIEW_PENDING`, `PRODUCTION_AUTHORITY_NOT_GRANTED`, and PM+'s unresolved training/supervision assumption.

**Undocumented behavioral provenance:** the adapter's `BOUNDED`, `ENGAGED`, `NEW_CONTENT_AVAILABLE` and `CorrectionStatus.NONE` assertions have generated reference strings but no corresponding observed user assertions; “reported” labels do not make them evidence. Turning one scope checkbox into seven established scope/safety facts likewise lacks a documented per-fact observation proof. This is the highest-priority provenance gap. Existing source-bound rules themselves have explicit clinical or architectural references.

## 11. Longitudinal-data status

Actual durable path: **committed user source → governed admission → exact source revision → conservative perception → validated evidence bundle → governed admission/structural contradiction → protected event ledger and projection → eligible formed state / historical reader → Therapy packet/memory/render support**. Journal and Biographer share that corpus. Journal response is current-entry-only; production Biographer does not consume the resulting coverage data. Therapy captures sources but does not project them into procedural session state.

| Concern | Implemented evidence handling | Production limitation / proof boundary |
| --- | --- | --- |
| Provenance | Stable source/revision IDs, author, acquisition mode, origin, interaction/session/turn IDs | Typed user sources only; assistant output has no automatic evidence conversion |
| Self-report/inference | Separate explicit assertion, self-report, self-belief, interpretation, third-party report and hypothesis types | Conservative grammar only; unsupported input retained as source, often zero assertions |
| Third-party claims | Attribution and epistemic separation; same name does not establish identity | Cannot generally infer relationships/motives; renderer fidelity beyond fixtures needs testing |
| Uncertainty | Unknown, approximate, ambiguous, partial and unsupported outcomes preserved | No broad general-language comprehension |
| Correction/supersession | Append revision/correction relations; old evidence made non-current/review-required; dependencies affected | App revision is whole-source explicit correction; ambiguous conversational correction remains unresolved |
| Contradiction | Comparable structural claims recorded, both retained; no automatic winner | Not a general semantic contradiction detector |
| Private evidence | Source retained; ordinary derivation/retrieval excluded; current Therapy can use current private text | Private Journal suppresses optional response; “use now” UI text is too general |
| Re-eligibility | Privacy removal creates review-required state rather than silent reactivation | UI can request review but supplies no complete review/adjudication workflow |
| Deletion | Whole stable-source history erased, dependencies removed/invalidated, tombstone and content-free checkpoint | Old as-of state intentionally unavailable; external backups and current transcript not erased |
| Identity | Candidate/unknown/same/different identity structures and governed links | Production Biographer identity-resolution workflow absent; ordinary recall suppresses ambiguous labels |
| Time | Event/report/record time separate; date/instant/range/approximate/relative/ongoing/unknown types | Relative “today/yesterday” often remains a relative descriptor; do not claim calendar-normalized longitudinal reasoning |
| Source history | Revisions, lifecycle, receipts, canonical digest and replay | Full source history durable; full conversation UI/history is not |
| Higher-order patterns | Structural recurrence candidates can be formed from ≥3 independent compatible behavior sources | CT16 Pattern Engine absent; production Therapy recurrence/open-question suppliers default to empty lists |
| Hypotheses/formulation | Ontology/admission/retrieval types and synthetic tests exist | No production conversational hypothesis/formulation engine or outcome revision observer |

Temporal and provenance tests establish representation and defined grammar behavior. A corpus of arbitrary free prose survives as text; that is not proof that Thomas has structured or understood it.

### Survival claims

| Event | Automated proof | Physical-device proof | Honest present claim |
| --- | --- | --- | --- |
| Runtime close/reopen / restart | JVM tests compare revision/digest, provenance and source count | Recorded TCL sequence compares before/after root reopen | Durable corpus supported |
| Process death | Disk persistence/reopen mechanics; operational state intentionally ephemeral | Q15 labels process-death persistence qualified, but detailed sequence/test chiefly demonstrates root close/reopen and reboot | Committed corpus persistence supported; no separately evidenced mid-turn kill/retry or ViewModel recreation campaign |
| Device reboot | Not a JVM proof | Actual reboot/unlock/ADB reauthorization and matching digest recorded | Supported on recorded TCL/build with synthetic fixture |
| Application upgrade | Schema 1→2 migration/recovery JVM tests | APKs updated in place after reboot, same corpus retained | In-place replacement observed; not a general cross-version/key/schema upgrade matrix |

Java object serialization plus allowlisted deserialization sits inside versioned authenticated framing. Future class/schema changes need migration qualification; successful replacement of the same app version is not proof of arbitrary upgrades. Formed state is rebuilt from eligible corpus, so large archives incur repeated parsing/replay work rather than a proven scalable database query path.

## 12. Personal-data security boundary

| Item | Current implementation / evidence | Qualification limit |
| --- | --- | --- |
| Store technology | Pure Kotlin encrypted atomic document with event chain, checkpoint, idempotency receipts, materialized projection; schema 2, minimum schema 1 | Production is **not SQLite/Room**; SQLite/JDBC is synthetic qualification/build only |
| Encryption | `AuthenticatedProtection`: AES/GCM/NoPadding, 256-bit key, provider-generated 12-byte nonce, 128-bit tag; AAD binds purpose/alias/format; protected ciphertext limit 64,000,000 bytes | Authenticated storage tests pass; not a full independent cryptographic review |
| Key management | AndroidKeyStore alias `ct-v2-14.personal-data.primary.v1`; randomized encryption, GCM, unlocked-device required, no per-operation user authentication; no raw primary-key export | `hardwareBacked=true` is recorded OEM key observation on TCL, not attestation or universal hardware guarantee |
| Internal location | `Context.noBackupFilesDir/thomas-personal-data/store.ctpd`, canonical containment, `AtomicFile` and fsync | Expected physical credential-protected location under app sandbox; not queried now |
| Platform backup | `allowBackup=false`; cloud and device-transfer exclusion rules; merged debug/release manifests verified | Recorded TCL flag/no-backup placement pass; no complete OEM transfer matrix |
| Export | Version 1 user-owned JSON and Markdown; exact sources separated from derived state/lineage, delivered by SAF | Intentionally plaintext at chosen destination; private retained sources can be exported by the user; no external erasure claim |
| Backup | Version 1 custody format, independently encrypted under generated 32-byte recovery key; separate key file | Security depends on custody of both files; picker can expose user-selected external providers |
| Restore | Validate framing/checksum/authentication/schema/ledger/projection/revision before empty-target restore | Android replacement resets live corpus after validation; a later write failure can leave old data gone. Not atomic rollback of existing corpus |
| Reset | Store deletion → key destruction → in-memory state cleared; returns three success flags | ViewModel/root do not fully honor partial-failure flags; see debt I5 |
| Selective deletion | Entire source history/dependent influence erased from current protected document; stable-ID tombstones | No secure overwrite/flash-forensic erasure claim; external artifacts and in-memory transcript remain |
| Logs/crash diagnostics | No production narrative logging/analytics/crash SDK path found in app/runtime/platform scans | OS/debugger/keyboard/screenshots and future diagnostics outside proven boundary |
| Temporary artifacts | Internal atomic artifacts contain ciphertext; serialization occurs in memory | Memory copies/Java strings are not universally zeroized; backup temporary plaintext array is not explicitly wiped |
| Model prompts/outputs | No model; renderer candidates/context/fingerprints operational only | User-visible Thomas transcript is process-local, not durable user evidence |
| Audio | No capture, retention, microphone permission, recognizer, or TTS | No claim about future speech providers |
| Transcript | Committed typed text durable as source; UI transcript/drafts in memory | Delete/revise does not remove/update the already displayed transcript |
| Screen protection | No `FLAG_SECURE` or separate application unlock surface found | At-rest encryption is not protection from screenshots, keyboard/provider access, or an unlocked/debuggable process |

Evidence: E6/E13; [AuthenticatedProtection.kt](../../thomas/personal-data-persistence/src/main/kotlin/com/conundrum/thomas/v2/personaldata/AuthenticatedProtection.kt); [Android key provider](../../platform/persistence-android/src/main/kotlin/com/conundrum/thomas/v2/platform/persistence/AndroidKeystorePersonalDataKeyProvider.kt); [atomic storage](../../platform/persistence-android/src/main/kotlin/com/conundrum/thomas/v2/platform/persistence/AndroidAtomicProtectedArtifactStorage.kt); [threat model](../security/CT-V2-14-PERSONAL-DATA-THREAT-MODEL.md); [backup/export/restore](../persistence/CT-V2-14-BACKUP-EXPORT-AND-RESTORE.md).

The threat model's claims against ordinary at-rest disclosure are supported by implementation and selected tests. It explicitly excludes compromised OS/root/debugger/arbitrary in-process code and unlocked-session control. Current build is a debug research APK or unsigned release APK; neither is a reviewed release security posture. No remote inference or automatic cloud psychological store exists. SAF is an explicit **external custody channel**, potentially backed by a user-selected cloud document provider; “local-first” must not be misread as “export can never leave the device.”

## 13. Journal status

Journal is the strongest currently testable acquisition surface:

- Commit preserves exact source independently of whether parsing succeeds.
- `NO_RESPONSE` is default, with **zero planner/renderer/assistant artifact** on its intended path. Silence does not mean no stored memory.
- `REFLECT` is current-entry-only, zero questions; `ASK_ONE_QUESTION` permits at most one grounded optional question. No separate acknowledge level or autonomous therapist handoff.
- Unsupported language can be stored without a response even when a response is requested; first-proposal grounding is not whole-entry summarization.
- Revision appends a source revision and invalidates older current meaning; current UI revision editor starts empty and does not show original source text.
- Private entries are retained but skip ordinary derivation **and optional response**. The general UI phrase “Private: use now” does not accurately describe private Journal response behavior.
- Protected restart/reboot support and explicit Journal→Therapy recall/correction/private exclusion have real qualification evidence.
- No model rendering, STT, TTS, historical Journal question engine, or general life-event extraction.

**Readiness:** yes for a clearly scoped typed Journal/storage/response campaign now, including unsupported language, negation/attribution, long input, retry, privacy, deletion, and rendering silence. It is not evidence that full Therapist/Biographer testing is ready. Evidence: E8's silence/posture/correction tests; 12 Journal unit tests and 64 Journal qualification tests; `JournalCaptureEngine`, `DeterministicJournalResponseIntentPlanner`, E2's Journal grounding.

## 14. Biographer status

| Requested behavior | Present implementation |
| --- | --- |
| Open narrative | Production open historical invitation and answer capture |
| Targeted historical-gap mode | Engine/contracts and qualification composition exist; **not reachable from Android runtime/UI** |
| Target selection | Deterministic engine supports 13 kinds with stable priority; production always open-story and no candidates |
| One-question constraint | Typed maximum one and renderer budget; real open prompt path validated |
| Coverage model | Unknown/sparse/covered/unresolved/private/declined plus eligibility states; represented periods/roles/places/relations/gaps |
| Unanswered target | Qualified operational offered/skip/defer/decline/private/stop/material-change handling |
| Production coverage/outcome consumption | **Absent:** default-empty evidence; no `recordOutcome` loop; no targeted controls |
| Evidence admission | Exact user answer, acquisition provenance, optional bounded extraction; prompts never become user evidence |
| Persistence | Real protected shared source store; target/session investigation state is ephemeral |
| Timeline reconstruction | Typed domain and qualification coverage construction; no production timeline/coverage view or automatic guided reconstruction |
| Later Therapy influence | Eligible open-narrative sources enter common corpus; bounded recall possible. Guided acquisition benefits not realized |

The qualification pipeline E9 constructs coverage from actual store/formed state and applies answer outcomes and governed coverage/identity/correction operations. Production E3 omits those behaviors. E8's test named “biographer target is selected upstream…” only asserts a non-null prompt/render result and an admitted source in either acquisition mode; it never requires a non-null target ID, a changing gap, or consumption of stored coverage. **That test passes even when every prompt is an untargeted open invitation.**

**Readiness:** open capture and one-question rendering can be tested now. Biographer cannot yet be rigorously evaluated as the requested **longitudinal acquisition system that chooses and fills historical gaps**. The minimum precursor must integrate qualified coverage formation and operational outcomes, not invent a new biography policy.

## 15. Speech status

### STT

**No implementation/provider.** No recognizer, microphone permission, partial/final callback, audio buffer, finish-speaking control, cancel/discard control, speech interruption logic, or offline/online speech behavior is present. `ProductionInputOrigin.SPEECH_TRANSCRIPT` and mode capture-origin equivalents are typed contracts exercised with synthetic text; they do not constitute speech recognition.

Text input is editable and only explicit commit creates evidence. The reserved adapter README requires future finish to retain a reviewable transcript and cancel to discard it, but those requirements are not implemented or device-qualified.

**Historical Cancel defect:** neither fixed nor reproduced in an implemented V2 speech path. It is **superseded by disabled/unimplemented speech**, with the desired distinction preserved as a future contract. Do not report it “resolved on device.”

### TTS

**No implementation/provider, engine, voice selection, language configuration, or speaking state.** Disabled by absence. There is no actual final-artifact TTS consumer to qualify; no candidates, silence, or hidden memory are spoken because nothing is spoken. Offline availability and interruption/finality behavior are N/A until implementation. No V1 speech code crossed the migration gate.

Evidence: [speech adapter README](../../platform/speech-android/README.md), [speech boundaries](../architecture/CT-V2-15-SPEECH-BOUNDARIES.md), E5, source/merged manifests, and E7's explicit non-admission.

## 16. UI / UX status

The real Compose surface has top-level Journal/Biographer/Therapy chips; a per-mode filtered transcript with “You” and “Thomas” bubbles; editable multiline draft; private checkbox; status; disabled “Speech unavailable”; and Commit/Working controls. Journal offers Silence/Reflect/One question. Therapy offers Listen/Understand/Practical, explicit recall, and ordinary non-emergency adult scope confirmation. Biographer has no posture or gap-selection control.

“Your data” shows source identity/mode/report time/revision/lifecycle metadata and privacy/review, revise, and delete actions. It exposes JSON/Markdown export, encrypted backup and separate recovery-key file, replacement restore, and reset. Deletion/revision/reset dialogs exist. No source-text reader, rich timeline, identity adjudication, review queue completion, model settings, audio controls, or durable conversation browser exists.

Loading/errors are mostly status strings and disabled Commit; unavailable opening is visible. There is no general typed error-detail/retry workflow. The runtime catches broad `RuntimeException` as `PERSISTENCE_UNAVAILABLE` even if the cause was elsewhere. A failed submit can still show “Not saved” user text and clear the draft. Some source-lifecycle coroutine failures have no local exception-to-state handling.

### UI issues that confound testing

- Understand/Practical controls lead to unpopulated policy states; safety checkbox does not observe subsequent text.
- Draft switching is **preserving**, not clearing: E5 saves `drafts[current.mode]` and restores `drafts[mode]`. Q15 and lifecycle prose claim mode-switch-cleared drafts. **Code wins.** No admission occurs just by switching.
- Mode/draft/transcript survive ViewModel-preserving configuration recreation only by architecture; no broad recreation test was found. Process death loses them while sources survive.
- Deleting or correcting a stored source leaves earlier transcript text visible. Restore refreshes source summaries/status but does not clear old transcript/drafts or rebase `nextTurnIndex` to the restored corpus. Subsequent ID collisions after restoring another/newer corpus are a code-derived risk.
- Runtime's busy guard covers submit/revision/privacy/deletion; export, backup, reset, next prompt and root replacement do not all participate. Custody buttons/callbacks do not consistently honor `processing`. “All operations serialized” is too strong.
- Custody reads/writes and some initial store/coverage work run synchronously on UI paths. Unbounded `InputStream.readBytes` precedes backup format validation; a large selected file can cause memory/UI problems.
- Reset success is inferred from absence of an exception, despite a result carrying false deletion/key flags. Root `resetAndReopen` reopens regardless. This can report “All local Thomas personal data was reset” when the store returned a partial failure.
- If opening fails, root.runtime is null; current reset/restore UI paths require an active runtime, leaving no complete in-app recovery flow for key loss/corrupt store.
- Backup file and recovery key are two picker operations. Cancellation, interruption and failed writes can leave an unusable external backup; no transactional two-file custody guarantee exists.

These are findings/risks to preserve for a subsequent order. Nothing was repaired here.


## 17. Automated-test inventory

### Fresh baseline

**946 executed JVM tests, 59 XML suites, 0 failures, 0 errors, 0 skips.** This exactly matches Q15's most recent recorded JVM full-suite count. Aggregate XML test time **44.694 seconds**; Gradle wall time **1m47s**. These are executions including parameterized cases, not 946 independent product scenarios.

| Module | XML suites | Executed tests | Principal proof |
| --- | ---: | ---: | --- |
| app | 1 | 1 | Foundation unit assertion |
| thomas:biographer | 2 | 16 | Coverage selection and answer-capture contracts |
| thomas:journal | 1 | 12 | Source-first capture/postures/privacy/failures |
| thomas:language-evidence | 4 | 41 | Bounded grammar, adversarial inputs, spans, temporal language |
| thomas:longitudinal | 3 | 23 | Domain validation, life structures and temporal representation |
| thomas:ontology | 1 | 10 | Typed vocabulary/epistemic contracts |
| thomas:personal-data-persistence | 5 | 46 | Encryption, lifecycle, backup/export, migration/recovery, lineage |
| qualification | 37 | 776 | Cross-module policy/evidence/store/retrieval/renderer/runtime and static authority tests |
| tools:provenance | 5 | 21 | Generated source database and exact ontology/rule source bindings |
| **Total** | **59** | **946** | **All pass in current execution** |

Modules without local tests have `NO-SOURCE` test tasks; their proof often resides in `qualification`. Engine/safety/runtime/renderer/retrieval are not independently untested merely because their own `src/test` is empty. Conversely, their shared suite does not prove full app reachability.

### Test types and limits

- **Unit/domain:** ontology, longitudinal time/life validation, perception, Journal/Biographer, protected-store operations.
- **Integration:** synthetic SQLite admission/replay and protected-store pipelines; current runtime has only **17** dedicated CT15 JVM tests across two suites.
- **Corpus/adversarial:** 96 CT11 retrieval cases, 129 CT12 integration cases, 131 CT13 renderer cases; parameterized/named fixed scenarios and negative constraints.
- **Security:** authentication/wrong/missing keys, tamper/truncation, source deletion and derivation consequences, backup/export, static no-network/log/artifact/bypass assertions.
- **Property/invariant:** deterministic repeated state, decision matrices, loops and negative constraints. No dedicated randomized fuzz/property engine, seed-driven fuzz campaign, or model-backed adversarial run found. Ordinary `SecureRandom` in cryptography is not test-corpus fuzzing.
- **Android instrumentation in source:** **7 test methods**: 5 `CTV215DevicePersistenceInstrumentedTest`, 1 `CTV215ProductionUiInstrumentedTest`, 1 `FoundationInstrumentedTest`. Q15 reports **6 CT15 physical executions** (five persistence/runtime plus one UI), combined 952 with JVM. It does not establish that the separate foundation instrumentation method was part of those six.
- **No instrumentation executed now.** The connected target is unauthorized; the principal device/UI suites are stateful and destructive to the app corpus.
- **Skipped/disabled:** zero skips in current XML; no `@Ignore`/`@Disabled`/Assume-based test exclusions found in searched test sources. Gradle `SKIPPED` plugin-check/vital-lint tasks are task conditions, not skipped JUnit cases.
- **Flakiness:** no documented statistical flaky-test inventory. Device gate methods a/b/c depend on a manually sequenced synthetic corpus/preferences and intervening reboot; they are not safe independent random-order tests.
- **Coverage:** no JaCoCo/Kover/line/branch percentage or requirements-to-production-reachability metric found. No CI workflow exists under `.github/workflows`. Static regex scans establish selected source patterns, not all possible authority paths.
- **Performance:** no sustained real-device load/battery/thermal/low-memory campaign; recorded three-sample micro-observations only.

**Longest fresh JVM suites:** `CTV215LifecycleAndAuthorityQualificationTest` 24.975s; `LanguageEvidenceBoundaryQualificationTest` 4.643s; `CTV211AcceptanceTest` 4.452s; `JournalAuthorityQualificationTest` 2.403s; `LongitudinalStoreBoundaryQualificationTest` 2.332s. The production archive fixture appends 120 unrelated sources; it is not a months-long latency qualification.

### Entire fresh suite ledger

Counts and seconds below come from `<module>/build/test-results/<test-task>/TEST-<fully-qualified-class>.xml` generated by the current canonical execution. Source suites are under each module's `src/test`. All rows passed with zero failures/errors/skips.

| Module | Suite | Tests | Seconds |
| --- | --- | ---: | ---: |
| app | `FoundationUnitTest` | 1 | 0.006 |
| thomas:biographer | `BiographerAnswerCaptureEngineTest` | 3 | 0.041 |
| thomas:biographer | `DeterministicBiographerCoverageEngineTest` | 13 | 0.020 |
| thomas:journal | `JournalCaptureEngineTest` | 12 | 0.052 |
| thomas:language-evidence | `AdversarialLanguageCorpusTest` | 2 | 0.091 |
| thomas:language-evidence | `ConservativeLanguagePerceptionTest` | 26 | 0.021 |
| thomas:language-evidence | `SourceSpanGroundingTest` | 6 | 0.009 |
| thomas:language-evidence | `TemporalLanguagePerceptionTest` | 7 | 0.005 |
| thomas:longitudinal | `LifeStructureTest` | 2 | 0.063 |
| thomas:longitudinal | `LongitudinalValidationTest` | 13 | 0.014 |
| thomas:longitudinal | `TemporalModelTest` | 8 | 0.008 |
| thomas:ontology | `TherapeuticOntologyTest` | 10 | 0.056 |
| thomas:personal-data-persistence | `PersonalDataBackupExportTest` | 12 | 0.294 |
| thomas:personal-data-persistence | `PersonalDataLifecycleTest` | 10 | 0.113 |
| thomas:personal-data-persistence | `PersonalDataLineageIntegrationTest` | 5 | 0.093 |
| thomas:personal-data-persistence | `PersonalDataMigrationRecoveryTest` | 8 | 0.088 |
| thomas:personal-data-persistence | `ProtectedPersonalDataStoreCoreTest` | 11 | 0.072 |
| qualification | `BiographerAuthorityQualificationTest` | 26 | 0.961 |
| qualification | `BiographerCoverageQualificationTest` | 25 | 0.454 |
| qualification | `BiographerSelectionQualificationTest` | 25 | 0.151 |
| qualification | `CoreOrdinaryConversationTest` | 7 | 0.007 |
| qualification | `CoreOrdinaryDecisionMatrixTest` | 22 | 0.003 |
| qualification | `CoreOrdinaryInvariantTest` | 11 | 0.022 |
| qualification | `CoreOrdinaryProgressionTest` | 9 | 0.002 |
| qualification | `CoreOrdinaryRendererTest` | 7 | 0.005 |
| qualification | `CTV211AcceptanceTest` | 96 | 4.452 |
| qualification | `CTV212AcceptanceTest` | 129 | 0.621 |
| qualification | `CTV213AcceptanceTest` | 131 | 0.150 |
| qualification | `CTV214PersonalDataGovernanceQualificationTest` | 11 | 0.106 |
| qualification | `CTV215LifecycleAndAuthorityQualificationTest` | 9 | 24.975 |
| qualification | `CTV215ProductionRuntimeQualificationTest` | 8 | 0.047 |
| qualification | `FoundationArchitectureTest` | 9 | 0.094 |
| qualification | `GovernedLongitudinalStoreCoreTest` | 16 | 0.138 |
| qualification | `JournalAuthorityQualificationTest` | 15 | 2.403 |
| qualification | `JournalCaptureQualificationTest` | 30 | 0.427 |
| qualification | `JournalModeAndLanguageQualificationTest` | 19 | 0.329 |
| qualification | `LanguageEvidenceBoundaryQualificationTest` | 7 | 4.643 |
| qualification | `LanguageEvidencePipelineTest` | 11 | 0.154 |
| qualification | `LanguageStateFormationTest` | 7 | 0.123 |
| qualification | `LongitudinalAdmissionRevisionTest` | 18 | 0.185 |
| qualification | `LongitudinalBoundaryQualificationTest` | 9 | 0.014 |
| qualification | `LongitudinalDurableFixtureTest` | 9 | 0.099 |
| qualification | `LongitudinalFixtureTest` | 9 | 0.001 |
| qualification | `LongitudinalStoreBoundaryQualificationTest` | 13 | 2.332 |
| qualification | `LongitudinalStoreInvariantTest` | 7 | 0.045 |
| qualification | `LongitudinalTemporalPersistenceTest` | 4 | 0.040 |
| qualification | `OntologyAuthorityQualificationTest` | 5 | 0.002 |
| qualification | `ProceduralDecisionTableTest` | 18 | 0.018 |
| qualification | `ProceduralPolicyInvariantTest` | 9 | 0.014 |
| qualification | `RendererBoundaryQualificationTest` | 5 | 0.003 |
| qualification | `SafetyGateCapabilityTest` | 4 | 0.001 |
| qualification | `SafetyRendererBoundaryTest` | 5 | 0.001 |
| qualification | `SafetyScopeDecisionTableTest` | 21 | 0.001 |
| qualification | `SafetyScopeInvariantTest` | 10 | 0.010 |
| tools:provenance | `CoreOrdinaryRuleSourceBindingTest` | 2 | 0.178 |
| tools:provenance | `OntologySourceBindingTest` | 4 | 0.086 |
| tools:provenance | `ProceduralRuleSourceBindingTest` | 2 | 0.056 |
| tools:provenance | `ProvenanceDatabaseTest` | 11 | 0.251 |
| tools:provenance | `SafetyRuleSourceBindingTest` | 2 | 0.044 |

Recorded growth, distinguishing historical claims from fresh execution: 00: 9; 01: 21; 02: 40; 03: 74 (clean run 73 plus final isolation test); 04: 116; 05: 174; 06: 215; 07: 282; 08: 348; 09: 424; 10: 516; 11: 612; 12: 741; 13: 872; 14: 929; 15/current: 946 JVM. Historical counts are from the respective Q records; they do not imply every intermediate phase was rerun separately now.

### What the passing estate does not establish

It does not establish production natural-language-to-Therapy progression, target-driven Biographer operation, semantic generality of parser/renderer, safe unrestricted real-user behavior, clinical benefit, model admission, real speech, production signing, full upgrade/device matrix, or absence of all privacy races. Q15's Biographer test permits a null target; its Therapy path principally tests LISTEN. Old multi-turn Therapy tests supply already-structured progression states. A fully green suite is consistent with the current production disconnects.

## 18. Current baseline execution

Inspection, including current Git/lineage, commands/build files, architecture, tests and their data effects, preceded building. No new test harness or test was added.

| Command / action | Outcome |
| --- | --- |
| `git status --porcelain=v2 --branch`, cached/uncached diff stats, untracked inventory | Clean entry; main +14/-0 |
| `git rev-parse --show-toplevel --absolute-git-dir HEAD 'HEAD^{tree}'` | Canonical root/.git and hashes in section 2 |
| `git remote -v`; `git rev-list --left-right --count 'HEAD...@{upstream}'` | One origin; 14/0 using local tracking state |
| `git log --all`; `git for-each-ref ... refs/tags`; `git tag --merged HEAD` / `--no-merged HEAD` | 79 visible commits, 17 annotated reachable tags, 0 unreachable |
| `.\tools\verify-canonical-git-root.ps1` | Pass: canonical root, .git, optional .idea mapping, no stale temporary metadata |
| `git diff --check` | Exit 0 |
| `git fsck --full --strict` | Exit 0, no findings |
| Initial `.\gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain` | Failed before Gradle: JAVA_HOME unset and java absent from shell PATH |
| Same canonical command with process-local JAVA_HOME set to installed Studio JDK | **BUILD SUCCESSFUL in 1m 47s**, 393 actionable tasks: 389 executed, 4 up-to-date; configuration cache reused |
| XML aggregation after build | 59 suites / 946 tests / 0 failures / 0 errors / 0 skips |
| Four debug lint XML reports | 0 errors/fatals; 17 warnings: app 16, renderer scaffold 1, other platform modules 0 |
| Provenance generation | `provenance/generated/thomas-provenance.sqlite` regenerated and confirmed ignored |
| Instrumentation | **Not run**: unauthorized ADB plus destructive fixture effects; no install/reset authorized by reconnaissance |
| APK archive-name and merged-manifest inspection | No model/db/backup/key/PDF/corpus entry matches in inspected archives; allowBackup=false, no INTERNET or RECORD_AUDIO permission, debug debuggable=true |
| Final verification | Section 26 records report-only worktree policy; no source/config/test changes |

Exact successful invocation:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
```

JDK observed: JetBrains OpenJDK **25.0.2+-15348964-b329.117**, Windows x86_64. Wrapper 9.5.0, AGP 9.3.2, Kotlin 2.2.10, Compose BOM 2026.02.01, compile/target SDK 37, minSdk 31, bytecode target 11 are repository declarations. No dependency upgrades or persistent environment edits were made. These versions are inventoried, not externally freshness-validated.

Lint warning IDs/counts: `RedundantLabel` 1; `AndroidGradlePluginVersion` 2; `GradleDependency` 1; `NewerVersionAvailable` 3; `ObsoleteSdkInt` 2; `UnusedResources` 7; `ChromeOsAbiSupport` 1.

### Produced APK identities

| Artifact | Bytes | Fresh SHA-256 |
| --- | ---: | --- |
| Debug | 31,466,324 | `3fa1f8590a16b8eadc85b0e007d7b7e6b67f425aff9f328516b73b5b2c5d2ae4` |
| Unsigned release | 24,246,543 | `ef0688bbbd2c3aab71ef8e9e525cd0269ad2aad61f91e81b162b4887aadedea8` |

Debug hash matches Q15 exactly. Q15's recorded release hash is `dd5cb034812eef5e5c125923d9aa19df3a0a7633ccc73ad23fef0d1790b6d5d4`, which **does not match** this run. Fresh release contains `META-INF/version-control-info.textproto` with revision `c987fbf3e3742124408a8e6f855d82d19e82f0d1`. HEAD-sensitive release metadata is a plausible explanation because sealing documentation followed implementation; the earlier APK bytes were not available for entry-by-entry comparison. **Do not claim the previous unsigned release was reproduced.** Q15's two-run reproducibility claim remains historical evidence, not proof of identity with the final sealed HEAD build.

A canonical JVM/build/lint/provenance command exists and works with the installed JDK. The readiness defect is not a missing build harness. Device execution is more fragmented: manual a/b/reboot/c fixture sequencing and safe test-data isolation need an explicit future protocol.

## 19. Physical-device status

Read-only commands:

```powershell
& 'C:/Users/Jared/AppData/Local/Android/Sdk/platform-tools/adb.exe' devices -l
& 'C:/Users/Jared/AppData/Local/Android/Sdk/platform-tools/adb.exe' -s BC9424B4E3C5002 shell getprop ro.product.model
```

Observed one target, `BC9424B4E3C5002`, **unauthorized**, transport_id 1. Property query returned “device unauthorized.” No emulator was listed. Current model, Android/API/ABI and installed Thomas package/version are therefore **UNKNOWN**, not silently copied from the prior device report. The target serial is an equipment identifier, not qualification user data. No ADB server restart, authorization change, launch, install, uninstall, wipe, reboot, key operation, or corpus access was performed.

### Most recent preserved actual-device evidence

[CT-V2-15 target-device evidence](../qualification/CT-V2-15-TARGET-DEVICE-EVIDENCE.md), recorded **2026-09-04 America/Los_Angeles**, describes **TCL 9491G, Android 15/API 35, arm64-v8a**, package **com.conundrum.thomas.v2**, **version 1.0 (1)**. The paired Q15 report records debug build hash matching today's debug APK and **6 CT15 instrumented tests passed, 0 failed/skipped**.

Recorded sequence: create/reset synthetic corpus; silent Journal source; no plaintext fixture in protected file; root close/reopen and matching revision/digest; physical reboot/unlock/ADB reauthorization; APK update in place; same corpus/digest; protected backup/validation/reset/restore; destroy primary key and observe fail-closed state; final reset. Platform backup flag absent, no-backup path and provider AES/AndroidKeyStore with hardwareBacked=true observed. Additional scenarios cover mode controls, silent Journal UI, open Biographer answer, listening/recall, explicit unspecified-scope preemption, source correction/privacy/deletion.

**Important proof limits:** one UI test checks controls and silent Journal commit, not every custody picker workflow or every Therapy route. The device runtime “golden paths” exercise API composition. There is no separately recorded broad user-operated product evaluation, mid-turn process kill matrix, guided target test, speech/model behavior, real clinical data, or population/device generalization.

### Recorded deterministic performance (three samples; not remeasured now)

| Operation | Median ms | Worst ms |
| --- | ---: | ---: |
| Cold start | 1,480 | 1,515 |
| Store open/recovery | 16.787 | 17.103 |
| Journal silent commit | 127.513 | 210.983 |
| Journal deterministic rendering | 476.071 | 602.100 |
| Biographer turn | 62.498 | 63.373 |
| Therapy without memory | 1,291.229 | 1,520.619 |
| Therapy explicit memory | 2,195.030 | 2,325.097 |
| Export | 30.397 | 32.516 |
| Protected backup | 4.923 | 6.168 |
| Validated reset/restore | 138.184 | 148.177 |

Model TTFT, model throughput, model resident memory, sustained PSS/RSS, battery and thermal measurements are absent. Device performance is already nontrivial without a model; archive growth deserves attention.

**Do we possess an exercised end-to-end build for the intended device?** **Yes, within the recorded CT15 synthetic typed-path scope:** prior TCL evidence exists and today's debug APK hash matches its report. **No current installed-state verification**, and no proof of the broader complete Therapist/guided-Biographer envelope.

## 20. Authority audit

Counts below distinguish actual call paths from transitive compile visibility. They are code-inspected and supported by static qualification, not formal whole-program proofs. “0” means no exercised implementation path found; it is not a claim that arbitrary malicious future code could never construct one.

| Path requested | Best-supported count / interpretation | Evidence and exception |
| --- | --- | --- |
| Production app composition roots | **1** canonical root class | E6; E8 static test |
| Production protected-store roots | **1** app open call site | E6 → Android factory |
| Production Therapy routes | **1** evaluator path; **5** declared ordinary route kinds; **3** user-selectable support routes, only LISTEN currently selects a normal action | E1/E2/E4; other routes remain library capability |
| Production safety routes | **1** policy family, **14** rules; **2** gate evaluations per Therapy turn; **2** UI declaration states | E1/E2/E11; CURRENT_EMERGENCY is runtime-contract only, not UI control |
| Model-backed realizers | **0** | No implementation |
| Model → Therapy policy | **0** | No model |
| Model → safety | **0** | No model |
| Model → longitudinal write | **0** | No model |
| Renderer → policy evaluation/mutation | **0 observed calls** | Renderer consumes governed commands, but policy classes transitively compile-visible through api dependencies |
| Renderer → persistence read/write | **0 observed calls/ports** | E12; no concrete store reference; public admission types transitively visible |
| UI → Therapy technique selection | **0 direct technique/evaluator calls** | User preference flows to evaluator; E1 nonetheless fabricates prerequisite state |
| UI → direct longitudinal mutation bypass | **0 direct SQL/DAO/admission calls** | Explicit UI lifecycle commands route through runtime/admission; not zero user-authorized lifecycle operations |
| Response → automatic user evidence | **0** | Source capture accepts committed user request, not assistant artifacts; explicit user copy/paste is a different action |
| Private unsurfaced memory → renderer | **0 observed historical paths** | CT11 eligibility + CT12 gate; current private Therapy text intentionally distinct |
| V1 therapeutic production paths | **0** | 24 denied migration records, no active V1 dependency |
| Remote inference paths | **0** | No client/model/backend; no app INTERNET permission |
| Cloud psychological-storage paths | **0 automatic app service/sync paths** | **1 explicit custody mechanism (SAF)** may deliver user-selected exports/backups to external/cloud providers; not automatic cloud storage |
| Unexpected UI declaration → established scope/safety facts | **1 adapter family populating 7 fields** | E1 ordinary checkbox expansion; insufficient evidence provenance |
| Unexpected current text → assumed procedural facts | **1 adapter**, fixed engaged/bounded/new-content/no-correction/summary=false values | E1; bypasses evidence-to-procedure formation |
| Qualified Biographer coverage → production candidate supply | **0** | E3 default-empty CoverageEvidence; E9 richer path is qualification-only |

The critical unexpected nonzero paths are upstream **manufactured facts**, not model or renderer authority. The strongest “compile-enforced” claims also require qualification: `runtime api(engine/safety/...)` and `renderer api(therapy-longitudinal)` expose more types than a literal isolation reading suggests.

Technical Android composition is authorized by WO15, while engine/safety rules still explicitly carry `NOT_GRANTED`/pending clinical production authority. This is not evidence that clinical approval was granted by Android wiring. **Production-shaped execution and approved therapeutic deployment are separate states.**

## 21. Consolidated debt register

Severity is relative to the requested testing/release decision. “Blocks testing” means the full proposed product campaign; individual components can often still be tested. Code-derived risks have not been newly fault-injected in this read-only order.

### Implementation debt

| ID / debt | Severity | Category | Blocks testing? | Blocks release? | Evidence |
| --- | --- | --- | --- | --- | --- |
| I1 Conversation-to-CT05 observation/progression absent; Understand/Practical lack actions | Critical | Functional | **Yes** for full Therapist | Yes | E1/E4; unknown prerequisites and fixed state |
| I2 Checkbox expands into established safety/scope facts; current text cannot update/contradict; request default ordinary | Critical | Safety | **Yes** for unconstrained/cross-mode safety | Yes | E1; ProductionRuntimeContracts default; E5 |
| I3 Biographer stored coverage/targeted mode/outcomes disconnected | High | Functional | **Yes** for guided longitudinal acquisition | Yes for claimed feature | E3 versus E9 |
| I4 Client turn index substitutes for material evidence revision; active route/pending state never carried | High | Functional/UX | Yes for progression/repetition tracks | Yes | E1/E2/E4 same-revision guard |
| I5 Reset UI treats returned partial-failure result as success | High | Privacy/Functional | No, suitable bounded fault target | Yes | E13 reset flags; E6 resetAndReopen; E5 runCatching.isSuccess |
| I6 Replacement restore validates first but resets before final write; no old-corpus rollback | High | Privacy/Functional | Requires disposable corpus | Yes pending explicit failure contract/proof | E6 replaceFromProtectedBackup |
| I7 Restore keeps old transcript/drafts/client index; deletion/correction leaves transcript copies | High | Privacy/UX | Confounds lifecycle/privacy results unless accounted for | Yes | E5 notifyCustodyStatus versus reset handling |
| I8 Turn busy guard excludes some custody/prompt/reset paths; UI custody callbacks synchronous/not consistently disabled | High | Functional/Privacy/Performance | Confounds concurrency tests; otherwise testable | Yes | E2 public methods, E5/E6 custody buttons |
| I9 Failed-open runtime lacks complete reset/restore recovery UI | High | Functional/UX | No, failure scenario interpretable | Yes | E6 requires non-null runtime; E5 early returns |
| I10 Journal rendered value/time may omit subject/predicate/polarity; finite validator/manifest trust | High | Functional/Safety | No, focused semantic test target | Yes pending fidelity qualification | E2 journalSurfaceMeaning; JournalRenderCommandAdapter; E12 |
| I11 Two-file backup cancellation/write failure; unbounded SAF reads and incomplete temporary-key cleanup paths | High | Privacy/Performance | Requires isolated custody files | Yes pending bounded handling/proof | AndroidDataCustodyController |
| I12 API re-exports weaken claimed compile isolation | Medium | Safety/Architecture | No; audit this explicitly | Yes for strong isolation claim | build.gradle.kts api graph, section 6 |
| I13 Production recurrence/open-question providers empty; no interpretation/outcome observer | Medium | Functional | Yes if these are included as current product claims | Yes for claimed features | ProductionTherapyPipeline defaults; E1/E11 |
| I14 Broad runtime exception flattened to persistence unavailable; some UI lifecycle coroutines lack error recovery | Medium | Functional/UX | Complicates failure attribution | Yes | E2 submit catch; E5 revise/lifecycle functions |

### Qualification debt

| ID / debt | Severity | Category | Blocks testing? | Blocks release? | Evidence |
| --- | --- | --- | --- | --- | --- |
| Q1 Green fixtures do not prove production reachability; coverage/no-target tests too permissive | High | Functional/Safety | Requires precursor acceptance proof | Yes | E8 versus E1/E3; 17 CT15 JVM tests |
| Q2 Device suites reset/delete the installed corpus and require manual sequence/reboot | High | Privacy/Testing | **Yes for nondestructive full-device execution here** | Yes | Instrumented methods a/c/d/e; UI test commits source |
| Q3 Current device unauthorized; installed state unknown | Medium | Device | Blocks present physical rerun only | Yes before device release decision | Current adb output |
| Q4 Broad process-kill/in-flight commit/restore/rotation/OEM lock-state/upgrade matrix absent | High | Functional/Privacy | No after isolated target | Yes | E7 limited recorded sequence |
| Q5 Parser and renderer empirical semantic generality unproven; no real model corpus | High | Safety/Quality | No for deterministic tests; model tracks deferred | Yes for broader language claims | E10/E12/Q13 |
| Q6 Security controls partly static; no independent penetration/forensic/custody provider audit | High | Privacy | No | Yes | Threat model and limited TCL gates |
| Q7 No coverage metrics, CI workflow, randomized fuzz campaign, broad production requirement coverage map | Medium | Testing | No; existing canonical command is usable | Yes for repeatable release process | Build/test inventory |
| Q8 Release hash differs from Q15; exact sealed release reproducibility not established | Medium | Release/Provenance | No | Yes for artifact seal | Section 18 |
| Q9 Physical evidence cannot be generalized to all documented capability tags | High | Safety/Functional | No; narrow claims first | Yes | Six CT15 scenarios versus broad terminal tokens |

### Product debt

| ID / debt | Severity | Category | Blocks testing? | Blocks release? | Evidence |
| --- | --- | --- | --- | --- | --- |
| P1 No speech input/output | Medium | UX | No for typed product; speech tests N/A | Depends on release promise | Empty speech adapter |
| P2 No model/natural language realization admission | Medium | UX/Research | No for deterministic product | Depends on product language promise | Empty model adapter; reference forms |
| P3 Transcript/mode/drafts/session progress lost on process death; no history browser | Medium | UX | No once persistence versus session distinction explicit | Yes for continuity claim | E5/E2 ephemeral state |
| P4 No guided Biographer controls, source text review, review completion, identity/correction workflow | High | UX/Functional | Guided acquisition blocked by I3 | Yes for those surfaces | E5/E9 |
| P5 Private “use now” wording conflicts with private Journal silence; not all response levels | Medium | UX | No | Yes for clear privacy/posture promise | JournalCaptureEngine / InputPanel |
| P6 Accessibility, large text, keyboard, screen sizing, long transcripts, cancellations unqualified | Medium | UX | No | Yes | One narrow UI instrumentation test |

### Research debt

| ID / debt | Severity | Category | Blocks testing? | Blocks release? | Evidence |
| --- | --- | --- | --- | --- | --- |
| R1 General language understanding within zero speculative-authority boundary | High | Functional/Research | No for bounded parser; broader product cannot be assumed | Yes for broad understanding | Exact-match grammar and source-only outcomes |
| R2 Naturalness, semantic fidelity, repetition, attribution, temporal honesty in sustained use | High | Quality | No after bridges; deterministic subset now | Yes | Finite reference forms; no human evaluation |
| R3 Pattern Engine and meaningful longitudinal formulation/outcome revision | Medium | Research | No for currently admitted narrow procedure | Yes only if claimed | Plan 16; CT12 narrowed authority |
| R4 Archive scaling, full-document writes/replay/reparse, device memory/storage/thermal cost | High | Performance | No; measure bounded envelopes | Yes | E10/E13 and 120/400-source fixture limits |
| R5 Clinical efficacy and suitability for autonomous helping | Critical | Safety/Research | Not a prerequisite to synthetic engineering tests | Yes for clinical claims/use | Pending source/rule reviews |

### Release debt

| ID / debt | Severity | Category | Blocks testing? | Blocks release? | Evidence |
| --- | --- | --- | --- | --- | --- |
| L1 Clinical/rights/legal/software-autonomy/scope/training reviews incomplete | Critical | Safety/Release | No for authorized synthetic tests | **Yes** for genuine therapeutic deployment | Source inventory/rule restrictions; plan 17 |
| L2 No production signing/distribution/accountability/release process | High | Release | No | Yes | Unsigned release, WO15 explicit scope exclusion |
| L3 Privacy/legal/app-store/resource governance and supported population/device decisions | High | Privacy/Release | No for synthetic campaign | Yes | Q15 limitations, plan 17 |
| L4 Documentation drift: risk register, module boundaries, drafts, scope, seal claims | Medium | Release/Provenance | No once report is authoritative baseline | Yes for accurate product representation | Sections 5/6/16/18 |
| L5 Dependency/toolchain drift advisories and compatibility matrix not release-qualified | Medium | Release | No, baseline passes | Yes pending release review | 17 lint warnings; dev minSdk/ABI floor |

## 22. Rigorous-testing readiness adjudication

**CTV2_RIGOROUS_TESTING_NOT_READY**

The requested standard is a system coherent enough that full end-to-end failures have interpretable causes. That standard fails for core Therapy progression and guided Biographer because their production inputs are synthetic/default-empty. Testing months of history through the app would mostly measure a listener plus open prompts and source storage, while qualified multi-stage procedures remain inaccessible.

This is not a recommendation to continue broad architecture construction. The repository already contains much of the required policy and a working protected-store/renderer/application structure. A **bounded production integration precursor** is smaller and more informative than adding a Pattern Engine or a model. It must fill/qualify the current missing observation and coverage bridge; it should not reinterpret missing facts as known just to make UI paths respond.

The disposition is not caused by absent speech/model, current ADB authorization, lint advisories, or unsigned release. Those can be excluded from a typed synthetic test envelope. It is caused by **materially disconnected core product behavior**, matching the Principal's explicit not-ready criterion.

## 23. Minimum precursor and subsequent test campaign design

### Smallest concrete precursor: Production Evidence-to-Procedure and Biographer Coverage Integration

**Exact missing capability:** truthful current-turn/session observation into the existing CT04/05 contracts, and protected-corpus-derived coverage plus operational answer handling into the existing CT10 engine, available from the canonical Android app.

**Scope:**

1. Establish an explicit evidence-backed observer/state transition boundary for the already-admitted ordinary routes. Carry required active route, pending information, confirmations/corrections, options/plan/outcome and material-evidence revision through a session. Unknown remains unknown; no adapter may label fixed assumptions as reported facts.
2. Define and qualify the UI/current-evidence safety input contract under existing CT04 policy. Explicit scope declarations must remain visibly declarations, not invented per-field clinical findings. Current clarification answers and contradictory current information must not be silently discarded. Journal/Biographer must respect any already-authorized scope interruption without improvising specialized treatment. Any missing observation authority must return a bounded unsupported/clarification outcome and be explicitly adjudicated, not filled by a keyword crisis policy.
3. Connect production Biographer to the existing coverage evidence construction, target selection, answer outcome/retry/privacy/decline semantics and explicit posture control. Use governed admission for durable coverage/identity/correction changes.
4. Add only the acceptance checks needed to prove these bridges through the actual runtime/UI. Freeze a named synthetic product envelope before the larger campaign.
5. Establish disposable device/test-data custody for later destructive instrumentation. This is a test-environment prerequisite, not permission to reset the currently installed corpus.

**Explicitly out of scope:** new numbered phase creation in this investigation; new therapies/specialized safety protocols, diagnoses, clinical sources/admission, models, speech, Pattern Engine, remote services, architecture refactor, persistence redesign, aesthetic redesign, dependency upgrades, and release authorization. The current parser may remain conservative; unsupported phrases must be identified in the test envelope. Any truly missing procedure is reported to the Principal instead of silently added.

**Completion criteria:**

- Through the actual typed app/runtime, one synthetic listen, understand, and practical conversation progresses through its already-defined stages; plan/outcome/correction/pause and explicit route change are observable without tests manually supplying the missing state.
- Repeated unchanged substantive evidence triggers the existing stagnation behavior despite another send; new evidence allows justified progress.
- Each safety/ordinary-state field has inspectable current evidence or an explicit unknown/declaration; the tested input contract cannot convert absence of observation into absence of danger.
- Biographer selects a grounded non-null target when a stored eligible gap exists, responds to an answer, excludes private/declined/unchanged recently asked targets, and changes selection for appropriate changed evidence.
- Journal/Biographer sources can contribute to later permitted Therapy recall without changing route/safety authority or losing provenance.
- Canonical 946-test regression remains green, with new meaningful bridge checks and bounded physical acceptance evidence; no failed target response is waived merely because a prompt/capture exists.
- Remaining debt is explicitly assigned to the later campaign; no claim of general clinical, speech, model, or release readiness.

If this cannot be achieved without new policy, stop at that exact unsupported contract and request its adjudication. Do not bundle a large capability roadmap into the precursor.

### Contingent rigorous campaign, after precursor acceptance

This is a test design only, **not an authorized new numbered development phase**. Recommended order: **A → E → I baseline → B and C → D and F → G and H → J**. Safety and privacy invariants run throughout; corpus and language tracks may run concurrently only after deterministic oracles and production reachability are fixed.

| Track | Scope and oracle | Order / automation |
| --- | --- | --- |
| **A. Architectural invariants** | Actual app-root turn traces; safety-before-ordinary, route-before-historical packet, pre-turn revision, no current-source self-recall, zero response→evidence, no private unsurfaced memory, actual call graph versus api compile exposure; malformed/stale authority and candidate manifests | First; automate JVM/static/build and selected instrumentation. Require zero unauthorized paths |
| **B. Therapeutic procedure** | Existing ordinary routes/stages, explicit preferences and transitions, refusals, uncertainty, corrected interpretation, influenceability, readiness, user options/choice, plan, attempted/not attempted/failed/helped outcome, no-progress loops, repeated sessions. Expected actions from unchanged CT05 rules, not new therapeutic opinions | After A/E and precursor; automate synthetic conversations through **production input**; manual semantic review for ambiguous language |
| **C. Longitudinal** | Weeks/months synthetic history, independent event/report/record dates, contemporary versus retrospective accounts, identical names, corrections/supersession/contradictions, private/declined evidence, deletion invalidation, cross-mode recall and Biographer acquisition, restart/replay and old backup restoration. Assert exact source lineage, bounded visible memories and stable route | After I baseline; mostly JVM/integration automation plus physical persistence scenarios |
| **D. Adversarial semantics** | Current/history instruction-like text, quoted diagnosis/advice, invented facts/dates/entities, certainty inflation, hidden motive/trait claims, private memory probes, malformed manifests, question stacking, mode/technique changes, safety override and historical commands. Check final artifact **and** state/authority trace | After A/B/C; automated corpus plus independent human adversarial review. Model-specific variants deferred until a model is separately admitted |
| **E. Safety boundaries** | All established/unknown/tentative/refused/conflicting scope fields, stale permits, declaration changes, contradictory current evidence, unsupported populations and specialized boundaries, historical risk vocabulary inertness; no new crisis scripts/score/clinical-policy expansion | Early gate; deterministic matrix plus actual UI input-boundary checks. Zero unauthorized ordinary continuation |
| **F. Language quality** | Naturalness, exact/semantic repetition, question usefulness, faithfulness, user/third-party attribution, uncertainty, chronology, length/tone, meaning preservation, valid reference variants and exhausted fallback. Compare semantic act and evidence before scoring fluency | After procedural correctness; automate budgets/fingerprints/grounding, blinded human scoring for meaning/naturalness. No model TTFT/quality claim |
| **G. Device robustness** | Disposable target: configuration recreation, background/foreground, process kill at commit boundaries, reboot/unlock, key unavailable, storage faults/fullness, interrupted restore, repeated sessions, mode switches, rapid sends/custody races, permissions/offline. Speech tracks explicitly N/A until separately implemented | After persistence contract and safe target setup; instrumentation/ADB orchestration plus manual device observation. Never run current reset fixtures on valued corpus |
| **H. Performance** | Real cold/warm start, commit→final artifact latency by mode and corpus size, PSS/RSS/peak heap, file size/write amplification, long sessions, low storage, practical battery/thermal observation. Report distributions and device conditions, not three-sample SLA. TTFT/tokens/sec only for separately admitted model | After correct paths exist; automate sampling where feasible; manual thermal/battery controls |
| **I. Privacy/persistence** | Primary/temporary file inspection with synthetic canaries, key custody, bad/wrong/truncated backup, read limits, reset partial failure, replacement restore interruption, user export of private/history content, deletion propagation and transcript cleanup, stale-client ID after restore, no prompt/log/audio leak | Start early on isolated data; automated fault injection/core tests, physical Keystore/SAF/manual picker trials |
| **J. Human usability** | Initially scripted synthetic roles/tasks: identify mode/privacy, silent capture, understand questions, correct/delete source, recover/export, distinguish finished speech versus discard only if speech later exists. Observe confusion, task success, error recovery, repetition and perceived attribution | Last, controlled opt-in observation after safety/privacy gates. No clinical efficacy study or real psychological ingestion inferred |

**Campaign evidence requirements:** freeze commit/tree/APK hash, device/build, exact input scenario, initial corpus/digest, expected authority/semantic action, actual disposition/artifact, latency, and final corpus/digest; distinguish infrastructure failures, unsupported language, policy refusal, renderer rejection and implementation defects. Keep synthetic fixtures only. Evaluate positive behavior and prohibited paths, not just absence of exceptions. A model's self-declared manifest cannot be the sole semantic oracle.

**Human protocol:** begin with consented internal observers performing scripted non-personal tasks on disposable test corpora; record task completion, wrong expectations, accidental commits/custody choices, repetition, comprehension of silence/private state and error recovery. Do not collect personal histories merely to test usability. Define stopping/escalation and data-retention rules before sessions. Clinical effectiveness or therapeutic use requires a separate qualified review/admission process, not a favorable usability score.

## 24. Current testable product envelope

### Thomas can reliably do

Within the **tested synthetic scope**: run a deterministic Android typed-input application; commit exact source text; store it under the governed encrypted local boundary; distinguish source/evidence/provenance; produce true Journal silence; execute defined typed policy and retrieval/renderer constraints; reopen the qualified protected corpus; and perform tested source revision/privacy/deletion and backup operations. “Reliably” here is bounded by passing fixtures and recorded TCL observations, not general real-user certification.

### Thomas can sometimes/boundedly do

Reflect supported Journal statements, ask one grounded question, preserve useful assertions from a small English grammar, invite and store an open biography narrative, reflect current Therapy concerns through LISTEN, and recall eligible prior Journal/Biographer/Therapy sources with bounded attribution and uncertainty. It can store much more prose than it can structurally understand. Device latency and large-history behavior have only small synthetic samples.

### Thomas cannot currently do

Progress normal Android Understand/Practical conversations through the qualified multi-stage procedure; conduct genuine stored-history-driven guided biography acquisition; infer safety from conversation; remember active conversational procedure/transcript across process death; generally understand life narratives; infer a mature evolving psychological formulation or Pattern Engine; run a language model; recognize or synthesize speech; provide cloud sync; or deliver an approved signed release.

### Thomas must not be represented as able to do

Provide clinically validated therapy, diagnose, assess/predict risk, manage crises or specialist conditions, guarantee comprehension/privacy under every device/attacker condition, demonstrate autonomous source/license/clinical authority, or demonstrate model/speech/device capabilities inherited from V1. The term “production integration” denotes technical app composition, not those claims.

## 25. Release-distance assessment

| Milestone | Assessment | Reason |
| --- | --- | --- |
| Architecture-complete | **NEAR** for the current narrow deterministic envelope | Major domains/owners/contracts exist; missing truthful observation/coverage bridges and overbroad compile-authority claims remain. Full forward-plan architecture is not complete |
| Integration-complete | **MATERIAL WORK REMAINS** | Therapy procedure and guided Biographer are disconnected despite technical composition seal; bounded precursor identified |
| Rigorous-test candidate | **NEAR** | Working baseline and substantial test estate; cannot call achieved until precursor acceptance |
| Internal alpha | **NEAR** for a scripted synthetic typed alpha | Runnable physical build exists, but current functionality/privacy recovery gaps prevent broader alpha claims |
| Controlled beta | **MATERIAL WORK REMAINS** | Integration, rigorous behavior/privacy/device qualification, human usability and admission decisions outstanding |
| Release candidate | **MATERIAL WORK REMAINS** | Release signing/artifact reproducibility, safety/rights/product/security acceptance not complete |
| Public release | **NOT YET ASSESSABLE** as a therapeutic product | Clinical/legal/rights/autonomy/population/product claims have not been admitted; publishing the source repository is not product release readiness |

No percentages or calendar estimates are supported by the evidence.

## 26. Recommended immediate next action and investigation closeout

Authorize **Production Evidence-to-Procedure and Biographer Coverage Integration** with section 23's bounded scope and exit criteria. Keep CT16 Pattern Engine, model and speech admission outside it. On completion, repeat this readiness decision against the actual production path, then launch the dedicated deterministic end-to-end campaign.

The immediate decision is whether to authorize that bounded precursor instead of advancing directly to full rigorous product testing or Pattern Engine work. No permission was needed to implement a next phase here because none was implemented.

This report is intentionally **uncommitted**. Entry worktree was clean, but no clear report-only commit convention required a commit. No forward-plan edits, numbered order/tag, source/test/configuration changes, push, or device-data changes occurred. Generated APKs/test reports/provenance database are ignored build artifacts. Final verification found zero staged or tracked modifications, this report as the sole untracked file, unchanged HEAD and forward-plan SHA-256, and git diff --check exit 0. All 75 local evidence links resolved. The canonical report path did not previously exist.

**Primary disposition:** CTV2_RIGOROUS_TESTING_NOT_READY

**Recommended next order:** Production Evidence-to-Procedure and Biographer Coverage Integration

**Principal decision required:** Authorize the bounded production-integration precursor before the full end-to-end campaign.
