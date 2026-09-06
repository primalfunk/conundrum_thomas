# CT-V2-15R1 qualification — production evidence, procedure, and Biographer integration

Date: 2026-09-05. Authority: [authorized work order](../work-orders/CT-V2-15R1-PRODUCTION-EVIDENCE-PROCEDURE-BIOGRAPHER-INTEGRATION.md). Historical entry findings remain preserved in [the reconnaissance report](../reconnaissance/CT-V2-CURRENT-STATE-AND-TEST-READINESS.md).

## Physical continuation — current status, 2026-09-05

The Principal subsequently authorized the isolated physical execution. Its current disposition is **CT_V2_15R1_PHYSICAL_QUALIFICATION_FAILED**: the prepared UI test failed a base-wording assertion on an authorized reflection variant. The dependent gates remain unqualified, and missing physical pause/stagnation coverage was identified. Both fixture packages were removed; the original APK, package metadata, private-file inventory, hashes, sizes, mtimes and inodes are unchanged. Fresh canonical regression still passes all 978 tests.

See [the complete physical execution report](CT-V2-15R1-PHYSICAL-QUALIFICATION.md) for the precise failure, custody proof, cleanup and next bounded order. No completion tag was created. The implementation-stage record below is preserved as historical evidence; its pending-approval language describes the earlier stage and is superseded by this physical report.

## Implementation-stage disposition (preserved)

**CT_V2_15R1_IMPLEMENTED_DEVICE_QUALIFICATION_PENDING**

The three production bridges are implemented and qualified through the actual typed runtime boundary on the JVM. The final canonical clean build passes **978 tests in 60 suites: 946 predecessor tests plus 32 new production conversation tests, with zero failures, errors, or skips**. Lint has zero errors/fatals and 16 warnings. The isolated physical fixture and its two UI instrumentation methods compile.

Physical qualification has **not** passed and is not implied by those results. ADB authorization became available during the work, but approval of the requested disposable device fixture/custody boundary has not arrived. No installation, application launch through instrumentation, force-stop, reset, restore, uninstall, or personal-data mutation was performed on the tablet. The completion tag is deliberately absent.

This is a bounded deterministic product candidate. It supports documented explicit ordinary-language declarations and contextual replies; it does not understand arbitrary conversational prose. That limitation is part of the system under test, not hidden behind successful synthetic examples. No model, speech, new Therapy, new safety doctrine, or CT-V2-16 was implemented.

## Repository and evidence identity

| Item | Evidence |
| --- | --- |
| Canonical root | C:\Android Studio Projects\ConundrumThomasV2 |
| Git metadata | Canonical root\.git; directory in place, no relocation |
| Branch | main |
| Starting HEAD | c987fbf3e3742124408a8e6f855d82d19e82f0d1 |
| Starting tree | 2081b1868cfa9979c63baa5d333d32ace7badfac |
| Entry worktree | No staged/unstaged changes; reconnaissance report was the sole untracked file |
| Predecessor annotated tag | ct-v2-15-android-production-integration |
| Predecessor tag object / target / type | da376b133bc4cdcc5a77fb3a9eeac5e92ec88bcf / starting HEAD / tag |
| Final tested implementation HEAD | 70200a6066d3749b5945c519bfa1f9bd8edfe8b3 |
| Final tested implementation tree | c6f252121413a3c3bfe95b1ac0f9ba484bfc6cd8 |
| Remote | origin — https://github.com/primalfunk/conundrum_thomas.git |
| Locally recorded upstream | origin/main at 64fa3af9069f820fd465f219e5a21855a7d47047 |
| Ahead/behind | Entry 14/0; tested implementation 18/0; final evidence-only commit adds one ahead. No fetch or push, so these compare local upstream metadata |
| Final repository disposition | Evidence-only documentation commit follows the tested implementation; exact final HEAD/tree are reported in the completion response. Production source and tests are unchanged by that commit |
| R1 completion tag | Not created: physical gate remains pending |
| Integrity | git diff --check, canonical-root verifier, and git fsck --full --strict pass; V1 register remains 24/24 DENIED |
| Push | None |

Commits made in order:

1. 723f509612bf890285a31e69de0858893b65ec02 — preserve reconnaissance and bounded work order.
2. 98be998eec4eb2c92a7ed538e352a9a31065cd61 — production procedure/safety/coverage bridges, actual runtime acceptance tests, architecture updates, and guarded device fixture.
3. 288485ffd81f2c1df189156339239369b0eb944e — ground non-temporal Biographer prompts in eligible target evidence and isolate mode-specific short replies.
4. 70200a6066d3749b5945c519bfa1f9bd8edfe8b3 — preserve internal punctuation in material evidence; regression distinguishes 1.5 from 15.
5. Final documentation-only qualification commit — this report and preserved execution evidence.

No entry discrepancy was found. No unknown work was discarded. No reset, clean of the Git worktree, stash, historical checkout, history rewrite, or push occurred. Gradle clean was used only as explicitly ordered to rebuild generated artifacts.

The reconnaissance report is now tracked at its original canonical path, unchanged from preservation commit 723f509. Historical qualification reports and the canonical forward plan were not rewritten.

## Evidence index and scope of proof

Repository paths below are relative to the canonical root.

| Evidence | Role |
| --- | --- |
| app/src/main/java/com/conundrum/thomas/v2/ThomasAndroidCompositionRoot.kt | Canonical Android protected-runtime composition |
| app/src/main/java/com/conundrum/thomas/v2/ThomasViewModel.kt, submit | Actual UI-to-runtime turn boundary; preferences and exact committed text |
| thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ThomasProductionRuntime.kt, submitTherapy / submitBiographer / nextBiographerPrompt | Production orchestration, delivery bookkeeping, grounded targets and answer loop |
| thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ProductionTherapyInputBoundary.kt, observe / delivered / meaning | Conservative observation, ephemeral state, material revision |
| thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ProductionSafetyObservationBoundary.kt, observe / replyDeclaration | Field-specific current declarations and pending-question replies |
| thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ProductionBiographerPipeline.kt, coverageEvidence / answer | Protected-corpus coverage and governed operational outcomes |
| thomas/biographer/src/main/kotlin/com/conundrum/thomas/v2/biographer/GovernedBiographerCoverage.kt, derive | Shared eligible structural coverage projection |
| thomas/therapy-longitudinal/src/main/kotlin/com/conundrum/thomas/v2/therapylongitudinal/LongitudinalTherapyIntegrationEngine.kt, integrate | Unchanged CT-V2-12 capture, safety permit, CT-V2-05 evaluator, retrieval, and memory support |
| thomas/engine/src/main/kotlin/com/conundrum/thomas/v2/engine/ordinary/CoreOrdinaryPolicy.kt, CoreOrdinaryState.kt, CoreOrdinaryActions.kt | Unchanged ordinary policy, typed facts and outcome contracts |
| thomas/language-renderer/src/main/kotlin/com/conundrum/thomas/v2/languagerenderer/TherapyRenderCommandAdapter.kt | Narrow non-attempt/unknown-time rendering corrections; no policy selection |
| qualification/src/test/kotlin/com/conundrum/thomas/v2/qualification/CTV215R1ProductionConversationTest.kt | 32 production-boundary JVM tests |
| app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1ProductionUiInstrumentedTest.kt | Two compiled, unexecuted physical UI methods, guarded to disposable package |
| docs/architecture/CT-V2-15R1-PRODUCTION-OBSERVATION-AND-COVERAGE.md | Full supported input grammar and lifecycle/authority contract |
| CT-V2-15R1-TEST-INVENTORY.json beside this report | All 60 executed JVM suite paths, counts, outcomes, and durations |
| CT-V2-15R1-PRODUCTION-CONVERSATION-TRACES.txt beside this report | Exact synthetic stdout: user input, typed revision/action/progression/safety, externally visible result |
| CT-V2-15R1-CANONICAL-BUILD-OUTPUT.txt beside this report | Preserved final canonical command output |
| CT-V2-15R1-ARTIFACTS.json beside this report | Exact APK paths, sizes, and SHA-256 |

Every new JVM acceptance scenario obtains state by submitting user text through ThomasProductionRuntime.submit, the boundary used by Android. CTV215Harness supplies a disposable protected store and synthetic key/storage adapters, not fabricated procedure state. No acceptance test injects CoreOrdinaryTherapyState or CoverageEvidence after a turn. Runtime results expose typed observations/plans for assertions in addition to final artifacts. This proves production composition and deterministic behavior under those storage adapters; it does not prove AndroidKeyStore or physical UI execution.

## Therapy input and session architecture

The production path is:

Typed input → current supported observation → ephemeral procedure snapshot → CT-V2-12 source-first capture and CT-V2-07 admission/language processing → CT-V2-04 safety permit → existing CT-V2-05 ordinary procedure → pre-turn CT-V2-11 retrieval → CT-V2-12 surfaced support → CT-V2-13 validation and DeterministicReferenceRealizer → final artifact.

The runtime additionally evaluates the same safety input to construct an authorized safety render command. It does not bypass the CT-V2-12 permit requirement or create another policy.

The observer begins with unknown evidence. “My specific concern is: …” declares the user's specific bounded concern and engagement. This is typed as user report, not independent verification. Non-Listen support permits only a literal tentative restatement. Missing detail, explicit correction, confirmation, influenceability, willingness, options, user choice, plan, attempt, and review each require supported current text and, where applicable, the relevant delivered question.

Button state supplies a requested route preference only. It cannot establish concern, engagement, shared understanding, influenceability, readiness, options, plan, attempted action, or safety absence. The former broad ordinary-scope checkbox is removed. MainActivity includes a concise typed starting hint. No internal-state editor was added to the UI.

Ephemeral state holds active route, pending information/confirmation, delivered summary, correction acknowledgement, options/choice/plan/outcome review, and action/repetition history. These do not become durable psychological assertions. Only successfully rendered text or governed NO_RESPONSE records delivery. The undelivered-understanding regression proves that a rejected render cannot be confirmed by a later Yes.

Material revision compares typed resolutions and normalized values, excluding send IDs, evidence-reference IDs, and history bookkeeping. Case, whitespace and terminal punctuation alone do not reset stagnation. Internal punctuation, including decimal points, remains meaningful. Explicit new detail, correction, and a requested missing answer can change revision. Unsupported paraphrase is neither guessed equivalent nor guessed novel; its source may be retained while procedure remains unchanged.

Pause, stop, explicit refusal and resume change engagement through existing CT-V2-05 states. A correction withdraws the old interpretation references and clears dependent practical assumptions before replacement is tentatively formed. New concerns similarly clear dependent facts. Successful source correction/privacy/deletion invalidates current procedure, safety declarations, memory state and pending Biographer plans conservatively.

Close/process death loses procedure and safety declarations. Reopening keeps durable evidence but does not reconstruct a plan, current safety, confirmation or an attempted outcome from guesses. Tests prove runtime/store reopen on the JVM; physical process death and Android reopening remain pending.

## LISTEN: actual production progression

Test: listenProgressionUsesDeliveredActionsAndExplicitReplies. Initial current scope is explicitly declared, either through the seven field declarations documented in the architecture contract or the existing gate's sequential questions.

| User input | Existing selected action |
| --- | --- |
| I want to begin | core-invite-expression |
| My specific concern is: the delayed project meeting | core-reflect-established-content |
| Yes, that's right. | core-invite-further-expression |
| That's all for now. | core-summarize-listening |
| Thank you. | core-check-further-or-close |
| Stop | core-acknowledge-close |

Assertions require the named action and successful final disposition, not just a non-null artifact. The trace preserves the actual rendered responses. No technique or wording variation is selected by the observer.

Repeated unchanged concern reaches core-offer-direction-choice and then STOP_NO_PROGRESS. “Another detail is: the manager moved the date” changes material state and permits reflection again. The decimal-value regression separately proves that changing 1.5 hours to 15 hours is not discarded as punctuation-only equivalence.

## UNDERSTAND: clarification, confirmation and correction

Test: understandClarifiesConfirmsAndSummarizesActualUserEvidence.

| User input | Existing action / typed result |
| --- | --- |
| My specific concern is: the delayed meeting; separate line: What I haven't explained is: who changed the time | core-ask-important-missing-piece |
| The missing detail is: my manager changed the time | core-verify-tentative-understanding; interpretation remains not established |
| Yes, that's right. | core-summarize-shared-understanding |
| Thank you | core-check-understanding-next-direction; delivered-summary flag is true |

Test: correctionWithdrawsTentativeMeaningBeforeAcceptingReplacement.

A “cancelled meeting” tentative interpretation is followed by “No, that's not what I mean.” The old interpretation is withdrawn; core-acknowledge-correction is delivered. “What I mean is: the meeting was delayed” produces a new tentative verification. After confirmation, the summary contains “delayed” and excludes “cancelled.”

This is a literal governed understanding procedure. It does not infer motives, a diagnosis, causal psychological explanation, or a general formulation.

## PRACTICAL: user agency, plan and actual outcome

Tests: practicalUsesUserOptionsChoicePlanAndActualOutcome, nonAttemptIsNotRenderedAsAnAttempt, optionsReadinessAndUnchosenPlansCannotBeManufactured.

| User input | Existing action / constraint |
| --- | --- |
| I want to begin | core-ask-problem-description |
| My specific concern is: arranging the project meeting | core-verify-problem-understanding |
| Yes, that's right. | core-ask-influenceable-part |
| I can influence: when I contact the manager | core-ask-readiness-for-options |
| I am willing to act | core-invite-user-options |
| My options are: email the manager; call the manager | core-ask-user-to-choose-option; selected option still unknown |
| I choose: email the manager | core-develop-bounded-plan |
| My first step is: email the manager; when: tomorrow morning | core-wait-for-outcome; governed NO_RESPONSE; attempted outcome still unknown |
| I attempted the plan | core-review-reported-outcome |
| What happened was: the manager replied and it helped | core-consolidate-plan-learning; ATTEMPTED and REVIEWED |

The parallel non-attempt case uses “I have not attempted the plan,” then “What happened was: I did not have time.” It remains NOT_ATTEMPTED. The narrow renderer repair removes the false assumption “you tried” from review/consolidation. No new action catalog entry or technique was introduced.

“I am willing to consider options” does not establish WILLING_TO_ACT: the existing CT-V2-05 precondition is stronger. An option not among those supplied by the user is not selected. A plan discussion never becomes attempted action. An unsupported phrase such as “I have not tried it yet” does not silently populate the typed outcome; the supported explicit outcome declaration is required.

The existing outcome enum distinguishes attempted/partly attempted/not attempted. The user's helped/not helped/failed narrative is retained as source text and an explicit review event; no new success/failure diagnosis, treatment adaptation or automatic revised plan was invented.

## Safety observation and provenance

The broad ordinary adult declaration now establishes **zero** unrelated fields. The explicit emergency control, if supplied through the existing runtime contract, establishes only that emergency declaration. Ordinary Therapy requires the unchanged CT-V2-04 gate.

All seven consumed fields have separate current observation paths: current emergency, acute medical emergency, self-harm relevance, harm-to-others relevance, specialized condition, population applicability and presenting scope. Explicit declaration evidence uses DIRECT_USER_REPORT and inspectable turn/line/declaration references. Established means established as that user report under the existing contract, not independently confirmed absence of risk.

| Situation | Proven behavior |
| --- | --- |
| No relevant observation / legacy ordinary checkbox only | All fields UNKNOWN; no ordinary route decision |
| Delivered CT-V2-04 question followed by supported Yes/No | Only that pending field changes; reference identifies the exact requirement |
| No to compound adult/supported-setting question | UNKNOWN; no invented age or failed subcondition |
| I am unsure: declaration | TENTATIVE |
| I decline to state: declaration / pending I decline to answer | USER_DECLINED |
| I withdraw: declaration / pending I don't know | UNKNOWN; no invented evidence reference |
| Opposing established current declarations | CONTRADICTORY with both references; no ordinary permit |
| Correction: declaration | Explicit replacement, re-evaluated by unchanged gate |
| Historical/quoted multiline header | No mining later lines for safety declarations |
| Biographer short Yes/No while Therapy question is pending | Does not answer the Therapy safety question |
| Renderer rejects the clarification | No delivered pending-question assumption |

Tests include unknownSafetyAndBroadCheckboxNeverManufactureAbsence, declarationsRemainReportedAndCurrentConflictStopsPolicy, tentativeRefusedWithdrawnAndQuotedSafetyRemainDistinct, safetyClarificationAnswersOnlyTheDeliveredField, ambiguousSafetyPopulationNoDoesNotInventAnAge, historicalQuotedDeclarationBlockCannotEstablishCurrentSafety and biographerShortAnswerCannotAnswerAPendingTherapySafetyQuestion.

Current explicit emergency/specialized/external-support decisions and conflicts interrupt Biographer through its already-admitted BLOCKED_BY_SAFETY_SCOPE contract. No historical prompt remains pending through that interruption. An ordinary permit clears the interruption. Source capture can remain possible; no crisis text or new screening question is invented.

**Journal limitation:** existing Journal contracts provide no admitted cross-mode safety interruption mapping. Journal response policy remains unchanged. This is an explicitly reported authority/product gap under the order's instruction to report undefined handling. It is not hidden as cross-mode emergency coverage and is not necessary to fabricate an ordinary Therapy permit. General arbitrary risk-like prose outside the bounded observation grammar remains unclassified. These facts prohibit clinical/public-release claims.

## Biographer coverage, target and answer loop

ProductionBiographerPipeline.coverageEvidence now calls GovernedBiographerCoverage.derive with the actual formed state and governed protected-store reader. The qualification pipeline calls the same implementation; there is no duplicate production coverage engine.

The projection includes eligible represented periods, roles/places/relationships, temporal gaps, unresolved identities/event times/corrections/contradictions, open evidence questions and governed coverage topics. Private/ineligible sources are excluded. The existing CT-V2-10 engine owns deterministic ranking, selected target and one-question plan.

Temporal gap boundaries use one oldest eligible witness per represented year so a duplicate dated report cannot replace a boundary simply by acquiring a new source ID. This is structural projection, not inferred identity of events. Non-temporal prompt grounding uses eligible selected target labels or source excerpts; an unsupported subject produces no fabricated generic prompt.

Tests groundedTargetAndMaterialAnswerChangeCoverageAndNextQuestion and unresolvedIdentityPromptNamesItsActualEligibleTarget establish:

1. Journal user input “I moved to Denver in 2010.” and “I moved to Portland in 2018.” is admitted through runtime.submit.
2. Production Biographer selects a **non-null TEMPORAL_GAP**, and the rendered question includes both 2010 and 2018.
3. User answers “I moved to Seattle in 2014.”
4. Capture is BIOGRAPHER_GUIDED_TIMELINE; outcome is ANSWERED_RELEVANT; materialEvidenceChanged is true.
5. A different non-null target is selected. Reopening derives a different target from the stored corpus, not a persisted answered counter.
6. Separately, “I think Alex was angry” and “Alex told me he was worried” produce an unresolved identity target. The question names Alex, asks about the same person, and contains exactly one question. “I don't know” does not resolve that identity or create material progress.

The delivered target/material token survives only until its legitimate answer/lifecycle boundary. Source deletion or changed grounding invalidates the pending target. Subsequent input is open narrative rather than falsely tagged as an answer to a stale question. Source revision from 2014 to 2015 rebuilds prompt bounds using 2015 and excludes the obsolete year.

| Answer type | Operational/evidence result |
| --- | --- |
| Material relevant dated answer | Existing answer outcome; structural coverage and subsequent target change |
| Other admitted evidence | ANSWERED_OTHER_EVIDENCE when it does not change the selected target |
| Unsupported/non-answer | NO_EXTRACTABLE_EVIDENCE or applicable bounded outcome; no invented coverage |
| Exact repeated eligible dated report | No manufactured material progress; original gap survives reopen |
| Skip | SKIPPED; no source merely for the command; current-session suppression only |
| Later/defer | DEFERRED; not permanent evidence; may return in a later session |
| I decline | Durable governed DECLINED coverage control, no assertion that the topic happened |
| This topic is private / private answer | PRIVATE control excludes that target; private source content remains ineligible |
| Stop | STOPPED; no further question until explicit open-story continuation |
| Correction/source revision | Recompute from current eligible evidence; stale target is not retained |

Only explicit private/declined controls persist through CT-V2-07 ChangeCoverage. Operational prompt history, unanswered status and skip/defer counters are ephemeral. Private/declined target controls survive reopen and do not generate variant replacement questions. Controls whose former grounding disappears do not generate new targets, though the independent control can remain until reset.

The production parser does not promise to resolve every CT-V2-10 target kind from unrestricted prose. Temporal answers, unresolved-identity questioning, explicit controls, changes and corrections are proven as described; broader semantic coverage remains a campaign target.

## Cross-mode longitudinal evidence

**Journal → Therapy:** journalRecallIsVisibleAndRetainsProvenance submits the dated Denver source through production Journal, reopens the protected store, then submits a current Listen concern and explicit source-recall request through production Therapy. The result selects core-reflect-established-content, surfaces JOURNAL provenance, and visibly names Journal and Denver.

**Biographer → Therapy:** biographyRecallPreservesSourceUncertaintyAndCurrentRoute first obtains a real targeted question, answers “Around 2014 I changed jobs,” then requests current Listen recall. The surfaced memory retains BIOGRAPHER_GUIDED_TIMELINE, exact approximate wording, and bounded uncertainty. The selected Therapy action remains reflection chosen from current state. Making the source private removes it from subsequent surfaced biography memories; deletion removes the approximate claim from formed state. Source correction separately rebuilds Biographer target dates from current revisions.

Explicit source recall uses “Please recall my earlier words: …” plus the existing user explicit-recall control. It matches exactly one eligible source; ambiguity, private evidence or missing text yields no anchor. Ordinary entity/lexical anchors use the current concern, not an unrelated safety declaration at the beginning of the turn. No new semantic-search policy was added.

The full predecessor CT-V2-09/11/12/14/15 suites continue to cover Journal custody, retrieval eligibility, source correction/privacy/deletion, provenance, historical instruction inertness and route/safety separation. New tests supplement them rather than replacing those assertions. Historical evidence cannot set current safety or select an ordinary route. Assistant questions and responses are never automatically committed as user evidence.

**Observed language debt:** the preserved Journal recall trace includes an additional generic attributed phrase, “an earlier eligible account,” alongside the exact real excerpt. Source-record event time remains UNKNOWN even when a derived assertion contains a date, so the conservative source-recall output can also sound awkward (“I am not sure of the event time”). The new adapter preserves uncertainty rather than inventing a source timestamp. These are visible renderer fidelity/quality limits for the campaign, not proof of general natural language quality.

## Acceptance matrix A–N

All rows below pass on the JVM through production input. Physical repetitions remain pending.

| Required scenario | Primary test / semantic oracle |
| --- | --- |
| A LISTEN | listenProgressionUsesDeliveredActionsAndExplicitReplies — six distinct authorized stages and delivery results |
| B UNDERSTAND | understandClarifiesConfirmsAndSummarizesActualUserEvidence — missing piece, tentative state, explicit confirmation, delivered summary |
| C PRACTICAL | practicalUsesUserOptionsChoicePlanAndActualOutcome — user options/choice, plan, genuine wait, actual reported attempt and review |
| D CORRECTION | correctionWithdrawsTentativeMeaningBeforeAcceptingReplacement — withdrawn references and corrected final wording |
| E PAUSE/RELUCTANCE | reluctancePauseAndResumeAreExplicit — pause, resume, refusal and close actions |
| F STAGNATION | unchangedAndStylisticEvidenceReachStagnationAndNewDetailReleasesIt — same revision, direction choice, STOP_NO_PROGRESS |
| G NEW EVIDENCE | Same test plus internalNumericPunctuationCannotCollapseDifferentEvidence — supported detail/numeric change advances material revision |
| H SAFETY UNKNOWN | unknownSafetyAndBroadCheckboxNeverManufactureAbsence — seven UNKNOWN observations, no permit |
| I SAFETY CONFLICT | declarationsRemainReportedAndCurrentConflictStopsPolicy — report origins/references, conflict and no ordinary decision |
| J BIOGRAPHER TARGET | groundedTargetAndMaterialAnswerChangeCoverageAndNextQuestion — real non-null temporal target with grounded years |
| K BIOGRAPHER ANSWER | Same test — ANSWERED_RELEVANT, real change, different next target and reopen |
| L BIOGRAPHER DECLINE/PRIVATE | declineAndPrivacySurviveReopenWithoutPsychologicalEvidence — excluded target and no source created by refusal command |
| M BIOGRAPHER → THERAPY | biographyRecallPreservesSourceUncertaintyAndCurrentRoute — guided provenance, approximate event, current route, privacy/deletion |
| N JOURNAL → THERAPY | journalRecallIsVisibleAndRetainsProvenance plus retained CT-V2-15 lifecycle tests — visible eligible recall after reopen |

Additional new tests cover non-attempt wording, unsupported choice/readiness, no facts from buttons, process-state loss, explicit safety clarification, quoted historical declarations/procedure, stale targets, repeated biography, deferral/stop, current Biographer interruption, custody invalidation, failed-render delivery, correction rebuilt coverage, selected identity grounding, and cross-mode short replies. The inventory records all 32.

## Mechanical authority audit

Counts refer to production executable paths/definitions unless explicitly described as textual references. Audited 93 production Kotlin files under app/thomas/platform src/main. Static searches are evidence of wiring, not a formal proof of all semantic noninterference; runtime tests and unchanged policy diffs supply the complementary evidence.

| Requested path / fact | Count / evidence |
| --- | --- |
| Canonical Android production composition root | 1, ThomasAndroidCompositionRoot; one protected Android open path |
| Production Therapy input/state formation path | 1, therapyInput.observe callsite in submitTherapy; one ephemeral observer |
| Unsupported manufactured procedural fact assignments | 0 found in reviewed observer: values come from explicit reports, literal tentative restatement or delivered session events; defaults UNKNOWN |
| Safety fields created without inspectable report/declaration authority | 0 found; seven independent field paths, unknown has no invented origin; tested broadly declared scope establishes none |
| Production CT-V2-05 evaluator path | 1, LongitudinalTherapyIntegrationEngine line 74, guarded by ordinaryTherapyPermit |
| Ordinary production route choices | Existing 3: LISTEN, UNDERSTAND, PRACTICAL; no added route |
| Production Biographer real coverage supplier | 1 canonical coverageEvidence → shared derive |
| Production targeted Biographer selection engine | 1 DeterministicBiographerCoverageEngine instance in ProductionBiographerPipeline |
| Qualification-only coverage construction sites | 39 textual CoverageEvidence constructor occurrences in qualification main/tests and Biographer tests; not production routes. Qualified pipeline now shares one derive supplier |
| UI → Therapy technique selection | 0 evaluator/action-selection references in app sources; user support preference is not a technique |
| UI → Biographer target selection | 0 engine references in app sources; mode/open-story choice is not target ranking |
| UI → direct admission/mutation requests | 0 LongitudinalAdmissionRequest/LongitudinalWriteOperation/store.admission references; custody surfaces call governed runtime APIs |
| Renderer → policy | 0 policy evaluation calls; adapter describes an already-selected action/target |
| Renderer → persistence | 0 protected-store/admission references in renderer |
| Model-backed realizers | 0; DeterministicReferenceRealizer remains canonical |
| Model → Therapy policy / safety / longitudinal writes | 0 / 0 / 0 |
| Model artifacts / remote inference / LM Studio | 0 / 0 / 0; APK and production source checks below |
| Response → automatic user evidence | 0; only committed user turns or explicit user custody commands reach admission |
| Private unsurfaced memory → renderer | 0 supported paths; eligibility checks precede source/target grounding and CT-V2-12 surfaced support |
| V1 therapeutic production paths | 0; zero V1 imports in production scan, 24/24 migration entries DENIED |
| Speech implementation / Pattern Engine authority | 0 / 0; reserved boundaries unchanged |

Representative mechanical searches: therapyInput.observe in runtime; fun coverageEvidence in production runtime; therapyPolicy.evaluate in CT-V2-12; CoreOrdinaryTherapyEvaluator/CoreOrdinaryActions/DeterministicBiographerCoverageEngine in app; protected-store/admission references and evaluation calls in renderer; non-v2 Thomas imports over all production Kotlin files.

A Git diff against c987fbf shows **no changes** in thomas/safety, thomas/engine, thomas/longitudinal-admission, thomas/journal, thomas/retrieval, thomas/therapy-longitudinal, thomas/personal-data-persistence, platform model/speech implementation, app/build.gradle.kts, or gradle/libs.versions.toml. The Biographer change extracts/supplies coverage and connects outcomes; it does not change the CT-V2-10 ranking policy. Renderer changes are narrow command grounding/wording repairs; validators and realization authority remain unchanged.

V1 verification: migration/v1-component-register.json defaultApprovalState=DENIED, v1CodeMigrationAuthorized=false, components=24, DENIED=24. Retained tests include CoreOrdinaryInvariantTest “V1 migration authority remains zero,” CT-V2-11/12 register scenarios, and BiographerAuthorityQualificationTest.acceptance72V1MigrationRegisterRemainsTwentyFourDenied.

## Regression and build execution

Final canonical command, run from the repository root at tested implementation commit 70200a6:

~~~powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
~~~

Result: **BUILD SUCCESSFUL in 1m 54s; 393 actionable tasks, 393 executed**. Java used the installed Android Studio JBR (25.0.2). Configuration cache reused; --rerun-tasks still executed all 393 actionable tasks. Provenance database generation completed from tracked inputs into ignored generated storage.

| Metric | Entry baseline | Final |
| --- | --- | --- |
| JVM suites | 59 | 60 |
| JVM tests | 946 | 978 |
| New tests | — | 32 |
| Failures / errors / skipped tests | 0 / 0 / 0 | 0 / 0 / 0 |
| Lint errors / fatals | 0 / 0 | 0 / 0 |
| Lint warnings | 17 | 16 |
| Physical new instrumentation | Not qualified | 2 compiled methods, 0 executed |

No predecessor tests were removed. CT-V2-15 successful Therapy fixtures now explicitly supply current evidence rather than relying on the broad checkbox. The predecessor targeted Biographer assertion now seeds real dated history and requires a non-null TEMPORAL_GAP and guided provenance; it no longer accepts open narrative as equivalent to targeted coverage. Its fixture turn indices remain chronological. No existing acceptable-result enum was broadened.

The retained suite estate includes ordinary/safety invariant tests, admission/ontology/language evidence, Journal, Biographer, retrieval/longitudinal Therapy, renderer adversarial/authority, persistence/custody/security, Android runtime/lifecycle qualification, provenance and app unit tests. See the full 60-suite inventory for exact module paths/counts.

Longest final suites: CT-V2-15 lifecycle/authority 9 tests, 23.928 seconds; LanguageEvidenceBoundaryQualificationTest 7 tests, 4.699 seconds; CT-V2-11 acceptance 96 tests, 2.677 seconds; JournalAuthorityQualificationTest 15 tests, 2.397 seconds. No test skips or failures were reported. This order did not establish a new coverage percentage or claim absence of latent/flaky behavior from one complete run.

Lint warnings: RedundantLabel 1, AndroidGradlePluginVersion 2, GradleDependency 1, NewerVersionAvailable 3, ObsoleteSdkInt 2, UnusedResources 7. No new suppression or dependency upgrade was used. Gradle's lintVitalRelease task is SKIPPED by the existing build arrangement; explicit lintDebug runs and reports the values above. Task skipping is distinct from zero skipped JVM tests.

Focused qualification was also run after each substantive correction:

~~~powershell
.\gradlew.bat :qualification:test --tests '*CTV215R1ProductionConversationTest' --console=plain
~~~

The final focused run passed, 38 actionable tasks (4 executed, 34 up-to-date), 15 seconds. Earlier focused failures during development were used to correct wiring/grounding and truthful rendering; they are not relabeled as passing historical baselines.

Additional final checks:

~~~powershell
git diff --check
powershell -NoProfile -File tools/verify-canonical-git-root.ps1
git fsck --full --strict
~~~

All pass. Root verifier reports canonical project root, .git, TEMPORARY_GIT_METADATA_COUNT=0 and VCS_ROOT_ON_DISK_VALID=true. No repair was needed.

## APK artifacts and provenance

| APK | Bytes | SHA-256 |
| --- | ---: | --- |
| Canonical debug | 31,548,244 | 8888a1f0bc43e86873650d824fc56615a6420aacf013a41074724f07da998b45 |
| Canonical unsigned release | 24,279,311 | 1c1fb35f56301162777a1bf5f412318e599f360953d704fb909000bfb657569a |
| Disposable fixture debug | 31,548,256 | 3fa8c91211961457287c640fcc40b46605ea2aad784d1830940bf63beedbc729 |
| Disposable fixture androidTest | 2,382,590 | a6608e4b99ac5dc2e37bcc03d2cd6c0b928928fbbef7a36d2547fc2046f7dd2e |

Paths are in the adjacent artifact inventory. Canonical APKs are under app/build/outputs/apk; fixture copies under ignored out/ct-v2-15r1-fixture survive the subsequent canonical Gradle clean.

The unsigned release META-INF/version-control-info.textproto records revision 70200a6066d3749b5945c519bfa1f9bd8edfe8b3. It does not claim the later evidence-only documentation commit. Changed release hashes across source/HEAD builds are expected; no byte-reproducibility claim is made.

aapt2 dump badging confirms canonical package com.conundrum.thomas.v2, version 1.0/code 1, minSdk31/targetSdk37/compileSdk37, ABIs arm64-v8a/armeabi-v7a/x86/x86_64. Only the application's DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION appears. INTERNET and RECORD_AUDIO are absent.

Both canonical APK ZIP scans found zero model-directory/artifact matches (models directory or .gguf/.safetensors/.onnx/.tflite/.pte). DEX scans found zero markers for llama_cpp, llama.cpp, lmstudio, api.openai, or org/tensorflow/lite. These scans supplement the composition/source proof; marker absence alone is not claimed as a general security theorem.

## Physical device and safe fixture

Read-only ADB observations:

| Field | Observed |
| --- | --- |
| Serial / state | BC9424B4E3C5002 / device |
| Model / product / device | TCL 9491G / 9491G_ZZ / Hera_Vis_WIFI |
| Android / API / ABI | 15 / 35 / arm64-v8a |
| Build fingerprint | TCL/9491G_ZZ/Hera_Vis_WIFI:15/AP3A.240905.015.A2/2FA6:user/release-keys |
| Canonical installed package | com.conundrum.thomas.v2, versionName1.0/versionCode1 |
| Existing first install / last update | 2026-09-04 21:17:05 / 2026-09-04 22:31:39 (device package metadata) |
| Disposable fixture installed | No |
| R1 physical tests executed | None |

The reconnaissance had observed unauthorized ADB. This changed to authorized during implementation; device-access availability must not be confused with approval to exercise a valued corpus. The Principal was asked to approve a separate disposable fixture preserving existing Thomas data. No answer has arrived. The blocker is now the device fixture/custody approval gate, not an unauthorized ADB connection.

tools/ct-v2-15r1-device-fixture.init.gradle overrides only the build-time applicationId to com.conundrum.thomas.v2.ctv215r1fixture. Canonical Android configuration on disk is unchanged. Android package/UID separation provides an independent private-file and AndroidKeyStore scope. Instrumentation asserts the exact fixture package before any user submission and requires an empty starting fixture corpus; it never resets unknown data.

Prepared build command:

~~~powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat -I tools/ct-v2-15r1-device-fixture.init.gradle :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
~~~

Final fixture build passes in 6 seconds, 133 actionable tasks: 41 executed and 92 up-to-date. Its output metadata confirms the separate applicationId. The following execution sequence is prepared, **not performed**, and applies only after explicit custody approval:

1. Verify fixture package absent or independently confirmed disposable; verify APK hashes above.
2. Install the separate fixture APK and test APK. Do not replace the canonical Thomas package.
3. Run only CTV215R1ProductionUiInstrumentedTest#aTypedProductionConversationsAndCrossModeRecall.
4. Force-stop only com.conundrum.thomas.v2.ctv215r1fixture.
5. In a separate instrumentation invocation, run #bColdReopenPreservesCorpusButRequiresCurrentSafety.
6. Preserve instrumentation output, exact APK/device/build identity, semantic observations and restart result. Do not run predecessor reset/restore instrumentation on the canonical package.

Method a uses the actual Compose text input/send button/mode and support controls to exercise Listen, Understand/correction, Practical/non-attempt, real targeted biography and changed target, both cross-mode recalls, and safety unknown/conflict. Method b checks durable corpus after process reopening while procedure/current safety reset. JVM tests already assert typed state/actions; compiled UI checks assert visible behavior and corpus state. They may reveal additional Android/UI defects and are not presumed to pass.

The bounded disposition therefore means implemented and JVM-qualified, with physical qualification still unproven. There is no claim that the installed canonical app contains R1, no claim of R1 physical end-to-end acceptance, and no completed R1 tag.

## Qualification tokens and preserved authorities

Within the explicit typed grammar and JVM scope documented above, evidence supports:

~~~text
PRODUCTION_THERAPY_EVIDENCE_STATE_BRIDGE_QUALIFIED
LISTEN_PRODUCTION_PROGRESSION_QUALIFIED
UNDERSTAND_PRODUCTION_PROGRESSION_QUALIFIED
PRACTICAL_PRODUCTION_PROGRESSION_QUALIFIED
MATERIAL_EVIDENCE_REVISION_QUALIFIED
PRODUCTION_STAGNATION_GUARD_QUALIFIED
PRODUCTION_CORRECTION_STATE_QUALIFIED
PRODUCTION_RELUCTANCE_PAUSE_STATE_QUALIFIED
SAFETY_OBSERVATION_PROVENANCE_QUALIFIED
ABSENCE_OF_OBSERVATION_NOT_ABSENCE_OF_RISK_QUALIFIED
CURRENT_CONVERSATION_SAFETY_CONFLICT_QUALIFIED
PRODUCTION_BIOGRAPHER_COVERAGE_EVIDENCE_QUALIFIED
PRODUCTION_BIOGRAPHER_TARGET_SELECTION_QUALIFIED
BIOGRAPHER_ANSWER_OUTCOME_LOOP_QUALIFIED
BIOGRAPHER_PRIVATE_DECLINED_TARGET_BOUNDARY_QUALIFIED
JOURNAL_TO_THERAPY_PROVENANCE_PRESERVED
BIOGRAPHER_TO_THERAPY_PROVENANCE_QUALIFIED
RENDERER_POLICY_AUTHORITY_ZERO
MODEL_POLICY_AUTHORITY_ZERO
MODEL_ARTIFACTS_ZERO
SPEECH_IMPLEMENTATION_AUTHORITY_ZERO
PATTERN_ENGINE_AUTHORITY_ZERO
~~~

CT_V2_04_SAFETY_AUTHORITY_PRESERVED; CT_V2_05_ORDINARY_THERAPY_AUTHORITY_PRESERVED; CT_V2_07_ADMISSION_AUTHORITY_PRESERVED; CT_V2_09_JOURNAL_AUTHORITY_PRESERVED; CT_V2_10_BIOGRAPHER_AUTHORITY_PRESERVED; CT_V2_11_RETRIEVAL_AUTHORITY_PRESERVED; CT_V2_12_LONGITUDINAL_THERAPY_AUTHORITY_PRESERVED; CT_V2_13_RENDERER_AUTHORITY_PRESERVED; CT_V2_14_PERSONAL_DATA_GOVERNANCE_PRESERVED; CT_V2_15_ANDROID_RUNTIME_AUTHORITY_PRESERVED.

These statements do not confer physical, clinical or release authority. RIGOROUS_TESTING_PRECURSOR_COMPLETE and the preferred fully complete phase token are **not** asserted while the mandatory physical gate remains open.

## Limitations and remaining debt

| Item | Kind / significance | Effect on this order and testing |
| --- | --- | --- |
| R1 physical scenarios unrun | Qualification; high | Blocks full phase seal; bounded pending disposition used |
| Exact supported input grammar, not general language understanding | Product/research; high | Rigorous tests must distinguish unsupported prose from incorrect observation. Arbitrary paraphrase must not be described as supported |
| Safety grammar does not classify arbitrary risk-like language | Safety/scope qualification; high | No clinical or public release claim; test supported declarations, unknowns, conflicts and inert history without adding doctrine |
| Journal cross-mode interruption contract absent | Authority/product; high for release | Reported per order; no invented mapping. Requires separate authority decision before representing all modes as safety-interruptible |
| Readiness to consider options is not willingness to act | Existing procedure/input boundary | Explicit stronger declaration needed by existing CT-V2-05; do not change policy to make a weaker answer pass |
| Success/failure narrative has no newly invented clinical outcome category | Existing policy envelope | Preserve source and review event; no automatic treatment/plan adaptation |
| Procedure and current safety lost on process death/custody changes | Intentional lifecycle boundary | Reopen evidence persists, procedure requires fresh current input; physical proof pending |
| Skip/defer can return after reopen | Intentional ephemeral history | Durable decline/private differs; test across sessions |
| General CT-V2-10 targets may lack supported answer extraction/grounding | Product/qualification | Proven temporal/identity cases do not imply arbitrary narrative resolution; unsupported grounding yields no invented prompt |
| Renderer generic attributed phrase and awkward unknown-time wording | Product/fidelity | Visible in preserved trace; dedicated semantic/quality track must evaluate it |
| Oversized/unsupported render-command construction can be classified by existing generic runtime catch as persistence unavailable | Existing error reporting debt, observed during qualification development | Do not equate every runtime error with failed storage. Long-input/error-path stress remains for bounded repair/testing, no general hardening absorbed |
| Existing restore rollback, reset partial-failure reporting, transcript cleanup, SAF read bounds, custody concurrency and failed-open UX | Preserved implementation/product debt | Out of R1 scope as ordered; prioritize privacy/persistence campaign with disposable fixtures |
| No model, STT/TTS, Pattern Engine, new techniques, clinical source admission or release hardening | Explicit authority limit | Remain closed. No model/speech performance or clinical efficacy claim |

The governing real-world procedure remains the already-admitted CT-V2-05 / bounded PM+ derived ordinary sequence and existing CT-V2-04 safety/scope authority. Source review/admission statuses were not changed. No clinical sources were added, surveyed sources promoted, or V1 therapeutic implementation imported.

## Testing-readiness adjudication and immediate next order

The missing production state/coverage connections are now real and produce interpretable deterministic acceptance results. A broad new architecture or Pattern Engine phase is not justified as a prerequisite. The smallest remaining gate is the prepared disposable physical qualification, with any defects it actually reveals handled within bounded integration scope before sealing R1.

After that gate, the dedicated rigorous campaign should run in this order:

1. **Architectural and observation invariants (automated first):** production composition, zero model/renderer/UI policy authority, declaration provenance, unknown/conflict handling, unsupported paraphrase and quoted instruction boundaries, material revision and delivery failure.
2. **Longitudinal/privacy correctness (automated plus disposable device):** Journal/Biographer/Therapy crossover, months of synthetic history, temporal uncertainty, correction/supersession, identity ambiguity, private/declined coverage, deletion/invalidation, restart and source attribution. Include the retained custody debt as explicit failure probes.
3. **Therapeutic procedure and safety boundary (automated corpora):** all existing routes/stages, non-influenceability, refusal/resume, changed direction, repeated sessions, stagnation, supported new evidence, attempted/non-attempted review. Use only admitted CT-V2-04/05 behaviors.
4. **Adversarial semantic and language quality (automated oracles plus human review):** unsupported facts, overcertainty, diagnosis-like output, technique drift, private memory, historic instructions, attribution, approximate time, repetition, response length and the observed generic-quote issue. There is no model track until separately admitted.
5. **Device robustness/performance and custody (isolated fixture):** actual input/mode controls, close/process death/reboot, permission/offline state, storage interruption and feasible low-memory pressure; cold start, typed end-to-end latency, memory/storage growth, practical battery/thermal measurements. STT/TTS and model TTFT/generation rate are out of the present envelope.
6. **Controlled human usability observation:** synthetic/disposable histories first; measure whether people can understand the current supported input and response behavior, recover from unknown/unsupported answers, and exercise correction/privacy controls. Evaluate usability and behavioral quality, not clinical efficacy.

Automation can start within the explicit deterministic envelope. The physical gate and its custody approval remain prerequisites to full R1 acceptance; they are not silently waived by the recommendation to test.

CTV2_RIGOROUS_TESTING_READY_WITH_BOUNDED_PREREQUISITES

Recommended next order: CT-V2-15R1 isolated physical-device qualification, followed by the Conundrum Thomas V2 rigorous deterministic end-to-end testing campaign.

Principal decision required: Approve installation and execution of the prepared disposable fixture package on TCL 9491G while preserving the existing canonical Thomas installation and corpus.
