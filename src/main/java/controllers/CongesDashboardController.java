package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import services.DemandeCongeService;

import java.sql.SQLException;
import java.util.Map;

public class CongesDashboardController {
    @FXML private Label lblCongeTotal, lblCongePending, lblCongeApproved, lblCongeRejected;
    @FXML private PieChart pieStatut;
    @FXML private BarChart<String, Number> barType;

    private final DemandeCongeService congeService = new DemandeCongeService();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            Map<String, Integer> stats = congeService.statsByStatutVisibles();
            int total = stats.values().stream().mapToInt(Integer::intValue).sum();
            lblCongeTotal.setText(String.valueOf(total));
            lblCongePending.setText(String.valueOf(stats.getOrDefault("EN_ATTENTE", 0)));
            lblCongeApproved.setText(String.valueOf(stats.getOrDefault("APPROUVE", 0)));
            lblCongeRejected.setText(String.valueOf(stats.getOrDefault("REFUSE", 0)));
            pieStatut.setData(FXCollections.observableArrayList(
                    stats.entrySet().stream().map(e -> new PieChart.Data(e.getKey(), e.getValue())).toList()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            congeService.statsByTypeVisibles().forEach((type, count) -> series.getData().add(new XYChart.Data<>(type, count)));
            barType.getData().setAll(series);
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Dashboard Congés");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
