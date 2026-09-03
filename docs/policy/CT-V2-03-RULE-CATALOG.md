# CT-V2-03 rule catalog

**Ruleset:** `ct-v2-03-bounded-problem-1.0.0`

**Rules:** 21

**Qualification-executable:** 21

**Production-runtime-authorized:** 0

All rules retain `PRODUCTION_AUTHORITY_NOT_GRANTED`. “FHS” means WHO/UNICEF Foundational Helping Skills 2025; “PM+” means WHO Individual PM+ generic field-trial v1.1 (2018). Full document/version/artifact identities are in the governed corpus and the vertical-slice document.

| Rule ID | Preconditions / exclusions | Result | Priority | Exact provenance | Pending restrictions |
|---|---|---|---:|---|---|
| `ctv203-r001-specialized-safety-handoff` | Upstream safety says specialized policy required | `SPECIALIZED_POLICY_REQUIRED`; safety handoff | 1200 | Principal CT-V2-03 scope, safety boundary | Production authority |
| `ctv203-r002-unknown-safety-stop` | Upstream safety is unknown | `INSUFFICIENT_INFORMATION`; upstream safety authority | 1190 | Principal CT-V2-03 scope, safety boundary | Production authority |
| `ctv203-r003-therapist-mode-only` | Mode is not Therapist | `OUT_OF_SCOPE`; active-mode policy | 1180 | Principal CT-V2-03 scope, mode boundary | Production authority |
| `ctv203-r004-specialized-scope-handoff` | Scope says specialized policy required | `SPECIALIZED_POLICY_REQUIRED` | 1170 | Principal CT-V2-03 scope | Production authority |
| `ctv203-r005-unknown-scope-stop` | Slice scope is unknown | `INSUFFICIENT_INFORMATION`; upstream scope authority | 1160 | Principal CT-V2-03 scope | Production authority |
| `ctv203-r006-out-of-scope-stop` | Explicitly outside slice | `OUT_OF_SCOPE` | 1150 | Principal CT-V2-03 scope | Production authority |
| `ctv203-r007-conflicting-evidence-stop` | Any material policy field is conflicting | `POLICY_CONFLICT`; evidence adjudication | 1140 | Principal CT-V2-03 conflict contract | Production authority |
| `ctv203-r008-respect-unwillingness` | Ordinary Therapist slice; user unwilling to continue | `pause-without-response` | 1100 | PM+ Ch. 3, publication pp. 26–27 / PDF pp. 28–29 | Clinical, rights, implementation, autonomy, human training/supervision, production |
| `ctv203-r009-establish-support-intent` | Ordinary slice; intent not established; excludes conflict/refusal | `ask-support-preference` | 1000 | FHS Module 8, publication pp. 118–120 / PDF pp. 129–131 | Clinical, rights, autonomy, production |
| `ctv203-r010-understand-present-problem` | Intent established; problem missing/vague; excludes conflict/refusal | `ask-problem-description` | 900 | FHS Module 1, publication pp. 27–31 / PDF pp. 38–42 | Clinical, rights, autonomy, production |
| `ctv203-r011-verify-problem-understanding` | Understanding/practical intent; bounded problem; understanding unconfirmed | `verify-problem-understanding` | 800 | FHS Module 8, publication pp. 126–127 / PDF pp. 137–138 | Clinical, rights, autonomy, production |
| `ctv203-r012-listening-reflection` | Listening requested; bounded problem | `reflect-for-listening` | 790 | FHS Module 1, publication pp. 27–31 / PDF pp. 38–42 | Clinical, rights, autonomy, production |
| `ctv203-r013-understanding-summary` | Understanding requested; bounded problem; shared understanding confirmed | `summarize-for-understanding` | 780 | FHS Module 1, publication pp. 27–31 / PDF pp. 38–42 | Clinical, rights, autonomy, production |
| `ctv203-r014-clarify-influenceable-part` | Practical intent; confirmed problem; influence unknown | `ask-influenceable-part` | 700 | PM+ Ch. 7 steps 1–3, publication pp. 46–49 / PDF pp. 48–51 | Clinical, rights, implementation, autonomy, human training/supervision, production |
| `ctv203-r015-non-influenceable-boundary` | Practical intent; confirmed problem; represented as not influenceable | `OUT_OF_SCOPE`; unopened policy required | 690 | PM+ Ch. 7 steps 1–3, publication pp. 46–49 / PDF pp. 48–51 | Clinical, rights, implementation, autonomy, human training/supervision, production |
| `ctv203-r016-establish-readiness-for-options` | Practical intent; influenceable problem; readiness not established | `ask-readiness-for-options` | 680 | FHS Module 8, publication pp. 118–120 / PDF pp. 129–131 | Clinical, rights, autonomy, production |
| `ctv203-r017-invite-user-options` | Practical intent; influenceable; willing to act; options missing | `invite-user-options` | 600 | PM+ Ch. 7 steps 4–5, publication pp. 49–51 / PDF pp. 51–53; Ch. 3 advice boundary, publication p. 24 / PDF p. 26 | Clinical, rights, implementation, autonomy, human training/supervision, production |
| `ctv203-r018-select-user-option` | User options established; selection missing | `ask-user-to-choose-option` | 500 | PM+ Ch. 7 steps 4–5, publication pp. 49–51 / PDF pp. 51–53 | Clinical, rights, implementation, autonomy, human training/supervision, production |
| `ctv203-r019-develop-bounded-plan` | User selection established; plan missing | `develop-bounded-plan` | 400 | PM+ Ch. 7 step 6, publication pp. 50–51 / PDF pp. 52–53 | Clinical, rights, implementation, autonomy, human training/supervision, production |
| `ctv203-r020-wait-for-reported-outcome` | Plan established; outcome missing | `wait-for-outcome` (`dialogue.no-response`) | 300 | PM+ Ch. 7 step 7, publication p. 51 / PDF p. 53 | Clinical, rights, implementation, autonomy, human training/supervision, production |
| `ctv203-r021-review-reported-outcome` | Plan and reported outcome established | `review-reported-outcome` | 200 | PM+ Ch. 7 step 7, publication p. 51 / PDF p. 53 | Clinical, rights, implementation, autonomy, human training/supervision, production |

## Tie-breaking

All matches are retained in the decision trace. A unique highest priority is required. Lower-priority matches remain eligible alternatives. Equal highest priorities terminate as `POLICY_CONFLICT`; lexical rule order is deterministic for display only and never resolves the conflict.

## Source and authority semantics

Architecture guards cite the tracked Principal scope record. Every source-derived action or boundary cites a `GovernedSourceReference` with document, version, section, and artifact locator. Missing provenance makes construction invalid. Pending reviews are surfaced in every selected decision but do not prevent controlled qualification execution. They do prevent any claim of production authority.
