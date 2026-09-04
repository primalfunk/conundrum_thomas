# CT-V2-13 Future Model Admission Contract

No model is admitted by CT-V2-13. A future implementation may only implement the narrow `LanguageRealizer` port and must treat its output as a candidate.

Before production consideration, an adapter must prove:

- exact adapter and model/version identity;
- required offline/local status and separately authorized network posture;
- bounded typed input and bounded output;
- no tools, database, retrieval, longitudinal read/write, policy, safety, or mode authority;
- semantic-act and epistemic-fidelity corpus passes;
- question, sentence, character, temporal, provenance, and memory-attribution passes;
- adversarial current/historical injection resistance;
- linguistic anti-repetition behavior;
- deterministic fallback on unavailable, exception, timeout, malformed, and rejected output;
- device latency, memory, storage, power, and resource qualification;
- separately authorized production privacy/security and Android integration.

A future adapter may internally serialize `RendererInput` as an instruction equivalent to “express this authorized meaning naturally.” It must not receive a task equivalent to “read the conversation and decide how to help.” Typed `GovernedRenderCommand` remains authoritative; adapter serialization is subordinate implementation detail.
