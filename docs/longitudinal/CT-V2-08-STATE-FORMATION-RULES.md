# CT-V2-08 state-formation rules

State formation is deterministic organization of admitted eligible evidence. It is not a psychological formulation.

| Rule | Deterministic effect |
|---|---|
| `SF-CURRENT-SOURCE` | Only the latest source revision for each stable source identity may contribute to current state. Prior evidence remains historical. |
| `SF-ELIGIBILITY` | A source/assertion excluded by CT-V2-07 lifecycle or privacy cannot contribute. Returning from private does not silently reactivate review-blocked evidence. |
| `SF-EPISTEMIC-PARTITION` | Active assertions are partitioned by explicit claim, self-report, self-belief, user interpretation, and third-party report. |
| `SF-IDENTITY-UNRESOLVED` | Same-label person references remain separate until governed identity decisions establish every pair. |
| `SF-CONTRADICTION` | Already-admitted same-scope contradictions are exposed without selecting a winner. |
| `SF-RECURRENCE-3-SOURCES` | Three or more comparable behavior reports from distinct stable sources may form `REPEATED_REPORTED_OCCURRENCE`. Multiple derivatives of one source count once. |
| `SF-OPEN-GAP` | Unknown time, ambiguous correction, unresolved identity, and unresolved contradiction form typed evidentiary gaps, not dialogue. |
| `SF-DIGEST` | Sorted logical identifiers and typed properties produce a SHA-256 state digest independent of database row order. |

Structural contradiction proposal is limited to active claims with the same predicate, unequal normalized structured values/polarity, and the same typed temporal scope. Different years and different phases are not contradictions. Contradiction admission uses the existing CT-V2-07 gate.

No rule forms a diagnosis, causal account, trait, motive, defense, attachment style, personality type, therapy question, or therapeutic action.
