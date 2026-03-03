package com.esprit.controllers;

import com.esprit.models.Participation;
import com.esprit.models.Evenement;
import com.esprit.services.ParticipationService;
import com.esprit.services.EvenementService;
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

public class AffichageParticipationController {

    @FXML private TableView<Participation> tableParticipations;
    @FXML private TableColumn<Participation, Integer> colId;
    @FXML private TableColumn<Participation, String> colEvenement;
    @FXML private TableColumn<Participation, LocalDate> colDateInscription;
    @FXML private TableColumn<Participation, String> colStatut;
    @FXML private TableColumn<Participation, Boolean> colPresence;
    @FXML private TableColumn<Participation, Void> colAction;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterEventCombo;
    @FXML private Label lblEventTitle;

    private VBox contentArea;
    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService evenementService = new EvenementService();
    private ObservableList<Participation> participationsList = FXCollections.observableArrayList();
    private FilteredList<Participation> filteredData;
    private Evenement currentEvent;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setContentArea(VBox contentArea) {
        System.out.println("AffichageParticipationController: contentArea set");
        this.contentArea = contentArea;
    }

    public void setEvent(Evenement event) {
        this.currentEvent = event;
        if (event != null) {
            lblEventTitle.setText("Participations pour: " + event.getTitre());
            chargerParticipationsParEvenement(event.getIdEvenement());
        }
    }

    @FXML
    public void initialize() {
        System.out.println("AffichageParticipationController: initialize() called");
        configurerColonnes();
        configurerColonneActions();
        chargerTousParticipations();
        configurerRecherche();
        chargerFiltreEvenements();
    }

    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_p"));

        // Configuration de la colonne Événement pour afficher le nom - CORRIGÉ
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

        // Formatage de la date
        colDateInscription.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        colDateInscription.setCellFactory(column -> new TableCell<Participation, LocalDate>() {
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

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Style pour le statut
        colStatut.setCellFactory(column -> new TableCell<Participation, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    switch (statut.toLowerCase()) {
                        case "confirmé":
                        case "confirme":
                            setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                            break;
                        case "en attente":
                            setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
                            break;
                        case "annulé":
                        case "annule":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

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

    private void configurerColonneActions() {
        Callback<TableColumn<Participation, Void>, TableCell<Participation, Void>> cellFactory = param -> new TableCell<>() {
            private final SVGPath editIcon = new SVGPath();
            private final SVGPath deleteIcon = new SVGPath();
            private final HBox pane = new HBox(15, editIcon, deleteIcon);

            {
                editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");

                editIcon.getStyleClass().add("action-icon");
                editIcon.getStyleClass().add("icon-edit");
                deleteIcon.getStyleClass().add("action-icon");
                deleteIcon.getStyleClass().add("icon-delete");

                pane.setStyle("-fx-alignment: center;");

                editIcon.setOnMouseClicked(event -> {
                    Participation participation = getTableView().getItems().get(getIndex());
                    handleModifier(participation);
                });

                deleteIcon.setOnMouseClicked(event -> {
                    Participation participation = getTableView().getItems().get(getIndex());
                    handleSupprimer(participation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
        colAction.setCellFactory(cellFactory);
    }

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

    private void chargerFiltreEvenements() {
        try {
            List<Evenement> events = evenementService.recuperer();
            filterEventCombo.getItems().clear();
            filterEventCombo.getItems().add("Tous les événements");
            for (Evenement e : events) {
                filterEventCombo.getItems().add(e.getIdEvenement() + " - " + e.getTitre());
            }
            filterEventCombo.setValue("Tous les événements");

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

    private void configurerRecherche() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(participation -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(participation.getId_p()).contains(lowerCaseFilter)) {
                    return true;
                } else if (participation.getEvenement() != null &&
                        participation.getEvenement().getTitre() != null &&
                        participation.getEvenement().getTitre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (participation.getStatut() != null &&
                        participation.getStatut().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (participation.getDateInscription() != null &&
                        participation.getDateInscription().format(DATE_FORMATTER).contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });
    }

    private void handleModifier(Participation participation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierParticipation.fxml"));
            Parent root = loader.load();

            ModifierParticipationController controller = loader.getController();
            controller.setContentArea(contentArea);
            controller.chargerDonnees(participation);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleSupprimer(Participation participation) {
        String nomEvenement = participation.getEvenement() != null ?
                participation.getEvenement().getTitre() :
                "cet événement";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        String message = "Voulez-vous vraiment supprimer cette participation pour l'événement \"" +
                nomEvenement + "\" ?";
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                participationService.supprimer(participation);
                participationsList.remove(participation);
                showAlert("Succès", "Participation supprimée avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer la participation: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutParticipation.fxml"));
            Parent root = loader.load();

            AjoutParticipationController controller = loader.getController();
            controller.setContentArea(contentArea);

            // If we're filtering by an event, pass it to the add controller
            if (currentEvent != null) {
                controller.setEvent(currentEvent);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'ajout", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageEvenement.fxml"));
            Parent root = loader.load();

            AffichageEvenementController controller = loader.getController();
            controller.setContentArea(contentArea);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        if (currentEvent != null) {
            chargerParticipationsParEvenement(currentEvent.getIdEvenement());
        } else {
            chargerTousParticipations();
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