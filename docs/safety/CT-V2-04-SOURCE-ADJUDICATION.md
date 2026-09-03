# CT-V2-04 safety-source adjudication

**Verified against governed corpus and official publication surfaces:** 2026-09-03

This adjudication determines only what CT-V2-04 may use for a qualification-only authority gate. It does not constitute clinical, legal, rights, implementation, or software-autonomy approval.

| Governed source | Population / setting / deliverer | Purpose | CT-V2-04 disposition |
|---|---|---|---|
| NICE NG225, `nice-ng225-2025` | Children, young people, and adults who have self-harmed; healthcare, social care, education, third-sector, and criminal-justice settings; qualified practitioners, with risk formulation assigned to mental-health professionals | Assessment, management, and recurrence prevention | **Used narrowly.** Recommendations 1.6.1–1.6.4 govern the non-predictive architecture. Recommendations 1.6.5–1.6.6 support preserving the needs/safety and specialist-assessment boundary. One self-harm disclosure rule therefore stops ordinary policy. No assessment, formulation, treatment, discharge, or safety procedure is implemented. |
| NIMH ASQ toolkit, `nimh-asq-toolkit-web-2026` | Medical patients ages 8+; emergency, inpatient, outpatient, and primary-care medical settings; medical staff and trained-clinician follow-up | Screening implementation and response to positive screens | **Deferred as a policy family.** It remains evidence that screening and follow-up are distinct and setting-specific. Its pathways are not generalized to an autonomous consumer app. |
| NIMH ASQ tool, `nimh-asq-tool-2025` | Medical patients ages 8+ with a positive-screen management pathway | Brief screening instrument | **Reference only.** Registered without items, wording, score, threshold, or response algorithm. Implementation authority and runtime authority are zero. |
| WHO LIVE LIFE, `who-live-life-2021` | National/local populations; countries, governments, focal points, and community stakeholders | Public-health suicide-prevention implementation | **Not used for individual gate rules.** It is not an individual assessment or crisis pathway. |
| WHO mhGAP guideline third edition, `who-mhgap-guideline-2023` | People with mental, neurological, and substance-use conditions; non-specialized health facilities; health workers and planners | Current evidence recommendations and service scale-up | **Deferred.** Professional and health-system assumptions do not authorize autonomous software delivery. |
| WHO mhGAP Intervention Guide v2.0, `who-mhgap-ig-v2-2016` | Priority conditions in non-specialized health settings; trained health workers | Integrated assessment/management algorithms | **Deferred.** The older procedural reference has an open procedure-versus-2023-evidence conflict and all-rights-reserved restrictions. |
| VA Safety Planning web resource, `va-safety-planning-web-2026` | Veterans in VA care working with a provider | Collaborative safety planning | **Deferred.** The page is not a complete protocol; third-party method rights and provider/autonomy assumptions remain unresolved. |

## NICE and NIMH scope boundary

The existing `scope-asq-vs-ng225` record remains `NOT_A_CONFLICT_DIFFERENT_SCOPE`:

- NIMH describes medical-setting screening followed by trained-clinician assessment.
- NICE rejects predictive tools and global low/medium/high stratification and focuses professional assessment on needs and safety.

CT-V2-04 does not synthesize these into one Thomas algorithm. It implements neither the ASQ nor NICE psychosocial/risk formulation. The NIMH family has no executable gate rule. The NICE-derived rule only denies ordinary-policy authority after established self-harm-relevant evidence.

## Rights and review state

- NICE: `LEGAL_REVIEW_REQUIRED`; international and AI use requires prior approval and agreement. Clinical, rights, legal, implementation-scope, and software-autonomy reviews remain pending.
- NIMH: government public-domain text subject to image, endorsement, accuracy, advertising, and medical-advice conditions. Clinical and autonomy reviews remain pending.
- WHO current sources: `CC BY-NC-SA 3.0 IGO`; commercial use requires permission.
- mhGAP IG v2.0: `COPYRIGHTED_REFERENCE_ONLY` pending permission analysis.
- VA safety planning: `RIGHTS_UNCLEAR` at method level.

No review event was added. No pending requirement was marked complete or waived. No protected form, item wording, script, worksheet, or long source excerpt entered code, documentation, or runtime artifacts.

## Source needs left open

- A governed harm-to-others authority suitable for software scope decisions.
- A source-adjudicated consumer-software emergency handoff policy.
- Population- and jurisdiction-appropriate external-support resource governance.
- Clinical and software-autonomy adjudication of any future direct safety question.
- Primary method and rights evidence before any safety-planning implementation.
