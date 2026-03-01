package controllers;

import entities.Apprenant;
import entities.Formation;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ApprenantService;
import services.FormationService;
import services.MailingApiService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ApprenantController implements Initializable {

    @FXML private TextField tfNom, tfPrenom, tfEmail, tfRecherche;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDebut, dpFin;
    @FXML private ComboBox<Formation> cbFormation;
    @FXML private ComboBox<String> cbSortBy;
    @FXML private ComboBox<String> cbSortOrder;

    @FXML private TableView<Apprenant> tableApprenant;
    @FXML private TableColumn<Apprenant, Integer> colId;
    @FXML private TableColumn<Apprenant, String> colNom;
    @FXML private TableColumn<Apprenant, String> colPrenom;
    @FXML private TableColumn<Apprenant, String> colEmail;
    @FXML private TableColumn<Apprenant, String> colStatut;
    @FXML private TableColumn<Apprenant, String> colFormation;

    private final ApprenantService service = new ApprenantService();
    private final FormationService formationService = new FormationService();
    private final MailingApiService mailingApiService = new MailingApiService();
    private FilteredList<Apprenant> filteredList;
    private List<Formation> formations;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            formations = formationService.recuperer();
            cbFormation.setItems(FXCollections.observableArrayList(formations));
            cbFormation.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Formation f) {
                    return f != null ? f.getTitre() : "";
                }

                @Override
                public Formation fromString(String s) {
                    return cbFormation.getItems().stream().filter(f -> f.getTitre().equals(s)).findFirst().orElse(null);
                }
            });

            cbStatut.setItems(FXCollections.observableArrayList("ACTIF", "EN_PAUSE", "TERMINE"));
            cbStatut.setValue("ACTIF");
            cbSortBy.setItems(FXCollections.observableArrayList("Nom", "Prénom", "Statut"));
            cbSortBy.setValue("Nom");
            cbSortOrder.setItems(FXCollections.observableArrayList("Asc", "Desc"));
            cbSortOrder.setValue("Asc");

            colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdApprenant()).asObject());
            colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
            colPrenom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
            colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
            colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));
            colFormation.setCellValueFactory(data -> new SimpleStringProperty(getTitreFormation(data.getValue().getId_formation())));

            refreshTable();
            tableApprenant.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) fillForm(newSel);
            });

            tfRecherche.textProperty().addListener((obs, o, n) -> applyFilterSort());
            cbSortBy.valueProperty().addListener((obs, o, n) -> applyFilterSort());
            cbSortOrder.valueProperty().addListener((obs, o, n) -> applyFilterSort());

        } catch (SQLException e) {
            alert("Init", e.getMessage());
        }
    }

    @FXML
    public void ajouter() {
        try {
            if (!validateForm()) return;
            Apprenant a = buildFromForm(new Apprenant());
            service.ajouter(a);
            sendRegistrationEmail(a);
            refreshTable();
            clearFields();
        } catch (SQLException e) {
            alert("Ajout", e.getMessage());
        }
    }

    @FXML
    public void modifier() {
        Apprenant selected = tableApprenant.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Modification", "Sélectionnez un apprenant.");
            return;
        }
        try {
            if (!validateForm()) return;
            buildFromForm(selected);
            service.modifier(selected);
            refreshTable();
        } catch (SQLException e) {
            alert("Modification", e.getMessage());
        }
    }

    @FXML
    public void supprimer() {
        Apprenant selected = tableApprenant.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Suppression", "Sélectionnez un apprenant.");
            return;
        }
        try {
            service.supprimer(selected.getIdApprenant());
            refreshTable();
            clearFields();
        } catch (SQLException e) {
            alert("Suppression", e.getMessage());
        }
    }


    @FXML
    public void envoyerMail() {
        if (tfEmail.getText().isBlank() || tfNom.getText().isBlank() || tfPrenom.getText().isBlank()) {
            alert("Mail", "Veuillez remplir nom, prénom et email avant l'envoi.");
            return;
        }
        Formation formation = cbFormation.getValue();
        String formationTitle = formation != null ? formation.getTitre() : "Formation";
        boolean sent = mailingApiService.sendRegistrationEmail(
                tfEmail.getText().trim(),
                tfPrenom.getText().trim() + " " + tfNom.getText().trim(),
                formationTitle
        );
        Alert info = new Alert(sent ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        info.setTitle("Mail inscription");
        info.setHeaderText(sent ? "Email envoyé" : "Email non envoyé");
        info.setContentText(sent
                ? "Le mail d'inscription a été envoyé via MailerSend."
                : "L'envoi du mail a échoué: " + mailingApiService.getLastError());
        info.showAndWait();
    }
    @FXML
    public void retourMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
            Stage stage = (Stage) tfNom.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            alert("Navigation", e.getMessage());
        }
    }

    @FXML
    public void afficher() {
        refreshTable();
    }

    private void refreshTable() {
        try {
            filteredList = new FilteredList<>(FXCollections.observableArrayList(service.recuperer()), p -> true);
            applyFilterSort();
        } catch (SQLException e) {
            alert("Chargement", e.getMessage());
        }
    }

    private void applyFilterSort() {
        if (filteredList == null) return;
        String text = tfRecherche.getText() == null ? "" : tfRecherche.getText().toLowerCase();
        filteredList.setPredicate(a -> text.isBlank()
                || safe(a.getNom()).contains(text)
                || safe(a.getPrenom()).contains(text)
                || safe(a.getEmail()).contains(text)
                || safe(a.getStatut()).contains(text)
                || getTitreFormation(a.getId_formation()).toLowerCase().contains(text));

        Comparator<Apprenant> comparator = switch (cbSortBy.getValue()) {
            case "Prénom" -> Comparator.comparing(a -> safe(a.getPrenom()));
            case "Statut" -> Comparator.comparing(a -> safe(a.getStatut()));
            default -> Comparator.comparing(a -> safe(a.getNom()));
        };
        if ("Desc".equals(cbSortOrder.getValue())) comparator = comparator.reversed();

        SortedList<Apprenant> sorted = new SortedList<>(filteredList);
        sorted.setComparator(comparator);
        tableApprenant.setItems(sorted);
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private void fillForm(Apprenant a) {
        tfNom.setText(a.getNom());
        tfPrenom.setText(a.getPrenom());
        tfEmail.setText(a.getEmail());
        cbStatut.setValue(a.getStatut());
        dpDebut.setValue(a.getDateDebut());
        dpFin.setValue(a.getDateFin());
        cbFormation.getItems().stream()
                .filter(f -> f.getId_formation() == a.getId_formation())
                .findFirst()
                .ifPresent(cbFormation::setValue);
    }

    private Apprenant buildFromForm(Apprenant a) {
        a.setNom(tfNom.getText().trim());
        a.setPrenom(tfPrenom.getText().trim());
        a.setEmail(tfEmail.getText().trim());
        a.setStatut(cbStatut.getValue());
        a.setDateDebut(dpDebut.getValue());
        a.setDateFin(dpFin.getValue());
        a.setId_formation(cbFormation.getValue().getId_formation());
        return a;
    }

    private boolean validateForm() {
        if (tfNom.getText().isBlank() || tfPrenom.getText().isBlank() || tfEmail.getText().isBlank()
                || cbStatut.getValue() == null || cbFormation.getValue() == null || dpDebut.getValue() == null) {
            alert("Validation", "Tous les champs obligatoires doivent être remplis.");
            return false;
        }
        if (!tfEmail.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            alert("Validation", "Email invalide.");
            return false;
        }
        LocalDate end = dpFin.getValue();
        if (end != null && end.isBefore(dpDebut.getValue())) {
            alert("Validation", "La date de fin doit être >= date début.");
            return false;
        }
        return true;
    }

    private String getTitreFormation(int idFormation) {
        return formations.stream().filter(f -> f.getId_formation() == idFormation).map(Formation::getTitre).findFirst().orElse("N/A");
    }


    private void sendRegistrationEmail(Apprenant apprenant) {
        Formation formation = cbFormation.getValue();
        String formationTitle = formation != null ? formation.getTitre() : "Formation";
        boolean sent = mailingApiService.sendRegistrationEmail(
                apprenant.getEmail(),
                apprenant.getPrenom() + " " + apprenant.getNom(),
                formationTitle
        );
        if (!sent) {
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setTitle("Notification");
            warn.setHeaderText("Inscription enregistrée");
            warn.setContentText("L'appel MailerSend a échoué. Vérifiez vos variables d'environnement MailerSend.");
            warn.showAndWait();
        }
    }
    private void clearFields() {
        tfNom.clear();
        tfPrenom.clear();
        tfEmail.clear();
        cbStatut.setValue("ACTIF");
        dpDebut.setValue(null);
        dpFin.setValue(null);
        cbFormation.setValue(null);
    }

    private void alert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Gestion Apprenants");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
