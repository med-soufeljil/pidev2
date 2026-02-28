package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import models.Candidat;
import models.Offre;
import models.Recrutement;
import services.CandidatService;
import services.OffreService;
import services.RecrutementService;
import utils.AuthContext;

import java.sql.SQLException;
import java.util.Optional;

public class RecrutementController {

    @FXML private TableView<Recrutement> tableRecrutement;
    @FXML private TableColumn<Recrutement, Integer> colId;
    @FXML private TableColumn<Recrutement, String> colOffre;
    @FXML private TableColumn<Recrutement, String> colCandidat;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private final RecrutementService service = new RecrutementService();
    private final CandidatService candidatService = new CandidatService();
    private final OffreService offreService = new OffreService();

    private final ObservableList<Recrutement> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idRec"));
        colOffre.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
        colCandidat.setCellValueFactory(new PropertyValueFactory<>("nomCandidat"));
        tableRecrutement.setItems(list);

        loadTable();

        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> openForm(tableRecrutement.getSelectionModel().getSelectedItem()));
        btnSupprimer.setOnAction(e -> supprimerRecrutement());

        boolean admin = AuthContext.isAdmin();
        btnAjouter.setDisable(!admin);
        btnModifier.setDisable(!admin);
        btnSupprimer.setDisable(!admin);
    }

    private void openForm(Recrutement selected) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(selected == null ? "Ajouter recrutement" : "Modifier recrutement");
        ButtonType save = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        ComboBox<Offre> comboOffre = new ComboBox<>();
        ComboBox<Candidat> comboCandidat = new ComboBox<>();
        try {
            comboOffre.setItems(FXCollections.observableArrayList(offreService.recuperer()));
            comboCandidat.setItems(FXCollections.observableArrayList(candidatService.recuperer()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }

        comboOffre.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Offre item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNomOffre()); }
        });
        comboOffre.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Offre item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNomOffre()); }
        });
        comboCandidat.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Candidat item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom()); }
        });
        comboCandidat.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Candidat item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom()); }
        });

        if (selected != null) {
            comboOffre.getSelectionModel().select(comboOffre.getItems().stream().filter(o -> o.getIdOffre() == selected.getIdOffre()).findFirst().orElse(null));
            comboCandidat.getSelectionModel().select(comboCandidat.getItems().stream().filter(c -> c.getIdCandidat() == selected.getIdCandidat()).findFirst().orElse(null));
        }

        GridPane g = new GridPane(); g.setHgap(8); g.setVgap(8);
        g.addRow(0, new Label("Offre"), comboOffre);
        g.addRow(1, new Label("Candidat"), comboCandidat);
        d.getDialogPane().setContent(g);

        Optional<ButtonType> r = d.showAndWait();
        if (r.isPresent() && r.get() == save) {
            Offre o = comboOffre.getValue();
            Candidat c = comboCandidat.getValue();
            if (o == null || c == null) return;
            try {
                if (selected == null) {
                    service.ajouter(new Recrutement(o.getIdOffre(), c.getIdCandidat()));
                } else {
                    selected.setIdOffre(o.getIdOffre());
                    selected.setIdCandidat(c.getIdCandidat());
                    service.modifier(selected);
                }
                loadTable();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
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