# CT-V2 existing Thomas model recovery and admission

Recorded for the V2 admission implementation. Model binaries remain external
governed artifacts and are not committed to V2.

## Recovery disposition

The historical lineage is recoverable. R007 selected `ct-r007-candidate-b-r16`
from the frozen Phi-4-mini-instruct base. R008 merged that exact adapter and
qualified the resulting GGUF matrix with llama.cpp b10621. R010/R011 later
studied a smaller student, but those records do not supersede the stronger
R007/R008 candidate for this admission.

| Candidate | Base / method | Artifact | Result | Disposition |
|---|---|---|---|---|
| R007 A r8 | Phi-4-mini-instruct, BF16 PEFT LoRA | adapter SHA `598395f7…5646748` | 54/60 protected raw acceptance; 8 quality failures | rejected in favor of B |
| R007 B r16 | Phi-4-mini-instruct, BF16 PEFT LoRA; adapter `cd3967b9…0113ad` | merged by R008 | 56/60 protected raw acceptance; 0 repairs; 1 known Biographer limitation | selected historical candidate |
| R008 Q8_0 | R007 B merged, GGUF | 4,084,610,784 bytes; SHA `6af1b4…7c6f4` | 45/47 raw; 2 replacements | qualified, not mobile recommendation |
| R008 Q6_K | R007 B merged, GGUF | 3,155,622,624 bytes; SHA `d8a98b…5cb95` | 46/47 raw; 1 replacement; 0 exact repeats | last accepted mobile recommendation |
| R008 Q4_K_M | R007 B merged, GGUF | 2,493,840,096 bytes; SHA `eca680…8c9e8` | 44/47 raw; 3 replacements | qualified comparison |
| R008 Q3_K_M | R007 B merged, GGUF | 2,121,464,544 bytes; SHA `747c9b…b687f7` | 45/47 raw; 2 replacements; broadened Biographer limitation | smallest behaviorally qualified fallback |
| R008 Q2_K | R007 B merged, GGUF | 1,733,753,568 bytes; SHA `df473f…e227c6` | 43/47 raw; 4 replacements; 2 repeats | rejected at quantization cliff |
| R010/R011 student | Qwen2.5-0.5B student | separate Q4_K_M artifact | later decomposed mobile study | not selected for this recovery |

The R007 Transformers/PEFT checkpoint and the R008 GGUF/mobile qualification
are distinct evidence. V2 admits the R008 Q6_K GGUF, not the adapter alone.

## Selected artifact and custody

- Model ID: `ct-r007-candidate-b-r16`
- Base: `microsoft/Phi-4-mini-instruct`, revision
  `cfbefacb99257ffa30c83adab238a50856ac3083`
- GGUF: `Thomas-CT-R007-Merged-Q6_K.gguf`
- Size: `3,155,622,624` bytes
- SHA-256: `d8a98b45c1c0e72ad63fe4a55105dbbc8c59bfa8910d37c0d0ff83d01125cb95`
- Quantization: `Q6_K`
- Historical host artifact path:
  `C:\Thomas\R003-AMD-Workspace\model-lab\models\exp-tml-r008\mobile\Thomas-CT-R007-Merged-Q6_K.gguf`
- V2 device custody path:
  `<app no-backup>/models/Thomas-CT-R007-Merged-Q6_K.gguf`
- Runtime: llama.cpp `b10621`, commit
  `c1d0e7a004015f23bc0233470b747b596f29b264`, CPU, `arm64-v8a`, 4,096
  context, 512 batch, four threads.

The provider accepts only this filename/size/hash tuple, verifies it before
JNI, requires the file to be in app-private no-backup model storage, and never
downloads or substitutes an artifact. Missing or corrupt custody produces an
observable provider failure and the governed deterministic fallback.

## Historical device evidence and remaining V2 gate

The prior R009B study mechanically loaded and exercised this exact artifact on
the TCL 9491G: load, streaming, cancellation, unload/reload, backgrounding,
and offline operation passed. It also recorded the hardware limitation: about
5.7 seconds TTFT for the short probe, about 2.45 tokens/s, and a 3,629-token
context that was cancelled at approximately 600 seconds. That is evidence of
runtime compatibility, not a V2 product-usability acceptance.

This checkout has no `adb` executable/device connection, so an exact V2 APK,
model staging, and current physical qualification cannot be claimed here.
