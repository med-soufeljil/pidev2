package controllers.event;

import models.event.Participation;
import models.event.Evenement;
import services.event.ParticipationService;
import services.event.EvenementService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur pour la modification d'une participation existante
 * Permet de modifier les détails d'une inscription à un événement
 */
public class ModifierParticipationController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private ComboBox<String> cbEvenement;        // Liste des événements
    @FXML private DatePicker dpDateInscription;        // Date d'inscription
    @FXML private ComboBox<String> cbStatut;           // Statut de la participation
    @FXML private CheckBox chkPresence;                // Présence (oui/non)
    @FXML private TextField tfIdEvenementCache;        // Champ caché pour ID événement
    @FXML private Label lblValidation;                  // Message de validation
    @FXML private Button btnModifier;                   // Bouton modifier
    @FXML private Button btnAnnuler;                    // Bouton annuler
    @FXML private Label lblEvenementInfo;               // Infos supplémentaires sur l'événement

    // ==================== ATTRIBUTS ====================
    private VBox contentArea;                            // Zone de contenu
    private Participation participationActuelle;         // Participation en cours de modification
    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService evenementService = new EvenementService();

    /**
     * Définit la zone de contenu principale
     * @param contentArea Le conteneur VBox
     */
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    /**
     * Charge les données de la participation à modifier
     * @param participation La participation à modifier
     */
    public void chargerDonnees(Participation participation) {
        this.participationActuelle = participation;

        chargerEvenements(); // Charge la liste des événements

        // Pré-sélectionner l'événement correspondant
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

        // Remplir les autres champs
        dpDateInscription.setValue(participation.getDateInscription());
        cbStatut.setValue(participation.getStatut());
        chkPresence.setSelected(participation.isPresence());
    }

    /**
     * Initialisation du contrôleur
     * Configure les statuts et la validation
     */
    @FXML
    public void initialize() {
        initializeStatuts();
        setupRealTimeValidation();
    }

    /**
     * Charge la liste des événements depuis la base de données
     */
    private void chargerEvenements() {
        try {
            List<Evenement> events = evenementService.recuperer();
            for (Evenement e : events) {
                cbEvenement.getItems().add(e.getIdEvenement() + " - " + e.getTitre() +
                        " (" + e.getNombreInscrits() + "/" + e.getNombrePlacesMax() + ")");
            }

            // Écouteur pour le changement de sélection
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
                validateForm(); // Revalide le formulaire
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les événements", Alert.AlertType.ERROR);
        }
    }

    /**
     * Initialise la liste des statuts possibles
     */
    private void initializeStatuts() {
        cbStatut.getItems().addAll("confirme", "en attente", "annule");
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    /**
     * Configure la validation en temps réel
     */
    private void setupRealTimeValidation() {
        cbEvenement.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        dpDateInscription.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    /**
     * Valide tous les champs du formulaire
     */
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

        // Affichage des erreurs
        if (!isValid) {
            lblValidation.setText(errors.toString());
            lblValidation.setStyle("-fx-text-fill: #d32f2f;"); // Rouge
            lblValidation.setVisible(true);
        } else {
            lblValidation.setText("✓ Tous les champs sont valides");
            lblValidation.setStyle("-fx-text-fill: #2e7d32;"); // Vert
            lblValidation.setVisible(true);
        }

        btnModifier.setDisable(!isValid);
    }

    /**
     * Action du bouton Modifier
     * Met à jour la participation avec les nouvelles données
     * @param event L'événement de clic
     */
    @FXML
    void modifier(ActionEvent event) {
        try {
            // Vérification événement sélectionné
            if (tfIdEvenementCache.getText() == null || tfIdEvenementCache.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner un événement", Alert.AlertType.ERROR);
                return;
            }

            int oldEventId = participationActuelle.getId_e();
            int newEventId = Integer.parseInt(tfIdEvenementCache.getText());

            // Mise à jour des données
            participationActuelle.setId_e(newEventId);
            participationActuelle.setDateInscription(dpDateInscription.getValue());
            participationActuelle.setStatut(cbStatut.getValue());
            participationActuelle.setPresence(chkPresence.isSelected());

            // Sauvegarde en base de données
            participationService.modifier(participationActuelle);

            showAlert("Succès", "Participation modifiée avec succès !", Alert.AlertType.INFORMATION);
            naviguerVersAffichage();

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();

            // Messages d'erreur plus explicites
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

    /**
     * Navigation vers la page d'affichage des participations
     * @throws IOException si le fichier FXML n'est pas trouvé
     */
    private void naviguerVersAffichage() throws IOException {
        // Charge la vue d'affichage
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/AffichageParticipation.fxml"));
        Parent root = loader.load();

        // Configure le contrôleur
        AffichageParticipationController controller = loader.getController();
        controller.setContentArea(contentArea);

        // Remplace le contenu actuel
        contentArea.getChildren().clear();
        contentArea.getChildren().add(root);
    }

    /**
     * Action du bouton Annuler
     * Retourne à la liste sans sauvegarder
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