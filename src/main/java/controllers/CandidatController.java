package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Candidat;
import services.CandidatService;
import services.RecruitmentWorkflowService;
import utils.AuthContext;
import utils.NavigationState;

import java.sql.SQLException;

public class CandidatController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTriChamp, comboTriOrdre;
    @FXML private TableView<Candidat> tableCandidat;
    @FXML private TableColumn<Candidat, String> colNom, colPrenom, colAdresse, colEmail, colCv, colStatut;
    @FXML private TableColumn<Candidat, Integer> colCIN, colTel;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnResetFiltre;
    @FXML private Button btnTagTous, btnTagNouveau, btnTagPremier, btnTagDeuxieme, btnTagOffre, btnTagAcceptee, btnTagRejetee;
    @FXML private Label lblCount;
    @FXML private Pagination pagination;

    private final CandidatService service = new CandidatService();
    private final RecruitmentWorkflowService workflowService = new RecruitmentWorkflowService();
    private final ObservableList<Candidat> master = FXCollections.observableArrayList();
    private FilteredList<Candidat> filtered;
    private static final int ROWS = 8;

    @FXML
    public void initialize() {
        comboTriChamp.getItems().addAll("Nom", "Prénom", "CIN", "Téléphone", "Adresse", "Email", "CV", "Statut");
        comboTriOrdre.getItems().addAll("Croissant", "Décroissant");
        comboTriOrdre.setValue("Croissant");

        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCIN.setCellValueFactory(new PropertyValueFactory<>("CIN"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCv.setCellValueFactory(new PropertyValueFactory<>("cv"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        loadTable();
        filtered = new FilteredList<>(master, c -> true);

        txtRecherche.textProperty().addListener((a, b, q) -> { applySearch(q); updatePagination(); });
        comboTriChamp.setOnAction(e -> applySort());
        comboTriOrdre.setOnAction(e -> applySort());

        btnTagTous.setOnAction(e -> filterByStatus(null));
        btnTagNouveau.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_NOUVEAU));
        btnTagPremier.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_PREMIER_ENTRETIEN));
        btnTagDeuxieme.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_DEUXIEME_ENTRETIEN));
        btnTagOffre.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_OFFRE_ENVOYEE));
        btnTagAcceptee.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_ACCEPTEE));
        btnTagRejetee.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_REJETEE));

        btnAjouter.setOnAction(e -> {
            NavigationState.clearAll();
            MainController.navigate("ApplicationForm.fxml");
        });
        btnModifier.setOnAction(e -> openSelectedForEdit());
        btnSupprimer.setOnAction(e -> deleteSelected());
        btnResetFiltre.setOnAction(e -> { txtRecherche.clear(); filterByStatus(null); updatePagination(); });

        tableCandidat.setRowFactory(tv -> {
            TableRow<Candidat> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getClickCount() == 1) {
                    NavigationState.clearAll();
                    NavigationState.selectedCandidat = row.getItem();
                    NavigationState.readOnly = !AuthContext.isAdmin();
                    MainController.navigate("ApplicationForm.fxml");
                }
            });
            return row;
        });

        boolean admin = AuthContext.isAdmin();
        btnAjouter.setDisable(!admin);
        btnModifier.setDisable(!admin);
        btnSupprimer.setDisable(!admin);

        updatePagination();
    }

    private void openSelectedForEdit() {
        Candidat selected = tableCandidat.getSelectionModel().getSelectedItem();
        if (selected == null || !AuthContext.isAdmin()) return;
        NavigationState.clearAll();
        NavigationState.selectedCandidat = selected;
        MainController.navigate("ApplicationForm.fxml");
    }

    private void deleteSelected() {
        if (!AuthContext.isAdmin()) return;
        Candidat c = tableCandidat.getSelectionModel().getSelectedItem();
        if (c == null) return;
        try {
            service.supprimer(c.getIdCandidat());
            loadTable();
            updatePagination();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void filterByStatus(String status) {
        filtered.setPredicate(c -> status == null || status.equalsIgnoreCase(c.getStatut()));
        updatePagination();
    }

    private void applySearch(String q) {
        filtered.setPredicate(c -> q == null || q.isBlank()
                || c.getNom().toLowerCase().contains(q.toLowerCase())
                || c.getPrenom().toLowerCase().contains(q.toLowerCase())
                || c.getEmail().toLowerCase().contains(q.toLowerCase())
                || (c.getStatut() != null && c.getStatut().toLowerCase().contains(q.toLowerCase())));
    }

    private void applySort() {
        String champ = comboTriChamp.getValue();
        if (champ == null) return;
        SortedList<Candidat> s = new SortedList<>(filtered);
        switch (champ) {
            case "Nom" -> s.setComparator((a, b) -> a.getNom().compareToIgnoreCase(b.getNom()));
            case "Prénom" -> s.setComparator((a, b) -> a.getPrenom().compareToIgnoreCase(b.getPrenom()));
            case "CIN" -> s.setComparator((a, b) -> Integer.compare(a.getCIN(), b.getCIN()));
            case "Téléphone" -> s.setComparator((a, b) -> Integer.compare(a.getTel(), b.getTel()));
            case "Adresse" -> s.setComparator((a, b) -> a.getAdresse().compareToIgnoreCase(b.getAdresse()));
            case "Email" -> s.setComparator((a, b) -> a.getEmail().compareToIgnoreCase(b.getEmail()));
            case "CV" -> s.setComparator((a, b) -> a.getCv().compareToIgnoreCase(b.getCv()));
            case "Statut" -> s.setComparator((a, b) -> a.getStatut().compareToIgnoreCase(b.getStatut()));
        }
        if ("Décroissant".equals(comboTriOrdre.getValue()) && s.getComparator() != null) s.setComparator(s.getComparator().reversed());
        tableCandidat.setItems(s);
    }

    private void loadTable() {
        try {
            master.setAll(service.recuperer());
            for (Candidat candidat : master) {
                candidat.setStatut(workflowService.getCandidatePhase(candidat.getIdCandidat()));
            }
            lblCount.setText(String.valueOf(master.size()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void updatePagination() {
        int total = filtered.size();
        int pages = (int) Math.ceil((double) total / ROWS);
        pagination.setPageCount(pages == 0 ? 1 : pages);
        pagination.setPageFactory(this::createPage);
    }

    private TableView<Candidat> createPage(int idx) {
        int from = idx * ROWS;
        int to = Math.min(from + ROWS, filtered.size());
        tableCandidat.setItems(FXCollections.observableArrayList(filtered.subList(from, to)));
        return tableCandidat;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
