$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

function Run-Or-Fail([string]$cmd) {
    Write-Host "> $cmd"
    Invoke-Expression $cmd
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code $LASTEXITCODE: $cmd"
    }
}

if (!(Test-Path ".env") -and (Test-Path ".env.example")) {
    Copy-Item ".env.example" ".env"
    Write-Host "[setup] .env créé depuis .env.example"
}

if (!(Test-Path "vendor")) {
    Write-Host "[setup] installation des dépendances composer..."
    Run-Or-Fail "composer install"
}
else {
    Write-Host "[setup] vendor existe déjà, skip composer install"
}

Write-Host "[setup] test connexion DB (même DB que Java)..."
Run-Or-Fail 'php bin/console doctrine:query:sql "SELECT 1"'

Write-Host "[setup] synchronisation schéma sur DB Java..."
Run-Or-Fail "php bin/console doctrine:schema:update --complete --force"

Write-Host "[setup] OK"
