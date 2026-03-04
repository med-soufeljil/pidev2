package org.soa.tp1.pi_dev_s2.service;

import org.soa.tp1.pi_dev_s2.dao.UtilisateurDAO;
import org.soa.tp1.pi_dev_s2.model.Utilisateur;

import java.sql.SQLException;

public class AuthService {

    private final UtilisateurDAO utilisateurDAO;

    public AuthService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    // ── Login classique ──────────────────────
    public Utilisateur login(String email, String password) {
        Utilisateur user = utilisateurDAO.findByEmail(email);
        if (user != null && user.getMotDePasse().equals(password)) {
            return user;
        }
        return null;
    }

    // ── Trouver par email ────────────────────
    public Utilisateur getUtilisateur(String email) {
        return utilisateurDAO.findByEmail(email);
    }

    // ── Google OAuth : créer ou récupérer ────
    public Utilisateur findOrCreateByGoogle(String email, String fullName) {
        Utilisateur user = utilisateurDAO.findByEmail(email);
        if (user == null) {
            // Séparer nom/prénom depuis le fullName Google
            String[] parts  = fullName.split(" ", 2);
            String nom      = parts[0];
            String prenom   = parts.length > 1 ? parts[1] : "";

            user = new Utilisateur();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setMotDePasse("");   // Pas de mot de passe pour comptes Google
            user.setRole("USER");

            try {
                utilisateurDAO.addUser(user);
                user = utilisateurDAO.findByEmail(email); // Recharger avec l'id
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return user;
    }
}