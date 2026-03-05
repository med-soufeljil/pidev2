# SmartHire Symfony Migration (base)

Cette migration **ne supprime rien** du projet Java. Le code Java reste à la racine, et la traduction Symfony est dans `symfony-migration/`.

## Objectif
Avoir le même projet SmartHire en Symfony avec:
- mêmes modules (Candidat, Offre, Recrutement, Réunion, Dashboard)
- même base de données (tables/colonnes Java conservées)
- interface web équivalente (navigation + CRUD complet: ajouter/modifier/supprimer)

## Important
Le projet Symfony est dans **`symfony-migration/`**.
Si vous lancez `composer install` à la racine (`Esprit-PIDEV-3A11-2026-SmartHire`), c'est normal d'avoir l'erreur "composer.json introuvable".

## 1) Prérequis
- PHP 8.2+
- Composer 2+
- SQLite (par défaut) ou MySQL si vous mettez l'URL de votre DB Java dans `DATABASE_URL`

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
1. Dashboard.
2. Candidats: ajouter / modifier / supprimer.
3. Offres: ajouter / modifier / supprimer.
4. Recrutements: ajouter / modifier / supprimer.
5. Réunions: ajouter / modifier / supprimer.
6. Vérifier que les données viennent de la même DB (mêmes tables Java).

## 6) Compatibilité base Java
Les entités Symfony mappent les mêmes tables/colonnes Java:
- `candidat(idCandidat, CIN, ai_analyse, ai_score, ... )`
- `offre(idOffre, nomOffre, type, competences, salaire)`
- `recrutement(idRec, idOffre, idCandidat)`
- `reunion(idReunion, idRH, idCandidat, date, link)`

## 7) Dépannage rapide
- **Erreur `composer.json file not found`**: vous n'êtes pas dans `symfony-migration/`.
- **Erreur `./bin/setup.sh not recognized` sous PowerShell**: utilisez les scripts `.ps1`.
- Si `composer install` échoue (proxy/tunnel/403): exécuter dans une machine avec accès internet ou configurer le proxy Composer.
