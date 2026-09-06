# CT-V2-15R1 production observation and coverage contracts

This document describes implemented runtime behavior. [The authorized order](../work-orders/CT-V2-15R1-PRODUCTION-EVIDENCE-PROCEDURE-BIOGRAPHER-INTEGRATION.md) controls scope; [qualification](../qualification/CT-V2-15R1-QUALIFICATION.md) distinguishes JVM proof from physical observation. CT-V2-04/05/07/09/10/11/12/13/14 policy catalogs and their clinical restrictions remain authoritative.

## Therapy ownership

`ThomasViewModel.submit` sends exact typed text and user-selected support/memory/privacy preferences to `ThomasProductionRuntime.submit`. It does not construct procedural state or select an intervention. The broad ordinary-scope checkbox was removed; the legacy runtime declaration enum remains compatible but its ordinary value establishes no safety fields. The default declaration is UNSPECIFIED.

The production flow is current observation → ephemeral procedure snapshot → CT-V2-12 source-first capture/admission and language processing → CT-V2-04 safety evaluation → unchanged CT-V2-05 evaluator → CT-V2-11 pre-turn retrieval → CT-V2-12 memory gate → CT-V2-13 validated reference rendering → final artifact. The runtime also evaluates the identical safety input for safety rendering. Those two evaluations are deterministic and grant no second safety authority.

`ProductionTherapyInputBoundary` is an ephemeral observer, not a policy evaluator. It stores the declared concern, literal tentative restatement, confirmations/corrections, readiness/options/choice/plan/outcome, pending information, active selected route, and delivered-action history. It never reads or writes the longitudinal corpus. `PolicyEvidence.reported` means resolved **as reported**, not independently verified. Tentative literal restatements remain TENTATIVE until the user confirms the delivered verification.

Only successful final text or a governed NO_RESPONSE records a delivered action. A failed renderer cannot mark a summary delivered, acknowledge a correction, or create a pending confirmation. The stagnation direction choice retains the underlying unresolved information requirement; a bare Yes cannot confirm an old interpretation after that direction choice.

Material revisions compare typed resolutions and normalized values, excluding submission IDs, evidence-reference IDs, and action-history bookkeeping. Case, repeated whitespace, and terminal punctuation do not themselves advance evidence. Unsupported paraphrases remain unclassified; their source text is captured, but they establish neither semantic equivalence nor novelty. A supported new detail or correction changes the typed state. Summary delivery and correction acknowledgement are explicit session events and may also change the next evaluated state.

There is no durable procedure engine, no reconstruction of a prior plan from guesses, and no inference that discussing an action means attempting it. Runtime close/process death loses procedure and safety declarations. Successful source correction/privacy/deletion clears ephemeral procedure, safety observations, pending Biographer plans, and session memory. This deliberately conservative custody boundary may require fresh current declarations even when a different source was edited. It prevents a deleted premise from surviving in procedural state. Transcript cleanup remains separate existing product debt.

## Supported typed procedure replies

These are bounded ordinary-language declarations, not a general language-understanding claim. Capitalization and terminal punctuation are tolerated for fixed commands. Payloads are retained literally. Prefixes and the plan delimiter are intentionally explicit. Unsupported prose is still committed as user source text; it does not manufacture procedural facts.

| Input / context | Observation |
| --- | --- |
| I want to begin | Explicit engagement; concern remains unknown; existing route can invite expression/problem description |
| My specific concern is: … | User-declared specific concern and boundedness, engagement, new content; non-Listen routes form only a literal tentative restatement |
| What I haven't explained is: … | An explicitly identified missing-information subject |
| The missing detail is: …, after the missing-piece question | Clear that pending missing piece and form a tentative literal concern-plus-detail restatement |
| Yes / Yes, that's right, after delivered verification | User confirmation of that tentative restatement |
| No, that's not what I mean | Withdraw the current interpretation and dependent practical assumptions; select the existing correction action |
| What I mean is: …, after correction acknowledgement | Replacement literal meaning, still tentative until confirmed |
| Yes, that's right, after reflection | Reflection received; existing policy can invite further expression |
| Another detail is: … | Append an explicitly supplied additional detail unless its normalized wording is already represented |
| That's all for now / I have finished explaining | Expression complete; existing Listen summary becomes eligible |
| I can influence: …, after influence question | Explicitly reported influenceable part |
| I cannot influence this problem | Existing non-influenceable boundary, no new technique |
| I am willing to act, after readiness question | WILLING_TO_ACT as explicitly reported; willingness merely to discuss options does not establish willingness to act |
| My options are: email; call | User-generated options, only at the pending options stage |
| I choose: email | Must match exactly one of the user's established options |
| My first step is: email the manager; when: tomorrow morning | Explicit step plus time, only after user selection; outcome remains unknown |
| I attempted the plan / I partly attempted the plan / I have not attempted the plan | Existing attempt-status enum, only while waiting for a reported outcome |
| What happened was: …, after outcome-review question | User's exact report is captured; the pending review is marked reviewed. CT-V2-05 does not derive a success/failure diagnosis or a new plan from it |
| Pause / Please pause / I want to pause | Existing silence/pause action |
| Stop / Please stop / I want to stop | Existing close action |
| I don't want to discuss this / I do not want to discuss this / I don't want to continue | Explicit reluctance/refusal |
| I want to continue / Let's continue / I am ready to resume | Explicit reengagement; current support remains a user declaration |

Multi-line procedural blocks must begin with a supported current procedural declaration or an exact safety declaration. A block beginning with a historical/quoted/unknown header does not get mined for procedural commands on later lines. This is a conservative syntax boundary, not a semantic classifier of arbitrary quotation.

Changing the support chip changes only route preference. The unchanged CT-V2-05 evaluator selects the actual route and action. New concerns invalidate dependent procedure; they do not imply influenceability, willingness, options, a plan, or an outcome.

## Safety observation

`ProductionSafetyObservationBoundary` accepts only explicit whole-line declarations at the beginning of a current block, or short replies to the exact CT-V2-04 clarification actually delivered. It never searches stored source text, diagnoses, predicts risk, or adds a question to the CT-V2-04 catalog.

The existing seven clarification questions can be answered one at a time. Yes/No binds to the pending field, not to another Therapy question. I don't know leaves that field UNKNOWN. I decline to answer records USER_DECLINED. A No to the compound adult/supported-scope question cannot establish which part failed; it stays UNKNOWN rather than inventing an age.

Direct declarations supported for synthetic qualification:

| Field | Explicit examples |
| --- | --- |
| Current emergency | There is no current emergency. / This is a current emergency. |
| Acute medical emergency | There is no acute medical emergency. / There is an acute medical emergency. |
| Self-harm relevance | Self-harm is not relevant now. / Self-harm is relevant now. |
| Harm-to-others relevance | Harm to others is not relevant now. / Harm to others is relevant now. |
| Specialized scope | I report no specialized condition for this conversation. / I report a specialized condition for this conversation. |
| Population | I am an adult in the supported setting. / I am not an adult. |
| Presenting scope | My present concern is one bounded ordinary personal problem. / My present concern needs specialized support. / My present concern is outside ordinary support. |

A declaration is DIRECT_USER_REPORT with a turn/line/declaration reference. Prefixing an exact declaration with `I am unsure: ` yields TENTATIVE; `I decline to state: ` yields USER_DECLINED; `I withdraw: ` removes it to UNKNOWN. `Correction: ` explicitly replaces the prior declaration. Opposing established declarations without correction become CONTRADICTORY with both references, and cannot issue an ordinary permit. Short replies retain the delivered requirement in their evidence reference and are accepted only in Therapy; a Biographer answer cannot answer a pending Therapy safety question. UNKNOWN itself carries no fabricated provenance, as required by CT-V2-04; the committed user source records the actual withdrawal wording.

Unrecognized text cannot establish absence. Declarations apply to the current runtime session until corrected, contradicted, withdrawn, or invalidated by lifecycle/close. There is no background persistence or replay of safety status. Arbitrary risk-like English outside this bounded observation grammar remains unclassified. This limitation must be included in testing and prohibits clinical/release claims.

## Cross-mode safety scope

Biographer already exposes CT-V2-10's BLOCKED_BY_SAFETY_SCOPE contract. Current explicit declarations in Biographer and authoritative current Therapy safety decisions can now interrupt its questioning through that contract. Emergency/specialized/external-support decisions and contradictions latch an interruption; an ordinary gate decision clears it. Source capture remains possible, but the interrupted historical question is no longer treated as pending and no new question is rendered. This adds no crisis text or clinical response.

Journal has no admitted safety-interruption input or cross-mode response policy in its existing capture/response contracts. Journal policy is unchanged. The missing Journal interruption mapping is reported as an authority/product gap, not filled with an invented protocol. Historical text retained in Journal is never reinterpreted as current safety by retrieval.

## Biographer coverage and answers

`ProductionBiographerPipeline.coverageEvidence` and the synthetic qualification pipeline use the same `GovernedBiographerCoverage.derive` implementation extracted from the qualified CT-V2-10 supplier. It consumes the eligible formed state and governed reader snapshot: represented periods, roles, places, relationships, sparse time intervals, unresolved event times/identities/corrections/contradictions, open evidence questions, and governed coverage topics.

The existing `DeterministicBiographerCoverageEngine` still owns ranking, eligibility, target selection, and the one-question plan. Production tries TARGETED_COVERAGE and uses OPEN_STORY when no eligible target exists. Typing Open story explicitly requests open narrative. The UI and renderer never choose a target.

One oldest eligible witness per represented year supplies temporal-gap boundaries; repeating a dated historical report cannot change a target merely by creating another source ID. This is coverage projection, not an assertion that different reports/events are identical. Fixed-date repeated text does not manufacture answer progress. Temporal prompt wording binds to the selected plan's bounds and preserves approximation/year precision. Non-temporal wording binds to eligible target entity labels or exact eligible grounding-source excerpts. If no supported grounding can name the selected subject, the runtime declines to render that prompt instead of substituting a generic invented subject.

The runtime retains the delivered target and material token through the answer. Admission uses that plan and preserves BIOGRAPHER_GUIDED_TIMELINE provenance. A lifecycle/material-grounding change invalidates the pending target; later prose is captured as open narrative rather than falsely attributed as an answer to a stale question.

Skip, Later/defer, I decline, This topic is private, Stop, and Change topic are operational outcomes, not extracted biography facts. They do not create sources merely by being commands. Actual private answer content is captured privately and not processed into ordinary evidence. Materially changed structural evidence can yield ANSWERED_RELEVANT; other admitted evidence is ANSWERED_OTHER_EVIDENCE; unsupported/unchanged input does not mark a gap covered. A response is not deemed relevant merely because it is nonblank.

Only explicit private/declined coverage control is durable, through CT-V2-07 ChangeCoverage. It has no assertion source for a refusal (`DECLINED_IS_NOT_EVIDENCE`). Controls are applied to the original target ID before selection; they do not generate replacement ordinary targets. Answered coverage is rebuilt from actual eligible evidence, not a persisted prompted/answered counter. Operational skip/defer/offer history and the pending question disappear on reopen. An unanswered or deferred gap can be asked in a new session; an explicitly private/declined target remains excluded. Independent coverage controls remain until reset even if their former grounding disappears; absent grounding produces no target.

## Retrieval and rendering integration

Current declared concern supplies lexical/entity anchors. Exact quotation using `Please recall my earlier words: …`, with the user's explicit-recall control enabled, can identify a single eligible source for the already-admitted CT-V2-11 EXPLICIT_SOURCE_RECALL path. Ambiguous, private, or absent matches yield no source anchor. There is no new lexical-recall policy and no inference that a similar-looking source is the one intended.

Journal and Biographer evidence can support the action already selected by current CT-V2-05 state. Historical evidence cannot choose the route, establish current safety, or become instructions. The renderer receives only bounded surfaced support.

Two narrow renderer repairs were required by these newly exercised paths: non-attempt review no longer says “you tried”; explicit source recall preserves the required unknown-event-time marker while retaining the user's exact approximate historical wording. No validator, model configuration, therapeutic action, or policy catalog was loosened.

## Physical qualification fixture

`tools/ct-v2-15r1-device-fixture.init.gradle` overrides only the build-time application ID to `com.conundrum.thomas.v2.ctv215r1fixture`; canonical Android configuration on disk is unchanged. A distinct Android UID gives the fixture separate private storage and AndroidKeyStore access. The new instrumentation refuses to submit to any other package and requires an initially empty synthetic corpus. It neither resets nor restores a corpus.

Run the two named methods only after the fixture-install/data-custody boundary is authorized. Run method a first; force-stop only the fixture package; then run b to test real process reopening. Do not run the predecessor destructive instrumentation suite against the valued canonical installation. Compilation is not device qualification.
