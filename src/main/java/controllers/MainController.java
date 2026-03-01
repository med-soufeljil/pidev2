package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.ExternalPublicApiService;
import utils.ApiRuntime;
import utils.SessionContext;

public class MainController {

    @FXML
    private Label lblSuggestion;
    @FXML
    private Label lblRole;

    @FXML
    public void initialize() {
        ApiRuntime.ensureStarted();
        String suggestion = new ExternalPublicApiService().fetchSuggestionTitle();
        lblSuggestion.setText("Suggestion externe: " + suggestion);
        lblRole.setText("Rôle actuel: non défini");
    }

    @FXML
    public void selectAdmin() {
        SessionContext.setCurrentRole(SessionContext.Role.ADMIN);
        lblRole.setText("Rôle actuel: ADMIN");
    }

    @FXML
    public void selectUser() {
        SessionContext.setCurrentRole(SessionContext.Role.USER);
        lblRole.setText("Rôle actuel: USER");
    }

    @FXML
    public void openFormation(ActionEvent event) {
        if (!ensureRoleSelected()) return;
        openInCurrentWindow(event, "/FormationView.fxml", "Gestion des Formations");
    }

    @FXML
    public void openApprenant(ActionEvent event) {
        if (!ensureRoleSelected()) return;
        if (SessionContext.isUser()) {
            showWarning("Accès refusé", "L'espace USER ne peut pas accéder au module Apprenants.");
            return;
        }
        openInCurrentWindow(event, "/ApprenantView.fxml", "Gestion des Apprenants");
    }

    @FXML
    public void openDashboard(ActionEvent event) {
        if (!ensureRoleSelected()) return;
        if (SessionContext.isUser()) {
            showWarning("Accès refusé", "L'espace USER ne peut pas accéder au dashboard admin.");
            return;
        }
        openInCurrentWindow(event, "/DashboardView.fxml", "Dashboard avancé");
    }

    @FXML
    public void quitter(ActionEvent event) {
        System.exit(0);
    }

    private boolean ensureRoleSelected() {
        if (SessionContext.getCurrentRole() == null) {
            showWarning("Choix du rôle", "Veuillez choisir ADMIN ou USER avant d'entrer.");
            return false;
        }
        return true;
    }

    private void showWarning(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openInCurrentWindow(ActionEvent event, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Navigation");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
