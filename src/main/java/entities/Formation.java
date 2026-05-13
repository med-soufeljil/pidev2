package entities;

public class Formation {
    private int id_formation;
    private String titre;
    private String description;
    private int duree;
    private Niveau niveau;
    private Categorie categorie;
    private boolean certification;
    private String courseUrl;

    public Formation(int id_formation, String titre, String description, int duree, Categorie categorie, Niveau niveau, boolean certification, String courseUrl) {
        this.id_formation = id_formation;
        this.titre = titre;
        this.description = description;
        this.duree = duree;
        this.categorie = categorie;
        this.niveau = niveau;
        this.certification = certification;
        this.courseUrl = courseUrl;
    }

    public Formation(String titre, String description, Niveau niveau, int duree, Categorie categorie, boolean certification, String courseUrl) {
        this.titre = titre;
        this.description = description;
        this.niveau = niveau;
        this.duree = duree;
        this.categorie = categorie;
        this.certification = certification;
        this.courseUrl = courseUrl;
    }

    public Formation() {
    }

    public int getId_formation() {
        return id_formation;
    }

    public void setId_formation(int id_formation) {
        this.id_formation = id_formation;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public Niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(Niveau niveau) {
        this.niveau = niveau;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public boolean isCertification() {
        return certification;
    }

    public void setCertification(boolean certification) {
        this.certification = certification;
    }

    public String getCourseUrl() {
        return courseUrl;
    }

    public void setCourseUrl(String courseUrl) {
        this.courseUrl = courseUrl;
    }

    @Override
    public String toString() {
        return "Formation{" +
                "id_formation=" + id_formation +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", duree=" + duree +
                ", niveau=" + niveau +
                ", categorie=" + categorie +
                ", certification=" + certification +
                ", courseUrl='" + courseUrl + '\'' +
                '}';
    }
}
