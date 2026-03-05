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

echo "[setup] création/mise à jour du schéma Doctrine..."
php bin/console doctrine:database:create --if-not-exists
php bin/console doctrine:schema:update --force

echo "[setup] OK"
