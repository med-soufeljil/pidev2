package org.soa.tp1.pi_dev_s2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.soa.tp1.pi_dev_s2.model.Utilisateur;
import org.soa.tp1.pi_dev_s2.service.UtilisateurService;
import java.util.regex.Pattern;

public class SinupController {

    @FXML private TextField     nom;
    @FXML private TextField     prenom;
    @FXML private TextField     email;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private ComboBox<String> comboRole;
    @FXML private Label         message;

    private UtilisateurService service;

    @FXML
    public void initialize() {
        service = new UtilisateurService();
        comboRole.getItems().setAll("candidat", "employe", "rh");
        comboRole.setValue("candidat");
    }

    @FXML
    private void handleSignup() {
        String nomVal    = nom.getText().trim();
        String prenomVal = prenom.getText().trim();
        String emailVal  = email.getText().trim();
        String passVal   = password.getText();
        String confirmVal= confirmPassword.getText();

        if (nomVal.isEmpty() || prenomVal.isEmpty() || emailVal.isEmpty()
                || passVal.isEmpty() || confirmVal.isEmpty()) {
            showMsg("Tous les champs sont obligatoires !", "error"); return;
        }
        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", emailVal)) {
            showMsg("Format email invalide !", "error"); return;
        }
        if (passVal.length() < 6) {
            showMsg("Mot de passe : 6 caractères minimum !", "error"); return;
        }
        if (!passVal.equals(confirmVal)) {
            showMsg("Les mots de passe ne correspondent pas !", "error"); return;
        }

        try {
            if (service.emailExists(emailVal) > 0) {
                showMsg("Cet email est déjà utilisé !", "error"); return;
            }
            Utilisateur u = new Utilisateur();
            u.setNom(nomVal);
            u.setPrenom(prenomVal);
            u.setEmail(emailVal);
            u.setMotDePasse(passVal);
            u.setRole(comboRole.getValue());
            service.addUser(u);

            showMsg("Compte créé avec succès !", "success");

            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(this::goToLogin);
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Erreur lors de la création.", "error");
        }
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) nom.getScene().getWindow();
            stage.setScene(new Scene(root, 440, 520));
            stage.setTitle("Connexion");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showMsg(String msg, String type) {
        message.setText(msg);
        message.setStyle(type.equals("error")
                ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #2E7D32;");
    }
}