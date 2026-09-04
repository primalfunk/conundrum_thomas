# CT-V2-14 Encryption and Key Boundary

## Primary artifact

The logical document is serialized in memory and protected with `AES/GCM/NoPadding`, a 256-bit key, a new random 96-bit nonce for every write, a 128-bit authentication tag, and associated data binding purpose, key alias, and protection-format version. The stored file contains a short format header, nonce, and ciphertext/tag. It contains no normal plaintext shadow database.

On Android, `AndroidKeystorePersonalDataKeyProvider` creates a non-exported AES key in `AndroidKeyStore`, requests GCM-only use, randomized encryption, 256 bits, and unlocked-device availability. Hardware backing is deliberately reported as unknown until device-specific attestation or inspection establishes it. User authentication per operation is not required by this initial policy.

`AndroidAtomicProtectedArtifactStorage` writes under `Context.noBackupFilesDir` with `AtomicFile`. Key material and ciphertext are therefore in separate platform facilities. The application-facing key port can request cryptographic use; it cannot request raw key bytes.

## Backup artifact

Backup uses a separate 256-bit recovery key held by the caller. A versioned custody container carries only format metadata, source revision, checksum, and AES-GCM protected payload. Restore validates container framing/checksum, authenticated decryption, document integrity, and revision agreement before committing to an empty target.

## Failure behavior

- Missing/unavailable Android key: state is unavailable; no fresh history is silently substituted.
- Wrong key or changed ciphertext: authenticated open/restore fails.
- Unsupported protection/document version: fail closed.
- Lost app data with retained key, or lost key with retained artifact: the pair is incomplete and cannot recover the corpus.
- Full reset removes the app-controlled artifact then destroys the key alias and clears in-memory state.

## Honest protection limits

This design protects ordinary at-rest filesystem disclosure and a powered-off lost device within Android platform assumptions. It does not claim protection from a compromised OS, a rooted/debuggable environment with sufficient privilege, memory inspection while unlocked and in use, malicious code executing in the app process, or a person controlling the unlocked session. A plaintext user export is intentionally outside this at-rest boundary after delivery to a user-chosen destination.
