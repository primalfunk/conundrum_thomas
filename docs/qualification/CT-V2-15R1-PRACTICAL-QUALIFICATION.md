# CT-V2-15R1 Practical transition adjudication and qualification

Date: 2026-09-06. Principal authority: bounded checkpoint-16 adjudication; repair only the established oracle/state/procedural defect; requalify; seal only on complete pass. Push and the rigorous deterministic end-to-end campaign remain unauthorized.

## Disposition

```text
CT_V2_15R1_PRACTICAL_TRANSITION_ADJUDICATED
CT_V2_15R1_PRACTICAL_SELECTION_CORRECT
CT_V2_15R1_EXPECTED_VERIFICATION_ACTION_CORRECT
CT_V2_15R1_RENDERER_REPEATED_OPENING_EXHAUSTION_ESTABLISHED
CT_V2_15R1_COMPLETE_PHYSICAL_QUALIFICATION_FAILED
CT_V2_15R1_OPEN
CT_V2_15R1_UNSEALED
CTV2_RIGOROUS_DETERMINISTIC_END_TO_END_TESTING_NOT_AUTHORIZED
```

**Thomas did not select summary instead of verification.** Physical and direct runtime traces prove that it selected core-verify-problem-understanding. The renderer received that act, rejected its sole authorized realization with REPEATED_OPENING, and returned RENDERING_UNAVAILABLE. The last delivered action therefore remained the prior core-summarize-shared-understanding.

The old expected semantic action was correct. Production mode propagation and procedural selection were correct. The action-history assertion exposed a delivery failure but did not identify the current selection. Changing the expectation to summary would mask the blocker.

This is neither Case A's correct new summary, Case B's failed Practical propagation, nor Case C's wrong procedural winner. It is also not unexplained nondeterminism. The question's two-winner premise is disproved. The actual failed boundary is renderer realization coverage after correct selection. No procedural/semantic-oracle repair is warranted; no production repair was made. Under the failure rule, the renderer blocker is returned to the Principal without expanding into another repair.

## Entry, candidate identity and preservation

Canonical main remained untouched throughout:

- Entry = final HEAD: 4f822fb3498b67b40124c0fb4ec1f19f98c92c8b.
- Entry = final tree: 69fa2a2b68105c4371574ac81316aa6002fd4d93.
- Branch main; clean; ahead 29 / behind 0 against locally recorded origin/main.
- Commits created: 0. Completion tag absent; tag object/target: none. Push: none.
- Exact reserved tag name: ct-v2-15r1-production-evidence-procedure-biographer-integration.

The prior dispatch candidate was reconstructed in a fresh detached local clone at out/ct-v2-15r1-practical/workspace:

| Artifact | Verified SHA-256 |
| --- | --- |
| out/ct-v2-15r1-dispatch/candidate.patch | 60d152f1b539a58a3e1315649ec374e56336213e51eed2128b4c3e1ecf50422a |
| out/ct-v2-15r1-dispatch/candidate.zip | da001ed1ade82075cd4334b34401d7e7dd26d1ebe67ad75971de41ce98baffad |

Forward/reverse patch checks pass. All 336 archived file identities matched at reconstruction, and all are preserved unchanged in the final candidate. The existing CT-V2-15R1-DISPATCH-QUALIFICATION.md is retained as historical evidence. New outgoing patch/archive identities are recorded in out/ct-v2-15r1-practical/candidate-artifacts.json.

## Exact checkpoint state and Practical propagation

[Direct adjudication](CT-V2-15R1-PRACTICAL-EVIDENCE/DIRECT-ADJUDICATION.md) and [physical/control comparison](CT-V2-15R1-PRACTICAL-EVIDENCE/physical-decision-comparison.json) provide the complete trace, field values, predicates, action history and capture limits.

Immediately before the new observation, the procedural session is android-therapy-state-83, safety revision 83, material revision 15, Therapist mode, last delivered route UNDERSTAND_CLARIFY. The previous concern/understanding is “the UI meeting was delayed”, user-confirmed; summary-delivered=true; correction ACKNOWLEDGED; pending CLOSURE_OR_NEW_DIRECTION; last delivered action core-summarize-shared-understanding. There are 14 delivered actions across UI turns 69–83; unchanged-evidence turn 71 added no action.

The mode path is proven at each layer:

| Boundary | Actual value/evidence |
| --- | --- |
| Physical control | Practical clicked; Compose selected-state assertion passes |
| UI / ViewModel | therapySupport=PRACTICAL_HELP; production mode THERAPY |
| Runtime request | requestedTherapySupport=PRACTICAL_HELP |
| Input observation | routePreference=PRACTICAL_HELP, RESOLVED_AS_REPORTED, current-turn-84-support-declaration |
| Procedural request | engine mode THERAPIST; matching fresh safety permit; ordinary policy allowed |
| Decision | PRACTICAL_PROBLEM_SOLVING; core-verify-problem-understanding; NEW_ACTION |
| Renderer input | selected verification act; CLARIFYING_QUESTION |
| Persistence | source acquisitionMode=THERAPIST_CONVERSATION; turn android-therapy-84 accepted |

There is no durable global support-preference field. UI support is active request state; source acquisition mode/content are persisted. Procedure, safety declarations and render history are intentionally ephemeral under the existing documented reopen contract. The pre-observation session still contains the prior Understand preference until observe applies the new request. activeRoute denotes the last delivered route, so its continued UNDERSTAND_CLARIFY value after rendering failure is not evidence that Practical failed to propagate.

The historical exact input is “My specific concern is: arranging a new UI meeting” without terminal punctuation. The Principal's terminal-period form and a different concern are separately tested and produce the same mechanism. Normalized comparison text is “my specific concern is: arranging a new ui meeting”.

Classification uses the existing general declaration rule. The new concern differs materially from the previous one. The observer establishes boundedness and engagement, forms a tentative literal restatement for the non-Listen route, resets the previous problem's dependent facts, retains delivered history, and advances material revision to 16. No new phrase match or exception is added.

The decision state is android-therapy-state-84 / safety revision 84 / material revision 16, PRACTICAL_HELP, ENGAGED, BOUNDED concern, NEW_CONTENT_AVAILABLE, tentative Thomas/shared understanding. Summary-delivered, correction, influence, willingness, options, selection, plan, outcome and review are unknown; pending information is null. Repeat authorization is null.

## Both competing actions

| Action / rule | Entry and blocking predicates | Adjudication |
| --- | --- | --- |
| core-verify-problem-understanding / ctv205-a017-problem-verify, priority 1000 | Practical route; established bounded concern; shared understanding TENTATIVE and Thomas understanding TENTATIVE | All match; sole eligible candidate |
| core-summarize-shared-understanding / ctv205-a013-understand-summarize-confirmed, priority 700 | Understand route; user-confirmed established understanding; summary not delivered | Wrong route and unconfirmed understanding reject it |

Route rule ctv205-r007-practical-route matches; ctv205-r006-understand-route does not. CoreRouteTransitionCatalog explicitly supports Understand → Practical. No competing priority tie or ordering defect exists.

Verification occurrences at revision 16 = 0; progression NEW_ACTION; no progression guard selected; no explicit-repeat authorization or direction-choice substitution. The prior summary changes delivery bookkeeping at revision 15, but the new concern resets confirmation-related state. Renderer history does not participate in selecting the procedural winner.

All considered action-rule traces, failed predicates, eligible candidate IDs, route rules and progression counters are retained in CTV215R1PracticalAdjudicationTest.xml. The real CoreOrdinaryTherapyEvaluator is exercised, not a mock decision table.

## Governing specification and oracle lineage

The governing docs/policy/CT-V2-05-DECISION-TABLE.md row “Practical, tentative” requires verification of a bounded problem with tentative understanding. The “Understand, confirmed” summary row requires different state. CT-V2-05-PATHWAY-GRAPHS.md agrees and permits the route transition.

docs/architecture/CT-V2-15R1-PRODUCTION-OBSERVATION-AND-COVERAGE.md explicitly says only successful final text or governed NO_RESPONSE records a delivered action. A failed renderer cannot create a pending confirmation or mark an action delivered.

Feature commit 98be998 originally selected Practical and checked for “Is that right”. Commit d0ca6c4 replaced lexical checks with core-verify-problem-understanding and read session.actionHistory.last().actionId. Later observation/identity changes retained that expectation. The source diffs are preserved in oracle-origin-98be998.txt and oracle-semantic-assertion-d0ca6c4.txt.

The expected act agrees with the governing contract and existing JVM Practical progression tests. The history-based failure message was ambiguous about selection versus delivery. No expected action was changed, and no assertion was relaxed.

## Established renderer blocker

At checkpoint 16, the plan carries core-verify-problem-understanding / dialogue.verify-understanding / goal.establish-shared-understanding into TherapyRenderCommandAdapter. The authorized text is:

“I might be understanding this as arranging a new UI meeting. Is that right?”

Verification has one authorized realization; the fallback is the same text. Existing therapyVariants adds invitation and certain reflective alternatives, but no alternate verification form.

RenderText.openingFingerprint hashes the first three normalized words. The unchanged validator rejects an opening already present twice in the last four delivered responses. Turns 80 and 82 both began “I might be”; the last-four window is 80, 81, 82, 83. Their opening fingerprint is f291cc5548ffd3d194f178b8eb72c7609ca669d536ba8b1d961f78ac0d73e15f.

The new full response is not an exact recent duplicate. REPEATED_OPENING is the rejection reason. Authorized realization search exhausts the single available text; the result is RENDERING_UNAVAILABLE, CLARIFYING_QUESTION, finalText=null, no assistant artifact. The runtime correctly refrains from recording delivery, leaving summary as the last delivered action.

Physical and direct complete GovernedRenderResult values match exactly, including digest:

56ba5b5032ddaf69b0c6ef465bca6c02462e803f332cfe7ebe4f5d0a8235b840.

This is an explicit rendering failure, not policy-authorized silence. Strict validation and Thomas's choice were preserved, but the warranted act was not realized. The full recurrence/delivery seal gate therefore fails. No validator relaxation, renderer development, procedural patch or policy substitution was performed.

## State equivalence and repeatability

Ten identical direct reconstructions produce one identical decision and one identical render result. The original text, terminal-period form and a different concern all select verification and encounter the same opening rejection.

The physical and JVM stores are not claimed identical: physical pre-turn revision is 83 and includes A–N history; direct pre-turn revision is 15. Both yield zero historical candidates/selected memories and NO_RELEVANT_MEMORY. Core policy runs before longitudinal retrieval.

Physical pre-state and classified-state captured prefixes match the direct serialized states exactly: 3,992/4,339 characters and 3,977/4,090 characters respectively. Android Logcat truncates long lines. The complete 14-entry delivered history is independently reconstructed from per-turn markers and matches exactly. Remaining fixed/default tail fields are governed by unchanged observer code; no byte-complete physical object dump is claimed. The complete renderer result fits in the log and is compared in full. Continuous capture prevents the prior run's early-event ring-buffer loss.

The post-checkpoint-15 session supplies the pre-state comparison: selecting Practical mutates UI preference, not that session; observe then applies the new request. Complete direct state/evidence references and physical measured boundaries are preserved separately.

Cold-reopen control preserves sources and digest, clears ephemeral procedure/render history, then fresh current declarations plus Practical input successfully select and deliver verification. Source 84 survives another JVM reopen and next allocation is 85. Adjacent Practical ordering remains verification → influenceable part → readiness → user options. These controls do not waive the deferred actual-UI reopen gates.

## Scope and software qualification

No production file or existing oracle was changed relative to the recovered candidate. All 336 recovered files remain byte-identical after temporary diagnostic instrumentation was restored.

Existing production repair retained unchanged:

- app/src/main/java/com/conundrum/thomas/v2/ThomasViewModel.kt: durable allocator delegation; SHA-256 08a2b697208d8ada205d6166c4bc56046a26053ba0f6e443d968333377bfe902.
- thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ThomasProductionRuntime.kt: durable canonical admission frontier plus live allocation frontier; SHA-256 c86e1222569557f72b26fd5ca8b9f7a8f26cb408d18613b04b99f4ce1fb51e20.

New test file:

- qualification/src/test/kotlin/com/conundrum/thomas/v2/qualification/CTV215R1PracticalAdjudicationTest.kt: four diagnostics covering exact-state repeatability/eligibility/render boundary, phrase variation, reopen/durability, and adjacent Practical ordering. These expose the known failure; they do not count it as a repaired delivery gate.

New diagnostics/evidence:

- docs/qualification/CT-V2-15R1-PRACTICAL-EVIDENCE/: recovery verification, pre-adjudication record, direct controls/XML, governing-rule analysis, oracle lineage, diagnostic-only runtime/UI source snapshots, physical runner/continuous capture/comparison, software inventories, source audit and raw/normalized manifests.
- This CT-V2-15R1-PRACTICAL-QUALIFICATION.md report.

Temporary runtime logging and UI selected-state observations were used only in the disposable diagnostic APK. Exact archived production and physical fixture files were restored afterward. Production dispatch, renderer, policy, ontology, Pattern Engine, longitudinal reasoning, model/prompt authority, speech, privacy/governance, corpus contents and collision defenses remain unchanged. No tests were deleted, skipped or weakened; no completion timeout was increased.

Clean command: gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain.

| Software gate | Exact result |
| --- | --- |
| JVM suites / tests | 65 / 1,004 |
| Failures / errors / skips | 0 / 0 / 0 |
| New adjudication diagnostics | 4/4 pass; 10 identical-state repetitions |
| Lint errors / fatals | 0 / 0 |
| Warnings | 16 app + 1 unchanged library ChromeOsAbiSupport = 17 aggregate |
| Warning adjudication | Same accepted nonfatal class; no new warning-producing production change |
| Clean build / provenance | PASS, 1m 51s; 393 actionable tasks, 391 executed, 2 up-to-date |
| V1 register | Default DENIED; migration false; 24/24 components DENIED |
| Source / root / Git integrity | PASS; informational dangling blobs retained |

Canonical APKs, captured before fixture rebuilding and never installed:

- Debug: 31,564,628 bytes; SHA-256 3819e58ff9ad224225f8e3be2256c76efb119f1f69048acf0febe8c01fb42992.
- Unsigned release: 24,295,695 bytes; SHA-256 ae54829ca31fd569c3bcdbfa0de87a701dd759862f585599ab4a0e80e35df6e4.

Both are byte-identical to the archived candidate. Release provenance remains entry HEAD; no general reproducible-build claim is made.

## Physical results

TCL 9491G, serial BC9424B4E3C5002, Android 15/API 35, arm64-v8a.
Fingerprint TCL/9491G_ZZ/Hera_Vis_WIFI:15/AP3A.240905.015.A2/2FA6:user/release-keys.

Run-01 reconstructs the full isolated history from empty fixture data: a → force-stop → b → force-stop → c. It is an instrumented adjudication run; no diagnostic APK is represented as an uninstrumented release candidate. The original expected action and completion timeout remain unchanged.

| Gate | Result |
| --- | --- |
| a, complete A–M runtime | PASS, 451.483 s |
| b, N cold reopen | PASS, 80.955 s |
| c, complete UI sequence attempt | FAIL at checkpoint 16, 827.585 s total |
| UI Practical propagation | PASS through selected control, ViewModel/request/observation and decision |
| UI checkpoint 16 admission/save | PASS, identity 84, draft cleared, Not saved absent |
| UI checkpoint 16 selected act | Correct verification, NEW_ACTION |
| UI checkpoint 16 faithful realization | FAIL, REPEATED_OPENING / RENDERING_UNAVAILABLE |
| d/e, additional UI reopen/durability/subsequent identities | NOT RUN after c failed |

A LISTEN; B UNDERSTAND; C both PRACTICAL outcomes; D CORRECTION; E PAUSE/RELUCTANCE; F STAGNATION; G NEW EVIDENCE; H SAFETY UNKNOWN; I SAFETY CONFLICT; J TARGETED BIOGRAPHER; K ANSWER/COVERAGE; L JOURNAL RECALL; M BIOGRAPHER RECALL; N CLOSE/REOPEN: all runtime gates pass. UI pause, resume and refusal pass again before checkpoint 16.

All actual UI inputs 69–84 commit once with increasing identities and Not saved absent. Turn 84 reaches the store and governed decision path successfully; the failed rendering does not become an unsaved input. The mandatory post-checkpoint actual-UI first/second reopen gates remain incomplete. JVM and stress reopen evidence do not waive them.

Run-02 separately reruns the required Android dispatch software gates on an empty disposable installation using the restored, uninstrumented candidate APK:

| Dispatch gate | Result |
| --- | --- |
| Controlled keyboard movement regression | PASS, 3.980 s |
| 20 consecutive accepted actual UI submissions | PASS, 44.626 s |
| All 20 exact sources after process reopen | PASS, 2.455 s |

Exactly one user turn and one source per accepted action; stress identities 2–21 survive reopen. Both apostrophe forms of the known refusal are included. This proves admission reliability, not successful rendering for every therapeutic input.

## Durable identity and renderer authority

The preserved allocator uses the highest committed canonical numeric identity in active/archived redacted admission history and its live frontier. It does not use store revision or record count, renumber history, or weaken collision rejection. Its archived failure-first oracle remains identities 2/3, revision 2, old reopened allocation 3/conflict; repaired allocation 4 then 5 across reopen. Its ten original regressions remain passing.

No new identity collision, duplicate dispatch or Not saved was observed. Final physical qualification of the allocator remains incomplete because the required downstream UI reopen gates did not run.

Thomas's semantic authority is proven at checkpoint 16: it warrants verification, and that exact act reaches rendering. Strict recent-duplicate/opening controls, deterministic rendering and semantic constraints remain active. Existing invitation alternatives still qualify pause/resume. The new verification opening-exhaustion blocker prevents the complete renderer recurrence/delivery contract from being claimed qualified or sealed.

## Custody and cleanup

Both new runs compare the protected original before/after and report unchanged custody and zero remaining disposable packages.

Protected original com.conundrum.thomas.v2, UID 10666, version 1.0/code 1.
First install 2026-09-04 21:17:05; last update 2026-09-04 22:31:39, unchanged.
Original APK SHA-256 before = after: 3fa1f8590a16b8eadc85b0e007d7b7e6b67f425aff9f328516b73b5b2c5d2ae4.
Code path unchanged: /data/app/~~EVUd780xI9Crs1SpbCSJlw==/com.conundrum.thomas.v2-ZGzmD8XuteCvmrgm8bYk7Q==.
Signing metadata unchanged: PackageSignatures{f1edb61 version:2, signatures:[6888906], past signatures:[]}.

| Protected private artifact | Bytes / mtime / inode, unchanged | SHA-256 before = after |
| --- | --- | --- |
| no_backup/thomas-personal-data/store.ctpd | 1246 / 1788586360 / 97279 | 0dfd2a464479fbdb9d230347b37ffc44a3ea090a017de02083b2c0c2a61dcd0b |
| shared_prefs/ct-v2-15-device-gate.xml | 65 / 1788586340 / 114513 | 3325d2a819fdd8062c2cdc48a09b995c9b012915bcdf88b1cf9742a7f057c793 |
| files/profileInstalled | 24 / 1788586350 / 108624 | 4f80c4b2cad6d3044bfcb1b2af6892596e9dc8072500276776df78dad4630334 |

Complete inventory, hashes, sizes, mtimes, inodes and required installation metadata match. Original app/corpus were not launched, reset, modified, migrated, exported, decrypted or removed. Atime is not claimed.

Binary manifests verify separate package/test/provider identities, exact instrumentation target and no sharedUserId. Diagnostic UID 10293 and restored-candidate UID 10295 are distinct from original 10666, with separate Context/data and Android UID-scoped key namespaces. Observed hardwareBacked=true is not hardware attestation.

Diagnostic APK: 31,900,184 bytes, SHA-256 ef046c85e1184ae9f354db235cfcc9f42a332a7bd9838460da1495ca9bb274e6.
Diagnostic test APK: 2,600,843 bytes, SHA-256 ca5a1d44f79243c9e444800d1b32d08cb653c3391cd84ee10021bb7b31498007.
Restored candidate fixture APK: 31,564,640 bytes, SHA-256 17bde74cb3e4c24699eddcd56425c27ee20c4a2e7026067289f2de72f5d0e551.
Restored test APK: 2,462,970 bytes, SHA-256 048abed235c0cd891b7458d704934cdecb3a38d1f590980fe7ac13e8b53e7b68.

Both disposable packages were removed after each run; continuous logcat helper was stopped. Temporary production instrumentation is absent from the preserved candidate. Reconstructed workspace, archives and raw diagnostic artifacts are deliberately retained as authorized failure evidence. Canonical main is clean; no informational dangling objects were deleted.

## Canonical evidence and return

All new work remains under ignored out/ct-v2-15r1-practical/:

- workspace/: reconstructed full candidate, intentionally retained.
- candidate/: verified source/evidence snapshot.
- candidate.patch / candidate.zip / candidate-manifest.json / candidate-artifacts.json: reviewable preserved candidate and exact identities.
- candidate/docs/qualification/CT-V2-15R1-PRACTICAL-EVIDENCE/DIRECT-ADJUDICATION.md: exact state, rule predicates and oracle lineage.
- candidate/docs/qualification/CT-V2-15R1-PRACTICAL-EVIDENCE/physical-decision-comparison.json and checkpoint-84-boundaries.txt: actual mode/decision/render boundary comparison.
- candidate/docs/qualification/CT-V2-15R1-PRACTICAL-EVIDENCE/automated-summary.json: exact software results, with build/suite/lint/XML/source inventories alongside.
- candidate/docs/qualification/CT-V2-15R1-PRACTICAL-EVIDENCE/run-01/ and run-02/: physical captures, manifest identities and custody.
- candidate/docs/qualification/CT-V2-15R1-PRACTICAL-QUALIFICATION.md: this report.
- final-repository-checks.txt: final unchanged HEAD/tree, clean main, upstream, absent tag, no push, root/integrity and package cleanup.

Return the established renderer verification-opening exhaustion blocker to the Principal. Do not change the correct Practical expectation or procedural rules to obtain a pass. No integration, seal, rigorous end-to-end campaign or next-phase work occurred.
