package controllers;

import entities.Categorie;
import entities.Formation;
import entities.FormationFeedback;
import entities.Niveau;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import services.FeedbackService;
import services.FormationService;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.ResourceBundle;

public class FormationController implements Initializable {

    @FXML private TextField tfRecherche;
    @FXML private ComboBox<String> cbSortBy;
    @FXML private ComboBox<String> cbSortOrder;

    @FXML private TextField tfTitre, tfDuree;
    @FXML private TextArea tfDescription;
    @FXML private ComboBox<Niveau> cbNiveau;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private CheckBox cbCertification;

    @FXML private TableView<Formation> tableFormation;
    @FXML private TableColumn<Formation, Integer> colId;
    @FXML private TableColumn<Formation, String> colTitre;
    @FXML private TableColumn<Formation, Integer> colDuree;
    @FXML private TableColumn<Formation, Niveau> colNiveau;
    @FXML private TableColumn<Formation, Categorie> colCategorie;
    @FXML private TableColumn<Formation, Boolean> colCertif;

    @FXML private TextField tfFeedbackAuteur;
    @FXML private TextArea taFeedback;
    @FXML private Slider slRating;
    @FXML private Label lblRatingValue;
    @FXML private Label lblAverageRating;
    @FXML private TableView<FormationFeedback> tableFeedback;
    @FXML private TableColumn<FormationFeedback, String> colFeedbackAuteur;
    @FXML private TableColumn<FormationFeedback, Integer> colFeedbackNote;
    @FXML private TableColumn<FormationFeedback, String> colFeedbackComment;
    @FXML private TableColumn<FormationFeedback, String> colFeedbackDate;

    private final FormationService service = new FormationService();
    private final FeedbackService feedbackService = new FeedbackService();
    private FilteredList<Formation> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbNiveau.setItems(FXCollections.observableArrayList(Niveau.values()));
        cbCategorie.setItems(FXCollections.observableArrayList(Categorie.values()));

        cbSortBy.setItems(FXCollections.observableArrayList("Titre", "Durée", "Niveau"));
        cbSortBy.setValue("Titre");
        cbSortOrder.setItems(FXCollections.observableArrayList("Asc", "Desc"));
        cbSortOrder.setValue("Asc");

        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId_formation()).asObject());
        colTitre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        colDuree.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDuree()).asObject());
        colNiveau.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getNiveau()));
        colCategorie.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCategorie()));
        colCertif.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isCertification()));

        colFeedbackAuteur.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        colFeedbackNote.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRating()).asObject());
        colFeedbackComment.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getComment()));
        colFeedbackDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt() == null ? "" : data.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        tableFormation.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                remplirChamps(newSel);
                loadFeedback(newSel.getId_formation());
            }
        });

        slRating.valueProperty().addListener((obs, old, val) -> lblRatingValue.setText(String.format("%.1f", val.doubleValue())));
        tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        cbSortBy.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        cbSortOrder.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());

        afficher();
    }

    @FXML
    void ajouter() {
        try {
            if (!valideFormulaire()) return;
            Formation f = new Formation();
            mapFormToFormation(f);
            service.ajouter(f);
            afficher();
            clearForm();
        } catch (SQLException e) {
            alert("Erreur", e.getMessage());
        }
    }

    @FXML
    void modifier() {
        Formation f = tableFormation.getSelectionModel().getSelectedItem();
        if (f == null) {
            alert("Aucune sélection", "Sélectionnez une formation à modifier.");
            return;
        }

        try {
            if (!valideFormulaire()) return;
            mapFormToFormation(f);
            service.modifier(f);
            afficher();
        } catch (SQLException e) {
            alert("Erreur", e.getMessage());
        }
    }

    @FXML
    void supprimer() {
        Formation f = tableFormation.getSelectionModel().getSelectedItem();
        if (f == null) {
            alert("Aucune sélection", "Sélectionnez une formation à supprimer.");
            return;
        }

        try {
            service.supprimer(f.getId_formation());
            afficher();
            clearForm();
            tableFeedback.getItems().clear();
            lblAverageRating.setText("0.0 / 5");
        } catch (SQLException e) {
            alert("Erreur", e.getMessage());
        }
    }

    @FXML
    void afficher() {
        try {
            filteredData = new FilteredList<>(FXCollections.observableArrayList(service.recuperer()), p -> true);
            applyFiltersAndSort();
        } catch (SQLException e) {
            alert("Erreur DB", e.getMessage());
        }
    }

    @FXML
    void addFeedback() {
        Formation selected = tableFormation.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Feedback", "Sélectionnez une formation avant d'ajouter un feedback.");
            return;
        }
        if (tfFeedbackAuteur.getText().isBlank() || taFeedback.getText().isBlank()) {
            alert("Feedback", "Auteur et commentaire sont obligatoires.");
            return;
        }

        FormationFeedback feedback = new FormationFeedback();
        feedback.setFormationId(selected.getId_formation());
        feedback.setAuthor(tfFeedbackAuteur.getText().trim());
        feedback.setComment(taFeedback.getText().trim());
        feedback.setRating((int) Math.round(slRating.getValue()));

        try {
            feedbackService.addFeedback(feedback);
            taFeedback.clear();
            loadFeedback(selected.getId_formation());
        } catch (SQLException e) {
            alert("Erreur feedback", e.getMessage());
        }
    }


    @FXML
    void afficherFeedbacks() {
        Formation selected = tableFormation.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Feedback", "Sélectionnez une formation pour afficher ses feedbacks.");
            return;
        }
        try {
            var feedbacks = FXCollections.observableArrayList(feedbackService.getByFormation(selected.getId_formation()));

            TableView<FormationFeedback> popupTable = new TableView<>(feedbacks);
            popupTable.getStyleClass().add("feedback-popup-table");

            TableColumn<FormationFeedback, String> auteurCol = new TableColumn<>("Auteur");
            auteurCol.setPrefWidth(150);
            auteurCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthor()));

            TableColumn<FormationFeedback, Integer> noteCol = new TableColumn<>("Note");
            noteCol.setPrefWidth(70);
            noteCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getRating()).asObject());

            TableColumn<FormationFeedback, String> commentaireCol = new TableColumn<>("Commentaire");
            commentaireCol.setPrefWidth(330);
            commentaireCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getComment()));

            TableColumn<FormationFeedback, String> dateCol = new TableColumn<>("Date");
            dateCol.setPrefWidth(170);
            dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getCreatedAt() == null ? "N/A" : d.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

            popupTable.getColumns().setAll(auteurCol, noteCol, commentaireCol, dateCol);
            popupTable.setPrefSize(760, 420);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Feedbacks - " + selected.getTitre());
            dialog.getDialogPane().setContent(popupTable);
            dialog.getDialogPane().setPrefSize(790, 500);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

            loadFeedback(selected.getId_formation());
        } catch (SQLException e) {
            alert("Feedback", e.getMessage());
        }
    }
    @FXML
    void retourMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
            Stage stage = (Stage) tfTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            alert("Navigation", e.getMessage());
        }
    }

    private void loadFeedback(int formationId) {
        try {
            tableFeedback.setItems(FXCollections.observableArrayList(feedbackService.getByFormation(formationId)));
            lblAverageRating.setText(String.format("%.1f / 5", feedbackService.getAverageRating(formationId)));
        } catch (SQLException e) {
            alert("Feedback", e.getMessage());
        }
    }

    private void mapFormToFormation(Formation f) {
        f.setTitre(tfTitre.getText().trim());
        f.setDescription(tfDescription.getText().trim());
        f.setDuree(Integer.parseInt(tfDuree.getText().trim()));
        f.setNiveau(cbNiveau.getValue());
        f.setCategorie(cbCategorie.getValue());
        f.setCertification(cbCertification.isSelected());
    }

    private void remplirChamps(Formation f) {
        tfTitre.setText(f.getTitre());
        tfDescription.setText(f.getDescription());
        tfDuree.setText(String.valueOf(f.getDuree()));
        cbNiveau.setValue(f.getNiveau());
        cbCategorie.setValue(f.getCategorie());
        cbCertification.setSelected(f.isCertification());
    }

    private void clearForm() {
        tfTitre.clear();
        tfDescription.clear();
        tfDuree.clear();
        cbNiveau.setValue(null);
        cbCategorie.setValue(null);
        cbCertification.setSelected(false);
    }

    private boolean valideFormulaire() {
        if (tfTitre.getText().isBlank() || tfDescription.getText().isBlank() || tfDuree.getText().isBlank()) {
            alert("Champs manquants", "Veuillez remplir tous les champs.");
            return false;
        }
        if (!tfTitre.getText().matches("[a-zA-ZÀ-ÿ0-9\\s-]+")) {
            alert("Titre invalide", "Le titre contient des caractères non autorisés.");
            return false;
        }
        try {
            int d = Integer.parseInt(tfDuree.getText().trim());
            if (d <= 0) {
                alert("Durée invalide", "La durée doit être > 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            alert("Durée invalide", "La durée doit être un nombre entier.");
            return false;
        }
        if (cbNiveau.getValue() == null || cbCategorie.getValue() == null) {
            alert("Champs manquants", "Niveau et catégorie sont obligatoires.");
            return false;
        }
        return true;
    }

    private void applyFiltersAndSort() {
        if (filteredData == null) return;
        String text = tfRecherche.getText() == null ? "" : tfRecherche.getText().toLowerCase().trim();
        filteredData.setPredicate(f -> text.isEmpty()
                || f.getTitre().toLowerCase().contains(text)
                || f.getDescription().toLowerCase().contains(text)
                || f.getNiveau().name().toLowerCase().contains(text)
                || f.getCategorie().name().toLowerCase().contains(text));

        Comparator<Formation> comparator;
        switch (cbSortBy.getValue()) {
            case "Durée" -> comparator = Comparator.comparingInt(Formation::getDuree);
            case "Niveau" -> comparator = Comparator.comparing(f -> f.getNiveau().name());
            default -> comparator = Comparator.comparing(Formation::getTitre, String.CASE_INSENSITIVE_ORDER);
        }
        if ("Desc".equals(cbSortOrder.getValue())) comparator = comparator.reversed();

        SortedList<Formation> sorted = new SortedList<>(filteredData);
        sorted.setComparator(comparator);
        tableFormation.setItems(sorted);
    }

    private void alert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Gestion Formations");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
