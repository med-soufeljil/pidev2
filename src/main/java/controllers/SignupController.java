package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import utils.AuthContext;
import utils.SessionContext;
import utils.ThemeContext;

public class SignupController {
    @FXML private TextField txtNom, txtPrenom, txtCin, txtMail, txtTel, txtPhoto;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private ToggleButton toggleDarkMode;
    @FXML private StackPane rootPane;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        cbRole.setItems(FXCollections.observableArrayList("EMPLOYE", "CANDIDAT"));
        cbRole.setValue("EMPLOYE");
        applyTheme(ThemeContext.isDarkMode());
        toggleDarkMode.setSelected(ThemeContext.isDarkMode());
        toggleDarkMode.selectedProperty().addListener((obs, old, dark) -> {
            ThemeContext.setDarkMode(dark);
            applyTheme(dark);
        });
    }

    @FXML
    private void signup() {
        try {
            Utilisateur user = new Utilisateur();
            user.setNom(txtNom.getText().trim());
            user.setPrenom(txtPrenom.getText().trim());
            user.setCin(parseInt(txtCin.getText()));
            user.setMail(txtMail.getText().trim());
            user.setTel(parseInt(txtTel.getText()));
            user.setPhotoProfil(txtPhoto.getText().trim().isBlank() ? "0" : txtPhoto.getText().trim());
            user.setRole(cbRole.getValue());
            user.setPassword(txtPassword.getText().isBlank() ? "1234" : txtPassword.getText());
            utilisateurService.ajouter(user);
            AuthContext.setCurrentUser(user);
            SessionContext.setCurrentRole(SessionContext.Role.USER);
            openScene("/Main.fxml", "PIDEV");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Inscription", e.getMessage());
        }
    }

    @FXML
    private void openLogin() {
        openScene("/Login.fxml", "Connexion");
    }

    private int parseInt(String value) {
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
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
