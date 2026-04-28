package models;

public class Recrutement {
    private int idRec;
    private int idOffre;
    private int idCandidat;

    // Champs affichables
    private String nomOffre;
    private String nomCandidat;

    public Recrutement() {}

    public Recrutement(int idOffre, int idCandidat) {
        this.idOffre = idOffre;
        this.idCandidat = idCandidat;
    }

    public int getIdRec() { return idRec; }
    public void setIdRec(int idRec) { this.idRec = idRec; }

    public int getIdOffre() { return idOffre; }
    public void setIdOffre(int idOffre) { this.idOffre = idOffre; }

    public int getIdCandidat() { return idCandidat; }
    public void setIdCandidat(int idCandidat) { this.idCandidat = idCandidat; }

    public String getNomOffre() { return nomOffre; }
    public void setNomOffre(String nomOffre) { this.nomOffre = nomOffre; }

    public String getNomCandidat() { return nomCandidat; }
    public void setNomCandidat(String nomCandidat) { this.nomCandidat = nomCandidat; }
}
