# CT-V2-12 Longitudinal Therapy Turn Contract

## Ordered decision

One turn has the fixed order below:

1. Record `preTurnHistoricalRevision` from the caller's governed store boundary.
2. Attempt admission of the exact current user turn with `THERAPIST_CONVERSATION` provenance.
3. Process eligible captured source text through CT-V2-08. Private source text remains immediate input but skips ordinary derivation.
4. Evaluate CT-V2-04 current-turn safety. A non-ordinary result ends ordinary integration.
5. Evaluate CT-V2-05 with only its existing state and safety-issued permit.
6. Request a CT-V2-11 Therapy packet at `preTurnHistoricalRevision`.
7. Narrow that packet through the memory-use gate.
8. Return an immutable `LongitudinalTherapyPlan` and a render-support envelope.

The route evaluator has no retrieval input. The retrieval port is unreachable until the route decision is complete. The current source may already have been admitted when retrieval occurs, but the request remains pinned to the earlier revision and the packet is additionally rejected if it contains the current source revision.

## Typed result

The plan records session and turn IDs, capture disposition and receipt, pre-turn revision, safety decision reference, route/action/progression reference, retrieval intent, packet summary and digest, memory-use disposition, surfaced memory support, degradation states, ordered integration trace, next ephemeral session-memory state, version, and canonical SHA-256 digest.

The plan is not prose and is not evidence. Its digest uses logical identifiers, versions, revisions, decisions, memory references, and operational state. It excludes machine paths, database row order, process identity, execution duration, and map iteration order.

## Degradation

- Capture failure: report it, do not retrieve, and retain a valid permitted memoryless Therapy plan.
- Post-capture language failure: retain the source, report the failure, suppress retrieval for the turn, and retain the base plan.
- Retrieval failure or invalid packet: retain the already-selected route and return a memoryless plan.
- Empty or weak context: return the route with no surfaced memory.
- Safety preemption: do not select an ordinary route and do not call ordinary retrieval.

No degradation path manufactures a stored turn, improvises a route, or broadens safety authority.
