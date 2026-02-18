package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Candidat;
import models.Offre;
import models.Recrutement;
import services.CandidatService;
import services.OffreService;
import services.RecrutementService;

import java.sql.SQLException;

public class RecrutementController {

    @FXML private ComboBox<Offre> comboOffre;
    @FXML private ComboBox<Candidat> comboCandidat;
    @FXML private TableView<Recrutement> tableRecrutement;
    @FXML private TableColumn<Recrutement, Integer> colId;
    @FXML private TableColumn<Recrutement, String> colOffre;
    @FXML private TableColumn<Recrutement, String> colCandidat;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private final RecrutementService service = new RecrutementService();
    private final CandidatService candidatService = new CandidatService();
    private final OffreService offreService = new OffreService();

    private final ObservableList<Recrutement> list = FXCollections.observableArrayList();
    private final ObservableList<Candidat> listCandidat = FXCollections.observableArrayList();
    private final ObservableList<Offre> listOffre = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("idRec"));
        colOffre.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
        colCandidat.setCellValueFactory(new PropertyValueFactory<>("nomCandidat"));

        tableRecrutement.setItems(list);

        loadComboBox();
        loadTable();
        setupComboBoxDisplay();
        setupTableSelection();

        btnAjouter.setOnAction(e -> ajouterRecrutement());
        btnModifier.setOnAction(e -> modifierRecrutement());
        btnSupprimer.setOnAction(e -> supprimerRecrutement());
    }

    // ============================
    // AFFICHAGE COMBOBOX
    // ============================

    private void setupComboBoxDisplay() {

        comboOffre.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Offre item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomOffre());
            }
        });

        comboOffre.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Offre item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomOffre());
            }
        });

        comboCandidat.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom());
                }
            }
        });

        comboCandidat.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom());
                }
            }
        });
    }

    // ============================
    // CHARGEMENT DONNÉES
    // ============================

    private void loadComboBox() {
        try {
            listCandidat.setAll(candidatService.recuperer());
            listOffre.setAll(offreService.recuperer());

            comboCandidat.setItems(listCandidat);
            comboOffre.setItems(listOffre);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void loadTable() {
        try {
            list.setAll(service.recuperer());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ============================
    // SELECTION TABLE
    // ============================

    private void setupTableSelection() {
        tableRecrutement.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, selected) -> {
                    if (selected != null) {
                        comboOffre.getSelectionModel().select(
                                listOffre.stream()
                                        .filter(o -> o.getIdOffre() == selected.getIdOffre())
                                        .findFirst()
                                        .orElse(null)
                        );

                        comboCandidat.getSelectionModel().select(
                                listCandidat.stream()
                                        .filter(c -> c.getIdCandidat() == selected.getIdCandidat())
                                        .findFirst()
                                        .orElse(null)
                        );
                    }
                });
    }

    // ============================
    // AJOUT
    // ============================

    private void ajouterRecrutement() {

        Offre offre = comboOffre.getValue();
        Candidat candidat = comboCandidat.getValue();

        if (offre == null || candidat == null) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez sélectionner un candidat et une offre");
            return;
        }

        try {
            Recrutement recrutement =
                    new Recrutement(offre.getIdOffre(), candidat.getIdCandidat());

            service.ajouter(recrutement);
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Recrutement ajouté avec succès !");

            loadTable();
            resetFields();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ============================
    // MODIFICATION
    // ============================

    private void modifierRecrutement() {

        Recrutement selected =
                tableRecrutement.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection",
                    "Veuillez sélectionner un recrutement");
            return;
        }

        Offre offre = comboOffre.getValue();
        Candidat candidat = comboCandidat.getValue();

        if (offre == null || candidat == null) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez sélectionner un candidat et une offre");
            return;
        }

        try {
            selected.setIdOffre(offre.getIdOffre());
            selected.setIdCandidat(candidat.getIdCandidat());

            service.modifier(selected);

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Recrutement modifié avec succès !");

            loadTable();
            resetFields();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ============================
    // SUPPRESSION
    // ============================

    private void supprimerRecrutement() {

        Recrutement selected =
                tableRecrutement.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection",
                    "Veuillez sélectionner un recrutement");
            return;
        }

        try {
            service.supprimer(selected.getIdRec());

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Recrutement supprimé avec succès !");

            loadTable();
            resetFields();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ============================
    // RESET
    // ============================

    private void resetFields() {
        comboOffre.getSelectionModel().clearSelection();
        comboCandidat.getSelectionModel().clearSelection();
        tableRecrutement.getSelectionModel().clearSelection();
    }

    // ============================
    // ALERT
    // ============================

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
