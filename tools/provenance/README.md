# Provenance build tooling

Build-only JVM tooling for reproducibly generating and checking the governed source corpus. This module is downstream of `:thomas:provenance`; no production or Android module depends on it.

Run `gradlew generateRuntimeProvenanceDb`. The task recreates `provenance/generated/thomas-provenance.sqlite` from tracked, checksum-recorded migrations and seeds. The generated database and every raw acquired artifact remain ignored.
