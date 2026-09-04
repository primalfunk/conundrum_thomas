# CT-V2-12 Therapy Source Provenance

The qualification composition root maps one committed Therapy user turn to one stable CT-V2-07 source identity. Revision one is admitted as:

- acquisition mode: `THERAPIST_CONVERSATION`;
- author: `USER`;
- capture origin: `TYPED` or `SPEECH_TRANSCRIPT`;
- exact committed text: inline canonical source content;
- report time: supplied user-turn report time;
- record time: assigned by the governed store clock;
- event time: unknown at source level unless separately governed; extractive CT-V2-08 assertions retain any supported event-time form;
- privacy: explicit `ELIGIBLE` or `PRIVATE`;
- metadata: session ID, turn ID, capture origin, and versioned capture contract.

The qualification adapter owns no persistence. It creates a typed `AdmitSource` request and delegates it to CT-V2-07. CT-V2-08 runs only after accepted source admission. The stable source and revision IDs derive from the Therapy session/turn identity; no second Therapy database or parallel source history exists.

Private current text is available to the current render-support envelope because it is immediate conversation. Its source is marked private, CT-V2-08 ordinary derivation is skipped, and future ordinary retrieval excludes it. Sensitive content does not infer privacy.

Capture receipts contain IDs, provenance, admission/language dispositions, evidence IDs, revisions, record time, and a canonical fingerprint. They do not log or duplicate the complete source body. Assistant plans, rendered Thomas text, memory references, route decisions, and session-memory history never enter this source path. Thomas cannot cite Thomas as user evidence.
