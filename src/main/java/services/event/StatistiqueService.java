package services.event;

import models.event.Evenement;
import models.event.Participation;
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

/**
 * Service de calcul et d'agrégation des statistiques
 * Fournit toutes les données pour le dashboard et les exports
 */
public class StatistiqueService {

    private final EvenementService evenementService = new EvenementService();
    private final ParticipationService participationService = new ParticipationService();

    /**
     * Méthode utilitaire pour convertir en LocalDate de façon sécurisée
     * Gère les différents types de dates (LocalDate, LocalDateTime, null)
     */
    private LocalDate toLocalDate(Object date) {
        if (date == null) return null;
        if (date instanceof LocalDate) return (LocalDate) date;
        if (date instanceof LocalDateTime) return ((LocalDateTime) date).toLocalDate();
        return null;
    }

    /**
     * Récupère les statistiques globales pour les KPIs du dashboard
     * @return Map contenant les indicateurs clés
     * @throws SQLException en cas d'erreur SQL
     */
    public Map<String, Integer> getStatistiquesGlobales() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();

        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        stats.put("totalEvenements", evenements.size());
        stats.put("totalParticipations", participations.size());

        // Événements à venir (date >= aujourd'hui)
        long futurs = evenements.stream()
                .filter(e -> e.getDateEvenement() != null && !e.getDateEvenement().isBefore(LocalDate.now()))
                .count();
        stats.put("evenementsFuturs", (int) futurs);

        // Taux de remplissage moyen (moyenne des taux de tous les événements)
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
     * @return Données pour un graphique en camembert
     * @throws SQLException en cas d'erreur SQL
     */
    public ObservableList<PieChart.Data> getRepartitionStatuts() throws SQLException {
        List<Participation> participations = participationService.recuperer();

        // Comptage par statut
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
     * @return Série de données pour un graphique en ligne
     * @throws SQLException en cas d'erreur SQL
     */
    public XYChart.Series<String, Number> getEvolutionParticipations() throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Participations");

        List<Participation> participations = participationService.recuperer();
        Map<String, Long> parMois = new TreeMap<>(); // TreeMap pour tri automatique

        // Agrégation par mois/année
        for (Participation p : participations) {
            LocalDate date = toLocalDate(p.getDateCreation());
            if (date != null) {
                String moisAnnee = date.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                parMois.put(moisAnnee, parMois.getOrDefault(moisAnnee, 0L) + 1);
            }
        }

        // Ajout des données à la série
        for (Map.Entry<String, Long> entry : parMois.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        return series;
    }

    /**
     * Récupère le top 5 des événements les plus populaires
     * @return Liste des événements triés par nombre de participations
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Map.Entry<Evenement, Long>> getTopEvenements() throws SQLException {
        List<Evenement> evenements = evenementService.recuperer();
        List<Participation> participations = participationService.recuperer();

        // Comptage des participations par événement
        Map<Integer, Long> participationsCount = participations.stream()
                .collect(Collectors.groupingBy(
                        Participation::getId_e,
                        Collectors.counting()
                ));

        // Association événement + nombre et tri décroissant
        return evenements.stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e,
                        participationsCount.getOrDefault(e.getIdEvenement(), 0L)))
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Récupère le taux de présence par événement
     * @return Map événement → taux de présence (%)
     * @throws SQLException en cas d'erreur SQL
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
     * @return Map date → nombre de participations
     * @throws SQLException en cas d'erreur SQL
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
     * @return Map catégorie → nombre d'événements
     * @throws SQLException en cas d'erreur SQL
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
     * @return Map événement → taux de remplissage (%)
     * @throws SQLException en cas d'erreur SQL
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
     * @return Map avec comptage des présents et absents
     * @throws SQLException en cas d'erreur SQL
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
     * Compare le mois dernier aux 30 jours précédents
     * @return Pourcentage de croissance (positif ou négatif)
     * @throws SQLException en cas d'erreur SQL
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
     * Prédiction du nombre de participants pour un événement
     * Basé sur la moyenne des événements similaires (même catégorie)
     * @param evenement L'événement pour lequel prédire
     * @return Nombre prédit de participants
     * @throws SQLException en cas d'erreur SQL
     */
    public int predireParticipants(Evenement evenement) throws SQLException {
        // Récupérer les événements similaires (même catégorie)
        List<Evenement> similaires = evenementService.recuperer().stream()
                .filter(e -> e.getIdCategorie() == evenement.getIdCategorie())
                .filter(e -> e.getIdEvenement() != evenement.getIdEvenement())
                .limit(5)
                .collect(Collectors.toList());

        if (similaires.isEmpty()) {
            // Par défaut, 50% de remplissage
            return (int) (evenement.getNombrePlacesMax() * 0.5);
        }

        // Calculer la moyenne des inscrits des événements similaires
        double moyenne = similaires.stream()
                .mapToInt(Evenement::getNombreInscrits)
                .average()
                .orElse(0);

        return (int) Math.min(moyenne, evenement.getNombrePlacesMax());
    }

    /**
     * Toutes les statistiques en une seule méthode
     * @return Map complète de toutes les statistiques
     * @throws SQLException en cas d'erreur SQL
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
     * @return Map mois → nombre de participations
     * @throws SQLException en cas d'erreur SQL
     */
    public Map<String, Long> getActivite3DerniersMois() throws SQLException {
        List<Participation> participations = participationService.recuperer();
        Map<String, Long> result = new LinkedHashMap<>();
        LocalDate maintenant = LocalDate.now();

        // Initialiser les 3 derniers mois avec 0
        for (int i = 2; i >= 0; i--) {
            LocalDate mois = maintenant.minusMonths(i);
            String moisAnnee = mois.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH));
            result.put(moisAnnee, 0L);
        }

        // Compter les participations par mois
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