package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.AnalyticsService;
import services.PdfExportService;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

public class RecruitmentDashboardController {
    @FXML private Label lblCandidates;
    @FXML private Label lblOffers;
    @FXML private Label lblRecruitments;
    @FXML private Label lblMeetings;
    @FXML private PieChart pieOffresType;
    @FXML private PieChart pieApplicationsStatut;
    @FXML private BarChart<String, Number> barApplicationsByOffer;

    private final AnalyticsService analyticsService = new AnalyticsService();

    @FXML
    public void initialize() {
        try {
            lblCandidates.setText(String.valueOf(analyticsService.countCandidats()));
            lblOffers.setText(String.valueOf(analyticsService.countOffres()));
            lblRecruitments.setText(String.valueOf(analyticsService.countRecrutements()));
            lblMeetings.setText(String.valueOf(analyticsService.countReunions()));

            pieOffresType.setData(FXCollections.observableArrayList());
            for (Map.Entry<String, Integer> e : analyticsService.offresParType().entrySet()) {
                pieOffresType.getData().add(new PieChart.Data(e.getKey(), e.getValue()));
            }

            pieApplicationsStatut.setData(FXCollections.observableArrayList());
            for (Map.Entry<String, Integer> e : analyticsService.applicationsParStatut().entrySet()) {
                pieApplicationsStatut.getData().add(new PieChart.Data(e.getKey(), e.getValue()));
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<String, Integer> e : analyticsService.applicationsParOffre().entrySet()) {
                series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
            }
            barApplicationsByOffer.setData(FXCollections.observableArrayList(series));
        } catch (SQLException e) {
            showError("Dashboard recrutement", e.getMessage());
        }
    }

    @FXML
    public void exportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter rapport PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = chooser.showSaveDialog(getStage());
        if (file == null) return;
        try {
            new PdfExportService().exportGlobalReport(Path.of(file.toURI()));
        } catch (Exception e) {
            showError("Export PDF", e.getMessage());
        }
    }

    private Stage getStage() {
        return (Stage) lblCandidates.getScene().getWindow();
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
