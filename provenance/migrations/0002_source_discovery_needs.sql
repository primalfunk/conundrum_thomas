-- Governed gaps where no admitted source presently supports the requested policy scope.
-- A discovery need grants no source, rule, review, or runtime authority.
-- @statement
CREATE TABLE source_discovery_need (
    need_id TEXT PRIMARY KEY,
    opened_phase TEXT NOT NULL,
    requested_domain TEXT NOT NULL,
    needed_scope TEXT NOT NULL,
    insufficiency_reason TEXT NOT NULL,
    minimum_authority_class TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'UNOPENED' CHECK (status IN ('UNOPENED', 'DISCOVERY_IN_PROGRESS', 'SOURCE_CANDIDATE_FOUND', 'CLOSED_NO_SOURCE')),
    no_source_authority INTEGER NOT NULL DEFAULT 1 CHECK (no_source_authority = 1),
    no_rule_authority INTEGER NOT NULL DEFAULT 1 CHECK (no_rule_authority = 1),
    created_at TEXT NOT NULL CHECK (created_at GLOB '????-??-??')
);
