# CT-V2 Kokoro local TTS rejection record

## Disposition

`CT_V2_KOKORO_LOCAL_TTS_REJECTED_FOR_CONVERSATIONAL_LATENCY`

Kokoro was evaluated only as a bounded, local Android diagnostic. It was not
admitted to the Thomas application and must not be used as the primary neural
provider for the Principal audition build.

## Custody and implementation examined

The experiment was made from published baseline `82d1988c78cfe2778c031335ff52a1216e5e48fa`
on the TCL 9491G (Android 15, arm64-v8a). It used the sherpa-onnx Android AAR
`v1.13.8` (50,129,134 bytes, SHA-256
`633c24321e06b1fe79feafa03ea16cbc0f8a286641e2da3559bac91bdb13bd96`) and
`kokoro-int8-en-v0_19.tar.bz2` (103,248,205 bytes, SHA-256
`c9f0dd393615805b0bab050c340834d5e684e732aec91c0e860cd30e982c08bd`). The
runtime and model were used fully locally. The upstream Kokoro model license
observed during reconnaissance was Apache-2.0.

The staged artifact was copied to the DEV app's app-private
`no_backup/tts/kokoro-int8-en-v0_19` directory and fail-closed checked before
runtime creation. Checked files were:

| File | Bytes | SHA-256 |
|---|---:|---|
| `model.int8.onnx` | 134,186,977 | `e0530c5031c03c5dc957edfc6d72582c2246e9b4566643ff67eeb03610c11641` |
| `voices.bin` | 5,755,904 | `a372c67b056ef0b695c375d39b99630d23fb07ad4c8d87aa32a19a62fca523ad` |
| `tokens.txt` | 1,078 | `4f31c71282d14af4e926cd12462078fe9d20d00c589e63fe2750a8f56d6d7f7b` |

Provisional, diagnostic-only speaker mapping was Male `am_michael` (6),
Female `af_bella` (1), and Neutral `af_sky` (4). None is a Principal-approved
Thomas identity.

## Physical result

The common preview was synthesized entirely on the device and played as
ephemeral `AudioTrack` PCM. No WAV/PCM was persisted. The first request had
2.731 s initialization cost. Measured synthesis and produced duration were:

| Candidate | Warm | Synthesis | Audio | RTF |
|---|---:|---:|---:|---:|
| Male | no | 12.316 s | 4.480 s | 2.75 |
| Female | yes | 11.518 s | 3.878 s | 2.97 |
| Neutral | yes | 10.663 s | 3.961 s | 2.69 |

The loaded diagnostic process measured approximately 457-465 MB PSS,
576-585 MB RSS, and 268-277 MB native heap. The idle app before neural
initialization was approximately 146 MB PSS, making the observed neural
increment roughly 320 MB PSS.

The diagnostic Principal DEV APK was
`com.conundrum.thomas.v2.principaldev` version `1.0-principaldev`, 167,390,458
bytes, SHA-256
`da95984aca925b224e64d7e162205dccaf4ddccc6560180671fb4dd7077ed7c2`.

## Decision

Kokoro did prove local generation, distinct speaker IDs, app-private artifact
custody, reflection-gated native runtime loading, PCM playback, and an Android
system-TTS fallback path. It failed the CT-V2 hard latency gate: warm
whole-utterance RTF was materially greater than 1.0 and time to playback was
over ten seconds for roughly four seconds of audio. Q6_K coexistence was not
attempted after that independent stop condition was met.

The transient provider, Gradle dependency hook, and ViewModel wiring are
intentionally removed after this evidence record. This document is the durable
record; it does not admit the implementation.
