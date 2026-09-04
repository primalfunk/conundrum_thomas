# CT-V2-13 — Governed Language Renderer

## Purpose

CT-V2-13 establishes the synthetic-only boundary that turns an already-governed semantic decision into language. Its invariant is:

> Thomas decides what to say. The renderer decides how to say it.

A `LanguageRealizer` returns an untrusted candidate, never a response. The governed renderer accepts a candidate only after deterministic validation; otherwise it uses a deterministic reference fallback. Journal silence invokes no realizer at all.

## Authority and visibility

The immutable `GovernedRenderCommand` carries only the authorized mode, semantic act, renderable units, explicitly surfaced historical support, epistemic and temporal constraints, prohibited claims, response budget, restrained style, advice/memory permissions, fallback authority, and policy versions. It carries no ContextPacket, store, reader, writer, safety engine, Therapy engine, Biographer engine, Journal capture engine, Android context, or model implementation.

Current or historical user text inside a semantic unit is typed data. It cannot alter mode, safety, route, target, posture, memory selection, technique, evidence, or future policy. The realizer has no search or write port.

## Candidate validation and fallback

Mechanical validation covers output presence, control characters, character/sentence/question limits, declared mode and act, visible semantic and memory IDs, prohibited source IDs, required attribution and uncertainty markers, temporal fidelity, introduced entity/date declarations, known prohibited claim families, fixed safety wording, recent normalized duplicates, and repeated sentence openings.

This does not claim to prove arbitrary natural-language semantic equivalence. The deterministic reference corpus and scripted adversarial candidates qualify the fixed surface. Any future model must undergo adapter-specific empirical semantic qualification before admission.

External realization receives at most two attempts. Unavailability, exception, timeout, malformed or empty output, budget violation, semantic violation, or repetition invokes a bounded deterministic fallback without changing the upstream decision.

## Modes

- Journal `NO_RESPONSE` is successful silence and makes zero realization calls.
- Journal `REFLECT` and `ASK_ONE_QUESTION` remain current-entry grounded and non-therapeutic.
- Biographer receives the already-selected target and may produce one neutral question.
- Therapy receives only the CT-V2-12 render-support envelope and cannot see unsurfaced retrieval results.
- Safety rendering preserves supplied governed semantics and fixed required wording over style variation.

No production model, prompt pipeline, Android wiring, real-user data, production persistence, new therapeutic behavior, or next-phase authority is introduced.
