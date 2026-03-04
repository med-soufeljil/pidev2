package org.soa.tp1.pi_dev_s2.service;

import org.soa.tp1.pi_dev_s2.dao.UtilisateurDAO;
import org.soa.tp1.pi_dev_s2.model.Utilisateur;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UtilisateurService {
    private UtilisateurDAO userDAO;

    public UtilisateurService() {
        this.userDAO = new UtilisateurDAO();
    }

    public void addUser(Utilisateur user) throws SQLException {
        userDAO.addUser(user);
    }

    public List<Utilisateur> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    public void updateUser(Utilisateur user) throws SQLException {
        userDAO.updateUser(user);
    }

    public void deleteUser(int id) throws SQLException {
        userDAO.deleteUser(id);
    }
    public int emailExists(String email) {
        return userDAO.emailExists(email);
    }

    public int emailExistsForOtherUser(String email, int id) {
        return userDAO.emailExistsForOtherUser(email, id);
    }


    public List<Utilisateur> getAllCandidates() throws SQLException {
        return userDAO.getAllCandidates();
    }

    public List<Utilisateur> getAllEmployees() throws SQLException {
        return userDAO.getAllEmployees();

    }

    public void validateCandidate(int id) throws SQLException {
        userDAO.validateCandidate(id);
    }

    public void updateCandidateCV(int id, String newCV) throws SQLException {
        userDAO.updateCandidateCV(id,newCV);
    }
}
