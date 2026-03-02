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
import javafx.scene.control.TableCell;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.FeedbackService;
import services.FormationService;
import utils.SessionContext;

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
    @FXML private TableColumn<Formation, Void> colPostulerAction;
    @FXML private TableColumn<Formation, Void> colDeleteAction;

    @FXML private VBox boxEdition;
    @FXML private HBox hbCrud;
    @FXML private Button btnPostuler;
    @FXML private Button btnAfficherFeedbacks;

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
        if (colPostulerAction != null) {
            colPostulerAction.setCellFactory(param -> new TableCell<>() {
                private final Button rowAction = new Button();
                {
                    rowAction.setOnAction(event -> {
                        Formation formation = getTableView().getItems().get(getIndex());
                        if (SessionContext.isUser()) {
                            postulerForFormation(formation);
                        } else {
                            openEditDialogForFormation(formation);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        return;
                    }
                    if (SessionContext.isUser()) {
                        rowAction.setText("Postuler");
                        rowAction.getStyleClass().setAll("postuler-row-btn");
                    } else {
                        rowAction.setText("✏");
                        rowAction.getStyleClass().setAll("secondary-btn");
                    }
                    setGraphic(rowAction);
                }
            });
        }
        if (colDeleteAction != null) {
            colDeleteAction.setCellFactory(param -> new TableCell<>() {
                private final Button deleteBtn = new Button("🗑");
                {
                    deleteBtn.getStyleClass().setAll("danger-btn");
                    deleteBtn.setOnAction(event -> {
                        Formation formation = getTableView().getItems().get(getIndex());
                        tableFormation.getSelectionModel().select(formation);
                        supprimer();
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || SessionContext.isUser()) {
                        setGraphic(null);
                    } else {
                        setGraphic(deleteBtn);
                    }
                }
            });
        }

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
        applyRolePermissions();
    }

    @FXML
    void ajouter() {
        if (!SessionContext.isAdmin()) {
            alert("Accès", "Seul l'admin peut ajouter.");
            return;
        }
        FormData data = showFormationFormDialog("Ajouter une formation", null);
        if (data == null) return;
        try {
            Formation f = new Formation();
            applyFormDataToFormation(f, data);
            service.ajouter(f);
            afficher();
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
        openEditDialogForFormation(f);
    }

    private void openEditDialogForFormation(Formation f) {
        if (!SessionContext.isAdmin()) {
            alert("Accès", "Seul l'admin peut modifier.");
            return;
        }
        FormData data = showFormationFormDialog("Modifier la formation", f);
        if (data == null) return;
        try {
            applyFormDataToFormation(f, data);
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

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un feedback");
        TextField authorField = new TextField();
        authorField.setPromptText("Votre nom");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Votre feedback");
        commentArea.setPrefRowCount(3);
        Slider ratingSlider = new Slider(1, 5, 4);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setSnapToTicks(true);

        VBox box = new VBox(10, new Label("Auteur"), authorField, new Label("Commentaire"), commentArea, new Label("Note"), ratingSlider);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        if (authorField.getText().isBlank() || commentArea.getText().isBlank()) {
            alert("Feedback", "Auteur et commentaire sont obligatoires.");
            return;
        }

        FormationFeedback feedback = new FormationFeedback();
        feedback.setFormationId(selected.getId_formation());
        feedback.setAuthor(authorField.getText().trim());
        feedback.setComment(commentArea.getText().trim());
        feedback.setRating((int) Math.round(ratingSlider.getValue()));

        try {
            feedbackService.addFeedback(feedback);
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

    private void applyFormDataToFormation(Formation f, FormData data) {
        f.setTitre(data.titre().trim());
        f.setDescription(data.description().trim());
        f.setDuree(data.duree());
        f.setNiveau(data.niveau());
        f.setCategorie(data.categorie());
        f.setCertification(data.certification());
    }

    private FormData showFormationFormDialog(String title, Formation initial) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);

        TextField titreField = new TextField(initial != null ? initial.getTitre() : "");
        TextArea descField = new TextArea(initial != null ? initial.getDescription() : "");
        descField.setPrefRowCount(3);
        TextField dureeField = new TextField(initial != null ? String.valueOf(initial.getDuree()) : "");
        ComboBox<Niveau> niveauField = new ComboBox<>(FXCollections.observableArrayList(Niveau.values()));
        ComboBox<Categorie> categorieField = new ComboBox<>(FXCollections.observableArrayList(Categorie.values()));
        CheckBox certifField = new CheckBox("Certification incluse");

        if (initial != null) {
            niveauField.setValue(initial.getNiveau());
            categorieField.setValue(initial.getCategorie());
            certifField.setSelected(initial.isCertification());
        }

        VBox box = new VBox(8,
                new Label("Titre"), titreField,
                new Label("Description"), descField,
                new Label("Durée (heures)"), dureeField,
                new Label("Niveau"), niveauField,
                new Label("Catégorie"), categorieField,
                certifField);

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return null;
        }

        String titre = titreField.getText() == null ? "" : titreField.getText().trim();
        String desc = descField.getText() == null ? "" : descField.getText().trim();
        if (titre.isBlank() || desc.isBlank() || dureeField.getText().isBlank()) {
            alert("Champs manquants", "Veuillez remplir tous les champs.");
            return null;
        }
        if (!titre.matches("[a-zA-ZÀ-ÿ0-9\s-]+")) {
            alert("Titre invalide", "Le titre contient des caractères non autorisés.");
            return null;
        }

        int duree;
        try {
            duree = Integer.parseInt(dureeField.getText().trim());
        } catch (NumberFormatException ex) {
            alert("Durée invalide", "La durée doit être un nombre entier.");
            return null;
        }
        if (duree <= 0) {
            alert("Durée invalide", "La durée doit être > 0.");
            return null;
        }
        if (niveauField.getValue() == null || categorieField.getValue() == null) {
            alert("Champs manquants", "Niveau et catégorie sont obligatoires.");
            return null;
        }
        return new FormData(titre, desc, duree, niveauField.getValue(), categorieField.getValue(), certifField.isSelected());
    }

    private void remplirChamps(Formation f) {
        // Le formulaire d'édition s'ouvre désormais dans une popup dédiée (add/modify).
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


    @FXML
    void postuler() {
        if (!SessionContext.isUser()) {
            alert("Postuler", "Ce bouton est destiné à l'espace USER.");
            return;
        }
        Formation selected = tableFormation.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Postuler", "Sélectionnez une formation pour postuler.");
            return;
        }
        postulerForFormation(selected);
    }

    private void postulerForFormation(Formation selected) {
        if (selected == null) {
            return;
        }
        SessionContext.setPendingFormation(selected.getId_formation(), selected.getTitre());
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ApprenantView.fxml"));
            Stage stage = (Stage) tableFormation.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            alert("Postuler", "Impossible d'ouvrir le formulaire apprenant: " + e.getMessage());
        }
    }

    private void applyRolePermissions() {
        boolean userMode = SessionContext.isUser();

        tfTitre.setVisible(false);
        tfTitre.setManaged(false);
        tfDescription.setVisible(false);
        tfDescription.setManaged(false);
        tfDuree.setVisible(false);
        tfDuree.setManaged(false);
        cbNiveau.setVisible(false);
        cbNiveau.setManaged(false);
        cbCategorie.setVisible(false);
        cbCategorie.setManaged(false);
        cbCertification.setVisible(false);
        cbCertification.setManaged(false);

        if (hbCrud != null) hbCrud.setVisible(false);
        if (hbCrud != null) hbCrud.setManaged(false);
        if (btnPostuler != null) btnPostuler.setVisible(userMode);
        if (btnPostuler != null) btnPostuler.setManaged(userMode);
        if (btnAfficherFeedbacks != null) btnAfficherFeedbacks.setVisible(!userMode);
        if (btnAfficherFeedbacks != null) btnAfficherFeedbacks.setManaged(!userMode);
        if (tableFeedback != null) tableFeedback.setVisible(!userMode);
        if (tableFeedback != null) tableFeedback.setManaged(!userMode);
        if (colPostulerAction != null) colPostulerAction.setVisible(true);
        if (colDeleteAction != null) colDeleteAction.setVisible(!userMode);
        if (tableFormation != null) tableFormation.refresh();
    }
    private void alert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Gestion Formations");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }


    private record FormData(String titre, String description, int duree, Niveau niveau, Categorie categorie, boolean certification) {}
}
