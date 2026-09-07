# Established submission-dispatch root cause and bounded correction

## Disposition of the production hypothesis

The evidence establishes a qualification-driver coordinate race, not an accepted production submission being swallowed. Production was treated as suspect until instrumented reproduction located the boundary. No production submission-dispatch repair is warranted by this evidence. The only production changes retained are the two files from the exact archived durable-identity candidate.

## Failure-first evidence

- Historical run: sixth UI commit android-therapy-74 completed. The seventh draft remained with processing=false and idle workers after the 60-second wait; that capture alone did not identify the boundary.
- Diagnostic run-01: full runtime A–M and N reopen passed (446.540 / 81.649 seconds). Additional pre-click geometry observation caused the exact seventh input and following resume to commit as 75 and 76. The diagnostic was intentionally stopped afterward to remove observer-induced synchronization; its process-stop failure is not a product crash or completed qualification.
- Run-02: the focused PowerShell runner flattened its one-element method array and never launched a test. The array shape was corrected; custody and cleanup passed. This is not product failure evidence.
- Run-03: the original unsynchronized touch path reproduced at ordinal 9, “I don’t want to discuss this”. The immediate assertion failed with processing=false and draftLength=28. No UI_ON_CLICK, VIEWMODEL_ENTER, allocation, job, runtime or store marker followed the attempted touch.
- Run-04: the same failure reproduced at ordinal 8, “I want to continue”, proving it is not tied to refusal wording or a fixed turn number. A non-consuming root MotionEvent observer established the geometry:
  - t=145964211351116: button bounds x=1380–1576, y=1784–1880; center (1478,1832).
  - t=145964228176347: button moved to y=1727–1823.
  - t=145964231998193 / 145964232765885: injected down/up remained (1478,1832), outside the current button.
  - No UI_ON_CLICK or VIEWMODEL_ENTER followed.
  - Immediate presentation-admission assertion failed; draftLength=18, processing=false.
- Run-05: a deterministic mechanism regression explicitly captured the button at (1478,2328), opened the real IME, asserted that the old point lay outside the current button rectangle (1380,1552,1576,1632), and delivered the obsolete point. The unchanged admission assertion failed in 3.267 seconds with STALE_COORDINATE_MUST_NOT_STRAND_SUBMISSION. This proves the coordinate mechanism without depending on a random race or broad timeout.

The installed primary Compose 1.10.4 sources corroborate the measured path: Android performClickImpl calls performTouchInput { click() }; coordinates are calculated before batched MotionEvents are dispatched to the Compose root view. An OS IME animation can move the layout between those stages. Activity-level touch callbacks do not see that direct view injection; the root motion observer was needed to capture actual delivered coordinates.

## Last boundary crossed

**SUBMIT ACTION NEVER FIRED.** The injected touch reached the Compose root at an obsolete position and missed the enabled submit button. **VIEWMODEL ENTRY NEVER REACHED.**

The seven requested distinctions are adjudicated:
1. Action never fired: observed.
2. UI guard rejected: not observed; no callback or guard-entry marker on the missed action.
3. ViewModel entry never reached: observed.
4. ViewModel reached but job not launched: ruled out for the reproduced miss.
5. Job launched but pipeline not reached: ruled out for the reproduced miss.
6. Pipeline reached but worker inactive: ruled out for the reproduced miss.
7. Input processed but UI stale: ruled out; no runtime/capture marker or new committed source.

Successful control turns independently show UI_ON_CLICK → VIEWMODEL_ENTER → GUARD_ACCEPT → IDENTITY_ALLOCATED → PROCESSING_TRUE → JOB_ENTER → WORKER_ENTER → RUNTIME_ENTER → PIPELINE_ACCEPT → STORE_COMMITTED → WORKER_RETURN → cleared draft. Authorized silence does not skip capture.

No button callback occurred on failure, so no ViewModel processing transition or coroutine could occur. The retained draft/idle worker state is therefore explained without hypothesizing dropped jobs, backpressure or therapeutic classification.

## Bounded correction

UiSubmissionDriver.kt is Android qualification code only. Before each input it observes the current activity's IME animation lifecycle. At commit it dismisses the keyboard through Espresso's ordinary UI action, requires IME invisibility and no outstanding animation, synchronizes Compose layout, and only then resolves and taps the enabled commit button.

It does not invoke a semantics click callback, call the ViewModel directly, retry a missed submission, inject procedure state, disable animations, sleep for a guessed duration, or extend the original 60-second completion timeout. The five-second event-boundary observation bounds an explicit keyboard/animation predicate; it is not a longer pipeline timeout.

Both the existing UI scenarios and new stress regression assert synchronous presentation admission immediately after the real click. An accepted valid action must set processing or already have appended exactly one user turn. Later assertions require exactly one committed source, no stranded draft, and durable exact content. A future missed tap now fails at its actual boundary rather than being mislabeled as a processing timeout.

The controlled regression retains the same real keyboard movement and state assertions, but replaces the obsolete-coordinate delivery with the corrected driver. Run-06: controlled regression PASS (3.965 seconds), 20 varied touch submissions PASS (44.901 seconds), separate process-reopen verification of all 20 exact sources PASS (2.777 seconds). Both known apostrophe forms were admitted. No production instrumentation remained in this build.

## Scope and authority

MainActivity, protected persistence, renderer, all semantic/procedural/safety code and normalization are restored byte-for-byte to the reconstructed candidate. Temporary probe code is removed. ThomasViewModel and ThomasProductionRuntime exactly retain the archived identity repair hashes:
- 08a2b697208d8ada205d6166c4bc56046a26053ba0f6e443d968333377bfe902
- c86e1222569557f72b26fd5ca8b9f7a8f26cb408d18613b04b99f4ce1fb51e20

No additional production dispatch behavior, response policy, privacy/governance, corpus content, model, speech or Pattern Engine change is introduced. The final qualifier must report this fixture correction candidly, not invent a production root cause or production patch.

All diagnostic attempts preserved original-installation custody and removed their disposable packages. They are investigation/targeted evidence, not a waiver of the required fresh final software and complete physical qualification.
