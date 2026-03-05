# SmartHire Symfony Migration (base)

Ce dossier contient la migration Symfony de l'application Java SmartHire (Candidat, Offre, Recrutement, Réunion, Dashboard), avec une structure prête à exécuter dans VS Code.

## Important
Le projet Symfony est dans **`symfony-migration/`**.
Si vous lancez `composer install` à la racine (`Esprit-PIDEV-3A11-2026-SmartHire`), c'est normal d'avoir l'erreur "composer.json introuvable".

## 1) Prérequis
- PHP 8.2+
- Composer 2+
- SQLite (par défaut) ou MySQL si vous changez `DATABASE_URL`

## 2) Ouvrir le bon dossier dans VS Code
Option A (recommandée): ouvrir directement le dossier `symfony-migration/`.

Option B: rester à la racine, puis faire:

```bash
cd symfony-migration
```

## 3) Installation rapide

### Linux / macOS / Git Bash
```bash
./bin/setup.sh
```

### Windows PowerShell
```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\bin\setup.ps1
```

## 4) Lancer l'application

### Linux / macOS / Git Bash
```bash
./bin/run.sh
```

### Windows PowerShell
```powershell
.\bin\run.ps1
```

Puis ouvrir: `http://127.0.0.1:8000`

## 5) Tester rapidement

### Test technique (lint PHP)
Linux/macOS:
```bash
./bin/check.sh
```

Windows PowerShell:
```powershell
.\bin\check.ps1
```

### Test fonctionnel manuel
1. Aller sur Dashboard.
2. Créer un candidat (`/candidats/new`).
3. Créer une offre (`/offres/new`).
4. Créer un recrutement (`/recrutements/new`).
5. Créer une réunion (`/reunions/new`).
6. Vérifier les listes et les compteurs dashboard.

## 6) Dépannage rapide
- **Erreur `composer.json file not found`**: vous n'êtes pas dans `symfony-migration/`.
- **Erreur `./bin/setup.sh not recognized` sous PowerShell**: utilisez les scripts `.ps1`.
- Si `composer install` échoue (proxy/tunnel/403): exécuter dans une machine avec accès internet ou configurer le proxy Composer.

## Correspondance Java -> Symfony
- `models/*` -> `src/Entity/*`
- `services/*` CRUD SQL -> Doctrine ORM (`Repository` + `EntityManager`)
- `controllers/*` JavaFX -> `src/Controller/*`
- `*.fxml` + `style.css` -> `templates/*` + `public/styles/app.css`

## État actuel
- ✅ CRUD base: Candidat, Offre, Recrutement, Réunion
- ✅ Dashboard compteurs
- ✅ UI homogène (sidebar, cards, tableaux)
- ⏳ À compléter ensuite: email, PDF, IA, API externes
