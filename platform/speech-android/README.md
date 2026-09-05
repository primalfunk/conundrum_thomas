# `:platform:speech-android`

Reserved Android speech input/output adapter boundary. It contains no V1 speech
code and remains implementation-free through CT-V2-15. The production app
therefore exposes typed input only and requests no microphone permission.
Future finish-speaking must produce a reviewable transcript for explicit user
commit; cancel must discard it. Future TTS may consume only a final validated
assistant artifact.
