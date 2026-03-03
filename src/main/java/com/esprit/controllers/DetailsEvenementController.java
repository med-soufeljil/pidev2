package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.OpenStreetMapService;
import com.esprit.services.WeatherService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class DetailsEvenementController {

    @FXML private Label emojiLabel;
    @FXML private Label titreLabel;
    @FXML private Rectangle colorRectangle;
    @FXML private Label dateLabel;
    @FXML private Label heureLabel;
    @FXML private Label lieuLabel;
    @FXML private Label meteoLabel;
    @FXML private Label placesLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button participerButton;
    @FXML private Button mapsButton;

    private Evenement evenement;
    private OpenStreetMapService.Location eventLocation;

    private final WeatherService weatherService = new WeatherService();
    private final OpenStreetMapService geocodingService = new OpenStreetMapService();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter HEURE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        afficherDetails();
        chargerMeteo();
        chargerGeolocalisation();

        // NE PAS désactiver le bouton - le garder actif et stylisé
        participerButton.setDisable(false);
        participerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20; -fx-cursor: hand;");
        participerButton.setText("Participer à cet événement");
    }

    private void afficherDetails() {
        titreLabel.setText(evenement.getTitre());
        emojiLabel.setText(getEmojiPourEvenement(evenement));

        String[] couleurs = {"#3b82f6", "#8b5cf6", "#ec4899", "#f97316", "#06b6d4", "#84cc16"};
        int index = (evenement.getIdEvenement() % couleurs.length);
        colorRectangle.setFill(Color.web(couleurs[index]));

        dateLabel.setText(evenement.getDateEvenement() != null ?
                evenement.getDateEvenement().format(DATE_FORMATTER) : "Date non définie");

        String heureDebut = evenement.getHeureDebut() != null ?
                evenement.getHeureDebut().format(HEURE_FORMATTER) : "--:--";
        String heureFin = evenement.getHeureFin() != null ?
                evenement.getHeureFin().format(HEURE_FORMATTER) : "--:--";
        heureLabel.setText(heureDebut + " - " + heureFin);

        lieuLabel.setText(evenement.getLieu());

        int placesRestantes = evenement.getNombrePlacesMax() - evenement.getNombreInscrits();
        placesLabel.setText(placesRestantes + " places disponibles sur " + evenement.getNombrePlacesMax());

        if (evenement.getDescription() != null && !evenement.getDescription().isEmpty()) {
            descriptionLabel.setText(evenement.getDescription());
        } else {
            descriptionLabel.setText("Aucune description disponible pour cet événement.");
        }
    }

    private void chargerMeteo() {
        WeatherService.WeatherInfo meteo = weatherService.getPrevisionEvenement(
                evenement.getLieu(), evenement.getDateEvenement()
        );
        meteoLabel.setText(meteo.getFormatted());
    }

    private void chargerGeolocalisation() {
        eventLocation = geocodingService.geocode(evenement.getLieu());
        if (mapsButton != null) {
            mapsButton.setDisable(eventLocation == null);
            if (eventLocation != null) {
                mapsButton.setText("Voir sur OpenStreetMap");
            } else {
                mapsButton.setText("Localisation non disponible");
            }
        }
    }

    private String getEmojiPourEvenement(Evenement evenement) {
        String titre = evenement.getTitre().toLowerCase();
        if (titre.contains("conference") || titre.contains("conférence")) return "🎤";
        if (titre.contains("webinaire")) return "💻";
        if (titre.contains("team building")) return "🤝";
        if (titre.contains("formation")) return "📚";
        if (titre.contains("soirée") || titre.contains("soiree")) return "🎉";
        if (titre.contains("sport")) return "⚽";
        return "📅";
    }

    @FXML
    private void handleParticiper() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutParticipation.fxml"));
            Parent root = loader.load();

            AjoutParticipationController controller = loader.getController();
            controller.setEvent(evenement);

            // Si le contrôleur a besoin de contentArea, on peut le passer
            // controller.setContentArea(contentArea);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Participer à l'événement");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Optionnel : fermer la fenêtre de détails après participation
            // Stage detailsStage = (Stage) participerButton.getScene().getWindow();
            // detailsStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de participation: " + e.getMessage());
        }
    }

    @FXML
    private void handleOuvrirMaps() {
        if (eventLocation != null) {
            String url = eventLocation.getGoogleMapsUrl();
            try {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleFermer() {
        Stage stage = (Stage) emojiLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}