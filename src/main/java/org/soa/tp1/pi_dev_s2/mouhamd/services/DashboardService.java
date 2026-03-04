package org.soa.tp1.pi_dev_s2.mouhamd.services;

import org.soa.tp1.pi_dev_s2.mouhamd.dto.DashboardStats;
import org.soa.tp1.pi_dev_s2.mouhamd.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardService {

    private final Connection connection;

    public DashboardService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public DashboardStats loadStats() throws SQLException {
        int formations = count("SELECT COUNT(*) FROM formation");
        int apprenants = count("SELECT COUNT(*) FROM apprenant");
        int certifiees = count("SELECT COUNT(*) FROM formation WHERE certification=1");
        double averageDuration = averageDuration();
        return new DashboardStats(formations, apprenants, averageDuration, certifiees);
    }


    public Map<String, Integer> loadFormationStatusCounts() throws SQLException {
        String sql = "SELECT COALESCE(statut, 'INCONNU') AS statut, COUNT(*) AS total FROM apprenant GROUP BY statut";
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                counts.put(rs.getString("statut"), rs.getInt("total"));
            }
        }
        return counts;
    }

    public Map<String, Integer> loadFeedbacksByFormation() throws SQLException {
        String sql = "SELECT f.titre, COUNT(ff.id) AS total_feedbacks "
                + "FROM formation f "
                + "LEFT JOIN formation_feedback ff ON ff.id_formation = f.id_formation "
                + "GROUP BY f.id_formation, f.titre "
                + "ORDER BY total_feedbacks DESC, f.titre ASC";
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                counts.put(rs.getString("titre"), rs.getInt("total_feedbacks"));
            }
        }
        return counts;
    }

    private int count(String sql) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    private double averageDuration() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT AVG(duree) FROM formation");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;
        }
    }
}
