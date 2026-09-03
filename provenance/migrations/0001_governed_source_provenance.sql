-- CT-V2-01 source and provenance schema. This schema represents authorities and review gates;
-- it contains no therapeutic rules, decisions, interventions, or safety algorithms.
-- @statement
CREATE TABLE schema_migration (
    file_name TEXT PRIMARY KEY,
    sha256 TEXT NOT NULL UNIQUE CHECK (sha256 GLOB '[0-9a-f]*' AND length(sha256) = 64),
    applied_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- @statement
CREATE TABLE seed_application (
    file_name TEXT PRIMARY KEY,
    sha256 TEXT NOT NULL UNIQUE CHECK (sha256 GLOB '[0-9a-f]*' AND length(sha256) = 64),
    applied_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- @statement
CREATE TABLE source_authority (
    authority_id TEXT PRIMARY KEY,
    display_name TEXT NOT NULL UNIQUE,
    authority_domain TEXT NOT NULL CHECK (authority_domain IN (
        'CLINICAL_PUBLIC_HEALTH', 'CLINICAL_GOVERNMENT',
        'CLINICAL_RESOURCE_PROVIDER', 'ENGINEERING_GOVERNANCE'
    )),
    jurisdiction TEXT,
    official_home TEXT NOT NULL CHECK (official_home LIKE 'https://%')
);
-- @statement
CREATE TABLE source_class (
    class_id TEXT PRIMARY KEY CHECK (class_id IN (
        'FOUNDATIONAL_HELPING', 'STRUCTURED_INTERVENTION', 'CLINICAL_GUIDELINE',
        'SAFETY_GUIDANCE', 'THERAPEUTIC_METHOD', 'IMPLEMENTATION_GUIDANCE',
        'COMPETENCY_ASSESSMENT', 'ENGINEERING_GOVERNANCE'
    )),
    description TEXT NOT NULL
);
-- @statement
CREATE TABLE review_state (
    state_id TEXT PRIMARY KEY CHECK (state_id IN (
        'DISCOVERED', 'VERIFIED', 'RIGHTS_REVIEW_REQUIRED', 'CLINICAL_REVIEW_REQUIRED',
        'APPROVED_AS_SOURCE', 'REJECTED', 'SUPERSEDED'
    )),
    description TEXT NOT NULL
);
-- @statement
CREATE TABLE review_state_transition (
    from_state TEXT NOT NULL REFERENCES review_state(state_id),
    to_state TEXT NOT NULL REFERENCES review_state(state_id),
    PRIMARY KEY (from_state, to_state),
    CHECK (from_state <> to_state)
);
-- @statement
CREATE TABLE source_document (
    document_id TEXT PRIMARY KEY,
    authority_id TEXT NOT NULL REFERENCES source_authority(authority_id),
    canonical_title TEXT NOT NULL,
    publisher TEXT NOT NULL,
    publication_family TEXT,
    canonical_identifier TEXT,
    thomas_relevance_abstract TEXT NOT NULL,
    non_authorization_statement TEXT NOT NULL,
    UNIQUE (authority_id, canonical_title),
    UNIQUE (authority_id, canonical_identifier)
);
-- @statement
CREATE TABLE source_document_class (
    document_id TEXT NOT NULL REFERENCES source_document(document_id),
    class_id TEXT NOT NULL REFERENCES source_class(class_id),
    is_primary INTEGER NOT NULL DEFAULT 0 CHECK (is_primary IN (0, 1)),
    PRIMARY KEY (document_id, class_id)
);
-- @statement
CREATE UNIQUE INDEX one_primary_class_per_document
ON source_document_class(document_id) WHERE is_primary = 1;
-- @statement
CREATE TABLE source_version (
    version_id TEXT PRIMARY KEY,
    document_id TEXT NOT NULL REFERENCES source_document(document_id),
    edition TEXT,
    version_label TEXT NOT NULL,
    publication_date TEXT CHECK (publication_date IS NULL OR publication_date GLOB '????-??-??'),
    publication_date_note TEXT NOT NULL,
    publication_state TEXT NOT NULL CHECK (publication_state IN (
        'CURRENT', 'CURRENT_WITH_SURVEILLANCE', 'PROCEDURAL_REFERENCE',
        'SUPERSEDED', 'HISTORICAL_REFERENCE'
    )),
    isbn_or_reference TEXT,
    language TEXT NOT NULL DEFAULT 'en',
    review_state TEXT NOT NULL REFERENCES review_state(state_id),
    source_verified_at TEXT NOT NULL CHECK (source_verified_at GLOB '????-??-??'),
    metadata_notes TEXT,
    UNIQUE (document_id, version_label, language)
);
-- @statement
CREATE TABLE source_locator (
    locator_id TEXT PRIMARY KEY,
    version_id TEXT NOT NULL REFERENCES source_version(version_id),
    locator_kind TEXT NOT NULL CHECK (locator_kind IN (
        'OFFICIAL_LANDING_PAGE', 'OFFICIAL_ARTIFACT', 'OFFICIAL_WEB_RESOURCE',
        'OFFICIAL_RIGHTS_NOTICE', 'OFFICIAL_SURVEILLANCE_RECORD'
    )),
    official_url TEXT NOT NULL CHECK (official_url LIKE 'https://%'),
    retrieved_at TEXT NOT NULL CHECK (retrieved_at GLOB '????-??-??'),
    media_type TEXT,
    artifact_sha256 TEXT CHECK (
        artifact_sha256 IS NULL OR
        (artifact_sha256 GLOB '[0-9a-f]*' AND length(artifact_sha256) = 64)
    ),
    artifact_byte_size INTEGER CHECK (artifact_byte_size IS NULL OR artifact_byte_size > 0),
    cache_file_name TEXT,
    locator_notes TEXT,
    UNIQUE (version_id, locator_kind, official_url),
    CHECK (
        (locator_kind = 'OFFICIAL_ARTIFACT' AND artifact_sha256 IS NOT NULL AND artifact_byte_size IS NOT NULL AND cache_file_name IS NOT NULL)
        OR
        (locator_kind <> 'OFFICIAL_ARTIFACT' AND artifact_sha256 IS NULL AND artifact_byte_size IS NULL AND cache_file_name IS NULL)
    )
);
-- @statement
CREATE UNIQUE INDEX unique_acquired_artifact_identity
ON source_locator(artifact_sha256, artifact_byte_size)
WHERE artifact_sha256 IS NOT NULL;
-- @statement
CREATE TRIGGER source_locator_artifact_identity_is_immutable
BEFORE UPDATE OF artifact_sha256, artifact_byte_size, cache_file_name ON source_locator
WHEN OLD.locator_kind = 'OFFICIAL_ARTIFACT'
BEGIN
    SELECT RAISE(ABORT, 'acquired artifact identity is immutable');
END;
-- @statement
CREATE TABLE source_section (
    section_id TEXT PRIMARY KEY,
    version_id TEXT NOT NULL REFERENCES source_version(version_id),
    locator_id TEXT REFERENCES source_locator(locator_id),
    location_type TEXT NOT NULL CHECK (location_type IN (
        'PAGE', 'PAGE_RANGE', 'SECTION', 'CHAPTER', 'RECOMMENDATION',
        'TABLE', 'TOOL', 'ANNEX', 'WEB_HEADING'
    )),
    location_value TEXT NOT NULL,
    subject_label TEXT NOT NULL,
    location_notes TEXT,
    UNIQUE (version_id, location_type, location_value, subject_label)
);
-- @statement
CREATE TABLE rights_record (
    rights_id TEXT PRIMARY KEY,
    version_id TEXT NOT NULL UNIQUE REFERENCES source_version(version_id),
    copyright_holder TEXT NOT NULL,
    license_identifier TEXT NOT NULL,
    rights_statement TEXT NOT NULL,
    commercial_use_status TEXT NOT NULL CHECK (commercial_use_status IN (
        'PUBLIC_DOMAIN', 'OPEN_COMMERCIAL_USE', 'OPEN_NONCOMMERCIAL_ONLY',
        'COMMERCIAL_PERMISSION_REQUIRED', 'COPYRIGHTED_REFERENCE_ONLY',
        'RIGHTS_UNCLEAR', 'LEGAL_REVIEW_REQUIRED'
    )),
    attribution_required INTEGER NOT NULL CHECK (attribution_required IN (0, 1)),
    adaptation_permission TEXT NOT NULL CHECK (adaptation_permission IN (
        'PERMITTED', 'NONCOMMERCIAL_ONLY', 'PERMISSION_REQUIRED', 'PROHIBITED', 'UNCLEAR'
    )),
    redistribution_permission TEXT NOT NULL CHECK (redistribution_permission IN (
        'PERMITTED', 'NONCOMMERCIAL_ONLY', 'PERMISSION_REQUIRED', 'PROHIBITED', 'UNCLEAR'
    )),
    share_alike_required INTEGER NOT NULL CHECK (share_alike_required IN (0, 1)),
    permissions_route TEXT,
    rights_verified_at TEXT NOT NULL CHECK (rights_verified_at GLOB '????-??-??'),
    rights_evidence_url TEXT NOT NULL CHECK (rights_evidence_url LIKE 'https://%'),
    rights_review_required INTEGER NOT NULL CHECK (rights_review_required IN (0, 1)),
    legal_determination_deferred INTEGER NOT NULL DEFAULT 1 CHECK (legal_determination_deferred = 1)
);
-- @statement
CREATE TABLE applicability_metadata (
    applicability_id TEXT PRIMARY KEY,
    version_id TEXT NOT NULL REFERENCES source_version(version_id),
    population TEXT NOT NULL,
    age_scope TEXT NOT NULL,
    context TEXT NOT NULL,
    setting TEXT NOT NULL,
    intended_deliverer TEXT NOT NULL,
    exclusions_or_limits TEXT NOT NULL,
    software_autonomy_authorized INTEGER NOT NULL DEFAULT 0 CHECK (software_autonomy_authorized = 0)
);
-- @statement
CREATE TABLE source_version_relationship (
    relationship_id TEXT PRIMARY KEY,
    from_version_id TEXT NOT NULL REFERENCES source_version(version_id),
    relationship_type TEXT NOT NULL CHECK (relationship_type IN (
        'SUPERSEDED_BY', 'UPDATED_BY', 'TRAINING_MANUAL_FOR', 'WEB_ANNEX_TO',
        'PROCEDURAL_REFERENCE_INFORMED_BY_NEWER_GUIDELINE', 'COMPLEMENTS'
    )),
    to_version_id TEXT NOT NULL REFERENCES source_version(version_id),
    rationale TEXT NOT NULL,
    adjudication_state TEXT NOT NULL CHECK (adjudication_state IN (
        'IDENTITY_VERIFIED', 'RECORDED_FOR_LATER_ADJUDICATION', 'RESOLVED'
    )),
    UNIQUE (from_version_id, relationship_type, to_version_id),
    CHECK (from_version_id <> to_version_id)
);
-- @statement
CREATE TABLE source_conflict (
    conflict_id TEXT PRIMARY KEY,
    left_version_id TEXT NOT NULL REFERENCES source_version(version_id),
    right_version_id TEXT NOT NULL REFERENCES source_version(version_id),
    conflict_kind TEXT NOT NULL CHECK (conflict_kind IN (
        'CURRENCY', 'POPULATION', 'DELIVERER', 'SETTING', 'RIGHTS',
        'PROCEDURE_VS_EVIDENCE', 'SOFTWARE_AUTONOMY', 'UNRESOLVED_EVIDENCE'
    )),
    description TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('OPEN', 'RESOLVED', 'NOT_A_CONFLICT_DIFFERENT_SCOPE')),
    adjudication_phase TEXT NOT NULL,
    resolution TEXT,
    CHECK ((status = 'RESOLVED' AND resolution IS NOT NULL) OR status <> 'RESOLVED'),
    CHECK (left_version_id <> right_version_id)
);
-- @statement
CREATE TABLE review_requirement (
    requirement_id TEXT PRIMARY KEY,
    version_id TEXT NOT NULL REFERENCES source_version(version_id),
    review_type TEXT NOT NULL CHECK (review_type IN (
        'CLINICAL', 'RIGHTS', 'LEGAL', 'IMPLEMENTATION_SCOPE', 'SOFTWARE_AUTONOMY'
    )),
    reviewer_role TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('PENDING', 'IN_REVIEW', 'COMPLETE', 'WAIVED')),
    requested_at TEXT NOT NULL CHECK (requested_at GLOB '????-??-??'),
    completed_at TEXT,
    disposition TEXT,
    concerns TEXT,
    scope_limitations TEXT,
    follow_up_required TEXT,
    UNIQUE (version_id, review_type, reviewer_role),
    CHECK ((status = 'COMPLETE' AND completed_at IS NOT NULL AND disposition IS NOT NULL) OR status <> 'COMPLETE')
);
-- @statement
CREATE TABLE review_event (
    event_id TEXT PRIMARY KEY,
    version_id TEXT NOT NULL REFERENCES source_version(version_id),
    sequence_number INTEGER NOT NULL CHECK (sequence_number > 0),
    from_state TEXT NOT NULL,
    to_state TEXT NOT NULL,
    reviewer_role TEXT NOT NULL,
    reviewer_identity TEXT NOT NULL,
    reviewed_at TEXT NOT NULL,
    disposition TEXT NOT NULL,
    concerns TEXT,
    scope_limitations TEXT,
    follow_up_required TEXT,
    UNIQUE (version_id, sequence_number),
    FOREIGN KEY (from_state, to_state) REFERENCES review_state_transition(from_state, to_state)
);
-- @statement
CREATE TABLE source_freshness (
    freshness_id TEXT PRIMARY KEY,
    version_id TEXT NOT NULL UNIQUE REFERENCES source_version(version_id),
    verified_at TEXT NOT NULL CHECK (verified_at GLOB '????-??-??'),
    official_source_url TEXT NOT NULL CHECK (official_source_url LIKE 'https://%'),
    latest_known_version TEXT NOT NULL,
    next_review_due TEXT NOT NULL CHECK (next_review_due GLOB '????-??-??'),
    surveillance_or_update_note TEXT NOT NULL
);
-- @statement
CREATE TABLE candidate_subject (
    candidate_id TEXT PRIMARY KEY,
    section_id TEXT NOT NULL REFERENCES source_section(section_id),
    subject_summary TEXT NOT NULL,
    earliest_authorized_phase TEXT NOT NULL CHECK (earliest_authorized_phase IN ('CT-V2-02', 'CT-V2-03', 'LATER')),
    status TEXT NOT NULL DEFAULT 'UNOPENED' CHECK (status = 'UNOPENED'),
    no_rule_authority INTEGER NOT NULL DEFAULT 1 CHECK (no_rule_authority = 1)
);
-- @statement
CREATE VIEW source_version_without_class AS
SELECT sv.version_id
FROM source_version sv
JOIN source_document sd ON sd.document_id = sv.document_id
LEFT JOIN source_document_class sdc ON sdc.document_id = sd.document_id
GROUP BY sv.version_id
HAVING COUNT(sdc.class_id) = 0;
-- @statement
CREATE VIEW source_version_without_freshness AS
SELECT sv.version_id
FROM source_version sv
LEFT JOIN source_freshness sf ON sf.version_id = sv.version_id
WHERE sf.version_id IS NULL;
-- @statement
CREATE VIEW clinical_version_without_pending_review AS
SELECT sv.version_id
FROM source_version sv
JOIN source_document sd ON sd.document_id = sv.document_id
JOIN source_authority sa ON sa.authority_id = sd.authority_id
WHERE sa.authority_domain <> 'ENGINEERING_GOVERNANCE'
AND NOT EXISTS (
    SELECT 1 FROM review_requirement rr
    WHERE rr.version_id = sv.version_id
      AND rr.review_type = 'CLINICAL'
      AND rr.status IN ('PENDING', 'IN_REVIEW', 'COMPLETE')
);
