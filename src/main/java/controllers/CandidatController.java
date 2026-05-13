package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Candidat;
import models.Offre;
import models.Recrutement;
import services.*;
import utils.AuthContext;
import utils.NavigationState;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CandidatController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTriChamp, comboTriOrdre, comboGroupeOffre;
    @FXML private TableView<Candidat> tableCandidat;
    @FXML private TableColumn<Candidat, String> colOffre, colNom, colPrenom, colAdresse, colEmail, colCv, colStatut, colAiAnalyse;
    @FXML private TableColumn<Candidat, Integer> colCIN, colTel, colAiScore;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnResetFiltre, btnTopFit;
    @FXML private Button btnTagTous, btnTagNouveau, btnTagPremier, btnTagDeuxieme, btnTagOffre, btnTagAcceptee, btnTagRejetee;
    @FXML private Label lblCount;
    @FXML private Pagination pagination;

    private final CandidatService service = new CandidatService();
    private final OffreService offreService = new OffreService();
    private final RecrutementService recrutementService = new RecrutementService();
    private final RecruitmentWorkflowService workflowService = new RecruitmentWorkflowService();
    private final ApplicationFitAiService fitAiService = new ApplicationFitAiService();

    private final ObservableList<Candidat> master = FXCollections.observableArrayList();
    private FilteredList<Candidat> filtered;
    private String selectedStatus;
    private Comparator<Candidat> currentComparator;
    private static final int ROWS = 8;

    @FXML
    public void initialize() {
        comboTriChamp.getItems().addAll("Offre", "Nom", "Prénom", "CIN", "Téléphone", "Adresse", "Email", "CV", "Statut", "AI Score");
        comboTriOrdre.getItems().addAll("Croissant", "Décroissant");
        comboTriOrdre.setValue("Croissant");

        colOffre.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCIN.setCellValueFactory(new PropertyValueFactory<>("CIN"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCv.setCellValueFactory(new PropertyValueFactory<>("cv"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colAiScore.setCellValueFactory(new PropertyValueFactory<>("aiScore"));
        colAiAnalyse.setCellValueFactory(new PropertyValueFactory<>("aiAnalyse"));

        loadTable();
        filtered = new FilteredList<>(master, c -> true);

        txtRecherche.textProperty().addListener((a, b, q) -> {
            applyCombinedFilters();
            updatePagination();
        });
        comboTriChamp.setOnAction(e -> { applySort(); updatePagination(); });
        comboTriOrdre.setOnAction(e -> { applySort(); updatePagination(); });
        comboGroupeOffre.setOnAction(e -> {
            applyCombinedFilters();
            updatePagination();
        });

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
        btnTopFit.setOnAction(e -> analyzeTopFitByOffer());
        btnResetFiltre.setOnAction(e -> {
            txtRecherche.clear();
            comboGroupeOffre.setValue("Toutes les offres");
            loadTable();
            refreshOfferGroupingChoices();
            filterByStatus(null);
        });

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
        btnTopFit.setDisable(!admin);

        pagination.currentPageIndexProperty().addListener((obs, oldV, newV) -> createPage(newV.intValue()));

        refreshOfferGroupingChoices();
        applyCombinedFilters();
        applySort();
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
            refreshOfferGroupingChoices();
            applyCombinedFilters();
            updatePagination();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void filterByStatus(String status) {
        selectedStatus = status;
        applyCombinedFilters();
        updatePagination();
    }

    private void applyCombinedFilters() {
        String query = txtRecherche.getText();
        String offerFilter = comboGroupeOffre.getValue();
        filtered.setPredicate(c -> {
            boolean statusOk = selectedStatus == null || selectedStatus.equalsIgnoreCase(c.getStatut());
            boolean offerOk = offerFilter == null || "Toutes les offres".equals(offerFilter)
                    || offerFilter.equalsIgnoreCase(Optional.ofNullable(c.getNomOffre()).orElse(""));
            String q = query == null ? "" : query.trim().toLowerCase();
            boolean searchOk = q.isBlank()
                    || c.getNom().toLowerCase().contains(q)
                    || c.getPrenom().toLowerCase().contains(q)
                    || c.getEmail().toLowerCase().contains(q)
                    || Optional.ofNullable(c.getNomOffre()).orElse("").toLowerCase().contains(q)
                    || Optional.ofNullable(c.getAiAnalyse()).orElse("").toLowerCase().contains(q)
                    || (c.getStatut() != null && c.getStatut().toLowerCase().contains(q));
            return statusOk && offerOk && searchOk;
        });
    }

    private void applySort() {
        String champ = comboTriChamp.getValue();
        currentComparator = null;
        if (champ == null) return;

        switch (champ) {
            case "Offre" -> currentComparator = Comparator.comparing(c -> Optional.ofNullable(c.getNomOffre()).orElse(""), String.CASE_INSENSITIVE_ORDER);
            case "Nom" -> currentComparator = (a, b) -> a.getNom().compareToIgnoreCase(b.getNom());
            case "Prénom" -> currentComparator = (a, b) -> a.getPrenom().compareToIgnoreCase(b.getPrenom());
            case "CIN" -> currentComparator = (a, b) -> Integer.compare(a.getCIN(), b.getCIN());
            case "Téléphone" -> currentComparator = (a, b) -> Integer.compare(a.getTel(), b.getTel());
            case "Adresse" -> currentComparator = (a, b) -> a.getAdresse().compareToIgnoreCase(b.getAdresse());
            case "Email" -> currentComparator = (a, b) -> a.getEmail().compareToIgnoreCase(b.getEmail());
            case "CV" -> currentComparator = (a, b) -> a.getCv().compareToIgnoreCase(b.getCv());
            case "Statut" -> currentComparator = (a, b) -> a.getStatut().compareToIgnoreCase(b.getStatut());
            case "AI Score" -> currentComparator = Comparator.comparingInt(Candidat::getAiScore);
        }

        if (currentComparator != null && "Décroissant".equals(comboTriOrdre.getValue())) {
            currentComparator = currentComparator.reversed();
        }
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

    private void refreshOfferGroupingChoices() {
        List<String> offers = master.stream()
                .map(Candidat::getNomOffre)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("Toutes les offres");
        items.addAll(offers);
        comboGroupeOffre.setItems(items);
        if (comboGroupeOffre.getValue() == null) {
            comboGroupeOffre.setValue("Toutes les offres");
        }
    }

    private void analyzeTopFitByOffer() {
        try {
            List<Candidat> allApplications = service.recuperer();
            for (Candidat candidat : allApplications) {
                candidat.setStatut(workflowService.getCandidatePhase(candidat.getIdCandidat()));
            }

            Map<Integer, Offre> offers = offreService.recuperer().stream().collect(Collectors.toMap(Offre::getIdOffre, o -> o));
            Map<Integer, Recrutement> linkByCandidate = recrutementService.recuperer().stream()
                    .collect(Collectors.toMap(Recrutement::getIdCandidat, r -> r, (a, b) -> a));

            for (Candidat candidat : allApplications) {
                Recrutement link = linkByCandidate.get(candidat.getIdCandidat());
                if (link == null) continue;
                Offre offer = offers.get(link.getIdOffre());
                if (offer == null) continue;

                var result = fitAiService.analyze(candidat, offer);
                candidat.setAiScore(result.score());
                candidat.setAiAnalyse("[" + result.engine() + "] " + result.analysis());
                service.modifier(candidat);
            }

            List<Candidat> ranked = new ArrayList<>(allApplications);
            ranked.sort(Comparator
                    .comparing((Candidat c) -> Optional.ofNullable(c.getNomOffre()).orElse(""), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Candidat::getAiScore, Comparator.reverseOrder()));

            Map<String, List<Candidat>> byOffer = ranked.stream()
                    .filter(c -> c.getNomOffre() != null && !c.getNomOffre().isBlank())
                    .collect(Collectors.groupingBy(Candidat::getNomOffre, LinkedHashMap::new, Collectors.toList()));

            List<Candidat> topFits = new ArrayList<>();
            for (List<Candidat> group : byOffer.values()) {
                topFits.add(group.get(0));
            }

            if (topFits.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Top Fit IA", "Aucune application liée à une offre pour le moment.");
                return;
            }

            master.setAll(topFits);
            selectedStatus = null;
            txtRecherche.clear();
            comboGroupeOffre.setValue("Toutes les offres");
            refreshOfferGroupingChoices();
            applyCombinedFilters();
            updatePagination();
            showAlert(Alert.AlertType.INFORMATION, "Top Fit IA", "Analyse terminée. La liste affiche le meilleur candidat pour chaque offre.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Top Fit IA", e.getMessage());
        }
    }

    private void updatePagination() {
        int total = getCurrentViewList().size();
        int pages = (int) Math.ceil((double) total / ROWS);
        pagination.setPageCount(pages == 0 ? 1 : pages);

        int current = pagination.getCurrentPageIndex();
        if (current >= pagination.getPageCount()) {
            current = Math.max(0, pagination.getPageCount() - 1);
            pagination.setCurrentPageIndex(current);
        }

        createPage(current);
        lblCount.setText(String.valueOf(total));
    }

    private TableView<Candidat> createPage(int idx) {
        List<Candidat> list = getCurrentViewList();
        int from = idx * ROWS;
        if (from >= list.size()) {
            tableCandidat.setItems(FXCollections.observableArrayList());
            return tableCandidat;
        }
        int to = Math.min(from + ROWS, list.size());
        tableCandidat.setItems(FXCollections.observableArrayList(list.subList(from, to)));
        return tableCandidat;
    }

    private List<Candidat> getCurrentViewList() {
        List<Candidat> list = new ArrayList<>(filtered);
        if (currentComparator != null) {
            list.sort(currentComparator);
        }
        return list;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
