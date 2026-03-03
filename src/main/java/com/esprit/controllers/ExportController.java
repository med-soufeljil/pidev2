package com.esprit.controllers;

import com.esprit.services.ExportService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class ExportController {

    @FXML private RadioButton rbPDF;
    @FXML private RadioButton rbExcel;
    @FXML private RadioButton rbCSV;
    @FXML private Button btnExporter;
    @FXML private VBox contentArea;

    private ExportService exportService = new ExportService();
    private ToggleGroup toggleGroup = new ToggleGroup();

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        rbPDF.setToggleGroup(toggleGroup);
        rbExcel.setToggleGroup(toggleGroup);
        rbCSV.setToggleGroup(toggleGroup);
        rbPDF.setSelected(true);
    }

    @FXML
    private void handleExport() {
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

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}