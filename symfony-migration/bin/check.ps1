$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

Get-ChildItem -Path src -Recurse -Filter *.php | ForEach-Object {
    php -l $_.FullName
}

Write-Host "[check] lint PHP OK"
