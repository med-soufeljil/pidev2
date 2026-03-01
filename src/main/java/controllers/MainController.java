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

public class MainController {

    @FXML
    private Label lblSuggestion;

    @FXML
    public void initialize() {
        ApiRuntime.ensureStarted();
        String suggestion = new ExternalPublicApiService().fetchSuggestionTitle();
        lblSuggestion.setText("Suggestion externe: " + suggestion);
    }

    @FXML
    public void openFormation(ActionEvent event) {
        openInCurrentWindow(event, "/FormationView.fxml", "Gestion des Formations");
    }

    @FXML
    public void openApprenant(ActionEvent event) {
        openInCurrentWindow(event, "/ApprenantView.fxml", "Gestion des Apprenants");
    }

    @FXML
    public void openDashboard(ActionEvent event) {
        openInCurrentWindow(event, "/DashboardView.fxml", "Dashboard avancé");
    }

    @FXML
    public void showApiInfo(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("API locale disponible");
        alert.setHeaderText("Documentation API (méthode + exemples)");
        String base = ApiRuntime.getBaseUrl();
        alert.setContentText(
                "GET  " + base + "/api/health\n"
                        + "GET  " + base + "/api/formations?q=java&sortBy=duree&order=desc\n"
                        + "GET  " + base + "/api/apprenants\n"
                        + "GET  " + base + "/api/dashboard\n"
                        + "GET  " + base + "/api/dashboard/pdf\n"
                        + "GET  " + base + "/api/feedbacks?formationId=1\n"
                        + "POST " + base + "/api/mailing/registration?email=a@b.com&name=Ali+Ben&formation=Java\n"
                        + "GET  " + base + "/api/external/suggestion"
        );
        alert.showAndWait();
    }

    @FXML
    public void quitter(ActionEvent event) {
        System.exit(0);
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
