package controllers.event;

import models.event.Evenement;
import services.event.RecommandationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur pour les recommandations d'événements
 * Affiche des suggestions personnalisées selon différents critères
 */
public class RecommandationController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private ComboBox<String> typeCombo;           // Type de recommandation
    @FXML private FlowPane cartesFlowPane;              // Grille des cartes d'événements
    @FXML private Label titreLabel;                      // Titre de la section
    @FXML private Label chargementLabel;                 // Indicateur de chargement
    @FXML private Button actualiserButton;               // Bouton d'actualisation
    @FXML private Button retourButton;                    // Bouton de retour

    // ==================== ATTRIBUTS ====================
    private final RecommandationService recommandationService = new RecommandationService();
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private final DateTimeFormatter HEURE_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private VBox contentArea;                              // Zone de contenu principale

    /**
     * Définit la zone de contenu principale
     * @param contentArea Le conteneur VBox
     */
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
        System.out.println("RecommandationController: contentArea set");
    }

    /**
     * Initialisation du contrôleur
     * Configure les options et charge les recommandations
     */
    @FXML
    public void initialize() {
        System.out.println("RecommandationController initialisé");

        // Remplit le ComboBox avec les types de recommandations
        typeCombo.getItems().addAll(
                "🔥 Les plus populaires",
                "📈 Tendances du moment",
                "📅 À venir"
        );
        typeCombo.setValue("🔥 Les plus populaires"); // Valeur par défaut

        // Écouteurs
        typeCombo.setOnAction(e -> chargerRecommandations());
        actualiserButton.setOnAction(e -> chargerRecommandations());
        retourButton.setOnAction(e -> handleRetour());

        // Chargement initial
        chargerRecommandations();
    }

    /**
     * Retour à la liste des événements
     */
    @FXML
    private void handleRetour() {
        if (contentArea == null) {
            System.err.println("❌ Erreur: contentArea est null");
            return;
        }

        try {
            // Charge la vue des événements
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/AffichageEvenementSimple.fxml"));
            Parent root = loader.load();

            // Configure le contrôleur
            AffichageEvenementSimpleController controller = loader.getController();
            controller.setContentArea(contentArea);

            // Remplace le contenu actuel
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge les recommandations selon le type sélectionné
     */
    private void chargerRecommandations() {
        cartesFlowPane.getChildren().clear();
        chargementLabel.setVisible(true); // Affiche l'indicateur de chargement

        try {
            List<Evenement> recommandations;
            String type = typeCombo.getValue();

            // Récupère les recommandations selon le type
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

            chargementLabel.setVisible(false); // Cache l'indicateur
            afficherCartes(recommandations);

        } catch (SQLException e) {
            chargementLabel.setVisible(false);
            afficherErreur("Erreur de chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche les événements sous forme de cartes
     * @param evenements Liste des événements à afficher
     */
    private void afficherCartes(List<Evenement> evenements) {
        cartesFlowPane.getChildren().clear();

        if (evenements.isEmpty()) {
            Label vide = new Label("Aucun événement trouvé pour cette catégorie");
            vide.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px; -fx-padding: 30; -fx-font-style: italic;");
            cartesFlowPane.getChildren().add(vide);
            return;
        }

        // Crée une carte pour chaque événement
        for (Evenement e : evenements) {
            VBox carte = creerCarteEvenement(e);
            cartesFlowPane.getChildren().add(carte);
        }
    }

    /**
     * Crée une carte graphique pour un événement
     * @param e L'événement à afficher
     * @return VBox contenant la carte
     */
    private VBox creerCarteEvenement(Evenement e) {
        VBox carte = new VBox(15);
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 15; -fx-border-width: 1;");
        carte.setPrefWidth(320);
        carte.setMaxWidth(320);

        // ========== EN-TÊTE AVEC COULEUR ==========
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Rectangle colorRect = new Rectangle(8, 70); // Barre colorée verticale
        colorRect.setArcWidth(5);
        colorRect.setArcHeight(5);

        // Couleur basée sur l'ID (pour varier)
        String[] couleurs = {"#3b82f6", "#8b5cf6", "#ec4899", "#f97316", "#06b6d4", "#84cc16"};
        int index = Math.abs(e.getIdEvenement() % couleurs.length);
        colorRect.setFill(Color.web(couleurs[index]));

        VBox titreBox = new VBox(5);
        Label titre = new Label(e.getTitre());
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titre.setWrapText(true);

        // ========== ÉTOILES DE POPULARITÉ ==========
        try {
            int score = recommandationService.getScorePopularite(e.getIdEvenement());
            HBox etoilesBox = new HBox(2);
            for (int i = 0; i < 5; i++) {
                Label etoile = new Label(i < score ? "⭐" : "☆");
                etoile.setStyle("-fx-font-size: 16px; -fx-text-fill: #f59e0b;");
                etoilesBox.getChildren().add(etoile);
            }
            titreBox.getChildren().addAll(titre, etoilesBox);
        } catch (SQLException ex) {
            titreBox.getChildren().add(titre); // Pas d'étoiles en cas d'erreur
        }

        header.getChildren().addAll(colorRect, titreBox);

        // ========== INFORMATIONS ==========
        VBox infos = new VBox(10);
        infos.setPadding(new Insets(10, 0, 10, 0));

        // Date
        HBox dateBox = new HBox(5);
        Label dateIcon = new Label("📅");
        Label dateValue = new Label(e.getDateEvenement() != null ?
                e.getDateEvenement().format(DATE_FORMAT) : "Date inconnue");
        dateValue.setStyle("-fx-text-fill: #475569;");
        dateBox.getChildren().addAll(dateIcon, dateValue);

        // Heure
        HBox heureBox = new HBox(5);
        Label heureIcon = new Label("⏰");
        String heureDebut = e.getHeureDebut() != null ? e.getHeureDebut().format(HEURE_FORMAT) : "--:--";
        String heureFin = e.getHeureFin() != null ? e.getHeureFin().format(HEURE_FORMAT) : "--:--";
        Label heureValue = new Label(heureDebut + " - " + heureFin);
        heureValue.setStyle("-fx-text-fill: #475569;");
        heureBox.getChildren().addAll(heureIcon, heureValue);

        // Lieu
        HBox lieuBox = new HBox(5);
        Label lieuIcon = new Label("📍");
        Label lieuValue = new Label(e.getLieu());
        lieuValue.setStyle("-fx-text-fill: #475569;");
        lieuValue.setWrapText(true);
        lieuBox.getChildren().addAll(lieuIcon, lieuValue);

        // Places disponibles (avec code couleur)
        HBox placesBox = new HBox(5);
        Label placesIcon = new Label("👥");
        int placesRestantes = e.getNombrePlacesMax() - e.getNombreInscrits();
        String placesText = placesRestantes + " places disponibles sur " + e.getNombrePlacesMax();
        Label placesValue = new Label(placesText);
        placesValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        if (placesRestantes <= 0) {
            placesValue.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;"); // Rouge (complet)
        } else if (placesRestantes <= 5) {
            placesValue.setStyle("-fx-text-fill: #f97316; -fx-font-weight: bold;"); // Orange (bientôt complet)
        } else {
            placesValue.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;"); // Vert (places)
        }
        placesBox.getChildren().addAll(placesIcon, placesValue);

        infos.getChildren().addAll(dateBox, heureBox, lieuBox, placesBox);

        // ========== BOUTON VOIR DÉTAILS ==========
        Button voirButton = new Button("🔍 Voir détails");
        voirButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10; " +
                "-fx-cursor: hand;");
        voirButton.setMaxWidth(Double.MAX_VALUE);

        final Evenement eventFinal = e;
        voirButton.setOnAction(ev -> afficherDetails(eventFinal));

        carte.getChildren().addAll(header, infos, voirButton);

        // ========== ANIMATION AU SURVOL ==========
        carte.setOnMouseEntered(ev ->
                carte.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                        "-fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.3), 15, 0, 0, 8); " +
                        "-fx-border-color: #3b82f6; -fx-border-radius: 15; -fx-border-width: 2;")
        );
        carte.setOnMouseExited(ev ->
                carte.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                        "-fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                        "-fx-border-color: #e2e8f0; -fx-border-radius: 15; -fx-border-width: 1;")
        );

        return carte;
    }

    /**
     * Affiche les détails d'un événement dans une nouvelle fenêtre
     * @param event L'événement à afficher
     */
    private void afficherDetails(Evenement event) {
        try {
            // Charge la vue des détails
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/DetailsEvenementAvecMeteo.fxml"));
            Parent root = loader.load();

            // Configure le contrôleur
            DetailsEvenementController controller = loader.getController();
            controller.setEvenement(event);

            // Crée une nouvelle fenêtre modale
            Stage stage = new Stage();
            stage.setTitle("Détails - " + event.getTitre());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails: " + e.getMessage());
        }
    }

    /**
     * Affiche un message d'erreur
     * @param message Le message d'erreur
     */
    private void afficherErreur(String message) {
        Label erreur = new Label("❌ " + message);
        erreur.setStyle("-fx-text-fill: #ef4444; -fx-padding: 30; -fx-font-size: 16px; -fx-font-weight: bold;");
        cartesFlowPane.getChildren().add(erreur);
    }

    /**
     * Affiche une alerte
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