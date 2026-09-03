-- Human-review queue. All items are pending; no reviewer identity, completion date, or approval is fabricated.
-- @statement
INSERT INTO review_requirement(
    requirement_id, version_id, review_type, reviewer_role, status, requested_at,
    concerns, scope_limitations, follow_up_required
) VALUES
('review-clinical-fhs', 'who-fhs-2025', 'CLINICAL', 'Foundational helping competency specialist', 'PENDING', '2026-09-03', 'Assess applicability of a human-helper training framework to software qualification.', 'Source approval would remain scoped and would not approve a Thomas abstraction.', 'Review before CT-V2-02 uses this source.'),
('review-clinical-equip', 'who-equip-2026', 'CLINICAL', 'Psychological-support competency assessment specialist', 'PENDING', '2026-09-03', 'Assess tool populations, assessor requirements, and construct validity for future qualification use.', 'Do not implement or score a tool before review.', 'Review before qualification abstractions.'),
('review-clinical-pm', 'who-pm-plus-v1-1-2018', 'CLINICAL', 'PM+ qualified clinical specialist', 'PENDING', '2026-09-03', 'Review intervention scope, exclusions, helper competence, supervision, and currentness.', 'Generic field-trial procedure is not autonomous-software authority.', 'Adjudicate with current training and broader guidelines.'),
('review-clinical-pm-training', 'who-pm-plus-training-2025', 'CLINICAL', 'PM+ training and supervision specialist', 'PENDING', '2026-09-03', 'Review relationship between training requirements, competency assessment, and intervention delivery.', 'Training metadata grants no implementation authority.', 'Review with the intervention manual.'),
('review-clinical-self-help', 'who-self-help-2026', 'CLINICAL', 'Digital psychological self-help specialist', 'PENDING', '2026-09-03', 'Review guided versus self-guided assumptions, population limits, and support boundaries.', 'Mobile relevance does not establish Thomas equivalence.', 'Review with each proposed annex or intervention.'),
('review-clinical-step', 'who-step-by-step-2026', 'CLINICAL', 'Step-by-Step intervention specialist', 'PENDING', '2026-09-03', 'Review annex scope, delivery model, exclusions, and relationship to the 2026 manual.', 'No content may be copied or abstracted before scoped review.', 'Link disposition to parent manual review.'),
('review-clinical-implementation', 'who-implementation-2024', 'CLINICAL', 'Psychological intervention implementation specialist', 'PENDING', '2026-09-03', 'Review which implementation principles are applicable outside established services.', 'Implementation guidance is not a therapeutic procedure.', 'Review before process abstractions.'),
('review-clinical-mhgap-2015', 'who-mhgap-guideline-2015', 'CLINICAL', 'mhGAP guideline specialist', 'PENDING', '2026-09-03', 'Confirm historical scope and supersession only.', 'Superseded source cannot become current recommendation authority.', 'Review only if needed for version history.'),
('review-clinical-mhgap-2023', 'who-mhgap-guideline-2023', 'CLINICAL', 'mhGAP guideline specialist', 'PENDING', '2026-09-03', 'Assess recommendation scope, populations, professional roles, and relevant updates.', 'Guideline assumes health-system actors and does not authorize software delivery.', 'Review before any mhGAP-derived abstraction.'),
('review-clinical-mhgap-ig1', 'who-mhgap-ig-v1-2010', 'CLINICAL', 'mhGAP procedural specialist', 'PENDING', '2026-09-03', 'Confirm historical relationship to version 2.0.', 'Superseded clinical algorithms are historical only.', 'Review only if version lineage is material.'),
('review-clinical-mhgap-ig2', 'who-mhgap-ig-v2-2016', 'CLINICAL', 'mhGAP procedural specialist', 'PENDING', '2026-09-03', 'Compare every relevant older procedure against the 2023 evidence guideline and addenda.', 'Trained health-worker algorithms are not directly portable to Thomas.', 'Resolve open procedure-versus-evidence conflict.'),
('review-clinical-live-life', 'who-live-life-2021', 'CLINICAL', 'Suicide-prevention public-health specialist', 'PENDING', '2026-09-03', 'Assess population-level applicability and distinguish it from individual crisis response.', 'Implementation guide is not an individual pathway.', 'Review before safety-source synthesis.'),
('review-clinical-ng222', 'nice-ng222-2026', 'CLINICAL', 'Adult depression guideline specialist', 'PENDING', '2026-09-03', 'Review jurisdiction, adult population, recommendation currentness, and treatment-setting assumptions.', 'No diagnostic or treatment authority follows from source registration.', 'Review before depression-related abstraction.'),
('review-clinical-ng225', 'nice-ng225-2025', 'CLINICAL', 'Self-harm guideline and safety specialist', 'PENDING', '2026-09-03', 'Review negative guidance, psychosocial-assessment roles, populations, and safety implications.', 'No risk model, assessment, or safety algorithm is authorized.', 'Review before any safety ontology or procedure.'),
('review-clinical-nimh-toolkit', 'nimh-asq-toolkit-web-2026', 'CLINICAL', 'Suicide screening and medical-pathway specialist', 'PENDING', '2026-09-03', 'Review validation populations, setting-specific pathways, positive-screen planning, and clinician roles.', 'Toolkit is not a complete decision algorithm.', 'Review with NICE negative guidance and product setting.'),
('review-clinical-nimh-asq', 'nimh-asq-tool-2025', 'CLINICAL', 'Suicide screening instrument specialist', 'PENDING', '2026-09-03', 'Review instrument validation, administration, licensing conditions, and downstream assessment.', 'No scoring or question delivery is authorized.', 'Review before any screening discussion.'),
('review-clinical-va-safety', 'va-safety-planning-web-2026', 'CLINICAL', 'Safety planning intervention specialist', 'PENDING', '2026-09-03', 'Review provider collaboration, evidence lineage, population, and third-party method identity.', 'The public web description is not a protocol.', 'Locate and govern primary method sources if later needed.'),
('review-clinical-tip35', 'samhsa-tip35-2019', 'CLINICAL', 'Motivational Interviewing specialist', 'PENDING', '2026-09-03', 'Separate SAMHSA consensus guidance from primary MI authority and assess population generalization.', 'Substance-use treatment manual does not authorize general therapeutic dialogue.', 'Review before MI source-family abstraction.'),
('review-clinical-cci', 'cci-self-help-web-2024', 'CLINICAL', 'CBT and guided self-help specialist', 'PENDING', '2026-09-03', 'Review evidence basis, maintenance dates, domain boundaries, and self-help versus clinician use.', 'No worksheet, diagnosis, or intervention is authorized.', 'Review domain by domain before abstraction.');
-- @statement
INSERT INTO review_requirement(
    requirement_id, version_id, review_type, reviewer_role, status, requested_at,
    concerns, scope_limitations, follow_up_required
)
SELECT
    'review-rights-' || version_id,
    version_id,
    'RIGHTS',
    'Authorized rights and licensing reviewer',
    'PENDING',
    '2026-09-03',
    'Determine permitted consultation, summarization, adaptation, implementation, reproduction, and commercial use for the proposed use.',
    'Corpus metadata is not a derivative-work or fair-use determination.',
    'Resolve before material crosses into product behavior, training data, prompts, or distributed artifacts.'
FROM rights_record
WHERE rights_review_required = 1;
-- @statement
INSERT INTO review_requirement(
    requirement_id, version_id, review_type, reviewer_role, status, requested_at,
    concerns, scope_limitations, follow_up_required
) VALUES
('review-legal-nice-ng222', 'nice-ng222-2026', 'LEGAL', 'Product counsel for NICE international and AI licensing', 'PENDING', '2026-09-03', 'NICE requires approval and agreement for international and AI uses.', 'Do not assume the UK Open Content Licence covers Thomas.', 'Obtain a written licensing disposition before use beyond consultation.'),
('review-legal-nice-ng225', 'nice-ng225-2025', 'LEGAL', 'Product counsel for NICE international and AI licensing', 'PENDING', '2026-09-03', 'NICE requires approval and agreement for international and AI uses.', 'Do not adapt recommendation wording or structure without authorization.', 'Obtain a written licensing disposition before use beyond consultation.'),
('review-autonomy-asq-toolkit', 'nimh-asq-toolkit-web-2026', 'SOFTWARE_AUTONOMY', 'Clinical software safety reviewer', 'PENDING', '2026-09-03', 'Medical screening pathways assign assessment and disposition roles to trained clinicians.', 'No autonomous pathway is admitted.', 'Review only after product safety authority is separately authorized.'),
('review-autonomy-asq-tool', 'nimh-asq-tool-2025', 'SOFTWARE_AUTONOMY', 'Clinical software safety reviewer', 'PENDING', '2026-09-03', 'A validated instrument is not automatically safe or appropriate for unsupervised software.', 'No screening implementation is admitted.', 'Review only after product safety authority is separately authorized.'),
('review-autonomy-ng225', 'nice-ng225-2025', 'SOFTWARE_AUTONOMY', 'Clinical software safety reviewer', 'PENDING', '2026-09-03', 'Negative guidance must constrain later architecture without becoming an improvised algorithm.', 'CT-V2-01 records evidence only.', 'Review during authorized safety-source synthesis.'),
('review-implementation-self-help', 'who-self-help-2026', 'IMPLEMENTATION_SCOPE', 'Digital intervention implementation reviewer', 'PENDING', '2026-09-03', 'Self-guided, guided, digital, and service-supported variants must remain distinct.', 'Mobile-first does not mean source-authorized.', 'Review before a vertical-slice intervention is selected.'),
('review-implementation-pm', 'who-pm-plus-v1-1-2018', 'IMPLEMENTATION_SCOPE', 'Non-specialist intervention implementation reviewer', 'PENDING', '2026-09-03', 'Helper training, supervision, competence, and program support assumptions may not be satisfied by Thomas.', 'No migration into runtime.', 'Review together with 2025 training manual and 2024 implementation manual.');
