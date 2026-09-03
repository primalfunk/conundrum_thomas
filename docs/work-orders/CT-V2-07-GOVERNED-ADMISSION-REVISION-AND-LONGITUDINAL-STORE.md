CONUNDRUM THOMAS V2
IMPLEMENTATION WORK ORDER

CT-V2-07 — GOVERNED ADMISSION, REVISION & LONGITUDINAL STORE

PRINCIPAL DISPOSITION

CT_V2_06_ACCEPTED
CT_V2_06_LONGITUDINAL_FOUNDATION_SEALED
CT_V2_07_AUTHORIZED
CT_V2_08_NOT_AUTHORIZED


======================================================================
1. PURPOSE
======================================================================

Implement the first durable longitudinal store for Conundrum Thomas V2 and
place every write behind a deterministic, versioned admission and revision
authority.

CT-V2-06 proved that Thomas can represent longitudinal evidence honestly.
CT-V2-07 must prove that this evidence can be admitted, revised, persisted,
reopened, audited, and deterministically reconstructed without allowing any
caller—especially an LLM, renderer, application component, or migrated V1
component—to mutate longitudinal state directly.

This phase establishes mechanics, not intelligence.

It must not extract evidence from natural language, decide what a user meant,
generate psychological interpretations, retrieve memories for conversation,
operate any user-facing mode, or authorize real personal use.

The successful result is a synthetic-only, qualification-only longitudinal
ledger and governed write boundary upon which CT-V2-08 can later build
language-to-evidence formation.


======================================================================
2. ACCEPTED ENTRY BASELINE
======================================================================

Work only from the following accepted CT-V2-06 state:

Repository:
    C:\Android Studio Projects\ConundrumThomasV2

Branch:
    main

Required HEAD:
    074df53b21361a6051e61c7c14e3d78dc91de9fc

Required tree:
    b3d0efea7987770c62d9026988dd8fb7863004cb

Required annotated tag:
    ct-v2-06-longitudinal-foundation

Required tag object:
    18260b701e5be85543178e187da8da5479f147c4

Required tag target:
    074df53b21361a6051e61c7c14e3d78dc91de9fc

Required starting worktree:
    clean

Expected remote state:
    no remote

Canonical forward plan:
    docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md

Required forward-plan size:
    25,031 bytes

Required forward-plan SHA-256:
    bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886

Before changing any tracked file:

1. Verify HEAD, tree, branch, worktree, tag object, and tag target.
2. Verify the forward-plan path, size, and SHA-256.
3. Verify that no root fdp.txt or redundant forward-plan copy exists.
4. Verify that :thomas:longitudinal remains isolated as reported.
5. Verify that Android, app, runtime, engine, safety, renderer, speech, and
   production persistence code do not consume it.
6. Verify that all CT-V2-06 tests pass before implementation begins.

If any required baseline fact differs, stop without implementation and report
the discrepancy. Do not normalize, repair, reset, stash, or reinterpret an
unexpected repository state.

Preserve this work order verbatim at:

    docs/work-orders/CT-V2-07-GOVERNED-ADMISSION-REVISION-AND-LONGITUDINAL-STORE.md

Do not alter the content of the canonical forward plan during this phase.


======================================================================
3. GOVERNING INVARIANTS
======================================================================

The following are non-negotiable.

3.1 Thomas owns admission policy

Only deterministic Thomas code may decide whether a structured write is
admissible.

A model may eventually propose structured candidates, but it may never:

- write directly to the store;
- construct an accepted admission receipt;
- assign user authorship;
- convert its own prose into user evidence;
- decide that two entities are the same;
- erase a contradiction;
- backdate record time;
- silently increase temporal precision;
- overwrite a source or assertion;
- decide that private or declined material is usable;
- issue a user correction;
- mutate a projection through a DAO or database handle.

There must be no model in CT-V2-07.

3.2 Source evidence and derived state remain structurally separate

The store must preserve the CT-V2-06 distinction among:

- original source records;
- source revisions;
- explicit user assertions;
- user interpretations;
- Thomas hypotheses;
- entities and entity-reference candidates;
- contradictions;
- corrections;
- supersessions;
- identity-link decisions;
- coverage and privacy states;
- dependency relations;
- admission and revision audit records.

A Thomas hypothesis is not a source record.

A Thomas response is not a user assertion.

A user belief about another person is not direct evidence of that other
person’s internal state.

A correction does not delete the corrected material.

3.3 Longitudinal history is append-oriented

Accepted evidence must never be silently rewritten in place.

Edits and corrections create new durable revisions or relations. The original
representation remains inspectable.

Current-state projections may change, but the events that caused those changes
must remain reconstructable.

3.4 One longitudinal substrate serves all modes

Journal, Biographer, and Therapist-conversation evidence must be capable of
coexisting in one store while retaining distinct acquisition provenance.

This does not authorize implementation or application wiring for any mode.

Journal must continue to default to NO_RESPONSE.

Biographer must remain investigation-only.

CT-V2-04 safety permits and CT-V2-05 therapeutic progression must remain
unchanged and unwired from the new store.

3.5 Temporal honesty is mandatory

Event time, report time, and record time remain independent.

The store must not turn:

- approximate time into exact time;
- a range into a single instant;
- relative time into an invented calendar date;
- ongoing time into a completed interval;
- uncertain time into settled time;
- unknown time into absence.

Record time must be assigned by trusted store-side machinery using an injected
clock for deterministic qualification. Ordinary callers must not be able to
backdate or fabricate it.

3.6 Contradiction is valid state

Contradictory admissible evidence must coexist.

The admission controller must not select a winner merely because two accounts
conflict. Corrections and explicit supersessions may alter current authority,
but the historical conflict must remain visible.

3.7 Identity uncertainty is valid state

Possible references to the same person, place, event, period, relationship, or
role may remain unresolved indefinitely.

No alias, matching name, content similarity, or repeated reference may cause an
automatic merge.

Identity resolution must itself be explicit, revisable, and audited.

3.8 Private and declined are valid—not missing—states

PRIVATE material may remain stored while being excluded from derivation and
ordinary retrieval eligibility.

DECLINED records that the user declined to provide coverage. It is not evidence
that an event did not happen or that a condition is absent.

Neither PRIVATE nor DECLINED material may silently support a hypothesis.

3.9 No real user data

All CT-V2-07 fixtures and databases must contain synthetic information only.

No existing V1 journal, therapy, biography, profile, speech, or user database
may be opened, copied, imported, inspected, or migrated.


======================================================================
4. REQUIRED MODULE AND DEPENDENCY BOUNDARY
======================================================================

Keep :thomas:longitudinal platform-independent and free of persistence
dependencies.

Add the minimum modules necessary to separate:

1. longitudinal domain representation;
2. deterministic admission and revision policy;
3. durable store implementation;
4. qualification fixtures and harnesses.

The preferred module structure is:

    :thomas:longitudinal
    :thomas:longitudinal-admission
    :thomas:longitudinal-store

Equivalent names may be used only when existing repository conventions make
them materially better. Any variation must preserve the same dependency
direction and be documented.

Required dependency direction:

    longitudinal-store
        -> longitudinal-admission
        -> longitudinal

The domain module must not depend on the admission or store modules.

Android, app, runtime, engine, safety, renderer, speech, provenance, and other
production-facing modules must not gain a dependency on the admission or store
implementation during CT-V2-07.

Only qualification code may exercise the new complete write path.

No database implementation type, SQL connection, DAO, transaction handle, or
mutable record class may escape the store boundary.


======================================================================
5. STORAGE POSTURE
======================================================================

5.1 Qualification-only relational store

Implement a versioned relational store capable of:

- durable file-backed operation;
- in-memory qualification operation where useful;
- complete close and reopen;
- transactional writes;
- foreign-key enforcement;
- explicit schema versioning;
- deterministic migration from an empty database;
- logical reconstruction of current state from durable history;
- deterministic canonical-state digest generation;
- safe failure on unsupported schema versions.

A SQLite-based JVM qualification adapter is appropriate.

Do not introduce Android Room or wire an Android database in this phase.

Do not introduce network storage, cloud synchronization, remote backup, or a
service process.

5.2 Security classification

The CT-V2-07 store must identify itself structurally and in documentation as:

    SYNTHETIC_QUALIFICATION_ONLY

It must not be represented as suitable for real psychological information.

Qualification database files must:

- live only under build, test, or explicitly temporary locations;
- be excluded from Git;
- be excluded from APK assets and resources;
- be excluded from ordinary application packaging;
- contain synthetic fixtures only;
- be removed by clean or explicit qualification cleanup;
- never be copied into V1 or production application-data paths.

Do not invent token encryption, XOR obfuscation, hard-coded keys, or any other
mechanism that creates a false encryption claim.

Production encryption, Android Keystore integration, recovery-key policy,
backup/export controls, physical deletion, lost-device behavior, and complete
data-retention governance remain unopened for CT-V2-14.

The store architecture must make those later protections possible, but
CT-V2-07 must not claim them.

5.3 No plaintext leakage through diagnostics

Ordinary logs, exception messages, test names, admission traces, and audit
receipts must not reproduce synthetic source content except inside explicitly
scoped fixture assertions.

Persisted audit information should use IDs, operation kinds, policy versions,
reason codes, and hashes where necessary—not duplicate full source text.

Tests must demonstrate that a rejected admission does not place its submitted
content in ordinary logs or persisted rejection records.


======================================================================
6. GOVERNED ADMISSION CONTRACT
======================================================================

Create a deterministic, versioned admission contract.

Every attempted write must enter through one governed controller and carry,
directly or through a trusted envelope:

- unique request identity;
- idempotency key;
- expected store or aggregate revision where applicable;
- declared actor/origin;
- declared operation;
- admission-policy version;
- structured payload;
- source and dependency references;
- synthetic qualification authority.

The result must be typed and must distinguish at least:

- ACCEPTED;
- IDEMPOTENT_REPLAY;
- REJECTED_VALIDATION;
- REJECTED_AUTHORITY;
- REJECTED_STALE_REVISION;
- REJECTED_IDEMPOTENCY_CONFLICT;
- REJECTED_MISSING_REFERENCE;
- REJECTED_DEPENDENCY;
- REJECTED_PRIVACY;
- REJECTED_TEMPORAL_DISHONESTY;
- REJECTED_RELATION_CYCLE;
- REJECTED_SCHEMA_OR_STORE_STATE;
- FAILED_WITHOUT_COMMIT.

An accepted receipt must expose:

- request ID;
- idempotency key;
- admission-policy version;
- operation type;
- prior store revision;
- resulting store revision;
- affected stable IDs;
- durable ledger-event IDs;
- record time assigned by the store;
- deterministic reason or decision trace;
- redacted payload fingerprint where useful.

A rejected request must make no partial domain change.

The same idempotency key and identical logical request must return the original
accepted result without creating another record or revision.

The same idempotency key with a different logical payload must fail closed.


======================================================================
7. REQUIRED WRITE OPERATIONS
======================================================================

Implement governed equivalents of the following capabilities. Exact class and
method names may follow repository conventions.

7.1 Admit source record

Admit an immutable initial source with:

- stable source identity;
- acquisition mode;
- speaker/author role;
- input origin;
- original content or governed reference;
- event-time representation;
- report time;
- store-assigned record time;
- source provenance;
- privacy/coverage eligibility;
- synthetic-fixture classification.

The source must be committed independently of later derived analysis.

7.2 Append source revision

A correction to source wording must append a revision linked to the prior
revision.

It must not:

- replace the original content;
- change the source’s stable identity;
- fabricate a new event time;
- erase provenance;
- cause an unrelated source to change.

7.3 Admit structured evidence bundle

Admit an atomic, structured bundle containing some combination of:

- evidence assertions;
- entities;
- event or period references;
- relationships;
- roles;
- behaviors;
- coping responses;
- outcomes;
- temporal claims;
- supporting and contradicting dependencies;
- unresolved identity candidates.

All referenced source and entity IDs must exist or be created atomically in the
same accepted bundle.

A failed member invalidates the complete transaction.

7.4 Record explicit user correction

An explicit user correction must be represented as new user-origin evidence
linked to what it corrects.

The original assertion or hypothesis must remain inspectable.

A user correction may cause prior current-state material to become superseded,
contested, or review-required, but it must not erase history.

No system or Thomas-origin request may masquerade as a user correction.

7.5 Record supersession or retirement

Supersession must be explicit, append-only, typed, and cycle-free.

Retired or superseded material remains visible in history but is excluded from
ordinary current-state reads.

The store must not infer supersession merely from recency.

7.6 Record contradiction

A contradiction relation must preserve both endpoints.

Creating a contradiction must not automatically weaken, delete, or resolve
either endpoint unless a separate governed revision explicitly does so.

7.7 Record and revise identity links

Support:

- unresolved possible-reference links;
- explicit resolution;
- explicit rejection;
- later revision of a prior resolution;
- preservation of every prior decision.

Resolving identity must not physically merge or delete the original referenced
objects.

7.8 Change privacy or coverage state

Support governed transitions involving applicable CT-V2-06 coverage states,
including PRIVATE and DECLINED.

A transition to PRIVATE must immediately exclude that material from eligible
derived/current views.

Existing derived objects depending on newly private evidence must be
deterministically marked ineligible or review-required. They must not remain
silently active.

A DECLINED state must never be converted into a negative factual assertion.

Full user-facing deletion and irreversible purge remain deferred.


======================================================================
8. VALIDATION AND AUTHORITY RULES
======================================================================

The admission controller must reject at least the following:

- duplicate stable IDs outside an idempotent replay;
- references to nonexistent sources, assertions, entities, or revisions;
- direct construction of accepted ledger events by callers;
- ordinary caller-supplied record times;
- record times earlier than their trusted transaction boundary;
- malformed or impossible temporal representations;
- false precision introduced between revisions;
- correction links without a valid prior object;
- self-correction or supersession relations where prohibited;
- correction and supersession cycles;
- hypothesis-dependency cycles;
- identity-resolution cycles or inconsistent active resolutions;
- direct-fact classification for a user interpretation about another person;
- user-assertion classification for Thomas-origin content;
- Thomas hypotheses without inspectable dependencies;
- hypotheses whose dependency chain cannot ultimately reach admissible source
  evidence or explicit user correction;
- use of PRIVATE material as active support;
- use of DECLINED coverage as substantive evidence;
- source revisions that silently alter source identity or acquisition mode;
- attempts to mutate an old ledger event;
- stale optimistic-concurrency revisions;
- idempotency-key reuse with changed payload;
- non-synthetic data origin in the qualification store;
- unsupported schema or policy versions.

Validation must be deterministic.

The same logical store state and same request must produce the same decision,
apart from explicitly injected IDs and clock values that are then fixed in the
durable event.


======================================================================
9. REVISION AND DEPENDENCY BEHAVIOR
======================================================================

Implement explicit dependency consequences without introducing psychological
judgment.

When supporting evidence is:

- corrected;
- superseded;
- retired;
- made private;
- invalidated by identity revision;

the store must identify every current dependent object.

The deterministic response is to mark affected derived material as
review-required, dependency-blocked, contested, or otherwise ineligible
according to a documented state machine.

Do not automatically decide that a hypothesis remains true or becomes false.

That kind of interpretation belongs to later governed state-formation and
pattern phases.

User correction has precedence over a conflicting Thomas interpretation in
ordinary current-state eligibility, while both remain available in historical
inspection.

Dependency state changes and their causes must be preserved as durable revision
events.


======================================================================
10. DURABLE LEDGER AND PROJECTION
======================================================================

The store must maintain a durable, ordered account of accepted operations.

Required properties:

- monotonically increasing store revision;
- immutable accepted ledger events;
- atomic event-plus-projection update;
- no successful projection change without a corresponding ledger event;
- no successful ledger event without the corresponding projection change;
- deterministic replay into an empty compatible store;
- deterministic logical-state digest after replay;
- current-state reads derived from accepted history;
- as-of-revision reads;
- complete revision history by stable object ID;
- dependency and dependent traversal;
- unresolved identity-link inspection;
- privacy/coverage eligibility inspection.

Physical row order, database-generated internal row IDs, or transaction timing
must not affect the canonical logical digest.

Introduce a controlled fault-injection seam for qualification. A simulated
failure between planned write steps must leave either the entire transaction
committed or none of it committed.


======================================================================
11. READ BOUNDARY
======================================================================

Provide exact, deterministic read operations needed to qualify the store,
including:

- source by stable ID;
- current source revision;
- complete source revision history;
- evidence assertion by stable ID;
- current and historical lifecycle states;
- entity by stable ID;
- event and temporal representation;
- direct dependencies;
- direct dependents;
- correction, contradiction, and supersession relations;
- unresolved and resolved identity links;
- coverage and privacy state;
- state as of a specified store revision;
- complete redacted admission history;
- canonical logical-state digest.

Do not implement:

- semantic search;
- embeddings;
- lexical ranking;
- conversational memory selection;
- mode-specific retrieval;
- context-packet construction;
- “most relevant” evidence;
- pattern detection;
- hypothesis generation.

Those belong to later phases.


======================================================================
12. REQUIRED QUALIFICATION CORPUS
======================================================================

Use synthetic fixtures only.

At minimum, qualify all nine CT-V2-06 demonstrated cases through actual durable
admission, close, reopen, and readback:

1. Contemporaneous Journal event.
2. Retrospective Biographer account referring to the same event.
3. Contradictory accounts preserved together.
4. Explicit correction preserving the original assertion.
5. User belief about another person retained as interpretation.
6. Thomas hypothesis retained separately with visible dependencies.
7. “Around 2012” preserved without false precision.
8. Two possible references to “Sam” left unresolved.
9. PRIVATE and DECLINED coverage represented as valid states.

Add deterministic qualification for at least the following:

10. A file-backed database survives complete close and reopen.
11. Replaying the ledger into an empty store produces the same logical digest.
12. Same idempotency key plus same request creates no duplicate.
13. Same idempotency key plus changed request is rejected.
14. A stale expected revision is rejected without mutation.
15. A missing source dependency rejects the complete bundle.
16. A duplicate stable ID rejects the complete bundle.
17. A hypothesis without ultimate source support is rejected.
18. Thomas-origin content cannot be admitted as an explicit user assertion.
19. System-origin content cannot create a user correction.
20. Correction preserves original content and every prior revision.
21. Correction marks affected derived dependents review-required.
22. Contradictory evidence remains simultaneously inspectable.
23. Supersession cycles are rejected.
24. Hypothesis-dependency cycles are rejected.
25. Identity uncertainty survives restart without compulsory merging.
26. An identity resolution can later be revised without deleting either
    original entity.
27. PRIVATE evidence cannot support a new derived assertion.
28. Making previously eligible evidence PRIVATE removes dependent material from
    ordinary current-state eligibility.
29. DECLINED coverage cannot be queried as evidence of absence.
30. Exact, approximate, ranged, relative, ongoing, uncertain, and unknown times
    round-trip without precision change.
31. Callers cannot supply or backdate record time.
32. Store-assigned record time is deterministic under an injected fixture
    clock.
33. An injected mid-transaction failure leaves no partial domain state.
34. Unsupported schema version fails closed.
35. Corrupt or inconsistent persistent state is not silently repaired into a
    different meaning.
36. Rejected content is absent from ordinary logs and persisted audit text.
37. Qualification cleanup leaves no fixture database outside ignored build or
    temporary paths.
38. Android, app, runtime, engine, safety, renderer, speech, and persistence
    production modules remain unwired.
39. CT-V2-04 permit behavior remains qualified.
40. CT-V2-05 deterministic progression and anti-repetition remain qualified.
41. The complete existing project test suite remains green.

Where useful, run randomized operation-order/property tests with a fixed,
reported seed. Randomized testing must supplement—not replace—the named
deterministic fixtures.


======================================================================
13. REQUIRED DOCUMENTATION
======================================================================

Produce at least:

    docs/CT-V2-07-GOVERNED-ADMISSION-REVISION-AND-LONGITUDINAL-STORE.md

    docs/longitudinal/CT-V2-07-ADMISSION-CONTRACT.md

    docs/longitudinal/CT-V2-07-REVISION-AND-DEPENDENCY-STATE-MACHINE.md

    docs/longitudinal/CT-V2-07-RELATIONAL-STORE-SCHEMA.md

    docs/security/CT-V2-07-SYNTHETIC-STORE-SECURITY-BOUNDARY.md

    docs/qualification/CT-V2-07-QUALIFICATION.md

Record architectural decisions covering:

- append-only ledger plus current projection;
- admission-controller exclusivity;
- idempotency and optimistic concurrency;
- store-assigned record time;
- qualification-only SQLite posture;
- deliberate deferral of production encryption and Android storage;
- privacy-state effects on dependent derived material;
- explicit exclusion of safety-state persistence;
- explicit prohibition on LLM write authority.

Document every table or persisted record family, its authority, and whether it
is canonical evidence, derived projection, or audit material.

Document the schema version and a deterministic schema fingerprint.

Link CT-V2-07 documentation from the README and existing architecture index
where appropriate.

Do not rewrite the forward development plan.


======================================================================
14. EXPLICITLY PROHIBITED WORK
======================================================================

Do not begin or partially implement CT-V2-08 or later work.

Specifically prohibited:

- natural-language extraction;
- LLM calls;
- prompts;
- model adapters;
- evidence proposal generation from prose;
- automated psychological inference;
- profile or pattern generation;
- embeddings;
- semantic or lexical retrieval;
- Journal UI or runtime wiring;
- Biographer UI or interview logic;
- Therapist UI or longitudinal retrieval;
- renderer integration;
- TTS or STT work;
- Room;
- Android Keystore;
- production encryption claims;
- backup or restore;
- export;
- irreversible deletion;
- remote synchronization;
- cloud services;
- telemetry;
- production data-retention rules;
- safety-state persistence;
- crisis or specialized therapeutic procedures;
- V1 data import;
- V1 schema migration;
- V1 therapeutic-controller reuse;
- model or adapter artifacts;
- real user psychological information;
- production longitudinal authority.

Do not broaden CT-V2-04 or CT-V2-05 therapeutic authority.

Do not convert the NDK llvm-strip sandbox limitation into unrelated native
build work. This phase does not authorize native-library changes.


======================================================================
15. BYPASS PROOF
======================================================================

Demonstrate structurally and through tests that:

- no public store API can mutate domain state without the admission controller;
- no DAO or SQL connection is available to app or runtime code;
- no production module depends on the store implementation;
- no model-facing interface exists;
- no accepted receipt can be constructed by an untrusted caller;
- no ordinary caller can assign record time;
- no current projection can change without an accepted ledger event;
- no ledger event can be altered after acceptance;
- no rejected request leaves partial state;
- no private or declined evidence can bypass eligibility checks;
- no stale revision can overwrite newer state;
- no idempotent retry creates duplicate evidence;
- no source correction overwrites original evidence;
- no identity operation silently merges original entities.

Report the number of discovered write paths and classify every one.

The intended accepted count is:

    production longitudinal writers:       0
    app/runtime longitudinal writers:       0
    model-authorized write paths:           0
    direct projection mutation paths:       0
    V1 migration write paths:               0
    qualification admission entry points:   exactly the documented governed path(s)


======================================================================
16. BUILD, TEST, AND REPOSITORY QUALIFICATION
======================================================================

Run the complete repository qualification from a clean state.

At minimum:

    gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks

Also run all new migration, replay, persistence, fault-injection, dependency,
privacy, and bypass tests.

Qualification must establish:

- all existing tests pass;
- all CT-V2-07 tests pass;
- zero skipped CT-V2-07 acceptance tests;
- lint has no new errors;
- debug APK assembles;
- unsigned release APK assembles;
- APKs contain no fixture database;
- APKs contain no real or synthetic longitudinal content;
- no generated database is tracked;
- no build output is tracked;
- no source cache is tracked;
- no model artifact is tracked;
- application backup remains disabled;
- the canonical forward-plan hash is unchanged;
- V1 deny-by-default register remains unchanged unless a documentation-only
  count update is strictly necessary;
- all V1 components remain denied;
- git diff and dependency inspection show no app/runtime wiring;
- git fsck --full --strict succeeds;
- final worktree is clean.

Record APK SHA-256 values while making no device, emulator, hardware, native
performance, clinical, production, or security-lifecycle claim.


======================================================================
17. STOP CONDITIONS
======================================================================

Stop and report without claiming completion if:

- the accepted entry baseline does not match;
- the canonical forward-plan hash changes unexpectedly;
- a real user database or V1 source must be opened;
- the work appears to require natural-language interpretation;
- an LLM is needed to pass an admission decision;
- direct store mutation cannot be eliminated;
- the domain module must depend on a persistence technology;
- Android/app/runtime wiring becomes necessary;
- a plaintext qualification database becomes reachable by the application;
- private material remains active in dependent current state;
- correction requires overwriting original evidence;
- deterministic replay cannot reproduce the logical state;
- a partial transaction can survive fault injection;
- record time can be caller-controlled;
- any existing CT-V2-04 or CT-V2-05 invariant regresses;
- any production authority would have to be granted;
- a test is weakened, deleted, or skipped merely to obtain a pass.

A governed partial result is preferable to a false completion claim.


======================================================================
18. COMMITS AND FINAL TAG
======================================================================

Use small, reviewable commits.

A suitable sequence is:

1. Record CT-V2-07 authorization, architecture decisions, and schema design.
2. Implement governed admission and revision.
3. Implement the durable qualification store and deterministic replay.
4. Add dependency/privacy/fault/bypass qualification.
5. Record final qualification evidence.

Do not squash away meaningful implementation history.

On complete qualification, create the annotated tag:

    ct-v2-07-governed-longitudinal-store

The tag must target the exact final qualified HEAD.

Do not configure a remote and do not push.


======================================================================
19. REQUIRED COMPLETION REPORT
======================================================================

The completion report must state:

Disposition:
- complete, partial, or blocked;
- no ambiguous “substantially complete” language.

Repository:
- starting and final HEAD;
- starting and final tree;
- branch;
- commit list;
- annotated tag name, object, and target;
- worktree status;
- remote and push status.

Forward plan:
- canonical path;
- before/after size;
- before/after SHA-256;
- confirmation that no redundant copy exists.

Architecture:
- final module graph;
- every new dependency;
- admission entry points;
- store implementation and schema version;
- schema fingerprint;
- ledger/projection design;
- record-time authority;
- idempotency behavior;
- concurrency behavior;
- privacy/dependency behavior;
- replay and canonical-digest behavior.

Authority audit:
- production writers;
- app/runtime writers;
- direct projection mutation paths;
- model write paths;
- real user records;
- V1 migration paths;
- persisted safety-state paths;
- packaged database files.

Qualification:
- exact clean command;
- task count;
- total test count;
- new CT-V2-07 test count;
- failures, errors, and skips;
- deterministic/property-test seeds;
- migration/reopen/replay results;
- fault-injection results;
- bypass proof;
- lint result;
- APK hashes;
- artifact scans;
- git fsck result.

Limitations:
- synthetic-only;
- no production encryption;
- no Android store;
- no backup/export/deletion lifecycle;
- no real user data;
- no model;
- no retrieval;
- no language-to-evidence formation;
- no production longitudinal or therapeutic authority.

Recommendation:
- whether CT-V2-08 — Language-to-Evidence & State Formation is ready for
  Principal consideration.


======================================================================
20. TARGET TERMINAL DISPOSITION
======================================================================

A fully successful CT-V2-07 should be capable of reporting:

CT_V2_07_GOVERNED_ADMISSION_REVISION_STORE_COMPLETE
DETERMINISTIC_LONGITUDINAL_WRITE_AUTHORITY_ESTABLISHED
APPEND_ONLY_LEDGER_AND_REBUILDABLE_PROJECTION_QUALIFIED
CORRECTION_PRIVACY_AND_DEPENDENCY_REVISION_BOUNDARIES_ESTABLISHED
SYNTHETIC_ONLY_STORAGE_BOUNDARY_PRESERVED
LLM_AND_APPLICATION_WRITE_AUTHORITY_ZERO
NO_PRODUCTION_LONGITUDINAL_AUTHORITY_GRANTED
CT_V2_08_READY_FOR_PRINCIPAL_CONSIDERATION

CT-V2-08 remains unopened until the Principal reviews the resulting completion
report.
