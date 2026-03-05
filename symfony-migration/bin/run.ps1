$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

if (!(Test-Path "vendor")) {
    Write-Host "[run] dépendances manquantes. Lance d'abord: .\\bin\\setup.ps1"
    exit 1
}

php -S 127.0.0.1:8000 -t public public/index.php
