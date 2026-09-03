# ADR 0007: Governed source authority and rights gates

**Status:** Accepted for CT-V2-01  
**Date:** 2026-09-03

## Decision

Therapeutic and engineering evidence is represented as versioned relational provenance generated from reviewable migrations and seeds. Clinical/public-health and engineering-governance authorities are structurally distinct. Every source version requires source classification, official location, applicability, rights, review state, and freshness metadata.

Raw documents remain outside Git and outside runtime artifacts. Exact acquired artifacts are identified by immutable SHA-256 and byte size. Rights metadata never substitutes for an authorized legal determination. Sources with noncommercial, international-use, AI-use, third-party, or unclear terms receive an explicit pending rights/legal gate.

Registration or `VERIFIED` status means bibliographic identity was checked against an official publisher. It does not mean clinical approval. `APPROVED_AS_SOURCE`, when later granted by a human reviewer for a documented scope, will still not create a therapeutic rule.

## Authority hierarchy

The default selection order is:

1. Current authoritative clinical or public-health guidelines.
2. Current competency frameworks and evidence-based intervention manuals from major public-health authorities.
3. Government and public-health treatment manuals and resources.
4. Professional-association guidelines.
5. High-quality structured clinical resources.
6. Peer-reviewed primary research when higher-level guidance is insufficient.
7. Textbooks and expert works for conceptual supplementation.
8. General web content as discovery assistance only.

The hierarchy is rebuttable. Within a specific domain, a more specialized and directly applicable source may outrank a general source. Future adjudication must consider currency, evidence authority, directness, population, setting, intended deliverer, procedural usefulness, and rights—not prestige alone.

## Consequences

- An older procedure cannot silently override a newer evidence recommendation.
- A screening instrument is not automatically a decision algorithm.
- A clinician pathway is not automatically safe for unsupervised software.
- Freely downloadable does not mean commercially reusable.
- A source can remain useful as a historical or procedural reference while being superseded as evidence authority.
- No V1 code or model gains admission through this corpus.
