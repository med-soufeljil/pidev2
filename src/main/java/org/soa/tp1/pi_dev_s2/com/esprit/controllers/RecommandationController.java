package org.soa.tp1.pi_dev_s2.com.esprit.controllers;

import org.soa.tp1.pi_dev_s2.com.esprit.models.Evenement;
import org.soa.tp1.pi_dev_s2.com.esprit.services.RecommandationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecommandationController {

    @FXML private ComboBox<String> typeCombo;
    @FXML private FlowPane cartesFlowPane; // Changé de VBox à FlowPane
    @FXML private Label titreLabel;
    @FXML private Label chargementLabel;
    @FXML private Button actualiserButton;
    @FXML private Button retourButton;

    private final RecommandationService recommandationService = new RecommandationService();
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter HEURE_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private VBox contentArea;

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
        System.out.println("RecommandationController: contentArea set");
    }

    @FXML
    public void initialize() {
        System.out.println("RecommandationController initialisé");

        typeCombo.getItems().addAll(
                "🔥 Les plus populaires",
                "📈 Tendances du moment",
                "📅 À venir"
        );
        typeCombo.setValue("🔥 Les plus populaires");

        typeCombo.setOnAction(e -> chargerRecommandations());
        actualiserButton.setOnAction(e -> chargerRecommandations());

        // Action du bouton retour
        retourButton.setOnAction(e -> handleRetour());

        chargerRecommandations();
    }

    @FXML
    private void handleRetour() {
        if (contentArea == null) {
            System.err.println("❌ Erreur: contentArea est null");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageEvenementSimple.fxml"));
            Parent root = loader.load();

            AffichageEvenementSimpleController controller = loader.getController();
            controller.setContentArea(contentArea);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chargerRecommandations() {
        cartesFlowPane.getChildren().clear();
        chargementLabel.setVisible(true);

        try {
            List<Evenement> recommandations;
            String type = typeCombo.getValue();

            if (type.contains("populaires")) {
                recommandations = recommandationService.getPopulaires();
                titreLabel.setText("🔥 Événements les plus populaires");
            } else if (type.contains("Tendances")) {
                recommandations = recommandationService.getTendances();
                titreLabel.setText("📈 Tendances du moment");
            } else {
                recommandations = recommandationService.getProchains();
                titreLabel.setText("📅 Événements à venir");
            }

            chargementLabel.setVisible(false);
            afficherCartes(recommandations);

        } catch (SQLException e) {
            chargementLabel.setVisible(false);
            e.printStackTrace();
        }
    }

    private void afficherCartes(List<Evenement> evenements) {
        cartesFlowPane.getChildren().clear();

        if (evenements.isEmpty()) {
            Label vide = new Label("Aucun événement trouvé pour cette catégorie");
            vide.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px; -fx-padding: 30;");
            cartesFlowPane.getChildren().add(vide);
            return;
        }

        for (Evenement e : evenements) {
            VBox carte = creerCarteEvenement(e);
            cartesFlowPane.getChildren().add(carte);
        }
    }

    private VBox creerCarteEvenement(Evenement e) {
        VBox carte = new VBox(12);
        carte.setPrefWidth(280);
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Titre
        Label titre = new Label(e.getTitre());
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setWrapText(true);
        titre.setStyle("-fx-text-fill: #1e293b;");

        // Date
        HBox dateBox = new HBox(8);
        Label dateIcon = new Label("📅");
        Label dateText = new Label(e.getDateEvenement() != null ? e.getDateEvenement().format(DATE_FORMAT) : "Date inconnue");
        dateText.setStyle("-fx-text-fill: #475569;");
        dateBox.getChildren().addAll(dateIcon, dateText);

        // Heure
        HBox heureBox = new HBox(8);
        Label heureIcon = new Label("⏰");
        String heureDebut = e.getHeureDebut() != null ? e.getHeureDebut().format(HEURE_FORMAT) : "--:--";
        String heureFin = e.getHeureFin() != null ? e.getHeureFin().format(HEURE_FORMAT) : "--:--";
        Label heureText = new Label(heureDebut + " - " + heureFin);
        heureText.setStyle("-fx-text-fill: #475569;");
        heureBox.getChildren().addAll(heureIcon, heureText);

        // Lieu
        HBox lieuBox = new HBox(8);
        Label lieuIcon = new Label("📍");
        Label lieuText = new Label(e.getLieu());
        lieuText.setStyle("-fx-text-fill: #475569;");
        lieuText.setWrapText(true);
        lieuBox.getChildren().addAll(lieuIcon, lieuText);

        // Places
        HBox placesBox = new HBox(8);
        Label placesIcon = new Label("👥");
        int placesRestantes = e.getNombrePlacesMax() - e.getNombreInscrits();
        String placesText = placesRestantes + " places disponibles sur " + e.getNombrePlacesMax();
        Label placesValue = new Label(placesText);
        placesValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        if (placesRestantes <= 0) {
            placesValue.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else if (placesRestantes <= 5) {
            placesValue.setStyle("-fx-text-fill: #f97316; -fx-font-weight: bold;");
        } else {
            placesValue.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
        }
        placesBox.getChildren().addAll(placesIcon, placesValue);

        // Score de popularité (étoiles)
        try {
            int score = recommandationService.getScorePopularite(e.getIdEvenement());
            HBox etoilesBox = new HBox(2);
            for (int i = 0; i < 5; i++) {
                Label etoile = new Label(i < score ? "⭐" : "☆");
                etoile.setStyle("-fx-font-size: 14px; -fx-text-fill: #f59e0b;");
                etoilesBox.getChildren().add(etoile);
            }
            carte.getChildren().addAll(titre, etoilesBox, dateBox, heureBox, lieuBox, placesBox);
        } catch (SQLException ex) {
            carte.getChildren().addAll(titre, dateBox, heureBox, lieuBox, placesBox);
        }

        // Animation au survol
        carte.setOnMouseEntered(ev ->
                carte.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #3b82f6; -fx-border-radius: 10; -fx-border-width: 2; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.3), 10, 0, 0, 5);")
        );
        carte.setOnMouseExited(ev ->
                carte.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);")
        );

        return carte;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}