# CT-V2-11 Retrieval Eligibility

Eligibility is evaluated before relevance. A high score cannot revive an ineligible object.

## Ordinary-use exclusions

The retriever excludes `PRIVATE_INELIGIBLE`, `SUPERSEDED`, `RETIRED`, `REVIEW_REQUIRED`, `DEPENDENCY_BLOCKED`, audit-only, and otherwise ordinary-ineligible objects. It also excludes non-current source revisions, non-user source authorship, assertions whose source is ineligible, and entities without eligible supporting assertions.

A hypothesis is ordinary-eligible only when its own lifecycle/status is eligible and every dependency chain is inspectable and eligible. If support or known counterevidence becomes private or blocked, the hypothesis is omitted instead of being presented one-sidedly. `DECLINED` coverage is operational absence of coverage and never substantive evidence.

Explicit source recall can inspect an existing non-private historical revision, but the returned item is marked non-current. `EXPLAIN_DERIVED_OBJECT` can inspect retired/review-required derived history, preserving its lifecycle and `currentAuthority = false`; it cannot present that object as Thomas's current belief.

## Revision behavior

Every request names a compatible snapshot revision. The qualification read adapter asks CT-V2-07 for evidence, lifecycle, and digest as of that revision. Current-source identity is computed inside that reconstructed snapshot. This keeps current and historical retrieval distinct.

There is no cache. Privacy, correction, retirement, supersession, source revision, or identity revision takes effect on the next request because its revision and state are reread.

## Fail-closed behavior

Invalid intent/mode combinations, production authority, unsupported policy versions, missing mandatory explicit targets, read failures, and revision mismatches produce typed rejection or revision-unavailable results. They do not broaden the search and cannot mutate state.
