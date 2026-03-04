package services;

import models.StatutDemande;
import models.StatutTeletravail;
import utils.MyDatabase;

import java.sql.*;

/**
 * Computes a behavioural score for an employee based on their full demand
 * history.
 *
 * Scoring rules:
 * +5 per congé APPROUVE
 * +5 per télétravail APPROUVE
 * -3 per congé REFUSE
 * -3 per télétravail REFUSE
 * -10 if AbuseDetectionService flags at least one abuse pattern
 * (detected separately, passed as parameter)
 *
 * Interpretation:
 * ≥ 80 → 🌟 Excellent
 * 50-79 → 👍 Bon
 * 20-49 → 😐 Moyen
 * < 20 → ⚠ Suspect
 */
public class EmployeeScoreService {

    private static final int PTS_APPROUVE = 5;
    private static final int PTS_REFUSE = -3;
    private static final int PTS_ABUSE = -10;

    private final Connection connection;

    public EmployeeScoreService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes the raw integer score for an employee.
     *
     * @param idEmploye the employee's database ID
     * @param hasAbuse  true if AbuseDetectionService detected any pattern
     */
    public int computeScore(int idEmploye, boolean hasAbuse) throws SQLException {
        int score = 0;

        // ── Congés ────────────────────────────────────────────────────────────
        String sqlC = "SELECT statut, COUNT(*) AS cnt FROM demande_conge " +
                "WHERE id_employe = ? GROUP BY statut";
        try (PreparedStatement ps = connection.prepareStatement(sqlC)) {
            ps.setInt(1, idEmploye);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String st = rs.getString("statut");
                    int cnt = rs.getInt("cnt");
                    if (StatutDemande.APPROUVE.name().equals(st))
                        score += cnt * PTS_APPROUVE;
                    else if (StatutDemande.REFUSE.name().equals(st))
                        score += cnt * PTS_REFUSE;
                }
            }
        }

        // ── Télétravail ───────────────────────────────────────────────────────
        String sqlT = "SELECT statut, COUNT(*) AS cnt FROM demande_teletravail " +
                "WHERE id_employe = ? GROUP BY statut";
        try (PreparedStatement ps = connection.prepareStatement(sqlT)) {
            ps.setInt(1, idEmploye);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String st = rs.getString("statut");
                    int cnt = rs.getInt("cnt");
                    if (StatutTeletravail.APPROUVE.name().equals(st))
                        score += cnt * PTS_APPROUVE;
                    else if (StatutTeletravail.REFUSE.name().equals(st))
                        score += cnt * PTS_REFUSE;
                }
            }
        }

        // ── Abuse penalty ─────────────────────────────────────────────────────
        if (hasAbuse)
            score += PTS_ABUSE;

        return score;
    }

    /**
     * Returns a display label for the score including the level badge.
     * Example: "🌟 Excellent (37 pts)"
     */
    public String getScoreLabel(int idEmploye, boolean hasAbuse) throws SQLException {
        int score = computeScore(idEmploye, hasAbuse);
        String level;
        if (score >= 80)
            level = "🌟 Excellent";
        else if (score >= 50)
            level = "👍 Bon";
        else if (score >= 20)
            level = "😐 Moyen";
        else
            level = "⚠ Suspect";
        return level + " (" + score + " pts)";
    }
}
