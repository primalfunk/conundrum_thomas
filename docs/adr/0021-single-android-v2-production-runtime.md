# ADR 0021: One auditable Android V2 production runtime

- Status: Accepted for CT-V2-15 qualification
- Date: 2026-09-04

## Context

CT-V2-14 left production-capable persistence deliberately disconnected from
the Android app. Journal, Biographer, Therapy, retrieval, longitudinal Therapy,
and governed rendering had qualified owners, but no single product path joined
them. Multiple application controllers would make it ambiguous which authority
produced a visible response.

## Decision

`:app` owns exactly one `ThomasAndroidCompositionRoot`. It opens the protected
Android store and gives that governed port to one platform-neutral
`ThomasProductionRuntime`. The runtime serializes committed turns and delegates
policy decisions to the existing mode, safety, Therapy, retrieval, and renderer
owners. `ThomasViewModel` projects results and sends commands; it does not own
domain policy.

The production realizer is CT-V2-13's deterministic reference realizer. Speech
is visibly unavailable because no private/offline STT or final-artifact TTS
adapter has been qualified. Generic Android backup remains disabled.

## Consequences

- Every production assistant artifact has one inspectable governed path.
- The app directly depends on the runtime and Android persistence adapter, but
  receives no database, SQL, DAO, key-material, retrieval, or renderer bypass.
- Journal `NO_RESPONSE` creates no assistant artifact and invokes no realizer.
- Data custody is explicit through user-selected documents and independently
  protected backups; key bytes are never exposed by the composition root.
- Model, remote-inference, V1 conversation, and speech authority remain zero.
