# Conundrum Thomas V2

This repository is the canonical architecture root for Conundrum Thomas V2.

> **THOMAS SELECTS AND GOVERNS THERAPEUTIC BEHAVIOR.**
>
> **THE MODEL ONLY RENDERS AUTHORIZED BEHAVIOR INTO LANGUAGE.**

V1 repositories, commits, binaries, and dirty worktrees are historical references, not dependencies. Migration is denied unless a component is individually admitted through the governed register in [`migration/v1-component-register.json`](migration/v1-component-register.json).

The governing plan is [`docs/CONUNDRUM-THOMAS-V2-ARCHITECTURE.md`](docs/CONUNDRUM-THOMAS-V2-ARCHITECTURE.md). CT-V2-00 established the sealed foundation. CT-V2-01 adds a governed source/provenance corpus under [`provenance/`](provenance/) and remains evidence-only: it implements no therapeutic rule, intervention, dialogue, safety algorithm, or model authority.

The initial inventory is documented in [`docs/provenance/INITIAL-SOURCE-INVENTORY.md`](docs/provenance/INITIAL-SOURCE-INVENTORY.md). Generate the ignored relational database with `gradlew generateRuntimeProvenanceDb`.
