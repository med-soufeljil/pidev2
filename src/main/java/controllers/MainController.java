package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML private Button btnCandidat, btnOffre, btnRecrutement, btnReunion;
    @FXML private ToggleButton toggleDarkMode;
    @FXML private AnchorPane contentArea;
    @FXML private VBox welcomePane;
    @FXML private BorderPane rootPane;

    @FXML
    public void initialize() {
        // Default light mode
        rootPane.getStyleClass().add("light-mode");

        // Dark mode toggle
        toggleDarkMode.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                rootPane.getStyleClass().remove("light-mode");
                rootPane.getStyleClass().add("dark-mode");
            } else {
                rootPane.getStyleClass().remove("dark-mode");
                rootPane.getStyleClass().add("light-mode");
            }
        });

        // Load FXMLs & hide welcome page
        btnCandidat.setOnAction(e -> loadUI("Candidat.fxml"));
        btnOffre.setOnAction(e -> loadUI("Offre.fxml"));
        btnRecrutement.setOnAction(e -> loadUI("Recrutement.fxml"));
        btnReunion.setOnAction(e -> loadUI("Reunion.fxml"));
    }

    private void loadUI(String fxml) {
        try {
            Parent pane = FXMLLoader.load(getClass().getResource("/" + fxml));
            if (pane != null) {
                contentArea.getChildren().setAll(pane);

                // Étendre le contenu sur tout l'AnchorPane
                AnchorPane.setTopAnchor(pane, 0.0);
                AnchorPane.setBottomAnchor(pane, 0.0);
                AnchorPane.setLeftAnchor(pane, 0.0);
                AnchorPane.setRightAnchor(pane, 0.0);

                // Si c'est un VBox, centre son contenu
                if (pane instanceof VBox vbox) {
                    vbox.setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
            welcomePane.setVisible(false);
        } catch (Exception ex) {
            System.err.println("Erreur lors du chargement du FXML: " + fxml);
            ex.printStackTrace();
        }
    }

}
