# ADR 0018: Route-first longitudinal Therapy composition

## Status

Accepted for synthetic qualification in CT-V2-12.

## Decision

Add a pure-JVM `:thomas:therapy-longitudinal` composition module. It consumes the existing safety, ordinary Therapy, context-packet, retrieval, language-evidence, admission, and longitudinal contracts but no store implementation. Only `:qualification` supplies synthetic CT-V2-07 admission/reader adapters.

The integration order is architectural: establish the pre-turn history revision, capture the user source, evaluate current safety, select the CT-V2-05 route/action, retrieve against the pre-turn revision, apply a separate deterministic memory-use gate, and emit a typed plan. The core Therapy evaluator cannot see retrieval state. The future renderer can see only the explicitly authorized render-support envelope, never the complete packet.

Current-turn capture failure and retrieval failure degrade to a truthful memoryless base plan. Safety preemption prevents ordinary policy and retrieval. Session memory-reference history is immutable, ephemeral operational state and cannot become user evidence.

## Consequences

Longitudinal evidence can improve continuity while retaining zero route, technique, safety, mode, admission, or profile-mutation authority. Same-turn self-retrieval is excluded by revision. Ordinary surfacing is capped at one direct structural reference. Hypotheses are unavailable for automatic ordinary surfacing; explicit explanation remains balanced and lifecycle-aware. No final renderer, prompt, model, Android composition, real-user data, production persistence, or production therapeutic authority is introduced.
