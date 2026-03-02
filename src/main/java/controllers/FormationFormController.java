package controllers;

import entities.Categorie;
import entities.Formation;
import entities.Niveau;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FormationFormController {
    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private TextField tfDuree;
    @FXML private ComboBox<Niveau> cbNiveau;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private CheckBox cbCertification;

    private Formation result;

    @FXML
    public void initialize() {
        cbNiveau.setItems(FXCollections.observableArrayList(Niveau.values()));
        cbCategorie.setItems(FXCollections.observableArrayList(Categorie.values()));
    }

    public void setInitial(Formation formation) {
        if (formation == null) return;
        tfTitre.setText(formation.getTitre());
        taDescription.setText(formation.getDescription());
        tfDuree.setText(String.valueOf(formation.getDuree()));
        cbNiveau.setValue(formation.getNiveau());
        cbCategorie.setValue(formation.getCategorie());
        cbCertification.setSelected(formation.isCertification());
    }

    @FXML
    public void save() {
        if (tfTitre.getText().isBlank() || taDescription.getText().isBlank() || tfDuree.getText().isBlank()
                || cbNiveau.getValue() == null || cbCategorie.getValue() == null) {
            return;
        }
        int duree;
        try { duree = Integer.parseInt(tfDuree.getText().trim()); } catch (Exception e) { return; }
        if (duree <= 0) return;

        Formation f = new Formation();
        f.setTitre(tfTitre.getText().trim());
        f.setDescription(taDescription.getText().trim());
        f.setDuree(duree);
        f.setNiveau(cbNiveau.getValue());
        f.setCategorie(cbCategorie.getValue());
        f.setCertification(cbCertification.isSelected());
        result = f;
        close();
    }

    @FXML
    public void cancel() { close(); }

    private void close() {
        Stage st = (Stage) tfTitre.getScene().getWindow();
        st.close();
    }

    public Formation getResult() { return result; }
}
