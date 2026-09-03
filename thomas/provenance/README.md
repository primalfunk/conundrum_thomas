# `:thomas:provenance`

Platform-independent source-governance vocabulary. This module contains only immutable metadata types shared by future consumers. It has no Android, database-driver, raw-document, therapeutic-runtime, or clinical-decision dependency.

The reviewable relational source of truth lives under `provenance/`. The build-only `:tools:provenance` module applies its migrations and seeds to an ignored generated SQLite database. No record in that database grants therapeutic authority.
