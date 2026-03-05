# SmartHire Symfony Migration (base)

Ce dossier contient une traduction **Symfony** de l'application Java (modules Candidat, Offre, Recrutement, Réunion + Dashboard) en gardant la même logique métier CRUD et une UI proche en sidebar + tableaux.

## Correspondance Java -> Symfony
- `models/*` -> `src/Entity/*`
- `services/*` (CRUD SQL) -> Doctrine ORM (`Repository` + `EntityManager`)
- `controllers/*` JavaFX -> `src/Controller/*` (routes web)
- `*.fxml` + `style.css` -> `templates/*` + `public/styles/app.css`

## Démarrage dans VS Code
1. Ouvrir le dossier `symfony-migration` dans VS Code.
2. Installer les dépendances:
   ```bash
   composer install
   ```
3. Copier l'environnement:
   ```bash
   cp .env.example .env
   ```
4. Créer la base + schéma:
   ```bash
   php bin/console doctrine:database:create
   php bin/console doctrine:schema:update --force
   ```
5. Lancer:
   ```bash
   symfony server:start
   # ou
   php -S 127.0.0.1:8000 -t public
   ```

## État de migration
- ✅ Structure Symfony prête.
- ✅ Entités alignées avec le schéma Java.
- ✅ Pages list/create pour les 4 modules.
- ✅ Dashboard avec compteurs.
- ⏳ Fonctions avancées (email, export PDF, IA scoring, API externes) à brancher dans des services Symfony dédiés.
