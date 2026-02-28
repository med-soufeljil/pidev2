package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import services.AnalyticsService;
import services.ExternalApiService;
import services.PdfExportService;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

public class DashboardController {

    @FXML
    private Label lblCandidates, lblOffers, lblRecruitments, lblMeetings, lblApiResult, lblMarketJobs;
    @FXML
    private PieChart pieOffresType;
    @FXML
    private TextField txtApiName, txtMarketSkill;

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final PdfExportService pdfExportService = new PdfExportService();
    private final ExternalApiService externalApiService = new ExternalApiService();

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

    @FXML
    private void predictGender() {
        String name = txtApiName.getText();
        if (name == null || name.isBlank()) {
            lblApiResult.setText("Please type a first name.");
            return;
        }

        try {
            lblApiResult.setText(externalApiService.getGenderPrediction(name));
        } catch (Exception e) {
            lblApiResult.setText("Gender API unavailable: " + e.getMessage());
        }
    }

    @FXML
    private void predictNationality() {
        String name = txtApiName.getText();
        if (name == null || name.isBlank()) {
            lblApiResult.setText("Please type a first name.");
            return;
        }

        try {
            lblApiResult.setText(externalApiService.getNationalityPrediction(name));
        } catch (Exception e) {
            lblApiResult.setText("Nationality API unavailable: " + e.getMessage());
        }
    }


    @FXML
    private void loadMarketJobs() {
        String skill = txtMarketSkill.getText();
        if (skill == null || skill.isBlank()) {
            lblMarketJobs.setText("Please type a skill.");
            return;
        }
        try {
            lblMarketJobs.setText(externalApiService.fetchMarketJobs(skill));
        } catch (Exception e) {
            lblMarketJobs.setText("Jobs API unavailable: " + e.getMessage());
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