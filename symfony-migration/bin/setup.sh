#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -f .env ] && [ -f .env.example ]; then
  cp .env.example .env
  echo "[setup] .env créé depuis .env.example"
fi

if [ ! -d vendor ]; then
  echo "[setup] installation des dépendances composer..."
  composer install
else
  echo "[setup] vendor existe déjà, skip composer install"
fi

echo "[setup] test connexion DB (même DB que Java)..."
php bin/console doctrine:query:sql "SELECT 1"

echo "[setup] synchronisation schéma sur DB Java..."
php bin/console doctrine:schema:update --complete --force

echo "[setup] OK"
