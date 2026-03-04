package controllers;

import javafx.collections.FXCollections;
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
import utils.Navigation;
import utils.Session;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminDashboardController {

    // ─── Header ──────────────────────────────────────────────────────────────
    @FXML
    private Label lblBienvenue;

    // ─── Global statistics ────────────────────────────────────────────────────
    @FXML
    private Label lblAdminStatCTotal;
    @FXML
    private Label lblAdminStatCAttente;
    @FXML
    private Label lblAdminStatCApprouve;
    @FXML
    private Label lblAdminStatCRefuse;
    @FXML
    private Label lblAdminStatTTotal;
    @FXML
    private Label lblAdminStatTAttente;
    @FXML
    private Label lblAdminStatTApprouve;
    @FXML
    private Label lblAdminStatTRefuse;

    // ─── Congés search/sort ───────────────────────────────────────────────────
    @FXML
    private TextField tfSearchAdminConge;
    @FXML
    private ComboBox<String> cbSortAdminConge;

    // ─── Congés table ─────────────────────────────────────────────────────────
    @FXML
    private TableView<DemandeConges> tvConges;
    @FXML
    private TableColumn<DemandeConges, String> colCNom;
    @FXML
    private TableColumn<DemandeConges, String> colCPrenom;
    @FXML
    private TableColumn<DemandeConges, String> colCType;
    @FXML
    private TableColumn<DemandeConges, LocalDate> colCDebut;
    @FXML
    private TableColumn<DemandeConges, LocalDate> colCFin;
    @FXML
    private TableColumn<DemandeConges, String> colCMotif;
    @FXML
    private TableColumn<DemandeConges, String> colCStatut;
    @FXML
    private TableColumn<DemandeConges, String> colCPriority;
    @FXML
    private TableColumn<DemandeConges, String> colCScore;
    @FXML
    private TableColumn<DemandeConges, String> colCHoliday;

    // ─── Congé action ─────────────────────────────────────────────────────────
    @FXML
    private TextField tfCommentaireConge;

    // ─── Télétravail search/sort ──────────────────────────────────────────────
    @FXML
    private TextField tfSearchAdminTT;
    @FXML
    private ComboBox<String> cbSortAdminTT;

    // ─── Télétravail table ────────────────────────────────────────────────────
    @FXML
    private TableView<DemandeTeletravail> tvTeletravail;
    @FXML
    private TableColumn<DemandeTeletravail, String> colTNom;
    @FXML
    private TableColumn<DemandeTeletravail, String> colTPrenom;
    @FXML
    private TableColumn<DemandeTeletravail, LocalDate> colTDebut;
    @FXML
    private TableColumn<DemandeTeletravail, LocalDate> colTFin;
    @FXML
    private TableColumn<DemandeTeletravail, String> colTMotif;
    @FXML
    private TableColumn<DemandeTeletravail, String> colTStatut;
    @FXML
    private TableColumn<DemandeTeletravail, String> colTAlerte;
    @FXML
    private TableColumn<DemandeTeletravail, String> colTScore;
    @FXML
    private TableColumn<DemandeTeletravail, String> colTWeather;

    // ─── Télétravail action ────────────────────────────────────────────────────
    @FXML
    private TextField tfCommentaireTT;

    private final DemandeCongeService congeService = new DemandeCongeService();
    private final DemandeTeletravailService ttService = new DemandeTeletravailService();

    // raw "EN_ATTENTE" lists as loaded from DB
    private List<DemandeConges> pendingConges;
    private List<DemandeTeletravail> pendingTT;

    @FXML
    public void initialize() {
        lblBienvenue.setText("Admin : " + Session.getCurrent().getNomComplet());

        // ── Sort combos ───────────────────────────────────────────────────────
        cbSortAdminConge.setItems(FXCollections.observableArrayList(
                "Date (↓ récent)", "Date (↑ ancien)", "Nom", "Type"));
        cbSortAdminConge.getSelectionModel().selectFirst();

        cbSortAdminTT.setItems(FXCollections.observableArrayList(
                "Date (↓ récent)", "Date (↑ ancien)", "Nom"));
        cbSortAdminTT.getSelectionModel().selectFirst();

        // ── Congé columns ──────────────────────────────────────────────────────
        colCNom.setCellValueFactory(new PropertyValueFactory<>("nomEmploye"));
        colCPrenom.setCellValueFactory(new PropertyValueFactory<>("prenomEmploye"));
        colCType.setCellValueFactory(new PropertyValueFactory<>("typeConge"));
        colCDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colCFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colCMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colCStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // ── Télétravail columns ────────────────────────────────────────────────
        colTNom.setCellValueFactory(new PropertyValueFactory<>("nomEmploye"));
        colTPrenom.setCellValueFactory(new PropertyValueFactory<>("prenomEmploye"));
        colTDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colTFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colTMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colTStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // ── Alert column with orange highlight ─────────────────────────────────
        colTAlerte.setCellValueFactory(new PropertyValueFactory<>("abuseSummary"));
        colTAlerte.setCellFactory(col -> new TableCell<DemandeTeletravail, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setWrapText(true);
                    setStyle(
                            "-fx-background-color:#7a3500;-fx-text-fill:#ffcc80;-fx-font-weight:bold;-fx-padding:4 6;");
                }
            }
        });

        // ── Priority column (green / red) ─────────────────────────────────────
        colCPriority.setCellValueFactory(new PropertyValueFactory<>("priorityNote"));
        colCPriority.setCellFactory(col -> new TableCell<DemandeConges, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setWrapText(true);
                    boolean haute = item.contains("haute");
                    setStyle(haute
                            ? "-fx-background-color:#0a3d00;-fx-text-fill:#a5d6a7;-fx-font-weight:bold;-fx-padding:4 6;"
                            : "-fx-background-color:#3d0000;-fx-text-fill:#ef9a9a;-fx-font-weight:bold;-fx-padding:4 6;");
                }
            }
        });

        // ── Score column — congés ─────────────────────────────────────────────
        colCScore.setCellValueFactory(new PropertyValueFactory<>("scoreLabel"));
        colCScore.setCellFactory(col -> scoreCell());

        // ── Score column — télétravail ────────────────────────────────────────
        colTScore.setCellValueFactory(new PropertyValueFactory<>("scoreLabel"));
        colTScore.setCellFactory(col -> scoreCellTT());

        // ── Holiday column (red if férié) ─────────────────────────────────────
        colCHoliday.setCellValueFactory(new PropertyValueFactory<>("holidayNote"));
        colCHoliday.setCellFactory(col -> new TableCell<DemandeConges, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setWrapText(true);
                    setStyle("-fx-background-color:#3d0000;-fx-text-fill:#ff8a80;" +
                            "-fx-font-weight:bold;-fx-padding:4 6;");
                }
            }
        });

        // ── Weather column (blue for recommended, grey otherwise) ─────────────
        colTWeather.setCellValueFactory(new PropertyValueFactory<>("weatherNote"));
        colTWeather.setCellFactory(col -> new TableCell<DemandeTeletravail, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setWrapText(true);
                    boolean recommended = item.contains("conseillé");
                    setStyle(recommended
                            ? "-fx-background-color:#003d4d;-fx-text-fill:#80deea;-fx-font-weight:bold;-fx-padding:4 6;"
                            : "-fx-background-color:#1a1a1a;-fx-text-fill:#b0bec5;-fx-padding:4 6;");
                }
            }
        });

        // Live search listeners
        tfSearchAdminConge.textProperty().addListener((obs, o, n) -> applyFilterConge());
        tfSearchAdminTT.textProperty().addListener((obs, o, n) -> applyFilterTT());

        refreshAll();
    }

    // ─── Refresh ─────────────────────────────────────────────────────────────

    private void refreshAll() {
        // ── Statistics (all demands) ───────────────────────────────────────────
        try {
            List<DemandeConges> tous = congeService.recupererTous();
            updateStatsConge(tous);
        } catch (SQLException e) {
            System.err.println("Erreur stats congés: " + e.getMessage());
        }
        try {
            List<DemandeTeletravail> tous = ttService.recupererTous();
            updateStatsTT(tous);
        } catch (SQLException e) {
            System.err.println("Erreur stats télétravail: " + e.getMessage());
        }

        // ── Pending lists (displayed in tables) ────────────────────────────────
        try {
            pendingConges = congeService.recupererEnAttente();
            applyFilterConge();
        } catch (SQLException e) {
            System.err.println("Erreur chargement congés en attente: " + e.getMessage());
        }
        try {
            pendingTT = ttService.recupererEnAttente();
            applyFilterTT();
        } catch (SQLException e) {
            System.err.println("Erreur chargement télétravail en attente: " + e.getMessage());
        }
    }

    // ─── Statistics helpers ───────────────────────────────────────────────────

    private void updateStatsConge(List<DemandeConges> list) {
        long total = list.size();
        long attente = list.stream().filter(d -> d.getStatut() == StatutDemande.EN_ATTENTE).count();
        long approuve = list.stream().filter(d -> d.getStatut() == StatutDemande.APPROUVE).count();
        long refuse = list.stream().filter(d -> d.getStatut() == StatutDemande.REFUSE).count();
        lblAdminStatCTotal.setText("Total: " + total);
        lblAdminStatCAttente.setText("En attente: " + attente);
        lblAdminStatCApprouve.setText("Approuvés: " + approuve);
        lblAdminStatCRefuse.setText("Refusés: " + refuse);
    }

    private void updateStatsTT(List<DemandeTeletravail> list) {
        long total = list.size();
        long attente = list.stream().filter(d -> d.getStatut() == StatutTeletravail.EN_ATTENTE).count();
        long approuve = list.stream().filter(d -> d.getStatut() == StatutTeletravail.APPROUVE).count();
        long refuse = list.stream().filter(d -> d.getStatut() == StatutTeletravail.REFUSE).count();
        lblAdminStatTTotal.setText("Total: " + total);
        lblAdminStatTAttente.setText("En attente: " + attente);
        lblAdminStatTApprouve.setText("Approuvés: " + approuve);
        lblAdminStatTRefuse.setText("Refusés: " + refuse);
    }

    // ─── Filter + Sort ────────────────────────────────────────────────────────

    private void applyFilterConge() {
        if (pendingConges == null)
            return;
        String kw = tfSearchAdminConge.getText().trim().toLowerCase();
        List<DemandeConges> filtered = pendingConges.stream()
                .filter(d -> kw.isEmpty()
                        || (d.getNomEmploye() != null && d.getNomEmploye().toLowerCase().contains(kw))
                        || (d.getPrenomEmploye() != null && d.getPrenomEmploye().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
        sortConge(filtered);
        tvConges.setItems(FXCollections.observableArrayList(filtered));
    }

    private void applyFilterTT() {
        if (pendingTT == null)
            return;
        String kw = tfSearchAdminTT.getText().trim().toLowerCase();
        List<DemandeTeletravail> filtered = pendingTT.stream()
                .filter(d -> kw.isEmpty()
                        || (d.getNomEmploye() != null && d.getNomEmploye().toLowerCase().contains(kw))
                        || (d.getPrenomEmploye() != null && d.getPrenomEmploye().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
        sortTT(filtered);
        tvTeletravail.setItems(FXCollections.observableArrayList(filtered));
    }

    private void sortConge(List<DemandeConges> list) {
        String sel = cbSortAdminConge.getValue();
        if (sel == null)
            return;
        if (sel.startsWith("Date (↓")) {
            list.sort(Comparator.comparing(DemandeConges::getDateDemande,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        } else if (sel.startsWith("Date (↑")) {
            list.sort(Comparator.comparing(DemandeConges::getDateDemande,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else if (sel.equals("Nom")) {
            list.sort(Comparator.comparing(d -> d.getNomEmploye() == null ? "" : d.getNomEmploye()));
        } else if (sel.equals("Type")) {
            list.sort(Comparator.comparing(d -> d.getTypeConge().name()));
        }
    }

    private void sortTT(List<DemandeTeletravail> list) {
        String sel = cbSortAdminTT.getValue();
        if (sel == null)
            return;
        if (sel.startsWith("Date (↓")) {
            list.sort(Comparator.comparing(DemandeTeletravail::getDateDemande,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        } else if (sel.startsWith("Date (↑")) {
            list.sort(Comparator.comparing(DemandeTeletravail::getDateDemande,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else if (sel.equals("Nom")) {
            list.sort(Comparator.comparing(d -> d.getNomEmploye() == null ? "" : d.getNomEmploye()));
        }
    }

    // ── Congé actions ─────────────────────────────────────────────────────────

    @FXML
    private void onApprouverConge() {
        DemandeConges selected = tvConges.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez une demande de congé.");
            return;
        }
        try {
            congeService.mettreAJourStatut(selected.getId(), StatutDemande.APPROUVE,
                    Session.getCurrent().getId(), tfCommentaireConge.getText().trim());
            refreshAll();
            tfCommentaireConge.clear();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onRefuserConge() {
        DemandeConges selected = tvConges.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez une demande de congé.");
            return;
        }
        try {
            congeService.mettreAJourStatut(selected.getId(), StatutDemande.REFUSE,
                    Session.getCurrent().getId(), tfCommentaireConge.getText().trim());
            refreshAll();
            tfCommentaireConge.clear();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage());
        }
    }

    // ── Télétravail actions ────────────────────────────────────────────────────

    @FXML
    private void onApprouverTT() {
        DemandeTeletravail selected = tvTeletravail.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez une demande de télétravail.");
            return;
        }
        try {
            ttService.mettreAJourStatut(selected.getId(), StatutTeletravail.APPROUVE,
                    Session.getCurrent().getId(), tfCommentaireTT.getText().trim());
            refreshAll();
            tfCommentaireTT.clear();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onRefuserTT() {
        DemandeTeletravail selected = tvTeletravail.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez une demande de télétravail.");
            return;
        }
        try {
            ttService.mettreAJourStatut(selected.getId(), StatutTeletravail.REFUSE,
                    Session.getCurrent().getId(), tfCommentaireTT.getText().trim());
            refreshAll();
            tfCommentaireTT.clear();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage());
        }
    }

    // ── Sort button handlers ───────────────────────────────────────────────────

    @FXML
    private void onSortAdminConge() {
        applyFilterConge();
    }

    @FXML
    private void onSortAdminTT() {
        applyFilterTT();
    }

    @FXML
    private void onRefresh(javafx.event.ActionEvent e) {
        refreshAll();
    }

    @FXML
    private void onLogout(javafx.event.ActionEvent e) {
        Session.clear();
        Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
        Navigation.switchTo(stage, "LoginView.fxml");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    /** Returns a colored TableCell factory for score labels (DemandeConges). */
    private TableCell<DemandeConges, String> scoreCell() {
        return new TableCell<DemandeConges, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                applyScoreStyle(this, item, empty);
            }
        };
    }

    /**
     * Returns a colored TableCell factory for score labels (DemandeTeletravail).
     */
    private TableCell<DemandeTeletravail, String> scoreCellTT() {
        return new TableCell<DemandeTeletravail, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                applyScoreStyle(this, item, empty);
            }
        };
    }

    private void applyScoreStyle(TableCell<?, String> cell, String item, boolean empty) {
        if (empty || item == null || item.isBlank()) {
            cell.setText(null);
            cell.setStyle("");
            return;
        }
        cell.setText(item);
        cell.setWrapText(true);
        String bg, fg;
        if (item.contains("Excellent")) {
            bg = "#0a3d00";
            fg = "#a5d6a7";
        } else if (item.contains("Bon")) {
            bg = "#00303d";
            fg = "#80deea";
        } else if (item.contains("Moyen")) {
            bg = "#3d2000";
            fg = "#ffcc80";
        } else {
            bg = "#3d0000";
            fg = "#ef9a9a";
        }
        cell.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg
                + ";-fx-font-weight:bold;-fx-padding:4 6;");
    }
}
