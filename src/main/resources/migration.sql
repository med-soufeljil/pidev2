-- ============================================================
-- Migration : personne -> utilisateur
-- Base : congee (MySQL)
-- ============================================================

-- 1. Rename the table
RENAME TABLE personne TO utilisateur;

-- 2. Drop the 'age' column (no longer needed)
ALTER TABLE utilisateur DROP COLUMN age;

-- 3. Add new columns
ALTER TABLE utilisateur
    ADD COLUMN mail     VARCHAR(100) NOT NULL DEFAULT '' AFTER prenom,
    ADD COLUMN role     ENUM('ADMIN','EMPLOYE') NOT NULL DEFAULT 'EMPLOYE' AFTER mail,
    ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '1234' AFTER role;

-- 4. Unique constraint on mail
ALTER TABLE utilisateur ADD UNIQUE (mail);

-- 5. Default ADMIN account (for testing)
INSERT INTO utilisateur (nom, prenom, mail, role, password)
    VALUES ('Admin', 'Super', 'admin@app.com', 'ADMIN', 'admin123')
    ON DUPLICATE KEY UPDATE id = id;

-- 6. Sample employee account (for testing)
INSERT INTO utilisateur (nom, prenom, mail, role, password)
    VALUES ('Dupont', 'Jean', 'jean@app.com', 'EMPLOYE', 'pass123')
    ON DUPLICATE KEY UPDATE id = id;

-- ============================================================
-- Verification : show the table
-- ============================================================
SELECT * FROM utilisateur;
