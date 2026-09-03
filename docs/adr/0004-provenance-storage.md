# ADR 0004: Versioned relational provenance

- Status: Accepted direction; CT-V2-01 implementation held
- Date: 2026-09-03

## Decision

Therapeutic provenance will use a versioned relational schema, explicit migrations, human-reviewable seed records, and a generated runtime database. Generated runtime databases are build artifacts and are not edited as the source of truth.

Raw clinical or source documents remain outside runtime artifacts and outside Git unless redistribution rights have been explicitly established. A citation or license record does not by itself authorize redistribution or adaptation.

## CT-V2-00 limit

This repository contains structural homes and this decision only. It contains no therapeutic source records, clinical propositions, runtime provenance schema, seed data, generated database, or raw source document. Those belong to CT-V2-01 after Principal authorization.
