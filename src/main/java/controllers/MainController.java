package controllers;

<<<<<<< HEAD
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

public class MainController {

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
        openInCurrentWindow(event, "/DashboardView.fxml", "Dashboard avancé");
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
=======
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import services.OfferResponseHttpServer;
import utils.AuthContext;

import java.util.Optional;

public class MainController {

    private static MainController instance;

    @FXML
    private Button btnDashboard, btnCandidat, btnOffre, btnReunion;
    @FXML
    private ToggleButton toggleDarkMode;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private VBox welcomePane;
    @FXML
    private BorderPane rootPane;
    @FXML
    private Label lblRole;

    @FXML
    public void initialize() {
        instance = this;
        rootPane.getStyleClass().add("light-mode");

        pickRoleIfNeeded();
        applyPermissions();
        OfferResponseHttpServer.ensureStarted();

        toggleDarkMode.selectedProperty().addListener((obs, oldVal, isDarkMode) -> {
            rootPane.getStyleClass().removeAll("light-mode", "dark-mode");
            rootPane.getStyleClass().add(isDarkMode ? "dark-mode" : "light-mode");
        });

        btnDashboard.setOnAction(e -> loadUI("Dashboard.fxml"));
        btnCandidat.setOnAction(e -> loadUI("Candidat.fxml"));
        btnOffre.setOnAction(e -> loadUI("Offre.fxml"));
        btnReunion.setOnAction(e -> loadUI("Reunion.fxml"));
    }

    public static void navigate(String fxml) {
        if (instance != null) {
            instance.loadUI(fxml);
        }
    }

    private void pickRoleIfNeeded() {
        if (AuthContext.getRole() != null) {
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>("ADMIN", FXCollections.observableArrayList("ADMIN", "CANDIDAT"));
        dialog.setTitle("Connexion rôle");
        dialog.setHeaderText("Sélectionnez votre rôle");
        dialog.setContentText("Rôle:");
        Optional<String> choice = dialog.showAndWait();
        AuthContext.setRole("CANDIDAT".equals(choice.orElse("ADMIN")) ? AuthContext.Role.CANDIDAT : AuthContext.Role.ADMIN);
    }

    private void applyPermissions() {
        boolean isAdmin = AuthContext.isAdmin();
        lblRole.setText("Role: " + (isAdmin ? "ADMIN RH" : "CANDIDAT"));

        btnDashboard.setVisible(isAdmin);
        btnDashboard.setManaged(isAdmin);

        btnCandidat.setDisable(!isAdmin);
        btnOffre.setDisable(false);
        btnReunion.setDisable(!isAdmin);
    }

    private void loadUI(String fxml) {
        try {
            Parent pane = FXMLLoader.load(getClass().getResource("/" + fxml));
            if (pane != null) {
                contentArea.getChildren().setAll(pane);
                AnchorPane.setTopAnchor(pane, 0.0);
                AnchorPane.setBottomAnchor(pane, 0.0);
                AnchorPane.setLeftAnchor(pane, 0.0);
                AnchorPane.setRightAnchor(pane, 0.0);
                welcomePane.setVisible(false);
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors du chargement du FXML: " + fxml);
            ex.printStackTrace();
>>>>>>> feature-mohamed
        }
    }
}
