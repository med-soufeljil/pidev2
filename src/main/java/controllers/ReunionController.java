package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import models.Candidat;
import models.Reunion;
import services.CandidatService;
import services.ExternalApiService;
import services.ReunionService;
import utils.AuthContext;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ReunionController {

    @FXML private TableView<Reunion> tableReunion;
    @FXML private TableColumn<Reunion, String> colCandidat, colDate, colLink;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnReset;

    private final ReunionService service = new ReunionService();
    private final CandidatService candidatService = new CandidatService();
    private final ExternalApiService externalApiService = new ExternalApiService();

    private final ObservableList<Reunion> list = FXCollections.observableArrayList();

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

        applyPermissions();
    }

    private void openForm(Reunion selected) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(selected == null ? "Ajouter réunion" : "Modifier réunion");
        ButtonType save = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        ComboBox<Candidat> comboCandidat = new ComboBox<>();
        DatePicker datePicker = new DatePicker();
        TextField txtIdRH = new TextField();
        TextField txtLink = new TextField();
        Button btnFillMeetLink = new Button("Générer lien Meet");

        try {
            comboCandidat.setItems(FXCollections.observableArrayList(candidatService.recuperer()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }

        comboCandidat.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Candidat item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom()); }
        });
        comboCandidat.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Candidat item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom()); }
        });

        btnFillMeetLink.setOnAction(e -> {
            try {
                txtLink.setText(externalApiService.generateMeetingLink());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        if (selected != null) {
            txtIdRH.setText(String.valueOf(selected.getIdRH()));
            datePicker.setValue(selected.getDate().toLocalDate());
            txtLink.setText(selected.getLink());
            Reunion finalSelected = selected;
            comboCandidat.getSelectionModel().select(comboCandidat.getItems().stream().filter(c->c.getIdCandidat()== finalSelected.getIdCandidat()).findFirst().orElse(null));
        }

        GridPane g = new GridPane();
        g.getStyleClass().add("form-grid");
        g.setHgap(8); g.setVgap(8);
        g.addRow(0, new Label("ID RH"), txtIdRH);
        g.addRow(1, new Label("Candidat"), comboCandidat);
        g.addRow(2, new Label("Date"), datePicker);
        g.addRow(3, new Label("Lien"), txtLink);
        g.add(new HBox(10, btnFillMeetLink), 1, 4);
        d.getDialogPane().setContent(g);

        Optional<ButtonType> r = d.showAndWait();
        if (r.isPresent() && r.get() == save) {
            Candidat c = comboCandidat.getValue();
            LocalDate date = datePicker.getValue();
            if (c == null || date == null || txtIdRH.getText().isBlank() || txtLink.getText().isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs.");
                return;
            }
            try {
                if (selected == null) selected = new Reunion();
                selected.setIdRH(Integer.parseInt(txtIdRH.getText()));
                selected.setIdCandidat(c.getIdCandidat());
                selected.setDate(date.atStartOfDay());
                selected.setLink(txtLink.getText());
                if (selected.getIdReunion() == 0) service.ajouter(selected); else service.modifier(selected);
                loadTable();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
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
        try { service.supprimer(r.getIdReunion()); loadTable(); }
        catch (SQLException ex) { showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage()); }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}