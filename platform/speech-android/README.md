# `:platform:speech-android`

Android speech adapter boundary. Speech input produces a reviewable transcript
for explicit user commit; cancellation preserves the pre-capture draft.

Speech output accepts only `ValidatedThomasResponse`, which is constructed by
the application after the runtime has returned its final assistant artifact and
the text has been placed in the conversation. Providers cannot receive prompts,
policy/safety state, action names, or model tokens. The current local fallback
is Android `TextToSpeech`; it never writes generated audio to storage and stops
on microphone start, mode change, backgrounding, explicit stop, and teardown.

Neural TTS is intentionally not represented as a fake provider here. It needs a
separately admitted, hash-verified artifact and in-process TCL memory/latency
qualification before it can become a primary provider.
