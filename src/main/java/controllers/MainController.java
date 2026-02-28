package controllers;

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
        ChoiceDialog<String> dialog = new ChoiceDialog<>("ADMIN", FXCollections.observableArrayList("ADMIN", "USER"));
        dialog.setTitle("Connexion rôle");
        dialog.setHeaderText("Sélectionnez votre rôle");
        dialog.setContentText("Rôle:");
        Optional<String> choice = dialog.showAndWait();
        AuthContext.setRole("USER".equals(choice.orElse("ADMIN")) ? AuthContext.Role.USER : AuthContext.Role.ADMIN);
    }

    private void applyPermissions() {
        boolean isAdmin = AuthContext.isAdmin();
        lblRole.setText("Role: " + (isAdmin ? "ADMIN RH" : "USER"));

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
        }
    }
}
