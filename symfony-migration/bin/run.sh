#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -d vendor ]; then
  echo "[run] dépendances manquantes. Lance d'abord: ./bin/setup.sh"
  exit 1
fi

php -S 127.0.0.1:8000 -t public public/index.php
