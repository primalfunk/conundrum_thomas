# CT-V2-09 Journal capture contract

## Commands

JournalCommitCommand provides stable entry and idempotency identities, expected store revision, exact committed text, typed/speech-transcript origin, response preference, privacy, report time, and explicit synthetic qualification authority.

JournalRevisionCommand identifies the stable entry, prior source revision, next revision number, replacement committed wording, expected store revision, report time, and new idempotency key.

JournalPrivacyChangeCommand changes only governed source privacy. It does not edit content or trigger perception.

JournalDraft is a non-authoritative value. No engine operation accepts it, and it has no source identity or admission envelope.

## Capture authority

JournalCaptureEngine.commit is the only Journal source-creation operation. Its admission envelopes have internal constructors and can only be produced by Journal code. A JournalAdmissionPort is the narrow infrastructure seam. CT-V2-09 supplies one adapter only in :qualification; it builds a CT-V2-07 AdmitSource request and submits it to store.admission.

    JournalEntryId
      -> SourceIdentityId("journal.<entry>")
      -> SourceRecordId("journal.<entry>.rev-<n>")

There is no separate Journal table or body copy.

## Ordering

1. Validate the command and synthetic authority.
2. Admit the exact source through CT-V2-07.
3. If private, stop with successful source-only capture.
4. Otherwise process the admitted source revision through CT-V2-08.
5. Finalize a redacted capture receipt.
6. If posture is not NO_RESPONSE, select a bounded response intent.
7. Render only through an explicitly invoked downstream seam.

## Receipt

JournalCaptureReceipt contains IDs, acquisition/capture origin, privacy, admission and language dispositions, admitted evidence IDs, unresolved count, resulting revision, response metadata, and SHA-256 canonical capture fingerprint. It contains neither source body nor rendered response.

The fingerprint covers capture facts and excludes response preference and response plan. Evidence IDs are sorted and de-duplicated.

## Failure boundary

Before source commitment:

- REJECTED_EMPTY_ENTRY
- REJECTED_INVALID_COMMAND
- REJECTED_AUTHORITY
- REJECTED_IDEMPOTENCY_CONFLICT
- REJECTED_PRIVACY_STATE
- SOURCE_ADMISSION_FAILED
- STORE_FAILURE_WITHOUT_COMMIT

After source commitment:

- EVIDENCE_PROCESSING_FAILED_AFTER_SOURCE_CAPTURE
- RESPONSE_PLANNING_FAILED_AFTER_CAPTURE
- renderer result RENDERING_FAILED_AFTER_CAPTURE

The latter group returns or retains a capture receipt and cannot undo the source.

## Logging

The module defines no logger or diagnostic body dump. Receipts and reason codes use IDs, dispositions, counts, versions, and fingerprints. Test assertions may inspect synthetic fixture bodies inside test scope only.
