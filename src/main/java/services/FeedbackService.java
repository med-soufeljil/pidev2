package services;

import entities.FormationFeedback;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackService {
    private final Connection connection;

    public FeedbackService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public void addFeedback(FormationFeedback feedback) throws SQLException {
        String primarySql = "INSERT INTO formation_feedback (id_formation, author, rating, comment_text, created_at) VALUES (?, ?, ?, ?, ?)";
        String fallbackSql = "INSERT INTO formation_feedback (id_formation, author, rating, comment, created_at) VALUES (?, ?, ?, ?, ?)";

        SQLException last = null;
        for (String sql : new String[]{primarySql, fallbackSql}) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, feedback.getFormationId());
                ps.setString(2, feedback.getAuthor());
                ps.setInt(3, feedback.getRating());
                ps.setString(4, feedback.getComment());
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
                return;
            } catch (SQLException e) {
                last = e;
            }
        }
        throw last;
    }

    public List<FormationFeedback> getByFormation(int formationId) throws SQLException {
        String primarySql = "SELECT id, id_formation, author, rating, comment_text, created_at FROM formation_feedback WHERE id_formation=? ORDER BY created_at DESC";
        String fallbackSql = "SELECT id, id_formation, author, rating, comment, created_at FROM formation_feedback WHERE id_formation=? ORDER BY created_at DESC";

        SQLException last = null;
        for (String sql : new String[]{primarySql, fallbackSql}) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, formationId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<FormationFeedback> result = new ArrayList<>();
                    while (rs.next()) {
                        FormationFeedback fb = new FormationFeedback();
                        fb.setId(rs.getInt("id"));
                        fb.setFormationId(rs.getInt("id_formation"));
                        fb.setAuthor(rs.getString("author"));
                        fb.setRating(rs.getInt("rating"));
                        String comment;
                        try {
                            comment = rs.getString("comment_text");
                        } catch (SQLException ignored) {
                            comment = rs.getString("comment");
                        }
                        fb.setComment(comment);
                        Timestamp ts = rs.getTimestamp("created_at");
                        fb.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
                        result.add(fb);
                    }
                    return result;
                }
            } catch (SQLException e) {
                last = e;
            }
        }
        throw last;
    }

    public double getAverageRating(int formationId) throws SQLException {
        String sql = "SELECT AVG(rating) FROM formation_feedback WHERE id_formation=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0;
    }
}
