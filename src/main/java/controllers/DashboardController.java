package controllers;

import dto.DashboardStats;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.DashboardService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import utils.ApiRuntime;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;

public class DashboardController {

    @FXML private Label lblTotalFormations;
    @FXML private Label lblTotalApprenants;
    @FXML private Label lblAvgDuree;
    @FXML private Label lblCertif;
    @FXML private PieChart pieChart;

    private final DashboardService dashboardService = new DashboardService();
    private DashboardStats stats;

    @FXML
    public void initialize() {
        ApiRuntime.ensureStarted();
        refresh();
    }

    @FXML
    public void refresh() {
        try {
            stats = dashboardService.loadStats();
            lblTotalFormations.setText(String.valueOf(stats.getTotalFormations()));
            lblTotalApprenants.setText(String.valueOf(stats.getTotalApprenants()));
            lblAvgDuree.setText(String.format("%.2f h", stats.getAverageDuration()));
            lblCertif.setText(String.valueOf(stats.getCertifiedFormations()));
            int nonCertif = Math.max(0, stats.getTotalFormations() - stats.getCertifiedFormations());
            pieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Certifiées", stats.getCertifiedFormations()),
                    new PieChart.Data("Non certifiées", nonCertif)
            ));
        } catch (SQLException e) {
            error("Chargement dashboard", e.getMessage());
        }
    }

    @FXML
    public void exportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter le dashboard en PDF (API)");
        chooser.setInitialFileName("dashboard-report.pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File destination = chooser.showSaveDialog(pieChart.getScene().getWindow());
        if (destination == null) return;

        try {
            URL url = new URL(ApiRuntime.getBaseUrl() + "/api/dashboard/pdf");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);

            int code = conn.getResponseCode();
            if (code != 200) {
                throw new IOException("API dashboard/pdf returned status " + code);
            }
            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            ok("Export PDF", "PDF généré via API: " + destination.getAbsolutePath());
        } catch (IOException e) {
            error("Export PDF", "Erreur API PDF: " + e.getMessage());
        }
    }

    @FXML
    public void exportCsv() {
        if (stats == null) refresh();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les statistiques en CSV");
        chooser.setInitialFileName("dashboard-report.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showSaveDialog(pieChart.getScene().getWindow());
        if (file == null) return;

        String csv = "metric,value\n"
                + "total_formations," + stats.getTotalFormations() + "\n"
                + "total_apprenants," + stats.getTotalApprenants() + "\n"
                + "average_duration," + String.format("%.2f", stats.getAverageDuration()) + "\n"
                + "certified_formations," + stats.getCertifiedFormations() + "\n";
        try {
            Files.writeString(file.toPath(), csv, StandardCharsets.UTF_8);
            ok("Export CSV", "CSV généré: " + file.getAbsolutePath());
        } catch (IOException e) {
            error("Export CSV", e.getMessage());
        }
    }

    @FXML
    public void retourMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
            Stage stage = (Stage) pieChart.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            error("Navigation", e.getMessage());
        }
    }

    private void error(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void ok(String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
