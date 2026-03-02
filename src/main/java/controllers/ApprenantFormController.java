package controllers;

import entities.Apprenant;
import entities.Formation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class ApprenantFormController {
    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;
    @FXML private ComboBox<Formation> cbFormation;

    private Apprenant result;

    @FXML
    public void initialize() {
        cbStatut.setItems(FXCollections.observableArrayList("ACTIF", "EN_PAUSE", "TERMINE"));
        cbStatut.setValue("ACTIF");
        cbFormation.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Formation f) { return f == null ? "" : f.getTitre(); }
            @Override public Formation fromString(String s) { return null; }
        });
    }

    public void setFormations(List<Formation> formations) {
        cbFormation.setItems(FXCollections.observableArrayList(formations));
    }

    public void setPendingFormationId(Integer idFormation) {
        if (idFormation == null || cbFormation.getItems() == null) return;
        cbFormation.getItems().stream().filter(f -> f.getId_formation() == idFormation).findFirst().ifPresent(cbFormation::setValue);
    }

    public void setInitial(Apprenant a) {
        if (a == null) return;
        tfNom.setText(a.getNom());
        tfPrenom.setText(a.getPrenom());
        tfEmail.setText(a.getEmail());
        cbStatut.setValue(a.getStatut());
        dpDebut.setValue(a.getDateDebut());
        dpFin.setValue(a.getDateFin());
        cbFormation.getItems().stream().filter(f -> f.getId_formation() == a.getId_formation()).findFirst().ifPresent(cbFormation::setValue);
    }

    @FXML
    public void save() {
        if (tfNom.getText().isBlank() || tfPrenom.getText().isBlank() || tfEmail.getText().isBlank() || cbFormation.getValue() == null || dpDebut.getValue() == null) return;
        if (!tfEmail.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) return;
        if (dpFin.getValue() != null && dpFin.getValue().isBefore(dpDebut.getValue())) return;
        Apprenant a = new Apprenant();
        a.setNom(tfNom.getText().trim());
        a.setPrenom(tfPrenom.getText().trim());
        a.setEmail(tfEmail.getText().trim());
        a.setStatut(cbStatut.getValue());
        a.setDateDebut(dpDebut.getValue());
        a.setDateFin(dpFin.getValue());
        a.setId_formation(cbFormation.getValue().getId_formation());
        result = a;
        close();
    }

    @FXML
    public void cancel() { close(); }
    private void close() { ((Stage) tfNom.getScene().getWindow()).close(); }
    public Apprenant getResult() { return result; }
}
