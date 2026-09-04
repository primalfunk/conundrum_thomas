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

:thomas:longitudinal-admission
  -> :thomas:longitudinal

:thomas:personal-data-persistence
  -> :thomas:longitudinal-admission

:thomas:longitudinal-store (synthetic qualification only)
  -> :thomas:personal-data-persistence

:thomas:language-evidence (synthetic qualification authority only)
  -> :thomas:longitudinal-admission

:thomas:journal (synthetic qualification authority only)
  -> :thomas:language-evidence

:thomas:biographer (synthetic qualification authority only)
  -> :thomas:language-evidence

:thomas:retrieval (synthetic qualification read authority only)
  -> :thomas:longitudinal

:thomas:context-packet (synthetic qualification only)
  -> :thomas:retrieval

:thomas:therapy-longitudinal (synthetic qualification only)
  -> :thomas:engine
  -> :thomas:safety
  -> :thomas:context-packet
  -> :thomas:retrieval
  -> :thomas:language-evidence
  -> :thomas:longitudinal-admission
  -> :thomas:longitudinal

:thomas:language-renderer (synthetic qualification only)
  -> :thomas:domain
  -> :thomas:journal
  -> :thomas:biographer
  -> :thomas:therapy-longitudinal
  -> :thomas:longitudinal

:qualification
  -> :thomas:longitudinal-store
  -> :thomas:personal-data-persistence
  -> :thomas:language-evidence
  -> :thomas:journal
  -> :thomas:biographer
  -> :thomas:context-packet
  -> :thomas:therapy-longitudinal
  -> :thomas:language-renderer

:platform:persistence-android
  -> :thomas:personal-data-persistence

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
- `:thomas:longitudinal` is platform independent and dependency-free. It defines immutable personal-source, assertion, life-structure, temporal, relationship, identity, coverage, and validation contracts. It does not reuse clinical-source provenance, depend on therapeutic policy, expose a writer/store, or persist personal data.
- `:thomas:longitudinal-admission` depends only on the longitudinal domain. It defines typed requests, deterministic policy, lifecycle/privacy/deletion consequences, canonical request/state encoding, and mutation plans. It cannot persist, create an accepted receipt, or assign record time.
- `:thomas:personal-data-persistence` depends only on admission and its transitive domain. It owns shared governed read/write ports plus the CT-V2-14 authenticated store, deletion checkpoints, export, protected backup/restore, reset, schema migration, integrity verification, and rebuild behavior. It has no Android, UI, renderer, model, retrieval, Therapy, Journal, or Biographer dependency.
- `:thomas:longitudinal-store` consumes the shared ports through personal-data-persistence and owns only the JVM SQLite qualification adapter, injected store clock, append-only ledger, current projection, schema, transactions, replay, and redacted audit. Only `:qualification` consumes the complete SQLite adapter.
- `:thomas:language-evidence` is pure Kotlin and contains bounded deterministic perception and persistence-neutral state formation. It can propose grounded structures but cannot persist them. Only `:qualification` composes it with the CT-V2-07 store, and every durable object passes through the admission controller.
- `:thomas:journal` is pure Kotlin and depends only on `:thomas:language-evidence` and its transitive governed contracts. It owns typed commit/revision/privacy commands and bounded response-intent policy, but no persistence implementation. Its sole adapter and complete write composition live in `:qualification` and delegate every durable operation to CT-V2-07.
- `:thomas:biographer` is pure Kotlin and depends only on `:thomas:language-evidence` and its transitive governed contracts. It owns structural coverage, deterministic target ranking, one-question plans, operational investigation history, and source-first answer orchestration. Only `:qualification` composes it with the store; Therapy, Journal, Android, runtime, models, and V1 do not consume it.
- `:thomas:retrieval` is pure Kotlin and depends only on `:thomas:longitudinal`. It owns the persistence-neutral read port, typed purpose/mode requests, eligibility, deterministic ranking, balanced evidence neighborhoods, exact-span excerpt proposals, and no writer.
- `:thomas:context-packet` is pure Kotlin and depends only on retrieval. It owns immutable packet layers, source-data authority labels, explicit count/text/depth budgets, epistemically safe excerpt truncation, and canonical packet digests. Only `:qualification` binds it to the CT-V2-07 reader.
- `:thomas:therapy-longitudinal` is a pure Kotlin CT-V2-12 composition module. It preserves current-source capture before safety, CT-V2-04 safety before CT-V2-05 policy, and route selection before CT-V2-11 retrieval. It receives only typed ports, exposes no SQL/JDBC or store implementation, limits ordinary surfaced history to one object, and gives a future renderer only explicitly authorized memory support. It is consumed only by `:qualification`.
- `:thomas:language-renderer` is a pure Kotlin CT-V2-13 realization boundary. It consumes only already-governed mode contracts and CT-V2-12 render-visible support, exposes no store/retrieval/write/model/Android port, treats external output as a candidate, enforces deterministic budgets and authority markers, and provides a validated reference/fallback realization. It is consumed only by `:qualification`.
- Retrieval does not depend on Journal, Biographer, Therapy, safety, Android, models, or the store. It cannot alter a mode contract, route, target, evidence, lifecycle, privacy, or projection.
- CT-V2-03 explicitly authorizes `:thomas:engine` to consume the ontology for a qualification-only ruleset. Ontology remains behavior-free and has no dependency back to engine.
- `:thomas:engine` has no Android, Room, JNI, speech, concrete-model, or qualification dependency. Its policies accept typed structured state, never raw user prose. CT-V2-05 adds hierarchical ordinary-route, action, and progression rules while preserving qualification execution authority and explicit production denial. Every ordinary evaluator requires a matching safety-issued permit.
- Raw prose is not a policy input. Engine, safety, runtime, and app do not depend on `:thomas:language-evidence`; CT-V2-08 therefore grants no route, goal, action, or safety authority to language perception.
- Journal raw prose is admitted as source evidence before perception. Response posture is not evidence; Journal never depends on engine, safety, Therapy, Biographer, retrieval, renderer implementations, Android, or app/runtime code.
- `:thomas:safety` does not depend on `:thomas:engine`. It consumes typed ontology/provenance contracts, and only it can normally construct an `OrdinaryTherapyPermit`. This preserves an acyclic graph while preventing ordinary policy from bypassing the gate.
- `:thomas:runtime` is the only production module allowed to depend on both engine and safety and is the sole future owner of ordered end-to-end orchestration.
- `:platform:persistence-android` is the single production-capable CT-V2-14 composition root. It supplies AndroidKeyStore and no-backup atomic ciphertext storage, but remains unreachable from `:app` and `:thomas:runtime` until a later authorized integration phase.
- `:app` does not depend on engine, safety, persistence, speech, or a renderer implementation. It cannot directly invoke a model.
- A renderer receives only a `RenderRequest`: one `RenderCommand` and explicitly authorized supporting text. Its output is an untrusted `RenderedDraft`.
- CT-V2-13 narrows that contract further through `GovernedRenderCommand`, typed semantic units, explicit surfaced-memory IDs, `RendererInput`, and `GovernedRenderResult`; no candidate becomes user-visible without validation.
- A `RenderCommand` fixes the selected policy action, dialogue act, goal, semantic boundaries, output form, length, question count, advice permission, tentative-status constraint, user-agency constraint, and response requirement. Deterministic stubs live downstream in `:qualification` and are not reachable from app or runtime.
- `:platform:renderer-llama-android` has no dependency on persistence, policy, safety, transcript, profile, or provenance implementations.
- Platform adapters implement technical capabilities. They do not acquire therapeutic decision authority.
- `:tools:provenance` owns the SQLite driver and source-to-database build. No production or Android module depends on it; `:thomas:provenance` remains platform independent.
- `:tools:provenance` uses ontology only in test scope to prove that bindings resolve to exact governed source and pending-review records.

The adapter modules remain unwired from the application. Future product composition must preserve these rules and requires separate authority.
