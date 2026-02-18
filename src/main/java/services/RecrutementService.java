package services;

import models.Recrutement;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecrutementService implements IService<Recrutement> {

    private Connection connection;

    public RecrutementService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Recrutement r) throws SQLException {
        String sql = "INSERT INTO recrutement (idOffre, idCandidat) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, r.getIdOffre());
            ps.setInt(2, r.getIdCandidat());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(Recrutement r) throws SQLException {
        String sql = "UPDATE recrutement SET idOffre = ?, idCandidat = ? WHERE idRec = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, r.getIdOffre());
            ps.setInt(2, r.getIdCandidat());
            ps.setInt(3, r.getIdRec());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM recrutement WHERE idRec = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Recrutement> recuperer() throws SQLException {
        String sql = """
            SELECT r.idRec, r.idOffre, r.idCandidat, 
                   o.nomOffre, CONCAT(c.nom, ' ', c.prenom) AS nomCandidat
            FROM recrutement r
            JOIN candidat c ON r.idCandidat = c.idCandidat
            JOIN offre o ON r.idOffre = o.idOffre
            """;

        List<Recrutement> recrutements = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Recrutement r = new Recrutement();
                r.setIdRec(rs.getInt("idRec"));
                r.setIdOffre(rs.getInt("idOffre"));
                r.setIdCandidat(rs.getInt("idCandidat"));
                r.setNomOffre(rs.getString("nomOffre"));
                r.setNomCandidat(rs.getString("nomCandidat"));
                recrutements.add(r);
            }
        }

        return recrutements;
    }


    // 🔥 Méthode avancée avec JOIN
    public void afficherDetailsRecrutement() throws SQLException {
        String sql = """
                SELECT c.nom, c.prenom, o.nomOffre
                FROM recrutement r
                JOIN candidat c ON r.idCandidat = c.idCandidat
                JOIN offre o ON r.idOffre = o.idOffre
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.println(
                        rs.getString("nom") + " " +
                                rs.getString("prenom") +
                                " a postulé à " +
                                rs.getString("nomOffre")
                );
            }
        }
    }
}
