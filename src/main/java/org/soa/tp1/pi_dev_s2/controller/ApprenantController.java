package org.soa.tp1.pi_dev_s2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Apprenant;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Formation;
import org.soa.tp1.pi_dev_s2.service.ApprenantService;

import java.time.LocalDate;
import java.util.List;

public class ApprenantController {

    @FXML private TableView<Apprenant>               tableApprenant;
    @FXML private TableColumn<Apprenant, String>     colNom;
    @FXML private TableColumn<Apprenant, String>     colPrenom;
    @FXML private TableColumn<Apprenant, String>     colEmail;
    @FXML private TableColumn<Apprenant, String>     colStatut;
    @FXML private TableColumn<Apprenant, String>     colDateDebut;
    @FXML private TableColumn<Apprenant, String>     colDateFin;

    @FXML private TextField   tfNom;
    @FXML private TextField   tfPrenom;
    @FXML private TextField   tfEmail;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker  dpDateDebut;
    @FXML private DatePicker  dpDateFin;
    @FXML private TextField   tfRecherche;
    @FXML private Label       lblCount;
    @FXML private Label       lblFormation;
    @FXML private Label       message;

    private final ApprenantService service = new ApprenantService();
    private ObservableList<Apprenant> allApprenants = FXCollections.observableArrayList();
    private Apprenant  selectedApprenant;
    private Formation currentFormation;

    // ── Appelé depuis FormationController ────────
    public void setFormation(Formation f) {
        this.currentFormation = f;
        if (lblFormation != null)
            lblFormation.setText("Formation : " + f.getTitre());
        loadApprenants();
    }

    @FXML
    public void initialize() {
        setupColumns();
        cbStatut.getItems().setAll("ACTIF", "En cours", "Terminé", "Abandonné");
        cbStatut.setValue("ACTIF");

        tableApprenant.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) fillForm(sel); });

        if (tfRecherche != null)
            tfRecherche.textProperty().addListener((obs, old, val) -> filtrer(val));

        // Si pas de formation définie → charger tous les apprenants
        if (currentFormation == null) loadApprenants();
    }

    private void setupColumns() {
        colNom.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNom()));
        colPrenom.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPrenom()));
        colEmail.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEmail()));
        colStatut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatut()));
        colDateDebut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDateDebut() != null
                        ? d.getValue().getDateDebut().toString() : ""));
        colDateFin.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDateFin() != null
                        ? d.getValue().getDateFin().toString() : ""));

        // Couleur statut
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (v == null || empty) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle(switch (v.toUpperCase()) {
                    case "ACTIF", "EN COURS" -> "-fx-text-fill:#2E7D32;-fx-font-weight:bold;";
                    case "TERMINÉ"           -> "-fx-text-fill:#1565C0;-fx-font-weight:bold;";
                    case "ABANDONNÉ"         -> "-fx-text-fill:#C62828;-fx-font-weight:bold;";
                    default                  -> "";
                });
            }
        });
    }

    private void loadApprenants() {
        try {
            List<Apprenant> list = currentFormation != null? service.recupererParFormation(currentFormation.getId_formation()): service.recuperer();
            allApprenants = FXCollections.observableArrayList(list);
            tableApprenant.setItems(allApprenants);
            updateCount();
        } catch (Exception e) { showMsg("Erreur : " + e.getMessage(), "error"); }
    }

    private void filtrer(String kw) {
        if (kw == null || kw.isBlank()) {
            tableApprenant.setItems(allApprenants);
        } else {
            String k = kw.toLowerCase();
            tableApprenant.setItems(allApprenants.filtered(a ->
                    a.getNom().toLowerCase().contains(k)    ||
                            a.getPrenom().toLowerCase().contains(k) ||
                            a.getEmail().toLowerCase().contains(k)));
        }
        updateCount();
    }

    private void fillForm(Apprenant a) {
        selectedApprenant = a;
        tfNom.setText(a.getNom());
        tfPrenom.setText(a.getPrenom());
        tfEmail.setText(a.getEmail());
        cbStatut.setValue(a.getStatut());
        dpDateDebut.setValue(a.getDateDebut());
        dpDateFin.setValue(a.getDateFin());
    }

    @FXML private void handleAjouter() {
        try {
            Apprenant a = buildFromForm();
            service.ajouter(a);
            loadApprenants();
            clearForm();
            showMsg("✅ Apprenant ajouté !", "success");
        } catch (Exception e) { showMsg("❌ " + e.getMessage(), "error"); }
    }

    @FXML private void handleModifier() {
        if (selectedApprenant == null) { showMsg("Sélectionnez un apprenant !", "error"); return; }
        try {
            Apprenant a = buildFromForm();
            a.setIdApprenant(selectedApprenant.getIdApprenant());
            service.modifier(a);
            loadApprenants();
            clearForm();
            showMsg("✅ Apprenant modifié !", "success");
        } catch (Exception e) { showMsg("❌ " + e.getMessage(), "error"); }
    }

    @FXML private void handleSupprimer() {
        if (selectedApprenant == null) { showMsg("Sélectionnez un apprenant !", "error"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + selectedApprenant.getNom() + " " + selectedApprenant.getPrenom() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    service.supprimer(selectedApprenant.getIdApprenant());
                    loadApprenants();
                    clearForm();
                    showMsg("✅ Apprenant supprimé !", "success");
                } catch (Exception ex) { showMsg("❌ " + ex.getMessage(), "error"); }
            }
        });
    }

    @FXML private void handleActualiser() {
        if (tfRecherche != null) tfRecherche.clear();
        loadApprenants();
        clearForm();
    }

    private Apprenant buildFromForm() {
        if (tfNom.getText().isBlank())    throw new IllegalArgumentException("Nom obligatoire !");
        if (tfPrenom.getText().isBlank()) throw new IllegalArgumentException("Prénom obligatoire !");
        if (tfEmail.getText().isBlank())  throw new IllegalArgumentException("Email obligatoire !");

        int idFormation = currentFormation != null ? currentFormation.getId_formation() : 1;

        return new Apprenant(
                tfNom.getText().trim(),
                tfPrenom.getText().trim(),
                tfEmail.getText().trim(),
                cbStatut.getValue(),
                dpDateDebut.getValue() != null ? dpDateDebut.getValue() : LocalDate.now(),
                dpDateFin.getValue()   != null ? dpDateFin.getValue()   : LocalDate.now().plusMonths(1),
                idFormation
        );
    }

    private void clearForm() {
        selectedApprenant = null;
        tfNom.clear(); tfPrenom.clear(); tfEmail.clear();
        cbStatut.setValue("ACTIF");
        dpDateDebut.setValue(null); dpDateFin.setValue(null);
        tableApprenant.getSelectionModel().clearSelection();
    }

    private void updateCount() {
        if (lblCount != null)
            lblCount.setText(tableApprenant.getItems().size() + " apprenant(s)");
    }

    private void showMsg(String msg, String type) {
        if (message != null) {
            message.setText(msg);
            message.setStyle(type.equals("error")
                    ? "-fx-text-fill:#C62828;" : "-fx-text-fill:#2E7D32;");
        }
    }
}