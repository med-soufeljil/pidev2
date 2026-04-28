package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import utils.SessionContext;

public class RoleSelectionController {
    @FXML private ComboBox<String> cbRole;

    @FXML
    public void initialize() {
        cbRole.setItems(FXCollections.observableArrayList("ADMIN", "USER"));
    }

    @FXML
    public void enterSelectedRole(ActionEvent event) {
        String role = cbRole.getValue();
        if (role == null) return;
        openMainForRole("ADMIN".equals(role) ? SessionContext.Role.ADMIN : SessionContext.Role.USER, event);
    }

    private void openMainForRole(SessionContext.Role role, ActionEvent event) {
        SessionContext.setCurrentRole(role);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/mainformation.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
