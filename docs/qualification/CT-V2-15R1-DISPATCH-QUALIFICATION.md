# CT-V2-15R1 submission-dispatch investigation and final requalification

Date: 2026-09-06. Principal authorization: preserve/recover the archived durable-identity candidate; investigate the submission boundary; make only an established bounded repair; rerun all software and isolated physical gates; integrate and seal only on complete pass.

## Disposition

```text
CT_V2_15R1_OPEN
CT_V2_15R1_UNSEALED
CT_V2_15R1_SUBMISSION_DRIVER_ROOT_CAUSE_ESTABLISHED
CT_V2_15R1_TARGETED_DISPATCH_REGRESSION_PASSED
CT_V2_15R1_DURABLE_IDENTITY_SOFTWARE_QUALIFICATION_PASSED
CT_V2_15R1_FULL_PHYSICAL_QUALIFICATION_FAILED
CTV2_RIGOROUS_DETERMINISTIC_END_TO_END_TESTING_NOT_AUTHORIZED
```

The final mandatory UI semantic-progression gate failed. Canonical main was never modified; no integration, commit, tag or push occurred. The two dependent UI reopen gates were not run. The candidate, patch/archive, diagnostics and physical evidence are preserved for the Principal. No repair scope was expanded after this failure.

## Actual final blocker

Final run-08 passed the complete A–M runtime sequence and N cold reopen. UI method c then passed the first 15 semantic checkpoints and admitted/committed its sixteenth input:

```text
My specific concern is: arranging a new UI meeting
```

After the fixture's Practical control click, the sixteenth turn completed in 36,417 ms and committed as android-therapy-84. Its draft cleared; exact submitted content was found in production persistence; committed=true; before frontier/revision=83; after frontier/revision=84; Not saved absent.

The next assertion failed at CTV215R1ProductionUiInstrumentedTest.kt:76, invoked at line 129:

```text
expected actionHistory.last.actionId: core-verify-problem-understanding
observed actionHistory.last.actionId: core-summarize-shared-understanding
```

The observed last action is also the preceding successful turn's action. This assertion alone does **not** prove that the new input selected or rendered that action. The capture does not establish whether the Practical preference event was accepted, whether carried procedural state explains the result, or whether the scenario's expectation is wrong. It establishes a mandatory actual-UI semantic-progression failure after successful submission and persistence. No new production policy defect or corrective change is claimed.

Method c failed after 712.553 seconds total. The runner stopped, captured evidence and removed both disposable packages. Methods d and e (first additional UI identity reopen, second reopen/subsequent allocation/activity recreation) remain UNQUALIFIED. This blocks sealing independently of the passing targeted dispatch and identity tests.

## Recovered candidate and identity repair

Canonical entry and final HEAD: 4f822fb3498b67b40124c0fb4ec1f19f98c92c8b.
Canonical entry and final tree: 69fa2a2b68105c4371574ac81316aa6002fd4d93.
Branch main; clean; ahead 29 / behind 0 against locally recorded origin/main.

The preserved prior candidate was verified before changes:

- out/ct-v2-15r1-identity/candidate.patch SHA-256: 0ba2c36b74c5921e6ead040e618861bf33a3ad748b957049002eef9487fc0a0f.
- out/ct-v2-15r1-identity/candidate.zip SHA-256: c0476eca60faf75220312dfb42bf158ebfe1c2dae2461feecabc221917df148c.
- Forward and reverse patch checks: PASS.
- Reconstructed archive contents: all 63 files exactly matched initially.
- Environment: independent detached local clone at the canonical entry commit, under out/ct-v2-15r1-dispatch/workspace. Canonical main remained unchanged throughout.

The durable allocator was not reinvented. Its original deterministic oracle consumes unrecorded prompt index 1, commits canonical turns 2 and 3, then reopens with store revision 2. The old storeRevision + 1 allocator chooses 3 and strict persistence rejects the different payload with IDEMPOTENCY_KEY_PAYLOAD_CONFLICT, no admitted source and no revision increase. Revision counts mutations rather than all allocations.

The preserved repair uses the highest committed canonical numeric identity in active and archived redacted admission history together with the live allocation frontier, followed by checked increment. The existing shared per-corpus sequence includes Journal/Biographer/Therapy turns, prompts, source revisions and lifecycle commands. Exact canonical key families determine membership. Deletion preserves archived admission identities. Gaps, out-of-order history and revision divergence cannot lower this maximum. Checked increment prevents numeric wraparound.

The same repaired oracle chooses 4, commits at revision 3, then chooses 5 after another reopen. Existing committed identities are unchanged; no record-count equivalence, revision equivalence, history rewrite, storage migration or persistence-defense change is introduced. The existing single-runtime writer and corpus reset/replacement contract remain in force.

[The pre-repair identity trace](CT-V2-15R1-DURABLE-IDENTITY-TRACE.md) documents definitions, domain, every production caller, persisted keys, reopen/reconstruction, the revision convention and compatibility. [Archived failure-first evidence](CT-V2-15R1-IDENTITY-EVIDENCE/) retains the failing test and repaired results.

## Established submission failure mechanism

The historical seventh-input timeout had no event-boundary instrumentation and did not locate the cause. Controlled repetitions against the exactly reconstructed identity candidate established a **qualification-driver coordinate race during real OS IME animation**.

The last confirmed boundary was a touch delivered to the Compose root at an obsolete position. **SUBMIT ACTION NEVER FIRED; VIEWMODEL ENTRY NEVER REACHED.** The touch missed the enabled button. There was no accepted production action, no guard rejection, no job launch, no runtime admission and no store capture for that missed touch.

- Run-01: extra pre-click geometry observation synchronized the path; the formerly failing seventh input and following resume committed as 75 and 76. The diagnostic was deliberately stopped afterward, not counted as a completed qualification.
- Run-02: a one-element PowerShell method-array shape error prevented test launch; corrected as a runner error, not product evidence.
- Run-03: original unsynchronized touch path failed at ordinal 9 with the exact curly-apostrophe specimen, processing=false and draftLength=28; no UI_ON_CLICK or downstream marker.
- Run-04: same failure at ordinal 8, “I want to continue”, rules out a phrase-specific or fixed-turn-count cause. Button center was (1478,1832); IME movement put its bottom at y=1823; delivered DOWN/UP remained (1478,1832), outside the button. No callback or ViewModel marker followed.
- Run-05 deterministic failure oracle: old center (1478,2328), new keyboard-open button rectangle (1380,1552,1576,1632); assert old point is outside, inject it, then immediate admission assertion fails in 3.267 seconds. This is a measured mechanism, not a timeout inference.
- Run-06 repaired targeted gates: controlled regression PASS 3.965 s; 20 exactly-once touch submissions PASS 44.901 s; separate-process exact-source durability PASS 2.777 s.
- Fresh final run-07, after full software qualification: the same three gates PASS 4.205 / 45.129 / 2.720 s.

[Complete submission trace](CT-V2-15R1-DISPATCH-EVIDENCE/PRE-REPAIR-TRACE.md) covers UI/IME predicates, callback, ViewModel, launch, worker, pipeline, persistence, rendering, draft ownership, completion and lifecycle. [Root-cause evidence](CT-V2-15R1-DISPATCH-EVIDENCE/ROOT-CAUSE-AND-REPAIR.md) adjudicates all seven requested boundary states.

Successful diagnostic controls record UI_ON_CLICK → VIEWMODEL_ENTER → GUARD_ACCEPT → IDENTITY_ALLOCATED → PROCESSING_TRUE → JOB_ENTER → WORKER_ENTER → RUNTIME_ENTER → PIPELINE_ACCEPT → STORE_COMMITTED → WORKER_RETURN → cleared draft. Temporary markers avoid personal content; qualification fixtures use synthetic text. Diagnostic production instrumentation was completely removed before final qualification.

## Bounded correction and silent-no-op defense

No production submission-dispatch patch is warranted by the established missed-touch cause. UiSubmissionDriver is Android qualification code: observe IME animation lifecycle, dismiss the keyboard through Espresso's ordinary UI action, require keyboard invisibility/no active animation, synchronize Compose layout, then resolve and tap the enabled submit button.

It does not call ViewModel/semantic click handlers directly, retry a missed tap, disable animations, add fixed sleeps or extend the original 60-second pipeline timeout. The separate five-second keyboard predicate bounds an explicit OS transition.

The controlled regression retains the same real keyboard movement and immediate admission assertion, replacing obsolete-coordinate delivery with the corrected driver. Both UI fixtures immediately assert processing has started or exactly one user turn already exists. Completion asserts exactly one user/source, committed status and cleared draft; the stress reopen test verifies all 20 exact persisted sources. These assertions make a future silently missed admission fail at its boundary. Existing semantic and persistence assertions were not weakened.

## Scope

Production, restored unchanged from the archived identity candidate:

1. app/src/main/java/com/conundrum/thomas/v2/ThomasViewModel.kt — remove revision-derived counter and delegate every existing allocation caller to the current runtime. SHA-256 08a2b697208d8ada205d6166c4bc56046a26053ba0f6e443d968333377bfe902.
2. thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ThomasProductionRuntime.kt — synchronized durable/live canonical frontier allocation. SHA-256 c86e1222569557f72b26fd5ca8b9f7a8f26cb408d18613b04b99f4ce1fb51e20.

Tests:

- qualification/src/test/kotlin/com/conundrum/thomas/v2/qualification/CTV215R1DurableTurnIdentityTest.kt — ten archived identity regressions, unchanged.
- app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1ProductionDeviceInstrumentedTest.kt — archived runtime fixture uses the allocator and retains strict collision checks; unchanged from candidate.
- app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1ProductionUiInstrumentedTest.kt — preserves archived semantic/identity/reopen assertions; adds synchronized actual touch and immediate/exactly-once admission checks.
- app/src/androidTest/java/com/conundrum/thomas/v2/UiSubmissionDriver.kt — OS IME/touch coordination.
- app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1DispatchInstrumentedTest.kt — controlled movement, 20-turn dispatch, separate-process durability tests.

Diagnostics/evidence:

- CT-V2-15R1-DISPATCH-EVIDENCE/ — trace, diagnostic-only source snapshots/hashes, runners, all eight captures, build/lint/suite inventories, source audit and raw-to-normalized capture manifests.
- Existing durable identity trace, historical qualification report and IDENTITY-EVIDENCE restored from archive.
- This report, CT-V2-15R1-DISPATCH-QUALIFICATION.md, records the superseding investigation and failed final requalification. It remains in the isolated candidate; canonical documentation was not changed.

MainActivity, persistence implementation/collision defenses, renderer/recurrence/strict duplicates/semantic authority, therapeutic ontology/policy, longitudinal reasoning, Pattern Engine, model prompts/authority, STT/TTS, privacy/governance and corpus contents are unchanged. No test deletion, assertion weakening, skip or timeout inflation occurred.

Git checks required EOF-only blank-line normalization in six evidence files and one new driver file. Before/after identities are recorded in formatting-normalization.json. Rebuilding produced byte-identical app and test APKs to the physically tested artifacts. Original archived patch/ZIP remain immutable; production files retain exact archived bytes.

## Software qualification

Canonical clean command in the isolated candidate:
gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain.

PASS in 1 minute 44 seconds; 393 actionable tasks, 389 executed and 4 up-to-date.

| Gate | Result |
| --- | --- |
| JVM suites | 64 |
| JVM tests | 1,000 |
| Failures / errors / skips | 0 / 0 / 0 |
| Identity regression | 10 / 10 pass, included above |
| New Android regressions | 3 / 3 pass on TCL, separately counted |
| Lint errors / fatals | 0 / 0 |
| Warnings | 16 app + 1 unchanged library ChromeOsAbiSupport = 17 aggregate |
| Warning adjudication | Accepted nonfatal class already documented in archived identity candidate; no new warning-producing production change |
| Clean Android build / provenance generation | PASS |
| V1 governance register | Default DENIED, migration false, 24/24 components DENIED |
| Scope / root / Git integrity | PASS; harmless dangling blobs retained |

Canonical debug APK: 31,564,628 bytes; SHA-256 3819e58ff9ad224225f8e3be2256c76efb119f1f69048acf0febe8c01fb42992.
Canonical unsigned release APK: 24,295,695 bytes; SHA-256 ae54829ca31fd569c3bcdbfa0de87a701dd759862f585599ab4a0e80e35df6e4.

Both match the prior identity candidate. Captured before disposable-package build; neither canonical APK installed. Release provenance refers to entry HEAD. No general byte-reproducibility claim is made. Full build log, all suite counts, lint issue inventory and targeted XMLs are retained.

## Final physical coverage

TCL 9491G, serial BC9424B4E3C5002, Android 15/API 35, arm64-v8a.
Fingerprint: TCL/9491G_ZZ/Hera_Vis_WIFI:15/AP3A.240905.015.A2/2FA6:user/release-keys.

After full software qualification, run-07 used a fresh empty disposable installation for dispatch tests. Run-08 used another fresh empty installation and began the complete a–e sequence from the start, force-stopping only the fixture between methods. No previous pass waived a rerun.

| Final gate | Result |
| --- | --- |
| run-07 controlled keyboard movement | PASS, 4.205 s |
| run-07 20 consecutive accepted actual UI submissions | PASS, 45.129 s |
| run-07 all 20 exact sources after process reopen | PASS, 2.720 s |
| run-08 a, complete A–M runtime | PASS, 452.967 s |
| run-08 b, N cold reopen | PASS, 81.585 s |
| run-08 c, actual UI semantic/persistence sequence | FAIL, 712.553 s; checkpoint 16 action-history mismatch |
| run-08 d, additional UI identity after reopen | NOT RUN |
| run-08 e, second UI reopen/subsequent identity/activity recreation | NOT RUN |
| Complete mandatory physical qualification | FAIL / UNSEALED |

A LISTEN; B UNDERSTAND; C both PRACTICAL outcomes; D CORRECTION; E PAUSE/RELUCTANCE; F STAGNATION; G NEW EVIDENCE; H SAFETY UNKNOWN; I SAFETY CONFLICT; J TARGETED BIOGRAPHER; K ANSWER/COVERAGE; L JOURNAL RECALL; M BIOGRAPHER RECALL; N CLOSE/REOPEN: runtime gates all PASS. N reconstructs exact opening state and target temporal-gap.1990.2010.5df761a1aa5b231a, including private/declined controls and fresh ephemeral procedure/safety state. Opening digest: 2741f8bf345ef5e1610607ad4f40936d098837c09aef686cf0d1fa85ccd45373.

The final 20-turn stress gate admits each valid action exactly once, with one user and one committed source. It includes ordinary, short, long, boundary/refusal, pause/resume and reopened-state content. Stress identities 2–21 all survive the following process reopen with exact original text; the separate controlled test first commits identity 1. Both apostrophe forms pass. Exact “I don’t want to discuss this” at stress ordinal 9 commits identity 10 and survives reopen; ASCII form at ordinal 7 commits identity 8.

Full UI method c commits identities 69–84 before stopping. Pause 73, resume 74, refusal 75 and resumed engagement 76 pass. No missed accepted submission, duplicate dispatch, committed identity collision or Not saved was observed in completed checkpoints. Exact stored source checks and increasing frontier checks precede the failing semantic assertion.

Evidence limit: the device's final main-log buffer begins at UI input 8; earlier main-buffer entries rotated during the long run. Live read-only progress observations and sequential assertions established earlier checkpoints, but the final logcat file is not represented as a complete raw capture of those early UI turns. Runtime a/b instrumentation streams, later UI identities/failure and complete run-07 stress/durability captures are retained. The stress run independently preserves direct known-specimen admission and reopen proof.

The full UI second-reopen identity gate is NOT complete. Stress reopen proof and prior A–N passes do not waive d/e. Activity recreation at e also remains physically unexecuted in the final run.

## Renderer authority

Full runtime pause/resume/recurrence scenarios pass again. Pause chooses authorized NO_RESPONSE. Resumed engagement warrants an invitation, and a later resume receives an authorized alternative realization. Thomas chooses the semantic/procedural act; renderer faithfully realizes it.

Existing renderer semantic fidelity, deterministic authorized alternatives, strict exact recent duplicates, attribution/epistemic/temporal constraints and exhaustion regressions remain unchanged and pass. The renderer may reject a duplicate realization and search alternatives; it may not revoke Thomas's warranted act or substitute an unauthorized one. UI pause/resume also pass before the later Practical-transition failure. This is not a claim that the incomplete overall UI sequence passed.

## Protected artifact custody and cleanup

Every run 01–08 passed original before/after custody comparison and removed both disposable packages. Final run-08 reports CANONICAL_CUSTODY_UNCHANGED=true, DISPOSABLE_PACKAGES_REMAINING=0, PHYSICAL_ALL_METHODS_PASSED=False.

Protected original: com.conundrum.thomas.v2, UID 10666, version 1.0/code 1.
First install 2026-09-04 21:17:05; last update 2026-09-04 22:31:39, unchanged.
Original APK SHA-256 before = after: 3fa1f8590a16b8eadc85b0e007d7b7e6b67f425aff9f328516b73b5b2c5d2ae4.
Original code path before = after: /data/app/~~EVUd780xI9Crs1SpbCSJlw==/com.conundrum.thomas.v2-ZGzmD8XuteCvmrgm8bYk7Q==.
Signing metadata before = after: PackageSignatures{f1edb61 version:2, signatures:[6888906], past signatures:[]}.

| Protected private artifact | Bytes / mtime / inode, unchanged | SHA-256 before = after |
| --- | --- | --- |
| no_backup/thomas-personal-data/store.ctpd | 1246 / 1788586360 / 97279 | 0dfd2a464479fbdb9d230347b37ffc44a3ea090a017de02083b2c0c2a61dcd0b |
| shared_prefs/ct-v2-15-device-gate.xml | 65 / 1788586340 / 114513 | 3325d2a819fdd8062c2cdc48a09b995c9b012915bcdf88b1cf9742a7f057c793 |
| files/profileInstalled | 24 / 1788586350 / 108624 | 4f80c4b2cad6d3044bfcb1b2af6892596e9dc8072500276776df78dad4630334 |

Complete private-file inventory, hashes, sizes, mtimes, inodes, APK and required package metadata matched. Original app/corpus were not launched, replaced, reset, migrated, deleted, exported or decrypted. Atime is not claimed.

Binary manifest checks verify separate app/test/provider identities, exact instrumentation target and no sharedUserId. Final run-08 fixture UID 10291 differs from original 10666; Android Context/data and UID-scoped key namespace remain isolated. Observed hardwareBacked=true is not hardware attestation.

Final fixture APK: 31,564,640 bytes, SHA-256 17bde74cb3e4c24699eddcd56425c27ee20c4a2e7026067289f2de72f5d0e551.
Test APK: 2,462,970 bytes, SHA-256 048abed235c0cd891b7458d704934cdecb3a38d1f590980fe7ac13e8b53e7b68.

Both disposable packages are absent. The protected original and its preexisting test package are untouched. Temporary production instrumentation is removed. The reconstructed workspace, candidate patch/archive and raw diagnostic artifacts are deliberately retained as required failure evidence. Unused integration/sealing preparation helpers are removed. No informational dangling objects were deleted.

## Repository and evidence

Canonical branch main, entry = final HEAD/tree as above, clean, ahead 29 / behind 0. Commits created: 0. Completion tag absent; tag object/target: none. Push: none. Remote configuration unchanged. Root/worktree checks and git fsck --full --strict pass with the five preexisting informational dangling blobs retained.

All new work remains under ignored out/ct-v2-15r1-dispatch/:

- Reconstructed candidate: workspace/.
- Reviewable source/evidence snapshot: candidate/.
- Complete candidate patch/archive: candidate.patch and candidate.zip; identities in candidate-artifacts.json.
- Targeted defect evidence: candidate/docs/qualification/CT-V2-15R1-DISPATCH-EVIDENCE/ROOT-CAUSE-AND-REPAIR.md and run-03/run-04/run-05/.
- Automated qualification: candidate/docs/qualification/CT-V2-15R1-DISPATCH-EVIDENCE/automated-summary.json, canonical-build.txt, suite-inventory.json and lint-issues.json.
- Final physical: candidate/docs/qualification/CT-V2-15R1-DISPATCH-EVIDENCE/run-07/ and run-08/.
- Final report: candidate/docs/qualification/CT-V2-15R1-DISPATCH-QUALIFICATION.md.
- Final repository checks: final-repository-checks.txt.

Return the newly established UI Practical-transition blocker to the Principal. Do not integrate or seal. Do not begin rigorous deterministic end-to-end testing or widen therapeutic/UI repair scope.
