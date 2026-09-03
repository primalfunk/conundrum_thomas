# ADR 0002: Independent V2 identity and development floor

- Status: Accepted
- Date: 2026-09-03
- Scope: CT-V2-00 development baseline

## Decision

- Android namespace and application ID are `com.conundrum.thomas.v2`.
- V2 installs independently of V1.
- CT-V2-00 provides no V1 user-data continuity.
- `minSdk 31` is the V2 development baseline.
- `arm64-v8a` is the initial native-inference ABI.

These platform values are development constraints, not final public-release compatibility commitments.

## Future importer requirement

If V1 data import is later authorized, it must be one-way, explicit, auditable, version-aware, validation-first, and separately qualified. No importer or compatibility storage is implemented in CT-V2-00.
