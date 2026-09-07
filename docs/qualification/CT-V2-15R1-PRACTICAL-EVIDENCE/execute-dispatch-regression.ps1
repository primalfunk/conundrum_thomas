param([ValidatePattern('^run-[0-9]{2}$')][string]$Run)
$ErrorActionPreference = 'Stop'
$adb = 'C:\Users\Jared\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$aapt = 'C:\Users\Jared\AppData\Local\Android\Sdk\build-tools\36.0.0\aapt2.exe'
$serial = 'BC9424B4E3C5002'
$canonical = 'com.conundrum.thomas.v2'
$fixture = 'com.conundrum.thomas.v2.ctv215r1fixture'
$fixtureTest = 'com.conundrum.thomas.v2.ctv215r1fixture.test'
$destination = Join-Path (Get-Location).Path ('out/ct-v2-15r1-practical/' + $Run)
if (Test-Path -LiteralPath $destination) { throw 'Preserve existing attempt; use a new run directory' }
if ((& $adb -s $serial get-state).Trim() -ne 'device') { throw 'Device unauthorized or unavailable' }
if (@(& $adb -s $serial shell pm list packages $fixture).Count -ne 0) { throw 'Disposable fixture must initially be absent' }
New-Item -ItemType Directory -Path $destination | Out-Null
& $adb devices -l | Set-Content -Encoding UTF8 (Join-Path $destination 'devices.txt')
$canonicalUid = (& $adb -s $serial shell run-as $canonical id -u).Trim()
if ($canonicalUid -notmatch '^\d+$') { throw 'Original UID unavailable' }

function Capture-Custody([string]$Suffix) {
    $files = @(& $adb -s $serial shell run-as $canonical find . -type f | Sort-Object)
    if ($LASTEXITCODE -ne 0 -or $files.Count -eq 0 -or @($files | Where-Object { $_ -notmatch '^\./[A-Za-z0-9_./-]+$' }).Count -gt 0) { throw 'Canonical inventory unavailable or unexpected' }
    $files | Set-Content -Encoding UTF8 (Join-Path $destination ('canonical-inventory-' + $Suffix + '.txt'))
    & $adb -s $serial shell run-as $canonical sha256sum @files | Set-Content -Encoding UTF8 (Join-Path $destination ('canonical-files-' + $Suffix + '.sha256'))
    if ($LASTEXITCODE -ne 0) { throw 'Canonical hash inspection failed' }
    & $adb -s $serial shell run-as $canonical stat -c '%n:%s:%Y:%i' @files | Set-Content -Encoding UTF8 (Join-Path $destination ('canonical-files-' + $Suffix + '.stat'))
    if ($LASTEXITCODE -ne 0) { throw 'Canonical stat inspection failed' }
    & $adb -s $serial shell dumpsys package $canonical | Select-String 'appId=|codePath=|versionCode=|versionName=|dataDir=|firstInstallTime=|lastUpdateTime=|signatures=' | Set-Content -Encoding UTF8 (Join-Path $destination ('canonical-package-' + $Suffix + '.txt'))
    $apkPath = ((& $adb -s $serial shell pm path $canonical) -replace '^package:', '').Trim()
    if ($apkPath -notmatch '^/data/app/[A-Za-z0-9_/=+.~-]+/base\.apk$') { throw 'Unexpected original APK path' }
    & $adb -s $serial shell sha256sum $apkPath | Set-Content -Encoding UTF8 (Join-Path $destination ('canonical-apk-' + $Suffix + '.sha256'))
    if ($LASTEXITCODE -ne 0) { throw 'Canonical APK inspection failed' }
}
Capture-Custody 'before'
@(& $adb -s $serial shell getprop ro.product.model
  & $adb -s $serial shell getprop ro.build.version.release
  & $adb -s $serial shell getprop ro.build.version.sdk
  & $adb -s $serial shell getprop ro.product.cpu.abi
  & $adb -s $serial shell getprop ro.build.fingerprint) | Set-Content -Encoding UTF8 (Join-Path $destination 'device.txt')
Copy-Item app/build/outputs/apk/debug/app-debug.apk (Join-Path $destination 'fixture.apk')
Copy-Item app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk (Join-Path $destination 'fixture-test.apk')
Copy-Item app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1ProductionDeviceInstrumentedTest.kt (Join-Path $destination 'device-fixture-source.kt')
Copy-Item app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1ProductionUiInstrumentedTest.kt (Join-Path $destination 'ui-fixture-source.kt')
$manifest = @(& $aapt dump xmltree (Join-Path $destination 'fixture.apk') --file AndroidManifest.xml)
$testManifest = @(& $aapt dump xmltree (Join-Path $destination 'fixture-test.apk') --file AndroidManifest.xml)
$manifestText = $manifest -join [char]10
$testManifestText = $testManifest -join [char]10
if ($manifestText -notmatch ('package="' + [regex]::Escape($fixture) + '"') -or $manifestText -match 'sharedUserId') { throw 'Fixture package isolation failed' }
if ($testManifestText -notmatch ('package="' + [regex]::Escape($fixtureTest) + '"') -or $testManifestText -notmatch ('targetPackage.*="' + [regex]::Escape($fixture) + '"') -or $testManifestText -match 'sharedUserId') { throw 'Instrumentation isolation failed' }
if ($manifestText -match 'authorities.*="com\.conundrum\.thomas\.v2\.(?!ctv215r1fixture\.)') { throw 'Provider identity is not isolated' }
$manifest | Set-Content -Encoding UTF8 (Join-Path $destination 'fixture-manifest.txt')
$testManifest | Set-Content -Encoding UTF8 (Join-Path $destination 'fixture-test-manifest.txt')
@('fixture.apk','fixture-test.apk') | ForEach-Object {
    $path = Join-Path $destination $_
    [pscustomobject]@{Name=$_;Bytes=(Get-Item $path).Length;SHA256=(Get-FileHash $path -Algorithm SHA256).Hash.ToLowerInvariant()}
} | ConvertTo-Json | Set-Content -Encoding UTF8 (Join-Path $destination 'fixture-artifacts.json')
Copy-Item app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1DispatchInstrumentedTest.kt (Join-Path $destination 'dispatch-fixture-source.kt')
Copy-Item app/src/androidTest/java/com/conundrum/thomas/v2/UiSubmissionDriver.kt (Join-Path $destination 'submission-driver-source.kt')
$fixtureUid = $null
$physicalPassed = $false
try {
    & $adb -s $serial install (Join-Path $destination 'fixture.apk') > (Join-Path $destination 'install-fixture.txt') 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'Fixture install failed' }
    & $adb -s $serial install (Join-Path $destination 'fixture-test.apk') > (Join-Path $destination 'install-test.txt') 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'Test install failed' }
    $fixtureUid = (& $adb -s $serial shell run-as $fixture id -u).Trim()
    if ($fixtureUid -notmatch '^\d+$' -or $fixtureUid -eq $canonicalUid) { throw 'Independent UID/key namespace not established' }
    @('canonicalUid=' + $canonicalUid; 'fixtureUid=' + $fixtureUid; 'Key namespace=Android UID scoped; alias text=ct-v2-14.personal-data.primary.v1') | Set-Content -Encoding UTF8 (Join-Path $destination 'uid-isolation.txt')
    & $adb -s $serial shell dumpsys package $fixture | Select-String 'appId=|codePath=|versionCode=|versionName=|dataDir=|firstInstallTime=|lastUpdateTime=' | Set-Content -Encoding UTF8 (Join-Path $destination 'fixture-package.txt')
    $methods = @(
        @('g','com.conundrum.thomas.v2.CTV215R1DispatchInstrumentedTest#keyboardMovementCannotStrandAnIntendedSubmission'),
        @('f','com.conundrum.thomas.v2.CTV215R1DispatchInstrumentedTest#twentyConsecutiveTouchSubmissionsCrossPresentationAdmissionExactlyOnce'),
        @('h','com.conundrum.thomas.v2.CTV215R1DispatchInstrumentedTest#twentySubmittedTurnsSurviveProcessReopen')
    )
    foreach ($method in $methods) {
        if ($method[0] -ne 'a') { & $adb -s $serial shell am force-stop $fixture }
        $log = Join-Path $destination ('instrumentation-' + $method[0] + '.txt')
        & $adb -s $serial shell am instrument -w -r -e canonicalUid $canonicalUid -e class $method[1] "$fixtureTest/androidx.test.runner.AndroidJUnitRunner" > $log 2>&1
        Get-Content -Encoding UTF8 $log -Tail 20
        if ([IO.File]::ReadAllText($log) -notmatch 'OK \(1 test\)') { throw ('PHYSICAL_METHOD_FAILED=' + $method[0]) }
        Write-Output ('PHYSICAL_METHOD_PASSED=' + $method[0])
    }
    $physicalPassed = $true
} finally {
    if ($fixtureUid -match '^\d+$' -and $fixtureUid -ne $canonicalUid) {
        & $adb -s $serial logcat -d "--uid=$fixtureUid" -v threadtime > (Join-Path $destination 'fixture-logcat.txt') 2>&1
    }
    foreach ($package in @($fixtureTest, $fixture)) {
        if (@(& $adb -s $serial shell pm list packages $package) -contains ('package:' + $package)) {
            & $adb -s $serial uninstall $package > (Join-Path $destination ('cleanup-' + $package + '.txt')) 2>&1
            if ($LASTEXITCODE -ne 0) { throw 'Disposable cleanup failed' }
        }
    }
    Capture-Custody 'after'
    foreach ($stem in @('canonical-inventory','canonical-files','canonical-package','canonical-apk')) {
        $extensions = switch ($stem) { 'canonical-files' { @('.sha256','.stat') }; 'canonical-apk' { @('.sha256') }; default { @('.txt') } }
        foreach ($extension in $extensions) {
            if (Compare-Object (Get-Content -Encoding UTF8 (Join-Path $destination ($stem + '-before' + $extension))) (Get-Content -Encoding UTF8 (Join-Path $destination ($stem + '-after' + $extension)))) { throw ('CUSTODY_CHANGED=' + $stem + $extension) }
        }
    }
    $remaining = @(& $adb -s $serial shell pm list packages $fixture)
    if ($remaining.Count -ne 0) { throw 'Disposable package remains' }
    @('CANONICAL_CUSTODY_UNCHANGED=true'; 'DISPOSABLE_PACKAGES_REMAINING=0'; 'PHYSICAL_ALL_METHODS_PASSED=' + $physicalPassed) | Set-Content -Encoding UTF8 (Join-Path $destination 'final-disposition.txt')
}
