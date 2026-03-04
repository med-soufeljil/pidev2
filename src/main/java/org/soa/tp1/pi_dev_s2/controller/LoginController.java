package org.soa.tp1.pi_dev_s2.controller;

import org.soa.tp1.pi_dev_s2.model.Utilisateur;
import org.soa.tp1.pi_dev_s2.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.regex.Pattern;

public class LoginController {

    @FXML private TextField     email;
    @FXML private PasswordField password;
    @FXML private Label         message;

    // ✅ Initialisé dans initialize() — pas au niveau du champ
    private AuthService authService;

    @FXML
    public void initialize() {
        System.out.println(">>> LoginController.initialize() START");
        try {
            authService = new AuthService();
            System.out.println(">>> ✅ AuthService créé OK");
        } catch (Exception e) {
            System.out.println(">>> ❌ ERREUR AuthService : " + e.getMessage());
            e.printStackTrace();
            // ✅ NE PAS crasher — continuer sans authService
            authService = null;
        }
        System.out.println(">>> LoginController.initialize() END");
    }

    @FXML
    public void handleLogin() {
        String emailVal = email.getText().trim();
        String passVal  = password.getText().trim();

        if (emailVal.isEmpty() || passVal.isEmpty()) {
            showMsg("Tous les champs sont obligatoires !", "error"); return;
        }
        if (!isValidEmail(emailVal)) {
            showMsg("Format email invalide !", "error"); return;
        }

        Utilisateur user = authService.login(emailVal, passVal);
        if (user != null) {
            navigateToDashboard(user);
        } else {
            showMsg("Email ou mot de passe incorrect !", "error");
        }
    }

    @FXML
    public void handleRegister() {
        navigate("/sinup.fxml", "Créer un compte", 480, 620);
    }

    private void navigateToDashboard(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController ctrl = loader.getController();
            ctrl.setUser(user);
            Stage stage = (Stage) email.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard — " + user.getNom());
            stage.setResizable(true);
            stage.setWidth(1150);
            stage.setHeight(720);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void navigate(String fxml, String title, int w, int h) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) email.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showMsg(String msg, String type) {
        message.setText(msg);
        message.setStyle(type.equals("error")
                ? "-fx-text-fill: #C62828; -fx-font-size: 12px;"
                : "-fx-text-fill: #2E7D32; -fx-font-size: 12px;");
    }

    private boolean isValidEmail(String e) {
        return Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", e);
    }
}