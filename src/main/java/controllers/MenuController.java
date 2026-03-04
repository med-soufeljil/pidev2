package controllers;

import javafx.event.ActionEvent;
import javafx.stage.Stage;
import utils.Navigation;

public class MenuController {

    public void goToConge(ActionEvent e) {
        Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
        Navigation.switchTo(stage, "DemandeCongeView.fxml");
    }

    public void goToTeletravail(ActionEvent e) {
        Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
        Navigation.switchTo(stage, "DemandeTeletravailView.fxml");
    }
}