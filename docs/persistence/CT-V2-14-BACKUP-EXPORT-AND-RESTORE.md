# CT-V2-14 Backup, Export, and Restore

## Export

`export()` creates two in-memory user-owned views:

- versioned JSON separating `sourceEvidence` from `derivedState` and preserving stable/revision IDs, acquisition mode, privacy, event/report/record time, and original authorized content;
- Markdown with the same source/derived distinction for independent human inspection.

Export is not a database dump and contains no primary/recovery key, JDBC detail, credential, or rejected internal candidate. Because export is readable by design, a later Android picker/share integration must make destination and protection consequences explicit.

## Backup

`createProtectedBackup` snapshots one verified logical document and protects it with an independent recovery key before returning custody bytes. Empty state, multiple revisions, corrections, privacy states, and post-deletion state are valid. Random nonces mean binary backup bytes are not deterministic; logical state digests are.

No cloud account, synchronization, or generic Android backup authority is added. `allowBackup=false`, all legacy full-backup domains are excluded, and all data-extraction cloud/device-transfer domains are excluded.

## Restore

Restore is permitted only into an empty target. It parses the versioned custody container, validates checksum, authenticates/decrypts, validates/migrates the document, verifies source revision metadata, and atomically writes with a fresh primary Android key. Any failure before commit leaves the target empty. Retry is safe.

Restore never merges into a live corpus. Changed/truncated/wrong-key/unsupported artifacts fail closed. A valid older backup can reintroduce user data only through this explicit restore action; current app-controlled deletions cannot erase independent external custody.
