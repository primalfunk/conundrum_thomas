# CT-V2-02 qualification record

**Qualified:** 2026-09-03

**Starting HEAD:** `dca9c9876861d4ea410678eb79e1f85089de8ec7`

**Starting tree:** `7c7556a229097960bf3216a0435a9e0ca37d3082`

**Starting tag:** `ct-v2-01-governed-source-corpus`

## Scope qualified

This record qualifies the typed ontology, mode-authority contracts, exact provenance bindings, and denial of runtime therapeutic authority. It does not clinically approve a concept, resolve a source conflict, authorize commercial use, qualify hardware, or open CT-V2-03.

## Implementation

- Added platform-independent `:thomas:ontology`, depending only on `:thomas:domain` and `:thomas:provenance`.
- Added typed Therapist, Biographer, and Journal contracts to `:thomas:domain`.
- Added stable source document/version/section/locator/conflict/review identifiers to `:thomas:provenance`.
- Defined ontology release `ct-v2-02-ontology` version `1.0.0`.
- Defined 109 concepts across observed state, goals, dialogue acts, intervention families, constraints, safety contexts, and procedural vocabulary.
- Added a non-numeric epistemic model for direct statement, observation, history, external/derived facts, hypothesis, contradiction, unresolved material, and unknown material.
- Added 13 provenance-only concept bindings to exact CT-V2-01 source records.
- Added zero applied therapeutic constraint links and zero production consumers of the ontology.

Implementation commit:

- `8a3f8ec1743ebfbafd814cd56731a646d625cad6` — `CT-V2-02 define ontology and mode authority contracts`

## Clean full build

Command:

```text
gradlew -g .gradle clean build lint generateRuntimeProvenanceDb --no-daemon --no-configuration-cache --stacktrace
```

Result: `BUILD SUCCESSFUL` in 27 seconds; 308 actionable tasks, 280 executed and 28 up-to-date.

- Application unit tests: 1 passed.
- Foundation architecture tests: 9 passed.
- Ontology anti-authority qualification tests: 5 passed.
- Ontology structural tests: 10 passed.
- Provenance database tests: 11 passed.
- Ontology/source-binding tests: 4 passed.
- Total: 40 passed, 0 failed, 0 errors, 0 skipped.
- Android lint: 0 errors; 17 advisory warnings in unchanged scaffold/dependency/ABI categories.
- Debug APK assembled: 30,452,090 bytes.
- Unsigned release APK assembled: 22,116,338 bytes.
- The ignored provenance database regenerated successfully.

No Android device or emulator was used. No hardware or device-launch qualification is claimed.

## Structural qualification

Tests establish:

- stable, unique, namespaced concept identifiers and deterministic catalog ordering;
- all seven concept families are represented without a giant flat enum;
- unknown future concept identifiers are preserved without reinterpretation;
- unknown, tentative, conflicting, and insufficient evidence are representable;
- contradictions identify their competing evidence;
- Thomas hypotheses cannot be represented as user-established reports;
- evidence has no direct conversion into a durable profile fact;
- every source-linked candidate resolves through exact document, version, section, and locator records;
- binding authority-domain, source review state, commercial-use state, pending review IDs, and conflict states match the generated governed corpus;
- clinical and engineering authority cannot be cross-bound;
- the full prerequisite/constraint/review/source-version grammar is representable without evaluation;
- Therapist, Biographer, and Journal remain distinct;
- Journal defaults to first-class `NO_RESPONSE`;
- Biographer has no direct profile-mutation authority;
- all source and ontology review requirements remain pending.

## Prohibited-authority audit

| Authority surface | Implemented/authorized |
|---|---:|
| Therapeutic rules | 0 |
| Runtime-authorized interventions | 0 |
| Runtime-authorized safety contexts | 0 |
| Applied therapeutic constraint links | 0 |
| Diagnostic classifiers | 0 |
| Predictive risk scores or low/medium/high bands | 0 |
| Crisis keyword routers | 0 |
| Safety or escalation algorithms | 0 |
| Autonomous language-model decisions | 0 |
| Direct profile-promotion paths | 0 |
| Production ontology consumers | 0 |
| V1 migrations authorized | 0 |

The ontology module has no dependency on engine, safety, runtime, app, Android, renderer, model, speech, persistence, Room, SQLite, or JNI. No production module depends on the ontology during CT-V2-02.

## Provenance and source disposition

- 13 concepts are source-linked candidates; every binding is `PROVENANCE_ONLY` and `NOT_AUTHORIZED`.
- Every linked clinical and rights review is `PENDING`.
- No CT-V2-01 source, version, rights, conflict, or review record was changed.
- No new source publication or raw artifact was added.
- Existing NIMH/NICE and CCI/WHO scope distinctions remain unresolved and represented.
- Six concepts explicitly require source review or discovery: coping planning, grounding/orientation, interpersonal exploration, ordinary-distress context, urgent external-intervention consideration, and emergency context.

## Repository and artifact integrity

- Pre-seal worktree: clean.
- `git fsck --full --strict`: exit 0; only harmless unreachable work blobs were reported.
- Tracked raw/restricted clinical artifacts: 0.
- Tracked model binaries: 0.
- Local raw cache and generated database remain ignored.
- V1 register: 24 components, 24 `DENIED`, 0 migration commits.
- Production psychological persistence: not introduced.
- Android backup policy: unchanged and disabled.

Final Git object identities and the annotated completion tag are reported at handoff because a commit cannot contain its own object ID.

## Intermediate qualification note

One intermediate combined run found that the pre-existing tracked-raw-source audit recognized only the CT-V2-01 temporary Git metadata name. The test was made phase-independent. The subsequent combined run and the full clean build passed.

## Disposition

CT-V2-02 meets its structural and anti-authority gates. CT-V2-03 remains unopened and requires separate Principal authorization.
