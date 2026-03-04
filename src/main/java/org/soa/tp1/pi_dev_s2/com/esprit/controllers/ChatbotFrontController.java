package org.soa.tp1.pi_dev_s2.com.esprit.controllers;

import org.soa.tp1.pi_dev_s2.com.esprit.services.TicketmasterService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatbotFrontController {

    @FXML private VBox messagesContainer;
    @FXML private TextField inputField;
    @FXML private Button sendButton;
    @FXML private Button resetButton;
    @FXML private ScrollPane scrollPane;
    @FXML private Label typingIndicator;

    private TicketmasterService ticketmasterService;
    private VBox contentArea;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        // Initialiser le service Ticketmaster
        ticketmasterService = new TicketmasterService();

        // Message de bienvenue
        ajouterMessageBot("👋 **Bonjour !** Je suis ton assistant pour trouver des événements !");
        ajouterMessageBot("Je peux rechercher des **concerts**, **festivals**, **matchs sportifs** et **spectacles** grâce à l'API Ticketmaster !\n\n" +
                "**Exemples :**\n" +
                "• 'Concerts à Tunis'\n" +
                "• 'Événements sportifs'\n" +
                "• 'Festivals de musique'\n" +
                "• 'Que faire ce weekend ?'");

        // Action du bouton Envoyer
        sendButton.setOnAction(e -> envoyerMessage());

        // Action de la touche Entrée
        inputField.setOnAction(e -> envoyerMessage());

        // Action du bouton Réinitialiser
        resetButton.setOnAction(e -> resetConversation());

        // Focus sur le champ de saisie
        Platform.runLater(() -> inputField.requestFocus());
    }

    @FXML
    private void envoyerMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        // Afficher le message de l'utilisateur
        ajouterMessageUtilisateur(message);
        inputField.clear();

        // Afficher l'indicateur de frappe
        typingIndicator.setVisible(true);

        // Désactiver les contrôles pendant le traitement
        inputField.setDisable(true);
        sendButton.setDisable(true);
        resetButton.setDisable(true);

        // Traiter la réponse en arrière-plan
        new Thread(() -> {
            String reponse;
            String messageLower = message.toLowerCase();

            // Détection des requêtes Ticketmaster
            if (messageLower.contains("concert") ||
                    messageLower.contains("evenement") ||
                    messageLower.contains("event") ||
                    messageLower.contains("spectacle") ||
                    messageLower.contains("festival") ||
                    messageLower.contains("sport") ||
                    messageLower.contains("match") ||
                    messageLower.contains("ce weekend") ||
                    messageLower.contains("ce week-end") ||
                    messageLower.contains("aujourd'hui") ||
                    messageLower.contains("demain")) {

                // Extraire la ville si mentionnée
                String city = null;
                String[] villes = {"tunis", "sfax", "sousse", "nabeul", "hammamet", "monastir", "bizerte", "ariana", "ben arous", "manouba"};
                for (String v : villes) {
                    if (messageLower.contains(v)) {
                        city = v.substring(0, 1).toUpperCase() + v.substring(1);
                        break;
                    }
                }

                // Extraire le mot-clé
                String keyword = null;
                if (messageLower.contains("concert") || messageLower.contains("musique") || messageLower.contains("rock") || messageLower.contains("jazz") || messageLower.contains("rap")) {
                    keyword = "music";
                    if (messageLower.contains("rock")) keyword = "rock";
                    else if (messageLower.contains("jazz")) keyword = "jazz";
                    else if (messageLower.contains("rap")) keyword = "rap";
                } else if (messageLower.contains("sport") || messageLower.contains("match") || messageLower.contains("foot") || messageLower.contains("basket") || messageLower.contains("tennis")) {
                    keyword = "sports";
                    if (messageLower.contains("foot")) keyword = "soccer";
                    else if (messageLower.contains("basket")) keyword = "basketball";
                    else if (messageLower.contains("tennis")) keyword = "tennis";
                } else if (messageLower.contains("festival")) {
                    keyword = "festival";
                } else if (messageLower.contains("theatre") || messageLower.contains("théâtre")) {
                    keyword = "theatre";
                } else if (messageLower.contains("famille") || messageLower.contains("enfant")) {
                    keyword = "family";
                }

                // Rechercher les événements
                List<TicketmasterService.TicketmasterEvent> events =
                        ticketmasterService.searchEvents(keyword, city, "TN,FR,IT,ES");

                if (events.isEmpty()) {
                    // Deuxième tentative sans filtre de pays
                    events = ticketmasterService.searchEvents(keyword, city, null);
                }

                if (events.isEmpty()) {
                    reponse = "😕 Désolé, je n'ai trouvé aucun événement correspondant à ta recherche.\n\n" +
                            "**Suggestions :**\n" +
                            "• Essaie avec une autre ville (Tunis, Sousse, Sfax)\n" +
                            "• Cherche par catégorie (concert, sport, festival)\n" +
                            "• Ou demande 'Concerts' tout simplement";
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("🎉 **Événements trouvés :**\n\n");

                    int count = Math.min(5, events.size()); // Afficher max 5 événements
                    for (int i = 0; i < count; i++) {
                        TicketmasterService.TicketmasterEvent event = events.get(i);
                        sb.append("**").append(i+1).append(".** ").append(event.getShortDisplay()).append("\n\n");
                    }

                    if (events.size() > 5) {
                        sb.append("... et ").append(events.size() - 5).append(" autres événements.\n");
                    }

                    sb.append("\n💡 **Astuce :** Demande-moi 'détails sur [nom]' pour plus d'informations !");
                    reponse = sb.toString();
                }

            } else if (messageLower.contains("détail") || messageLower.contains("detail") ||
                    messageLower.contains("info sur") || messageLower.contains("plus sur")) {

                // Recherche par nom approximatif (simplifié)
                String searchTerm = message.replaceAll("(?i).*?(détail|detail|info sur|plus sur)\\s*", "").trim();

                if (searchTerm.isEmpty()) {
                    reponse = "❓ Pour voir les détails d'un événement, précise son nom.\n" +
                            "Exemple : 'détails sur concert à Tunis'";
                } else {
                    List<TicketmasterService.TicketmasterEvent> events =
                            ticketmasterService.searchEvents(searchTerm, null, "TN,FR,IT,ES");

                    if (events.isEmpty()) {
                        reponse = "😕 Désolé, je n'ai pas trouvé d'événement correspondant à '" + searchTerm + "'.";
                    } else {
                        TicketmasterService.TicketmasterEvent event = events.get(0);
                        reponse = "📋 **Détails de l'événement :**\n\n" + event.getDetailedDisplay();
                    }
                }

            } else if (messageLower.contains("aide") || messageLower.contains("help") ||
                    messageLower.contains("commande") || messageLower.contains("commandes")) {

                reponse = "🤖 **Commandes disponibles :**\n\n" +
                        "🎵 **Musique :** 'concerts', 'festival de musique', 'jazz à Tunis'\n" +
                        "⚽ **Sport :** 'match de foot', 'événements sportifs', 'tennis'\n" +
                        "🎭 **Culture :** 'spectacles', 'théâtre', 'famille'\n" +
                        "📍 **Lieux :** 'événements à Sousse', 'concerts à Paris'\n" +
                        "📅 **Période :** 'ce weekend', 'aujourd'hui', 'demain'\n" +
                        "ℹ️ **Détails :** 'détails sur [nom]'";

            } else if (messageLower.contains("bonjour") || messageLower.contains("salut") ||
                    messageLower.contains("hello") || messageLower.contains("coucou")) {

                reponse = "👋 Bonjour ! Comment puis-je t'aider à trouver des événements aujourd'hui ?";

            } else if (messageLower.contains("merci") || messageLower.contains("thanks")) {

                reponse = "😊 Avec plaisir ! N'hésite pas si tu as d'autres questions.";

            } else {
                reponse = "❓ Je ne suis pas sûr de comprendre. Je peux t'aider à trouver des **événements** !\n\n" +
                        "**Exemples :**\n" +
                        "• 'Concerts à Tunis'\n" +
                        "• 'Matchs de foot ce weekend'\n" +
                        "• 'Festivals en France'\n" +
                        "• 'Spectacles à Paris'\n\n" +
                        "Tape 'aide' pour voir toutes les commandes.";
            }

            final String finalReponse = reponse;
            Platform.runLater(() -> {
                ajouterMessageBot(finalReponse);
                typingIndicator.setVisible(false);
                inputField.setDisable(false);
                sendButton.setDisable(false);
                resetButton.setDisable(false);
                inputField.requestFocus();
            });

        }).start();
    }

    @FXML
    private void resetConversation() {
        messagesContainer.getChildren().clear();

        // Remettre les messages de bienvenue
        ajouterMessageBot("👋 **Bonjour !** Je suis ton assistant pour trouver des événements !");
        ajouterMessageBot("Je peux rechercher des **concerts**, **festivals**, **matchs sportifs** et **spectacles** grâce à l'API Ticketmaster !\n\n" +
                "**Exemples :**\n" +
                "• 'Concerts à Tunis'\n" +
                "• 'Événements sportifs'\n" +
                "• 'Festivals de musique'\n" +
                "• 'Que faire ce weekend ?'");
    }

    private void ajouterMessageUtilisateur(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 10, 5, 50));

        VBox messageContent = new VBox(3);
        messageContent.setAlignment(Pos.CENTER_RIGHT);

        // Heure
        Label timeLabel = new Label(LocalTime.now().format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
        timeLabel.setAlignment(Pos.CENTER_RIGHT);

        // Message
        Text text = new Text(message);
        text.setFont(Font.font("System", 13));

        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 15 15 5 15; -fx-padding: 10;");
        textFlow.setMaxWidth(400);

        // Forcer la couleur du texte en blanc
        text.setStyle("-fx-fill: white;");

        messageContent.getChildren().addAll(textFlow, timeLabel);
        messageBox.getChildren().add(messageContent);

        messagesContainer.getChildren().add(messageBox);

        // Auto-scroll vers le bas
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private void ajouterMessageBot(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 50, 5, 10));

        // Avatar
        Label avatar = new Label("🤖");
        avatar.setStyle("-fx-font-size: 32px; -fx-padding: 0 10 0 0;");

        VBox messageContent = new VBox(3);

        // Heure
        Label timeLabel = new Label(LocalTime.now().format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        // Message
        Text text = new Text(message);
        text.setFont(Font.font("System", 13));

        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 15 15 15 5; -fx-padding: 12;");
        textFlow.setMaxWidth(400);

        messageContent.getChildren().addAll(textFlow, timeLabel);
        messageBox.getChildren().addAll(avatar, messageContent);

        messagesContainer.getChildren().add(messageBox);

        // Auto-scroll vers le bas
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    @FXML
    private void handleSuggestionEvenements() {
        inputField.setText("Quels sont les événements à venir cette semaine ?");
        envoyerMessage();
    }

    @FXML
    private void handleSuggestionInscription() {
        inputField.setText("Comment puis-je m'inscrire à un événement ?");
        envoyerMessage();
    }

    @FXML
    private void handleSuggestionMeteo() {
        inputField.setText("Quelle est la météo prévue pour les prochains événements ?");
        envoyerMessage();
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageEvenementSimple.fxml"));
            Parent root = loader.load();

            AffichageEvenementSimpleController controller = loader.getController();
            controller.setContentArea(contentArea);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la liste des événements.");
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