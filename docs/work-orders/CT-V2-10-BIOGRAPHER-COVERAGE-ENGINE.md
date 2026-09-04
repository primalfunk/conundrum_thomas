CONUNDRUM THOMAS V2
IMPLEMENTATION WORK ORDER

CT-V2-10 — BIOGRAPHER COVERAGE ENGINE


PRINCIPAL DISPOSITION

CT_V2_09_ACCEPTED
CT_V2_09_JOURNAL_CAPTURE_ENGINE_SEALED
CT_V2_10_AUTHORIZED
NEXT_PHASE_NOT_AUTHORIZED


======================================================================
1. PURPOSE
======================================================================

Implement the governed Biographer Coverage Engine for Conundrum Thomas V2.

Biographer has a different longitudinal job from Journal.

Journal captures life as it arrives.

Biographer deliberately investigates life that already happened.

Its purpose is to increase longitudinal coverage over time by:

- identifying meaningful structural gaps in known history;
- identifying unresolved evidentiary questions;
- allowing the user to begin anywhere they choose;
- selecting one useful historical target when active investigation is wanted;
- asking a bounded, evidence-honest question;
- capturing the answer with BIOGRAPHER provenance;
- updating the common longitudinal evidence state;
- remembering what has already been investigated;
- avoiding needless repetition;
- respecting private, declined, deferred, unknown, and uncertain areas.

Biographer is investigation.

It is not Therapy.

It is not Journal.

It is not a psychological diagnosis engine.

It is not an excuse to interrogate every possible topic.

The successful result is a synthetic-only deterministic pipeline:

    longitudinal state
        ->
    coverage analysis
        ->
    investigation target
        ->
    one governed question intent
        ->
    synthetic user answer
        ->
    BIOGRAPHER source capture
        ->
    CT-V2-08 language-to-evidence
        ->
    CT-V2-07 governed admission
        ->
    revised coverage state

Two first-class operating postures must exist:

    OPEN_STORY
        "Start wherever you like."

    TARGETED_COVERAGE
        "Let's fill in this particular blank."

The user always retains authority to:

    answer
    skip
    defer
    mark private
    decline coverage
    change topic
    stop


======================================================================
2. ACCEPTED ENTRY BASELINE
======================================================================

Canonical repository:

    C:\Android Studio Projects\ConundrumThomasV2

Required branch:

    main

Required starting HEAD:

    52cc385f2acc2fdf6f1e93a6b89594c8a87cb7c0

Required starting tree:

    184c163d358ead8955e9340f23694b8f0dd7b50f

Required annotated tag:

    ct-v2-09-journal-capture-engine

Required tag object:

    0546cfef5c89804527c81af75ebd1c6f3aef7eca

Required tag target:

    52cc385f2acc2fdf6f1e93a6b89594c8a87cb7c0

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
2. Verify forward-plan path, size, and SHA-256.
3. Verify all 24 V1 migration components remain DENIED.
4. Verify root fdp.txt remains absent.
5. Verify exactly one canonical forward-plan copy exists.
6. Run:

       tools\verify-canonical-git-root.ps1

7. Verify:

       git rev-parse --show-toplevel
       git rev-parse --git-dir
       git status --porcelain=v1

Required:

       root:
       C:/Android Studio Projects/ConundrumThomasV2

       metadata:
       .git

       temporary Git metadata directories:
       0

8. Run the complete accepted test suite before behavioral changes.

If the baseline differs, STOP.

Do not reset, stash, move .git, construct alternate Git metadata, or silently
repair an unexpected repository state.


======================================================================
3. CORE BIOGRAPHER CONCEPT
======================================================================

The Biographer is responsible for answering:

    WHAT PARTS OF THE USER'S HISTORY DO WE KNOW?

    WHAT PARTS DO WE ONLY PARTLY KNOW?

    WHAT IS UNRESOLVED?

    WHAT HAVE WE NEVER ASKED ABOUT?

    WHAT HAS THE USER DECLINED TO DISCUSS?

    WHAT IS PRIVATE?

    WHAT SHOULD WE NOT ASK AGAIN?

    WHAT SINGLE HISTORICAL QUESTION WOULD MOST USEFULLY IMPROVE
    LONGITUDINAL COVERAGE RIGHT NOW?

This is a coverage problem.

It is not a therapeutic formulation problem.

The engine may use structural longitudinal information.

It may not assign psychological significance merely to decide what to ask.


======================================================================
4. TWO GOVERNED BIOGRAPHER POSTURES
======================================================================

Implement explicit typed posture authority.

Required:

    OPEN_STORY
    TARGETED_COVERAGE


----------------------------------------------------------------------
4.1 OPEN_STORY
----------------------------------------------------------------------

OPEN_STORY allows the user to provide historical material without the system
first choosing a target.

Conceptually:

    "Start wherever you like."

The engine may provide a neutral invitation, but it must not impose:

- a period;
- a topic;
- a psychological interpretation;
- a relationship;
- a trauma frame;
- a therapeutic objective.

The resulting answer is admitted as BIOGRAPHER provenance.

After processing, coverage may improve wherever the answer actually provided
evidence.

OPEN_STORY is not unstructured in storage.

The conversation may be open, while resulting evidence remains governed.


----------------------------------------------------------------------
4.2 TARGETED_COVERAGE
----------------------------------------------------------------------

TARGETED_COVERAGE allows Thomas to select exactly one eligible coverage target.

The target must arise from deterministic structural evidence such as:

- a temporal gap;
- a known period with little detail;
- unresolved event timing;
- unresolved identity;
- unresolved correction;
- unresolved contradiction;
- incomplete role history;
- incomplete place history;
- incomplete relationship context;
- an explicit open evidentiary question;
- a user-named topic requesting further exploration.

Target selection must not be based on:

- guessed trauma;
- diagnosis;
- presumed psychological importance;
- emotional intensity alone;
- model intuition;
- likelihood of producing an interesting answer.

The target exists because the history is structurally incomplete or unresolved.


======================================================================
5. COVERAGE IS NOT A CLAIM OF LIFE COMPLETENESS
======================================================================

Do not define a finite list of subjects and claim that filling them produces a
"complete person."

Longitudinal history is open-ended.

Coverage therefore means:

    coverage of currently represented or structurally discoverable history

not:

    percentage of the user's entire life known

Do not produce false metrics such as:

    BIOGRAPHY 83% COMPLETE

unless a future Principal-authorized product explicitly defines a bounded
questionnaire.

CT-V2-10 may report deterministic structural summaries such as:

    known periods
    sparse periods
    unresolved intervals
    unresolved identities
    open questions
    deferred targets
    declined targets
    private targets
    investigated targets

These are coverage facts, not psychological scores.


======================================================================
6. COVERAGE TARGET TYPES
======================================================================

Define typed investigation targets.

At minimum support categories equivalent to:

    OPEN_STORY

    TEMPORAL_GAP

    PERIOD_DETAIL

    EVENT_DETAIL

    EVENT_TIME_UNRESOLVED

    ROLE_GAP

    PLACE_GAP

    RELATIONSHIP_CONTEXT

    ENTITY_IDENTITY_UNRESOLVED

    CONTRADICTION_CLARIFICATION

    CORRECTION_TARGET_UNRESOLVED

    USER_NAMED_TOPIC

    EXISTING_OPEN_EVIDENTIARY_QUESTION

Exact names may follow repository conventions.

Each target must expose enough information to establish:

- target ID;
- target kind;
- governing evidence IDs;
- relevant entities/events/periods;
- known temporal bounds;
- uncertainty;
- why the target exists;
- eligibility state;
- prior investigation history;
- question-rule version.


======================================================================
7. TEMPORAL COVERAGE
======================================================================

Biographer must understand honest historical gaps without manufacturing
precision.

Example:

Known:

    lived in Seattle in 2010
    lived in Portland in 2015

Potential target:

    PERIOD BETWEEN 2010 AND 2015

But the engine must not invent:

    moved on 2012-07-01

or even assume that one continuous missing residence exists.

A valid target is structurally:

    "There is little information represented for this interval."

Approximate source evidence must remain approximate.

Example:

    "around 2012"

must not create exact boundary arithmetic that assumes January 1, 2012.

Unknown and uncertain time are valid states.

The engine should prefer:

    broad truthful gap

over:

    precise fictional gap.


======================================================================
8. COVERAGE STATE
======================================================================

Reuse the CT-V2-06/07 coverage and privacy semantics wherever applicable.

Do not create parallel incompatible meaning.

The coverage engine must be able to distinguish at least:

    UNKNOWN
    PARTIAL / SPARSE
    COVERED ENOUGH FOR CURRENT PURPOSE
    UNRESOLVED
    PRIVATE
    DECLINED

If additional Biographer-session behavior is needed, introduce interview-local
states such as:

    DEFERRED
    RECENTLY_ASKED

without turning those into factual psychological evidence.

DEFERRED means:

    not now

DECLINED means:

    user has declined coverage

PRIVATE means:

    material exists but is not eligible for ordinary derivation/use

UNKNOWN means:

    we do not know

None of these means:

    the event did not happen.


======================================================================
9. QUESTION AUTHORITY
======================================================================

The Coverage Engine may authorize at most one Biographer question at a time.

A question intent must contain at least:

    target ID
    target kind
    grounding IDs
    known facts safe to reference
    uncertainty constraints
    question semantic act
    prohibited assumptions
    reason code
    question-rule version

The deterministic engine selects:

    WHAT TO ASK ABOUT

and:

    WHAT THE QUESTION MAY PRESUPPOSE

A later renderer may eventually choose fluent wording.

The renderer must never decide:

    which gap to investigate
    whether the gap exists
    whether an identity is resolved
    what the user probably meant
    whether something is psychologically important


======================================================================
10. QUESTION STYLE
======================================================================

Biographer is intended to feel easy-going.

That does not mean epistemically leading.

Questions may be directional.

They must not embed an unsupported answer.

Preferred conceptual forms include:

    "What do you remember about that period?"

    "What was happening around then?"

    "You mentioned working there around 2012. What do you remember about
     how that period began?"

    "You've referred to two people named Sam. Are those the same person,
     different people, or are you not sure?"

Avoid forms such as:

    "Why did that experience traumatize you?"

    "How did your father make you insecure?"

    "Was that when your depression began?"

    "You must have felt abandoned, right?"

The question may guide attention.

It may not manufacture psychological content.


======================================================================
11. USER AGENCY
======================================================================

Every targeted investigation must permit:

    ANSWER
    SKIP
    DEFER
    DECLINE
    PRIVATE
    CHANGE_TOPIC
    STOP

These outcomes are not failures.

SKIP:

    do not force an answer;
    target may remain unresolved.

DEFER:

    preserve target eligibility but avoid immediate reselection.

DECLINE:

    establish a governed declined-coverage state where applicable;
    do not ask again automatically.

PRIVATE:

    obey existing privacy semantics.

CHANGE_TOPIC:

    allow user-named coverage target or OPEN_STORY.

STOP:

    ends Biographer investigation without adverse state.


======================================================================
12. ANTI-REPETITION
======================================================================

Biographer must remember what it has already investigated.

Implement deterministic target/question history.

At minimum retain:

- target first offered;
- target last offered;
- offer count;
- answer disposition;
- last substantive answer source;
- deferred state;
- declined state;
- whether new evidence has materially changed the target.

A target must not be selected repeatedly merely because it remains incomplete.

Revisiting is permitted only under documented deterministic conditions such as:

- the user explicitly requests it;
- materially new evidence changes the target;
- a prior ambiguous answer becomes resolvable;
- a prior contradiction gains a new relevant claim;
- a deferred target becomes eligible after a defined boundary.

Do not use arbitrary conversational randomness as anti-repetition.


======================================================================
13. DETERMINISTIC TARGET SELECTION
======================================================================

Implement a deterministic ranking/selection policy.

Factors may include:

    explicit user-selected target
    unresolved open-question status
    structural gap magnitude
    number of independent evidence dependencies
    target answerability
    temporal adjacency to known history
    prior investigation count
    recent-question penalty
    deferred penalty
    privacy/decline exclusion

Do not include:

    psychological severity
    estimated pathology
    trauma probability
    diagnostic value
    therapeutic leverage
    predicted emotional response

Tie-breaking must be deterministic.

The same longitudinal state and same investigation history must produce the
same selected target.


======================================================================
14. PRIORITY ORDER
======================================================================

Use a documented rule hierarchy.

A suitable initial hierarchy is:

1. Explicit current user target.
2. Deterministically actionable correction ambiguity.
3. Deterministically actionable identity ambiguity.
4. Existing structural open evidentiary question.
5. Unresolved contradiction requiring factual clarification.
6. Known event/period with unresolved temporal structure.
7. Sparse structurally bounded period.
8. Other eligible historical coverage gap.
9. OPEN_STORY fallback.

This hierarchy may be adjusted if implementation evidence justifies a better
deterministic ordering.

Any change must remain structural rather than psychological.


======================================================================
15. DO NOT QUESTION EVERYTHING
======================================================================

Not every missing field deserves a question.

The engine must be able to return:

    NO_TARGET

when no useful eligible investigation target exists.

NO_TARGET is success.

Do not create questions simply to keep conversation moving.

Biographer should tolerate:

    sufficient ambiguity
    unknowable dates
    forgotten names
    ordinary unimportant detail
    incomplete but usable history

The goal is richer history over time, not total information extraction.


======================================================================
16. BIOGRAPHER ANSWER CAPTURE
======================================================================

Implement the qualification path for Biographer answers.

Conceptual sequence:

    question target selected
        ->
    question intent issued
        ->
    synthetic answer received
        ->
    answer committed as BIOGRAPHER source
        ->
    CT-V2-08 perception
        ->
    CT-V2-07 admission
        ->
    state formation
        ->
    coverage recomputed
        ->
    target history updated

The committed answer is the evidence.

The question itself is not user evidence.

Thomas's question must never become source evidence for Thomas.


======================================================================
17. BIOGRAPHER PROVENANCE
======================================================================

Every committed answer must remain recognizably:

    acquisition mode = BIOGRAPHER

Preserve at least:

    stable source identity
    source revision
    Biographer acquisition mode
    answer origin
    target/question ID
    report time
    record time
    original committed answer content
    privacy state

Biographer provenance must remain distinct from:

    JOURNAL
    THERAPIST_CONVERSATION
    USER_CORRECTION

Two accounts of the same historical event may coexist:

    Journal source
    Biographer source

They must not be collapsed merely because they refer to the same event.


======================================================================
18. EVENT TIME VERSUS INTERVIEW TIME
======================================================================

Biographer inherently produces retrospective reports.

Therefore qualification must strongly preserve:

    EVENT TIME
    REPORT TIME
    RECORD TIME

Example:

Interview occurs:

    2040

User says:

    "We moved around 1998."

Required:

    event time:
        approximate 1998

    report time:
        2040 interview

    record time:
        governed store time

Never rewrite the historical event as if it occurred at interview time.


======================================================================
19. QUESTIONS FROM CONTRADICTIONS
======================================================================

Biographer may select a contradiction clarification target.

It must not assume:

    newer statement is correct
    older statement is false
    user lied
    memory is defective

Example:

Source A:
    "We moved in 2012."

Source B:
    "We moved in 2013."

Permitted conceptual target:

    clarify the differing reported years

Not permitted:

    silently replace 2012 with 2013

If the user replies:

    "Actually, 2013 is right."

the resulting correction must still use CT-V2-07 governed correction/revision
authority.


======================================================================
20. QUESTIONS FROM IDENTITY AMBIGUITY
======================================================================

Biographer may investigate unresolved identity.

Example:

    "Sam from work..."
    "Sam, my old roommate..."

If the evidence does not establish identity, the engine may ask whether the
references concern:

    same person
    different people
    unknown / unsure

A user's resolution becomes governed evidence.

The question engine itself may not merge entities.


======================================================================
21. OPEN STORY BEHAVIOR
======================================================================

OPEN_STORY must be a first-class valid Biographer interaction.

When selected:

- no coverage target is required;
- no hidden "real target" may be embedded;
- the user can narrate any historical material;
- resulting evidence is processed normally;
- coverage improvements are discovered after capture;
- the engine may subsequently identify new gaps;
- no immediate follow-up is mandatory.

A successful OPEN_STORY interaction may end with:

    no next question.


======================================================================
22. USER-NAMED TARGET
======================================================================

Support explicit user direction such as:

    "Ask me about high school."

    "Let's talk about when I lived in Chicago."

    "I want to fill in my work history."

The user-selected historical topic should outrank ordinary engine-generated
coverage targets when eligible.

The engine may still avoid unsupported presuppositions.

A broad user target may produce a broad invitation rather than a fabricated
specific question.


======================================================================
23. RELATION TO JOURNAL
======================================================================

Preserve the CT-V2-09 distinction.

Journal:

    captures present/runtime material as supplied by the user;
    defaults to NO_RESPONSE;
    does not investigate historical coverage.

Biographer:

    intentionally investigates historical coverage;
    normally operates through one open or targeted question;
    captures answers with BIOGRAPHER provenance.

The same underlying event may be represented by both.

Do not create separate psychographic universes.

Both feed the common longitudinal substrate.


======================================================================
24. RELATION TO THERAPY
======================================================================

Biographer must not choose Therapy behavior.

Historical questions may concern difficult material.

That does not itself authorize:

    cognitive restructuring
    exposure
    behavioral activation
    grounding
    interpretation
    trauma processing
    diagnosis
    therapeutic challenge

Biographer asks to understand history.

Therapy later uses governed psychological state to help with a present problem.

Keep these authorities separate.


======================================================================
25. SAFETY BOUNDARY
======================================================================

CT-V2-10 does not expand safety policy.

If existing governed safety architecture requires an investigation response to
be blocked or redirected, preserve that authority.

Do not add new crisis procedures.

Do not encode therapy-like safety content into the Biographer coverage rules.

Coverage targeting must never override a higher existing safety authority.


======================================================================
26. COVERAGE MAP
======================================================================

Implement a deterministic inspectable coverage map.

At minimum it should expose:

    map/state revision
    represented periods
    sparse periods
    unresolved temporal gaps
    represented roles
    represented places
    represented relationships
    unresolved identities
    unresolved contradictions
    unresolved corrections
    open evidentiary questions
    deferred targets
    declined targets
    private targets
    previously investigated targets
    currently eligible targets
    selected target if any
    derivation/ranking rule version
    canonical digest

The coverage map must be reproducible from:

    admitted longitudinal history
    +
    governed Biographer investigation history

No free-form LLM summary is permitted.


======================================================================
27. INVESTIGATION HISTORY
======================================================================

Investigation history is operational state.

It must not masquerade as user psychological evidence.

Required distinctions:

    "Thomas asked about 1998"

is operational history.

    "User said X happened in 1998"

is source evidence.

Do not mix them.

Store or model investigation state using the narrowest mechanism appropriate
for qualification.

If durable persistence is introduced for synthetic qualification, it must be
clearly classified as Biographer operational state and must not bypass
CT-V2-07 for actual user evidence.


======================================================================
28. PRIVACY AND DECLINED COVERAGE
======================================================================

PRIVATE and DECLINED are hard exclusions from automatic targeting.

If an area is PRIVATE:

    do not select it automatically.

If the user DECLINED coverage:

    do not select it automatically again.

Do not infer that:

    private = important
    declined = traumatic
    declined = absent
    declined = suspicious

A user may later explicitly reopen a subject through a separately governed
action.

That does not erase the prior privacy/decline history.


======================================================================
29. DEFERRED TARGETS
======================================================================

Implement DEFERRED as an operational investigation disposition if useful.

DEFERRED means:

    "not now"

It must not mean:

    "never"

A deferred target must receive a deterministic reselection penalty or
temporary exclusion.

Do not immediately ask the same deferred target again.

The exact re-eligibility rule must be documented and tested.


======================================================================
30. ANSWER ADEQUACY
======================================================================

Do not judge answer adequacy psychologically.

The engine may determine structurally whether an answer:

    provided admissible evidence relevant to the target
    provided evidence but not relevant to the target
    remained ambiguous
    supplied no extractable evidence
    explicitly declined
    explicitly deferred
    changed topic

A brief answer such as:

    "I don't remember."

is a valid answer.

It must not trigger escalating pressure.

It may leave the target unresolved.


======================================================================
31. FOLLOW-UP BOUNDARY
======================================================================

One captured answer may create another structurally useful target.

That does not grant unlimited follow-up chaining.

Each new question requires a fresh deterministic selection.

Do not implement conversational recursion such as:

    keep asking until target complete

The user must be able to stop naturally after every answer.

One question at a time remains the governing interaction unit.


======================================================================
32. NO MODEL REQUIREMENT
======================================================================

CT-V2-10 must qualify without an LLM.

The core must deterministically decide:

    coverage target
    target eligibility
    target ranking
    question semantic intent
    prohibited assumptions
    investigation history
    next-target eligibility

If later renderer qualification uses an existing renderer seam, the renderer
may only humanize an authorized semantic question plan.

It may not invent:

    new targets
    facts
    dates
    relationships
    motives
    psychological meaning
    follow-up authority

Do not add:

    new GGUF
    model training
    model extraction
    remote inference
    LLM target ranking
    prompt-based coverage decisions


======================================================================
33. RECOMMENDED MODULE BOUNDARY
======================================================================

Prefer a pure Kotlin module such as:

    :thomas:biographer

or:

    :thomas:biographer-coverage

Use repository conventions if another name is materially superior.

Preferred dependency graph:

    :thomas:biographer
        -> :thomas:language-evidence
        -> :thomas:longitudinal-admission
        -> :thomas:longitudinal

    :qualification
        -> :thomas:biographer
        -> :thomas:longitudinal-store

The Biographer module must not depend on:

- Android;
- Compose;
- Room;
- speech implementation;
- renderer implementation;
- llama.cpp;
- network;
- Journal implementation except shared abstractions where strictly needed;
- Therapy implementation;
- V1.

Longitudinal modules must not depend on Biographer.


======================================================================
34. REQUIRED SYNTHETIC QUALIFICATION CORPUS
======================================================================

At minimum qualify the following cases.


A. OPEN STORY

01. OPEN_STORY with no preselected target.
    -> valid question/invitation intent.

02. User gives historical narrative.
    -> BIOGRAPHER source provenance.

03. Narrative spans multiple events.
    -> evidence processed conservatively.

04. OPEN_STORY answer contains unsupported language.
    -> source retained; unsupported material tolerated.

05. OPEN_STORY interaction creates no mandatory follow-up.


B. TARGETED TEMPORAL COVERAGE

06. Known evidence in 2010 and 2015 with sparse interval.
    -> eligible temporal-gap target.

07. Same case with approximate boundaries.
    -> no false exact interval.

08. User answers with historical event.
    -> gap coverage improves.

09. User answers "I don't remember."
    -> no invented event; target remains unresolved.

10. User defers.
    -> no immediate reselection.

11. User declines.
    -> excluded from automatic targeting.

12. Target made PRIVATE.
    -> excluded from automatic targeting.


C. EVENT / ROLE / PLACE COVERAGE

13. Known job begins but ending unknown.
    -> role-gap target.

14. Known residence ends but subsequent place unknown.
    -> place-gap target.

15. Known event exists with unknown date.
    -> event-time target.

16. Partial period with several evidence points.
    -> PERIOD_DETAIL may exist without claiming total absence.


D. IDENTITY

17. Two possible "Sam" references.
    -> identity target.

18. User confirms same person.
    -> governed identity resolution.

19. User confirms different people.
    -> governed rejection of merge.

20. User is unsure.
    -> unresolved state retained.

21. Same ambiguity previously declined.
    -> not targeted again automatically.


E. CONTRADICTION / CORRECTION

22. 2012 versus 2013 event-year contradiction.
    -> clarification target.

23. User confirms 2013.
    -> correction governed through existing authority.

24. User says both reports may be wrong.
    -> uncertainty preserved.

25. Apparent contradiction actually separated in time.
    -> no false clarification target.


F. USER-NAMED TOPIC

26. User says "ask me about high school."
    -> user target outranks ordinary gap.

27. User names broad topic with little structured evidence.
    -> broad non-presuppositional question intent.

28. User changes topic.
    -> previous target not forced.

29. User returns to deferred topic explicitly.
    -> eligible again.


G. TARGET SELECTION

30. Multiple eligible gaps.
    -> deterministic selected target.

31. Same state repeated.
    -> same selected target.

32. Tied targets.
    -> deterministic tie-break.

33. Recently asked unresolved target plus untouched eligible target.
    -> anti-repetition favors appropriate untouched target.

34. No eligible target.
    -> NO_TARGET.

35. All remaining targets PRIVATE/DECLINED.
    -> NO_TARGET.

36. Explicit user target versus engine target.
    -> user target wins when eligible.


H. ANTI-REPETITION

37. Answered target becomes covered enough.
    -> not immediately asked again.

38. Deferred target.
    -> not immediately asked again.

39. Declined target.
    -> not automatically asked again.

40. New evidence materially changes old unresolved target.
    -> documented revisiting rule may make it eligible.

41. Repeated execution without new evidence.
    -> does not cycle on one question.


I. PROVENANCE

42. Same historical event reported once in Journal and once in Biographer.
    -> separate source provenance.

43. Both may support one event.
    -> no source collapse.

44. Interview/report time current; event time historical.
    -> all three temporal dimensions preserved.

45. Biographer question text itself.
    -> never admitted as user evidence.


J. PRIVACY

46. PRIVATE evidence creates structural gap nearby.
    -> question must not leak or reference private evidence.

47. PRIVATE target itself.
    -> excluded.

48. DECLINED target.
    -> excluded.

49. Privacy later restored.
    -> existing CT-V2-07 review semantics preserved.

50. User explicitly reopens previously declined subject.
    -> governed reopening can be represented without erasing history.


K. ANSWER CAPTURE

51. Typed synthetic answer.
    -> BIOGRAPHER provenance.

52. Speech-transcript synthetic answer if common provenance abstraction permits.
    -> BIOGRAPHER + SPEECH_TRANSCRIPT provenance.

53. Identical answer retry/idempotency key.
    -> no duplicate source.

54. Changed answer under same key.
    -> fail closed.

55. Source captured but downstream evidence processing fails.
    -> source survives.

56. Question rendering failure before answer.
    -> no source mutation.


L. MODE SEPARATION

57. Biographer target contains emotionally intense event.
    -> no Therapy route.

58. Biographer discovers cognitive distortion-like wording.
    -> no therapeutic technique selection.

59. Biographer asks historical question.
    -> Journal response posture not invoked.

60. Journal entry containing historical material.
    -> does not itself trigger Biographer coverage selection.


M. REGRESSION

61. CT-V2-04 safety permit qualification remains green.

62. CT-V2-05 deterministic therapy progression remains green.

63. CT-V2-05 anti-repetition remains green.

64. CT-V2-06 longitudinal evidence invariants remain green.

65. CT-V2-07 governed store invariants remain green.

66. CT-V2-08 epistemic/state invariants remain green.

67. CT-V2-09 Journal capture invariants remain green.

68. Journal default remains NO_RESPONSE.

69. Production writers remain zero.

70. Android/app longitudinal writers remain zero.

71. Model-authorized evidence writers remain zero.

72. V1 migration register remains 24/24 DENIED.

73. Canonical .git remains valid.

74. Temporary Git metadata directories remain zero.


======================================================================
35. QUESTION-PLAN QUALIFICATION EXAMPLES
======================================================================

Example A

Known:

    User reported living in Seattle around 2010.
    User reported living in Portland by 2015.
    Little represented evidence exists between those reports.

Permitted semantic target:

    TEMPORAL_GAP

Permitted conceptual question:

    "What do you remember about the period between living in Seattle
     and living in Portland?"

Not permitted:

    "Why did you leave Seattle?"

unless evidence already establishes that the user left Seattle.

Not permitted:

    "What went wrong in Seattle?"


Example B

Known:

    "Sam from work..."
    "My old roommate Sam..."

Identity unresolved.

Permitted:

    "Are those references to the same Sam, different people, or are you
     not sure?"

Not permitted:

    silently merging them.


Example C

Known:

    2012 account
    2013 account
    same event
    contradiction unresolved

Permitted:

    "You've given two different years for that move. Do you remember
     which is closer, or is the date still uncertain?"

Not permitted:

    "So it was actually 2013, right?"


Example D

Target:

    childhood period is sparsely represented

Permitted:

    "What do you remember about that period of your childhood?"

Not permitted:

    "What childhood experiences caused your current problems?"


======================================================================
36. LOGGING / PRIVACY BOUNDARY
======================================================================

Operational logs must not contain:

- Biographer answer bodies;
- source excerpts;
- private content;
- free-form psychological prose;
- rendered question text unless explicitly test-scoped;
- user names or narrative strings merely for diagnostics.

Operational logs may contain:

- IDs;
- target kinds;
- dispositions;
- counts;
- rule versions;
- hashes;
- timing;
- eligibility decisions.

Qualification fixtures may inspect synthetic text in test scope.


======================================================================
37. NO PRODUCTION ANDROID WIRING
======================================================================

CT-V2-10 remains synthetic qualification work.

Do not connect:

    Android Biographer UI
        ->
    plaintext qualification longitudinal store

Do not introduce real user history.

Do not use the Principal's actual biography.

Do not ingest V1 history.

Do not open production personal-data security lifecycle work.


======================================================================
38. DOCUMENTATION
======================================================================

Preserve this work order verbatim at:

    docs/work-orders/CT-V2-10-BIOGRAPHER-COVERAGE-ENGINE.md

Produce at least:

    docs/CT-V2-10-BIOGRAPHER-COVERAGE-ENGINE.md

    docs/biographer/
    CT-V2-10-COVERAGE-MODEL.md

    docs/biographer/
    CT-V2-10-INVESTIGATION-TARGETS.md

    docs/biographer/
    CT-V2-10-TARGET-SELECTION-AND-ANTI-REPETITION.md

    docs/biographer/
    CT-V2-10-QUESTION-AUTHORITY.md

    docs/qualification/
    CT-V2-10-QUALIFICATION.md

Add an ADR if architectural choices justify one.

Update README/module-boundary navigation where appropriate.

Do not substantively change the canonical forward plan.


======================================================================
39. AUTHORITY / BYPASS AUDIT
======================================================================

Demonstrate:

- Biographer coverage does not call Therapy policy;
- Biographer does not invoke Journal response posture;
- questions cannot become user evidence;
- question renderer cannot select targets;
- question renderer cannot change eligibility;
- answer capture uses BIOGRAPHER provenance;
- answers enter durable evidence only through CT-V2-07;
- language interpretation remains CT-V2-08 governed;
- private evidence cannot become a target;
- declined coverage cannot become a target;
- target ranking has no psychological/diagnostic input;
- target selection is deterministic;
- repeated target selection obeys anti-repetition;
- raw operational investigation state does not masquerade as user evidence;
- no Android production writer is added;
- no direct SQL/JDBC writer is added;
- no model-authorized writer is added;
- no V1 writer is added.

Intended counts:

    production Biographer writers:              0
    Android/app Biographer writers:             0
    direct Biographer SQL/JDBC writers:         0
    model-authorized Biographer writers:        0
    Biographer -> Therapy routing paths:         0
    Biographer -> Journal response paths:        0
    question -> user-evidence paths:             0
    private-target selection paths:              0
    declined-target selection paths:             0
    qualification Biographer pipelines:          documented only
    durable user-evidence admission gate:        CT-V2-07 only


======================================================================
40. EXPLICITLY PROHIBITED WORK
======================================================================

Do not implement:

- production Android Biographer UI;
- real-user biography storage;
- full production persistence;
- Room migration for longitudinal data;
- Keystore;
- production encryption claims;
- cloud sync;
- backup/export/recovery;
- deletion lifecycle;
- embeddings;
- semantic retrieval;
- open-ended conversational memory retrieval;
- Therapy use of longitudinal history;
- Therapy rule expansion;
- Journal changes unrelated to regression;
- profile inspector UI;
- diagnostic inference;
- trauma classification;
- psychological scoring;
- LLM target selection;
- LLM evidence extraction;
- GGUF additions;
- model training;
- V1 biography import;
- V1 profile migration;
- safety-policy expansion.

Do not begin the next phase.


======================================================================
41. VCS STABILITY
======================================================================

The permanent canonical Git invariant remains in force.

Throughout CT-V2-10:

- .git remains present;
- .git remains canonical;
- no .git-ct-v2-* directory is created;
- no alternate --git-dir development workaround is used;
- Android Studio project root remains a valid Git root.

Before sealing run:

    tools\verify-canonical-git-root.ps1

and:

    git rev-parse --show-toplevel
    git rev-parse --git-dir
    git status --porcelain=v1
    git fsck --full --strict


======================================================================
42. COMPLETE BUILD QUALIFICATION
======================================================================

From final implementation state run at minimum:

    gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain

Required:

- BUILD SUCCESSFUL;
- all prior tests pass;
- all CT-V2-10 tests pass;
- zero skipped CT-V2-10 acceptance tests;
- zero new lint errors/fatals;
- debug APK assembles;
- unsigned release APK assembles;
- APK contains no Biographer fixture corpus;
- APK contains no qualification longitudinal database;
- APK contains no model artifact;
- no generated database is tracked;
- no raw/restricted fixture data is tracked;
- no build output is tracked;
- application backup remains disabled;
- canonical forward-plan size/hash unchanged;
- all 24 V1 components remain DENIED;
- canonical Git-root check passes;
- temporary Git metadata count remains zero;
- final worktree clean;
- git diff --check passes;
- git fsck --full --strict succeeds.

Record APK sizes and SHA-256 values.

No device, production-storage, real-user, clinical, therapeutic-efficacy, or
production-security qualification is claimed.


======================================================================
43. STOP CONDITIONS
======================================================================

STOP rather than broaden scope if:

- accepted baseline differs;
- canonical .git invariant regresses;
- real user data would be required;
- production plaintext biography storage would be required;
- target selection requires psychological interpretation;
- model judgment is required to rank gaps;
- private or declined areas must be probed;
- an unanswered target must be treated as absence;
- approximate history would need false temporal precision;
- target selection becomes nondeterministic;
- Biographer must invoke Therapy;
- Biographer must invoke Journal response policy;
- question text must become evidence;
- entity ambiguity would have to be forcibly resolved;
- correction would overwrite history;
- CT-V2-07 admission must be bypassed;
- CT-V2-08 epistemic distinctions must be weakened;
- a prior invariant regresses;
- a test must be weakened or skipped to pass.

A Biographer that leaves a gap open is preferable to one that fills the gap
with assumption.


======================================================================
44. COMMITS AND TAG
======================================================================

Use small reviewable commits.

Suitable sequence:

1. Record Biographer coverage and investigation authority.
2. Implement deterministic coverage map.
3. Implement target selection and investigation history.
4. Implement OPEN_STORY and TARGETED_COVERAGE question intents.
5. Implement governed synthetic Biographer answer capture.
6. Add privacy, decline, anti-repetition, and mode-boundary qualification.
7. Record final qualification.

Do not squash meaningful history.

On successful qualification create annotated tag:

    ct-v2-10-biographer-coverage-engine

Tag the exact final qualified HEAD.

Do not add a remote.

Do not push.


======================================================================
45. REQUIRED COMPLETION REPORT
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
- canonical-root verification result;
- confirmation .git was never relocated.

FORWARD PLAN
- path;
- before/after size;
- before/after SHA-256;
- redundant copies.

BIOGRAPHER ARCHITECTURE
- module graph;
- coverage-map representation;
- target types;
- posture types;
- investigation-history representation;
- answer-capture path.

TARGET SELECTION
- priority hierarchy;
- deterministic tie-breaking;
- anti-repetition;
- deferred behavior;
- declined behavior;
- private behavior;
- NO_TARGET behavior.

QUESTION AUTHORITY
- one-question invariant;
- semantic question-plan structure;
- renderer authority;
- unsupported-presupposition protections.

LONGITUDINAL
- BIOGRAPHER provenance;
- event/report/record-time handling;
- Journal/Biographer same-event behavior;
- identity clarification;
- contradiction clarification;
- correction behavior;
- replay/digest behavior.

AUTHORITY
- production Biographer writers;
- Android/app writers;
- SQL/JDBC writers;
- model-authorized writers;
- Biographer->Therapy paths;
- Biographer->Journal paths;
- question->evidence paths;
- V1 writers.

QUALIFICATION
- exact clean command;
- actionable tasks;
- total tests;
- new CT-V2-10 tests;
- failures/errors/skips;
- target-selection tests;
- anti-repetition tests;
- privacy/decline tests;
- provenance tests;
- replay tests;
- lint;
- APK sizes/hashes;
- artifact scan;
- Git integrity.

LIMITATIONS
- synthetic-only;
- no Android Biographer wiring;
- no real user history;
- no model target selection;
- no therapeutic formulation;
- no production privacy lifecycle;
- no production longitudinal authority.

RECOMMENDATION
- read the canonical forward development plan;
- identify the exact next canonical phase;
- state whether it is ready for Principal consideration;
- do not open it.


======================================================================
46. TARGET TERMINAL DISPOSITION
======================================================================

A fully successful phase should be able to report:

CT_V2_10_BIOGRAPHER_COVERAGE_ENGINE_COMPLETE
OPEN_STORY_AND_TARGETED_COVERAGE_POSTURES_QUALIFIED
DETERMINISTIC_LONGITUDINAL_COVERAGE_MAP_QUALIFIED
STRUCTURAL_GAP_SELECTION_QUALIFIED
ONE_QUESTION_INVESTIGATION_AUTHORITY_QUALIFIED
BIOGRAPHER_PROVENANCE_QUALIFIED
USER_SKIP_DEFER_DECLINE_PRIVATE_AUTHORITY_PRESERVED
DETERMINISTIC_TARGET_ANTI_REPETITION_QUALIFIED
UNKNOWN_AND_UNRESOLVED_STATES_PRESERVED
QUESTION_TEXT_USER_EVIDENCE_AUTHORITY_ZERO
BIOGRAPHER_THERAPY_AUTHORITY_ZERO
BIOGRAPHER_JOURNAL_RESPONSE_AUTHORITY_ZERO
CT_V2_07_REMAINS_SOLE_DURABLE_ADMISSION_GATE
CT_V2_08_EPISTEMIC_DISCIPLINE_PRESERVED
ANDROID_STUDIO_CANONICAL_VCS_ROOT_REMAINS_VALID
NO_PRODUCTION_LONGITUDINAL_AUTHORITY_GRANTED
NEXT_PHASE_READY_FOR_PRINCIPAL_CONSIDERATION

The next phase remains unopened pending Principal review.
