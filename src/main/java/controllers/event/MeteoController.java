package controllers.event;

import models.event.Evenement;
import services.event.EvenementService;
import services.event.WeatherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur pour l'affichage des informations météo des événements
 * Permet de visualiser les prévisions météo pour chaque événement
 */
public class MeteoController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private ComboBox<Evenement> cbEvenement;      // Liste déroulante des événements
    @FXML private VBox meteoCard;                        // Carte météo principale
    @FXML private Label lblVille;                         // Nom de la ville
    @FXML private Label lblDate;                          // Date de l'événement
    @FXML private Label lblMeteoPrincipale;               // Description météo avec icône
    @FXML private Label lblTemperature;                   // Température actuelle
    @FXML private Label lblRessenti;                      // Température ressentie
    @FXML private Label lblMinMax;                        // Températures min/max
    @FXML private Label lblHumidite;                      // Taux d'humidité
    @FXML private Label lblVent;                          // Vitesse du vent
    @FXML private Label lblPression;                      // Pression atmosphérique
    @FXML private Label lblNuages;                        // Couverture nuageuse
    @FXML private Label lblConseil;                       // Conseil basé sur la météo
    @FXML private VBox contentArea;                        // Zone de contenu

    // ==================== ATTRIBUTS ====================
    private EvenementService evenementService = new EvenementService(); // Service événement
    private WeatherService weatherService = new WeatherService();       // Service météo
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy"); // Format date long

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
        System.out.println("MeteoController initialisé");
        chargerEvenements();
    }

    /**
     * Charge tous les événements depuis la base de données
     * Remplit le ComboBox et configure l'affichage
     */
    private void chargerEvenements() {
        try {
            List<Evenement> evenements = evenementService.recuperer();
            cbEvenement.setItems(FXCollections.observableArrayList(evenements));

            // Configuration de l'affichage des événements dans le ComboBox
            cbEvenement.setConverter(new StringConverter<Evenement>() {
                @Override
                public String toString(Evenement evenement) {
                    if (evenement == null) return "";
                    return evenement.getTitre() + " - " + evenement.getLieu(); // Affiche "Titre - Lieu"
                }

                @Override
                public Evenement fromString(String string) {
                    return null;
                }
            });

            // Sélectionne le premier événement par défaut
            if (!evenements.isEmpty()) {
                cbEvenement.setValue(evenements.get(0));
                afficherMeteo();
            }

            // Écouteur pour changer d'événement
            cbEvenement.setOnAction(e -> afficherMeteo());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    /**
     * Affiche les informations météo pour l'événement sélectionné
     * Met à jour tous les labels avec les données météo
     */
    private void afficherMeteo() {
        Evenement event = cbEvenement.getValue();
        if (event != null) {
            // Récupère les prévisions météo
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

            // Changer la couleur de fond de la carte (version plus claire de la couleur météo)
            String couleurFond = lightenColor(meteo.couleur);
            meteoCard.setStyle("-fx-background-color: " + couleurFond + "; -fx-background-radius: 15; -fx-padding: 20;");
        }
    }

    /**
     * Éclaircit une couleur pour l'utiliser comme fond
     * @param hexColor La couleur en format hexadécimal
     * @return La couleur éclaircie en format hexadécimal
     */
    private String lightenColor(String hexColor) {
        try {
            Color color = Color.web(hexColor);
            Color lighter = color.deriveColor(0, 1, 1.2, 0.2); // Éclaircit la couleur
            return toRGBCode(lighter);
        } catch (Exception e) {
            return "#f0f9ff"; // Couleur par défaut en cas d'erreur
        }
    }

    /**
     * Convertit un objet Color en code RGB hexadécimal
     * @param color La couleur à convertir
     * @return Le code hexadécimal (ex: #FF0000 pour rouge)
     */
    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Affiche une alerte d'erreur
     * @param title Titre
     * @param message Message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}