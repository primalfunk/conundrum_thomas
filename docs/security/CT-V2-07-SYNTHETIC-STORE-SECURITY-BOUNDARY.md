# CT-V2-07 synthetic-store security boundary

## Classification

The CT-V2-07 relational adapter is structurally classified `SYNTHETIC_QUALIFICATION_ONLY`. It is designed to qualify admission, revision, persistence, transaction, and replay mechanics. It is not approved to store real psychological information.

## Enforced boundary

- Requests accept no production data classification.
- File locations must be under a Gradle `build` segment or the operating-system temporary directory and end in `.sqlite`.
- Fixtures are synthetic and cleanup removes their database files.
- Database files and build outputs are ignored by Git.
- No application, runtime, engine, safety, renderer, speech, Android persistence, or provenance-production module depends on the store.
- The app packages neither the store implementation nor a qualification database.
- No public API exposes JDBC, SQL, transactions, DAOs, mutable rows, ledger constructors, or concrete accepted-receipt constructors.
- There is no model, prompt, NLP extractor, network storage, telemetry, or V1 import path.
- Safety-state persistence is absent.

## Diagnostic minimization

Accepted and rejected audit surfaces use request IDs, idempotency keys, operation kinds, policy versions, stable reason codes, affected stable IDs, times, and SHA-256 payload fingerprints. Ordinary rejection records do not duplicate source content. Transaction failures expose exception classes rather than payload text.

This minimizes diagnostic duplication; it is not a claim that the qualification database is encrypted. Synthetic content exists inside explicitly scoped fixture source records and the private persisted operation/projection payload required for the storage test.

## Deferred production controls

The following remain mandatory before any real persistence is admitted, but are intentionally not implemented here:

- encryption at rest and Android Keystore-backed key management;
- key loss, recovery, rotation, and lost-device behavior;
- backup, restore, export, and sharing policy;
- explicit deletion and physical purge behavior;
- retention rules, including separately governed safety-state retention;
- Android process/storage boundaries and production migrations;
- privacy-aware retrieval and user-visible controls;
- full personal-data lifecycle qualification in CT-V2-14.

No XOR, hard-coded key, token encryption, or other placeholder mechanism is present. CT-V2-07 therefore makes no false production-security claim.
