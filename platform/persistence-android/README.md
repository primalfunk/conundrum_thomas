# `:platform:persistence-android`

CT-V2-14 Android adapter for the governed personal-data persistence core. It stores only authenticated ciphertext under `noBackupFilesDir`, uses a non-exportable AES-256 key in Android Keystore, and commits ciphertext through `AtomicFile`.

`AndroidPersonalDataPersistenceFactory` is the sole platform persistence
factory. CT-V2-15's one `ThomasAndroidCompositionRoot` consumes it and passes
only governed admission/read/lifecycle ports to the production runtime. The
adapter owns no admission policy, retrieval, Therapy, Journal, Biographer,
renderer, model, or conversational authority.
