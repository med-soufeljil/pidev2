// Le package "services" sert à regrouper les classes qui gèrent
// la logique métier et l’accès à la base de données.
package services;

// Importation de la classe Apprenant (entité représentant un apprenant)
import entities.Apprenant;

// Importation de la classe MyDatabase qui gère la connexion à la base de données
import utils.MyDatabase;

// Importation de toutes les classes nécessaires pour travailler avec SQL
import java.sql.*;

// Importation de LocalDate pour manipuler des dates (sans heure)
import java.time.LocalDate;

// Importation de ArrayList (liste dynamique)
import java.util.ArrayList;

// Importation de List (interface représentant une liste)
import java.util.List;

// Déclaration de la classe ApprenantService
// Cette classe contient toutes les méthodes pour manipuler la table "apprenant"
public class ApprenantService {

    // Déclaration d’un objet Connection
    // Il représente la connexion avec la base de données
    private Connection connection;

    // Constructeur de la classe ApprenantService
    public ApprenantService() {
        // On récupère une seule instance de MyDatabase (singleton)
        // puis on récupère la connexion à la base de données
        connection = MyDatabase.getInstance().getConnection();
    }

    // ============================
    // MÉTHODE AJOUTER (INSERT)
    // ============================
    // Cette méthode permet d’ajouter un apprenant dans la base de données
    public void ajouter(Apprenant a) throws SQLException {

        // Requête SQL d’insertion avec des paramètres (?)
        String sql = "INSERT INTO apprenant (nom, prenom, email, statut, date_debut, date_fin, id_formation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Préparation de la requête SQL
        PreparedStatement ps = connection.prepareStatement(sql);

        // On remplace chaque ? par une valeur de l’objet Apprenant
        ps.setString(1, a.getNom());          // 1er ? → nom
        ps.setString(2, a.getPrenom());       // 2e ? → prénom
        ps.setString(3, a.getEmail());        // 3e ? → email
        ps.setString(4, a.getStatut());       // 4e ? → statut

        // Conversion LocalDate → Date (SQL) si la date n’est pas nulle
        ps.setDate(5, a.getDateDebut() != null ? Date.valueOf(a.getDateDebut()) : null);

        // Conversion LocalDate → Date (SQL) si la date n’est pas nulle
        ps.setDate(6, a.getDateFin() != null ? Date.valueOf(a.getDateFin()) : null);

        // 7e ? → id de la formation
        ps.setInt(7, a.getId_formation());

        // Exécution de la requête (INSERT)
        ps.executeUpdate();
    }

    // ============================
    // MÉTHODE MODIFIER (UPDATE)
    // ============================
    // Cette méthode permet de modifier un apprenant existant
    public void modifier(Apprenant a) throws SQLException {

        // Requête SQL de mise à jour
        String sql = "UPDATE apprenant SET nom=?, prenom=?, email=?, statut=?, date_debut=?, date_fin=?, id_formation=? " +
                "WHERE id_apprenant=?";

        // Préparation de la requête SQL
        PreparedStatement ps = connection.prepareStatement(sql);

        // Remplacement des paramètres ?
        ps.setString(1, a.getNom());          // nom
        ps.setString(2, a.getPrenom());       // prénom
        ps.setString(3, a.getEmail());        // email
        ps.setString(4, a.getStatut());       // statut

        // Conversion LocalDate → Date pour date_debut
        ps.setDate(5, a.getDateDebut() != null ? Date.valueOf(a.getDateDebut()) : null);

        // Conversion LocalDate → Date pour date_fin
        ps.setDate(6, a.getDateFin() != null ? Date.valueOf(a.getDateFin()) : null);

        // id de la formation
        ps.setInt(7, a.getId_formation());

        // id de l’apprenant (condition WHERE)
        ps.setInt(8, a.getIdApprenant());

        // Exécution de la requête UPDATE
        ps.executeUpdate();
    }

    // ============================
    // MÉTHODE SUPPRIMER (DELETE)
    // ============================
    // Cette méthode permet de supprimer un apprenant par son id
    public void supprimer(int id) throws SQLException {

        // Requête SQL de suppression
        String sql = "DELETE FROM apprenant WHERE id_apprenant=?";

        // Préparation de la requête
        PreparedStatement ps = connection.prepareStatement(sql);

        // Remplacement du ? par l’id reçu en paramètre
        ps.setInt(1, id);

        // Exécution de la requête DELETE
        ps.executeUpdate();
    }

    // ============================
    // MÉTHODE RÉCUPÉRER (SELECT)
    // ============================
    // Cette méthode permet de récupérer tous les apprenants de la base
    public List<Apprenant> recuperer() throws SQLException {

        // Requête SQL de sélection
        String sql = "SELECT * FROM apprenant";

        // Création d’un objet Statement pour exécuter la requête
        Statement st = connection.createStatement();

        // Exécution de la requête et stockage du résultat dans un ResultSet
        ResultSet rs = st.executeQuery(sql);

        // Création d’une liste pour stocker les apprenants
        List<Apprenant> apprenants = new ArrayList<>();

        // Tant qu’il reste des lignes dans le résultat
        while (rs.next()) {

            // Création d’un nouvel objet Apprenant
            Apprenant a = new Apprenant();

            // Récupération des valeurs depuis la base de données
            a.setIdApprenant(rs.getInt("id_apprenant"));
            a.setNom(rs.getString("nom"));
            a.setPrenom(rs.getString("prenom"));
            a.setEmail(rs.getString("email"));
            a.setStatut(rs.getString("statut"));

            // Récupération de la date_debut (SQL Date)
            Date db = rs.getDate("date_debut");

            // Conversion Date (SQL) → LocalDate si non nulle
            a.setDateDebut(db != null ? db.toLocalDate() : null);

            // Récupération de la date_fin
            Date df = rs.getDate("date_fin");

            // Conversion Date (SQL) → LocalDate si non nulle
            a.setDateFin(df != null ? df.toLocalDate() : null);

            // Récupération de l’id de la formation
            a.setId_formation(rs.getInt("id_formation"));

            // Ajout de l’apprenant dans la liste
            apprenants.add(a);
        }

        // Retour de la liste des apprenants
        return apprenants;
    }
}