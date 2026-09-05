# CT-V2-15 Qualification

## Disposition

`CT_V2_15_ANDROID_PRODUCTION_INTEGRATION_COMPLETE`

One canonical production-shaped Android V2 runtime is implemented and the
mandatory JVM, build, artifact, and TCL 9491G physical-device gates passed.
Qualification data was fixed synthetic data. No generative model or speech
implementation was admitted.

## Repository

- Starting HEAD/tree: `d8f4b5cb03a9a3a282ea8e8be438376a74a117b9` /
  `a3bd9fb0ca4ef25a1ed3716ef97cc1773fbe34d4`.
- Branch: `main`.
- Starting worktree: clean.
- Remote: `origin` -> `https://github.com/primalfunk/conundrum_thomas.git`.
- Starting relation: seven commits ahead of `origin/main`.
- CT-V2-15 pushes: 0.
- Predecessor annotated tag:
  `ct-v2-14-personal-data-governance-persistence`, object
  `a5a70d722296d6340202b8e18006a9ae43aa4184`, targeting the accepted
  starting commit.
- Implementation commits: `4d16acc` governed runtime, `9553570` Android app,
  `065d876` qualification corpus, followed by the final audit/documentation
  commit recorded in Git history.

The canonical root remained
`C:/Android Studio Projects/ConundrumThomasV2`, Git metadata remained `.git`,
and no alternate or temporary Git metadata was used.

## Production architecture

```text
:app (one ThomasAndroidCompositionRoot)
  -> :platform:persistence-android
  -> :thomas:runtime
       -> Journal / Biographer / language-evidence
       -> CT-V2-04 safety / CT-V2-05 Therapy
       -> CT-V2-11 retrieval / context packet
       -> CT-V2-12 longitudinal Therapy
       -> CT-V2-13 governed renderer
```

The root opens one CT-V2-14 Android protected store and gives its governed ports
to one platform-neutral `ThomasProductionRuntime`. The runtime serializes turn
execution and owns only operational session state. Journal posture, Biographer
target, safety, Therapy route/technique, retrieval, memory surfacing, and
rendering remain in their qualified owners.

The production turn contract is typed: `ProductionTurnRequest` fixes mode,
origin, privacy, posture/support, explicit memory intent, current safety scope,
commit time, and client turn identity. `ProductionTurnResult` carries capture,
store revision, Therapy plan, validated render result, final artifact, and typed
disposition. Raw renderer candidates never become artifacts.

## Android application

- Explicit top-level Journal, Biographer, and Therapy modes.
- Process-local draft/transcript with explicit typed commit.
- Journal `NO_RESPONSE`, `REFLECT`, and `ASK_ONE_QUESTION` controls.
- Existing Therapy support selection, explicit-recall control, and required
  current ordinary-scope confirmation; no UI-defined technique.
- Explicit private-turn selection.
- Source custody list with append-only correction/revision, privacy/review, and
  deletion actions.
- User-selected machine JSON and human Markdown export.
- User-selected protected backup and separately saved recovery key.
- Integrity-checked replacement restore and unmistakable full reset.
- Busy guard and monotonically allocated client-turn identity.

Mode switching clears the uncommitted draft rather than admitting it. The UI
does not persist route, safety, target, retrieval, packet, or render-attempt
state as longitudinal evidence.

## Persistence and physical device

The application composes AES-256-GCM authenticated storage through
`AtomicFile` at `noBackupFilesDir/thomas-personal-data/store.ctpd`. The primary
AES key is generated and used through AndroidKeyStore with randomized
encryption and unlocked-device requirement. The provider generates each GCM
nonce; no caller-supplied IV is used with the KeyStore encryption key.

On physical TCL 9491G (Android 15, API 35, `arm64-v8a`), qualification proved:

- synthetic source creation and protected persistence;
- plaintext fixture absent from the protected artifact;
- equivalent canonical revision/digest after runtime close/reopen;
- equivalent revision/digest after actual device reboot;
- Android `FLAG_ALLOW_BACKUP` absent and no-backup placement;
- protected backup, authenticated validation, reset, empty-target restore, and
  equivalent restored digest;
- application reset and corpus absence;
- destroyed/missing primary key fails closed rather than opening empty history;
- provider `AndroidKeyStore`, algorithm `AES`, and device-reported
  `hardwareBacked=true` for this key on this device.

Hardware-backed status is an OEM/device observation, not a universal claim.
The final physical performance fixture reset its synthetic corpus.

See [target-device evidence](CT-V2-15-TARGET-DEVICE-EVIDENCE.md) for the exact
sequence and timing table.

## Mode and longitudinal results

- Journal commits a source exactly once. `NO_RESPONSE` produced no assistant
  artifact and no renderer invocation end to end.
- Biographer target selection remained upstream; the rendered prompt accepted
  one synthetic answer through governed admission with Biographer provenance.
- Therapy captured `THERAPIST_CONVERSATION`, evaluated current safety, fixed the
  CT-V2-05 route, then retrieved as of the pre-turn revision and applied the
  CT-V2-12 memory gate.
- Empty history and 120 unrelated sources produced the same Therapy route and
  action, with at most one memory reference.
- Journal-origin memory used for explicit Therapy recall retained Journal
  provenance; private Journal evidence never entered render support.
- Current Therapy input could not retrieve itself as historical memory.
- A correction appended source revision 2, removed the old active claim, and
  made later recall use only the corrected revision.
- Privacy and whole-source deletion propagated into later retrieval and
  renderer visibility.
- Historical source text ordering diagnosis, quitting, or policy changes
  remained inert data and did not affect route, renderer, or writes.
- Current safety preemption prevented ordinary route/retrieval continuation;
  historical risk-like text did not create current safety authority.

## Speech

STT and TTS are disabled. The app requests no microphone permission, exposes
typed input as authoritative, and reports speech unavailable. No recognizer was
claimed offline/private, and no ambiguous finish-versus-cancel control was
introduced. TTS finality and interruption are not claimed because no TTS
implementation exists.

## Authority audit

| Authority/path | Result |
| --- | ---: |
| Canonical production app composition roots | 1 |
| Legacy/V1 production conversation roots | 0 |
| Production protected-store roots | 1 |
| Governed admission interface | 1 |
| App direct CT-V2-07 admission calls | 0 |
| App direct SQLite/JDBC/Room paths | 0 |
| App direct Biographer capture/store writer paths | 0 |
| Renderer -> persistence paths | 0 |
| LanguageRealizer -> persistence paths | 0 |
| Model -> persistence/retrieval paths | 0 |
| UI -> Therapy-route paths | 0 |
| Renderer -> Therapy-route/safety paths | 0 |
| UI/renderer -> Biographer target paths | 0 |
| Thomas response -> user-evidence paths | 0 |
| Production model-backed realizers | 0 |
| Remote inference paths | 0 |
| Generic Android backup eligibility | 0 |
| Uncontrolled plaintext personal stores | 0 |

Static qualification locates exactly one
`ThomasAndroidCompositionRoot.kt`, exactly one app call to
`AndroidPersonalDataPersistenceFactory.open`, no direct app admission request,
no renderer persistence imports, and no runtime model/GGUF/network path.

## Qualification

Canonical command:

```powershell
.\gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
```

- Result: `BUILD SUCCESSFUL` in 2m15s.
- Actionable tasks: 393/393 executed.
- JVM suites/tests: 59 / 946.
- JVM failures/errors/skips: 0 / 0 / 0.
- New CT-V2-15 JVM suites/tests: 2 / 17.
- Physical-device instrumentation tests: 6 (five persistence/runtime and one
  Compose UI); final accepted outcomes: 6 passed, 0 failed, 0 skipped.
- Combined automated test executions: 952.
- Lint errors/fatals/warnings: 0 / 0 / 17. Remaining warnings are recorded baseline/version,
  resource, manifest-label, SDK-level, and ChromeOS ABI advisories; no warning
  is treated as a passed error gate.
- Debug APK: 31,466,324 bytes; SHA-256
  `3fa1f8590a16b8eadc85b0e007d7b7e6b67f425aff9f328516b73b5b2c5d2ae4`.
- Unsigned release APK: 24,246,543 bytes; SHA-256
  `1d6e158b3ea058ad3d5baabc0104bb33be25651dfce0a08a5fd1b9898c4a93fd`.
- A second `--rerun-tasks` assembly produced identical byte hashes for both
  APKs; the final clean assembly reverified these values.
- Generated provenance database remained ignored/untracked.

Development qualification discovered and corrected two real integration
defects before the final seal: AndroidKeyStore randomized-encryption rejected a
caller-supplied GCM IV, and the pre-CT-V2-15 Biographer audit read generated
build metadata rather than direct app dependencies. The provider now supplies
its own nonce, and the regression asserts zero direct app Biographer/store
writer bypasses.

## Performance observation

Three physical-device samples recorded median/worst milliseconds:

- cold start 1480 / 1515;
- store open 16.787 / 17.103;
- Journal silent commit 127.513 / 210.983;
- deterministic Journal rendering 476.071 / 602.100;
- Biographer turn 62.498 / 63.373;
- Therapy without memory 1291.229 / 1520.619;
- Therapy with explicit memory 2195.030 / 2325.097;
- export 30.397 / 32.516;
- protected backup 4.923 / 6.168;
- validated restore 138.184 / 148.177.

These desktop/device observations are not production latency SLAs and contain
no model latency.

## Security, artifact, and repository integrity

- Android manifest keeps `android:allowBackup="false"`; data-extraction and
  backup rules exclude all relevant domains.
- APK scans found no model weights/GGUF, prompt or context dumps, qualification
  corpus, database, backup/export artifact, recovery key, secret, or real-user
  record.
- No build output, generated database, key, backup, export, or real personal
  fixture is tracked.
- V1 register remained 24/24 `DENIED`.
- Forward plan remained exactly 25,031 bytes with SHA-256
  `bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886`;
  canonical copies 1, redundant copies 0, root `fdp.txt` absent.
- Canonical root verifier, `git diff --check`, and `git fsck --full --strict`
  passed; temporary Git metadata directories 0.

## Limitations and recommendation

CT-V2-15 does not admit an LLM, model artifact, remote inference, STT, TTS,
cloud sync/storage, accounts, new Therapy or safety policy, diagnosis, V1
therapeutic migration, production signing/release, telemetry, clinical claims,
or app-store/legal readiness.

The canonical forward plan names the next phase **CT-V2-16 - Longitudinal
Pattern Engine**, not model admission. The repository is ready for Principal
consideration of that exact phase, but CT-V2-16 remains unopened and
unauthorized.

## Terminal dispositions

```text
SINGLE_CANONICAL_ANDROID_V2_RUNTIME_QUALIFIED
PRODUCTION_V2_COMPOSITION_ROOT_QUALIFIED
END_TO_END_GOVERNED_TURN_PATH_QUALIFIED
PROTECTED_PERSISTENCE_ANDROID_COMPOSITION_QUALIFIED
ANDROID_KEYSTORE_TARGET_DEVICE_BEHAVIOR_QUALIFIED
PROCESS_DEATH_PERSISTENCE_QUALIFIED
DEVICE_REBOOT_PERSISTENCE_QUALIFIED
JOURNAL_ANDROID_INTEGRATION_QUALIFIED
BIOGRAPHER_ANDROID_INTEGRATION_QUALIFIED
THERAPY_ANDROID_INTEGRATION_QUALIFIED
LONGITUDINAL_CROSS_MODE_INTEGRATION_QUALIFIED
SAFETY_ANDROID_PATH_QUALIFIED
GOVERNED_RENDERER_ANDROID_INTEGRATION_QUALIFIED
NO_RESPONSE_END_TO_END_SILENCE_QUALIFIED
MODE_SWITCHING_QUALIFIED
ANDROID_LIFECYCLE_STATE_QUALIFIED
TURN_SERIALIZATION_QUALIFIED
SELECTIVE_DELETION_END_TO_END_QUALIFIED
PRIVATE_EVIDENCE_END_TO_END_QUALIFIED
EXPORT_BACKUP_RESTORE_UI_CUSTODY_QUALIFIED
TYPED_INPUT_PRODUCTION_PATH_QUALIFIED
UI_THERAPEUTIC_AUTHORITY_ZERO
RENDERER_THERAPEUTIC_AUTHORITY_ZERO
MODEL_THERAPEUTIC_AUTHORITY_ZERO
MODEL_PERSISTENCE_AUTHORITY_ZERO
MODEL_ARTIFACTS_PACKAGED_ZERO
REMOTE_INFERENCE_AUTHORITY_ZERO
THOMAS_RESPONSE_USER_EVIDENCE_AUTHORITY_ZERO
V1_THERAPEUTIC_MIGRATION_AUTHORITY_ZERO
GENERIC_ANDROID_BACKUP_AUTHORITY_ZERO
CT_V2_04_SAFETY_AUTHORITY_PRESERVED
CT_V2_05_THERAPY_AUTHORITY_PRESERVED
CT_V2_09_JOURNAL_AUTHORITY_PRESERVED
CT_V2_10_BIOGRAPHER_AUTHORITY_PRESERVED
CT_V2_11_RETRIEVAL_AUTHORITY_PRESERVED
CT_V2_12_LONGITUDINAL_THERAPY_AUTHORITY_PRESERVED
CT_V2_13_RENDERER_AUTHORITY_PRESERVED
CT_V2_14_PERSONAL_DATA_GOVERNANCE_PRESERVED
ANDROID_STUDIO_CANONICAL_VCS_ROOT_REMAINS_VALID
NEXT_PHASE_READY_FOR_PRINCIPAL_CONSIDERATION
```
