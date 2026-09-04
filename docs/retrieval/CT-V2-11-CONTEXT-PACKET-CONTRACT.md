# CT-V2-11 ContextPacket Contract

`ContextPacket` is immutable structured data, not a prompt string and not evidence.

## Layers

1. **Authority:** an existing mode contract; retrieved text has no instruction authority.
2. **Safety:** constraints supplied by a higher authority; retrieval cannot infer safety state.
3. **Immediate conversation:** current user data and assistant continuity data, distinct from durable evidence.
4. **Runtime state:** dated, explicitly supplied runtime observations; no trait conversion.
5. **Longitudinal objects:** selected IDs, lifecycle/current authority, epistemic role, uncertainty, times, provenance, related IDs, unresolved identity, typed value, and retrieval reasons.
6. **Source excerpts:** exact, revision-bound user-source data with offsets, fingerprint, acquisition/report/event time, epistemic role, and truncation marker.
7. **Metadata:** policy/revision/intent/mode, candidate and exclusion counts, budget use, truncation, traversal depth, and digest.

## Default budget (`ct-v2-11.context-budget.v1`)

| Limit | Value |
|---|---:|
| Total textual characters | 4,096 |
| Longitudinal objects | 8 |
| Source excerpts | 4 |
| Characters per excerpt | 320 |
| Dependency depth | 2 |
| Immediate items | 6 |
| Runtime items | 4 |

Budgets are validated, versioned, and deterministic. Exhaustion is normal. Authority and epistemic honesty outrank breadth. Text values may be represented as explicitly omitted when the total-text budget is exhausted. Source excerpts are omitted when a truncation would discard a negation or uncertainty marker.

## Digest

The packet digest is SHA-256 over a length-delimited canonical logical encoding. Anchor sets are sorted; safety constraints are sorted; selected IDs and relationships are deterministically ordered. The digest excludes file paths, database row order, process identity, wall clock, and hash-map iteration. Legitimate correction/privacy/revision changes change logical content and therefore the digest.

Packets are ephemeral. CT-V2-11 adds no packet table, history, log dump, or shadow database.
