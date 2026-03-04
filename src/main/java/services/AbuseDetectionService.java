package services;

import models.DemandeTeletravail;
import models.StatutTeletravail;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects abusive télétravail patterns for a given employee.
 *
 * Three patterns are checked:
 * 1. Lundi + Vendredi — requesting both Monday AND Friday in the same week (≥3
 * occurrences)
 * 2. Chaque semaine — télétravail present in ≥80 % of the weeks over the last 3
 * months
 * 3. Refus répétés — ≥2 REFUSE demands in the employee's full history
 */
public class AbuseDetectionService {

    // ── Seuils configurables ──────────────────────────────────────────────────
    private static final int SEUIL_LUNDI_VENDREDI = 3; // nb semaines Lun+Ven pour déclencher
    private static final double SEUIL_CHAQUE_SEMAINE = 0.80; // ratio semaines couvertes
    private static final int HORIZON_MOIS = 3; // mois d'historique analysé
    private static final int SEUIL_REFUS = 2; // nb refus avant alerte

    private static final WeekFields WEEK_FIELDS = WeekFields.of(DayOfWeek.MONDAY, 4);

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Analyses the full history of one employee and injects the abuse flags
     * into the given {@code target} demand.
     *
     * @param target     The demand to annotate (will be mutated in place)
     * @param allHistory All télétravail demands of the same employee (all statuses)
     */
    public void analyze(DemandeTeletravail target, List<DemandeTeletravail> allHistory) {
        List<String> flags = new ArrayList<>();

        detectLundiVendredi(allHistory, flags);
        detectChaqueSmaine(allHistory, flags);
        detectRefusRepetes(allHistory, flags);

        target.setAbuseFlags(flags);
    }

    // ── Pattern 1 : Lundi + Vendredi ─────────────────────────────────────────

    /**
     * Counts the weeks where the employee has at least one day on Monday
     * AND at least one day on Friday. Fires if count ≥ SEUIL_LUNDI_VENDREDI.
     */
    private void detectLundiVendredi(List<DemandeTeletravail> history, List<String> flags) {
        // group demand day-ranges by ISO year-week
        Map<String, Boolean[]> weekMap = new HashMap<>(); // key="YYYY-WW", value=[hasMonday, hasFriday]

        for (DemandeTeletravail d : history) {
            if (d.getDateDebut() == null || d.getDateFin() == null)
                continue;
            // iterate every day in the demand range
            LocalDate day = d.getDateDebut();
            while (!day.isAfter(d.getDateFin())) {
                String weekKey = weekKey(day);
                Boolean[] flags2 = weekMap.computeIfAbsent(weekKey, k -> new Boolean[] { false, false });
                if (day.getDayOfWeek() == DayOfWeek.MONDAY)
                    flags2[0] = true;
                if (day.getDayOfWeek() == DayOfWeek.FRIDAY)
                    flags2[1] = true;
                day = day.plusDays(1);
            }
        }

        long count = weekMap.values().stream()
                .filter(v -> v[0] && v[1])
                .count();

        if (count >= SEUIL_LUNDI_VENDREDI) {
            flags.add("⚠ Extension weekend : " + count + " sem. Lun+Ven");
        }
    }

    // ── Pattern 2 : Chaque semaine ────────────────────────────────────────────

    /**
     * Counts how many distinct weeks over the last HORIZON_MOIS months are
     * covered by at least one télétravail demand. Fires if ratio ≥
     * SEUIL_CHAQUE_SEMAINE.
     */
    private void detectChaqueSmaine(List<DemandeTeletravail> history, List<String> flags) {
        LocalDate cutoff = LocalDate.now().minusMonths(HORIZON_MOIS);
        LocalDate today = LocalDate.now();

        // all calendar weeks in the horizon
        Set<String> allWeeks = new HashSet<>();
        LocalDate cursor = cutoff;
        while (!cursor.isAfter(today)) {
            allWeeks.add(weekKey(cursor));
            cursor = cursor.plusWeeks(1);
        }
        if (allWeeks.isEmpty())
            return;

        // weeks actually covered by demands within the horizon
        Set<String> coveredWeeks = new HashSet<>();
        for (DemandeTeletravail d : history) {
            if (d.getDateFin() == null || d.getDateFin().isBefore(cutoff))
                continue;
            LocalDate day = d.getDateDebut() == null ? cutoff
                    : d.getDateDebut().isBefore(cutoff) ? cutoff : d.getDateDebut();
            while (!day.isAfter(today) && !day.isAfter(d.getDateFin())) {
                coveredWeeks.add(weekKey(day));
                day = day.plusDays(1);
            }
        }

        coveredWeeks.retainAll(allWeeks); // only count weeks in the horizon
        double ratio = (double) coveredWeeks.size() / allWeeks.size();

        if (ratio >= SEUIL_CHAQUE_SEMAINE) {
            int pct = (int) Math.round(ratio * 100);
            flags.add("⚠ Télétravail systématique : " + coveredWeeks.size()
                    + "/" + allWeeks.size() + " sem. (" + pct + "%)");
        }
    }

    // ── Pattern 3 : Refus répétés ─────────────────────────────────────────────

    /**
     * Counts REFUSE demands in the employee's history. Fires if count ≥
     * SEUIL_REFUS.
     */
    private void detectRefusRepetes(List<DemandeTeletravail> history, List<String> flags) {
        long refusCount = history.stream()
                .filter(d -> d.getStatut() == StatutTeletravail.REFUSE)
                .count();
        if (refusCount >= SEUIL_REFUS) {
            flags.add("⚠ " + refusCount + " refus antérieur(s)");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns "YYYY-WW" ISO week key for a given date. */
    private String weekKey(LocalDate date) {
        int year = date.get(WEEK_FIELDS.weekBasedYear());
        int week = date.get(WEEK_FIELDS.weekOfWeekBasedYear());
        return year + "-" + String.format("%02d", week);
    }
}
