package models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandeTeletravail {

    private int id;
    private int idEmploye;
    // transient display fields (populated by JOIN in service, not persisted)
    private String nomEmploye;
    private String prenomEmploye;
    // transient abuse-detection flags (populated by AbuseDetectionService)
    private List<String> abuseFlags = new ArrayList<>();
    // transient score badge (populated by EmployeeScoreService)
    private String scoreLabel = "";
    // transient weather note (populated by WeatherService)
    private String weatherNote = "";

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private int nbJours;
    private String motif;

    private StatutTeletravail statut;

    private LocalDateTime dateDemande;

    private Integer validePar;
    private LocalDateTime dateValidation;
    private String commentaireDecision;

    private String moisConcerne; // "YYYY-MM"

    public DemandeTeletravail() {
    }

    // pour AJOUT
    public DemandeTeletravail(int idEmploye, LocalDate dateDebut, LocalDate dateFin,
            int nbJours, String motif, String moisConcerne) {
        this.idEmploye = idEmploye;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nbJours = nbJours;
        this.motif = motif;
        this.moisConcerne = moisConcerne;
        this.statut = StatutTeletravail.EN_ATTENTE;
    }

    // pour MODIF
    public DemandeTeletravail(int id, int idEmploye, LocalDate dateDebut, LocalDate dateFin,
            int nbJours, String motif, StatutTeletravail statut,
            Integer validePar, LocalDateTime dateValidation,
            String commentaireDecision, String moisConcerne) {
        this.id = id;
        this.idEmploye = idEmploye;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nbJours = nbJours;
        this.motif = motif;
        this.statut = statut;
        this.validePar = validePar;
        this.dateValidation = dateValidation;
        this.commentaireDecision = commentaireDecision;
        this.moisConcerne = moisConcerne;
    }

    // Getters/Setters
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

    public int getNbJours() {
        return nbJours;
    }

    public void setNbJours(int nbJours) {
        this.nbJours = nbJours;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public StatutTeletravail getStatut() {
        return statut;
    }

    public void setStatut(StatutTeletravail statut) {
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

    public String getMoisConcerne() {
        return moisConcerne;
    }

    public void setMoisConcerne(String moisConcerne) {
        this.moisConcerne = moisConcerne;
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

    // ─── Abuse flags ──────────────────────────────────────────────────────────
    public List<String> getAbuseFlags() {
        return abuseFlags;
    }

    public void setAbuseFlags(List<String> abuseFlags) {
        this.abuseFlags = abuseFlags;
    }

    /** Joins all abuse flags into a single string for display in the table cell. */
    public String getAbuseSummary() {
        return abuseFlags == null || abuseFlags.isEmpty() ? "" : String.join("\n", abuseFlags);
    }

    public String getScoreLabel() {
        return scoreLabel;
    }

    public void setScoreLabel(String scoreLabel) {
        this.scoreLabel = scoreLabel;
    }

    public String getWeatherNote() {
        return weatherNote;
    }

    public void setWeatherNote(String weatherNote) {
        this.weatherNote = weatherNote;
    }

    @Override
    public String toString() {
        return "DemandeTeletravail{" +
                "id=" + id +
                ", idEmploye=" + idEmploye +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", nbJours=" + nbJours +
                ", statut=" + statut +
                ", moisConcerne='" + moisConcerne + '\'' +
                '}';
    }
}
