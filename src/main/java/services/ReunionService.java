package services;

import models.Reunion;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReunionService implements IService<Reunion> {

    private Connection connection;

    public ReunionService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Reunion r) throws SQLException {
        String sql = "INSERT INTO reunion (idRH, idCandidat, date, link) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setInt(1, r.getIdRH());
        ps.setInt(2, r.getIdCandidat());
        ps.setTimestamp(3, Timestamp.valueOf(r.getDate())); // LocalDateTime → SQL
        ps.setString(4, r.getLink());

        ps.executeUpdate();
    }

    @Override
    public void modifier(Reunion r) throws SQLException {
        String sql = "UPDATE reunion SET idRH = ?, idCandidat = ?, date = ?, link = ? WHERE idReunion = ?";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setInt(1, r.getIdRH());
        ps.setInt(2, r.getIdCandidat());
        ps.setTimestamp(3, Timestamp.valueOf(r.getDate()));
        ps.setString(4, r.getLink());
        ps.setInt(5, r.getIdReunion());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM reunion WHERE idReunion = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Reunion> recuperer() throws SQLException {
        String sql = "SELECT * FROM reunion";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Reunion> reunions = new ArrayList<>();

        while (rs.next()) {
            Reunion r = new Reunion();
            r.setIdReunion(rs.getInt("idReunion"));
            r.setIdRH(rs.getInt("idRH"));
            r.setIdCandidat(rs.getInt("idCandidat"));
            r.setDate(rs.getTimestamp("date").toLocalDateTime()); // SQL → LocalDateTime
            r.setLink(rs.getString("link"));

            reunions.add(r);
        }
        return reunions;
    }

}
