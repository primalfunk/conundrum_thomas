# CT-V2-15 speech boundaries

## Disposition

Speech input and output are disabled for CT-V2-15. Typed input is the canonical
production path.

The repository has a platform speech module boundary, but no implementation is
admitted and the application requests no microphone permission. This is a
governance result, not a missing fallback: no available recognizer was shown to
provide the required private/offline behavior and unambiguous finish-versus-
cancel semantics on the target device.

## Future STT requirements

An admitted adapter must produce a transcript only. Partial results, final
results, finish/use, and cancel/discard must be distinct. Only an explicitly
committed final transcript may enter the same governed admission path as typed
input. Raw audio must not persist, and observed network behavior must be
disclosed before psychological content is sent to a provider.

## Future TTS requirements

TTS may consume only a final visible assistant artifact after CT-V2-13
validation. It must never speak candidates, prompts, packets, private material,
or `NO_RESPONSE`. New input, mode change, and lifecycle interruption must stop
obsolete speech.

CT-V2-15 makes no STT privacy/offline claim and no TTS qualification claim.
