package models;

import java.time.LocalDateTime;

public class Soldeconges {

    private int id;
    private int idEmploye;
    private int totalAnnuel;
    private int utiliseAnnuel;
    private int restantAnnuel;
    private LocalDateTime misAJourLe;

    public Soldeconges() {
    }

    // pour AJOUT
    public Soldeconges(int idEmploye, int totalAnnuel, int utiliseAnnuel, int restantAnnuel) {
        this.idEmploye = idEmploye;
        this.totalAnnuel = totalAnnuel;
        this.utiliseAnnuel = utiliseAnnuel;
        this.restantAnnuel = restantAnnuel;
    }

    // pour MODIFICATION
    public Soldeconges(int id, int idEmploye, int totalAnnuel, int utiliseAnnuel, int restantAnnuel) {
        this.id = id;
        this.idEmploye = idEmploye;
        this.totalAnnuel = totalAnnuel;
        this.utiliseAnnuel = utiliseAnnuel;
        this.restantAnnuel = restantAnnuel;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdEmploye() { return idEmploye; }
    public void setIdEmploye(int idEmploye) { this.idEmploye = idEmploye; }

    public int getTotalAnnuel() { return totalAnnuel; }
    public void setTotalAnnuel(int totalAnnuel) { this.totalAnnuel = totalAnnuel; }

    public int getUtiliseAnnuel() { return utiliseAnnuel; }
    public void setUtiliseAnnuel(int utiliseAnnuel) { this.utiliseAnnuel = utiliseAnnuel; }

    public int getRestantAnnuel() { return restantAnnuel; }
    public void setRestantAnnuel(int restantAnnuel) { this.restantAnnuel = restantAnnuel; }

    public LocalDateTime getMisAJourLe() { return misAJourLe; }
    public void setMisAJourLe(LocalDateTime misAJourLe) { this.misAJourLe = misAJourLe; }

    @Override
    public String toString() {
        return "SoldeConge{" +
                "id=" + id +
                ", idEmploye=" + idEmploye +
                ", totalAnnuel=" + totalAnnuel +
                ", utiliseAnnuel=" + utiliseAnnuel +
                ", restantAnnuel=" + restantAnnuel +
                ", misAJourLe=" + misAJourLe +
                '}';
    }
}
