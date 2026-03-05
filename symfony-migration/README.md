# SmartHire Symfony Migration (base)

Ce dossier contient la migration Symfony de l'application Java SmartHire (Candidat, Offre, Recrutement, Réunion, Dashboard), avec une structure prête à exécuter dans VS Code.

## 1) Prérequis
- PHP 8.2+
- Composer 2+
- SQLite (par défaut) ou MySQL si vous changez `DATABASE_URL`

## 2) Ouvrir le projet dans VS Code
Ouvre **le dossier `symfony-migration/`** (pas la racine Java) dans VS Code.

## 3) Installation rapide (recommandée)
Dans le terminal VS Code:

```bash
./bin/setup.sh
```

Ce script fait automatiquement:
- copie `.env.example` -> `.env`
- `composer install`
- création de la base
- mise à jour du schéma Doctrine

## 4) Lancer l'application

```bash
./bin/run.sh
```

Puis ouvre: `http://127.0.0.1:8000`

## 5) Tester rapidement

### Test technique (lint PHP)
```bash
./bin/check.sh
```

### Test fonctionnel manuel
1. Aller sur Dashboard.
2. Créer un candidat (`/candidats/new`).
3. Créer une offre (`/offres/new`).
4. Créer un recrutement (`/recrutements/new`).
5. Créer une réunion (`/reunions/new`).
6. Vérifier les listes et les compteurs dashboard.

## 6) Si `composer install` échoue
Si vous voyez une erreur réseau (proxy/tunnel/403):
- exécuter dans une machine avec accès internet
- ou configurer le proxy Composer (`composer config -g ...`)

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
