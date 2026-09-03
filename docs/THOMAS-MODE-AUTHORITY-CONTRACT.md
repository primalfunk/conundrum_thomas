# Thomas mode-authority contract

**Status:** Locked by CT-V2-02; qualification-only Therapist repertoire expanded by CT-V2-05

**Runtime authority granted by this contract:** None

Thomas exposes three architecturally distinct functions. They are not prompt personas and cannot borrow one another's future authority implicitly.

| Mode | Primary function | Governing question | Default response |
|---|---|---|---|
| Therapist | Intervention | What, if anything, would be helpful to do next? | Requires future governed procedural selection. |
| Biographer | Investigation | What is worth learning next? | Requires future governed procedural selection. |
| Journal | Capture | What should be preserved from this entry? | `NO_RESPONSE` |

All three contracts carry `PRODUCTION_RUNTIME_NOT_GRANTED`, `NONE` direct profile-mutation authority, and `NONE` language-model decision authority.

## Therapist

Therapist's responsibility is governed therapeutic-action selection, subject to independent safety authority. CT-V2-03 implements one qualification-only bounded-problem ruleset; it is not wired to production runtime and grants no production authority. A language model may eventually render an already selected act; it cannot choose it.

CT-V2-04 makes the independent safety boundary compile-visible: the ordinary Therapist evaluator requires a state- and evidence-revision-bound `OrdinaryTherapyPermit`. Journal and Biographer cannot receive that capability through their mode contracts. The permit remains qualification-only and grants no production runtime authority.

CT-V2-05 adds qualification-only listening/support, understanding/clarification, bounded practical problem solving, and consolidate/close routes. User preference selects among them, correction withdraws tentative interpretation, and unchanged evidence cannot drive indefinite repetition. None of these routes is wired to runtime, and none changes Biographer or Journal authority.

CT-V2-06 defines one shared longitudinal evidence vocabulary to which future mode-specific acquisition paths may contribute. It implements no mode writer or reader. Journal remains capture with default `NO_RESPONSE`; Biographer remains investigation; Therapist receives no longitudinal retrieval authority.

## Biographer

Biographer's future responsibility is governed information-gap selection. Its intended conceptual loop is:

```text
OBSERVE → IDENTIFY GAP → VALUE GAP → SELECT QUESTION → ASK
        → RECORD EVIDENCE → REASSESS
```

The loop is documentation only. No gap valuation, question selection, or profile inference is implemented. Biographer output can only be conceived as evidence submitted for governed admission. It cannot promote a Thomas hypothesis, resolve a contradiction, or write a durable fact directly.

## Journal

Journal's primary function is capture, not conversation. Its default contract is:

```text
USER ENTRY → PRESERVE → CATALOGUE → EXTRACT GOVERNED EVIDENCE
           → NO GENERATED RESPONSE
```

The preservation steps describe future responsibility; production persistence is not implemented. `dialogue.no-response` is a first-class ontology concept and Journal's explicit default.

Future response overlays such as acknowledge, reflect, or ask may be designed only through separately authorized policy and user controls. They do not alter Journal's core capture function.

## Shared boundaries

- Silence is a valid Thomas action.
- A mode can submit evidence but cannot directly rewrite the durable psychological profile.
- A source binding provides provenance, not behavior authority.
- A renderer receives only a future authorized command and authorized supporting material.
- A language model cannot decide intervention, risk, escalation, question selection, profile admission, Journal response, or procedural path.
- A mode contract does not itself authorize a therapeutic rule. CT-V2-03 rule execution comes only from its separately tracked Principal scope and remains qualification-only.
