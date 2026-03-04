package org.soa.tp1.pi_dev_s2.com.esprit.services;

import org.soa.tp1.pi_dev_s2.com.esprit.models.CategorieEvenement;
import org.soa.tp1.pi_dev_s2.com.esprit.utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategorieEvenementService implements IService<CategorieEvenement> {

    private Connection connection;

    public CategorieEvenementService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void ajouter(CategorieEvenement c) throws SQLException {
        String sql = "INSERT INTO categorie_evenement (nomCategorie, description) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, c.getNomCategorie());
        ps.setString(2, c.getDescription());
        ps.executeUpdate();
    }

    @Override
    public void modifier(CategorieEvenement c) throws SQLException {
        String sql = "UPDATE categorie_evenement SET nomCategorie=?, description=? WHERE idCategorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, c.getNomCategorie());
        ps.setString(2, c.getDescription());
        ps.setInt(3, c.getIdCategorie());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(CategorieEvenement c) throws SQLException {
        String sql = "DELETE FROM categorie_evenement WHERE idCategorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, c.getIdCategorie());
        ps.executeUpdate();
    }

    // Helper method for tests that expect delete by ID
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM categorie_evenement WHERE idCategorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<CategorieEvenement> recuperer() throws SQLException {
        String sql = "SELECT * FROM categorie_evenement";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<CategorieEvenement> categories = new ArrayList<>();
        while (rs.next()) {
            CategorieEvenement c = new CategorieEvenement();
            c.setIdCategorie(rs.getInt("idCategorie"));
            c.setNomCategorie(rs.getString("nomCategorie"));
            c.setDescription(rs.getString("description"));
            categories.add(c);
        }
        return categories;
    }

    /**
     * Récupère toutes les catégories sous forme de Map (id -> nom)
     */
    public Map<Integer, String> getCategoriesMap() {
        Map<Integer, String> categories = new HashMap<>();
        String sql = "SELECT idCategorie, nomCategorie FROM categorie_evenement";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                categories.put(rs.getInt("idCategorie"), rs.getString("nomCategorie"));
            }

            // If the table is empty, log a warning but don't use defaults
            if (categories.isEmpty()) {
                System.err.println("WARNING: categorie_evenement table is empty!");
                // You might want to insert default categories here
                insertDefaultCategories();
                // Try again
                return getCategoriesMap();
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to load categories from database: " + e.getMessage());
            e.printStackTrace();

            // Instead of returning defaults that might be incomplete,
            // we throw a runtime exception to make the problem visible
            throw new RuntimeException("Cannot load categories from database. Please check your database connection and table.", e);
        }

        return categories;
    }

    /**
     * Insert default categories if the table is empty
     */
    private void insertDefaultCategories() {
        try {
            String sql = "INSERT INTO categorie_evenement (nomCategorie, description) VALUES (?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);

            // Insert all three categories
            ps.setString(1, "conference");
            ps.setString(2, "Conférences et séminaires");
            ps.executeUpdate();

            ps.setString(1, "webinaire");
            ps.setString(2, "Webinaires en ligne");
            ps.executeUpdate();

            ps.setString(1, "team building");
            ps.setString(2, "Activités de team building");
            ps.executeUpdate();

            System.out.println("Default categories inserted successfully");
        } catch (SQLException e) {
            System.err.println("Failed to insert default categories: " + e.getMessage());
        }
    }

    /**
     * Récupère le nom d'une catégorie à partir de son ID
     */
    public String getCategorieNameById(int id) {
        String sql = "SELECT nomCategorie FROM categorie_evenement WHERE idCategorie = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nomCategorie");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère l'ID d'une catégorie à partir de son nom
     */
    public int getCategorieIdByName(String nom) {
        String sql = "SELECT idCategorie FROM categorie_evenement WHERE nomCategorie = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idCategorie");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}