package org.soa.tp1.pi_dev_s2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.soa.tp1.pi_dev_s2.com.esprit.models.Evenement;
import org.soa.tp1.pi_dev_s2.com.esprit.models.Participation;
import org.soa.tp1.pi_dev_s2.service.EvenementService;
import org.soa.tp1.pi_dev_s2.service.ParticipationService;

import java.time.LocalDate;
import java.util.List;

public class EvenementController {

    @FXML private TableView<Evenement>               tableEvenements;
    @FXML private TableColumn<Evenement, String>     colTitre;
    @FXML private TableColumn<Evenement, String>     colDate;
    @FXML private TableColumn<Evenement, String>     colLieu;
    @FXML private TableColumn<Evenement, String>     colPlaces;
    @FXML private TableColumn<Evenement, String>     colInscrits;
    @FXML private TableColumn<Evenement, String>     colStatut;

    @FXML private TextField   tfTitre;
    @FXML private TextField   tfLieu;
    @FXML private TextField   tfPlaces;
    @FXML private TextArea    tfDescription;
    @FXML private DatePicker  dpDate;
    @FXML private TextField   tfHeureDebut;
    @FXML private TextField   tfHeureFin;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<Integer> cbCategorie;
    @FXML private TextField   tfRecherche;
    @FXML private Label       lblCount;
    @FXML private Label       message;

    private final EvenementService     evenementService     = new EvenementService();
    private final ParticipationService participationService = new ParticipationService();
    private ObservableList<Evenement>  allEvenements        = FXCollections.observableArrayList();
    private Evenement                  selectedEvenement;

    @FXML
    public void initialize() {
        setupColumns();
        cbStatut.getItems().setAll("planifie", "en cours", "termine");
        cbStatut.setValue("planifie");
        cbCategorie.getItems().setAll(1, 2, 4); // IDs catégories de la BDD
        cbCategorie.setValue(1);
        loadEvenements();

        tableEvenements.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) fillForm(sel);
        });

        if (tfRecherche != null)
            tfRecherche.textProperty().addListener((obs, old, val) -> filtrer(val));
    }

    private void setupColumns() {
        colTitre.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTitre()));
        colDate.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDateEvenement() != null
                        ? d.getValue().getDateEvenement().toString() : ""));
        colLieu.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getLieu()));
        colPlaces.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getNombrePlacesMax())));
        colInscrits.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getNombreInscrits())));
        colStatut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatut()));

        // Badge couleur statut
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (v == null || empty) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle(switch (v.toLowerCase()) {
                    case "planifie"  -> "-fx-text-fill: #1565C0; -fx-font-weight: bold;";
                    case "en cours"  -> "-fx-text-fill: #E65100; -fx-font-weight: bold;";
                    case "termine"   -> "-fx-text-fill: #2E7D32; -fx-font-weight: bold;";
                    default          -> "";
                });
            }
        });
    }

    private void loadEvenements() {
        try {
            List<Evenement> list = evenementService.recuperer();
            allEvenements = FXCollections.observableArrayList(list);
            tableEvenements.setItems(allEvenements);
            updateCount();
        } catch (Exception e) { showMsg("Erreur : " + e.getMessage(), "error"); }
    }

    private void filtrer(String kw) {
        if (kw == null || kw.isBlank()) {
            tableEvenements.setItems(allEvenements);
        } else {
            String k = kw.toLowerCase();
            tableEvenements.setItems(allEvenements.filtered(e ->
                    e.getTitre().toLowerCase().contains(k) ||
                            e.getLieu().toLowerCase().contains(k) ||
                            e.getStatut().toLowerCase().contains(k)));
        }
        updateCount();
    }

    private void fillForm(Evenement e) {
        selectedEvenement = e;
        tfTitre.setText(e.getTitre());
        tfLieu.setText(e.getLieu());
        tfPlaces.setText(String.valueOf(e.getNombrePlacesMax()));
        tfDescription.setText(e.getDescription());
        dpDate.setValue(e.getDateEvenement());
        tfHeureDebut.setText(e.getHeureDebut() != null ? e.getHeureDebut().toString() : "");
        tfHeureFin.setText(e.getHeureFin()     != null ? e.getHeureFin().toString()   : "");
        cbStatut.setValue(e.getStatut());
        cbCategorie.setValue(e.getIdCategorie());
    }

    @FXML private void handleAjouter() {
        try {
            Evenement e = buildFromForm();
            evenementService.ajouter(e);
            loadEvenements();
            clearForm();
            showMsg("✅ Événement ajouté !", "success");
        } catch (Exception e) { showMsg("❌ " + e.getMessage(), "error"); }
    }

    @FXML private void handleModifier() {
        if (selectedEvenement == null) { showMsg("Sélectionnez un événement !", "error"); return; }
        try {
            Evenement e = buildFromForm();
            e.setIdEvenement(selectedEvenement.getIdEvenement());
            evenementService.modifier(e);
            loadEvenements();
            clearForm();
            showMsg("✅ Événement modifié !", "success");
        } catch (Exception e) { showMsg("❌ " + e.getMessage(), "error"); }
    }

    @FXML private void handleSupprimer() {
        if (selectedEvenement == null) { showMsg("Sélectionnez un événement !", "error"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + selectedEvenement.getTitre() + "\" ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    evenementService.supprimer(selectedEvenement.getIdEvenement());
                    loadEvenements();
                    clearForm();
                    showMsg("✅ Événement supprimé !", "success");
                } catch (Exception ex) { showMsg("❌ " + ex.getMessage(), "error"); }
            }
        });
    }

    @FXML private void handleActualiser() {
        if (tfRecherche != null) tfRecherche.clear();
        loadEvenements();
        clearForm();
    }

    @FXML private void handleParticiper() {
        if (selectedEvenement == null) { showMsg("Sélectionnez un événement !", "error"); return; }
        if (selectedEvenement.getNombreInscrits() >= selectedEvenement.getNombrePlacesMax()) {
            showMsg("❌ Plus de places disponibles !", "error"); return;
        }
        try {
            Participation p = new Participation(
                    selectedEvenement.getIdEvenement(),
                    LocalDate.now(), "confirme", false);
            participationService.ajouter(p);
            loadEvenements();
            showMsg("✅ Participation enregistrée !", "success");
        } catch (Exception e) { showMsg("❌ " + e.getMessage(), "error"); }
    }

    private Evenement buildFromForm() throws Exception {
        if (tfTitre.getText().isBlank()) throw new Exception("Titre obligatoire !");
        if (dpDate.getValue() == null)  throw new Exception("Date obligatoire !");
        int places;
        try { places = Integer.parseInt(tfPlaces.getText().trim()); }
        catch (NumberFormatException e) { throw new Exception("Nombre de places invalide !"); }

        Evenement e = new Evenement();
        e.setTitre(tfTitre.getText().trim());
        e.setLieu(tfLieu.getText().trim());
        e.setNombrePlacesMax(places);
        e.setNombreInscrits(0);
        e.setDescription(tfDescription.getText().trim());
        e.setDateEvenement(dpDate.getValue());
        e.setIdCategorie(cbCategorie.getValue() != null ? cbCategorie.getValue() : 1);
        e.setStatut(cbStatut.getValue() != null ? cbStatut.getValue() : "planifie");
        try {
            if (!tfHeureDebut.getText().isBlank())
                e.setHeureDebut(java.time.LocalTime.parse(tfHeureDebut.getText().trim()));
            if (!tfHeureFin.getText().isBlank())
                e.setHeureFin(java.time.LocalTime.parse(tfHeureFin.getText().trim()));
        } catch (Exception ex) { throw new Exception("Format heure invalide ! (ex: 09:00)"); }
        return e;
    }

    private void clearForm() {
        selectedEvenement = null;
        tfTitre.clear(); tfLieu.clear(); tfPlaces.clear(); tfDescription.clear();
        dpDate.setValue(null); tfHeureDebut.clear(); tfHeureFin.clear();
        cbStatut.setValue("planifie");
        tableEvenements.getSelectionModel().clearSelection();
    }

    private void updateCount() {
        if (lblCount != null)
            lblCount.setText(tableEvenements.getItems().size() + " événement(s)");
    }

    private void showMsg(String msg, String type) {
        if (message != null) {
            message.setText(msg);
            message.setStyle(type.equals("error")
                    ? "-fx-text-fill:#C62828;" : "-fx-text-fill:#2E7D32;");
        }
    }
}