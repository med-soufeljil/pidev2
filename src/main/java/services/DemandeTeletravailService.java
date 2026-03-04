package services;

import models.DemandeTeletravail;
import models.StatutTeletravail;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemandeTeletravailService implements IService<DemandeTeletravail> {
    private Connection connection;

    public DemandeTeletravailService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ✅ AJOUT
    public void ajouter(DemandeTeletravail d) throws SQLException {
        String sql = "INSERT INTO demande_teletravail " +
                "(id_employe, date_debut, date_fin, nb_jours, motif, statut, mois_concerne) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, d.getIdEmploye());
            ps.setDate(2, Date.valueOf(d.getDateDebut()));
            ps.setDate(3, Date.valueOf(d.getDateFin()));
            ps.setInt(4, d.getNbJours());
            ps.setString(5, d.getMotif());
            ps.setString(6, d.getStatut().name());
            ps.setString(7, d.getMoisConcerne());
            ps.executeUpdate();
        }
    }

    // ✅ MODIFIER
    public void modifier(DemandeTeletravail d) throws SQLException {
        String sql = "UPDATE demande_teletravail SET id_employe=?, date_debut=?, date_fin=?, nb_jours=?, " +
                "motif=?, statut=?, valide_par=?, date_validation=?, commentaire_decision=?, mois_concerne=? " +
                "WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, d.getIdEmploye());
            ps.setDate(2, Date.valueOf(d.getDateDebut()));
            ps.setDate(3, Date.valueOf(d.getDateFin()));
            ps.setInt(4, d.getNbJours());
            ps.setString(5, d.getMotif());
            ps.setString(6, d.getStatut().name());
            if (d.getValidePar() != null)
                ps.setInt(7, d.getValidePar());
            else
                ps.setNull(7, Types.INTEGER);
            if (d.getDateValidation() != null)
                ps.setTimestamp(8, Timestamp.valueOf(d.getDateValidation()));
            else
                ps.setNull(8, Types.TIMESTAMP);
            ps.setString(9, d.getCommentaireDecision());
            ps.setString(10, d.getMoisConcerne());
            ps.setInt(11, d.getId());
            ps.executeUpdate();
        }
    }

    // ✅ SUPPRIMER
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM demande_teletravail WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ✅ RECUPERER (all)
    public List<DemandeTeletravail> recuperer() throws SQLException {
        String sql = "SELECT * FROM demande_teletravail";
        List<DemandeTeletravail> demandes = new ArrayList<>();
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                demandes.add(mapRow(rs));
        }
        return demandes;
    }

    /** Demands for a specific employee (employee dashboard). */
    public List<DemandeTeletravail> recupererParEmploye(int idEmploye) throws SQLException {
        String sql = "SELECT * FROM demande_teletravail WHERE id_employe = ? ORDER BY date_demande DESC";
        List<DemandeTeletravail> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEmploye);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * All pending and approved télétravail demands with employee name (admin
     * dashboard). Runs abuse detection on every demand before returning.
     */
    public List<DemandeTeletravail> recupererEnAttente() throws SQLException {
        String sql = "SELECT dt.*, u.nom AS emp_nom, u.prenom AS emp_prenom " +
                "FROM demande_teletravail dt " +
                "LEFT JOIN utilisateur u ON u.id = dt.id_employe " +
                "WHERE dt.statut IN ('EN_ATTENTE','APPROUVE') ORDER BY dt.date_demande ASC";
        List<DemandeTeletravail> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                DemandeTeletravail d = mapRow(rs);
                d.setNomEmploye(rs.getString("emp_nom"));
                d.setPrenomEmploye(rs.getString("emp_prenom"));
                list.add(d);
            }
        }

        // ── Abuse detection ───────────────────────────────────────────────────
        // Cache full history per employee to avoid N+1 queries
        AbuseDetectionService detector = new AbuseDetectionService();
        Map<Integer, List<DemandeTeletravail>> historyCache = new HashMap<>();
        Map<Integer, Boolean> abuseCache = new HashMap<>();
        for (DemandeTeletravail d : list) {
            int empId = d.getIdEmploye();
            if (!historyCache.containsKey(empId)) {
                historyCache.put(empId, recupererParEmploye(empId));
            }
            detector.analyze(d, historyCache.get(empId));
            // track whether THIS employee has any abuse flag
            abuseCache.merge(empId, !d.getAbuseFlags().isEmpty(), Boolean::logicalOr);
        }

        // ── Score annotation (cached per employee) ────────────────────────────
        EmployeeScoreService scoreService = new EmployeeScoreService();
        Map<Integer, String> scoreCache = new HashMap<>();
        for (DemandeTeletravail d : list) {
            int empId = d.getIdEmploye();
            if (!scoreCache.containsKey(empId)) {
                boolean hasAbuse = abuseCache.getOrDefault(empId, false);
                try {
                    scoreCache.put(empId, scoreService.getScoreLabel(empId, hasAbuse));
                } catch (Exception ex) {
                    scoreCache.put(empId, "");
                }
            }
            d.setScoreLabel(scoreCache.get(empId));
        }

        // ── Weather annotation ────────────────────────────────────────────────
        WeatherService weatherService = new WeatherService();
        for (DemandeTeletravail d : list) {
            if (d.getDateDebut() != null) {
                d.setWeatherNote(weatherService.getWeatherNote(d.getDateDebut()));
            }
        }

        return list;
    }

    /** All télétravail demands with employee name (admin statistics). */
    public List<DemandeTeletravail> recupererTous() throws SQLException {
        String sql = "SELECT dt.*, u.nom AS emp_nom, u.prenom AS emp_prenom " +
                "FROM demande_teletravail dt " +
                "LEFT JOIN utilisateur u ON u.id = dt.id_employe " +
                "ORDER BY dt.date_demande DESC";
        List<DemandeTeletravail> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                DemandeTeletravail d = mapRow(rs);
                d.setNomEmploye(rs.getString("emp_nom"));
                d.setPrenomEmploye(rs.getString("emp_prenom"));
                list.add(d);
            }
        }
        return list;
    }

    /** Admin approves or rejects a télétravail demand. */
    public void mettreAJourStatut(int id, StatutTeletravail statut, int validePar, String commentaire)
            throws SQLException {
        String sql = "UPDATE demande_teletravail SET statut=?, valide_par=?, date_validation=NOW(), commentaire_decision=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            ps.setInt(2, validePar);
            ps.setString(3, commentaire);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private DemandeTeletravail mapRow(ResultSet rs) throws SQLException {
        DemandeTeletravail d = new DemandeTeletravail();
        d.setId(rs.getInt("id"));
        d.setIdEmploye(rs.getInt("id_employe"));
        d.setDateDebut(rs.getDate("date_debut").toLocalDate());
        d.setDateFin(rs.getDate("date_fin").toLocalDate());
        d.setNbJours(rs.getInt("nb_jours"));
        d.setMotif(rs.getString("motif"));
        d.setStatut(StatutTeletravail.valueOf(rs.getString("statut")));
        d.setMoisConcerne(rs.getString("mois_concerne"));
        Timestamp dd = rs.getTimestamp("date_demande");
        if (dd != null)
            d.setDateDemande(dd.toLocalDateTime());
        d.setValidePar((Integer) rs.getObject("valide_par"));
        Timestamp dv = rs.getTimestamp("date_validation");
        if (dv != null)
            d.setDateValidation(dv.toLocalDateTime());
        d.setCommentaireDecision(rs.getString("commentaire_decision"));
        return d;
    }
}
