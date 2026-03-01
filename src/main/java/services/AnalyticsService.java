package services;

import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyticsService {

    private final Connection connection;

    public AnalyticsService() {
        this.connection = MyDatabase.getInstance().getConnection();
        ensureStatusColumn();
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


    public Map<String, Integer> applicationsParStatut() throws SQLException {
        String sql = "SELECT COALESCE(statut, 'Nouveau') AS statut, COUNT(*) AS total FROM candidat GROUP BY statut ORDER BY total DESC";
        Map<String, Integer> result = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("statut"), rs.getInt("total"));
            }
        }
        return result;
    }

    public Map<String, Integer> applicationsParOffre() throws SQLException {
        String sql = """
                SELECT o.nomOffre, COUNT(*) AS total
                FROM recrutement r
                JOIN offre o ON r.idOffre = o.idOffre
                GROUP BY o.idOffre, o.nomOffre
                ORDER BY total DESC, o.nomOffre
                """;
        Map<String, Integer> result = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("nomOffre"), rs.getInt("total"));
            }
        }
        return result;
    }


    private void ensureStatusColumn() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("ALTER TABLE candidat ADD COLUMN statut VARCHAR(60) NOT NULL DEFAULT 'Nouveau'");
        } catch (SQLException ignored) {
        }
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("UPDATE candidat SET statut='Nouveau' WHERE statut IS NULL OR statut = ''");
        } catch (SQLException ignored) {
        }
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
