package com.esprit.controllers;

import com.esprit.models.Participation;
import com.esprit.models.Evenement;
import com.esprit.services.ParticipationService;
import com.esprit.services.EvenementService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AjoutParticipationController {

    @FXML private ComboBox<String> cbEvenement;
    @FXML private DatePicker dpDateInscription;
    @FXML private ComboBox<String> cbStatut;
    @FXML private CheckBox chkPresence;
    @FXML private TextField tfIdEvenementCache;
    @FXML private Label lblValidation;
    @FXML private Button btnAjouter;
    @FXML private Button btnAnnuler;

    private VBox contentArea;
    private Evenement eventSelectionne;
    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService evenementService = new EvenementService();

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    public void setEvent(Evenement event) {
        this.eventSelectionne = event;
        if (event != null) {
            // Pré-sélectionner l'événement
            cbEvenement.setValue(event.getIdEvenement() + " - " + event.getTitre());
            cbEvenement.setDisable(true);
            tfIdEvenementCache.setText(String.valueOf(event.getIdEvenement()));
        }
    }

    @FXML
    public void initialize() {
        System.out.println("AjoutParticipationController: initialize()");
        try {
            chargerEvenements();
            initializeStatuts();
            dpDateInscription.setValue(LocalDate.now());
            setupRealTimeValidation();
            btnAjouter.setDisable(true);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur d'initialisation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void chargerEvenements() {
        try {
            List<Evenement> events = evenementService.recuperer();
            for (Evenement e : events) {
                cbEvenement.getItems().add(e.getIdEvenement() + " - " + e.getTitre());
            }

            cbEvenement.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    int eventId = Integer.parseInt(newVal.split(" - ")[0]);
                    tfIdEvenementCache.setText(String.valueOf(eventId));
                } else {
                    tfIdEvenementCache.clear();
                }
                validateForm();
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les événements", Alert.AlertType.ERROR);
        }
    }

    private void initializeStatuts() {
        // Utiliser des valeurs sans accents pour éviter les problèmes d'encodage
        cbStatut.getItems().addAll("confirme", "en attente", "annule");
        cbStatut.setValue("confirme");
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void setupRealTimeValidation() {
        cbEvenement.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        dpDateInscription.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        chkPresence.selectedProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void validateForm() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        // Validation Événement
        if (cbEvenement.getValue() == null) {
            errors.append("• L'événement est obligatoire\n");
            isValid = false;
        }

        // Validation Date
        if (dpDateInscription.getValue() == null) {
            errors.append("• La date d'inscription est obligatoire\n");
            isValid = false;
        } else if (dpDateInscription.getValue().isAfter(LocalDate.now())) {
            errors.append("• La date d'inscription ne peut pas être dans le futur\n");
            isValid = false;
        }

        // Validation Statut
        if (cbStatut.getValue() == null) {
            errors.append("• Le statut est obligatoire\n");
            isValid = false;
        }

        // Afficher les erreurs
        if (!isValid) {
            lblValidation.setText(errors.toString());
            lblValidation.setStyle("-fx-text-fill: #d32f2f;");
            lblValidation.setVisible(true);
        } else {
            lblValidation.setText("✓ Tous les champs sont valides");
            lblValidation.setStyle("-fx-text-fill: #2e7d32;");
            lblValidation.setVisible(true);
        }

        btnAjouter.setDisable(!isValid);
    }

    @FXML
    void ajouter(ActionEvent event) {
        try {
            if (tfIdEvenementCache.getText() == null || tfIdEvenementCache.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner un événement", Alert.AlertType.ERROR);
                return;
            }

            int idEvent = Integer.parseInt(tfIdEvenementCache.getText());
            LocalDate dateInscription = dpDateInscription.getValue();
            String statut = cbStatut.getValue();
            boolean presence = chkPresence.isSelected();

            // Créer l'objet Participation
            Participation participation = new Participation(idEvent, dateInscription, statut, presence);

            // Ajouter à la base de données
            participationService.ajouter(participation);

            showAlert("Succès", "Participation ajoutée avec succès !", Alert.AlertType.INFORMATION);
            naviguerVersAffichage();

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();

            // Message d'erreur plus détaillé
            String errorMessage = "Erreur base de données: " + e.getMessage();
            if (e.getMessage().contains("Data truncated")) {
                errorMessage = "La valeur du statut est trop longue. Utilisez 'confirme' au lieu de 'confirmé'.";
            } else if (e.getMessage().contains("foreign key")) {
                errorMessage = "L'événement sélectionné n'existe pas dans la base de données.";
            }
            showAlert("Erreur", errorMessage, Alert.AlertType.ERROR);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format de nombre invalide", Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur de navigation", Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur inattendue: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void naviguerVersAffichage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageParticipation.fxml"));
        Parent root = loader.load();

        AffichageParticipationController controller = loader.getController();
        controller.setContentArea(contentArea);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(root);
    }

    @FXML
    void annuler(ActionEvent event) {
        try {
            naviguerVersAffichage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}