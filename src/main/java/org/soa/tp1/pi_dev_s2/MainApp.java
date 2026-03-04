package org.soa.tp1.pi_dev_s2;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        try {
            System.out.println(">>> Chargement login.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            System.out.println(">>> ✅ login.fxml chargé");
            stage.setScene(new Scene(root, 440, 540));
            stage.setTitle("PI_DEV — Connexion");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
            System.out.println(">>> ✅ Fenêtre affichée");

        } catch (Exception e) {
            System.out.println(">>> ❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
