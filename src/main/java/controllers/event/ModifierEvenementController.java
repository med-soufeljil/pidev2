package controllers.event;

import models.event.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import services.event.EvenementService;
import services.event.CategorieEvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour la modification d'un événement existant
 * Permet de modifier tous les détails d'un événement
 */
public class ModifierEvenementController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private TextField tfTitre;                    // Champ titre
    @FXML private ComboBox<String> cbCategorie;         // Combo catégorie
    @FXML private DatePicker dpDateEvenement;           // Sélecteur date
    @FXML private TextField tfHeureDebut;               // Heure début
    @FXML private TextField tfHeureFin;                 // Heure fin
    @FXML private TextField tfLieu;                     // Lieu
    @FXML private TextField tfNombrePlacesMax;          // Places max
    @FXML private TextField tfNombreInscrits;           // Inscrits (lecture seule)
    @FXML private ComboBox<String> cbStatut;            // Statut
    @FXML private TextArea taDescription;               // Description
    @FXML private TextField tfIdCategorie;              // ID catégorie (caché)
    @FXML private Label lblValidation;                   // Message validation
    @FXML private Button btnModifier;                    // Bouton modifier
    @FXML private Button btnAnnuler;                     // Bouton annuler

    // ==================== ATTRIBUTS ====================
    private VBox contentArea;                            // Zone de contenu
    private Evenement evenementActuel;                   // Événement en cours de modification
    private EvenementService evenementService = new EvenementService();
    private CategorieEvenementService categorieService = new CategorieEvenementService();
    private Map<String, Integer> categoriesMap = new HashMap<>(); // Map nom catégorie → ID

    /**
     * Définit la zone de contenu principale
     * @param contentArea Le conteneur VBox
     */
    public void setContentArea(VBox contentArea) {
        System.out.println("ModifierEvenementController: contentArea set");
        this.contentArea = contentArea;
    }

    /**
     * Charge les données de l'événement à modifier dans le formulaire
     * @param evenement L'événement à modifier
     */
    public void chargerDonnees(Evenement evenement) {
        System.out.println("ModifierEvenementController: Charging event with ID: " + evenement.getIdEvenement());
        this.evenementActuel = evenement;

        // Charger les catégories d'abord (nécessaire pour la suite)
        chargerCategories();

        // Remplir les champs avec les données de l'événement
        tfTitre.setText(evenement.getTitre());
        dpDateEvenement.setValue(evenement.getDateEvenement());

        if (evenement.getHeureDebut() != null) {
            tfHeureDebut.setText(evenement.getHeureDebut().toString());
        }

        if (evenement.getHeureFin() != null) {
            tfHeureFin.setText(evenement.getHeureFin().toString());
        }

        tfLieu.setText(evenement.getLieu());
        tfNombrePlacesMax.setText(String.valueOf(evenement.getNombrePlacesMax()));
        tfNombreInscrits.setText(String.valueOf(evenement.getNombreInscrits()));
        tfNombreInscrits.setEditable(false); // Non modifiable
        tfNombreInscrits.setStyle("-fx-background-color: #f0f0f0;");

        cbStatut.setValue(evenement.getStatut());
        taDescription.setText(evenement.getDescription());

        // Stocker l'ID de la catégorie dans le champ caché
        tfIdCategorie.setText(String.valueOf(evenement.getIdCategorie()));

        System.out.println("ModifierEvenementController: Event loaded successfully");
    }

    /**
     * Charge les catégories depuis la base de données
     * Utilise des valeurs par défaut si la BDD échoue
     */
    private void chargerCategories() {
        try {
            // Récupérer les catégories depuis la base de données
            Map<Integer, String> dbCategories = categorieService.getCategoriesMap();

            if (dbCategories != null && !dbCategories.isEmpty()) {
                // Conversion Map<ID, Nom> → Map<Nom, ID> pour le ComboBox
                for (Map.Entry<Integer, String> entry : dbCategories.entrySet()) {
                    categoriesMap.put(entry.getValue(), entry.getKey());
                }
            } else {
                // Fallback: valeurs par défaut
                categoriesMap.put("team building", 1);
                categoriesMap.put("webinaire", 2);
                categoriesMap.put("conférence", 3);
            }

            // Remplir la ComboBox avec les noms des catégories
            ObservableList<String> categoriesNoms = FXCollections.observableArrayList(categoriesMap.keySet());
            cbCategorie.setItems(categoriesNoms);
            cbCategorie.setPromptText("Sélectionner une catégorie");

            // IMPORTANT: Sélectionner la catégorie actuelle après avoir rempli la ComboBox
            if (evenementActuel != null) {
                // Trouver le nom de la catégorie à partir de l'ID
                String nomCategorie = null;
                for (Map.Entry<String, Integer> entry : categoriesMap.entrySet()) {
                    if (entry.getValue().equals(evenementActuel.getIdCategorie())) {
                        nomCategorie = entry.getKey();
                        break;
                    }
                }

                if (nomCategorie != null) {
                    System.out.println("Setting category to: " + nomCategorie + " (ID: " + evenementActuel.getIdCategorie() + ")");
                    cbCategorie.setValue(nomCategorie);
                } else {
                    System.out.println("Category not found for ID: " + evenementActuel.getIdCategorie());
                }
            }

            // Écouteur pour mettre à jour l'ID caché quand l'utilisateur change la catégorie
            cbCategorie.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    Integer id = categoriesMap.get(newVal);
                    if (id != null) {
                        tfIdCategorie.setText(String.valueOf(id));
                        System.out.println("Category changed to: " + newVal + " with ID: " + id);
                    }
                }
                validateForm(); // Revalide le formulaire
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des catégories: " + e.getMessage(), Alert.AlertType.ERROR);

            // Fallback: valeurs par défaut
            categoriesMap.put("team building", 1);
            categoriesMap.put("webinaire", 2);
            categoriesMap.put("conférence", 3);
            cbCategorie.setItems(FXCollections.observableArrayList(categoriesMap.keySet()));
        }
    }

    /**
     * Initialise la liste des statuts possibles
     */
    private void initializeStatuts() {
        cbStatut.getItems().addAll(
                "planifié",
                "en cours",
                "terminé"
        );
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    /**
     * Action du bouton Modifier
     * Met à jour l'événement avec les nouvelles données
     * @param event L'événement de clic
     */
    @FXML
    void modifier(ActionEvent event) {
        System.out.println("ModifierEvenementController: modifier() called");

        try {
            // Validation du formulaire
            if (!validateForm()) {
                return;
            }

            // Vérifier que l'ID catégorie n'est pas vide
            if (tfIdCategorie.getText() == null || tfIdCategorie.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner une catégorie", Alert.AlertType.ERROR);
                return;
            }

            // Mise à jour de l'événement
            evenementActuel.setTitre(tfTitre.getText().trim());
            evenementActuel.setIdCategorie(Integer.parseInt(tfIdCategorie.getText()));
            evenementActuel.setDateEvenement(dpDateEvenement.getValue());

            // Gestion des heures (optionnelles)
            if (tfHeureDebut.getText() != null && !tfHeureDebut.getText().trim().isEmpty()) {
                try {
                    evenementActuel.setHeureDebut(LocalTime.parse(tfHeureDebut.getText().trim()));
                } catch (DateTimeParseException e) {
                    showAlert("Erreur", "Format d'heure de début invalide", Alert.AlertType.ERROR);
                    return;
                }
            } else {
                evenementActuel.setHeureDebut(null);
            }

            if (tfHeureFin.getText() != null && !tfHeureFin.getText().trim().isEmpty()) {
                try {
                    evenementActuel.setHeureFin(LocalTime.parse(tfHeureFin.getText().trim()));
                } catch (DateTimeParseException e) {
                    showAlert("Erreur", "Format d'heure de fin invalide", Alert.AlertType.ERROR);
                    return;
                }
            } else {
                evenementActuel.setHeureFin(null);
            }

            // Validation cohérence des heures
            if (evenementActuel.getHeureDebut() != null && evenementActuel.getHeureFin() != null) {
                if (evenementActuel.getHeureFin().isBefore(evenementActuel.getHeureDebut())) {
                    showAlert("Erreur", "L'heure de fin doit être après l'heure de début", Alert.AlertType.ERROR);
                    return;
                }
            }

            evenementActuel.setLieu(tfLieu.getText().trim());
            evenementActuel.setNombrePlacesMax(Integer.parseInt(tfNombrePlacesMax.getText().trim()));
            evenementActuel.setStatut(cbStatut.getValue());
            evenementActuel.setDescription(taDescription.getText().trim());

            // Sauvegarde en base de données
            System.out.println("ModifierEvenementController: Updating event ID: " + evenementActuel.getIdEvenement());
            evenementService.modifier(evenementActuel);
            System.out.println("ModifierEvenementController: Event updated successfully");

            showAlert("Succès", "Événement modifié avec succès !", Alert.AlertType.INFORMATION);

            // Retour à l'affichage
            naviguerVersAffichage();

        } catch (SQLException e) {
            System.err.println("ModifierEvenementController: SQL Error - " + e.getMessage());
            showAlert("Erreur", "Impossible de modifier l'événement: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("ModifierEvenementController: Number Format Error - " + e.getMessage());
            showAlert("Erreur", "Le nombre de places doit être un nombre valide", Alert.AlertType.ERROR);
        } catch (IOException e) {
            System.err.println("ModifierEvenementController: IO Error - " + e.getMessage());
            showAlert("Erreur", "Erreur de navigation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ModifierEvenementController: Unexpected Error - " + e.getMessage());
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Valide tous les champs du formulaire
     * @return true si le formulaire est valide, false sinon
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        // Validation Titre
        if (tfTitre.getText() == null || tfTitre.getText().trim().isEmpty()) {
            errors.append("• Le titre est obligatoire\n");
            isValid = false;
        } else if (tfTitre.getText().trim().length() < 3) {
            errors.append("• Le titre doit contenir au moins 3 caractères\n");
            isValid = false;
        }

        // Validation Catégorie
        if (cbCategorie.getValue() == null) {
            errors.append("• La catégorie est obligatoire\n");
            isValid = false;
        }

        // Validation Date
        if (dpDateEvenement.getValue() == null) {
            errors.append("• La date est obligatoire\n");
            isValid = false;
        }

        // Validation Lieu
        if (tfLieu.getText() == null || tfLieu.getText().trim().isEmpty()) {
            errors.append("• Le lieu est obligatoire\n");
            isValid = false;
        } else if (tfLieu.getText().trim().length() < 2) {
            errors.append("• Le lieu doit contenir au moins 2 caractères\n");
            isValid = false;
        }

        // Validation Nombre de places
        if (tfNombrePlacesMax.getText() == null || tfNombrePlacesMax.getText().trim().isEmpty()) {
            errors.append("• Le nombre de places est obligatoire\n");
            isValid = false;
        } else {
            try {
                int places = Integer.parseInt(tfNombrePlacesMax.getText().trim());
                if (places <= 0) {
                    errors.append("• Le nombre de places doit être positif\n");
                    isValid = false;
                } else if (places > 9999) {
                    errors.append("• Le nombre de places ne peut pas dépasser 9999\n");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                errors.append("• Le nombre de places doit être un nombre valide\n");
                isValid = false;
            }
        }

        // Validation Statut
        if (cbStatut.getValue() == null) {
            errors.append("• Le statut est obligatoire\n");
            isValid = false;
        }

        // Validation Heures (format)
        if (tfHeureDebut.getText() != null && !tfHeureDebut.getText().trim().isEmpty()) {
            try {
                LocalTime.parse(tfHeureDebut.getText().trim());
            } catch (DateTimeParseException e) {
                errors.append("• Format d'heure de début invalide (utilisez HH:MM)\n");
                isValid = false;
            }
        }

        if (tfHeureFin.getText() != null && !tfHeureFin.getText().trim().isEmpty()) {
            try {
                LocalTime.parse(tfHeureFin.getText().trim());
            } catch (DateTimeParseException e) {
                errors.append("• Format d'heure de fin invalide (utilisez HH:MM)\n");
                isValid = false;
            }
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

        return isValid;
    }

    /**
     * Navigation vers la page d'affichage des événements
     * @throws IOException si le fichier FXML n'est pas trouvé
     */
    private void naviguerVersAffichage() throws IOException {
        System.out.println("ModifierEvenementController: Navigating to display");

        if (contentArea != null) {
            // Charge la vue d'affichage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/AffichageEvenement.fxml"));
            Parent affichageView = loader.load();

            // Configure le contrôleur
            AffichageEvenementController controller = loader.getController();
            controller.setContentArea(contentArea);

            // Remplace le contenu actuel
            contentArea.getChildren().clear();
            contentArea.getChildren().add(affichageView);

            System.out.println("ModifierEvenementController: Navigation complete");
        } else {
            System.err.println("ModifierEvenementController: contentArea is null");
        }
    }

    /**
     * Action du bouton Annuler
     * Retourne à la liste sans sauvegarder
     * @param event L'événement de clic
     */
    @FXML
    void annuler(ActionEvent event) {
        System.out.println("ModifierEvenementController: annuler() called");
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