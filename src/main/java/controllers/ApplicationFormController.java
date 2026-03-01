package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import models.Candidat;
import models.Offre;
import models.Reunion;
import services.*;
import utils.AuthContext;
import utils.NavigationState;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class ApplicationFormController {

    @FXML private Button btnBack, btnGenerateReunion, btnSendOffer, btnSave, btnPickCv, btnOpenCv;
    @FXML private Button btnTagNouveau, btnTagPremier, btnTagDeuxieme, btnTagOffre, btnTagAcceptee, btnTagRejetee;
    @FXML private Label pageTitle, lblReunionInfo, lblOfferResponse, lblAiScore, lblAiAnalyse;
    @FXML private HBox reunionActions;
    @FXML private ComboBox<Offre> comboOffre;
    @FXML private TextField formNom, formPrenom, formCin, formTel, formAdresse, formEmail, formCv, salaryField;
    @FXML private ComboBox<String> formStatus;

    private final CandidatService candidatService = new CandidatService();
    private final OffreService offreService = new OffreService();
    private final RecrutementService recrutementService = new RecrutementService();
    private final RecruitmentWorkflowService workflowService = new RecruitmentWorkflowService();
    private final ReunionService reunionService = new ReunionService();
    private final ExternalApiService externalApiService = new ExternalApiService();

    private Candidat current;

    @FXML
    public void initialize() {
        btnBack.setOnAction(e -> MainController.navigate(AuthContext.isCandidat() ? "Offre.fxml" : "Candidat.fxml"));
        btnSave.setOnAction(e -> save());
        btnGenerateReunion.setOnAction(e -> openGenerateReunionPopup());
        btnSendOffer.setOnAction(e -> sendOffer());
        btnPickCv.setOnAction(e -> pickCvFile());
        btnOpenCv.setOnAction(e -> openCvFile());

        formStatus.setItems(FXCollections.observableArrayList(
                RecruitmentWorkflowService.STATUS_NOUVEAU,
                RecruitmentWorkflowService.STATUS_PREMIER_ENTRETIEN,
                RecruitmentWorkflowService.STATUS_DEUXIEME_ENTRETIEN,
                RecruitmentWorkflowService.STATUS_OFFRE_ENVOYEE,
                RecruitmentWorkflowService.STATUS_ACCEPTEE,
                RecruitmentWorkflowService.STATUS_REJETEE
        ));
        formStatus.setValue(RecruitmentWorkflowService.STATUS_NOUVEAU);

        btnTagNouveau.setOnAction(e -> applyStatusTag(RecruitmentWorkflowService.STATUS_NOUVEAU));
        btnTagPremier.setOnAction(e -> applyStatusTag(RecruitmentWorkflowService.STATUS_PREMIER_ENTRETIEN));
        btnTagDeuxieme.setOnAction(e -> applyStatusTag(RecruitmentWorkflowService.STATUS_DEUXIEME_ENTRETIEN));
        btnTagOffre.setOnAction(e -> applyStatusTag(RecruitmentWorkflowService.STATUS_OFFRE_ENVOYEE));
        btnTagAcceptee.setOnAction(e -> applyStatusTag(RecruitmentWorkflowService.STATUS_ACCEPTEE));
        btnTagRejetee.setOnAction(e -> applyStatusTag(RecruitmentWorkflowService.STATUS_REJETEE));

        try {
            comboOffre.setItems(FXCollections.observableArrayList(offreService.recuperer()));
        } catch (Exception ignored) {}
        comboOffre.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Offre item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNomOffre()); }
        });
        comboOffre.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Offre item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNomOffre()); }
        });
        comboOffre.setConverter(new StringConverter<>() {
            @Override public String toString(Offre offre) { return offre == null ? "" : offre.getNomOffre(); }
            @Override public Offre fromString(String s) { return null; }
        });

        current = NavigationState.selectedCandidat;
        Offre prefilledOffre = NavigationState.selectedOffre;

        if (current != null) {
            pageTitle.setText("Application: " + current.getNom() + " " + current.getPrenom());
            formNom.setText(current.getNom());
            formPrenom.setText(current.getPrenom());
            formCin.setText(String.valueOf(current.getCIN()));
            formTel.setText(String.valueOf(current.getTel()));
            formAdresse.setText(current.getAdresse());
            formEmail.setText(current.getEmail());
            formCv.setText(current.getCv());
            lblAiScore.setText(String.valueOf(current.getAiScore()));
            lblAiAnalyse.setText(current.getAiAnalyse() == null || current.getAiAnalyse().isBlank() ? "-" : current.getAiAnalyse());
            try {
                formStatus.setValue(workflowService.getCandidatePhase(current.getIdCandidat()));
                lblReunionInfo.setText(workflowService.getLatestReunionSummary(current.getIdCandidat()));
                String response = workflowService.getOfferResponse(current.getIdCandidat());
                lblOfferResponse.setText(response == null ? "-" : response);
                int offerId = recrutementService.recuperer().stream()
                        .filter(r -> r.getIdCandidat() == current.getIdCandidat())
                        .map(r -> r.getIdOffre()).findFirst().orElse(0);
                if (offerId > 0) comboOffre.getItems().stream().filter(o -> o.getIdOffre() == offerId).findFirst().ifPresent(comboOffre::setValue);
            } catch (Exception ignored) {}
        } else {
            pageTitle.setText("Nouvelle application");
            lblReunionInfo.setText("Aucune réunion planifiée");
            lblOfferResponse.setText("-");
            lblAiScore.setText("-");
            lblAiAnalyse.setText("-");
        }

        if (prefilledOffre != null) {
            comboOffre.setValue(prefilledOffre);
        }

        boolean readOnly = NavigationState.readOnly;
        formNom.setDisable(readOnly);
        formPrenom.setDisable(readOnly);
        formCin.setDisable(readOnly);
        formTel.setDisable(readOnly);
        formAdresse.setDisable(readOnly);
        formEmail.setDisable(readOnly);
        formCv.setDisable(readOnly);
        formStatus.setDisable(readOnly);
        btnPickCv.setDisable(readOnly);
        comboOffre.setDisable(readOnly);
        btnOpenCv.setDisable(formCv.getText() == null || formCv.getText().isBlank());
        btnSave.setDisable(readOnly || !AuthContext.isAdmin());
        btnGenerateReunion.setDisable(readOnly || !AuthContext.isAdmin());
        btnSendOffer.setDisable(readOnly || !AuthContext.isAdmin());

        if (!AuthContext.isAdmin()) {
            btnTagNouveau.setDisable(true);
            btnTagPremier.setDisable(true);
            btnTagDeuxieme.setDisable(true);
            btnTagOffre.setDisable(true);
            btnTagAcceptee.setDisable(true);
            btnTagRejetee.setDisable(true);

            btnGenerateReunion.setVisible(false);
            btnGenerateReunion.setManaged(false);
            btnSendOffer.setVisible(false);
            btnSendOffer.setManaged(false);
            salaryField.setVisible(false);
            salaryField.setManaged(false);
            if (reunionActions != null) {
                reunionActions.setVisible(false);
                reunionActions.setManaged(false);
            }
        }
    }


    private void pickCvFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sélectionner un CV");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents CV", "*.pdf", "*.doc", "*.docx")
        );
        File selected = chooser.showOpenDialog(btnPickCv.getScene().getWindow());
        if (selected != null) {
            formCv.setText(selected.getAbsolutePath());
            btnOpenCv.setDisable(false);
        }
    }

    private void openCvFile() {
        String path = formCv.getText();
        if (path == null || path.isBlank()) return;
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le CV: " + ex.getMessage()).showAndWait();
        }
    }

    private void applyStatusTag(String status) {
        formStatus.setValue(status);
        if (current != null) {
            try {
                workflowService.updateCandidatePhase(current.getIdCandidat(), status);
                lblOfferResponse.setText(workflowService.getOfferResponse(current.getIdCandidat()) == null ? "-" : workflowService.getOfferResponse(current.getIdCandidat()));
            } catch (Exception ignored) {}
        }
    }

    private void save() {
        try {
            if (comboOffre.getValue() == null) throw new IllegalStateException("Choisissez une offre d'emploi.");
            Candidat c = current == null ? new Candidat() : current;
            c.setNom(formNom.getText());
            c.setPrenom(formPrenom.getText());
            c.setCIN(Integer.parseInt(formCin.getText()));
            c.setTel(Integer.parseInt(formTel.getText()));
            c.setAdresse(formAdresse.getText());
            c.setEmail(formEmail.getText());
            if (formCv.getText() == null || formCv.getText().isBlank()) throw new IllegalStateException("Ajoutez un CV en PDF/DOC/DOCX.");
            c.setCv(formCv.getText());
            c.setStatut(formStatus.getValue());

            if (c.getIdCandidat() == 0) candidatService.ajouter(c); else candidatService.modifier(c);
            workflowService.updateCandidatePhase(c.getIdCandidat(), formStatus.getValue());
            linkCandidateToOffer(c.getIdCandidat(), comboOffre.getValue().getIdOffre());

            lblAiScore.setText(String.valueOf(c.getAiScore()));
            lblAiAnalyse.setText(c.getAiAnalyse() == null || c.getAiAnalyse().isBlank() ? "-" : c.getAiAnalyse());

            Alert a = new Alert(Alert.AlertType.INFORMATION, "Application enregistrée.");
            a.showAndWait();
            MainController.navigate("Candidat.fxml");
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private void linkCandidateToOffer(int candidateId, int offerId) throws Exception {
        var existing = recrutementService.recuperer().stream().filter(r -> r.getIdCandidat() == candidateId).findFirst().orElse(null);
        if (existing == null) {
            recrutementService.ajouter(new models.Recrutement(offerId, candidateId));
        } else {
            existing.setIdOffre(offerId);
            recrutementService.modifier(existing);
        }
    }

    private void openGenerateReunionPopup() {
        if (current == null || current.getIdCandidat() == 0) {
            new Alert(Alert.AlertType.WARNING, "Enregistrez l'application d'abord.").showAndWait();
            return;
        }

        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Générer réunion");
        ButtonType generateType = new ButtonType("Générer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(generateType, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        TextField linkField = null;
        try {
            linkField = new TextField(externalApiService.generateMeetingLink());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Button regen = new Button("Regénérer lien");
        TextField finalLinkField = linkField;
        regen.setOnAction(e -> {
            try {
                finalLinkField.setText(externalApiService.generateMeetingLink());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.addRow(0, new Label("Date de réunion"), datePicker);
        grid.addRow(1, new Label("Lien"), linkField);
        grid.add(regen, 1, 2);
        d.getDialogPane().setContent(grid);

        TextField finalLinkField1 = linkField;
        d.showAndWait().ifPresent(btn -> {
            if (btn == generateType) {
                try {
                    Reunion reunion = new Reunion();
                    reunion.setIdRH(1);
                    reunion.setIdCandidat(current.getIdCandidat());
                    reunion.setDate(datePicker.getValue().atStartOfDay());
                    reunion.setLink(finalLinkField1.getText());
                    reunionService.ajouter(reunion);
                    lblReunionInfo.setText(workflowService.getLatestReunionSummary(current.getIdCandidat()));
                    new Alert(Alert.AlertType.INFORMATION, "Réunion générée avec succès.").showAndWait();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
                }
            }
        });
    }

    private void sendOffer() {
        try {
            if (current == null || current.getIdCandidat() == 0) throw new IllegalStateException("Enregistrez l'application d'abord.");
            int salary = Integer.parseInt(salaryField.getText());
            workflowService.generateSalaryOfferAndSend(current, salary);
            formStatus.setValue(RecruitmentWorkflowService.STATUS_OFFRE_ENVOYEE);
            new Alert(Alert.AlertType.INFORMATION, "Offre envoyée par email.").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }
}
