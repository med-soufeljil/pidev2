package org.soa.tp1.pi_dev_s2.service;

import org.soa.tp1.pi_dev_s2.com.esprit.models.Evenement;
import org.soa.tp1.pi_dev_s2.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService {

    private Connection connection;

    public EvenementService() {
        this.connection = DatabaseConfig.getConnection();
    }

    public void ajouter(Evenement e) throws SQLException {
        String sql = "INSERT INTO evenements (titre, idCategorie, dateEvenement, heureDebut, heureFin, " +
                "lieu, nombrePlacesMax, nombreInscrits, statut, description) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setInt(2, e.getIdCategorie());
            ps.setDate(3, e.getDateEvenement() != null ? Date.valueOf(e.getDateEvenement()) : null);
            ps.setTime(4, e.getHeureDebut()    != null ? Time.valueOf(e.getHeureDebut())    : null);
            ps.setTime(5, e.getHeureFin()      != null ? Time.valueOf(e.getHeureFin())      : null);
            ps.setString(6, e.getLieu());
            ps.setInt(7, e.getNombrePlacesMax());
            ps.setInt(8, e.getNombreInscrits());
            ps.setString(9, e.getStatut());
            ps.setString(10, e.getDescription());
            ps.executeUpdate();
        }
    }

    public void modifier(Evenement e) throws SQLException {
        String sql = "UPDATE evenements SET titre=?, idCategorie=?, dateEvenement=?, heureDebut=?, heureFin=?, " +
                "lieu=?, nombrePlacesMax=?, statut=?, description=? WHERE idEvenement=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setInt(2, e.getIdCategorie());
            ps.setDate(3, e.getDateEvenement() != null ? Date.valueOf(e.getDateEvenement()) : null);
            ps.setTime(4, e.getHeureDebut()    != null ? Time.valueOf(e.getHeureDebut())    : null);
            ps.setTime(5, e.getHeureFin()      != null ? Time.valueOf(e.getHeureFin())      : null);
            ps.setString(6, e.getLieu());
            ps.setInt(7, e.getNombrePlacesMax());
            ps.setString(8, e.getStatut());
            ps.setString(9, e.getDescription());
            ps.setInt(10, e.getIdEvenement());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM evenements WHERE idEvenement=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Evenement> recuperer() throws SQLException {
        List<Evenement> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM evenements ORDER BY dateEvenement DESC")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Evenement recupererParId(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM evenements WHERE idEvenement=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Evenement> recupererPlanifies() throws SQLException {
        List<Evenement> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM evenements WHERE statut='planifie' ORDER BY dateEvenement")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Evenement mapRow(ResultSet rs) throws SQLException {
        Evenement e = new Evenement();
        e.setIdEvenement(rs.getInt("idEvenement"));
        e.setTitre(rs.getString("titre"));
        e.setIdCategorie(rs.getInt("idCategorie"));
        Date d = rs.getDate("dateEvenement");
        e.setDateEvenement(d != null ? d.toLocalDate() : null);
        Time hd = rs.getTime("heureDebut");
        e.setHeureDebut(hd != null ? hd.toLocalTime() : null);
        Time hf = rs.getTime("heureFin");
        e.setHeureFin(hf != null ? hf.toLocalTime() : null);
        e.setLieu(rs.getString("lieu"));
        e.setNombrePlacesMax(rs.getInt("nombrePlacesMax"));
        e.setNombreInscrits(rs.getInt("nombreInscrits"));
        e.setStatut(rs.getString("statut"));
        e.setDescription(rs.getString("description"));
        return e;
    }
}