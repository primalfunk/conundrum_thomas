# V1 migration governance

[`v1-component-register.json`](v1-component-register.json) is the authoritative deny-by-default register. Its classifications describe audit reuse potential; they do not authorize copying, importing, refactoring, reimplementation, or binary admission.

A component can move from `DENIED` only through a reviewable change that identifies an immutable source object, approves a destination and adaptation plan, defines qualification, records explicit authorization, and later records the exact V2 migration commit. Dirty-state material is never directly migratable.

CT-V2-00 authorizes no migrations. The future user-data path is also denied: any later importer must be one-way, explicit, auditable, version-aware, validation-first, and independently authorized.
