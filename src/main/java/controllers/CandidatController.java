package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
import java.util.Optional;

public class CandidatController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTriChamp;
    @FXML private ComboBox<String> comboTriOrdre;
    @FXML private TableView<Candidat> tableCandidat;
    @FXML private TableColumn<Candidat, String> colNom, colPrenom, colAdresse, colEmail, colCv, colStatut;
    @FXML private TableColumn<Candidat, Integer> colCIN, colTel;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnFiltre, btnResetFiltre;
    @FXML private Label lblCount;
    @FXML private Pagination pagination;

    private final CandidatService service = new CandidatService();
    private final RecruitmentWorkflowService workflowService = new RecruitmentWorkflowService();
    private final ReunionService reunionService = new ReunionService();
    private final ExternalApiService externalApiService = new ExternalApiService();
    private final ObservableList<Candidat> master = FXCollections.observableArrayList();
    private FilteredList<Candidat> filtered;
    private static final int ROWS = 8;
    private boolean openingProfile;

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
        filtered = new FilteredList<>(master, x -> true);

        txtRecherche.textProperty().addListener((a, b, q) -> { applySearch(q); updatePagination(); });
        comboTriChamp.setOnAction(e -> applySort());
        comboTriOrdre.setOnAction(e -> applySort());

        btnAjouter.setOnAction(e -> openForm(null));
        btnModifier.setOnAction(e -> openForm(tableCandidat.getSelectionModel().getSelectedItem()));
        btnSupprimer.setOnAction(e -> deleteSelected());
        btnFiltre.setOnAction(e -> openFilterDialog());
        btnResetFiltre.setOnAction(e -> { filtered.setPredicate(x -> true); updatePagination(); });

        tableCandidat.setRowFactory(tv -> {
            TableRow<Candidat> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getClickCount() == 1 && AuthContext.isAdmin() && !openingProfile) {
                    openProfileDialog(row.getItem());
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

    private void openForm(Candidat c) {
        if (!AuthContext.isAdmin()) return;

        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(c == null ? "Ajouter application" : "Modifier application");
        ButtonType save = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        TextField nom = new TextField();
        TextField prenom = new TextField();
        TextField cin = new TextField();
        TextField tel = new TextField();
        TextField adr = new TextField();
        TextField email = new TextField();
        TextField cv = new TextField();
        ComboBox<String> status = new ComboBox<>();
        status.setItems(FXCollections.observableArrayList(
                RecruitmentWorkflowService.STATUS_NOUVEAU,
                RecruitmentWorkflowService.STATUS_PREMIER_ENTRETIEN,
                RecruitmentWorkflowService.STATUS_DEUXIEME_ENTRETIEN,
                RecruitmentWorkflowService.STATUS_OFFRE_ENVOYEE,
                RecruitmentWorkflowService.STATUS_ACCEPTEE,
                RecruitmentWorkflowService.STATUS_REJETEE
        ));
        status.setValue(RecruitmentWorkflowService.STATUS_NOUVEAU);

        Button btnGenerateReunion = new Button("Générer réunion");
        btnGenerateReunion.getStyleClass().add("apply-btn");
        btnGenerateReunion.setDisable(true);

        final Candidat[] current = new Candidat[]{c};

        if (c != null) {
            nom.setText(c.getNom());
            prenom.setText(c.getPrenom());
            cin.setText(String.valueOf(c.getCIN()));
            tel.setText(String.valueOf(c.getTel()));
            adr.setText(c.getAdresse());
            email.setText(c.getEmail());
            cv.setText(c.getCv());
            status.setValue(c.getStatut() == null ? RecruitmentWorkflowService.STATUS_NOUVEAU : c.getStatut());
        }

        status.valueProperty().addListener((obs, oldV, newV) ->
                btnGenerateReunion.setDisable(current[0] == null || !workflowService.requiresInterviewMeeting(newV))
        );

        btnGenerateReunion.setOnAction(e -> {
            if (current[0] == null || current[0].getIdCandidat() <= 0) {
                showAlert(Alert.AlertType.WARNING, "Réunion", "Enregistrez d'abord l'application avant de générer la réunion.");
                return;
            }
            scheduleInterviewDialog(current[0]);
        });

        GridPane g = new GridPane();
        g.getStyleClass().add("form-grid");
        g.setHgap(8);
        g.setVgap(8);
        g.addRow(0, new Label("Nom"), nom);
        g.addRow(1, new Label("Prénom"), prenom);
        g.addRow(2, new Label("CIN"), cin);
        g.addRow(3, new Label("Téléphone"), tel);
        g.addRow(4, new Label("Adresse"), adr);
        g.addRow(5, new Label("Email"), email);
        g.addRow(6, new Label("CV"), cv);
        g.addRow(7, new Label("Statut"), status);
        g.add(new HBox(10, btnGenerateReunion), 1, 8);
        d.getDialogPane().setContent(g);

        Optional<ButtonType> r = d.showAndWait();
        if (r.isPresent() && r.get() == save) {
            try {
                if (current[0] == null) current[0] = new Candidat();
                current[0].setNom(nom.getText());
                current[0].setPrenom(prenom.getText());
                current[0].setCIN(Integer.parseInt(cin.getText()));
                current[0].setTel(Integer.parseInt(tel.getText()));
                current[0].setAdresse(adr.getText());
                current[0].setEmail(email.getText());
                current[0].setCv(cv.getText());

                if (current[0].getIdCandidat() == 0) {
                    service.ajouter(current[0]);
                } else {
                    service.modifier(current[0]);
                }

                if (current[0].getIdCandidat() > 0) {
                    workflowService.updateCandidatePhase(current[0].getIdCandidat(), status.getValue());
                    current[0].setStatut(status.getValue());
                }

                loadTable();
                updatePagination();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        }
    }

    private void scheduleInterviewDialog(Candidat candidat) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Générer réunion pour " + candidat.getNom() + " " + candidat.getPrenom());
        ButtonType save = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        TextField txtIdRh = new TextField("1");
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        TextField txtLink = new TextField(externalApiService.generateMeetingLink());
        Button btnRegenerateLink = new Button("Regénérer lien Meet");
        btnRegenerateLink.getStyleClass().add("apply-btn");
        btnRegenerateLink.setOnAction(e -> txtLink.setText(externalApiService.generateMeetingLink()));

        GridPane grid = new GridPane();
        grid.getStyleClass().add("form-grid");
        grid.setHgap(8);
        grid.setVgap(8);
        grid.addRow(0, new Label("ID RH"), txtIdRh);
        grid.addRow(1, new Label("Date"), datePicker);
        grid.addRow(2, new Label("Lien"), txtLink);
        grid.add(new HBox(10, btnRegenerateLink), 1, 3);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == save) {
            try {
                Reunion reunion = new Reunion();
                reunion.setIdRH(Integer.parseInt(txtIdRh.getText()));
                reunion.setIdCandidat(candidat.getIdCandidat());
                reunion.setDate(datePicker.getValue().atStartOfDay());
                reunion.setLink(txtLink.getText());
                reunionService.ajouter(reunion);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réunion générée et ajoutée dans le menu Réunions.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        }
    }

    private void openProfileDialog(Candidat c) {
        openingProfile = true;
        try {
            Dialog<ButtonType> d = new Dialog<>();
            d.setTitle("Profil application");
            ButtonType generate = new ButtonType("Générer offre & Envoyer mail", ButtonBar.ButtonData.OK_DONE);
            d.getDialogPane().getButtonTypes().addAll(generate, ButtonType.CANCEL);

            String phase = workflowService.getCandidatePhase(c.getIdCandidat());
            String offer = workflowService.getGeneratedOffer(c.getIdCandidat());

            Label info = new Label("Nom: " + c.getNom() + " " + c.getPrenom()
                    + "\nEmail: " + c.getEmail()
                    + "\nTéléphone: " + c.getTel()
                    + "\nStatut: " + phase
                    + "\nOffre: " + (offer == null ? "Aucune" : offer));
            info.setWrapText(true);

            TextField salaireField = new TextField();
            salaireField.setPromptText("Ex: 3500");
            salaireField.textProperty().addListener((obs, oldV, newV) -> {
                if (!newV.matches("\\d*")) salaireField.setText(newV.replaceAll("[^\\d]", ""));
            });

            VBox box = new VBox(12, info, new Label("Salaire de l'offre"), salaireField);
            box.getStyleClass().add("profile-card");
            d.getDialogPane().setContent(box);

            Optional<ButtonType> res = d.showAndWait();
            if (res.isPresent() && res.get() == generate) {
                if (salaireField.getText().isBlank()) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir un salaire.");
                    return;
                }
                int salary = Integer.parseInt(salaireField.getText());
                workflowService.generateSalaryOfferAndSend(c, salary);
                loadTable();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre envoyée par API mail avec boutons Accepter/Rejeter.");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        } finally {
            openingProfile = false;
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

    private void openFilterDialog() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Filtre Nom");
        d.setHeaderText(null);
        d.setContentText("Contient:");
        d.showAndWait().ifPresent(v -> {
            filtered.setPredicate(c -> c.getNom().toLowerCase().contains(v.toLowerCase()));
            updatePagination();
        });
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
