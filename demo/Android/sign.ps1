#!/usr/bin/env pwsh
[CmdletBinding()]
param (
    [Parameter(Mandatory)]
    [string]
    $InstrumentedApp,
    [Parameter(Mandatory)]
    [string]
    $Key,
    [Parameter()]
    [AllowEmptyString()]
    [string]
    $KeyStorePass = "android"
)

$Script:ErrorActionPreference = "Stop"

# Get path to the Android SDK
$androidHome = ''
if ($env:ANDROID_HOME) {
    $androidHome = $env:ANDROID_HOME
}
if (!($androidHome) -and ($IsWindows -or [System.Environment]::OSVersion.Platform -eq 'Win32NT')) {
    $androidHome = Join-Path $env:LOCALAPPDATA 'Android/Sdk'
}
if (!(Test-Path $androidHome)) {
    throw "Can't find Android SDK, please set ANDROID_HOME environment variable"
}

# Get path to the Android SDK build-tools
$buildToolsDir = Join-Path -Resolve $androidHome 'build-tools'
$buildTools = Get-ChildItem $buildToolsDir | Sort-Object CreationTime -Descending | Select-Object -First 1
$buildTools = $buildTools.FullName
if (!($buildTools)) {
    throw "Can't find build-tools, please install Android SDK"
}
Write-Host "Using build tools: $buildTools"

# Get path to utilities
$zipAlign = Join-Path $buildTools 'zipalign'
$apkSigner = Join-Path $buildTools 'apksigner'

# Get a temporary file to store the signed app
$tmpFile = [System.IO.Path]::ChangeExtension([System.IO.Path]::GetTempFileName(), 'apk')

# Align the app
& $zipalign -f 4 $InstrumentedApp $tmpFile
if ($LASTEXITCODE -ne 0) {
    throw "Failed to align the app"
}

# Sign the app
$passOpt = @()
if ($KeyStorePass) {
    $passOpt = @('--ks-pass', "pass:$KeyStorePass")
}
& $apkSigner sign --ks $Key @passOpt $tmpFile
if ($LASTEXITCODE -ne 0) {
    throw "Failed to sign the app"
}

# Copy the signed app to the original location
Copy-Item $tmpFile $InstrumentedApp
Remove-Item $tmpFile

Write-Output "The app is signed and located in $InstrumentedApp"
