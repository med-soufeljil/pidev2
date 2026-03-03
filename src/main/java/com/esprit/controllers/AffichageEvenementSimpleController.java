package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AffichageEvenementSimpleController {

    @FXML private FlowPane evenementsFlowPane;
    @FXML private TextField searchField;
    @FXML private Button btnRecommandation;

    private EvenementService evenementService = new EvenementService();
    private ParticipationService participationService = new ParticipationService();
    private WeatherService weatherService = new WeatherService();
    private OpenStreetMapService osmService = new OpenStreetMapService();

    private List<Evenement> tousLesEvenements;
    private VBox contentArea;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HEURE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
        System.out.println("Frontoffice: contentArea set");
    }

    @FXML
    public void initialize() {
        System.out.println("Frontoffice: initialisation");
        chargerEvenements();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerEvenements(newValue);
        });

        btnRecommandation.setOnAction(e -> handleRecommandation());
    }

    private void chargerEvenements() {
        try {
            tousLesEvenements = evenementService.recuperer();

            for (Evenement evenement : tousLesEvenements) {
                int nbInscrits = participationService.compterParticipationsParEvenement(evenement.getIdEvenement());
                evenement.setNombreInscrits(nbInscrits);
            }

            afficherEvenements(tousLesEvenements);
            System.out.println("✅ " + tousLesEvenements.size() + " événements chargés");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    private void filtrerEvenements(String recherche) {
        if (tousLesEvenements == null) return;

        if (recherche == null || recherche.trim().isEmpty()) {
            afficherEvenements(tousLesEvenements);
        } else {
            String rechercheLower = recherche.toLowerCase().trim();
            List<Evenement> evenementsFiltres = tousLesEvenements.stream()
                    .filter(e -> e.getTitre().toLowerCase().contains(rechercheLower) ||
                            e.getLieu().toLowerCase().contains(rechercheLower))
                    .collect(Collectors.toList());
            afficherEvenements(evenementsFiltres);
            System.out.println("🔍 Recherche: " + recherche + " - " + evenementsFiltres.size() + " résultat(s)");
        }
    }

    private void afficherEvenements(List<Evenement> evenements) {
        evenementsFlowPane.getChildren().clear();

        for (Evenement evenement : evenements) {
            VBox carte = creerCarteEvenementFrontoffice(evenement);
            evenementsFlowPane.getChildren().add(carte);
        }

        if (evenements.isEmpty()) {
            Label aucunResultat = new Label("Aucun événement trouvé");
            aucunResultat.setStyle("-fx-font-size: 16px; -fx-text-fill: #999999;");
            evenementsFlowPane.getChildren().add(aucunResultat);
        }
    }

    private VBox creerCarteEvenementFrontoffice(Evenement evenement) {
        VBox carte = new VBox(10);
        carte.setPrefWidth(280);
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #dddddd; -fx-border-radius: 10; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        carte.setPadding(new Insets(0, 0, 10, 0));

        // ========== BANNIÈRE COLORÉE AVEC EMOJI ==========
        StackPane banniere = new StackPane();
        banniere.setPrefHeight(100);
        banniere.setStyle("-fx-background-radius: 10 10 0 0;");

        Rectangle rectangle = new Rectangle(280, 100);
        rectangle.setArcWidth(10);
        rectangle.setArcHeight(10);

        String[] couleurs = {"#3498db", "#9b59b6", "#e74c3c", "#f39c12", "#1abc9c", "#e67e22"};
        int index = (evenement.getIdEvenement() % couleurs.length);
        rectangle.setFill(Color.web(couleurs[index]));

        Label emojiLabel = new Label(getEmojiPourEvenement(evenement));
        emojiLabel.setStyle("-fx-font-size: 40px;");

        banniere.getChildren().addAll(rectangle, emojiLabel);

        // ========== CONTENU PRINCIPAL ==========
        VBox contenu = new VBox(8);
        contenu.setPadding(new Insets(10, 10, 0, 10));

        Label titreLabel = new Label(evenement.getTitre());
        titreLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titreLabel.setWrapText(true);
        titreLabel.setStyle("-fx-text-fill: #333333;");

        HBox dateBox = new HBox(5);
        Label dateIcon = new Label("📅");
        Label dateText = new Label(evenement.getDateEvenement().format(DATE_FORMATTER));
        dateText.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        dateBox.getChildren().addAll(dateIcon, dateText);

        HBox heureBox = new HBox(5);
        Label heureIcon = new Label("⏰");
        String heureDebut = evenement.getHeureDebut() != null ? evenement.getHeureDebut().format(HEURE_FORMATTER) : "--:--";
        String heureFin = evenement.getHeureFin() != null ? evenement.getHeureFin().format(HEURE_FORMATTER) : "--:--";
        Label heureText = new Label(heureDebut + " - " + heureFin);
        heureText.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        heureBox.getChildren().addAll(heureIcon, heureText);

        HBox lieuBox = new HBox(5);
        Label lieuIcon = new Label("📍");
        Label lieuText = new Label(evenement.getLieu());
        lieuText.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        lieuText.setWrapText(true);
        lieuBox.getChildren().addAll(lieuIcon, lieuText);

        HBox placesBox = new HBox(5);
        Label placesIcon = new Label("👥");
        int placesRestantes = evenement.getNombrePlacesMax() - evenement.getNombreInscrits();
        String placesText = placesRestantes + " / " + evenement.getNombrePlacesMax() + " places";
        Label placesValue = new Label(placesText);
        placesValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        if (placesRestantes <= 0) {
            placesValue.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else if (placesRestantes <= 5) {
            placesValue.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            placesValue.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
        placesBox.getChildren().addAll(placesIcon, placesValue);

        // ========== SECTION API MÉTÉO ==========
        Separator separator1 = new Separator();
        separator1.setPadding(new Insets(5, 0, 5, 0));

        VBox meteoBox = new VBox(3);
        meteoBox.setStyle("-fx-background-color: #f0f9ff; -fx-padding: 8; -fx-background-radius: 5;");

        Label meteoTitle = new Label("🌦️ Météo");
        meteoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0369a1;");

        WeatherService.WeatherInfo meteo = weatherService.getPrevisionEvenement(
                evenement.getLieu(), evenement.getDateEvenement()
        );

        HBox meteoDetails = new HBox(10);
        meteoDetails.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label meteoIcon = new Label(meteo.iconeEmoji);
        meteoIcon.setStyle("-fx-font-size: 18px;");

        VBox meteoInfos = new VBox(2);
        Label tempLabel = new Label(String.format("%.1f°C", meteo.temperature));
        tempLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label descLabel = new Label(meteo.description);
        descLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        meteoInfos.getChildren().addAll(tempLabel, descLabel);

        meteoDetails.getChildren().addAll(meteoIcon, meteoInfos);
        meteoBox.getChildren().addAll(meteoTitle, meteoDetails);

        // ========== SECTION API CARTE AMÉLIORÉE ==========
        Separator separator2 = new Separator();
        separator2.setPadding(new Insets(5, 0, 5, 0));

        VBox carteBox = new VBox(3);
        carteBox.setStyle("-fx-background-color: #f5f3ff; -fx-padding: 8; -fx-background-radius: 5;");

        Label carteTitle = new Label("🗺️ Localisation");
        carteTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #5b21b6;");

        // Tentative de géocodage
        OpenStreetMapService.Location location = null;
        try {
            location = osmService.geocode(evenement.getLieu());
        } catch (Exception e) {
            System.err.println("Erreur géocodage: " + e.getMessage());
        }

        HBox carteDetails = new HBox(10);
        carteDetails.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (location != null) {
            Label carteIcon = new Label("📍");
            carteIcon.setStyle("-fx-font-size: 18px;");

            VBox carteInfos = new VBox(2);

            String localisationText = location.city != null && !location.city.isEmpty()
                    ? location.city
                    : (location.displayName.length() > 30
                    ? location.displayName.substring(0, 30) + "..."
                    : location.displayName);

            Label villeLabel = new Label(localisationText);
            villeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

            Label coordsLabel = new Label(String.format("%.4f, %.4f", location.lat, location.lon));
            coordsLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 9px; -fx-text-fill: #666666;");

            Hyperlink osmLink = new Hyperlink("Voir sur la carte");
            osmLink.setStyle("-fx-font-size: 10px; -fx-text-fill: #3b82f6;");

            // Créer une copie finale de location pour la lambda
            final OpenStreetMapService.Location finalLocation = location;
            osmLink.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().browse(
                            java.net.URI.create(finalLocation.getOpenStreetMapUrl())
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            carteInfos.getChildren().addAll(villeLabel, coordsLabel, osmLink);
            carteDetails.getChildren().addAll(carteIcon, carteInfos);
        } else {
            Label carteIcon = new Label("📍");
            carteIcon.setStyle("-fx-font-size: 18px; -fx-opacity: 0.5;");

            VBox carteInfos = new VBox(2);
            Label nonDispo = new Label("Localisation non disponible");
            nonDispo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-style: italic;");

            // Créer une copie finale du lieu pour la lambda
            final String lieuRecherche = evenement.getLieu();

            Hyperlink searchLink = new Hyperlink("Rechercher sur Google Maps");
            searchLink.setStyle("-fx-font-size: 10px;");
            searchLink.setOnAction(e -> {
                try {
                    String encoded = URLEncoder.encode(lieuRecherche + ", Tunisie", "UTF-8");
                    java.awt.Desktop.getDesktop().browse(
                            java.net.URI.create("https://www.google.com/maps/search/?api=1&query=" + encoded)
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            carteInfos.getChildren().addAll(nonDispo, searchLink);
            carteDetails.getChildren().addAll(carteIcon, carteInfos);
        }

        carteBox.getChildren().addAll(carteTitle, carteDetails);

        // ========== BOUTON DÉTAILS ==========
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.setPadding(new Insets(5, 10, 0, 10));

        Button detailsButton = new Button("📋 Voir détails");
        detailsButton.setPrefWidth(200);
        detailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8; -fx-cursor: hand;");

        detailsButton.setOnAction(e -> handleVoirDetails(evenement));

        buttonBox.getChildren().add(detailsButton);

        contenu.getChildren().addAll(titreLabel, dateBox, heureBox, lieuBox, placesBox);
        carte.getChildren().addAll(banniere, contenu, separator1, meteoBox, separator2, carteBox, buttonBox);

        return carte;
    }

    private void handleVoirDetails(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsEvenementAvecMeteo.fxml"));
            Parent root = loader.load();

            DetailsEvenementController controller = loader.getController();
            controller.setEvenement(evenement);

            Stage stage = new Stage();
            stage.setTitle("Détails - " + evenement.getTitre());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRecommandation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RecommandationView.fxml"));
            Parent root = loader.load();

            RecommandationController controller = loader.getController();
            controller.setContentArea(contentArea); // TRÈS IMPORTANT

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les recommandations: " + e.getMessage());
            e.printStackTrace();
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private HBox creerBadgeStatut(String statut) {
        HBox statutBox = new HBox(5);
        statutBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        statutBox.setPadding(new Insets(5, 0, 5, 0));

        Label statutBadge = new Label();
        if ("en cours".equals(statut)) {
            statutBadge.setText("🔴 EN COURS");
            statutBadge.setStyle("-fx-background-color: #eab308; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
        } else if ("terminé".equals(statut)) {
            statutBadge.setText("✅ TERMINÉ");
            statutBadge.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
        } else {
            statutBadge.setText("📅 PLANIFIÉ");
            statutBadge.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
        }
        statutBox.getChildren().add(statutBadge);

        return statutBox;
    }
    // Dans AffichageEvenementSimpleController.java, ajoute cette méthode
    @FXML
    private void handleChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChatbotFrontView.fxml"));
            Parent root = loader.load();

            ChatbotFrontController controller = loader.getController();
            controller.setContentArea(contentArea);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger l'assistant: " + e.getMessage());
            e.printStackTrace();
        }
    }
}