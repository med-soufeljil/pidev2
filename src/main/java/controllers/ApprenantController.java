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
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.application.Platform;
import services.ApprenantService;
import services.FormationService;
import services.MailingApiService;
import utils.SessionContext;

import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ApprenantController implements Initializable {

    @FXML private TextField tfRecherche;
    @FXML private ComboBox<String> cbSortBy;
    @FXML private ComboBox<String> cbSortOrder;

    @FXML private TableView<Apprenant> tableApprenant;
    @FXML private TableColumn<Apprenant, Integer> colId;
    @FXML private TableColumn<Apprenant, String> colNom;
    @FXML private TableColumn<Apprenant, String> colPrenom;
    @FXML private TableColumn<Apprenant, String> colEmail;
    @FXML private TableColumn<Apprenant, String> colStatut;
    @FXML private TableColumn<Apprenant, String> colFormation;
    @FXML private TableColumn<Apprenant, Void> colAction;
    @FXML private Label lblCount;

    private final ApprenantService service = new ApprenantService();
    private final FormationService formationService = new FormationService();
    private final MailingApiService mailingApiService = new MailingApiService();
    private FilteredList<Apprenant> filteredList;
    private List<Formation> formations;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            formations = formationService.recuperer();
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
            initActionColumn();

            refreshTable();
            applyPendingFormationSelection();
            tfRecherche.textProperty().addListener((obs, o, n) -> applyFilterSort());
            cbSortBy.valueProperty().addListener((obs, o, n) -> applyFilterSort());
            cbSortOrder.valueProperty().addListener((obs, o, n) -> applyFilterSort());

        } catch (SQLException e) {
            alert("Init", e.getMessage());
        }
    }

    private void initActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final HBox box = new HBox(6);
            private final Button editBtn = new Button("✎");
            private final Button delBtn = new Button("🗑");
            {
                editBtn.getStyleClass().addAll("action-edit-btn", "action-icon-btn");
                delBtn.getStyleClass().addAll("action-delete-btn", "action-icon-btn");
                box.getStyleClass().add("action-column-box");
                editBtn.setOnAction(e -> {
                    Apprenant a = getTableView().getItems().get(getIndex());
                    openFormDialog(a);
                });
                delBtn.setOnAction(e -> {
                    Apprenant a = getTableView().getItems().get(getIndex());
                    try { service.supprimer(a.getIdApprenant()); refreshTable(); } catch (SQLException ex) { alert("Suppression", ex.getMessage()); }
                });
                box.getChildren().addAll(editBtn, delBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    public void ajouter() {
        openFormDialog(null);
    }

    private void openFormDialog(Apprenant initial) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ApprenantFormView.fxml"));
            Parent root = loader.load();
            ApprenantFormController c = loader.getController();
            c.setFormations(formations);
            c.setInitial(initial);
            if (initial == null && SessionContext.hasPendingFormation()) {
                c.setPendingFormationId(SessionContext.getPendingFormationId());
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (tableApprenant != null && tableApprenant.getScene() != null) {
                stage.initOwner(tableApprenant.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            stage.setTitle(initial == null ? "Ajouter apprenant" : "Modifier apprenant");
            stage.showAndWait();

            Apprenant result = c.getResult();
            if (result == null) return;
            if (initial == null) {
                service.ajouter(result);
                sendRegistrationEmail(result);
                if (SessionContext.isUser()) {
                    Parent rootBack = FXMLLoader.load(getClass().getResource("/FormationView.fxml"));
                    Stage mainStage = null;
                    Window owner = stage.getOwner();
                    if (owner instanceof Stage) {
                        mainStage = (Stage) owner;
                    } else if (tableApprenant != null && tableApprenant.getScene() != null) {
                        mainStage = (Stage) tableApprenant.getScene().getWindow();
                    }
                    if (mainStage == null && result != null && stage.getScene() != null) {
                        mainStage = (Stage) stage.getScene().getWindow();
                    }
                    if (mainStage != null) {
                        mainStage.setScene(new Scene(rootBack));
                        mainStage.show();
                    }
                    SessionContext.clearPendingFormation();
                    return;
                }
            } else {
                initial.setNom(result.getNom());
                initial.setPrenom(result.getPrenom());
                initial.setEmail(result.getEmail());
                initial.setStatut(result.getStatut());
                initial.setDateDebut(result.getDateDebut());
                initial.setDateFin(result.getDateFin());
                initial.setId_formation(result.getId_formation());
                service.modifier(initial);
            }
            refreshTable();
        } catch (Exception e) {
            alert("Formulaire", e.getMessage());
        }
    }

    @FXML
    public void envoyerMail() {
        Apprenant selected = tableApprenant.getSelectionModel().getSelectedItem();
        if (selected == null) { alert("Mail", "Sélectionnez un apprenant."); return; }
        Formation formation = formations.stream().filter(f -> f.getId_formation() == selected.getId_formation()).findFirst().orElse(null);
        String formationTitle = formation != null ? formation.getTitre() : "Formation";
        boolean sent = mailingApiService.sendRegistrationEmail(selected.getEmail(), selected.getPrenom() + " " + selected.getNom(), formationTitle);
        Alert info = new Alert(sent ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        info.setHeaderText(sent ? "Email envoyé" : "Email non envoyé");
        info.setContentText(sent ? "Mail envoyé avec succès." : "Echec envoi: " + mailingApiService.getLastError());
        info.showAndWait();
    }

    @FXML
    public void retourMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
            Stage stage = (Stage) tableApprenant.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            alert("Navigation", e.getMessage());
        }
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
        if (lblCount != null) lblCount.setText(String.valueOf(sorted.size()));
    }

    private void applyPendingFormationSelection() {
        if (!SessionContext.hasPendingFormation()) return;
        Platform.runLater(() -> {
            if (SessionContext.hasPendingFormation()) {
                openFormDialog(null);
            }
        });
    }

    private String safe(String s) { return s == null ? "" : s.toLowerCase(); }

    private String getTitreFormation(int idFormation) {
        return formations.stream().filter(f -> f.getId_formation() == idFormation).map(Formation::getTitre).findFirst().orElse("N/A");
    }

    private void sendRegistrationEmail(Apprenant apprenant) {
        Formation formation = formations.stream().filter(f -> f.getId_formation() == apprenant.getId_formation()).findFirst().orElse(null);
        String formationTitle = formation != null ? formation.getTitre() : "Formation";
        mailingApiService.sendRegistrationEmail(apprenant.getEmail(), apprenant.getPrenom() + " " + apprenant.getNom(), formationTitle);
    }

    private void alert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Gestion Apprenants");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
