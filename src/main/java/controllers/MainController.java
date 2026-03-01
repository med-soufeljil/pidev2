package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

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
        alert.setContentText("GET http://localhost:8080/api/formations\n"
                + "GET http://localhost:8080/api/apprenants\n"
                + "GET http://localhost:8080/api/dashboard\n"
                + "GET http://localhost:8080/api/dashboard/pdf\n"
                + "GET http://localhost:8080/api/feedbacks?formationId=1");
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
