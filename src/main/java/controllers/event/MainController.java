package controllers.event;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Contrôleur principal de l'application
 * Gère la navigation entre les vues et le basculement Frontoffice/Backoffice
 */
public class MainController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private VBox contentArea;          // Zone centrale où s'affichent les vues
    @FXML private Button btnToggleView;      // Bouton pour basculer entre Front/Back

    // ==================== ATTRIBUTS ====================
    private boolean isBackoffice = false;    // false = Frontoffice, true = Backoffice

    /**
     * Initialisation du contrôleur
     * Charge la vue par défaut (Frontoffice)
     */
    @FXML
    public void initialize() {
        System.out.println("✅ MainController initialisé");
        showEvenements(); // Frontoffice par défaut
        updateToggleButtonText();
    }

    /**
     * Bascule entre le mode Frontoffice et Backoffice
     */
    @FXML
    private void toggleView() {
        isBackoffice = !isBackoffice;
        updateToggleButtonText();
        showEvenements(); // Recharge la vue avec le nouveau mode
    }

    /**
     * Met à jour le texte et le style du bouton de bascule
     */
    private void updateToggleButtonText() {
        if (isBackoffice) {
            btnToggleView.setText("🎨 Passer au Frontoffice");
            btnToggleView.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        } else {
            btnToggleView.setText("🔧 Passer au Backoffice");
            btnToggleView.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        }
    }

    /**
     * Affiche la vue des événements (Frontoffice ou Backoffice selon le mode)
     */
    @FXML
    private void showEvenements() {
        System.out.println("📅 Chargement des Événements...");
        if (isBackoffice) {
            loadView("event/AffichageEvenement.fxml");
        } else {
            loadView("event/AffichageEvenementSimple.fxml");
        }
    }

    /**
     * Charge une vue FXML et l'injecte dans la zone de contenu
     * @param fxmlFile Le nom du fichier FXML à charger
     */
    private void loadView(String fxmlFile) {
        try {
            // Charge le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent view = loader.load();

            // Récupère le contrôleur et lui injecte la zone de contenu
            Object controller = loader.getController();

            // Injection selon le type de contrôleur
            if (controller instanceof AffichageEvenementSimpleController) {
                ((AffichageEvenementSimpleController) controller).setContentArea(contentArea);
            } else if (controller instanceof AffichageEvenementController) {
                ((AffichageEvenementController) controller).setContentArea(contentArea);
            } else if (controller instanceof AjoutEvenementController) {
                ((AjoutEvenementController) controller).setContentArea(contentArea);
            } else if (controller instanceof ModifierEvenementController) {
                ((ModifierEvenementController) controller).setContentArea(contentArea);
            }

            // Remplace le contenu actuel par la nouvelle vue
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}