# CT-V2-14 Personal-Data Threat and Failure Model

| Threat/failure | Control | Residual boundary |
| --- | --- | --- |
| Another ordinary Android app | App sandbox + non-exported internal file + AndroidKeyStore | Compromised OS is out of scope |
| Filesystem extraction / powered-off lost device | AES-256-GCM ciphertext; key stored separately in AndroidKeyStore; unlocked-device requirement | Hardware backing and lock-state guarantees vary by qualified device |
| Plaintext artifacts | In-memory serialization; ciphertext-only atomic storage; explicit artifact scans | User-requested readable export is plaintext after delivery |
| Database/blob modification | GCM authentication, framing, chain fingerprints, logical replay digest | Availability can be lost; speculative repair is forbidden |
| Incomplete transaction/process death | candidate-first protection + atomic replace + post-failure verification | Filesystem/platform atomicity is assumed through documented APIs |
| Stale derived state | projection digest + deterministic replay/rebuild + lifecycle invalidation | Derivation-version changes may require explicit reconstruction |
| Stale or modified backup | explicit restore, encrypted payload, custody checksum, GCM authentication, revision check | An authentic older user-held backup remains restorable by explicit choice |
| Wrong/lost key | typed unavailable/integrity failure | Data is intentionally unrecoverable without both artifact and correct key |
| Developer log/crash leak | no narrative logging authority; static scans; IDs/reason codes only | External crash tooling must be separately admitted/configured |
| Component bypass | one admission method; no public DAO/SQL; renderer/model/app remain unwired | Malicious code inside the process is outside this module boundary |
| Deletion not propagating | dependency-aware removal, lifecycle tombstones, deletion checkpoint, post-delete backup tests | External exports/backups are outside app deletion control |
| Schema mismatch | strict current/prior/future handling and atomic migration | Unsupported history remains unavailable pending authorized migration |
| Renderer/model unauthorized access | no dependency, store, retrieval, or write ports in renderer/realizer | Future integration must preserve this graph |

This model does not claim defense against root with sufficient privileges, a hostile operating system, a debugger controlling the process, arbitrary code execution in Thomas, or an attacker controlling the unlocked user session.
