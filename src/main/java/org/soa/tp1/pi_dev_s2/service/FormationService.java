package org.soa.tp1.pi_dev_s2.service;

import org.soa.tp1.pi_dev_s2.config.DatabaseConfig;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Categorie;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Formation;
import org.soa.tp1.pi_dev_s2.mouhamd.entities.Niveau;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationService {

    private Connection connection;

    public FormationService() {
        // ✅ Utilise la même DatabaseConfig que le reste du projet
        this.connection = DatabaseConfig.getConnection();
    }

    public void ajouter(Formation f) throws SQLException {
        String sql = "INSERT INTO formation (titre, description, duree, niveau, categorie, certification) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, f.getTitre());
            ps.setString(2, f.getDescription());
            ps.setInt(3, f.getDuree());
            ps.setString(4, f.getNiveau().name());
            ps.setString(5, f.getCategorie().name());
            ps.setBoolean(6, f.isCertification());
            ps.executeUpdate();
        }
    }

    public void modifier(Formation f) throws SQLException {
        String sql = "UPDATE formation SET titre=?, description=?, duree=?, niveau=?, categorie=?, certification=? " +
                "WHERE id_formation=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, f.getTitre());
            ps.setString(2, f.getDescription());
            ps.setInt(3, f.getDuree());
            ps.setString(4, f.getNiveau().name());
            ps.setString(5, f.getCategorie().name());
            ps.setBoolean(6, f.isCertification());
            ps.setInt(7, f.getId_formation());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        boolean auto = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM formation_feedback WHERE id_formation=?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM apprenant WHERE id_formation=?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM formation WHERE id_formation=?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(auto);
        }
    }

    public List<Formation> recuperer() throws SQLException {
        List<Formation> list = new ArrayList<>();
        String sql = "SELECT * FROM formation";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Formation f = new Formation();
                f.setId_formation(rs.getInt("id_formation"));
                f.setTitre(rs.getString("titre"));
                f.setDescription(rs.getString("description"));
                f.setDuree(rs.getInt("duree"));
                f.setNiveau(Niveau.valueOf(rs.getString("niveau")));
                f.setCategorie(Categorie.valueOf(rs.getString("categorie")));
                f.setCertification(rs.getBoolean("certification"));
                list.add(f);
            }
        }
        return list;
    }

    public Formation recupererParId(int id) throws SQLException {
        String sql = "SELECT * FROM formation WHERE id_formation=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Formation f = new Formation();
                f.setId_formation(rs.getInt("id_formation"));
                f.setTitre(rs.getString("titre"));
                f.setDescription(rs.getString("description"));
                f.setDuree(rs.getInt("duree"));
                f.setNiveau(Niveau.valueOf(rs.getString("niveau")));
                f.setCategorie(Categorie.valueOf(rs.getString("categorie")));
                f.setCertification(rs.getBoolean("certification"));
                return f;
            }
        }
        return null;
    }
}