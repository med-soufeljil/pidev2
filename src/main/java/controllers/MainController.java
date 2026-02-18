// Le package permet d’organiser les classes dans des dossiers logiques.
// Ici, cette classe appartient au package "controllers".
package controllers;

// Importation de la classe ActionEvent qui représente un événement déclenché
// par une action utilisateur (clic sur un bouton, menu, etc.).
import javafx.event.ActionEvent;

// Importation de l’annotation FXML qui permet de lier le contrôleur au fichier FXML.
import javafx.fxml.FXML;

// Importation de FXMLLoader qui sert à charger un fichier FXML.
import javafx.fxml.FXMLLoader;

// Importation de Parent, qui représente la racine (conteneur principal) d’une interface graphique.
import javafx.scene.Parent;

// Importation de Scene, qui représente le contenu graphique d’une fenêtre.
import javafx.scene.Scene;

// Importation de Stage, qui représente une fenêtre JavaFX.
import javafx.stage.Stage;

// Déclaration de la classe MainController.
// Cette classe est le contrôleur de l’interface principale (MainView.fxml par exemple).
public class MainController {

    // @FXML indique que cette méthode est liée à un élément du fichier FXML
    // (par exemple un bouton avec onAction="#openFormation").
    @FXML
    public void openFormation(ActionEvent event) {
        // Cette méthode ouvre une nouvelle fenêtre contenant FormationView.fxml
        // avec le titre "Gestion des Formations".
        openWindow("/FormationView.fxml", "Gestion des Formations");
    }

    // @FXML indique que cette méthode est appelée depuis le fichier FXML.
    @FXML
    public void openApprenant(ActionEvent event) {
        // Cette méthode ouvre une nouvelle fenêtre contenant ApprenantView.fxml
        // avec le titre "Gestion des Apprenants".
        openWindow("/ApprenantView.fxml", "Gestion des Apprenants");
    }

    // @FXML indique que cette méthode est liée à un bouton "Quitter".
    @FXML
    public void quitter(ActionEvent event) {
        // System.exit(0) ferme complètement l’application Java.
        // 0 signifie "arrêt normal du programme".
        System.exit(0);
    }

    // Méthode privée (accessible uniquement dans cette classe)
    // qui permet d’ouvrir une nouvelle fenêtre à partir d’un fichier FXML.
    private void openWindow(String fxml, String title) {
        try {
            // FXMLLoader.load(...) charge le fichier FXML indiqué par son chemin.
            // getClass().getResource(fxml) cherche le fichier dans les ressources du projet.
            Parent root = FXMLLoader.load(getClass().getResource(fxml));

            // Création d’un nouvel objet Stage (une nouvelle fenêtre).
            Stage stage = new Stage();

            // Définition du titre de la fenêtre (barre du haut).
            stage.setTitle(title);

            // Création d’une scène (Scene) qui contient l’interface graphique "root".
            stage.setScene(new Scene(root));

            // Affichage de la fenêtre à l’écran.
            stage.show();

        } catch (Exception e) {
            // En cas d’erreur (fichier FXML introuvable, erreur de chargement, etc.),
            // on affiche les détails de l’erreur dans la console.
            e.printStackTrace();
        }
    }
}