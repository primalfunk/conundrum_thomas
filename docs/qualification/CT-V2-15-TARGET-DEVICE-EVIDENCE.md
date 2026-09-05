# CT-V2-15 target-device evidence

## Device

| Field | Observed value |
| --- | --- |
| Manufacturer / model | TCL 9491G |
| Android | 15 |
| API | 35 |
| ABI | arm64-v8a |
| Application | `com.conundrum.thomas.v2`, version 1.0 (1) |
| Qualification date | 2026-09-04, America/Los_Angeles |
| Fixture class | fixed synthetic, non-identifying |

## Mandatory persistence sequence

The following sequence ran against the physical device, not an emulator:

1. installed the debug app and instrumentation APK;
2. reset the app-controlled corpus;
3. committed one synthetic Journal source with `NO_RESPONSE`;
4. verified the ciphertext artifact existed in `noBackupFilesDir` and did not
   contain the plaintext fixture;
5. closed and reopened the production root and matched revision and canonical
   logical digest;
6. rebooted the device;
7. unlocked the device and re-authorized ADB;
8. updated the APKs in place without clearing application data;
9. reopened and matched the same revision, digest, source count, and Journal
   provenance;
10. created and authenticated a recovery-key protected backup;
11. reset and restored into an empty target and matched the original digest;
12. destroyed the primary KeyStore key and observed fail-closed unavailable
    state rather than invented history;
13. completed full reset and verified no corpus remained.

Android application flags confirmed `FLAG_ALLOW_BACKUP` absent. The protected
artifact path was under the application's `noBackupFilesDir`. The AndroidKeyStore
observation reported provider `AndroidKeyStore`, algorithm `AES`, and
`hardwareBacked=true` on this TCL. This observation applies only to this device
and key and is not generalized to other hardware.

## Application scenarios

Physical instrumentation also passed:

- all three mode controls are explicit;
- typed Journal commit with `NO_RESPONSE` creates a user artifact but no Thomas
  artifact;
- no speech implementation is offered as available;
- data-custody controls are present;
- Journal -> Therapy recall preserves Journal provenance;
- governed Biographer target/question/answer capture;
- Therapy with and without longitudinal support;
- current-turn safety preemption without historical authority;
- correction to a new source revision changes current retrieval;
- privacy excludes the source from ordinary recall;
- deletion removes the source and its future influence.

## Performance observations

Three synthetic samples were recorded on the target. These are observations,
not an SLA.

| Production operation | Median ms | Worst ms |
| --- | ---: | ---: |
| Store open / restart recovery | 16.787 | 17.103 |
| Journal commit with silence | 127.513 | 210.983 |
| Journal deterministic render | 476.071 | 602.100 |
| Biographer governed turn | 62.498 | 63.373 |
| Therapy without memory | 1291.229 | 1520.619 |
| Therapy with explicit memory | 2195.030 | 2325.097 |
| User export generation | 30.397 | 32.516 |
| Protected backup | 4.923 | 6.168 |
| Validated reset/restore | 138.184 | 148.177 |

Three cold starts measured with Android Activity Manager `am start -W` produced
total times of 1480 ms, 1459 ms, and 1515 ms (median 1480 ms, worst 1515 ms).

## Speech disposition

STT and TTS were not admitted and therefore were not device-qualified. The app
requests no microphone permission and identifies speech as unavailable. This
preserves typed input and validated visible output without making unsupported
privacy, offline-recognition, or TTS-finality claims.

## End state

The performance fixture performed a final full reset. No synthetic corpus or
recovery key was intentionally left in app-controlled personal storage.
