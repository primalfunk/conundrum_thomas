# CT-V2-07 revision and dependency state machine

## Lifecycle states

| State | Eligible for ordinary use | Meaning |
|---|---:|---|
| `ACTIVE` | yes, unless source privacy denies it | Current admitted material. |
| `CONTESTED` | no | A correction contests the prior claim without erasing it. |
| `SUPERSEDED` | no | An explicit revision, correction, or supersession replaced current authority. |
| `RETIRED` | no | Explicitly retired while retained historically. |
| `REVIEW_REQUIRED` | no | Supporting state changed; no truth judgment was made. |
| `DEPENDENCY_BLOCKED` | no | An active dependency became unusable, including through privacy. |
| `PRIVATE_INELIGIBLE` | no | Source or derived material is private and unavailable for ordinary use. |

Each transition is recorded at a store revision with a cause code and immutable ledger anchor. Historical states remain readable.

## Transition causes

```text
new object                         -> ACTIVE or PRIVATE_INELIGIBLE
source revision appended           -> prior revision SUPERSEDED; new revision ACTIVE;
                                      prior-wording derivations REVIEW_REQUIRED
explicit correction (contest)      -> corrected claim CONTESTED
explicit correction (supersede)    -> corrected claim SUPERSEDED
explicit supersession              -> predecessor SUPERSEDED
retirement                          -> claim RETIRED
support corrected/superseded        -> derived dependent REVIEW_REQUIRED
support made private                -> source/assertions PRIVATE_INELIGIBLE;
                                      derived dependent DEPENDENCY_BLOCKED
privacy restored                   -> affected material REVIEW_REQUIRED;
                                      no silent reactivation
```

No transition deletes a source, assertion, entity, hypothesis, or relation. Recency alone causes no transition.

## Dependency behavior

Hypothesis dependencies are explicit directed relations. Admission requires every new hypothesis dependency chain to terminate at eligible source-backed evidence. Cycles fail closed.

When a supporting claim is corrected, superseded, retired, or made private, direct and transitive derived dependents are located deterministically and made ineligible. The transition says only that review is required or support is blocked; it does not decide that the hypothesis is true or false.

An explicit user correction takes precedence over a conflicting Thomas interpretation for current eligibility. Both remain visible in historical reads, with the correction source and relation preserving how the change became known.

## Contradiction

A contradiction adds a durable relation between two assertions. Neither endpoint is automatically removed, downgraded, superseded, or selected. Resolution requires a separate governed operation.

## Identity decisions

Identity links are decision history, not physical merges. A pair may remain unresolved, be established as the same entity, or be established as different. Changing that decision requires the current decision ID and appends a new decision. Original entities and all prior decisions remain present.

Conflicting simultaneous resolutions and stale decision revisions fail closed. Invalidated identity support can make dependent material review-required; it does not rewrite the referenced objects.

## Coverage and privacy

`PRIVATE` and `DECLINED` are valid coverage states. `DECLINED` carries no substantive assertion and cannot be queried as evidence that something is absent. A PRIVATE source may remain stored but cannot support new derived material. A transition to PRIVATE immediately removes current eligibility from its assertions and blocks derived dependents. Restoring source privacy changes the privacy flag but leaves affected evidence review-required; a privacy toggle cannot silently revive evidence whose lifecycle may have changed.

Irreversible purge, retention policy, and user-facing deletion are not part of CT-V2-07.
