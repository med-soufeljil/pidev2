package org.soa.tp1.pi_dev_s2.com.esprit.services;

import org.soa.tp1.pi_dev_s2.com.esprit.models.Participation;
import org.soa.tp1.pi_dev_s2.com.esprit.models.Evenement;
import org.soa.tp1.pi_dev_s2.com.esprit.utils.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService implements IService<Participation> {

    private Connection connection;
    private EvenementService evenementService = new EvenementService();

    public ParticipationService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void ajouter(Participation p) throws SQLException {
        if (!peutAjouterParticipation(p.getId_e())) {
            throw new SQLException("Nombre maximum de participants atteint pour cet événement");
        }

        if (!verifierDateEvenement(p.getId_e(), p.getDateInscription())) {
            throw new SQLException("Impossible de s'inscrire à un événement déjà passé");
        }

        String sql = "INSERT INTO participations (id_e, dateInscription, statut, presence, dateCreation) " +
                "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, p.getId_e());
        ps.setDate(2, Date.valueOf(p.getDateInscription()));
        ps.setString(3, p.getStatut());
        ps.setBoolean(4, p.isPresence());
        ps.setDate(5, Date.valueOf(LocalDate.now()));
        ps.executeUpdate();

        mettreAJourNombreInscrits(p.getId_e(), +1);
    }

    @Override
    public void modifier(Participation p) throws SQLException {
        String sql = "UPDATE participations SET id_e=?, dateInscription=?, statut=?, presence=?, dateModification=? WHERE id_p=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, p.getId_e());
        ps.setDate(2, Date.valueOf(p.getDateInscription()));
        ps.setString(3, p.getStatut());
        ps.setBoolean(4, p.isPresence());
        ps.setDate(5, Date.valueOf(LocalDate.now()));
        ps.setInt(6, p.getId_p());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(Participation p) throws SQLException {
        int idEvent = p.getId_e();

        String sql = "DELETE FROM participations WHERE id_p=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, p.getId_p());
        ps.executeUpdate();

        mettreAJourNombreInscrits(idEvent, -1);
    }

    @Override
    public List<Participation> recuperer() throws SQLException {
        String sql = "SELECT p.*, e.titre as evenement_titre, e.dateEvenement as evenement_date, " +
                "e.lieu as evenement_lieu, e.nombrePlacesMax, e.nombreInscrits as evenement_inscrits " +
                "FROM participations p " +
                "LEFT JOIN evenements e ON p.id_e = e.idEvenement";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<Participation> list = new ArrayList<>();

        while (rs.next()) {
            try {
                Participation p = new Participation();
                p.setId_p(rs.getInt("id_p"));
                p.setId_e(rs.getInt("id_e"));

                // Gestion sécurisée de la date d'inscription
                Date dateInsc = rs.getDate("dateInscription");
                if (dateInsc != null) {
                    p.setDateInscription(dateInsc.toLocalDate());
                } else {
                    p.setDateInscription(LocalDate.now()); // valeur par défaut
                }

                p.setStatut(rs.getString("statut"));
                p.setPresence(rs.getBoolean("presence"));

                // Récupérer les dates avec gestion des valeurs nulles ou zéro
                try {
                    Date creation = rs.getDate("dateCreation");
                    if (creation != null) {
                        // Vérifier si c'est une date valide (pas 0000-00-00)
                        String dateStr = creation.toString();
                        if (!dateStr.equals("0000-00-00")) {
                            p.setDateCreation(creation.toLocalDate());
                        }
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs de date
                    System.out.println("⚠️ Date de création invalide pour participation ID: " + p.getId_p());
                }

                try {
                    Date modification = rs.getDate("dateModification");
                    if (modification != null) {
                        String dateStr = modification.toString();
                        if (!dateStr.equals("0000-00-00")) {
                            p.setDateModification(modification.toLocalDate());
                        }
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs de date
                }

                // Créer et set l'objet Evenement
                Evenement e = new Evenement();
                e.setIdEvenement(rs.getInt("id_e"));
                e.setTitre(rs.getString("evenement_titre"));

                try {
                    Date eventDate = rs.getDate("evenement_date");
                    if (eventDate != null) {
                        String dateStr = eventDate.toString();
                        if (!dateStr.equals("0000-00-00")) {
                            e.setDateEvenement(eventDate.toLocalDate());
                        }
                    }
                } catch (Exception ex) {
                    // Ignorer
                }

                e.setLieu(rs.getString("evenement_lieu"));
                e.setNombrePlacesMax(rs.getInt("nombrePlacesMax"));
                e.setNombreInscrits(rs.getInt("evenement_inscrits"));

                p.setEvenement(e);
                list.add(p);

            } catch (SQLException ex) {
                System.err.println("❌ Erreur lors du traitement d'une participation: " + ex.getMessage());
                // Continuer avec la prochaine ligne
            }
        }
        return list;
    }

    public List<Participation> recupererParEvenement(int idEvent) throws SQLException {
        String sql = "SELECT p.*, e.titre as evenement_titre, e.dateEvenement as evenement_date, " +
                "e.lieu as evenement_lieu, e.nombrePlacesMax, e.nombreInscrits as evenement_inscrits " +
                "FROM participations p " +
                "LEFT JOIN evenements e ON p.id_e = e.idEvenement " +
                "WHERE p.id_e = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idEvent);
        ResultSet rs = ps.executeQuery();

        List<Participation> list = new ArrayList<>();

        while (rs.next()) {
            try {
                Participation p = new Participation();
                p.setId_p(rs.getInt("id_p"));
                p.setId_e(rs.getInt("id_e"));

                // Gestion sécurisée de la date d'inscription
                Date dateInsc = rs.getDate("dateInscription");
                if (dateInsc != null) {
                    p.setDateInscription(dateInsc.toLocalDate());
                } else {
                    p.setDateInscription(LocalDate.now());
                }

                p.setStatut(rs.getString("statut"));
                p.setPresence(rs.getBoolean("presence"));

                // Date de création avec gestion d'erreur
                try {
                    Date creation = rs.getDate("dateCreation");
                    if (creation != null) {
                        String dateStr = creation.toString();
                        if (!dateStr.equals("0000-00-00")) {
                            p.setDateCreation(creation.toLocalDate());
                        }
                    }
                } catch (Exception e) {
                    // Ignorer
                }

                Evenement e = new Evenement();
                e.setIdEvenement(rs.getInt("id_e"));
                e.setTitre(rs.getString("evenement_titre"));

                try {
                    Date eventDate = rs.getDate("evenement_date");
                    if (eventDate != null) {
                        String dateStr = eventDate.toString();
                        if (!dateStr.equals("0000-00-00")) {
                            e.setDateEvenement(eventDate.toLocalDate());
                        }
                    }
                } catch (Exception ex) {
                    // Ignorer
                }

                e.setLieu(rs.getString("evenement_lieu"));
                e.setNombrePlacesMax(rs.getInt("nombrePlacesMax"));
                e.setNombreInscrits(rs.getInt("evenement_inscrits"));

                p.setEvenement(e);
                list.add(p);

            } catch (SQLException ex) {
                System.err.println("❌ Erreur pour participation dans événement " + idEvent + ": " + ex.getMessage());
            }
        }
        return list;
    }

    private boolean peutAjouterParticipation(int idEvent) throws SQLException {
        Evenement event = evenementService.recupererParId(idEvent);
        if (event == null) return false;

        int nbInscrits = compterParticipationsParEvenement(idEvent);
        return nbInscrits < event.getNombrePlacesMax();
    }

    private boolean verifierDateEvenement(int idEvent, LocalDate dateInscription) throws SQLException {
        Evenement event = evenementService.recupererParId(idEvent);
        if (event == null || event.getDateEvenement() == null) return false;

        return !dateInscription.isAfter(event.getDateEvenement());
    }

    public int compterParticipationsParEvenement(int idEvent) throws SQLException {
        String sql = "SELECT COUNT(*) FROM participations WHERE id_e = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idEvent);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    private void mettreAJourNombreInscrits(int idEvent, int increment) throws SQLException {
        String sql = "UPDATE evenements SET nombreInscrits = nombreInscrits + ? WHERE idEvenement = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, increment);
        ps.setInt(2, idEvent);
        ps.executeUpdate();
    }
}