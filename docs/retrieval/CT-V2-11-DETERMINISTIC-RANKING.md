# CT-V2-11 Deterministic Ranking

## Candidate hierarchy

Eligibility precedes ranking. Eligible candidates are ordered by the following fixed tuple, with lower penalties preferred:

1. explicit target/source match;
2. direct evidence or Biographer-target dependency;
3. same explicitly resolved entity;
4. same event;
5. same relationship;
6. same period;
7. same typed predicate;
8. lexical match count (Look Back only);
9. temporal relation to explicit bounds;
10. mode acquisition preference;
11. stable ID.

The trace is represented by typed `RetrievalReason` values such as `EXPLICIT_TARGET`, `DIRECT_EVIDENCE`, `SAME_EVENT`, `ACTIVE_CONTRADICTION`, `CURRENT_CORRECTION`, and `REPRESENTATIVE_COUNTEREVIDENCE`.

## Lexical policy

Lexical matching is local and model-free. It uses Unicode alphanumeric tokenization, `Locale.ROOT` lowercase normalization, a fixed small stop-word set, exact normalized-token comparison, and deterministic counts. A multiword anchor is decomposed into tokens; CT-V2-11 applies no phrase bonus. It is enabled as a candidate reason only for explicit Journal Look Back. It never outranks explicit structural linkage and never resolves identity.

## Temporal policy

Exact instants, dates, approximate dates/years, ranges, and known starts of ongoing intervals produce coarse year windows solely for overlap/distance ordering. Approximate time remains approximate in selected objects. Relative, uncertain, and unknown forms receive no invented year and remain eligible through non-temporal anchors.

## Balanced neighborhoods

A selected hypothesis requires one representative support item and, when present, one representative weakening item. A contradiction requires both endpoints. If the object count or dependency-depth budget cannot carry the necessary neighborhood, the derived/contradiction object is omitted. The engine does not choose a winner.
