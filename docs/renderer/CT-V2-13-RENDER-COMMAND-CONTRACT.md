# CT-V2-13 Render Command Contract

`GovernedRenderCommand` is the sole authoritative input to CT-V2-13. It is immutable structured data, not a prompt. It fixes the mode, response posture, semantic act and version, visible semantic units, explicitly surfaceable history, epistemic and temporal constraints, prohibited claims, style and directiveness ceilings, advice and memory permission, response budget, bounded fallback, and synthetic qualification authority.

Semantic units distinguish user self-report, belief, interpretation, third-party report, current event/entity, structural fact, reflection/question/action targets, memory recall, tentative connection, evidence explanation, and safety requirements. Each unit declares epistemic status, attribution, source-data versus governed-meaning authority, temporal scope, allowed use, required surface markers, and transformations the realizer may not perform.

The command never contains a longitudinal store, `ContextPacket`, retrieval port, admission controller, route evaluator, safety evaluator, Journal capture engine, Biographer coverage engine, Android context, model handle, network client, or arbitrary governing prompt.

## Fixed limits

| Budget | Characters | Sentences | Questions |
|---|---:|---:|---:|
| silence | 0 | 0 | 0 |
| brief reflection | 320 | 2 | 0 |
| one question | 320 | 2 | 1 |
| Therapy | 640 | 4 | 1 |
| evidence explanation | 900 | 6 | 2 |
| fixed safety | 480 | 3 | 1 |

External realization is capped at two attempts. Memory permission is `NONE`, `ONE_AUTHORIZED`, or bounded explicit support with at most four already-selected items. Ordinary CT-V2-12 Therapy supplies at most one.

`RendererInput` is derived from this command plus fingerprint-only recent render history. `LanguageRealizer.realize(RendererInput, attempt)` returns a `CandidateRealizationOutcome`; it never returns accepted user-visible output.

`GovernedRenderResult` records disposition, accepted text when any, mode, semantic act, realization source, attempt count, fallback use, measured counts, surfaced-memory IDs, validation reasons, next ephemeral history, policy version, and a canonical logical digest.
