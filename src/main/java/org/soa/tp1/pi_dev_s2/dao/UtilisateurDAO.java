package org.soa.tp1.pi_dev_s2.dao;

import org.soa.tp1.pi_dev_s2.model.Utilisateur;
import org.soa.tp1.pi_dev_s2.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    private Connection connection;

    public UtilisateurDAO() {
        this.connection = DatabaseConfig.getConnection();
        if (this.connection == null) {
            throw new RuntimeException("MySQL non disponible !");
        }
    }
    // ── Mapper réutilisable ──────────────────
    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));               // ✅ id récupéré
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(rs.getString("role"));
        return u;
    }

    // ── Trouver par email ────────────────────
    public Utilisateur findByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Trouver par id ───────────────────────
    public Utilisateur findById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── CREATE ───────────────────────────────
    public void addUser(Utilisateur user) throws SQLException {
        String sql = "INSERT INTO utilisateur(nom, prenom, email, mot_de_passe, role) VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getMotDePasse());
            stmt.setString(5, user.getRole());
            stmt.executeUpdate();
        }
    }

    // ── READ ALL ─────────────────────────────
    public List<Utilisateur> getAllUsers() throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));  // ✅ id inclus
        }
        return list;
    }

    // ── UPDATE ───────────────────────────────
    public void updateUser(Utilisateur user) throws SQLException {
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, mot_de_passe=?, role=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getMotDePasse());
            stmt.setString(5, user.getRole());
            stmt.setInt(6, user.getId());
            stmt.executeUpdate();
        }
    }

    // ── DELETE ───────────────────────────────
    public void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // ── Vérifications email ──────────────────
    public int emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int emailExistsForOtherUser(String email, int id) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ? AND id != ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── Candidats ────────────────────────────
    public List<Utilisateur> getAllCandidates() throws SQLException {
        return getUsersByRole("candidat");
    }

    // ── Employés ─────────────────────────────
    public List<Utilisateur> getAllEmployees() throws SQLException {
        return getUsersByRole("employe");
    }

    private List<Utilisateur> getUsersByRole(String role) throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void validateCandidate(int id) throws SQLException {
        String sql = "UPDATE utilisateur SET role = 'employe' WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void updateCandidateCV(int id, String newCV) throws SQLException {
        String sql = "UPDATE utilisateur SET cv = ? WHERE id = ? AND role = 'candidat'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newCV);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    // ── Stats pour le dashboard ──────────────
    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE role = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM utilisateur";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}