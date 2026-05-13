package services;

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

    public Utilisateur login(String mail, String password) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE mail = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, mail);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public void ajouter(Utilisateur u) throws SQLException {
        String sql = """
                INSERT INTO utilisateur (nom, prenom, cin, mail, tel, photo_profil, role, password)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fill(ps, u);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
        }
    }

    @Override
    public void modifier(Utilisateur u) throws SQLException {
        String sql = """
                UPDATE utilisateur
                SET nom=?, prenom=?, cin=?, mail=?, tel=?, photo_profil=?, role=?, password=?
                WHERE id=?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            fill(ps, u);
            ps.setInt(9, u.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM utilisateur WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Utilisateur> recuperer() throws SQLException {
        List<Utilisateur> users = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM utilisateur ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(map(rs));
        }
        return users;
    }

    private void fill(PreparedStatement ps, Utilisateur u) throws SQLException {
        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setInt(3, u.getCin());
        ps.setString(4, u.getMail());
        ps.setInt(5, u.getTel());
        ps.setString(6, u.getPhotoProfil() == null || u.getPhotoProfil().isBlank() ? "0" : u.getPhotoProfil());
        ps.setString(7, u.getRole() == null || u.getRole().isBlank() ? "EMPLOYE" : u.getRole());
        ps.setString(8, u.getPassword() == null || u.getPassword().isBlank() ? "1234" : u.getPassword());
    }

    private Utilisateur map(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setCin(rs.getInt("cin"));
        u.setMail(rs.getString("mail"));
        u.setTel(rs.getInt("tel"));
        u.setPhotoProfil(rs.getString("photo_profil"));
        u.setRole(rs.getString("role"));
        u.setPassword(rs.getString("password"));
        return u;
    }
}
