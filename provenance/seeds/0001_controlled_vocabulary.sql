-- Controlled source-governance vocabulary. These values classify evidence; they do not define behavior.
-- @statement
INSERT INTO source_class(class_id, description) VALUES
('FOUNDATIONAL_HELPING', 'Competency and behavior expected of psychologically supportive helpers.'),
('STRUCTURED_INTERVENTION', 'A manualized psychological intervention or self-help procedure.'),
('CLINICAL_GUIDELINE', 'Evidence-based recommendations on recognition, treatment, limits, referral, safety, or escalation.'),
('SAFETY_GUIDANCE', 'Material specifically relevant to self-harm, suicide, crisis, urgent support, or related boundaries.'),
('THERAPEUTIC_METHOD', 'Method-specific material such as cognitive behavioral or motivational approaches.'),
('IMPLEMENTATION_GUIDANCE', 'Guidance for implementation, supervision, adaptation, monitoring, or evaluation.'),
('COMPETENCY_ASSESSMENT', 'Observable standards for later evaluation of helper performance.'),
('ENGINEERING_GOVERNANCE', 'Non-clinical material governing AI, software, privacy, security, or qualification risk.');
-- @statement
INSERT INTO review_state(state_id, description) VALUES
('DISCOVERED', 'Located but not independently verified.'),
('VERIFIED', 'Bibliographic identity and official locator verified; no clinical approval implied.'),
('RIGHTS_REVIEW_REQUIRED', 'A rights specialist must resolve permitted use.'),
('CLINICAL_REVIEW_REQUIRED', 'A qualified clinical reviewer must assess intended use.'),
('APPROVED_AS_SOURCE', 'Approved for a specifically documented source scope; no rule authority implied.'),
('REJECTED', 'Rejected for the reviewed scope.'),
('SUPERSEDED', 'Retained as history but replaced by a newer source version.');
-- @statement
INSERT INTO review_state_transition(from_state, to_state) VALUES
('DISCOVERED', 'VERIFIED'),
('DISCOVERED', 'REJECTED'),
('VERIFIED', 'RIGHTS_REVIEW_REQUIRED'),
('VERIFIED', 'CLINICAL_REVIEW_REQUIRED'),
('VERIFIED', 'APPROVED_AS_SOURCE'),
('VERIFIED', 'REJECTED'),
('VERIFIED', 'SUPERSEDED'),
('RIGHTS_REVIEW_REQUIRED', 'CLINICAL_REVIEW_REQUIRED'),
('RIGHTS_REVIEW_REQUIRED', 'APPROVED_AS_SOURCE'),
('RIGHTS_REVIEW_REQUIRED', 'REJECTED'),
('RIGHTS_REVIEW_REQUIRED', 'SUPERSEDED'),
('CLINICAL_REVIEW_REQUIRED', 'RIGHTS_REVIEW_REQUIRED'),
('CLINICAL_REVIEW_REQUIRED', 'APPROVED_AS_SOURCE'),
('CLINICAL_REVIEW_REQUIRED', 'REJECTED'),
('CLINICAL_REVIEW_REQUIRED', 'SUPERSEDED'),
('APPROVED_AS_SOURCE', 'RIGHTS_REVIEW_REQUIRED'),
('APPROVED_AS_SOURCE', 'CLINICAL_REVIEW_REQUIRED'),
('APPROVED_AS_SOURCE', 'SUPERSEDED');
-- @statement
INSERT INTO source_authority(authority_id, display_name, authority_domain, jurisdiction, official_home) VALUES
('WHO', 'World Health Organization', 'CLINICAL_PUBLIC_HEALTH', 'International', 'https://www.who.int/'),
('WHO_UNICEF', 'World Health Organization / United Nations Children''s Fund', 'CLINICAL_PUBLIC_HEALTH', 'International', 'https://equipcompetency.org/'),
('NICE', 'National Institute for Health and Care Excellence', 'CLINICAL_GOVERNMENT', 'England and Wales; international reuse governed separately', 'https://www.nice.org.uk/'),
('NIMH', 'National Institute of Mental Health', 'CLINICAL_GOVERNMENT', 'United States', 'https://www.nimh.nih.gov/'),
('VA', 'United States Department of Veterans Affairs', 'CLINICAL_GOVERNMENT', 'United States', 'https://www.mentalhealth.va.gov/'),
('SAMHSA', 'Substance Abuse and Mental Health Services Administration', 'CLINICAL_GOVERNMENT', 'United States', 'https://www.samhsa.gov/'),
('CCI', 'Centre for Clinical Interventions, North Metropolitan Health Service', 'CLINICAL_RESOURCE_PROVIDER', 'Western Australia', 'https://www.cci.health.wa.gov.au/'),
('NIST', 'National Institute of Standards and Technology', 'ENGINEERING_GOVERNANCE', 'United States', 'https://www.nist.gov/');
