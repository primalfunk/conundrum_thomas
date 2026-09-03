# ADR 0003: Psychological-data admission boundary

- Status: Accepted
- Date: 2026-09-03
- Scope: All V2 runtime and qualification work

## Decision for CT-V2-00

Android application backup is disabled and backup/data-transfer rules exclude every application data domain. No production psychological or user data may be stored in CT-V2-00. The empty persistence adapter is a boundary marker, not authorization to persist data.

No transcript, profile, formulation, intervention history, or safety-state content may appear in ordinary application logs.

## Gate before real persistence

An explicit later decision, threat model, implementation, and qualification are required before persistence of real user data. The admitted design must include:

- local data protection at rest;
- OS-backed key management;
- explicit and verifiable deletion behavior;
- governed retention periods and transitions;
- structural exclusion of transcript/profile content from ordinary logs; and
- separately considered safety-state retention, including its distinct necessity, access, deletion, and escalation requirements.

Backup flags do not replace storage encryption, key management, retention, or deletion design. CT-V2-00 intentionally does not select or implement the final persistence/security mechanism.
