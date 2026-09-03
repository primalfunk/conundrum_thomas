package com.conundrum.thomas.v2.longitudinal.store

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object LongitudinalStoreSchema {
    const val VERSION = 1
    const val CLASSIFICATION = "SYNTHETIC_QUALIFICATION_ONLY"

    internal val statements = listOf(
        """CREATE TABLE store_metadata (
            key TEXT PRIMARY KEY NOT NULL,
            value TEXT NOT NULL
        ) STRICT""".trimIndent(),
        """CREATE TABLE ledger_event (
            event_id TEXT PRIMARY KEY NOT NULL,
            store_revision INTEGER NOT NULL UNIQUE CHECK (store_revision > 0),
            request_id TEXT NOT NULL,
            idempotency_key TEXT NOT NULL UNIQUE,
            policy_version TEXT NOT NULL,
            operation_type TEXT NOT NULL,
            actor TEXT NOT NULL,
            origin TEXT NOT NULL,
            record_time TEXT NOT NULL,
            payload_fingerprint TEXT NOT NULL CHECK (length(payload_fingerprint) = 64),
            operation_payload BLOB NOT NULL,
            affected_stable_ids TEXT NOT NULL,
            decision_trace TEXT NOT NULL
        ) STRICT""".trimIndent(),
        """CREATE TABLE current_state_projection (
            singleton_id INTEGER PRIMARY KEY NOT NULL CHECK (singleton_id = 1),
            store_revision INTEGER NOT NULL,
            state_payload BLOB NOT NULL,
            canonical_digest TEXT NOT NULL CHECK (length(canonical_digest) = 64),
            ledger_event_id TEXT,
            FOREIGN KEY (ledger_event_id) REFERENCES ledger_event(event_id)
        ) STRICT""".trimIndent(),
        """CREATE TABLE immutable_object_index (
            object_type TEXT NOT NULL,
            stable_id TEXT NOT NULL,
            first_store_revision INTEGER NOT NULL,
            ledger_event_id TEXT NOT NULL,
            PRIMARY KEY (object_type, stable_id),
            FOREIGN KEY (ledger_event_id) REFERENCES ledger_event(event_id),
            FOREIGN KEY (first_store_revision) REFERENCES ledger_event(store_revision)
        ) STRICT""".trimIndent(),
        """CREATE TABLE lifecycle_history (
            object_type TEXT NOT NULL,
            stable_id TEXT NOT NULL,
            store_revision INTEGER NOT NULL,
            lifecycle_status TEXT NOT NULL,
            eligible INTEGER NOT NULL CHECK (eligible IN (0, 1)),
            cause_code TEXT NOT NULL,
            ledger_event_id TEXT NOT NULL,
            PRIMARY KEY (object_type, stable_id, store_revision),
            FOREIGN KEY (ledger_event_id) REFERENCES ledger_event(event_id),
            FOREIGN KEY (store_revision) REFERENCES ledger_event(store_revision)
        ) STRICT""".trimIndent(),
        """CREATE TABLE idempotency_record (
            idempotency_key TEXT PRIMARY KEY NOT NULL,
            logical_fingerprint TEXT NOT NULL CHECK (length(logical_fingerprint) = 64),
            ledger_event_id TEXT NOT NULL,
            store_revision INTEGER NOT NULL,
            FOREIGN KEY (ledger_event_id) REFERENCES ledger_event(event_id),
            FOREIGN KEY (store_revision) REFERENCES ledger_event(store_revision)
        ) STRICT""".trimIndent(),
        """CREATE TABLE rejection_audit (
            rejection_sequence INTEGER PRIMARY KEY AUTOINCREMENT,
            request_id TEXT NOT NULL,
            idempotency_key TEXT NOT NULL,
            policy_version TEXT NOT NULL,
            operation_type TEXT NOT NULL,
            disposition TEXT NOT NULL,
            reason_codes TEXT NOT NULL,
            payload_fingerprint TEXT NOT NULL CHECK (length(payload_fingerprint) = 64),
            record_time TEXT NOT NULL
        ) STRICT""".trimIndent(),
        """CREATE TRIGGER ledger_event_no_update BEFORE UPDATE ON ledger_event
            BEGIN SELECT RAISE(ABORT, 'immutable ledger event'); END""".trimIndent(),
        """CREATE TRIGGER ledger_event_no_delete BEFORE DELETE ON ledger_event
            BEGIN SELECT RAISE(ABORT, 'immutable ledger event'); END""".trimIndent(),
        """CREATE TRIGGER lifecycle_history_no_update BEFORE UPDATE ON lifecycle_history
            BEGIN SELECT RAISE(ABORT, 'immutable lifecycle history'); END""".trimIndent(),
        """CREATE TRIGGER lifecycle_history_no_delete BEFORE DELETE ON lifecycle_history
            BEGIN SELECT RAISE(ABORT, 'immutable lifecycle history'); END""".trimIndent(),
    )

    val fingerprint: String = MessageDigest.getInstance("SHA-256")
        .digest(statements.joinToString("\n-- statement --\n").toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
