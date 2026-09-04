# CT-V2-14 Data Lifecycle and Retention

| Category | Policy |
| --- | --- |
| Committed user source | Retain until governed user deletion |
| Source revision/correction lineage | Revision-retained |
| Derived state and indexes | Recomputable; never authoritative without eligible support |
| Committed speech transcript | Retain as text source; never imply raw-audio retention |
| Context packet / retrieval working set | Ephemeral |
| Render anti-repetition fingerprint | Ephemeral session state |
| Draft | Never persisted as evidence |
| Raw speech audio | Never persisted by this boundary |
| Renderer candidate/rejected candidate | Never persisted |
| Model prompt/output | Never persisted by this boundary |
| Narrative logs/crash diagnostics | Never authorized |

Privacy immediately makes source revisions and direct evidence ineligible and dependency-blocks derived state. Restoring an eligible privacy designation creates review-required state rather than silently reactivating it. Correction retains original and corrected evidence, records the relation, and makes current authority explicit. Supersession and identity decisions survive restart and protected backup/restore.

Selective deletion removes an entire stable-source history and its dependent current material. Complete reset removes the app-controlled protected artifact, destroys primary key material, clears in-memory state, and does not claim erasure of externally held exports/backups.

Temporary files used for atomic storage are ciphertext only and are removed on success/failure. The repository ignores database, protected store, backup, export, key, log, model, and build artifacts.
