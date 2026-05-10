package services;

import models.DemandeTeletravail;
import utils.AuthContext;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DemandeTeletravailService implements IService<DemandeTeletravail> {
    private final Connection connection;

    public DemandeTeletravailService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(DemandeTeletravail d) throws SQLException {
        String sql = """
                INSERT INTO demande_teletravail (id_employe, date_debut, date_fin, nb_jours, motif, statut, mois_concerne)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            fillEditable(ps, d);
            ps.setString(6, defaultStatut(d.getStatut()));
            ps.setString(7, resolveMois(d));
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(DemandeTeletravail d) throws SQLException {
        String sql = """
                UPDATE demande_teletravail
                SET id_employe=?, date_debut=?, date_fin=?, nb_jours=?, motif=?, statut=?, mois_concerne=?, commentaire_decision=?
                WHERE id=?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            fillEditable(ps, d);
            ps.setString(6, defaultStatut(d.getStatut()));
            ps.setString(7, resolveMois(d));
            ps.setString(8, d.getCommentaireDecision());
            ps.setInt(9, d.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM demande_teletravail WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<DemandeTeletravail> recuperer() throws SQLException {
        return recupererAvecFiltre(null, 0);
    }

    public List<DemandeTeletravail> recupererVisibles() throws SQLException {
        if (AuthContext.isAdmin()) {
            return recupererAvecFiltre("dt.statut = ?", "EN_ATTENTE");
        }
        return recupererAvecFiltre("dt.id_employe = ?", AuthContext.getCurrentUserId());
    }

    private List<DemandeTeletravail> recupererAvecFiltre(String where, Object value) throws SQLException {
        String sql = """
                SELECT dt.*, CONCAT(COALESCE(u.prenom,''), ' ', COALESCE(u.nom,'')) AS employe_nom
                FROM demande_teletravail dt
                LEFT JOIN utilisateur u ON u.id = dt.id_employe
                """ + (where == null ? "" : " WHERE " + where) + " ORDER BY dt.date_demande DESC, dt.id DESC";
        List<DemandeTeletravail> demandes = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (where != null && value instanceof String stringValue) ps.setString(1, stringValue);
            if (where != null && value instanceof Integer intValue) ps.setInt(1, intValue);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) demandes.add(map(rs));
            }
        }
        return demandes;
    }

    public void changerStatut(int id, String statut, int validePar, String commentaire) throws SQLException {
        String sql = """
                UPDATE demande_teletravail
                SET statut=?, valide_par=?, date_validation=?, commentaire_decision=?
                WHERE id=?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, validePar);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, commentaire == null ? "" : commentaire);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    public Map<String, Integer> statsByStatut() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("EN_ATTENTE", 0);
        stats.put("APPROUVE", 0);
        stats.put("REFUSE", 0);
        try (PreparedStatement ps = connection.prepareStatement("SELECT statut, COUNT(*) total FROM demande_teletravail GROUP BY statut");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) stats.put(rs.getString("statut"), rs.getInt("total"));
        }
        return stats;
    }

    public Map<String, Integer> statsByMois() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT mois_concerne, COUNT(*) total FROM demande_teletravail GROUP BY mois_concerne ORDER BY mois_concerne");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) stats.put(rs.getString("mois_concerne"), rs.getInt("total"));
        }
        return stats;
    }

    public Map<String, Integer> statsByStatutVisibles() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("EN_ATTENTE", 0);
        stats.put("APPROUVE", 0);
        stats.put("REFUSE", 0);
        for (DemandeTeletravail demande : recupererVisibles()) {
            stats.merge(demande.getStatut(), 1, Integer::sum);
        }
        return stats;
    }

    public Map<String, Integer> statsByMoisVisibles() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        for (DemandeTeletravail demande : recupererVisibles()) {
            stats.merge(demande.getMoisConcerne(), 1, Integer::sum);
        }
        return stats;
    }

    private void fillEditable(PreparedStatement ps, DemandeTeletravail d) throws SQLException {
        ps.setInt(1, d.getIdEmploye());
        ps.setDate(2, Date.valueOf(d.getDateDebut()));
        ps.setDate(3, Date.valueOf(d.getDateFin()));
        ps.setInt(4, d.getNbJours());
        ps.setString(5, d.getMotif());
    }

    private DemandeTeletravail map(ResultSet rs) throws SQLException {
        DemandeTeletravail d = new DemandeTeletravail();
        d.setId(rs.getInt("id"));
        d.setIdEmploye(rs.getInt("id_employe"));
        d.setEmployeNom(rs.getString("employe_nom"));
        d.setDateDebut(rs.getDate("date_debut").toLocalDate());
        d.setDateFin(rs.getDate("date_fin").toLocalDate());
        d.setNbJours(rs.getInt("nb_jours"));
        d.setMotif(rs.getString("motif"));
        d.setStatut(rs.getString("statut"));
        Timestamp dateDemande = rs.getTimestamp("date_demande");
        if (dateDemande != null) d.setDateDemande(dateDemande.toLocalDateTime());
        int validePar = rs.getInt("valide_par");
        if (!rs.wasNull()) d.setValidePar(validePar);
        Timestamp dateValidation = rs.getTimestamp("date_validation");
        if (dateValidation != null) d.setDateValidation(dateValidation.toLocalDateTime());
        d.setCommentaireDecision(rs.getString("commentaire_decision"));
        d.setMoisConcerne(rs.getString("mois_concerne"));
        return d;
    }

    private String resolveMois(DemandeTeletravail d) {
        if (d.getMoisConcerne() != null && !d.getMoisConcerne().isBlank()) return d.getMoisConcerne();
        return YearMonth.from(d.getDateDebut()).toString();
    }

    private String defaultStatut(String statut) {
        return statut == null || statut.isBlank() ? "EN_ATTENTE" : statut;
    }
}
