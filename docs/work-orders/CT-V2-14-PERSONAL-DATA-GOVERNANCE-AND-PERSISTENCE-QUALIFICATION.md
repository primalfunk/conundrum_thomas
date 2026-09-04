# CONUNDRUM THOMAS V2

## CT-V2-14 — PERSONAL DATA GOVERNANCE & PERSISTENCE QUALIFICATION

### Development Work Order

**Status:** AUTHORIZED FOR IMPLEMENTATION
**Predecessor:** CT-V2-13 — Governed Language Renderer
**Canonical repository:** `C:\Android Studio Projects\ConundrumThomasV2`
**Canonical branch:** `main`

Starting accepted repository state:

* HEAD: `36393ab51b4f10b5133f8342a25b6d27355c555a`
* Tree: `2ea6f5f79e4a6aced2fc0bbdcc83e92e753c3da2`
* Accepted predecessor tag: `ct-v2-13-governed-language-renderer`
* Worktree at CT-V2-13 completion: clean
* Remotes: 0
* Canonical forward plan:
  `docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md`
* Canonical forward-plan SHA-256 at entry:
  `bfcd281f95e8aa188cc585121e740a635d1899a1f7661bbe69e798d990705886`
* Root `fdp.txt`: absent
* V1 governance register: 24/24 DENIED

---

# 1. PURPOSE

CT-V2-14 shall establish and qualify the **real personal-data persistence and lifecycle boundary** for Conundrum Thomas V2.

Prior phases established what Thomas may know, how personal evidence is admitted, how longitudinal state is formed, how Journal and Biographer contribute to it, how Therapist retrieves and uses it, and how authorized behavior is rendered into language.

Those phases deliberately did **not** grant production authority to persist a real person's psychological or longitudinal record.

CT-V2-14 exists to close that boundary.

The central question is:

> **Can Thomas durably retain highly personal longitudinal information on a real Android device while preserving provenance, correction history, privacy, deletion, recovery, migration, and component authority boundaries—and while failing closed when those guarantees cannot be maintained?**

This is a privacy, integrity, lifecycle, and recovery phase.

It is **not** a new Therapy phase.

It is **not** a model phase.

It is **not** general Android product integration.

It is **not** permission to start collecting real-user psychological data before qualification succeeds.

---

# 2. GOVERNING INVARIANTS

The following invariants are binding.

## 2.1 Thomas authority remains upstream

The governing V2 invariant remains:

> **THOMAS DECIDES. THE MODEL ONLY REALIZES AUTHORIZED LANGUAGE.**

Persistence shall not alter that relationship.

A database, DAO, migration layer, encryption layer, backup mechanism, restore mechanism, serializer, renderer, realizer, model, UI, or Android lifecycle component shall receive no authority to:

* choose a Therapy route;
* choose a therapeutic technique;
* change safety disposition;
* select Biographer targets;
* select Journal response posture;
* manufacture evidence;
* promote unsupported psychological interpretations;
* merge uncertain identities;
* expose private or ineligible evidence;
* transform Thomas-generated text into user evidence.

## 2.2 Evidence lineage remains authoritative

Accepted source evidence and its provenance remain the foundation of longitudinal state.

Persistence must preserve:

* source identity;
* source revision identity;
* original source wording where retention is authorized;
* provenance;
* event/report/record temporal distinctions;
* corrections;
* contestation;
* supersession;
* privacy/exclusion state;
* identity uncertainty;
* contradiction lineage;
* derivation dependencies.

A restored Thomas must not silently possess a materially different conception of the user merely because data passed through backup, restore, migration, restart, or corruption recovery.

## 2.3 Source history is not silently rewritten

Ordinary edits remain revision-producing operations.

Persistence must not introduce convenient SQL/Room mutation paths that bypass the accepted append/revision semantics.

Derived state may be rebuilt.

Accepted historical evidence may not be silently rewritten to make rebuilding easier.

## 2.4 Derived state is subordinate and rebuildable

Derived psychological or longitudinal state must remain dependent upon eligible admitted evidence.

Deleting, privatizing, excluding, correcting, superseding, or otherwise invalidating source evidence must cause dependent derived material to be:

* invalidated;
* recomputed;
* retired;
* withheld;

as appropriate under existing V2 semantics.

No derived object may survive merely because it was previously materialized.

## 2.5 Private means behaviorally private

A privacy designation must have enforceable behavioral consequences.

Private/ineligible material must not leak through:

* Therapy retrieval;
* Biographer selection;
* Journal response planning;
* renderer input;
* backup metadata;
* logs;
* diagnostics;
* crash reports;
* filenames;
* indexes;
* derived summaries;
* search surfaces;
* stale caches.

## 2.6 Failure is fail-closed

Unsupported schema versions, incorrect keys, malformed records, corrupted databases, incomplete restores, missing provenance, broken dependency graphs, partial migrations, and other integrity failures must not be silently repaired into plausible user history.

When trustworthy reconstruction is impossible, the affected information must become unavailable rather than fabricated.

---

# 3. ENTRY CONDITIONS

Before implementation:

1. Verify the canonical repository root.
2. Verify `.git` remains the canonical Git metadata directory.
3. Run the existing canonical VCS-root verifier.
4. Record:

   * starting HEAD;
   * starting tree;
   * branch;
   * worktree state;
   * tag state;
   * forward-plan path, size, and SHA-256.
5. Verify CT-V2-13 tag target.
6. Verify no temporary Git metadata directory exists.
7. Verify the V1 register remains deny-by-default.
8. Verify no real-user personal or psychological fixture has been committed to the repository.
9. Preserve the existing forward development plan unless a factual phase-status update is explicitly required. Do not create another planning copy.

If the repository differs materially from the accepted CT-V2-13 state, document the discrepancy before altering implementation.

Do not hide, move, rename, redirect, or temporarily replace `.git`.

---

# 4. REQUIRED ARCHITECTURE

Create the narrowest architecture necessary to provide production-grade local personal-data persistence without collapsing existing module authority boundaries.

The exact package/module decomposition may be determined from the existing repository architecture, but the resulting dependency graph must preserve separation between at least:

* longitudinal domain semantics;
* governed admission;
* persistent storage implementation;
* personal-data protection/key handling;
* lifecycle operations;
* backup/export/restore;
* Journal;
* Biographer;
* Therapy retrieval/integration;
* governed rendering;
* Android platform adapters.

The persistence implementation must be an **implementation of existing governed contracts**, not a second longitudinal authority system.

There shall be no parallel "easy" user-profile database whose semantics diverge from CT-V2-06 through CT-V2-12.

---

# 5. PRODUCTION LOCAL PERSISTENCE

Implement a production-capable local store suitable for sensitive longitudinal personal data on Android.

The implementation shall preserve the logical guarantees previously qualified in the synthetic store, including:

* immutable accepted ledger semantics;
* lifecycle history;
* source revisions;
* idempotency;
* projection rebuildability;
* provenance;
* correction and supersession;
* contradiction state;
* unresolved identity state;
* deterministic logical state;
* failed-closed unsupported/corrupt-state handling.

Where the production storage technology cannot naturally provide an existing invariant, implement an explicit enforcement mechanism and test it.

No public DAO or generic SQL interface may become a bypass around governed admission.

Persistence implementation details must not escape upward as new authority surfaces.

---

# 6. DATA-AT-REST PROTECTION

Thomas's durable personal longitudinal data must be protected at rest.

Qualify an Android-appropriate local protection design using platform security facilities where practical.

At minimum establish:

* protected primary persistent data;
* protected retained sensitive auxiliary data;
* protected backup artifacts;
* controlled access to encryption material;
* separation between application data and cryptographic key material;
* behavior after application data/key loss;
* behavior after invalid or unavailable key material;
* no plaintext shadow copy created as part of normal operation.

Cryptographic/key-handling implementation must avoid homemade cryptography where an appropriate platform primitive exists.

The qualification report must identify exactly:

* what is encrypted/protected;
* what is not;
* where key material resides;
* which component can request use of the key;
* what happens when it is unavailable;
* what is visible to ordinary filesystem inspection.

Do not claim protection that has not actually been demonstrated.

---

# 7. ACCESS BOUNDARIES

Establish explicit read/write authority boundaries for persisted personal data.

At minimum audit:

* longitudinal admission writers;
* Journal source writers;
* Biographer writers, if any;
* derived-state writers;
* retrieval readers;
* Therapist readers;
* renderer readers;
* LanguageRealizer/model visibility;
* UI visibility;
* backup/export readers;
* deletion/reset authority;
* migration authority.

The expected posture remains:

* model-authorized longitudinal writes: **0**;
* renderer longitudinal writes: **0**;
* LanguageRealizer direct database access: **0**;
* renderer direct database access: **0**;
* Therapy route evaluator database mutation authority: **0**;
* Biographer target selection authority created by persistence: **0**;
* Thomas response → user evidence automatic paths: **0**.

Any broader mechanism discovered during implementation must be explicitly classified in the qualification report.

---

# 8. SELECTIVE SOURCE DELETION

Implement and qualify deliberate deletion of individual source material.

Deletion semantics must address the distinction between:

* deletion of one Journal entry;
* deletion of a Journal revision/history chain where permitted;
* deletion of a Biographer-originating source;
* deletion of Therapy-originating user evidence;
* deletion of derived state;
* deletion of an entire user corpus.

A source deletion must propagate into dependent state.

The qualification must prove that deleted evidence cannot continue to influence Thomas indirectly through:

* projections;
* recurrence;
* contradictions;
* hypotheses/interpretations;
* retrieval indexes;
* cached context;
* Biographer coverage;
* Therapist longitudinal support;
* renderer-visible support;
* backup artifacts subsequently generated.

Do not satisfy deletion merely by hiding a source from one UI query.

---

# 9. DERIVED-DATA INVALIDATION

Create an explicit mechanism for dependency-aware invalidation/rebuild.

Required scenarios include:

* source deleted;
* source made private;
* source restored from private status where existing policy allows;
* source corrected;
* source revision superseded;
* contested evidence changes disposition;
* identity resolution changes;
* previously merged/associated information becomes uncertain;
* derivation version changes.

The system must never permit stale derived psychological state to continue to masquerade as current merely because its materialized row still exists.

Where safe deterministic rebuilding is possible, rebuild.

Where it is not possible, fail closed and mark state unavailable or requiring reconstruction.

---

# 10. CORRECTION AND PROVENANCE THROUGH PERSISTENCE

Prove that corrections do not destroy historical provenance.

A correction must preserve the ability to determine:

* what was originally reported;
* what later corrected it;
* when the correction entered the record;
* what dependent interpretations changed;
* what currently governs retrieval;
* whether the original remains retained, contested, superseded, or deleted under policy.

Backup/restore and schema migration must preserve this lineage.

The logical meaning of a correction must survive application restart.

---

# 11. USER EXPORT

Implement a governed local export mechanism for the user's own data.

Export must be understandable enough to inspect independently of Thomas.

Provide:

1. a machine-readable representation; and
2. a human-readable representation where feasible.

The export must distinguish source evidence from derived interpretation rather than flattening the two together.

Export should preserve useful provenance including:

* source/revision relationships;
* timestamps and temporal qualification;
* provenance/mode;
* privacy state where appropriate;
* correction/supersession relationships;
* derived-state evidence dependencies.

An export mechanism must not become an unrestricted internal database dump containing secrets, encryption keys, implementation credentials, or data outside its declared scope.

Qualification fixtures shall remain synthetic.

---

# 12. BACKUP

Implement a local backup artifact suitable for preserving the Thomas longitudinal corpus.

The backup shall be protected before it leaves the application's protected storage boundary.

Backup must preserve enough information to reconstruct the user's legitimate longitudinal state, including the lineage required to rebuild derived state.

Qualify:

* backup creation;
* deterministic/consistent logical coverage;
* protection of backup contents;
* backup of empty state;
* backup after multiple revisions;
* backup after corrections;
* backup after privacy changes;
* backup after selective deletion;
* failed backup;
* interrupted backup;
* corrupt backup handling.

A backup operation may not grant cloud synchronization authority.

No cloud account system is authorized by CT-V2-14.

---

# 13. RESTORE

Implement governed restore into an appropriate empty/recovery state.

Restore must verify integrity before granting restored information ordinary runtime authority.

A partially trusted restore shall not produce partially trusted psychological state.

Qualify:

* successful restore;
* source history preservation;
* correction preservation;
* privacy preservation;
* identity uncertainty preservation;
* contradiction preservation;
* projection rebuild;
* logical equivalence before backup and after restore;
* wrong-key or unavailable-key failure;
* truncated backup;
* modified backup;
* unsupported backup format/version;
* interrupted restore;
* restore into prohibited non-empty state;
* idempotent retry where applicable.

The principal acceptance criterion is:

> A restored Thomas must derive the same authorized longitudinal understanding from the same surviving evidence, subject only to explicitly versioned derivation changes.

---

# 14. APPLICATION RESET / COMPLETE DELETION

Implement a deliberate full local reset capable of removing the user's Thomas personal corpus.

Qualification shall establish what remains afterward.

At minimum remove or invalidate:

* source records;
* revisions;
* longitudinal projections;
* derived psychological state;
* retrieval indexes;
* cached render/history fingerprints where personal;
* private entries;
* Biographer coverage derived from deleted sources;
* Therapy longitudinal support;
* local backup references under application control;
* persistent identifiers that would silently reconnect deleted psychological state.

Application reset must not falsely claim to erase an independently exported backup that exists outside application control.

This distinction must be explicit in documentation.

---

# 15. RETENTION POLICY

Document and implement the initial local retention policy.

The policy must state which categories are:

* retained until user deletion;
* revision-retained;
* ephemeral;
* recomputable;
* automatically discarded;
* never persisted.

Particular attention shall be given to:

* raw speech audio;
* speech transcripts;
* drafts;
* renderer candidates;
* rejected renderer candidates;
* renderer fingerprints;
* context packets;
* retrieval working sets;
* model prompts;
* model outputs;
* logs;
* crash diagnostics;
* temporary export/backup files.

Do not begin retaining a new category merely because persistence now exists.

Raw audio must not become durable by accident.

Unchecked model output must not become a durable personal-data category.

---

# 16. DATABASE / SCHEMA MIGRATION

Create and qualify an explicit persistent schema-version migration mechanism.

Existing schema/version contracts must be respected.

Qualify at least:

* current schema open;
* supported prior schema migration;
* migration preserving record count and semantic identity;
* migration preserving source/revision provenance;
* migration preserving privacy/deletion state;
* migration preserving correction history;
* migration preserving idempotency semantics;
* migration preserving unresolved identity;
* failed migration;
* interrupted migration;
* unsupported future schema;
* malformed schema metadata;
* migration retry/recovery behavior.

A failed migration must not silently initialize a fresh Thomas and thereby discard the user's history.

A future/unknown schema must fail closed.

---

# 17. CORRUPTION AND RECOVERY

Define corruption classes and recovery behavior.

Test recoverable and unrecoverable cases.

Examples should include:

* damaged projection only;
* damaged rebuildable index;
* damaged derived state;
* damaged ledger;
* missing source revision;
* invalid dependency;
* incorrect fingerprint/digest;
* incomplete transaction;
* malformed lifecycle row;
* truncated store;
* inconsistent schema metadata.

Where the immutable/admitted evidence layer remains trustworthy, rebuilding derived state is preferred to speculative repair.

Where source evidence itself cannot be trusted, the system must not manufacture continuity.

Produce explicit typed failure/recovery dispositions.

---

# 18. PROCESS-DEATH AND RESTART QUALIFICATION

Qualify persistence across ordinary Android/runtime interruptions.

At minimum test state survival or correct non-survival, as appropriate, through:

* process death;
* ordinary application restart;
* device/emulator restart where feasible in the qualification environment;
* interrupted write;
* interrupted projection rebuild;
* interrupted export;
* interrupted backup;
* interrupted restore;
* interrupted migration.

Atomicity must be demonstrated for operations where partial completion would corrupt psychological meaning.

---

# 19. BACKUP POLICY AT THE ANDROID PLATFORM LEVEL

Review the current Android backup configuration.

CT-V2-13 reported application backup as disabled.

Do not casually enable generic Android/cloud backup for sensitive Thomas data.

If platform backup remains disabled, document why and prove the governed Thomas backup path is independent.

If any platform extraction rule changes are required, they must be specifically justified and qualified.

No personal Thomas database may accidentally become eligible for uncontrolled platform backup merely because CT-V2-14 introduces durable storage.

---

# 20. LOGGING, DIAGNOSTIC, AND ARTIFACT HYGIENE

Audit production and qualification code for sensitive content escaping into:

* Logcat;
* stdout/stderr;
* exception text;
* test reports;
* Gradle reports;
* filenames;
* temporary directories;
* crash artifacts;
* Git-tracked fixtures;
* APK assets/resources;
* generated provenance artifacts.

Synthetic fixtures may be used.

Real-user psychological data must not be used for qualification.

Rejected/private source text must not appear unnecessarily in ordinary diagnostics.

Secrets or encryption keys must never be committed.

At completion, scan both debug and release APKs for prohibited:

* fixture corpora;
* plaintext databases;
* backup artifacts;
* export artifacts;
* keys/secrets;
* context packet dumps;
* longitudinal dumps;
* model artifacts;
* prompt corpora.

---

# 21. RENDERER ISOLATION REGRESSION

CT-V2-13's language boundary must survive persistence implementation.

Explicitly prove:

* renderer receives no general persistence handle;
* LanguageRealizer receives no persistence handle;
* renderer cannot retrieve arbitrary memories;
* renderer cannot query private memories;
* renderer cannot cause evidence writes;
* candidate model output cannot enter the source ledger;
* render history does not become a shadow psychological store;
* `NO_RESPONSE` continues to create no assistant artifact and no realizer call;
* persisted data does not widen render-visible support beyond the governed command.

Any persistence shortcut that violates this boundary is disqualifying.

---

# 22. THERAPY / SAFETY REGRESSION

No new therapeutic authority is granted.

Run complete regression coverage sufficient to prove preservation of:

* CT-V2-04 safety authority;
* CT-V2-05 ordinary Therapy route authority;
* deterministic progression;
* anti-repetition;
* permit boundaries;
* CT-V2-11 retrieval authority;
* CT-V2-12 longitudinal Therapist authority;
* CT-V2-13 renderer authority.

Persistence must not cause a previously denied therapeutic route to become reachable.

Do not add new Therapy techniques or safety policies in this phase.

If an unrelated defect is discovered, repair only what is necessary to preserve already-authorized behavior and document it.

---

# 23. JOURNAL REGRESSION

Preserve CT-V2-09 semantics.

Prove at minimum:

* only committed Journal material becomes source evidence;
* drafts remain non-evidence;
* typed and speech-transcript provenance remain distinct;
* private Journal entries behave according to established private semantics;
* edits remain revisions rather than silent rewrites;
* `NO_RESPONSE` remains true silence;
* response posture does not create evidence revisions;
* persistence restart does not duplicate Journal evidence;
* selective deletion propagates into longitudinal state.

---

# 24. BIOGRAPHER REGRESSION

Preserve CT-V2-10 semantics.

Persistence must not grant Biographer authority to:

* invent historical events;
* select a new target through renderer behavior;
* convert unanswered questions into evidence;
* convert speculative prompts into user facts.

Prove that its historical coverage and gap state survive legitimate restart/backup/restore while responding correctly to deleted/private/corrected source material.

---

# 25. LONGITUDINAL RETRIEVAL REGRESSION

Persisted state must preserve CT-V2-11 retrieval behavior.

Explicitly test that retrieval after restart/restore:

* remains bounded;
* preserves provenance;
* respects privacy/ineligibility;
* respects correction/supersession;
* does not return deleted source material;
* does not expose stale derived state;
* retains uncertainty;
* retains temporal distinctions;
* does not silently resolve unresolved identity.

Where projections/indexes are reconstructible, qualification should include deletion followed by rebuild followed by retrieval.

---

# 26. NO REAL USER DATA BEFORE QUALIFICATION

This is a hard phase boundary.

Until all CT-V2-14 mandatory qualification gates pass:

> **NO PRODUCTION PSYCHOLOGICAL PERSISTENCE AUTHORITY IS GRANTED.**

Do not populate the production-capable store with actual user Journal, Biographer, Therapy, history, psychographic, or other sensitive personal content for convenience.

Use synthetic, non-identifying qualification fixtures only.

Successful implementation alone does not lift this restriction.

Only completion of this work order and Principal acceptance may authorize the next phase to compose persistence into the real Android product.

---

# 27. OUT OF SCOPE

CT-V2-14 SHALL NOT introduce:

* a model-backed LanguageRealizer;
* model selection/admission;
* GGUF packaging;
* general Android UI integration;
* final Android conversation composition;
* STT integration;
* TTS integration;
* cloud synchronization;
* cloud user accounts;
* remote psychological storage;
* telemetry containing user content;
* social sharing;
* multi-user profiles;
* multi-device synchronization;
* new Therapy routes;
* new therapeutic techniques;
* new safety doctrine;
* diagnosis;
* clinical screening;
* passive sensing;
* V1 user-data migration;
* production real-user psychological ingestion.

Those require later authority.

CT-V2-15 remains the intended next integration phase unless the forward plan or qualification evidence establishes otherwise.

---

# 28. REQUIRED THREAT / FAILURE MODEL

Produce a concise CT-V2-14 personal-data threat and failure model covering at least:

* another ordinary application;
* filesystem extraction;
* lost/stolen powered-off device within realistic platform assumptions;
* accidental plaintext artifacts;
* database corruption;
* incomplete transactions;
* stale derived data;
* stale backup;
* backup modification;
* wrong-key restore;
* developer logging leakage;
* component-authority bypass;
* model/renderer attempting unauthorized access;
* user deletion not propagating;
* schema-version mismatch.

Do not make unsupported claims regarding protection against a fully compromised operating system, rooted device, debugger with sufficient privileges, or an attacker controlling the user's unlocked session unless actually demonstrated.

Document the protection boundary accurately.

---

# 29. QUALIFICATION CORPUS

Construct a rich synthetic longitudinal corpus that exercises the persistence lifecycle.

It should include, at minimum:

* multiple Journal entries;
* Journal revisions;
* private Journal content;
* Biographer historical answers;
* repeated longitudinal patterns;
* self-report;
* self-belief;
* user interpretation;
* third-party report;
* tentative Thomas interpretation;
* exact time;
* approximate time;
* ranges;
* relative dates;
* ongoing events;
* unknown time;
* contradiction;
* apparent change over time that is not contradiction;
* correction;
* contested evidence;
* superseded evidence;
* unresolved identity;
* resolved identity where permitted;
* deleted evidence;
* derived state with multiple dependencies;
* Therapy-originating user evidence;
* historical instruction-like text that must remain inert user data.

Use this corpus to exercise:

`create → persist → restart → retrieve → correct → privatize → delete → rebuild → backup → reset → restore → migrate → retrieve`

The final state must be mechanically auditable.

---

# 30. REQUIRED TEST CLASSES

Implement deterministic automated qualification for at least the following classes:

1. storage integrity;
2. governed write authority;
3. access boundary;
4. encryption/protection behavior;
5. key-unavailable failure;
6. restart durability;
7. transaction atomicity;
8. selective deletion;
9. complete reset;
10. derived-data invalidation;
11. correction/provenance;
12. private-data exclusion;
13. export;
14. backup;
15. backup protection;
16. restore;
17. restore integrity;
18. corrupt backup rejection;
19. schema migration;
20. interrupted migration;
21. corrupt-store recovery;
22. unrecoverable corruption failure;
23. Journal regression;
24. Biographer regression;
25. retrieval regression;
26. Therapy regression;
27. safety regression;
28. renderer isolation;
29. historical-instruction non-authority;
30. model-authority-zero;
31. APK/artifact hygiene;
32. V1 deny-by-default regression.

Use additional tests wherever necessary.

Do not optimize for a particular test count. Optimize for demonstrated coverage.

---

# 31. REQUIRED MECHANICAL AUDITS

At qualification, report counts or equivalent mechanical evidence for:

* production persistent-store composition roots;
* qualification-only store roots;
* governed admission write entry points;
* direct database mutation entry points;
* renderer → database paths;
* realizer → database paths;
* model → database paths;
* Therapy route → database mutation paths;
* Journal source creation paths;
* Biographer source creation paths;
* backup readers/writers;
* export readers/writers;
* reset/delete authorities;
* migration authorities;
* plaintext persistent sensitive stores;
* platform-generic backup eligibility;
* tracked database files;
* tracked backup/export artifacts;
* tracked sensitive fixtures;
* APK packaged databases;
* APK packaged longitudinal corpora;
* APK packaged keys/secrets;
* real-user records used in qualification.

Unexpected nonzero counts must be investigated, classified, and justified.

---

# 32. DOCUMENTATION DELIVERABLES

At minimum create and preserve:

`docs/work-orders/CT-V2-14-PERSONAL-DATA-GOVERNANCE-AND-PERSISTENCE-QUALIFICATION.md`

`docs/qualification/CT-V2-14-QUALIFICATION.md`

And appropriate architecture/security documentation describing:

* persistent-store architecture;
* encryption/key boundary;
* data categories and retention;
* backup/export/restore;
* deletion/reset propagation;
* migration;
* corruption/recovery;
* authority surfaces;
* threat model.

Use ADRs where the repository's established conventions warrant them.

Do not duplicate the canonical forward development plan.

---

# 33. FORWARD PLAN INTEGRITY

The canonical plan remains:

`docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md`

Record before and after:

* path;
* file size;
* SHA-256;
* number of tracked canonical copies;
* redundant copies;
* root `fdp.txt` status.

Do not casually rewrite historical planning content.

If phase status is updated, make only the minimum semantically correct planning change and report the resulting hash separately.

---

# 34. BUILD AND QUALIFICATION

At completion run the repository's full canonical build, test, lint, provenance, and qualification suite.

Use the existing sealing pattern unless repository reality requires a justified adjustment.

Expected baseline pattern:

`.\gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain`

Also run:

* `git diff --check`
* canonical Git-root verifier
* `git fsck --full --strict`
* V1 governance verification
* artifact/APK inspection
* authority-path audit
* sensitive-content scan
* applicable Android backup-rule inspection

Record:

* task count;
* test suites;
* total tests;
* CT-V2-14 tests;
* failures;
* errors;
* skips;
* lint errors/fatals/warnings;
* build duration;
* APK sizes and SHA-256;
* relevant persistent schema/version identifiers.

Do not mark COMPLETE if a mandatory gate is skipped because implementation is difficult.

Environment-impossible checks must be separately identified and must not be misrepresented as passed.

---

# 35. REPOSITORY DISCIPLINE

Use coherent commits representing meaningful milestones.

Before completion:

* working tree must be clean;
* no temporary Git metadata may remain;
* no generated databases may be tracked unless explicitly intended and justified;
* no keys/secrets may be tracked;
* no backup/export fixture containing sensitive data may be tracked;
* no build output may be tracked;
* canonical project root must remain valid.

Do not push.

No remote is required.

---

# 36. COMPLETION TAG

Only after all mandatory CT-V2-14 qualification gates pass, create an annotated tag:

`ct-v2-14-personal-data-governance-persistence`

The tag must point to the final clean qualification commit.

Record:

* tag name;
* tag object;
* target commit;
* tag type.

Do not seal an incomplete or conditionally failed implementation under the completion tag.

---

# 37. COMPLETION DISPOSITION

The successful terminal disposition is:

`CT_V2_14_PERSONAL_DATA_GOVERNANCE_PERSISTENCE_COMPLETE`

A successful report should also be able to substantiate dispositions equivalent to:

`PRODUCTION_LOCAL_PERSISTENCE_BOUNDARY_QUALIFIED`

`PERSONAL_DATA_AT_REST_PROTECTION_QUALIFIED`

`PERSISTENCE_ACCESS_BOUNDARIES_QUALIFIED`

`IMMUTABLE_SOURCE_LINEAGE_PRESERVED`

`CORRECTION_AND_PROVENANCE_PERSISTENCE_QUALIFIED`

`PRIVATE_EVIDENCE_PERSISTENCE_BOUNDARY_QUALIFIED`

`SELECTIVE_SOURCE_DELETION_QUALIFIED`

`DERIVED_DATA_INVALIDATION_QUALIFIED`

`COMPLETE_LOCAL_RESET_QUALIFIED`

`RETENTION_POLICY_QUALIFIED`

`HUMAN_READABLE_EXPORT_QUALIFIED`

`MACHINE_READABLE_EXPORT_QUALIFIED`

`PROTECTED_LOCAL_BACKUP_QUALIFIED`

`BACKUP_RESTORE_LINEAGE_QUALIFIED`

`CORRUPT_BACKUP_FAIL_CLOSED_QUALIFIED`

`SCHEMA_MIGRATION_QUALIFIED`

`CORRUPTION_AND_RECOVERY_BEHAVIOR_QUALIFIED`

`PROCESS_RESTART_DURABILITY_QUALIFIED`

`PLATFORM_BACKUP_BOUNDARY_QUALIFIED`

`RENDERER_PERSISTENCE_AUTHORITY_ZERO`

`LANGUAGE_REALIZER_PERSISTENCE_AUTHORITY_ZERO`

`MODEL_PERSISTENCE_AUTHORITY_ZERO`

`THOMAS_RESPONSE_USER_EVIDENCE_AUTHORITY_ZERO`

`CT_V2_04_SAFETY_AUTHORITY_PRESERVED`

`CT_V2_05_THERAPY_AUTHORITY_PRESERVED`

`CT_V2_09_JOURNAL_AUTHORITY_PRESERVED`

`CT_V2_10_BIOGRAPHER_AUTHORITY_PRESERVED`

`CT_V2_11_RETRIEVAL_AUTHORITY_PRESERVED`

`CT_V2_12_LONGITUDINAL_THERAPY_AUTHORITY_PRESERVED`

`CT_V2_13_RENDERER_AUTHORITY_PRESERVED`

`V1_MIGRATION_AUTHORITY_DENIED`

`REAL_USER_QUALIFICATION_DATA_ZERO`

`ANDROID_STUDIO_CANONICAL_VCS_ROOT_REMAINS_VALID`

`PRODUCTION_PSYCHOLOGICAL_PERSISTENCE_BOUNDARY_QUALIFIED`

`NEXT_PHASE_READY_FOR_PRINCIPAL_CONSIDERATION`

---

# 38. FAILURE DISPOSITION

If one or more mandatory gates cannot be established, do not use COMPLETE.

Report the most precise bounded disposition available, for example:

`CT_V2_14_IMPLEMENTED_QUALIFICATION_BLOCKED`

or

`CT_V2_14_PARTIAL_PERSONAL_DATA_GOVERNANCE_ONLY`

and explicitly state:

* which gate failed;
* whether user data would be at risk;
* what authority remains denied;
* what work is required to resolve it.

In all incomplete outcomes:

`NO_PRODUCTION_PSYCHOLOGICAL_PERSISTENCE_AUTHORITY_GRANTED`

must remain true.

---

# 39. COMPLETION REPORT

Return a structured completion report containing at minimum:

## DISPOSITION

Final machine-readable disposition tokens.

## REPOSITORY

* starting HEAD/tree;
* final HEAD/tree;
* branch;
* worktree status;
* commits;
* tag;
* push status;
* Git integrity.

## ARCHITECTURE

* module/dependency graph;
* persistent store;
* encryption/key boundary;
* write/read authorities.

## PERSONAL DATA LIFECYCLE

* persistence;
* privacy;
* correction;
* selective deletion;
* full reset;
* retention;
* export;
* backup;
* restore.

## RECOVERY

* restart;
* corruption;
* rebuild;
* migration;
* interrupted operation behavior.

## AUTHORITY AUDIT

Exact or mechanically supported counts for significant access/write paths.

## REGRESSION

Evidence that CT-V2-04/05/09/10/11/12/13 remain preserved.

## SECURITY / ARTIFACT AUDIT

* backup policy;
* APK findings;
* logs;
* tracked sensitive artifacts;
* plaintext exposure;
* real-user-data count.

## QUALIFICATION

* commands;
* build;
* tests;
* lint;
* APKs;
* hashes;
* qualification corpus results.

## LIMITATIONS

Clearly identify what CT-V2-14 still does not authorize.

## RECOMMENDATION

State whether the repository is ready for:

**CT-V2-15 — Android Production Integration**

Do not open CT-V2-15 unless separately authorized by the Principal.

---

# PRINCIPAL INTENT

CT-V2-14 should leave Conundrum Thomas V2 with something it has intentionally not possessed before:

> **A real, durable place to remember a person.**

That capability carries unusually high trust requirements because Thomas is designed to accumulate information about a person's history, relationships, beliefs, behavior, problems, contradictions, and psychological patterns over long periods of time.

The persistence system therefore cannot merely be "a database that works."

It must preserve the distinction between:

* what the user actually supplied;
* what Thomas inferred;
* what was uncertain;
* what was later corrected;
* what was made private;
* what was deleted;
* what remains authorized for use.

And the user must remain capable of taking that history with them, recovering it after failure, or making Thomas forget it.

Build the persistence layer so that **longitudinal memory increases Thomas's usefulness without decreasing the user's ownership of their own life history.**

No production psychological persistence authority exists until this work order succeeds.

Proceed through implementation and qualification, stop safely on any genuine governance failure, and return the complete evidence package for Principal review.
