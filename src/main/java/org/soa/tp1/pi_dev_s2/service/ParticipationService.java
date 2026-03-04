package org.soa.tp1.pi_dev_s2.service;

import org.soa.tp1.pi_dev_s2.com.esprit.models.Participation;
import org.soa.tp1.pi_dev_s2.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService {

    private Connection connection;

    public ParticipationService() {
        this.connection = DatabaseConfig.getConnection();
    }

    public void ajouter(Participation p) throws SQLException {
        String sql = "INSERT INTO participations (id_e, dateInscription, statut, presence, dateCreation) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, p.getId_e());
            ps.setDate(2, Date.valueOf(p.getDateInscription()));
            ps.setString(3, p.getStatut());
            ps.setBoolean(4, p.isPresence());
            ps.setDate(5, p.getDateCreation() != null ? Date.valueOf(p.getDateCreation()) : Date.valueOf(java.time.LocalDate.now()));
            ps.executeUpdate();
        }
        // Incrémenter nombreInscrits
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE evenements SET nombreInscrits = nombreInscrits + 1 WHERE idEvenement=?")) {
            ps.setInt(1, p.getId_e());
            ps.executeUpdate();
        }
    }

    public void modifier(Participation p) throws SQLException {
        String sql = "UPDATE participations SET statut=?, presence=?, dateModification=? WHERE id_p=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getStatut());
            ps.setBoolean(2, p.isPresence());
            ps.setDate(3, Date.valueOf(java.time.LocalDate.now()));
            ps.setInt(4, p.getId_p());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        // Récupérer id_e avant suppression
        int id_e = 0;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id_e FROM participations WHERE id_p=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) id_e = rs.getInt("id_e");
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM participations WHERE id_p=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        // Décrémenter nombreInscrits
        if (id_e > 0) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE evenements SET nombreInscrits = GREATEST(0, nombreInscrits - 1) WHERE idEvenement=?")) {
                ps.setInt(1, id_e);
                ps.executeUpdate();
            }
        }
    }

    public List<Participation> recuperer() throws SQLException {
        List<Participation> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM participations ORDER BY dateInscription DESC")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Participation> recupererParEvenement(int idEvenement) throws SQLException {
        List<Participation> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM participations WHERE id_e=?")) {
            ps.setInt(1, idEvenement);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Participation mapRow(ResultSet rs) throws SQLException {
        Participation p = new Participation();
        p.setId_p(rs.getInt("id_p"));
        p.setId_e(rs.getInt("id_e"));
        Date di = rs.getDate("dateInscription");
        p.setDateInscription(di != null ? di.toLocalDate() : null);
        p.setStatut(rs.getString("statut"));
        p.setPresence(rs.getBoolean("presence"));
        Date dc = rs.getDate("dateCreation");
        p.setDateCreation(dc != null ? dc.toLocalDate() : null);
        Date dm = rs.getDate("dateModification");
        p.setDateModification(dm != null ? dm.toLocalDate() : null);
        return p;
    }
}