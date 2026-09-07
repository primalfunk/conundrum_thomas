# CT-V2-15R1 submission-dispatch reconnaissance before repair

Canonical main is unchanged at HEAD 4f822fb3498b67b40124c0fb4ec1f19f98c92c8b, tree 69fa2a2b68105c4371574ac81316aa6002fd4d93. The exact archived identity candidate was reconstructed in a detached disposable local clone; all 63 file hashes match its zip manifest. Forward and reverse patch checks pass. No production remedy has yet been selected.

## Complete boundary trace

1. MainActivity.ThomasApp collects ThomasViewModel.state (StateFlow) and passes the latest ThomasUiState to InputPanel.
2. InputPanel uses a Material3 Button tagged commit-turn. enabled = runtimeAvailable && !processing && draft.isNotBlank().
3. The multiline OutlinedTextField has no KeyboardActions or submit IME action configured. Keyboard input updates the draft; explicit commit is via the button. Scaffold has imePadding; keyboard/window-inset animation can change button position.
4. Button onClick is the bound viewModel::submit callback. It does not capture a draft snapshot or choose a semantic act.
5. ViewModel.submit reads mutableState.value at entry. It returns for processing, runtime unavailability, or blank draft. The predicate matches button enablement, but callback and recomposition times are independently observable.
6. Successful guard passes allocateTurnIndex to the archived runtime allocator, then synchronously set processing=true before launching work.
7. viewModelScope.launch enters on the main dispatcher, then withContext(Dispatchers.Default) calls the production runtime. No channel, queue, flow collector, or application mutex mediates this path.
8. Drafts live in a per-mode map plus immutable UI state. updateDraft synchronously updates both. Draft content is retained while processing; completion clears it. Mode changes are blocked while processing.
9. ThomasProductionRuntime.submit checks closed/blank, then AtomicBoolean.compareAndSet(false,true) guards pipeline admission. Busy/blank/closed return explicit ProductionTurnResult dispositions.
10. The worker calls the mode pipeline; persistence admission uses ProtectedPersonalDataStoreImpl.submit, synchronized and atomic. Duplicate keys retain strict fingerprint conflict checking.
11. Rendering follows governed policy/capture boundaries. Therapy may return authorized silence, while its user source remains independently captured. Renderer authority and duplicate validation are unchanged.
12. Worker result returns to main; ViewModel appends the user transcript with committedSourceId-derived saved flag, adds any assistant artifact, clears the draft and sets processing=false. Null runtime sets explicit unavailable status.
13. Activity configuration recreation normally retains the ViewModel. onCleared closes the runtime; viewModelScope cancellation follows ViewModel lifetime. No evidence currently establishes a cancellation in the failed submission.
14. StateFlow recomposition updates enabled/disabled and layout. Conversation scroll animation is separate from submission. Keyboard inset movement and Compose touch injection remain hypotheses requiring measured touch/click geometry.
15. The prior failure proves only that completion was not reached: draft stayed present, processing=false, workers idle. It does not by itself locate the failed event boundary.

## Temporary instrumentation

The isolated diagnostic build adds timestamped, content-free markers for activity touch coordinates/result; button bounds and enablement; UI_ON_CLICK; VIEWMODEL_ENTER; GUARD_ACCEPT/REJECT; IDENTITY_ALLOCATED; PROCESSING_TRUE; JOB_ENTER; WORKER_ENTER/RETURN; RUNTIME_ENTER; PIPELINE_ACCEPT; STORE_ENTER/COMMITTED; RENDER_ENTER; and VIEWMODEL_CLEARED. The fixture additionally records its target geometry and state immediately after performClick.

These are diagnostic-only changes, preserved separately and removed before any final candidate. No guard, policy, delay, queue, dispatch, identity, or persistence behavior is changed by the instrumentation.

The decisive classification will use the last observed stage:
- physical action with no UI_ON_CLICK: submit action never fired;
- UI_ON_CLICK with GUARD_REJECT: action fired but existing state rule rejected;
- UI_ON_CLICK without VIEWMODEL_ENTER: callback delegation failure;
- GUARD_ACCEPT without JOB_ENTER: admission/launch boundary;
- JOB_ENTER without RUNTIME_ENTER: worker/dispatch boundary;
- RUNTIME_ENTER without PIPELINE_ACCEPT: runtime guard/disposition;
- committed/returned result with unchanged draft: presentation completion failure.

No classification is inferred from a timeout alone.
