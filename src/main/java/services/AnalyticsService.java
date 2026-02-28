package services;

import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyticsService {

    private final Connection connection;

    public AnalyticsService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public int countCandidats() throws SQLException {
        return fetchCount("SELECT COUNT(*) FROM candidat");
    }

    public int countOffres() throws SQLException {
        return fetchCount("SELECT COUNT(*) FROM offre");
    }

    public int countRecrutements() throws SQLException {
        return fetchCount("SELECT COUNT(*) FROM recrutement");
    }

    public int countReunions() throws SQLException {
        return fetchCount("SELECT COUNT(*) FROM reunion");
    }

    public Map<String, Integer> offresParType() throws SQLException {
        String sql = "SELECT type, COUNT(*) AS total FROM offre GROUP BY type";
        Map<String, Integer> result = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("type"), rs.getInt("total"));
            }
        }
        return result;
    }

    private int fetchCount(String sql) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
}
