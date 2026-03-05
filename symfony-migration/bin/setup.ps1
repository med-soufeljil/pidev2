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

Write-Host "[setup] création/mise à jour du schéma Doctrine..."
php bin/console doctrine:database:create --if-not-exists
php bin/console doctrine:schema:update --force

Write-Host "[setup] OK"
