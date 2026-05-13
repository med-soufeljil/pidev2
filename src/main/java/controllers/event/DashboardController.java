package controllers.event;

import models.event.Evenement;
import services.event.StatistiqueService;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Contrôleur pour le tableau de bord (dashboard)
 * Affiche des statistiques et graphiques sur les événements et participations
 */
public class DashboardController {

    // ==================== COMPOSANTS FXML ====================
    @FXML private Label totalEvenementsLabel;          // Nombre total d'événements
    @FXML private Label totalParticipationsLabel;      // Nombre total de participations
    @FXML private Label evenementsFutursLabel;         // Événements à venir
    @FXML private Label tauxRemplissageLabel;          // Taux de remplissage moyen
    @FXML private PieChart statutsPieChart;            // Camembert des statuts
    @FXML private LineChart<String, Number> evolutionLineChart;  // Courbe d'évolution
    @FXML private CategoryAxis xAxis;                   // Axe X du graphique
    @FXML private NumberAxis yAxis;                     // Axe Y du graphique
    @FXML private VBox topEvenementsContainer;          // Conteneur pour top événements

    // ==================== ATTRIBUTS ====================
    private final StatistiqueService statistiqueService = new StatistiqueService();
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private VBox contentArea;                            // Zone de contenu

    /**
     * Définit la zone de contenu principale
     * @param contentArea Le conteneur VBox
     */
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
        System.out.println("DashboardController: contentArea set");
    }

    /**
     * Initialisation du contrôleur
     * Charge toutes les statistiques
     */
    @FXML
    public void initialize() {
        System.out.println("DashboardController initialisé");
        chargerStatistiques();
    }

    /**
     * Charge toutes les statistiques depuis le service
     * Met à jour tous les composants du dashboard
     */
    private void chargerStatistiques() {
        try {
            // 1. Statistiques globales (KPIs)
            Map<String, Integer> stats = statistiqueService.getStatistiquesGlobales();
            totalEvenementsLabel.setText(String.valueOf(stats.get("totalEvenements")));
            totalParticipationsLabel.setText(String.valueOf(stats.get("totalParticipations")));
            evenementsFutursLabel.setText(String.valueOf(stats.get("evenementsFuturs")));

            int taux = stats.get("tauxRemplissage");
            tauxRemplissageLabel.setText(taux + "%");

            // Couleur selon le taux (vert/orange/rouge)
            String couleur;
            if (taux >= 75) {
                couleur = "#22c55e"; // Vert (bon)
            } else if (taux >= 50) {
                couleur = "#eab308"; // Jaune (moyen)
            } else {
                couleur = "#ef4444"; // Rouge (faible)
            }
            tauxRemplissageLabel.setStyle("-fx-text-fill: " + couleur + "; -fx-font-weight: bold; -fx-font-size: 24px;");

            // 2. Graphique des statuts (camembert)
            statutsPieChart.setData(statistiqueService.getRepartitionStatuts());
            statutsPieChart.setTitle("Répartition par statut");
            statutsPieChart.setLabelsVisible(true);
            statutsPieChart.setLabelLineLength(10);

            // Ajouter des tooltips (infobulles) et animations
            for (PieChart.Data data : statutsPieChart.getData()) {
                Tooltip tooltip = new Tooltip(data.getPieValue() + " participations");
                Tooltip.install(data.getNode(), tooltip);

                // Animation au survol
                data.getNode().setOnMouseEntered(e ->
                        data.getNode().setStyle("-fx-scale-x: 1.1; -fx-scale-y: 1.1;")
                );
                data.getNode().setOnMouseExited(e ->
                        data.getNode().setStyle("-fx-scale-x: 1; -fx-scale-y: 1;")
                );
            }

            // 3. Graphique d'évolution (ligne)
            evolutionLineChart.getData().clear();
            evolutionLineChart.getData().add(statistiqueService.getEvolutionParticipations());
            evolutionLineChart.setTitle("Évolution des participations");
            evolutionLineChart.setAnimated(true);
            evolutionLineChart.setCreateSymbols(true);

            // Personnalisation des axes
            xAxis.setLabel("Mois");
            yAxis.setLabel("Nombre de participations");

            // 4. Top événements
            afficherTopEvenements();

            // 5. Graphique des présences (optionnel)
            chargerGraphiquePresences();

        } catch (SQLException e) {
            System.err.println("Erreur chargement statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche le top 5 des événements les plus populaires
     * @throws SQLException si erreur de base de données
     */
    private void afficherTopEvenements() throws SQLException {
        topEvenementsContainer.getChildren().clear();

        var topEvents = statistiqueService.getTopEvenements();

        // Titre de la section
        Label titre = new Label("🏆 Top 5 des événements");
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        topEvenementsContainer.getChildren().add(titre);

        if (topEvents.isEmpty()) {
            Label vide = new Label("Aucun événement pour le moment");
            vide.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            topEvenementsContainer.getChildren().add(vide);
            return;
        }

        // Affichage de chaque événement
        for (int i = 0; i < topEvents.size(); i++) {
            var entry = topEvents.get(i);
            Evenement e = entry.getKey();
            Long nbParticipants = entry.getValue();

            // Conteneur pour chaque événement
            VBox eventBox = new VBox(5);
            eventBox.setStyle("-fx-padding: 10; -fx-background-color: #f8fafc; -fx-background-radius: 5;");

            // Rang et titre (avec médailles pour les 3 premiers)
            String medal = i == 0 ? "🥇 " : (i == 1 ? "🥈 " : (i == 2 ? "🥉 " : (i + 1) + ". "));
            Label classement = new Label(medal + e.getTitre());
            classement.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            // Détails
            Label details = new Label(
                    "   📅 " + (e.getDateEvenement() != null ? e.getDateEvenement().format(DATE_FORMAT) : "Date inconnue") +
                            " | 👥 " + nbParticipants + " participant" + (nbParticipants > 1 ? "s" : "") +
                            " | 📍 " + e.getLieu()
            );
            details.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

            // Barre de progression visuelle (taux de remplissage)
            double taux = e.getNombrePlacesMax() > 0 ?
                    (double) e.getNombreInscrits() / e.getNombrePlacesMax() * 100 : 0;
            String barre = "█".repeat((int) (taux / 10)) + "░".repeat(10 - (int) (taux / 10));
            Label progression = new Label(String.format("   %s %.0f%% (%d/%d places)",
                    barre, taux, e.getNombreInscrits(), e.getNombrePlacesMax()));
            progression.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

            eventBox.getChildren().addAll(classement, details, progression);
            topEvenementsContainer.getChildren().add(eventBox);
        }
    }

    /**
     * Charge et affiche le graphique des taux de présence
     * @throws SQLException si erreur de base de données
     */
    private void chargerGraphiquePresences() throws SQLException {
        Map<String, Double> tauxPresence = statistiqueService.getTauxPresence();

        // Créer un graphique à barres pour les taux de présence
        if (!tauxPresence.isEmpty()) {
            CategoryAxis xAxis2 = new CategoryAxis();
            NumberAxis yAxis2 = new NumberAxis(0, 100, 10);
            BarChart<String, Number> presenceChart = new BarChart<>(xAxis2, yAxis2);
            presenceChart.setTitle("Taux de présence par événement");
            presenceChart.setAnimated(true);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Taux de présence (%)");

            tauxPresence.entrySet().stream()
                    .limit(5)
                    .forEach(entry -> series.getData().add(
                            new XYChart.Data<>(entry.getKey().length() > 15 ?
                                    entry.getKey().substring(0, 15) + "..." : entry.getKey(),
                                    entry.getValue())
                    ));

            presenceChart.getData().add(series);
            // Décommentez la ligne suivante pour l'ajouter au dashboard
            // topEvenementsContainer.getChildren().add(presenceChart);
        }
    }
}