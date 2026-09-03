# ADR 0006: V1 migration is deny by default

- Status: Accepted
- Date: 2026-09-03

## Decision

V1 is an immutable engineering reference, never an architectural dependency. Every potential component begins with `approvalState: DENIED`. Classification as reusable is an audit opinion, not migration approval.

A later migration requires an immutable source commit and path/blob identity, an approved destination, stated adaptation requirements, qualification criteria, explicit approval, and the eventual V2 migration commit. Dirty-state evidence cannot be migrated directly. Material must first become an accepted immutable source under a separately authorized process.

No V1 production source, model, adapter, database, asset, or test implementation crosses into V2 during CT-V2-00.
