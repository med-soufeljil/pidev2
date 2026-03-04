package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.DemandeConges;
import models.StatutDemande;
import models.TypeConge;
import services.DemandeCongeService;
import utils.Navigation;
import utils.Session;

import java.sql.SQLException;

public class DemandeCongesController {

    @FXML
    private TextField tfMotif;
    @FXML
    private ComboBox<TypeConge> cbTypeConge;
    @FXML
    private DatePicker dpDebut;
    @FXML
    private DatePicker dpFin;
    @FXML
    private Label lblMsg;
    @FXML
    private Label lblEmploye;

    private final DemandeCongeService service = new DemandeCongeService();

    @FXML
    public void initialize() {
        cbTypeConge.setItems(FXCollections.observableArrayList(TypeConge.values()));
        lblMsg.setText("");
        lblEmploye.setText("Employé : " + Session.getCurrent().getNomComplet());

        tfMotif.setTextFormatter(new TextFormatter<>(change -> {
            String nt = change.getControlNewText();
            return nt.length() <= 255 ? change : null;
        }));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void setError(String msg) {
        lblMsg.setStyle("-fx-text-fill:#cc0000;");
        lblMsg.setText(msg);
    }

    private void setSuccess(String msg) {
        lblMsg.setStyle("-fx-text-fill:#008000;");
        lblMsg.setText(msg);
    }

    private void validateForm() {
        if (cbTypeConge.getValue() == null)
            throw new IllegalArgumentException("Type congé obligatoire");
        if (dpDebut.getValue() == null)
            throw new IllegalArgumentException("Date début obligatoire");
        if (dpFin.getValue() == null)
            throw new IllegalArgumentException("Date fin obligatoire");
        if (dpFin.getValue().isBefore(dpDebut.getValue()))
            throw new IllegalArgumentException("Date fin doit être >= date début");
        String motif = tfMotif.getText() == null ? "" : tfMotif.getText().trim();
        if (motif.isEmpty())
            throw new IllegalArgumentException("Motif obligatoire");
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    @FXML
    private void onAjouter() {
        try {
            validateForm();

            DemandeConges d = new DemandeConges();
            d.setIdEmploye(Session.getCurrent().getId());
            d.setTypeConge(cbTypeConge.getValue());
            d.setDateDebut(dpDebut.getValue());
            d.setDateFin(dpFin.getValue());
            d.setMotif(tfMotif.getText().trim());
            d.setStatut(StatutDemande.EN_ATTENTE);

            service.ajouter(d);
            setSuccess("Demande congé envoyée ✅ (EN_ATTENTE)");
            onClear();

        } catch (Exception e) {
            setError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onClear() {
        tfMotif.clear();
        cbTypeConge.setValue(null);
        dpDebut.setValue(null);
        dpFin.setValue(null);
        lblMsg.setText("");
    }

    @FXML
    private void goBack(javafx.event.ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Navigation.switchTo(stage, "EmployeDashboard.fxml");
    }
}
