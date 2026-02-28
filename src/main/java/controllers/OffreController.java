package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.Candidat;
import models.Offre;
import models.TypeOffre;
import services.CandidatService;
import services.OffreService;
import services.RecruitmentWorkflowService;
import utils.AuthContext;

import java.sql.SQLException;

public class OffreController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTri;
    @FXML private ComboBox<String> comboOrdre;

    @FXML private TableView<Offre> tableOffre;
    @FXML private TableColumn<Offre, String> colNom, colType, colCompetences;
    @FXML private TableColumn<Offre, Integer> colSalaire;
    @FXML private TableColumn<Offre, Void> colAction;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnFiltre, btnResetFiltre;
    @FXML private Label lblCount;
    @FXML private Pagination pagination;

    @FXML private VBox offerFormPane, applyFormPane;
    @FXML private Label offerFormTitle, applyTitle;
    @FXML private TextField formNom, formCompetences, formSalaire;
    @FXML private ComboBox<TypeOffre> formType;
    @FXML private Button btnSaveOfferForm, btnCancelOfferForm;
    @FXML private TextField applyNom, applyPrenom, applyCin, applyTel, applyAdresse, applyEmail, applyCv;
    @FXML private Button btnSubmitApply, btnCancelApply;

    private final OffreService service = new OffreService();
    private final CandidatService candidatService = new CandidatService();
    private final RecruitmentWorkflowService workflowService = new RecruitmentWorkflowService();
    private final ObservableList<Offre> masterList = FXCollections.observableArrayList();
    private FilteredList<Offre> filteredList;
    private Offre editing;
    private Offre applyingTo;

    private static final int ROWS_PER_PAGE = 8;

    @FXML
    public void initialize() {
        comboTri.getItems().addAll("Nom", "Type", "Compétences", "Salaire");
        comboOrdre.getItems().addAll("Croissant", "Décroissant");
        comboOrdre.setValue("Croissant");
        formType.setItems(FXCollections.observableArrayList(TypeOffre.values()));

        colNom.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCompetences.setCellValueFactory(new PropertyValueFactory<>("competences"));
        colSalaire.setCellValueFactory(new PropertyValueFactory<>("salaire"));

        loadTable();
        filteredList = new FilteredList<>(masterList, b -> true);

        txtRecherche.textProperty().addListener((obs, o, q) -> {
            filteredList.setPredicate(offre -> {
                if (q == null || q.isBlank()) return true;
                String lower = q.toLowerCase();
                return offre.getNomOffre().toLowerCase().contains(lower)
                        || offre.getType().name().toLowerCase().contains(lower)
                        || offre.getCompetences().toLowerCase().contains(lower)
                        || String.valueOf(offre.getSalaire()).contains(lower);
            });
            updatePagination();
        });

        comboTri.setOnAction(e -> appliquerTri());
        comboOrdre.setOnAction(e -> appliquerTri());

        btnAjouter.setOnAction(e -> openOfferForm(null));
        btnModifier.setOnAction(e -> openOfferForm(tableOffre.getSelectionModel().getSelectedItem()));
        btnSupprimer.setOnAction(e -> supprimerOffre());
        btnFiltre.setOnAction(e -> applySalaryFilterFromSearch());
        btnResetFiltre.setOnAction(e -> { filteredList.setPredicate(o -> true); updatePagination(); });

        btnSaveOfferForm.setOnAction(e -> saveOfferForm());
        btnCancelOfferForm.setOnAction(e -> hideOfferForm());
        btnSubmitApply.setOnAction(e -> submitApplication());
        btnCancelApply.setOnAction(e -> hideApplyForm());

        configureActionColumn();
        applyPermissions();
        updatePagination();
    }

    private void applyPermissions() {
        boolean admin = AuthContext.isAdmin();
        btnAjouter.setDisable(!admin);
        btnModifier.setDisable(!admin);
        btnSupprimer.setDisable(!admin);
        btnFiltre.setDisable(!admin);
        btnResetFiltre.setDisable(!admin);
    }

    private void configureActionColumn() {
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Postuler");
            {
                btn.getStyleClass().add("apply-btn");
                btn.setOnAction(e -> {
                    Offre offre = getTableView().getItems().get(getIndex());
                    openApplyForm(offre);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void openApplyForm(Offre offre) {
        applyingTo = offre;
        applyTitle.setText("Postuler à : " + offre.getNomOffre());
        applyNom.clear();
        applyPrenom.clear();
        applyCin.clear();
        applyTel.clear();
        applyAdresse.clear();
        applyEmail.clear();
        applyCv.clear();
        applyFormPane.setVisible(true);
        applyFormPane.setManaged(true);
    }

    private void submitApplication() {
        if (applyingTo == null) return;
        try {
            Candidat c = new Candidat(
                    applyNom.getText(), applyPrenom.getText(), Integer.parseInt(applyCin.getText()),
                    Integer.parseInt(applyTel.getText()), applyAdresse.getText(), applyEmail.getText(), applyCv.getText()
            );
            candidatService.ajouter(c);
            if (c.getIdCandidat() > 0) {
                workflowService.updateCandidatePhase(c.getIdCandidat(), RecruitmentWorkflowService.STATUS_NOUVEAU);
            }
            showAlert(Alert.AlertType.INFORMATION, "Postulation", "Votre candidature a été envoyée pour l'offre: " + applyingTo.getNomOffre());
            hideApplyForm();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void hideApplyForm() {
        applyFormPane.setVisible(false);
        applyFormPane.setManaged(false);
        applyingTo = null;
    }

    private void openOfferForm(Offre selected) {
        if (!AuthContext.isAdmin()) return;
        editing = selected;
        offerFormTitle.setText(selected == null ? "Ajouter Offre" : "Modifier Offre");
        formNom.setText(selected == null ? "" : selected.getNomOffre());
        formType.setValue(selected == null ? null : selected.getType());
        formCompetences.setText(selected == null ? "" : selected.getCompetences());
        formSalaire.setText(selected == null ? "" : String.valueOf(selected.getSalaire()));
        offerFormPane.setVisible(true);
        offerFormPane.setManaged(true);
    }

    private void saveOfferForm() {
        try {
            Offre target = editing == null ? new Offre() : editing;
            target.setNomOffre(formNom.getText());
            target.setType(formType.getValue());
            target.setCompetences(formCompetences.getText());
            target.setSalaire(Integer.parseInt(formSalaire.getText()));

            if (target.getIdOffre() == 0) service.ajouter(target); else service.modifier(target);
            loadTable();
            updatePagination();
            hideOfferForm();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void hideOfferForm() {
        offerFormPane.setVisible(false);
        offerFormPane.setManaged(false);
        editing = null;
    }

    private void supprimerOffre() {
        Offre selected = tableOffre.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            service.supprimer(selected.getIdOffre());
            loadTable();
            updatePagination();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void applySalaryFilterFromSearch() {
        try {
            int min = Integer.parseInt(txtRecherche.getText().trim());
            filteredList.setPredicate(o -> o.getSalaire() >= min);
            updatePagination();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.INFORMATION, "Filtre salaire", "Saisissez un montant dans la recherche puis cliquez Filtre avancé.");
        }
    }

    private void appliquerTri() {
        String champ = comboTri.getValue();
        String ordre = comboOrdre.getValue();
        if (champ == null) return;

        SortedList<Offre> sorted = new SortedList<>(filteredList);
        switch (champ) {
            case "Nom" -> sorted.setComparator((o1, o2) -> o1.getNomOffre().compareToIgnoreCase(o2.getNomOffre()));
            case "Type" -> sorted.setComparator((o1, o2) -> o1.getType().name().compareToIgnoreCase(o2.getType().name()));
            case "Compétences" -> sorted.setComparator((o1, o2) -> o1.getCompetences().compareToIgnoreCase(o2.getCompetences()));
            case "Salaire" -> sorted.setComparator((o1, o2) -> Integer.compare(o1.getSalaire(), o2.getSalaire()));
        }
        if ("Décroissant".equals(ordre) && sorted.getComparator() != null) sorted.setComparator(sorted.getComparator().reversed());
        tableOffre.setItems(sorted);
    }

    private void loadTable() {
        try {
            masterList.setAll(service.recuperer());
            lblCount.setText(String.valueOf(masterList.size()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void updatePagination() {
        int total = filteredList.size();
        int pageCount = (int) Math.ceil((double) total / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        pagination.setPageFactory(this::createPage);
    }

    private TableView<Offre> createPage(int pageIndex) {
        int from = pageIndex * ROWS_PER_PAGE;
        int to = Math.min(from + ROWS_PER_PAGE, filteredList.size());
        tableOffre.setItems(FXCollections.observableArrayList(filteredList.subList(from, to)));
        return tableOffre;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
