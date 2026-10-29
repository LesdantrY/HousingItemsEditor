param([string[]]$Tasks = @('build'))
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = Join-Path $PSScriptRoot '.tooling\jdk'
$env:GRADLE_USER_HOME = Join-Path $PSScriptRoot '.tooling\gradle-home'
& (Join-Path $PSScriptRoot 'gradlew.bat') @Tasks --console=plain --no-daemon
exit $LASTEXITCODE
