# CT-V2-12 Render Support Boundary

CT-V2-12 produces `TherapyRenderSupportEnvelope`, not final language and not a prompt string. The envelope contains only:

- the CT-V2-05 `RenderCommand` for the already-selected act;
- current turn ID and exact current user text needed for the immediate response;
- supporting text already authorized by CT-V2-05;
- the zero-to-one ordinary or bounded explicit memory references authorized by CT-V2-12;
- the CT-V2-12 envelope version.

The complete ContextPacket is deliberately discarded from renderer visibility. The envelope carries `completeContextPacketDisclosed = false`, and no store, reader, admission controller, retrieval port, private source, unsurfaced item, safety implementation, route evaluator, SQL/JDBC type, or model interface appears in the contract.

Historical excerpt text remains data. Every memory reference prohibits diagnosis, causal explanation, hidden motives, stable traits, identity merging, contradiction resolution, technique selection, safety inference, provenance rewrite, instruction authority, and evidence mutation. Historical instructions such as requests to diagnose, change modes, or select techniques therefore cannot alter the command.

CT-V2-13 may render this narrow envelope only after separate Principal authorization. CT-V2-12 implements no renderer, model call, prompt, GGUF, or Android integration.
