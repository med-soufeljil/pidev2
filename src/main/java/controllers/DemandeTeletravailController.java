package controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.DemandeTeletravail;
import models.StatutTeletravail;
import services.DemandeTeletravailService;
import utils.MyDatabase;
import utils.Navigation;
import utils.Session;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class DemandeTeletravailController {

    @FXML
    private DatePicker dpPremierJour, dpDeuxiemeJour;
    @FXML
    private TextArea taDemandeSpeciale;
    @FXML
    private Label lblMsg;
    @FXML
    private Label lblEmploye;

    private final DemandeTeletravailService service = new DemandeTeletravailService();

    @FXML
    public void initialize() {
        lblMsg.setText("");
        lblEmploye.setText("Employé : " + Session.getCurrent().getNomComplet());
    }

    // ── UI helpers ────────────────────────────────────────────────────────────
    private void setError(String m) {
        lblMsg.setStyle("-fx-text-fill:#cc0000;");
        lblMsg.setText(m);
    }

    private void setSuccess(String m) {
        lblMsg.setStyle("-fx-text-fill:#008000;");
        lblMsg.setText(m);
    }

    // ── Date helpers ──────────────────────────────────────────────────────────
    private LocalDate startOfWeek(LocalDate d) {
        return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDate endOfWeek(LocalDate d) {
        return d.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    private boolean isWeekday(LocalDate d) {
        DayOfWeek w = d.getDayOfWeek();
        return w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY;
    }

    // Count already requested days in the week (EN_ATTENTE or APPROUVE)
    private int countTeletravailDaysInWeek(int idEmploye, LocalDate weekStart, LocalDate weekEnd) throws SQLException {
        String sql = """
                SELECT date_debut, date_fin
                FROM demande_teletravail
                WHERE id_employe = ?
                  AND statut IN ('EN_ATTENTE','APPROUVE')
                  AND (
                       (date_debut BETWEEN ? AND ?) OR
                       (date_fin BETWEEN ? AND ?)
                  )
                """;
        Connection cnx = MyDatabase.getInstance().getConnection();
        int count = 0;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idEmploye);
            ps.setDate(2, Date.valueOf(weekStart));
            ps.setDate(3, Date.valueOf(weekEnd));
            ps.setDate(4, Date.valueOf(weekStart));
            ps.setDate(5, Date.valueOf(weekEnd));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate d1 = rs.getDate("date_debut").toLocalDate();
                    LocalDate d2 = rs.getDate("date_fin").toLocalDate();
                    if (!d1.isBefore(weekStart) && !d1.isAfter(weekEnd))
                        count++;
                    if (!d2.isBefore(weekStart) && !d2.isAfter(weekEnd) && !d2.equals(d1))
                        count++;
                }
            }
        }
        return count;
    }

    // ── Validation ─────────────────────────────────────────────────────────────
    private void validateForm() {
        if (dpPremierJour.getValue() == null)
            throw new IllegalArgumentException("Premier jour obligatoire");
        if (dpDeuxiemeJour.getValue() == null)
            throw new IllegalArgumentException("Deuxième jour obligatoire");

        LocalDate j1 = dpPremierJour.getValue();
        LocalDate j2 = dpDeuxiemeJour.getValue();

        if (j1.equals(j2))
            throw new IllegalArgumentException("Les 2 jours doivent être différents");
        if (!isWeekday(j1) || !isWeekday(j2))
            throw new IllegalArgumentException("Choisir uniquement du Lundi au Vendredi");
    }

    @FXML
    private void onAjouter() {
        try {
            validateForm();

            int idEmp = Session.getCurrent().getId();

            LocalDate premier = dpPremierJour.getValue();
            LocalDate deuxieme = dpDeuxiemeJour.getValue();

            LocalDate weekStart = startOfWeek(premier);
            LocalDate weekEnd = endOfWeek(premier);

            if (deuxieme.isBefore(weekStart) || deuxieme.isAfter(weekEnd)) {
                throw new IllegalArgumentException("Les 2 jours doivent être dans la même semaine");
            }

            int already = countTeletravailDaysInWeek(idEmp, weekStart, weekEnd);
            if (already >= 2) {
                throw new IllegalArgumentException("Limite atteinte : 2 jours max par semaine");
            }
            if (already + 2 > 2) {
                throw new IllegalArgumentException("Cette semaine : il reste " + (2 - already) + " jour(s) seulement");
            }

            String moisConcerne = premier.getYear() + "-" + String.format("%02d", premier.getMonthValue());

            DemandeTeletravail dt = new DemandeTeletravail();
            dt.setIdEmploye(idEmp);
            dt.setDateDebut(premier);
            dt.setDateFin(deuxieme);
            dt.setNbJours(2);
            dt.setMotif(taDemandeSpeciale.getText());
            dt.setMoisConcerne(moisConcerne);
            dt.setStatut(StatutTeletravail.EN_ATTENTE);

            service.ajouter(dt);
            setSuccess("Demande télétravail envoyée ✅");
            onClear();

        } catch (SQLException e) {
            setError("Erreur SQL: " + e.getMessage());
        } catch (Exception e) {
            setError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onClear() {
        dpPremierJour.setValue(null);
        dpDeuxiemeJour.setValue(null);
        taDemandeSpeciale.clear();
        lblMsg.setText("");
    }

    @FXML
    private void goBack(javafx.event.ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Navigation.switchTo(stage, "EmployeDashboard.fxml");
    }
}