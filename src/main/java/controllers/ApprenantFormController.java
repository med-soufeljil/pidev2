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

    private static final String NAME_PATTERN = "^[\\p{L}]+(?:[ '\\-][\\p{L}]+)*$";

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
        String nom = tfNom.getText() == null ? "" : tfNom.getText().trim();
        String prenom = tfPrenom.getText() == null ? "" : tfPrenom.getText().trim();
        String email = tfEmail.getText() == null ? "" : tfEmail.getText().trim();

        if (nom.isBlank() || prenom.isBlank() || email.isBlank() || cbFormation.getValue() == null || dpDebut.getValue() == null) {
            showValidation("Champs obligatoires", "Veuillez remplir tous les champs requis.");
            return;
        }
        if (!nom.matches(NAME_PATTERN) || !prenom.matches(NAME_PATTERN)) {
            showValidation("Nom/Prénom invalide", "Le nom et le prénom doivent contenir uniquement des caractères alphabétiques.");
            return;
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showValidation("Email invalide", "Veuillez saisir une adresse email valide.");
            return;
        }
        if (dpFin.getValue() != null && dpFin.getValue().isBefore(dpDebut.getValue())) {
            showValidation("Dates invalides", "La date de fin doit être postérieure à la date de début.");
            return;
        }
        Apprenant a = new Apprenant();
        a.setNom(nom);
        a.setPrenom(prenom);
        a.setEmail(email);
        a.setStatut(cbStatut.getValue());
        a.setDateDebut(dpDebut.getValue());
        a.setDateFin(dpFin.getValue());
        a.setId_formation(cbFormation.getValue().getId_formation());
        result = a;
        close();
    }

    @FXML
    public void cancel() { close(); }

    private void showValidation(String header, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Validation");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void close() { ((Stage) tfNom.getScene().getWindow()).close(); }

    public Apprenant getResult() { return result; }
}
