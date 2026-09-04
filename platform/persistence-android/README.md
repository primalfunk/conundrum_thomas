# `:platform:persistence-android`

CT-V2-14 Android adapter for the governed personal-data persistence core. It stores only authenticated ciphertext under `noBackupFilesDir`, uses a non-exportable AES-256 key in Android Keystore, and commits ciphertext through `AtomicFile`.

`AndroidPersonalDataPersistenceFactory` is the sole production-capable composition root. It is deliberately not consumed by `:app` or `:thomas:runtime` in CT-V2-14, so no real-user ingestion path exists. The adapter owns no admission, retrieval, Therapy, Journal, Biographer, renderer, model, or policy authority.
