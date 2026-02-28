package services;

import models.Candidat;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public boolean requiresInterviewMeeting(String status) {
        return STATUS_PREMIER_ENTRETIEN.equalsIgnoreCase(status)
                || STATUS_DEUXIEME_ENTRETIEN.equalsIgnoreCase(status);
    }

    public void generateSalaryOfferAndSend(Candidat candidat, int salary) throws Exception {
        String offerLabel = "Offre salaire: " + salary;
        String acceptUrl = "https://recruitflow.app/offer/accept?candidate=" + candidat.getIdCandidat();
        String rejectUrl = "https://recruitflow.app/offer/reject?candidate=" + candidat.getIdCandidat();

        saveGeneratedOffer(candidat.getIdCandidat(), offerLabel);
        emailApiService.sendOfferEmail(
                candidat.getEmail(),
                candidat.getPrenom() + " " + candidat.getNom(),
                salary,
                acceptUrl,
                rejectUrl
        );
    }

    private void saveGeneratedOffer(int candidateId, String generatedOffer) throws SQLException {
        String sql = """
                INSERT INTO candidat_workflow(idCandidat, phase, generatedOffer)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE phase=VALUES(phase), generatedOffer=VALUES(generatedOffer)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setString(2, STATUS_OFFRE_ENVOYEE);
            ps.setString(3, generatedOffer);
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
                    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }
}
