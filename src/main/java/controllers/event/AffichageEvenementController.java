package controllers.event;

import models.event.Evenement;
import services.event.EvenementService;
import services.event.ExportService;
import services.event.ParticipationService;
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
import javafx.util.Callback;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AffichageEvenementController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private TableView<Evenement> tableEvenements;        // Tableau principal des événements
    @FXML private TableColumn<Evenement, String> colTitre;     // Colonne du titre
    @FXML private TableColumn<Evenement, LocalDate> colDate;   // Colonne de la date
    @FXML private TableColumn<Evenement, LocalTime> colHeureDebut;  // Colonne heure début
    @FXML private TableColumn<Evenement, LocalTime> colHeureFin;    // Colonne heure fin
    @FXML private TableColumn<Evenement, String> colLieu;      // Colonne lieu
    @FXML private TableColumn<Evenement, Integer> colPlaces;   // Colonne nombre de places
    @FXML private TableColumn<Evenement, String> colStatut;    // Colonne statut
    @FXML private TableColumn<Evenement, Integer> colInscrits; // Colonne nombre d'inscrits
    @FXML private TableColumn<Evenement, Void> colAction;      // Colonne des boutons d'action
    @FXML private TextField searchField;                        // Champ de recherche

    // ==================== ATTRIBUTS ====================
    private VBox contentArea;                                   // Zone de contenu principale (pour navigation)
    private final EvenementService evenementService = new EvenementService();           // Service événement
    private final ParticipationService participationService = new ParticipationService() {
        @Override
        public void supprimer(int id) throws SQLException {

        }
    }; // Service participation
    private final ExportService exportService = new ExportService();                     // Service d'export
    private ObservableList<Evenement> evenementsList = FXCollections.observableArrayList(); // Liste observable
    private FilteredList<Evenement> filteredData;               // Liste filtrée (pour recherche)

    // Formatters pour l'affichage des dates et heures
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Définit la zone de contenu principale (utilisée pour la navigation)
     * @param contentArea Le conteneur VBox où afficher les vues
     */
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    /**
     * Méthode d'initialisation appelée automatiquement après le chargement du FXML
     * Configure le tableau, charge les données et met à jour les statuts
     */
    @FXML
    public void initialize() {
        System.out.println("Backoffice: initialisation");

        // Mettre à jour les statuts au démarrage (planifié, en cours, terminé)
        try {
            evenementService.mettreAJourStatutsSimplifie();
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour statuts: " + e.getMessage());
        }

        configurerColonnes();      // Configure l'affichage des colonnes
        configurerColonneActions(); // Ajoute les boutons d'action
        chargerDonnees();          // Charge les données depuis la BDD
        configurerRecherche();      // Configure la recherche en temps réel
    }

    /**
     * Configure l'affichage et le formatage des colonnes du tableau
     * Définit comment extraire et afficher les données de chaque propriété
     */
    private void configurerColonnes() {
        // Colonne Titre - liaison simple avec la propriété
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));

        // Colonne Date
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateEvenement"));
        colDate.setCellFactory(column -> new TableCell<Evenement, LocalDate>() {
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

        // Colonne Heure de début - formatage HH:mm
        colHeureDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colHeureDebut.setCellFactory(column -> new TableCell<Evenement, LocalTime>() {
            @Override
            protected void updateItem(LocalTime heure, boolean empty) {
                super.updateItem(heure, empty);
                if (empty || heure == null) {
                    setText(null);
                } else {
                    setText(heure.format(TIME_FORMATTER));
                }
            }
        });

        // Colonne Heure de fin - formatage HH:mm
        colHeureFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        colHeureFin.setCellFactory(column -> new TableCell<Evenement, LocalTime>() {
            @Override
            protected void updateItem(LocalTime heure, boolean empty) {
                super.updateItem(heure, empty);
                if (empty || heure == null) {
                    setText(null);
                } else {
                    setText(heure.format(TIME_FORMATTER));
                }
            }
        });

        // Colonnes simples (liaison directe)
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colPlaces.setCellValueFactory(new PropertyValueFactory<>("nombrePlacesMax"));

        // ✅ Colonne Statut avec code couleur
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(column -> new TableCell<Evenement, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    // Appliquer une couleur selon le statut
                    switch (statut) {
                        case "planifie": // Bleu pour planifié
                            setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                            break;
                        case "en cours": // Jaune pour en cours
                            setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
                            break;
                        case "termine": // Vert pour terminé
                            setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Colonne Inscrits - affiche "inscrits / places totales"
        colInscrits.setCellValueFactory(new PropertyValueFactory<>("nombreInscrits"));
        colInscrits.setCellFactory(column -> new TableCell<Evenement, Integer>() {
            @Override
            protected void updateItem(Integer inscrits, boolean empty) {
                super.updateItem(inscrits, empty);
                if (empty || inscrits == null || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                } else {
                    Evenement evenement = getTableView().getItems().get(getIndex());
                    int total = evenement.getNombrePlacesMax();
                    setText(inscrits + " / " + total); // Format: "5 / 20"
                }
            }
        });
    }

    /**
     * Configure la colonne des actions avec les boutons Modifier, Supprimer et Voir participations
     * Crée une cellule personnalisée contenant les trois boutons
     */
    private void configurerColonneActions() {
        Callback<TableColumn<Evenement, Void>, TableCell<Evenement, Void>> cellFactory =
                new Callback<TableColumn<Evenement, Void>, TableCell<Evenement, Void>>() {
                    @Override
                    public TableCell<Evenement, Void> call(final TableColumn<Evenement, Void> param) {
                        return new TableCell<Evenement, Void>() {
                            // Création des boutons
                            private final Button editButton = new Button("✏️ Modifier");
                            private final Button deleteButton = new Button("🗑️ Supprimer");
                            private final Button participationsButton = new Button("👥 Participations");
                            private final HBox pane = new HBox(10, editButton, deleteButton, participationsButton);

                            {
                                // Stylisation des boutons
                                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 3;");
                                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 3;");
                                participationsButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 3;");

                                // Action du bouton Modifier
                                editButton.setOnAction(event -> {
                                    Evenement evenement = getTableView().getItems().get(getIndex());
                                    handleModifier(evenement);
                                });

                                // Action du bouton Supprimer
                                deleteButton.setOnAction(event -> {
                                    Evenement evenement = getTableView().getItems().get(getIndex());
                                    handleSupprimer(evenement);
                                });

                                // Action du bouton Voir participations
                                participationsButton.setOnAction(event -> {
                                    Evenement evenement = getTableView().getItems().get(getIndex());
                                    handleVoirParticipations(evenement);
                                });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                setGraphic(empty ? null : pane); // Affiche les boutons seulement si la cellule n'est pas vide
                            }
                        };
                    }
                };
        colAction.setCellFactory(cellFactory);
    }

    /**
     * Charge les données des événements depuis la base de données
     * Récupère la liste des événements et le nombre d'inscrits pour chacun
     */
    private void chargerDonnees() {
        try {
            // Récupère tous les événements
            List<Evenement> list = evenementService.recuperer();
            System.out.println("📊 Nombre d'événements chargés: " + list.size());

            // Pour chaque événement, compte le nombre de participants
            for (Evenement evenement : list) {
                int nbInscrits = participationService.compterParticipationsParEvenement(evenement.getIdEvenement());
                evenement.setNombreInscrits(nbInscrits);
            }

            // Met à jour la liste observable
            evenementsList.clear();
            evenementsList.addAll(list);

            // Initialise la liste filtrée (pour la recherche)
            filteredData = new FilteredList<>(evenementsList, b -> true);
            tableEvenements.setItems(filteredData);

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Configure la recherche en temps réel sur le champ de recherche
     * Filtre les événements selon le titre, lieu, statut ou date
     */
    private void configurerRecherche() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(evenement -> {
                // Si champ vide, afficher tous les événements
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Recherche dans le titre
                if (evenement.getTitre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Recherche dans le lieu
                else if (evenement.getLieu().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Recherche dans le statut
                else if (evenement.getStatut().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Recherche dans la date formatée
                else if (evenement.getDateEvenement() != null &&
                        evenement.getDateEvenement().format(DATE_FORMATTER).contains(lowerCaseFilter)) {
                    return true;
                }

                return false; // Pas de correspondance
            });
        });
    }

    /**
     * Gère l'action de modification d'un événement
     * Charge la vue de modification et y passe l'événement sélectionné
     * @param evenement L'événement à modifier
     */
    private void handleModifier(Evenement evenement) {
        try {
            // Charge la vue de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/ModifierEvenement.fxml"));
            Parent root = loader.load();

            // Récupère le contrôleur et lui passe les données nécessaires
            ModifierEvenementController controller = loader.getController();
            controller.setContentArea(contentArea);
            controller.chargerDonnees(evenement); // Prépare le formulaire avec les données existantes

            // Remplace le contenu actuel par la vue de modification
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Gère la suppression d'un événement
     * Demande une confirmation avant de supprimer
     * @param evenement L'événement à supprimer
     */
    private void handleSupprimer(Evenement evenement) {
        // Boîte de dialogue de confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer l'événement : " + evenement.getTitre() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Supprime de la base de données
                evenementService.supprimer(evenement);
                // Supprime de la liste observable (mise à jour automatique du tableau)
                evenementsList.remove(evenement);
                showAlert("Succès", "Événement supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer l'événement: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    /**
     * Gère l'affichage des participations pour un événement
     * Charge la vue des participations et y passe l'événement sélectionné
     * @param evenement L'événement dont on veut voir les participations
     */
    private void handleVoirParticipations(Evenement evenement) {
        try {
            // Charge la vue des participations
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/AffichageParticipation.fxml"));
            Parent root = loader.load();

            // Configure le contrôleur
            AffichageParticipationController controller = loader.getController();
            controller.setContentArea(contentArea);
            controller.setEvent(evenement); // Passe l'événement pour filtrer les participations

            // Affiche la vue
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger les participations", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Gère l'ajout d'un nouvel événement
     * Charge la vue d'ajout d'événement
     */
    @FXML
    private void handleAjouter() {
        try {
            // Charge la vue d'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/AjoutEvenement.fxml"));
            Parent root = loader.load();

            // Configure le contrôleur
            AjoutEvenementController controller = loader.getController();
            controller.setContentArea(contentArea);

            // Affiche la vue
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'ajout", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchit les données du tableau
     * Met à jour les statuts puis recharge les données
     */
    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Rafraîchissement des données");

        // Met à jour les statuts (planifié/en cours/terminé)
        try {
            evenementService.mettreAJourStatutsSimplifie();
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour statuts: " + e.getMessage());
        }

        // Recharge les données
        chargerDonnees();
    }

    // ==================== MÉTHODES D'EXPORT ====================

    /**
     * Exporte tous les événements au format PDF standard
     * Ouvre une boîte de dialogue pour choisir l'emplacement du fichier
     */
    @FXML
    private void handleExportPDFStandard() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en PDF (standard)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("evenements_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                exportService.exportEvenementsPDF(file.getAbsolutePath());
                showAlert("Succès", "Export PDF standard créé : " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Export échoué : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    /**
     * Exporte les statistiques avancées au format PDF
     * Ouvre une boîte de dialogue pour choisir l'emplacement du fichier
     */
    @FXML
    private void handleExportPDFStatistiques() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en PDF (statistiques avancées)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("statistiques_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                exportService.exportStatistiquesPDF(file.getAbsolutePath());
                showAlert("Succès", "Export PDF statistiques créé : " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Export échoué : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    /**
     * Exporte les événements filtrés par statut au format PDF
     * Demande d'abord le statut à filtrer, puis l'emplacement du fichier
     */
    @FXML
    private void handleExportPDFFiltre() {
        // Dialogue pour choisir le statut
        ChoiceDialog<String> dialog = new ChoiceDialog<>("planifie", "planifie", "en cours", "termine");
        dialog.setTitle("Filtrer par statut");
        dialog.setHeaderText("Choisir le statut des événements à exporter");
        dialog.setContentText("Statut:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(statut -> {

            // Vérifie s'il y a des événements avec ce statut
            try {
                List<Evenement> allEvents = evenementService.recuperer();
                long count = allEvents.stream().filter(e -> statut.equals(e.getStatut())).count();
                System.out.println("🔍 Événements avec statut '" + statut + "' : " + count);

                if (count == 0) {
                    // Aucun événement trouvé
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Attention");
                    alert.setHeaderText(null);
                    alert.setContentText("Aucun événement avec le statut '" + statut + "' n'a été trouvé.");
                    alert.showAndWait();
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Choix de l'emplacement du fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter en PDF (filtré)");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf")
            );
            fileChooser.setInitialFileName("evenements_" + statut + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                try {
                    exportService.exportEvenementsFiltresPDF(file.getAbsolutePath(), statut);
                    showAlert("Succès", "Export PDF filtré créé : " + file.getName(), Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Erreur", "Export échoué : " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Exporte les statistiques avancées au format Excel
     * Ouvre une boîte de dialogue pour choisir l'emplacement du fichier
     */
    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en Excel (statistiques avancées)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx")
        );
        fileChooser.setInitialFileName("statistiques_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                exportService.exportStatistiquesExcel(file.getAbsolutePath());
                showAlert("Succès", "Export Excel avancé créé : " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Export échoué : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    /**
     * Exporte les événements au format CSV
     * Ouvre une boîte de dialogue pour choisir l'emplacement du fichier
     */
    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier CSV", "*.csv")
        );
        fileChooser.setInitialFileName("evenements_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                exportService.exportEvenementsCSV(file.getAbsolutePath());
                showAlert("Succès", "Export CSV créé : " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Export échoué : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    /**
     * Navigue vers le dashboard
     * Charge et affiche la vue du dashboard
     */
    @FXML
    private void handleDashboard() {
        try {
            // Charge la vue du dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/DashboardView.fxml"));
            Parent root = loader.load();

            // Remplace le contenu actuel par le dashboard
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Méthode utilitaire pour afficher des alertes
     * @param title Titre de l'alerte
     * @param content Contenu du message
     * @param type Type d'alerte (INFO, ERROR, WARNING, CONFIRMATION)
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}