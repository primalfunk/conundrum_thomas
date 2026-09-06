# CT-V2-15R1 renderer/procedure recurrence repair qualification

Date: 2026-09-05. Authority: Principal-authorized bounded renderer/procedure recurrence repair and complete isolated physical rerun. This is CT-V2-15R1; CT-V2-16 and the rigorous campaign remain unopened.

The [previous physical rerun](CT-V2-15R1-PHYSICAL-RERUN.md) remains unchanged evidence of the actual production defect. The original lexical failure and all subsequent failed attempts are also preserved.

## Current disposition

**CT_V2_15R1_PHYSICAL_QUALIFICATION_FAILED_PRODUCTION_DEFECT**

**CT_V2_15R1_RENDERER_PROCEDURAL_RECURRENCE_REPAIR_QUALIFIED**

The authorized recurrence defect is repaired and qualified on the JVM and TCL. The complete production-runtime scenarios A–M and close/reopen gate N passed together. The actual UI-control gate then exposed a different production defect: after reopen, a user turn rendered a response but was not admitted. A minimal diagnostic confirms that the current application allocation formula can reuse a committed turn identity after an unrecorded prompt; the store correctly rejects the conflicting payload.

R1 remains unsealed. No failed gate was waived, no allocator/persistence repair was added, no completion tag or push occurred, and the rigorous campaign was not started. All original-installation custody checks passed; every disposable installation was removed.
## Entry and scope

Verified clean main at c50671c9f11f345891161a6e407f933e10bc1136, tree ac5ff62b0c8cf9e364f55c046fb29d3bd88db91d, 22 ahead / 0 behind locally recorded origin/main. Canonical root is C:\Android Studio Projects\ConundrumThomasV2 with its original .git directory. Root verifier passed; git fsck --full --strict exited zero, retaining informational dangling blobs. No reset, stash, cleanup of unknown work, history rewrite, remote fetch or push occurred.

Production repair commit: 973a360fd55298447ede71b00de31375576126e2. Exactly two production files changed:

- thomas/language-renderer/src/main/kotlin/com/conundrum/thomas/v2/languagerenderer/GovernedLanguageRenderer.kt — bounded exhaustive validation of authorized reference alternatives and fallback.
- thomas/language-renderer/src/main/kotlin/com/conundrum/thomas/v2/languagerenderer/TherapyRenderCommandAdapter.kt — three equivalent surface forms for the existing core-invite-further-expression act, including its original form.

No CT-V2-04/05/10/11/12/14 policy, route, retrieval, observation, session state, persistence semantics, Android production configuration, model, speech or Pattern Engine code changed. The deterministic reference realizer and validator themselves are unchanged. All 978 predecessor JVM tests remain unchanged.

## Exact defect trace and cause

The new CTV215R1RecurrenceRuntimeTest reproduces the physical ordinary conversation through ThomasProductionRuntime.submit, with no internal procedural state injection. Its pre-repair execution failed at the resumed invitation. The initial test discovery attempt required a qualification-only Unit return annotation for JUnit; that discovery error is separately preserved and is not claimed as product failure proof.

At the decisive pre-repair turn:

| Field | Actual value |
| --- | --- |
| Turn / command identity | 37 / android.therapy.37 |
| Current input | I am ready to resume |
| Upstream route | LISTEN_SUPPORT |
| Upstream action / progression | core-invite-further-expression / NEW_ACTION |
| Semantic act / mode | CLARIFYING_QUESTION / THERAPY |
| Semantic units | current-user-content: current user data; therapy-policy-act: governed invitation meaning |
| Surfaced historical support | Empty |
| Budget | 640 characters, 4 sentences, 1 question |
| Authorized reference forms | One: What else would you like to say about it? |
| Deterministic fallback | Same text |
| Normalized response | what else would you like to say about it |
| Response fingerprint | 9c21b5ea3ff3b72fa7d5cb82fa32f148ace12c8388bb810a1f4c3a2293f803f9 |
| Opening / fingerprint | what else would / 2f601b13c11c61a955cd0a481f991daa0d7fbc6bbfd9ddfff8b8cf2c7de65e28 |
| Recent response window | Six accepted responses: turns 23, 25, 27, 29, 31, 33; the invitation at 25 remains present |
| Recent opening window | Four accepted responses: turns 27, 29, 31, 33; the invitation opening is no longer present |
| Pause turn 35 | Policy-authorized NO_RESPONSE; spoken history unchanged |
| Exact duplicate result | EXACT_RECENT_DUPLICATE, one question, one sentence, 41 characters |
| Final disposition / artifact | RENDERING_UNAVAILABLE / null, distinct from NO_RESPONSE |
| Pre-repair canonical render digest | b3ccb215cc4fdcdff17388987755c23cd81542bffcf649c79989fb0d263c5f50 |

The command does not have a separate command-only digest field. Its identity, full immutable command, semantic units, every history fingerprint and final canonicalRenderDigest are captured in the pre-repair trace; the render digest is not mislabeled as a command-only hash.

Root-cause adjudication against the order's alternatives:

| Candidate cause | Finding |
| --- | --- |
| A: single realization for recurring act | Yes, decisive in the physical failure |
| B: selection ignores history | Partly: DeterministicReferenceRealizer considers the last four opening fingerprints, but not the six-response exact-duplicate window |
| C: validation without trying all alternatives | Yes: fallback previously considered only the selected reference and base fallback, not every authorized reference |
| D: fallback reproduces same text | Yes, selected reference and fallback collapse to one candidate in this case |
| E: semantic recurrence versus accidental repetition | Procedural justification was already upstream and correct. No new renderer policy/state classifier is needed or authorized |

Evidence symbols: DeterministicReferenceRealizer.realize; DeterministicRenderValidator.validate; RenderHistoryState.append; GovernedLanguageRenderer.fallback; TherapyRenderCommandAdapter.therapyBase/therapyVariants; ProductionTherapyInputBoundary.observe/delivered; ThomasProductionRuntime.submitTherapy/render.

## Repair and semantic authority

The first reference candidate retains the existing deterministic selection. If it fails, fallback now enumerates the selected reference, every command-authorized reference realization, and the deterministic fallback, deduplicated by text. Each candidate independently passes the unchanged complete validator. The first valid candidate is accepted; rejected reasons remain inspectable. Search is deterministic and bounded by the command's finite authorized repertoire.

For the already-selected further-expression invitation, the adapter retains the original form and adds:

- What more would you like to share about it?
- Is there anything else you would like to say about it?

Each remains one optional invitation about the same referent, without added facts, certainty, memory, advice, route, technique or obligation. This is a fixed deterministic mapping in source, with no generative runtime, remote service or model invocation. The mapping does not inspect session state or decide that resumption is warranted.

Exact duplicates and repeated openings remain prohibited under the original validator. No ordinary-output whitelist, fixed-safety exemption expansion, meaningless suffix, history clearing, filler turn or upstream state manipulation was introduced. Candidate-attempt counters retain their existing external/reference-call meaning; rejectedCandidateReasons captures the additional bounded reference validations.

When every authorized realization is invalid or recently duplicated, rendering still returns explicit RENDERING_UNAVAILABLE with validation failure and unchanged history. It never labels this failure NO_RESPONSE. Infinite surface variation is not claimed. The qualified invariant is that duplicate filtering cannot suppress an otherwise renderable authorized recurrence while an independently valid authorized alternative remains.

The renderer receives no procedural evaluator, safety gate, longitudinal writer or route-selection port. Render history stays linguistic data. ThomasProductionRuntime marks the already-selected action delivered only after an accepted artifact or genuine policy-authorized silence; fingerprints are never converted into procedural state.

## Focused recurrence matrix

Eleven new JVM tests in two suites; all predecessor assertions are unchanged.

| Governing distinction | Test |
| --- | --- |
| Exact physical recurrence, pause silence, new revision, same route/action, distinct accepted invitation, refusal/resume | CTV215R1RecurrenceRuntimeTest.physicalPauseResumeTraceRequiresDistinctAuthorizedResponse |
| Unchanged evidence, upstream direction choice/STOP_NO_PROGRESS, genuinely new detail and distinct authorized reflection | CTV215R1RecurrenceRuntimeTest.unchangedStateStagnatesUpstreamAndNewEvidenceAuthorizesDistinctReflection |
| Exact duplicate with an alternate beyond selected reference/base fallback | CTV215R1RendererRecurrenceTest.exactDuplicateSearchesBeyondSelectedReferenceAndBaseFallback |
| Repeated opening with valid different opening | repeatedOpeningRejectsCandidateButAcceptsAuthorizedDifferentOpening |
| All authorized variants and fallback exhausted | allAuthorizedFormsAndFallbackExhaustedIsExplicitFailureNotSilence |
| Unchanged surface and duplicate fallback remain rejected | oneUnchangedSurfaceAndDuplicateFallbackRemainProhibited |
| Distinct fallback independently valid | distinctFallbackIsIndependentlyValidatedAfterReferenceExhaustion |
| Distinct but unauthorized fallback still rejected | distinctButUnauthorizedMeaningCannotRescueExhaustedCommand |
| Invalid reference alternative still rejected | invalidReferenceAlternativesStillPassThroughFullValidator |
| Fixed safety repetition remains exact | legitimateFixedSafetyPhraseStillRepeatsExactly |
| Journal silence bypasses realization and preserves history | journalNoResponseDoesNotInvokeRealizerOrAlterHistory |

The focused run also included all 32 existing R1 production conversations: 43 tests passed. The pre-repair failure is preserved separately from the post-repair pass.

## Physical execution record

The full isolated sequence uses the real Android composition root, protected store and AndroidKeyStore. Method a drives the same runtime submission boundary used by the UI; method b performs close/process-stop/reopen checks; method c drives actual Compose input controls. A direct runtime pass is not mislabeled as UI execution.

The established fixture package is com.conundrum.thomas.v2.ctv215r1fixture, with instrumentation package com.conundrum.thomas.v2.ctv215r1fixture.test. The original com.conundrum.thomas.v2 and its existing test package are never replaced or used as destructive instrumentation targets.

### Recurrence run-01 — preserved fixture setup failure

One method ran for 192.372 seconds and failed at the later privacy-target setup. The repaired pause/resume and resumed reluctance, stagnation/new evidence, UNDERSTAND/correction, and both PRACTICAL outcome traversals passed their checkpoints.

The exhaustive reference search also successfully rendered the legitimate post-decline Biographer target temporal-gap.1990.2010.292341a9da3ef0d6. Consequently all existing unchanged temporal gaps had been offered. The later fixture incorrectly expected an additional eligible target and dereferenced a null selectedTarget in the authorized OPEN_STORY result.

Classification: FIXTURE_SETUP_DEFECT. DeterministicBiographerCoverageEngine.eligibility explicitly maps unchanged OFFERED targets to RECENTLY_ASKED; the earlier answer/no-answer/decline records cover the other gaps. ThomasProductionRuntime.nextBiographerPrompt explicitly uses OPEN_STORY when targeted selection has no plan. Returning an open invitation here is authorized, not another production recurrence failure.

Fixture-only correction: assert the exhausted eligible-target set and OPEN_STORY posture, admit a new synthetic Journal report (I moved to Boston in 1970.) through production, then require a real selected target grounded in that exact newly admitted source before exercising privacy. No history or coverage state is injected or cleared. Strict target and renderer assertions remain. Additional prompt-decision traces precede target assertions.

Both disposable packages were removed and all original custody comparisons passed after run-01. The entire physical sequence restarts from empty disposable data for run-02; it does not resume after the failed assertion.

### Recurrence run-02 — preserved safety fixture oracle failure

The corrected exhausted-coverage assertion physically passed: all old gaps were either DECLINED or RECENTLY_ASKED and eligibleTargets was empty. New Journal evidence created a selected target grounded in its exact source; privacy, Journal recall, Biographer recall/uncertainty and private-source exclusion all passed their checkpoints.

The run then reached corrected current self-harm relevance. Production correctly returned SPECIALIZED_POLICY_REQUIRED, no ordinary route/permit, no selected safety action and SAFETY_PREEMPTED with no artifact. The fixture incorrectly passed that actionless decision to SafetyRenderRequestFactory, whose explicit contract requires a selected user-facing clarification action. One method failed after 334.571 seconds.

Classification: FIXTURE_ORACLE_DEFECT. No safety policy or runtime response was changed. The helper now requires the exact specialized preemption/no-action/no-permit/no-artifact contract; clarification cases still require independently validated fixed safety rendering. This is not accepting arbitrary silence or suppressing a required response.

Static review also removed artificial fixture turn-number gaps: commits/prompts increment once, and reopen starts at store revision plus one, matching ThomasViewModel's allocation convention. The previous doubled increments and arbitrary plus-ten reopen seed were fixture behavior, not production conversation observations. The change does not inject procedural state, alter corpus contents, clear history or modify the application's allocator. It does not claim general turn-identity durability beyond the exercised scenarios.

The original installation and corpus again matched every custody comparison, and both disposable packages were removed. Run-03 repeats the entire scenario set from a new empty fixture; it does not resume after the safety assertion.
### Recurrence run-03 — complete runtime/reopen pass, UI timeout unresolved

Method a passed all A–M scenarios in 344.396 seconds. Method b passed the separate-process close/reopen gate in 65.445 seconds. At reopen, the exact logical digest was ae29d5ec54331564a2ef882291b8175167f30d1fc34db02f4865202b1d48f3d1; corpus revision 65 and 56 source records were preserved before new turns. Private/declined coverage controls, reconstructed target selection, fresh unknown safety/procedure and eligible recall were asserted.

Method c reached its first accepted listening response, then timed out on the second submission at the fixture's 20-second completion wait. Total method duration was 47.769 seconds. No application exception was present in fixture-UID diagnostics; the available trace did not establish whether the second submission was still processing or whether the input action had not completed.

Classification at this point: UI_COMPLETION_TIMEOUT_UNADJUDICATED. This is not a physical pass and is not automatically labeled a production or fixture defect. The 20-second fixture deadline was not a governing product latency requirement.

For run-04, the qualification-only UI method records input/completion timing and failure state/thread diagnostics, observes for at most 60 seconds, and additionally requires the actual submitted user turn's committed flag. Semantic action, state, mode, response-count and accepted-render-fingerprint assertions remain unchanged. A completed render alone cannot substitute for an admitted user turn. Production code is unchanged.

Both disposable packages were removed and all original custody comparisons passed after run-03. Run-04 starts the complete A–N plus UI sequence again from empty disposable data.
### Recurrence run-04 — runtime/reopen pass, actual UI admission failure

| Method | Result |
| --- | --- |
| aCompleteTypedProductionScenarios | PASS, 347.163 seconds; A_THROUGH_M_COMPLETE=true |
| bColdReopenDurabilityAndCoverage | PASS, 65.121 seconds; N_REOPEN_COMPLETE=true |
| cActualInputControlsAndSemanticProgression | FAIL, 22.015 seconds; first user submission completed processing but committed=false |

The final attempt started from an empty disposable corpus and ran a, b and c in order, with fixture-only process stops between methods. At the end of a the synthetic corpus contained 56 sources at revision 65. Its logical digest 0494e7da2097f64cdb894ef7d524517a49249f3dcf8c88b1d8b30cb8c21da452 matched exactly at b's reopen. Digest differences between independent attempts reflect their real report/record times, not a failure of within-run preservation.

The repaired physical recurrence in a was:

| Input / turn | Actual governed result |
| --- | --- |
| Yes, that's right / 13 | core-invite-further-expression; What else would you like to say about it?; VALID |
| Please pause / 18 | revision 8; core-pause-without-response; true NO_RESPONSE |
| I am ready to resume / 19 | revision 9; LISTEN_SUPPORT; core-invite-further-expression; NEW_ACTION; Is there anything else you would like to say about it?; VALID |
| I don't want to discuss this / 20 | revision 10; qualified reluctance; true NO_RESPONSE |
| I want to continue / 21 | revision 11; same authorized invitation act; What more would you like to share about it?; VALID |

Earlier repaired attempts also observed validated fallback on resume; the deterministic choice varies with command identity while the semantic oracle remains unchanged. No exact duplicate was accepted to obtain these passes.

### Final scenario coverage

| Required scenario | Observed proof in run-04 |
| --- | --- |
| A LISTEN | Reflection, invitation, summary, further/close and stop sequence; exact expected actions and validated rendering |
| B UNDERSTAND | Missing detail, tentative understanding, confirmation and summary; UNDERSTAND_CLARIFY route |
| C PRACTICAL | Two full user-option/choice/plan/wait/review traversals, with NOT_ATTEMPTED and ATTEMPTED outcomes |
| D CORRECTION | Old interpretation references withdrawn; replacement understanding and summary exclude the cancelled interpretation |
| E PAUSE / RELUCTANCE | Explicit pause/refusal silence and both resumed invitations pass |
| F STAGNATION | Formatting-equivalent submissions retain revision 12; direction choice then STOP_NO_PROGRESS with no artifact |
| G NEW EVIDENCE | Explicit manager/date detail raises revision to 13 and authorizes a new validated reflection |
| H SAFETY UNKNOWN | All seven inputs initially UNKNOWN; later withdrawal retains UNKNOWN and deterministic clarification |
| I SAFETY CONFLICT | Current opposing declaration produces CONTRADICTORY with both references; correction produces SPECIALIZED_POLICY_REQUIRED, no ordinary route/permit/action/artifact |
| J TARGETED BIOGRAPHER | Non-null eligible 2010–2018 gap grounded in stored Journal assertions; plan/target identity and one-question contract |
| K BIOGRAPHER ANSWER | Approximate 2014 answer changes coverage/next target; nonanswer, decline, exhausted coverage, new evidence and exact-grounded private target exclusions |
| L JOURNAL to THERAPY | Exact original Journal source/provenance and bounded surfaced support; private Journal source excluded |
| M BIOGRAPHER to THERAPY | BIOGRAPHER_GUIDED_TIMELINE provenance, exact approximate source wording and uncertainty; later source privacy excludes recall |
| N CLOSE / REOPEN | Different process PID, identical durable digest/revision/count, reconstructed coverage, durable private/declined controls, fresh unknown procedure/safety and eligible recall |
| Actual UI control/admission gate | FAILED: processing completed for first typed submission, but the transcript's user committed flag was false; later controls were not reached |

A–N are real Android-runtime and protected-store observations, not claims that the full actual UI method passed. The additional UI admission assertion exposed a gap that the older response/action-only oracle missed.

### Newly confirmed blocker: reopened turn identity reuse

The physical UI input completed in 7,576 ms after input-entry timing began. It did not consume the expanded 60-second observation deadline. The fixture then failed the mandatory assertion that the actual user transcript item was committed. MainActivity.TranscriptBubble already shows an uncommitted user item as Not saved; the failure is not described as completely hidden from the UI.

The precise identity-reuse mechanism is independently reproduced by CTV215R1ReopenIdentityDiagnosticTest through the production runtime and the exact current ThomasViewModel allocation formula:

1. Empty store revision is 0; the UI initializes nextTurnIndex to 1.
2. An open Biographer prompt consumes client index 1 without admitting user evidence; store revision remains 0.
3. A Therapy user turn uses index 2, commits source therapy.android-session.android-therapy-2 and leaves store revision 1; the next unused interaction index is 3.
4. Close/reopen initializes the UI counter from storeRevision + 1, yielding 2 again.
5. A different user turn now reuses android-therapy-2. ProtectedPersonalDataStoreImpl.submit correctly rejects IDEMPOTENCY_KEY_PAYLOAD_CONFLICT.
6. LongitudinalTherapyIntegrationEngine reports REJECTED_IDEMPOTENCY_CONFLICT with a null capture receipt. ThomasProductionRuntime still produces a validated reflection from current observations: disposition COMPLETED, committedSourceId=null.
7. The original source remains intact and store revision remains 1. The new input is not admitted.

This diagnostic deliberately asserts correct conflicting-identity rejection and preservation of the old record. Its passing test status is not a passing UI-admission acceptance result. It does not repair or legitimize the allocator. It adds one diagnostic test beyond the eleven recurrence tests.

Evidence: app/src/main/java/com/conundrum/thomas/v2/ThomasViewModel.kt, nextTurnIndex initialization, allocateTurnIndex and requestBiographerPrompt; ThomasProductionRuntime.identity/submitTherapy; LongitudinalTherapyIntegrationEngine.capture; ProtectedPersonalDataStoreImpl admission idempotency guard. The exact typed diagnostic is preserved in [reopen-identity-diagnostic.xml](CT-V2-15R1-RECURRENCE-EVIDENCE/reopen-identity-diagnostic.xml).

Physical evidence directly establishes failed UI admission after reopen. The minimal JVM diagnostic establishes the allocator's conflicting-identity mechanism independently; it does not pretend that the physical test retained an internal ProductionTurnResult from the ViewModel. The earlier second-submission UI timeout remains unresolved and must be re-exercised in the next complete rerun.

Classification: PRODUCTION_IMPLEMENTATION_DEFECT, subtype REOPENED_UI_TURN_IDENTITY_REUSE_CAUSES_SOURCE_ADMISSION_FAILURE. This is outside the authorized renderer recurrence repair. No allocator, source-admission guard or persistence semantics were changed to make it pass.

### Device identity, isolation and custody

TCL 9491G; ADB BC9424B4E3C5002; Android 15 / API 35 / arm64-v8a. Fingerprint: TCL/9491G_ZZ/Hera_Vis_WIFI:15/AP3A.240905.015.A2/2FA6:user/release-keys.

Original package com.conundrum.thomas.v2 remained UID 10666, version 1.0/code 1, first installed 2026-09-04 21:17:05 and last updated 2026-09-04 22:31:39. Original APK SHA-256 remained 3fa1f8590a16b8eadc85b0e007d7b7e6b67f425aff9f328516b73b5b2c5d2ae4.

Before each installation the binary manifests established distinct fixture application/test/provider identities, exact fixture instrumentation target and no sharedUserId. The fixture uses its application Context for noBackupFilesDir and private preferences. Effective AndroidKeyStore key namespaces are separated by UID; the literal alias text ct-v2-14.personal-data.primary.v1 is the same, not falsely claimed to be a different string. Fixture UIDs were 10184, 10193, 10211 and 10236; each was checked against 10666 before execution. No original UID grant, shared key access or original Context was introduced.

The fixture reported AndroidKeyStore AES hardwareBacked=true and userAuthenticationRequired=false on the device. This is the provider's observed key-property report, not hardware attestation or a new security certification. Original keys or contents were not exported, inspected or decrypted.

| Original private file | Bytes / mtime / inode | SHA-256 before and after every run |
| --- | --- | --- |
| no_backup/thomas-personal-data/store.ctpd | 1246 / 1788586360 / 97279 | 0dfd2a464479fbdb9d230347b37ffc44a3ea090a017de02083b2c0c2a61dcd0b |
| shared_prefs/ct-v2-15-device-gate.xml | 65 / 1788586340 / 114513 | 3325d2a819fdd8062c2cdc48a09b995c9b012915bcdf88b1cf9742a7f057c793 |
| files/profileInstalled | 24 / 1788586350 / 108624 | 4f80c4b2cad6d3044bfcb1b2af6892596e9dc8072500276776df78dad4630334 |

The full private-file inventory, hashes, sizes, mtimes and inodes matched before/after. APK path/hash, version/signing/install metadata also matched. Both disposable packages were successfully removed after every attempt; the original app and its pre-existing test package remain present. No original launch, update, force-stop, data clear, migration, reset, restore, uninstall or key mutation occurred. Only non-content custody evidence was collected; atime preservation is not claimed.

### Physical artifacts

All four runs used the same repaired production fixture APK: 31,548,256 bytes, SHA-256 ade3c7a78f74652f51bad5299563b1673bb0f349c60ae563dd69b66db606fc2a.

| Run | Instrumentation APK bytes | SHA-256 |
| --- | --- | --- |
| 01 | 2423374 | 03d8463aa97d86daf0fadb3ac74eb6ea6365e5c807644dd633e9bf8d9c79903f |
| 02 | 2522103 | 756244856ddecc623c36596928a52483d71756d490c24466091c108cff576aee |
| 03 | 2522471 | d1785b3bc5861dd57f1c7ab010e8467ae36f34fa66a98ea64a40335004df4213 |
| 04 | 2525643 | b04738cabc91f1a829e62d6ea9d809c4482655e509f7a09d2446e41d68238ab6 |

Every method was invoked with adb shell am instrument -w -r, the exact fixture test runner, explicit class/method and -e canonicalUid 10666. The preserved execution script checks the runner's actual OK (1 test) summary, not adb's host exit code, and stops dependent methods after failure. Raw APKs remain in ignored out/ct-v2-15r1-recurrence; they are not committed.
## Canonical regression and artifacts

Initial full post-repair qualification at 973a360fd55298447ede71b00de31375576126e2:

~~~powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
~~~

BUILD SUCCESSFUL in 1m 59s; 393 actionable tasks, all executed. 989 tests / 62 suites, zero failures/errors/skips. Lint: zero errors/fatals, 16 warnings. Eleven new recurrence tests; all 978 predecessor tests retained.

Initial canonical debug APK: 31,548,244 bytes, SHA-256 dbb0428ae7dd2c70767bf72810bb3fde59a6f59814ad3ffcb5a303e724be7c9b.

Initial unsigned release APK: 24,279,311 bytes, SHA-256 3d775fb225a64535960c13222613a639c2479a13eccf1f5766103244afb9a695; embedded revision 973a360fd55298447ede71b00de31375576126e2. Neither canonical APK was installed on the device. These are identified artifacts, not a byte-reproducibility claim.

### Final canonical qualification

After the confirmed identity diagnostic was committed at 333f5b26ba4bd5edc4aee54aef87f811d6e1ea73 (tree 2baa84b9a0448989ee2efda9038982265fb5295e), the complete canonical command ran again.

BUILD SUCCESSFUL in 2m 2s; 393 actionable tasks, all executed. Final inventory: **990 tests in 63 suites, zero failures/errors/skips; lint zero errors/fatals and 16 warnings**. This comprises all 978 predecessor tests, eleven recurrence tests and one explicit identity-conflict diagnostic. The diagnostic's pass establishes correct conflict rejection, not successful reopened UI admission.

An intervening full build before the diagnostic passed 989 tests in 62 suites in 2m 4s; its log is preserved separately. Repetition of the canonical command followed qualification-source changes; no predecessor test was weakened.

Final canonical APK identities:

- app/build/outputs/apk/debug/app-debug.apk: 31548244 bytes; SHA-256 dbb0428ae7dd2c70767bf72810bb3fde59a6f59814ad3ffcb5a303e724be7c9b.
- app/build/outputs/apk/release/app-release-unsigned.apk: 24279311 bytes; SHA-256 a5fedb295b6771afc621dd8b0c879d571193fbbe2dbbb16d306ac5df1e5ad54f.

Release metadata identifies 333f5b26ba4bd5edc4aee54aef87f811d6e1ea73. The final documentation-only commit follows this tested source state; it is not falsely claimed as the embedded APK revision. Both canonical APK scans found zero model artifacts and zero listed inference markers. Neither was installed on the protected device.

## Authority audit

| Authority path / invariant | Current evidence |
| --- | --- |
| Renderer to Therapy policy decision | 0; no evaluator call or port |
| Renderer to Therapy route selection | 0; only renders the upstream command |
| Renderer to safety decision authority | 0; fixed-safety adapter/validator contract unchanged |
| Renderer to persistence writes | 0; no writer/store port |
| Render history to procedure state | 0; runtime history references are creation, linguistic rendering update and lifecycle reset |
| Model-backed realizers / model artifacts / remote inference | 0; composition unchanged, canonical APK scans recorded |
| Duplicate handling cancels an otherwise renderable authorized recurrence | 0 in the qualified recurrence matrix; full authorized space is validated before explicit exhaustion |
| Procedural stagnation | CT-V2-05 remains upstream; unchanged-state/new-evidence production regression passes |
| Journal NO_RESPONSE | Preserved; no realizer invocation or history append |
| Fixed safety wording repetition | Preserved; exemption unchanged and explicitly tested |
| V1 governance | Default DENIED; migration false; 24/24 components DENIED |
| Speech / Pattern Engine / CT-V2-16 | No implementation or authority introduced |

Counts concern execution/decision authority. Existing compile-visible API re-exports are not misrepresented as absent dependencies.

## Evidence index and final repository disposition

The [evidence manifest](CT-V2-15R1-RECURRENCE-EVIDENCE/evidence-manifest.json) records original capture paths, original SHA-256 and normalized tracked SHA-256. Text is preserved as UTF-8 with normalized line endings and trailing whitespace; original captures and APKs remain in ignored out/ct-v2-15r1-recurrence. Only fixture-UID diagnostics and original non-content custody metadata were collected.

- [Exact pre-repair command/history trace](CT-V2-15R1-RECURRENCE-EVIDENCE/exact-pre-repair-trace.txt), [expected failing runtime regression](CT-V2-15R1-RECURRENCE-EVIDENCE/pre-repair-runtime.xml), and [post-repair runtime pass](CT-V2-15R1-RECURRENCE-EVIDENCE/post-repair-runtime.xml).
- [Focused renderer matrix](CT-V2-15R1-RECURRENCE-EVIDENCE/renderer-matrix.xml).
- Final run-04 [A–M instrumentation](CT-V2-15R1-RECURRENCE-EVIDENCE/run-04/instrumentation-a.txt), [reopen instrumentation](CT-V2-15R1-RECURRENCE-EVIDENCE/run-04/instrumentation-b.txt), [actual UI admission failure](CT-V2-15R1-RECURRENCE-EVIDENCE/run-04/instrumentation-c.txt), and [custody/cleanup disposition](CT-V2-15R1-RECURRENCE-EVIDENCE/run-04/final-disposition.txt).
- [Minimal reopened-identity diagnostic](CT-V2-15R1-RECURRENCE-EVIDENCE/reopen-identity-diagnostic.xml).
- [Final canonical command output](CT-V2-15R1-RECURRENCE-EVIDENCE/final-canonical-regression.txt), [990-test suite inventory](CT-V2-15R1-RECURRENCE-EVIDENCE/final-suite-inventory.json), and [final APK identities/scans](CT-V2-15R1-RECURRENCE-EVIDENCE/final-canonical-artifacts.json).
- [Authority audit](CT-V2-15R1-RECURRENCE-EVIDENCE/authority-audit.txt), [V1 governance](CT-V2-15R1-RECURRENCE-EVIDENCE/v1-governance.json), and [final Git/root/package checks](CT-V2-15R1-RECURRENCE-EVIDENCE/final-repository-checks.txt).
- All earlier attempts in this order, their source snapshots, manifests, APK hashes, instrumented failures and custody comparisons remain in the same evidence tree. The previous orders' evidence directories and reports are unchanged.

Commits in this order: 973a360 (production recurrence repair and eleven regressions), 3285ec8/bd654df (privacy-fixture setup and exact grounding), 8331a64 (specialized-safety oracle and application-like fixture allocation), 330b3f7 (UI timing/admission oracle), 333f5b2 (identity-conflict diagnostic), followed by a documentation-only evidence commit. Exact final HEAD/tree are supplied in the completion response.

Branch main remains local, against origin https://github.com/primalfunk/conundrum_thomas.git. Locally recorded upstream remains 64fa3af9069f820fd465f219e5a21855a7d47047. Final tested source was 28 ahead / 0 behind; the documentation commit adds one ahead. Git integrity and canonical-root checks pass, with pre-existing informational dangling blobs retained. The completion tag is absent. No push or history rewrite occurred.
## Final physical and sealing adjudication

The renderer/procedure recurrence repair is qualified. The complete physical runtime and reopen set passed in two coherent fresh runs, but the actual UI admission gate failed on a newly demonstrated production defect. A green JVM suite and passed A–N runtime scenarios do not waive that gate.

No annotated completion tag is created. In particular, ct-v2-15r1-production-evidence-procedure-biographer-integration remains absent. CT-V2-16, model/speech admission, new Therapy/safety doctrine, release/signing work and the full rigorous campaign remain unopened.

The smallest next repair is production turn-identity allocation across unrecorded prompts and reopen. It must preserve the existing idempotency-conflict rejection and prior evidence, ensure new committed user turns receive unused identities, and demonstrate admission of both old and new input across restart. It should retain the failure trace, then rerun the full isolated A–N and actual UI sequence, including the prior timed-out second submission. No broad persistence or UI redesign is justified by this order.

Clinical efficacy, arbitrary-language understanding, reboot/upgrade qualification, release readiness, unlimited recurrence variation and general performance qualification are not claimed. The existing UI startup delay and earlier completion timeout remain device/performance qualification debt; the mandatory new source-admission defect is an implementation blocker.

CTV2_RIGOROUS_TESTING_NOT_READY

Recommended next order: CT-V2-15R1 durable turn-identity allocation repair and complete isolated physical rerun.

Principal decision required: Adjudicate the confirmed turn-identity defect and authorize the bounded allocation repair before R1 sealing.
