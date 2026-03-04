package org.soa.tp1.pi_dev_s2.com.esprit.controllers;

import org.soa.tp1.pi_dev_s2.com.esprit.models.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.soa.tp1.pi_dev_s2.com.esprit.services.EvenementService;
import org.soa.tp1.pi_dev_s2.com.esprit.services.CategorieEvenementService;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class AjoutEvenementController {

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
    @FXML private Button btnAjouter;
    @FXML private Button btnAnnuler;

    private VBox contentArea;
    private EvenementService evenementService = new EvenementService();
    private CategorieEvenementService categorieService = new CategorieEvenementService();
    private Map<String, Integer> categoriesMap = new HashMap<>();

    public void setContentArea(VBox contentArea) {
        System.out.println("AjoutEvenementController: contentArea set to " + contentArea);
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        System.out.println("AjoutEvenementController: initialize() called");
        try {
            chargerCategories();
            initializeStatuts();
            tfNombreInscrits.setText("0");
            tfNombreInscrits.setEditable(false);
            tfNombreInscrits.setStyle("-fx-background-color: #f0f0f0;");
            setupRealTimeValidation();
            btnAjouter.setDisable(true);

            // Set default date to today
            dpDateEvenement.setValue(LocalDate.now());
        } catch (Exception e) {
            showAlert("Erreur d'initialisation",
                    "Impossible de charger les données: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void chargerCategories() {
        try {
            Map<Integer, String> dbCategories = categorieService.getCategoriesMap();

            if (dbCategories != null && !dbCategories.isEmpty()) {
                for (Map.Entry<Integer, String> entry : dbCategories.entrySet()) {
                    categoriesMap.put(entry.getValue(), entry.getKey());
                }
            } else {
                categoriesMap.put("team building", 1);
                categoriesMap.put("webinaire", 2);
                categoriesMap.put("conference", 3);
            }

            ObservableList<String> categoriesNoms = FXCollections.observableArrayList(categoriesMap.keySet());
            cbCategorie.setItems(categoriesNoms);
            cbCategorie.setPromptText("Sélectionner une catégorie");

            cbCategorie.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    Integer id = categoriesMap.get(newVal);
                    if (id != null) {
                        tfIdCategorie.setText(String.valueOf(id));
                        System.out.println("Selected category: " + newVal + " with ID: " + id);
                    }
                } else {
                    tfIdCategorie.clear();
                }
                validateForm();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des catégories: " + e.getMessage(), Alert.AlertType.ERROR);

            categoriesMap.put("team building", 1);
            categoriesMap.put("webinaire", 2);
            categoriesMap.put("conference", 3);
            cbCategorie.setItems(FXCollections.observableArrayList(categoriesMap.keySet()));
        }
    }

    private void initializeStatuts() {
        cbStatut.getItems().addAll(
                "planifié",
                "en cours",
                "terminé"
        );
        cbStatut.setValue("planifié");
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void setupRealTimeValidation() {
        tfTitre.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        cbCategorie.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        dpDateEvenement.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        tfLieu.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        tfNombrePlacesMax.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        tfHeureDebut.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        tfHeureFin.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        taDescription.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void validateForm() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        if (tfTitre.getText() == null || tfTitre.getText().trim().isEmpty()) {
            errors.append("• Le titre est obligatoire\n");
            isValid = false;
        } else if (tfTitre.getText().trim().length() < 3) {
            errors.append("• Le titre doit contenir au moins 3 caractères\n");
            isValid = false;
        } else if (tfTitre.getText().trim().length() > 100) {
            errors.append("• Le titre ne peut pas dépasser 100 caractères\n");
            isValid = false;
        }

        if (cbCategorie.getValue() == null) {
            errors.append("• La catégorie est obligatoire\n");
            isValid = false;
        }

        if (dpDateEvenement.getValue() == null) {
            errors.append("• La date est obligatoire\n");
            isValid = false;
        } else if (dpDateEvenement.getValue().isBefore(LocalDate.now())) {
            errors.append("• La date ne peut pas être dans le passé\n");
            isValid = false;
        }

        if (tfLieu.getText() == null || tfLieu.getText().trim().isEmpty()) {
            errors.append("• Le lieu est obligatoire\n");
            isValid = false;
        } else if (tfLieu.getText().trim().length() < 2) {
            errors.append("• Le lieu doit contenir au moins 2 caractères\n");
            isValid = false;
        }

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
                errors.append("• Le nombre de places doit être un nombre entier valide\n");
                isValid = false;
            }
        }

        if (cbStatut.getValue() == null) {
            errors.append("• Le statut est obligatoire\n");
            isValid = false;
        }

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

        if (isValidHeureFormat()) {
            if (tfHeureDebut.getText() != null && !tfHeureDebut.getText().trim().isEmpty() &&
                    tfHeureFin.getText() != null && !tfHeureFin.getText().trim().isEmpty()) {

                LocalTime debut = LocalTime.parse(tfHeureDebut.getText().trim());
                LocalTime fin = LocalTime.parse(tfHeureFin.getText().trim());

                if (fin.isBefore(debut)) {
                    errors.append("• L'heure de fin doit être après l'heure de début\n");
                    isValid = false;
                }
            }
        }

        if (taDescription.getText() != null && taDescription.getText().length() > 1000) {
            errors.append("• La description ne peut pas dépasser 1000 caractères\n");
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

        btnAjouter.setDisable(!isValid);
    }

    private boolean isValidHeureFormat() {
        try {
            if (tfHeureDebut.getText() != null && !tfHeureDebut.getText().trim().isEmpty()) {
                LocalTime.parse(tfHeureDebut.getText().trim());
            }
            if (tfHeureFin.getText() != null && !tfHeureFin.getText().trim().isEmpty()) {
                LocalTime.parse(tfHeureFin.getText().trim());
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @FXML
    void ajouter(ActionEvent event) {
        System.out.println("AjoutEvenementController: ajouter() called");

        try {
            if (tfIdCategorie.getText() == null || tfIdCategorie.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner une catégorie", Alert.AlertType.ERROR);
                return;
            }

            String titre = tfTitre.getText().trim();
            int idCategorie = Integer.parseInt(tfIdCategorie.getText());
            LocalDate dateEvenement = dpDateEvenement.getValue();

            LocalTime heureDebut = null;
            LocalTime heureFin = null;

            if (tfHeureDebut.getText() != null && !tfHeureDebut.getText().trim().isEmpty()) {
                heureDebut = LocalTime.parse(tfHeureDebut.getText().trim());
            }

            if (tfHeureFin.getText() != null && !tfHeureFin.getText().trim().isEmpty()) {
                heureFin = LocalTime.parse(tfHeureFin.getText().trim());
            }

            String lieu = tfLieu.getText().trim();
            int nombrePlacesMax = Integer.parseInt(tfNombrePlacesMax.getText().trim());
            int nombreInscrits = 0;
            String statut = cbStatut.getValue();
            String description = taDescription.getText() != null ? taDescription.getText().trim() : "";

            Evenement evenement = new Evenement(
                    0, titre, idCategorie, dateEvenement,
                    heureDebut, heureFin, lieu, nombrePlacesMax,
                    nombreInscrits, statut, description
            );

            System.out.println("AjoutEvenementController: Saving event: " + evenement.getTitre());
            evenementService.ajouter(evenement);
            System.out.println("AjoutEvenementController: Event saved successfully");

            showAlert("Succès", "Événement créé avec succès !", Alert.AlertType.INFORMATION);
            naviguerVersAffichage();

        } catch (SQLException e) {
            System.err.println("AjoutEvenementController: SQL Error - " + e.getMessage());
            showAlert("Erreur base de données", "Impossible de créer l'événement: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("AjoutEvenementController: Number Format Error - " + e.getMessage());
            showAlert("Erreur de format", "Le nombre de places doit être un nombre valide.", Alert.AlertType.ERROR);
        } catch (DateTimeParseException e) {
            System.err.println("AjoutEvenementController: DateTime Parse Error - " + e.getMessage());
            showAlert("Erreur de format d'heure", "Utilisez le format HH:MM (exemple: 14:30)", Alert.AlertType.ERROR);
        } catch (IOException e) {
            System.err.println("AjoutEvenementController: IO Error - " + e.getMessage());
            showAlert("Erreur de navigation", "Impossible de charger la page d'affichage: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("AjoutEvenementController: Unexpected Error - " + e.getMessage());
            showAlert("Erreur inattendue", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Navigate to the event display page
     */
    private void naviguerVersAffichage() throws IOException {
        System.out.println("AjoutEvenementController: naviguerVersAffichage() called");
        System.out.println("AjoutEvenementController: contentArea = " + contentArea);

        if (contentArea == null) {
            System.err.println("AjoutEvenementController: ERREUR - contentArea est null!");
            showAlert("Erreur", "Erreur de navigation: contentArea non initialisé", Alert.AlertType.ERROR);
            return;
        }

        System.out.println("AjoutEvenementController: Loading AffichageEvenement.fxml");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageEvenement.fxml"));

        if (loader.getLocation() == null) {
            System.err.println("AjoutEvenementController: ERROR - Cannot find /AffichageEvenement.fxml");
            showAlert("Erreur", "Fichier FXML non trouvé: /AffichageEvenement.fxml", Alert.AlertType.ERROR);
            return;
        }

        System.out.println("AjoutEvenementController: FXML location: " + loader.getLocation());

        Parent affichageView = loader.load();
        System.out.println("AjoutEvenementController: FXML loaded successfully");

        AffichageEvenementController controller = loader.getController();

        if (controller == null) {
            System.err.println("AjoutEvenementController: ERROR - Controller is null");
            return;
        }

        System.out.println("AjoutEvenementController: Setting contentArea on AffichageEvenementController");
        controller.setContentArea(contentArea);

        System.out.println("AjoutEvenementController: Replacing content");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(affichageView);

        System.out.println("AjoutEvenementController: Navigation complete");
    }

    @FXML
    void annuler(ActionEvent event) {
        System.out.println("AjoutEvenementController: annuler() called");
        try {
            naviguerVersAffichage(); // Retour vers l'affichage
        } catch (IOException e) {
            System.err.println("AjoutEvenementController: Erreur lors de l'annulation - " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à l'affichage: " + e.getMessage(), Alert.AlertType.ERROR);
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