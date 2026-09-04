# ADR 0019: Treat language realization as an untrusted candidate producer

Status: Accepted for CT-V2-13 qualification

## Decision

Thomas's upstream typed semantic command remains authoritative. A language realizer receives only a minimal `RendererInput` and returns a `CandidateRealization`. The candidate cannot become user-visible until deterministic validation accepts it. Bounded failure uses a deterministic fallback.

Reference realization, external realization, validation, and surface anti-repetition are implemented in a pure Kotlin module. The renderer has no safety, route, retrieval, longitudinal-write, mode-selection, Journal-posture, or Biographer-target port.

## Consequences

Surface wording can vary without moving semantic authority into a model. Renderer-visible memory is limited to memory already authorized upstream. Candidate text, accepted Thomas text, and render-history fingerprints are not user evidence. A future model adapter must be separately admitted and cannot bypass validation.

Deterministic validation proves mechanical constraints and the fixed qualification corpus; it does not prove arbitrary semantic equivalence for unconstrained natural language.
