package models.event;

import java.time.LocalDate;

public class Participation {
    private int id_p;
    private int id_e;
    private LocalDate dateInscription;
    private String statut;
    private boolean presence;
    private LocalDate dateCreation;  // Changé de LocalDateTime à LocalDate
    private LocalDate dateModification; // Changé de LocalDateTime à LocalDate
    private Evenement evenement;

    public Participation() {}

    public Participation(int id_e, LocalDate dateInscription, String statut, boolean presence) {
        this.id_e = id_e;
        this.dateInscription = dateInscription;
        this.statut = statut;
        this.presence = presence;
    }

    // Getters et Setters
    public int getId_p() { return id_p; }
    public void setId_p(int id_p) { this.id_p = id_p; }

    public int getId_e() { return id_e; }
    public void setId_e(int id_e) { this.id_e = id_e; }

    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public boolean isPresence() { return presence; }
    public void setPresence(boolean presence) { this.presence = presence; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public LocalDate getDateModification() { return dateModification; }
    public void setDateModification(LocalDate dateModification) { this.dateModification = dateModification; }

    public Evenement getEvenement() { return evenement; }
    public void setEvenement(Evenement evenement) { this.evenement = evenement; }
}