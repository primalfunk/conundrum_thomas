# CT-V2-01 qualification record

**Qualified:** 2026-09-03  
**Starting HEAD:** `990bf8a61533134c5e713b5c384e6eaa10b955ee`  
**Starting tree:** `de9bffa56f111f50093874bcc26ee84af955e11a`

## Scope qualified

This record qualifies the governed source/provenance mechanism and its continued compliance with the CT-V2-00 foundation. It does not qualify any source clinically, grant therapeutic authority, approve commercial use, or open CT-V2-02.

## Clean full build

Command:

```text
gradlew -g .gradle clean build lint generateRuntimeProvenanceDb --no-daemon --no-configuration-cache --stacktrace
```

Result: `BUILD SUCCESSFUL` in 36 seconds; 303 actionable tasks executed.

- Debug and release Android applications assembled.
- Generated debug APK: `app/build/outputs/apk/debug/app-debug.apk` (30,419,264 bytes).
- Application unit tests: 1 passed, 0 failed, 0 skipped.
- Foundation/architecture tests: 9 passed, 0 failed, 0 skipped.
- Provenance qualification tests: 11 passed, 0 failed, 0 skipped.
- Android lint completed with zero errors: 17 advisory warnings (16 app, 1 renderer scaffold).
- The ignored provenance database regenerated successfully from one zero-state migration and nine ordered seed files.

No Android device or emulator was connected during the final run. Application regression is established here by successful debug/release packaging, manifest/resource validation, application unit testing, and foundation architecture testing; no device-level behavioral claim is made.

## Provenance qualification gates

The executable suite proves:

- migration from an empty database;
- migration and seed input SHA-256 recording;
- document/version identity and uniqueness;
- authority and source-class linkage;
- one rights record and one freshness record for every source version;
- exact, immutable SHA-256 and byte size for every acquired artifact;
- multiple versions and recursive supersession traversal;
- open conflict and different-scope representations;
- exact recommendation/section addressing;
- constrained review-state transitions and an unclaimed review queue;
- ignored raw-cache isolation and absence from the Git index;
- optional local raw artifacts exactly matching all recorded hashes, sizes, and PDF signatures;
- platform independence of `:thomas:provenance`;
- no production dependency on raw documents or the build-only SQLite generator;
- no CT-V2-02 behavior in the provenance core.

## Corpus result

- 8 authorities across structurally distinct clinical/public-health and engineering-governance domains.
- 8 controlled source classes.
- 19 canonical documents.
- 21 source versions.
- 18 acquired artifacts with exact SHA-256 and byte size.
- 21 rights records and 21 freshness records.
- 19 pending clinical reviews, 19 pending rights reviews, 2 pending legal reviews, 3 pending software-autonomy reviews, and 2 pending implementation-scope reviews.
- 0 completed review requirements and 0 review events.

## Boundary and integrity disposition

- Raw source artifacts remain ignored under `provenance/raw/`; zero raw or restricted artifacts are tracked.
- The generated SQLite database remains ignored and is not packaged into the app.
- No model binary is present.
- The V1 migration manifest remains deny-by-default for every component; no eventual migration commit is recorded.
- No V1 implementation source crossed the migration gate.
- No therapeutic rule, intervention, dialogue act, state machine, screening score, crisis/safety algorithm, diagnostic classifier, or model authority was implemented.
- Android backup remains disabled and no production psychological-data persistence was introduced.

Final Git object identities are reported at phase handoff because a commit cannot contain its own object ID.
