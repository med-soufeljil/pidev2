package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DemandeTeletravail {
    private int id;
    private int idEmploye;
    private String employeNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int nbJours;
    private String motif;
    private String statut;
    private LocalDateTime dateDemande;
    private Integer validePar;
    private LocalDateTime dateValidation;
    private String commentaireDecision;
    private String moisConcerne;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdEmploye() { return idEmploye; }
    public void setIdEmploye(int idEmploye) { this.idEmploye = idEmploye; }
    public String getEmployeNom() { return employeNom; }
    public void setEmployeNom(String employeNom) { this.employeNom = employeNom; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public int getNbJours() { return nbJours; }
    public void setNbJours(int nbJours) { this.nbJours = nbJours; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }
    public Integer getValidePar() { return validePar; }
    public void setValidePar(Integer validePar) { this.validePar = validePar; }
    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }
    public String getCommentaireDecision() { return commentaireDecision; }
    public void setCommentaireDecision(String commentaireDecision) { this.commentaireDecision = commentaireDecision; }
    public String getMoisConcerne() { return moisConcerne; }
    public void setMoisConcerne(String moisConcerne) { this.moisConcerne = moisConcerne; }
}
