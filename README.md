# Conundrum Thomas V2

**A deterministic, evidence-governed architecture for a local-first
conversational system.**

Conundrum Thomas V2 explores a deliberately narrow thesis:

> **Thomas decides what to say. The renderer decides how to say it.**

Mode, safety, therapeutic behavior, memory selection, epistemic status, and
response limits belong to typed, inspectable policy. A language model—if one is
admitted in a future phase—may only realize an already-authorized meaning and
cannot write evidence, search memory, choose a route, or bypass validation.

> [!IMPORTANT]
> The Android application now composes one governed V2 runtime for Journal,
> Biographer, and Therapy. It uses the protected local store, deterministic
> retrieval and rendering, and explicit export/backup/restore/reset custody
> controls. Qualification uses synthetic data only. No language model, remote
> inference service, speech recognizer, or speech synthesizer has been admitted.

> [!WARNING]
> This project is not a medical device, clinical service, crisis service, or
> production therapy system. Do not use the current repository for clinical
> care or with real personal or psychological data.

## Why this architecture exists

A fluent response is not necessarily a justified response. Thomas separates
behavioral authority from language generation so important decisions remain
reviewable and testable:

- procedure precedes generation;
- safety is an independent authority;
- user-authored sources remain primary evidence;
- interpretations remain distinct from facts;
- private, corrected, contradicted, and uncertain material stays visibly so;
- memory is retrieved for a declared purpose and cannot choose policy;
- assistant output cannot become evidence about the user;
- invalid realization falls back without changing the governed decision.

The result is intentionally less autonomous than a conventional chatbot and
more explicit about what each component is allowed to know, decide, and write.

## Three distinct modes

| Mode | Purpose | Governing boundary |
| --- | --- | --- |
| **Journal** | Capture life as the user records it | Source-first; silence is the default; optional reflection is bounded |
| **Biographer** | Investigate structurally incomplete history | One deterministic target and at most one non-leading question |
| **Therapy** | Address a present concern through governed policy | Current safety first, deterministic route second, historical memory last |

These names describe product modes and authority boundaries. They do not assert
diagnosis, clinical validation, or professional care.

## Governed production turn flow

The Android application follows one authoritative path. The application and
renderer sequence the qualified owners; they do not replace them:

```mermaid
flowchart LR
    U["Committed typed user turn"] --> A["Source-first governed admission"]
    U --> S["Mode policy / safety and scope gate"]
    S --> P["Authorized semantic action"]
    P --> R["Purpose-bound retrieval when authorized"]
    H["Governed longitudinal history"] --> R
    A -. "eligible on future turns" .-> H
    R --> M["Conservative memory-use gate"]
    M --> C["Governed render command"]
    C --> L["Untrusted language realizer"]
    L --> V["Deterministic validator"]
    V --> O["Final assistant artifact or true silence"]
```

The order is architectural: historical memory cannot alter safety, route, or
technique selection; the current turn cannot retrieve itself as history; and
no renderer candidate is user-visible before deterministic validation.

## Current implementation status

| Capability | Current state |
| --- | --- |
| Mode, provenance, ontology, safety, and ordinary Therapy policy | Pure Kotlin, deterministic, production-composed |
| Longitudinal evidence, revisions, corrections, privacy, and identity | Typed domain with governed protected Android persistence |
| Language-to-evidence formation | Conservative, source-grounded, deterministic production path |
| Journal capture | Production-composed source-first engine; default `NO_RESPONSE` |
| Biographer coverage | Production-composed deterministic coverage map and one-question authority |
| Retrieval and context packets | Read-only, purpose-bound, correction-aware, budgeted |
| Longitudinal Therapy composition | Production route-first integration with at most one ordinary memory reference |
| Language rendering | Production deterministic reference realizer behind typed commands, validation, and fallback |
| Personal-data lifecycle | AES-GCM protected atomic store, AndroidKeyStore key, deletion, correction, export, protected backup, restore, reset, migration, and recovery |
| Android application | One Compose runtime with explicit Journal, Biographer, Therapy, privacy, and data-custody surfaces |
| Speech input/output | **Not admitted; typed input is authoritative** |
| Production language model | **Not admitted** |

CT-V2-15 qualification covers the complete JVM regression suite, Android
instrumentation, APK hygiene, and a physical-device persistence/reboot gate.
See the [qualification record](docs/qualification/CT-V2-15-QUALIFICATION.md)
for exact evidence and limitations. The repository remains a research and
qualification system, not a clinical claim or a model-admission claim.

## Quick start

### Prerequisites

The recorded qualification environment uses:

- Git;
- JDK 25 for the Gradle daemon;
- Android SDK Platform 37;
- the checked-in Gradle 9.5 wrapper.

The project emits Java/Kotlin 11-compatible bytecode. Android Studio can manage
the SDK path through the ignored `local.properties` file. The first build
requires network access to resolve declared dependencies.

### Clone and verify

```bash
git clone https://github.com/primalfunk/conundrum_thomas.git
cd conundrum_thomas
./gradlew clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
```

From PowerShell on Windows:

```powershell
git clone https://github.com/primalfunk/conundrum_thomas.git
Set-Location conundrum_thomas
.\gradlew.bat clean build lint generateRuntimeProvenanceDb --rerun-tasks --console=plain
```

The provenance task builds an ignored, synthetic SQLite database from tracked
migrations and seeds. It is build and qualification infrastructure, not a
production user-data store.

To verify that Android Studio and command-line Git share the canonical project
root on Windows:

```powershell
.\tools\verify-canonical-git-root.ps1
```

## Repository map

| Path | Responsibility |
| --- | --- |
| [`app/`](app/) | Canonical Android/Compose V2 application and composition root |
| [`thomas/domain/`](thomas/domain/) | Shared mode and rendering authority contracts |
| [`thomas/engine/`](thomas/engine/) | Deterministic ordinary Therapy policy |
| [`thomas/safety/`](thomas/safety/) | Independent safety and scope permit |
| [`thomas/longitudinal*/`](thomas/) | Evidence domain, admission, and synthetic qualification store |
| [`thomas/journal/`](thomas/journal/) | Journal commit and bounded-response contracts |
| [`thomas/biographer/`](thomas/biographer/) | Coverage map, target selection, and question authority |
| [`thomas/retrieval/`](thomas/retrieval/) | Read-only deterministic historical retrieval |
| [`thomas/context-packet/`](thomas/context-packet/) | Bounded, typed, purpose-specific context |
| [`thomas/therapy-longitudinal/`](thomas/therapy-longitudinal/) | Route-first Therapy and memory composition |
| [`thomas/language-renderer/`](thomas/language-renderer/) | Untrusted realization boundary, validation, and fallback |
| [`thomas/personal-data-persistence/`](thomas/personal-data-persistence/) | Governed protected store, lifecycle, export, backup, restore, and recovery |
| [`platform/`](platform/) | Android persistence adapter plus deliberately unadmitted renderer/speech boundaries |
| [`qualification/`](qualification/) | Synthetic end-to-end and adversarial qualification |
| [`provenance/`](provenance/) | Governed source schema, migrations, and synthetic seeds |
| [`docs/`](docs/) | Architecture, ADRs, policies, risks, phase records, and qualification evidence |

The compile-enforced dependency graph and authority rules are documented in
[`docs/MODULE-BOUNDARIES.md`](docs/MODULE-BOUNDARIES.md).

## Start with these documents

- [Architecture](docs/CONUNDRUM-THOMAS-V2-ARCHITECTURE.md)
- [Mode authority contract](docs/THOMAS-MODE-AUTHORITY-CONTRACT.md)
- [Module boundaries](docs/MODULE-BOUNDARIES.md)
- [Architectural decision records](docs/adr/README.md)
- [Risk register](docs/RISK-REGISTER.md)
- [Latest Android integration](docs/CT-V2-15-ANDROID-PRODUCTION-INTEGRATION.md)
- [Production runtime architecture](docs/architecture/CT-V2-15-ANDROID-PRODUCTION-RUNTIME.md)
- [Personal-data threat model](docs/security/CT-V2-14-PERSONAL-DATA-THREAT-MODEL.md)
- [Latest qualification evidence](docs/qualification/CT-V2-15-QUALIFICATION.md)
- [Forward development plan](docs/planning/CONUNDRUM-THOMAS-V2-FORWARD-DEVELOPMENT-PLAN.md)
- [V1 migration register](migration/v1-component-register.json)

V1 code, binaries, models, and repositories are not dependencies. Every one of
the 24 inventoried V1 components remains denied unless separately admitted
through the governed migration register.

## Privacy and repository hygiene

This public repository must remain synthetic-only:

- never commit real Journal entries, Biographer answers, Therapy conversations,
  profile data, recordings, source excerpts, credentials, signing keys, model
  artifacts, generated databases, context-packet dumps, or rendered-response
  logs;
- do not place secrets in tracked Gradle configuration;
- keep private or rights-restricted source material outside Git;
- treat assistant responses and renderer history as operational artifacts, not
  evidence;
- preserve `android:allowBackup="false"`; Thomas backup is explicit,
  user-directed, and independently protected rather than generic platform backup.

The root [`.gitignore`](.gitignore) encodes these boundaries. Safe example
configuration may be tracked; populated local configuration may not.

## Working on the project

Before changing a governed boundary:

1. Read the relevant authority contract and ADR.
2. Preserve compile-time dependency direction.
3. Add deterministic qualification for permitted and prohibited behavior.
4. Use synthetic data only.
5. Run the complete qualification command.
6. Record limitations without converting qualification into a production,
   clinical, privacy, or security claim.

A sophisticated output is not evidence that an authority boundary is correct.
In this repository, behavior is accepted because its provenance, policy,
failure handling, and tests are inspectable.

## License and attribution

Copyright 2026 primalfunk.

Conundrum Thomas V2 is licensed under the
[Apache License, Version 2.0](LICENSE). Distributed derivative works must
preserve the attribution notices in [`NOTICE`](NOTICE) as required by the
license.

**Attribution:** Conundrum Thomas V2 by
[`primalfunk`](https://github.com/primalfunk).
