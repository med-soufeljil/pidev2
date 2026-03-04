package services;

import models.Personne;
import models.Soldeconges;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SoldeCongeService implements IService<Soldeconges> {

    private Connection connection;

    public SoldeCongeService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ✅ AJOUT
    public void ajouter(Soldeconges s) throws SQLException {
        String sql = "INSERT INTO solde_conge (id_employe, total_annuel, utilise_annuel, restant_annuel) " +
                "VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, s.getIdEmploye());
        ps.setInt(2, s.getTotalAnnuel());
        ps.setInt(3, s.getUtiliseAnnuel());
        ps.setInt(4, s.getRestantAnnuel());
        ps.executeUpdate();
    }

    // ✅ MODIFIER
    public void modifier(Soldeconges s) throws SQLException {
        String sql = "UPDATE solde_conge SET id_employe=?, total_annuel=?, utilise_annuel=?, restant_annuel=? " +
                "WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, s.getIdEmploye());
        ps.setInt(2, s.getTotalAnnuel());
        ps.setInt(3, s.getUtiliseAnnuel());
        ps.setInt(4, s.getRestantAnnuel());
        ps.setInt(5, s.getId());
        ps.executeUpdate();
    }

    // ✅ SUPPRIMER
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM solde_conge WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ✅ RECUPERER
    public List<Soldeconges> recuperer() throws SQLException {
        String sql = "SELECT * FROM solde_conge";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<Soldeconges> soldes = new ArrayList<>();

        while (rs.next()) {
            Soldeconges s = new Soldeconges();
            s.setId(rs.getInt("id"));
            s.setIdEmploye(rs.getInt("id_employe"));
            s.setTotalAnnuel(rs.getInt("total_annuel"));
            s.setUtiliseAnnuel(rs.getInt("utilise_annuel"));
            s.setRestantAnnuel(rs.getInt("restant_annuel"));
            s.setMisAJourLe(rs.getTimestamp("mis_a_jour_le").toLocalDateTime());

            soldes.add(s);
        }
        return soldes;
    }
}
