package services;

import models.DemandeConges;
import models.StatutDemande;
import models.TypeConge;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandeCongeService implements IService<DemandeConges> {
    private Connection connection;

    public DemandeCongeService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // 🔹 AJOUT
    public void ajouter(DemandeConges d) throws SQLException {
        String sql = "INSERT INTO demande_conge (id_employe, type_conge, date_debut, date_fin, motif) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, d.getIdEmploye());
        ps.setString(2, d.getTypeConge().name());
        ps.setDate(3, Date.valueOf(d.getDateDebut()));
        ps.setDate(4, Date.valueOf(d.getDateFin()));
        ps.setString(5, d.getMotif());
        ps.executeUpdate();
    }

    public void modifier(DemandeConges d) throws SQLException {
        String sql = "UPDATE demande_conge SET id_employe=?, type_conge=?, date_debut=?, date_fin=?, " +
                "motif=?, statut=?, valide_par=?, date_validation=?, commentaire_decision=? " +
                "WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setInt(1, d.getIdEmploye());
        ps.setString(2, d.getTypeConge().name());
        ps.setDate(3, Date.valueOf(d.getDateDebut()));
        ps.setDate(4, Date.valueOf(d.getDateFin()));
        ps.setString(5, d.getMotif());
        ps.setString(6, d.getStatut().name());

        // champs nullable
        if (d.getValidePar() != null)
            ps.setInt(7, d.getValidePar());
        else
            ps.setNull(7, Types.INTEGER);

        if (d.getDateValidation() != null)
            ps.setTimestamp(8, Timestamp.valueOf(d.getDateValidation()));
        else
            ps.setNull(8, Types.TIMESTAMP);

        ps.setString(9, d.getCommentaireDecision());

        ps.setInt(10, d.getId());

        ps.executeUpdate();
    }

    // 🔹 SUPPRESSION
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM demande_conge WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // 🔹 RÉCUPÉRATION
    public List<DemandeConges> recuperer() throws SQLException {
        String sql = "SELECT * FROM demande_conge";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<DemandeConges> demandes = new ArrayList<>();

        while (rs.next()) {
            DemandeConges d = new DemandeConges();
            d.setId(rs.getInt("id"));
            d.setIdEmploye(rs.getInt("id_employe"));
            d.setTypeConge(TypeConge.valueOf(rs.getString("type_conge")));
            d.setDateDebut(rs.getDate("date_debut").toLocalDate());
            d.setDateFin(rs.getDate("date_fin").toLocalDate());
            d.setMotif(rs.getString("motif"));
            d.setStatut(StatutDemande.valueOf(rs.getString("statut")));
            d.setDateDemande(rs.getTimestamp("date_demande").toLocalDateTime());

            Timestamp dv = rs.getTimestamp("date_validation");
            if (dv != null)
                d.setDateValidation(dv.toLocalDateTime());

            d.setValidePar((Integer) rs.getObject("valide_par"));
            d.setCommentaireDecision(rs.getString("commentaire_decision"));

            demandes.add(d);
        }
        return demandes;
    }

    public List<DemandeConges> rechercherParNomPrenom(String nom, String prenom) throws SQLException {
        String sql = """
                    SELECT dc.*
                    FROM demande_conge dc
                    JOIN utilisateur u ON u.id = dc.id_employe
                    WHERE u.nom LIKE ? AND u.prenom LIKE ?
                """;

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "%" + nom + "%");
        ps.setString(2, "%" + prenom + "%");

        ResultSet rs = ps.executeQuery();
        List<DemandeConges> list = new ArrayList<>();

        while (rs.next()) {
            DemandeConges d = new DemandeConges();
            d.setId(rs.getInt("id"));
            d.setIdEmploye(rs.getInt("id_employe"));
            d.setTypeConge(TypeConge.valueOf(rs.getString("type_conge")));
            d.setDateDebut(rs.getDate("date_debut").toLocalDate());
            d.setDateFin(rs.getDate("date_fin").toLocalDate());
            d.setMotif(rs.getString("motif"));
            d.setStatut(StatutDemande.valueOf(rs.getString("statut")));
            d.setDateDemande(rs.getTimestamp("date_demande").toLocalDateTime());
            list.add(d);
        }
        return list;
    }

    /** Returns all demands for a specific employee (for employee dashboard). */
    public List<DemandeConges> recupererParEmploye(int idEmploye) throws SQLException {
        String sql = "SELECT * FROM demande_conge WHERE id_employe = ? ORDER BY date_demande DESC";
        List<DemandeConges> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEmploye);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DemandeConges d = new DemandeConges();
                    d.setId(rs.getInt("id"));
                    d.setIdEmploye(rs.getInt("id_employe"));
                    d.setTypeConge(TypeConge.valueOf(rs.getString("type_conge")));
                    d.setDateDebut(rs.getDate("date_debut").toLocalDate());
                    d.setDateFin(rs.getDate("date_fin").toLocalDate());
                    d.setMotif(rs.getString("motif"));
                    d.setStatut(StatutDemande.valueOf(rs.getString("statut")));
                    Timestamp dd = rs.getTimestamp("date_demande");
                    if (dd != null)
                        d.setDateDemande(dd.toLocalDateTime());
                    d.setCommentaireDecision(rs.getString("commentaire_decision"));
                    list.add(d);
                }
            }
        }
        return list;
    }

    /**
     * Returns all pending and approved demands with employee name (for admin
     * dashboard). Runs priority detection and score annotation.
     */
    public List<DemandeConges> recupererEnAttente() throws SQLException {
        String sql = "SELECT dc.*, u.nom AS emp_nom, u.prenom AS emp_prenom " +
                "FROM demande_conge dc " +
                "LEFT JOIN utilisateur u ON u.id = dc.id_employe " +
                "WHERE dc.statut IN ('EN_ATTENTE','APPROUVE') ORDER BY dc.date_demande ASC";
        List<DemandeConges> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                DemandeConges d = new DemandeConges();
                d.setId(rs.getInt("id"));
                d.setIdEmploye(rs.getInt("id_employe"));
                d.setNomEmploye(rs.getString("emp_nom"));
                d.setPrenomEmploye(rs.getString("emp_prenom"));
                d.setTypeConge(TypeConge.valueOf(rs.getString("type_conge")));
                d.setDateDebut(rs.getDate("date_debut").toLocalDate());
                d.setDateFin(rs.getDate("date_fin").toLocalDate());
                d.setMotif(rs.getString("motif"));
                d.setStatut(StatutDemande.valueOf(rs.getString("statut")));
                Timestamp dd = rs.getTimestamp("date_demande");
                if (dd != null)
                    d.setDateDemande(dd.toLocalDateTime());
                list.add(d);
            }
        }

        // ── Priority detection ────────────────────────────────────────────────
        new PriorityService().annotate(list);

        // ── Score annotation (cached per employee) ────────────────────────────
        EmployeeScoreService scoreService = new EmployeeScoreService();
        java.util.Map<Integer, String> scoreCache = new java.util.HashMap<>();
        for (DemandeConges d : list) {
            int empId = d.getIdEmploye();
            if (!scoreCache.containsKey(empId)) {
                scoreCache.put(empId, scoreService.getScoreLabel(empId, false));
            }
            d.setScoreLabel(scoreCache.get(empId));
        }

        // ── Holiday detection ─────────────────────────────────────────────────
        HolidayService holidayService = new HolidayService();
        for (DemandeConges d : list) {
            if (d.getDateDebut() != null) {
                d.setHolidayNote(holidayService.getHolidayNote(d.getDateDebut()));
            }
        }

        return list;
    }

    /** Returns ALL demands with employee name (for admin statistics). */
    public List<DemandeConges> recupererTous() throws SQLException {
        String sql = "SELECT dc.*, u.nom AS emp_nom, u.prenom AS emp_prenom " +
                "FROM demande_conge dc " +
                "LEFT JOIN utilisateur u ON u.id = dc.id_employe " +
                "ORDER BY dc.date_demande DESC";
        List<DemandeConges> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                DemandeConges d = new DemandeConges();
                d.setId(rs.getInt("id"));
                d.setIdEmploye(rs.getInt("id_employe"));
                d.setNomEmploye(rs.getString("emp_nom"));
                d.setPrenomEmploye(rs.getString("emp_prenom"));
                d.setTypeConge(TypeConge.valueOf(rs.getString("type_conge")));
                d.setDateDebut(rs.getDate("date_debut").toLocalDate());
                d.setDateFin(rs.getDate("date_fin").toLocalDate());
                d.setMotif(rs.getString("motif"));
                d.setStatut(StatutDemande.valueOf(rs.getString("statut")));
                Timestamp dd = rs.getTimestamp("date_demande");
                if (dd != null)
                    d.setDateDemande(dd.toLocalDateTime());
                list.add(d);
            }
        }
        return list;
    }

    /** Admin approves or rejects a demand. */
    public void mettreAJourStatut(int id, StatutDemande statut, int validePar, String commentaire) throws SQLException {
        String sql = "UPDATE demande_conge SET statut=?, valide_par=?, date_validation=NOW(), commentaire_decision=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            ps.setInt(2, validePar);
            ps.setString(3, commentaire);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }
}
