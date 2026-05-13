package services.event;

import models.event.Evenement;
import services.IService;
import utils.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des événements
 * Implémente l'interface IService pour les opérations CRUD sur les événements
 */
public class EvenementService implements IService<Evenement> {

    private Connection connection; // Connexion à la base de données

    /**
     * Constructeur - initialise la connexion à la base de données
     */
    public EvenementService() {
        connection = DataSource.getInstance().getConnection();
    }

    /**
     * Ajoute un nouvel événement dans la base de données
     * @param e L'événement à ajouter
     * @throws SQLException en cas d'erreur SQL
     */
    @Override
    public void ajouter(Evenement e) throws SQLException {
        String sql = "INSERT INTO evenements (titre, idCategorie, dateEvenement, heureDebut, heureFin, lieu, " +
                "nombrePlacesMax, statut, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, e.getTitre());
        ps.setInt(2, e.getIdCategorie());
        ps.setDate(3, e.getDateEvenement() != null ? Date.valueOf(e.getDateEvenement()) : null);
        ps.setTime(4, e.getHeureDebut() != null ? Time.valueOf(e.getHeureDebut()) : null);
        ps.setTime(5, e.getHeureFin() != null ? Time.valueOf(e.getHeureFin()) : null);
        ps.setString(6, e.getLieu());
        ps.setInt(7, e.getNombrePlacesMax());
        ps.setString(8, e.getStatut());
        ps.setString(9, e.getDescription());
        ps.executeUpdate();
    }

    /**
     * Modifie un événement existant
     * @param e L'événement avec les nouvelles données
     * @throws SQLException en cas d'erreur SQL
     */
    @Override
    public void modifier(Evenement e) throws SQLException {
        String sql = "UPDATE evenements SET titre=?, idCategorie=?, dateEvenement=?, heureDebut=?, heureFin=?, " +
                "lieu=?, nombrePlacesMax=?, statut=?, description=? WHERE idEvenement=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, e.getTitre());
        ps.setInt(2, e.getIdCategorie());
        ps.setDate(3, e.getDateEvenement() != null ? Date.valueOf(e.getDateEvenement()) : null);
        ps.setTime(4, e.getHeureDebut() != null ? Time.valueOf(e.getHeureDebut()) : null);
        ps.setTime(5, e.getHeureFin() != null ? Time.valueOf(e.getHeureFin()) : null);
        ps.setString(6, e.getLieu());
        ps.setInt(7, e.getNombrePlacesMax());
        ps.setString(8, e.getStatut());
        ps.setString(9, e.getDescription());
        ps.setInt(10, e.getIdEvenement());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {

    }

    /**
     * Supprime un événement
     * @param e L'événement à supprimer
     * @throws SQLException en cas d'erreur SQL
     */

    public void supprimer(Evenement e) throws SQLException {
        String sql = "DELETE FROM evenements WHERE idEvenement=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, e.getIdEvenement());
        ps.executeUpdate();
    }

    /**
     * Récupère tous les événements
     * @return Liste de tous les événements
     * @throws SQLException en cas d'erreur SQL
     */
    @Override
    public List<Evenement> recuperer() throws SQLException {
        String sql = "SELECT * FROM evenements";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<Evenement> evenements = new ArrayList<>();
        while (rs.next()) {
            Evenement e = new Evenement();
            e.setIdEvenement(rs.getInt("idEvenement"));
            e.setTitre(rs.getString("titre"));
            e.setIdCategorie(rs.getInt("idCategorie"));

            Date d = rs.getDate("dateEvenement");
            e.setDateEvenement(d != null ? d.toLocalDate() : null);

            Time hd = rs.getTime("heureDebut");
            e.setHeureDebut(hd != null ? hd.toLocalTime() : null);

            Time hf = rs.getTime("heureFin");
            e.setHeureFin(hf != null ? hf.toLocalTime() : null);

            e.setLieu(rs.getString("lieu"));
            e.setNombrePlacesMax(rs.getInt("nombrePlacesMax"));
            e.setNombreInscrits(rs.getInt("nombreInscrits"));
            e.setStatut(rs.getString("statut"));
            e.setDescription(rs.getString("description"));

            evenements.add(e);
        }
        return evenements;
    }

    /**
     * Récupère un événement par son ID
     * @param id L'ID de l'événement
     * @return L'événement correspondant, ou null si non trouvé
     * @throws SQLException en cas d'erreur SQL
     */
    public Evenement recupererParId(int id) throws SQLException {
        String sql = "SELECT * FROM evenements WHERE idEvenement = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Evenement e = new Evenement();
            e.setIdEvenement(rs.getInt("idEvenement"));
            e.setTitre(rs.getString("titre"));
            e.setIdCategorie(rs.getInt("idCategorie"));
            e.setDateEvenement(rs.getDate("dateEvenement") != null ?
                    rs.getDate("dateEvenement").toLocalDate() : null);
            e.setHeureDebut(rs.getTime("heureDebut") != null ?
                    rs.getTime("heureDebut").toLocalTime() : null);
            e.setHeureFin(rs.getTime("heureFin") != null ?
                    rs.getTime("heureFin").toLocalTime() : null);
            e.setLieu(rs.getString("lieu"));
            e.setNombrePlacesMax(rs.getInt("nombrePlacesMax"));
            e.setNombreInscrits(rs.getInt("nombreInscrits"));
            e.setStatut(rs.getString("statut"));
            e.setDescription(rs.getString("description"));
            return e;
        }
        return null;
    }

    /**
     * Met à jour automatiquement le statut des événements en fonction de la date et heure actuelles
     * Fonctionnalité avancée : gestion dynamique des statuts
     * @throws SQLException en cas d'erreur SQL
     */
    public void mettreAJourStatutsSimplifie() throws SQLException {
        LocalDate aujourdhui = LocalDate.now();
        LocalTime maintenant = LocalTime.now();

        System.out.println("🔄 Vérification des statuts des événements...");

        // 1. Marquer comme "terminé" les événements dont la date est passée
        String sqlTermine = "UPDATE evenements SET statut = 'terminé' WHERE dateEvenement < ? AND statut != 'terminé'";
        try (PreparedStatement ps = connection.prepareStatement(sqlTermine)) {
            ps.setDate(1, Date.valueOf(aujourdhui));
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("✅ " + rows + " événements marqués comme terminés (date passée)");
        }

        // 2. Marquer comme "en cours" les événements qui ont lieu aujourd'hui et dont l'heure actuelle est entre début et fin
        String sqlEnCours = "UPDATE evenements SET statut = 'en cours' " +
                "WHERE dateEvenement = ? AND heureDebut <= ? AND heureFin >= ? AND statut != 'en cours'";
        try (PreparedStatement ps = connection.prepareStatement(sqlEnCours)) {
            ps.setDate(1, Date.valueOf(aujourdhui));
            ps.setTime(2, Time.valueOf(maintenant));
            ps.setTime(3, Time.valueOf(maintenant));
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("✅ " + rows + " événements marqués comme en cours (en ce moment)");
        }

        // 3. Marquer comme "terminé" les événements qui ont lieu aujourd'hui mais dont l'heure de fin est passée
        String sqlTermineAujourdhui = "UPDATE evenements SET statut = 'terminé' " +
                "WHERE dateEvenement = ? AND heureFin < ? AND statut != 'terminé'";
        try (PreparedStatement ps = connection.prepareStatement(sqlTermineAujourdhui)) {
            ps.setDate(1, Date.valueOf(aujourdhui));
            ps.setTime(2, Time.valueOf(maintenant));
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("✅ " + rows + " événements d'aujourd'hui marqués comme terminés (heure passée)");
        }

        // 4. Remettre à "planifié" les événements futurs qui auraient été mal étiquetés
        String sqlPlanifie = "UPDATE evenements SET statut = 'planifié' " +
                "WHERE dateEvenement > ? AND statut != 'planifié'";
        try (PreparedStatement ps = connection.prepareStatement(sqlPlanifie)) {
            ps.setDate(1, Date.valueOf(aujourdhui));
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("✅ " + rows + " événements remis à planifié");
        }
    }
}