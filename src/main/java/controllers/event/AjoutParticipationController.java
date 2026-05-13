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
 * Contrôleur pour l'ajout d'une nouvelle participation
 * Gère le formulaire d'inscription à un événement
 */
public class AjoutParticipationController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private ComboBox<String> cbEvenement;        // Liste déroulante des événements
    @FXML private DatePicker dpDateInscription;        // Sélecteur de date
    @FXML private ComboBox<String> cbStatut;           // Statut de la participation
    @FXML private CheckBox chkPresence;                // Case à cocher présence
    @FXML private TextField tfIdEvenementCache;        // Champ caché pour l'ID événement
    @FXML private Label lblValidation;                  // Message de validation
    @FXML private Button btnAjouter;                    // Bouton d'ajout
    @FXML private Button btnAnnuler;                    // Bouton d'annulation

    // ==================== ATTRIBUTS ====================
    private VBox contentArea;                           // Zone de contenu principale
    private Evenement eventSelectionne;                  // Événement sélectionné (optionnel)
    private final ParticipationService participationService = new ParticipationService(); // Service participation
    private final EvenementService evenementService = new EvenementService();           // Service événement

    /**
     * Définit la zone de contenu principale
     * @param contentArea Le conteneur VBox
     */
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    /**
     * Définit l'événement à pré-sélectionner
     * Utilisé quand on vient de la vue des participations d'un événement spécifique
     * @param event L'événement sélectionné
     */
    public void setEvent(Evenement event) {
        this.eventSelectionne = event;
        if (event != null) {
            // Pré-sélectionner l'événement dans le ComboBox
            cbEvenement.setValue(event.getIdEvenement() + " - " + event.getTitre());
            cbEvenement.setDisable(true); // Désactiver le choix (forcé)
            tfIdEvenementCache.setText(String.valueOf(event.getIdEvenement()));
        }
    }

    /**
     * Initialisation du contrôleur
     * Charge les événements, configure les statuts et la validation
     */
    @FXML
    public void initialize() {
        System.out.println("AjoutParticipationController: initialize()");
        try {
            chargerEvenements();          // Charge la liste des événements
            initializeStatuts();           // Initialise les statuts possibles
            dpDateInscription.setValue(LocalDate.now()); // Date par défaut = aujourd'hui
            setupRealTimeValidation();      // Validation en temps réel
            btnAjouter.setDisable(true);    // Bouton désactivé par défaut
        } catch (Exception e) {
            showAlert("Erreur", "Erreur d'initialisation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Charge la liste des événements depuis la base de données
     * Remplit le ComboBox avec "ID - Titre"
     */
    private void chargerEvenements() {
        try {
            List<Evenement> events = evenementService.recuperer();
            for (Evenement e : events) {
                cbEvenement.getItems().add(e.getIdEvenement() + " - " + e.getTitre());
            }

            // Écouteur pour mettre à jour l'ID événement caché
            cbEvenement.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    int eventId = Integer.parseInt(newVal.split(" - ")[0]);
                    tfIdEvenementCache.setText(String.valueOf(eventId));
                } else {
                    tfIdEvenementCache.clear();
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
     * Utilise des valeurs sans accents pour éviter les problèmes d'encodage
     */
    private void initializeStatuts() {
        cbStatut.getItems().addAll("confirme", "en attente", "annule");
        cbStatut.setValue("confirme"); // Valeur par défaut
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    /**
     * Configure la validation en temps réel sur tous les champs
     */
    private void setupRealTimeValidation() {
        cbEvenement.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        dpDateInscription.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        chkPresence.selectedProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    /**
     * Valide tous les champs du formulaire
     * Affiche les messages d'erreur et active/désactive le bouton d'ajout
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

        btnAjouter.setDisable(!isValid);
    }

    /**
     * Action du bouton Ajouter
     * Crée une nouvelle participation et l'enregistre en base de données
     * @param event L'événement de clic
     */
    @FXML
    void ajouter(ActionEvent event) {
        try {
            // Vérification événement sélectionné
            if (tfIdEvenementCache.getText() == null || tfIdEvenementCache.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner un événement", Alert.AlertType.ERROR);
                return;
            }

            // Récupération des données
            int idEvent = Integer.parseInt(tfIdEvenementCache.getText());
            LocalDate dateInscription = dpDateInscription.getValue();
            String statut = cbStatut.getValue();
            boolean presence = chkPresence.isSelected();

            // Création de l'objet Participation
            Participation participation = new Participation(idEvent, dateInscription, statut, presence);

            // Sauvegarde en base de données
            participationService.ajouter(participation);

            showAlert("Succès", "Participation ajoutée avec succès !", Alert.AlertType.INFORMATION);
            naviguerVersAffichage();

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();

            // Messages d'erreur plus explicites selon le type d'erreur
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