# CT-V2-15R1 physical fixture repair and isolated rerun

Date: 2026-09-05. This report records the Principal-authorized qualification repair following [the first physical attempt](CT-V2-15R1-PHYSICAL-QUALIFICATION.md). It preserves that failed attempt and all four attempts in this execution.

## Disposition

**CT_V2_15R1_PHYSICAL_QUALIFICATION_FAILED_PRODUCTION_DEFECT**

The brittle lexical oracle was corrected, and the omitted physical scenarios were added to the qualification fixture. No production source, JVM test, therapeutic policy, safety policy, model configuration, speech boundary or Android production configuration changed.

The rerun found an actual production integration defect on resumed listening. CT-V2-05 selected a valid action at a new material revision, but CT-V2-13 rejected its only available realization as an exact recent duplicate. The runtime returned RENDERING_UNAVAILABLE and no assistant artifact. The mandatory pause/resume gate therefore fails. Later gates are not claimed passed.

The original Thomas installation and corpus remain unchanged. Both disposable packages were removed after the final attempt. Fresh canonical regression passes all 978 JVM tests; that result does not override the failed physical gate. No completion tag, push, or rigorous testing campaign was performed.

## Verified entry and repository changes

| Item | Entry / result |
| --- | --- |
| Root / Git directory | C:\Android Studio Projects\ConundrumThomasV2 / canonical .git |
| Entry HEAD | 2212ea7e7510f7bae8fa0b71d13d31d28df295f2 |
| Entry tree | b3b9866f3d17b4bb0026f8c2ff08c1ba6daed6aa |
| Branch / worktree | main / clean at entry |
| Remote / local upstream | origin https://github.com/primalfunk/conundrum_thomas.git / origin/main 64fa3af9069f820fd465f219e5a21855a7d47047 |
| Ahead/behind | Entry 20/0; fixture commit 21/0; final documentation commit adds one ahead. No fetch or push |
| Recorded baseline | 978 tests, 60 suites, zero failures/errors/skips; lint 0 errors/fatals, 16 warnings |
| Prior physical evidence | Preserved; original private-file hashes still matched its after-cleanup record |
| Fixture packages at entry | Both absent; canonical app and its pre-existing test package present |
| R1 completion tag at entry / finish | Absent / absent |
| Fixture repair commit | d0ca6c4466cf24d16e140786ebb1859a65838b6e |
| Fixture repair tree | 91caaea961dda2366cd7799d6ad60b7b8b768877 |
| Final commit | Documentation-only descendant of the tested fixture commit; exact HEAD/tree in completion response |
| Production source modifications | ZERO |
| JVM test modifications | ZERO |
| Push / history rewrite | None / none |

The code changes are exclusively in app/src/androidTest/java/com/conundrum/thomas/v2:

- CTV215R1ProductionDeviceInstrumentedTest.kt — added full typed-result physical scenarios and cold-reopen method.
- CTV215R1ProductionUiInstrumentedTest.kt — replaced lexical assertions with actual delivered-action/history inspection and added Compose pause, repetition/new evidence, correction, and practical controls.

The previous two-method fixture remains recoverable at entry commit 2212ea7; its failed execution records were not erased. The repaired fixture now has three methods: a complete typed production conversation, b process reopen, and c actual UI controls. All three compile. Only method a was reached in this execution because its mandatory gate failed.

## Oracle design and fixture/production separation

The full semantic fixture opens ThomasAndroidCompositionRoot using InstrumentationRegistry.targetContext and calls the exact ThomasProductionRuntime.submit boundary used by ThomasViewModel. It uses the real Android protected store and AndroidKeyStore, not a JVM substitute. Its only constructed turn data are user-like text, mode/support/privacy/memory preferences, turn identity and actual report time.

No CoreOrdinaryTherapyState, CoverageEvidence, internal confirmation, chosen technique, safety permit or target is injected. Typed observations, plans and render results come from actual production execution.

For each ordinary action the fixture independently asserts the expected existing action ID, ordinary safety permit, state validity and expected output disposition. It then uses the production TherapyRenderCommandAdapter on the actual selected render-support envelope to obtain the authoritative semantic act, mode, budget and reference-realization set. It requires:

- correct semantic act and mode;
- successful validation with exactly VALID;
- question, sentence and character counts within the existing command budget;
- final text in the actual authorized reference set;
- exact surfaced-memory identities matching the authorized support;
- required source attribution and temporal/epistemic markers;
- correct silence with no assistant artifact when the selected act requires NO_RESPONSE.

A fallback passes only when it is an actual validated DETERMINISTIC_FALLBACK, records rejected-candidate reasons, and still satisfies the same command/wording constraints. An external realization, rejected render, wrong action, wrong mode, unauthorized phrase, missing marker, excessive question or arbitrary nonempty response does not pass.

Biographer assertions require a non-null eligible target, real grounding, selected target/plan identity agreement, targeted posture, the correct question semantic act, accepted validation, one question, and the command budget. A read-only invocation of the runtime's existing biographerGrounding projection supplies the exact selected eligible grounding to the existing BiographerRenderCommandAdapter for reference-set checking. It does not select a target or mutate state.

The Compose method retains actual text-entry, commit, mode and support controls. Qualification-only reflection reads the actual session and accepted renderer history; it never writes a field. It asserts the delivered action ID, state transitions, exact response-count changes or required silence, and binds the displayed response to the renderer's accepted response fingerprint. This supplements the full typed-result method because the production ViewModel intentionally does not retain ProductionTurnResult in its UI state.

These are distinct evidence levels: method a drives the same production runtime boundary with real Android persistence; method c drives the physical UI controls. Method c was compiled but not executed after the production failure. No UI execution is implied by method a's results.

## Fixture defects corrected and attempt history

Every attempt started from newly installed, empty disposable packages. No attempt resumed after the failed assertion. The retry procedure verified original hashes and file metadata, preserved fixture-only diagnostics, removed only the disposable packages, rechecked absence/manifest isolation, installed the next fixture, verified its new UID, and ran method a from its first input.

| Attempt | Failure classification | Evidence / correction |
| --- | --- | --- |
| Original physical attempt, preserved separately | FIXTURE_ORACLE_DEFECT | Expected “hear”; actual authorized “What stands out is that the delayed meeting.” Production wording and permitted variants were unchanged |
| Rerun run-01 | FIXTURE_SETUP_DEFECT | Fixture supplied a 2040 report time to Android's real 2026 store clock. First Journal capture returned SOURCE_CAPTURE_FAILED. LongitudinalAdmissionPolicy rejects STORE_TIME_PRECEDES_REPORT_TIME. Fixed fixture committedAt to Instant.now(), matching the UI; no clock injection or policy change |
| Rerun run-02 | FIXTURE_ORACLE_DEFECT | Expected only ACCEPTED_REFERENCE_REALIZATION; production returned a governed fallback on a selected Biographer target. Existing renderer fallback acceptance requires successful validation. Fixed oracle to verify the full validated fallback contract, not merely broaden a success enum |
| Rerun run-03 | FIXTURE_SETUP_DEFECT | Fixture assumed a declined question must always produce a non-null replacement target. Production recorded DECLINED, excluded the target, and returned no question. Available structural-gap openings were already exhausted by the unchanged repetition guard. The privacy scenario was moved to a later actually delivered target; decline was still required not to recur |
| Rerun run-04 | PRODUCTION_IMPLEMENTATION_DEFECT | Valid pause/resume selected core-invite-further-expression at material revision 9, but the sole realization duplicated a recent response and was rejected. This failure was not relaxed or worked around |

Run-03 independently confirmed the fallback rejection reason was REPEATED_OPENING and the accepted fallback satisfied the selected target's reference set and validation. The valid-no-output behavior after decline differs from the failed resume: decline does not require a replacement question, whereas the selected ordinary resumed-listening action requires its authorized response.

A final static fixture review corrected an unexecuted recall assertion: retrieved currentAuthority identifies current eligible evidence, not authority to select Therapy policy. The current source-recall items are active/current; the fixture asserts that fact while separately asserting the route selected from current Therapy state. DeterministicLongitudinalRetriever constructs that flag from currentSourceRevisionIds and eligibility. This one-line correction did not affect or waive the earlier failing resume assertion. Its final APK was compiled but not installed. The exact source snapshots used in run-03/run-04 are preserved with those runs.

Attempt logs, artifacts and source snapshots are under [CT-V2-15R1-RERUN-EVIDENCE](CT-V2-15R1-RERUN-EVIDENCE/evidence-manifest.json). The prior original attempt remains under CT-V2-15R1-PHYSICAL-EVIDENCE.

## Exact blocking production trace

The decisive trace is in [run-04 instrumentation output](CT-V2-15R1-RERUN-EVIDENCE/run-04/instrumentation-a.txt): one method attempted, one failure, 19.042 seconds. Earlier synthetic biography capture and coverage checks had completed. The relevant ordinary sequence was:

| Turn | User input | Actual action / result |
| --- | --- | --- |
| 23 | Current field-specific scope declarations plus My specific concern is: the delayed meeting | Revision 2; core-reflect-established-content; accepted authorized variant “What stands out is that the delayed meeting.” |
| 25 | Yes, that's right | Revision 3; core-invite-further-expression; accepted “What else would you like to say about it?” |
| 27 | That's all for now | Revision 4; core-summarize-listening; accepted |
| 29 | Thank you | Revision 5; core-check-further-or-close; accepted |
| 31 | Stop | Revision 6; core-acknowledge-close; accepted |
| 33 | My specific concern is: a separate project meeting | Revision 7; core-reflect-established-content; accepted |
| 35 | Please pause | Revision 8; core-pause-without-response; validated NO_RESPONSE |
| 37 | I am ready to resume | Revision 9; LISTEN_SUPPORT; core-invite-further-expression; NEW_ACTION; ordinary permit granted; RENDERING_UNAVAILABLE |

At turn 37:

~~~text
safety = ORDINARY_POLICY_ALLOWED
route = LISTEN_SUPPORT
action = core-invite-further-expression
progression = NEW_ACTION
material revision = 9 (previous pause = 8)
semantic act = CLARIFYING_QUESTION
render disposition = RENDERING_UNAVAILABLE
validation.accepted = false
validation.reasonCodes = [EXACT_RECENT_DUPLICATE]
assistant artifact = null
~~~

This fails the required resumed-engagement behavior even though the observer and policy selected the correct ordinary action. It is not an English-variant mismatch: no accepted final response exists.

Mechanism, from unchanged production code:

1. ProductionTherapyInputBoundary.observe recognizes explicit resumed engagement and derives a new material revision.
2. CT-V2-05 selects core-invite-further-expression with NEW_ACTION under the existing ordinary permit.
3. TherapyRenderCommandAdapter.therapyBase provides only “What else would you like to say about it?” for this action. Its therapyVariants only adds alternatives for reflective forms; this interrogative has one realization.
4. DeterministicRenderValidator rejects exact normalized text already in CT_V2_13_RECENT_RESPONSE_WINDOW, which is six accepted responses. The earlier invitation remains in that window; the silent pause does not add a spoken response.
5. The reference candidate and fallback cannot provide another authorized realization. GovernedLanguageRenderer returns RENDERING_UNAVAILABLE.
6. ThomasProductionRuntime produces no assistant artifact and does not falsely mark the undelivered action as delivered.

The existing qualified pieces disagree at this integration boundary: a legitimate recurring procedural act has no currently acceptable realization. This is evidence of an implementation/repertoire integration defect; it does not establish a need for new Therapy policy, safety doctrine, a model or Pattern Engine.

Production evidence paths:

- thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ProductionTherapyInputBoundary.kt — observe and delivered.
- thomas/engine/src/main/kotlin/com/conundrum/thomas/v2/engine/ordinary/CoreOrdinaryPolicy.kt — existing resumed-expression selection.
- thomas/language-renderer/src/main/kotlin/com/conundrum/thomas/v2/languagerenderer/TherapyRenderCommandAdapter.kt — therapyBase and therapyVariants.
- thomas/language-renderer/src/main/kotlin/com/conundrum/thomas/v2/languagerenderer/DeterministicRenderValidator.kt — EXACT_RECENT_DUPLICATE check.
- thomas/language-renderer/src/main/kotlin/com/conundrum/thomas/v2/languagerenderer/RendererContracts.kt — six-response history window.
- thomas/language-renderer/src/main/kotlin/com/conundrum/thomas/v2/languagerenderer/GovernedLanguageRenderer.kt — validated fallback and no-output failure.
- thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ThomasProductionRuntime.kt — submitTherapy delivery boundary.

## Required scenario coverage

The added fixture contains all A–N scenarios. This table distinguishes implemented assertions from physical outcomes; rows reached inside a failed method are checkpoints, not independently passed instrumentation methods.

| Gate | Fixture assertion / run-04 outcome |
| --- | --- |
| A LISTEN | Full reflection, further expression, summary, next direction and stop action sequence observed and validated |
| B UNDERSTAND | Missing information, tentative understanding, confirmation and summary assertions added; not reached |
| C PRACTICAL | User options/choice/plan, real wait, attempted and unattempted outcomes/review assertions added; not reached |
| D CORRECTION | Withdrawal of old interpretation references and corrected summary assertions added; not reached |
| E PAUSE / RELUCTANCE | Explicit pause and required silence passed; first resumed engagement failed rendering. Later refusal and resume not reached |
| F STAGNATION | Same material revision, direction choice, STOP_NO_PROGRESS and no artifact assertions added; not reached |
| G NEW EVIDENCE | Paired explicit-detail revision increase and renewed action assertions added; not reached |
| H SAFETY UNKNOWN | Seven UNKNOWN observations, clarification gate and no ordinary route observed before scope declarations |
| I SAFETY CONFLICT | Later current contradictory observation, two provenance references, no ordinary route, and specialized correction assertions added; not reached |
| J TARGETED BIOGRAPHER | Real non-null eligible TEMPORAL_GAP grounded in Journal 2010/2018 evidence, selected plan identity, one question, reference-set validation observed |
| K BIOGRAPHER ANSWER | Approximate 2014 answer admitted as BIOGRAPHER_GUIDED_TIMELINE, ANSWERED_RELEVANT, material change, different target and coverage digest observed. Nonanswer and decline exclusions also observed; later private-target case not reached |
| L JOURNAL → THERAPY | Exact source/provenance, current route, surfaced support and attribution assertions added; not reached |
| M BIOGRAPHER → THERAPY | Guided provenance, approximate source wording, uncertainty, private evidence exclusion assertions added; not reached |
| N CLOSE / REOPEN | Separate-process method compares corpus digest/revision/count, reconstructs coverage and durable private/declined controls, and requires fresh procedure/safety; not run because method a failed |

The separate Compose method adds actual control coverage for LISTEN, pause/refusal/resume, paired stagnation/new evidence, UNDERSTAND/correction and PRACTICAL. It is compiled, not physically qualified in this execution. The test architecture does not claim full UI behavior from the direct runtime method.

## Device, isolation and custody

Device remained TCL 9491G, serial BC9424B4E3C5002, Android 15 / API 35 / arm64-v8a, fingerprint TCL/9491G_ZZ/Hera_Vis_WIFI:15/AP3A.240905.015.A2/2FA6:user/release-keys.

Original application: com.conundrum.thomas.v2, UID 10666, version 1.0/code 1. First installation remains 2026-09-04 21:17:05 and last update remains 2026-09-04 22:31:39.

Fixture application/test identities remained com.conundrum.thomas.v2.ctv215r1fixture and com.conundrum.thomas.v2.ctv215r1fixture.test. Binary manifest checks required the distinct package/provider identity, exact fixture instrumentation target and absence of sharedUserId. Post-install UID checks preceded test execution. The final run fixture UID was 10172, with /data/user/0/com.conundrum.thomas.v2.ctv215r1fixture.

The store uses application-context noBackupFilesDir; fixture preferences use its own Context. AndroidKeyStore alias text remains ct-v2-14.personal-data.primary.v1, but effective aliases/keys are isolated by Android UID. No original Context, UID/key grant, shared storage or original-package instrumentation is used. Key bytes were not exported or inspected. No new hardware-backed-key claim is made.

All data submitted were synthetic. The fixture's initial snapshot asserted zero sources and store revision zero. No original contents were read or displayed; only hashes, filenames and filesystem/package metadata were inspected. No original application launch, update, data clear, reset, restore, migration, uninstall or key mutation occurred.

Each unsuccessful fixture installation was removed before the next run. After the final failure, both disposable packages were removed successfully and package-manager queries confirmed absence.

Final preservation checks matched:

| Original asset | SHA-256 / preservation |
| --- | --- |
| Installed original APK | 3fa1f8590a16b8eadc85b0e007d7b7e6b67f425aff9f328516b73b5b2c5d2ae4 |
| no_backup/thomas-personal-data/store.ctpd, 1,246 bytes | 0dfd2a464479fbdb9d230347b37ffc44a3ea090a017de02083b2c0c2a61dcd0b |
| shared_prefs/ct-v2-15-device-gate.xml, 65 bytes | 3325d2a819fdd8062c2cdc48a09b995c9b012915bcdf88b1cf9742a7f057c793 |
| files/profileInstalled, 24 bytes | 4f80c4b2cad6d3044bfcb1b2af6892596e9dc8072500276776df78dad4630334 |
| Full original private-file inventory | Identical |
| Sizes, mtimes, inodes | Identical |
| APK path/hash, package version, signing and installation metadata | Identical |

The initial rerun baseline also matched the prior execution's after-cleanup private-file hashes. Custody evidence for intermediate runs and the complete final before/after comparisons are preserved in [run-04](CT-V2-15R1-RERUN-EVIDENCE/run-04/canonical-files-after.sha256) and [custody result](CT-V2-15R1-RERUN-EVIDENCE/custody-result.txt). Reading may affect unmeasured access accounting; no claim about atime is made.

## Physical artifacts and execution identity

All installed fixture app APKs were identical and production-source unchanged:

- Bytes: 31,548,256.
- SHA-256: 3fa8c91211961457287c640fcc40b46605ea2aad784d1830940bf63beedbc729.

The test APKs changed only with qualification code:

| Attempt | Test APK bytes | SHA-256 |
| --- | ---: | --- |
| run-01 | 2,514,427 | 9f222187a0e3c58892b38bd37ffff84d2fd453aef59bf0ce80918df69748edda |
| run-02 | 2,514,371 | dfa324297c4b97bebcfeba18e1dab261566cffcf29e925e65c0c171469500f6f |
| run-03 | 2,517,511 | 94c8fea582738e7f34896b2227e6c870ac91366a883f2196fb7021c0ea0af1b8 |
| run-04 | 2,517,619 | 784c4ed225d2ac68440607f4370ac9ed56ad9c6f38a5ff244d029c555c13643c |

The final compiled-only test APK after the static evidence-authority assertion correction has SHA-256 f34e444c6e14c290ccb54babd46a0b25c57f6045b079eab99dd94d6972b339c6. It was not installed; it is not substituted for run-04's physical artifact identity.

Each attempt ran:

~~~text
adb -s BC9424B4E3C5002 shell am instrument -w -r -e canonicalUid 10666 -e class com.conundrum.thomas.v2.CTV215R1ProductionDeviceInstrumentedTest#aCompleteTypedProductionScenarios com.conundrum.thomas.v2.ctv215r1fixture.test/androidx.test.runner.AndroidJUnitRunner
~~~

The prepared runner only proceeds to b after a passes, and to c after b passes; it requires the actual “OK (1 test)” runner summary, not host adb exit status. Neither dependent method was invoked after a failed. The bounded rerun script and all logs are preserved; raw APKs remain in ignored out/ct-v2-15r1-rerun.

## Canonical regression and authority checks

Final fixture commit d0ca6c4 was compiled with:

~~~powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat -I tools/ct-v2-15r1-device-fixture.init.gradle :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
~~~

Pass: 4 seconds, 133 actionable tasks, 4 executed / 129 up-to-date.

Then the required complete canonical qualification ran:

~~~powershell
.\gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
~~~

**BUILD SUCCESSFUL in 1m 56s; 393 actionable tasks, all 393 executed.**

| Metric | Result |
| --- | --- |
| JVM suites / tests | 60 / 978 |
| Failures / errors / skips | 0 / 0 / 0 |
| Added/removed/weakened JVM tests | 0 / 0 / 0 |
| Lint errors / fatals / warnings | 0 / 0 / 16 |
| Fixture methods compiled | 3 |
| Physical method invocations in this execution | 4 invocations of a, all failed at the documented points |
| Physically passed complete method set | None |
| V1 governance | Default DENIED, migration false, 24/24 DENIED |
| Production diff from entry | Empty |
| Model artifacts / inference backend markers | 0 / 0 in both canonical APK scans |
| Root verifier / diff whitespace check | Pass |
| git fsck --full --strict | Exit 0; pre-existing harmless dangling blob 4350721d914b66808ab247f55e195165fb762cfc retained |
| Tag / push / rigorous campaign | None / none / not started |

Fresh canonical debug APK: 31,548,244 bytes, SHA-256 8888a1f0bc43e86873650d824fc56615a6420aacf013a41074724f07da998b45.

Fresh unsigned release APK: 24,279,311 bytes, SHA-256 200e1930195d06fdcd60d1e42fc1e507a9245aa454930dde26a8e93fce17675d.

Release metadata identifies d0ca6c4466cf24d16e140786ebb1859a65838b6e. Neither canonical APK was installed on the tablet. Later documentation-only commit identity is not claimed as the APK's embedded revision. No byte-reproducibility claim is made.

[Final repository, scope and package checks](CT-V2-15R1-RERUN-EVIDENCE/final-repository-checks.txt) preserve the exact verifier outputs and local upstream identity.

The zero production diff preserves CT-V2-04/05/07/09/10/11/12/13/14/15 authorities and all existing model/speech/Pattern Engine prohibitions. No broader JVM assertion was introduced to accommodate physical behavior.

## Evidence preservation

[Evidence manifest](CT-V2-15R1-RERUN-EVIDENCE/evidence-manifest.json) records original and normalized tracked hashes. Text evidence is preserved as UTF-8 with normalized line endings/trailing whitespace; raw captures and APKs remain in ignored out/ct-v2-15r1-rerun. Only fixture-UID logcat was collected. The original personal corpus was never copied or decrypted.

The first historical physical report and its raw evidence remain preserved. Both that report and the main R1 qualification now point to this current adjudication, so their older pending/fixture-failure dispositions cannot be mistaken for the current frontier.

## Minimum next order and readiness

The smallest blocker is the **production renderer/procedure recurrence integration** exposed by the legitimate resumed-listening action.

A bounded repair should preserve this physical trace as a regression, resolve the lack of an acceptable realization for recurring authorized acts under existing CT-V2-13 authority, and rerun the entire isolated scenario set from empty data. It must not obtain a pass by clearing history arbitrarily, inserting filler turns, treating legitimate new evidence as stagnation, accepting failed rendering, weakening duplicate/semantic validation, adding policy or adding a model.

This order did not implement that production remedy. The qualification fixture is ready to expose it again. Later fixture assertions are compiled but remain physically unproven and may require further evidence-based fixture adjudication.

The mandatory R1 resume gate is failed and the complete physical sequence is unfinished. R1 cannot be sealed, and the full rigorous campaign should not begin under a claim of completed R1 acceptance.

CTV2_RIGOROUS_TESTING_NOT_READY

Recommended next order: CT-V2-15R1 production renderer/procedure recurrence repair and complete isolated physical rerun.

Principal decision required: Adjudicate the documented production defect and authorize its bounded repair before R1 sealing.
