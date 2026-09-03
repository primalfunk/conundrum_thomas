# CT-V2-00 foundation scope and gates

## Authorized disposition

- `GO_FOR_CT_V2_00_FOUNDATION`
- `HOLD_CT_V2_01`
- `NO_V1_CODE_MIGRATION_AUTHORIZED`
- `NO_THERAPEUTIC_IMPLEMENTATION_AUTHORIZED`

## Foundation contents

- Independent Git origin and immutable untouched-starter tag.
- V1 lineage references and dirty-evidence fingerprints without dependencies.
- Governing V2 architecture and accepted foundation ADRs.
- Compile-enforced module direction and minimal authority contracts.
- Deny-by-default component migration register.
- Empty provenance and layer-qualification homes.
- Independent Android identity, development platform floor, and disabled backup.

## Explicit absences

CT-V2-00 contains no therapeutic rule, clinical source record, safety pathway, intervention, formulation, perception classifier, renderer implementation, JNI/llama.cpp code, model or adapter binary, speech implementation, persistence implementation, user-data importer, V1 production source, production psychological/user data, or clinical qualification scenario.

## Decisions settled for foundation

- V2 is canonical and independent, using `com.conundrum.thomas.v2`.
- V1 references are evidence only; migration is denied by default.
- R009D is qualified but not accepted; R009E is dirty and unaccepted.
- Development floor is minSdk 31 and initial native ABI is arm64-v8a.
- Backup is disabled; persistence requires a later security admission decision.
- Provenance uses versioned relational sources, migrations, reviewable seeds, and generated runtime data after authorization.
- Renderer input is restricted to command plus authorized support; historical models are not admitted.
- Runtime alone owns future pipeline ordering; safety is independently authoritative.

## Gates before CT-V2-01 work

Principal authorization is required to lift `HOLD_CT_V2_01`. The authorization should define source-corpus acquisition scope and reviewers. Schema details, license fields, source-ingestion workflow, raw-source storage location, and generated-database tooling are CT-V2-01 design work under the accepted relational direction; they are not blockers to completing CT-V2-00 and are not pre-decided here.
