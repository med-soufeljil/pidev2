package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Role;
import models.Utilisateur;
import services.UtilisateurService;
import utils.Navigation;
import utils.Session;

public class LoginController {

    @FXML
    private TextField tfMail;
    @FXML
    private PasswordField pfPassword;
    @FXML
    private Label lblMsg;

    private final UtilisateurService service = new UtilisateurService();

    @FXML
    public void initialize() {
        lblMsg.setText("");
    }

    @FXML
    private void onLogin(javafx.event.ActionEvent e) {
        lblMsg.setText("");
        String mail = tfMail.getText() == null ? "" : tfMail.getText().trim();
        String pass = pfPassword.getText() == null ? "" : pfPassword.getText();

        if (mail.isEmpty() || pass.isEmpty()) {
            setError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            Utilisateur user = service.authenticate(mail, pass);
            if (user == null) {
                setError("Identifiants incorrects.");
                return;
            }

            Session.setCurrent(user);
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();

            if (user.getRole() == Role.ADMIN) {
                Navigation.switchTo(stage, "AdminDashboard.fxml");
            } else {
                Navigation.switchTo(stage, "EmployeDashboard.fxml");
            }

        } catch (Exception ex) {
            setError("Erreur: " + ex.getMessage());
        }
    }

    private void setError(String msg) {
        lblMsg.setStyle("-fx-text-fill:#cc0000;");
        lblMsg.setText(msg);
    }
}
