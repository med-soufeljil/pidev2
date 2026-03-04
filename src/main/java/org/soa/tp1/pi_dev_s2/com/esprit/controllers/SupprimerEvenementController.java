package org.soa.tp1.pi_dev_s2.com.esprit.controllers;

import org.soa.tp1.pi_dev_s2.com.esprit.models.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.soa.tp1.pi_dev_s2.com.esprit.services.EvenementService;

import java.io.IOException;
import java.sql.SQLException;

public class SupprimerEvenementController {

    @FXML private Label lblTitreEvenement;
    @FXML private Label lblDateEvenement;
    @FXML private Label lblLieuEvenement;
    @FXML private Label lblStatutEvenement;
    @FXML private Label lblPlacesEvenement;
    @FXML private Label lblDescriptionEvenement;
    @FXML private Button btnConfirmerSuppression;
    @FXML private Button btnAnnuler;

    private VBox contentArea;
    private Evenement evenementASupprimer;
    private EvenementService evenementService = new EvenementService();

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    /**
     * Charge les données de l'événement à supprimer
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

    @FXML
    void confirmerSuppression(ActionEvent event) {
        // Demander une confirmation supplémentaire
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

    @FXML
    void annuler(ActionEvent event) {
        try {
            // Retour à la liste sans supprimer
            naviguerVersAffichage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void naviguerVersAffichage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageEvenement.fxml"));
        Parent affichageView = loader.load();

        AffichageEvenementController controller = loader.getController();
        controller.setContentArea(contentArea);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(affichageView);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}