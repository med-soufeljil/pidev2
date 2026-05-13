package entities;

import java.time.LocalDate;

public class Apprenant {
    private int idApprenant;
    private String nom;
    private String prenom;
    private String email;
    private String statut;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int id_formation; // juste l'id de la formation

    public Apprenant() {}

    public Apprenant(int idApprenant, String nom, String prenom, String email, String statut,
                     LocalDate dateDebut, LocalDate dateFin, int id_formation) {
        this.idApprenant = idApprenant;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.statut = statut;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.id_formation = id_formation;
    }

    public Apprenant(String nom, String prenom, String email, String statut,
                     LocalDate dateDebut, LocalDate dateFin, int id_formation) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.statut = statut;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.id_formation = id_formation;
    }

    // Getters & Setters
    public int getIdApprenant() { return idApprenant; }
    public void setIdApprenant(int idApprenant) { this.idApprenant = idApprenant; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public int getId_formation() { return id_formation; }
    public void setId_formation(int idFormation) { this.id_formation = idFormation; }

    @Override
    public String toString() {
        return "Apprenant{" +
                "idApprenant=" + idApprenant +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", statut='" + statut + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", idFormation=" + id_formation +
                '}';
    }
}
