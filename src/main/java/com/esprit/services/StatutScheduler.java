package com.esprit.services;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatutScheduler {

    private final EvenementService evenementService;
    private ScheduledExecutorService scheduler;
    private boolean estActif = false;

    public StatutScheduler() {
        this.evenementService = new EvenementService();
    }

    /**
     * Démarre la tâche planifiée de mise à jour des statuts
     */
    public void demarrer() {
        if (estActif) {
            System.out.println("⚠️ Le scheduler est déjà en cours d'exécution");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        estActif = true;

        // Planifier une mise à jour toutes les minutes
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Exécuter la mise à jour
                evenementService.mettreAJourStatutsSimplifie();
                String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.out.println("⏰ [" + heure + "] Mise à jour automatique des statuts effectuée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la mise à jour automatique: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("❌ Erreur inattendue: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);

        System.out.println("✅ Scheduler de mise à jour des statuts démarré (toutes les minutes)");
    }

    /**
     * Arrête la tâche planifiée
     */
    public void arreter() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                // Attendre la fin des tâches en cours (max 5 secondes)
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            estActif = false;
            System.out.println("🛑 Scheduler de mise à jour des statuts arrêté");
        }
    }

    /**
     * Vérifie si le scheduler est actif
     */
    public boolean isActif() {
        return estActif;
    }
}