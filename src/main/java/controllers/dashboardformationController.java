package controllers;

import dto.DashboardStats;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.DashboardService;
import utils.ApiRuntime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class dashboardformationController {

    @FXML private Label lblTotalFormations;
    @FXML private Label lblTotalApprenants;
    @FXML private Label lblAvgDuree;
    @FXML private Label lblCertif;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private LineChart<String, Number> lineChart;

    @FXML private TextField tfTech;
    @FXML private Label lblMarket1;
    @FXML private Label lblMarket2;
    @FXML private Label lblMarket3;

    private final DashboardService dashboardService = new DashboardService();
    private DashboardStats stats;

    @FXML
    public void initialize() {
        ApiRuntime.ensureStarted();
        Platform.runLater(() -> {
            Stage stage = getStage();
            if (stage != null) stage.setMaximized(true);
        });
        loadData();
    }

    private void loadData() {
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

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Formations", stats.getTotalFormations()));
            series.getData().add(new XYChart.Data<>("Apprenants", stats.getTotalApprenants()));
            series.getData().add(new XYChart.Data<>("Certifiées", stats.getCertifiedFormations()));
            barChart.setData(FXCollections.observableArrayList(series));

            XYChart.Series<String, Number> line = new XYChart.Series<>();
            line.getData().add(new XYChart.Data<>("Jan", Math.max(1, stats.getTotalFormations()/2.0)));
            line.getData().add(new XYChart.Data<>("Feb", Math.max(1, stats.getTotalApprenants()/3.0)));
            line.getData().add(new XYChart.Data<>("Mar", Math.max(1, stats.getCertifiedFormations())));
            lineChart.setData(FXCollections.observableArrayList(line));

        } catch (SQLException e) {
            error("Chargement dashboard", e.getMessage());
        }
    }

    @FXML
    public void findTopMarkets() {
        String tech = tfTech.getText() == null ? "" : tfTech.getText().trim();
        if (tech.isEmpty()) {
            error("Top markets", "Veuillez saisir une technologie.");
            return;
        }
        try {
            URL url = new URL(ApiRuntime.getBaseUrl() + "/api/market/top?tech=" + tech.replace(" ", "%20"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            String body;
            try (InputStream in = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream()) {
                body = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (code < 200 || code >= 300) {
                error("Top markets", body.isBlank() ? "Erreur API." : body);
                return;
            }

            List<String> entries = parseTopMarketsResponse(body);
            if (entries.isEmpty()) {
                error("Top markets", "Aucun résultat exploitable retourné par l'API.");
                return;
            }

            lblMarket1.setText(entries.size() > 0 ? entries.get(0) : "-");
            lblMarket2.setText(entries.size() > 1 ? entries.get(1) : "-");
            lblMarket3.setText(entries.size() > 2 ? entries.get(2) : "-");
            showTopMarketsPopup(tech, entries);
        } catch (Exception e) {
            error("Top markets", e.getMessage());
        }
    }

    @FXML
    public void exportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = chooser.showSaveDialog(getStage());
        if (file == null) return;

        try {
            URL url = new URL(ApiRuntime.getBaseUrl() + "/api/dashboard/pdf");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                String body;
                try (InputStream in = conn.getErrorStream()) {
                    body = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
                error("Export PDF", body.isBlank() ? "Echec export PDF." : body);
                return;
            }
            try (InputStream in = conn.getInputStream()) {
                Files.write(file.toPath(), in.readAllBytes());
            }
            info("Export PDF", "PDF exporté: " + file.getAbsolutePath());
        } catch (Exception e) {
            error("Export PDF", e.getMessage());
        }
    }

    @FXML
    public void exportCsv() {
        if (stats == null) {
            error("Export CSV", "Aucune donnée à exporter.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showSaveDialog(getStage());
        if (file == null) return;

        String csv = "metric,value\n"
                + "total_formations," + stats.getTotalFormations() + "\n"
                + "total_apprenants," + stats.getTotalApprenants() + "\n"
                + "average_duration," + stats.getAverageDuration() + "\n"
                + "certified_formations," + stats.getCertifiedFormations() + "\n";
        try {
            Files.writeString(file.toPath(), csv, StandardCharsets.UTF_8);
            info("Export CSV", "CSV exporté: " + file.getAbsolutePath());
        } catch (IOException e) {
            error("Export CSV", e.getMessage());
        }
    }

    @FXML
    public void retourMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/mainformation.fxml"));
            Stage stage = getStage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            error("Navigation", e.getMessage());
        }
    }

    private void showTopMarketsPopup(String tech, List<String> entries) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Résultat Top Markets");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/stylef.css").toExternalForm());

        VBox box = new VBox(8);
        box.getStyleClass().addAll("card", "top-market-popup");
        box.getChildren().add(new Label("Top 3 marchés pour: " + tech));
        box.getChildren().add(new Label("1) " + (entries.size() > 0 ? entries.get(0) : "-")));
        box.getChildren().add(new Label("2) " + (entries.size() > 1 ? entries.get(1) : "-")));
        box.getChildren().add(new Label("3) " + (entries.size() > 2 ? entries.get(2) : "-")));

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    private Stage getStage() {
        return (Stage) lblTotalFormations.getScene().getWindow();
    }

    private void error(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void info(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private List<String> parseTopMarketsResponse(String json) {
        List<String> out = new ArrayList<>();
        if (json == null) return out;
        String s = json.trim();

        if (s.startsWith("[") && s.endsWith("]")) {
            String inner = s.substring(1, s.length() - 1).trim();
            if (inner.isEmpty()) return out;
            String[] parts = inner.split(",");
            for (String p : parts) {
                String x = p.trim();
                if (x.startsWith("\"") && x.endsWith("\"")) x = x.substring(1, x.length() - 1);
                out.add(x.replace("\\\"", "\""));
            }
            return out;
        }

        int keyIdx = s.indexOf("\"topMarkets\"");
        if (keyIdx < 0) return out;
        int arrStart = s.indexOf('[', keyIdx);
        int arrEnd = s.indexOf(']', arrStart);
        if (arrStart < 0 || arrEnd < 0) return out;
        String inner = s.substring(arrStart + 1, arrEnd).trim();
        if (inner.isEmpty()) return out;
        String[] parts = inner.split(",");
        for (String p : parts) {
            String x = p.trim();
            if (x.startsWith("\"") && x.endsWith("\"")) x = x.substring(1, x.length() - 1);
            out.add(x.replace("\\\"", "\""));
        }
        return out;
    }
}
