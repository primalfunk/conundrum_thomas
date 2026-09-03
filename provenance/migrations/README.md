# Provenance migrations

Migrations are applied in lexical order. Each independently executable SQLite statement begins after a `-- @statement` marker. Applied filenames and SHA-256 values are recorded in the generated database.

Migrations are append-only after acceptance. Correct a released schema with a later migration; do not rewrite its history.
