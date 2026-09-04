# CT-V2-10 Coverage Model

## Inputs

The qualification adapter projects only eligible CT-V2-07/08 state into `CoverageEvidence`. It uses stable IDs, typed life entities, honest `EventTime` values, governed coverage topics, contradictions, unresolved identities/corrections, and open evidentiary questions. Private source revisions do not contribute grounding.

`CoverageEvidence` contains:

- store revision and derivation-rule version;
- represented periods, roles, places, and relationships;
- structural `CoverageCandidate` values;
- each candidate's target ID/kind, grounding IDs, entity IDs, temporal bounds, uncertainty, status, reason code, safe facts, answerability, and material-change token.

## Output map

`BiographerCoverageMap` deterministically reports represented/sparse periods; unresolved temporal, identity, contradiction, correction, and open-question targets; represented roles, places, and relationships; deferred, declined, private, and previously investigated targets; every target and eligibility; eligible targets; at most one selected target; rule versions; and a SHA-256 logical digest.

The digest excludes object identity, map iteration order, rendering, and wall-clock timing. Same inputs and history produce the same map and selection.

## Status mapping

| Longitudinal meaning | Biographer meaning | Automatic targeting |
|---|---|---|
| `UNKNOWN` / `NOT_EXPLORED` | `UNKNOWN` | eligible only when a structural target exists |
| `PARTIAL` | `SPARSE` | eligible |
| `UNRESOLVED` | `UNRESOLVED` | eligible |
| `SUFFICIENTLY_UNDERSTOOD` / `IRRELEVANT` | covered enough | excluded |
| `PRIVATE` | `PRIVATE` | hard excluded |
| `DECLINED` | `DECLINED` | hard excluded unless user explicitly reopens |

Operational `DEFERRED` and `RECENTLY_ASKED` do not become longitudinal facts.

## Temporal coverage

The adapter may identify a sparse interval between two represented calendar years. It retains both typed boundary expressions, and approximate bounds remain approximate. The target means only that little represented evidence exists between the bounds. It never asserts a move date, continuous residence, causal transition, or exact interval.

## Persistence posture

The map and operational history are qualification values, not production data stores. Durable source evidence, coverage transitions, corrections, contradictions, and identity decisions remain in the CT-V2-07 ledger. No production Biographer persistence is introduced.
