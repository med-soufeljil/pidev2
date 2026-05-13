package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import services.DemandeTeletravailService;

import java.sql.SQLException;
import java.util.Map;

public class TeletravailDashboardController {
    @FXML private Label lblTtTotal, lblTtPending, lblTtApproved, lblTtRejected;
    @FXML private PieChart pieStatut;
    @FXML private BarChart<String, Number> barMois;

    private final DemandeTeletravailService ttService = new DemandeTeletravailService();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            Map<String, Integer> stats = ttService.statsByStatutVisibles();
            int total = stats.values().stream().mapToInt(Integer::intValue).sum();
            lblTtTotal.setText(String.valueOf(total));
            lblTtPending.setText(String.valueOf(stats.getOrDefault("EN_ATTENTE", 0)));
            lblTtApproved.setText(String.valueOf(stats.getOrDefault("APPROUVE", 0)));
            lblTtRejected.setText(String.valueOf(stats.getOrDefault("REFUSE", 0)));
            pieStatut.setData(FXCollections.observableArrayList(
                    stats.entrySet().stream().map(e -> new PieChart.Data(e.getKey(), e.getValue())).toList()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            ttService.statsByMoisVisibles().forEach((mois, count) -> series.getData().add(new XYChart.Data<>(mois, count)));
            barMois.getData().setAll(series);
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Dashboard Télétravail");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
