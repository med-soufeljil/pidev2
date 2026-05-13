package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import utils.AuthContext;
import utils.SessionContext;
import utils.ThemeContext;

public class LoginController {
    @FXML private TextField txtMail;
    @FXML private PasswordField txtPassword;
    @FXML private ToggleButton toggleDarkMode;
    @FXML private StackPane rootPane;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        applyTheme(ThemeContext.isDarkMode());
        toggleDarkMode.setSelected(ThemeContext.isDarkMode());
        toggleDarkMode.selectedProperty().addListener((obs, old, dark) -> {
            ThemeContext.setDarkMode(dark);
            applyTheme(dark);
        });
    }

    @FXML
    private void login() {
        try {
            Utilisateur user = utilisateurService.login(txtMail.getText().trim(), txtPassword.getText());
            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Connexion", "Email ou mot de passe incorrect.");
                return;
            }
            AuthContext.setCurrentUser(user);
            SessionContext.setCurrentRole(AuthContext.isAdmin() ? SessionContext.Role.ADMIN : SessionContext.Role.USER);
            openMain();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Connexion", e.getMessage());
        }
    }

    @FXML
    private void openSignup() {
        openScene("/Signup.fxml", "Inscription");
    }

    private void openMain() {
        openScene("/Main.fxml", "PIDEV");
    }

    private void openScene(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            if (ThemeContext.isDarkMode()) root.getStyleClass().add("dark-mode");
            Stage stage = (Stage) txtMail.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation", e.getMessage());
        }
    }

    private void applyTheme(boolean dark) {
        rootPane.getStyleClass().remove("dark-mode");
        if (dark) rootPane.getStyleClass().add("dark-mode");
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
