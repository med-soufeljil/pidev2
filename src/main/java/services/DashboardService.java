package services;

import dto.DashboardStats;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
