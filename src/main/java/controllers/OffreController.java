package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Offre;
import models.TypeOffre;
import services.OffreService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class OffreController {

    @FXML private TextField txtNomOffre, txtCompetences, txtSalaire, txtRecherche;
    @FXML private ComboBox<TypeOffre> comboType;
    @FXML private TableView<Offre> tableOffre;
    @FXML private TableColumn<Offre, String> colNom, colType, colCompetences;
    @FXML private TableColumn<Offre, Integer> colSalaire;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnReset;

    private final OffreService service = new OffreService();
    private final ObservableList<Offre> list = FXCollections.observableArrayList();
    private FilteredList<Offre> filteredList;

    @FXML
    public void initialize() {

        // 🔹 Remplir ComboBox avec ENUM
        comboType.getItems().setAll(TypeOffre.values());

        // 🔹 Liaison des colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCompetences.setCellValueFactory(new PropertyValueFactory<>("competences"));
        colSalaire.setCellValueFactory(new PropertyValueFactory<>("salaire"));

        loadTable();

        // 🔎 Recherche dynamique
        filteredList = new FilteredList<>(list, b -> true);

        txtRecherche.textProperty().addListener((obs, oldValue, newValue) -> {
            filteredList.setPredicate(offre -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lower = newValue.toLowerCase();

                return offre.getNomOffre().toLowerCase().contains(lower)
                        || offre.getType().name().toLowerCase().contains(lower)
                        || offre.getCompetences().toLowerCase().contains(lower)
                        || String.valueOf(offre.getSalaire()).contains(lower);
            });
        });

        SortedList<Offre> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableOffre.comparatorProperty());
        tableOffre.setItems(sortedList);

        // 🔹 Remplir champs lors sélection
        tableOffre.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtNomOffre.setText(newSel.getNomOffre());
                comboType.setValue(newSel.getType());
                txtCompetences.setText(newSel.getCompetences());
                txtSalaire.setText(String.valueOf(newSel.getSalaire()));
            }
        });

        // 🎯 Salaire chiffres uniquement
        txtSalaire.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtSalaire.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // 🔹 Actions boutons
        btnAjouter.setOnAction(e -> ajouterOffre());
        btnModifier.setOnAction(e -> modifierOffre());
        btnSupprimer.setOnAction(e -> supprimerOffre());
        btnReset.setOnAction(e -> resetForm());
    }

    // 🔹 Charger données
    private void loadTable() {
        try {
            list.clear();
            List<Offre> offres = service.recuperer();
            list.addAll(offres);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // 🔹 Ajouter
    private void ajouterOffre() {

        if (!validerChamps()) return;

        try {
            int salaire = Integer.parseInt(txtSalaire.getText());

            Offre o = new Offre(
                    txtNomOffre.getText(),
                    comboType.getValue(),
                    txtCompetences.getText(),
                    salaire
            );

            service.ajouter(o);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre ajoutée !");
            resetForm();
            loadTable();

        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    // 🔹 Modifier
    private void modifierOffre() {

        Offre o = tableOffre.getSelectionModel().getSelectedItem();

        if (o == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une offre");
            return;
        }

        if (!validerChamps()) return;

        try {
            o.setNomOffre(txtNomOffre.getText());
            o.setType(comboType.getValue());
            o.setCompetences(txtCompetences.getText());
            o.setSalaire(Integer.parseInt(txtSalaire.getText()));

            service.modifier(o);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre modifiée !");
            resetForm();
            loadTable();

        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    // 🔹 Supprimer
    private void supprimerOffre() {

        Offre o = tableOffre.getSelectionModel().getSelectedItem();

        if (o == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une offre");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer cette offre ?");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(o.getIdOffre());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre supprimée !");
                resetForm();
                loadTable();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        }
    }

    // 🔹 Validation
    private boolean validerChamps() {

        if (txtNomOffre.getText().isEmpty()
                || comboType.getValue() == null
                || txtCompetences.getText().isEmpty()
                || txtSalaire.getText().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs");
            return false;
        }

        if (!txtSalaire.getText().matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Salaire doit être numérique");
            return false;
        }

        return true;
    }

    // 🔹 Reset
    private void resetForm() {
        txtNomOffre.clear();
        comboType.getSelectionModel().clearSelection();
        txtCompetences.clear();
        txtSalaire.clear();
        tableOffre.getSelectionModel().clearSelection();
    }

    // 🔹 Alert
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
