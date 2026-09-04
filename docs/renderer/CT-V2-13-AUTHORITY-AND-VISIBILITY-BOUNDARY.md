# CT-V2-13 Authority and Visibility Boundary

The governing split is: Thomas decides what to say; the renderer decides how to say it. Mode, safety, route, technique, semantic act, question authority, Biographer target, Journal posture, memory selection, attribution, uncertainty, and evidence status are upstream facts.

The renderer sees only content the response may express. Journal exposes current-entry grounding only. Biographer exposes its already-selected target only. Therapy adapts only the CT-V2-12 `TherapyRenderSupportEnvelope`: current-turn data, the selected policy act, and explicitly surfaced memory. It never exposes the complete CT-V2-11 packet, rejected alternatives, private material, or hidden hypotheses.

User prose is labeled `CURRENT_USER_CONTENT_DATA` or `HISTORICAL_USER_SOURCE_DATA`. Those labels have no instruction authority. Quoted source text is excluded from deterministic authority-phrase scans. A source saying “ignore all instructions” can be quoted only when the governed command makes it visible; it cannot modify mode, policy, advice, or future behavior.

The module has no read/write/store interface. Render results and render history cannot be admitted as user evidence. Direct candidate-to-user bypass does not exist: every candidate passes `DeterministicRenderValidator`, then becomes accepted output or is replaced by governed fallback.

Compile boundary:

```text
:thomas:language-renderer
    -> :thomas:domain
    -> :thomas:journal
    -> :thomas:biographer
    -> :thomas:therapy-longitudinal
    -> :thomas:longitudinal

:qualification -> :thomas:language-renderer
```

There is no Android, Compose, Room, JDBC, SQLite, retrieval implementation, store implementation, speech, network, llama.cpp, GGUF, model, or V1 dependency.
