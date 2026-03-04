package org.soa.tp1.pi_dev_s2.service;

import org.soa.tp1.pi_dev_s2.config.DatabaseConfig;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Apprenant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApprenantService {

    private Connection connection;

    public ApprenantService() {
        this.connection = DatabaseConfig.getConnection();
    }

    public void ajouter(Apprenant a) throws SQLException {
        String sql = "INSERT INTO apprenant (nom, prenom, email, statut, date_debut, date_fin, id_formation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getPrenom());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getStatut());
            ps.setDate(5, a.getDateDebut() != null ? Date.valueOf(a.getDateDebut()) : null);
            ps.setDate(6, a.getDateFin()   != null ? Date.valueOf(a.getDateFin())   : null);
            ps.setInt(7, a.getId_formation());
            ps.executeUpdate();
        }
    }

    public void modifier(Apprenant a) throws SQLException {
        String sql = "UPDATE apprenant SET nom=?, prenom=?, email=?, statut=?, date_debut=?, date_fin=?, id_formation=? " +
                "WHERE id_apprenant=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getPrenom());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getStatut());
            ps.setDate(5, a.getDateDebut() != null ? Date.valueOf(a.getDateDebut()) : null);
            ps.setDate(6, a.getDateFin()   != null ? Date.valueOf(a.getDateFin())   : null);
            ps.setInt(7, a.getId_formation());
            ps.setInt(8, a.getIdApprenant());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM apprenant WHERE id_apprenant=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Apprenant> recuperer() throws SQLException {
        List<Apprenant> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM apprenant")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Apprenant> recupererParFormation(int idFormation) throws SQLException {
        List<Apprenant> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM apprenant WHERE id_formation=?")) {
            ps.setInt(1, idFormation);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Apprenant mapRow(ResultSet rs) throws SQLException {
        Apprenant a = new Apprenant();
        a.setIdApprenant(rs.getInt("id_apprenant"));
        a.setNom(rs.getString("nom"));
        a.setPrenom(rs.getString("prenom"));
        a.setEmail(rs.getString("email"));
        a.setStatut(rs.getString("statut"));
        Date dd = rs.getDate("date_debut");
        Date df = rs.getDate("date_fin");
        a.setDateDebut(dd != null ? dd.toLocalDate() : null);
        a.setDateFin(df   != null ? df.toLocalDate() : null);
        a.setId_formation(rs.getInt("id_formation"));
        return a;
    }
}