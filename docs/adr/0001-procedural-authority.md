# ADR 0001: Thomas owns therapeutic authority

- Status: Accepted
- Date: 2026-09-03
- Scope: Governing V2 architecture

## Decision

Thomas selects and governs therapeutic behavior. A language model may perform only a bounded, explicitly authorized inference or language-realization role. It may not determine therapeutic objective, intervention, safety disposition, prohibited behavior, or certainty.

The ordered therapeutic pipeline is owned by `:thomas:runtime`; structured policy is implemented in `:thomas:engine`; safety is independently authoritative in `:thomas:safety`; renderer implementations are downstream platform adapters. The application cannot directly invoke model or inference implementations.

## Consequences

- Policy accepts structured state, not raw user prose.
- Safety can replace or suppress ordinary policy output and does not depend on the ordinary policy implementation.
- Model fluency cannot be treated as evidence of therapeutic competence.
- V1 therapeutic prompting and autonomous-controller designs are retired from the V2 target architecture.
- Existing V1 engineering cannot force a change in this authority model.
