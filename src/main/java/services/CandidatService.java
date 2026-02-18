package services;

import models.Candidat;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidatService implements IService<Candidat> {

    private Connection connection;

    public CandidatService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Candidat c) throws SQLException {
        String sql = "INSERT INTO candidat (nom, prenom, CIN, tel, adresse, email, cv) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getPrenom());
        ps.setInt(3, c.getCIN());
        ps.setInt(4, c.getTel());
        ps.setString(5, c.getAdresse());
        ps.setString(6, c.getEmail());
        ps.setString(7, c.getCv());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Candidat c) throws SQLException {
        String sql = "UPDATE candidat SET nom = ?, prenom = ?, CIN = ?, tel = ?, adresse = ?, email = ?, cv = ? WHERE idCandidat = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getPrenom());
        ps.setInt(3, c.getCIN());
        ps.setInt(4, c.getTel());
        ps.setString(5, c.getAdresse());
        ps.setString(6, c.getEmail());
        ps.setString(7, c.getCv());
        ps.setInt(8, c.getIdCandidat());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM candidat WHERE idCandidat = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Candidat> recuperer() throws SQLException {
        String sql = "SELECT * FROM candidat";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Candidat> candidats = new ArrayList<>();

        while (rs.next()) {
            Candidat c = new Candidat();
            c.setIdCandidat(rs.getInt("idCandidat"));
            c.setNom(rs.getString("nom"));
            c.setPrenom(rs.getString("prenom"));
            c.setCIN(rs.getInt("CIN"));
            c.setTel(rs.getInt("tel"));
            c.setAdresse(rs.getString("adresse"));
            c.setEmail(rs.getString("email"));
            c.setCv(rs.getString("cv"));

            candidats.add(c);
        }
        return candidats;
    }
    public Candidat getById(int id) throws SQLException {
        String sql = "SELECT * FROM candidat WHERE idCandidat = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Candidat c = new Candidat();
            c.setIdCandidat(rs.getInt("idCandidat"));
            c.setNom(rs.getString("nom"));
            c.setPrenom(rs.getString("prenom"));
            c.setCIN(rs.getInt("CIN"));
            c.setTel(rs.getInt("tel"));
            c.setAdresse(rs.getString("adresse"));
            c.setEmail(rs.getString("email"));
            c.setCv(rs.getString("cv"));
            return c;
        }
        return null;
    }

}

