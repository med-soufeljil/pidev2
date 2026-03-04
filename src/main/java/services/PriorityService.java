package services;

import models.DemandeConges;
import models.StatutDemande;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects congé conflicts (>2 employees absent on the same day)
 * and assigns a priority note to each demand.
 *
 * Priority rule: the fewer accepted leaves an employee has historically,
 * the HIGHER their priority.
 */
public class PriorityService {

    /** Max simultaneous absences allowed by company policy. */
    private static final int MAX_SIMULTANEOUS = 2;

    private final Connection connection;

    public PriorityService() {
        connection = utils.MyDatabase.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Mutates each demand in {@code pending} by setting its priorityNote.
     * Only demands whose dates overlap with > MAX_SIMULTANEOUS other demands
     * get annotated.
     */
    public void annotate(List<DemandeConges> pending) throws SQLException {
        if (pending == null || pending.isEmpty())
            return;

        // 1. Pre-load the accepted congé count for each employee
        Map<Integer, Integer> acceptedCount = loadAcceptedCounts(pending);

        // 2. Find conflict groups: sets of demands sharing at least one common day
        List<List<DemandeConges>> conflicts = findConflicts(pending);

        // 3. Annotate each demand that is part of a conflict group
        Set<Integer> annotated = new HashSet<>();
        for (List<DemandeConges> group : conflicts) {
            // sort by accepted count ASC → lower count = higher priority
            List<DemandeConges> sorted = group.stream()
                    .sorted(Comparator.comparingInt(d -> acceptedCount.getOrDefault(d.getIdEmploye(), 0)))
                    .collect(Collectors.toList());

            for (int i = 0; i < sorted.size(); i++) {
                DemandeConges d = sorted.get(i);
                if (annotated.contains(d.getId()))
                    continue; // already annotated by another group
                int count = acceptedCount.getOrDefault(d.getIdEmploye(), 0);
                if (i < MAX_SIMULTANEOUS) {
                    d.setPriorityNote("🟢 Priorité haute (" + count + " congé(s))");
                } else {
                    d.setPriorityNote("🔴 Priorité basse (" + count + " congé(s))");
                }
                annotated.add(d.getId());
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns a list of conflict groups. A group contains all demands that
     * share at least one day with another demand in the group AND the group
     * size exceeds MAX_SIMULTANEOUS.
     */
    private List<List<DemandeConges>> findConflicts(List<DemandeConges> pending) {
        // Build a map: day -> list of demands covering that day
        Map<LocalDate, List<DemandeConges>> dayMap = new LinkedHashMap<>();
        for (DemandeConges d : pending) {
            if (d.getDateDebut() == null || d.getDateFin() == null)
                continue;
            LocalDate day = d.getDateDebut();
            while (!day.isAfter(d.getDateFin())) {
                dayMap.computeIfAbsent(day, k -> new ArrayList<>()).add(d);
                day = day.plusDays(1);
            }
        }

        // Collect groups (days where more than MAX_SIMULTANEOUS demands overlap)
        Set<Set<Integer>> seen = new HashSet<>();
        List<List<DemandeConges>> groups = new ArrayList<>();
        for (Map.Entry<LocalDate, List<DemandeConges>> e : dayMap.entrySet()) {
            List<DemandeConges> group = e.getValue();
            if (group.size() > MAX_SIMULTANEOUS) {
                Set<Integer> key = group.stream().map(DemandeConges::getId).collect(Collectors.toSet());
                if (seen.add(key)) {
                    groups.add(group);
                }
            }
        }
        return groups;
    }

    /**
     * Counts APPROUVE congés per employee (across all history, not just pending).
     */
    private Map<Integer, Integer> loadAcceptedCounts(List<DemandeConges> pending) throws SQLException {
        // Build IN clause for involved employee ids
        Set<Integer> empIds = pending.stream()
                .map(DemandeConges::getIdEmploye)
                .collect(Collectors.toSet());
        if (empIds.isEmpty())
            return Collections.emptyMap();

        String inClause = empIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = "SELECT id_employe, COUNT(*) AS cnt FROM demande_conge " +
                "WHERE statut = 'APPROUVE' AND id_employe IN (" + inClause + ") " +
                "GROUP BY id_employe";

        Map<Integer, Integer> result = new HashMap<>();
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.put(rs.getInt("id_employe"), rs.getInt("cnt"));
            }
        }
        return result;
    }
}
