package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import models.DemandeTeletravail;
import services.DemandeTeletravailService;
import utils.AuthContext;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public class TeletravailController {
    @FXML private TextField txtSearchTt;
    @FXML private TableView<DemandeTeletravail> tableTt;
    @FXML private TableColumn<DemandeTeletravail, Integer> colTtEmployeId, colTtJours;
    @FXML private TableColumn<DemandeTeletravail, String> colTtEmploye, colTtMotif, colTtStatut, colTtMois;
    @FXML private TableColumn<DemandeTeletravail, LocalDate> colTtDebut, colTtFin;
    @FXML private TableColumn<DemandeTeletravail, Void> colTtAction;
    @FXML private Button btnAddTt, btnEditTt, btnDeleteTt, btnApproveTt, btnRejectTt;

    private final DemandeTeletravailService ttService = new DemandeTeletravailService();
    private final ObservableList<DemandeTeletravail> ttMaster = FXCollections.observableArrayList();
    private FilteredList<DemandeTeletravail> ttFiltered;

    @FXML
    public void initialize() {
        configureTable();
        configureActions();
        applyPermissions();
        refreshTable();
    }

    private void configureTable() {
        colTtEmployeId.setCellValueFactory(new PropertyValueFactory<>("idEmploye"));
        colTtEmploye.setCellValueFactory(new PropertyValueFactory<>("employeNom"));
        colTtDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colTtFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colTtJours.setCellValueFactory(new PropertyValueFactory<>("nbJours"));
        colTtMois.setCellValueFactory(new PropertyValueFactory<>("moisConcerne"));
        colTtMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colTtStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        ttFiltered = new FilteredList<>(ttMaster, d -> true);
        tableTt.setItems(ttFiltered);
        txtSearchTt.textProperty().addListener((obs, old, value) -> filterTeletravail(value));
        configureActionColumn();
    }


    private void configureActionColumn() {
        colTtAction.setCellFactory(col -> new TableCell<>() {
            private final Button approve = new Button("Approuver");
            private final Button reject = new Button("Refuser");
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(6, approve, reject);
            {
                approve.getStyleClass().add("apply-btn");
                reject.getStyleClass().add("danger-btn");
                approve.setOnAction(e -> decideTeletravail(getTableView().getItems().get(getIndex()), "APPROUVE"));
                reject.setOnAction(e -> decideTeletravail(getTableView().getItems().get(getIndex()), "REFUSE"));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                DemandeTeletravail demande = empty ? null : getTableView().getItems().get(getIndex());
                setGraphic(empty || !AuthContext.isAdmin() || demande == null || !"EN_ATTENTE".equals(demande.getStatut()) ? null : box);
            }
        });
    }

    private void configureActions() {
        btnAddTt.setOnAction(e -> showTeletravailDialog(null));
        btnEditTt.setOnAction(e -> {
            DemandeTeletravail selected = tableTt.getSelectionModel().getSelectedItem();
            if (selected != null) showTeletravailDialog(selected);
        });
        btnDeleteTt.setOnAction(e -> deleteTeletravail());
        btnApproveTt.setOnAction(e -> decideTeletravail("APPROUVE"));
        btnRejectTt.setOnAction(e -> decideTeletravail("REFUSE"));
    }

    private void applyPermissions() {
        boolean admin = AuthContext.isAdmin();
        btnApproveTt.setVisible(false);
        btnApproveTt.setManaged(false);
        btnRejectTt.setVisible(false);
        btnRejectTt.setManaged(false);
        colTtAction.setVisible(admin);
    }

    private void refreshTable() {
        try {
            ttMaster.setAll(AuthContext.isAdmin() ? ttService.recuperer() : ttService.recupererVisibles());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Chargement", e.getMessage());
        }
    }

    private void filterTeletravail(String query) {
        String q = query == null ? "" : query.toLowerCase().trim();
        ttFiltered.setPredicate(d -> q.isEmpty()
                || String.valueOf(d.getIdEmploye()).contains(q)
                || safe(d.getEmployeNom()).contains(q)
                || safe(d.getMoisConcerne()).contains(q)
                || safe(d.getMotif()).contains(q)
                || safe(d.getStatut()).contains(q));
    }

    private void showTeletravailDialog(DemandeTeletravail editing) {
        Dialog<DemandeTeletravail> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "Nouvelle demande télétravail" : "Modifier demande télétravail");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        TextField idEmploye = new TextField(editing == null ? String.valueOf(AuthContext.getCurrentUserId()) : String.valueOf(editing.getIdEmploye()));
        idEmploye.setDisable(!AuthContext.isAdmin());
        DatePicker debut = new DatePicker(editing == null ? LocalDate.now() : editing.getDateDebut());
        DatePicker fin = new DatePicker(editing == null ? LocalDate.now() : editing.getDateFin());
        TextField nbJours = new TextField(editing == null ? "1" : String.valueOf(editing.getNbJours()));
        TextField motif = new TextField(editing == null ? "" : editing.getMotif());
        ComboBox<String> statut = new ComboBox<>(FXCollections.observableArrayList("EN_ATTENTE", "APPROUVE", "REFUSE"));
        statut.setValue(editing == null ? "EN_ATTENTE" : editing.getStatut());
        statut.setDisable(!AuthContext.isAdmin());
        TextField mois = new TextField(editing == null ? YearMonth.now().toString() : editing.getMoisConcerne());
        TextField commentaire = new TextField(editing == null ? "" : editing.getCommentaireDecision());

        debut.valueProperty().addListener((obs, old, value) -> mois.setText(value == null ? "" : YearMonth.from(value).toString()));
        fin.valueProperty().addListener((obs, old, value) -> nbJours.setText(String.valueOf(calculateDays(debut.getValue(), value))));

        dialog.getDialogPane().setContent(formGrid(
                new Label("Employé ID"), idEmploye,
                new Label("Début"), debut,
                new Label("Fin"), fin,
                new Label("Nombre jours"), nbJours,
                new Label("Mois concerné"), mois,
                new Label("Motif"), motif,
                new Label("Statut"), statut,
                new Label("Commentaire décision"), commentaire));

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            DemandeTeletravail d = editing == null ? new DemandeTeletravail() : editing;
            d.setIdEmploye(Integer.parseInt(idEmploye.getText().trim()));
            d.setDateDebut(debut.getValue());
            d.setDateFin(fin.getValue());
            d.setNbJours(Integer.parseInt(nbJours.getText().trim()));
            d.setMoisConcerne(mois.getText().trim());
            d.setMotif(motif.getText());
            d.setStatut(statut.getValue());
            d.setCommentaireDecision(commentaire.getText());
            return d;
        });

        dialog.showAndWait().ifPresent(d -> {
            try {
                validateDates(d.getDateDebut(), d.getDateFin());
                if (d.getNbJours() <= 0) throw new IllegalArgumentException("Le nombre de jours doit être positif.");
                if (d.getId() == 0) ttService.ajouter(d); else ttService.modifier(d);
                refreshTable();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Télétravail", ex.getMessage());
            }
        });
    }

    private void deleteTeletravail() {
        DemandeTeletravail selected = tableTt.getSelectionModel().getSelectedItem();
        if (selected == null || !confirm("Supprimer cette demande télétravail ?")) return;
        try {
            ttService.supprimer(selected.getId());
            refreshTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Suppression", e.getMessage());
        }
    }

    private void decideTeletravail(String statut) {
        decideTeletravail(tableTt.getSelectionModel().getSelectedItem(), statut);
    }

    private void decideTeletravail(DemandeTeletravail selected, String statut) {
        if (selected == null) return;
        try {
            ttService.changerStatut(selected.getId(), statut, AuthContext.getCurrentUserId(), askCommentaire(statut));
            refreshTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Décision", e.getMessage());
        }
    }

    private GridPane formGrid(Object... nodes) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("form-grid");
        for (int i = 0, row = 0; i < nodes.length; i += 2, row++) {
            grid.add((javafx.scene.Node) nodes[i], 0, row);
            grid.add((javafx.scene.Node) nodes[i + 1], 1, row);
        }
        return grid;
    }

    private String askCommentaire(String statut) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Décision " + statut);
        dialog.setHeaderText("Ajouter un commentaire de décision");
        dialog.setContentText("Commentaire:");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return dialog.showAndWait().orElse("");
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.CANCEL, ButtonType.OK);
        alert.setHeaderText("Confirmation");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void validateDates(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) throw new IllegalArgumentException("Les dates sont obligatoires.");
        if (fin.isBefore(debut)) throw new IllegalArgumentException("La date fin doit être après la date début.");
    }

    private long calculateDays(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null || fin.isBefore(debut)) return 1;
        return ChronoUnit.DAYS.between(debut, fin) + 1;
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
