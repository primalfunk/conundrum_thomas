# Conundrum Thomas V2 architectural risk register

| Risk | Current control | Next gate |
|---|---|---|
| Renderer or adapter accumulates therapeutic authority | Narrow render request, dependency isolation, architecture tests | Renderer admission tests must measure policy leakage and unsupported additions. |
| Ordinary policy can bypass safety | Safety is a sibling of engine and does not depend on it; runtime alone may orchestrate both | Define safety-governed action types and adversarial pathways under authorized source work. |
| Raw prose reaches policy | Typed `StructuredPolicyState` contract | CT-V2-02 must define finite structured state without smuggling transcript text into policy authority. |
| Hypotheses become facts | `EpistemicRecord` separates evidence kind, resolution, strength, contradictions, and supersession; every record requires governed profile admission | CT-V2-05 must implement admission and revision without adding an implicit fact-conversion path. |
| Sensitive data appears before security design | Empty persistence adapter, backup disabled, no production-data phase, ADR 0003 | Threat model and persistence ADR before any real data storage. |
| Backup or device-transfer behavior varies across Android implementations | `allowBackup=false` plus explicit exclusion rules; no production data | Re-qualify on representative target devices before persistence or release. |
| V1 architecture shapes V2 | Deny-by-default register and retired V1 therapeutic/controller entries | Review every proposed migration against V2 need and authority rules. |
| Dirty R009E material becomes de facto source | Fingerprint-only lineage record and no immutable commit | Require accepted immutable source before even proposing migration. |
| Historical models are mistaken for admitted renderers | All model candidates are reference-only and binaries are Git-ignored | Renderer-specific fidelity, leakage, resource, and artifact admission. |
| Clinical material is redistributed or adapted without rights | Raw/source directories excluded; provenance and ontology bindings preserve pending rights gates | Authorized rights review before abstraction, implementation, model use, or distribution. |
| Ontology definition is mistaken for therapeutic authority | Every concept and binding is runtime-denied; no production module consumes `:thomas:ontology` | CT-V2-03 must explicitly admit only the concepts needed by its bounded slice. |
| Source linkage launders pending material into behavior | Bindings are `PROVENANCE_ONLY` and cross-tested against pending clinical/rights records | No linked concept becomes executable without separate review and policy authorization. |
| Safety vocabulary becomes a covert risk score | Safety contexts are candidate-only and define no rank, prediction, threshold, keyword mapping, or response | Specialist adjudication before any safety state or transition is implemented. |
| Mode boundaries collapse into prompt personas | Typed domain contracts distinguish intervention, investigation, and capture; model and direct-profile authority are zero | Future policy and UI must depend on mode contracts rather than model prompts. |
| Empty qualification scaffolds imply clinical validation | Every placeholder explicitly says no scenarios or expectations | Populate only with authority and reviewer status in later phases. |
| Development floor becomes an accidental release promise | minSdk 31 and arm64-v8a documented as development-only | Product/device evidence before public compatibility decision. |
| New Gradle/Java toolchain drifts or fails offline | Versions, distribution checksum, wrapper JAR checksum, SDK and JDK recorded | Reproducible clean build in CI and dependency-locking decision before wider development. |
| Requested desktop hash spelling is ambiguous | Audited repository's full 40-hex commit is recorded alongside the 39-character requested spelling | No migration may use the malformed spelling; verify the immutable commit again at proposal time. |
