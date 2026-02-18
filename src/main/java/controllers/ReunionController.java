package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Candidat;
import models.Reunion;
import services.CandidatService;
import services.ReunionService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReunionController {

    @FXML private ComboBox<Candidat> comboCandidat;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtLink;
    @FXML private TableView<Reunion> tableReunion;
    @FXML private TableColumn<Reunion, String> colCandidat, colDate, colLink;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnReset;

    private ReunionService service = new ReunionService();
    private CandidatService candidatService = new CandidatService();

    private ObservableList<Reunion> list = FXCollections.observableArrayList();
    private ObservableList<Candidat> listCandidat = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer les colonnes
        colCandidat.setCellValueFactory(new PropertyValueFactory<>("nomCandidat"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLink.setCellValueFactory(new PropertyValueFactory<>("link"));

        // Lier le tableau à l’ObservableList
        tableReunion.setItems(list);

        // Charger ComboBox et TableView
        loadComboBox();
        loadTable();
        setupComboBoxDisplay();

        // Actions
        btnAjouter.setOnAction(e -> ajouterReunion());
        btnModifier.setOnAction(e -> modifierReunion());
        btnSupprimer.setOnAction(e -> supprimerReunion());
        if (btnReset != null) {
            btnReset.setOnAction(e -> clearFields());
        }

        // Sélection dans le tableau → remplir les champs
        tableReunion.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                try {
                    Candidat c = candidatService.getById(newSel.getIdCandidat());
                    comboCandidat.setValue(c);
                    datePicker.setValue(newSel.getDate().toLocalDate());
                    txtLink.setText(newSel.getLink());
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
                }
            }
        });
    }

    private void setupComboBoxDisplay() {
        comboCandidat.setCellFactory(lv -> new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });

        comboCandidat.setButtonCell(new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
    }

    private void loadComboBox() {
        try {
            listCandidat.clear();
            listCandidat.addAll(candidatService.recuperer());
            comboCandidat.setItems(listCandidat);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void loadTable() {
        try {
            list.clear();
            list.addAll(service.recuperer());

            // Ajouter le nom du candidat pour chaque réunion
            List<Candidat> candidats = candidatService.recuperer();
            for (Reunion r : list) {
                for (Candidat c : candidats) {
                    if (c.getIdCandidat() == r.getIdCandidat()) {
                        r.setNomCandidat(c.getNom() + " " + c.getPrenom());
                        break;
                    }
                }
            }

            tableReunion.setItems(list);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void ajouterReunion() {
        Candidat c = comboCandidat.getValue();
        LocalDate date = datePicker.getValue();

        if (c == null || date == null || txtLink.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs");
            return;
        }

        try {
            Reunion r = new Reunion();
            r.setIdCandidat(c.getIdCandidat());
            r.setDate(date.atStartOfDay());
            r.setLink(txtLink.getText());

            service.ajouter(r);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réunion ajoutée !");
            clearFields();
            loadTable();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void modifierReunion() {
        Reunion r = tableReunion.getSelectionModel().getSelectedItem();
        if (r == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une réunion");
            return;
        }

        Candidat c = comboCandidat.getValue();
        LocalDate date = datePicker.getValue();

        if (c == null || date == null || txtLink.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs");
            return;
        }

        try {
            r.setIdCandidat(c.getIdCandidat());
            r.setDate(date.atStartOfDay());
            r.setLink(txtLink.getText());

            service.modifier(r);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réunion modifiée !");
            clearFields();
            loadTable();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void supprimerReunion() {
        Reunion r = tableReunion.getSelectionModel().getSelectedItem();
        if (r == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une réunion");
            return;
        }

        try {
            service.supprimer(r.getIdReunion());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réunion supprimée !");
            clearFields();
            loadTable();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void clearFields() {
        comboCandidat.setValue(null);
        datePicker.setValue(null);
        txtLink.clear();
        tableReunion.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
