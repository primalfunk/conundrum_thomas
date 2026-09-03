# CT-V2-06 personal evidence and provenance

## Separate provenance domains

Clinical-source provenance answers why Thomas behavior might be justified. Personal-evidence provenance answers where information about one user's life came from. CT-V2-06 defines only the latter and does not reuse clinical authority records as personal truth.

## Source to assertions

One `SourceRecord` can yield multiple assertions. Multiple source records can support one `LifeEntity`. Every assertion retains its source identifier, and every life structure retains its supporting assertion identifiers. Original content remains present or future-addressable through `OriginalSourceContent`.

The structured representation is intentionally not claimed to be a lossless substitute for the user's words.

## Fact, interpretation, and hypothesis

```text
Source: "I think my father hated that job."

preserved fact: the user communicated those words
user interpretation: the user suspects a third party held that attitude
third party's internal state: not established
Thomas hypothesis: absent unless separately created with dependencies
```

The constructor prevents a third-party internal-state predicate from being labeled an explicit user-established fact. `ThomasHypothesis` is structurally separate from `EvidenceAssertion`.

## Contradiction

A contradiction relates two preserved assertions. It does not select a winner or delete either source. Adjudication can remain `UNRESOLVED`, point to an explicit correction, identify different scope, or record a resolution while preserving history.

## Correction

A correction is a new assertion from a `USER_CORRECTION` source. It links to the earlier assertion. `CORRECTS_AND_SUPERSEDES` additionally requires an explicit acyclic supersession edge. The original assertion remains queryable.

## Supersession

Supersession is distinct from mere contradiction. It states that one assertion or hypothesis refines, replaces, corrects, or invalidates a predecessor. The relationship has its own stable identity and rationale. Acyclic validation prevents dishonest histories.

## Hypothesis dependency

A hypothesis cannot exist validly without at least one dependency. Each dependency points to an existing assertion or hypothesis and identifies whether it supports, contextualizes, or weakens the hypothesis. This makes later review after source correction possible without implementing that later behavior now.

## Identity uncertainty

References such as `Sam` and `friend Sam from college` may remain separate `Person` entities connected by `UNRESOLVED` or `CANDIDATE_SAME_ENTITY`. Only evidence-backed links can establish sameness or difference. Ingestion never silently merges them.

## Authority

No personal source or assertion grants therapeutic authority. No mode may directly mutate a psychological profile. There is no admission engine, production writer, persistence path, extractor, state projector, retrieval packet, or pattern engine in CT-V2-06.
