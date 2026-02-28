package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.Candidat;
import models.Offre;
import models.Recrutement;
import services.CandidatService;
import services.OffreService;
import services.RecrutementService;
import utils.AuthContext;

import java.sql.SQLException;

public class RecrutementController {

    @FXML private TableView<Recrutement> tableRecrutement;
    @FXML private TableColumn<Recrutement, Integer> colId;
    @FXML private TableColumn<Recrutement, String> colOffre;
    @FXML private TableColumn<Recrutement, String> colCandidat;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private ComboBox<Offre> formOffre;
    @FXML private ComboBox<Candidat> formCandidat;
    @FXML private Button btnSaveForm, btnCancelForm;

    private final RecrutementService service = new RecrutementService();
    private final CandidatService candidatService = new CandidatService();
    private final OffreService offreService = new OffreService();

    private final ObservableList<Recrutement> list = FXCollections.observableArrayList();
    private Recrutement editing;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idRec"));
        colOffre.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
        colCandidat.setCellValueFactory(new PropertyValueFactory<>("nomCandidat"));
        tableRecrutement.setItems(list);

        formOffre.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Offre item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNomOffre()); }
        });
        formOffre.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Offre item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNomOffre()); }
        });
        formCandidat.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Candidat item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom()); }
        });
        formCandidat.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Candidat item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom()); }
        });

        loadTable();

        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> openForm(tableRecrutement.getSelectionModel().getSelectedItem()));
        btnSupprimer.setOnAction(e -> supprimerRecrutement());
        btnSaveForm.setOnAction(e -> saveForm());
        btnCancelForm.setOnAction(e -> hideForm());

        boolean admin = AuthContext.isAdmin();
        btnAjouter.setDisable(!admin);
        btnModifier.setDisable(!admin);
        btnSupprimer.setDisable(!admin);
    }

    private void openForm(Recrutement selected) {
        editing = selected;
        formTitle.setText(selected == null ? "Ajouter recrutement" : "Modifier recrutement");

        try {
            formOffre.setItems(FXCollections.observableArrayList(offreService.recuperer()));
            formCandidat.setItems(FXCollections.observableArrayList(candidatService.recuperer()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }

        if (selected == null) {
            formOffre.getSelectionModel().clearSelection();
            formCandidat.getSelectionModel().clearSelection();
        } else {
            formOffre.getSelectionModel().select(formOffre.getItems().stream().filter(o -> o.getIdOffre() == selected.getIdOffre()).findFirst().orElse(null));
            formCandidat.getSelectionModel().select(formCandidat.getItems().stream().filter(c -> c.getIdCandidat() == selected.getIdCandidat()).findFirst().orElse(null));
        }

        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    private void saveForm() {
        Offre o = formOffre.getValue();
        Candidat c = formCandidat.getValue();
        if (o == null || c == null) return;
        try {
            if (editing == null) {
                service.ajouter(new Recrutement(o.getIdOffre(), c.getIdCandidat()));
            } else {
                editing.setIdOffre(o.getIdOffre());
                editing.setIdCandidat(c.getIdCandidat());
                service.modifier(editing);
            }
            hideForm();
            loadTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void hideForm() {
        formPane.setVisible(false);
        formPane.setManaged(false);
        editing = null;
    }

    private void loadTable() {
        try { list.setAll(service.recuperer()); }
        catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
    }

    private void supprimerRecrutement() {
        Recrutement selected = tableRecrutement.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try { service.supprimer(selected.getIdRec()); loadTable(); }
        catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
