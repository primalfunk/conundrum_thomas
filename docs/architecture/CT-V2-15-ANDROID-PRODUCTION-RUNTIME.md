# CT-V2-15 Android production runtime

## Module graph

```text
:app
  -> :thomas:runtime
  -> :platform:persistence-android
  -> :platform:speech-android (explicit unavailable capability only)
  -> AndroidX / Compose

:thomas:runtime
  -> :thomas:domain, :thomas:provenance
  -> :thomas:engine, :thomas:safety
  -> :thomas:personal-data-persistence
  -> :thomas:language-evidence
  -> :thomas:journal, :thomas:biographer
  -> :thomas:retrieval, :thomas:context-packet
  -> :thomas:therapy-longitudinal
  -> :thomas:language-renderer

:platform:persistence-android
  -> :thomas:personal-data-persistence
```

`ThomasAndroidCompositionRoot` is the only production composition root. It
owns the platform store lifetime and one `ThomasProductionRuntime`. The runtime
owns sequencing and ephemeral session state, not policy. It accepts typed
`ProductionTurnRequest` values and returns typed `ProductionTurnResult` values.

## Store and key boundary

`AndroidPersonalDataPersistenceFactory` constructs:

- an `AtomicFile` protected artifact under `noBackupFilesDir`;
- an AES key held by AndroidKeyStore;
- the CT-V2-14 authenticated persistence implementation;
- governed CT-V2-07 admission and CT-V2-11 read ports.

Neither app UI nor runtime receives SQL, JDBC, DAO, raw key bytes, a mutable
projection, or a generic file handle. All source writes enter the shared
admission port. Correction appends a revision; privacy and deletion submit
governed lifecycle operations. Derived state is rebuilt from eligible ledger
state.

## Canonical turn ownership

The runtime uses one atomic processing guard and explicit monotonically
increasing client turn identity. Mode is fixed on each request. A ViewModel
mode change saves/restores its mode-specific draft and does not mutate the in-flight request. Only a
completed render result can create `ProductionAssistantArtifact`.

For Therapy the structural order is:

1. capture the current source against the pre-turn history revision;
2. evaluate current-turn safety and scope;
3. select the CT-V2-05 route;
4. retrieve CT-V2-11 context as of the pre-turn revision;
5. apply the CT-V2-12 memory gate;
6. render only the authorized support envelope.

History cannot affect steps 2 or 3, and the renderer cannot see unsurfaced
packet items.

## Failure behavior

Store/key/corruption failures make the root unavailable with a typed redacted
reason. Busy and blank turns are rejected without admission. Journal silence
is successful absence of output. Renderer failure never exposes a candidate;
the CT-V2-13 fallback governs the final result. A custody operation validates a
backup before clearing the existing corpus and restores only into an empty
protected target.

## CT-V2-15R1 current integration

The production observation/session and protected-corpus coverage bridges are described in [CT-V2-15R1 contracts](CT-V2-15R1-PRODUCTION-OBSERVATION-AND-COVERAGE.md). This supersedes the CT-V2-15 fixed procedural inputs and empty coverage supplier. Clinical policy authority remains unchanged.
