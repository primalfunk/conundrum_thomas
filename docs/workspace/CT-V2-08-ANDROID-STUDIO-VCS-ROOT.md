# CT-V2-08 Android Studio VCS root

The canonical project and Git root is `C:\Android Studio Projects\ConundrumThomasV2`. Its canonical metadata location is the ordinary `.git` directory directly beneath that root. Android Studio's local `.idea/vcs.xml`, when present, must map `$PROJECT_DIR$` to `Git`.

The CT-V2-07 qualification process temporarily renamed `.git` to `.git-ct-v2-07-work`. The metadata was restored, but Android Studio could retain an invalid-root warning from the interval in which `.git` was absent. At CT-V2-08 entry, `.git` and the local `$PROJECT_DIR$` mapping were already correct on disk; no destructive repair was necessary and `.idea` remains ignored.

From CT-V2-08 onward, phase work must never rename, move, hide, replace, temporarily relocate, or redirect the canonical `.git` directory. The tracked `tools/verify-canonical-git-root.ps1` check verifies the project root, `.git` resolution, optional IDE mapping, and absence of stale temporary metadata directories without hard-coding a username.

Because automated repository work cannot inspect the running Android Studio UI, the on-disk repair is qualified while visual warning removal requires a Principal IDE reload/restart confirmation:

`VCS_MAPPING_REPAIR_IMPLEMENTED`

`PRINCIPAL_IDE_RESTART_CONFIRMATION_REQUIRED`

## CT-V2-09 follow-up

CT-V2-09 re-ran the canonical-root script before implementation and final sealing. Git continued to report `C:/Android Studio Projects/ConundrumThomasV2` with `.git`, the project mapping remained `$PROJECT_DIR$` / `Git`, and temporary metadata count remained zero.

    VCS_ROOT_ON_DISK_VALID
    IDE_VISUAL_CONFIRMATION_AVAILABLE = false
    IDE_INVALID_VCS_WARNING_OBSERVED = unknown

The running IDE was not directly observable, so CT-V2-09 does not claim the prior warning visibly absent. `.git` was never relocated, hidden, substituted, or redirected.
