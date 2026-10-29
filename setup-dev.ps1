$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$tooling = Join-Path $root '.tooling'
New-Item -ItemType Directory -Force $tooling | Out-Null
$manifest = Invoke-RestMethod 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'
$releases = @($manifest.versions | Where-Object { $_.type -eq 'release' -and [datetime]$_.releaseTime -ge [datetime]'2025-12-09' })
$releases | Select-Object id,releaseTime | ConvertTo-Json | Set-Content (Join-Path $tooling 'releases.json')
$loader = Invoke-RestMethod 'https://meta.fabricmc.net/v2/versions/loader'
$loader[0].version | Set-Content (Join-Path $tooling 'loader.txt')
Invoke-WebRequest 'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml' -OutFile (Join-Path $tooling 'fabric-api.xml')
Invoke-WebRequest 'https://maven.fabricmc.net/net/fabricmc/fabric-loom/maven-metadata.xml' -OutFile (Join-Path $tooling 'loom.xml')
if (-not (Test-Path (Join-Path $tooling 'jdk'))) {
    $jdk = Invoke-RestMethod 'https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=x64&image_type=jdk&os=windows'
    Invoke-WebRequest $jdk[0].binary.package.link -OutFile (Join-Path $tooling 'jdk.zip')
    $actualHash = (Get-FileHash (Join-Path $tooling 'jdk.zip') -Algorithm SHA256).Hash
    if ($actualHash -ne $jdk[0].binary.package.checksum) { throw 'JDK download checksum mismatch.' }
    Expand-Archive (Join-Path $tooling 'jdk.zip') -DestinationPath (Join-Path $tooling 'java') -Force
    $dir = Get-ChildItem (Join-Path $tooling 'java') -Directory | Select-Object -First 1
    Move-Item -LiteralPath $dir.FullName -Destination (Join-Path $tooling 'jdk')
}
Get-Content (Join-Path $tooling 'releases.json')
Write-Output 'Development tools ready.'
