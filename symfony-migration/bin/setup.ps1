$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

if (!(Test-Path ".env") -and (Test-Path ".env.example")) {
    Copy-Item ".env.example" ".env"
    Write-Host "[setup] .env créé depuis .env.example"
}

if (!(Test-Path "vendor")) {
    Write-Host "[setup] installation des dépendances composer..."
    composer install
}
else {
    Write-Host "[setup] vendor existe déjà, skip composer install"
}

Write-Host "[setup] test connexion DB (même DB que Java)..."
php bin/console doctrine:query:sql "SELECT 1"

Write-Host "[setup] synchronisation schéma sur DB Java..."
php bin/console doctrine:schema:update --complete --force

Write-Host "[setup] OK"
