package controllers.event;

import models.event.Evenement;
import services.event.OpenStreetMapService;
import services.event.WeatherService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

/**
 * Contrôleur pour l'affichage des détails d'un événement
 * Affiche toutes les informations d'un événement dans une fenêtre modale
 */
public class DetailsEvenementController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private Label emojiLabel;                // Emoji représentant l'événement
    @FXML private Label titreLabel;                 // Titre de l'événement
    @FXML private Rectangle colorRectangle;         // Rectangle coloré décoratif
    @FXML private Label dateLabel;                  // Date formatée
    @FXML private Label heureLabel;                  // Heures de début/fin
    @FXML private Label lieuLabel;                   // Lieu
    @FXML private Label meteoLabel;                  // Informations météo
    @FXML private Label placesLabel;                 // Places disponibles
    @FXML private Label descriptionLabel;            // Description longue
    @FXML private Button participerButton;           // Bouton pour participer
    @FXML private Button mapsButton;                 // Bouton pour voir sur la carte

    // ==================== ATTRIBUTS ====================
    private Evenement evenement;                      // Événement à afficher
    private OpenStreetMapService.Location eventLocation; // Localisation géocodée

    private final WeatherService weatherService = new WeatherService();
    private final OpenStreetMapService geocodingService = new OpenStreetMapService();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter HEURE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Définit l'événement à afficher et déclenche l'affichage
     * @param evenement L'événement sélectionné
     */
    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        afficherDetails();          // Affiche les informations de base
        chargerMeteo();             // Charge la météo depuis l'API
        chargerGeolocalisation();   // Charge la localisation depuis l'API
    }

    /**
     * Affiche les détails de base de l'événement
     */
    private void afficherDetails() {
        if (evenement == null) return;

        // Titre
        titreLabel.setText(evenement.getTitre() != null ? evenement.getTitre() : "Titre non disponible");

        // Emoji selon le type d'événement
        emojiLabel.setText(getEmojiPourEvenement(evenement));

        // Rectangle coloré (décoratif)
        String[] couleurs = {"#3b82f6", "#8b5cf6", "#ec4899", "#f97316", "#06b6d4", "#84cc16"};
        int index = (evenement.getIdEvenement() % couleurs.length);
        colorRectangle.setFill(Color.web(couleurs[index]));

        // Date formatée
        if (evenement.getDateEvenement() != null) {
            dateLabel.setText(evenement.getDateEvenement().format(DATE_FORMATTER));
        } else {
            dateLabel.setText("Date non définie");
        }

        // Heures de début et fin
        String heureDebut = evenement.getHeureDebut() != null ?
                evenement.getHeureDebut().format(HEURE_FORMATTER) : "--:--";
        String heureFin = evenement.getHeureFin() != null ?
                evenement.getHeureFin().format(HEURE_FORMATTER) : "--:--";
        heureLabel.setText(heureDebut + " - " + heureFin);

        // Lieu
        lieuLabel.setText(evenement.getLieu() != null ? evenement.getLieu() : "Lieu non défini");

        // Places disponibles
        int placesRestantes = evenement.getNombrePlacesMax() - evenement.getNombreInscrits();
        placesLabel.setText(placesRestantes + " places disponibles sur " + evenement.getNombrePlacesMax());

        // Description
        if (evenement.getDescription() != null && !evenement.getDescription().isEmpty()) {
            descriptionLabel.setText(evenement.getDescription());
        } else {
            descriptionLabel.setText("Aucune description disponible pour cet événement.");
        }
    }

    /**
     * Charge les informations météo pour l'événement
     */
    private void chargerMeteo() {
        try {
            WeatherService.WeatherInfo meteo = weatherService.getPrevisionEvenement(
                    evenement.getLieu(), evenement.getDateEvenement()
            );
            meteoLabel.setText(meteo.getDetailFormatted());
        } catch (Exception e) {
            meteoLabel.setText("🌡️ Information météo non disponible");
            System.err.println("Erreur météo: " + e.getMessage());
        }
    }

    /**
     * Charge la localisation (géocodage) de l'événement
     */
    private void chargerGeolocalisation() {
        try {
            eventLocation = geocodingService.geocode(evenement.getLieu());
            if (mapsButton != null) {
                mapsButton.setDisable(eventLocation == null);
                if (eventLocation != null) {
                    mapsButton.setText("🗺️ Voir sur Google Maps");
                } else {
                    mapsButton.setText("📍 Localisation non disponible");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur géolocalisation: " + e.getMessage());
        }
    }

    /**
     * Détermine l'emoji approprié selon le type d'événement
     * @param evenement L'événement
     * @return L'emoji correspondant
     */
    private String getEmojiPourEvenement(Evenement evenement) {
        if (evenement == null || evenement.getTitre() == null) return "📅";

        String titre = evenement.getTitre().toLowerCase();
        if (titre.contains("conference") || titre.contains("conférence")) return "🎤";
        if (titre.contains("webinaire")) return "💻";
        if (titre.contains("team building")) return "🤝";
        if (titre.contains("formation")) return "📚";
        if (titre.contains("soirée") || titre.contains("soiree")) return "🎉";
        if (titre.contains("sport")) return "⚽";
        if (titre.contains("robotique")) return "🤖";
        return "📅";
    }

    /**
     * Ouvre Google Maps avec la localisation de l'événement
     */
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

    /**
     * Ferme la fenêtre de détails
     */
    @FXML
    private void handleFermer() {
        Stage stage = (Stage) emojiLabel.getScene().getWindow();
        stage.close();
    }
}