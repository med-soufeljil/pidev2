package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Offre;
import models.TypeOffre;
import services.OffreService;
import utils.NavigationState;

public class OffreFormController {

    @FXML private Button btnBack, btnSave;
    @FXML private Label pageTitle;
    @FXML private TextField formNom, formCompetences, formSalaire;
    @FXML private ComboBox<TypeOffre> formType;

    private final OffreService service = new OffreService();
    private Offre editing;

    @FXML
    public void initialize() {
        btnBack.setOnAction(e -> MainController.navigate("Offre.fxml"));
        btnSave.setOnAction(e -> save());

        formType.setItems(FXCollections.observableArrayList(TypeOffre.values()));
        editing = NavigationState.selectedOffre;

        if (editing == null) {
            pageTitle.setText("Nouvelle offre");
        } else {
            pageTitle.setText("Modifier offre");
            formNom.setText(editing.getNomOffre());
            formType.setValue(editing.getType());
            formCompetences.setText(editing.getCompetences());
            formSalaire.setText(String.valueOf(editing.getSalaire()));
        }
    }

    private void save() {
        try {
            Offre target = editing == null ? new Offre() : editing;
            target.setNomOffre(formNom.getText());
            target.setType(formType.getValue());
            target.setCompetences(formCompetences.getText());
            target.setSalaire(Integer.parseInt(formSalaire.getText()));
            if (target.getIdOffre() == 0) service.ajouter(target); else service.modifier(target);
            MainController.navigate("Offre.fxml");
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }
}
