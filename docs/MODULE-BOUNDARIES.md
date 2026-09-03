# CT-V2 module authority boundaries

These are production compile dependencies. `:qualification` may depend on each layer in test scope so that layers can be qualified independently.

```text
:app
  -> :thomas:runtime

:thomas:runtime
  -> :thomas:domain
  -> :thomas:provenance
  -> :thomas:engine
  -> :thomas:safety

:thomas:engine
  -> :thomas:domain
  -> :thomas:provenance
  -> :thomas:ontology
  -> :thomas:safety

:thomas:safety
  -> :thomas:domain
  -> :thomas:provenance
  -> :thomas:ontology

:thomas:provenance
  -> :thomas:domain

:thomas:ontology
  -> :thomas:domain
  -> :thomas:provenance

:thomas:longitudinal
  -> (no project dependencies)

:platform:persistence-android
  -> :thomas:domain

:platform:renderer-llama-android
  -> :thomas:domain

:platform:speech-android
  -> :thomas:domain

:tools:provenance (build/qualification only; ontology is test scope)
  -> :thomas:provenance
  -test-> :thomas:ontology
```

## Authority rules

- `:thomas:domain` is platform independent.
- `:thomas:domain` owns the three mode-authority contracts because Therapist, Biographer, and Journal identity precedes therapeutic policy. All three deny model decision authority and direct profile mutation; Journal defaults to `NO_RESPONSE`.
- `:thomas:ontology` is platform independent and depends only on domain and provenance contracts. It defines typed vocabulary and source bindings, never decisions.
- `:thomas:longitudinal` is platform independent and dependency-free. It defines immutable personal-source, assertion, life-structure, temporal, relationship, identity, coverage, and validation contracts. It does not reuse clinical-source provenance, depend on therapeutic policy, expose a writer/store, or persist personal data. CT-V2-06 wires it only to `:qualification`.
- CT-V2-03 explicitly authorizes `:thomas:engine` to consume the ontology for a qualification-only ruleset. Ontology remains behavior-free and has no dependency back to engine.
- `:thomas:engine` has no Android, Room, JNI, speech, concrete-model, or qualification dependency. Its policies accept typed structured state, never raw user prose. CT-V2-05 adds hierarchical ordinary-route, action, and progression rules while preserving qualification execution authority and explicit production denial. Every ordinary evaluator requires a matching safety-issued permit.
- `:thomas:safety` does not depend on `:thomas:engine`. It consumes typed ontology/provenance contracts, and only it can normally construct an `OrdinaryTherapyPermit`. This preserves an acyclic graph while preventing ordinary policy from bypassing the gate.
- `:thomas:runtime` is the only production module allowed to depend on both engine and safety and is the sole future owner of ordered end-to-end orchestration.
- No production module consumes `:thomas:longitudinal` in CT-V2-06. Future Journal, Biographer, retrieval, or Therapist integration requires separately authorized boundaries.
- `:app` does not depend on engine, safety, persistence, speech, or a renderer implementation. It cannot directly invoke a model.
- A renderer receives only a `RenderRequest`: one `RenderCommand` and explicitly authorized supporting text. Its output is an untrusted `RenderedDraft`.
- A `RenderCommand` fixes the selected policy action, dialogue act, goal, semantic boundaries, output form, length, question count, advice permission, tentative-status constraint, user-agency constraint, and response requirement. Deterministic stubs live downstream in `:qualification` and are not reachable from app or runtime.
- `:platform:renderer-llama-android` has no dependency on persistence, policy, safety, transcript, profile, or provenance implementations.
- Platform adapters implement technical capabilities. They do not acquire therapeutic decision authority.
- `:tools:provenance` owns the SQLite driver and source-to-database build. No production or Android module depends on it; `:thomas:provenance` remains platform independent.
- `:tools:provenance` uses ontology only in test scope to prove that bindings resolve to exact governed source and pending-review records.

The adapter modules remain unwired. Future composition must preserve these rules; a platform composition root may be introduced only when actual adapters are authorized.
