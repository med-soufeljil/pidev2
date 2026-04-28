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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

    @FXML private VBox boxEdition;
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
        initActionColumn();

        tfRecherche.textProperty().addListener((obs, o, n) -> applyFiltersAndSort());
        cbSortBy.valueProperty().addListener((obs, o, n) -> applyFiltersAndSort());
        cbSortOrder.valueProperty().addListener((obs, o, n) -> applyFiltersAndSort());
        tableFormation.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                try {
                    lblAverageRating.setText(String.format("%.1f / 5", feedbackService.getAverageRating(n.getId_formation())));
                } catch (SQLException ignored) {}
            }
        });

        afficher();
        applyRolePermissions();
    }

    private void initActionColumn() {
        colPostulerAction.setCellFactory(param -> new TableCell<>() {
            private final HBox box = new HBox(6);
            private final Button editBtn = new Button("✎");
            private final Button delBtn = new Button("🗑");
            private final Button postulerBtn = new Button("Postuler");
            {
                editBtn.getStyleClass().addAll("action-edit-btn", "action-icon-btn");
                delBtn.getStyleClass().addAll("action-delete-btn", "action-icon-btn");
                postulerBtn.getStyleClass().add("postuler-row-btn");
                box.getStyleClass().add("action-column-box");
                editBtn.setOnAction(e -> {
                    Formation f = getTableView().getItems().get(getIndex());
                    openFormationFormDialog(f);
                });
                delBtn.setOnAction(e -> {
                    Formation f = getTableView().getItems().get(getIndex());
                    tableFormation.getSelectionModel().select(f);
                    supprimer();
                });
                postulerBtn.setOnAction(e -> postulerForFormation(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                box.getChildren().clear();
                if (SessionContext.isAdmin()) {
                    box.getChildren().addAll(editBtn, delBtn);
                } else {
                    box.getChildren().add(postulerBtn);
                }
                setGraphic(box);
            }
        });
    }

    @FXML
    void ajouter() {
        if (!SessionContext.isAdmin()) return;
        openFormationFormDialog(null);
    }

    @FXML
    void modifier() {
        Formation f = tableFormation.getSelectionModel().getSelectedItem();
        if (f != null) openFormationFormDialog(f);
    }

    private void openFormationFormDialog(Formation initial) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormationFormView.fxml"));
            Parent root = loader.load();
            FormationFormController controller = loader.getController();
            controller.setInitial(initial);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle(initial == null ? "Ajouter formation" : "Modifier formation");
            stage.showAndWait();

            Formation result = controller.getResult();
            if (result == null) return;
            if (initial == null) {
                service.ajouter(result);
            } else {
                initial.setTitre(result.getTitre());
                initial.setDescription(result.getDescription());
                initial.setDuree(result.getDuree());
                initial.setNiveau(result.getNiveau());
                initial.setCategorie(result.getCategorie());
                initial.setCertification(result.isCertification());
                service.modifier(initial);
            }
            afficher();
        } catch (Exception e) {
            alert("Formulaire", e.getMessage());
        }
    }

    @FXML
    void supprimer() {
        if (!SessionContext.isAdmin()) return;
        Formation f = tableFormation.getSelectionModel().getSelectedItem();
        if (f == null) return;
        try {
            service.supprimer(f.getId_formation());
            afficher();
        } catch (SQLException e) {
            alert("Erreur", e.getMessage());
        }
    }

    @FXML
    void afficher() {
        try {
            filteredData = new FilteredList<>(FXCollections.observableArrayList(service.recuperer()), p -> true);
            applyFiltersAndSort();
            tableFormation.refresh();
        } catch (SQLException e) {
            alert("Erreur DB", e.getMessage());
        }
    }

    @FXML
    void addFeedback() {
        Formation selected = tableFormation.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Feedback", "Sélectionnez une formation.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FeedbackFormView.fxml"));
            Parent root = loader.load();
            FeedbackFormController controller = loader.getController();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter feedback");
            stage.showAndWait();

            if (!controller.isSaved()) return;
            FormationFeedback feedback = new FormationFeedback();
            feedback.setFormationId(selected.getId_formation());
            feedback.setAuthor(controller.getAuteur());
            feedback.setComment(controller.getCommentaire());
            feedback.setRating(controller.getNote());
            feedbackService.addFeedback(feedback);
            lblAverageRating.setText(String.format("%.1f / 5", feedbackService.getAverageRating(selected.getId_formation())));
        } catch (Exception e) {
            alert("Feedback", e.getMessage());
        }
    }

    @FXML
    void afficherFeedbacks() {
        if (!SessionContext.isAdmin()) return;
        Formation selected = tableFormation.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            var feedbacks = FXCollections.observableArrayList(feedbackService.getByFormation(selected.getId_formation()));
            TableView<FormationFeedback> popupTable = new TableView<>(feedbacks);
            popupTable.getStyleClass().add("feedback-popup-table");

            TableColumn<FormationFeedback, String> auteurCol = new TableColumn<>("Auteur");
            auteurCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthor()));
            TableColumn<FormationFeedback, String> noteCol = new TableColumn<>("Note");
            noteCol.setCellValueFactory(d -> new SimpleStringProperty(stars(d.getValue().getRating()) + " (" + d.getValue().getRating() + "/5)"));
            TableColumn<FormationFeedback, String> commentaireCol = new TableColumn<>("Commentaire");
            commentaireCol.setPrefWidth(360);
            commentaireCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getComment() == null ? "" : d.getValue().getComment()));
            TableColumn<FormationFeedback, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getCreatedAt() == null ? "N/A" : d.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            popupTable.getColumns().setAll(auteurCol, noteCol, commentaireCol, dateCol);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Feedbacks - " + selected.getTitre());
            dialog.getDialogPane().setContent(popupTable);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (SQLException e) {
            alert("Feedback", e.getMessage());
        }
    }

    @FXML
    void retourMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Mainformation.fxml"));
            Stage stage = (Stage) tableFormation.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            alert("Navigation", e.getMessage());
        }
    }

    private void applyFiltersAndSort() {
        if (filteredData == null) return;
        String text = tfRecherche.getText() == null ? "" : tfRecherche.getText().toLowerCase().trim();
        filteredData.setPredicate(f -> text.isEmpty()
                || f.getTitre().toLowerCase().contains(text)
                || f.getDescription().toLowerCase().contains(text)
                || f.getNiveau().name().toLowerCase().contains(text)
                || f.getCategorie().name().toLowerCase().contains(text));

        Comparator<Formation> comparator = switch (cbSortBy.getValue()) {
            case "Durée" -> Comparator.comparingInt(Formation::getDuree);
            case "Niveau" -> Comparator.comparing(f -> f.getNiveau().name());
            default -> Comparator.comparing(Formation::getTitre, String.CASE_INSENSITIVE_ORDER);
        };
        if ("Desc".equals(cbSortOrder.getValue())) comparator = comparator.reversed();

        SortedList<Formation> sorted = new SortedList<>(filteredData);
        sorted.setComparator(comparator);
        tableFormation.setItems(sorted);
    }

    @FXML
    void postuler() {
        Formation selected = tableFormation.getSelectionModel().getSelectedItem();
        if (selected != null) postulerForFormation(selected);
    }

    private void postulerForFormation(Formation selected) {
        if (!SessionContext.isUser() || selected == null) return;
        SessionContext.setPendingFormation(selected.getId_formation(), selected.getTitre());
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ApprenantView.fxml"));
            Stage stage = (Stage) tableFormation.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            alert("Postuler", e.getMessage());
        }
    }

    private void applyRolePermissions() {
        boolean userMode = SessionContext.isUser();
        if (btnAfficherFeedbacks != null) btnAfficherFeedbacks.setVisible(!userMode);
        if (btnAfficherFeedbacks != null) btnAfficherFeedbacks.setManaged(!userMode);
        if (tableFeedback != null) tableFeedback.setVisible(false);
        if (tableFeedback != null) tableFeedback.setManaged(false);
    }

    private String stars(int rating) {
        int v = Math.max(0, Math.min(5, rating));
        return "★".repeat(v) + "☆".repeat(5 - v);
    }

    private void alert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Gestion Formations");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
