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
import utils.AuthContext;
import utils.NavigationState;

import java.sql.SQLException;
import java.util.List;

public class ReunionController {

    @FXML private TableView<Reunion> tableReunion;
    @FXML private TableColumn<Reunion, String> colCandidat, colDate, colLink;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnReset;

    private final ReunionService service = new ReunionService();
    private final CandidatService candidatService = new CandidatService();
    private final ObservableList<Reunion> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colCandidat.setCellValueFactory(new PropertyValueFactory<>("nomCandidat"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLink.setCellValueFactory(new PropertyValueFactory<>("link"));
        tableReunion.setItems(list);
        loadTable();

        btnAjouter.setOnAction(e -> { NavigationState.clearAll(); MainController.navigate("ReunionForm.fxml"); });
        btnModifier.setOnAction(e -> {
            Reunion r = tableReunion.getSelectionModel().getSelectedItem();
            if (r == null || !AuthContext.isAdmin()) return;
            NavigationState.clearAll(); NavigationState.selectedReunion = r; MainController.navigate("ReunionForm.fxml");
        });
        btnSupprimer.setOnAction(e -> supprimerReunion());
        btnReset.setOnAction(e -> tableReunion.getSelectionModel().clearSelection());

        applyPermissions();
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
