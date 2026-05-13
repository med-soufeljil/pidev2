package services.event;

import models.event.CategorieEvenement;
import services.IService;
import utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour la gestion des catégories d'événements
 * Implémente l'interface IService pour les opérations CRUD
 */
public class CategorieEvenementService implements IService<CategorieEvenement> {

    private Connection connection; // Connexion à la base de données

    /**
     * Constructeur - initialise la connexion à la base de données
     */
    public CategorieEvenementService() {
        connection = DataSource.getInstance().getConnection();
    }

    /**
     * Ajoute une nouvelle catégorie dans la base de données
     * @param c La catégorie à ajouter
     * @throws SQLException en cas d'erreur SQL
     */
    @Override
    public void ajouter(CategorieEvenement c) throws SQLException {
        String sql = "INSERT INTO categorie_evenement (nomCategorie, description) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, c.getNomCategorie());
        ps.setString(2, c.getDescription());
        ps.executeUpdate();
    }

    /**
     * Modifie une catégorie existante
     * @param c La catégorie avec les nouvelles données
     * @throws SQLException en cas d'erreur SQL
     */
    @Override
    public void modifier(CategorieEvenement c) throws SQLException {
        String sql = "UPDATE categorie_evenement SET nomCategorie=?, description=? WHERE idCategorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, c.getNomCategorie());
        ps.setString(2, c.getDescription());
        ps.setInt(3, c.getIdCategorie());
        ps.executeUpdate();
    }

    /**
     * Supprime une catégorie
     * @param c La catégorie à supprimer
     * @throws SQLException en cas d'erreur SQL
     */

    public void supprimer(CategorieEvenement c) throws SQLException {
        String sql = "DELETE FROM categorie_evenement WHERE idCategorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, c.getIdCategorie());
        ps.executeUpdate();
    }

    /**
     * Supprime une catégorie par son ID (méthode utilitaire)
     * @param id L'ID de la catégorie à supprimer
     * @throws SQLException en cas d'erreur SQL
     */
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM categorie_evenement WHERE idCategorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    /**
     * Récupère toutes les catégories
     * @return Liste de toutes les catégories
     * @throws SQLException en cas d'erreur SQL
     */
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
     * Utilisé pour les ComboBox et la conversion ID/nom
     * @return Map avec id en clé et nom en valeur
     * @throws RuntimeException si impossible de charger les catégories
     */
    public Map<Integer, String> getCategoriesMap() {
        Map<Integer, String> categories = new HashMap<>();
        String sql = "SELECT idCategorie, nomCategorie FROM categorie_evenement";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                categories.put(rs.getInt("idCategorie"), rs.getString("nomCategorie"));
            }

            // Si la table est vide, insérer les catégories par défaut
            if (categories.isEmpty()) {
                System.err.println("WARNING: categorie_evenement table is empty!");
                insertDefaultCategories();
                return getCategoriesMap(); // Récursion pour récupérer après insertion
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to load categories from database: " + e.getMessage());
            e.printStackTrace();

            // Lancer une exception pour signaler le problème
            throw new RuntimeException("Cannot load categories from database. Please check your database connection and table.", e);
        }

        return categories;
    }

    /**
     * Insère les catégories par défaut dans la base de données
     * Méthode privée appelée automatiquement si la table est vide
     */
    private void insertDefaultCategories() {
        try {
            String sql = "INSERT INTO categorie_evenement (nomCategorie, description) VALUES (?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);

            // Insérer les trois catégories de base
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
     * @param id L'ID de la catégorie
     * @return Le nom de la catégorie, ou null si non trouvée
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
     * @param nom Le nom de la catégorie
     * @return L'ID de la catégorie, ou -1 si non trouvée
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