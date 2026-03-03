package com.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    @FXML
    private VBox contentArea;

    @FXML
    private Button btnToggleView;

    private boolean isBackoffice = false;

    @FXML
    public void initialize() {
        System.out.println("✅ MainController initialisé");
        showEvenements(); // Frontoffice par défaut
        updateToggleButtonText();
    }

    @FXML
    private void toggleView() {
        isBackoffice = !isBackoffice;
        updateToggleButtonText();
        showEvenements();
    }

    private void updateToggleButtonText() {
        if (isBackoffice) {
            btnToggleView.setText("🎨 Passer au Frontoffice");
            btnToggleView.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        } else {
            btnToggleView.setText("🔧 Passer au Backoffice");
            btnToggleView.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void showEvenements() {
        System.out.println("📅 Chargement des Événements...");
        if (isBackoffice) {
            loadView("AffichageEvenement.fxml");
        } else {
            loadView("AffichageEvenementSimple.fxml");
        }
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent view = loader.load();

            Object controller = loader.getController();

            if (controller instanceof AffichageEvenementSimpleController) {
                ((AffichageEvenementSimpleController) controller).setContentArea(contentArea);
            } else if (controller instanceof AffichageEvenementController) {
                ((AffichageEvenementController) controller).setContentArea(contentArea);
            } else if (controller instanceof AjoutEvenementController) {
                ((AjoutEvenementController) controller).setContentArea(contentArea);
            } else if (controller instanceof ModifierEvenementController) {
                ((ModifierEvenementController) controller).setContentArea(contentArea);
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}