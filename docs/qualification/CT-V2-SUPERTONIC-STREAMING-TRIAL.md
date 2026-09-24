# CT-V2 final Supertonic buffered-playback trial

## Disposition

`CT_V2_LOCAL_NEURAL_TTS_DEFERRED_FOR_HARDWARE_LIMITS`

This was the single authorized post-bakeoff Supertonic 3 INT8 trial. It did
not admit Supertonic as a Thomas provider on TCL 9491G. The practical voice
path remains Android system TextToSpeech. No other neural engine is authorized
by this record.

## Authority and implementation boundary

The diagnostic provider accepted `ValidatedThomasResponse` only. It split that
already-final display text at sentence, clause, comma, then bounded word
boundaries (target 6--12 words), generated PCM in memory, and queued ordered
`AudioTrack` playback. It never received model tokens, prompts, policy state,
or persisted audio. A cancellation generation invalidated active, queued, and
in-flight chunks.

This was post-validation audio buffering, not language-model token streaming.

## Runtime and artifact

* Runtime: sherpa-onnx Android AAR `v1.13.8`, CPU, two threads.
* Model: `sherpa-onnx-supertonic-3-tts-int8-2026-05-11`.
* Source archive: `supertonic.tar.bz2`, 128,774,318 bytes,
  SHA-256 `82fa96f91c4ef8abaae3a14a3f4153facf88bed821d1f7331cec2700f432c427`.
* Extracted artifact set: 145,316,356 bytes; upstream terms included
  OpenRAIL-M.
* Device: TCL 9491G, Android 15, arm64-v8a.

## Physical results

The common preview phrase was split into six- and seven-word chunks. Times
below are monotonic device logs from synthesis start to queued playback
request; conversion/track setup added 14--42 ms.

| Scenario | First chunk synth | First chunk audio | Time to playback request | Following chunk synth / audio | Sustained result |
|---|---:|---:|---:|---:|---|
| Cold Male preview | 1.439 s + 1.545 s initialization | 2.614 s | ~2.998 s | 1.410 s / 2.252 s | queued in time |
| Warm Female preview | 1.589 s | 2.611 s | ~1.603 s | 1.412 s / 2.249 s | queued in time |
| Warm Neutral preview | 1.580 s | 2.697 s | ~1.598 s | 1.408 s / 2.322 s | queued in time |
| Warm Therapy final response with Q6_K resident, 12 words | 2.528 s | 4.271 s | ~2.574 s | n/a | hard-gate failure |

The short warmed previews could maintain a two-chunk buffer, but this does not
establish natural multi-sentence seams or rescue the provider. The actual
validated Therapy response exceeded the maximum two-second time-to-first-audio
gate. Its first-chunk RTF was about 0.59. Cold first speech was about 3.0 s.
Therefore buffering cannot make ordinary Thomas responses reliably
conversational on this hardware.

## Q6_K coexistence observation

The real Therapy cycle loaded the required
`Thomas-CT-R007-Merged-Q6_K.gguf` with SHA-256
`d8a98b45c1c0e72ad63fe4a55105dbbc8c59bfa8910d37c0d0ff83d01125cb95`.
It generated a 63-character final reply without process death or ANR. During
the combined session the process reported approximately 4,106,823 KB PSS,
3,901,660 KB RSS, and 556,264 KB native-heap PSS. This is an observation, not
an admission: the time-to-first-audio gate failed first.

The trial was ended before claiming seam quality, a full interruption timing,
or voice-profile qualification. Those are not carried forward as evidence.

## Cleanup

The diagnostic provider, chunker, test, Principal-DEV AAR hook, and ViewModel
wiring are removed. The app-private Supertonic directory is removed from the
DEV package after this evidence is recorded. The source archive remains ignored
local research material and is not a product artifact or Git content.
