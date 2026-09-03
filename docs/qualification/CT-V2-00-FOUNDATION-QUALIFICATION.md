# CT-V2-00 foundation qualification

- Qualification date: 2026-09-03
- Foundation candidate commit: `9879696132da535520e6327bab6f581e6a165d06`
- Candidate tree: `06ba4985c160acb83a4a41aca05fcae68ed204f6`
- Result: `PASS_WITH_DISCLOSED_DEVICE_LIMITATION`

## Root seal

- Starter digest reverified before Git initialization: `eacbaa2ccdad22d8fa9ddf16cb946fccd4eb310899409d7ce6ce7753423d9aaa`
- Scope: 40 files, 114,491 bytes
- Root commit: `33e3b601cf9dc535436c6874168dcb138208e1ae`
- Root tree: `d1cd0b15cbc78438dc44997be4e34aadcc3abc6e`
- Annotated tag: `ct-v2-root-baseline`

## Build and tests

The final forced build command was:

```powershell
.\gradlew.bat --offline --no-daemon --rerun-tasks build
```

Result: `BUILD SUCCESSFUL` in 16 seconds under host SDK permissions; 286 actionable tasks, 286 executed. The pass included all 10 Gradle modules, debug and unsigned release APK assembly, Android library AAR assembly, lint, JVM compilation, Android local unit tests, and the qualification test suite. Network was disabled for this final pass.

- Foundation architecture tests: 8 run, 0 failures, 0 errors, 0 skipped.
- App local unit tests: 1 run, 0 failures, 0 errors, 0 skipped.
- Android lint: pass for app and all Android adapter modules.
- Debug APK: 30,402,880 bytes; SHA-256 `8b2415019b653ff08f73aad7e2993001a96285bda6b98228e6eb8ce1d5ad488e`.
- Unsigned release APK: 22,083,570 bytes; SHA-256 `1f4da157a02b7a1253dcbffe999269cf1e14773a726404a7956b2e602e98e5ff`.

An earlier isolated-user build also passed all 286 forced tasks; it could not execute the host SDK's native strip utility and packaged an AndroidX native dependency unstripped. The authoritative host-permission pass above executed the strip tasks without that fallback.

## Dependency and authority gates

The executable foundation tests verified:

- all required modules are present;
- domain has no project dependency;
- engine and safety are siblings and safety does not depend on engine;
- runtime is the only production module depending on engine and safety together;
- app depends only on runtime among V2 projects;
- app cannot directly reach renderer, persistence, engine, or safety modules;
- renderer depends only on domain and cannot reach policy, safety, provenance, runtime, or persistence;
- the renderer request exposes exactly `RenderCommand` and explicitly authorized supporting text;
- application identity, minSdk, ABI, and backup policy are fixed; and
- CT-V2-01 seed records and production persistence remain absent.

## APK launchability and backup

`aapt2 dump xmltree` verified the built debug APK contains:

- package `com.conundrum.thomas.v2`;
- minSdk 31 and targetSdk 37;
- `com.conundrum.thomas.v2.MainActivity` exported with `android.intent.action.MAIN` and `android.intent.category.LAUNCHER`; and
- `android:allowBackup=false`.

Both merged debug and release manifests independently report package `com.conundrum.thomas.v2` and `allowBackup=false`. Source backup and data-extraction rules exclude all credential- and device-protected root, file, database, shared-preference, and external domains from cloud backup and device transfer.

No Android device was attached and no Android Virtual Device or installed system image existed on the workstation. A physical process launch and instrumentation run were therefore unavailable. APK assembly, manifest merge, bytecode/resource packaging, and the launch intent were verified; live-device smoke testing remains an environment-dependent follow-up and does not affect CT-V2-01's documentation/provenance authorization gate.

## Migration and content integrity

- Migration components: 24.
- `REUSE_LIKELY`: 3.
- `REUSE_WITH_ADAPTATION`: 10.
- `REFERENCE_ONLY`: 7.
- `RETIRE`: 3.
- `UNDETERMINED`: 1.
- Approval state: 24 `DENIED`, 0 non-denied.
- Eventual migration commits recorded: 0.
- Legacy non-V2 package declarations in production/test source: 0.
- Tracked model binaries with prohibited extensions: 0.
- Raw/source clinical-material directories: absent.
- CT-V2-01 provenance seed records: 0.
- Persistence implementation source files: 0.

The 19 V2 implementation/build/XML files added or changed from the starter baseline were compared by Git blob identity with every blob in the three audited V1 reference HEADs. Exact V1 blob overlaps: 0. This is supporting evidence, alongside the bounded diff and migration register, that no V1 implementation file crossed into V2.

## Remaining final seal checks

After this evidence record is committed, the final seal procedure must run `git diff --check`, `git fsck --strict`, tracked-file exclusion scans, tag verification, and clean-worktree verification. The immutable final commit and tree are reported externally because a commit cannot contain its own hash.
