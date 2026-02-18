package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Candidat;
import services.CandidatService;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class CandidatController {

    @FXML private TextField txtNom, txtPrenom, txtCIN, txtTel, txtAdresse, txtEmail, txtCv;
    @FXML private TableView<Candidat> tableCandidat;
    @FXML private TableColumn<Candidat, String> colNom, colPrenom, colCIN, colTel, colAdresse, colEmail, colCv;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer;

    private CandidatService service = new CandidatService();
    private ObservableList<Candidat> list = FXCollections.observableArrayList();

    private final Pattern nomPrenomPattern = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");
    private final Pattern emailPattern = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,6}$");

    @FXML
    public void initialize() {
        // Lier les colonnes aux propriétés
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCIN.setCellValueFactory(new PropertyValueFactory<>("CIN"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCv.setCellValueFactory(new PropertyValueFactory<>("cv"));

        loadTable();

        btnAjouter.setOnAction(e -> ajouterCandidat());
        btnModifier.setOnAction(e -> modifierCandidat());
        btnSupprimer.setOnAction(e -> supprimerCandidat());

        tableCandidat.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtNom.setText(newSel.getNom());
                txtPrenom.setText(newSel.getPrenom());
                txtCIN.setText(String.valueOf(newSel.getCIN()));
                txtTel.setText(String.valueOf(newSel.getTel()));
                txtAdresse.setText(newSel.getAdresse());
                txtEmail.setText(newSel.getEmail());
                txtCv.setText(newSel.getCv());
            }
        });
    }

    private void loadTable() {
        try {
            list.clear();
            List<Candidat> candidats = service.recuperer();
            list.addAll(candidats);
            tableCandidat.setItems(list);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private boolean validerChamps() {
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() ||
                txtCIN.getText().isEmpty() || txtTel.getText().isEmpty() ||
                txtAdresse.getText().isEmpty() || txtEmail.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires");
            return false;
        }

        if (!nomPrenomPattern.matcher(txtNom.getText()).matches()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom ne doit pas contenir de chiffres ou caractères spéciaux !");
            return false;
        }

        if (!nomPrenomPattern.matcher(txtPrenom.getText()).matches()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le prénom ne doit pas contenir de chiffres ou caractères spéciaux !");
            return false;
        }

        if (!txtCIN.getText().matches("\\d{8}")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le CIN doit contenir exactement 8 chiffres !");
            return false;
        }

        if (!txtTel.getText().matches("\\d{8}")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le téléphone doit contenir exactement 8 chiffres !");
            return false;
        }

        if (!emailPattern.matcher(txtEmail.getText()).matches()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Adresse email invalide !");
            return false;
        }

        return true;
    }

    private void ajouterCandidat() {
        if (!validerChamps()) return;

        try {
            Candidat c = new Candidat(
                    txtNom.getText(),
                    txtPrenom.getText(),
                    Integer.parseInt(txtCIN.getText()),
                    Integer.parseInt(txtTel.getText()),
                    txtAdresse.getText(),
                    txtEmail.getText(),
                    txtCv.getText()
            );

            service.ajouter(c);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Candidat ajouté !");
            clearFields();
            loadTable();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void modifierCandidat() {
        Candidat c = tableCandidat.getSelectionModel().getSelectedItem();
        if (c == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un candidat");
            return;
        }

        if (!validerChamps()) return;

        try {
            c.setNom(txtNom.getText());
            c.setPrenom(txtPrenom.getText());
            c.setCIN(Integer.parseInt(txtCIN.getText()));
            c.setTel(Integer.parseInt(txtTel.getText()));
            c.setAdresse(txtAdresse.getText());
            c.setEmail(txtEmail.getText());
            c.setCv(txtCv.getText());

            service.modifier(c);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Candidat modifié !");
            clearFields();
            loadTable();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void supprimerCandidat() {
        Candidat c = tableCandidat.getSelectionModel().getSelectedItem();
        if (c == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un candidat");
            return;
        }

        try {
            service.supprimer(c.getIdCandidat());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Candidat supprimé !");
            clearFields();
            loadTable();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void clearFields() {
        txtNom.clear();
        txtPrenom.clear();
        txtCIN.clear();
        txtTel.clear();
        txtAdresse.clear();
        txtEmail.clear();
        txtCv.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
