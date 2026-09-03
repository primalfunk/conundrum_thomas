# ADR 0011: Core ordinary route and progression policy

**Status:** Accepted for CT-V2-05 qualification

## Context

The CT-V2-03 slice proved one bounded problem flow, while CT-V2-04 made a current safety permit mandatory. A broader ordinary repertoire must honor user direction and progress without hiding repetition behind different wording.

## Decision

Implement a hierarchical deterministic policy:

`permit -> route -> goal -> eligible actions -> selected action -> progression guard`

The opened routes are listening/support, understanding/clarification, practical problem solving, and consolidate/close. A fifth internal route clarifies an unknown user preference. Standalone decision support remains unopened.

Every evaluation requires a revision-matching `OrdinaryTherapyPermit`. Route selection and action selection use typed structured evidence only. Equivalent state and policy version produce equivalent results.

The policy records prior action and conversation-evidence revision. Repeating an action against unchanged evidence is denied unless one explicit, bounded repeat authorization exists. A first repeated substantive action is replaced with a bounded direction choice; repeated silence or continued stagnation ends with `NO_AUTHORIZED_ACTION`.

User correction withdraws the referenced tentative Thomas interpretation before a new interpretation may be considered. No policy result mutates a durable profile.

## Consequences

- Thomas can follow materially different qualification conversations with no LLM.
- Wording variation is not used as a substitute for progression.
- The rules remain clinically, commercially, and production unapproved.
- Choice inside the PM+ problem procedure is supported; a general decision-support pathway is not.
