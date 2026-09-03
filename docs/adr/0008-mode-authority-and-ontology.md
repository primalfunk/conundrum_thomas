# ADR 0008: Mode authority and behavior-free ontology

- Status: Accepted for CT-V2-02
- Date: 2026-09-03

## Decision

Therapist, Biographer, and Journal are compile-visible architectural contracts, not language-model personas:

- Therapist's primary function is intervention.
- Biographer's primary function is investigation.
- Journal's primary function is capture and defaults to `NO_RESPONSE`.

All three contracts deny direct profile mutation and language-model decision authority. CT-V2-02 grants none of the modes runtime authority.

Therapeutic vocabulary is isolated in the platform-independent `:thomas:ontology` module. Concepts carry stable IDs, definition maturity, source-support state, and a runtime-authorization state. Every CT-V2-02 concept is runtime-denied; intervention and safety concepts remain candidates.

Source-derived candidates bind to exact governed provenance records. A binding is provenance only and cannot authorize a rule or behavior.

## Consequences

- Journal silence is a first-class action rather than a renderer failure.
- Biographer evidence cannot become a durable fact without a later governed admission subsystem.
- Unknown, tentative, conflicting, and insufficient evidence remain explicit.
- No production module consumes the ontology during CT-V2-02.
- CT-V2-03 must introduce any production dependency or selection behavior explicitly and under separate authorization.
