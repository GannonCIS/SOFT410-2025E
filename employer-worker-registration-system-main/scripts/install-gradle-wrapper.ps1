#!/usr/bin/env pwsh
Param(
    [string]$WrapperVersion = 'v8.10.2'
)

Write-Host "Creating gradle\wrapper directory..."
New-Item -ItemType Directory -Force -Path gradle\wrapper | Out-Null

$jarUrl = "https://raw.githubusercontent.com/gradle/gradle/$WrapperVersion/gradle/wrapper/gradle-wrapper.jar"
$outFile = "gradle\wrapper\gradle-wrapper.jar"

Write-Host "Downloading gradle-wrapper.jar from $jarUrl"
$downloadOk = $false
try {
    Invoke-WebRequest -Uri $jarUrl -OutFile $outFile -UseBasicParsing -ErrorAction Stop
    Write-Host "Downloaded gradle-wrapper.jar to $outFile (from GitHub)"
    $downloadOk = $true
} catch {
    Write-Host "GitHub download failed or returned 404 for $jarUrl"
}

# If GitHub download didn't work or file looks too small, try Maven Central as a fallback
if (-not $downloadOk -or (Test-Path -LiteralPath $outFile -PathType Leaf -ErrorAction SilentlyContinue -ErrorVariable ev) ) {
    $sizeOk = $false
    if (Test-Path -LiteralPath $outFile) {
        $len = (Get-Item -LiteralPath $outFile).Length
        if ($len -gt 2000) { $sizeOk = $true }
    }
    if (-not $downloadOk -or -not $sizeOk) {
        # Build Maven Central URL (remove leading 'v' if present)
        $versionNumeric = $WrapperVersion -replace '^v',''
        $mavenUrl = "https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/$versionNumeric/gradle-wrapper-$versionNumeric.jar"
        Write-Host "Attempting fallback download from Maven Central: $mavenUrl"
        try {
            Invoke-WebRequest -Uri $mavenUrl -OutFile $outFile -UseBasicParsing -ErrorAction Stop
            $len = (Get-Item -LiteralPath $outFile).Length
            Write-Host "Downloaded gradle-wrapper.jar to $outFile (from Maven Central) -- $len bytes"
            $downloadOk = $true
        } catch {
            Write-Host "Failed to download gradle-wrapper.jar from Maven Central: $mavenUrl"
            Write-Host "Error: $_"
        }
    }
}

if (-not $downloadOk) {
    Write-Host "Failed to download gradle-wrapper.jar from both GitHub and Maven Central."
    Write-Host "You can alternatively generate the wrapper locally after installing Gradle, or download the wrapper jar manually." 
    # As a last-resort fallback, try downloading the Gradle distribution zip and extracting the wrapper jar from it
    try {
        Write-Host "Attempting last-resort download: Gradle distribution zip (this may be large)."
        $distUrl = "https://services.gradle.org/distributions/gradle-$versionNumeric-bin.zip"
        $zipTmp = Join-Path $PWD "gradle-wrapper-dist.zip"
        Invoke-WebRequest -Uri $distUrl -OutFile $zipTmp -UseBasicParsing -ErrorAction Stop
        $tmpDir = Join-Path $PWD "gradle-wrapper-tmp"
        if (Test-Path $tmpDir) { Remove-Item -LiteralPath $tmpDir -Recurse -Force }
        Expand-Archive -Path $zipTmp -DestinationPath $tmpDir
        $jarCandidate = Get-ChildItem -Path $tmpDir -Recurse -Filter 'gradle-wrapper.jar' | Select-Object -First 1
        if ($null -ne $jarCandidate) {
            Copy-Item -LiteralPath $jarCandidate.FullName -Destination $outFile -Force
            $len = (Get-Item -LiteralPath $outFile).Length
            Write-Host "Extracted gradle-wrapper.jar from distribution to $outFile ($len bytes)"
            $downloadOk = $true
        } else {
            Write-Host "Could not find gradle-wrapper.jar inside the distribution archive."
        }
        # cleanup
        Remove-Item -LiteralPath $zipTmp -Force -ErrorAction SilentlyContinue
        if (Test-Path $tmpDir) { Remove-Item -LiteralPath $tmpDir -Recurse -Force -ErrorAction SilentlyContinue }
    } catch {
        Write-Host "Last-resort distribution download/extract failed: $_"
    }
    if (-not $downloadOk) { exit 1 }
}

Write-Host "Setting execute permission for gradlew (Unix) if present..."
if (Test-Path './gradlew') {
    try { icacls ./gradlew /grant Everyone:RX } catch { }
}

Write-Host "Done. You should now be able to run .\gradlew.bat build (Windows) or ./gradlew build (Unix)."
