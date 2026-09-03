-- Version relationships preserve currency and scope without adjudicating therapeutic behavior.
-- @statement
INSERT INTO source_version_relationship(
    relationship_id, from_version_id, relationship_type, to_version_id, rationale, adjudication_state
) VALUES
('rel-mhgap-guideline-2015-2023', 'who-mhgap-guideline-2015', 'SUPERSEDED_BY', 'who-mhgap-guideline-2023', 'WHO identifies the third edition as an update of the 2015 guideline update.', 'IDENTITY_VERIFIED'),
('rel-mhgap-ig1-ig2', 'who-mhgap-ig-v1-2010', 'SUPERSEDED_BY', 'who-mhgap-ig-v2-2016', 'WHO identifies version 2.0 as the update of the 2010 intervention guide.', 'IDENTITY_VERIFIED'),
('rel-mhgap-ig2-current-guideline', 'who-mhgap-ig-v2-2016', 'PROCEDURAL_REFERENCE_INFORMED_BY_NEWER_GUIDELINE', 'who-mhgap-guideline-2023', 'The older procedural guide must later be checked against the current recommendation authority before any use.', 'RECORDED_FOR_LATER_ADJUDICATION'),
('rel-pm-training-manual', 'who-pm-plus-training-2025', 'TRAINING_MANUAL_FOR', 'who-pm-plus-v1-1-2018', 'The official training-manual page explicitly links and describes the Individual PM+ intervention manual.', 'IDENTITY_VERIFIED'),
('rel-step-self-help', 'who-step-by-step-2026', 'WEB_ANNEX_TO', 'who-self-help-2026', 'WHO publishes the Step-by-Step intervention material as an annex to the self-help delivery-manual family.', 'IDENTITY_VERIFIED'),
('rel-equip-fhs', 'who-equip-2026', 'COMPLEMENTS', 'who-fhs-2025', 'The assessment compendium and training manual are distinct products of the joint EQUIP program.', 'IDENTITY_VERIFIED'),
('rel-self-help-implementation', 'who-self-help-2026', 'COMPLEMENTS', 'who-implementation-2024', 'Intervention-specific self-help delivery and general implementation guidance provide distinct scopes.', 'IDENTITY_VERIFIED'),
('rel-nist-genai-rmf', 'nist-genai-profile-2024', 'COMPLEMENTS', 'nist-ai-rmf-1-0-2023', 'NIST identifies AI 600-1 as a cross-sector companion profile to AI RMF 1.0.', 'IDENTITY_VERIFIED');
-- @statement
INSERT INTO source_conflict(
    conflict_id, left_version_id, right_version_id, conflict_kind, description,
    status, adjudication_phase, resolution
) VALUES
('conflict-mhgap-procedure-currency', 'who-mhgap-ig-v2-2016', 'who-mhgap-guideline-2023', 'PROCEDURE_VS_EVIDENCE', 'The 2016 procedural algorithms predate the 2023 recommendation update. Any material difference must be clinically adjudicated; the newer evidence source cannot be silently overridden.', 'OPEN', 'CT-V2-02 or later clinical adjudication', NULL),
('scope-asq-vs-ng225', 'nimh-asq-tool-2025', 'nice-ng225-2025', 'SETTING', 'NIMH describes brief screening in medical settings followed by trained-clinician assessment; NICE separately prohibits predictive and global risk-stratification uses. Screening must not be conflated with prediction or a complete disposition algorithm.', 'NOT_A_CONFLICT_DIFFERENT_SCOPE', 'CT-V2-02 safety-source analysis', NULL),
('scope-cci-vs-who-self-help', 'cci-self-help-web-2024', 'who-self-help-2026', 'SETTING', 'Both contain self-help resources, but their intervention families, implementation assumptions, populations, and rights differ. They are not interchangeable procedural authorities.', 'NOT_A_CONFLICT_DIFFERENT_SCOPE', 'CT-V2-02 source-family analysis', NULL);
-- @statement
INSERT INTO source_freshness(
    freshness_id, version_id, verified_at, official_source_url, latest_known_version,
    next_review_due, surveillance_or_update_note
) VALUES
('fresh-who-fhs', 'who-fhs-2025', '2026-09-03', 'https://www.who.int/publications/i/item/9789240105935/', '2025 electronic edition with incorporated 2025-08-25 corrigendum', '2027-03-03', 'Recheck official WHO and EQUIP publication surfaces for correction or successor.'),
('fresh-who-equip', 'who-equip-2026', '2026-09-03', 'https://www.who.int/westernpacific/publications/i/item/9789240121041', '2026 first edition', '2027-03-03', 'New publication; recheck EQUIP tools and official publication surface.'),
('fresh-who-pm', 'who-pm-plus-v1-1-2018', '2026-09-03', 'https://www.who.int/publications/i/item/WHO-MSD-MER-18.5', 'Generic field-trial version 1.1', '2027-03-03', 'Procedural reference remains linked by the 2025 training manual; recheck for a replacement intervention manual.'),
('fresh-who-pm-training', 'who-pm-plus-training-2025', '2026-09-03', 'https://www.who.int/publications/i/item/9789240109926', '2025 training manual', '2027-03-03', 'Recheck training and intervention-manual relationship.'),
('fresh-who-self-help', 'who-self-help-2026', '2026-09-03', 'https://www.who.int/publications/i/item/9789240120785', '2026 first edition', '2027-03-03', 'Recheck official self-help intervention family and annex list.'),
('fresh-who-step', 'who-step-by-step-2026', '2026-09-03', 'https://www.who.int/publications/i/item/B09738', '2026 annex B09738', '2027-03-03', 'Recheck annex identity and relationship to delivery manual.'),
('fresh-who-implementation', 'who-implementation-2024', '2026-09-03', 'https://www.who.int/publications/i/item/9789240087149', '2024 first edition', '2027-03-03', 'Recheck for implementation-manual revisions.'),
('fresh-who-mhgap-2015', 'who-mhgap-guideline-2015', '2026-09-03', 'https://www.who.int/publications/i/item/9789241549417', 'Superseded by third edition (2023)', '2027-09-03', 'Historical identity check only; do not restore as current authority.'),
('fresh-who-mhgap-2023', 'who-mhgap-guideline-2023', '2026-09-03', 'https://www.who.int/westernpacific/publications/i/item/9789240084278', 'Third edition (2023)', '2026-12-03', 'Safety- and treatment-relevant guideline; check for updated recommendations, corrections, or successor.'),
('fresh-who-mhgap-ig1', 'who-mhgap-ig-v1-2010', '2026-09-03', 'https://www.who.int/publications/i/item/9789241548069', 'Superseded by version 2.0', '2027-09-03', 'Historical identity check only.'),
('fresh-who-mhgap-ig2', 'who-mhgap-ig-v2-2016', '2026-09-03', 'https://www.who.int/publications/i/item/9789241549790', 'Version 2.0 (2016; publication surface 2019)', '2026-12-03', 'Recheck addenda and current guideline relationship before procedural analysis.'),
('fresh-who-live-life', 'who-live-life-2021', '2026-09-03', 'https://www.who.int/publications/i/item/9789240026629', '2021 first edition', '2026-12-03', 'Safety-guidance cadence; recheck successor and WHO suicide-prevention publications.'),
('fresh-nice-ng222', 'nice-ng222-2026', '2026-09-03', 'https://www.nice.org.uk/guidance/ng222', 'NG222; last reviewed 2026-01-30', '2026-12-03', 'NICE will review if new evidence is likely to change recommendations.'),
('fresh-nice-ng225', 'nice-ng225-2025', '2026-09-03', 'https://www.nice.org.uk/guidance/ng225', 'NG225; update information current through 2024-08', '2026-12-03', 'Safety-guidance cadence; recheck surveillance, update information, and rights terms.'),
('fresh-nimh-toolkit', 'nimh-asq-toolkit-web-2026', '2026-09-03', 'https://www.nimh.nih.gov/research/research-conducted-at-nimh/asq-toolkit-materials', 'Living toolkit web state verified 2026-09-03', '2026-12-03', 'Living safety toolkit; verify pathways and materials at each review.'),
('fresh-nimh-asq', 'nimh-asq-tool-2025', '2026-09-03', 'https://www.nimh.nih.gov/research/research-conducted-at-nimh/asq-toolkit-materials', 'Official screening PDF dated 2025-09-29', '2026-12-03', 'Recheck official tool artifact and toolkit integration.'),
('fresh-va-safety', 'va-safety-planning-web-2026', '2026-09-03', 'https://www.mentalhealth.va.gov/get-help/treatment/ebt.asp', 'Living VA web state verified 2026-09-03', '2026-12-03', 'Recheck content, provider assumptions, and rights provenance.'),
('fresh-samhsa-tip35', 'samhsa-tip35-2019', '2026-09-03', 'https://www.samhsa.gov/resource/ebp/tip-35-enhancing-motivation-change-substance-use-disorder-treatment', 'TIP 35 updated 2019; resource page updated 2026-03-10', '2027-03-03', 'Recheck TIP update status and publication-rights notice.'),
('fresh-cci-self-help', 'cci-self-help-web-2024', '2026-09-03', 'https://www.cci.health.wa.gov.au/Resources/Looking-After-Yourself', 'Living collection; domain pages verified 2026-09-03', '2026-12-03', 'Recheck individual domain last-updated dates and copyright notice.'),
('fresh-nist-rmf', 'nist-ai-rmf-1-0-2023', '2026-09-03', 'https://www.nist.gov/itl/ai-risk-management-framework', 'AI RMF 1.0; NIST reports revision underway', '2026-12-03', 'Monitor the official revision; retain version 1.0 as immutable history.'),
('fresh-nist-genai', 'nist-genai-profile-2024', '2026-09-03', 'https://www.nist.gov/itl/ai-risk-management-framework', 'NIST AI 600-1 (2024)', '2026-12-03', 'Recheck profile updates alongside AI RMF revision.');
