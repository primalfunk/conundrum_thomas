# Governed source provenance

This directory is the reviewable source of truth for CT-V2-01:

`official authority -> document -> version -> locator/section -> candidate subject -> future abstraction/rule/test`

Only the left side through candidate subject exists. Candidate subjects carry `no_rule_authority = 1`; they are unopened inputs to a later authorized phase.

- `migrations/` contains the versioned relational schema.
- `seeds/` contains reviewable metadata and Thomas-authored abstracts.
- `generated/` is ignored output rebuilt with `gradlew generateRuntimeProvenanceDb`.
- `raw/` is an ignored local acquisition cache. Raw artifacts never become a runtime dependency.

Source registration, metadata verification, and hash capture do not constitute clinical approval, rights clearance, or authorization to implement behavior.
