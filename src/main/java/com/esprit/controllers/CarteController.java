package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.EvenementService;
import com.esprit.services.OpenStreetMapService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

public class CarteController {

    @FXML
    private ComboBox<Evenement> cbEvenement;

    @FXML
    private Label lblCoordonnees;

    @FXML
    private Label lblAdresse;

    @FXML
    private Label lblVille;

    @FXML
    private Label lblPays;

    @FXML
    private Hyperlink linkGoogleMaps;

    @FXML
    private Hyperlink linkOpenStreetMap;

    @FXML
    private VBox contentArea;

    private EvenementService evenementService = new EvenementService();
    private OpenStreetMapService osmService = new OpenStreetMapService();

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
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
                afficherCarte();
            }

            cbEvenement.setOnAction(e -> afficherCarte());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    private void afficherCarte() {
        Evenement event = cbEvenement.getValue();
        if (event != null) {
            String lieu = event.getLieu();
            System.out.println("🔍 Recherche du lieu: " + lieu);

            OpenStreetMapService.Location location = osmService.geocode(lieu);

            if (location != null) {
                lblCoordonnees.setText(String.format("Latitude: %.6f, Longitude: %.6f", location.lat, location.lon));
                lblAdresse.setText("Adresse: " + location.displayName);
                lblVille.setText("Ville: " + (location.city.isEmpty() ? "Non spécifiée" : location.city));
                lblPays.setText("Pays: " + (location.country.isEmpty() ? "Non spécifié" : location.country));

                double lat = location.lat;
                double lon = location.lon;

                linkGoogleMaps.setOnAction(e -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(
                                java.net.URI.create(location.getGoogleMapsUrl())
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                linkOpenStreetMap.setOnAction(e -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(
                                java.net.URI.create(location.getOpenStreetMapUrl())
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

            } else {
                lblCoordonnees.setText("❌ Lieu non trouvé");
                lblAdresse.setText("Lieu recherché: " + lieu);
                lblVille.setText("");
                lblPays.setText("");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}