package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import models.DemandeConge;
import models.DemandeTeletravail;
import services.DemandeCongeService;
import services.DemandeTeletravailService;
import utils.AuthContext;
import utils.NavigationState;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class CongesTeletravailController {
    @FXML private Label lblCongeTotal, lblTtTotal, lblPendingTotal, lblApprovedTotal;
    @FXML private TextField txtSearchConge, txtSearchTt;
    @FXML private TabPane tabPane;
    @FXML private Tab tabDashboard, tabConge, tabTeletravail;

    @FXML private TableView<DemandeConge> tableConge;
    @FXML private TableColumn<DemandeConge, Integer> colCongeEmployeId;
    @FXML private TableColumn<DemandeConge, String> colCongeEmploye, colCongeType, colCongeMotif, colCongeStatut;
    @FXML private TableColumn<DemandeConge, LocalDate> colCongeDebut, colCongeFin;
    @FXML private TableColumn<DemandeConge, Long> colCongeJours;
    @FXML private Button btnAddConge, btnEditConge, btnDeleteConge, btnApproveConge, btnRejectConge;

    @FXML private TableView<DemandeTeletravail> tableTt;
    @FXML private TableColumn<DemandeTeletravail, Integer> colTtEmployeId, colTtJours;
    @FXML private TableColumn<DemandeTeletravail, String> colTtEmploye, colTtMotif, colTtStatut, colTtMois;
    @FXML private TableColumn<DemandeTeletravail, LocalDate> colTtDebut, colTtFin;
    @FXML private Button btnAddTt, btnEditTt, btnDeleteTt, btnApproveTt, btnRejectTt;

    private final DemandeCongeService congeService = new DemandeCongeService();
    private final DemandeTeletravailService ttService = new DemandeTeletravailService();
    private final ObservableList<DemandeConge> congeMaster = FXCollections.observableArrayList();
    private final ObservableList<DemandeTeletravail> ttMaster = FXCollections.observableArrayList();
    private FilteredList<DemandeConge> congeFiltered;
    private FilteredList<DemandeTeletravail> ttFiltered;

    @FXML
    public void initialize() {
        configureCongeTable();
        configureTtTable();
        configureActions();
        applyPermissions();
        refreshAll();
        selectRequestedTab();
    }

    private void configureCongeTable() {
        colCongeEmployeId.setCellValueFactory(new PropertyValueFactory<>("idEmploye"));
        colCongeEmploye.setCellValueFactory(new PropertyValueFactory<>("employeNom"));
        colCongeType.setCellValueFactory(new PropertyValueFactory<>("typeConge"));
        colCongeDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colCongeFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colCongeJours.setCellValueFactory(new PropertyValueFactory<>("nombreJours"));
        colCongeMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colCongeStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        congeFiltered = new FilteredList<>(congeMaster, d -> true);
        tableConge.setItems(congeFiltered);
        txtSearchConge.textProperty().addListener((obs, old, value) -> filterConges(value));
    }

    private void configureTtTable() {
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
    }

    private void configureActions() {
        btnAddConge.setOnAction(e -> showCongeDialog(null));
        btnEditConge.setOnAction(e -> {
            DemandeConge selected = tableConge.getSelectionModel().getSelectedItem();
            if (selected != null) showCongeDialog(selected);
        });
        btnDeleteConge.setOnAction(e -> deleteConge());
        btnApproveConge.setOnAction(e -> decideConge("APPROUVE"));
        btnRejectConge.setOnAction(e -> decideConge("REFUSE"));

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
        btnEditConge.setDisable(!admin);
        btnDeleteConge.setDisable(!admin);
        btnApproveConge.setDisable(!admin);
        btnRejectConge.setDisable(!admin);
        btnEditTt.setDisable(!admin);
        btnDeleteTt.setDisable(!admin);
        btnApproveTt.setDisable(!admin);
        btnRejectTt.setDisable(!admin);
    }

    private void selectRequestedTab() {
        String view = NavigationState.congesTtView;
        if ("CONGE".equals(view)) tabPane.getSelectionModel().select(tabConge);
        else if ("TT".equals(view)) tabPane.getSelectionModel().select(tabTeletravail);
        else tabPane.getSelectionModel().select(tabDashboard);
    }

    private void refreshAll() {
        try {
            congeMaster.setAll(congeService.recuperer());
            ttMaster.setAll(ttService.recuperer());
            updateDashboard();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Chargement", e.getMessage());
        }
    }

    private void updateDashboard() throws SQLException {
        Map<String, Integer> congeStats = congeService.statsByStatut();
        Map<String, Integer> ttStats = ttService.statsByStatut();
        int congeTotal = congeStats.values().stream().mapToInt(Integer::intValue).sum();
        int ttTotal = ttStats.values().stream().mapToInt(Integer::intValue).sum();
        int pending = congeStats.getOrDefault("EN_ATTENTE", 0) + ttStats.getOrDefault("EN_ATTENTE", 0);
        int approved = congeStats.getOrDefault("APPROUVE", 0) + ttStats.getOrDefault("APPROUVE", 0);
        lblCongeTotal.setText(String.valueOf(congeTotal));
        lblTtTotal.setText(String.valueOf(ttTotal));
        lblPendingTotal.setText(String.valueOf(pending));
        lblApprovedTotal.setText(String.valueOf(approved));
    }

    private void filterConges(String query) {
        String q = query == null ? "" : query.toLowerCase().trim();
        congeFiltered.setPredicate(d -> q.isEmpty()
                || String.valueOf(d.getIdEmploye()).contains(q)
                || safe(d.getEmployeNom()).contains(q)
                || safe(d.getTypeConge()).contains(q)
                || safe(d.getMotif()).contains(q)
                || safe(d.getStatut()).contains(q));
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

    private void showCongeDialog(DemandeConge editing) {
        Dialog<DemandeConge> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "Nouvelle demande congé" : "Modifier demande congé");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        TextField idEmploye = new TextField(editing == null ? "" : String.valueOf(editing.getIdEmploye()));
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("CONGES_PAYES", "CONGES_VIE_PROFESSIONNELLE_FAMILIALE", "CONGES_MALADIE", "CONGES_SANS_SOLDE", "AUTRE"));
        type.setValue(editing == null ? "CONGES_PAYES" : editing.getTypeConge());
        DatePicker debut = new DatePicker(editing == null ? LocalDate.now() : editing.getDateDebut());
        DatePicker fin = new DatePicker(editing == null ? LocalDate.now() : editing.getDateFin());
        TextField motif = new TextField(editing == null ? "" : editing.getMotif());
        ComboBox<String> statut = new ComboBox<>(FXCollections.observableArrayList("EN_ATTENTE", "APPROUVE", "REFUSE"));
        statut.setValue(editing == null ? "EN_ATTENTE" : editing.getStatut());
        TextField commentaire = new TextField(editing == null ? "" : editing.getCommentaireDecision());

        dialog.getDialogPane().setContent(formGrid(
                new Label("Employé ID"), idEmploye,
                new Label("Type"), type,
                new Label("Début"), debut,
                new Label("Fin"), fin,
                new Label("Motif"), motif,
                new Label("Statut"), statut,
                new Label("Commentaire décision"), commentaire));

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            DemandeConge d = editing == null ? new DemandeConge() : editing;
            d.setIdEmploye(Integer.parseInt(idEmploye.getText().trim()));
            d.setTypeConge(type.getValue());
            d.setDateDebut(debut.getValue());
            d.setDateFin(fin.getValue());
            d.setMotif(motif.getText());
            d.setStatut(statut.getValue());
            d.setCommentaireDecision(commentaire.getText());
            return d;
        });

        dialog.showAndWait().ifPresent(d -> {
            try {
                validateDates(d.getDateDebut(), d.getDateFin());
                if (d.getId() == 0) congeService.ajouter(d); else congeService.modifier(d);
                refreshAll();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Congés", ex.getMessage());
            }
        });
    }

    private void showTeletravailDialog(DemandeTeletravail editing) {
        Dialog<DemandeTeletravail> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "Nouvelle demande télétravail" : "Modifier demande télétravail");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        TextField idEmploye = new TextField(editing == null ? "" : String.valueOf(editing.getIdEmploye()));
        DatePicker debut = new DatePicker(editing == null ? LocalDate.now() : editing.getDateDebut());
        DatePicker fin = new DatePicker(editing == null ? LocalDate.now() : editing.getDateFin());
        TextField nbJours = new TextField(editing == null ? "1" : String.valueOf(editing.getNbJours()));
        TextField motif = new TextField(editing == null ? "" : editing.getMotif());
        ComboBox<String> statut = new ComboBox<>(FXCollections.observableArrayList("EN_ATTENTE", "APPROUVE", "REFUSE"));
        statut.setValue(editing == null ? "EN_ATTENTE" : editing.getStatut());
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
                refreshAll();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Télétravail", ex.getMessage());
            }
        });
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

    private void deleteConge() {
        DemandeConge selected = tableConge.getSelectionModel().getSelectedItem();
        if (selected == null || !confirm("Supprimer cette demande congé ?")) return;
        try {
            congeService.supprimer(selected.getId());
            refreshAll();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Suppression", e.getMessage());
        }
    }

    private void deleteTeletravail() {
        DemandeTeletravail selected = tableTt.getSelectionModel().getSelectedItem();
        if (selected == null || !confirm("Supprimer cette demande télétravail ?")) return;
        try {
            ttService.supprimer(selected.getId());
            refreshAll();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Suppression", e.getMessage());
        }
    }

    private void decideConge(String statut) {
        DemandeConge selected = tableConge.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            congeService.changerStatut(selected.getId(), statut, 1, askCommentaire(statut));
            refreshAll();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Décision", e.getMessage());
        }
    }

    private void decideTeletravail(String statut) {
        DemandeTeletravail selected = tableTt.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            ttService.changerStatut(selected.getId(), statut, 1, askCommentaire(statut));
            refreshAll();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Décision", e.getMessage());
        }
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
