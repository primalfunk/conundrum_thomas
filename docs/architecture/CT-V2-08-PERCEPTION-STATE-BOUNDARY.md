# CT-V2-08 perception/state boundary

```text
:thomas:longitudinal
          ^
          |
:thomas:longitudinal-admission
          ^
          |
:thomas:language-evidence
  perception -> stateformation
          ^
          |
:qualification -> :thomas:longitudinal-store
```

`CommittedSourceText` can be constructed only from an existing `SourceRecord` containing exact inline content. Perception returns proposals, never receipts or mutations. `LanguageEvidenceProposalValidator` and CT-V2-07 admission both validate grounding. `GovernedLanguageEvidencePipeline` exists only in qualification and sends every durable object through `LongitudinalAdmissionController.submit`.

Engine, safety, runtime, app, Android adapters, and renderer do not depend on language evidence. Therefore raw prose cannot become ordinary therapeutic-policy input. State formation accepts only an immutable snapshot plus explicit eligibility sets and perception dispositions; it cannot access SQL or mutate the projection.
