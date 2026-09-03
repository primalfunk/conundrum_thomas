# CT-V2-00 immutable lineage register

This document records historical references only. It creates no source, binary, runtime, build, or data dependency on V1.

## Status vocabulary

- `ACCEPTED_CANONICAL`: explicit governing lineage accepted for its original V1 purpose.
- `ACCEPTED_MILESTONE`: explicitly accepted historical milestone, but not necessarily the latest V1 oracle.
- `QUALIFIED_NOT_ACCEPTED`: qualification evidence exists, but Principal acceptance must not be inferred.
- `RESEARCH_REFERENCE`: experimental evidence only; not a production or renderer admission.
- `DIRTY_UNACCEPTED_EVIDENCE`: uncommitted worktree evidence identified only by observed fingerprints; never a migration source.

Qualification, acceptance, research usefulness, and migration authorization are separate decisions.

## V2 origin

- Repository: `C:\Android Studio Projects\ConundrumThomasV2`
- Branch: `main`
- Remote: none at foundation creation
- Accepted starter audit digest: `eacbaa2ccdad22d8fa9ddf16cb946fccd4eb310899409d7ce6ce7753423d9aaa`
- Digest scope: 40 non-generated starter files, 114,491 bytes; sorted `relative/path|bytes|sha256` rows joined by LF and SHA-256 hashed; `.gradle`, `.idea`, `build`, and `.git` excluded
- Root commit: `33e3b601cf9dc535436c6874168dcb138208e1ae`
- Root tree: `d1cd0b15cbc78438dc44997be4e34aadcc3abc6e`
- Annotated root tag: `ct-v2-root-baseline`

The commit contains the 39 version-controlled starter files. `local.properties` contributed to the accepted starter audit digest but was deliberately excluded from Git as machine-local state.

## Historical V1 references

| Reference | Repository | Immutable identity | Status | Meaning |
|---|---|---|---|---|
| Desktop V1 oracle | `C:\Thomas\R003-AMD-Workspace` | commit `05283e82db26b1266dcb1eef58309d4202aec302`, tree `a3b56bd67e78f2cdd1324b1e57a35411a0252b55` | `ACCEPTED_CANONICAL` | Canonical desktop behavioral oracle for historical comparison only. |
| `ct-10-accepted` | `C:\Thomas\R003-AMD-Workspace` | tag/ref resolves to `f695243ff0cebb24c803b89d9b0ade9f9efd8be8` | `ACCEPTED_MILESTONE` | Last explicitly tagged accepted milestone discovered in the audit. |
| R010/R011 | `C:\Thomas\R010-Mobile-Model-Lab` | commit `d94278b6cf3853bab9b89b6b05db36f7002dd0be`, tree `25e6a1f21aa27c9a86f95807e85e195961bca5b7` | `RESEARCH_REFERENCE` | Mobile-model and role-decomposition research only. |
| R009D committed Android | `C:\Android Studio Projects\Conundrum Thomas Android` | commit `eb3efd8c318ef56d4cce9e2e12c9ffd4b973358d`, tree `7f0f189e2fa8f83aa1964259a34600e020be836a` | `QUALIFIED_NOT_ACCEPTED` | Last committed native Android boundary. Qualification does not imply Principal acceptance. |

The desktop commit was verified directly from the audited repository as the 40-hex identity `05283e82db26b1266dcb1eef58309d4202aec302`. The implementation directive's rendered `05283e82db26b1266dc1eef58309d4202aec302` omits one `b`; it is retained in the machine-readable record as the requested spelling and resolved to the verified commit above.

## R009E dirty-state evidence

R009E is `DIRTY_UNACCEPTED_EVIDENCE`. It is not a commit, accepted lineage, dependency, or copy source.

- Base commit: `eb3efd8c318ef56d4cce9e2e12c9ffd4b973358d`
- Observed tracked state: 8 modified files
- Observed untracked state: 5 files
- Observed diff statistic: 695 insertions, 176 deletions
- Tracked-diff Git blob SHA-1: `4c1cbc92d7fb6d01bd18bbcceaa9db57d82f8ace`
- Untracked evidence fingerprints:
  - `ba2a7aa00bb9c2a30f50905ccb814d4a8a50821185d21e4ef0138b1e6a4e6d09` — `app/src/androidTest/java/com/conundrum/thomas/ui/ThomasConversationControllerInstrumentedTest.kt`
  - `0ba691b1244a09b2c3d20a26524d928fcbdf9182ab54d8b1c65fed9714e80b31` — `app/src/main/java/com/conundrum/thomas/speech/AndroidSpeechInputController.kt`
  - `ab34bdd9c28086fa734ac95de9ca180e8ce41a7b44baca6f75dbf0c38b302ad6` — `app/src/main/java/com/conundrum/thomas/speech/SpeechInputContracts.kt`
  - `30fc8ac16a4f4bf34ef1b38399f2dd5bf8e2bb2d41f274e1d94f0dbd326bac93` — `app/src/test/java/com/conundrum/thomas/speech/SpeechInputSessionTest.kt`
  - `3cb61224224c3aa2bc3e7a7d2f1a74fd2685af0528690088687c5ddf07addf7e` — `docs/CT-R009E-QUALIFICATION.md`

No R009E file may enter V2. Its future use, if any, first requires an accepted immutable source and a separately approved migration-register transition.

## Historical models

R007, R008, R010, R011, and every other V1 model or adapter remain historical/research candidates. None is admitted as a V2 renderer. Future admission requires renderer-specific fidelity and therapeutic-policy-leakage qualification and an explicit approval record.
