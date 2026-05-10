package models;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private int cin;
    private String mail;
    private int tel;
    private String photoProfil;
    private String role;
    private String password;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public int getCin() { return cin; }
    public void setCin(int cin) { this.cin = cin; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public int getTel() { return tel; }
    public void setTel(int tel) { this.tel = tel; }
    public String getPhotoProfil() { return photoProfil; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNomComplet() {
        return (prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom);
    }
}
