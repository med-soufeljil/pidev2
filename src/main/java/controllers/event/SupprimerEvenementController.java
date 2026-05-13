package controllers.event;

import models.event.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.event.EvenementService;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Contrôleur pour la suppression d'un événement
 * Affiche une page de confirmation avant suppression définitive
 */
public class SupprimerEvenementController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private Label lblTitreEvenement;           // Titre de l'événement
    @FXML private Label lblDateEvenement;            // Date de l'événement
    @FXML private Label lblLieuEvenement;            // Lieu de l'événement
    @FXML private Label lblStatutEvenement;          // Statut de l'événement
    @FXML private Label lblPlacesEvenement;          // Nombre de places
    @FXML private Label lblDescriptionEvenement;     // Description
    @FXML private Button btnConfirmerSuppression;    // Bouton de confirmation
    @FXML private Button btnAnnuler;                  // Bouton d'annulation

    // ==================== ATTRIBUTS ====================
    private VBox contentArea;                          // Zone de contenu
    private Evenement evenementASupprimer;             // Événement à supprimer
    private EvenementService evenementService = new EvenementService();

    /**
     * Définit la zone de contenu principale
     * @param contentArea Le conteneur VBox
     */
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    /**
     * Charge les données de l'événement à supprimer
     * Affiche un résumé des informations
     * @param evenement L'événement à supprimer
     */
    public void chargerDonnees(Evenement evenement) {
        this.evenementASupprimer = evenement;

        // Afficher les détails de l'événement
        lblTitreEvenement.setText(evenement.getTitre());

        if (evenement.getDateEvenement() != null) {
            lblDateEvenement.setText(evenement.getDateEvenement().toString());
        } else {
            lblDateEvenement.setText("Non définie");
        }

        lblLieuEvenement.setText(evenement.getLieu());
        lblStatutEvenement.setText(evenement.getStatut());
        lblPlacesEvenement.setText(String.valueOf(evenement.getNombrePlacesMax()));

        if (evenement.getDescription() != null && !evenement.getDescription().isEmpty()) {
            lblDescriptionEvenement.setText(evenement.getDescription());
        } else {
            lblDescriptionEvenement.setText("Aucune description");
        }

        // Colorer le statut
        switch (evenement.getStatut()) {
            case "planifié":
                lblStatutEvenement.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                break;
            case "en cours":
                lblStatutEvenement.setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
                break;
            case "terminé":
                lblStatutEvenement.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                break;
        }
    }

    /**
     * Action du bouton Confirmer
     * Supprime définitivement l'événement après double confirmation
     * @param event L'événement de clic
     */
    @FXML
    void confirmerSuppression(ActionEvent event) {
        // Demander une confirmation supplémentaire (double confirmation)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'événement ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement l'événement \""
                + evenementASupprimer.getTitre() + "\" ?\n\nCette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Supprimer de la base de données
                    evenementService.supprimer(evenementASupprimer);

                    // Message de succès
                    showAlert("Succès", "Événement supprimé avec succès !", Alert.AlertType.INFORMATION);

                    // Retour à la liste
                    naviguerVersAffichage();

                } catch (SQLException e) {
                    showAlert("Erreur", "Impossible de supprimer l'événement: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                } catch (IOException e) {
                    showAlert("Erreur", "Erreur de navigation: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Action du bouton Annuler
     * Retourne à la liste sans supprimer
     * @param event L'événement de clic
     */
    @FXML
    void annuler(ActionEvent event) {
        try {
            naviguerVersAffichage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigation vers la page d'affichage des événements
     * @throws IOException si le fichier FXML n'est pas trouvé
     */
    private void naviguerVersAffichage() throws IOException {
        // Charge la vue d'affichage
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/AffichageEvenement.fxml"));
        Parent affichageView = loader.load();

        // Configure le contrôleur
        AffichageEvenementController controller = loader.getController();
        controller.setContentArea(contentArea);

        // Remplace le contenu actuel
        contentArea.getChildren().clear();
        contentArea.getChildren().add(affichageView);
    }

    /**
     * Affiche une alerte
     * @param title Titre
     * @param content Contenu
     * @param type Type d'alerte
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}