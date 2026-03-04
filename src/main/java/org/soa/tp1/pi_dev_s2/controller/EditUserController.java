package org.soa.tp1.pi_dev_s2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.soa.tp1.pi_dev_s2.model.Utilisateur;
import org.soa.tp1.pi_dev_s2.service.UtilisateurService;

import java.util.regex.Pattern;

public class EditUserController {

    @FXML private TextField     nom;
    @FXML private TextField     prenom;
    @FXML private TextField     email;
    @FXML private PasswordField password;
    @FXML private ComboBox<String> comboRole;
    @FXML private Label         message;
    @FXML private Label         title;

    private UtilisateurService service;
    private Utilisateur        utilisateur;
    private boolean            isEdit = false;

    @FXML
    public void initialize() {
        service = new UtilisateurService();
        comboRole.getItems().setAll("admin", "rh", "employe", "candidat", "client");
        comboRole.setValue("candidat");
    }

    public void setUtilisateur(Utilisateur u) {
        if (u == null) {
            title.setText("➕ Ajouter un utilisateur");
            return;
        }
        this.utilisateur = u;
        this.isEdit = true;
        title.setText("✏ Modifier l'utilisateur");
        nom.setText(u.getNom());
        prenom.setText(u.getPrenom());
        email.setText(u.getEmail());
        comboRole.setValue(u.getRole());
        password.setPromptText("Laisser vide pour ne pas changer");
    }

    @FXML
    private void handleSave() {
        String n = nom.getText().trim();
        String p = prenom.getText().trim();
        String e = email.getText().trim();
        String pw = password.getText();
        String role = comboRole.getValue();

        if (n.isEmpty() || p.isEmpty() || e.isEmpty()) {
            showMsg("Tous les champs sont obligatoires !", "error"); return;
        }
        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", e)) {
            showMsg("Format email invalide !", "error"); return;
        }
        if (!isEdit && pw.length() < 6) {
            showMsg("Mot de passe : 6 caractères minimum !", "error"); return;
        }

        try {
            if (isEdit) {
                utilisateur.setNom(n);
                utilisateur.setPrenom(p);
                utilisateur.setEmail(e);
                utilisateur.setRole(role);
                if (!pw.isEmpty()) utilisateur.setMotDePasse(pw);
                // Vérif email unique
                if (service.emailExistsForOtherUser(e, utilisateur.getId()) > 0) {
                    showMsg("Cet email est déjà utilisé !", "error"); return;
                }
                service.updateUser(utilisateur);
                showMsg("✅ Utilisateur modifié !", "success");
            } else {
                if (service.emailExists(e) > 0) {
                    showMsg("Cet email est déjà utilisé !", "error"); return;
                }
                Utilisateur newUser = new Utilisateur();
                newUser.setNom(n);
                newUser.setPrenom(p);
                newUser.setEmail(e);
                newUser.setMotDePasse(pw);
                newUser.setRole(role);
                service.addUser(newUser);
                showMsg("✅ Utilisateur ajouté !", "success");
            }
            // Fermer après 1s
            new Thread(() -> {
                try { Thread.sleep(900); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() ->
                        ((Stage) nom.getScene().getWindow()).close());
            }).start();

        } catch (Exception ex) {
            ex.printStackTrace();
            showMsg("Erreur : " + ex.getMessage(), "error");
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nom.getScene().getWindow()).close();
    }

    private void showMsg(String msg, String type) {
        message.setText(msg);
        message.setStyle(type.equals("error")
                ? "-fx-text-fill:#C62828;-fx-font-size:12px;"
                : "-fx-text-fill:#2E7D32;-fx-font-size:12px;");
    }
}