// Le package "services" contient les classes qui gèrent
// l’accès à la base de données et la logique métier.
package services;

// Importation de la classe Formation (entité représentant une formation)
import entities.Formation;

// Importation de l’énumération Niveau (ex: DEBUTANT, INTERMEDIAIRE, AVANCE)
import entities.Niveau;

// Importation de l’énumération Categorie (ex: INFORMATIQUE, MANAGEMENT, etc.)
import entities.Categorie;

// Importation de la classe MyDatabase qui gère la connexion à la base de données
import utils.MyDatabase;

// Importation de toutes les classes nécessaires pour travailler avec SQL
import java.sql.*;

// Importation de ArrayList (liste dynamique)
import java.util.ArrayList;

// Importation de List (interface représentant une liste)
import java.util.List;

// Déclaration de la classe FormationService
// Cette classe permet de gérer la table "formation" dans la base de données
public class FormationService {

    // Déclaration d’un objet Connection
    // Il représente la connexion active avec la base de données
    private Connection connection;

    // Constructeur de la classe FormationService
    public FormationService() {
        // On récupère l’unique instance de MyDatabase (singleton)
        // puis on récupère la connexion à la base de données
        connection = MyDatabase.getInstance().getConnection();
    }

    // ============================
    // MÉTHODE AJOUTER (INSERT)
    // ============================
    // Cette méthode permet d’ajouter une formation dans la base de données
    public void ajouter(Formation f) throws SQLException {

        // Requête SQL d’insertion avec des paramètres ?
        String sql = "INSERT INTO formation (titre, description, duree, niveau, categorie, certification) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        // Préparation de la requête SQL
        PreparedStatement ps = connection.prepareStatement(sql);

        // Remplacement des ? par les valeurs de l’objet Formation
        ps.setString(1, f.getTitre());              // 1er ? → titre
        ps.setString(2, f.getDescription());        // 2e ? → description
        ps.setInt(3, f.getDuree());                 // 3e ? → durée
        ps.setString(4, f.getNiveau().name());      // 4e ? → niveau (enum → String)
        ps.setString(5, f.getCategorie().name());   // 5e ? → catégorie (enum → String)
        ps.setBoolean(6, f.isCertification());      // 6e ? → certification (true/false)

        // Exécution de la requête INSERT
        ps.executeUpdate();
    }

    // ============================
    // MÉTHODE MODIFIER (UPDATE)
    // ============================
    // Cette méthode permet de modifier une formation existante
    public void modifier(Formation f) throws SQLException {

        // Requête SQL de mise à jour avec condition WHERE
        String sql = "UPDATE formation SET titre=?, description=?, duree=?, niveau=?, categorie=?, certification=? " +
                "WHERE id_formation=?";

        // Préparation de la requête SQL
        PreparedStatement ps = connection.prepareStatement(sql);

        // Remplacement des ? par les valeurs de l’objet Formation
        ps.setString(1, f.getTitre());              // nouveau titre
        ps.setString(2, f.getDescription());        // nouvelle description
        ps.setInt(3, f.getDuree());                 // nouvelle durée
        ps.setString(4, f.getNiveau().name());      // nouveau niveau
        ps.setString(5, f.getCategorie().name());   // nouvelle catégorie
        ps.setBoolean(6, f.isCertification());      // nouvelle valeur certification

        // Condition WHERE (id de la formation à modifier)
        ps.setInt(7, f.getId_formation());

        // Exécution de la requête UPDATE
        ps.executeUpdate();
    }

    // ============================
    // MÉTHODE SUPPRIMER (DELETE)
    // ============================
    // Cette méthode permet de supprimer une formation par son id
    public void supprimer(int id) throws SQLException {

        // Requête SQL de suppression
        String sql = "DELETE FROM formation WHERE id_formation=?";

        // Préparation de la requête SQL
        PreparedStatement ps = connection.prepareStatement(sql);

        // Remplacement du ? par l’id reçu en paramètre
        ps.setInt(1, id);

        // Exécution de la requête DELETE
        ps.executeUpdate();
    }

    // ============================
    // MÉTHODE RÉCUPÉRER (SELECT)
    // ============================
    // Cette méthode permet de récupérer toutes les formations depuis la base de données
    public List<Formation> recuperer() throws SQLException {

        // Requête SQL de sélection
        String sql = "SELECT * FROM formation";

        // Création d’un Statement pour exécuter la requête
        Statement st = connection.createStatement();

        // Exécution de la requête et récupération du résultat
        ResultSet rs = st.executeQuery(sql);

        // Création d’une liste pour stocker les formations
        List<Formation> formations = new ArrayList<>();

        // Parcours de toutes les lignes du ResultSet
        while (rs.next()) {

            // Création d’un nouvel objet Formation
            Formation f = new Formation();

            // Récupération des valeurs depuis la base de données
            f.setId_formation(rs.getInt("id_formation"));
            f.setTitre(rs.getString("titre"));
            f.setDescription(rs.getString("description"));
            f.setDuree(rs.getInt("duree"));

            // Récupération des valeurs "niveau" et "categorie" sous forme String
            String niveauDB = rs.getString("niveau");
            String categorieDB = rs.getString("categorie");

            // Conversion String → Enum Niveau
            f.setNiveau(Niveau.valueOf(niveauDB.toUpperCase()));

            // Conversion String → Enum Categorie
            f.setCategorie(Categorie.valueOf(categorieDB.toUpperCase()));

            // Récupération du champ certification (true/false)
            f.setCertification(rs.getBoolean("certification"));

            // Ajout de la formation dans la liste
            formations.add(f);
        }

        // Retour de la liste des formations
        return formations;
    }
}