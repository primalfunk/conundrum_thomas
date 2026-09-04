CONUNDRUM THOMAS V2
IMPLEMENTATION WORK ORDER

CT-V2-09 — JOURNAL CAPTURE ENGINE


PRINCIPAL DISPOSITION

CT_V2_08_ACCEPTED
CT_V2_08_LANGUAGE_EVIDENCE_STATE_FORMATION_SEALED
CT_V2_09_AUTHORIZED
NEXT_PHASE_NOT_AUTHORIZED


======================================================================
1. PURPOSE
======================================================================

Implement the first complete governed Journal capture engine for Conundrum
Thomas V2.

Journal is Thomas's ordinary present-facing evidence intake surface.

Its job is simple in concept:

    let the user record life
    preserve exactly what was committed
    attach honest provenance
    feed the common longitudinal evidence system
    optionally permit a restrained Journal response
    otherwise stay out of the way

Journal is not Therapy.

Journal is not Biographer.

Journal is not the Psychographer.

Journal does not decide what the user's writing "really means."

Journal does not have to respond.

The successful CT-V2-09 result must establish a deterministic, inspectable,
synthetic-only Journal pipeline:

    committed journal entry
        -> Journal capture authority
        -> immutable/revisioned source evidence
        -> CT-V2-08 language-to-evidence processing
        -> CT-V2-07 governed admission
        -> deterministic longitudinal state consequences
        -> optional bounded Journal response intent

The source entry is primary.

Interpretation is secondary.

Response is optional.

Capture must succeed independently of response generation.


======================================================================
2. ACCEPTED ENTRY BASELINE
======================================================================

Canonical repository:

    C:\Android Studio Projects\ConundrumThomasV2

Required branch:

    main

Required starting HEAD:

    0ea9df3131f1da31ca3479d530fd96eeb71910ee

Required starting tree:

    5172149c0c6ad96bcf8224bfd8e82cd005bb561f

Required annotated tag:

    ct-v2-08-language-evidence-state-formation

Required tag object:

    1cb6412c89be1c0eef92c60c17fe28de9dd59e77

Required tag target:

    0ea9df3131f1da31ca3479d530fd96eeb71910ee

Required worktree:

    clean

Required remote:

    none

Required forward plan:

    docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md

Required forward-plan size:

    25,031 bytes

Required forward-plan SHA-256:

    bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886

Before implementation:

1. Verify HEAD, tree, branch, tag object, tag target, worktree, and remote.
2. Verify the canonical forward-plan size and SHA-256.
3. Verify all 24 V1 migration components remain DENIED.
4. Verify no root fdp.txt exists.
5. Verify no redundant forward-plan copy exists.
6. Run:

       tools\verify-canonical-git-root.ps1

7. Verify:

       git rev-parse --show-toplevel
       git rev-parse --git-dir
       git status --porcelain=v1

8. Required Git state:

       root:
       C:/Android Studio Projects/ConundrumThomasV2

       metadata:
       .git

       temporary Git metadata directories:
       0

9. Run the existing CT-V2-08 qualification before changing behavior.

If the entry baseline differs, STOP.

Do not reset, stash, repair, move Git metadata, or reinterpret an unexpected
repository state without reporting it.


======================================================================
3. CLOSE THE ANDROID STUDIO VCS UI LOOP
======================================================================

CT-V2-08 established that the repository itself is healthy.

The remaining prior qualification statement was:

    VCS_MAPPING_REPAIR_IMPLEMENTED
    PRINCIPAL_IDE_RESTART_CONFIRMATION_REQUIRED

Before substantive CT-V2-09 implementation, record whether Android Studio now
visibly recognizes the project as a valid Git repository after reload/restart.

If the builder has access to the running IDE and can observe this directly:

    confirm the prior "Invalid VCS root mapping" warning is absent.

If direct IDE observation is unavailable:

    do not claim visual confirmation.

Record:

    VCS_ROOT_ON_DISK_VALID
    IDE_VISUAL_CONFIRMATION_AVAILABLE = true/false
    IDE_INVALID_VCS_WARNING_OBSERVED = true/false/unknown

If the warning is still visibly present while:

    git rev-parse --show-toplevel
    git rev-parse --git-dir
    .idea/vcs.xml

are all correct, investigate Android Studio's cached VCS registration only.

Do not move, rename, hide, substitute, or redirect .git.

The CT-V2-08 canonical Git metadata stability invariant remains permanent.


======================================================================
4. JOURNAL PRODUCT CONTRACT
======================================================================

Journal is a writing surface first.

The central object is:

    JOURNAL ENTRY

not:

    chat message

not:

    therapy turn

not:

    interview answer

not:

    profile update

A Journal entry may describe:

- something occurring now;
- something that happened earlier;
- a memory;
- a thought;
- a feeling;
- a decision;
- a relationship;
- a belief;
- an uncertainty;
- an ordinary mundane event;
- something the user merely wants recorded.

Journal must not require psychological significance.

The fact that the user chose Journal does not mean:

    "analyze me"
    "help me"
    "challenge me"
    "ask me questions"
    "begin therapy"
    "investigate my history"

The default Journal behavior remains:

    NO_RESPONSE


======================================================================
5. REQUIRED JOURNAL RESPONSE POSTURES
======================================================================

Implement the Journal response preference as an explicit typed authority.

Required postures:

    NO_RESPONSE
    REFLECT
    ASK_ONE_QUESTION

Aliases presented later in UI may use friendly labels such as:

    Silent
    Reflect
    Ask one question

but the internal contract must remain explicit and deterministic.

----------------------------------------------------------------------
5.1 NO_RESPONSE
----------------------------------------------------------------------

NO_RESPONSE is the default.

Required behavior:

- commit the entry;
- process evidence normally where permitted;
- produce no Thomas response plan;
- make no renderer/model call;
- create no assistant turn;
- create no placeholder response;
- create no empty response row;
- create no hidden "acknowledgement";
- do not interpret silence as an error.

A silent Journal entry is a fully successful Journal interaction.

----------------------------------------------------------------------
5.2 REFLECT
----------------------------------------------------------------------

REFLECT authorizes a small Journal response posture.

It does not authorize Therapy.

The semantic plan may:

- acknowledge a salient part of the current entry;
- paraphrase or lightly mirror what the user explicitly expressed;
- recognize emotion only where directly supported by the current entry;
- reflect uncertainty using matching uncertainty;
- remain brief.

It must not:

- diagnose;
- formulate a therapeutic hypothesis;
- challenge cognition;
- offer treatment technique;
- infer hidden motives;
- tell the user what they "really" feel;
- retrieve old history merely to appear insightful;
- open a Biographer investigation;
- ask multiple questions;
- turn every entry into a psychologically important event.

The response plan should be marginal to the entry, not the center of the
interaction.

----------------------------------------------------------------------
5.3 ASK_ONE_QUESTION
----------------------------------------------------------------------

ASK_ONE_QUESTION authorizes exactly one useful Journal question.

The question must:

- be grounded in the committed current entry;
- be understandable without hidden context;
- be non-leading;
- avoid presupposing an unproven psychological interpretation;
- avoid Therapy technique selection;
- avoid Biographer-style systematic gap pursuit;
- be optional in tone;
- contain one actual question.

The engine may choose NO_RESPONSE instead if no useful grounded question can
be formed safely.

ASK_ONE_QUESTION is permission to ask at most one question.

It is not an obligation to manufacture one.


======================================================================
6. RESPONSE PREFERENCE MUST NOT CHANGE THE EVIDENCE
======================================================================

The same journal text committed under:

    NO_RESPONSE
    REFLECT
    ASK_ONE_QUESTION

must create the same source evidence and the same admissible language-derived
state, all else being equal.

Response preference is conversational metadata.

It is not evidence.

It must not:

- change epistemic classification;
- change source provenance;
- strengthen or weaken a hypothesis;
- alter event time;
- alter contradiction detection;
- alter identity resolution;
- alter privacy;
- alter admission eligibility.

Qualification must compare all three postures against identical synthetic
entries and prove evidence/state equivalence.


======================================================================
7. CAPTURE MUST PRECEDE RESPONSE
======================================================================

Journal entry commitment is authoritative before optional response behavior.

Required conceptual ordering:

    validate commit command
        ->
    admit source
        ->
    process permissible language evidence
        ->
    finalize Journal capture receipt
        ->
    determine optional response intent

A response-planning or rendering failure must never roll back a successfully
committed Journal entry.

A failed Journal response is:

    RESPONSE_FAILURE_AFTER_SUCCESSFUL_CAPTURE

not:

    ENTRY_FAILURE

No model/rendering failure may cause the user to lose what they wrote.


======================================================================
8. REQUIRED JOURNAL CAPTURE CONTRACT
======================================================================

Implement a typed Journal commit contract carrying the minimum information
required to establish:

- journal entry identity;
- idempotency identity;
- committed text;
- capture origin;
- response preference;
- privacy state;
- report/commit time inputs where appropriate;
- synthetic qualification authority.

Capture origin must distinguish at least:

    TYPED
    SPEECH_TRANSCRIPT

Do not accept raw microphone audio.

Committed text is the canonical Journal source.

Speech transcript text must use the same downstream Journal path as typed
text, differing only in provenance.

Do not build speech capture in this phase.


======================================================================
9. ENTRY IDENTITY
======================================================================

One committed Journal entry must have one durable stable source identity.

The Journal layer must not create a second competing canonical identity
system where CT-V2-07 source identity already provides the necessary durable
identity.

Where useful, expose:

    JournalEntryId

as a semantic wrapper or mapping around the governed source identity.

Do not create parallel source history.

Do not duplicate the source body into a separate Journal evidence store.


======================================================================
10. DRAFT VERSUS COMMITTED ENTRY
======================================================================

A draft is not evidence.

CT-V2-09 may define a draft contract if useful for future UI integration, but
must preserve:

    DRAFT != COMMITTED SOURCE

Uncommitted draft text must not enter:

- longitudinal store;
- language perception;
- state formation;
- contradiction detection;
- recurrence detection;
- retrieval;
- profile state;
- response generation requiring durable evidence.

A future UI may autosave drafts separately.

That is not production-authorized by this phase.

Only explicit Journal commit creates canonical evidence.


======================================================================
11. POST-COMMIT EDITING IS REVISION
======================================================================

Once committed, changing Journal text is not ordinary mutable editing.

It is revision.

Implement or expose the governed Journal revision path:

    existing Journal entry
        ->
    user-authorized source revision
        ->
    CT-V2-07 AppendSourceRevision
        ->
    CT-V2-08 reprocessing
        ->
    conservative dependent-state invalidation/review

Required behavior:

- original text remains inspectable;
- stable source identity remains;
- new revision receives a new revision identity;
- previous interpretation tied to old wording becomes review-required where
  applicable;
- no silent overwrite occurs;
- response preference change alone does not revise source text.

A revision may correct transcription, wording, date, or meaning only when the
user actually changes the committed source.


======================================================================
12. JOURNAL PROVENANCE
======================================================================

Every Journal entry must remain recognizably Journal-origin evidence.

Preserve at least:

    acquisition mode = JOURNAL
    capture origin = TYPED or SPEECH_TRANSCRIPT
    stable source identity
    source revision
    report time
    record time
    original committed content
    privacy state

Journal provenance must remain distinct from:

    BIOGRAPHER
    THERAPIST_CONVERSATION
    USER_CORRECTION

A retrospective account entered in Journal is still Journal provenance even
when the EVENT occurred years earlier.

Example:

    "I remembered today that we moved when I was ten."

must preserve:

    report provenance/time:
        current Journal entry

and:

    event time:
        whatever the evidence honestly supports about the move

Do not conflate event time with Journal entry time.


======================================================================
13. PRIVATE JOURNAL ENTRIES
======================================================================

CT-V2-09 must exercise the existing PRIVATE evidence capability in Journal
capture.

A Journal commit may be designated:

    PRIVATE

Required behavior:

- the source entry may be retained;
- it remains Journal evidence;
- it does not enter ordinary derivation;
- CT-V2-08 perception/state formation must not promote its content into
  ordinary active derived state;
- optional Thomas response must not use excluded longitudinal material;
- later privacy restoration follows the CT-V2-07 review-required rule;
- restoration cannot silently reactivate derived state.

Do not invent additional privacy levels in this phase.

Do not infer PRIVATE from sensitive subject matter.

Privacy is user/system authority, not content classification.


======================================================================
14. JOURNAL DOES NOT INTERVIEW
======================================================================

Journal must not become a passive-looking Biographer.

A Journal entry must not cause the system to automatically pursue:

- timeline gaps;
- missing childhood periods;
- unknown parents;
- relationship coverage;
- job-history completeness;
- unresolved biographical dates;
- "highest information gain" questions.

ASK_ONE_QUESTION is local to the current entry.

Biographer later owns deliberate longitudinal investigation.

This separation must be structurally testable.


======================================================================
15. JOURNAL DOES NOT PRACTICE THERAPY
======================================================================

Journal response policy must not call or imitate the ordinary Therapy route
engine merely because the entry contains emotion.

Examples:

    "I had a terrible day."

does not authorize Therapy.

    "I feel anxious about tomorrow."

does not authorize a therapeutic technique.

    "I can't stop thinking about the argument."

does not authorize a rumination intervention.

    "I hate myself today."

may eventually intersect safety authority where separately required, but it
does not convert Journal into ordinary Therapy.

Preserve all CT-V2-04 and CT-V2-05 therapeutic authority boundaries.

Journal response policy is its own bounded posture.


======================================================================
16. NO AUTOMATIC HISTORICAL RETRIEVAL
======================================================================

CT-V2-09 does not open longitudinal conversational retrieval.

Journal optional responses must use the current entry and currently authorized
local structured interpretation only.

Do not retrieve prior entries merely because:

- names match;
- topics match;
- emotions match;
- dates match;
- Thomas could make a clever connection.

The future product may support "look back."

That is not ordinary automatic Journal response authority in this phase.

No embeddings or semantic retrieval are authorized.


======================================================================
17. SOURCE-FIRST LANGUAGE PROCESSING
======================================================================

Use CT-V2-08 exactly as established.

For eligible Journal entries:

1. admit the committed source;
2. bind perception to the exact admitted source revision;
3. validate source spans;
4. classify language conservatively;
5. admit eligible structured evidence only through CT-V2-07;
6. form deterministic state;
7. retain ambiguity/unsupported results without guessing.

An unsupported sentence remains a perfectly valid Journal entry.

Example:

    "What a weird day."

may be worth preserving even if no durable structured evidence can safely be
formed.

Journal value does not depend on extraction success.


======================================================================
18. JOURNAL CAPTURE RECEIPT
======================================================================

Return an inspectable typed capture result.

A successful receipt should expose at least:

- Journal entry/stable source ID;
- source revision ID;
- acquisition mode;
- capture origin;
- privacy;
- CT-V2-07 admission result;
- CT-V2-08 processing disposition;
- admitted evidence IDs;
- unresolved/unsupported count;
- resulting store/state revision where applicable;
- response preference;
- response-intent disposition;
- canonical capture/result fingerprint.

Do not reproduce the complete source body in ordinary logs.

Response content is not part of the canonical source receipt.


======================================================================
19. FAILURE MODEL
======================================================================

Define typed Journal capture failures.

At minimum distinguish:

    REJECTED_EMPTY_ENTRY
    REJECTED_INVALID_COMMAND
    REJECTED_AUTHORITY
    REJECTED_IDEMPOTENCY_CONFLICT
    REJECTED_PRIVACY_STATE
    SOURCE_ADMISSION_FAILED
    EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE
    RESPONSE_PLANNING_FAILED_AFTER_CAPTURE
    STORE_FAILURE_WITHOUT_COMMIT

Carefully distinguish failure before source commitment from failure after
source commitment.

If the source is durably admitted but downstream derivation fails:

    the source must remain valid.

Do not roll back truthful source evidence merely because Psychographer-like
processing failed.


======================================================================
20. IDEMPOTENCY
======================================================================

Submitting the identical Journal commit with the same idempotency key must not
create duplicate entries.

Required behavior:

same key + same logical commit:
    return the accepted result / idempotent replay

same key + different text:
    fail closed

same key + changed privacy:
    fail closed unless explicitly defined as a separate governed operation

same key + changed response posture:
    must not create another source

Choose and document the exact semantics of response-posture-only retries.

The important invariant is:

    retry cannot manufacture duplicate evidence.


======================================================================
21. RESPONSE PLAN — NOT RESPONSE EVIDENCE
======================================================================

If CT-V2-09 introduces a Journal response plan, it must be a typed
conversation artifact separate from evidence.

At minimum the plan should express:

    posture
    whether response is permitted
    grounding source IDs
    semantic act
    maximum question count
    prohibited acts
    reason code

Possible semantic acts:

    NONE
    BRIEF_REFLECTION
    ONE_GROUNDED_QUESTION

Do not persist Thomas response text as user evidence.

Do not feed Thomas response prose back through Journal perception.

Thomas must never become evidence for Thomas.


======================================================================
22. MODEL / RENDERER AUTHORITY
======================================================================

No model is required to qualify CT-V2-09.

The core Journal Capture Engine and response-intent policy must be completely
deterministic.

If an already-existing renderer abstraction is used in qualification, it may
only render an already-authorized Journal response plan.

It may not:

- choose response posture;
- decide whether to ask;
- choose Therapy behavior;
- add historical evidence;
- create longitudinal claims;
- alter source capture;
- mutate state.

A rendering failure must leave the Journal entry successfully captured.

Do not add:

- a new model;
- GGUF artifact;
- model training;
- model prompt authority;
- remote inference;
- ungoverned generative interpretation.


======================================================================
23. RECOMMENDED MODULE BOUNDARY
======================================================================

Prefer a pure Kotlin module such as:

    :thomas:journal

or:

    :thomas:journal-capture

Use repository conventions if another name is materially better.

Preferred conceptual graph:

    :thomas:journal
        -> :thomas:language-evidence
        -> :thomas:longitudinal-admission
        -> :thomas:longitudinal

    :qualification
        -> :thomas:journal
        -> :thomas:longitudinal-store

Do not make Journal depend on:

- Android;
- Compose;
- Room;
- speech implementation;
- renderer implementation;
- llama.cpp;
- network;
- V1;
- Therapy implementation;
- Biographer implementation.

Do not make the longitudinal domain depend on Journal.


======================================================================
24. NO PRODUCTION ANDROID STORE YET
======================================================================

CT-V2-07/08 remain synthetic qualification foundations.

CT-V2-09 does not authorize storing actual Principal/user Journal content in
the plaintext qualification store.

Do not wire:

    Android Journal UI
        ->
    qualification SQLite store

Do not convert SYNTHETIC_QUALIFICATION_ONLY into production use.

The Journal engine may be architecturally ready for later Android integration,
but this phase does not grant real-user persistence authority.


======================================================================
25. REQUIRED SYNTHETIC QUALIFICATION CORPUS
======================================================================

At minimum qualify the following.

A. BASIC CAPTURE

01. Typed Journal entry under NO_RESPONSE.
    -> source admitted
    -> Journal provenance
    -> zero response intent beyond NONE

02. Speech-transcript Journal entry.
    -> same semantic path
    -> SPEECH_TRANSCRIPT provenance
    -> no raw audio

03. Same text via typed versus speech transcript.
    -> downstream evidence equivalent except provenance

04. Ordinary mundane entry.
    "I bought groceries after work."
    -> valid Journal source
    -> no requirement for psychological importance

05. Unsupported/ambiguous prose.
    -> source retained
    -> safe NO_EVIDENCE_PROPOSAL permitted

B. RESPONSE POSTURES

06. Identical entry under NO_RESPONSE.
07. Identical entry under REFLECT.
08. Identical entry under ASK_ONE_QUESTION.

Required:
    source/evidence/state equivalence across 06-08.

09. NO_RESPONSE makes zero model/renderer invocation.

10. REFLECT produces only a bounded current-entry response intent.

11. ASK_ONE_QUESTION produces at most one grounded question intent.

12. No useful safe question exists.
    -> ASK_ONE_QUESTION may degrade to NO_RESPONSE.

13. Journal response planner failure.
    -> committed source survives unchanged.

14. Renderer/model failure if a renderer seam is exercised.
    -> committed source survives unchanged.

15. Thomas response artifact cannot become Journal evidence.

C. TEMPORAL / PROVENANCE

16. "I feel exhausted today."
    -> current self-report.

17. "I remembered today that we moved around 2012."
    -> Journal report time current
    -> event time approximate historical

18. "Maybe that happened before college."
    -> uncertainty and relative timing preserved.

19. Same historical event separately reported later by Biographer fixture.
    -> provenance remains distinct
    -> may support same event without source collapse.

D. PRIVACY

20. PRIVATE Journal entry.
    -> source retained
    -> ordinary derivation excluded.

21. PRIVATE entry containing otherwise extractable self-report.
    -> no active derived state.

22. Restore privacy.
    -> review required
    -> no silent reactivation.

E. REVISION

23. Commit entry.
24. User edits committed wording.
    -> append source revision
    -> original retained.

25. Correction changes an event date.
    -> honest revision/correction relation
    -> previous derivation review-required.

26. Response preference changes without source text change.
    -> no source revision.

F. IDEMPOTENCY

27. Same commit/key repeated.
    -> one source.

28. Same key with different text.
    -> fail closed.

29. Same key retry after response failure.
    -> no duplicate source.

G. MODE SEPARATION

30. Emotionally intense Journal entry.
    -> does not invoke Therapy route.

31. Entry mentioning childhood gap.
    -> does not invoke Biographer investigation.

32. ASK_ONE_QUESTION.
    -> current-entry local
    -> does not query longitudinal coverage.

33. Journal mode may capture past history.
    -> provenance remains JOURNAL.

H. LANGUAGE / STATE

34. Explicit self-report.
35. User interpretation.
36. Third-party report.
37. Counterfactual.
38. Interrogative.
39. Ambiguous entity.
40. Approximate date.
41. Explicit correction.
42. Contradictory later entry.

Each must preserve CT-V2-08 epistemic discipline.

I. MULTI-ENTRY LONGITUDINAL BEHAVIOR

43. Several independent Journal entries create independent sources.

44. Three genuinely independent qualifying reports may support structural
    recurrence under the existing CT-V2-08 rule.

45. Three sentences inside one Journal entry do not falsely count as three
    independent sources.

46. Contradictory entries coexist.

47. Change over time is not automatically contradiction.

48. Close/reopen/replay produces equal canonical state digest.

J. REGRESSION / AUTHORITY

49. CT-V2-04 safety permit tests remain qualified.

50. CT-V2-05 deterministic therapeutic progression remains qualified.

51. CT-V2-05 anti-repetition remains qualified.

52. CT-V2-06 longitudinal evidence invariants remain qualified.

53. CT-V2-07 admission/store invariants remain qualified.

54. CT-V2-08 language/state invariants remain qualified.

55. Journal default remains NO_RESPONSE.

56. Biographer remains investigation-only.

57. Production longitudinal writers remain zero.

58. App/runtime longitudinal writers remain zero.

59. Model-authorized evidence writers remain zero.

60. V1 migration components remain 24/24 DENIED.

61. Canonical .git remains present throughout qualification.

62. Temporary Git metadata directories remain zero.


======================================================================
26. RESPONSE QUALITY FIXTURES
======================================================================

For deterministic response-plan qualification, include examples such as:

ENTRY:
    "Work was exhausting today. I kept getting interrupted."

NO_RESPONSE:
    no response plan

REFLECT:
    may authorize:
        brief reflection on the reported exhaustion/interruptions

    must not authorize:
        diagnosis
        "You have trouble setting boundaries"
        childhood connection
        coping intervention

ASK_ONE_QUESTION:
    may authorize one grounded question such as conceptually:
        what part of the interruptions was hardest?

    must not authorize:
        three questions
        "Why do you let people disrespect you?"
        "Does this remind you of your father?"
        CBT/DBT/etc. technique selection


ENTRY:
    "Sam hates me."

REFLECT:
    must preserve that this is the user's interpretation.

It must not plan language equivalent to:

    "Sam hates you."


ENTRY:
    "I feel like I fail at everything."

REFLECT:
    may reflect the user's harsh self-evaluation.

It must not promote:

    "You fail at everything"

or:

    "You have a persistent negative core belief"

as established fact.


======================================================================
27. PRIVACY / LOGGING BOUNDARY
======================================================================

Operational logs must not contain:

- Journal entry bodies;
- source excerpts;
- response prose;
- derived psychological prose;
- speech transcript content.

Logs may contain:

- IDs;
- operation type;
- response posture;
- disposition;
- counts;
- policy/rule versions;
- hashes/fingerprints;
- timings.

Qualification fixtures may assert source contents inside test scope.

Do not create a debug path that dumps Journal text to Logcat.


======================================================================
28. DOCUMENTATION
======================================================================

Preserve this work order verbatim at:

    docs/work-orders/CT-V2-09-JOURNAL-CAPTURE-ENGINE.md

Produce at least:

    docs/CT-V2-09-JOURNAL-CAPTURE-ENGINE.md

    docs/journal/
    CT-V2-09-JOURNAL-CAPTURE-CONTRACT.md

    docs/journal/
    CT-V2-09-JOURNAL-RESPONSE-POSTURES.md

    docs/journal/
    CT-V2-09-JOURNAL-REVISION-AND-PRIVACY.md

    docs/qualification/
    CT-V2-09-QUALIFICATION.md

Add an ADR if architectural decisions warrant one.

Update README/module-boundary navigation as appropriate.

Do not substantively rewrite the canonical forward development plan.


======================================================================
29. AUTHORITY / BYPASS AUDIT
======================================================================

Demonstrate structurally that:

- Journal commit is the only Journal source-creation authority;
- drafts cannot become evidence;
- response preference cannot change source evidence;
- response generation cannot precede or control source commitment;
- NO_RESPONSE performs no response/model call;
- Journal responses cannot directly mutate longitudinal state;
- Thomas response text cannot become source evidence;
- CT-V2-07 remains the only durable admission authority;
- CT-V2-08 remains the language/state authority;
- Journal does not call Therapy policy;
- Journal does not call Biographer investigation;
- Journal does not perform semantic historical retrieval;
- no Android production writer exists;
- no direct SQL/JDBC mutation path is added;
- no V1 writer is introduced;
- no model-authorized evidence path exists.

Report all discovered Journal-to-store paths.

Intended counts:

    production Journal writers:                 0
    Android/app Journal writers:                0
    direct Journal SQL/JDBC writers:            0
    model-authorized Journal writers:           0
    raw draft -> evidence paths:                0
    response -> user evidence paths:            0
    Journal -> Therapy routing paths:            0
    Journal -> Biographer routing paths:         0
    qualification Journal capture pipelines:    documented governed path(s)
    durable admission gate:                     CT-V2-07 only


======================================================================
30. EXPLICITLY PROHIBITED WORK
======================================================================

Do not begin later phases.

Specifically prohibited:

- production Android Journal persistence;
- real-user Journal data;
- Room migration for the longitudinal store;
- Keystore work;
- production encryption claims;
- cloud synchronization;
- export/backup/recovery;
- irreversible deletion lifecycle;
- semantic search;
- embeddings;
- historical conversational retrieval;
- "look back" implementation;
- Biographer question selection;
- Biographer interview state;
- Therapy longitudinal retrieval;
- Therapy rule expansion;
- profile inspector UI;
- Journal Android UI redesign;
- microphone implementation;
- STT repair;
- TTS work;
- renderer/model replacement;
- new GGUF model;
- LLM extraction;
- prompt-driven evidence admission;
- diagnosis;
- psychological scoring;
- V1 migration;
- real profile data.

Do not use CT-V2-09 as an excuse to open the production privacy/storage phase.


======================================================================
31. VCS STABILITY QUALIFICATION
======================================================================

Throughout CT-V2-09:

- .git must remain canonical and present;
- do not rename it;
- do not redirect --git-dir for normal development;
- do not create .git-ct-v2-*;
- do not hide the repository from Android Studio.

Before final sealing run:

    tools\verify-canonical-git-root.ps1

and:

    git rev-parse --show-toplevel
    git rev-parse --git-dir
    git status --porcelain=v1
    git fsck --full --strict

Required:

    canonical Git root valid
    .git canonical
    temporary metadata count = 0


======================================================================
32. COMPLETE BUILD QUALIFICATION
======================================================================

From the final implementation state run at minimum:

    gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain

Required:

- BUILD SUCCESSFUL;
- all prior tests pass;
- all CT-V2-09 tests pass;
- zero skipped CT-V2-09 acceptance tests;
- zero new lint errors/fatals;
- debug APK assembles;
- unsigned release APK assembles;
- APK contains no Journal fixture corpus;
- APK contains no longitudinal qualification database;
- APK contains no model artifact;
- no generated database is tracked;
- no raw/restricted fixture source is tracked;
- no build output is tracked;
- application backup remains disabled;
- forward-plan size/hash remain unchanged;
- all 24 V1 components remain DENIED;
- temporary Git metadata count remains zero;
- final worktree is clean;
- git diff --check passes;
- git fsck --full --strict succeeds.

Record APK size and SHA-256.

No device, production storage, real-user, security-lifecycle, or therapeutic
efficacy qualification is claimed.


======================================================================
33. STOP CONDITIONS
======================================================================

STOP rather than broaden scope if:

- accepted baseline differs;
- Android Studio Git-root repair has regressed;
- .git would need to be moved;
- real user data is required;
- production plaintext Journal storage would be required;
- response generation must control source commitment;
- Journal response posture changes evidence interpretation;
- NO_RESPONSE causes a model call;
- Journal begins automatically behaving like Therapy;
- Journal begins automatically behaving like Biographer;
- historical retrieval becomes necessary;
- a model is required for admission;
- a Journal correction would overwrite original evidence;
- a response must become source evidence;
- private entries enter ordinary derived state;
- CT-V2-07 admission must be bypassed;
- CT-V2-08 epistemic distinctions must be weakened;
- a prior therapeutic/safety invariant regresses;
- a test must be weakened or skipped to obtain a pass.

A quiet Journal that captures perfectly is preferable to an impressive Journal
that overreaches.


======================================================================
34. COMMITS AND TAG
======================================================================

Use small reviewable commits.

A suitable sequence is:

1. Record CT-V2-09 Journal authority and contracts.
2. Implement Journal capture engine.
3. Implement bounded Journal response postures.
4. Implement revision/privacy/idempotency behavior.
5. Add synthetic Journal qualification corpus.
6. Record final qualification evidence.

Do not squash meaningful history.

On successful qualification create the annotated tag:

    ct-v2-09-journal-capture-engine

Tag the exact final qualified HEAD.

Do not configure a remote.

Do not push.


======================================================================
35. REQUIRED COMPLETION REPORT
======================================================================

Report:

DISPOSITION
- COMPLETE / PARTIAL / BLOCKED.

REPOSITORY
- starting/final HEAD;
- starting/final tree;
- branch;
- commits;
- tag name/object/target/type;
- worktree;
- remote;
- push.

VCS
- canonical root;
- .git location;
- temporary metadata count;
- IDE visual confirmation availability;
- whether the prior Android Studio warning is visibly absent;
- confirmation .git was never relocated.

FORWARD PLAN
- path;
- before/after bytes;
- before/after SHA-256;
- redundant copy count.

JOURNAL ARCHITECTURE
- module graph;
- Journal commit authority;
- entry/source identity relationship;
- draft boundary;
- typed/speech provenance;
- private-entry behavior;
- revision behavior;
- failure behavior;
- idempotency behavior.

RESPONSE POSTURES
- NO_RESPONSE behavior;
- REFLECT behavior;
- ASK_ONE_QUESTION behavior;
- response-plan representation;
- proof posture does not alter evidence;
- proof capture survives response failure.

LONGITUDINAL
- CT-V2-07 admission path;
- CT-V2-08 language/state path;
- source/evidence/state counts for synthetic fixtures;
- replay/digest behavior;
- privacy and revision consequences.

AUTHORITY
- production Journal writers;
- Android/app Journal writers;
- direct SQL/JDBC writers;
- model evidence writers;
- draft-to-evidence paths;
- response-to-evidence paths;
- Journal-to-Therapy paths;
- Journal-to-Biographer paths;
- V1 writers.

QUALIFICATION
- exact clean command;
- actionable tasks;
- total tests;
- new CT-V2-09 tests;
- failures/errors/skips;
- posture-equivalence tests;
- privacy/revision/idempotency tests;
- close/reopen/replay;
- lint;
- APK hashes;
- artifact scans;
- VCS invariant;
- git fsck.

LIMITATIONS
- synthetic-only;
- no production Journal storage;
- no Android Journal wiring;
- no model requirement;
- no historical retrieval;
- no Biographer investigation;
- no Therapy expansion;
- no production privacy lifecycle;
- no real user data.

RECOMMENDATION
- read the canonical forward development plan;
- identify the exact next planned phase by its canonical name;
- state whether that phase is ready for Principal consideration;
- do not open it.


======================================================================
36. TARGET TERMINAL DISPOSITION
======================================================================

A fully successful CT-V2-09 should be able to report:

CT_V2_09_JOURNAL_CAPTURE_ENGINE_COMPLETE
JOURNAL_ENTRY_AS_FIRST_CLASS_SOURCE_QUALIFIED
JOURNAL_SOURCE_FIRST_CAPTURE_BOUNDARY_QUALIFIED
TYPED_AND_SPEECH_TRANSCRIPT_PROVENANCE_QUALIFIED
NO_RESPONSE_DEFAULT_PRESERVED
BOUNDED_REFLECT_AND_ONE_QUESTION_POSTURES_QUALIFIED
RESPONSE_POSTURE_EVIDENCE_INDEPENDENCE_QUALIFIED
CAPTURE_SURVIVES_RESPONSE_FAILURE_QUALIFIED
PRIVATE_JOURNAL_DERIVATION_EXCLUSION_QUALIFIED
JOURNAL_REVISION_HISTORY_PRESERVED
CT_V2_07_REMAINS_SOLE_DURABLE_ADMISSION_GATE
CT_V2_08_EPISTEMIC_DISCIPLINE_PRESERVED
JOURNAL_THERAPY_AUTHORITY_ZERO
JOURNAL_BIOGRAPHER_INVESTIGATION_AUTHORITY_ZERO
MODEL_EVIDENCE_AUTHORITY_ZERO
ANDROID_STUDIO_CANONICAL_VCS_ROOT_REMAINS_VALID
NO_PRODUCTION_LONGITUDINAL_AUTHORITY_GRANTED
NEXT_PHASE_READY_FOR_PRINCIPAL_CONSIDERATION

The next phase remains unopened until Principal review.
