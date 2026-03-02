package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import services.AnalyticsService;
import services.PdfExportService;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

public class DashboardController {

    @FXML
    private Label lblCandidates, lblOffers, lblRecruitments, lblMeetings;
    @FXML
    private PieChart pieOffresType, pieApplicationsStatut;
    @FXML
    private BarChart<String, Number> barApplicationsByOffer;

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final PdfExportService pdfExportService = new PdfExportService();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    @FXML
    private void refreshDashboard() {
        try {
            lblCandidates.setText(String.valueOf(analyticsService.countCandidats()));
            lblOffers.setText(String.valueOf(analyticsService.countOffres()));
            lblRecruitments.setText(String.valueOf(analyticsService.countRecrutements()));
            lblMeetings.setText(String.valueOf(analyticsService.countReunions()));

            pieOffresType.getData().clear();
            for (Map.Entry<String, Integer> entry : analyticsService.offresParType().entrySet()) {
                pieOffresType.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            pieApplicationsStatut.getData().clear();
            for (Map.Entry<String, Integer> entry : analyticsService.applicationsParStatut().entrySet()) {
                pieApplicationsStatut.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<String, Integer> entry : analyticsService.applicationsParOffre().entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            barApplicationsByOffer.setData(FXCollections.observableArrayList(series));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Dashboard", e.getMessage());
        }
    }

    @FXML
    private void exportPdf() {
        try {
            Path exported = pdfExportService.exportGlobalReport(Path.of("recruitment_report.pdf"));
            showAlert(Alert.AlertType.INFORMATION, "PDF Export", "Report exported: " + exported.toAbsolutePath());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "PDF Export", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
