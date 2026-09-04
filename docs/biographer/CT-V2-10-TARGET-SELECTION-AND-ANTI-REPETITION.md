# CT-V2-10 Target Selection and Anti-Repetition

## Deterministic hierarchy

The `ct-v2-10.target-ranking.v1` hierarchy is:

1. current explicit user target;
2. actionable correction-target ambiguity;
3. actionable identity ambiguity;
4. existing structural open evidentiary question;
5. unresolved contradiction;
6. unresolved event time;
7. sparse temporal gap;
8. partial period;
9. role gap;
10. place gap;
11. relationship context;
12. event detail.

Stable target ID is the deterministic tie-break. Private, declined, covered, unanswerable, deferred, and unchanged recently asked candidates do not enter the ranking set.

`OPEN_STORY` is explicitly selected as a posture. `NO_TARGET` is returned when targeted coverage has no useful eligible candidate; the engine does not manufacture a question merely to continue.

## Operational history

`BiographerInvestigationHistory` records target first/last offered revision, offer count, last answer disposition, last substantive answer source ID, and material-change token. It is operational state, not evidence about the user.

## Progression rule

After `OFFERED`, `SKIPPED`, `DEFERRED`, `ANSWERED_AMBIGUOUS`, `NO_EXTRACTABLE_EVIDENCE`, `CHANGED_TOPIC`, or `STOPPED`, unchanged evidence makes the target ineligible for immediate automatic reselection. `ANSWERED_RELEVANT` becomes covered for the unchanged token. `DECLINED` and `MARKED_PRIVATE` are hard exclusions.

Revisiting is permitted only when the user explicitly reopens/selects the target or governing target evidence produces a different material-change token. This is deterministic progression. Random wording or random target choice has no role.
