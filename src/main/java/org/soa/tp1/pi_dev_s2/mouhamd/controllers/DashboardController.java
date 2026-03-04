package org.soa.tp1.pi_dev_s2.mouhamd.controllers;

import org.soa.tp1.pi_dev_s2.mouhamd.dto.DashboardStats;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.soa.tp1.pi_dev_s2.mouhamd.services.DashboardService;
import org.soa.tp1.pi_dev_s2.mouhamd.utils.ApiRuntime;
import org.soa.tp1.pi_dev_s2.mouhamd.utils.ThemeUtil;

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

public class DashboardController {

    @FXML private Label lblTotalFormations;
    @FXML private Label lblTotalApprenants;
    @FXML private Label lblAvgDuree;
    @FXML private Label lblCertif;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barFeedbacks;
    @FXML private PieChart pieSecondary;

    @FXML private TextField tfTech;
    private final DashboardService dashboardService = new DashboardService();
    private DashboardStats stats;

    @FXML
    public void initialize() {
        ApiRuntime.ensureStarted();
        Platform.runLater(() -> {
            Stage stage = getStage();
            if (stage != null) {
                var bounds = Screen.getPrimary().getVisualBounds();
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
                stage.setMaximized(true);
            }
        });
        loadData();
    }

    private void loadData() {
        Platform.runLater(() -> {
            if (lblTotalFormations != null && lblTotalFormations.getScene() != null) {
                ThemeUtil.applyTheme((Parent) lblTotalFormations.getScene().getRoot());
            }
        });
        try {
            stats = dashboardService.loadStats();
            lblTotalFormations.setText(String.valueOf(stats.getTotalFormations()));
            lblTotalApprenants.setText(String.valueOf(stats.getTotalApprenants()));
            lblAvgDuree.setText(String.format("%.2f h", stats.getAverageDuration()));
            lblCertif.setText(String.valueOf(stats.getCertifiedFormations()));

            int nonCertif = Math.max(0, stats.getTotalFormations() - stats.getCertifiedFormations());
            if (pieChart != null) {
                pieChart.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Certifiées", stats.getCertifiedFormations()),
                        new PieChart.Data("Non certifiées", nonCertif)
                ));
            }

            XYChart.Series<String, Number> feedbackSeries = new XYChart.Series<>();
            feedbackSeries.setName("Nombre de feedbacks");
            var feedbacksByFormation = dashboardService.loadFeedbacksByFormation();
            feedbacksByFormation.forEach((formationTitle, totalFeedbacks) ->
                    feedbackSeries.getData().add(new XYChart.Data<>(formationTitle, totalFeedbacks)));
            if (barFeedbacks != null) {
                barFeedbacks.setData(FXCollections.observableArrayList(feedbackSeries));
                applyFeedbackBarLabels(feedbackSeries);
            }

            if (pieSecondary != null) {
                var statusCounts = dashboardService.loadFormationStatusCounts();
                var statusData = FXCollections.<PieChart.Data>observableArrayList();
                statusCounts.forEach((status, total) -> statusData.add(new PieChart.Data(status, total)));
                pieSecondary.setData(statusData);
            }

        } catch (SQLException e) {
            error("Chargement dashboard", e.getMessage());
        }
    }


    private void applyFeedbackBarLabels(XYChart.Series<String, Number> feedbackSeries) {
        for (XYChart.Data<String, Number> data : feedbackSeries.getData()) {
            if (data.getNode() != null) {
                attachBarLabel(data, data.getNode());
            }
            data.nodeProperty().addListener((obs, oldNode, node) -> attachBarLabel(data, node));
        }
    }

    private void attachBarLabel(XYChart.Data<String, Number> data, Node node) {
        if (node == null) return;
        String valueText = String.valueOf(data.getYValue().intValue());
        Tooltip.install(node, new Tooltip(valueText + " feedback(s)"));
        Text label = new Text(valueText);
        label.getStyleClass().add("bar-value-label");
        node.parentProperty().addListener((pObs, oldParent, newParent) -> {
            if (newParent instanceof javafx.scene.Group group && !group.getChildren().contains(label)) {
                group.getChildren().add(label);
            }
        });
        node.boundsInParentProperty().addListener((bObs, oldBounds, bounds) -> {
            label.setLayoutX(Math.round(bounds.getMinX() + bounds.getWidth() / 2 - 8));
            label.setLayoutY(Math.round(bounds.getMinY() - 6));
        });
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
    public void refreshDashboard() {
        loadData();
    }

    @FXML
    public void retourMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
            ThemeUtil.applyTheme(root);
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
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Style.css").toExternalForm());

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
