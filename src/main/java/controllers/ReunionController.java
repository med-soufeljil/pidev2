package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.Candidat;
import models.Reunion;
import services.CandidatService;
import services.ExternalApiService;
import services.ReunionService;
import utils.AuthContext;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReunionController {

    @FXML private TableView<Reunion> tableReunion;
    @FXML private TableColumn<Reunion, String> colCandidat, colDate, colLink;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnReset;

    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField formIdRh, formLink;
    @FXML private ComboBox<Candidat> formCandidat;
    @FXML private DatePicker formDate;
    @FXML private Button btnGenerateLink, btnSaveForm, btnCancelForm;

    private final ReunionService service = new ReunionService();
    private final CandidatService candidatService = new CandidatService();
    private final ExternalApiService externalApiService = new ExternalApiService();

    private final ObservableList<Reunion> list = FXCollections.observableArrayList();
    private Reunion editing;

    @FXML
    public void initialize() {
        colCandidat.setCellValueFactory(new PropertyValueFactory<>("nomCandidat"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLink.setCellValueFactory(new PropertyValueFactory<>("link"));

        tableReunion.setItems(list);
        loadTable();

        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> openForm(tableReunion.getSelectionModel().getSelectedItem()));
        btnSupprimer.setOnAction(e -> supprimerReunion());
        btnReset.setOnAction(e -> tableReunion.getSelectionModel().clearSelection());

        btnGenerateLink.setOnAction(e -> formLink.setText(externalApiService.generateMeetingLink()));
        btnSaveForm.setOnAction(e -> saveForm());
        btnCancelForm.setOnAction(e -> hideForm());

        formCandidat.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Candidat item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom()); }
        });
        formCandidat.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Candidat item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom()); }
        });

        applyPermissions();
    }

    private void openForm(Reunion selected) {
        editing = selected;
        formTitle.setText(selected == null ? "Ajouter réunion" : "Modifier réunion");

        try {
            formCandidat.setItems(FXCollections.observableArrayList(candidatService.recuperer()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }

        if (selected == null) {
            formIdRh.setText("1");
            formDate.setValue(LocalDate.now().plusDays(1));
            formLink.setText(externalApiService.generateMeetingLink());
            formCandidat.getSelectionModel().clearSelection();
        } else {
            formIdRh.setText(String.valueOf(selected.getIdRH()));
            formDate.setValue(selected.getDate().toLocalDate());
            formLink.setText(selected.getLink());
            formCandidat.getSelectionModel().select(formCandidat.getItems().stream().filter(c -> c.getIdCandidat() == selected.getIdCandidat()).findFirst().orElse(null));
        }

        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    private void saveForm() {
        Candidat c = formCandidat.getValue();
        LocalDate date = formDate.getValue();
        if (c == null || date == null || formIdRh.getText().isBlank() || formLink.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs.");
            return;
        }
        try {
            Reunion target = editing == null ? new Reunion() : editing;
            target.setIdRH(Integer.parseInt(formIdRh.getText()));
            target.setIdCandidat(c.getIdCandidat());
            target.setDate(date.atStartOfDay());
            target.setLink(formLink.getText());
            if (target.getIdReunion() == 0) service.ajouter(target); else service.modifier(target);
            hideForm();
            loadTable();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void hideForm() {
        formPane.setVisible(false);
        formPane.setManaged(false);
        editing = null;
    }

    private void applyPermissions() {
        boolean admin = AuthContext.isAdmin();
        btnAjouter.setDisable(!admin);
        btnModifier.setDisable(!admin);
        btnSupprimer.setDisable(!admin);
    }

    private void loadTable() {
        try {
            list.clear();
            list.addAll(service.recuperer());
            List<Candidat> candidats = candidatService.recuperer();
            for (Reunion r : list) {
                for (Candidat c : candidats) {
                    if (c.getIdCandidat() == r.getIdCandidat()) {
                        r.setNomCandidat(c.getNom() + " " + c.getPrenom());
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void supprimerReunion() {
        Reunion r = tableReunion.getSelectionModel().getSelectedItem();
        if (r == null) return;
        try {
            service.supprimer(r.getIdReunion());
            loadTable();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
