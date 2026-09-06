param([ValidatePattern('^run-[0-9]{2}$')][string]$PriorRun,[ValidatePattern('^run-[0-9]{2}$')][string]$Run)
$ErrorActionPreference='Stop'
$adb='C:\Users\Jared\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$aapt='C:\Users\Jared\AppData\Local\Android\Sdk\build-tools\36.0.0\aapt2.exe'
$serial='BC9424B4E3C5002'
$base=Join-Path $pwd 'out/ct-v2-15r1-rerun'
$prior=Join-Path $base $PriorRun
$next=Join-Path $base $Run
if(Test-Path -LiteralPath $next){throw 'Preserve existing attempt; destination must be new'}
$uid=(& $adb -s $serial shell run-as com.conundrum.thomas.v2.ctv215r1fixture id -u).Trim()
if($uid -notmatch '^\d+$' -or $uid -eq '10666'){throw 'Unexpected prior fixture UID'}
& $adb -s $serial logcat -d "--uid=$uid" -v threadtime > (Join-Path $prior 'fixture-logcat.txt') 2>&1
$files=@(& $adb -s $serial shell run-as com.conundrum.thomas.v2 find . -type f|Sort-Object)
if($LASTEXITCODE -ne 0 -or @($files|Where-Object {$_ -notmatch '^\./[A-Za-z0-9_./-]+$'}).Count -gt 0){throw 'Canonical inventory unavailable'}
$hashes=@(& $adb -s $serial shell run-as com.conundrum.thomas.v2 sha256sum @files)
$stats=@(& $adb -s $serial shell run-as com.conundrum.thomas.v2 stat -c '%n:%s:%Y:%i' @files)
if(Compare-Object (Get-Content (Join-Path $prior 'canonical-files-before.sha256')) $hashes){throw 'Original hash changed'}
if(Compare-Object (Get-Content (Join-Path $prior 'canonical-files-before.stat')) $stats){throw 'Original metadata changed'}
$hashes|Set-Content -Encoding UTF8 (Join-Path $prior 'canonical-files-after.sha256')
$stats|Set-Content -Encoding UTF8 (Join-Path $prior 'canonical-files-after.stat')
& $adb -s $serial uninstall com.conundrum.thomas.v2.ctv215r1fixture.test > (Join-Path $prior 'cleanup-test.txt') 2>&1
if($LASTEXITCODE -ne 0){throw 'Test cleanup failed'}
& $adb -s $serial uninstall com.conundrum.thomas.v2.ctv215r1fixture > (Join-Path $prior 'cleanup-fixture.txt') 2>&1
if($LASTEXITCODE -ne 0){throw 'Fixture cleanup failed'}
New-Item -ItemType Directory $next|Out-Null
$hashes|Set-Content -Encoding UTF8 (Join-Path $next 'canonical-files-before.sha256')
$stats|Set-Content -Encoding UTF8 (Join-Path $next 'canonical-files-before.stat')
& $adb -s $serial shell dumpsys package com.conundrum.thomas.v2 | Select-String 'appId=|codePath=|versionCode=|versionName=|dataDir=|firstInstallTime=|lastUpdateTime=|signatures=' | Set-Content -Encoding UTF8 (Join-Path $next 'canonical-package-before.txt')
$apkPath=((& $adb -s $serial shell pm path com.conundrum.thomas.v2)-replace '^package:','').Trim()
if($apkPath -notmatch '^/data/app/[A-Za-z0-9_/=+.~-]+/base\.apk$'){throw 'Unexpected APK path'}
& $adb -s $serial shell sha256sum $apkPath|Set-Content -Encoding UTF8 (Join-Path $next 'canonical-apk-before.sha256')
@(& $adb -s $serial shell getprop ro.product.model;& $adb -s $serial shell getprop ro.build.version.release;& $adb -s $serial shell getprop ro.build.version.sdk;& $adb -s $serial shell getprop ro.product.cpu.abi;& $adb -s $serial shell getprop ro.build.fingerprint)|Set-Content -Encoding UTF8 (Join-Path $next 'device.txt')
if(@(& $adb -s $serial shell pm list packages com.conundrum.thomas.v2.ctv215r1fixture).Count -gt 0){throw 'Fixture not absent'}
Copy-Item app/build/outputs/apk/debug/app-debug.apk (Join-Path $next 'fixture.apk')
Copy-Item app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk (Join-Path $next 'fixture-test.apk')
Copy-Item app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1ProductionDeviceInstrumentedTest.kt (Join-Path $next 'device-fixture-source.kt')
Copy-Item app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1ProductionUiInstrumentedTest.kt (Join-Path $next 'ui-fixture-source.kt')
$manifest=@(& $aapt dump xmltree (Join-Path $next 'fixture.apk') --file AndroidManifest.xml)
$testManifest=@(& $aapt dump xmltree (Join-Path $next 'fixture-test.apk') --file AndroidManifest.xml)
if(($manifest-join [Environment]::NewLine) -notmatch 'package="com.conundrum.thomas.v2.ctv215r1fixture"' -or ($manifest-join [Environment]::NewLine) -match 'sharedUserId'){throw 'Fixture identity failed'}
if(($testManifest-join [Environment]::NewLine) -notmatch 'targetPackage.*="com.conundrum.thomas.v2.ctv215r1fixture"' -or ($testManifest-join [Environment]::NewLine) -match 'sharedUserId'){throw 'Test identity failed'}
$manifest|Set-Content -Encoding UTF8 (Join-Path $next 'fixture-manifest.txt')
$testManifest|Set-Content -Encoding UTF8 (Join-Path $next 'fixture-test-manifest.txt')
@('fixture.apk','fixture-test.apk')|ForEach-Object{$p=Join-Path $next $_;[pscustomobject]@{Name=$_;Bytes=(Get-Item $p).Length;SHA256=(Get-FileHash $p -Algorithm SHA256).Hash.ToLower()}}|ConvertTo-Json|Set-Content -Encoding UTF8 (Join-Path $next 'fixture-artifacts.json')
& $adb -s $serial install (Join-Path $next 'fixture.apk') > (Join-Path $next 'install-fixture.txt') 2>&1
if($LASTEXITCODE -ne 0){throw 'Install failed'}
& $adb -s $serial install (Join-Path $next 'fixture-test.apk') > (Join-Path $next 'install-test.txt') 2>&1
if($LASTEXITCODE -ne 0){throw 'Test install failed'}
$uid=(& $adb -s $serial shell run-as com.conundrum.thomas.v2.ctv215r1fixture id -u).Trim()
if($uid -notmatch '^\d+$' -or $uid -eq '10666'){throw 'UID isolation failed'}
@('canonicalUid=10666';'fixtureUid='+$uid)|Set-Content -Encoding UTF8 (Join-Path $next 'uid-isolation.txt')
$methods=@(
 @('a','com.conundrum.thomas.v2.CTV215R1ProductionDeviceInstrumentedTest#aCompleteTypedProductionScenarios'),
 @('b','com.conundrum.thomas.v2.CTV215R1ProductionDeviceInstrumentedTest#bColdReopenDurabilityAndCoverage'),
 @('c','com.conundrum.thomas.v2.CTV215R1ProductionUiInstrumentedTest#cActualInputControlsAndSemanticProgression')
)
foreach($method in $methods){
 if($method[0] -ne 'a'){& $adb -s $serial shell am force-stop com.conundrum.thomas.v2.ctv215r1fixture}
 $log=Join-Path $next ('instrumentation-'+$method[0]+'.txt')
 & $adb -s $serial shell am instrument -w -r -e canonicalUid 10666 -e class $method[1] com.conundrum.thomas.v2.ctv215r1fixture.test/androidx.test.runner.AndroidJUnitRunner > $log 2>&1
 Get-Content $log -Tail 36
 if(([IO.File]::ReadAllText($log)) -notmatch 'OK \(1 test\)'){Write-Output ('PHYSICAL_METHOD_FAILED='+$method[0]);exit 1}
 Write-Output ('PHYSICAL_METHOD_PASSED='+$method[0])
}
