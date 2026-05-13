package controllers.event;

import services.event.ExportService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Contrôleur pour l'exportation de données
 * Permet de choisir le format et d'exporter les événements/statistiques
 */
public class ExportController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private RadioButton rbPDF;           // Option PDF
    @FXML private RadioButton rbExcel;         // Option Excel
    @FXML private RadioButton rbCSV;           // Option CSV
    @FXML private Button btnExporter;          // Bouton d'export
    @FXML private VBox contentArea;             // Zone de contenu

    // ==================== ATTRIBUTS ====================
    private ExportService exportService = new ExportService(); // Service d'export
    private ToggleGroup toggleGroup = new ToggleGroup();       // Groupe de boutons radio

    /**
     * Définit la zone de contenu principale
     * @param contentArea Le conteneur VBox
     */
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    /**
     * Initialisation du contrôleur
     * Configure les boutons radio
     */
    @FXML
    public void initialize() {
        // Ajoute les boutons au même groupe (sélection unique)
        rbPDF.setToggleGroup(toggleGroup);
        rbExcel.setToggleGroup(toggleGroup);
        rbCSV.setToggleGroup(toggleGroup);
        rbPDF.setSelected(true); // PDF par défaut
    }

    /**
     * Action du bouton Exporter
     * Ouvre un FileChooser puis exporte selon le format choisi
     */
    @FXML
    private void handleExport() {
        // Configuration du FileChooser selon le format
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les données");

        RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();

        if (selected == rbPDF) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf")
            );
            fileChooser.setInitialFileName("evenements.pdf");
        } else if (selected == rbExcel) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx")
            );
            fileChooser.setInitialFileName("statistiques.xlsx");
        } else {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichier CSV", "*.csv")
            );
            fileChooser.setInitialFileName("evenements.csv");
        }

        // Ouvre la boîte de dialogue de sauvegarde
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                // Appel au service d'export selon le format choisi
                if (selected == rbPDF) {
                    exportService.exportEvenementsPDF(file.getAbsolutePath());
                    showAlert("Succès", "Export PDF réussi !\nFichier: " + file.getName());
                } else if (selected == rbExcel) {
                    exportService.exportStatistiquesExcel(file.getAbsolutePath());
                    showAlert("Succès", "Export Excel réussi !\nFichier: " + file.getName());
                } else {
                    exportService.exportEvenementsCSV(file.getAbsolutePath());
                    showAlert("Succès", "Export CSV réussi !\nFichier: " + file.getName());
                }
            } catch (Exception e) {
                showAlert("Erreur", "Échec de l'export: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Affiche une alerte de succès
     * @param title Titre
     * @param message Message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}