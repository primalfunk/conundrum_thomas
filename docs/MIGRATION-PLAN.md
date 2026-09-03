# Controlled V1-to-V2 migration plan

No migration is authorized in CT-V2-00. This plan defines how a future authorization can remain narrow and auditable.

## Admission sequence

1. **Proposal:** Select one register component. State the V2 need without treating V1 design as a constraint.
2. **Immutable source resolution:** Pin one accepted or otherwise explicitly approved 40-hex source commit and enumerate every source path with its Git blob/tree identity. Dirty worktree material is ineligible.
3. **Rights and security review:** Establish license, redistribution, secrets, user-data, model, and source-material disposition before extraction.
4. **Qualification contract:** Add the required non-clinical or appropriately authorized behavioral tests before implementation. Therapeutic expectations cannot be invented by a migration.
5. **Explicit approval:** Change only that component's register state from `DENIED` to `APPROVED_FOR_MIGRATION` in a reviewable governance commit. Classification alone cannot make this change.
6. **Controlled extraction:** In a dedicated one-component branch/worktree, extract only the enumerated objects from the immutable source commit. Verify each extracted byte sequence against its recorded object identity. Do not add V1 as a Gradle dependency, Git submodule, subtree, package repository, or live source path.
7. **V2 adaptation:** Place material only in the approved destination and adapt it to V2 contracts. Delete any therapeutic, prompt, persistence, logging, authority, or lifecycle behavior not expressly admitted.
8. **Qualification and audit:** Run component, architecture, privacy, artifact, and applicable layer-specific tests. Scan the diff for undeclared source and dependency expansion.
9. **Migration payload commit:** Commit the bounded implementation with source attribution and evidence. One migration component per payload commit.
10. **Receipt commit:** Record the payload commit as `eventualMigrationCommit`, attach qualification evidence, and set the register entry to `MIGRATED`. The receipt must not obscure or squash away the source identity.

If adaptation is so extensive that V1 code provides no defensible value, write a fresh V2 implementation from the V2 contract after authorization and retain V1 as `REFERENCE_ONLY`.

## Candidate sequence if later approved

The order below is dependency-informed, not an authorization recommendation:

1. Evidence/checksum conventions and artifact-confinement concepts.
2. Narrow inference lifecycle/cancellation/metrics concepts.
3. Android process/lifecycle foundation where the V2 app actually needs it.
4. Speech output, then independently qualified speech input.
5. JNI/llama renderer adapter only after the renderer contract and admission benchmark exist.
6. Persistence only after ADR 0003's security and retention gate is closed.
7. Durable profile/biographer concepts only after V2 state semantics exist.
8. Qualification harness ideas only after each target layer has authorized expectations.

V1 therapeutic planners, autonomous prompts, the monolithic controller, and Vulkan integration remain retired. V1 guards, fallbacks, retrieval, UI, desktop stack, and models remain reference-only unless reclassified and explicitly approved.

## Future V1 user-data importer

CT-V2-00 implements no importer and establishes no continuity. A later importer must be one-way, opt-in, version-aware, preflight-validating, transactionally applied, provenance-recording, resumable or safely reversible before commit, and able to produce an auditable import receipt without exposing content in logs. It requires its own authorization, threat model, deletion semantics, and qualification corpus.
