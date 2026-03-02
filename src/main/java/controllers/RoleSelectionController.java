package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.SessionContext;

public class RoleSelectionController {

    @FXML
    public void enterAdmin(ActionEvent event) {
        openMainForRole(SessionContext.Role.ADMIN, event);
    }

    @FXML
    public void enterUser(ActionEvent event) {
        openMainForRole(SessionContext.Role.USER, event);
    }

    private void openMainForRole(SessionContext.Role role, ActionEvent event) {
        SessionContext.setCurrentRole(role);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
