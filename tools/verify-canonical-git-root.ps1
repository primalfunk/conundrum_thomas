[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$topLevel = (& git -C $projectRoot rev-parse --show-toplevel).Trim().Replace('/', '\')
$gitDirectory = (& git -C $projectRoot rev-parse --git-dir).Trim()
$inside = (& git -C $projectRoot rev-parse --is-inside-work-tree).Trim()

if ([System.IO.Path]::GetFullPath($topLevel) -ne [System.IO.Path]::GetFullPath($projectRoot)) {
    throw "Git top-level mismatch: $topLevel"
}
if ($gitDirectory -ne '.git') { throw "Canonical Git metadata must resolve through .git, found: $gitDirectory" }
if ($inside -ne 'true') { throw 'Project root is not inside the canonical Git work tree' }
if (-not (Test-Path -LiteralPath (Join-Path $projectRoot '.git') -PathType Container)) {
    throw 'Canonical .git directory is absent'
}

$staleNames = @('.git-work', '.git-temp', 'git-metadata-backup')
$stale = Get-ChildItem -LiteralPath $projectRoot -Force -Directory | Where-Object {
    $_.Name -like '.git-ct-v2-*' -or $_.Name -in $staleNames
}
if ($stale) { throw "Stale temporary Git metadata found: $($stale.Name -join ', ')" }

$vcsFile = Join-Path $projectRoot '.idea\vcs.xml'
if (Test-Path -LiteralPath $vcsFile) {
    $vcsText = Get-Content -LiteralPath $vcsFile -Raw
    if ($vcsText -notmatch '<mapping directory="\$PROJECT_DIR\$" vcs="Git"\s*/>') {
        throw 'Android Studio VCS mapping is not $PROJECT_DIR$ -> Git'
    }
}

Write-Output "CANONICAL_PROJECT_ROOT=$projectRoot"
Write-Output "CANONICAL_GIT_DIR=$gitDirectory"
Write-Output 'TEMPORARY_GIT_METADATA_COUNT=0'
Write-Output 'VCS_ROOT_ON_DISK_VALID=true'
