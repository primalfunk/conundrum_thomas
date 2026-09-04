# CT-V2-10 Question Authority

`BiographerQuestionPlan` is a renderer contract, not prose and not evidence. It contains posture, target ID/kind, grounding IDs, structured safe facts, uncertainty constraints, semantic act, exactly-one maximum question count, prohibited acts, reason code, and question-rule version.

Semantic acts are open historical invitation, structural-gap exploration, identity clarification, contradiction clarification, correction-target clarification, and user-topic exploration.

Every plan forbids diagnosis, therapeutic technique, psychological causal assertion, trauma presupposition, unsupported emotion/date, identity merge, multiple questions, and coverage pressure. Approximate bounds add `DO_NOT_INCREASE_TEMPORAL_PRECISION`.

A renderer may render only the supplied plan. It cannot discover a target, alter eligibility, add facts, choose follow-up authority, or persist anything. More than one question fails deterministically. Rendering failure leaves the store unchanged.

The question text is never admitted as user evidence. Only a separately committed answer can become a source, and every durable answer crosses CT-V2-07.
