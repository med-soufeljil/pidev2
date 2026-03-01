package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import utils.ApiRuntime;

public class MainController {

    @FXML
    public void openFormation(ActionEvent event) {
        openWindow("/FormationView.fxml", "Gestion des Formations");
    }

    @FXML
    public void openApprenant(ActionEvent event) {
        openWindow("/ApprenantView.fxml", "Gestion des Apprenants");
    }

    @FXML
    public void openDashboard(ActionEvent event) {
        openWindow("/DashboardView.fxml", "Dashboard avancé");
    }

    @FXML
    public void showApiInfo(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("API locale disponible");
        alert.setHeaderText("Endpoints API");
        String base = ApiRuntime.getBaseUrl();
        alert.setContentText("GET " + base + "/api/formations\n"
                + "GET " + base + "/api/apprenants\n"
                + "GET " + base + "/api/dashboard\n"
                + "GET " + base + "/api/dashboard/pdf\n"
                + "GET " + base + "/api/feedbacks?formationId=1\n"
                + "GET " + base + "/api/health");
        alert.showAndWait();
    }

    @FXML
    public void quitter(ActionEvent event) {
        System.exit(0);
    }

    private void openWindow(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = new Stage();
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
