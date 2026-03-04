package org.soa.tp1.pi_dev_s2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.soa.tp1.pi_dev_s2.model.Utilisateur;
import org.soa.tp1.pi_dev_s2.service.UtilisateurService;


public class ProfileController {

    // ─── Header ──────────────────────────────────────────────────────────
    @FXML private Label lblAvatar;
    @FXML private Label lblNomComplet;
    @FXML private Label lblEmailHeader;
    @FXML private Label lblRoleBadge;

    // ─── Champs formulaire
    @FXML private TextField     txtNom;
    @FXML private TextField     txtPrenom;
    @FXML private TextField     txtEmail;

    // ─── Section mot de passe ────────────────────────────────────────────
    @FXML private VBox          panelMdp;
    @FXML private Button        btnToggleMdp;
    @FXML private PasswordField txtPasswordActuel;
    @FXML private PasswordField txtPasswordNouveau;
    @FXML private PasswordField txtPasswordConfirm;

    // ─── Feedback ────────────────────────────────────────────────────────
    @FXML private Label lblMessage;

    // ─── État ─────────────────────────────────────────────────────────────
    private Utilisateur          utilisateur;
    private final UtilisateurService service = new UtilisateurService();
    private boolean              mdpVisible  = false;


    @FXML
    public void initialize() {
        panelMdp.setVisible(false);
        panelMdp.setManaged(false);
    }


    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
        remplir();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REMPLISSAGE
    // ══════════════════════════════════════════════════════════════════════

    private void remplir() {
        if (utilisateur == null) return;

        txtNom.setText(safe(utilisateur.getNom()));
        txtPrenom.setText(safe(utilisateur.getPrenom()));
        txtEmail.setText(safe(utilisateur.getEmail()));

        // Avatar = initiales dans le cercle
        String n = utilisateur.getNom()    != null && !utilisateur.getNom().isEmpty()    ? String.valueOf(utilisateur.getNom().charAt(0)).toUpperCase()    : "?";
        String p = utilisateur.getPrenom() != null && !utilisateur.getPrenom().isEmpty() ? String.valueOf(utilisateur.getPrenom().charAt(0)).toUpperCase() : "";
        lblAvatar.setText(n + p);

        lblNomComplet.setText(safe(utilisateur.getNom()) + " " + safe(utilisateur.getPrenom()));
        lblEmailHeader.setText(safe(utilisateur.getEmail()));

        String role = utilisateur.getRole() != null ? utilisateur.getRole().toUpperCase() : "USER";
        lblRoleBadge.setText(role);
        lblRoleBadge.setStyle(badgeStyle(role));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SAUVEGARDE
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    public void handleSave() {
        if (utilisateur == null) return;

        String nom   = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email  = txtEmail.getText().trim();

        // ─ Validation ─
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            msg("⚠ Nom, prénom et email sont obligatoires.", false);
            return;
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            msg("⚠ Format d'email invalide.", false);
            return;
        }
        if (service.emailExistsForOtherUser(email, utilisateur.getId()) > 0) {
            msg("⚠ Cet email est déjà utilisé par un autre compte.", false);
            return;
        }

        // ─ Changement mot de passe ─
        if (mdpVisible) {
            String actuel  = txtPasswordActuel.getText();
            String nouveau = txtPasswordNouveau.getText();
            String confirm = txtPasswordConfirm.getText();

            if (!actuel.isEmpty() || !nouveau.isEmpty() || !confirm.isEmpty()) {
                if (!actuel.equals(utilisateur.getMotDePasse())) {
                    msg("⚠ Mot de passe actuel incorrect.", false);
                    return;
                }
                if (nouveau.length() < 6) {
                    msg("⚠ Le nouveau mot de passe doit faire au moins 6 caractères.", false);
                    return;
                }
                if (!nouveau.equals(confirm)) {
                    msg("⚠ Les mots de passe ne correspondent pas.", false);
                    return;
                }
                utilisateur.setMotDePasse(nouveau);
            }
        }

        // ─ Mise à jour BDD ─
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);

        try {
            service.updateUser(utilisateur);
            remplir();
            txtPasswordActuel.clear();
            txtPasswordNouveau.clear();
            txtPasswordConfirm.clear();
            msg("✅ Profil mis à jour avec succès !", true);
        } catch (Exception e) {
            msg("❌ Erreur : " + e.getMessage(), false);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  TOGGLE SECTION MOT DE PASSE
    // ──────────────────────────────────────────────────────────────────────

    @FXML
    public void handleToggleMdp() {
        mdpVisible = !mdpVisible;
        panelMdp.setVisible(mdpVisible);
        panelMdp.setManaged(mdpVisible);
        btnToggleMdp.setText(mdpVisible ? "🔒 Annuler" : "🔑 Changer mon mot de passe");
        lblMessage.setText("");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private void msg(String texte, boolean succes) {
        lblMessage.setText(texte);
        lblMessage.setStyle(succes
                ? "-fx-text-fill:#2E7D32;-fx-font-size:12px;"
                : "-fx-text-fill:#C62828;-fx-font-size:12px;");
    }

    private String safe(String v) { return v != null ? v : ""; }

    private String badgeStyle(String role) {
        String color = switch (role) {
            case "ADMIN"   -> "#C62828";
            case "RH"      -> "#6A1B9A";
            case "EMPLOYE" -> "#1565C0";
            case "CANDIDAT"-> "#E65100";
            default        -> "#2E7D32";
        };
        return "-fx-background-color:" + color + "22;" +
                "-fx-text-fill:" + color + ";" +
                "-fx-background-radius:20;-fx-padding:3 14;" +
                "-fx-font-size:11px;-fx-font-weight:bold;";
    }
}