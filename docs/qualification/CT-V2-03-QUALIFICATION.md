# CT-V2-03 qualification record

**Qualified:** 2026-09-03

**Starting HEAD:** `fc65e6863f17f33c293e8b51296ddcbb8c1020c7`

**Starting tree:** `8d20fe8bad6f04096c4e8f89af6f89ac9a704653`

**Starting tag:** `ct-v2-02-therapeutic-ontology`

## Disposition

CT-V2-03 is complete as a deterministic, qualification-only architecture proof for one bounded non-emergency personal-problem slice.

It does not clinically approve the rules, grant commercial rights, authorize autonomous software delivery, establish safety handling, create production therapeutic authority, or open a later phase.

## Implementation

- Added a typed `BoundedProblemPolicyState` with explicit epistemic uncertainty and upstream safety/scope inputs.
- Added 21 typed rules, 13 actions, 13 procedural stages, structured preconditions and exclusions, unique-priority selection, visible conflicts, terminal/handoff results, and complete evaluation traces.
- Added exact rule provenance through CT-V2-01 document/version/section/artifact records.
- Added 11 page-level source-section records without adding or tracking source content.
- Added structured renderer constraints and explicitly authorized per-request supporting text.
- Added a non-LLM deterministic renderer and a nine-turn synthetic scenario in `:qualification` only.
- Added exact decision-table, property, renderer-boundary, mode-authority, V1-denial, and source-binding tests.
- Left `:thomas:safety` unchanged and unwired; no safety inference or crisis behavior exists.
- Left `:thomas:runtime` without pipeline code; app, runtime, safety, and platform adapters do not reference the qualification evaluator or renderer.

Implementation commit:

- `e43a510c9d3de50fbab522b7793465937bf36ba2` — `CT-V2-03 implement qualification-only procedural slice`

The final sealing commit and annotated tag are reported at handoff because a commit cannot contain its own object ID.

## Source derivation

The exact controlled artifacts were re-hashed before rule implementation:

| Source version | SHA-256 | Exact rule subjects | Source-derived rules |
|---|---|---|---:|
| WHO/UNICEF Foundational Helping Skills 2025 with incorporated corrigendum (`who-fhs-2025`) | `b3dde55d3e1a601699a41b4aaaef4d020cc9416fbc6773848396db81df52cbe9` | Verbal communication; collaborative goals; eliciting feedback | 6 |
| WHO Individual PM+ generic field-trial v1.1, 2018 (`who-pm-plus-v1-1-2018`) | `aaa6ce06dacc1058ba8b695b7d8146d33abfdfcf8a13ea7d888f71a01a833cbe` | Respecting reluctance; advice boundary; problem definition; options; action plan; outcome review | 8 |

Seven architecture scope guards cite the tracked Principal authority-scope record. Fourteen source-derived rules cite exact governed clinical source records. All 21 rules therefore have exact provenance; zero lack provenance.

All clinical, rights, implementation-scope, software-autonomy, human-helper training, and supervision restrictions remain visible and unresolved. No review status was changed, no review event was fabricated, and no source conflict was adjudicated.

## Canonical synthetic flow

The fixture contains no real user information.

| Turn | Structured state delta | Goal | Selected action | Deterministic output | Expected next information |
|---:|---|---|---|---|---|
| 1 | Intent unknown | `goal.clarify` | `ask-support-preference` | “Would you like me to listen, help you understand it, or work through a practical next step?” | `SUPPORT_INTENT` |
| 2 | Practical-help intent established | `goal.understand` | `ask-problem-description` | “What is the one present problem you would like us to focus on?” | `BOUNDED_PROBLEM_DESCRIPTION` |
| 3 | Synthetic scheduling problem established; understanding tentative | `goal.establish-shared-understanding` | `verify-problem-understanding` | Tentative summary of only the authorized synthetic problem, then one correction question | `SHARED_UNDERSTANDING_CONFIRMATION` |
| 4 | Understanding confirmed; ready to act | `goal.clarify` | `ask-influenceable-part` | “Which part of this situation do you think you can influence or change?” | `INFLUENCEABLE_PART` |
| 5 | At least partly influenceable | `goal.support-problem-solving` | `invite-user-options` | “What possible ways of influencing that part come to mind for you?” | `USER_GENERATED_OPTIONS` |
| 6 | Two user-generated synthetic options established | `goal.support-decision-making` | `ask-user-to-choose-option` | “Which of your options seems most helpful and feasible to try?” | `USER_SELECTED_OPTION` |
| 7 | User selection established | `goal.support-behavioral-planning` | `develop-bounded-plan` | “What small first step would you take, and when would you take it?” | `BOUNDED_ACTION_PLAN` |
| 8 | Bounded plan established; no outcome yet | `goal.reduce-conversational-burden` | `wait-for-outcome` | Exact empty string (`dialogue.no-response`) | `REPORTED_PLAN_OUTCOME` |
| 9 | Attempted outcome reported | `goal.consolidate-learning` | `review-reported-outcome` | “What happened when you tried the plan?” | `OUTCOME_MEANING_OR_OBSTACLE` |

At every turn, the decision includes all considered rules, precondition and exclusion evaluations, eligible and rejected alternatives, the unique winner, exact provenance, unresolved restrictions, and the outcome contract.

## Determinism and failure behavior

- Identical immutable input plus policy version produces an equal `PolicyDecision` object.
- No random, clock, network, embedding, prompt, or model dependency exists in policy.
- A unique highest priority is required; equal winners produce `POLICY_CONFLICT`.
- A matching non-executable candidate produces `REVIEW_BLOCKED`.
- Missing graph coverage produces `NO_AUTHORIZED_ACTION`.
- Invalid structured state produces `INVALID_INPUT` before rule execution.
- Unknown safety produces `INSUFFICIENT_INFORMATION`, never ordinary-safe.
- Specialized safety or scope produces a typed handoff and no ordinary action.

## Qualification results

Clean combined command:

```text
gradlew -g .gradle clean build lint generateRuntimeProvenanceDb --no-daemon --no-configuration-cache --stacktrace
```

Result: `BUILD SUCCESSFUL` in 1 minute 26 seconds; 309 actionable tasks, 281 executed and 28 up-to-date.

The clean run passed 73 tests. One final production-wiring isolation regression was then added and passed; the final suite contains 74 tests:

- application foundation: 1;
- foundation architecture: 9;
- ontology authority: 5;
- procedural decision table: 18;
- procedural policy and authority invariants: 9;
- renderer boundary: 5;
- ontology structural: 10;
- ontology/source bindings: 4;
- procedural rule/source bindings: 2;
- provenance database: 11.

Final result: 74 passed, 0 failed, 0 errors, 0 skipped.

Android lint: 0 errors and 11 advisory warnings in starter-scaffold/development-floor categories: seven unused scaffold colors, two obsolete SDK guards/resources, one redundant label, and one expected arm64-only ChromeOS ABI advisory.

The initial sandboxed assembly completed but could not invoke the external NDK strip executable. Both APKs were then reassembled outside that restriction with all 94 tasks rerun; `stripDebugDebugSymbols` and `stripReleaseDebugSymbols` completed and the build succeeded in 28 seconds.

- Debug APK: 30,796,154 bytes; SHA-256 `adc7e3605921a06ed227e47da9c0f490820e3a940e1db9502fc8159539b7c697`.
- Unsigned release APK: 22,411,250 bytes; SHA-256 `032ee1e191fc0e45c37162c27e8fe857f67a592fe48bdae4183353e05de15d93`.
- Generated ignored provenance database: 356,352 bytes; final SHA-256 `ff55ff486fc4f089d66f98246d0887799adbb8303b9f7e564a1a2f0e051a0c7c`.

No Android device or emulator was used. No device, hardware, clinical, or production qualification is claimed.

## Authority audit

| Authority surface | Count |
|---|---:|
| Procedural rules implemented | 21 |
| Rules with exact provenance | 21 |
| Rules lacking provenance | 0 |
| Qualification-executable rules | 21 |
| Production-runtime-authorized rules | 0 |
| Candidate action definitions | 13 |
| Runtime-authorized ontology intervention concepts | 0 |
| Diagnostic classifiers | 0 |
| Predictive risk scores | 0 |
| Crisis keyword routers | 0 |
| Safety algorithms | 0 |
| LLM-selected therapeutic actions | 0 |
| Profile-direct-mutation paths | 0 |
| V1 migration authorities | 0 |

## Repository and artifact integrity

- Raw/restricted clinical artifacts tracked: 0.
- Model artifacts tracked: 0.
- New raw source artifacts acquired: 0.
- V1 register: 24 components, 24 `DENIED`, 0 migration commits.
- V1 implementation files migrated: 0.
- Review events fabricated: 0.
- Production psychological persistence introduced: 0.
- Backup disposition: unchanged and disabled.
- Remote/push: none / not performed.
- Final worktree and `git fsck --full --strict` are verified at seal and reported at handoff.

## Recommendation

The result proves the CT-V2-03 architectural thesis: Thomas can know the intended action, its reason, source, limits, and expected outcome with every language model removed.

The repository is ready for the Principal to decide whether to authorize the next bounded development phase. It is not ready for production therapeutic use. Clinical, rights, implementation-scope, safety, and product admission remain genuine gates before any user-facing therapeutic authority.
