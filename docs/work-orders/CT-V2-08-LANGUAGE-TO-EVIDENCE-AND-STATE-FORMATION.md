CONUNDRUM THOMAS V2
IMPLEMENTATION WORK ORDER

CT-V2-08 — LANGUAGE-TO-EVIDENCE & STATE FORMATION
INCLUDING ANDROID STUDIO VCS ROOT REPAIR

PRINCIPAL DISPOSITION

CT_V2_07_ACCEPTED
CT_V2_07_GOVERNED_LONGITUDINAL_STORE_SEALED
CT_V2_08_AUTHORIZED
NEXT_PHASE_NOT_AUTHORIZED


======================================================================
1. PURPOSE
======================================================================

CT-V2-08 establishes the first governed path from ordinary committed user
language into structured longitudinal evidence and derived state.

CT-V2-07 proved that properly structured evidence can be admitted, revised,
persisted, replayed, and audited without unauthorized mutation.

CT-V2-08 must now prove that Thomas can conservatively interpret a bounded
class of ordinary language while preserving epistemic distinctions such as:

- what the user explicitly said happened;
- what the user believes about themselves;
- what the user believes or infers about another person;
- what is uncertain;
- what is approximate;
- what is contradictory;
- what may refer to the same entity but remains unresolved;
- what Thomas may derive as a provisional observation;
- what Thomas does not understand well enough to promote at all.

This phase is fundamentally about PERCEPTION and STATE FORMATION.

It is not a therapy expansion.

It is not retrieval.

It is not conversation orchestration.

It is not a license for an LLM to become the Psychographer.

It is not production psychological-data authority.

The successful result is a synthetic-only pipeline:

    committed source text
        -> conservative language perception
        -> typed evidence proposals
        -> deterministic validation
        -> CT-V2-07 governed admission
        -> inspectable derived longitudinal state

Anything that cannot safely make that journey must remain unresolved,
unclassified, or source-only.


======================================================================
2. ACCEPTED ENTRY BASELINE
======================================================================

Canonical repository:

    C:\Android Studio Projects\ConundrumThomasV2

Required branch:

    main

Required starting HEAD:

    2c1ea59bd804de297cfe1f441236ab101c90e217

Required starting tree:

    e0d0226f85eca603ed511d05a501ee2d09e04fd8

Required annotated tag:

    ct-v2-07-governed-longitudinal-store

Required tag object:

    c6e88c97d424897664e012f461a3a79b181a5acb

Required tag target:

    2c1ea59bd804de297cfe1f441236ab101c90e217

Required worktree:

    clean

Required remote:

    none

Required forward plan:

    docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md

Required size:

    25,031 bytes

Required SHA-256:

    bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886

Before implementation:

1. Verify every baseline fact above.
2. Verify CT-V2-07 tag type and target.
3. Verify all 24 V1 migration components remain DENIED.
4. Verify the forward-plan hash.
5. Verify no root fdp.txt exists.
6. Verify the worktree is clean.
7. Run the existing qualification suite before changing behavior.

If the baseline differs, STOP.

Do not reset, stash, repair, normalize, or reinterpret unexpected repository
state without reporting it.


======================================================================
3. PRELIMINARY REPAIR — ANDROID STUDIO VCS ROOT
======================================================================

This repair is part of CT-V2-08 qualification and occurs before substantive
implementation.

Observed Principal-facing symptom:

    Invalid VCS root mapping

for:

    C:\Android Studio Projects\ConundrumThomasV2

CT-V2-07 execution temporarily operated with Git metadata named:

    .git-ct-v2-07-work

before restoring it to:

    .git

That practice is now prohibited.

----------------------------------------------------------------------
3.1 Establish canonical Git-root health
----------------------------------------------------------------------

Verify from the exact project root:

    git rev-parse --show-toplevel
    git rev-parse --git-dir
    git rev-parse --is-inside-work-tree
    git status --porcelain=v1
    git fsck --full --strict

Required result:

    show-toplevel =
    C:/Android Studio Projects/ConundrumThomasV2

The canonical repository metadata must be available through:

    C:\Android Studio Projects\ConundrumThomasV2\.git

A normal .git directory is expected for this repository.

If .git is absent, STOP.

Do not reconstruct Git from dangling objects or another repository.

Verify that no temporary Git metadata directory remains, including patterns
such as:

    .git-ct-v2-*
    .git-work
    .git-temp
    git-metadata-backup

No such directory may remain beside .git.

----------------------------------------------------------------------
3.2 Inspect Android Studio VCS configuration
----------------------------------------------------------------------

Inspect local project configuration including:

    .idea\vcs.xml

if present.

For this repository the intended Git mapping is the project itself:

    $PROJECT_DIR$

with VCS:

    Git

A conventional representation is:

    <mapping directory="$PROJECT_DIR$" vcs="Git" />

Do not introduce machine-specific absolute VCS paths when $PROJECT_DIR$ is
sufficient.

If the existing mapping names a stale location, nested directory, former
repository root, or no-longer-valid VCS root, repair the local IDE mapping.

Do not begin tracking .idea merely to fix this issue.

Preserve the repository's existing IDE-file tracking policy.

----------------------------------------------------------------------
3.3 Clear stale IDE state conservatively
----------------------------------------------------------------------

Do not delete the complete .idea directory as a first response.

Do not invalidate unrelated Android Studio configuration.

Repair only the VCS mapping necessary to make the canonical repository root
recognizable.

After repair, reload the project/VCS state.

If a restart of Android Studio is necessary because the running IDE cached the
period during which .git was absent, that is acceptable.

Do not claim the issue fully resolved until either:

A. the builder can verify that Android Studio no longer reports
   "Invalid VCS root mapping";

or

B. all on-disk Git and IDE mappings are proven correct and the completion
   report explicitly says:

       VCS_MAPPING_REPAIR_IMPLEMENTED
       PRINCIPAL_IDE_RESTART_CONFIRMATION_REQUIRED

Do not alter application source merely to silence an IDE warning.

----------------------------------------------------------------------
3.4 Permanent repository-metadata invariant
----------------------------------------------------------------------

From CT-V2-08 onward:

NEVER rename, move, hide, replace, temporarily relocate, or redirect the
canonical .git metadata directory as an ordinary build technique.

In particular, do not repeat:

    .git -> .git-ct-v2-XX-work

while Android Studio has the project registered.

Do not use alternate --git-dir metadata copies to conduct normal phase work.

Normal Git branches, commits, tags, and ordinary Git worktrees are permitted
when genuinely required, but the canonical repository must remain a valid Git
root throughout.

Add a lightweight qualification/check mechanism if useful so future phases can
prove:

    repository root == expected Android Studio project root
    canonical Git metadata exists
    no stale temporary Git directory exists

The mechanism must not hard-code a username.

Record the repair and invariant in project documentation.


======================================================================
4. GOVERNING CT-V2-08 ARCHITECTURAL RULE
======================================================================

RAW PROSE IS NOT FORMULATION.

RAW PROSE IS NOT THERAPEUTIC POLICY INPUT.

RAW PROSE IS NOT DURABLE DERIVED PSYCHOLOGICAL STATE.

The ordered conceptual boundary is:

    SOURCE
      -> PERCEPTION
      -> EVIDENCE
      -> STATE FORMATION
      -> later formulation/policy stages

No downstream ordinary therapeutic policy introduced in CT-V2-03/04/05 may
begin accepting raw user prose.

Policy continues to consume governed structured state only.

The language interpreter may identify what language appears to assert.

It may not decide what therapeutic action should follow.


======================================================================
5. MODEL AUTHORITY
======================================================================

CT-V2-08 DOES NOT AUTHORIZE A GENERATIVE MODEL TO OWN PERCEPTION.

The governing V2 principle remains:

    Thomas owns behavioral and psychological authority.
    The generative conversational model is subordinate.

Do not add an LLM, remote model, local GGUF, adapter, prompt-driven
Psychographer, or model-authored evidence path merely because natural-language
interpretation is difficult.

The first CT-V2-08 implementation must establish a conservative,
deterministic language-to-evidence authority that can say:

    UNDERSTOOD
    PARTIALLY_UNDERSTOOD
    AMBIGUOUS
    UNSUPPORTED
    NO_EVIDENCE_PROPOSAL

when appropriate.

Handling less language correctly is preferable to handling more language by
guessing.

If a future model-assisted perception adapter is desired, it must be a
separately authorized subordinate proposal mechanism and must still possess
zero admission authority.

That decision is not opened by this order.


======================================================================
6. REQUIRED MODULE BOUNDARY
======================================================================

Preserve existing V2 dependency rules.

Implement the minimum pure-Kotlin authority needed for language perception and
state formation.

Preferred placement is within the existing engine architecture, with clear
package boundaries equivalent to:

    perception
    stateformation

Create new Gradle modules only if doing so materially improves compile-enforced
authority separation.

Do not create one module per class.

Required conceptual dependency:

    language perception
        -> longitudinal domain types where necessary

    state formation
        -> perception results
        -> longitudinal read contracts
        -> longitudinal admission contract

Neither may depend on:

- Android UI;
- Compose;
- Room;
- JNI;
- llama.cpp;
- speech;
- renderer implementation;
- network;
- V1 controller code;
- model implementation.

The longitudinal domain module must remain persistence-independent.

The CT-V2-07 governed admission controller remains the only path by which
new durable evidence/state enters its store.


======================================================================
7. COMMITTED SOURCE FIRST
======================================================================

Source evidence must exist independently of successful interpretation.

For a synthetic committed source:

1. preserve the original source text exactly;
2. admit the source through CT-V2-07;
3. run perception against that exact source revision;
4. bind every accepted extractive candidate to that revision;
5. retain exact source-span provenance where applicable;
6. form derived state only from admitted evidence.

If perception fails, the source still exists.

If state formation fails, the source and accepted direct evidence still exist.

No perception failure may destroy or rewrite source content.

No derived state may become the canonical copy of what the user said.


======================================================================
8. SOURCE-SPAN GROUNDING
======================================================================

Every extractive proposition must be capable of identifying why the parser
believes the source supports it.

Introduce a governed source-span representation containing sufficient
information to establish at least:

- source revision ID;
- start/end offsets or equivalent deterministic location;
- exact source fragment or its governed reference;
- source-revision fingerprint;
- extraction rule/version;
- normalization performed, if any.

Whitespace or normalized representations may aid comparison, but the original
source remains authoritative.

A candidate whose claimed span does not match the source must be rejected.

A derived interpretation may depend on several evidence objects but must still
ultimately resolve to admitted source or explicit user correction.


======================================================================
9. REQUIRED EPISTEMIC CLASSES
======================================================================

Perception must structurally distinguish at least:

EXPLICIT_USER_ASSERTION

    A proposition the user directly states as fact or recollection.

EXPLICIT_SELF_REPORT

    A direct report of the user's own present or past internal experience.

SELF_BELIEF

    What the user explicitly believes, judges, or says about themselves.

USER_INTERPRETATION

    The user's explanation, causal belief, judgment, or inference.

THIRD_PARTY_REPORT

    What the user reports another person said or claimed.

ENTITY_REFERENCE

    A mention of a person/place/organization/etc. whose identity may or may
    not be resolved.

EVENT_REFERENCE

    Something represented by the user as occurring, with honest temporal
    uncertainty.

CORRECTION_CANDIDATE

    Language that explicitly revises something previously supplied by the
    user.

UNCERTAINTY_MARKER

    Language such as "maybe", "I think", "probably", "I'm not sure",
    "I don't remember exactly", etc., where semantically applicable.

NEGATION

    Negative propositions must remain negative and must not be normalized into
    their positive form.

NONASSERTIVE_LANGUAGE

    Questions, hypotheticals, counterfactuals, examples, quotations,
    speculation, jokes, or other material that cannot safely be promoted as
    asserted fact.

AMBIGUOUS

    Language for which the parser cannot conservatively establish the intended
    proposition or referent.

The implementation may use more precise classes.

It may not collapse these distinctions.


======================================================================
10. PARTICULARLY IMPORTANT SEMANTIC BOUNDARIES
======================================================================

The following distinctions require explicit qualification.

"I was angry yesterday."

    May support a user self-report concerning the user's experience.
    It does not prove a psychiatric condition or enduring trait.

"I think Sam was angry."

    Is the user's interpretation of Sam.
    It is not direct evidence of Sam's internal state.

"Sam told me he was angry."

    Is a report that Sam made a statement.
    It is not equivalent to Thomas independently knowing Sam was angry.

"I always screw things up."

    Is evidence that the user said/believed a generalization.
    It is not proof of a recurring objective pattern.

"Maybe I moved there around 2012."

    Must retain both epistemic and temporal uncertainty.

"If I'd stayed at that job, I'd probably be miserable."

    Is counterfactual/speculative.
    It must not become an event.

"Did I tell you that I moved in 2012?"

    Is interrogative and must not automatically become a factual assertion
    merely because a proposition appears inside the question.

"Actually, it was 2013, not 2012."

    May become a correction only when its target can be deterministically
    identified.
    Otherwise it remains an unresolved correction candidate.

"Sam hates me."

    Is ordinarily a user interpretation about Sam's internal state, not
    direct fact.

"I feel like nobody likes me."

    Must not become the factual proposition that nobody likes the user.

These examples are representative, not a lexical template list.


======================================================================
11. TEMPORAL INTERPRETATION
======================================================================

Use the CT-V2-06 temporal model.

Language perception must preserve:

- exact;
- date-only where supported;
- approximate;
- range;
- relative;
- ongoing;
- uncertain;
- unknown

temporal forms without manufacturing precision.

Examples:

    "around 2012"
        != 2012-01-01

    "when I was about ten"
        != a fabricated birthday-relative date unless sufficient governed
           evidence actually supports that calculation

    "a few years later"
        != an invented calendar year when the reference point is unresolved

    "for a while"
        != an exact duration

Relative-time resolution may occur only where the reference frame is explicit
and deterministic.

The parser must prefer UNKNOWN/RELATIVE over invented precision.


======================================================================
12. ENTITY AND REFERENCE FORMATION
======================================================================

Detect and represent possible entities conservatively.

Required behaviors include:

- repeated name does not prove same identity;
- pronoun resolution may remain unresolved;
- "my brother Sam" and "Sam from work" must not merge automatically;
- aliases require evidence;
- two possible "Sam" references may coexist;
- entity resolution is separate from mention extraction;
- later evidence can revise identity decisions without rewriting source.

Language perception may propose identity candidates.

Only the governed identity mechanisms established in CT-V2-07 may make or
revise durable identity decisions.


======================================================================
13. CONSERVATIVE STATE FORMATION
======================================================================

Implement deterministic state formation from admitted evidence.

This layer is allowed to organize evidence.

It is not allowed to invent psychological truth.

At minimum, the derived state should be able to represent:

- current explicit assertions;
- current self-reports;
- current self-beliefs;
- current user interpretations;
- current entities/events/periods;
- unresolved references;
- contradictions;
- contested/corrected material;
- currently ineligible/private material;
- open evidentiary questions produced by structural uncertainty;
- dated runtime-state evidence where explicitly supported.

State formation may also produce narrowly defined structural observations such
as:

    REPEATED_REPORTED_OCCURRENCE

when multiple independent admissible sources actually contain comparable
reports.

Such a result must not silently become:

    TRAIT
    DISORDER
    CAUSE
    MOTIVE
    DEFENSE
    ATTACHMENT_STYLE
    PERSONALITY_TYPE

No causal psychological interpretation is authorized merely because similar
language recurs.


======================================================================
14. ONE-OFF VERSUS RECURRENCE
======================================================================

The system must not call something a pattern merely because:

- the user uses "always";
- one event is emotionally intense;
- one source contains repeated wording;
- Thomas previously said the same thing;
- several derived objects all descend from one source.

If recurrence is represented, qualification must show that the supporting
evidence comes from genuinely distinct admissible source records or distinct
qualified events.

The number and independence requirements must be explicit and deterministic.

Do not invent clinically meaningful thresholds.

Prefer a neutral structural label such as:

    recurrence candidate

or:

    repeated reported occurrence

until later formulation authority is established.


======================================================================
15. CONTRADICTION FORMATION
======================================================================

State formation may identify a contradiction only where the relevant
propositions are sufficiently comparable.

A contradiction:

- preserves both claims;
- records the comparison basis;
- remains unresolved by default;
- does not imply deception;
- does not choose the newest statement automatically;
- does not silently downgrade older evidence;
- may become an open investigation question.

Temporal scope must be considered.

For example:

    "I lived in Seattle in 2010."
    "I lived in Portland in 2018."

is not inherently contradictory.

Likewise:

    "I loved that job at first."
    "By the end I hated that job."

may represent a trajectory rather than contradiction.

Qualification must prove these distinctions.


======================================================================
16. USER CORRECTION
======================================================================

Explicit correction remains higher authority than Thomas-derived
interpretation.

Language perception may recognize a possible correction.

It may only execute a correction through CT-V2-07 when:

- the speaker authority is valid;
- the target is deterministically identified;
- the correction content is grounded;
- temporal semantics remain honest;
- no unrelated claim is overwritten.

Ambiguous corrections must remain unresolved.

Example:

    "No, that was 2014."

without an unambiguous target must not mutate some convenient earlier date.


======================================================================
17. PRIVATE / DECLINED BOUNDARY
======================================================================

Preserve all CT-V2-07 privacy semantics.

The language engine may not infer PRIVATE merely because content appears
sensitive.

The language engine may not infer consent merely because the user discussed a
topic.

Explicit product/user commands governing privacy remain separate authority.

DECLINED remains absence of coverage, not negative factual evidence.

PRIVATE evidence remains unavailable to derived state formation.

A source returning from PRIVATE to otherwise eligible status remains subject
to CT-V2-07 review requirements and must not silently reactivate.


======================================================================
18. STATE SNAPSHOT
======================================================================

Add a deterministic inspectable state snapshot suitable for qualification.

It should identify, at a minimum:

- snapshot/store revision;
- contributing source/evidence IDs;
- active explicit claims;
- user interpretations;
- contradictions;
- unresolved identities;
- unresolved corrections;
- structural recurrence candidates if implemented;
- open evidentiary questions;
- excluded/ineligible dependencies;
- derivation rule/version information.

The snapshot must be reproducible from the same admitted history.

It must not contain untraceable prose assertions.

It must not be a free-form LLM summary.

A canonical logical digest should be possible.


======================================================================
19. OPEN QUESTIONS
======================================================================

CT-V2-08 may form open questions only from deterministic evidentiary gaps.

Examples include:

- unresolved identity;
- ambiguous correction target;
- unresolved contradiction;
- unknown event time;
- competing entity references.

Do not generate therapy questions.

Do not generate probing Biographer dialogue.

Do not decide when a question should be asked.

This phase may establish:

    "this question exists because the evidence is unresolved"

but not:

    "Thomas should ask it now."


======================================================================
20. REQUIRED SYNTHETIC QUALIFICATION CORPUS
======================================================================

Build a synthetic plain-language corpus.

At minimum include cases proving:

01. "I moved to Denver in 2018."
    -> explicit user assertion + event/time.

02. "I think moving there was a mistake."
    -> user interpretation, not objective fact.

03. "I was furious yesterday."
    -> self-report, not enduring trait.

04. "I think Sam was furious."
    -> user interpretation about Sam.

05. "Sam told me he was furious."
    -> third-party report distinction.

06. "Around 2012 I changed jobs."
    -> approximate time remains approximate.

07. "Maybe it was 2012."
    -> uncertainty preserved.

08. "I don't remember when it happened."
    -> unknown time, no fabricated date.

09. "If I'd stayed, I'd probably have hated it."
    -> no factual event admitted from counterfactual.

10. "Did I move there in 2012?"
    -> interrogative proposition not automatically factual.

11. "Actually, it was 2013, not 2012."
    -> valid correction with an unambiguous target.

12. Same correction without a resolvable target.
    -> unresolved, no mutation.

13. "Sam hates me."
    -> interpretation, not Sam-state fact.

14. "I feel like nobody likes me."
    -> self-report/belief, not factual census of other minds.

15. "I always fail at relationships."
    -> self-belief/generalization, not automatically a pattern.

16. Three genuinely distinct synthetic source records containing comparable
    behavior reports.
    -> recurrence may become structural candidate if the defined rule is met.

17. Three derived objects all depending on one source.
    -> must NOT count as three independent observations.

18. Two different synthetic people named Sam.
    -> remain unresolved unless evidence permits resolution.

19. Pronoun with multiple plausible antecedents.
    -> unresolved rather than guessed.

20. Two mutually exclusive claims about the same historical fact.
    -> contradiction preserved.

21. Two apparently different claims separated by time.
    -> no false contradiction.

22. Change over time.
    -> trajectory/change representation preferred to contradiction where
       semantically justified.

23. Source revision changes wording.
    -> affected extraction/state requires review/reprocessing.

24. User correction supersedes conflicting Thomas-derived interpretation while
    preserving all history.

25. PRIVATE supporting evidence.
    -> excluded from state formation.

26. PRIVATE restored to ELIGIBLE.
    -> no silent reactivation.

27. DECLINED coverage.
    -> no substantive fact inferred.

28. Source-span offsets mismatch source.
    -> rejected.

29. Candidate cites wrong source revision.
    -> rejected.

30. Extraction says direct fact while language is explicitly speculative.
    -> rejected/downgraded.

31. A Thomas-derived claim has no ultimate admissible source.
    -> rejected.

32. Reprocessing identical source revision.
    -> deterministic/idempotent result.

33. Reprocessing after source revision.
    -> old extraction remains historical; current derivation changes
       conservatively.

34. Close/reopen/replay.
    -> identical logical state digest.

35. No input at all.
    -> valid empty state.

36. Unsupported language.
    -> safe NO_EVIDENCE_PROPOSAL rather than fabricated evidence.

37. Malformed/hostile parser input.
    -> controlled failure without store mutation.

38. Existing CT-V2-06 and CT-V2-07 fixtures remain valid.

39. CT-V2-04 therapeutic permit behavior remains unchanged.

40. CT-V2-05 route progression and anti-repetition remain unchanged.

41. Journal remains default NO_RESPONSE.

42. Biographer remains investigation-only.

43. No production/app/runtime longitudinal writer appears.

44. No model artifact or prompt is added.

45. Android Studio project root remains a valid Git root throughout final
    qualification.


======================================================================
21. ADVERSARIAL LANGUAGE CASES
======================================================================

Add explicit tests for language forms prone to accidental overclaiming:

- negation;
- double negation;
- quotations;
- reported speech;
- rhetorical questions;
- hypothetical statements;
- counterfactuals;
- sarcasm/ambiguous humor where deterministic understanding is unavailable;
- modal verbs;
- "maybe/probably/I guess";
- "always/never/everyone/no one";
- mixed first-person and third-person claims;
- multiple events in one sentence;
- multiple dates in one sentence;
- ambiguous antecedents;
- embedded corrections;
- statements whose temporal scope has changed.

The required policy is conservative failure.

UNKNOWN is a successful result when the language does not justify certainty.


======================================================================
22. NO PSYCHOLOGICAL OVERREACH
======================================================================

CT-V2-08 must not automatically infer:

- diagnosis;
- disorder;
- attachment style;
- trauma;
- repressed memory;
- unconscious motive;
- defense mechanism;
- personality type;
- causal childhood explanation;
- intent of another person;
- stable trait from one state report;
- stable trait from one event;
- stable trait merely from emphatic wording.

The distinction is structural:

    USER SAID X

is not equivalent to:

    X IS TRUE

and neither is equivalent to:

    THOMAS EXPLAINS X AS Y.


======================================================================
23. NO THERAPEUTIC POLICY EXPANSION
======================================================================

Do not change:

- CT-V2-04 permit authority;
- CT-V2-05 ordinary therapy routes;
- deterministic progression;
- anti-repetition;
- Journal response defaults;
- Biographer authority;
- Safety Governor boundaries.

Do not add therapeutic rules.

Do not let extracted keywords select therapeutic techniques.

Words such as:

    stuck
    ruminate
    conflict
    anxious
    depressed
    trauma

must not themselves constitute therapeutic routing authority.


======================================================================
24. EXPLICITLY PROHIBITED WORK
======================================================================

Do not implement:

- production Android longitudinal persistence;
- Room integration for the CT-V2-07 store;
- real-user psychological storage;
- encryption or Keystore claims;
- semantic search;
- embeddings;
- retrieval ranking;
- context packet construction;
- Biographer question selection;
- Therapy longitudinal retrieval;
- Profile UI;
- user-visible Psychographer UI;
- generative profile summaries;
- LLM extraction;
- prompt-based extraction;
- GGUF additions;
- model training;
- renderer changes unrelated to regression;
- STT/TTS changes;
- V1 controller migration;
- V1 profile migration;
- cloud services;
- telemetry;
- safety-state persistence;
- diagnosis;
- crisis-procedure expansion;
- production longitudinal authority.


======================================================================
25. DOCUMENTATION
======================================================================

Produce at least:

    docs/work-orders/
    CT-V2-08-LANGUAGE-TO-EVIDENCE-AND-STATE-FORMATION.md

    docs/
    CT-V2-08-LANGUAGE-TO-EVIDENCE-AND-STATE-FORMATION.md

    docs/architecture/
    CT-V2-08-PERCEPTION-STATE-BOUNDARY.md

    docs/longitudinal/
    CT-V2-08-EPISTEMIC-CLASSIFICATION.md

    docs/longitudinal/
    CT-V2-08-STATE-FORMATION-RULES.md

    docs/qualification/
    CT-V2-08-QUALIFICATION.md

Also record the Android Studio/Git repair, either in an existing repository
workspace document or a narrowly scoped new document.

That record must state:

    canonical project root
    canonical Git metadata location
    expected VCS mapping
    cause of the CT-V2-07 warning if established
    permanent prohibition on temporary .git relocation

Do not modify the substantive content of the canonical forward plan.


======================================================================
26. BYPASS / AUTHORITY AUDIT
======================================================================

Demonstrate:

- raw prose cannot enter therapeutic policy directly;
- raw prose cannot directly mutate durable derived state;
- every durable language-derived object enters through CT-V2-07 admission;
- every extractive object names its source revision;
- source-span claims are validated;
- derived objects ultimately resolve to admitted evidence;
- current PRIVATE evidence cannot support derived state;
- user interpretation cannot masquerade as direct fact;
- third-party report cannot masquerade as direct fact;
- Thomas inference cannot masquerade as user assertion;
- unsupported language can safely result in no evidence proposal;
- no model-facing evidence-writer exists;
- no app/runtime writer exists;
- no direct SQL/JDBC projection mutation appears;
- no V1 write authority appears;
- no therapy-policy dependency on raw prose appears.

Report counts for every discovered language-to-store path.

Intended authority:

    production language-derived writers:             0
    app/runtime language-derived writers:             0
    model-authorized evidence writers:                0
    direct raw-prose-to-policy paths:                  0
    direct raw-prose-to-projection paths:              0
    qualification governed language pipeline:          documented only
    CT-V2-07 admission controller:                     sole durable write gate


======================================================================
27. VCS-ROOT QUALIFICATION
======================================================================

At phase end, verify again:

    git rev-parse --show-toplevel
    git rev-parse --git-dir
    git status --porcelain=v1
    git fsck --full --strict

Confirm:

- root is C:\Android Studio Projects\ConundrumThomasV2;
- .git is canonical and present;
- no .git-ct-v2-* directory exists;
- Android Studio VCS mapping resolves to the project repository;
- .idea tracking policy is unchanged unless separately justified;
- no temporary Git metadata operation occurred during the phase.

The completion report must explicitly state whether the Principal-facing
Android Studio warning has been visually confirmed absent.

If not directly observable, state exactly that rather than claiming it.


======================================================================
28. COMPLETE BUILD QUALIFICATION
======================================================================

From the final implementation state run, at minimum:

    gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain

Required:

- BUILD SUCCESSFUL;
- all prior tests pass;
- all CT-V2-08 tests pass;
- zero skipped CT-V2-08 acceptance tests;
- lint has zero new errors;
- debug APK assembles;
- unsigned release APK assembles;
- APKs contain no fixture corpus or longitudinal qualification database;
- no generated database is tracked;
- no model artifact is tracked;
- no raw/restricted source material is tracked;
- no temporary Git metadata directory exists;
- application backup remains disabled;
- canonical forward-plan hash is unchanged;
- V1 register remains deny-by-default;
- all 24 V1 components remain DENIED;
- no app/runtime longitudinal store integration exists;
- no production language authority exists;
- final worktree is clean;
- git fsck --full --strict succeeds.

Record APK SHA-256 values.

Do not claim:

- device qualification;
- clinical qualification;
- production privacy qualification;
- real-language population coverage;
- human-level language understanding;
- therapeutic efficacy;
- production longitudinal authority.


======================================================================
29. STOP CONDITIONS
======================================================================

STOP rather than broaden authority if:

- the Git baseline differs;
- .git cannot be established as the canonical project repository;
- repairing the VCS warning would require destructive repository surgery;
- the canonical forward-plan hash changes unexpectedly;
- a model appears necessary to meet the bounded acceptance corpus;
- language must be guessed rather than conservatively classified;
- raw prose must enter therapeutic policy;
- an unsupported inference would have to be persisted;
- source evidence would have to be rewritten;
- user interpretation would have to be treated as direct fact;
- temporal precision would have to be invented;
- ambiguous identity would have to be forcibly resolved;
- a direct store bypass would have to be introduced;
- Android production persistence would have to be opened;
- real user data would be required;
- existing therapeutic qualification regresses;
- a test must be weakened or skipped to obtain a pass.

A smaller but epistemically honest language surface is the preferred outcome.


======================================================================
30. COMMITS AND TAG
======================================================================

Use small reviewable commits.

A suitable sequence is:

1. Repair and document stable Android Studio/Git root.
2. Establish CT-V2-08 perception/state contracts.
3. Implement conservative language perception.
4. Implement deterministic state formation.
5. Add adversarial and longitudinal qualification corpus.
6. Record final qualification.

Do not squash meaningful history.

On full qualification create:

    ct-v2-08-language-evidence-state-formation

as an annotated tag targeting the exact final qualified HEAD.

Do not add a remote.

Do not push.


======================================================================
31. REQUIRED COMPLETION REPORT
======================================================================

Report:

DISPOSITION
- COMPLETE / PARTIAL / BLOCKED.

REPOSITORY
- starting/final HEAD;
- starting/final tree;
- branch;
- commit list;
- tag name/object/target/type;
- remote;
- push status;
- worktree state.

VCS REPAIR
- original condition found;
- root returned by Git;
- canonical .git location;
- stale temporary Git directories found/removed;
- .idea/vcs mapping before/after where applicable;
- whether Android Studio warning was visibly confirmed absent;
- confirmation that .git was never relocated during CT-V2-08.

FORWARD PLAN
- path;
- before/after bytes;
- before/after SHA-256;
- redundant copy count.

LANGUAGE ARCHITECTURE
- module/package graph;
- parser/perception authority;
- source-span contract;
- epistemic classes;
- temporal treatment;
- ambiguity treatment;
- entity-reference treatment;
- correction treatment.

STATE FORMATION
- state types formed;
- recurrence rules if any;
- contradiction rules;
- open-question rules;
- dependency requirements;
- canonical state digest/replay behavior.

AUTHORITY
- model evidence writers;
- app/runtime evidence writers;
- raw-prose-to-policy paths;
- direct projection mutation paths;
- V1 writers;
- production writers.

QUALIFICATION
- exact clean command;
- task count;
- test count;
- new CT-V2-08 test count;
- failures/errors/skips;
- adversarial corpus results;
- replay/determinism results;
- lint;
- APK hashes;
- artifact scan;
- Git integrity.

LIMITATIONS
- bounded deterministic language understanding;
- synthetic-only;
- no model extraction;
- no retrieval;
- no production Android store;
- no real user data;
- no production security lifecycle;
- no new therapeutic authority.

RECOMMENDATION
- identify the exact next phase from the canonical forward plan;
- state whether it is ready for Principal consideration;
- do not open it.


======================================================================
32. TARGET TERMINAL DISPOSITION
======================================================================

A fully successful phase should be able to report:

CT_V2_08_LANGUAGE_TO_EVIDENCE_STATE_FORMATION_COMPLETE
ANDROID_STUDIO_CANONICAL_VCS_ROOT_REPAIRED
CANONICAL_GIT_METADATA_STABILITY_INVARIANT_ESTABLISHED
SOURCE_FIRST_PERCEPTION_BOUNDARY_QUALIFIED
EPISTEMIC_CLASSIFICATION_BOUNDARY_QUALIFIED
CONSERVATIVE_AMBIGUITY_AND_TEMPORAL_HANDLING_QUALIFIED
DETERMINISTIC_STATE_FORMATION_QUALIFIED
ALL_DURABLE_WRITES_REMAIN_CT_V2_07_GOVERNED
RAW_PROSE_THERAPEUTIC_AUTHORITY_ZERO
LLM_EVIDENCE_AUTHORITY_ZERO
NO_PRODUCTION_LONGITUDINAL_AUTHORITY_GRANTED
NEXT_PHASE_READY_FOR_PRINCIPAL_CONSIDERATION

The next phase remains unopened until the Principal reviews the completion
report.

Do not open the next phase early.
