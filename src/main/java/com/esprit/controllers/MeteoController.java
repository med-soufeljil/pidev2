package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.EvenementService;
import com.esprit.services.WeatherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MeteoController {

    @FXML
    private ComboBox<Evenement> cbEvenement;

    @FXML
    private VBox meteoCard;

    @FXML
    private Label lblVille;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblMeteoPrincipale;

    @FXML
    private Label lblTemperature;

    @FXML
    private Label lblRessenti;

    @FXML
    private Label lblMinMax;

    @FXML
    private Label lblHumidite;

    @FXML
    private Label lblVent;

    @FXML
    private Label lblPression;

    @FXML
    private Label lblNuages;

    @FXML
    private Label lblConseil;

    @FXML
    private VBox contentArea;

    private EvenementService evenementService = new EvenementService();
    private WeatherService weatherService = new WeatherService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy");

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        System.out.println("MeteoController initialisé");
        chargerEvenements();
    }

    private void chargerEvenements() {
        try {
            List<Evenement> evenements = evenementService.recuperer();
            cbEvenement.setItems(FXCollections.observableArrayList(evenements));

            cbEvenement.setConverter(new StringConverter<Evenement>() {
                @Override
                public String toString(Evenement evenement) {
                    if (evenement == null) return "";
                    return evenement.getTitre() + " - " + evenement.getLieu();
                }

                @Override
                public Evenement fromString(String string) {
                    return null;
                }
            });

            if (!evenements.isEmpty()) {
                cbEvenement.setValue(evenements.get(0));
                afficherMeteo();
            }

            cbEvenement.setOnAction(e -> afficherMeteo());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    private void afficherMeteo() {
        Evenement event = cbEvenement.getValue();
        if (event != null) {
            WeatherService.WeatherInfo meteo = weatherService.getPrevisionEvenement(
                    event.getLieu(), event.getDateEvenement()
            );

            // Mettre à jour l'affichage
            lblVille.setText(meteo.getCityDisplay());
            lblDate.setText(event.getDateEvenement().format(dateFormatter));

            // Icône et description principales
            lblMeteoPrincipale.setText(meteo.iconeEmoji + " " + meteo.description);

            // Températures
            lblTemperature.setText(String.format("%.1f°C", meteo.temperature));
            lblRessenti.setText(String.format("Ressenti %.1f°C", meteo.ressenti));
            lblMinMax.setText(String.format("Min %.1f°C / Max %.1f°C", meteo.tempMin, meteo.tempMax));

            // Détails
            lblHumidite.setText(String.format("%d%%", meteo.humidite));
            lblVent.setText(String.format("%.1f m/s", meteo.ventVitesse));
            lblPression.setText(String.format("%d hPa", meteo.pression));
            lblNuages.setText(String.format("%d%%", meteo.nuages));

            // Conseil
            lblConseil.setText(meteo.conseil);

            // Changer la couleur de fond de la carte
            String couleurFond = lightenColor(meteo.couleur);
            meteoCard.setStyle("-fx-background-color: " + couleurFond + "; -fx-background-radius: 15; -fx-padding: 20;");
        }
    }

    private String lightenColor(String hexColor) {
        try {
            Color color = Color.web(hexColor);
            Color lighter = color.deriveColor(0, 1, 1.2, 0.2);
            return toRGBCode(lighter);
        } catch (Exception e) {
            return "#f0f9ff";
        }
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}