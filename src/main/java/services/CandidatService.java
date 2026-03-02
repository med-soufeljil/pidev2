package services;

import models.Candidat;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidatService implements IService<Candidat> {

    private final Connection connection;

    public CandidatService() {
        connection = MyDatabase.getInstance().getConnection();
        ensureCandidateColumns();
    }

    @Override
    public void ajouter(Candidat c) throws SQLException {
        String sql = "INSERT INTO candidat (nom, prenom, CIN, tel, adresse, email, cv, statut, ai_analyse, ai_score) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getPrenom());
            ps.setInt(3, c.getCIN());
            ps.setInt(4, c.getTel());
            ps.setString(5, c.getAdresse());
            ps.setString(6, c.getEmail());
            ps.setString(7, c.getCv());
            ps.setString(8, c.getStatut() == null || c.getStatut().isBlank() ? "Nouveau" : c.getStatut());
            ps.setString(9, c.getAiAnalyse());
            ps.setInt(10, c.getAiScore());
            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                c.setIdCandidat(generatedKeys.getInt(1));
            }
        }
    }

    @Override
    public void modifier(Candidat c) throws SQLException {
        String sql = "UPDATE candidat SET nom = ?, prenom = ?, CIN = ?, tel = ?, adresse = ?, email = ?, cv = ?, statut = ?, ai_analyse = ?, ai_score = ? WHERE idCandidat = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getPrenom());
            ps.setInt(3, c.getCIN());
            ps.setInt(4, c.getTel());
            ps.setString(5, c.getAdresse());
            ps.setString(6, c.getEmail());
            ps.setString(7, c.getCv());
            ps.setString(8, c.getStatut() == null || c.getStatut().isBlank() ? "Nouveau" : c.getStatut());
            ps.setString(9, c.getAiAnalyse());
            ps.setInt(10, c.getAiScore());
            ps.setInt(11, c.getIdCandidat());
            ps.executeUpdate();
        }
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
        String sql = """
                SELECT c.*, o.nomOffre
                FROM candidat c
                LEFT JOIN recrutement r ON c.idCandidat = r.idCandidat
                LEFT JOIN offre o ON r.idOffre = o.idOffre
                ORDER BY c.idCandidat DESC
                """;
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Candidat> candidats = new ArrayList<>();

        while (rs.next()) {
            Candidat c = mapCandidat(rs);
            c.setNomOffre(rs.getString("nomOffre"));
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
            return mapCandidat(rs);
        }
        return null;
    }

    private Candidat mapCandidat(ResultSet rs) throws SQLException {
        Candidat c = new Candidat();
        c.setIdCandidat(rs.getInt("idCandidat"));
        c.setNom(rs.getString("nom"));
        c.setPrenom(rs.getString("prenom"));
        c.setCIN(rs.getInt("CIN"));
        c.setTel(rs.getInt("tel"));
        c.setAdresse(rs.getString("adresse"));
        c.setEmail(rs.getString("email"));
        c.setCv(rs.getString("cv"));
        c.setStatut(rs.getString("statut"));
        c.setAiAnalyse(rs.getString("ai_analyse"));
        c.setAiScore(rs.getInt("ai_score"));
        return c;
    }

    private void ensureCandidateColumns() {
        addColumnIfMissing("ALTER TABLE candidat ADD COLUMN statut VARCHAR(60) NOT NULL DEFAULT 'Nouveau'");
        addColumnIfMissing("ALTER TABLE candidat ADD COLUMN ai_analyse TEXT NULL");
        addColumnIfMissing("ALTER TABLE candidat ADD COLUMN ai_score INT NOT NULL DEFAULT 0");
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("UPDATE candidat SET statut='Nouveau' WHERE statut IS NULL OR statut = ''");
        } catch (SQLException ignored) {
        }
    }

    private void addColumnIfMissing(String ddl) {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(ddl);
        } catch (SQLException ignored) {
        }
    }
}
