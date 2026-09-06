# CT-V2-15R1 isolated physical-device qualification

Date: 2026-09-05. This is the authorized physical continuation of [CT-V2-15R1 qualification](CT-V2-15R1-QUALIFICATION.md). It does not open the rigorous testing campaign or authorize production repairs.

## Current disposition

**CT_V2_15R1_PHYSICAL_QUALIFICATION_FAILED**

Failure classification: **PHYSICAL_FIXTURE_ORACLE_FAILURE**.

The prepared physical test ran on the authorized TCL 9491G and failed a wording-specific assertion at its first Therapy response. The app rendered an explicitly authorized reflection variant. This establishes a fixture oracle defect, not a demonstrated production-policy defect. It does **not** establish a physical pass: the multi-turn Therapy gates and dependent reopening gate remain unqualified.

No assertion was weakened, no gate was waived, no production code or test source was modified, and no completion tag was created. This execution stops at the requested bounded failure report.

**The pre-existing Thomas installation and corpus were preserved.** Before installation, after the failed test, and after fixture cleanup, the original private-file inventory and SHA-256 values matched. After cleanup, APK identity, package metadata, private-file sizes, modification times and inodes also matched the pre-install baseline. Both disposable packages were removed successfully.

The fresh canonical regression remains green: **978 JVM tests / 60 suites / 0 failures / 0 errors / 0 skips; lint 0 errors/fatals and 16 warnings**. A green JVM baseline does not override the failed physical gate.

## Repository and authorization

Entry HEAD: c61f6f92c93b790c3176c858434f44f6b6ac5981.

Entry tree: 4fd7272be37f6ba4ddc22bf1ae9bc315ae02ebda.

Branch: main; entry worktree clean. The physical APKs were the already-prepared artifacts from implementation revision 70200a6066d3749b5945c519bfa1f9bd8edfe8b3. Production source and test source at entry are unchanged from that revision. This continuation adds only qualification documentation and execution evidence.

The Principal explicitly approved installation and execution of the isolated fixture, with the existing application/corpus unchanged. No further approval was requested for the authorized device actions. Tool escalation was required because the filesystem execution helper failed in its default sandbox; this was not a renewed application-custody approval gate.

No original application launch, force-stop, install/replace, uninstall, data clear, reset, restore, migration, or backup import occurred. The original corpus and preferences were hashed on-device; no personal content or key bytes were displayed, copied or decrypted.

## Device and package identities

| Item | Observed |
| --- | --- |
| ADB serial / state | BC9424B4E3C5002 / device |
| Model / product / device | TCL 9491G / 9491G_ZZ / Hera_Vis_WIFI |
| Android / API / ABI | 15 / 35 / arm64-v8a |
| Build fingerprint | TCL/9491G_ZZ/Hera_Vis_WIFI:15/AP3A.240905.015.A2/2FA6:user/release-keys |
| Original package | com.conundrum.thomas.v2 |
| Original Android UID | 10666 |
| Original version | 1.0 / code 1 |
| Original first installation | 2026-09-04 21:17:05 |
| Original last update | 2026-09-04 22:31:39, unchanged afterward |
| Fixture package | com.conundrum.thomas.v2.ctv215r1fixture |
| Fixture test package | com.conundrum.thomas.v2.ctv215r1fixture.test |
| Fixture Android UID | 10018, mechanically different from 10666 |
| Fixture version | 1.0 / code 1 |
| Fixture install / update | 2026-09-05 18:29:03, device package metadata |
| Instrumentation interval | Device log 2026-09-05 18:29:05–18:29:14 |
| Instrumentation duration | 9.035 seconds |

Raw non-content identity evidence: [device](CT-V2-15R1-PHYSICAL-EVIDENCE/device.txt), [original before](CT-V2-15R1-PHYSICAL-EVIDENCE/canonical-package-before.txt), [original after](CT-V2-15R1-PHYSICAL-EVIDENCE/canonical-package-after.txt), [fixture package](CT-V2-15R1-PHYSICAL-EVIDENCE/fixture-package.txt), [UID isolation](CT-V2-15R1-PHYSICAL-EVIDENCE/uid-isolation.txt).

## Pre-install isolation gate

The gate passed before installing the fixture:

1. Neither fixture package was installed; no unknown fixture data was overwritten.
2. SHA-256 of each prepared fixture APK matched the committed artifact inventory.
3. aapt2 inspected both binary manifests. The fixture application ID and provider authority are distinct, and the test instrumentation target is **only** the fixture package. Neither manifest contains sharedUserId.
4. AndroidAtomicProtectedArtifactStorage resolves the protected artifact beneath context.noBackupFilesDir, checks its canonical path stays beneath that root, and uses no external/shared corpus location.
5. AndroidKeystorePersonalDataKeyProvider uses AndroidKeyStore with default alias ct-v2-14.personal-data.primary.v1. The alias text is the same in both APKs, but its effective key identity is scoped by Android UID. It is not a globally shared alias or exported key. The fixture has no shared UID, key grant, original-app Context, or original key-provider access path.
6. After installation and **before test launch**, run-as id -u confirmed original UID 10666 and fixture UID 10018. The fixture's dataDir is /data/user/0/com.conundrum.thomas.v2.ctv215r1fixture; the original is /data/user/0/com.conundrum.thomas.v2.
7. The test itself asserts the exact fixture target package before submitting any input and requires an initially empty fixture corpus.

Thus storage, preferences, key namespace and synthetic data are isolated by application identity/UID. No claim of distinct literal alias strings is made. No original key bytes were read, no hardware-backed-key claim was added, and the original application's decryptability was not retested by launching it. Preservation of its key namespace is supported by the verified UID boundary and the exact operations performed; corpus preservation is additionally verified by unchanged bytes/metadata.

Source evidence:

- tools/ct-v2-15r1-device-fixture.init.gradle — separate build-time applicationId only.
- platform/persistence-android/src/main/kotlin/com/conundrum/thomas/v2/platform/persistence/AndroidAtomicProtectedArtifactStorage.kt — package-context no-backup root and containment.
- platform/persistence-android/src/main/kotlin/com/conundrum/thomas/v2/platform/persistence/AndroidKeystorePersonalDataKeyProvider.kt — AndroidKeyStore provider and alias.
- app/src/main/java/com/conundrum/thomas/v2/ThomasAndroidCompositionRoot.kt — applicationContext passed to the protected store.
- app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1ProductionUiInstrumentedTest.kt, fixture — exact target-package guard.
- [binary fixture manifest](CT-V2-15R1-PHYSICAL-EVIDENCE/fixture-manifest.txt) and [binary test manifest](CT-V2-15R1-PHYSICAL-EVIDENCE/fixture-test-manifest.txt).

An initial local APK-path allowlist rejected Android's ordinary “~~” installation directory. No installation had occurred. Inspection confirmed that path, and the non-content hash command was rerun with tilde permitted in the path character set. No isolation requirement was relaxed.

## Installed APKs and exact command

| Artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| Disposable app-debug.apk | 31,548,256 | 3fa8c91211961457287c640fcc40b46605ea2aad784d1830940bf63beedbc729 |
| Disposable app-debug-androidTest.apk | 2,382,590 | a6608e4b99ac5dc2e37bcc03d2cd6c0b928928fbbef7a36d2547fc2046f7dd2e |

The preserved artifacts remain under ignored out/ct-v2-15r1-fixture. They were installed without a replacement flag:

~~~text
adb -s BC9424B4E3C5002 install out/ct-v2-15r1-fixture/app-debug.apk
adb -s BC9424B4E3C5002 install out/ct-v2-15r1-fixture/app-debug-androidTest.apk
~~~

Both returned Success. Only after the UID check did the prepared method run:

~~~text
adb -s BC9424B4E3C5002 shell am instrument -w -r -e class com.conundrum.thomas.v2.CTV215R1ProductionUiInstrumentedTest#aTypedProductionConversationsAndCrossModeRecall com.conundrum.thomas.v2.ctv215r1fixture.test/androidx.test.runner.AndroidJUnitRunner
~~~

[Complete instrumentation output](CT-V2-15R1-PHYSICAL-EVIDENCE/instrumentation-a.txt) records one test, one failure, zero ignored. The host adb process returning normally is not interpreted as test success: the assertion stack and runner summary are authoritative.

Evidence files are preserved as UTF-8 with line endings and trailing whitespace normalized; evidence-manifest.json records both the original local-file hashes and tracked-file hashes. Original captures remain under ignored out/ct-v2-15r1-physical. Fixture-only logcat was collected with --uid=10018; it did not collect original-application logs. [Fixture logcat](CT-V2-15R1-PHYSICAL-EVIDENCE/fixture-logcat.txt) retains the synthetic inputs, status messages and failure.

## Observed execution and exact failure

The method successfully reached these checkpoints before failing:

| Step | Physical observation / limit |
| --- | --- |
| Fixture guard and empty corpus | Passed; exact target package and empty source summaries asserted |
| Journal: I moved to Denver in 2010. | Actual Compose typed input/send; status No response |
| Journal: I moved to Portland in 2018. | Actual Compose typed input/send; status No response |
| Switch to Biographer | Visible question assertions for both 2010 and 2018 passed |
| Biographer: I moved to Seattle in 2014. | Status Completed; next visible response assertion containing 2014 passed |
| Switch to Therapy and submit explicit scope declarations plus “My specific concern is: the delayed meeting” | A reflection was visibly produced |
| First Therapy response assertion | Failed: expected substring “hear”; actual “What stands out is that the delayed meeting.” |

These are preliminary checkpoints within a **failed test**, not separately passed physical qualification methods. The Biographer UI checks show a grounded question and answer-dependent response, but do not substitute for the full typed-state/target assertions in the JVM suite.

Exact failure:

~~~text
java.lang.AssertionError:
Expected 'hear' in 'What stands out is that the delayed meeting.'

CTV215R1ProductionUiInstrumentedTest.contains:41
CTV215R1ProductionUiInstrumentedTest.aTypedProductionConversationsAndCrossModeRecall:55

Time: 9.035
FAILURES!!!
Tests run: 1, Failures: 1
~~~

Why the failure is classified as an oracle defect:

- TherapyRenderCommandAdapter.kt:159 constructs core-reflect-established-content from the actual concern.
- Its therapyVariants function at lines 198–204 explicitly authorizes the base “I hear that,” “What stands out is that,” and “You have described that” reference forms.
- DeterministicReferenceRealizer.realize orders those forms by command-ID seed and avoids recent opening fingerprints.
- The prepared UI test incorrectly requires only the base form's substring. Android turn/command identities need not select the same reference form as a particular JVM conversation.

This conclusion comes from actual output and unchanged production code, not an expanded acceptable-result enum. The test was not edited or rerun with a looser assertion. There is no evidence here that Therapy selected a wrong technique, but full procedural behavior remains untested on this device run.

## Required physical coverage adjudication

| Required gate | This run |
| --- | --- |
| LISTEN progression | Incomplete: first reflection observed; assertion then failed |
| UNDERSTAND progression | Not reached |
| PRACTICAL progression | Not reached |
| Correction | Not reached |
| Pause/reluctance | Not implemented in the prepared physical methods; JVM-only evidence remains |
| Stagnation versus genuinely new evidence | Not implemented in the prepared physical methods; JVM-only evidence remains |
| Safety UNKNOWN | Not reached |
| Safety conflict/current observation | Not reached |
| Targeted Biographer selection | Preliminary visible question checkpoints passed before test failure |
| Biographer answer-dependent progression | Preliminary visible changed-response checkpoint passed before test failure |
| Journal → Therapy recall | Not reached |
| Biographer → Therapy recall | Not reached |
| Durable state across close/reopen | Not executed: method b requires completed method a and more than 20 captured sources; that setup was not reached |

The dependent bColdReopenPreservesCorpusButRequiresCurrentSafety method was not invoked with invalid prerequisites. No reset, corpus fabrication, direct internal-state injection, or manual marking of a gate as complete was used.

Review also confirmed that the two prepared methods do not contain the requested pause/reluctance and stagnation/new-evidence sequences. Even if the wording assertion were corrected, those physical scenarios would still need to be added and exercised before full R1 completion.

## Cleanup and original-corpus preservation

After preserving diagnostics, the original private-file hashes were checked and still matched. The fixture UID was rechecked as 10018, then only the exact disposable packages were removed:

~~~text
adb -s BC9424B4E3C5002 uninstall com.conundrum.thomas.v2.ctv215r1fixture.test
adb -s BC9424B4E3C5002 uninstall com.conundrum.thomas.v2.ctv215r1fixture
~~~

Both returned Success. Package-manager query confirmed zero remaining fixture packages. This cleanup deletes only this execution's synthetic fixture installation/data under its separate UID. No destructive persistence scenario was run; there was no reason to reset or restore even the fixture corpus.

The original package remains present with identical codePath, dataDir, version, firstInstallTime, lastUpdateTime and signing metadata. Its installed APK SHA-256 is unchanged:

3fa1f8590a16b8eadc85b0e007d7b7e6b67f425aff9f328516b73b5b2c5d2ae4

Complete original private-file inventory, before and after:

| File | Bytes | SHA-256 |
| --- | ---: | --- |
| no_backup/thomas-personal-data/store.ctpd | 1,246 | 0dfd2a464479fbdb9d230347b37ffc44a3ea090a017de02083b2c0c2a61dcd0b |
| shared_prefs/ct-v2-15-device-gate.xml | 65 | 3325d2a819fdd8062c2cdc48a09b995c9b012915bcdf88b1cf9742a7f057c793 |
| files/profileInstalled | 24 | 4f80c4b2cad6d3044bfcb1b2af6892596e9dc8072500276776df78dad4630334 |

Sizes, mtimes and inodes also match exactly. The comparison covers the full file inventory returned by run-as find, not only the named corpus file. No new or missing private files were observed. Reading hashes may affect filesystem access accounting; the claim concerns installation/corpus contents and recorded structural metadata, not unmeasured atime.

Evidence: [custody result](CT-V2-15R1-PHYSICAL-EVIDENCE/custody-result.txt), [before hashes](CT-V2-15R1-PHYSICAL-EVIDENCE/canonical-files-before.sha256), [after hashes](CT-V2-15R1-PHYSICAL-EVIDENCE/canonical-files-after.sha256), [before stat](CT-V2-15R1-PHYSICAL-EVIDENCE/canonical-files-before.stat), [after stat](CT-V2-15R1-PHYSICAL-EVIDENCE/canonical-files-after.stat), and corresponding APK/package records in the evidence directory.

Fixture key bytes were never exported. Package removal and absence are observed; a separate forensic audit of Android's internal key-erasure mechanism is not claimed.

## Fresh canonical regression and sealing checks

Run at entry HEAD c61f6f9 after fixture cleanup, with no production or test modifications:

~~~powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
~~~

**BUILD SUCCESSFUL in 1m 57s; 393 actionable tasks, 393 executed.**

| Metric | Result |
| --- | --- |
| JVM suites / tests | 60 / 978 |
| Failures / errors / skips | 0 / 0 / 0 |
| New tests in this physical execution | 0 |
| Lint errors / fatals / warnings | 0 / 0 / 16 |
| Physical methods executed / failed | 1 / 1 |
| Dependent physical method | Not run |
| V1 register | Default DENIED; migration false; 24/24 components DENIED |
| Canonical root verifier | Pass; canonical .git, no temporary metadata directories |
| git diff --check | Pass after documentation staging |
| git fsck --full --strict | Exit 0; existing harmless dangling blob 4350721d914b66808ab247f55e195165fb762cfc retained, no repair |
| Completion tag / push | Neither performed |

[Full fresh build output](CT-V2-15R1-PHYSICAL-EVIDENCE/canonical-regression.txt), [suite inventory](CT-V2-15R1-PHYSICAL-EVIDENCE/regression-suite-inventory.json), [summary](CT-V2-15R1-PHYSICAL-EVIDENCE/regression-summary.json), and [V1 verification](CT-V2-15R1-PHYSICAL-EVIDENCE/v1-governance.json) are preserved.

| Fresh canonical artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| Debug APK | 31,548,244 | 8888a1f0bc43e86873650d824fc56615a6420aacf013a41074724f07da998b45 |
| Unsigned release APK | 24,279,311 | 19e69ec44685d793041068257c8bf23d960480c34ab457ae1660015674fb5231 |

Both fresh APKs have zero model artifact matches and zero inference-backend marker matches using the same scans recorded in the implementation qualification. The release metadata identifies c61f6f92c93b790c3176c858434f44f6b6ac5981. Its changed hash relative to the earlier build reflects different embedded repository metadata; byte reproducibility is not claimed. These freshly built canonical APKs were **not installed on the tablet**.

The final evidence-only commit is a descendant of that tested HEAD. Source, tests, Android configuration, model/speech boundaries, and policy remain unchanged. The final commit identity and clean worktree verification are recorded in the completion response.

## Minimum next order

The bounded next task is **CT-V2-15R1 physical-fixture oracle correction, missing-scenario coverage, and isolated rerun**:

1. Replace the base-wording assumption with an oracle grounded in the existing authorized action, required concern meaning and legitimate reference variants. Do not alter production wording/policy merely to satisfy the old assertion or accept arbitrary non-null responses.
2. Add the missing physical pause/reluctance and stagnation/new-evidence checks through the real input controls, without manually constructing procedure state.
3. Review the remaining physical oracles for the same variant-sensitive assumption.
4. Rebuild/hash the disposable fixture, repeat the non-content custody/isolation checks, execute the entire method set and independent process-reopen boundary, and preserve all failures.
5. Seal R1 only when every required physical gate passes and the original installation/corpus remains unchanged.

No model, speech, Pattern Engine, new therapy/safety authority, release work or full rigorous campaign is part of that bounded correction.

CTV2_RIGOROUS_TESTING_READY_WITH_BOUNDED_PREREQUISITES

Recommended next order: CT-V2-15R1 physical-fixture oracle correction, missing-scenario coverage, and isolated rerun.

Principal decision required: Adjudicate this failed physical gate and authorize the bounded fixture correction/rerun before R1 sealing.
