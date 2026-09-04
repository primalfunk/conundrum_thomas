# CT-V2-11 Mode and Intent Retrieval Policy

## Intents

| Intent | Required purpose | Boundary |
|---|---|---|
| `ORDINARY_MODE_CONTEXT` | Small potentially useful ordinary context | Journal and Biographer return no historical context; Therapy is qualification-only |
| `EXPLICIT_LOOK_BACK` | User-directed prior Journal recall | Requires Journal mode and explicit user direction |
| `BIOGRAPHER_TARGET_CONTEXT` | Local context for an existing CT-V2-10 target | Requires Biographer mode and a target ID; cannot select or reprioritize it |
| `EXPLAIN_DERIVED_OBJECT` | Minimal “why do you think that?” evidence neighborhood | Requires an explicit hypothesis ID; reports non-current lifecycle honestly |
| `EXPLICIT_SOURCE_RECALL` | Exact user-directed source/revision inspection | Requires explicit source identity/revision; privacy remains absolute |

## Mode pressure, not truth

- **Journal:** ordinary historical retrieval is empty. Explicit Look Back considers eligible Journal sources using exact structural anchors, optional time bounds, and deterministic lexical overlap. It creates no hypothesis and cannot enter Therapy.
- **Biographer:** target context is locally aggressive around the already-selected target: direct assertions, entities, events, periods, contradiction sides, and boundary evidence. It cannot change the target or revive private/declined material.
- **Therapy:** qualification-only ordinary retrieval may select a small set of directly anchored claims, entities, events, beliefs, recurrence candidates, or hypotheses. Retrieval does not choose an intervention and keyword overlap cannot trigger a technique.

The same underlying eligibility and epistemic role apply in every mode. Only candidate pressure differs.

## Request authority

Each request carries a request ID, policy version, intent, mode, exact snapshot revision, structured anchors, budget, any mandatory target ID, explicit-user-direction flag, and `SYNTHETIC_QUALIFICATION_ONLY` authority. No model-generated query or arbitrary database query interface exists.
