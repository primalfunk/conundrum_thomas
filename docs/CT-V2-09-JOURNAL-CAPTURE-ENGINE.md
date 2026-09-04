# CT-V2-09 Journal Capture Engine

## Purpose

CT-V2-09 establishes Journal as a source-first, synthetic-only capture path. A committed Journal entry is admitted as immutable or revisioned JOURNAL source evidence before bounded language processing or optional response planning occurs.

Journal remains capture, not Therapy, Biographer investigation, formulation, retrieval, or profile mutation. An entry can be valuable even when perception returns no evidence proposal. No production persistence or user-facing Android integration is authorized.

## Governed pipeline

    JournalCommitCommand
      -> JournalCaptureEngine
      -> JournalAdmissionPort
      -> CT-V2-07 LongitudinalAdmissionController
      -> committed JOURNAL source revision
      -> CT-V2-08 GovernedLanguageEvidencePipeline (eligible entries only)
      -> governed evidence admission and deterministic formed state
      -> JournalCaptureReceipt
      -> optional JournalResponsePlan
      -> optional downstream renderer

The only complete composition root is GovernedJournalCapturePipeline in :qualification. :app, :thomas:runtime, Android persistence, Therapy, safety, renderer implementations, and Biographer do not depend on :thomas:journal.

## Entry and source identity

JournalEntryId is a semantic wrapper, not a parallel record system. It deterministically maps to one CT-V2-07 SourceIdentityId and revision IDs:

    entry foo -> source identity journal.foo
    revision 1 -> journal.foo.rev-1
    revision 2 -> journal.foo.rev-2

The committed body exists only as the governed source content. Capture origin is provenance metadata: TYPED or SPEECH_TRANSCRIPT. Both origins follow the same pipeline; no raw audio is accepted.

JournalDraft deliberately has no admission relationship. Only commit accepts source text.

## Source-first ordering and failures

The engine validates a synthetic command, admits the source, processes eligible evidence, creates the capture receipt, and only then plans a response. It distinguishes pre-commit failures from:

- EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE;
- RESPONSE_PLANNING_FAILED_AFTER_CAPTURE;
- RENDERING_FAILED_AFTER_CAPTURE.

Post-capture failures retain the admitted source. No response failure can revoke, roll back, or rewrite an entry.

## Response postures

- NO_RESPONSE is the default. It creates no response plan and bypasses the planner and renderer.
- REFLECT may produce one brief, current-entry-grounded reflection plan.
- ASK_ONE_QUESTION may produce at most one current-entry-grounded optional question. If no safe grounded assertion exists, it produces no response plan.

Response posture is not stored as source provenance and is excluded from the canonical capture fingerprint. Identical entry content produces equivalent source/evidence/state under all three postures. A posture-only replay may select a different transient response plan but cannot create another source or revision.

Every response plan preserves attribution and uncertainty where required and prohibits diagnosis, psychological formulation, therapy technique, cognitive challenge, hidden-motive inference, historical retrieval, Biographer gap pursuit, multiple questions, directive advice, and evidence mutation.

## Privacy and revision

PRIVATE commits are admitted as Journal sources but skip language processing and optional response planning. CT-V2-07 excludes them from current derived eligibility. Restoring eligibility is a governed privacy transition that remains review-required; CT-V2-09 does not reprocess or silently reactivate it.

Post-commit text change uses CT-V2-07 AppendSourceRevision. Stable source identity and old revisions remain inspectable. CT-V2-08 reprocesses the new exact revision, while CT-V2-07 marks old-revision dependents according to its review/ineligibility state machine. A response-preference change alone creates no source revision.

## Idempotency

Journal idempotency keys are namespaced into the CT-V2-07 request. The source operation excludes response posture:

- same key and same source operation: IDEMPOTENT_REPLAY, no duplicate;
- same key and changed text or privacy: fail closed as an idempotency conflict;
- same key and changed response posture: replay the same source, re-evaluate only the transient response intent;
- retry after a response failure: replay the captured source without duplication.

## Bounded perception additions

The existing CT-V2-08 deterministic grammar was narrowly extended for the authorized synthetic Journal corpus: ordinary grocery events, direct present feelings, reported exhaustion/interruption, remembered approximate moves, relative uncertain time, and multiple independent clauses in one source. These additions preserve CT-V2-08 epistemic and temporal classes. An unresolved referent now yields a structural UNRESOLVED_REFERENCE evidence question; it does not authorize dialogue.

## Authority and unopened areas

CT-V2-07 remains the sole durable admission gate and CT-V2-08 remains the language/state authority. The Journal engine performs no SQL/JDBC operation, retrieval, therapy routing, safety classification, Biographer coverage selection, model invocation, or Android persistence.

CT-V2-09 is SYNTHETIC_QUALIFICATION_ONLY. Production Journal writers, production longitudinal authority, real-user storage, model evidence authority, Therapy authority, and Biographer investigation authority are all zero.
