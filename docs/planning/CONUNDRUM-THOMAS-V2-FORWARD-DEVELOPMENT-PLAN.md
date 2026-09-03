# CONUNDRUM THOMAS V2

## REVISED FORWARD DEVELOPMENT PLAN

### Post CT-V2-05 — Longitudinal Architecture & State Formation

## STATUS

FORWARD PLAN REVISED

CT-V2-05 is complete and sealed.

Starting point for subsequent work:

* HEAD: `e93ca7e69c81443f62ac0c171decd5116d02aac5`
* Tree: `a58d1ebc0018f31f17ede281adc46d3311d18953`
* Tag: `ct-v2-05-core-ordinary-therapy`
* Branch: `main`
* Worktree: clean
* Remote: none

CT-V2-05 established:

* four ordinary therapeutic pathways;
* deterministic route selection;
* deterministic progression;
* user correction;
* explicit closure;
* anti-repetition;
* mandatory Safety & Scope permits;
* zero LLM therapeutic authority.

The principal technical limitation has therefore changed.

Thomas now knows **what to do given trustworthy structured state**.

Thomas does not yet know how to construct that trustworthy state from ordinary human conversation or connect it to a durable understanding of one person across time.

That becomes the next program.

---

# 1. CANONICAL PRODUCT ARCHITECTURE

Thomas maintains one governed longitudinal understanding of the user.

The three principal modes have distinct relationships to it:

> **BIOGRAPHER BACKFILLS THE USER'S LIFE.**

> **JOURNAL RECORDS THE LIFE NOW BEING LIVED.**

> **THERAPIST DRAWS CAREFULLY GOVERNED HELP FROM BOTH.**

These are not three memory systems.

They are different policies operating around one longitudinal evidence architecture.

The intended system is:

`USER LANGUAGE`

→ `SOURCE RECORD`

→ `CANDIDATE EVIDENCE`

→ `GOVERNED ADMISSION`

→ `LONGITUDINAL RECORD`

→ `BOUNDED RETRIEVAL`

→ `PROCEDURAL POLICY`

→ `AUTHORIZED ACTION`

→ `LANGUAGE RENDERER`

---

# 2. TWO DIFFERENT KINDS OF STATE

Future work must explicitly distinguish:

## TURN STATE

What Thomas presently needs to know in order to make the next procedural decision.

Examples:

* user wants listening;
* problem is not yet established;
* a tentative understanding has been rejected;
* options have already been generated;
* user wants to stop.

This state drives CT-V2-04/05 policy.

## LONGITUDINAL STATE

What Thomas legitimately knows, suspects, or remains uncertain about across the user's life.

Examples:

* a reported event;
* an ongoing relationship;
* a historical period;
* a previous coping attempt;
* a repeated observation;
* a user interpretation;
* a Thomas hypothesis.

These must interact without becoming the same object.

A conversational state may disappear at the end of a session.

A longitudinal fact or evidence record may remain for years.

---

# 3. THE CENTRAL STATE-FORMATION BOUNDARY

The language model must not be asked:

> “What psychological state is this person in?”

and then be allowed to populate Therapist policy fields arbitrarily.

Instead, establish:

`RAW LANGUAGE`

→ `SEMANTIC EVIDENCE CANDIDATES`

→ `GOVERNED ADMISSION / TURN EVIDENCE`

→ `DETERMINISTIC STATE PROJECTION`

→ `THERAPEUTIC POLICY STATE`

For example:

User says:

“I don't really want advice. I just need to get this off my chest.”

The extraction layer may identify candidate evidence equivalent to:

* explicit conversational preference: listening;
* explicit advice preference: not presently wanted.

A deterministic state projector may then establish:

`requestedRoute = LISTEN_SUPPORT`

The model does **not** directly choose `LISTEN_SUPPORT`.

This distinction is foundational.

---

# 4. LONGITUDINAL RECORD MODEL

“One shared record” shall not become one giant mutable psychological profile.

Preserve distinct logical layers.

## Layer A — Source

What the user actually said or wrote.

## Layer B — Evidence Assertions

Structured claims extracted from the source.

## Layer C — Life Structure

Events, people, relationships, places, periods, decisions, behaviors, outcomes, and related entities.

## Layer D — User Interpretation

What the user believes or understands about those facts.

## Layer E — Thomas Hypothesis

A derived possibility proposed by Thomas.

## Layer F — Longitudinal Pattern

A supported but revisable relationship across multiple pieces of evidence.

## Layer G — Retrieval View

A small bounded packet assembled for a specific current purpose.

No downstream layer may erase the provenance of the upstream evidence from which it was derived.

---

# 5. TEMPORAL INVARIANT

Preserve separately:

* event time;
* report time;
* record time.

Support:

* exact dates;
* approximate dates;
* ranges;
* relative periods;
* ongoing states;
* unknown dates;
* uncertain chronology.

A Journal entry does not later need to be moved into Biographer history.

Its event remains anchored in time.

What changes is simply that the present becomes the past.

Its original Journal provenance remains intact.

---

# 6. REVISED PHASE SEQUENCE

The canonical sequence after CT-V2-05 shall be:

**CT-V2-06 — Longitudinal Evidence & Temporal Foundation**

↓

**CT-V2-07 — Governed Admission, Revision & Store**

↓

**CT-V2-08 — Language-to-Evidence & State Formation**

↓

**CT-V2-09 — Journal Capture Engine**

↓

**CT-V2-10 — Biographer Coverage Engine**

↓

**CT-V2-11 — Longitudinal Retrieval & Context Packets**

↓

**CT-V2-12 — Longitudinal Therapist Integration**

↓

**CT-V2-13 — Governed Language Renderer**

↓

**CT-V2-14 — Personal Data Governance & Persistence Qualification**

↓

**CT-V2-15 — Three-Mode Android Integration**

↓

**CT-V2-16 — Longitudinal Pattern Engine**

↓

**CT-V2-17 — Authority Admission / Product Qualification**

↓

**CT-V2-18+ — Broader Therapeutic & Specialized Capability**

---

# 7. CT-V2-06 — LONGITUDINAL EVIDENCE & TEMPORAL FOUNDATION

## Governing question

> **What does it mean for Thomas to know something about a person's life?**

This phase defines the shared canonical model before implementing writers.

### Establish typed representations for:

* SourceRecord;
* EvidenceAssertion;
* LifeEvent;
* Person;
* Relationship;
* Place;
* LifePeriod;
* Role;
* Decision;
* Behavior;
* CopingResponse;
* Outcome;
* UserInterpretation;
* ThomasHypothesis;
* PatternCandidate;
* contradiction;
* correction;
* supersession;
* uncertainty;
* evidence dependency.

### Establish provenance types for at least:

* JOURNAL;
* BIOGRAPHER_OPEN_NARRATIVE;
* BIOGRAPHER_GUIDED_TIMELINE;
* THERAPIST_CONVERSATION;
* USER_CORRECTION.

### Establish temporal representation for:

* event time;
* report time;
* record time.

### Critical qualification cases

Prove:

1. A Journal account and later Biographer recollection of the same event can coexist.
2. Contradictory accounts do not overwrite one another.
3. A user correction can supersede an earlier assertion without deleting history.
4. A Thomas hypothesis cannot be represented as an explicitly stated user fact.
5. Event date can remain uncertain independently of report date.
6. Unknown, private, declined, and irrelevant information remain legitimate states.

No real psychological persistence is required.

---

# 8. CT-V2-07 — GOVERNED ADMISSION, REVISION & LONGITUDINAL STORE

## Governing question

> **What evidence is allowed to change Thomas's durable understanding, and how?**

Implement the persistence and admission boundary.

### Required capabilities

* append-oriented source preservation;
* candidate-evidence submission;
* governed evidence admission;
* source linkage;
* entity linking;
* event linking;
* correction;
* contradiction;
* supersession;
* dependent-hypothesis invalidation or review;
* provenance retention;
* chronological reconstruction.

### Critical invariant

Modes submit evidence.

Modes do not directly mutate psychological truth.

Journal cannot say:

`profile.user_is_avoidant = true`

Biographer cannot say:

`childhood_relationship = resolved`

Therapist cannot turn a useful conversational interpretation directly into durable fact.

All such changes pass through governed admission semantics.

### Qualification mode

Use synthetic data only.

Production psychological persistence remains disabled.

---

# 9. CT-V2-08 — LANGUAGE-TO-EVIDENCE & STATE FORMATION

This becomes the immediate bridge to a functioning Thomas.

## Governing question

> **What did the user actually communicate, and what policy state legitimately follows from that evidence?**

Implement two deliberately separate components.

## A. Evidence Extractor

Input:

`raw user language + tightly bounded relevant context`

Output:

candidate structures such as:

* explicit statement;
* conversational preference;
* event;
* person reference;
* relationship assertion;
* temporal expression;
* feeling report;
* thought report;
* behavior;
* decision;
* outcome;
* correction;
* refusal;
* uncertainty;
* user interpretation.

The extractor proposes evidence.

It does not govern behavior.

## B. Deterministic State Projector

Input:

`admitted/turn evidence + existing procedural state`

Output:

the typed fields required by CT-V2-04 and CT-V2-05.

This layer decides mechanically, for example, that an explicit request to “just listen” satisfies the defined evidence requirement for the LISTEN_SUPPORT route.

The model never directly chooses the route.

---

# 10. CT-V2-08 EXTRACTION DISCIPLINE

Qualification must aggressively test:

* negation;
* ambiguity;
* pronouns;
* uncertain dates;
* corrections;
* hypothetical statements;
* sarcasm where feasible;
* statements about another person;
* quotations of another person;
* user beliefs versus factual assertions;
* current versus historical events;
* explicit versus inferred emotion;
* multiple events in one utterance;
* unsupported inference;
* omission;
* false precision.

Example:

> “My dad probably hated that job.”

must not automatically become:

`father hated job = FACT`.

It may become:

`USER_INTERPRETATION(father_hated_job)`

with appropriate uncertainty.

The original source remains authoritative evidence of what was actually said.

---

# 11. CT-V2-08 END-TO-END PROOF

By completion, qualification should demonstrate:

`NATURAL USER UTTERANCE`

→ `EVIDENCE CANDIDATES`

→ `DETERMINISTIC STATE`

→ `SAFETY/SCOPE GATE`

→ `ORDINARY THERAPY PERMIT`

→ `CT-V2-05 ROUTE`

→ `GOAL`

→ `ACTION`

without giving the extraction model authority over the action.

This is the next major architectural proof.

---

# 12. CT-V2-09 — JOURNAL CAPTURE ENGINE

## Governing question

> **What should be preserved from this entry?**

Journal becomes the first real longitudinal acquisition policy.

Core flow:

`ENTRY`

→ `SOURCE PRESERVATION`

→ `EVIDENCE EXTRACTION`

→ `ADMISSION`

→ `LONGITUDINAL LINKAGE`

Response behavior remains independently governed.

## Response levels

Preserve:

* RECORD ONLY;
* ACKNOWLEDGE;
* REFLECT;
* EXPLORE.

`RECORD ONLY` should remain the default unless product testing gives strong reason otherwise.

### Critical invariant

`NO_RESPONSE != NO_MEMORY`

A silent Journal entry must still undergo authorized capture and admission.

### Explore boundary

Journal `EXPLORE` may authorize bounded Journal-oriented inquiry.

It does not grant Therapist authority.

If the user requests therapeutic help, the system should perform an explicit mode/policy handoff rather than quietly turning Journal into Therapist.

---

# 13. CT-V2-10 — BIOGRAPHER COVERAGE ENGINE

## Governing question

> **What permissible part of the user's past is worth learning about next?**

Support:

### OPEN NARRATIVE

User chooses the subject.

Thomas follows and structures.

### GUIDED TIMELINE COMPLETION

Thomas selects a meaningful gap and invites discussion.

## Coverage states

Support concepts equivalent to:

* UNKNOWN;
* PARTIAL;
* SUFFICIENT;
* PRIVATE;
* DECLINED;
* IRRELEVANT;
* UNRESOLVED;
* NOT_EXPLORED.

There is no target of “100% biography completion.”

A declined subject may be permanently complete from Biographer's perspective.

## Gap-selection considerations

Where procedurally appropriate:

* importance;
* temporal coverage;
* major transitions;
* unresolved entity identity;
* thinly understood relationships;
* contradiction;
* existing evidence density;
* user interest;
* sensitivity;
* prior refusal;
* repetition.

The coverage engine chooses the information need.

A renderer merely phrases the question.

---

# 14. BIOGRAPHER SAFETY BOUNDARY

Biographer remains investigation, not therapy.

If historical discussion produces evidence that changes safety/scope authority:

`BIOGRAPHER`

→ `SAFETY/SCOPE REASSESSMENT`

→ appropriate governed disposition.

Biographer must not improvise a therapeutic intervention because a biography becomes emotionally difficult.

Likewise, Therapist must not begin interrogating the user's childhood merely because the information might be interesting.

Mode authority remains explicit.

---

# 15. CT-V2-11 — LONGITUDINAL RETRIEVAL & CONTEXT PACKETS

## Governing question

> **What is the smallest legitimate slice of longitudinal evidence needed for the present task?**

Build purpose-specific retrieval.

A Therapist packet might contain:

* current problem-relevant events;
* previously reported similar situations;
* prior attempted coping strategies;
* outcomes;
* relevant user preferences;
* source provenance;
* uncertainty;
* contradictions.

A Biographer packet might instead contain:

* timeline coverage;
* known people;
* known transitions;
* gaps;
* previous refusals.

A Journal linkage packet may contain:

* candidate matching people;
* current ongoing events;
* existing open decisions.

Do not use one generic “memory dump.”

---

# 16. RETRIEVAL PRIVILEGE

Possession of information does not imply every subsystem should receive it.

Retrieval should be:

* purpose-bound;
* mode-aware;
* minimal;
* provenance-preserving.

Therapist should not routinely receive an entire biography.

Biographer should not receive unrelated intimate Journal details merely because they exist.

Future privacy controls should be compatible with evidence-level or domain-level exclusion.

---

# 17. CT-V2-12 — LONGITUDINAL THERAPIST INTEGRATION

This phase joins the already-proven Therapist to the longitudinal architecture.

## Governing question

> **How should relevant prior evidence alter the procedural decision now?**

Examples may include:

* avoid asking for something already known;
* recognize that a strategy was previously attempted;
* recognize that it previously helped;
* recognize that it previously failed;
* recall an explicit preference;
* connect a current problem to a previously established event only when policy permits;
* recognize recurrence;
* identify changed circumstances.

The retrieval engine supplies evidence.

The therapeutic policy decides what to do with it.

The model does neither.

---

# 18. GOVERNED FORMULATION & OUTCOME REVISION

CT-V2-12 must also close the limitation identified by CT-V2-05 around outcome/formulation revision.

A therapeutic conversation may create new evidence such as:

* user confirms Thomas understood correctly;
* user rejects an interpretation;
* plan was attempted;
* plan was not attempted;
* outcome helped;
* outcome did not help;
* user changed their understanding.

These should flow:

`THERAPIST CONVERSATION`

→ `NEW SOURCE/EVIDENCE`

→ `GOVERNED ADMISSION`

→ `LONGITUDINAL REVISION`

A therapeutic hypothesis may be:

* supported;
* weakened;
* contradicted;
* superseded;
* left unresolved.

It must not simply overwrite the profile.

---

# 19. CT-V2-13 — GOVERNED LANGUAGE RENDERER

Only after:

* therapeutic judgment works;
* safety authority works;
* state formation works;
* Journal capture works;
* Biographer selection works;
* longitudinal retrieval works;

should conversational language generation become a shared production component.

The renderer receives an already authorized contract.

For Therapist:

`selected therapeutic act`

For Biographer:

`selected information need/question act`

For Journal:

`selected response level/action`

The model may vary expression.

It may not alter:

* mode;
* policy goal;
* question count;
* therapeutic act;
* evidence status;
* authority;
* safety disposition.

---

# 20. MODEL RESPONSIBILITY CONTRACT

At this point the model should have only two major intelligent responsibilities:

## Extraction

> “What candidate information appears to have been communicated?”

## Rendering

> “How can this already-authorized semantic act be expressed naturally?”

It does **not** own:

* therapy selection;
* Biographer gap selection;
* Journal memory decision;
* safety authority;
* durable evidence admission;
* longitudinal truth;
* retrieval authority.

This should remain the fundamental Thomas V2 model boundary.

---

# 21. CT-V2-14 — PERSONAL DATA GOVERNANCE & PERSISTENCE QUALIFICATION

Before real longitudinal psychological data becomes an ordinary product feature, qualify the data lifecycle explicitly.

Address:

* local-at-rest protection;
* database access boundary;
* Android backup policy;
* export;
* deletion;
* full reset;
* selective source deletion;
* derived-data consequences;
* orphaned hypothesis handling;
* corruption/recovery;
* migration;
* retention;
* provenance after correction;
* backup/restore if later enabled.

Deletion semantics require particular care.

Deleting an original Journal entry cannot leave unsupported derived psychological conclusions silently behind.

No production psychological persistence should be admitted before this phase succeeds.

---

# 22. CT-V2-15 — THREE-MODE ANDROID INTEGRATION

Now build the actual Thomas experience around the proven architecture.

Top-level experience:

* JOURNAL;
* BIOGRAPHER;
* THERAPIST.

## Journal controls

Expose response level clearly:

* Record only;
* Acknowledge;
* Reflect;
* Explore.

Memory capture remains independent of response level.

## Therapist controls

Expose appropriate response/interaction controls derived from the product design rather than arbitrary model personalities.

## Speech

Correct the earlier V1 interaction ambiguity.

Distinguish explicitly:

* **Done / finish speaking** — retain speech and continue;
* **Cancel / discard** — abandon the capture.

Do not make Cancel mean “I am finished talking.”

## Orientation/state

Preserve conversation and active mode through normal device state changes.

---

# 23. CT-V2-16 — LONGITUDINAL PATTERN ENGINE

Only after the evidence system is functioning should Thomas infer higher-order patterns.

Candidate patterns include:

* recurrence;
* change over time;
* repeated triggers;
* repeated responses;
* repeated outcomes;
* relationships between decisions and outcomes;
* shifting interpretations;
* differences between retrospective and contemporaneous accounts.

Patterns must remain:

* evidence-linked;
* inspectable;
* uncertain;
* revisable;
* invalidatable.

A pattern is not a personality label.

A repeated event does not automatically establish a psychological trait.

---

# 24. CT-V2-17 — AUTHORITY ADMISSION & PRODUCT QUALIFICATION

Up to this point large portions of the system may remain qualification-only.

Before genuine therapeutic production authority, explicitly adjudicate:

* clinical review;
* rights;
* commercial use;
* legal considerations;
* software autonomy;
* implementation scope;
* training/deliverer assumptions;
* supervision assumptions;
* renderer fidelity;
* extraction reliability;
* data governance;
* device behavior.

Do not let “technically works” become “clinically approved” through momentum.

Authority must move through explicit governed state.

---

# 25. CT-V2-18+ — CAPABILITY EXPANSION

Only after the integrated foundation is secure should the program aggressively expand therapeutic breadth.

Candidate future work includes:

* additional ordinary pathways;
* behavioral activation;
* coping procedures;
* additional problem-solving depth;
* motivational procedures;
* structured self-help;
* specialized safety policies;
* harm-to-others policy;
* supported populations beyond the initial scope;
* richer Biographer coverage strategy;
* richer Journal observation;
* more sophisticated longitudinal therapeutic use.

Each new capability should reuse the established architecture:

`SOURCE`

→ `ONTOLOGY`

→ `POLICY`

→ `QUALIFICATION`

→ `REVIEW`

→ `AUTHORITY`

rather than becoming an LLM prompt feature.

---

# 26. CROSS-PHASE INVARIANTS

The following are now governing product architecture.

## ONE LIFE, MULTIPLE ACQUISITION PATHS

Biographer and Journal contribute to one longitudinal record.

## ORIGINAL EVIDENCE SURVIVES DERIVATION

Structured understanding does not erase what the user actually said.

## EVENT TIME != REPORT TIME

Retrospective and contemporaneous accounts remain distinguishable.

## FACT != INTERPRETATION != HYPOTHESIS

These distinctions are structural.

## CONTRADICTIONS ARE INFORMATION

They are preserved, not silently repaired.

## CORRECTIONS PROPAGATE

Dependent interpretations must be reviewable when underlying evidence changes.

## UNKNOWN IS VALID

Thomas is not required to fill every blank.

## PRIVATE AND DECLINED ARE VALID

Biographer must not treat them as retry queues.

## JOURNAL RESPONSE != JOURNAL MEMORY

Record-only entries still contribute to governed longitudinal understanding.

## RETRIEVAL IS BOUNDED

The Therapist gets relevant evidence, not unrestricted biography.

## SAFETY PRECEDES ORDINARY THERAPY

The CT-V2-04 permit boundary remains mandatory.

## PROGRESSION IS STATE-BASED

The CT-V2-05 anti-repetition architecture remains mandatory.

## LLM DOES NOT GOVERN

Models extract candidates and render authorized acts.

They do not become Thomas.

---

# 27. WHY THE SEQUENCE CHANGED

The earlier forward plan placed language-to-evidence work after Journal, Biographer, and retrieval.

CT-V2-05 demonstrates that this is now unnecessarily late.

The actual blocking dependency is:

> **Thomas's procedural mind is ready, but real language does not yet produce trustworthy governed state.**

Accordingly:

1. Define the shared longitudinal target.
2. Define how evidence enters and changes it.
3. Immediately solve language-to-evidence and deterministic state projection.
4. Build Journal and Biographer on that real acquisition boundary.
5. Build bounded retrieval.
6. Connect longitudinal evidence to the Therapist.
7. Add natural rendering only after Thomas already knows what he means.

This preserves the successful V2 development philosophy:

> **Separate understanding from authority.**

and:

> **Separate judgment from language generation.**

---

# 28. NEXT AUTHORIZED PHASE RECOMMENDATION

The immediate next work order should be:

> **CT-V2-06 — LONGITUDINAL EVIDENCE & TEMPORAL FOUNDATION**

Do not begin with the database.

Do not begin with the LLM.

Do not begin with Journal UI.

First define precisely what it means for Thomas to know:

> something happened;

> someone was involved;

> the user reported it later;

> the user believed something about it;

> Thomas formed a hypothesis about it;

> another account contradicted it;

> the user later corrected it;

> and none of those facts need to be destroyed for Thomas's understanding to evolve.

Once that foundation exists, CT-V2-07 can make it durable and CT-V2-08 can finally connect natural human conversation to the procedural Thomas already proven in CT-V2-05.

---

# 29. PROGRAM MILESTONES

The V2 program has now passed its first major milestone:

> **Thomas can procedurally decide how to respond.**

The next milestone should be:

> **Thomas can reliably understand what the user actually communicated.**

Then:

> **Thomas can remember one life correctly across time.**

Then:

> **Thomas can use that memory selectively and appropriately.**

And finally:

> **Thomas can express all of this naturally without surrendering control to the language model.**

That is now the forward path.
