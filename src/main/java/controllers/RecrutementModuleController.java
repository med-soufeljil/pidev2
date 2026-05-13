package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import utils.AuthContext;

public class RecrutementModuleController {
    private static RecrutementModuleController instance;

    @FXML private Button btnDashboard, btnCandidat, btnOffre, btnReunion;
    @FXML private AnchorPane contentArea;

    @FXML
    public void initialize() {
        instance = this;
        boolean isAdmin = AuthContext.isAdmin();
        btnDashboard.setVisible(isAdmin);
        btnDashboard.setManaged(isAdmin);
        btnCandidat.setDisable(!isAdmin);
        btnReunion.setDisable(!isAdmin);

        btnDashboard.setOnAction(e -> loadUI("Dashboard.fxml"));
        btnCandidat.setOnAction(e -> loadUI("Candidat.fxml"));
        btnOffre.setOnAction(e -> loadUI("Offre.fxml"));
        btnReunion.setOnAction(e -> loadUI("Reunion.fxml"));
        loadUI("Dashboard.fxml");
    }

    public static boolean navigateInModule(String fxml) {
        if (instance == null) return false;
        instance.loadUI(fxml);
        return true;
    }

    private void loadUI(String fxml) {
        try {
            Parent pane = FXMLLoader.load(getClass().getResource("/" + fxml));
            contentArea.getChildren().setAll(pane);
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
