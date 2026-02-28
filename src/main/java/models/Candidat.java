package models;

public class Candidat {
    private int idCandidat;
    private String nom;
    private String prenom;
    private int CIN;
    private int tel;
    private String adresse;
    private String email;
    private String cv;
    private String statut;

    public Candidat(int idCandidat, String nom, int CIN, String prenom, int tel, String adresse, String email, String cv) {
        this.idCandidat = idCandidat;
        this.nom = nom;
        this.CIN = CIN;
        this.prenom = prenom;
        this.tel = tel;
        this.adresse = adresse;
        this.email = email;
        this.cv = cv;
        this.statut = "Nouveau";
    }

    public Candidat(String nom, String prenom, int CIN, int tel, String adresse, String email, String cv) {
        this.nom = nom;
        this.prenom = prenom;
        this.CIN = CIN;
        this.tel = tel;
        this.adresse = adresse;
        this.email = email;
        this.cv = cv;
        this.statut = "Nouveau";
    }

    public Candidat() {
    }

    public int getIdCandidat() {
        return idCandidat;
    }

    public void setIdCandidat(int idCandidat) {
        this.idCandidat = idCandidat;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getTel() {
        return tel;
    }

    public void setTel(int tel) {
        this.tel = tel;
    }

    public int getCIN() {
        return CIN;
    }

    public void setCIN(int CIN) {
        this.CIN = CIN;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Candidat{" +
                "idCandidat=" + idCandidat +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", CIN=" + CIN +
                ", tel=" + tel +
                ", adresse='" + adresse + '\'' +
                ", email='" + email + '\'' +
                ", cv='" + cv + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
