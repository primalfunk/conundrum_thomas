# CT-V2-09 Journal revision and privacy

## Revision

A committed entry is never edited in place. JournalRevisionCommand invokes the qualification adapter's CT-V2-07 AppendSourceRevision operation:

    stable Journal source
      -> prior immutable revision
      -> appended new revision
      -> CT-V2-08 processing of the new exact revision

The old wording, stable identity, report history, and provenance remain inspectable. Existing derived objects tied to the old current revision follow CT-V2-07 lifecycle consequences and become review-required/ineligible as appropriate. The revision path does not fabricate a USER_CORRECTION acquisition mode; it records user-authorized Journal wording revision.

Changing only response posture uses idempotent replay and creates no revision.

## Privacy

Journal supports only the already-governed ELIGIBLE and PRIVATE source states.

- A private commit persists the source with Journal provenance.
- The Journal engine skips CT-V2-08 processing.
- No optional response plan is formed.
- Private content cannot enter active ordinary derived state.
- Privacy is explicit command authority, never inferred from content.

Changing from eligible to private goes through CT-V2-07 ChangePrivacy and immediately applies its dependency-ineligibility state machine. Changing back to eligible remains review-required. CT-V2-09 deliberately performs no implicit reprocessing, response planning, or derived-state reactivation.

DECLINED remains a coverage-state concept, not a Journal privacy level and not evidence of absence.

## Idempotent and concurrency-safe operations

Commit, revision, and privacy operations each use separate namespaced idempotency keys and an expected store revision. CT-V2-07 rejects stale concurrency, missing references, duplicate IDs, and changed payload reuse atomically. Journal cannot construct an accepted receipt or assign record time.
