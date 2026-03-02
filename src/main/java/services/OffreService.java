package services;

import models.Offre;
import models.TypeOffre;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffreService implements IService<Offre> {

    private Connection connection;

    public OffreService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Offre offre) throws SQLException {
        String sql = "INSERT INTO offre (nomOffre, type, competences, salaire) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, offre.getNomOffre());
        ps.setString(2, offre.getType().name()); // ✅ ENUM → String
        ps.setString(3, offre.getCompetences());
        ps.setInt(4, offre.getSalaire());

        ps.executeUpdate();
    }

    @Override
    public void modifier(Offre offre) throws SQLException {
        String sql = "UPDATE offre SET nomOffre = ?, type = ?, competences = ?, salaire = ? WHERE idOffre = ?";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, offre.getNomOffre());
        ps.setString(2, offre.getType().name()); // ✅ ENUM → String
        ps.setString(3, offre.getCompetences());
        ps.setInt(4, offre.getSalaire());
        ps.setInt(5, offre.getIdOffre());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM offre WHERE idOffre = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Offre> recuperer() throws SQLException {
        String sql = "SELECT * FROM offre";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Offre> offres = new ArrayList<>();

        while (rs.next()) {
            Offre o = new Offre();

            o.setIdOffre(rs.getInt("idOffre"));
            o.setNomOffre(rs.getString("nomOffre"));

            // ✅ String (DB) → ENUM
            o.setType(TypeOffre.valueOf(rs.getString("type")));

            o.setCompetences(rs.getString("competences"));
            o.setSalaire(rs.getInt("salaire"));

            offres.add(o);
        }

        return offres;
    }
}
