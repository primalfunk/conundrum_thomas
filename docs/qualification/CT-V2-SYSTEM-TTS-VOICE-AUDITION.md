# CT-V2 Android system-voice audition candidate

## Disposition

`CT_V2_SYSTEM_TTS_VOICE_AUDITION_READY`

Supertonic is deferred for the current hardware generation. The Principal DEV
candidate therefore uses the existing fully local Android TextToSpeech provider
as the practical voice path. This is an audition disposition only: the
Principal must listen to and accept the three mappings before they are frozen.

## Device and engine catalog

Measured on TCL 9491G, Android 15, arm64-v8a. The active engine is
`com.google.android.tts` (device log identifies Google TTS
`googletts.google-speech-apk_20260817.01_p0.966249458`). The installed,
non-network, US-English voice inventory was:

* `en-us-x-iob-local`
* `en-us-x-iog-local`
* `en-us-x-iol-local`
* `en-us-x-iom-local`
* `en-us-x-sfg-local`
* `en-us-x-tpc-local`
* `en-us-x-tpd-local`
* `en-us-x-tpf-local`

Android exposes no reliable gender or naturalness metadata for these IDs. The
names remain DEV diagnostic identity only; ordinary Settings exposes only the
product-level Male, Female, and Neutral names.

## Provisional audition mapping

| Product profile | Offline system voice | Alternate retained for diagnosis |
|---|---|---|
| Male | `en-us-x-iob-local` | `en-us-x-iom-local` |
| Female | `en-us-x-iog-local` | `en-us-x-sfg-local` |
| Neutral | `en-us-x-iol-local` | `en-us-x-tpc-local` |

The chosen three are distinct, installed local voices. They were not pitch
shifted. The alternates are not exposed in ordinary Settings and are not a
claim that any raw engine label conveys gender; they exist only if the
Principal finds a slot unsuitable in the listening session.

## Physical behavior

The common preview text was: “Hi. I’m Thomas. Take your time, and tell me
what’s on your mind.” Android callback time from `speak()` request to
`onStart()` was:

| Profile | Voice | Start callback latency |
|---|---|---:|
| Male, first request after engine setup | iob | 318 ms |
| Male, warmed | iob | 303 ms; subsequent run 8 ms |
| Female, warmed | iog | 67 ms |
| Neutral, warmed | iol | 58 ms |

`onStart()` is the closest platform callback to audible start; the system does
not expose a hardware-speaker first-sample timestamp. These results are
materially immediate compared with the rejected neural trials.

Starting a Female preview after a Male preview issued the expected new
`QUEUE_FLUSH` request, so the later preview replaced the former. For the
microphone path, a Male preview received `stop()` 322 ms after start; the app
then entered its STT transcript flow without stale Thomas playback. The
existing Stop and Replay controls use the same provider and canonical final
text. No TTS audio files were found in `cache`, `files`, or `no_backup`; the
Supertonic app-private directory was removed.

## Audition procedure

On the installed `com.conundrum.thomas.v2.principaldev` package, open
**Settings → Voice** and choose **Preview Male**, **Preview Female**, or
**Preview Neutral**. The preview is isolated from conversation history and
uses no Journal, Biographer, or Therapy input. The build remains visibly marked
DEV and production is not installed.

## Accessibility and privacy regression

The existing six palettes, text size, reduced motion, background controls,
Thought Loom, stop-on-microphone preference, and IBM Plex Sans/Mono-only UI
remain unchanged. The system provider receives `ValidatedThomasResponse` text
only, runs locally, uses no cloud API, and does not persist synthesized audio.
