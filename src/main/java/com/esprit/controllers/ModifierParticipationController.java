package com.esprit.controllers;

import com.esprit.models.Participation;
import com.esprit.models.Evenement;
import com.esprit.services.ParticipationService;
import com.esprit.services.EvenementService;
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

public class ModifierParticipationController {

    @FXML private ComboBox<String> cbEvenement;
    @FXML private DatePicker dpDateInscription;
    @FXML private ComboBox<String> cbStatut;
    @FXML private CheckBox chkPresence;
    @FXML private TextField tfIdEvenementCache;
    @FXML private Label lblValidation;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;
    @FXML private Label lblEvenementInfo; // Nouveau label pour afficher l'info de l'événement

    private VBox contentArea;
    private Participation participationActuelle;
    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService evenementService = new EvenementService();

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    public void chargerDonnees(Participation participation) {
        this.participationActuelle = participation;

        chargerEvenements();

        // Pré-sélectionner l'événement
        try {
            List<Evenement> events = evenementService.recuperer();
            for (Evenement e : events) {
                if (e.getIdEvenement() == participation.getId_e()) {
                    cbEvenement.setValue(e.getIdEvenement() + " - " + e.getTitre());
                    tfIdEvenementCache.setText(String.valueOf(e.getIdEvenement()));

                    // Afficher des informations supplémentaires sur l'événement
                    if (lblEvenementInfo != null) {
                        lblEvenementInfo.setText("Date: " + e.getDateEvenement() +
                                " | Lieu: " + e.getLieu() +
                                " | Places: " + e.getNombreInscrits() + "/" + e.getNombrePlacesMax());
                    }
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dpDateInscription.setValue(participation.getDateInscription());
        cbStatut.setValue(participation.getStatut());
        chkPresence.setSelected(participation.isPresence());
    }

    @FXML
    public void initialize() {
        initializeStatuts();
        setupRealTimeValidation();
    }

    private void chargerEvenements() {
        try {
            List<Evenement> events = evenementService.recuperer();
            for (Evenement e : events) {
                cbEvenement.getItems().add(e.getIdEvenement() + " - " + e.getTitre() +
                        " (" + e.getNombreInscrits() + "/" + e.getNombrePlacesMax() + ")");
            }

            cbEvenement.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    int eventId = Integer.parseInt(newVal.split(" - ")[0]);
                    tfIdEvenementCache.setText(String.valueOf(eventId));

                    // Mettre à jour les infos de l'événement sélectionné
                    try {
                        List<Evenement> events2 = evenementService.recuperer();
                        for (Evenement e : events2) {
                            if (e.getIdEvenement() == eventId) {
                                if (lblEvenementInfo != null) {
                                    lblEvenementInfo.setText("Date: " + e.getDateEvenement() +
                                            " | Lieu: " + e.getLieu() +
                                            " | Places: " + e.getNombreInscrits() + "/" + e.getNombrePlacesMax());
                                }
                                break;
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                validateForm();
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les événements", Alert.AlertType.ERROR);
        }
    }

    private void initializeStatuts() {
        cbStatut.getItems().addAll("confirme", "en attente", "annule");
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void setupRealTimeValidation() {
        cbEvenement.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        dpDateInscription.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void validateForm() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        if (cbEvenement.getValue() == null) {
            errors.append("• L'événement est obligatoire\n");
            isValid = false;
        }

        if (dpDateInscription.getValue() == null) {
            errors.append("• La date d'inscription est obligatoire\n");
            isValid = false;
        } else if (dpDateInscription.getValue().isAfter(LocalDate.now())) {
            errors.append("• La date d'inscription ne peut pas être dans le futur\n");
            isValid = false;
        }

        if (cbStatut.getValue() == null) {
            errors.append("• Le statut est obligatoire\n");
            isValid = false;
        }

        if (!isValid) {
            lblValidation.setText(errors.toString());
            lblValidation.setStyle("-fx-text-fill: #d32f2f;");
            lblValidation.setVisible(true);
        } else {
            lblValidation.setText("✓ Tous les champs sont valides");
            lblValidation.setStyle("-fx-text-fill: #2e7d32;");
            lblValidation.setVisible(true);
        }

        btnModifier.setDisable(!isValid);
    }

    @FXML
    void modifier(ActionEvent event) {
        try {
            if (tfIdEvenementCache.getText() == null || tfIdEvenementCache.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner un événement", Alert.AlertType.ERROR);
                return;
            }

            int oldEventId = participationActuelle.getId_e();
            int newEventId = Integer.parseInt(tfIdEvenementCache.getText());

            participationActuelle.setId_e(newEventId);
            participationActuelle.setDateInscription(dpDateInscription.getValue());
            participationActuelle.setStatut(cbStatut.getValue());
            participationActuelle.setPresence(chkPresence.isSelected());

            participationService.modifier(participationActuelle);

            showAlert("Succès", "Participation modifiée avec succès !", Alert.AlertType.INFORMATION);
            naviguerVersAffichage();

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();

            String errorMessage = "Erreur base de données: " + e.getMessage();
            if (e.getMessage().contains("Data truncated")) {
                errorMessage = "La valeur du statut est trop longue. Utilisez 'confirme' au lieu de 'confirmé'.";
            }
            showAlert("Erreur", errorMessage, Alert.AlertType.ERROR);
        } catch (IOException e) {
            showAlert("Erreur", "Erreur de navigation", Alert.AlertType.ERROR);
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