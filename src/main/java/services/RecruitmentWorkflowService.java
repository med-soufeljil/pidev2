package services;

import models.Candidat;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RecruitmentWorkflowService {

    public static final String STATUS_NOUVEAU = "Nouveau";
    public static final String STATUS_PREMIER_ENTRETIEN = "Premier entretien";
    public static final String STATUS_DEUXIEME_ENTRETIEN = "Deuxième entretien";
    public static final String STATUS_OFFRE_ENVOYEE = "Offre envoyée";
    public static final String STATUS_ACCEPTEE = "Acceptée";
    public static final String STATUS_REJETEE = "Rejetée";

    private final Connection connection;
    private final EmailApiService emailApiService = new EmailApiService();

    public RecruitmentWorkflowService() {
        this.connection = MyDatabase.getInstance().getConnection();
        ensureWorkflowTable();
    }

    public String getCandidatePhase(int candidateId) throws SQLException {
        String stored = getStoredPhase(candidateId);
        if (stored != null && !stored.isBlank()) {
            return stored;
        }

        boolean hasRecrutement = exists("SELECT 1 FROM recrutement WHERE idCandidat = ?", candidateId);
        boolean hasReunion = exists("SELECT 1 FROM reunion WHERE idCandidat = ?", candidateId);

        if (hasReunion) return STATUS_PREMIER_ENTRETIEN;
        if (hasRecrutement) return STATUS_DEUXIEME_ENTRETIEN;
        return STATUS_NOUVEAU;
    }

    public String getGeneratedOffer(int candidateId) throws SQLException {
        String sql = "SELECT generatedOffer FROM candidat_workflow WHERE idCandidat = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("generatedOffer") : null;
        }
    }

    public String getOfferResponse(int candidateId) throws SQLException {
        String sql = "SELECT offerResponse FROM candidat_workflow WHERE idCandidat = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("offerResponse") : null;
        }
    }

    public String getLatestReunionSummary(int candidateId) throws SQLException {
        String sql = "SELECT date, link FROM reunion WHERE idCandidat = ? ORDER BY date DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return "Date réunion: " + rs.getTimestamp("date") + " | Lien: " + rs.getString("link");
            }
            return "Aucune réunion planifiée";
        }
    }

    public void updateCandidatePhase(int candidateId, String phase) throws SQLException {
        String sql = """
                INSERT INTO candidat_workflow(idCandidat, phase)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE phase=VALUES(phase)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setString(2, phase);
            ps.executeUpdate();
        }
    }

    public void recordOfferResponse(String token, String response) throws SQLException {
        String sql = "UPDATE candidat_workflow SET offerResponse=? WHERE offerResponseToken=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, response);
            ps.setString(2, token);
            ps.executeUpdate();
        }
    }

    public boolean requiresInterviewMeeting(String status) {
        return STATUS_PREMIER_ENTRETIEN.equalsIgnoreCase(status)
                || STATUS_DEUXIEME_ENTRETIEN.equalsIgnoreCase(status);
    }

    public void generateSalaryOfferAndSend(Candidat candidat, int salary) throws Exception {
        String offerLabel = "Offre salaire: " + salary;
        String responseToken = UUID.randomUUID().toString();
        String baseUrl = System.getProperty("offer.response.base.url", System.getenv().getOrDefault("OFFER_RESPONSE_BASE_URL", "http://localhost:8090"));
        String acceptUrl = baseUrl + "/offer/accept?token=" + responseToken;
        String rejectUrl = baseUrl + "/offer/reject?token=" + responseToken;

        saveGeneratedOffer(candidat.getIdCandidat(), offerLabel, responseToken);
        emailApiService.sendOfferEmail(
                candidat.getEmail(),
                candidat.getPrenom() + " " + candidat.getNom(),
                salary,
                acceptUrl,
                rejectUrl
        );
    }

    private void saveGeneratedOffer(int candidateId, String generatedOffer, String responseToken) throws SQLException {
        String sql = """
                INSERT INTO candidat_workflow(idCandidat, phase, generatedOffer, offerResponseToken)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE phase=VALUES(phase), generatedOffer=VALUES(generatedOffer), offerResponseToken=VALUES(offerResponseToken)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setString(2, STATUS_OFFRE_ENVOYEE);
            ps.setString(3, generatedOffer);
            ps.setString(4, responseToken);
            ps.executeUpdate();
        }
    }

    private String getStoredPhase(int candidateId) throws SQLException {
        String sql = "SELECT phase FROM candidat_workflow WHERE idCandidat = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("phase") : null;
        }
    }

    private boolean exists(String sql, int candidateId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    private void ensureWorkflowTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS candidat_workflow (
                    idCandidat INT PRIMARY KEY,
                    phase VARCHAR(60) NOT NULL,
                    generatedOffer VARCHAR(255),
                    offerResponse VARCHAR(60),
                    offerResponseToken VARCHAR(100),
                    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }

        try (PreparedStatement ps = connection.prepareStatement("ALTER TABLE candidat_workflow ADD COLUMN offerResponse VARCHAR(60) NULL")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {}

        try (PreparedStatement ps = connection.prepareStatement("ALTER TABLE candidat_workflow ADD COLUMN offerResponseToken VARCHAR(100) NULL")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }
}
