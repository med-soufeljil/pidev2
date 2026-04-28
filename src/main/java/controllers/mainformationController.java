package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.ExternalPublicApiService;
import utils.ApiRuntime;
import utils.SessionContext;

public class mainformationController {

    @FXML private Label lblSuggestion;
    @FXML private Label lblRole;
    @FXML private Label lblWelcome;
    @FXML private Button btnFormation;
    @FXML private Button btnApprenant;
    @FXML private Button btnDashboard;

    @FXML
    public void initialize() {
        ApiRuntime.ensureStarted();
        String suggestion = new ExternalPublicApiService().fetchSuggestionTitle();
        lblSuggestion.setText("Suggestion externe: " + suggestion);

        if (SessionContext.getCurrentRole() == null) {
            SessionContext.setCurrentRole(SessionContext.Role.USER);
        }

        if (SessionContext.isAdmin()) {
            lblRole.setText("Rôle actuel: ADMIN");
            lblWelcome.setText("Espace Admin - Gestion complète des modules");
            btnApprenant.setVisible(true);
            btnApprenant.setManaged(true);
            btnDashboard.setVisible(true);
            btnDashboard.setManaged(true);
        } else {
            lblRole.setText("Rôle actuel: USER");
            lblWelcome.setText("Espace User - Consultation formations et postulation");
            btnApprenant.setVisible(false);
            btnApprenant.setManaged(false);
            btnDashboard.setVisible(false);
            btnDashboard.setManaged(false);
        }
    }

    @FXML
    public void openFormation(ActionEvent event) {
        openInCurrentWindow(event, "/FormationView.fxml", "Gestion des Formations");
    }

    @FXML
    public void openApprenant(ActionEvent event) {
        if (SessionContext.isUser()) {
            showWarning("Accès refusé", "L'espace USER ne peut pas accéder au module Apprenants.");
            return;
        }
        openInCurrentWindow(event, "/ApprenantView.fxml", "Gestion des Apprenants");
    }

    @FXML
    public void openDashboard(ActionEvent event) {
        if (SessionContext.isUser()) {
            showWarning("Accès refusé", "L'espace USER ne peut pas accéder au dashboard admin.");
            return;
        }
        openInCurrentWindow(event, "/dashboardformation.fxml", "Dashboard avancé");
    }

    @FXML
    public void backToRoleSelection(ActionEvent event) {
        SessionContext.setCurrentRole(null);
        openInCurrentWindow(event, "/RoleSelectionView.fxml", "Choix de l'espace");
    }

    @FXML
    public void quitter(ActionEvent event) {
        System.exit(0);
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
