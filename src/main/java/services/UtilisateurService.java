package services;

import models.Role;
import models.Utilisateur;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService implements IService<Utilisateur> {

    private final Connection connection;

    public UtilisateurService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateur (nom, prenom, mail, role, password) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getMail());
            ps.setString(4, u.getRole().name());
            ps.setString(5, u.getPassword());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, mail=?, role=?, password=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getMail());
            ps.setString(4, u.getRole().name());
            ps.setString(5, u.getPassword());
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Utilisateur> recuperer() throws SQLException {
        String sql = "SELECT * FROM utilisateur";
        List<Utilisateur> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }

    /** Find user by mail (case-insensitive). Returns null if not found. */
    public Utilisateur findByMail(String mail) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE mail = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, mail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Authenticates a user.
     * Returns the Utilisateur on success, or null if credentials are wrong.
     */
    public Utilisateur authenticate(String mail, String password) throws SQLException {
        Utilisateur u = findByMail(mail);
        if (u != null && u.getPassword().equals(password))
            return u;
        return null;
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setMail(rs.getString("mail"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setPassword(rs.getString("password"));
        return u;
    }
}
