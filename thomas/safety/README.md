# `:thomas:safety`

Independent deterministic safety and scope authority.

CT-V2-04 adds a qualification-only `SafetyScopeGate` that consumes typed evidence and may issue a revision-bound `OrdinaryTherapyPermit`. It contains no raw-language classifier, diagnosis, risk score, risk band, screening instrument, crisis script, or production-authorized pathway.

The module depends on `:thomas:domain`, `:thomas:provenance`, and `:thomas:ontology`. It does not depend on the ordinary engine, Android, persistence, JNI, speech, a renderer implementation, or a model. The ordinary engine depends on this module solely so its evaluator can require the permit.
