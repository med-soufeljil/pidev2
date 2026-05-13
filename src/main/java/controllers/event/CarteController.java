package controllers.event;

import models.event.Evenement;
import services.event.EvenementService;
import services.event.OpenStreetMapService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrôleur pour l'affichage de la carte/localisation des événements
 * Utilise OpenStreetMap pour géocoder les adresses et afficher les coordonnées
 */
public class CarteController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private ComboBox<Evenement> cbEvenement;      // Liste déroulante des événements
    @FXML private Label lblCoordonnees;                  // Label pour les coordonnées GPS
    @FXML private Label lblAdresse;                       // Label pour l'adresse complète
    @FXML private Label lblVille;                         // Label pour la ville
    @FXML private Label lblPays;                          // Label pour le pays
    @FXML private Hyperlink linkGoogleMaps;               // Lien vers Google Maps
    @FXML private Hyperlink linkOpenStreetMap;            // Lien vers OpenStreetMap
    @FXML private VBox contentArea;                        // Zone de contenu (non utilisé ici)

    // ==================== ATTRIBUTS ====================
    private EvenementService evenementService = new EvenementService();       // Service événement
    private OpenStreetMapService osmService = new OpenStreetMapService();     // Service de géocodage

    /**
     * Définit la zone de contenu principale
     * @param contentArea Le conteneur VBox
     */
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    /**
     * Initialisation du contrôleur
     * Charge la liste des événements
     */
    @FXML
    public void initialize() {
        chargerEvenements();
    }

    /**
     * Charge tous les événements depuis la base de données
     * Remplit le ComboBox et configure l'affichage
     */
    private void chargerEvenements() {
        try {
            // Récupère tous les événements
            List<Evenement> evenements = evenementService.recuperer();
            cbEvenement.setItems(FXCollections.observableArrayList(evenements));

            // Configure comment afficher un événement dans le ComboBox
            cbEvenement.setConverter(new StringConverter<Evenement>() {
                @Override
                public String toString(Evenement evenement) {
                    if (evenement == null) return "";
                    return evenement.getTitre() + " - " + evenement.getLieu(); // Affiche "Titre - Lieu"
                }

                @Override
                public Evenement fromString(String string) {
                    return null; // Non utilisé
                }
            });

            // Sélectionne le premier événement par défaut
            if (!evenements.isEmpty()) {
                cbEvenement.setValue(evenements.get(0));
                afficherCarte(); // Affiche sa localisation
            }

            // Écouteur pour changer d'événement
            cbEvenement.setOnAction(e -> afficherCarte());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    /**
     * Affiche la localisation de l'événement sélectionné
     * Utilise OpenStreetMap pour géocoder l'adresse
     */
    private void afficherCarte() {
        Evenement event = cbEvenement.getValue();
        if (event != null) {
            String lieu = event.getLieu();
            System.out.println("🔍 Recherche du lieu: " + lieu);

            // Appel au service de géocodage
            OpenStreetMapService.Location location = osmService.geocode(lieu);

            if (location != null) {
                // Affichage des informations de localisation
                lblCoordonnees.setText(String.format("Latitude: %.6f, Longitude: %.6f", location.lat, location.lon));
                lblAdresse.setText("Adresse: " + location.displayName);
                lblVille.setText("Ville: " + (location.city.isEmpty() ? "Non spécifiée" : location.city));
                lblPays.setText("Pays: " + (location.country.isEmpty() ? "Non spécifié" : location.country));

                double lat = location.lat;
                double lon = location.lon;

                // Configuration du lien Google Maps
                linkGoogleMaps.setOnAction(e -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(
                                java.net.URI.create(location.getGoogleMapsUrl())
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                // Configuration du lien OpenStreetMap
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
                // Lieu non trouvé
                lblCoordonnees.setText("❌ Lieu non trouvé");
                lblAdresse.setText("Lieu recherché: " + lieu);
                lblVille.setText("");
                lblPays.setText("");
            }
        }
    }

    /**
     * Affiche une alerte d'erreur
     * @param title Titre de l'alerte
     * @param message Contenu du message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}