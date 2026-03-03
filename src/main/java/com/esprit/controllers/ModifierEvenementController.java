package com.esprit.controllers;

import com.esprit.models.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import com.esprit.services.EvenementService;
import com.esprit.services.CategorieEvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class ModifierEvenementController {

    @FXML private TextField tfTitre;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private DatePicker dpDateEvenement;
    @FXML private TextField tfHeureDebut;
    @FXML private TextField tfHeureFin;
    @FXML private TextField tfLieu;
    @FXML private TextField tfNombrePlacesMax;
    @FXML private TextField tfNombreInscrits;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextArea taDescription;
    @FXML private TextField tfIdCategorie;
    @FXML private Label lblValidation;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;

    private VBox contentArea;
    private Evenement evenementActuel;
    private EvenementService evenementService = new EvenementService();
    private CategorieEvenementService categorieService = new CategorieEvenementService();
    private Map<String, Integer> categoriesMap = new HashMap<>(); // Changed to String->Integer for easier lookup

    public void setContentArea(VBox contentArea) {
        System.out.println("ModifierEvenementController: contentArea set");
        this.contentArea = contentArea;
    }

    public void chargerDonnees(Evenement evenement) {
        System.out.println("ModifierEvenementController: Charging event with ID: " + evenement.getIdEvenement());
        this.evenementActuel = evenement;

        // Charger les catégories d'abord
        chargerCategories();

        // Remplir les champs
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
        tfNombreInscrits.setEditable(false);
        tfNombreInscrits.setStyle("-fx-background-color: #f0f0f0;");

        cbStatut.setValue(evenement.getStatut());
        taDescription.setText(evenement.getDescription());

        // Initialiser les statuts
        initializeStatuts();

        // IMPORTANT: Set the category ID in the hidden field
        tfIdCategorie.setText(String.valueOf(evenement.getIdCategorie()));

        System.out.println("ModifierEvenementController: Event loaded successfully");
    }

    private void chargerCategories() {
        try {
            // Récupérer les catégories depuis la base de données
            Map<Integer, String> dbCategories = categorieService.getCategoriesMap();

            if (dbCategories != null && !dbCategories.isEmpty()) {
                // Convert to String->Integer map for easy lookup
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

            // Set prompt text
            cbCategorie.setPromptText("Sélectionner une catégorie");

            // IMPORTANT: Select the current category after populating the ComboBox
            if (evenementActuel != null) {
                // Find the category name from the ID
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

            // Listener pour mettre à jour l'ID caché quand l'utilisateur change la catégorie
            cbCategorie.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    Integer id = categoriesMap.get(newVal);
                    if (id != null) {
                        tfIdCategorie.setText(String.valueOf(id));
                        System.out.println("Category changed to: " + newVal + " with ID: " + id);
                    }
                }
                validateForm();
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

    private void initializeStatuts() {
        cbStatut.getItems().addAll(
                "planifié",
                "en cours",
                "terminé"
        );
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    @FXML
    void modifier(ActionEvent event) {
        System.out.println("ModifierEvenementController: modifier() called");

        try {
            // Validation
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

            // Gestion des heures
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

            // Sauvegarde
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

        return isValid;
    }

    private void naviguerVersAffichage() throws IOException {
        System.out.println("ModifierEvenementController: Navigating to display");

        if (contentArea != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageEvenement.fxml"));
            Parent affichageView = loader.load();

            AffichageEvenementController controller = loader.getController();
            controller.setContentArea(contentArea);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(affichageView);

            System.out.println("ModifierEvenementController: Navigation complete");
        } else {
            System.err.println("ModifierEvenementController: contentArea is null");
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        System.out.println("ModifierEvenementController: annuler() called");
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