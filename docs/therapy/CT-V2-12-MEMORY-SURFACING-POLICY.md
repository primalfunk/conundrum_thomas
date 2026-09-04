# CT-V2-12 Memory Surfacing Policy

## Retrieved is not surfaced

CT-V2-11 may provide planner-visible eligible context. CT-V2-12 separately decides what may be visible to the future renderer. Packet availability never obligates a memory reference.

For `ORDINARY`, automatic surfacing requires one of:

- same resolved entity;
- same event;
- same relationship;
- a CT-V2-08 qualified recurrence candidate sharing the predicate; or
- an explicit user target.

Lexical overlap, temporal proximity, shared emotion, a generic topic, intensity, and unresolved identity are insufficient. An ordinary plan may surface at most one historical object. Hypotheses and contradiction objects are never automatically surfaced as ordinary truth. Known contradictory or contested context is omitted rather than made one-sided.

`EXPLICIT_RECALL` authorizes up to four CT-V2-11-selected eligible non-hypothesis objects. `EXPLAIN_THOMAS_VIEW` authorizes up to four balanced explanation items, including explicit lifecycle status, supporting evidence, counterevidence, correction state, and uncertainty. Neither path strengthens or writes the object.

## Session behavior

The session state records object ID, surfacing turn, relation, reason, packet meaning token, explicit reinvocation, and connection rejection. It is ephemeral operational state, not user evidence.

An ordinary memory is surfaced at most once per session unless the user explicitly invokes it again or materially changed evidence changes its meaning. Direct continuation is immediate conversation, not renewed historical recall. A rejected tentative connection is suppressed for later ordinary turns; explicit recall can reopen it without rewriting the underlying evidence.

## Renderer semantics

Each authorized reference identifies direct recall, tentative connection, user-requested comparison, or evidence explanation. It carries provenance, report and event time, epistemic role, uncertainty, lifecycle, identity and contradiction flags, exact eligible excerpt when selected, and a complete prohibited-overclaim set. The renderer is never asked to invent epistemic status.
