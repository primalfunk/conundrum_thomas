# ADR 0010: ordinary Therapist policy requires a safety-scope capability

**Status:** Accepted for CT-V2-04 qualification

## Decision

The ordinary Therapist evaluator requires an `OrdinaryTherapyPermit`. Only the deterministic safety/scope gate can construct that permit in normal Kotlin code. A permit is bound to the gate policy version, structured-state identifier, and safety-evidence revision.

No state-only ordinary-policy evaluation overload exists. A denied, unresolved, specialized, emergency, conflicting, invalid, or review-blocked gate decision contains no permit. A stale or mismatched permit fails closed before ordinary rules execute.

## Consequences

- Safety is a compile-visible prerequisite rather than caller convention.
- The gate decides software authority, not a person's clinical risk level.
- CT-V2-03 retains defense-in-depth scope guards after admission.
- Gate and ordinary policy remain unwired from application/runtime code.
- All permits and rules are qualification-only; production authority remains zero.
