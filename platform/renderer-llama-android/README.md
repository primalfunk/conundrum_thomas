# `:platform:renderer-llama-android`

Android local Thomas-model renderer adapter. It targets `arm64-v8a` and the
historically qualified llama.cpp b10621 CPU runtime. The adapter receives only
the typed `RendererInput` port, verifies the externally managed R007/R008 Q6_K
artifact, and returns an untrusted candidate to CT-V2-13 validation. It has no
policy, safety, retrieval, persistence, mode-selection, or network authority.

Model custody is app-private no-backup storage under `models/`; no GGUF is
packaged in the APK or committed to Git. If the exact artifact is absent or
fails verification, the renderer records local-model unavailability and uses
the existing deterministic fallback.
