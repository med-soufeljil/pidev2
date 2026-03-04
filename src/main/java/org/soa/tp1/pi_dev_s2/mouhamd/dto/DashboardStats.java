package org.soa.tp1.pi_dev_s2.mouhamd.dto;

public class DashboardStats {
    private final int totalFormations;
    private final int totalApprenants;
    private final double averageDuration;
    private final int certifiedFormations;

    public DashboardStats(int totalFormations, int totalApprenants, double averageDuration, int certifiedFormations) {
        this.totalFormations = totalFormations;
        this.totalApprenants = totalApprenants;
        this.averageDuration = averageDuration;
        this.certifiedFormations = certifiedFormations;
    }

    public int getTotalFormations() {
        return totalFormations;
    }

    public int getTotalApprenants() {
        return totalApprenants;
    }

    public double getAverageDuration() {
        return averageDuration;
    }

    public int getCertifiedFormations() {
        return certifiedFormations;
    }
}
