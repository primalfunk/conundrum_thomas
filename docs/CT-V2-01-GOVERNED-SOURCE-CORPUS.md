# CT-V2-01 — Governed therapeutic source and provenance corpus

**Foundation:** CT-V2-00 at `990bf8a61533134c5e713b5c384e6eaa10b955ee`  
**Verified:** 2026-09-03  
**Scope:** Sources and provenance only

## Outcome

CT-V2-01 establishes a reviewable chain from official source authority through canonical publication, version, official locator/artifact, precise section, rights, applicability, currency, conflict, and human-review requirements. The generated SQLite database is reproducible from tracked migrations and seeds and is not an editable authority.

The primary invariant remains unchanged:

> THOMAS SELECTS AND GOVERNS THERAPEUTIC BEHAVIOR.  
> THE MODEL ONLY RENDERS AUTHORIZED BEHAVIOR INTO LANGUAGE.

No source record grants Thomas authority to act. No literature statement has been converted into a rule, dialogue act, intervention selector, state machine, screening score, safety algorithm, or model prompt.

## Governed chain

```text
SourceAuthority
  -> SourceDocument
     -> SourceVersion
        -> SourceLocator / SourceSection
        -> RightsRecord
        -> ApplicabilityMetadata
        -> SourceFreshness
        -> SupersessionRelationship / SourceConflict
        -> ReviewRequirement
        -> unopened CandidateSubject
           -X-> future abstraction/rule/test (not opened)
```

## Runtime and raw-artifact boundary

The generated database lives at `provenance/generated/thomas-provenance.sqlite` and remains ignored. It is not an app asset and no production module depends on the SQLite build tool. Raw official artifacts live only in `provenance/raw`, which is ignored at both repository and provenance-workspace levels. The tracked corpus stores official locators, retrieval dates, SHA-256 values, byte sizes, and original short abstracts—not PDFs, worksheets, or long excerpts.

## Freshness and review

Every version has `verified_at`, an official source, a latest-known-version description, a deterministic next-review date, and a surveillance/update note. Safety-relevant and actively changing sources receive shorter recheck intervals. Older versions remain addressable as superseded or procedural references.

The complete inventory and artifact identities are in [`provenance/INITIAL-SOURCE-INVENTORY.md`](provenance/INITIAL-SOURCE-INVENTORY.md). Rights, conflict, and review dispositions are in [`provenance/RIGHTS-CONFLICTS-AND-REVIEW.md`](provenance/RIGHTS-CONFLICTS-AND-REVIEW.md). Qualification evidence is in [`qualification/CT-V2-01-QUALIFICATION.md`](qualification/CT-V2-01-QUALIFICATION.md).

All clinical sources begin with pending specialist review. Metadata verification is not clinical review. Rights and legal gates remain pending where product use could involve noncommercial terms, international/AI restrictions, third-party material, method rights, or fee-distribution restrictions.

## Phase boundary

CT-V2-02 remains unopened. Its eventual authorization would permit ontology analysis, not automatically rule extraction or therapeutic implementation. V1 migration remains denied by default and no V1 production artifact entered this phase.
