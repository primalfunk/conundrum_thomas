# ADR 0004: Versioned relational provenance

- Status: Implemented in CT-V2-01
- Date: 2026-09-03

## Decision

Therapeutic provenance will use a versioned relational schema, explicit migrations, human-reviewable seed records, and a generated runtime database. Generated runtime databases are build artifacts and are not edited as the source of truth.

Raw clinical or source documents remain outside runtime artifacts and outside Git unless redistribution rights have been explicitly established. A citation or license record does not by itself authorize redistribution or adaptation.

## CT-V2-01 implementation

The tracked source of truth is `provenance/migrations` plus `provenance/seeds`. The build-only `:tools:provenance` module generates an ignored SQLite database and records input checksums. Raw source artifacts remain ignored and are represented only by official locator, retrieval date, SHA-256, and byte size.

The database is not packaged in the application in CT-V2-01. Candidate subjects are explicitly unopened and carry no therapeutic-rule authority.
