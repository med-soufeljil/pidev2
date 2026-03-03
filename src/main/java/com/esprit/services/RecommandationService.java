package com.esprit.services;

import com.esprit.models.Evenement;
import com.esprit.models.Participation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RecommandationService {

    private final EvenementService evenementService = new EvenementService();
    private final ParticipationService participationService = new ParticipationService();

    /**
     * 1. Événements les plus populaires (par nombre total de participations)
     */
    public List<Evenement> getPopulaires() throws SQLException {
        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        Map<Integer, Long> compteur = participations.stream()
                .collect(Collectors.groupingBy(
                        Participation::getId_e,
                        Collectors.counting()
                ));

        return evenements.stream()
                .filter(e -> !e.getDateEvenement().isBefore(LocalDate.now()))
                .sorted((e1, e2) -> {
                    long count1 = compteur.getOrDefault(e1.getIdEvenement(), 0L);
                    long count2 = compteur.getOrDefault(e2.getIdEvenement(), 0L);
                    return Long.compare(count2, count1);
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 2. Événements "tendance" (forte progression dans les 7 derniers jours)
     */
    public List<Evenement> getTendances() throws SQLException {
        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        LocalDate semaineDerniere = LocalDate.now().minusDays(7);

        Map<Integer, Long> recentes = participations.stream()
                .filter(p -> p.getDateCreation() != null)
                .filter(p -> !p.getDateCreation().isBefore(semaineDerniere))
                .collect(Collectors.groupingBy(
                        Participation::getId_e,
                        Collectors.counting()
                ));

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
     */
    public List<Evenement> getProchains() throws SQLException {
        return evenementService.recuperer().stream()
                .filter(e -> !e.getDateEvenement().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Evenement::getDateEvenement))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 4. Recommandation par catégorie
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
     */
    public int getScorePopularite(int idEvenement) throws SQLException {
        Evenement event = evenementService.recupererParId(idEvenement);

        if (event == null || event.getNombrePlacesMax() == 0) return 0;

        double taux = (double) event.getNombreInscrits() / event.getNombrePlacesMax();
        return (int) Math.min(5, Math.max(1, taux * 5));
    }

    /**
     * 6. Score de tendance (basé sur les inscriptions récentes)
     */
    public int getScoreTendance(int idEvenement) throws SQLException {
        List<Participation> participations = participationService.recupererParEvenement(idEvenement);
        LocalDate semaineDerniere = LocalDate.now().minusDays(7);

        long recentes = participations.stream()
                .filter(p -> p.getDateCreation() != null)
                .filter(p -> !p.getDateCreation().isBefore(semaineDerniere))
                .count();

        return (int) Math.min(5, recentes);
    }

    /**
     * 7. Mélange intelligent (combinaison des scores)
     */
    public List<Evenement> getRecommandationsIntelligentes() throws SQLException {
        List<Evenement> evenements = getPopulaires();
        List<Evenement> tendances = getTendances();

        Set<Evenement> ensemble = new LinkedHashSet<>(evenements);
        ensemble.addAll(tendances);

        return new ArrayList<>(ensemble).stream()
                .limit(10)
                .collect(Collectors.toList());
    }
}