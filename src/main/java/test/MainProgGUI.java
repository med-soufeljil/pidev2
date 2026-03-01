package test;

import api.ApiServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainProgGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        startApiServer();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Workshop PIDEV");
        primaryStage.show();
    }

    private void startApiServer() {
        try {
            ApiServer apiServer = new ApiServer();
            apiServer.start(8080);
        } catch (IOException e) {
            System.err.println("Unable to start API server: " + e.getMessage());
        }
    }
}
