# SmartHire Symfony Translation (même projet, même DB)

Tu as demandé **le même projet** en Symfony, sans supprimer le Java.
C'est ce qui est fait:
- Java reste inchangé à la racine.
- Symfony est dans `symfony-migration/`.
- Même base de données que Java: `pidev` MySQL localhost.

## Base de données partagée avec Java
Référence Java: `src/main/java/utils/MyDatabase.java`
- URL: `jdbc:mysql://localhost:3306/pidev`
- USER: `root`
- PASSWORD: vide

Symfony utilise exactement ces valeurs via `.env`:
- `DATABASE_URL="mysql://root:@127.0.0.1:3306/pidev?serverVersion=8.0&charset=utf8mb4"`

## Lancer le projet Symfony (fonctionnel)
Depuis la racine du repo:

```bash
cd symfony-migration
```

### Linux / macOS / Git Bash
```bash
./bin/setup.sh
./bin/run.sh
```

### Windows PowerShell
```powershell
cd symfony-migration
Set-ExecutionPolicy -Scope Process Bypass
.\bin\setup.ps1
.\bin\run.ps1
```

Puis ouvrir: `http://127.0.0.1:8000`

## Ce que fait setup
- installe les dépendances (`composer install`)
- teste la connexion sur la DB Java (`SELECT 1`)
- synchronise le schéma Doctrine sur la même DB (`doctrine:schema:update --complete --force`)

## Modules traduits
- Candidats
- Offres
- Recrutements
- Réunions
- Dashboard

Tous les modules ont CRUD: ajouter / modifier / supprimer.

## Important
Ne pas lancer `composer install` à la racine Java.
Il faut être dans `symfony-migration/`.
