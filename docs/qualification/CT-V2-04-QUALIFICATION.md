# CT-V2-04 qualification record

**Qualified:** 2026-09-03

**Starting HEAD:** `0cad8c83b864bd1137c128d0604d3a5c45df6830`

**Starting tree:** `38cbc8b8e0035743b718f0e426216d13082a6267`

**Starting tag:** `ct-v2-03-procedural-vertical-slice`

## Disposition

CT-V2-04 is complete as a deterministic, qualification-only safety and scope authority gate. The gate decides whether the bounded ordinary Therapist policy may run; it does not predict harm, diagnose, perform screening, or implement a crisis-response procedure.

No safety rule, ordinary therapeutic rule, screening instrument, clarification act, or renderer contract has production authority. All specialized response procedures remain unopened unless explicitly described as an engineering termination boundary.

## Architecture implemented

- Added typed, epistemically explicit safety/scope evidence. `ESTABLISHED`, `UNKNOWN`, `NOT_ASKED`, `USER_DECLINED`, `TENTATIVE`, and `CONTRADICTORY` remain distinct.
- Added 14 deterministic, priority-aware rules with complete considered/matched/rejected traces, missing facts, conflicts, review blockers, provenance, prohibited paths, and next-state requirements.
- Added software authority results for ordinary admission, clarification, specialized-policy handoff, emergency boundary, insufficiency, unsupported population/scope, review blocking, conflict, invalid input, and absent graph coverage.
- Added a revision-bound `OrdinaryTherapyPermit`. The CT-V2-03 evaluator has no state-only entry point and rejects a stale or mismatched permit.
- Added a qualification-only safety clarification action and narrow renderer contract. The renderer cannot choose the safety result, change the act, add ordinary therapeutic content, or add an unselected emergency instruction.
- Added a deterministic test renderer; no LLM, NLP classifier, keyword router, network, embedding, clock, or randomness participates.
- Registered the ASQ only as a governed bibliographic screening-instrument reference. No instrument questions, wording, scoring, thresholds, or pathways were implemented.
- Kept all policy and evidence structures independent of Android and free of direct profile mutation.

Implementation commit:

- `50749efcc3265bc4bb5c09ae56159d1aeeb2caba` - `CT-V2-04 implement procedural safety scope gate`

The final sealing commit and annotated tag are reported at handoff because a commit cannot contain its own object ID.

## Source adjudication

One clinical rule was adopted narrowly from NICE NG225 version `nice-ng225-2025`:

- Rule `ctv204-r003-self-harm-specialized-boundary` uses recommendations 1.6.5-1.6.6, governed section `section-ng225-assessment-focus`, PDF page 16 of 77.
- Governed artifact SHA-256: `869813027e9ff30351a26ae9ceda614ec3cf3f0dc553384be1fb8087405d35c2`.
- The rule only stops the ordinary policy after established self-harm-relevant evidence. It does not perform psychosocial assessment, risk formulation, treatment, discharge, safety planning, or emergency routing.
- Clinical, rights, legal, implementation-scope, software-autonomy, deliverer-training, supervision, and production reviews remain pending.
- Conflict record `scope-asq-vs-ng225` remains `NOT_A_CONFLICT_DIFFERENT_SCOPE`; it was not collapsed into a shared algorithm.

NIMH ASQ, WHO LIVE LIFE, WHO mhGAP guideline and Intervention Guide, and VA safety-planning material were reviewed but deferred from executable rules because their setting, deliverer, procedural completeness, currency/conflict, rights, or software-autonomy assumptions do not authorize a general autonomous consumer-app procedure. Exact dispositions are in `docs/safety/CT-V2-04-SOURCE-ADJUDICATION.md`.

The remaining 13 rules are explicitly labeled engineering authority or epistemic guards. They do not claim clinical provenance.

## Deterministic decision coverage

The synthetic matrix covers ordinary admission, unknown and not-asked evidence, refusal, established self-harm relevance, contradictory and tentative evidence, specialized conditions, medical and current emergencies, emergency precedence, harm to others, unsupported population and setting, specialized/out-of-scope presentations, review blocking, equal-priority conflict, malformed input, repeated evaluation, CT-V2-03 admission and denial, reassessment at a new evidence revision, empty graph coverage, and stale permits.

Equivalent immutable evidence and policy version produce equal decisions. Unique highest priority is required. Equal highest-priority winners return `POLICY_CONFLICT`; missing graph coverage returns `NO_AUTHORIZED_ACTION`; unknown evidence never becomes established absence.

## Qualification results

Clean combined command:

```text
gradlew -g .gradle clean build lint generateRuntimeProvenanceDb --no-daemon --no-configuration-cache --console=plain
```

Result: `BUILD SUCCESSFUL` in 1 minute 32 seconds; 309 actionable tasks, 281 executed and 28 up-to-date.

The final suite contains 116 tests:

- application foundation: 1;
- foundation architecture: 9;
- ontology authority: 5;
- CT-V2-03 procedural decision table: 18;
- CT-V2-03 procedural policy invariants: 9;
- CT-V2-03 renderer boundary: 5;
- safety-gate capability: 4;
- safety renderer boundary: 5;
- safety/scope decision table: 21;
- safety/scope invariants and negative authority checks: 10;
- ontology structural: 10;
- ontology/source bindings: 4;
- CT-V2-03 procedural source bindings: 2;
- safety rule/source bindings: 2;
- provenance database: 11.

Final test result: 116 passed, 0 failed, 0 errors, 0 skipped.

Android lint: 0 errors and 11 non-error advisory warnings in existing starter/development-floor categories.

The sandboxed build could not invoke the external NDK strip executable and emitted the expected unstripped-library warning. Debug and release APKs were therefore reassembled outside that restriction with all 94 tasks rerun; both strip tasks and the assembly completed successfully in 32 seconds.

- Debug APK: 30,959,994 bytes; SHA-256 `eccb45352b43248d74b21cc1214e21b51787c6c09e35fd23f1d04d3afb7c1020`.
- Unsigned release APK: 22,542,322 bytes; SHA-256 `6614f9d213c4edaeb9ce0c5748646e547b65cfb5ffe6beb4e017a36159b19d54`.
- Generated ignored provenance database: 356,352 bytes; SHA-256 `bdcce2b3c289eb22047455e72c43aff775d170c3e8e0c51f8c77562967daa71c`.

No Android device or emulator was used. No device, hardware, clinical, legal, rights, software-autonomy, or production qualification is claimed.

## Authority audit

| Authority surface | Count |
|---|---:|
| Safety/scope rules | 14 |
| Clinically sourced safety rules | 1 |
| Engineering authority/epistemic guards | 13 |
| Safety rules with exact provenance | 14 |
| Safety rules lacking provenance | 0 |
| Qualification-executable safety rules | 14 |
| Production-authorized safety rules | 0 |
| CT-V2-03 ordinary therapeutic rules | 21 |
| Production-authorized ordinary therapeutic rules | 0 |
| Screening instruments implemented | 0 |
| Screening instruments runtime-authorized | 0 |
| Numeric risk scores | 0 |
| Risk bands | 0 |
| Predictive classifiers | 0 |
| Diagnostic classifiers | 0 |
| Keyword/regex crisis routers | 0 |
| LLM safety decisions | 0 |
| LLM therapeutic decisions | 0 |
| Ordinary-policy bypass paths | 0 |
| Direct profile mutation paths | 0 |
| V1 migration authorities | 0 |

## Repository and artifact integrity

- Raw/restricted clinical artifacts tracked: 0.
- Model artifacts tracked: 0.
- Build outputs tracked: 0.
- New raw source artifacts acquired: 0.
- V1 register: 24 components, 24 `DENIED`, 0 migration commits; migration phase marker advanced to CT-V2-04 without granting authority.
- V1 implementation files migrated: 0.
- Review events fabricated: 0.
- Production psychological persistence introduced: 0.
- Backup disposition: unchanged and disabled.
- Remote/push: none / not performed.
- Final worktree and `git fsck --full --strict` are verified at seal and reported at handoff.

## Recommendation

The repository is architecturally ready for the Principal to consider a separately authorized phase of broader ordinary therapeutic policy development. Any such work should remain qualification-only until the pending clinical, rights, legal, implementation-scope, and software-autonomy gates are resolved. Specialized self-harm, emergency, harm-to-others, medical, and other excluded procedures remain genuine open work and must not be inferred from the present termination boundaries.

No next phase is opened by this qualification record.
