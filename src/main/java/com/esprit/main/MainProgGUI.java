package com.esprit.main;

import com.esprit.services.StatutScheduler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainProgGUI extends Application {

    private StatutScheduler statutScheduler;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Démarrer le scheduler de mise à jour des statuts
            statutScheduler = new StatutScheduler();
            statutScheduler.demarrer();

            System.out.println("🚀 Démarrage de l'application...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));

            if (loader.getLocation() == null) {
                System.err.println("❌ ERREUR: MainView.fxml non trouvé dans resources!");
                return;
            }

            Parent root = loader.load();
            System.out.println("✅ MainView.fxml chargé avec succès");

            Scene scene = new Scene(root);

            try {
                String css = getClass().getResource("/style.css").toExternalForm();
                scene.getStylesheets().add(css);
                System.out.println("✅ style.css chargé");
            } catch (Exception e) {
                System.out.println("ℹ️ style.css non trouvé (optionnel)");
            }

            primaryStage.setTitle("Gestion des Événements");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

            System.out.println("✅ Application démarrée avec succès!");

        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Arrêter le scheduler à la fermeture de l'application
        if (statutScheduler != null) {
            statutScheduler.arreter();
            System.out.println("🛑 Scheduler arrêté");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}