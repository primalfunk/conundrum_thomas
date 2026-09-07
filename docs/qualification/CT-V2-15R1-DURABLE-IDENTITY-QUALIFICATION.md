# CT-V2-15R1 durable identity repair: unsealed qualification report

Date: 2026-09-06. Principal-authorized bounded identity repair and full isolated physical rerun. The rigorous deterministic end-to-end campaign was not started.

## Disposition

**CT_V2_15R1_PHYSICAL_QUALIFICATION_FAILED_UI_SUBMISSION_TIMEOUT**

**CT_V2_15R1_NOT_SEALED**

The repair candidate passes its targeted identity contract and full software regression. The complete A–N runtime/reopen scenarios pass physically. The actual UI method fails on its seventh submission; the explicit second-reopen identity methods consequently remain unrun. No gate is waived and no completion disposition is claimed.

The Principal's commit authorization is conditional on every gate passing. No commit or tag was created. To also satisfy the clean-worktree requirement without losing the repair, all candidate source, tests and evidence are preserved in ignored local out/ct-v2-15r1-identity/candidate, candidate.patch and candidate.zip. Only this order's working changes are restored to the verified entry afterward. The patch includes the bounded repair, new tests, physical fixture extensions and this evidence; it is reviewable/recoverable, but is **not applied to final canonical main**. See final-repository-checks.txt beside the archive for the post-restoration verification.

## Repository

- Starting and final canonical HEAD: 4f822fb3498b67b40124c0fb4ec1f19f98c92c8b.
- Starting and final canonical tree: 69fa2a2b68105c4371574ac81316aa6002fd4d93.
- Branch: main; locally recorded origin/main relationship: ahead 29 / behind 0.
- Commits created: none. Push: none. Remote configuration unchanged.
- Completion tag ct-v2-15r1-production-evidence-procedure-biographer-integration: absent; object and target: not applicable.
- The candidate was tested as an uncommitted source delta from entry. Its canonical release APK embeds the entry revision; this is not a claim that entry alone contains the repair.
- Canonical root and git fsck --full --strict pass. Five informational dangling blobs are retained, not deleted.
- The original recurrence repair, prior evidence and all prior tests remain unchanged in canonical main.

## Failure-first oracle and source trace

[Pre-edit trace](CT-V2-15R1-DURABLE-IDENTITY-TRACE.md) documents definitions, uniqueness domain, allocation and callers, durable keys, reopen reconstruction, revision divergence and compatibility before production changes.

[Pre-repair XML](CT-V2-15R1-IDENTITY-EVIDENCE/pre-repair.xml) and [original regression source](CT-V2-15R1-IDENTITY-EVIDENCE/pre-repair-test.kt) preserve an actual deterministic failure against the original allocation formula. The test uses the exact starting ViewModel initialization/increment as a fixture adapter; after repair its allocation calls delegate to the new production runtime allocator. All identity/admission assertions remain.

1. An unrecorded Biographer prompt uses index 1 without changing store revision.
2. Two actual protected-store Therapy captures commit android-therapy-2 and android-therapy-3; revision is 2 and the live next unused index is 4.
3. The harness closes the runtime/protected file store and reconstructs it through ProtectedPersonalDataStoreFactory.open.
4. Original allocation restores revision + 1 = 3 and submits a different payload as android-therapy-3.
5. The store returns IDEMPOTENCY_KEY_PAYLOAD_CONFLICT, committedSourceId=null, revision remains 2. The assertion requiring a new admitted identity fails.

With the repair, the same scenario commits android-therapy-4 at revision 3; another reopen preserves all three sources and allocates 5. These are identity assertions, not merely exception-free execution.

Revision counts accepted mutations, not allocated turn indexes. Prompts consume indexes without persistence; evidence/lifecycle operations can add revisions without a new user turn. Neither source count nor revision proves the committed identity frontier.

## Bounded repair

Exactly two production files in the archived candidate differ from entry:

- thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ThomasProductionRuntime.kt: adds synchronized allocateTurnIndex. It reads canonical numeric suffixes from accepted redacted admission keys across active and archived history, takes their maximum together with its in-memory allocated frontier, and uses checked increment. Explicit client-supplied identities and collision checking remain unchanged.
- app/src/main/java/com/conundrum/thomas/v2/ThomasViewModel.kt: removes revision-derived counter initialization/reset and delegates every allocation caller to the current runtime. This also follows composition-root replacement/restore automatically.

The matching durable key families are journal.commit.commit-android-journal-N, biographer.answer.answer-android-biographer-N, therapy.capture.capture-android-therapy-N, android-source-revision-N, and android-lifecycle-{make-private|request-eligible-review|delete}-N.

For every successful allocation, the returned positive index is strictly greater than every previously committed canonical index in the current corpus and every live reservation. Deletion preserves redacted identity headers; gaps and out-of-order preexisting commits cannot reduce the maximum. Checked increment prevents Long wraparound. Reopen and supported backup reconstruction retain the same ledger information. No schema migration, renumbering, history rewriting or persistence code change is required. The existing corpus reset/replacement and single-runtime store-writer contracts are retained.

Therapeutic policy, procedural authority, ontology, Pattern Engine, longitudinal reasoning, models/prompts/authority, speech/STT/TTS, response policy, duplicate strictness, privacy/governance, unrelated UI, corpus contents and renderer production code were not modified.

## Tests and evidence scope

New JVM suite: qualification/src/test/kotlin/com/conundrum/thomas/v2/qualification/CTV215R1DurableTurnIdentityTest.kt, ten tests:
fresh index; consecutive three-mode/prompt allocation; multiple-turn close/reopen oracle; sparse/out-of-order existing history; revision ahead of identity; deleted highest identity; every lifecycle/revision key; strict conflicting payload; Long exhaustion; direct commits observed by a live allocator.

Existing CTV215R1ReopenIdentityDiagnosticTest is unchanged and still proves strict collision rejection. Every original recurrence test and assertion is retained.

Qualification-only Android changes:
- CTV215R1ProductionDeviceInstrumentedTest.kt uses the actual repaired allocator throughout and rejects observed idempotency conflicts.
- CTV215R1ProductionUiInstrumentedTest.kt retains the full UI sequence and 60,000 ms wait; adds committed-frontier/content diagnostics, absence-of-Not-saved assertions, two process-reopen methods and activity-recreation progression.

The disposable build initially rejected an unnecessary imported Compose assertion name; removing that import fixed compilation. The assertion is an existing member and remains present. Both failed and corrected build logs are preserved. No timeout was inflated.

Evidence files: this report; the pre-edit trace; all files under CT-V2-15R1-IDENTITY-EVIDENCE (manifest, original/post-repair XML, suite inventory, lint issues, artifact/source hashes, canonical/fixture build logs, physical runner, run-01 source snapshots/manifests/logs/custody comparisons). The archive additionally preserves the complete source delta.

## Automated qualification

Final command: gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain, with Android Studio JBR.

**BUILD SUCCESSFUL in 1m 45s; 393 actionable tasks, all 393 executed.**

- 64 suites, 1,000 tests, 0 failures, 0 errors, 0 skips.
- All 63 baseline suites retain their original test counts: 990 predecessor tests plus ten new identity tests.
- Lint errors/fatals: 0/0.
- App warnings: 16, the accepted existing classes.
- Aggregate warnings across app and library reports: 17. The additional counted library issue is unchanged ChromeOsAbiSupport in platform/renderer-llama-android. This report explicitly adjudicates it as preexisting/nonfatal and out of this allocator repair's scope; it is not hidden or fixed through unrelated ABI work.
- No deleted tests, inserted skips, weakened assertions or timeout inflation.
- V1 register: default DENIED, migration false, 24/24 components DENIED.
- Both final canonical APK scans contain zero listed model artifacts/inference library filename markers.
- All renderer/policy/store production diffs outside the two named files are empty. The runtime delta contains only identity allocation.

Final canonical debug APK: 31,564,628 bytes, SHA-256 3819e58ff9ad224225f8e3be2256c76efb119f1f69048acf0febe8c01fb42992.
Final unsigned release APK: 24,295,695 bytes, SHA-256 ae54829ca31fd569c3bcdbfa0de87a701dd759862f585599ab4a0e80e35df6e4.
Neither canonical APK was installed. Final artifacts were recaptured after rebuilding canonical outputs following the disposable build, preventing fixture/canonical artifact confusion. No byte-reproducibility claim is made.

## Complete isolated physical attempt

Device: TCL 9491G, serial BC9424B4E3C5002; Android 15 / API 35 / arm64-v8a.
Fingerprint: TCL/9491G_ZZ/Hera_Vis_WIFI:15/AP3A.240905.015.A2/2FA6:user/release-keys.

One complete attempt began from empty disposable data. Runner proceeded a → force-stop → b → force-stop → c, and stopped on c's failure. It requires the instrumentation OK (1 test) result, not host adb exit status.

| Gate | Result |
| --- | --- |
| aCompleteTypedProductionScenarios | PASS, 452.548 seconds; A_THROUGH_M_COMPLETE=true |
| bColdReopenDurabilityAndCoverage | PASS, 82.104 seconds; N_REOPEN_COMPLETE=true |
| cActualInputControlsAndSemanticProgression | FAIL, 300.504 seconds total; seventh input times out at unchanged 60-second completion wait |
| dActualUiIdentityAfterProcessReopen | NOT RUN, dependent on c |
| eSecondReopenDurabilityAndSubsequentIdentity | NOT RUN, dependent on d |
| Explicit full UI identity/reopen durability gate | INCOMPLETE, cannot pass or seal |

A LISTEN, B UNDERSTAND, C both PRACTICAL outcomes, D CORRECTION, E PAUSE/RELUCTANCE, F STAGNATION, G NEW EVIDENCE, H SAFETY UNKNOWN, I SAFETY CONFLICT, J TARGETED BIOGRAPHER, K ANSWER/COVERAGE, L JOURNAL RECALL, M BIOGRAPHER RECALL and N CLOSE/REOPEN all pass in the runtime methods. This is not mislabeled as complete UI coverage.

At the end of a: 56 sources, revision 65, logical digest 9ccbe6af98603df30a93f64ee8ecd02b36ef45bb5c2aa58c03ac3522526818f9. Method b preserves that exact digest/revision/count before continuing. Reconstructed target: temporal-gap.1990.2010.5df761a1aa5b231a. Private/declined controls and fresh ephemeral procedure/safety assertions pass.

Physical recurrence:
- Turn 18, Please pause: core-pause-without-response, NO_RESPONSE, VALID.
- Turn 19, I am ready to resume: LISTEN_SUPPORT, core-invite-further-expression, NEW_ACTION, VALID; “Is there anything else you would like to say about it?”
- Turn 21, I want to continue: same warranted invitation act, VALID; “What more would you like to share about it?”

The renderer realizes Thomas's warranted act through authorized deterministic alternatives; it does not cancel the act because preferred wording repeats. Strict recent-duplicate validation and semantic fidelity remain unchanged and independently tested. Exact duplicate exhaustion remains an explicit rendering failure, not policy-authorized silence.

## Actual UI failure and identity observations

The first six actual UI submissions after cold reopen commit identities 69, 70, 71, 72, 73 and 74. For each, fixture diagnostics assert:
- new index exceeds committed frontier;
- user transcript committed=true;
- exact submitted content is in the production store under its therapy.turn-id;
- Not saved is absent from actual Compose UI;
- durable frontier advances to the allocated value.

Pause at 73 and resume at 74 both pass their UI semantic assertions. The resumed UI response is “What else would you like to say about it?” and is linked to the accepted render-history fingerprint.

At 12:57:21.342 device log time, the seventh input is “I don't want to discuss this”, processing=false, draft populated, prior status=Completed. After performClick and the full 60-second wait, at 12:58:21.543:
- processing=false;
- the same draft remains;
- status remains Completed;
- source count remains 65;
- dispatcher workers are parked;
- main thread is polling the message queue.

This establishes an **uncompleted actual UI submission**, not a demonstrated new store collision. It is consistent with the click not reaching submission, but the capture does not prove the exact input-dispatch cause or conclusively classify fixture versus production UI behavior. No timeout inflation, repeated click, semantic bypass, UI code repair or unrelated optimization was attempted to force passage.

There is no observed repaired-candidate identity collision or Not saved defect in the completed checkpoints. The second reopen durability check and subsequent identity gate are unrun, so their absence of collision cannot be claimed as qualified. The precise sealing blocker is the failed actual UI submission gate and its dependent unexecuted identity durability gates.

## Artifact custody and cleanup

Protected original: com.conundrum.thomas.v2, UID 10666, version 1.0/code 1; first installed 2026-09-04 21:17:05, last updated 2026-09-04 22:31:39.

Original APK SHA-256 before/after: 3fa1f8590a16b8eadc85b0e007d7b7e6b67f425aff9f328516b73b5b2c5d2ae4.
Path before/after: /data/app/~~EVUd780xI9Crs1SpbCSJlw==/com.conundrum.thomas.v2-ZGzmD8XuteCvmrgm8bYk7Q==/base.apk.
Signing metadata before/after: PackageSignatures{f1edb61 version:2, signatures:[6888906], past signatures:[]}.

| Protected private artifact | Bytes / mtime / inode, unchanged | SHA-256 before and after |
| --- | --- | --- |
| no_backup/thomas-personal-data/store.ctpd | 1246 / 1788586360 / 97279 | 0dfd2a464479fbdb9d230347b37ffc44a3ea090a017de02083b2c0c2a61dcd0b |
| shared_prefs/ct-v2-15-device-gate.xml | 65 / 1788586340 / 114513 | 3325d2a819fdd8062c2cdc48a09b995c9b012915bcdf88b1cf9742a7f057c793 |
| files/profileInstalled | 24 / 1788586350 / 108624 | 4f80c4b2cad6d3044bfcb1b2af6892596e9dc8072500276776df78dad4630334 |

Full private-file inventory, file hashes/sizes/mtimes/inodes and package/APK metadata match before/after and the recorded reference. Original contents and keys were not exported or decrypted; original app was not launched, replaced, reset, migrated or uninstalled. Atime is not claimed.

Disposable application com.conundrum.thomas.v2.ctv215r1fixture ran under independent UID 10272, with its own application Context/data directory and Android UID-scoped key namespace. Binary manifests verified distinct app/test/provider identities, exact instrumentation target and no sharedUserId before installation. The observed candidate AndroidKeyStore report says AES, hardwareBacked=true, userAuthenticationRequired=false; this is not hardware attestation.

Fixture APK: 31,564,640 bytes; SHA-256 17bde74cb3e4c24699eddcd56425c27ee20c4a2e7026067289f2de72f5d0e551.
Instrumentation APK: 2,439,022 bytes; SHA-256 dbf6520d8934d40351b4ef49481dec0617d326216e2b7c1d5c0ba223f84d262c.

Both disposable packages were removed successfully. Remaining queried packages are only the protected original and its preexisting test package. Raw candidate APKs, authorized evidence, source archive and recovery patch are intentionally retained locally; disposable helper scripts are removed after archival verification. No unknown or prior evidence artifacts are deleted.

## Evidence entry points

Relative to this report:
- Targeted defect: CT-V2-15R1-IDENTITY-EVIDENCE/pre-repair.xml and CTV215R1DurableTurnIdentityTest.xml.
- Software: CT-V2-15R1-IDENTITY-EVIDENCE/automated-summary.json, suite-inventory.json, lint-issues.json, canonical-build.txt and canonical-artifacts.json.
- Physical: CT-V2-15R1-IDENTITY-EVIDENCE/run-01/instrumentation-a.txt, instrumentation-b.txt, instrumentation-c.txt and fixture-logcat.txt.
- Custody: CT-V2-15R1-IDENTITY-EVIDENCE/run-01/final-disposition.txt and matching canonical-*-before/after files.
- Capture provenance: CT-V2-15R1-IDENTITY-EVIDENCE/evidence-manifest.json.
- Final repository/patch validation: out/ct-v2-15r1-identity/final-repository-checks.txt in canonical root.

Return to the Principal with CT-V2-15R1 open. The next required decision concerns the unresolved actual UI submission failure and a fresh complete rerun; the rigorous deterministic end-to-end campaign remains unauthorized.
