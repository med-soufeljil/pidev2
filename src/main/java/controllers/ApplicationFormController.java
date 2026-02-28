package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;
import models.Candidat;
import models.Offre;
import models.Reunion;
import services.*;
import utils.AuthContext;
import utils.NavigationState;

import java.time.LocalDate;

public class ApplicationFormController {

    @FXML private Button btnBack, btnGenerateReunion, btnSendOffer, btnSave;
    @FXML private Label pageTitle;
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
        btnBack.setOnAction(e -> MainController.navigate("Candidat.fxml"));
        btnSave.setOnAction(e -> save());
        btnGenerateReunion.setOnAction(e -> generateReunion());
        btnSendOffer.setOnAction(e -> sendOffer());

        formStatus.setItems(FXCollections.observableArrayList(
                RecruitmentWorkflowService.STATUS_NOUVEAU,
                RecruitmentWorkflowService.STATUS_PREMIER_ENTRETIEN,
                RecruitmentWorkflowService.STATUS_DEUXIEME_ENTRETIEN,
                RecruitmentWorkflowService.STATUS_OFFRE_ENVOYEE,
                RecruitmentWorkflowService.STATUS_ACCEPTEE,
                RecruitmentWorkflowService.STATUS_REJETEE
        ));
        formStatus.setValue(RecruitmentWorkflowService.STATUS_NOUVEAU);

        try {
            comboOffre.setItems(FXCollections.observableArrayList(offreService.recuperer()));
        } catch (Exception ignored) {}
        comboOffre.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Offre item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNomOffre()); }
        });
        comboOffre.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Offre item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.getNomOffre()); }
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
            try {
                formStatus.setValue(workflowService.getCandidatePhase(current.getIdCandidat()));
                int offerId = recrutementService.recuperer().stream()
                        .filter(r -> r.getIdCandidat() == current.getIdCandidat())
                        .map(r -> r.getIdOffre()).findFirst().orElse(0);
                if (offerId > 0) comboOffre.getItems().stream().filter(o -> o.getIdOffre() == offerId).findFirst().ifPresent(o -> comboOffre.setValue(o));
            } catch (Exception ignored) {}
        } else {
            pageTitle.setText("Nouvelle application");
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
        comboOffre.setDisable(readOnly);
        btnSave.setDisable(readOnly || !AuthContext.isAdmin());
        btnGenerateReunion.setDisable(readOnly || !AuthContext.isAdmin());
        btnSendOffer.setDisable(readOnly || !AuthContext.isAdmin());
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
            c.setCv(formCv.getText());

            if (c.getIdCandidat() == 0) candidatService.ajouter(c); else candidatService.modifier(c);
            workflowService.updateCandidatePhase(c.getIdCandidat(), formStatus.getValue());
            linkCandidateToOffer(c.getIdCandidat(), comboOffre.getValue().getIdOffre());

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

    private void generateReunion() {
        try {
            if (current == null || current.getIdCandidat() == 0) throw new IllegalStateException("Enregistrez l'application d'abord.");
            Reunion reunion = new Reunion();
            reunion.setIdRH(1);
            reunion.setIdCandidat(current.getIdCandidat());
            reunion.setDate(LocalDate.now().plusDays(1).atStartOfDay());
            reunion.setLink(externalApiService.generateMeetingLink());
            reunionService.ajouter(reunion);
            new Alert(Alert.AlertType.INFORMATION, "Réunion générée.").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private void sendOffer() {
        try {
            if (current == null || current.getIdCandidat() == 0) throw new IllegalStateException("Enregistrez l'application d'abord.");
            int salary = Integer.parseInt(salaryField.getText());
            workflowService.generateSalaryOfferAndSend(current, salary);
            new Alert(Alert.AlertType.INFORMATION, "Offre envoyée par email.").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }
}
