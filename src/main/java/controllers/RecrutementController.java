package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Recrutement;
import services.RecrutementService;
import utils.AuthContext;
import utils.NavigationState;

import java.sql.SQLException;

public class RecrutementController {

    @FXML private TableView<Recrutement> tableRecrutement;
    @FXML private TableColumn<Recrutement, Integer> colId;
    @FXML private TableColumn<Recrutement, String> colOffre;
    @FXML private TableColumn<Recrutement, String> colCandidat;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private final RecrutementService service = new RecrutementService();
    private final ObservableList<Recrutement> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idRec"));
        colOffre.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
        colCandidat.setCellValueFactory(new PropertyValueFactory<>("nomCandidat"));
        tableRecrutement.setItems(list);
        loadTable();

        btnAjouter.setOnAction(e -> { NavigationState.clearAll(); MainController.navigate("RecrutementForm.fxml"); });
        btnModifier.setOnAction(e -> {
            Recrutement selected = tableRecrutement.getSelectionModel().getSelectedItem();
            if (selected == null || !AuthContext.isAdmin()) return;
            NavigationState.clearAll(); NavigationState.selectedRecrutement = selected; MainController.navigate("RecrutementForm.fxml");
        });
        btnSupprimer.setOnAction(e -> supprimerRecrutement());

        boolean admin = AuthContext.isAdmin();
        btnAjouter.setDisable(!admin);
        btnModifier.setDisable(!admin);
        btnSupprimer.setDisable(!admin);
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
