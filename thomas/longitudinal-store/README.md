# Longitudinal store

Qualification-only SQLite ledger and projection adapter for CT-V2-07. Its single mutation surface is the governed `LongitudinalAdmissionController`; SQL, transactions, mutable projections, accepted receipt implementations, and persisted payloads do not escape this module. The adapter accepts only synthetic fixtures in Gradle build or operating-system temporary locations.

No Android, Room, app, runtime, model, network, safety-state, production persistence, encryption, backup, export, deletion-lifecycle, or retrieval authority is present.
