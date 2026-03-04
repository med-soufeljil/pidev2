package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.DemandeConges;
import models.DemandeTeletravail;
import models.StatutDemande;
import models.StatutTeletravail;
import services.DemandeCongeService;
import services.DemandeTeletravailService;
import services.EmployeeScoreService;
import utils.Navigation;
import utils.Session;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeDashboardController {

    // ─── Header ──────────────────────────────────────────────────────────────
    @FXML
    private Label lblBienvenue;
    @FXML
    private Label lblScore;

    // ─── Statistics ───────────────────────────────────────────────────────────
    @FXML
    private Label lblStatCTotal;
    @FXML
    private Label lblStatCAttente;
    @FXML
    private Label lblStatCApprouve;
    @FXML
    private Label lblStatCRefuse;
    @FXML
    private Label lblStatTTotal;
    @FXML
    private Label lblStatTAttente;
    @FXML
    private Label lblStatTApprouve;
    @FXML
    private Label lblStatTRefuse;

    // ─── Congés search/sort ───────────────────────────────────────────────────
    @FXML
    private TextField tfSearchConge;
    @FXML
    private ComboBox<String> cbSortConge;

    // ─── Congés table ─────────────────────────────────────────────────────────
    @FXML
    private TableView<DemandeConges> tvConges;
    @FXML
    private TableColumn<DemandeConges, String> colCType;
    @FXML
    private TableColumn<DemandeConges, LocalDate> colCDebut;
    @FXML
    private TableColumn<DemandeConges, LocalDate> colCFin;
    @FXML
    private TableColumn<DemandeConges, String> colCStatut;
    @FXML
    private TableColumn<DemandeConges, LocalDateTime> colCDate;
    @FXML
    private TableColumn<DemandeConges, String> colCCommentaire;

    // ─── Télétravail search/sort ──────────────────────────────────────────────
    @FXML
    private TextField tfSearchTT;
    @FXML
    private ComboBox<String> cbSortTT;

    // ─── Télétravail table ────────────────────────────────────────────────────
    @FXML
    private TableView<DemandeTeletravail> tvTeletravail;
    @FXML
    private TableColumn<DemandeTeletravail, LocalDate> colTDebut;
    @FXML
    private TableColumn<DemandeTeletravail, LocalDate> colTFin;
    @FXML
    private TableColumn<DemandeTeletravail, String> colTStatut;
    @FXML
    private TableColumn<DemandeTeletravail, LocalDateTime> colTDate;
    @FXML
    private TableColumn<DemandeTeletravail, String> colTCommentaire;

    private final DemandeCongeService congeService = new DemandeCongeService();
    private final DemandeTeletravailService ttService = new DemandeTeletravailService();

    // raw data (full lists, pre-filter)
    private List<DemandeConges> allConges;
    private List<DemandeTeletravail> allTT;

    @FXML
    public void initialize() {
        lblBienvenue.setText("Bienvenue, " + Session.getCurrent().getNomComplet()
                + "  (" + Session.getCurrent().getRole() + ")");

        // ── Employee score badge ───────────────────────────────────────────────
        try {
            int empId = Session.getCurrent().getId();
            EmployeeScoreService scoreService = new EmployeeScoreService();
            String label = scoreService.getScoreLabel(empId, false);
            lblScore.setText("🎯 " + label);
            // Color the badge based on level
            String color;
            if (label.contains("Excellent"))
                color = "#a5d6a7";
            else if (label.contains("Bon"))
                color = "#80deea";
            else if (label.contains("Moyen"))
                color = "#ffcc80";
            else
                color = "#ef9a9a";
            lblScore.setStyle(
                    "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + color + ";" +
                            "-fx-background-color:#16213e; -fx-background-radius:20; -fx-padding:4 12;");
        } catch (Exception ex) {
            lblScore.setText("");
        }

        // ── Sort options ──────────────────────────────────────────────────────
        cbSortConge.setItems(FXCollections.observableArrayList(
                "Date (↓ récent)", "Date (↑ ancien)", "Type", "Statut"));
        cbSortConge.getSelectionModel().selectFirst();

        cbSortTT.setItems(FXCollections.observableArrayList(
                "Date (↓ récent)", "Date (↑ ancien)", "Statut"));
        cbSortTT.getSelectionModel().selectFirst();

        // ── Congés columns ────────────────────────────────────────────────────
        colCType.setCellValueFactory(new PropertyValueFactory<>("typeConge"));
        colCDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colCFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colCStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colCDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colCCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaireDecision"));

        // ── Télétravail columns ───────────────────────────────────────────────
        colTDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colTFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colTStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colTDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colTCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaireDecision"));

        // Live search listeners
        tfSearchConge.textProperty().addListener((obs, o, n) -> applyFilterConge());
        tfSearchTT.textProperty().addListener((obs, o, n) -> applyFilterTT());

        refreshTables();
    }

    // ─── Refresh ─────────────────────────────────────────────────────────────

    private void refreshTables() {
        int id = Session.getCurrent().getId();
        try {
            allConges = congeService.recupererParEmploye(id);
            updateStatsConge(allConges);
            applyFilterConge();
        } catch (SQLException e) {
            System.err.println("Erreur chargement congés: " + e.getMessage());
        }
        try {
            allTT = ttService.recupererParEmploye(id);
            updateStatsTT(allTT);
            applyFilterTT();
        } catch (SQLException e) {
            System.err.println("Erreur chargement télétravail: " + e.getMessage());
        }
    }

    // ─── Statistics helpers ───────────────────────────────────────────────────

    private void updateStatsConge(List<DemandeConges> list) {
        long total = list.size();
        long attente = list.stream().filter(d -> d.getStatut() == StatutDemande.EN_ATTENTE).count();
        long approuve = list.stream().filter(d -> d.getStatut() == StatutDemande.APPROUVE).count();
        long refuse = list.stream().filter(d -> d.getStatut() == StatutDemande.REFUSE).count();
        lblStatCTotal.setText("Congés total: " + total);
        lblStatCAttente.setText("En attente: " + attente);
        lblStatCApprouve.setText("Approuvés: " + approuve);
        lblStatCRefuse.setText("Refusés: " + refuse);
    }

    private void updateStatsTT(List<DemandeTeletravail> list) {
        long total = list.size();
        long attente = list.stream().filter(d -> d.getStatut() == StatutTeletravail.EN_ATTENTE).count();
        long approuve = list.stream().filter(d -> d.getStatut() == StatutTeletravail.APPROUVE).count();
        long refuse = list.stream().filter(d -> d.getStatut() == StatutTeletravail.REFUSE).count();
        lblStatTTotal.setText("Télétravail total: " + total);
        lblStatTAttente.setText("En attente: " + attente);
        lblStatTApprouve.setText("Approuvés: " + approuve);
        lblStatTRefuse.setText("Refusés: " + refuse);
    }

    // ─── Filter + Sort ────────────────────────────────────────────────────────

    private void applyFilterConge() {
        if (allConges == null)
            return;
        String kw = tfSearchConge.getText().trim().toLowerCase();
        List<DemandeConges> filtered = allConges.stream()
                .filter(d -> kw.isEmpty()
                        || d.getTypeConge().name().toLowerCase().contains(kw)
                        || d.getStatut().name().toLowerCase().contains(kw))
                .collect(Collectors.toList());
        sortConge(filtered);
        tvConges.setItems(FXCollections.observableArrayList(filtered));
    }

    private void applyFilterTT() {
        if (allTT == null)
            return;
        String kw = tfSearchTT.getText().trim().toLowerCase();
        List<DemandeTeletravail> filtered = allTT.stream()
                .filter(d -> kw.isEmpty()
                        || d.getStatut().name().toLowerCase().contains(kw))
                .collect(Collectors.toList());
        sortTT(filtered);
        tvTeletravail.setItems(FXCollections.observableArrayList(filtered));
    }

    private void sortConge(List<DemandeConges> list) {
        String sel = cbSortConge.getValue();
        if (sel == null)
            return;
        if (sel.startsWith("Date (↓")) {
            list.sort(Comparator.comparing(DemandeConges::getDateDemande,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        } else if (sel.startsWith("Date (↑")) {
            list.sort(Comparator.comparing(DemandeConges::getDateDemande,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else if (sel.equals("Type")) {
            list.sort(Comparator.comparing(d -> d.getTypeConge().name()));
        } else if (sel.equals("Statut")) {
            list.sort(Comparator.comparing(d -> d.getStatut().name()));
        }
    }

    private void sortTT(List<DemandeTeletravail> list) {
        String sel = cbSortTT.getValue();
        if (sel == null)
            return;
        if (sel.startsWith("Date (↓")) {
            list.sort(Comparator.comparing(DemandeTeletravail::getDateDemande,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        } else if (sel.startsWith("Date (↑")) {
            list.sort(Comparator.comparing(DemandeTeletravail::getDateDemande,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else if (sel.equals("Statut")) {
            list.sort(Comparator.comparing(d -> d.getStatut().name()));
        }
    }

    // ─── FXML handlers ───────────────────────────────────────────────────────

    @FXML
    private void onSortConge() {
        applyFilterConge();
    }

    @FXML
    private void onSortTT() {
        applyFilterTT();
    }

    @FXML
    private void goToConge(javafx.event.ActionEvent e) {
        Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
        Navigation.switchTo(stage, "DemandeCongeView.fxml");
    }

    @FXML
    private void goToTeletravail(javafx.event.ActionEvent e) {
        Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
        Navigation.switchTo(stage, "DemandeTeletravailView.fxml");
    }

    @FXML
    private void onRefresh(javafx.event.ActionEvent e) {
        refreshTables();
    }

    @FXML
    private void onLogout(javafx.event.ActionEvent e) {
        Session.clear();
        Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
        Navigation.switchTo(stage, "LoginView.fxml");
    }
}
