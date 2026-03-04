package org.soa.tp1.pi_dev_s2.com.esprit.controllers;

import org.soa.tp1.pi_dev_s2.com.esprit.models.Evenement;
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
import org.soa.tp1.pi_dev_s2.com.esprit.services.ExportService;
import org.soa.tp1.pi_dev_s2.com.esprit.services.EvenementService;
import org.soa.tp1.pi_dev_s2.com.esprit.services.ParticipationService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AffichageEvenementController {

    @FXML private TableView<Evenement> tableEvenements;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, LocalDate> colDate;
    @FXML private TableColumn<Evenement, LocalTime> colHeureDebut;
    @FXML private TableColumn<Evenement, LocalTime> colHeureFin;
    @FXML private TableColumn<Evenement, String> colLieu;
    @FXML private TableColumn<Evenement, Integer> colPlaces;
    @FXML private TableColumn<Evenement, String> colStatut;
    @FXML private TableColumn<Evenement, Integer> colInscrits;
    @FXML private TableColumn<Evenement, Void> colAction;
    @FXML private TextField searchField;

    private VBox contentArea;
    private final EvenementService evenementService = new EvenementService();
    private final ParticipationService participationService = new ParticipationService();
    private final ExportService exportService = new ExportService();
    private ObservableList<Evenement> evenementsList = FXCollections.observableArrayList();
    private FilteredList<Evenement> filteredData;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        System.out.println("Backoffice: initialisation");

        // Mettre à jour les statuts au démarrage
        try {
            evenementService.mettreAJourStatutsSimplifie();
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour statuts: " + e.getMessage());
        }

        configurerColonnes();
        configurerColonneActions();
        chargerDonnees();
        configurerRecherche();
    }

    private void configurerColonnes() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));

        colDate.setCellValueFactory(new PropertyValueFactory<>("dateEvenement"));
        colDate.setCellFactory(column -> new TableCell<Evenement, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DATE_FORMATTER));
                }
            }
        });

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

        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colPlaces.setCellValueFactory(new PropertyValueFactory<>("nombrePlacesMax"));

        // Colonne Statut avec couleurs
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
                    switch (statut) {
                        case "planifié":
                            setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                            break;
                        case "en cours":
                            setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
                            break;
                        case "terminé":
                            setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

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
                    setText(inscrits + " / " + total);
                }
            }
        });
    }

    private void configurerColonneActions() {
        Callback<TableColumn<Evenement, Void>, TableCell<Evenement, Void>> cellFactory =
                new Callback<TableColumn<Evenement, Void>, TableCell<Evenement, Void>>() {
                    @Override
                    public TableCell<Evenement, Void> call(final TableColumn<Evenement, Void> param) {
                        return new TableCell<Evenement, Void>() {
                            private final Button editButton = new Button("✏️ Modifier");
                            private final Button deleteButton = new Button("🗑️ Supprimer");
                            private final Button participationsButton = new Button("👥 Participations");
                            private final HBox pane = new HBox(10, editButton, deleteButton, participationsButton);

                            {
                                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 3;");
                                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 3;");
                                participationsButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 3;");

                                editButton.setOnAction(event -> {
                                    Evenement evenement = getTableView().getItems().get(getIndex());
                                    handleModifier(evenement);
                                });

                                deleteButton.setOnAction(event -> {
                                    Evenement evenement = getTableView().getItems().get(getIndex());
                                    handleSupprimer(evenement);
                                });

                                participationsButton.setOnAction(event -> {
                                    Evenement evenement = getTableView().getItems().get(getIndex());
                                    handleVoirParticipations(evenement);
                                });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                setGraphic(empty ? null : pane);
                            }
                        };
                    }
                };
        colAction.setCellFactory(cellFactory);
    }

    private void chargerDonnees() {
        try {
            List<Evenement> list = evenementService.recuperer();

            for (Evenement evenement : list) {
                int nbInscrits = participationService.compterParticipationsParEvenement(evenement.getIdEvenement());
                evenement.setNombreInscrits(nbInscrits);
            }

            evenementsList.clear();
            evenementsList.addAll(list);

            filteredData = new FilteredList<>(evenementsList, b -> true);
            tableEvenements.setItems(filteredData);

            System.out.println("📊 " + list.size() + " événements chargés");

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void configurerRecherche() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(evenement -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (evenement.getTitre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (evenement.getLieu().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (evenement.getStatut().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (evenement.getDateEvenement() != null &&
                        evenement.getDateEvenement().format(DATE_FORMATTER).contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });
    }

    private void handleModifier(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvenement.fxml"));
            Parent root = loader.load();

            ModifierEvenementController controller = loader.getController();
            controller.setContentArea(contentArea);
            controller.chargerDonnees(evenement);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleSupprimer(Evenement evenement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer l'événement : " + evenement.getTitre() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                evenementService.supprimer(evenement);
                evenementsList.remove(evenement);
                showAlert("Succès", "Événement supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer l'événement: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void handleVoirParticipations(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageParticipation.fxml"));
            Parent root = loader.load();

            AffichageParticipationController controller = loader.getController();
            controller.setContentArea(contentArea);
            controller.setEvent(evenement);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger les participations", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutEvenement.fxml"));
            Parent root = loader.load();

            AjoutEvenementController controller = loader.getController();
            controller.setContentArea(contentArea);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'ajout", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Rafraîchissement des données");

        // Mettre à jour les statuts avant de rafraîchir
        try {
            evenementService.mettreAJourStatutsSimplifie();
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour statuts: " + e.getMessage());
        }

        chargerDonnees();
    }

    @FXML
    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("evenements_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                exportService.exportEvenementsPDF(file.getAbsolutePath());
                showAlert("Succès", "Export PDF créé : " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Export échoué : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx")
        );
        fileChooser.setInitialFileName("statistiques_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                exportService.exportStatistiquesExcel(file.getAbsolutePath());
                showAlert("Succès", "Export Excel créé : " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Export échoué : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardView.fxml"));
            Parent root = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}