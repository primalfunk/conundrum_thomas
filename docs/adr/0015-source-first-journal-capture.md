# ADR 0015: Source-first Journal capture

## Status

Accepted for CT-V2-09 qualification; production authority denied.

## Decision

Introduce pure Kotlin `:thomas:journal`. Journal commits exact source evidence through an abstract admission port before language processing or optional response planning. Its only CT-V2-09 implementation adapter lives in `:qualification` and delegates every durable operation to the CT-V2-07 admission controller.

Journal defaults to `NO_RESPONSE`. `REFLECT` and `ASK_ONE_QUESTION` authorize only a typed, current-entry-grounded response plan. Response posture is excluded from source provenance and canonical capture identity. Private entries persist as sources but bypass derivation and response. Post-commit edits append revisions.

## Consequences

Capture survives perception, planning, or rendering failure. Drafts, responses, models, Android/app/runtime code, Therapy, Biographer, and direct SQL cannot create Journal evidence. CT-V2-09 remains synthetic-only and does not authorize production persistence, retrieval, privacy lifecycle, or user-facing Journal behavior.
