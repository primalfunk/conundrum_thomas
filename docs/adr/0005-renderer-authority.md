# ADR 0005: Narrow renderer authority

- Status: Accepted
- Date: 2026-09-03

## Decision

A renderer receives exactly:

1. a `RenderCommand`; and
2. supporting evidence or text explicitly authorized for that render request.

It receives no autonomous therapeutic authority and no unrestricted profile, transcript, provenance, session, or database access. A renderer returns an untrusted draft for downstream validation; it does not return a therapeutic decision.

The renderer module may depend on the platform-independent renderer contract. It may not depend on ordinary policy, safety implementation, or persistence. The application does not depend directly on the renderer implementation.

Historical R007, R008, R010, R011, and other V1 model artifacts are research candidates only. None is an admitted V2 renderer. Admission requires renderer-specific instruction-fidelity, forbidden-act, unsupported-addition, and therapeutic-policy-leakage qualification on target hardware.
