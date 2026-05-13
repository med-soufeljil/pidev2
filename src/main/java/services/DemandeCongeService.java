package services;

import models.DemandeConge;
import utils.AuthContext;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DemandeCongeService implements IService<DemandeConge> {
    private final Connection connection;

    public DemandeCongeService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(DemandeConge d) throws SQLException {
        String sql = """
                INSERT INTO demande_conge (id_employe, type_conge, date_debut, date_fin, motif, statut)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            fillEditable(ps, d);
            ps.setString(6, defaultStatut(d.getStatut()));
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(DemandeConge d) throws SQLException {
        String sql = """
                UPDATE demande_conge
                SET id_employe=?, type_conge=?, date_debut=?, date_fin=?, motif=?, statut=?, commentaire_decision=?
                WHERE id=?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            fillEditable(ps, d);
            ps.setString(6, defaultStatut(d.getStatut()));
            ps.setString(7, d.getCommentaireDecision());
            ps.setInt(8, d.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM demande_conge WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<DemandeConge> recuperer() throws SQLException {
        return recupererAvecFiltre(null, 0);
    }

    public List<DemandeConge> recupererVisibles() throws SQLException {
        if (AuthContext.isAdmin()) {
            return recupererAvecFiltre("dc.statut = ?", "EN_ATTENTE");
        }
        return recupererAvecFiltre("dc.id_employe = ?", AuthContext.getCurrentUserId());
    }

    private List<DemandeConge> recupererAvecFiltre(String where, Object value) throws SQLException {
        String sql = """
                SELECT dc.*, CONCAT(COALESCE(u.prenom,''), ' ', COALESCE(u.nom,'')) AS employe_nom
                FROM demande_conge dc
                LEFT JOIN utilisateur u ON u.id = dc.id_employe
                """ + (where == null ? "" : " WHERE " + where) + " ORDER BY dc.date_demande DESC, dc.id DESC";
        List<DemandeConge> demandes = new ArrayList<>();
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
                UPDATE demande_conge
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
        try (PreparedStatement ps = connection.prepareStatement("SELECT statut, COUNT(*) total FROM demande_conge GROUP BY statut");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) stats.put(rs.getString("statut"), rs.getInt("total"));
        }
        return stats;
    }

    public Map<String, Integer> statsByType() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT type_conge, COUNT(*) total FROM demande_conge GROUP BY type_conge");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) stats.put(rs.getString("type_conge"), rs.getInt("total"));
        }
        return stats;
    }

    public Map<String, Integer> statsByStatutVisibles() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("EN_ATTENTE", 0);
        stats.put("APPROUVE", 0);
        stats.put("REFUSE", 0);
        for (DemandeConge demande : recupererVisibles()) {
            stats.merge(demande.getStatut(), 1, Integer::sum);
        }
        return stats;
    }

    public Map<String, Integer> statsByTypeVisibles() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        for (DemandeConge demande : recupererVisibles()) {
            stats.merge(demande.getTypeConge(), 1, Integer::sum);
        }
        return stats;
    }

    private void fillEditable(PreparedStatement ps, DemandeConge d) throws SQLException {
        ps.setInt(1, d.getIdEmploye());
        ps.setString(2, d.getTypeConge());
        ps.setDate(3, Date.valueOf(d.getDateDebut()));
        ps.setDate(4, Date.valueOf(d.getDateFin()));
        ps.setString(5, d.getMotif());
    }

    private DemandeConge map(ResultSet rs) throws SQLException {
        DemandeConge d = new DemandeConge();
        d.setId(rs.getInt("id"));
        d.setIdEmploye(rs.getInt("id_employe"));
        d.setEmployeNom(rs.getString("employe_nom"));
        d.setTypeConge(rs.getString("type_conge"));
        d.setDateDebut(rs.getDate("date_debut").toLocalDate());
        d.setDateFin(rs.getDate("date_fin").toLocalDate());
        d.setMotif(rs.getString("motif"));
        d.setStatut(rs.getString("statut"));
        Timestamp dateDemande = rs.getTimestamp("date_demande");
        if (dateDemande != null) d.setDateDemande(dateDemande.toLocalDateTime());
        int validePar = rs.getInt("valide_par");
        if (!rs.wasNull()) d.setValidePar(validePar);
        Timestamp dateValidation = rs.getTimestamp("date_validation");
        if (dateValidation != null) d.setDateValidation(dateValidation.toLocalDateTime());
        d.setCommentaireDecision(rs.getString("commentaire_decision"));
        return d;
    }

    private String defaultStatut(String statut) {
        return statut == null || statut.isBlank() ? "EN_ATTENTE" : statut;
    }
}
