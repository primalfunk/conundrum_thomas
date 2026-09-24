# CT-V2 local neural TTS bakeoff

## Disposition

`CT_V2_LOCAL_NEURAL_TTS_CANDIDATES_INSUFFICIENT`

No candidate in this bounded, physical bakeoff met the CT-V2 conversational
latency gate on the TCL 9491G. No neural provider, model, speaker mapping, or
audition build is admitted by this record.

## Method

All diagnostics ran in `com.conundrum.thomas.v2.principaldev` on TCL 9491G,
Android 15, arm64-v8a. Each used sherpa-onnx `v1.13.8` Android AAR, local
app-private `no_backup/tts-bakeoff` artifacts, CPU provider, two threads, the
same final-text-only preview call, and ephemeral `AudioTrack` PCM playback.
No speech audio was written to files or conversation history. The common text
was: “Hi. I’m Thomas. Take your time, and tell me what’s on your mind.”

RTF is synthesis time divided by generated duration. CT-V2 requires RTF below
0.5 to be usable and below 0.25 to be preferred. Time-to-audio here is the
logged synthesis end to playback request, plus the synthesis itself; therefore
the reported synthesis duration is the material user-perceived wait.

## Artifacts evaluated

| Candidate | Archive | Archive SHA-256 | Extracted model family |
|---|---|---|---|
| Supertonic 3 INT8 | `supertonic.tar.bz2` (128,774,318 bytes) | `82fa96f91c4ef8abaae3a14a3f4153facf88bed821d1f7331cec2700f432c427` | `sherpa-onnx-supertonic-3-tts-int8-2026-05-11`; 145,316,356 bytes extracted |
| VITS/VCTK INT8 | `vits-vctk.tar.bz2` (151,758,892 bytes) | `4f0a02db66914b3760b144cebc004e65dd4d1aeef43379f2b058849e74002490` | VITS VCTK, 109 speakers |
| KittenTTS Nano 0.8 INT8 | `kitten.tar.bz2` (31,220,690 bytes) | `6fa5be852612ce761094ba74ee6123b4fc4acfefa79bf64dc63acae4a83af2fd` | `kitten-nano-en-v0_8-int8`; 45,652,547 bytes extracted |

The Supertonic model carried its upstream OpenRAIL-M terms. VITS voice-model
terms must be reviewed per voice model. Kitten source/model terms were carried
in the released package. License suitability was not advanced because no model
met the performance gate.

## Physical measurements

| Candidate/profile | Init | Synthesis | Audio | RTF | Process PSS / RSS |
|---|---:|---:|---:|---:|---:|
| Supertonic Male cold, 2 threads | 1.559 s | 2.185 s | 4.362 s | 0.50 | 438 MB / 556 MB |
| Supertonic Female warm, 2 threads | 0 s | 2.269 s | 4.364 s | 0.52 | 463 MB / 582 MB |
| Supertonic Neutral warm, 2 threads | 0 s | 2.503 s | 4.499 s | 0.56 | 463 MB / 582 MB |
| Supertonic Male cold, 4 threads | 1.604 s | 3.883 s | 4.362 s | 0.89 | 433 MB / 551 MB |
| VITS/VCTK Male cold, 2 threads | 6.085 s | 10.335 s | 3.959 s | 2.61 | 485 MB / 604 MB |
| Kitten Male cold, 2 threads | 1.738 s | 4.266 s | 5.367 s | 0.79 | 312 MB / 426 MB |
| Kitten Female warm, 2 threads | 0 s | 4.504 s | 5.555 s | 0.81 | 346 MB / 462 MB |
| Kitten Neutral warm, 2 threads | 0 s | 4.199 s | 5.329 s | 0.79 | 346 MB / 462 MB |

The initial Supertonic male result rounds to 0.50 but is approximately 0.501;
it does not satisfy the strict `< 0.5` gate. Its warm profile results are also
over that threshold. The 4-thread pass was slower, so it is not an optimization
path for this device/runtime combination. VITS also logged unknown-token
warnings during the test and is not suitable for further consideration.

## Quality and operational observations

The test established that all three released artifacts could be initialized
locally through the same Android native runtime (VITS with token warnings) and
that Supertonic and Kitten produced distinct speaker IDs. This is not a
Principal listening evaluation and makes no claims about final warmth,
naturalness, or neutral-voice suitability. Each failed latency before a
quality-based selection could responsibly occur.

No Q6_K + neural coexistence test was run: the independent neural latency gate
failed first. No speaker was frozen, no fallback claim beyond the already
published Android-system-TTS behavior is made, and no audition disposition is
claimed.

## Cleanup

The diagnostic provider, optional AAR Gradle hook, and ViewModel wiring are
removed. The staged model archives and external sources are ignored local
research material; app-private `tts-bakeoff` and prior rejected Kokoro `tts`
directories are removed from the DEV package. The device is returned to the
published Android-TTS fallback implementation.
