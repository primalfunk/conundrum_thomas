# CT-V2-15 - Android Production Integration

CT-V2-15 composes the previously qualified V2 subsystems into one
production-shaped Android runtime. The canonical app supports committed typed
turns in Journal, Biographer, and Therapy; protected persistence; correction,
privacy, and deletion; deterministic rendering; and explicit user custody.

## Resulting path

```text
Compose UI / committed typed turn
              |
              v
ThomasAndroidCompositionRoot
              |
              v
ThomasProductionRuntime (sequencing only)
  | Journal policy -> admission -> optional governed render
  | Biographer target -> answer admission -> coverage -> governed render
  | Therapy capture -> safety -> CT-V2-05 route -> retrieval -> memory gate
              |
              v
CT-V2-13 validated deterministic rendering
              |
              v
final assistant artifact or true NO_RESPONSE silence
```

The same root composes CT-V2-14's AndroidKeyStore-backed, AES-GCM protected
atomic artifact in `noBackupFilesDir`. User-directed machine/human export,
protected backup plus separately saved recovery key, validated empty-target
restore, source correction/privacy/deletion, and complete reset are exposed as
explicit custody operations.

## Authority retained upstream

The UI, runtime orchestrator, persistence adapter, renderer, and platform have
no authority to choose safety, a Therapy route or technique, a Journal posture,
a Biographer target, evidence certainty, or memory relevance. Renderer output
cannot become user evidence. Historical source instructions remain data.

## Deliberate omissions

No model or remote inference path is present. No STT implementation is enabled:
the available platform boundary had not proved an offline/private recognizer or
finish-versus-cancel semantics. No TTS implementation is enabled. Typed input
therefore remains the only admitted production input, and visible validated
text is the only output surface.

See:

- [Android production runtime](architecture/CT-V2-15-ANDROID-PRODUCTION-RUNTIME.md)
- [Turn lifecycle and UI authority](architecture/CT-V2-15-TURN-LIFECYCLE-AND-UI-AUTHORITY.md)
- [Speech boundaries](architecture/CT-V2-15-SPEECH-BOUNDARIES.md)
- [Target-device evidence](qualification/CT-V2-15-TARGET-DEVICE-EVIDENCE.md)
- [Qualification record](qualification/CT-V2-15-QUALIFICATION.md)
