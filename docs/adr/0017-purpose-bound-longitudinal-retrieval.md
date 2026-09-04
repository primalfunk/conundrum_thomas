# ADR 0017: Purpose-bound read-only longitudinal retrieval

## Status

Accepted for synthetic qualification in CT-V2-11.

## Decision

Add two pure-JVM modules. `:thomas:retrieval` depends only on the longitudinal domain and exposes a narrow read port plus deterministic selection. `:thomas:context-packet` depends on retrieval and owns immutable bounded packet assembly. Only `:qualification` adapts the CT-V2-07 store reader.

Eligibility precedes relevance. Requests are purpose-, mode-, revision-, policy-, and budget-bound. Structural relations outrank deterministic lexical overlap. Selected derived objects require a bounded balanced dependency neighborhood. Historical source text is explicitly typed as non-authoritative data. Packets are ephemeral and have a canonical logical digest.

## Consequences

Retrieval and packet assembly possess no writer, DAO, JDBC, Android, model, Therapy, Journal, or Biographer implementation dependency. Journal ordinary recall remains empty, CT-V2-10 retains target authority, and CT-V2-05 retains route authority. Embeddings, model search/reranking, production composition, packet persistence, and real-user retrieval remain unimplemented and unauthorized.
