package org.soa.tp1.pi_dev_s2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Categorie;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Formation;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Niveau;
import org.soa.tp1.pi_dev_s2.mouhamd.services.FormationService;

import java.util.List;

public class FormationController {

    @FXML private TableView<Formation>                      tableFormation;
    @FXML private TableColumn<Formation, String>            colTitre;
    @FXML private TableColumn<Formation, String>            colDuree;
    @FXML private TableColumn<Formation, String>            colNiveau;
    @FXML private TableColumn<Formation, String>            colCategorie;
    @FXML private TableColumn<Formation, String>            colCertif;

    @FXML private TextField   tfTitre;
    @FXML private TextArea    tfDescription;
    @FXML private TextField   tfDuree;
    @FXML private ComboBox<Niveau>    cbNiveau;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private CheckBox    cbCertification;
    @FXML private TextField   tfRecherche;
    @FXML private Label       lblCount;
    @FXML private Label       message;

    private final FormationService service = new FormationService();
    private ObservableList<Formation> allFormations = FXCollections.observableArrayList();
    private Formation selectedFormation;

    @FXML
    public void initialize() {
        setupColumns();
        cbNiveau.getItems().setAll(Niveau.values());
        cbCategorie.getItems().setAll(Categorie.values());
        loadFormations();

        tableFormation.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) fillForm(sel);
        });

        tfRecherche.textProperty().addListener((obs, old, val) -> filtrer(val));
    }

    private void setupColumns() {
        colTitre.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTitre()));
        colDuree.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDuree() + " h"));
        colNiveau.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNiveau() != null ? d.getValue().getNiveau().name() : ""));
        colCategorie.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCategorie() != null ? d.getValue().getCategorie().name() : ""));
        colCertif.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isCertification() ? "✅" : "❌"));
    }

    private void loadFormations() {
        try {
            List<Formation> list = service.recuperer();
            allFormations = FXCollections.observableArrayList(list);
            tableFormation.setItems(allFormations);
            updateCount();
        } catch (Exception e) { showMsg("Erreur chargement : " + e.getMessage(), "error"); }
    }

    private void filtrer(String kw) {
        if (kw == null || kw.isBlank()) {
            tableFormation.setItems(allFormations);
        } else {
            String k = kw.toLowerCase();
            tableFormation.setItems(allFormations.filtered(f ->
                    f.getTitre().toLowerCase().contains(k) ||
                            (f.getNiveau() != null && f.getNiveau().name().toLowerCase().contains(k)) ||
                            (f.getCategorie() != null && f.getCategorie().name().toLowerCase().contains(k))));
        }
        updateCount();
    }

    private void fillForm(Formation f) {
        selectedFormation = f;
        tfTitre.setText(f.getTitre());
        tfDescription.setText(f.getDescription());
        tfDuree.setText(String.valueOf(f.getDuree()));
        cbNiveau.setValue(f.getNiveau());
        cbCategorie.setValue(f.getCategorie());
        cbCertification.setSelected(f.isCertification());
    }

    @FXML private void handleAjouter() {
        try {
            Formation f = buildFromForm();
            service.ajouter(f);
            loadFormations();
            clearForm();
            showMsg("✅ Formation ajoutée !", "success");
        } catch (Exception e) { showMsg("❌ " + e.getMessage(), "error"); }
    }

    @FXML private void handleModifier() {
        if (selectedFormation == null) { showMsg("Sélectionnez une formation !", "error"); return; }
        try {
            Formation f = buildFromForm();
            f.setId_formation(selectedFormation.getId_formation());
            service.modifier(f);
            loadFormations();
            clearForm();
            showMsg("✅ Formation modifiée !", "success");
        } catch (Exception e) { showMsg("❌ " + e.getMessage(), "error"); }
    }

    @FXML private void handleSupprimer() {
        if (selectedFormation == null) { showMsg("Sélectionnez une formation !", "error"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + selectedFormation.getTitre() + "\" ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    service.supprimer(selectedFormation.getId_formation());
                    loadFormations();
                    clearForm();
                    showMsg("✅ Formation supprimée !", "success");
                } catch (Exception e) { showMsg("❌ " + e.getMessage(), "error"); }
            }
        });
    }

    @FXML private void handleActualiser() {
        tfRecherche.clear();
        loadFormations();
        clearForm();
    }

    @FXML private void handleApprenants() {
        if (selectedFormation == null) { showMsg("Sélectionnez une formation !", "error"); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ApprenantView.fxml"));
            Parent root = loader.load();
            ApprenantController ctrl = loader.getController();
            ctrl.setFormation(selectedFormation);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Apprenants — " + selectedFormation.getTitre());
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Formation buildFromForm() {
        if (tfTitre.getText().isBlank()) throw new IllegalArgumentException("Titre obligatoire !");
        int duree;
        try { duree = Integer.parseInt(tfDuree.getText().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Durée invalide !"); }
        Formation f = new Formation();
        f.setTitre(tfTitre.getText().trim());
        f.setDescription(tfDescription.getText().trim());
        f.setDuree(duree);
        f.setNiveau(cbNiveau.getValue());
        f.setCategorie(cbCategorie.getValue());
        f.setCertification(cbCertification.isSelected());
        return f;
    }

    private void clearForm() {
        selectedFormation = null;
        tfTitre.clear(); tfDescription.clear(); tfDuree.clear();
        cbNiveau.setValue(null); cbCategorie.setValue(null);
        cbCertification.setSelected(false);
        tableFormation.getSelectionModel().clearSelection();
    }

    private void updateCount() {
        if (lblCount != null)
            lblCount.setText(tableFormation.getItems().size() + " formation(s)");
    }

    private void showMsg(String msg, String type) {
        if (message != null) {
            message.setText(msg);
            message.setStyle(type.equals("error")
                    ? "-fx-text-fill:#C62828;" : "-fx-text-fill:#2E7D32;");
        }
    }
}