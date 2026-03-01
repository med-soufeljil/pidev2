package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Candidat;
import models.Offre;
import models.Recrutement;
import services.CandidatService;
import services.OffreService;
import services.RecrutementService;
import utils.NavigationState;

public class RecrutementFormController {
    @FXML private Button btnBack, btnSave;
    @FXML private Label title;
    @FXML private ComboBox<Offre> formOffre;
    @FXML private ComboBox<Candidat> formCandidat;

    private final RecrutementService service = new RecrutementService();
    private final CandidatService candidatService = new CandidatService();
    private final OffreService offreService = new OffreService();
    private Recrutement editing;

    @FXML public void initialize() {
        btnBack.setOnAction(e -> MainController.navigate("Recrutement.fxml"));
        btnSave.setOnAction(e -> save());
        editing = NavigationState.selectedRecrutement;
        title.setText(editing == null ? "Nouveau recrutement" : "Modifier recrutement");
        try {
            formOffre.setItems(FXCollections.observableArrayList(offreService.recuperer()));
            formCandidat.setItems(FXCollections.observableArrayList(candidatService.recuperer()));
        } catch (Exception ignored) {}
        formOffre.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Offre i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.getNomOffre()); }});
        formOffre.setButtonCell(new ListCell<>() { @Override protected void updateItem(Offre i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.getNomOffre()); }});
        formCandidat.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Candidat i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.getNom()+" "+i.getPrenom()); }});
        formCandidat.setButtonCell(new ListCell<>() { @Override protected void updateItem(Candidat i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.getNom()+" "+i.getPrenom()); }});
        if (editing != null) {
            formOffre.getItems().stream().filter(o -> o.getIdOffre() == editing.getIdOffre()).findFirst().ifPresent(v -> formOffre.setValue(v));
            formCandidat.getItems().stream().filter(c -> c.getIdCandidat() == editing.getIdCandidat()).findFirst().ifPresent(v -> formCandidat.setValue(v));
        }
    }

    private void save() {
        try {
            Recrutement target = editing == null ? new Recrutement() : editing;
            target.setIdOffre(formOffre.getValue().getIdOffre());
            target.setIdCandidat(formCandidat.getValue().getIdCandidat());
            if (target.getIdRec() == 0) service.ajouter(target); else service.modifier(target);
            MainController.navigate("Recrutement.fxml");
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }
}
