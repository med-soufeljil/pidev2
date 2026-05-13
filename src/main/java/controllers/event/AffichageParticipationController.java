package controllers.event;

import models.event.Participation;
import models.event.Evenement;
import services.event.ParticipationService;
import services.event.EvenementService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour l'affichage et la gestion des participations aux événements
 * Permet de visualiser, filtrer, modifier et supprimer les participations
 */
public class AffichageParticipationController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private TableView<Participation> tableParticipations;    // Tableau des participations
    @FXML private TableColumn<Participation, Integer> colId;        // Colonne ID
    @FXML private TableColumn<Participation, String> colEvenement;  // Colonne nom événement
    @FXML private TableColumn<Participation, LocalDate> colDateInscription; // Colonne date
    @FXML private TableColumn<Participation, String> colStatut;     // Colonne statut
    @FXML private TableColumn<Participation, Boolean> colPresence;  // Colonne présence
    @FXML private TableColumn<Participation, Void> colAction;       // Colonne actions
    @FXML private TextField searchField;                             // Champ recherche
    @FXML private ComboBox<String> filterEventCombo;                // Filtre par événement
    @FXML private Label lblEventTitle;                               // Titre de l'événement courant

    // ==================== ATTRIBUTS ====================
    private VBox contentArea;        // Zone de contenu principale
    private final ParticipationService participationService = new ParticipationService();  // Service participation
    private final EvenementService evenementService = new EvenementService();            // Service événement
    private ObservableList<Participation> participationsList = FXCollections.observableArrayList(); // Liste observable
    private FilteredList<Participation> filteredData;              // Liste filtrée
    private Evenement currentEvent;                                 // Événement courant (si filtré)

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Définit la zone de contenu principale
     * @param contentArea Le conteneur VBox
     */
    public void setContentArea(VBox contentArea) {
        System.out.println("AffichageParticipationController: contentArea set");
        this.contentArea = contentArea;
    }

    /**
     * Définit l'événement courant et charge ses participations
     * @param event L'événement sélectionné
     */
    public void setEvent(Evenement event) {
        this.currentEvent = event;
        if (event != null) {
            lblEventTitle.setText("Participations pour: " + event.getTitre());
            chargerParticipationsParEvenement(event.getIdEvenement());
        }
    }

    /**
     * Initialisation du contrôleur
     * Configure les colonnes, les actions et charge les données
     */
    @FXML
    public void initialize() {
        System.out.println("AffichageParticipationController: initialize() called");
        configurerColonnes();          // Configure l'affichage des colonnes
        configurerColonneActions();    // Ajoute les icônes d'action
        chargerTousParticipations();   // Charge toutes les participations
        configurerRecherche();         // Configure la recherche
        chargerFiltreEvenements();     // Configure le filtre par événement
    }

    /**
     * Configure les colonnes du tableau
     * Définit comment extraire et formater les données
     */
    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_p"));

        // Colonne Événement - affiche le nom de l'événement associé
        colEvenement.setCellValueFactory(cellData -> {
            Participation participation = cellData.getValue();
            String nomEvenement;
            if (participation.getEvenement() != null && participation.getEvenement().getTitre() != null) {
                nomEvenement = participation.getEvenement().getTitre();
            } else {
                nomEvenement = "Événement inconnu (ID: " + participation.getId_e() + ")";
            }
            return new javafx.beans.property.SimpleStringProperty(nomEvenement);
        });

        // Colonne Date - avec formatage personnalisé
        colDateInscription.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        colDateInscription.setCellFactory(column -> new TableCell<Participation, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DATE_FORMATTER)); // Format dd/MM/yyyy
                }
            }
        });

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Colonne Statut - avec code couleur
        colStatut.setCellFactory(column -> new TableCell<Participation, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    // Appliquer une couleur selon le statut
                    switch (statut.toLowerCase()) {
                        case "confirmé":
                        case "confirme":
                            setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;"); // Vert
                            break;
                        case "en attente":
                            setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;"); // Jaune
                            break;
                        case "annulé":
                        case "annule":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;"); // Rouge
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Colonne Présence - avec icône texte
        colPresence.setCellValueFactory(new PropertyValueFactory<>("presence"));
        colPresence.setCellFactory(column -> new TableCell<Participation, Boolean>() {
            @Override
            protected void updateItem(Boolean presence, boolean empty) {
                super.updateItem(presence, empty);
                if (empty || presence == null) {
                    setText(null);
                } else {
                    setText(presence ? "✓ Présent" : "✗ Absent");
                    setStyle(presence ? "-fx-text-fill: #22c55e;" : "-fx-text-fill: #ef4444;");
                }
            }
        });
    }

    /**
     * Configure la colonne des actions avec des icônes SVG
     * Ajoute les icônes Modifier et Supprimer pour chaque ligne
     */
    private void configurerColonneActions() {
        Callback<TableColumn<Participation, Void>, TableCell<Participation, Void>> cellFactory = param -> new TableCell<>() {
            // Icônes SVG pour les actions
            private final SVGPath editIcon = new SVGPath();   // Icône crayon
            private final SVGPath deleteIcon = new SVGPath(); // Icône poubelle
            private final HBox pane = new HBox(15, editIcon, deleteIcon);

            {
                // Définition des chemins SVG pour les icônes
                editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");

                // Applique les styles CSS
                editIcon.getStyleClass().add("action-icon");
                editIcon.getStyleClass().add("icon-edit");
                deleteIcon.getStyleClass().add("action-icon");
                deleteIcon.getStyleClass().add("icon-delete");

                pane.setStyle("-fx-alignment: center;");

                // Action pour modifier
                editIcon.setOnMouseClicked(event -> {
                    Participation participation = getTableView().getItems().get(getIndex());
                    handleModifier(participation);
                });

                // Action pour supprimer
                deleteIcon.setOnMouseClicked(event -> {
                    Participation participation = getTableView().getItems().get(getIndex());
                    handleSupprimer(participation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane); // Affiche les icônes seulement si la cellule n'est pas vide
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    /**
     * Charge toutes les participations depuis la base de données
     */
    private void chargerTousParticipations() {
        try {
            List<Participation> list = participationService.recuperer();
            participationsList.clear();
            participationsList.addAll(list);

            filteredData = new FilteredList<>(participationsList, b -> true);
            tableParticipations.setItems(filteredData);

            System.out.println("AffichageParticipationController: " + list.size() + " participations loaded");

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les participations: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Charge les participations pour un événement spécifique
     * @param eventId L'ID de l'événement
     */
    private void chargerParticipationsParEvenement(int eventId) {
        try {
            List<Participation> list = participationService.recupererParEvenement(eventId);
            participationsList.clear();
            participationsList.addAll(list);

            filteredData = new FilteredList<>(participationsList, b -> true);
            tableParticipations.setItems(filteredData);

            System.out.println("AffichageParticipationController: " + list.size() + " participations loaded for event " + eventId);

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les participations: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Configure le filtre par événement (ComboBox)
     * Permet de filtrer les participations par événement
     */
    private void chargerFiltreEvenements() {
        try {
            // Récupère tous les événements
            List<Evenement> events = evenementService.recuperer();
            filterEventCombo.getItems().clear();
            filterEventCombo.getItems().add("Tous les événements"); // Option par défaut

            // Ajoute chaque événement au combo
            for (Evenement e : events) {
                filterEventCombo.getItems().add(e.getIdEvenement() + " - " + e.getTitre());
            }
            filterEventCombo.setValue("Tous les événements");

            // Écouteur pour le changement de sélection
            filterEventCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    if (newVal.equals("Tous les événements")) {
                        chargerTousParticipations();
                    } else {
                        int eventId = Integer.parseInt(newVal.split(" - ")[0]);
                        chargerParticipationsParEvenement(eventId);
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configure la recherche en temps réel
     * Filtre les participations selon l'ID, l'événement, le statut ou la date
     */
    private void configurerRecherche() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(participation -> {
                // Si champ vide, afficher toutes les participations
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Recherche dans l'ID
                if (String.valueOf(participation.getId_p()).contains(lowerCaseFilter)) {
                    return true;
                }
                // Recherche dans le titre de l'événement
                else if (participation.getEvenement() != null &&
                        participation.getEvenement().getTitre() != null &&
                        participation.getEvenement().getTitre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Recherche dans le statut
                else if (participation.getStatut() != null &&
                        participation.getStatut().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Recherche dans la date formatée
                else if (participation.getDateInscription() != null &&
                        participation.getDateInscription().format(DATE_FORMATTER).contains(lowerCaseFilter)) {
                    return true;
                }

                return false; // Pas de correspondance
            });
        });
    }

    /**
     * Gère la modification d'une participation
     * @param participation La participation à modifier
     */
    private void handleModifier(Participation participation) {
        try {
            // Charge la vue de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/ModifierParticipation.fxml"));
            Parent root = loader.load();

            // Configure le contrôleur
            ModifierParticipationController controller = loader.getController();
            controller.setContentArea(contentArea);
            controller.chargerDonnees(participation); // Passe les données à modifier

            // Affiche la vue
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Gère la suppression d'une participation
     * @param participation La participation à supprimer
     */
    private void handleSupprimer(Participation participation) {
        // Prépare le message de confirmation
        String nomEvenement = participation.getEvenement() != null ?
                participation.getEvenement().getTitre() :
                "cet événement";

        // Boîte de dialogue de confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        String message = "Voulez-vous vraiment supprimer cette participation pour l'événement \"" +
                nomEvenement + "\" ?";
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Supprime de la base de données
                participationService.supprimer(participation);
                // Supprime de la liste observable
                participationsList.remove(participation);
                showAlert("Succès", "Participation supprimée avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer la participation: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    /**
     * Gère l'ajout d'une nouvelle participation
     * Charge la vue d'ajout
     */
    @FXML
    private void handleAjouter() {
        try {
            // Charge la vue d'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/AjoutParticipation.fxml"));
            Parent root = loader.load();

            // Configure le contrôleur
            AjoutParticipationController controller = loader.getController();
            controller.setContentArea(contentArea);

            // Si on filtre par événement, on passe l'événement au contrôleur d'ajout
            if (currentEvent != null) {
                controller.setEvent(currentEvent);
            }

            // Affiche la vue
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'ajout", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Gère le retour à la liste des événements
     */
    @FXML
    private void handleRetour() {
        try {
            // Charge la vue des événements
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/AffichageEvenement.fxml"));
            Parent root = loader.load();

            // Configure le contrôleur
            AffichageEvenementController controller = loader.getController();
            controller.setContentArea(contentArea);

            // Affiche la vue
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchit les données affichées
     */
    @FXML
    private void handleRefresh() {
        if (currentEvent != null) {
            chargerParticipationsParEvenement(currentEvent.getIdEvenement());
        } else {
            chargerTousParticipations();
        }
    }

    /**
     * Affiche une alerte
     * @param title Titre
     * @param content Contenu
     * @param type Type d'alerte
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}