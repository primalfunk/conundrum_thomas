# ADR 0009: Qualification-only procedural vertical slice

- Status: Accepted for CT-V2-03
- Date: 2026-09-03

## Decision

`:thomas:engine` may depend on the platform-independent `:thomas:ontology` and implement one deterministic bounded-problem ruleset. Every rule must declare stable identity, typed conditions, exact provenance, unresolved reviews, qualification execution status, and production therapeutic denial.

The engine consumes structured state only. It does not classify raw prose. Upstream safety and scope dispositions are mandatory inputs; unknown or specialized dispositions terminate the ordinary slice. The safety module remains independent and unchanged.

A structured render command carries the already-selected action, dialogue act, goal, semantic boundaries, content authorization, form, length, question, and advice constraints. A deterministic renderer stub exists only in `:qualification`. Neither app nor runtime wires the slice for user-facing use.

## Consequences

- A language model is unnecessary for policy selection or qualification.
- The application has no configured therapeutic flow or renderer path.
- `EXECUTABLE_FOR_QUALIFICATION` is distinct from production runtime authority.
- Source and reviewer restrictions remain visible in every decision.
- Equal-priority conflicts, missing authority, and undefined states fail visibly.
- Journal defaults to `NO_RESPONSE`; Biographer receives no therapeutic authority.
- Later production admission requires separately authorized clinical, rights, implementation, safety, and runtime work.
