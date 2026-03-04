package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DemandeConges {

    private int id;
    private int idEmploye;
    // transient display fields (populated by JOIN in service, not persisted)
    private String nomEmploye;
    private String prenomEmploye;
    // transient computed fields
    private String priorityNote = ""; // priority system annotation
    private String scoreLabel = ""; // employee score badge
    private String holidayNote = ""; // public holiday warning
    private TypeConge typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motif;
    private StatutDemande statut;
    private LocalDateTime dateDemande;
    private Integer validePar;
    private LocalDateTime dateValidation;
    private String commentaireDecision;

    public DemandeConges() {
    }

    public DemandeConges(int idEmploye, TypeConge typeConge, LocalDate dateDebut,
            LocalDate dateFin, String motif) {
        this.idEmploye = idEmploye;
        this.typeConge = typeConge;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.motif = motif;
        this.statut = StatutDemande.EN_ATTENTE;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEmploye() {
        return idEmploye;
    }

    public void setIdEmploye(int idEmploye) {
        this.idEmploye = idEmploye;
    }

    public TypeConge getTypeConge() {
        return typeConge;
    }

    public void setTypeConge(TypeConge typeConge) {
        this.typeConge = typeConge;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public StatutDemande getStatut() {
        return statut;
    }

    public void setStatut(StatutDemande statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
    }

    public Integer getValidePar() {
        return validePar;
    }

    public void setValidePar(Integer validePar) {
        this.validePar = validePar;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }

    public String getCommentaireDecision() {
        return commentaireDecision;
    }

    public void setCommentaireDecision(String commentaireDecision) {
        this.commentaireDecision = commentaireDecision;
    }

    public String getNomEmploye() {
        return nomEmploye;
    }

    public void setNomEmploye(String nomEmploye) {
        this.nomEmploye = nomEmploye;
    }

    public String getPrenomEmploye() {
        return prenomEmploye;
    }

    public void setPrenomEmploye(String prenomEmploye) {
        this.prenomEmploye = prenomEmploye;
    }

    public String getPriorityNote() {
        return priorityNote;
    }

    public void setPriorityNote(String priorityNote) {
        this.priorityNote = priorityNote;
    }

    public String getScoreLabel() {
        return scoreLabel;
    }

    public void setScoreLabel(String scoreLabel) {
        this.scoreLabel = scoreLabel;
    }

    public String getHolidayNote() {
        return holidayNote;
    }

    public void setHolidayNote(String holidayNote) {
        this.holidayNote = holidayNote;
    }

    @Override
    public String toString() {
        return "DemandeConge{" +
                "id=" + id +
                ", idEmploye=" + idEmploye +
                ", typeConge=" + typeConge +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", statut=" + statut +
                '}';
    }
}
