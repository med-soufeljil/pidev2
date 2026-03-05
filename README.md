# SmartHire (Symfony)

Le projet est maintenant **100% Symfony**.

## Stack
- Symfony 7
- Doctrine ORM
- Twig
- MySQL (`pidev`)

## Base de données
`.env` est configuré pour:

```env
DATABASE_URL="mysql://root:@127.0.0.1:3306/pidev?serverVersion=8.0&charset=utf8mb4"
```

## Lancer avec terminal VS Code

### Linux / macOS / Git Bash
```bash
./bin/setup.sh
./bin/run.sh
```

### Windows PowerShell
```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\bin\setup.ps1
.\bin\run.ps1
```

Puis ouvrir: `http://127.0.0.1:8000`

## Vérification rapide

### Linux / macOS / Git Bash
```bash
./bin/check.sh
```

### Windows PowerShell
```powershell
.\bin\check.ps1
```

## Modules
- Dashboard
- Candidats (CRUD)
- Offres (CRUD)
- Recrutements (CRUD)
- Réunions (CRUD)
