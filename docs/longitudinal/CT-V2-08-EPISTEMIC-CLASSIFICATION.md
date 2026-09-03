# CT-V2-08 epistemic classification

## Durable evidence classes

| Class | Meaning | Explicit non-claim |
|---|---|---|
| `EXPLICIT_USER_ASSERTION` | The user stated an observable/recollected proposition | Independent verification is not implied |
| `EXPLICIT_SELF_REPORT` | The user directly reported their own experience | No enduring trait or diagnosis is implied |
| `SELF_BELIEF` | The user expressed a belief/generalization about self | No objective pattern is established |
| `USER_INTERPRETATION` | The user expressed meaning, cause, judgment, or another-person inference | The interpretation is not converted to external fact |
| `THIRD_PARTY_REPORT` | The user reported what another person said | Thomas does not thereby know the reported content independently |
| `ENTITY_REFERENCE` | Language mentioned a possible entity | Identity resolution is not implied |
| `EVENT_REFERENCE` | Language represented an event | Temporal precision beyond the source is not implied |

Correction candidates, uncertainty markers, negation, nonassertive language, and ambiguity are perception results rather than alternate authorship. Only accepted evidence classes enter an evidence bundle. A correction needs a valid user source and deterministic target; otherwise its source remains durable and its correction remains unresolved.

## Structural protections

- `EvidenceAssertion.kind` remains the CT-V2-06 user-fact/user-interpretation boundary; the more precise `epistemicClass` must agree with it.
- `AssertionPolarity.NEGATIVE` preserves negation.
- `SourceSpanGrounding` anchors exact source revision, offsets, fragment, SHA-256, parser version, and any comparison-only normalization.
- A direct user assertion cannot describe another person's internal state as fact.
- A Thomas hypothesis remains the separate `ThomasHypothesis` type and still requires admitted dependencies.
- Questions, hypotheticals, counterfactuals, unsupported prose, and ambiguous content produce no evidence proposal.

No class grants therapeutic, diagnostic, profile-mutation, identity-resolution, privacy, or production authority.
