# CT-V2-15 turn lifecycle and UI authority

## State ownership

| State | Owner | Persistence |
| --- | --- | --- |
| committed source and lifecycle | governed longitudinal store | protected durable |
| Therapy route / safety result | CT-V2-04/05 result | turn result only |
| retrieval and context packet | CT-V2-11 | ephemeral |
| render history | CT-V2-13/runtime | ephemeral fingerprint state |
| selected mode and draft | ViewModel/UI | process-local |
| transcript presentation | ViewModel/UI | process-local |
| Biographer target/session memory | governed runtime | process-local operational state |
| encryption key | AndroidKeyStore | platform protected |

The UI is a projection and command surface. It displays explicit modes,
Journal posture, Therapy support controls and current safety clarification, private status, transcript,
runtime availability, and custody operations. It does not own longitudinal
truth or copy domain rules into callbacks.

## Commit and lifecycle rules

- Text is evidence only after an explicit send.
- Blank sends, drafts, discarded text, and unsent mode-specific drafts are not
  admitted.
- A private current turn remains usable as current input but is ineligible for
  future ordinary retrieval.
- Journal `NO_RESPONSE` admits a committed source while creating no Thomas
  artifact and making no renderer call.
- Source correction appends a governed revision and conservatively reforms
  eligible evidence. It does not overwrite the old source revision.
- Making a source private and deleting a source use governed lifecycle commands.
- Restore authenticates and validates the complete backup before clearing the
  current local corpus; the target after clearing is empty.

## Concurrency and lifecycle

`ThomasProductionRuntime` permits one turn/lifecycle operation at a time. The
ViewModel disables send while processing and allocates explicit client turn
indices. Durable source evidence survives process death and device reboot through the
protected store. Draft, transcript, listening, speaking, and render-attempt
state are deliberately not converted into durable psychological data.

Full reset destroys the application-controlled corpus and AndroidKeyStore key,
then reopens a new empty root. It cannot erase exports or protected backups the
user previously placed outside application storage.

CT-V2-15R1 retains explicit ephemeral Therapy procedure and delivered-action history in the runtime. Process death or successful source custody edits invalidate that machinery and current safety declarations; no procedure is guessed from durable history. See [the current observation/coverage contract](CT-V2-15R1-PRODUCTION-OBSERVATION-AND-COVERAGE.md).
