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

:platform:persistence-android
  -> :thomas:domain

:platform:renderer-llama-android
  -> :thomas:domain

:platform:speech-android
  -> :thomas:domain

:tools:provenance (build/qualification only)
  -> :thomas:provenance
```

## Authority rules

- `:thomas:domain` is platform independent.
- `:thomas:engine` has no Android, Room, JNI, speech, or concrete-model dependency. Its policy contract accepts `StructuredPolicyState`, never raw user prose.
- `:thomas:safety` does not depend on `:thomas:engine`. Its result governs an ordinary action proposal; ordinary policy cannot override it.
- `:thomas:runtime` is the only production module allowed to depend on both engine and safety and is the sole future owner of ordered end-to-end orchestration.
- `:app` does not depend on engine, safety, persistence, speech, or a renderer implementation. It cannot directly invoke a model.
- A renderer receives only a `RenderRequest`: one `RenderCommand` and explicitly authorized supporting text. Its output is an untrusted `RenderedDraft`.
- `:platform:renderer-llama-android` has no dependency on persistence, policy, safety, transcript, profile, or provenance implementations.
- Platform adapters implement technical capabilities. They do not acquire therapeutic decision authority.
- `:tools:provenance` owns the SQLite driver and source-to-database build. No production or Android module depends on it; `:thomas:provenance` remains platform independent.

The adapter modules remain unwired. Future composition must preserve these rules; a platform composition root may be introduced only when actual adapters are authorized.
