package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Candidat;
import models.Reunion;
import services.CandidatService;
import services.ExternalApiService;
import services.ReunionService;
import utils.NavigationState;

import java.io.IOException;
import java.time.LocalDate;

public class ReunionFormController {
    @FXML private Button btnBack, btnGenerate, btnSave;
    @FXML private Label title;
    @FXML private TextField formIdRh, formLink;
    @FXML private ComboBox<Candidat> formCandidat;
    @FXML private DatePicker formDate;

    private final ReunionService service = new ReunionService();
    private final CandidatService candidatService = new CandidatService();
    private final ExternalApiService externalApiService = new ExternalApiService();
    private Reunion editing;

    @FXML public void initialize() {
        btnBack.setOnAction(e -> MainController.navigate("Reunion.fxml"));
        btnGenerate.setOnAction(e -> {
            try {
                formLink.setText(externalApiService.generateMeetingLink());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        btnSave.setOnAction(e -> save());
        formCandidat.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Candidat i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.getNom()+" "+i.getPrenom());}});
        formCandidat.setButtonCell(new ListCell<>() { @Override protected void updateItem(Candidat i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.getNom()+" "+i.getPrenom());}});
        try { formCandidat.setItems(FXCollections.observableArrayList(candidatService.recuperer())); } catch (Exception ignored) {}
        editing = NavigationState.selectedReunion;
        if (editing == null) {
            title.setText("Nouvelle réunion");
            formIdRh.setText("1"); formDate.setValue(LocalDate.now().plusDays(1));
            try {
                formLink.setText(externalApiService.generateMeetingLink());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            title.setText("Modifier réunion");
            formIdRh.setText(String.valueOf(editing.getIdRH())); formDate.setValue(editing.getDate().toLocalDate()); formLink.setText(editing.getLink());
            formCandidat.getItems().stream().filter(c -> c.getIdCandidat()==editing.getIdCandidat()).findFirst().ifPresent(v -> formCandidat.setValue(v));
        }
    }

    private void save() {
        try {
            Reunion target = editing == null ? new Reunion() : editing;
            target.setIdRH(Integer.parseInt(formIdRh.getText()));
            target.setIdCandidat(formCandidat.getValue().getIdCandidat());
            target.setDate(formDate.getValue().atStartOfDay());
            target.setLink(formLink.getText());
            if (target.getIdReunion() == 0) service.ajouter(target); else service.modifier(target);
            MainController.navigate("Reunion.fxml");
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }
}
