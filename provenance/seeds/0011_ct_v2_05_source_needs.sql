-- CT-V2-05 records the unsupported pathway rather than inventing it.
-- @statement
INSERT INTO source_discovery_need(
    need_id,
    opened_phase,
    requested_domain,
    needed_scope,
    insufficiency_reason,
    minimum_authority_class,
    status,
    created_at
) VALUES (
    'need-ordinary-standalone-decision-support',
    'CT-V2-05',
    'ORDINARY_THERAPEUTIC_METHOD',
    'Non-diagnostic adult decision support for choosing among reasonable alternatives, including considerations, assumptions, and tradeoffs outside the PM+ manageable-problem procedure.',
    'The governed PM+ source supports choice only within its bounded problem-solving procedure; the corpus does not establish a standalone decision-support method or autonomous-software scope.',
    'Current authoritative guideline, public-health manual, or government/professional method resource with applicable scope and reviewable rights.',
    'UNOPENED',
    '2026-09-03'
);
-- @statement
INSERT INTO review_requirement(
    requirement_id, version_id, review_type, reviewer_role, status, requested_at,
    concerns, scope_limitations, follow_up_required
) VALUES
(
    'review-autonomy-fhs-core-ordinary',
    'who-fhs-2025',
    'SOFTWARE_AUTONOMY',
    'Clinical software autonomy reviewer',
    'PENDING',
    '2026-09-03',
    'Determine whether abstract human-helper communication competencies may be realized by autonomous software and under what supervision and product constraints.',
    'Qualification execution does not admit user-facing or production use.',
    'Review the CT-V2-05 rule interpretations and renderer constraints before any runtime admission.'
);
-- @statement
INSERT INTO review_requirement(
    requirement_id, version_id, review_type, reviewer_role, status, requested_at,
    concerns, scope_limitations, follow_up_required
) VALUES
(
    'review-autonomy-pm-core-ordinary',
    'who-pm-plus-v1-1-2018',
    'SOFTWARE_AUTONOMY',
    'Clinical software autonomy reviewer with PM+ expertise',
    'PENDING',
    '2026-09-03',
    'Determine whether the bounded procedural abstraction is appropriate for autonomous software given PM+ training, supervision, setting, and support assumptions.',
    'Qualification execution does not establish equivalence to a trained and supervised PM+ helper.',
    'Review together with clinical, rights, and implementation-scope requirements before any runtime admission.'
);
