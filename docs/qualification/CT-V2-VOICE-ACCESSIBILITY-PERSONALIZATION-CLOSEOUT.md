# CT-V2 voice, accessibility, and personalization closeout

## Disposition

`CT_V2_THOMAS_VOICE_ACCESSIBILITY_PERSONALIZATION_QUALIFIED` is **not
claimed** by this record. The mechanical/device gates below passed, but the
closeout remains incomplete because the live Principal speech session and
full physical visual review were not performed in this run, and the complete
qualification task remains red on five repository-boundary assertions.

No completion tag was created.

## Baseline and device custody

Measured on TCL 9491G, Android 15, arm64-v8a, serial `BC9424B4E3C5002`.

| Item | Result |
|---|---|
| Starting/final source baseline before this evidence commit | `3be1d4a2811e064e4e36c77de2a570a8e110d50a` |
| Branch | `main` |
| Package installed | `com.conundrum.thomas.v2.principaldev` |
| Production package | absent |
| Activity | `com.conundrum.thomas.v2/com.conundrum.thomas.v2.MainActivity` |
| Visible identity | `DEV BUILD` / `Thomas DEV` |
| Model | `Thomas-CT-R007-Merged-Q6_K.gguf` |
| Model size | `3,155,622,624` bytes |
| Model SHA-256 | `d8a98b45c1c0e72ad63fe4a55105dbbc8c59bfa8910d37c0d0ff83d01125cb95` |
| Model custody | DEV app-private `no_backup/models` |
| TTS engine | `com.google.android.tts` |
| Final screen | Settings → Voice |

The earlier null TTS default was repaired as device configuration by selecting
`com.google.android.tts`; no application source change was required.

## Frozen Principal acceptance and voice checks

The Principal acceptance supplied by the closeout order is recorded here; no
second subjective audition was performed.

| Product profile | Frozen offline voice | Principal acceptance |
|---|---|---|
| Male | `en-us-x-iob-local` | ACCEPTED |
| Female | `en-us-x-iog-local` | ACCEPTED |
| Neutral | `en-us-x-iol-local` | ACCEPTED |

Mechanical Preview checks passed after app restart. Log evidence showed exact
profile-to-voice requests and successful starts:

* Male / `en-us-x-iob-local` / 284 ms;
* Female / `en-us-x-iog-local` / 74 ms;
* Neutral / `en-us-x-iol-local` / 79 ms.

The earlier temporary offline run also passed all three previews with Wi-Fi
disabled and restored connectivity afterward. A new preview replaced the
previous preview. No network voice substitution was observed.

## Spoken interaction and controls

| Gate | Result | Evidence boundary |
|---|---|---|
| Q6_K model verification and llama.cpp initialization | PASS | Device log reported exact hash, model verification, native load, and generation completion. |
| Keyboard Therapy turn | PASS | Synthetic keyboard input produced a non-echoing Thomas response and exact final-response TTS request. |
| Final-response-only TTS | PASS mechanically | Log showed `SYSTEM_TTS_REQUEST` only after validated completion, using the selected Neutral voice. |
| Thought Loom completion transition | PASS mechanically | UI returned to `Completed` and displayed the final response. |
| Replay | PASS mechanically | Replay entered `Thomas is speaking` and requested the canonical Neutral response. |
| Stop | PASS mechanically | Replay/Stop probe returned the conversation to idle with the displayed response intact. |
| Microphone interruption | PASS mechanically | Earlier probe stopped speech at approximately 861 ms and entered recognizer flow without overlapping playback. |
| Live Principal STT → Thomas → TTS turn | NOT RUN | A live human Principal transcript was not available to this run; no result is fabricated. |
| Contextual spoken follow-up | NOT RUN as speech | A synthetic keyboard follow-up generated a contextual response, but it is not a substitute for live speech acceptance. |
| Keyboard parity | PASS mechanically | Keyboard turn used the same real Q6_K → validation → TTS path. |

The synthetic keyboard follow-up used malformed ADB escaping in its displayed
user text, so it is retained only as a bounded runtime/context probe, not as
Principal conversation evidence.

## Persistence, accessibility, appearance, and privacy

The app was restarted and the following persisted selections were verified in
the Settings hierarchy: Neutral voice, Normal rate, Thomas dark, Standard text
size, and stop-on-microphone enabled. The six palette controls, four text-size
controls, Reduced Motion control, and Choose image control were exercised and
the baseline selections were restored. The Android image picker opened
successfully; no personal image was selected or imported in this run.

The following are therefore mechanical/control checks only, not a complete
Principal visual sign-off:

* six palette options were present and selectable;
* High contrast dark and High contrast light were present and selectable;
* Small, Standard, Large, and Extra large were present and selectable;
* Reduced Motion was toggled and restored off;
* Choose image launched the Android photo picker;
* Remove was correctly disabled when no background was selected;
* Settings exposed Voice, Appearance, Accessibility, Conversation, Privacy,
  and Therapy-related controls;
* source audit confirmed IBM Plex Sans for ordinary UI and IBM Plex Mono for
  technical identity, with no other visible font family identified;
* app-private custody contained the Q6_K model, personal-data store, voice
  preference XML, and profile marker only;
* no WAV, PCM, neural-TTS, ONNX, or TFLite artifact was present in app-private
  files;
* preview and replay were not added as conversation turns by the mechanical
  probes.

Full custom-background dim/blur/crop/readability and all-size visual
clipping/landscape checks remain unperformed. Therapy, Journal, Biographer,
and live safety-mode physical smoke also remain unperformed in this run.

## Automated results

Passing focused commands:

* `:app:testPrincipalDevDebugUnitTest`
* `:platform:speech-android:test`
* `:app:lint`
* `:app:assemblePrincipalDevDebug`
* `:app:assembleProductionDebug`

The complete command reached build, production compile, lint, speech, and
qualification tasks, but `:qualification:test` finished with **863 tests
completed, 5 failed**:

1. `CTV211AcceptanceTest > acceptanceScenario[90_production_writers_zero]`;
2. `FoundationArchitectureTest > no restricted source or model artifact is present`;
3. `JournalAuthorityQualificationTest > acceptance57ProductionLongitudinalAndJournalWritersRemainZero`;
4. `LanguageEvidenceBoundaryQualificationTest > onlyAuthorizedSyntheticCaptureAndQualificationModulesConsumeLanguageEvidence`;
5. `LongitudinalStoreBoundaryQualificationTest > only qualification module consumes complete store`.

The failure diagnostics identify pre-existing ignored `out\\` artifacts and
historical generated qualification material as inputs to those repository
boundary scans. Those artifacts were preserved. No test was weakened and no
source repair was made.

## Build and source-control evidence

DEV APK:

* task: `:app:assemblePrincipalDevDebug`;
* path: `app/build/outputs/apk/principalDev/debug/app-principalDev-debug.apk`;
* size: `47,330,914` bytes;
* SHA-256: `f697768b54bda8c2e6728c17fc9473d6fc3cd31c710dc20ad94b7ce165ee8521`;
* version: `1.0-principaldev`;
* version code: `1`.

No product code or configuration was changed for this closeout. This evidence
file is the only intended repository change. No repair commit, completion tag,
or qualification claim is made until the unresolved physical and automated
gates are completed.
