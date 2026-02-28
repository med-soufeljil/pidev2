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
import models.Reunion;
import services.CandidatService;
import services.ExternalApiService;
import services.RecruitmentWorkflowService;
import services.ReunionService;
import utils.AuthContext;

import java.sql.SQLException;
import java.time.LocalDate;

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

    @FXML private VBox formPane, profilePane;
    @FXML private Label formTitle, profileInfo;
    @FXML private TextField formNom, formPrenom, formCin, formTel, formAdresse, formEmail, formCv;
    @FXML private ComboBox<String> formStatus;
    @FXML private Button btnGenerateReunion, btnSaveForm, btnCancelForm;
    @FXML private TextField profileSalary;
    @FXML private Button btnSendOffer, btnCloseProfile;

    private final CandidatService service = new CandidatService();
    private final RecruitmentWorkflowService workflowService = new RecruitmentWorkflowService();
    private final ReunionService reunionService = new ReunionService();
    private final ExternalApiService externalApiService = new ExternalApiService();

    private final ObservableList<Candidat> master = FXCollections.observableArrayList();
    private FilteredList<Candidat> filtered;
    private static final int ROWS = 8;
    private Candidat editing;
    private Candidat viewing;

    @FXML
    public void initialize() {
        comboTriChamp.getItems().addAll("Nom", "Prénom", "CIN", "Téléphone", "Adresse", "Email", "CV", "Statut");
        comboTriOrdre.getItems().addAll("Croissant", "Décroissant");
        comboTriOrdre.setValue("Croissant");

        formStatus.setItems(FXCollections.observableArrayList(
                RecruitmentWorkflowService.STATUS_NOUVEAU,
                RecruitmentWorkflowService.STATUS_PREMIER_ENTRETIEN,
                RecruitmentWorkflowService.STATUS_DEUXIEME_ENTRETIEN,
                RecruitmentWorkflowService.STATUS_OFFRE_ENVOYEE,
                RecruitmentWorkflowService.STATUS_ACCEPTEE,
                RecruitmentWorkflowService.STATUS_REJETEE
        ));

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

        txtRecherche.textProperty().addListener((a, b, q) -> {
            applySearch(q);
            updatePagination();
        });
        comboTriChamp.setOnAction(e -> applySort());
        comboTriOrdre.setOnAction(e -> applySort());

        btnTagTous.setOnAction(e -> filterByStatus(null));
        btnTagNouveau.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_NOUVEAU));
        btnTagPremier.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_PREMIER_ENTRETIEN));
        btnTagDeuxieme.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_DEUXIEME_ENTRETIEN));
        btnTagOffre.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_OFFRE_ENVOYEE));
        btnTagAcceptee.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_ACCEPTEE));
        btnTagRejetee.setOnAction(e -> filterByStatus(RecruitmentWorkflowService.STATUS_REJETEE));

        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> openForm(tableCandidat.getSelectionModel().getSelectedItem()));
        btnSupprimer.setOnAction(e -> deleteSelected());
        btnResetFiltre.setOnAction(e -> {
            txtRecherche.clear();
            filterByStatus(null);
            comboTriChamp.setValue(null);
            tableCandidat.setItems(FXCollections.observableArrayList(filtered));
            updatePagination();
        });

        tableCandidat.setRowFactory(tv -> {
            TableRow<Candidat> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getClickCount() == 1 && AuthContext.isAdmin()) {
                    showProfile(row.getItem());
                }
            });
            return row;
        });

        btnSaveForm.setOnAction(e -> saveForm());
        btnCancelForm.setOnAction(e -> hideForm());
        btnGenerateReunion.setOnAction(e -> generateReunion());
        btnSendOffer.setOnAction(e -> sendOfferFromProfile());
        btnCloseProfile.setOnAction(e -> {
            profilePane.setVisible(false);
            profilePane.setManaged(false);
        });

        boolean admin = AuthContext.isAdmin();
        btnAjouter.setDisable(!admin);
        btnModifier.setDisable(!admin);
        btnSupprimer.setDisable(!admin);

        updatePagination();
    }

    private void openForm(Candidat c) {
        if (!AuthContext.isAdmin()) return;
        this.editing = c;
        formTitle.setText(c == null ? "Nouvelle application" : "Modifier application");

        formNom.setText(c == null ? "" : c.getNom());
        formPrenom.setText(c == null ? "" : c.getPrenom());
        formCin.setText(c == null ? "" : String.valueOf(c.getCIN()));
        formTel.setText(c == null ? "" : String.valueOf(c.getTel()));
        formAdresse.setText(c == null ? "" : c.getAdresse());
        formEmail.setText(c == null ? "" : c.getEmail());
        formCv.setText(c == null ? "" : c.getCv());
        formStatus.setValue(c == null || c.getStatut() == null ? RecruitmentWorkflowService.STATUS_NOUVEAU : c.getStatut());

        updateGenerateReunionButton();
        formStatus.valueProperty().addListener((obs, oldV, newV) -> updateGenerateReunionButton());

        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    private void updateGenerateReunionButton() {
        boolean enabled = editing != null && editing.getIdCandidat() > 0 && workflowService.requiresInterviewMeeting(formStatus.getValue());
        btnGenerateReunion.setDisable(!enabled);
    }

    private void saveForm() {
        try {
            Candidat target = editing == null ? new Candidat() : editing;
            target.setNom(formNom.getText());
            target.setPrenom(formPrenom.getText());
            target.setCIN(Integer.parseInt(formCin.getText()));
            target.setTel(Integer.parseInt(formTel.getText()));
            target.setAdresse(formAdresse.getText());
            target.setEmail(formEmail.getText());
            target.setCv(formCv.getText());

            if (target.getIdCandidat() == 0) service.ajouter(target); else service.modifier(target);
            workflowService.updateCandidatePhase(target.getIdCandidat(), formStatus.getValue());

            hideForm();
            loadTable();
            updatePagination();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void hideForm() {
        formPane.setVisible(false);
        formPane.setManaged(false);
        editing = null;
    }

    private void generateReunion() {
        if (editing == null || editing.getIdCandidat() <= 0) return;
        try {
            Reunion reunion = new Reunion();
            reunion.setIdRH(1);
            reunion.setIdCandidat(editing.getIdCandidat());
            reunion.setDate(LocalDate.now().plusDays(1).atStartOfDay());
            reunion.setLink(externalApiService.generateMeetingLink());
            reunionService.ajouter(reunion);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réunion générée dans le menu Réunions.");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void showProfile(Candidat c) {
        this.viewing = c;
        try {
            String phase = workflowService.getCandidatePhase(c.getIdCandidat());
            String offer = workflowService.getGeneratedOffer(c.getIdCandidat());
            profileInfo.setText("Nom: " + c.getNom() + " " + c.getPrenom()
                    + "\nEmail: " + c.getEmail()
                    + "\nTéléphone: " + c.getTel()
                    + "\nStatut: " + phase
                    + "\nOffre: " + (offer == null ? "Aucune" : offer));
            profilePane.setVisible(true);
            profilePane.setManaged(true);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void sendOfferFromProfile() {
        if (viewing == null) return;
        try {
            int salary = Integer.parseInt(profileSalary.getText());
            workflowService.generateSalaryOfferAndSend(viewing, salary);
            loadTable();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre envoyée par email.");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
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
        if ("Décroissant".equals(comboTriOrdre.getValue()) && s.getComparator() != null) {
            s.setComparator(s.getComparator().reversed());
        }
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
