# CT-V2-04 safety/scope rule catalog

All rules use policy version `ct-v2-04-safety-scope-gate-1.0.0`, are qualification-executable, and have zero production authority. Equal highest-priority matches are conflicts rather than ties resolved by guesswork.

| Rule | Priority | Prerequisite | Result | Provenance / restrictions |
|---|---:|---|---|---|
| `ctv204-r001-explicit-emergency-boundary` | 2200 | Current emergency explicitly established | `EMERGENCY_BOUNDARY_REACHED`; external-action policy required | Engineering authority scope; production denied |
| `ctv204-r002-explicit-medical-emergency-boundary` | 2190 | Acute medical emergency established present | `EMERGENCY_BOUNDARY_REACHED`; urgent-medical policy required | Engineering authority scope; production denied |
| `ctv204-r003-self-harm-specialized-boundary` | 2100 | Self-harm relevance established present | `SPECIALIZED_POLICY_REQUIRED` | NICE NG225 1.6.5–1.6.6, PDF p. 16/77; all clinical, rights, legal, scope, autonomy, training, supervision, and production restrictions pending; `scope-asq-vs-ng225` preserved |
| `ctv204-r004-harm-to-others-specialized-boundary` | 2090 | Harm-to-others relevance established present | `SPECIALIZED_POLICY_REQUIRED`; source/policy needed | Engineering authority scope; no violence algorithm |
| `ctv204-r005-specialized-condition-boundary` | 2080 | Specialized condition other than none established | `SPECIALIZED_POLICY_REQUIRED` | Engineering authority scope |
| `ctv204-r006-specialized-presenting-scope` | 2070 | Presenting scope established specialized | `SPECIALIZED_POLICY_REQUIRED` | Engineering authority scope |
| `ctv204-r007-out-of-scope-boundary` | 2060 | Presenting scope established out of scope | `OUT_OF_SCOPE` | Engineering authority scope |
| `ctv204-r008-unsupported-population-boundary` | 2050 | Unsupported population or setting established | `OUT_OF_SUPPORTED_POPULATION` | Engineering authority scope |
| `ctv204-r009-therapist-mode-only` | 2040 | Active mode is not Therapist | `OUT_OF_SCOPE` | Engineering authority scope; no Journal/Biographer behavior |
| `ctv204-r010-contradictory-evidence-clarification` | 1800 | Any required field contradictory | `CLARIFICATION_REQUIRED`; first contradictory field | Engineering epistemic guard |
| `ctv204-r011-declined-required-information` | 1700 | Any required field user-declined | `INSUFFICIENT_INFORMATION`; no repeated question | Engineering epistemic/autonomy guard |
| `ctv204-r012-tentative-evidence-clarification` | 1600 | Any required field tentative | `CLARIFICATION_REQUIRED`; first tentative field | Engineering epistemic guard |
| `ctv204-r013-missing-evidence-clarification` | 1500 | Any required field unknown or not asked | `CLARIFICATION_REQUIRED`; first missing field | Engineering epistemic guard |
| `ctv204-r014-ordinary-policy-permit` | 1000 | Therapist mode and all seven facts established as ordinary/supported/absent | `ORDINARY_POLICY_ALLOWED`; issue revision-bound permit | Engineering authority scope |

Rules define software authority only. They do not classify a person, establish safety from raw language, or supply missing specialized procedures.
