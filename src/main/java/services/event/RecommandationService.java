package services.event;

import models.event.Evenement;
import models.event.Participation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de recommandation d'événements
 * Propose différents algorithmes pour suggérer des événements aux utilisateurs
 */
public class RecommandationService {

    private final EvenementService evenementService = new EvenementService();
    private final ParticipationService participationService = new ParticipationService();

    /**
     * 1. Événements les plus populaires (par nombre total de participations)
     * @return Liste des 10 événements les plus populaires à venir
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Evenement> getPopulaires() throws SQLException {
        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        // Compter les participations par événement
        Map<Integer, Long> compteur = participations.stream()
                .collect(Collectors.groupingBy(
                        Participation::getId_e,
                        Collectors.counting()
                ));

        // Filtrer les événements à venir et trier par popularité
        return evenements.stream()
                .filter(e -> !e.getDateEvenement().isBefore(LocalDate.now()))
                .sorted((e1, e2) -> {
                    long count1 = compteur.getOrDefault(e1.getIdEvenement(), 0L);
                    long count2 = compteur.getOrDefault(e2.getIdEvenement(), 0L);
                    return Long.compare(count2, count1); // Tri décroissant
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 2. Événements "tendance" (forte progression dans les 7 derniers jours)
     * @return Liste des 10 événements avec le plus d'inscriptions récentes
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Evenement> getTendances() throws SQLException {
        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        LocalDate semaineDerniere = LocalDate.now().minusDays(7);

        // Compter les participations des 7 derniers jours
        Map<Integer, Long> recentes = participations.stream()
                .filter(p -> p.getDateCreation() != null)
                .filter(p -> !p.getDateCreation().isBefore(semaineDerniere))
                .collect(Collectors.groupingBy(
                        Participation::getId_e,
                        Collectors.counting()
                ));

        // Trier par inscriptions récentes
        return evenements.stream()
                .filter(e -> !e.getDateEvenement().isBefore(LocalDate.now()))
                .sorted((e1, e2) -> {
                    long count1 = recentes.getOrDefault(e1.getIdEvenement(), 0L);
                    long count2 = recentes.getOrDefault(e2.getIdEvenement(), 0L);
                    return Long.compare(count2, count1);
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 3. Événements à venir (par date la plus proche)
     * @return Liste des 10 prochains événements
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Evenement> getProchains() throws SQLException {
        return evenementService.recuperer().stream()
                .filter(e -> !e.getDateEvenement().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Evenement::getDateEvenement)) // Tri chronologique
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 4. Recommandation par catégorie
     * @param categorieId L'ID de la catégorie
     * @return Liste des événements à venir dans cette catégorie
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Evenement> getParCategorie(int categorieId) throws SQLException {
        return evenementService.recuperer().stream()
                .filter(e -> e.getIdCategorie() == categorieId)
                .filter(e -> !e.getDateEvenement().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Evenement::getDateEvenement))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 5. Score de popularité sur 5 étoiles
     * @param idEvenement L'ID de l'événement
     * @return Un score de 1 à 5 étoiles
     * @throws SQLException en cas d'erreur SQL
     */
    public int getScorePopularite(int idEvenement) throws SQLException {
        Evenement event = evenementService.recupererParId(idEvenement);

        if (event == null || event.getNombrePlacesMax() == 0) return 0;

        double taux = (double) event.getNombreInscrits() / event.getNombrePlacesMax();
        return (int) Math.min(5, Math.max(1, taux * 5)); // Convertit le taux en étoiles
    }

    /**
     * 6. Score de tendance (basé sur les inscriptions récentes)
     * @param idEvenement L'ID de l'événement
     * @return Un score basé sur les inscriptions des 7 derniers jours
     * @throws SQLException en cas d'erreur SQL
     */
    public int getScoreTendance(int idEvenement) throws SQLException {
        List<Participation> participations = participationService.recupererParEvenement(idEvenement);
        LocalDate semaineDerniere = LocalDate.now().minusDays(7);

        long recentes = participations.stream()
                .filter(p -> p.getDateCreation() != null)
                .filter(p -> !p.getDateCreation().isBefore(semaineDerniere))
                .count();

        return (int) Math.min(5, recentes); // Max 5
    }

    /**
     * 7. Mélange intelligent (combinaison des scores)
     * Fusionne les recommandations populaires et tendances sans doublons
     * @return Liste des 10 meilleures recommandations
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Evenement> getRecommandationsIntelligentes() throws SQLException {
        List<Evenement> evenements = getPopulaires();
        List<Evenement> tendances = getTendances();

        Set<Evenement> ensemble = new LinkedHashSet<>(evenements);
        ensemble.addAll(tendances); // Fusion sans doublons

        return new ArrayList<>(ensemble).stream()
                .limit(10)
                .collect(Collectors.toList());
    }
}