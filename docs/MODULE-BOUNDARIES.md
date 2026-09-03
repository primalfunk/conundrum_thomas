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

:thomas:safety
  -> :thomas:domain
  -> :thomas:provenance

:thomas:provenance
  -> :thomas:domain

:thomas:ontology
  -> :thomas:domain
  -> :thomas:provenance

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
- In CT-V2-02 no production consumer depends on `:thomas:ontology`. This compile-enforces that merely defining a concept cannot produce runtime behavior. CT-V2-03 may change a consumer edge only with explicit authorization.
- `:thomas:engine` has no Android, Room, JNI, speech, or concrete-model dependency. Its policy contract accepts `StructuredPolicyState`, never raw user prose.
- `:thomas:safety` does not depend on `:thomas:engine`. Its result governs an ordinary action proposal; ordinary policy cannot override it.
- `:thomas:runtime` is the only production module allowed to depend on both engine and safety and is the sole future owner of ordered end-to-end orchestration.
- `:app` does not depend on engine, safety, persistence, speech, or a renderer implementation. It cannot directly invoke a model.
- A renderer receives only a `RenderRequest`: one `RenderCommand` and explicitly authorized supporting text. Its output is an untrusted `RenderedDraft`.
- `:platform:renderer-llama-android` has no dependency on persistence, policy, safety, transcript, profile, or provenance implementations.
- Platform adapters implement technical capabilities. They do not acquire therapeutic decision authority.
- `:tools:provenance` owns the SQLite driver and source-to-database build. No production or Android module depends on it; `:thomas:provenance` remains platform independent.
- `:tools:provenance` uses ontology only in test scope to prove that bindings resolve to exact governed source and pending-review records.

The adapter modules remain unwired. Future composition must preserve these rules; a platform composition root may be introduced only when actual adapters are authorized.
