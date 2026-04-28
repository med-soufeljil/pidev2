package models;

public class Offre {
    private int idOffre;
    private String nomOffre;
    private TypeOffre type;
    private String competences;
    private int salaire;

    public Offre(int idOffre, String nomOffre, TypeOffre  type, String competences, int salaire) {
        this.idOffre = idOffre;
        this.nomOffre = nomOffre;
        this.type = type;
        this.competences = competences;
        this.salaire = salaire;
    }

    public Offre(String nomOffre, TypeOffre  type, String competences, int salaire) {
        this.nomOffre = nomOffre;
        this.type = type;
        this.competences = competences;
        this.salaire = salaire;
    }

    public Offre() {
    }

    public int getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(int idOffre) {
        this.idOffre = idOffre;
    }

    public String getNomOffre() {
        return nomOffre;
    }

    public void setNomOffre(String nomOffre) {
        this.nomOffre = nomOffre;
    }

    public TypeOffre  getType() {
        return type;
    }

    public void setType(TypeOffre  type) {
        this.type = type;
    }

    public String getCompetences() {
        return competences;
    }

    public void setCompetences(String competences) {
        this.competences = competences;
    }

    public int getSalaire() {
        return salaire;
    }

    public void setSalaire(int salaire) {
        this.salaire = salaire;
    }

    @Override
    public String toString() {
        return "Offre{" +
                "idOffre=" + idOffre +
                ", nomOffre='" + nomOffre + '\'' +
                ", type='" + type + '\'' +
                ", competences='" + competences + '\'' +
                ", salaire=" + salaire +
                '}';
    }
}
