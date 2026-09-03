# CT-V2-06 temporal model

## Three independent coordinates

For an assertion Thomas can resolve:

1. `eventTime`: when the described event occurred;
2. `reportTime`: when the user communicated the source;
3. `recordTime`: when Thomas recorded the source structure.

`eventTime` belongs to the assertion. `reportTime` and `recordTime` belong to the source. `LongitudinalEvidenceSnapshot.temporalCoordinates` joins them without collapsing them. Record time cannot precede report time, but event time can precede, equal, follow, or remain unrelated to both.

## Event-time forms

`EventTime` is sealed and supports:

- exact instant;
- calendar date;
- approximate date with declared precision;
- approximate year;
- range with exact or approximate endpoints;
- relative period with an optional event/entity anchor;
- ongoing interval with known or unknown start;
- explicit before/after relation;
- uncertain chronology with multiple named candidates;
- unknown time with an optional stated reason.

Calendar boundaries may be a date, month, or year. Invalid reversed ranges cannot be constructed.

## No false precision

Examples:

```text
"around 2012"                 -> ApproximateYear(2012)
"sometime that winter"        -> RelativePeriod("that winter", AROUND, optional anchor)
"a couple of years before..." -> BeforeOrAfter(reference, BEFORE, original description)
"for several months"          -> RelativePeriod(description, DURATION)
"still ongoing"               -> OngoingInterval(...)
"I do not remember exactly"   -> Unknown(reason) or UncertainChronology(...)
```

No date parser, clock-based inference, or LLM resolution exists in this phase.

## Present becomes past

A Journal source recorded on 2026-09-03 retains Journal acquisition, report time, record time, and event time permanently. A later Biographer source about the same event is another source and assertion. Time passing makes the event historical; it does not convert its provenance to Biographer or require data migration.
