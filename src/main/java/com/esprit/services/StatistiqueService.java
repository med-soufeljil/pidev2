package com.esprit.services;

import com.esprit.models.Evenement;
import com.esprit.models.Participation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StatistiqueService {

    private final EvenementService evenementService = new EvenementService();
    private final ParticipationService participationService = new ParticipationService();

    /**
     * Méthode utilitaire pour convertir en LocalDate de façon sécurisée
     */
    private LocalDate toLocalDate(Object date) {
        if (date == null) return null;
        if (date instanceof LocalDate) return (LocalDate) date;
        if (date instanceof LocalDateTime) return ((LocalDateTime) date).toLocalDate();
        return null;
    }

    /**
     * Récupère les statistiques globales pour les KPIs
     */
    public Map<String, Integer> getStatistiquesGlobales() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();

        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        stats.put("totalEvenements", evenements.size());
        stats.put("totalParticipations", participations.size());

        // Événements à venir
        long futurs = evenements.stream()
                .filter(e -> e.getDateEvenement() != null && !e.getDateEvenement().isBefore(LocalDate.now()))
                .count();
        stats.put("evenementsFuturs", (int) futurs);

        // Taux de remplissage moyen
        double tauxMoyen = evenements.stream()
                .filter(e -> e.getNombrePlacesMax() > 0)
                .mapToDouble(e -> (double) e.getNombreInscrits() / e.getNombrePlacesMax() * 100)
                .average()
                .orElse(0);
        stats.put("tauxRemplissage", (int) Math.round(tauxMoyen));

        return stats;
    }

    /**
     * Récupère la répartition des participations par statut
     */
    public ObservableList<PieChart.Data> getRepartitionStatuts() throws SQLException {
        List<Participation> participations = participationService.recuperer();

        Map<String, Long> comptage = participations.stream()
                .filter(p -> p.getStatut() != null)
                .collect(Collectors.groupingBy(
                        Participation::getStatut,
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        comptage.forEach((statut, count) ->
                pieData.add(new PieChart.Data(statut + " (" + count + ")", count))
        );

        return pieData;
    }

    /**
     * Récupère l'évolution des participations par mois
     */
    public XYChart.Series<String, Number> getEvolutionParticipations() throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Participations");

        List<Participation> participations = participationService.recuperer();
        Map<String, Long> parMois = new TreeMap<>();

        for (Participation p : participations) {
            LocalDate date = toLocalDate(p.getDateCreation());
            if (date != null) {
                String moisAnnee = date.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                parMois.put(moisAnnee, parMois.getOrDefault(moisAnnee, 0L) + 1);
            }
        }

        for (Map.Entry<String, Long> entry : parMois.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        return series;
    }

    /**
     * Récupère le top 5 des événements les plus populaires
     */
    public List<Map.Entry<Evenement, Long>> getTopEvenements() throws SQLException {
        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        Map<Integer, Long> participationsCount = participations.stream()
                .collect(Collectors.groupingBy(
                        Participation::getId_e,
                        Collectors.counting()
                ));

        return evenements.stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e,
                        participationsCount.getOrDefault(e.getIdEvenement(), 0L)))
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Récupère le taux de présence par événement
     */
    public Map<String, Double> getTauxPresence() throws SQLException {
        Map<String, Double> result = new LinkedHashMap<>();
        List<Evenement> evenements = evenementService.recuperer();

        for (Evenement e : evenements) {
            List<Participation> participationsEvent =
                    participationService.recupererParEvenement(e.getIdEvenement());

            long presents = participationsEvent.stream()
                    .filter(Participation::isPresence)
                    .count();
            long total = participationsEvent.size();

            double taux = total > 0 ? (double) presents / total * 100 : 0;
            result.put(e.getTitre(), Math.round(taux * 10) / 10.0);
        }

        return result;
    }

    /**
     * Récupère les participations des 7 derniers jours
     */
    public Map<LocalDate, Long> getParticipations7Jours() throws SQLException {
        List<Participation> participations = participationService.recuperer();
        LocalDate aujourdhui = LocalDate.now();
        LocalDate ilYASemaine = aujourdhui.minusDays(7);
        Map<LocalDate, Long> result = new TreeMap<>();

        for (Participation p : participations) {
            LocalDate date = toLocalDate(p.getDateCreation());
            if (date != null && !date.isBefore(ilYASemaine) && !date.isAfter(aujourdhui)) {
                result.put(date, result.getOrDefault(date, 0L) + 1);
            }
        }

        return result;
    }

    /**
     * Statistiques par catégorie
     */
    public Map<String, Integer> getStatistiquesParCategorie() throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        List<Evenement> evenements = evenementService.recuperer();

        for (Evenement e : evenements) {
            String nomCategorie = "Catégorie " + e.getIdCategorie();
            result.put(nomCategorie, result.getOrDefault(nomCategorie, 0) + 1);
        }

        return result;
    }

    /**
     * Taux de remplissage par événement
     */
    public Map<String, Double> getTauxRemplissageParEvenement() throws SQLException {
        Map<String, Double> result = new LinkedHashMap<>();
        List<Evenement> evenements = evenementService.recuperer();

        for (Evenement e : evenements) {
            if (e.getNombrePlacesMax() > 0) {
                double taux = (double) e.getNombreInscrits() / e.getNombrePlacesMax() * 100;
                result.put(e.getTitre(), Math.round(taux * 10) / 10.0);
            }
        }

        return result;
    }

    /**
     * Répartition présences/absences
     */
    public Map<String, Integer> getRepartitionPresences() throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        List<Participation> participations = participationService.recuperer();

        long presents = participations.stream()
                .filter(Participation::isPresence)
                .count();
        long absents = participations.size() - presents;

        result.put("Présents", (int) presents);
        result.put("Absents", (int) absents);

        return result;
    }

    /**
     * Taux de croissance des participations
     */
    public double getTauxCroissance() throws SQLException {
        List<Participation> participations = participationService.recuperer();

        if (participations.isEmpty()) return 0;

        LocalDate maintenant = LocalDate.now();
        LocalDate ilYA30Jours = maintenant.minusDays(30);
        LocalDate ilYA60Jours = maintenant.minusDays(60);

        long ceMois = 0;
        long moisPrecedent = 0;

        for (Participation p : participations) {
            LocalDate date = toLocalDate(p.getDateCreation());
            if (date != null) {
                if (!date.isBefore(ilYA30Jours)) {
                    ceMois++;
                } else if (!date.isBefore(ilYA60Jours)) {
                    moisPrecedent++;
                }
            }
        }

        if (moisPrecedent == 0) return ceMois > 0 ? 100 : 0;
        return ((double) (ceMois - moisPrecedent) / moisPrecedent) * 100;
    }

    /**
     * Prédiction de participants
     */
    public int predireParticipants(Evenement evenement) throws SQLException {
        List<Evenement> similaires = evenementService.recuperer().stream()
                .filter(e -> e.getIdCategorie() == evenement.getIdCategorie())
                .filter(e -> e.getIdEvenement() != evenement.getIdEvenement())
                .limit(5)
                .collect(Collectors.toList());

        if (similaires.isEmpty()) {
            return (int) (evenement.getNombrePlacesMax() * 0.5);
        }

        double moyenne = similaires.stream()
                .mapToInt(Evenement::getNombreInscrits)
                .average()
                .orElse(0);

        return (int) Math.min(moyenne, evenement.getNombrePlacesMax());
    }

    /**
     * Toutes les statistiques en une seule méthode
     */
    public Map<String, Object> getStatistiquesCompletes() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("globales", getStatistiquesGlobales());
        stats.put("repartitionStatuts", getRepartitionStatuts());
        stats.put("evolution", getEvolutionParticipations());
        stats.put("topEvenements", getTopEvenements());
        stats.put("tauxPresence", getRepartitionPresences());
        stats.put("croissance", getTauxCroissance());
        stats.put("dateGeneration", LocalDate.now());
        stats.put("totalParticipations", participationService.recuperer().size());
        stats.put("totalEvenements", evenementService.recuperer().size());

        return stats;
    }

    /**
     * Récupère les 3 derniers mois d'activité
     */
    public Map<String, Long> getActivite3DerniersMois() throws SQLException {
        List<Participation> participations = participationService.recuperer();
        Map<String, Long> result = new LinkedHashMap<>();
        LocalDate maintenant = LocalDate.now();

        for (int i = 2; i >= 0; i--) {
            LocalDate mois = maintenant.minusMonths(i);
            String moisAnnee = mois.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH));
            result.put(moisAnnee, 0L);
        }

        for (Participation p : participations) {
            LocalDate date = toLocalDate(p.getDateCreation());
            if (date != null && !date.isBefore(maintenant.minusMonths(3))) {
                String moisAnnee = date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH));
                result.put(moisAnnee, result.getOrDefault(moisAnnee, 0L) + 1);
            }
        }

        return result;
    }
}